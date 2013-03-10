package groovytools.builder;

import groovy.transform.Canonical;
import spock.lang.Specification;

class CollectionInitializationSpec extends Specification {
	
	MetaBuilder mb
	
	def setup() {
		mb = new MetaBuilder()
		mb.define {
			item(factory: CollectionTestItem) {
				properties {
					sku(req: true)
					price(def: "1.0")
				}
			}
		}
	}

	def "Lists: Initialized lists should be used"() {
		given:
		mb.define {
			wrapper(factory: CollectionTestClass) {
				collections {
					items(collection:'initialzedList'){
						item(schema: 'item')
					}
				}
			}
		}
		
		when:
		CollectionTestClass result = performBuild()
		
		then:	
		result.initialzedList[0] == new CollectionTestItem(sku: "pre", price: "5.0")
		result.initialzedList[1] == new CollectionTestItem(sku: "new", price: "2.0")
	}
	
	def "Lists: Initialized lists should be used, even when factory is defined"() {
		given:
		mb.define {
			wrapper(factory: CollectionTestClass) {
				collections {
					items(collection:'initialzedList', factory: {[]}){
						item(schema: 'item')
					}
				}
			}
		}
		
		when:
		CollectionTestClass result = performBuild()
		
		then:
		result.initialzedList[0] == new CollectionTestItem(sku: "pre", price: "5.0")
		result.initialzedList[1] == new CollectionTestItem(sku: "new", price: "2.0")
	}
	
	def "Lists: Factory should be used for non initialized lists"() {
		given:
		mb.define {
			wrapper(factory: CollectionTestClass) {
				collections {
					items(collection:'nonInitialzedList', factory: {[]}){
						item(schema: 'item')
					}
				}
			}
		}
		
		when:
		CollectionTestClass result = performBuild()
		
		then:
		result.nonInitialzedList[0] == new CollectionTestItem(sku: "new", price: "2.0")
	}
	
	def "Lists: Non initialized lists should be auto initialzed"() {
		given:
		mb.define {
			wrapper(factory: CollectionTestClass) {
				collections {
					items(collection:'nonInitialzedList'){
						item(schema: 'item')
					}
				}
			}
		}
		
		when:
		CollectionTestClass result = performBuild()
		
		then:
		result.nonInitialzedList[0] == new CollectionTestItem(sku: "new", price: "2.0")
	}
	
	def "Maps: Initialized maps should be used"() {
		given:
		mb.define {
			wrapper(factory: CollectionTestClass) {
				collections {
					items(collection:'initialzedMap', key:'sku'){
						item(schema: 'item')
					}
				}
			}
		}
		
		when:
		CollectionTestClass result = performBuild()
		
		then:
		result.initialzedMap["pre"] == new CollectionTestItem(sku: "pre", price: "5.0")
		result.initialzedMap["new"] == new CollectionTestItem(sku: "new", price: "2.0")
	}
	
	def "Maps: Initialized maps should be used, even when factory is defined"() {
		given:
		mb.define {
			wrapper(factory: CollectionTestClass) {
				collections {
					items(collection:'initialzedMap', key:'sku', factory: {[:]}){
						item(schema: 'item')
					}
				}
			}
		}
		
		when:
		CollectionTestClass result = performBuild()
		
		then:
		result.initialzedMap["pre"] == new CollectionTestItem(sku: "pre", price: "5.0")
		result.initialzedMap["new"] == new CollectionTestItem(sku: "new", price: "2.0")
	}
	
	def "Maps: Factory should be used for non initialized maps"() {
		given:
		mb.define {
			wrapper(factory: CollectionTestClass) {
				collections {
					items(collection:'nonInitialzedMap', key:'sku', factory: {[:]}){
						item(schema: 'item')
					}
				}
			}
		}
		
		when:
		CollectionTestClass result = performBuild()
		
		then:
		result.nonInitialzedMap["new"] == new CollectionTestItem(sku: "new", price: "2.0")
	}
	
	def "Maps: Non initialized maps should be auto initialzed"() {
		given:
		mb.define {
			wrapper(factory: CollectionTestClass) {
				collections {
					items(collection:'nonInitialzedMap', key:'sku'){
						item(schema: 'item')
					}
				}
			}
		}
		
		when:
		CollectionTestClass result = performBuild()
		
		then:
		result.nonInitialzedMap["new"] == new CollectionTestItem(sku: "new", price: "2.0")
	}
	
	private CollectionTestClass performBuild() {
		mb.build {
			wrapper {
				item(sku: "new") {
					price = "2.0"
				}
			}
		}
	}
	
}

@Canonical
class CollectionTestItem {
	String sku
	String price
}

@Canonical
class CollectionTestClass {
	List initialzedList = [new CollectionTestItem(sku: "pre", price: "5.0")]
	List nonInitialzedList
	Map initialzedMap = ["pre":new CollectionTestItem(sku: "pre", price: "5.0")]
	Map nonInitialzedMap
} 

