import groovy.xml.MarkupBuilder
import groovytools.builder.MetaBuilder
import groovytools.builder.MetaBuilder2

/**
 */
class MarkupBuilderTest {
    public void test1() {

        def mb = new MetaBuilder2()
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

        def writer = new StringWriter()
        def mkupb = new MarkupBuilder(writer)

        def invoices = mb.build(mkupb).invoices {
            invoice(date: new Date()) {
                items {
                    item(upc: 123, qty: 1, price: 14.99)
                    item(ups: 234, qty: 4, price: 14.99)
                    item(upc: 345, qty: 6, price: 14.99)
                }
            }
        }

        println(writer.toString())
    }

    public static void main(String[] args) {
        try {
            MarkupBuilderTest t = new MarkupBuilderTest()
            t.test1();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

}