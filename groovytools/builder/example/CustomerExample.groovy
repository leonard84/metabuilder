import groovytools.builder.*

MetaBuilder mb = new MetaBuilder()

mb.define {
    customer {
        properties {
            name(req: true)
            age(check: 1..120)
            sex(check: ~/M|F/)
        }
    }
}

def aCustomer = mb.build {
    customer(name: 'J. Doe', age: 25, sex: 'M')
    customer(name: 'J. Doe', age: 25, sex: 'M')
}

println(aCustomer)