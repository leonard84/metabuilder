import groovytools.builder.*

MetaBuilder mb = new MetaBuilder()

mb.define {
    customer (check: { v -> v.'@name' != null } ) {
        properties {
            name(req: true)
            age(check: 1..120)
            sex(check: ~/M|F/)
        }
    }
}

def aCustomer = mb.build {
    customer(name: 'J. Doe', age: null, sex: 'M')
    customer(name: null, age: 25, sex: 'M')
}

println(aCustomer)
