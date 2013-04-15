import groovytools.builder.MetaBuilder

class FactoryTest {
}

//MetaBuilder mb = new MetaBuilder(classLoader: getClass().classLoader)
MetaBuilder mb = new MetaBuilder()

mb.define {
    fact1(factory: { new FactoryTest() })  {
    }
    fact2(factory: 'FactoryTest')  {
    }
    fact3(factory: FactoryTest )  {
    }
}

def fact0 = new FactoryTest()

def fact1 = mb.build {
    fact1 {
    }
}

def fact2 = mb.build {
    fact2 {
    }
}
def fact3 = mb.build {
    fact3 {
    }
}

assert fact0.getClass() == fact1.getClass()
assert fact0.getClass() == fact2.getClass()
assert fact0.getClass() == fact3.getClass()
