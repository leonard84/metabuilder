/*
 *      Copyright 2008 the original author or authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * @version $Id$
 */
public class MetaObjectGraphBuilder extends ObjectGraphBuilder {

    /**
     * The {@link MetaBuilder} that owns this {@link MetaObjectGraphBuilder}.
     */
    private MetaBuilder metaBuilder;

    /**
     * The default schema.
     */
    private SchemaNode defaultSchema;

    /**
     * Keeps track of the current schema while descending into an object graph.
     */
    private LinkedList schemaStack;

    /**
     * Keeps track of which properties were set/unset
     */
    private LinkedList propertiesStack;

    /**
     * Default factory to use when no other can be resolved.
     */
    private Factory defaultFactory;

    /**
     * Constructs a {@link MetaObjectGraphBuilder}.
     *
     * @param metaBuilder the {@link MetaBuilder} providing the build context
     * @param defaultSchema
     * @param defaultFactory
     */
    public MetaObjectGraphBuilder(MetaBuilder metaBuilder, SchemaNode defaultSchema, Factory defaultFactory) {
        super();
        this.metaBuilder = metaBuilder;
        schemaStack = new LinkedList();
        propertiesStack = new LinkedList();
        this.defaultSchema = defaultSchema;
        this.defaultFactory = defaultFactory;

        setClassNameResolver(createClassNameResolver());
        setClassLoader(metaBuilder.getClassLoader());
        setIdentifierResolver(createIdentifierResolver());
    }

    public MetaBuilder getMetaBuilder() {
        return metaBuilder;
    }

    public void pushProperties(SchemaNode propertiesSchema) {
        List propertiesList = (List)propertiesSchema.value();
        Map propertiesMap = new HashMap();
        for(int i = 0; i < propertiesList.size(); i++) {
            SchemaNode propertySchema = (SchemaNode)propertiesList.get(i);
            propertiesMap.put(propertySchema.name(), propertySchema);
        }
        propertiesStack.push(propertiesMap);
    }

    public void pushProperties(Map properties) {
        propertiesStack.push(properties);
    }

    public Map popProperties() {
        return (Map)propertiesStack.pop();
    }

    public Map getCurrentProperties() {
        return (Map)propertiesStack.peek();
    }

    public void pushSchema(SchemaNode schema) {
        schemaStack.push(schema);
    }

    public SchemaNode popSchema() {
        return (SchemaNode)schemaStack.pop();
    }

    public SchemaNode getCurrentSchema() {
        return (SchemaNode)schemaStack.peek();
    }

    /**
     * Returns the schema referenced by name.
     *
     * @param schemaRef the schema reference
     * @return see above
     */
    protected SchemaNode resolveSchemaRef(Object schemaRef) {
        return schemaRef instanceof String
                ? metaBuilder.getSchema((String)schemaRef)
                : (SchemaNode)schemaRef;
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
    protected SchemaNode findSchema(SchemaNode parentSchema, String containerName, String name) {
        NodeList schemaNodesList = (NodeList)parentSchema.get(containerName);
        SchemaNode nodesSchema = schemaNodesList.size() > 0 ? (SchemaNode)schemaNodesList.get(0) : null;
        NodeList nodes = nodesSchema != null ? (NodeList)nodesSchema.get(name) : null;
        nodes = nodes != null && nodes.size() > 0 ? nodes : nodesSchema != null ? (NodeList)nodesSchema.get("%") : null;
        SchemaNode childSchema = nodes != null && nodes.size() > 0 ? (SchemaNode)nodes.get(0) : null;
        if(childSchema == null) {
            Object extendSchemaRef = parentSchema.attribute("schema");
            SchemaNode extendSchema = resolveSchemaRef(extendSchemaRef);
            if(extendSchema != null) childSchema = findSchema(extendSchema, containerName, name);
        }
        return childSchema;
    }

    protected Object findSchemaAttribute(SchemaNode parentSchema, String name) {
        Object attribute = parentSchema.attribute(name);
        if(attribute == null) {
            Object extendedSchemaRef = parentSchema.attribute("schema");
            SchemaNode extendedSchema = resolveSchemaRef(extendedSchemaRef);
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
        // MetaObjectGraphBuilder bascially works by matching name against a child node of the current schema.
        SchemaNode currentSchema = getCurrentSchema();
        SchemaNode childSchema = null;
        if(currentSchema == null) {
            // If there is no current schema, its because the builder is building the root node.
            currentSchema = metaBuilder.getSchema((String)name);
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
            childSchema = childSchema != null ? childSchema : findSchema(currentSchema, "properties", (String)name);

            childSchema = childSchema != null ? childSchema : findSchema(currentSchema, "collections", (String)name);
            if(childSchema == null) {
                NodeList nodeList = (NodeList)currentSchema.get((String)name);
                if(nodeList.size() == 0) nodeList = (NodeList)currentSchema.get("%");
                if(nodeList.size() > 0) {
                    childSchema = (SchemaNode)nodeList.get(0);
                }
            }
        }
        if(childSchema == null) {
            throw MetaBuilder.createSchemaNotFoundException((String)name);
        }

        pushSchema(childSchema);
        // store the set of mergedProperties for later checking of defaults and missing req properties
        SchemaNode mergedProperties = getMergedProperties(childSchema);
        pushProperties(mergedProperties);

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

    public void setVariable(String name, Object value) {
        setVariable(getCurrent(), getCurrentSchema(), name, value);
    }

    public void setVariable(Object node, SchemaNode schema, String name, Object value) {
        SchemaNode mergedProperties = getMergedProperties(schema);
        SchemaNode propertySchema = (SchemaNode)mergedProperties.firstChild(name);

        // remove the entry from the currentProperties so we won't try to set a default or check req later.
        Map currentProperties = getCurrentProperties();
        currentProperties.remove(name);

        if(propertySchema == null) {
            // Check for the possibility of a wild card indicated by a schema with name = %
            propertySchema = findSchema(schema, "properties", "%");
            if(propertySchema == null) {
                throw MetaBuilder.createPropertyException(name, "property unkown");
            }
        }

        if(value != null) {
            Comparable min = (Comparable)propertySchema.attribute("min");
            Comparable minMaxValComp = null;
            if(min != null && min.compareTo(minMaxValComp = getMinMaxValComp(name, value)) > 0) {
                throw MetaBuilder.createPropertyException(name, "min check failed");
            }
            Comparable max = (Comparable)propertySchema.attribute("max");
            if(max != null && max.compareTo(minMaxValComp) < 0) {
                throw MetaBuilder.createPropertyException(name, "max check failed");
            }
        }
        checkPropertyValue(propertySchema, value);

        setProperty(node, value, propertySchema);
    }

    /**
     * Overrides the default implementation to sync the schema with the current node.
     *
     * @param parent
     * @param node
     */
    protected void nodeCompleted(Object parent, Object node) {
        SchemaNode childSchema = popSchema();

        // go through the unset properties and set defaults or check if req
        Map unsetProperties = new HashMap(getCurrentProperties()); // use copy to avoid ConcurrentModificationException
        for(Iterator properties = unsetProperties.entrySet().iterator(); properties.hasNext();) {
            Map.Entry property = (Map.Entry)properties.next();
            SchemaNode propertySchema = (SchemaNode)property.getValue();
            Map attributes = propertySchema.attributes();
            if(attributes.containsKey("def")) {
                Object value = attributes.get("def");
                if(value instanceof Closure) {
                    value = ((Closure)value).call();
                }
                setVariable(node, childSchema, (String)propertySchema.name(), value);
            }
            else {
                Boolean req = (Boolean)attributes.get("req");
                if(req != null && req) {
                    throw MetaBuilder.createPropertyException((String)propertySchema.name(), "property required");
                }
            }
        }
        popProperties();

        // remove the child schema from the parents list of unset properties
        // in case it was defined as a node and not as an attribute
        Map parentProperties = getCurrentProperties();
        if(parentProperties != null) {
            parentProperties.remove(childSchema.name());
        }

        super.nodeCompleted(parent, node);
    }

    /**
     * Override to modify the {@link groovy.util.ObjectGraphBuilder.IdentifierResolver} behavior.
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
     * Overrides the default implementation in {@link ClassNameResolver} in order to support
     * resolution of the class name using the <code>factory</code> schema attribute.
     */
    public class FactoryClassNameResolver implements ObjectGraphBuilder.ClassNameResolver {
        public String resolveClassname(String className) {
            SchemaNode schema = getCurrentSchema();
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
     * @return see above
     */
    protected Factory resolveFactory(Object name, Map attributes, Object value) {
        // Need to have this implementation act first before super,
        // but FactoryBuilderSupport.resolveFactory() sets the CHILD_BUILDER context
        // So it must be done directly here.  Not using CHILD_BUILDER for Groovy 1.5 compatibility.
        getContext().put("_CHILD_BUILDER_"/* CHILD_BUILDER */, this);

        SchemaNode schema = getCurrentSchema();
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
    }

    protected SchemaNode getMergedProperties(SchemaNode schema) {
        SchemaNode mergedProperties = (SchemaNode)schema.firstChild("mergedProperties");
        if(mergedProperties == null) {
            SchemaNode properties = (SchemaNode)schema.firstChild("properties");
            SchemaNode superSchema = (SchemaNode)resolveSchemaRef(schema.attribute("schema"));
            mergedProperties = new SchemaNode(schema, "mergedProperties");
            if(superSchema != null) {
                // add all of the super schema's properties first
                SchemaNode superMergedProperties = getMergedProperties(superSchema);
                for(Iterator children = superMergedProperties.children().iterator(); children.hasNext();) {
                    SchemaNode property = (SchemaNode)children.next();
                    new SchemaNode(mergedProperties, property.name(), property.attributes());
                }
            }
            if(properties != null) {
                for(Iterator children = properties.children().iterator(); children.hasNext();) {
                    SchemaNode property = (SchemaNode)children.next();
                    SchemaNode mergedProperty = (SchemaNode)mergedProperties.firstChild((String)property.name());
                    if(mergedProperty == null) {
                        // simple copy
                        new SchemaNode(mergedProperties, property.name(), property.attributes());
                    }
                    else {
                        // overwrites previous attribute, if present
                        mergedProperty.attributes().putAll(property.attributes());
                    }
                }
            }
        }
        return mergedProperties;
    }

    /**
     * Sets properties on the node based on the current schema.
     *
     * @param node
     * @param attributesMap
     *
     * @see #setProperty
     */
    protected void setNodeAttributes(Object node, Map attributesMap) {
        for(Iterator attributes = attributesMap.entrySet().iterator(); attributes.hasNext();) {
            Map.Entry attribute = (Map.Entry)attributes.next();
            setVariable(node, getCurrentSchema(), (String)attribute.getKey(), attribute.getValue());
        }
    }

    /**
     * Check <code>value</code> against a <code>propertySchema</code>'s <code>check</code> attribute, if it exists.
     *
     * @param propertySchema see above
     * @param val the value
     */
    protected void checkPropertyValue(SchemaNode propertySchema, Object val) {
        if(propertySchema.attributes().containsKey("check") == false) return;
        Object check = propertySchema.attribute("check");
        boolean b = true;
        try {
            b =  ScriptBytecodeAdapter.isCase(val, check);
        }
        catch(Throwable t) {
            throw MetaBuilder.createPropertyException((String)propertySchema.name(), t);
        }
        if(!b) {
            throw MetaBuilder.createPropertyException((String)propertySchema.name(), "value invalid");
        }
    }

    /**
     * Returns a {@link Comparable} object that can be used with the <code>min</code> and <code>max</code> constraints.
     *
     * @param name the property name (used to report errors)
     * @param val the property value
     * @return see above
     */
    protected Comparable getMinMaxValComp(String name, Object val) {
        if(val instanceof String) {
            return ((String)val).length();
        }
        else if(val instanceof Collection) {
            return ((Collection)val).size();
        }
        else if(val instanceof Map) {
            return ((Map)val).size();
        }
        else if(val instanceof Comparable) {
            return (Comparable)val;
        }
        else {
            throw MetaBuilder.createPropertyException(name, "value is not comparable");
        }
    }

    /**
     * Sets a node's property value, allowing for property renaming.  The property name is taken from the property
     * schema's <code>property</code> attribute, if any, else the property node name is used by default.
     *
     * @param node           the current node
     * @param value          the value of the property
     * @param propertySchema       the property's property attribute or name
     */
    protected void setProperty(Object node, Object value, SchemaNode propertySchema) {
        String propertyName = null;
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
        else {
            propertyName = (String)propertySchema.name();
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