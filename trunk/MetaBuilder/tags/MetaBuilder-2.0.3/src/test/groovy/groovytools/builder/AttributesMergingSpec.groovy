package groovytools.builder

import groovytools.builder.support.TestChild;
import groovytools.builder.support.TestParent;
import spock.lang.Specification

class AttributesMergingSpec extends Specification {

	def "Stronger child's collection constraint shouldn't be propagated to parent"() {
		given:
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
		
		when:
		
		// Should work
		def parentConstrained = mb.build {
			parentConstrained (name:'parentConstrainedName'){
			  child(name:'name1')
			  child(name:'name2')
			}
		}
		
		
		then:
		'parentConstrainedName' == parentConstrained.name
		
		when:		
		// Should also work without a
		// (Collection 'listOfChildren': min check failed)
		def parent = mb.build {
			parent (name:'parentName')
		}
		
		then:
		'parentName' == parent.name
	}
	
	def "Child should not override default parent value"() {
		given:
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
		
		when:
		def parent1 = mb.build {
			parent()
		}
		
		then:
		'parentName' == parent1.name
		
		
		when:
		def grandParent = mb.build {
			grandParent()
		}
		then:
		'grandParentName' == grandParent.name
		
		when:
		def parent2 = mb.build {
			parent()
		}
		then:
		'parentName' == parent2.name  //Name should not be overridden
	}
}
