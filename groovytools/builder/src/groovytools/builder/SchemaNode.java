package groovytools.builder;

import groovy.util.*;

import java.util.*;

/**
 * {@link SchemaNode} simply extends {@link Node} with some extra functionality to support cyclical graphs
 *
 * @author didge
 * @version $REV$
 */
public class SchemaNode extends Node{

    protected SchemaNode _parent;

    public SchemaNode(SchemaNode parent, Object name) {
        super(parent, name);
        _parent = parent;
    }

    public SchemaNode(SchemaNode parent, Object name, Object value) {
        super(parent, name, value);
        _parent = parent;
    }

    public SchemaNode(SchemaNode parent, Object name, Map attributes) {
        super(parent, name, attributes);
        _parent = parent;
    }

    public SchemaNode(SchemaNode parent, Object name, Map attributes, Object value) {
        super(parent, name, attributes, value);
        _parent = parent;
    }

    public Node appendNode(Object name, Map attributes) {
        return new SchemaNode(this, name, attributes);
    }

    public Node appendNode(Object name) {
        return new SchemaNode(this, name);
    }

    public Node appendNode(Object name, Object value) {
        return new SchemaNode(this, name, value);
    }

    public Node appendNode(Object name, Map attributes, Object value) {
        return new SchemaNode(this, name, attributes, value);
    }

    public Node appendNode(SchemaNode node) {
        SchemaNode nodeParent = node._parent;
        if(nodeParent != null) {
            Object oldParentsChildren = nodeParent.children();
            if(oldParentsChildren instanceof List) {
                ((List)oldParentsChildren).remove(node);

            }
        }
        List list = null;
        Object value = value();
        if (value instanceof List) {
            list = (List) value;
        } else {
            list = new NodeList();
            list.add(value);
            setValue(list);
        }
        list.add(node);
        return node;
    }

    /**
     * Returns the {@link SchemaNode}'s parent.
     *
     * @return see above
     */
    public Node parent() {
        return _parent;
    }
}
