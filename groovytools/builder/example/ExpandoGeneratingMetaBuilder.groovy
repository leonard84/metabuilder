import org.codehaus.groovy.control.*
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import groovytools.builder.*
import groovyjarjarasm.asm.*

/**
 * This {@link MetaBuilder} variant constructs {@link Expando}s for build nodes.
 *
 * @author didge
 * @version $Id$
 */
class ExpandoGeneratingMetaBuilder extends MetaBuilder {
    public ExpandoGeneratingMetaBuilder() {
        setDefaultBuildNodeFactory(new ExpandoBuilderFactory())
    }

    public static void main(String[] args) {
        try {
            def mb = new ExpandoGeneratingMetaBuilder()
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

class ExpandoBuilderFactory extends AbstractFactory {
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        Expando expando = new Expando()

        MetaObjectGraphBuilder mogb = (MetaObjectGraphBuilder)builder
        SchemaNode schema = mogb.getCurrentSchema()

        SchemaNode schemaCollectionsNode = schema.firstChild("collections")
        schemaCollectionsNode?.value()?.each { collection ->
            if(collection.attributes().containsKey("key")) {
                // need a map
                expando.setProperty(collection.name(), [:])
            }
            else {
                // need a list
                expando.setProperty(collection.name(), [])
            }
        }
        return expando
    }
}

