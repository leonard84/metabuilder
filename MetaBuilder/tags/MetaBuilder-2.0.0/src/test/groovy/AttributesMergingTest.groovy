import groovytools.builder.MetaBuilder

/**
 * Test ovverriden on collection inheritance
 *
 * @author Stefan Ivanov
 */
class AttributesMergingTest extends GroovyTestCase {

    /**
     * Stronger child's collection constraint shouldn't be propagated to parent
     */
    public void testMergingParentCollections() {
        MetaBuilder mb = new MetaBuilder()
        
        mb.define {
            child(factory: TestChild) {
                properties {
                    name()
                }                
            }
        }
        
        mb.define {
            parent(factory: TestParent) {
                properties {
                    name()
                }
                collections {
                    listOfChildren {
                        child(schema: 'child')
                    }
                }
            }
        }
            
        // Add collection constraint
        mb.define {
            parentConstrained(schema:'parent'){
                collections {
                    listOfChildren(min:1){
                        child(schema: 'child')
                    }
                }
            }
            
        }

        // Works
        def parentConstrained = mb.build {
            parentConstrained (name:'parentConstrainedName'){
              child(name:'name1')
              child(name:'name2')
            } 
        }

        assertEquals 'parentConstrainedName', parentConstrained.name

        // FAILS (Collection 'listOfChildren': min check failed)
        def parent = mb.build {
            parent (name:'parentName')
        }
        
        assertEquals 'parentName', parent.name
    }
    
    /**
     * Child should not override default parent value
     */
    public void testMergingParentProperties() {
        MetaBuilder mb = new MetaBuilder()
        mb.define {
            parent(factory: TestParent) {
                properties {
                    name(def:'parentName')
                }
            }
            
            grandParent(schema:'parent'){
                properties {
                    name(def:'grandParentName')
                }
            }
            
        }
        
        def parent1 = mb.build {
            parent()
        }
        assertEquals 'parentName', parent1.name  //OK
        
        def grandParent = mb.build {
            grandParent()
        }
        assertEquals 'grandParentName', grandParent.name  //OK
        
        def parent2 = mb.build {
            parent()
        }
        assertEquals 'parentName', parent2.name  //NOT OK
    }

    public static void main(String[] args) {
        try {
            AttributesMergingTest t = new AttributesMergingTest()
            t.testMergingParentCollections()
            t.testMergingParentProperties()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
