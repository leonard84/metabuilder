package groovytools.builder.support
/**
 * Simple bean for testing.
 *
 * @author didge
 * @version $Id: TestChild.groovy 73 2013-02-01 19:55:48Z damokles $
 */
class TestChild {
    String name

    TestChild() {
        name = null
    }

    TestChild(String name) {
        this.name = name
    }

}