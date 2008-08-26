import groovytools.builder.MetaBuilder

/**
 * Tests the schema attribute.
 */
class SchemaAttributeTest extends GroovyTestCase {
    public void test1() {
        MetaBuilder mb = new MetaBuilder()
        mb.define {
            original(factory: TestParent) {
                properties {
                    name(def: 'original')
                }
                collections {
                    listOfChildren {
                        child(schema: 'original')
                    }
                }
            }

            extended(schema: 'original') {
                properties {
                    name(def: 'extended')
                }
                collections {
                    listOfChildren {
                        child(schema: 'extended')
                    }
                }
            }
        }

        def obj = mb.build {
            extended() {
                listOfChildren {
                    child()
                    child()
                    child()
                }
            }
        }

        assertTrue(obj instanceof TestParent)
        assertEquals('extended', obj.name)
        assertTrue(obj.listOfChildren[0] instanceof TestParent)
        assertEquals('extended', obj.listOfChildren[0].name)
    }

    public static void main(String[] args) {
        try {
            SchemaAttributeTest t = new SchemaAttributeTest()
            t.test1()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}