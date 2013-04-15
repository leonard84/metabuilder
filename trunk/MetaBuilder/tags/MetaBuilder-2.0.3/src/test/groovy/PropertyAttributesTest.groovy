import groovytools.builder.MetaBuilder

/**
 * Tests various attributes of properties.
 *
 * @author didge
 * @version $Id$
 */
class PropertyAttributesTest extends GroovyTestCase {

    public void test1() {
        MetaBuilder mb = new MetaBuilder()
        def parentDef = mb.define {
            child(factory: TestChild) {
                properties {
                    name()
                }
            }
            parent(factory: TestParent) {
                properties {
                    name()
                    name2(property: 'name', check: ~/Lists.*/)
                    name3(property: { b, v -> b.setName(v) } )
                    listOfChildren()
                }
            }
        }

        def parent1 = mb.build { parent(name: 'Lists of Children1') }
        assertEquals('Lists of Children1', parent1.name)

        def parent2 = mb.build { parent(name2: 'Lists of Children2') }
        assertEquals('Lists of Children2', parent2.name)

        def parent3 = mb.build { parent(name3: 'Lists of Children3') }
        assertEquals('Lists of Children3', parent3.name)
    }

    public static void main(String[] args) {
        try {
            PropertyAttributesTest t = new PropertyAttributesTest()
            t.test1()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
