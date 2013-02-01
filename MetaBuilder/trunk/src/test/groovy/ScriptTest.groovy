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
        mb.define(ScriptTest.class.getResource('DefineScript.dsl'))
        def obj = mb.buildList(ScriptTest.class.getResource('BuildScript.dsl'))
        def orders = [:]
        mb.build({ if(it.isRoot) {orders[it.node.id] = it.node}; it.node }, ScriptTest.class.getResource('BuildScript.dsl'))
        println(orders)
        def moreOrders = mb.buildList(ScriptTest.class.getResource('BuildScript.dsl'))
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