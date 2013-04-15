import groovytools.builder.MetaBuilder

/**
 * Tests inherited collection definitions.
 *
 * @author christian ohr
 * @version $Id$
 */
class CollectionInheritanceTest extends GroovyTestCase {

    public void test1() {
        MetaBuilder mb = new MetaBuilder()

        def childDef = mb.define {
            child(factory: TestChild) {
                properties {
                    name()
                }
            }
        }

        def parentDef1 = mb.define {
            parent1(factory: TestParent) {
                properties {
                    name()
                }
                collections {
                    listOfChildren {
                        son(schema: 'child')
                    }
                }
            }
        }

        def parentDef2 = mb.define {
            parent2(schema: 'parent1') {
                collections {
                    listOfChildren {
                        daughter(schema: 'child')
                    }
                }
            }
        }

        def parent = mb.build {
            parent2 (name: '1') {
                son (name: '1.1')
                son (name: '1.2')
                daughter (name: '2.1')
            }
        }

        assertEquals(3, parent.listOfChildren.size())
    }

    public static void main(String[] args) {
        try {
            CollectionInheritanceTest t = new CollectionInheritanceTest()
            t.test1()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}