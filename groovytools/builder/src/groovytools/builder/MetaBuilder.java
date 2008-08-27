/*
 *         Copyright 2008 Dave 'didge' Masser-Frye
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
 * <code>MetaBuilder</code> is a builder that uses schemas to more conveniently and correctly
 * build object hierarchies.
 * <h2>Usage</h2>
 * <code>MetaBuilder</code> is easy to use.  Just follow these steps:
 * <ol>
 * <li>Create a <code>MetaBuilder</code> instance.</li>
 * <li>Define your schemas with {@link #define}.</li>
 * <li>Build your objects with {@link #build}.</li>
 * </ol>
 * <h2>Example</h2>
 * Here is a very simple example demonstrating the steps above:
 * <pre>
 * // Create a MetaBuilder
 * MetaBuilder mb = new MetaBuilder()
 *
 * // Define a schema
 * mb.define {
 *    invoice {
 *         collections {
 *             items {
 *                 item {
 *                     properties {
 *                         qty(req: true)
 *                     }
 *                 }
 *             }
 *             payments {
 *                 payment {
 *                     properties {
 *                         amt(req: true)
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 * // Build an object.
 * mb.build {
 *     invoice {
 *         items {
 *             item(qty: 1)
 *             item(qty: 20)
 *         }
 *         payments {
 *             payment(amt: 100.00)
 *         }
 *     }
 * }
 * </pre>
 * <h2>The MetaBuilder Meta-Schema</h2>
 * The schemas that may be defined are governed by the following meta-schema.  Note, <code>'%'</code> is used to stand
 * for any sequence of non-whitespace characters:
 * <pre>
 * def schemaNodeFactory = new MetaBuilder.SchemaNodeFactory()
 * def collectionSchemaNodeFactory = new MetaBuilder.CollectionSchemaNodeFactory()
 * def nullOrBoolean = { v -> v == null || v instanceof Boolean }
 * def nullOrClosure = { v -> v == null || v instanceof Closure }
 * def nullOrStringOrClosure = { v -> v == null || v instanceof String || v instanceof Closure }
 * def nullOrStringOrClassOrFactoryOrClosure = { v -> v == null || v instanceof String || v instanceof Class || v instanceof Factory || v instanceof Closure }
 * def nullOrStringOrSchemaNode = { v -> v == null || v instanceof String || v instanceof SchemaNode }
 *
 * def metaSchema = '%'(factory: schemaNodeFactory) {
 *     properties() {
 *         schema(check: nullOrStringOrSchemaNode )
 *         factory(check: nullOrStringOrClassOrFactoryOrClosure )
 *     }
 *     collections() {
 *         collections(factory: schemaNodeFactory) {
 *             '%'(factory: collectionSchemaNodeFactory) {
 *                 properties(factory: schemaNodeFactory) {
 *                     collection(check: nullOrStringOrClosure )
 *                     key(check: nullOrStringOrClosure )
 *                     add(check: nullOrStringOrClosure )
 *                 }
 *                 '%'(shema: metaSchema)
 *             }
 *         }
 *         properties(factory: schemaNodeFactory) {
 *             '%'(schema: metaSchema)
 *                 properties() {
 *                     property(check: nullOrStringOrClosure)
 *                     check(check: nullOrClosure)
 *                     req(check: nullOrBoolean)
 *                     def()
 *                     // Inherited from metaSchema:
 *                     // schema()
 *                     // factory()
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 * <h2>Property Values</h2>
 * The following table describes the allowable property values when defining your own schema using <code>MetaBuilder</code>'s
 * default meta-schema. Values may be of type: literal, object, {@link Class} and/or {@link Closure}.
 * <p/>
 * Only one value may be specied for each property at a time.
 * <table border="1" cellspacing="0">
 * <tr>
 *  <td>Name</td>
 *  <td>Description</td>
 *  <td>Literal</td>
 *  <td>Object</td>
 *  <td>Class</td>
 *  <td>Closure</td>
 * </tr>
 * <tr>
 *  <td><code>schema</code></td>
 *  <td>Allows a schema to inherit and extend the properties and collections of another schema.</td>
 *  <td>The name of another schema.  Optional.  The named schema does not have to be previously defined.</td>
 *  <td>A previously defined schema object.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * <tr>
 *  <td><code>factory</code></td>
 *  <td>Specifies the factory to use for building nodes.  Optional.  The default factory builds {@link SchemaNode}s.</td>
 *  <td>A fully qualified class name for direct instatiation with {@link Class#newInstance} .</td>
 *  <td>A {@link groovy.util.Factory} object.</td>
 *  <td>A {@link Class} for direct instatiation with {@link Class#newInstance}.</td>
 *  <td>A {@link Closure} returning an object of the form
 *   <ul>
 *    <li><code>{ -> ...}</code></li>
 *    <li><code>{n -> ...}</code></li>
 *    <li><code>{n, v -> ...}</code></li>
 *    <li><code>{n, v, a -> ...}</code></li>
 *   </ul>
 *   where
 *   <ul>
 *    <li><code>n</code> is the name of the node</li>
 *    <li><code>v</code> is the value of the node (may be null)</li>
 *    <li><code>a</code> is a map of the node's attributes (may be empty)</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td>collection</td>
 *  <td>Used to identify or access the actual collection name.  Optional.  The default is to access the collection using the node's name.</td>
 *  <td>A property or field name</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} returning the collection of the form
 *   <ul>
 *    <li><code>{o -> ...}</code></li>
 *   </li>
 *   where
 *   <ul>
 *    <li><code>o</code> is the owner of the collection</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>key</code></td>
 *  <td>When collection is a {@link Map}, key must be defined to return the key to be used to add an object to children to the collection unless add is also specified with two arguments.</td>
 *  <td>A property or field name</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td><code>{c -> ...}</code><br/>
 *   where
 *   <ul>
 *    <li><code>c</code> is the child</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>add</code></td>
 *  <td>Used to specify an alternative for adding children to the collection.  Optional.  Useful when a modifiable collection is not accessible.</td>
 *  <td>A method name.  The method must accept two {@link Object} argument values for the key and value, in that order.</code></td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} of the form
 *   <ul>
 *    <li><code>{p, k, c -> ...}</code></li>
 *    <li><code>{p, c -> ...}</code></li>
 *   </ul>
 *   where
 *   <ul>
 *    <li><code>p</code> is the parent</li>
 *    <li><code>k</code> is the key</li>
 *    <li><code>c</code> is the child</li>
 *   </ul>
 *  Note, if the first {@link Closure} form is used, then the property <code>key</code> must be specfied.  By using the
 *  second form, the {@link Closure} is responsible for determining and using the correct key for the child.
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>property</code></td>
 *  <td>Used to identify or modify the actual property.  Optional.  The default is to set the property using the node's name.  Note that this value may be inherited or overridden.</td>
 *  <td>A property or field name.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} of the form
 *   <ul>
 *     <li><code>{o, v -> ...}</code></li>
 *   </li>
 *   where
 *   <ul>
 *    <li><code>o</code> is the object</li>
 *    <li><code>v</code> is the value</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>check</code></td>
 *  <td>Used to specify a check on the value of the property.  Optional.  Note that when inherited and overridden, all checks in the hierarchy are applied.</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 *  <td>A {@link Closure} returning a Groovy Truth value of the form
 *   <ul>
 *    <li><code>{v -> ...}</code></li>
 *   </ul>
 *   where
 *   <ul>
 *    <li><code>v</code> is the value</li>
 *   <ul>
 *  </td>
 * </tr>
 * <tr>
 *  <td><code>req</code></td>
 *  <td>Used to specify if a property must be specified.  Optional.  Note that this value may be inherited but not overridden.</td>
 *  <td>n/a</td>
 *  <td><code>true</code> or <code>false</code></td>
 *  <td>n/a</td>
 *  <td>n/a</td>
 * </tr>
 * <tr>
 *  <td><code>def</code></td>
 *  <td>Used to specify a default value.  Optional.  Note that this value may be inherited or overridden.</td>
 *  <td>Any literal value may be passed to the property.</td>
 *  <td>Any object may be passed to the property.</td>
 *  <td>Any {@link Class} is passed like an object.</td>
 *  <td>Any {@link Closure} is passed like an object.</td>
 * </tr>
 * </table>
 *
 * @see ObjectGraphBuilder
 *
 * @author didge
 * @version $REV$
 */
public class MetaBuilder extends Binding {
    private Map schemas;
    private Node defaultMetaSchema;
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
        this.defaultMetaSchema = createDefaultMetaSchema();
    }

    /**
     * Constructs a <code>MetaBuilder</code> with the default meta schema, node factory and specified class loader.
     *
     * @param classLoader
     * @see #createDefaultMetaSchema()
     */
    public MetaBuilder(ClassLoader classLoader) {
        this(null, classLoader);
        this.defaultMetaSchema = createDefaultMetaSchema();
    }

    /**
     * Constructs a MetaBuilder with the given default meta schema
     *
     * @param defaultMetaSchema the default schema
     */
    public MetaBuilder(Node defaultMetaSchema, ClassLoader classLoader) {
        schemas = new HashMap();
        this.defaultMetaSchema = defaultMetaSchema;
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
     * Subclasses may override this method to implement their own default meta schemas as needed.
     *
     * @return see above
     */
    protected Node createDefaultMetaSchema() {

        Factory schemaNodeFactory = new DefaultDefineSchemaNodeFactory();
        Factory collectionNodeFactory = new DefaultCollectionSchemaNodeFactory();
        Closure nullOrBoolean = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof Boolean;
            }
        };

        Closure nullOrClosure = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof Closure;
            }
        };

        Closure nullOrStringOrClosure = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof String || v instanceof Closure;
            }
        };

        Closure nullOrStringOrClassOrFactoryOrClosure = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof String ||v instanceof Class ||  v instanceof Factory || v instanceof Closure;
            }
        };

        Closure nullOrStringOrSchemaNode = new Closure(this) {
            public Object call(Object v) {
                return v == null || v instanceof String || v instanceof SchemaNode;
            }
        };

        SchemaNode metaSchema = new SchemaNode(null, "%");
        metaSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode propertiesMetaSchema = new SchemaNode(metaSchema, "properties");
        propertiesMetaSchema.attributes().put("factory", schemaNodeFactory);
        SchemaNode schemaNode = new SchemaNode(propertiesMetaSchema, "schema");
        schemaNode.attributes().put("check", nullOrStringOrSchemaNode);
        SchemaNode factoryNode = new SchemaNode(propertiesMetaSchema, "factory");
        factoryNode.attributes().put("check", nullOrStringOrClassOrFactoryOrClosure);

        SchemaNode colMetaSchema = new SchemaNode(metaSchema, "collections");
        SchemaNode colsSchema = new SchemaNode(colMetaSchema, "collections");
        colsSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode colSchema = new SchemaNode(colsSchema, "%");  // allows the collection to have any name, e.g. foos
        colSchema.attributes().put("factory", collectionNodeFactory);
        SchemaNode colSchemaProperties = new SchemaNode(colSchema, "properties");
        SchemaNode colNode = new SchemaNode(colSchemaProperties, "collection");
        colNode.attributes().put("check", nullOrStringOrClosure);
        SchemaNode addNode = new SchemaNode(colSchemaProperties, "add");
        addNode.attributes().put("check", nullOrStringOrClosure);
        SchemaNode keyNode = new SchemaNode(colSchemaProperties, "key");
        keyNode.attributes().put("check", nullOrStringOrClosure);

        SchemaNode colElementSchema = new SchemaNode(colSchema, "%");  // allows the collection's element to have any name, e.g. foo
        colElementSchema.attributes().put("schema", metaSchema);

        SchemaNode propertiesSchema = new SchemaNode(colMetaSchema, "properties");
        propertiesSchema.attributes().put("factory", schemaNodeFactory);

        SchemaNode propertiesElementSchema = new SchemaNode(propertiesSchema, "%");
        propertiesElementSchema.attributes().put("schema", metaSchema);

        SchemaNode propertiesElementSchemaProperties = new SchemaNode(propertiesElementSchema, "properties");
        SchemaNode propertyNode = new SchemaNode(propertiesElementSchemaProperties, "property");
        propertyNode.attributes().put("check", nullOrStringOrClosure);
        SchemaNode reqNode = new SchemaNode(propertiesElementSchemaProperties, "req");
        reqNode.attributes().put("check", nullOrBoolean);
        SchemaNode defNode = new SchemaNode(propertiesElementSchemaProperties, "def");
        SchemaNode checkNode = new SchemaNode(propertiesElementSchemaProperties, "check");
        checkNode.attributes().put("check", nullOrClosure);

        return metaSchema;
    }

    /**
     * Defines, registers and returns a new schema using the default meta schema.  Defined schemas are available for
     * use in building new objects on the MetaBuilder directly.
     *
     * @param c
     *
     * @return see above
     */
    public Object define(Closure c) {
        c.setDelegate(createMetaObjectGraphBuilder(defaultMetaSchema, defaultDefineNodeFactory));
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object schema = c.call();
        return schema;

    }

    public Object define(Class viewClass) {
        if (Script.class.isAssignableFrom(viewClass)) {
            Script script = InvokerHelper.createScript(viewClass, this);
            return define(script);
        } else {
            throw new RuntimeException("Only scripts can be executed via build(Class)");
        }
    }

    public Object define(Script script) {
        synchronized (script) {
            MetaClass scriptMetaClass = script.getMetaClass();
            try {
                MetaObjectGraphBuilder metaObjectGraphBuilder = createMetaObjectGraphBuilder(defaultMetaSchema, defaultDefineNodeFactory);
                script.setMetaClass(new FactoryInterceptorMetaClass(scriptMetaClass, metaObjectGraphBuilder));
                script.setBinding(this);
                return script.run();
            } finally {
                script.setMetaClass(scriptMetaClass);
            }
        }
    }

    public Object define(final String script, GroovyClassLoader loader) {
        return define(loader.parseClass(script));
    }

    public Object build(Closure c) {
        c.setDelegate(createMetaObjectGraphBuilder(null, defaultBuildNodeFactory));
        c.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object schema = c.call();
        return schema;
    }

    public Object build(Class viewClass) {
        if (Script.class.isAssignableFrom(viewClass)) {
            Script script = InvokerHelper.createScript(viewClass, this);
            return build(script);
        } else {
            throw new RuntimeException("Only scripts can be executed via build(Class)");
        }
    }

    public Object build(Script script) {
        synchronized (script) {
            MetaClass scriptMetaClass = script.getMetaClass();
            try {
                MetaObjectGraphBuilder metaObjectGraphBuilder = createMetaObjectGraphBuilder(null, defaultBuildNodeFactory);
                script.setMetaClass(new FactoryInterceptorMetaClass(scriptMetaClass, metaObjectGraphBuilder));
                script.setBinding(this);
                return script.run();
            } finally {
                script.setMetaClass(scriptMetaClass);
            }
        }
    }

    public Object build(final String script, GroovyClassLoader loader) {
        return build(loader.parseClass(script));
    }

    /**
     * Returns a previously defined schema with the given name.
     *
     * @param name see above
     *
     * @return see above
     */
    public Object getSchema(String name) {
        return schemas.get(name);
    }

    /**
     * Adds a previously defined schema with the given name.
     *
     * @param name see above
     * @param schema
     *
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
    public Node getDefaultMetaSchema() {
        return defaultMetaSchema;
    }

    /**
     * Returns a new {@link MetaObjectGraphBuilder} with the given default schema and node factory
     *
     * @param defaultSchema
     * @param defaultNodeFactory
     *
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
            SchemaNode schemaNode = (SchemaNode)builder.getCurrent();
            if(schemaNode instanceof CollectionSchemaNode) {
                CollectionSchemaNode collectionSchemaNode = (CollectionSchemaNode)schemaNode;
                SchemaNode parent = (SchemaNode)collectionSchemaNode.getParentBean();
//                new SchemaNode(parent, name);
            }
            return new SchemaNode(null, name);
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

    /**
     * Supports builder scripts by dispatching methods against {@link MetaObjectGraphBuilder}.
     * <p>
     * Borrowed from {@link FactoryBuilderSupport}.  Is there a reason it wasn't made a public class to begin with?
     */
    public static class FactoryInterceptorMetaClass extends DelegatingMetaClass {

        FactoryBuilderSupport factory;

        public FactoryInterceptorMetaClass(MetaClass delegate, FactoryBuilderSupport factory) {
            super(delegate);
            this.factory = factory;
        }

        public Object invokeMethod(Object object, String methodName, Object arguments) {
            try {
                return delegate.invokeMethod(object, methodName, arguments);
            } catch (MissingMethodException mme) {
                // attempt factory resolution
                try {
                    if (factory.getMetaClass().respondsTo(factory, methodName).isEmpty()) {
                        // dispatch to fectories if it is not a literal method
                        return factory.invokeMethod(methodName, arguments);
                    } else {
                        return InvokerHelper.invokeMethod(factory, methodName, arguments);
                    }
                } catch (MissingMethodException mme2) {
                    // throw original
                    // should we chain in mme2 somehow?
                    throw mme;
                }
            }
        }

        public Object invokeMethod(Object object, String methodName, Object[] arguments) {
            try {
                return delegate.invokeMethod(object, methodName, arguments);
            } catch (MissingMethodException mme) {
                // attempt factory resolution
                try {
                    if (factory.getMetaClass().respondsTo(factory, methodName).isEmpty()) {
                        // dispatch to fectories if it is not a literal method
                        return factory.invokeMethod(methodName, arguments);
                    } else {
                        return InvokerHelper.invokeMethod(factory, methodName, arguments);
                    }
                } catch (MissingMethodException mme2) {
                    // throw original
                    // should we chain in mme2 somehow?
                    throw mme;
                }
            }
        }
    }}
