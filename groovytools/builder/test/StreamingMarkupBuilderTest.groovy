import groovy.xml.MarkupBuilder
import groovytools.builder.MetaBuilder
import groovy.xml.StreamingMarkupBuilder

/**
 */
class StreamingMarkupBuilderTest {
    public void test1() {

        def mb = new MetaBuilder()
        mb.define {
            invoices {
                collections {
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
            }
        }


        def invoices = mb.invoices {
            invoice(date: new Date()) {
                items {
                    item(upc: 123, qty: 1, price: 14.99)
                    item(ups: 234, qty: 4, price: 14.99)
                    item(upc: 345, qty: 6, price: 14.99)
                }
            }
        }

        def writer = new StringWriter()
        writer << new StreamingMarkupBuilder().bind {
           mkp.declareNamespace( 'invoices': 'http://www.w3.org/1999/xhtml' )
           mkp.yield invoices
        }

        println(writer.toString())
    }

    public static void main(String[] args) {
        try {
            StreamingMarkupBuilderTest t = new StreamingMarkupBuilderTest()
            t.test1();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

}