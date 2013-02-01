import groovytools.builder.MetaBuilder

/**
 * Tests any properties.
 *
 * @author didge
 * @version $Id: PropertyAttributesTest.groovy 37 2008-09-04 00:31:12Z didge $
 */
class AnyPropertyTest extends GroovyTestCase {

    public void test1() {

        MetaBuilder mbOne = new MetaBuilder()
        MetaBuilder mbTwo = new MetaBuilder()

        def one = mbOne.&build
        def two = mbTwo.&build

        def parentDef = mbOne.define {
            parent(factory: TestParent) {
                properties {
                    name()
                }
                collections {
                    listOfChildren() { // simple example of a collection of child objects
                        '%'(factory: { n,v,a -> v } )
                    }
                    anotherListOfChildren(collection: 'listOfChildren') { // simple example of a collection of child objects
                        '%'(factory: { n -> new TestChild(n) } )
                    }
                }
            }
            childOne(factory: TestChild) {
                properties {
                    name()
                }
            }
        }

        def childDef = mbTwo.define {
            childTwo(factory: TestChild) {
                properties {
                    name()
                }
            }
        }

        def parentList = mbOne.build {
            parent (name: two { childTwo(name:'aChildTwo')} ) {
                listOfChildren {
                    foo(one {childOne(name: 'aChildOne')} )
                    bar(two {childTwo(name: 'anotherChildTwo')})
                }
                anotherListOfChildren {
                    foo()
                    bar()
                }
            }
        }

        println(parentList)
    }

    public static void main(String[] args) {
        try {
            AnyPropertyTest t = new AnyPropertyTest()
            t.test1()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}

