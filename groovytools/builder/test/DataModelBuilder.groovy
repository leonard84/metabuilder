import groovytools.builder.*
public class DataModelBuilder {
    MetaBuilder mb = new MetaBuilder(getClass().getClassLoader())
    DataModelBuilder(){
        mb.define{
            dataModel(factory:DataModel){
                properties{
                    merge(property:{
                        object,value->
                        object.merge(value)
                    })
                }
                collections{
                    tables(key:'name'){
                        table(schema:'table')
                    }
                    relations(){
                        relation(schema:'relation')
                    }
                }
            }
            table(factory:Table){
                properties{
                    name()
                }
                collections{
                    columns{
                        column(schema:'column')
                    }
                }
            }
            relation(factory:Relation){
                properties{
                    from()
                    to()
                }
            }
            column(factory:Column){
                properties{
                    name()
                }
            }
        }
    }
    def build=mb.&build
}
class Relation{
    String to
    String from
}
class DataModel{
    def tables=[:]
    def relations=[]
}
class Table{
    def name
    def columns=[]
}
class Column{
    def name
}

