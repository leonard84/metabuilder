package groovytools.builder

import spock.lang.Specification

class FactorySpec extends Specification {

	MetaBuilder mb = new MetaBuilder()
		
	def "Fully Qualyfied Classname as Factory"() {
		given:
		mb.define {
			fact(factory: 'groovytools.builder.FactoryTestParent') {				
			}
		}
		
		when:
		def fact = buildParent()
		
		then:
		assertParent(fact)
	}
	
	def "Class reference as Factory"() {
		given:
		mb.define {
			fact(factory: FactoryTestParent) {
			}
		}
		
		when:
		def fact = buildParent()
		
		then:
		assertParent(fact)
	}
	
	def "Closure no arg as Factory"() {
		given:
		mb.define {
			fact(factory: {new FactoryTestParent()}) {
			}
		}
		
		when:
		def fact = buildParent()
		
		then:
		assertParent(fact)
	}
	
	def "Closure one arg as Factory"() {
		given:
		mb.define {
			fact(factory: { n -> new FactoryTestParent()}) {
			}
		}
		
		when:
		def fact = buildParent()
		
		then:
		assertParent(fact)
	}

	
	def "Closure two arg as Factory"() {
		given:
		mb.define {
			fact(factory: { n, v -> new FactoryTestParent()}) {
			}
		}
		
		when:
		def fact = buildParent()
		
		then:
		assertParent(fact)
	}
	
	
	def "Closure three arg as Factory"() {
		given:
		mb.define {
			fact(factory: { n, v, a -> new FactoryTestParent()}) {
			}
		}
		
		when:
		def fact = buildParent()
		
		then:
		assertParent(fact)
	}
	
	def "Fully Qualyfied Classname as Factory for properties"() {
		given:
		mb.define {
			fact(factory: FactoryTestParent) {
				properties {
					child(factory: 'groovytools.builder.FactoryTestChild')
				}
			}
		}
		
		when:
		def fact = buildParentWithChild()
	
		then:
		assertChild(fact)
	}
	
	def "Class reference as Factory for properties"() {
		given:
		mb.define {
			fact(factory: FactoryTestParent) {
				properties {
					child(factory: FactoryTestChild)
				}
			}
		}
		
		when:
		def fact = buildParentWithChild()
		
		then:
		assertChild(fact)
	}
	
	def "Closure no arg as Factory for properties"() {
		given:
		mb.define {
			fact(factory: FactoryTestParent) {
				properties {
					child(factory: {new FactoryTestChild()})
				}
			}
		}
		
		when:
		def fact = buildParentWithChild()
		
		then:
		assertChild(fact)
	}
	
	def "Closure one arg as Factory for properties"() {
		given:
		mb.define {
			fact(factory: FactoryTestParent) {
				properties {
					child(factory: { n ->new FactoryTestChild()})
				}
			}
		}
		
		when:
		def fact = buildParentWithChild()
		
		then:
		assertChild(fact)
	}

	
	def "Closure two arg as Factory for properties"() {
		given:
		mb.define {
			fact(factory: FactoryTestParent) {
				properties {
					child(factory: { n, v -> new FactoryTestChild()})
				}
			}
		}
		
		when:
		def fact = buildParentWithChild()
		
		then:
		assertChild(fact)
	}
	
	
	def "Closure three arg as Factory for properties"() {
		given:
		mb.define {
			fact(factory: FactoryTestParent) {
				properties {
					child(factory: { n, v, a -> new FactoryTestChild()})
				}
			}
		}
		
		when:
		def fact = buildParentWithChild()
		
		then:
		assertChild(fact)
	}
	
	
	private def buildParent() {
		mb.build {
			fact()
		}
	}
	
	private def buildParentWithChild() {
		mb.build {
			fact {
				child()
			}
		}
	}
	
	private def assertParent(fact) {
		assert fact != null
		assert fact instanceof FactoryTestParent : 'Parent has correct class'
		return true
	}

	private def assertChild(fact) {
		assert fact != null
		assert fact instanceof FactoryTestParent : 'Parent has correct class'

		assert fact.child != null
		assert fact.child instanceof FactoryTestChild : 'Child has correct class'
		return true
	}
}

class FactoryTestParent {
	String name
	FactoryTestChild child
}

class FactoryTestChild {
	String name
}