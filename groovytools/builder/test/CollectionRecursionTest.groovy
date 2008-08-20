import groovytools.builder.MetaBuilder

/**
 */
class CollectionRecursionTest extends GroovyTestCase {

    public void test1() {
        MetaBuilder mb = new MetaBuilder()
        def parentDef = mb.define {
            parent(factory: TestParent) {
                properties {
                    name()
                }
                collections {
                    listOfChildren {
                        child(schema: 'parent')
                    }
                }
            }
        }

        println parentDef

        def parent = mb.build {
            parent (name: '1') {
                listOfChildren {
                    child (name: '1.1') {
                        listOfChildren {
                            child (name: '1.1.1'){
                            }
                        }
                    }
                    child (name: '1.2') {
                        listOfChildren {
                            child (name: '1.2.1'){
                            }
                        }
                    }
                }
            }
        }

        assertEquals("recursion", '1.2.1', parent.listOfChildren[0].listOfChildren[1].listOfChildren[0].name)
    }

    public static void main(String[] args) {
        try {
            CollectionRecursionTest t = new CollectionRecursionTest()
            t.test1()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
