package groovytools.builder;

import groovy.lang.*;
import groovy.util.*;

import java.util.*;

/**
 * {@link MetaBuilder} is a builder that uses metadata you specify in order to more conveniently and correctly
 * build object hierarchies.
 *
 * @see ObjectGraphBuilder
 *
 * @author didge
 * @version $REV$
 */
public class MetaBuilder extends GroovyObjectSupport {
    private Map schemas;
    private Node defaultSchema;
    private ClassLoader classLoader;
    private Factory defaultBuildNodeFactory;
    private Factory defaultDefineNodeFactory;

    /**
     * Constructs a <code>MetaBuilder</code> with the default meta schema, node factory and class loader.
     *
     * @see #createDefaultMetaSchema()
     */
    public MetaBuilder() {
        this(null, null);
        this.classLoader = getClass().getClassLoader();
        this.defaultSchema = createDefaultMetaSchema();
    }

    /**
     * Constructs a <code>MetaBuilder</code> with the default meta schema, node factory and specified class loader.
     *
     * @param classLoader
     * @see #createDefaultMetaSchema()
     */
    public MetaBuilder(ClassLoader classLoader) {
        this(null, classLoader);
        this.defaultSchema = createDefaultMetaSchema();
    }

    /**
     * Constructs a MetaBuilder with the given default meta schema
     *
     * @param defaultSchema the default schema
     */
    public MetaBuilder(Node defaultSchema, ClassLoader classLoader) {
        schemas = new HashMap();
        this.defaultSchema = defaultSchema;
        this.defaultBuildNodeFactory = createDefaultBuildNodeFactory();
        this.defaultDefineNodeFactory = createDefaultDefineNodeFactory();
        this.classLoader = classLoader;
    }

    /**
     * Subclasses may override this to specify an alternative factory for defining schema.
     *
     * @return see above
     */
    protected Factory createDefaultDefineNodeFactory() {
        return new DefaultDefineSchemaNodeFactory();
    }

    /**
     * Subclasses may override this to specify an alternative factory for building object
     *
     * @return see above
     */
    protected Factory createDefaultBuildNodeFactory() {
        return new DefaultBuildSchemaNodeFactory();
    }

    /**
     * The default implementantion returns the MetaBuilder meta schema:
     * <pre>
     * schemaNodeFactory = new MetaBuilder.SchemaNodeFactory()
     * collectionSchemaNodeFactory = new MetaBuilder.CollectionSchemaNodeFactory()
     * <p/>
     * def metaSchema = '%'(factory: schemaNodeFactory) {
     *     properties() {
     *         schema()
     *         factory()
     *     }
     *     collections() {
     *         collections(factory: schemaNodeFactory) {
     *             '%'(factory: collectionSchemaNodeFactory) {
     *                 properties(factory: schemaNodeFactory) {
     *                     collection()
     *                     add()
     *                     key()
     *                 }
     *                 '%'(shema: metaSchema)
     *             }
     *             properties(factory: schemaNodeFactory) {
     *                 '%'(schema: metaSchema)
     *                     properties() {
     *                         property()
     *                         check()
     *                         req()
     *                         def()
     *                     }
     *                 }
     *             }
     *         }
     *     }
     * }
     *  </pre>
     * Subclasses may override this method to implement their own default meta schemas as needed.
     *
     * @return see above
     */
    protected Node createDefaultMetaSchema() {

        Factory schemaNodeFactory = new DefaultDefineSchemaNodeFactory();
        Factory collectionNodeFactory = new DefaultCollectionSchemaNodeFactory();

        SchemaNode metaSchema = new SchemaNode(null, "%");
        metaSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode propertiesMetaSchema = new SchemaNode(metaSchema, "properties");
        propertiesMetaSchema.attributes().put("factory", schemaNodeFactory);
        SchemaNode schemaNode = new SchemaNode(propertiesMetaSchema, "schema");
        SchemaNode factoryNode = new SchemaNode(propertiesMetaSchema, "factory");
        SchemaNode propertyNode = new SchemaNode(propertiesMetaSchema, "property");
        SchemaNode reqNode = new SchemaNode(propertiesMetaSchema, "req");
        SchemaNode defNode = new SchemaNode(propertiesMetaSchema, "def");
        SchemaNode checkNode = new SchemaNode(propertiesMetaSchema, "check");

        SchemaNode colsMetaSchema = new SchemaNode(metaSchema, "collections");
        SchemaNode colsSchema = new SchemaNode(colsMetaSchema, "collections");
        colsSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode colSchema = new SchemaNode(colsSchema, "%");  // allows the collection to have any name, e.g. foos
        colSchema.attributes().put("factory", collectionNodeFactory);

        SchemaNode colPropertiesSchema = new SchemaNode(colSchema, "properties");
        SchemaNode colNode = new SchemaNode(colPropertiesSchema, "collection");
        SchemaNode addNode = new SchemaNode(colPropertiesSchema, "add");
        SchemaNode keyNode = new SchemaNode(colPropertiesSchema, "key");

        SchemaNode colElementSchema = new SchemaNode(colSchema, "%");  // allows the collection's element to have any name, e.g. foo
        colElementSchema.attributes().put("schema", metaSchema);

        SchemaNode colsPropertiesSchema = new SchemaNode(colsMetaSchema, "properties");
        colsPropertiesSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode childMetaSchema = new SchemaNode(colsPropertiesSchema, "%");
        childMetaSchema.attributes().put("schema", metaSchema);

        return metaSchema;
    }

    /**
     * Defines, registers and returns a new schema using the default meta schema.  Defined schemas are available for
     * use in building new objects on the MetaBuilder directly.
     *
     * @param c
     * @return
     */
    public Object define(Closure c) {
        c.setDelegate(createMetaObjectGraphBuilder(getDefaultSchema(), defaultDefineNodeFactory));
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object schema = c.call();
        return schema;

    }

    public Object build(Closure c) {
        c.setDelegate(createMetaObjectGraphBuilder(null, defaultBuildNodeFactory));
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object schema = c.call();
        return schema;
    }

    /**
     * Returns a previously defined schema with the given name.
     *
     * @param name see above
     * @return see above
     */
    public Object getSchema(String name) {
        return schemas.get(name);
    }

    /**
     * Adds a previously defined schema with the given name.
     *
     * @param name see above
     * @return see above
     */
    public Object addSchema(String name, Object schema) {
        return schemas.put(name, schema);
    }

    /**
     * Returns the class loader in use by the MetaBuilder.
     *
     * @return see above
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the default schema.
     *
     * @return see above
     */
    public Node getDefaultSchema() {
        return defaultSchema;
    }

    /**
     * Returns a new {@link MetaObjectGraphBuilder} with the given default schema and node factory
     *
     * @param defaultSchema
     * @param defaultNodeFactory
     * @return see above
     */
    protected MetaObjectGraphBuilder createMetaObjectGraphBuilder(Node defaultSchema, Factory defaultNodeFactory) {
        return new MetaObjectGraphBuilder(this, defaultSchema, defaultNodeFactory);
    }

    /**
     * Sets the {@link ClassLoader} to use by the <code>MetaBuilder</code>.  It is sometimes necessary, especially in Groovy scripts,
     * to provide {@link ClassLoader} explicity to resolve classes by name.
     *
     * @param classLoader the {@link ClassLoader} to use
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static RuntimeException createPropertyException(String name, String error) {
        StringBuilder message = new StringBuilder("Property '").append(name).append("': ").append(error);
        return new PropertyException(message.toString());
    }

    public static RuntimeException createPropertyException(String name, Throwable error) {
        StringBuilder message = new StringBuilder("Property '").append(name).append("': ").append(error);
        return new PropertyException(message.toString(), error);
    }

    public static RuntimeException createCollectionException(String name, String error) {
        StringBuilder message = new StringBuilder("Collection '").append(name).append("': ").append(error);
        return new CollectionException(message.toString());
    }

    public static RuntimeException createCollectionException(String name, Throwable error) {
        StringBuilder message = new StringBuilder("Collection '").append(name).append("': ").append(error);
        return new CollectionException(message.toString(), error);
    }

    public static RuntimeException createFactoryException(String name, String error) {
        StringBuilder message = new StringBuilder("'").append(name).append("' factory: ").append(error);
        return new FactoryException(message.toString());
    }

    public static RuntimeException createFactoryException(String name, Throwable error) {
        StringBuilder message = new StringBuilder("'").append(name).append("' factory: ").append(error);
        return new FactoryException(message.toString(), error);
    }

    public static RuntimeException createSchemaNotFoundException(String name) {
        StringBuilder message = new StringBuilder(name);
        return new SchemaNotFoundException(message.toString());
    }

    public static RuntimeException createClassNameNotFoundException(String name) {
        StringBuilder message = new StringBuilder(name);
        return new ClassNameNotFoundException(message.toString());
    }

    /**
     * Default {@link SchemaNode} factory used when {@link MetaBuilder#define} is called.  Differs from
     * {@link DefaultBuildSchemaNodeFactory} in that it does include {@link CollectionSchemaNode}s in the result.
     */
    protected static class DefaultDefineSchemaNodeFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            return new SchemaNode((SchemaNode)builder.getCurrent(), name);
        }
    }

    /**
     * Default {@link SchemaNode} factory used when {@link MetaBuilder#build} is called.  Differs from 
     * {@link DefaultDefineSchemaNodeFactory} in that it doesn't include {@link CollectionSchemaNode}s in the result.
     */
    protected static class DefaultBuildSchemaNodeFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            if(attributes == null) attributes = new HashMap();
            if(value == null) value = new NodeList();
            SchemaNode schemaNode = (SchemaNode)builder.getCurrent();
            if(schemaNode instanceof CollectionSchemaNode) {
                CollectionSchemaNode collectionSchemaNode = (CollectionSchemaNode)schemaNode;
                schemaNode = (SchemaNode)collectionSchemaNode.getParentBean();
            }
            return new SchemaNode(schemaNode, name);
        }
    }

    /**
     * Default {@link CollectionSchemaNode} factory used when {@link MetaBuilder#define} is called.
     */
    protected static class DefaultCollectionSchemaNodeFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            return new CollectionSchemaNode((SchemaNode)builder.getCurrent(), name);
        }
    }
}
