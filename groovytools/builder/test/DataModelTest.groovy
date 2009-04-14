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

class DataModelTest extends GroovyTestCase{
    void testBuilder(){
        def dataModelBuilder=new DataModelBuilder()
        def dm= dataModelBuilder.build{
            dataModel{
                table(name:'table1'){
                    column(name:'col1')
                    column(name:'table1.col2')
                }
                table(name:'ta')
                table(name:'3rd')
                relation(from:'table1',to:'ta')
                relation(from:'table1',to:'srd')
            }
        }
        assert dm!=null
        assert dm.tables['table1']!=null : "$dm.tables"
        assert dm.tables['table1'].name=='table1'
        assert dm.tables['ta']!=null
        assert dm.tables.ta.name=='ta'
        assert dm.tables.table1!=null
        assert dm.tables.table1.columns
        assert dm.tables.table1.columns[0].name=='col1'
    }
}
