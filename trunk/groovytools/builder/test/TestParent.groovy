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

    public void addChildToMap(Object key, TestChild child) {
       mapOfChildren.put(key, child) 
    }
}
