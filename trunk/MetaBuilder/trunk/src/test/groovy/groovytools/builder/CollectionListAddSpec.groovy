package groovytools.builder

import groovytools.builder.support.TestChild
import groovytools.builder.support.TestParent

import java.lang.invoke.MethodHandleImpl.BindCaller.T

import spock.lang.Specification

class CollectionListAddSpec extends Specification {

	MetaBuilder mb = defaultMetaBuilder()

	def "Simple List"() {
		given:
		mb.define {
			parent(factory: TestParent) {
				collections {
                    listOfChildren() { // simple example of a collection of child objects
						child(schema: 'child')
					}
				}
			}
		}

		when: //without outer collection block
		TestParent parent1 = mb.build {
			parent {
                child(name: 'Jeb')
                child(name: 'Job')
            }
		}

		then:
		parent1.listOfChildren*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                listOfChildren {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.listOfChildren*.name == ['Jeb', 'Job']
	}

    def "List with different name defined by string property"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                    listOfChildren2(collection: 'listOfChildren') { // simple example of a collection of child objects
                        child(schema: 'child')
                    }
                }
            }
        }

        when: //without outer collection block
        TestParent parent1 = mb.build {
            parent {
                child(name: 'Jeb')
                child(name: 'Job')
            }
        }

        then:
        parent1.listOfChildren*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                listOfChildren2 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.listOfChildren*.name == ['Jeb', 'Job']
    }

    def "List with different name via closure getter"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                    listOfChildren3(collection: {p -> p.getListOfChildren() }) { // simple example of a collection of child objects
                        child(schema: 'child')
                    }
                }
            }
        }

        when: //without outer collection block
        TestParent parent1 = mb.build {
            parent {
                child(name: 'Jeb')
                child(name: 'Job')
            }
        }

        then:
        parent1.listOfChildren*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                listOfChildren3 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.listOfChildren*.name == ['Jeb', 'Job']
    }

    def "Add to list via function in parent class"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                    listOfChildren4(add: 'addChildToList') { // simple example of a collection of child objects
                        child(schema: 'child')
                    }
                }
            }
        }

        when: //without outer collection block
        TestParent parent1 = mb.build {
            parent {
                child(name: 'Jeb')
                child(name: 'Job')
            }
        }

        then:
        parent1.listOfChildren*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                listOfChildren4 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.listOfChildren*.name == ['Jeb', 'Job']
    }

    def "Add to list via a closure"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                    listOfChildren5(add: {p, c -> p.addChildToList(c) }) { // simple example of a collection of child objects
                        child(schema: 'child')
                    }
                }
            }
        }

        when: //without outer collection block
        TestParent parent1 = mb.build {
            parent {
                child(name: 'Jeb')
                child(name: 'Job')
            }
        }

        then:
        parent1.listOfChildren*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                listOfChildren5 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.listOfChildren*.name == ['Jeb', 'Job']
    }

    def "List as Property"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                properties {
                    listOfChildrenAsProperty(property: 'listOfChildren')
                }
            }
        }

        when: //property passed as attribute
        TestParent parent1 = mb.build {
            parent(listOfChildrenAsProperty: [new TestChild(name: 'Jeb'), new TestChild(name: 'Job')])
        }

        then:
        parent1.listOfChildren*.name == ['Jeb', 'Job']

        when: //property assigned via setter
        TestParent parent2 = mb.build {
            parent {
                listOfChildrenAsProperty = [new TestChild(name: 'Jeb'), new TestChild(name: 'Job')]
            }
        }

        then:
        parent2.listOfChildren*.name == ['Jeb', 'Job']
    }

	def "List as Property unchecked"() {
		given:
		mb.define {
			parent(factory: TestParent) {
				properties {
					listOfChildrenAsProperty(property: 'listOfChildren')
				}
			}
		}

		when:
		TestParent parent1 = mb.build {
			parent(listOfChildrenAsProperty: ['Jeb', 'Job'])
		}

		then:
		parent1.listOfChildren == ['Jeb', 'Job']

		when:
		TestParent parent2 = mb.build {
			parent {
				listOfChildrenAsProperty = ['Jeb', 'Job']
			}
		}

		then:
		parent2.listOfChildren == ['Jeb', 'Job']
	}

	private MetaBuilder defaultMetaBuilder() {
		MetaBuilder mb = new MetaBuilder()
		def parentDef = mb.define {
			child(factory: TestChild) {
				properties { name(min: 3) }
			}
		}
		return mb
	}
}
