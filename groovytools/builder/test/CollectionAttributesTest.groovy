import groovytools.builder.MetaBuilder

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
                    name()
                }
            }
            parent(factory: TestParent) {
                properties {
                    name()
                    listOfChildrenAsProperty(property: 'listOfChildren')
                }
                collections {
                    listOfChildren { // simple example of a collection of child objects
                        child(schema: 'child')
                    }
                    listOfChildren2(collection: "listOfChildren") { // uses the collection above
                        child(schema: 'child')
                    }
                    listOfChildren3(collection: {p -> p.getListOfChildren() }) {
                        child(schema: 'child')
                    }
                    listOfChildren4(add: 'addChildToList') {
                        child(schema: 'child')
                    }
                    listOfChildren5(add: {p, c -> p.addChildToList(c) }) {
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
            parent (name: 'Lists of Children', listOfChildrenAsProperty: ['Jeb', 'Job']) {
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

        assertEquals(6, parent1.listOfChildren.size())
        assertEquals(5, parent1.mapOfChildren.size())
    }

    public static void main(String[] args) {
        try {
            CollectionAttributesTest t = new CollectionAttributesTest()
            t.test1()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
