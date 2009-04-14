package groovytools.builder

class Contact {
    def name
    def phone
}

MetaBuilder mb = new MetaBuilder(getClass().getClassLoader())

mb.define {
    contact(factory: Contact, check: { it.name == "Doe" && it.phone == "555" } ) {
        properties {
            name()
            phone()
        }
    }
}

def aContact = mb.build {
    contact {
        name = "Doe"
        phone = "111" // when the check closure is executed the name property value is not set yet
    }
}
