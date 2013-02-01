/**
 * Simple bean for testing.
 * 
 * @author didge
 * @version $Id$
 */
public class TestParent {
    def name
    def listOfChildren = []
    def mapOfChildren = [:]

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
