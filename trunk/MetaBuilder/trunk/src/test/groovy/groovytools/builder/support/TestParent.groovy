package groovytools.builder.support
/**
 * Simple bean for testing.
 * 
 * @author didge
 * @version $Id: TestParent.groovy 84 2013-02-22 17:25:48Z damokles $
 */
public class TestParent {
    String name
    List listOfChildren = []
    Map mapOfChildren = [:]
	TestChild onlyChild

    public void addChildToList(TestChild child) {
        listOfChildren.add(child)
    }

    public int getListSize() {
        return listOfChildren.size()
    }

    public void addChildToMap(Object key, TestChild child) {
       mapOfChildren.put(key, child) 
    }

    public int getMapSize() {
        return mapOfChildren.size()
    }

}
