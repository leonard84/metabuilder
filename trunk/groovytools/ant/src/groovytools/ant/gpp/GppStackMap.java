package groovytools.ant.gpp;

import java.util.HashMap;

/**
 * Quick and dirty Stack of Maps.
 */
public class GppStackMap extends HashMap {
    protected GppStackMap _parent;

    public GppStackMap() {
        _parent = this;
    }

    public GppStackMap(GppStackMap parent) {
        _parent = parent;
    }

    protected Object superPut(Object key, Object value) {
        return super.put(key, value);
    }

    public Object put(Object key, Object value) {
        return _parent.superPut(key, value);
    }

    protected Object superGet(Object key) {
        return super.get(key);
    }

    public Object get(Object key) {
        GppStackMap map = _parent;
        do {
            if(map.containsKey(key)) {
                return map.superGet(key);
            }
            else if(map != this) {
                map = map._parent;
            }
            else {
                break;
            }
        } while(true);

        return null;
    }

    public void clear() {
        _parent.superClear();
    }

    protected void superClear() {
        super.clear();
    }

    public void push() {
        _parent = new GppStackMap(_parent);
    }

    public void pop() {
        _parent.superClear();
        _parent = _parent._parent;
    }

    public void popAll() {
        while(_parent != this) {
            pop();
        }
        super.clear();
    }

    public static void main(String[] args) {
        try {
            GppStackMap map = new GppStackMap();
            map.put("foo", "foo");
            map.push();
            map.push();
            map.push();
            map.put("foo", "foo2");
            System.out.println(map.get("foo"));
        }
        catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    public String toString() {
        return "t=" + Integer.toHexString(System.identityHashCode(this)) + " p=" + Integer.toHexString(System.identityHashCode(_parent));
    }

}
