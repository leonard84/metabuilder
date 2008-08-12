package groovytools.builder;

import groovy.util.*;
import groovy.lang.*;

import java.util.*;

/**
 * {@link MetaBuilder} is a builder that uses metadata you specify in order to more conveniently and correctly
 * build object hierarchies.
 *
 * @see ObjectGraphBuilder
 */
public class MetaBuilder extends GroovyObjectSupport {
    protected Map _schemas;
    protected Node _defaultSchema;
    protected ClassLoader _classLoader;
    protected BuilderSuppportProxy _builderSuppportProxy;

    /**
     * Constructs a <code>MetaBuilder</code> with the default schema.
     *
     * @see #createDefaultMetaSchema()
     */
    public MetaBuilder() {
        this(null);
        _defaultSchema = createDefaultMetaSchema();
    }

    /**
     * Constructs a MetaBuilder with the given default meta schema
     *
     * @param defaultSchema the default schema
     */
    public MetaBuilder(Node defaultSchema) {
        _schemas = new HashMap();
        _defaultSchema = defaultSchema;
    }

    /**
     * The default implementantion returns the MetaBuilder meta schema:
     * <pre>
     * schemaNodeFactory = new MetaBuilder.SchemaNodeFactory()
     * collectionSchemaNodeFactory = new MetaBuilder.CollectionSchemaNodeFactory()
     *
     * metaSchema(factory: schemaNodeFactory) {
     *     collections() {
     *         collections(factory: schemaNodeFactory) {
     *             '%'(factory: collectionSchemaNodeFactory) {
     *                 '%'(schema: metaSchema)
     *             }
     *             properties(factory: schemaNodeFactory) {
     *                 '%'(schema: metaSchema)
     *             }
     *         }
     *     }
     *     '%'(schema: metaSchema)
     * }
     *  </pre>
     * Subclasses may override this method to implement their own default meta schemas as needed.
     *
     * @return see above
     */
    protected Node createDefaultMetaSchema() {

        Factory schemaNodeFactory = new SchemaNodeFactory();
        Factory collectionNodeFactory = new CollectionSchemaNodeFactory();

        SchemaNode metaSchema = new SchemaNode(null, "%");
        metaSchema.attributes().put("factory", schemaNodeFactory);

/*
        SchemaNode metaSchemaPropMetaSchema = new SchemaNode(metaSchema, "properties");
        metaSchemaPropMetaSchema.attributes().put("factory", schemaNodeFactory);
        Node metaSchemaPropSchema = new SchemaNode(metaSchemaPropMetaSchema , "%");
        metaSchemaPropSchema.attributes().put("schema", metaSchema);
*/
        SchemaNode colsMetaSchema = new SchemaNode(metaSchema, "collections");
        SchemaNode colsSchema = new SchemaNode(colsMetaSchema, "collections");
        colsSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode colSchema = new SchemaNode(colsSchema, "%");  // allows the collection to have any name
        colSchema.attributes().put("factory", collectionNodeFactory);

        Node colElementSchema = new SchemaNode(colSchema, "%");  // allows the collection's element to have any name
        colElementSchema.attributes().put("schema", metaSchema);

        SchemaNode propMetaSchema = new SchemaNode(colsMetaSchema, "properties");
        propMetaSchema.attributes().put("factory", schemaNodeFactory);
        Node propSchema = new SchemaNode(propMetaSchema, "%");
        propSchema.attributes().put("schema", metaSchema);

        SchemaNode childMetaSchema = new SchemaNode(metaSchema, "%");
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
        c.setDelegate(new MetaBuilderBuilder(_schemas, _defaultSchema, _classLoader != null ? _classLoader : getClass().getClassLoader()));
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object schema = c.call();
        return schema;

    }

    public Object build(Closure c) {
        c.setDelegate(new MetaBuilderBuilder(_schemas, null, _classLoader != null ? _classLoader : getClass().getClassLoader()));
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object schema = c.call();
        return schema;

    }

    public Object build(Object builder, Closure c) {
        _builderSuppportProxy = new BuilderSuppportProxy(builder);
        MetaBuilderBuilder metaBuilderBuilder = new MetaBuilderBuilder(_schemas, null, _classLoader != null ? _classLoader : getClass().getClassLoader());
        metaBuilderBuilder.setBuilderSupportProxy(_builderSuppportProxy);
        c.setDelegate(metaBuilderBuilder);
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
    public Object schema(String name) {
        return _schemas.get(name);
    }

    /**
     * Returns the class loader in use by the MetaBuilder.
     *
     * @return see above
     */
    public ClassLoader getClassLoader() {
        return _classLoader;
    }

    /**
     * Sets the {@link ClassLoader} to use by the <code>MetaBuilder</code>.  It is sometimes necessary, especially in Groovy scripts,
     * to provide {@link ClassLoader} explicity to resolve classes by name.
     *
     * @param classLoader the {@link ClassLoader} to use
     */
    public void setClassLoader(ClassLoader classLoader) {
        _classLoader = classLoader;
    }

    /**
     * Overrides default implementation to delegate to a new {@link MetaBuilderBuilder}.  This is method by which
     * {@link MetaBuilder} supports nested definitions.
     *
     * @return the result of the call
     * @param methodName the name of the method to invoke
     */
    public Object invokeMethod(String methodName) {
        MetaBuilderBuilder mbb = new MetaBuilderBuilder(_schemas, null, _classLoader != null ? _classLoader : getClass().getClassLoader());
        mbb.setBuilderSupportProxy(_builderSuppportProxy);
            return mbb.invokeMethod(methodName);
    }

    /**
     * Overrides default implementation to delegate to a new {@link MetaBuilderBuilder}.  This is method by which
     * {@link MetaBuilder} supports nested definitions.
     *
     * @return the result of the call
     * @param methodName the name of the method to invoke
     */
    public Object invokeMethod(String methodName, Object args) {
        MetaBuilderBuilder mbb = new MetaBuilderBuilder(_schemas, null, _classLoader != null ? _classLoader : getClass().getClassLoader());
        mbb.setBuilderSupportProxy(_builderSuppportProxy);
        return mbb.invokeMethod(methodName, args);
    }

    private static class SchemaNodeFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            if(attributes == null) attributes = new HashMap();
            if(value == null) value = new NodeList();
            return new SchemaNode((SchemaNode)builder.getCurrent(), name, attributes, value);
        }
    }

    private static class CollectionSchemaNodeFactory extends AbstractFactory {
        public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
            return new CollectionSchemaNode((SchemaNode)builder.getCurrent(), name, attributes, value);
        }
    }
}
