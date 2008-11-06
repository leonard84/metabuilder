import com.seventytwomiles.architecturerules.configuration.*
import com.seventytwomiles.architecturerules.domain.*
import groovytools.builder.*

architecture(factory: Configuration) {
    properties {
        doCyclicDependencyTest(def: true)
        throwExceptionWhenNoPackages(def: true)
    }
    collections {
        sources {
            jar(factory: { n, v -> new SourceDirectory(v) }) {
                properties {
                    notFound(def: 'exception')
                }
            }
        }
        rules {
            '%' (factory: { n -> new Rule(n) } ) {
                properties {
                    id()
                    comment()
                }
                collections {
                    packages(add: 'addPackage', min: 1) {
                        'package'(factory: {n, v -> v })
                    }
                    violations(add: {p, c -> p.addViolation(c) }, min: 1) {
                        violation(factory: {n, v -> new JPackage(v)})
                    }
                }
            }
        }
    }
}

