import groovytools.builder.*
import org.codehaus.groovy.runtime.StackTraceUtils

/**
 * Tests alternative collection definitions.
 *
 * @author didge
 * @version $Id$
 */
class CollectionAttributesTest extends GroovyTestCase {

    public void test1() {
        MetaBuilder mb = new MetaBuilder()
        def parentDef = mb.define {
            child(factory: TestChild) {
                properties {
                    name(min: 3)
                }
            }
            parent(factory: TestParent) {
                properties {
                    name()
                    listOfChildrenAsProperty(property: 'listOfChildren')
                }
                collections {
                    listOfChildren(min: 6) { // simple example of a collection of child objects
                        child(schema: 'child')
                    }
                    listOfChildren2(collection: "listOfChildren") { // uses the collection above
                        child(schema: 'child')
                    }
                    listOfChildren3(collection: {p -> p.getListOfChildren() }) {
                        child(schema: 'child')
                    }
                    listOfChildren4(add: 'addChildToList', size: 'listSize') {
                        child(schema: 'child')
                    }
                    listOfChildren5(add: {p, c -> p.addChildToList(c) }, size: {p -> p.listSize() }) {
                        child(schema: 'child')
                    }
                    mapOfChildren(key: 'name') { // simple example of a Map of child objects, using getName() as the key
                        child(schema: 'child')
                    }
                    mapOfChildren2(collection: 'mapOfChildren', key: 'name') {
                        child(schema: 'child')
                    }
                    mapOfChildren3(collection: {p -> p.getMapOfChildren() }, key: 'name') {
                        child(schema: 'child')
                    }
                    mapOfChildren4(add: 'addChildToMap', key: 'name') {  // note, addChild called like this: p.addChild(key, child)
                        child(schema: 'child')
                    }
                    mapOfChildren5(add: {p, k, c -> p.addChildToMap(k, c) }, key: 'name') {
                        child(schema: 'child')
                    }
                    mapOfChildren6(add: {p, c -> p.addChildToMap(c.getName(), c) }) {
                        child(schema: 'child')
                    }
                }
            }
        }

        def parent1 = mb.build {
            parent(name: 'Lists of Children', listOfChildrenAsProperty: ['Jeb', 'Job']) {
                listOfChildren {
                    child(name: 'Jay')
                }
                listOfChildren2 {
                    child(name: 'Jan')
                }
                listOfChildren3 {
                    child(name: 'Joe')
                }
                listOfChildren4 {
                    child(name: 'Jer')
                }
                mapOfChildren {
                    child(name: 'Jim')
                }
                mapOfChildren2 {
                    child(name: 'Jen')
                }
                mapOfChildren3 {
                    child(name: 'Jon')
                }
                mapOfChildren4 {
                    child(name: 'Jem')
                }
                mapOfChildren5 {
                    child(name: 'Jed')
                }
            }
        }

        //assertEquals(6, parent1.listOfChildren.size())
        assertEquals(5, parent1.mapOfChildren.size())
    }

    public void test2() {
        MetaBuilder mb = new MetaBuilder()

        def children

        def parentDef = mb.define {
            child(factory: TestChild) {
                properties {
                    name(min: 3)
                }
            }
            parent(factory: TestParent) {
                properties {
                    name()
                }
                collections {
                    listOfChildren(def: {
                        mb.buildList {
                            child('Jic')
                            child('Joc')
                            child('Jac')
                        }
                    })
                            {
                                child(schema: 'child')
                            }
                }
            }
        }

        def parent1 = mb.build {
            parent(name: 'Lists of Children') {
                child('Jöb')
            }
        }
        def parent2 = mb.build {
            parent(name: 'Lists of Children') {
            }
        }

        assertEquals(1, parent1.listOfChildren.size())
        assertEquals(3, parent2.listOfChildren.size())
    }

    public static void main(String[] args) {
        try {
            CollectionAttributesTest t = new CollectionAttributesTest()
            t.test1()
            t.test2()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }

}
