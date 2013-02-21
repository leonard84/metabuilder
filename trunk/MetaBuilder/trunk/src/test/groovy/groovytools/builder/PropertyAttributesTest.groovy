package groovytools.builder
import spock.lang.Specification;
import groovytools.builder.MetaBuilder

/**
 * Tests various attributes of properties.
 *
 * @author didge
 * @version $Id: PropertyAttributesTest.groovy 73 2013-02-01 19:55:48Z damokles $
 */
class PropertyAttributesTest extends Specification {
	
	MetaBuilder mb = new MetaBuilder()

	def setup() {
		mb.define {
			child(factory: 'TestChild') {
				properties {
					name()
				}
			}
			parent(factory: 'TestParent') {
				properties {
					name()
					name2(property: 'name', check: ~/Lists.*/)
					name3(property: { b, v -> b.setName(v) } )
					listOfChildren()
					onlyChild(schema: 'child')
					stepChild(property: 'onlyChild', schema: 'child')
					otherChild(property: { b, v -> b.setOnlyChild(v) } , schema: 'child')
				}
			}
		}
	}
	
	def "Node name should be used as default"() {
		when:
		def parent = mb.build { parent(name: 'Lists of Children1') }
		
		then:
		parent.name == 'Lists of Children1'
	}
	
	def "Node name should be used as default for nested properties"() {
		when:
		def parent = mb.build { 
			parent {
				onlyChild(name: 'derp')
			 }
		}
		then:
		parent.onlyChild.name == 'derp'
	}
	
	def "Property String attribute should be respected"() {
		when:
		def parent = mb.build { parent(name2: 'Lists of Children2') }
		
		then:
		parent.name == 'Lists of Children2'
	}
	
	
	def "Property String attribute should be respected for nested properties"() {
		when:
		def parent = mb.build { 
			parent {
				stepChild(name: 'herp')
			 }
		}
			
		then:
		parent.onlyChild.name == 'herp'
	}
	
	def "Property Closure attribute should be respected"() {
		when:
		def parent = mb.build { parent(name3: 'Lists of Children2') }
		
		then:
		parent.name == 'Lists of Children2'
	}
	
	
	def "Property Closure attribute should be respected for nested properties"() {
		when:
		def parent = mb.build {
			parent {
				otherChild(name: 'twerp')
			 }
		}
			
		then:
		parent.onlyChild.name == 'twerp'
	}
}
