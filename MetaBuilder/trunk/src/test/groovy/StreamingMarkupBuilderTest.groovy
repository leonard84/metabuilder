import groovy.xml.MarkupBuilder
import groovytools.builder.MetaBuilder
import groovy.xml.StreamingMarkupBuilder
import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.Diff
import groovytools.builder.SchemaNode

/**
 * This test demonstrates the use of {@link MetaBuilder}'s default node factories.  The objects built are constructed
 * using {@link SchemaNode}s, which may be used in with utilities such as {@link XmlNodePrinter} that normally expect
 * {@link Node}s.
 *
 * @author didge
 * @version $Id$
 */
class StreamingMarkupBuilderTest extends GroovyTestCase {
    public void test1() {

        def mb = new MetaBuilder()
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

        def writer = new StringWriter()
        XmlNodePrinter xnp = new XmlNodePrinter(new PrintWriter(writer))
        xnp.print(invoiceNodes)

        def expected = """<invoice date="$invoiceDate">
              <items>
                <item upc="123" qty="1" price="14.99"/>
                <item upc="234" qty="4" price="14.99"/>
                <item upc="345" qty="6" price="14.99"/>
              </items>
            </invoice>"""

        XMLUnit.ignoreWhitespace = true
        XMLUnit.ignoreAttributeOrder = true
        def xmlDiff = new Diff(expected, writer.toString())
        assertEquals(true, xmlDiff.similar())

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