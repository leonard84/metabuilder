import junit.framework.*
def suite = AllTestSuite.suite('.', "*Test.groovy");
junit.textui.TestRunner.run(suite)
