import groovytools.builder.MetaBuilder

/**
 */
class ScriptTest extends GroovyTestCase {
    public void test1() {
        ClassLoader classLoader = getClass().classLoader
        GroovyScriptEngine gse = new GroovyScriptEngine('test', classLoader)
        Class defineScript = gse.loadScriptByName('DefineScript')
        Class buildScript = gse.loadScriptByName('BuildScript')

        MetaBuilder mb = new MetaBuilder(classLoader)

        mb.define(defineScript)
        def obj = mb.build(buildScript)
        println(obj)
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