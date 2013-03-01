/**
 * @author didge
 * @version $Id$
 */
import junit.framework.*
def suite = AllTestSuite.suite('groovy', "*Test.groovy");
junit.textui.TestRunner.run(suite)
