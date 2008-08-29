/**
 * @author didge
 * @version $Id$
 */
import junit.framework.*
def suite = AllTestSuite.suite('test', "*Test.groovy");
junit.textui.TestRunner.run(suite)
