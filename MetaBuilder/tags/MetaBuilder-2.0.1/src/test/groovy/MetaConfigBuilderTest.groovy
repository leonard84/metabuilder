import groovytools.builder.MetaConfigBuilder

/**
 * Tests MetaConfigBuilder.
 *
 * @author didge
 * @version $Id$
 */
class MetaConfigBuilderTest extends GroovyTestCase {
    public void test1() {

        def mb = new MetaConfigBuilder()
        mb.define {
            invoice {
                properties {
                    id()
                    date(def: new Date())
                    customer() {
                        properties {
                            fname()
                            lname()
                        }
                    }
                }
            }
        }

        def invoiceNodes = mb.build {
            invoice {
                id = 1
                customer {
                    fname = 'didge'
                }
            }
        }

        assertNotNull(invoiceNodes.date)
        assertEquals('didge', invoiceNodes.customer.fname)
    }

    public static void main(String[] args) {
        try {
            MetaConfigBuilderTest t = new MetaConfigBuilderTest()
            t.test1();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
