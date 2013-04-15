package groovytools.builder

import groovytools.builder.support.TestChild
import groovytools.builder.support.TestParent

import spock.lang.Specification

class CollectionMapAddSpec extends Specification {

	MetaBuilder mb = defaultMetaBuilder()

	def "Simple Map"() {
		given:
		mb.define {
			parent(factory: TestParent) {
				collections {
                    mapOfChildren(key: 'name')  { // simple example of a collection of child objects
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
		parent1.mapOfChildren.values()*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                mapOfChildren {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.mapOfChildren.values()*.name == ['Jeb', 'Job']
	}

    def "Map with different name defined by string property"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                    mapOfChildren2(collection: 'mapOfChildren', key: 'name') { // simple example of a collection of child objects
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
        parent1.mapOfChildren.values()*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                mapOfChildren2 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.mapOfChildren.values()*.name == ['Jeb', 'Job']
    }

    def "Map with different name via closure getter"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                    mapOfChildren3(collection: {p -> p.getMapOfChildren() }, key: 'name') { // simple example of a collection of child objects
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
        parent1.mapOfChildren.values()*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                mapOfChildren3 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.mapOfChildren.values()*.name == ['Jeb', 'Job']
    }

    def "Add to map via function in parent class"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                    mapOfChildren4(add: 'addChildToMap', key: 'name') { // simple example of a collection of child objects
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
        parent1.mapOfChildren.values()*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                mapOfChildren4 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.mapOfChildren.values()*.name == ['Jeb', 'Job']
    }

    def "Map with different name via closure and key"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                     mapOfChildren5(add: {p, k, c -> p.addChildToMap(k, c) }, key: 'name') { // simple example of a collection of child objects
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
        parent1.mapOfChildren.values()*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                mapOfChildren5 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.mapOfChildren.values()*.name == ['Jeb', 'Job']
    }

    def "Map with different name via closure only"() {
        given:
        mb.define {
            parent(factory: TestParent) {
                collections {
                      mapOfChildren6(add: {p, c -> p.addChildToMap(c.name, c) }) { // simple example of a collection of child objects
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
        parent1.mapOfChildren.values()*.name == ['Jeb', 'Job']


        when: //with outer collection block
        TestParent parent2 = mb.build {
            parent {
                mapOfChildren6 {
                    child(name: 'Jeb')
                    child(name: 'Job')
                }
            }
        }

        then:
        parent2.mapOfChildren.values()*.name == ['Jeb', 'Job']
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
