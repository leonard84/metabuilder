import com.seventytwomiles.architecturerules.configuration.*
import com.seventytwomiles.architecturerules.domain.*
import groovytools.builder.*

/**
 */
MetaBuilder mb = new MetaBuilder()

mb.define {
    architecture(factory: Configuration) {
        properties {
            doCyclicDependencyTest(def: true)
            throwExceptionWhenNoPackages(def: true)
        }
        collections {
            sources {
                source(factory: SourceDirectory) {
                    properties {
                        path()
                        notFound(def: 'exception')
                    }
                }
            }
            rules {
                rule(factory: Rule) {
                    properties {
                        id()
                        comment()
                    }
                    collections {
                        packages(add: 'addPackage') {
                            'package'(factory: {n, v -> v })
                        }
                        violations(add: {p, c -> p.addViolation(c) }) {
                            violation(factory: {n, v -> new JPackage(v)})
                        }
                    }
                }
            }
        }
    }
}

Configuration c = (Configuration) mb.build {
    architecture {
        sources {
            source(path: "spring.jar")
        }

        rules {
            rule(id: "beans-web", comment: "org.springframework.beans.factory cannot depend on org.springframework.web") {
                packages {
                    'package'('org.springframework.beans')
                }
                violations {
                    violation('org.springframework.web')
                }
            }

            rule(id: 'must-fail', comment: "org.springframework.orm.hibernate3 cannot depend on org.springframework.core.io") {
                packages {
                    'package'('org.springframework.orm.hibernate3')
                }
                violations {
                    violation('org.springframework.core.io')
                }
            }
        }
    }
}
println(c)
