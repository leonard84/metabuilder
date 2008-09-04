import groovytools.builder.MetaBuilder

/**
 * Tests loading external definitions and build scripts.
 *
 * @author didge
 * @version $Id$
 */
class ScriptTest extends GroovyTestCase {
    public void test1() {
        MetaBuilder mb = new MetaBuilder()
        mb.define(new File('test/DefineScript.groovy').toURL())
        def obj = mb.build(new File('test/BuildScript.groovy').toURL())
    }

    public static void main(String[] args) {
        try {
            ScriptTest t = new ScriptTest()
            t.test1()
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}