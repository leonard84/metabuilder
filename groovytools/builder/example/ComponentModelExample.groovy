/**
 * An example demonstrating defining and building a non-trivial component model.
 *
 * @author didge
 * @version $Id$
 */

import groovytools.builder.*;

MetaBuilder mb = new MetaBuilder(classLoader: getClass().classLoader)

class Component {
    String name
    int x, y, w, h;
}

class Label extends Component {
    String text
}

class Container extends Component {
    String parentAssocRef
    boolean border
    def components = []
    Component title
}

class Panel extends Container {
}

def componentSchema = mb.define {
    component(factory: Component) {
        properties {
            name()
            x(def: 0)
            y(def: 0)
            w(def: 1)
            h(def: 1)
        }
    }
}
def aComponent = mb.build {
    component(name: 'aComponent', x: 1, y: 2, w: 1, h: 1)
}

def labelSchema = mb.define {
    label(schema: componentSchema, factory: Label) {
        properties {
            text()
        }
    }
}

def containerSchema = mb.define {
    container(schema: componentSchema, factory: Container) {
        properties {
            parentAssocRef()
            border()
            title()
        }
        collections {
            components {
                label(schema: labelSchema)
                panel(schema: 'panel')
            }
        }
    }
}
println containerSchema

def panelSchema = mb.define {
    panel(schema: containerSchema, factory: Panel)
}

def aContainer = mb.build {
    container(name: 'aContainer', parentAssocRef: 'someParent', border: true,
        title: mb.build { label(name: 'titleLabel') }
    ) {
        components {
            label(name: 'label2')
            panel(name: 'panel1')
        }
    }
}
