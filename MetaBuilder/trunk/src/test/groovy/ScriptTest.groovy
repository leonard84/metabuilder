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
        def obj = mb.buildList(new File('test/BuildScript.groovy').toURL())
        def orders = [:]
        mb.build({ if(it.isRoot) {orders[it.node.id] = it.node}; it.node }, new File('test/BuildScript.groovy').toURL())
        println(orders)
        def moreOrders = mb.buildList(new File('test/BuildScript.groovy').toURL())
        println(moreOrders)
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