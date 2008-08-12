package groovytools.builder;

import groovy.util.*;
import groovy.lang.*;

import java.util.*;

import org.codehaus.groovy.runtime.*;

/**
 * This class is the workhorse behind {@link MetaBuilder}.  It is responsible for building object hierarchies according
 * to the schemas given to it.  It is not intended to be used independently of {@link MetaBuilder}.
 *
 * @author didge
 */
public class MetaBuilderBuilder extends ObjectGraphBuilder {

    /**
     * Map of stored schemas for reuse.
     */
    protected Map schemas;

    /**
     * Default schema for use when no other schema has been specified.
     */
    protected Node defaultSchema;

    /**
     * Keeps track of the current schema while descending into an object graph.
     */
    protected LinkedList schemaStack;

    /**
     * Allows {@link MetaBuilderBuilder} to delegate to another builder for node creation.
     */
    protected BuilderSuppportProxy builderSuppportProxy;

    /**
     * Constructs a {@link MetaBuilderBuilder}.
     *
     * @param schemas       an initial set of schemas, may be empty, but must not be null
     * @param defaultSchema the default schema
     * @param classLoader   the {@link ClassLoader} to use
     */
    public MetaBuilderBuilder(Map schemas, Node defaultSchema, ClassLoader classLoader) {
        super();
        this.defaultSchema = defaultSchema;
        this.schemas = schemas;
        schemaStack = new LinkedList();
        setClassNameResolver(new FactoryClassNameResolver());
        setClassLoader(classLoader);
    }

    protected Node getDefaultSchema() {
        return defaultSchema;
    }

    public Object getBuilderSupportProxy() {
        return builderSuppportProxy;
    }

    public void setBuilderSupportProxy(BuilderSuppportProxy builderSuppportProxy) {
        this.builderSuppportProxy = builderSuppportProxy;
    }

    /**
     * Returns the schema referenced by name.
     *
     * @param schemaRef the schema reference
     * @return see above
     */
    protected Node resolveSchemaRef(Object schemaRef) {
        return schemaRef instanceof String
                ? (Node)schemas.get(schemaRef)
                : (Node)schemaRef;
    }

    /**
     * Finds and returns a child schema with the given name, in the specified container or null if not found.
     *
     * @param parentSchema  the parent schema
     * @param containerName the name of a container of child schemas, e.g. collections, properties
     * @param name          the name of the child schema
     * @return see above
     */
    protected Node findSchema(Node parentSchema, String containerName, String name) {
        NodeList schemaNodesList = (NodeList)parentSchema.get(containerName);
        Node nodesSchema = schemaNodesList.size() > 0 ? (Node)schemaNodesList.get(0) : null;
        NodeList nodes = nodesSchema != null ? (NodeList)nodesSchema.get(name) : null;
        nodes = nodes != null && nodes.size() > 0 ? nodes : nodesSchema != null ? (NodeList)nodesSchema.get("%") : null;
        Node childSchema = nodes != null && nodes.size() > 0 ? (Node)nodes.get(0) : (Node)parentSchema.attribute("schema");
        if(childSchema == null) {
            Object extendSchemaRef = parentSchema.attribute("extend");
            Node extendSchema = resolveSchemaRef(extendSchemaRef);
            if(extendSchema != null) childSchema = findSchema(extendSchema, containerName, name);
        }
        return childSchema;
    }

    /**
     * Checks the current set of attributes against the set of properties defined in the current schema's 'properties' node, if any.
     * <p>
     * Checking is done as follows:
     * For each property, attempt to retrieve the corresponding attribute's value.
     *  <ol>
     *  <li>If the attribute has not been specified and a default value has been provided, set the value of the
     * attribute to the default value.</li>
     *  <li>If the attribute has not been specified and no default value has been provided, and the property is required,
     * throw a {@link PropertyException}</li>
     *  <li>If the attribute has been specified and a check provided, check the attribute's value.  If the check returns
     * false, throw a {@link PropertyException}</li>
     * </ol>
     * Further, if the current schema does not include the special property '%' and any attribute is specified that not
     * corresponding to a defined proeprty, then a {@link PropertyException} is thrown
     *
     * @param attributes
     */
    protected void checkAttributes(Map attributes) {
        HashMap attCopy = new HashMap(attributes);

        Node schema = (Node)schemaStack.peek();

        NodeList nodeList = (NodeList)schema.get("properties");
        // if there are no properties, skip right down to checking for unkown properties
        boolean hasWildCard = ((NodeList)schema.get("%")).size() > 0;

        if(nodeList.size() != 0) {
            SchemaNode propertySchema = (SchemaNode)nodeList.get(0);
            hasWildCard = propertySchema.get("%") != null;

            List propertyList = propertySchema.children();
            for(int i = 0; i < propertyList.size(); i++) {
                Node property = (Node)propertyList.get(i);
                String name = (String)property.name();
                Boolean req = (Boolean)property.attribute("req");
                Object def = property.attribute("def");
                Object val = attCopy.remove(name);
                if(val == null && def != null) {
                    val = def;
                    attributes.put(name, val);
                }
                if(val == null && req != null && req.booleanValue()) {
                    throw createPropertyException(name, "required property missing");
                }
                Closure check = (Closure)property.attribute("check");
                if(check != null) {
                    Boolean b = (Boolean)check.call(val);
                    if(b != null && !b.booleanValue()) {
                        throw createPropertyException(name, "value invalid");
                    }
                }
            }
        }
        if(!hasWildCard) {
            for(Iterator leftovers = attCopy.keySet().iterator(); leftovers.hasNext();) {
                String leftover = (String)leftovers.next();
                throw createPropertyException(leftover, "property unkown");
            }
        }
    }

    /**
     * Overrides the default implementation in order to construct nodes based on the current schema definition.
     *
     * @param name       the name of the node
     * @param attributes optional attributes of the current node
     * @param value      optional value of the current node
     * @return a node
     */
    protected Object createNode(Object name, Map attributes, Object value) {
        Node currentSchema = (Node)schemaStack.peek();
        Node childSchema = null;
        if(currentSchema == null) {
            currentSchema = (Node)schemas.get(name);
        }
        if(currentSchema == null) {
            childSchema = getDefaultSchema();
        }
        else if(getCurrent() == null) {
            String rootName = (String)currentSchema.name();
            if(!rootName.equals(name) && !rootName.equals("%")) {
                throw new IllegalArgumentException((String)name);
            }
            childSchema = currentSchema;
        }
        else {
            Node propertySchema = findSchema(currentSchema, "properties", (String)name);
            // supports setting a property not as an attribute, but like propname(value)
            if(propertySchema != null) {
                // simply set simple values, otherwise, build the property value like any other node
                if(attributes == Collections.EMPTY_MAP && value != null) {
                    Object node = getCurrent();
                    setProperty(node, value, (String)name, propertySchema);
                    return null;
                }
                else {
                    childSchema = propertySchema;
                }
            }
            childSchema = childSchema != null ? childSchema : findSchema(currentSchema, "collections", (String)name);
            if(childSchema == null) {
                NodeList nodeList = (NodeList)currentSchema.get((String)name);
                if(nodeList.size() == 0) nodeList = (NodeList)currentSchema.get("%");
                if(nodeList.size() > 0) {
                    childSchema = (Node)nodeList.get(0);
                    Object schemaRef = childSchema.attribute("schema");
                    if(schemaRef != null) childSchema = resolveSchemaRef(schemaRef);
                }
            }
        }
        if(childSchema == null) {
            throw createSchemaNotFoundException((String)name);
        }
        schemaStack.push(childSchema);
        checkAttributes(attributes);
        Object node = null;
        try {
            if(builderSuppportProxy == null) {
                node = super.createNode(name, attributes, value);
            }
            else {
                node = builderSuppportProxy.createNode(name, attributes, value);
            }
        }
        catch(RuntimeException e) {
            // If FactoryBuilderSupport throws an exception caused by
            // a MetaBuilder, simply unwrap and rethrow the cause.
            // This will be a lot easier for the user to understand
            // than the FactoryBuilderSupport exception.
            Throwable t = e.getCause();
            if(t instanceof MetaBuilderException) {
                throw (MetaBuilderException)t;
            }
            else throw e;
        }
        if(currentSchema == null) {
            // only null if defining a top level schema
            schemas.put(name, node);  // it would be nice not to have to cast to Node() to get the name...
        }

        return node;
    }

    /**
     * Overrides the default implementation to sync the schema with the current node.
     *
     * @param parent
     * @param node
     */
    protected void nodeCompleted(Object parent, Object node) {
        schemaStack.pop();
        if(builderSuppportProxy != null) {
            builderSuppportProxy.nodeCompleted(parent, node);
        }
        else {
            super.nodeCompleted(parent, node);
        }
    }

    /**
     * Overrides the default implementation to support resolution of the factory name in a schema attribute.
     */
    public class FactoryClassNameResolver implements ClassNameResolver {
        public String resolveClassname(String className) {
            Node schema = (Node)schemaStack.peek();
            Object factory = schema.attribute("factory");
            if(factory instanceof String)
                return (String)factory;
            else if(factory instanceof Class) {
                return ((Class)factory).getName();  // kind of silly...
            }
            throw createClassNameNotFoundException(className);
        }
    }

    /**
     * Overrides the default implementation to support lookup of a factory defined in a schema attribute.  The
     * {@link Factory} is resolved as follows:
     * <ol>
     * <li>If the schema itself is an instance of {@link Factory}, then it is returned.</li>
     * <li>If the schema defines an attribute value called <code>factory</code> which is an instance of {@link Factory},
     * then the attribute value is returned.</li>
     * <li>If the schema defines an attribute value called <code>factory</code> which is an instance of {@link Closure},
     * then the Closure will be returned, wrapped by {@link ClosureFactoryAdapter}</li>
     * <li>If the schema defines an attribute value called <code>factory</code> which is an instance of {@link String}
     * or {@link Class}, then the corresponding class will be instantiated and returned.</li>
     * </ol>
     *
     * @param name
     * @param attributes
     * @param value
     * @return
     */
    protected Factory resolveFactory(Object name, Map attributes, Object value) {
        Node schema = (Node)schemaStack.peek();
        if(schema instanceof Factory) {
            return (Factory)schema;
        }
        Object factory = schema.attribute("factory");
        if(factory != null && factory instanceof Factory) {
            return (Factory)factory;
        }
        if(factory instanceof Closure) {
            // Is it cool to wrap the closure and replace it?
            // It does save having to keep wrapping the closure everytime, but is there a downside?
            ClosureFactoryAdapter closureFactoryAdapter = new ClosureFactoryAdapter((Closure)factory);
            attributes.put("factory", closureFactoryAdapter);
            return closureFactoryAdapter;
        }
        if(factory instanceof String || factory instanceof Class) {
            return super.resolveFactory(name, attributes, value);
        }
        throw createFactoryException((String)name, " unsupported factory");
    }

    /**
     * Overrides the default implementation to support property name changes defined in the schema.
     *
     * @param node
     * @param attributes
     * @see #setProperty(Object, Object, String, Node)
     */
    protected void setNodeAttributes(Object node, Map attributes) {
        Node schema = (Node)schemaStack.peek();

        // skip setting properties on Nodes because they
        // had their properties set already
        if(node instanceof Node) return;

        for(Iterator iter = attributes.entrySet()
                .iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            Object value = entry.getValue();
            String propertyNodeName = entry.getKey().toString();
            Node propertySchema = findSchema(schema, "properties", propertyNodeName);
            if(propertySchema == null) {
                throw createPropertyException(propertyNodeName, "property unknown");
            }
            setProperty(node, value, propertyNodeName, propertySchema);
        }
    }

    /**
     * Sets a node's property value, allowing for property renaming.  The property name is taken from the property
     * schema's <code>property</code> attribute, if any, else the property node name is used by default.
     *
     * @param node             the current node
     * @param value            the value of the property
     * @param propertyNodeName the name of the property as it appears in the script
     * @param propertySchema   the property's schema
     */
    protected void setProperty(Object node, Object value, String propertyNodeName, Node propertySchema) {
        String propertyName = (String)propertySchema.attribute("property");
        if(propertyName == null) {
            propertyName = propertyNodeName;
        }
        InvokerHelper.setProperty(node, propertyName, value);
    }

    protected RuntimeException createPropertyException(String name, String error) {
        StringBuilder message = new StringBuilder("Property '").append(name).append("': ").append(error);
        return new PropertyException(message.toString());

    }

    protected RuntimeException createFactoryException(String name, String error) {
        StringBuilder message = new StringBuilder("'").append(name).append("' factory: ").append(error);
        return new FactoryException(message.toString());

    }

    protected RuntimeException createSchemaNotFoundException(String name) {
        StringBuilder message = new StringBuilder(name);
        return new SchemaNotFoundException(message.toString());

    }

    protected RuntimeException createClassNameNotFoundException(String name) {
        StringBuilder message = new StringBuilder(name);
        return new ClassNameNotFoundException(message.toString());
    }

    /**
     * Overrides the default implementation in order set the parent using the current schema definition.
     *
     * @param parent
     * @param child
     */
    protected void setParent(Object parent, Object child) {
        if(builderSuppportProxy != null) {
            builderSuppportProxy.setParent(parent, child);
        }
        else {
            FactoryBuilderSupport proxyBuilder = getProxyBuilder();
            proxyBuilder.getCurrentFactory().setParent(this, parent, child);
            Factory parentFactory = proxyBuilder.getParentFactory();
            if(child instanceof CollectionSchemaNode == false && parentFactory != null) {
                parentFactory.setChild(this, parent, child);
            }
        }
    }

    /**
     * Used in a couple of places to provide a {@link Factory} when delegating to another builder to satisfy
     * {@link FactoryBuilderSupport}.
     */
    protected static final Factory builderFactoryAdapter = new Factory() {
        public boolean isLeaf() {
            return false;
        }

        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            return null;
        }

        public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
            return false;
        }

        public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        }

        public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        }

        public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        }
    };

    public Factory getCurrentFactory() {
        if(builderSuppportProxy != null) {
            return builderFactoryAdapter;
        }
        else {
            return super.getCurrentFactory();
        }
    }
}