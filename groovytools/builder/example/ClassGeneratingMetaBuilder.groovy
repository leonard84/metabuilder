import org.codehaus.groovy.control.*
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import groovytools.builder.*
import groovyjarjarasm.asm.*

/**
 * This {@link MetaBuilder} variant is novel because it builds a hierarchy of objects using classes generated from
 * the schema.
 *
 * @author didge
 * @version $Id$
 */
class ClassGeneratingMetaBuilder extends MetaBuilder {
    public ClassGeneratingMetaBuilder(GroovyClassLoader groovyClassLoader, String codeBase) {
        super(groovyClassLoader)
        def compileUnit = new CompileUnit(groovyClassLoader, new CompilerConfiguration())
        def moduleNode = new ModuleNode(compileUnit)
        setDefaultBuildNodeFactory(new ClassBuilderFactory(groovyClassLoader, compileUnit, moduleNode, codeBase))
    }

    public static void main(String[] args) {
        try {
            def mb = new ClassGeneratingMetaBuilder(new GroovyClassLoader(), "test")
            mb.define {
                invoice {
                    properties {
                        id()
                        date()
                        customer()
                    }
                    collections {
                        items {
                            item {
                                properties {
                                    upc()
                                    price()
                                    qty()
                                }
                            }
                        }
                    }
                }
            }

            def invoiceDate = new Date()

            def invoiceNodes = mb.build {
                invoice(date: invoiceDate) {
                    items {
                        item(upc: 123, qty: 1, price: 14.99)
                        item(upc: 234, qty: 4, price: 14.99)
                        item(upc: 345, qty: 6, price: 14.99)
                    }
                }
            }

            println(invoiceNodes)
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}

class ClassBuilderFactory extends AbstractFactory {
    GroovyClassLoader groovyClassLoader
    CompileUnit compileUnit
    ModuleNode moduleNode
    String codeBase

    def ClassBuilderFactory(GroovyClassLoader groovyClassLoader, compileUnit, moduleNode, codeBase) {
        this.groovyClassLoader = groovyClassLoader
        this.compileUnit = compileUnit
        this.moduleNode = moduleNode
        this.codeBase = codeBase
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        MetaObjectGraphBuilder mogb = (MetaObjectGraphBuilder)builder
        SchemaNode schema = mogb.getCurrentSchema()

        // upper the 1st char because ObjectGraphBuilder expects classes to have the 1st char in upper case
        String className = createClassName(schema.name())
        
        ClassNode classNode = new ClassNode(className, Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        classNode.module = moduleNode

        SchemaNode schemaPropertiesNode = schema.firstChild("properties")
        schemaPropertiesNode?.value()?.each { property ->
            classNode.addProperty(new PropertyNode((String)property.name(), Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE, classNode, null, null, null))
        }

        SchemaNode schemaCollectionsNode = schema.firstChild("collections")
        schemaCollectionsNode?.value()?.each { collection ->
            if(collection.attributes().containsKey("key")) {
                // need a map
                classNode.addProperty(new PropertyNode((String)collection.name(), Opcodes.ACC_PUBLIC, ClassHelper.MAP_TYPE, classNode,
                        new ConstructorCallExpression(ClassHelper.make(HashMap.class), ArgumentListExpression.EMPTY_ARGUMENTS), null, null))
            }
            else {
                // need a list
                classNode.addProperty(new PropertyNode((String)collection.name(), Opcodes.ACC_PUBLIC, ClassHelper.LIST_TYPE, classNode,
                        new ConstructorCallExpression(ClassHelper.make(ArrayList.class), ArgumentListExpression.EMPTY_ARGUMENTS), null, null))
            }
        }
        Class newClass = groovyClassLoader.defineClass(classNode, classNode.getName() + ".groovy", codeBase)

        schema.attributes().factory = newClass
        return newClass.newInstance()
    }

    protected String createClassName(String n) {
        return n.length() == 1 ?  n.toUpperCase() : n[0].toUpperCase() + n.substring(1);
    }
}

