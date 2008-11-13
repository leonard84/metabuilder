import groovytools.builder.*

class Talk {
    def name
    def desc
    Talk(def name) { this.name = name }
}

class Speaker {
    def name
    def talks = []
    Speaker(def name) { this.name = name }
}


MetaBuilder mb = new MetaBuilder(getClass().classLoader)

mb.define {
    '%'(factory: { new Speaker(it) }) {
        collections {
            talks {
                '%'(factory: { new Talk(it) } ) {
                    properties {
                        desc()
                    }
                }
            }
        }
    }
}

def speakers = mb.buildList {
    Duffy {
        talks {
            'A Great Talk' (desc: 'A great talk!')
            'A Wonderful Speech' (desc: 'A wonderful speech!')
        }
    }
}

speakers.each { speaker ->
    println speaker.name
    speaker.talks.each { talk ->
        println '  ' + talk.name
    }
}
