package groovytools.builder;

import groovy.lang.*;
import groovy.util.*;
import org.codehaus.groovy.runtime.*;

import java.util.*;

/**
 * This class is the workhorse behind {@link MetaBuilder}.  It is responsible for building object hierarchies according
 * to the schemas given to it.  It is not intended to be used independently of {@link MetaBuilder}.
 *
 * @author didge
 * @version $REV$
 */
public class MetaObjectGraphBuilder extends ObjectGraphBuilder {

    /**
     * The {@link MetaBuilder} that owns this {@link MetaObjectGraphBuilder}.
     */
    private MetaBuilder metaBuilder;

    /**
     * The default schema.
     */
    private Node defaultSchema;

    /**
     * Keeps track of the current schema while descending into an object graph.
     */
    private LinkedList schemaStack;

    /**
     * Default factory to use when no other can be resolved.
     */
    private Factory defaultFactory;

    /**
     * Constructs a {@link MetaObjectGraphBuilder}.
     *
     * @param metaBuilder the {@link MetaBuilder} providing the build context
     */
    public MetaObjectGraphBuilder(MetaBuilder metaBuilder, Node defaultSchema, Factory defaultFactory) {
        super();
        this.metaBuilder = metaBuilder;
        schemaStack = new LinkedList();
        this.defaultSchema = defaultSchema;
        this.defaultFactory = defaultFactory;

        setClassNameResolver(createClassNameResolver());
        setClassLoader(metaBuilder.getClassLoader());
        setIdentifierResolver(createIdentifierResolver());
    }

    public MetaBuilder getMetaBuilder() {
        return metaBuilder;
    }

    protected void pushSchema(Node schema) {
        schemaStack.push(schema);
    }

    protected Node popSchema() {
        return (Node)schemaStack.pop();
    }

    protected Node getCurrentSchema() {
        return (Node)schemaStack.peek();
    }

    /**
     * Returns the schema referenced by name.
     *
     * @param schemaRef the schema reference
     * @return see above
     */
    protected Node resolveSchemaRef(Object schemaRef) {
        return schemaRef instanceof String
                ? (Node)metaBuilder.getSchema((String)schemaRef)
                : (Node)schemaRef;
    }

    /**
     * Finds and returns a child schema with the given name, in the specified container or null if not found.  This
     * method will also search any super-schemas specified with the 'schema' attribute.
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
        Node childSchema = nodes != null && nodes.size() > 0 ? (Node)nodes.get(0) : null;
        if(childSchema == null) {
            Object extendSchemaRef = parentSchema.attribute("schema");
            Node extendSchema = resolveSchemaRef(extendSchemaRef);
            if(extendSchema != null) childSchema = findSchema(extendSchema, containerName, name);
        }
        return childSchema;
    }

    protected Object findSchemaAttribute(Node parentSchema, String name) {
        Object attribute = parentSchema.attribute(name);
        if(attribute == null) {
            Object extendedSchemaRef = parentSchema.attribute("schema");
            Node extendedSchema = resolveSchemaRef(extendedSchemaRef);
            if(extendedSchema != null) attribute = findSchemaAttribute(extendedSchema, name);
        }
        return attribute;
    }

    /**
     * Overrides in order to construct nodes based on the current schema definition.
     *
     * @param name       the name of the node
     * @param attributes optional attributes of the current node
     * @param value      optional value of the current node
     * @return a node
     */
    protected Object createNode(Object name, Map attributes, Object value) {
        // MetaObjectGraphBuilder bascially works by matching name against a child of the current schema.
        Node currentSchema = getCurrentSchema();
        Node childSchema = null;
        if(currentSchema == null) {
            // If there is no current schema, its because the builder is building the root node.
            currentSchema = (Node)metaBuilder.getSchema((String)name);
        }
        if(currentSchema == null) {
            // If we can't find a schema using the root node's name, try using the default schema
            childSchema = defaultSchema;
        }
        else if(getCurrent() == null) {
            // No current node exists, see if the  schema supports any name
            String rootName = (String)currentSchema.name();
            if(!rootName.equals(name) && !rootName.equals("%")) {
                throw new IllegalArgumentException((String)name);
            }
            childSchema = currentSchema;
        }
        else {
            // We have a current schema
            Node propertySchema = findSchema(currentSchema, "properties", (String)name);
            // supports setting a property not as an attribute, but like propname(value)
            // todo - didge: req:true is checked in setNodeAttributes(), so any properties set this way can't be required proprerties (at least not yet)
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
            throw MetaBuilder.createSchemaNotFoundException((String)name);
        }

        pushSchema(childSchema);

        Object node = null;
        try {
            node = super.createNode(name, attributes, value);
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
            // will only be null if defining a top level schema
            metaBuilder.addSchema((String)name, node);
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
        popSchema();
        super.nodeCompleted(parent, node);
    }

    /**
     * Override to modify the {@link IdentifierResolver} behavior.
     *
     * @return see above
     */
    protected IdentifierResolver createIdentifierResolver() {
        // id is a pretty common property name, so rename it
        return new ObjectGraphBuilder.IdentifierResolver() {
            public String getIdentifierFor(String nodeName) {
                return "metaId";
            }
        };
    }

    /**
     * Override to modify the {@link ClassNameResolver} behavior.
     *
     * @return see above
     */
    protected ClassNameResolver createClassNameResolver() {
        return new MetaObjectGraphBuilder.FactoryClassNameResolver();
    }

    /**
     * Overrides the default implementation in {@link ObjectGraphBuilder.ClassNameResolver} in order to support
     * resolution of the class name using the <code>factory</code> schema attribute.
     */
    public class FactoryClassNameResolver implements ObjectGraphBuilder.ClassNameResolver {
        public String resolveClassname(String className) {
            Node schema = getCurrentSchema();
            Object factory = findSchemaAttribute(schema, "factory");
            if(factory instanceof String)
                return (String)factory;
            else if(factory instanceof Class) {
                return ((Class)factory).getName();  // kind of silly...
            }
            throw MetaBuilder.createClassNameNotFoundException(className);
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
        Node schema = getCurrentSchema();
        if(schema instanceof Factory) {
            return (Factory)schema;
        }
        Object factory = findSchemaAttribute(schema, "factory");
        if(factory != null && factory instanceof Factory) {
            return (Factory)factory;
        }
        if(factory instanceof Closure) {
            // Is it cool to wrap the closure and replace it?
            // It does save having to keep wrapping the closure everytime, but is there a downside?
            ClosureFactoryAdapter closureFactoryAdapter = new ClosureFactoryAdapter((Closure)factory);
            schema.attributes().put("factory", closureFactoryAdapter);
            return closureFactoryAdapter;
        }
        if(factory instanceof String || factory instanceof Class) {
            return super.resolveFactory(name, attributes, value);
        }
        return defaultFactory;
        //throw MetaBuilder.createFactoryException((String)name, " unsupported factory");
    }

    /**
     * Overrides the default implementation to support property name changes, default and required values and checking.
     *
     * @param node
     * @param attributes
     * @see #setProperty(Object, Object, String, Node)
     */
    protected void setNodeAttributes(Object node, Map attributes) {
        Node schema = getCurrentSchema();

        // skip setting properties on Nodes because they
        // had their properties set already
        //if(node instanceof Node) return;

        HashMap attCopy = new HashMap(attributes);

        NodeList nodeList = (NodeList)schema.get("properties");

        while(nodeList != null) {
            if(nodeList.size() > 0) {
                SchemaNode propertySchema = (SchemaNode)nodeList.get(0);

                List propertyList = propertySchema.children();
                for(int i = 0; i < propertyList.size(); i++) {
                    Node property = (Node)propertyList.get(i);
                    String name = (String)property.name();
                    Object def = property.attribute("def");
                    if(attCopy.containsKey(name) || def != null) {
                        Object val = attCopy.remove(name);
                        if(val == null && def != null) {
                            val = def;
                        }
                        Closure check = (Closure)property.attribute("check");
                        if(check != null) {
                            Boolean b = (Boolean)check.call(val);
                            if(b != null && !b.booleanValue()) {
                                throw MetaBuilder.createPropertyException(name, "value invalid");
                            }
                        }
                        setProperty(node, val, name, property);
                    }
                    else {
                        Boolean req = (Boolean)property.attribute("req");
                        if(req != null && req.booleanValue()) {
                            throw MetaBuilder.createPropertyException(name, "required property missing");
                        }
                    }
                }
            }
            // check any 'super' schemas for additional properties
            Node superSchema = (Node)schema.attribute("schema");
            if(superSchema != null) {
                schema = superSchema;
                nodeList = (NodeList)superSchema.get("properties");
            }
            else {
                nodeList = null;
            }
        }
        Node wildCardNode = null;
        for(Iterator leftovers = attCopy.entrySet().iterator(); leftovers.hasNext();) {
            Map.Entry leftover = (Map.Entry)leftovers.next();
            String name = (String)leftover.getKey();
            Object val = leftover.getValue();
            if(wildCardNode == null) {
                wildCardNode = findSchema(schema, "properties", name);
                if(wildCardNode == null) {
                    throw MetaBuilder.createPropertyException(name, "property unkown");
                }
            }
            setProperty(node, val, name, wildCardNode);
        }
    }

    /**
     * Sets a node's property value, allowing for property renaming.  The property name is taken from the property
     * schema's <code>property</code> attribute, if any, else the property node name is used by default.
     *
     * @param node           the current node
     * @param value          the value of the property
     * @param propertyName   the name of the property as it appears in the script
     * @param propertySchema the property's schema
     */
    protected void setProperty(Object node, Object value, String propertyName, Node propertySchema) {
        Object propertyAttr = propertySchema.attribute("property");
        if(propertyAttr != null) {
            if(propertyAttr instanceof Closure) {
                Closure propertyClosure = (Closure)propertyAttr;
                propertyClosure.call(new Object[]{node, value});
                return;
            }
            else if(propertyAttr instanceof String) {
                propertyName = (String)propertyAttr;
            }
            else {
                throw MetaBuilder.createPropertyException(propertyName, "'property' attribute of schema does not specify a string or closure.");
            }
        }
        // todo - didge: Rethink special case for SchemaNodes.  An alternative would be to mess around with its MetaClass
        if(node instanceof SchemaNode) {
            SchemaNode schemaNode = (SchemaNode)node;
            schemaNode.attributes().put(propertyName, value);
        }
        else {
            InvokerHelper.setProperty(node, propertyName, value);
        }
    }

    /**
     * Overrides the default implementation in order set the parent using the current schema definition.
     *
     * @param parent
     * @param child
     */
    protected void setParent(Object parent, Object child) {
        FactoryBuilderSupport proxyBuilder = getProxyBuilder();
        proxyBuilder.getCurrentFactory().setParent(this, parent, child);
        Factory parentFactory = proxyBuilder.getParentFactory();
        if(child instanceof CollectionSchemaNode == false && parentFactory != null) {
            parentFactory.setChild(this, parent, child);
        }
    }
}