package groovytools.builder;

import groovy.lang.*;
import groovy.util.*;
import org.codehaus.groovy.runtime.*;

import java.util.*;

/**
 */
public class CollectionSchemaNode extends SchemaNode implements Factory {
    protected Object parentBean;

    public CollectionSchemaNode(SchemaNode parent, Object name) {
        super(parent, name);
    }

    public CollectionSchemaNode(SchemaNode parent, Object name, Object value) {
        super(parent, name, value);
    }

    public CollectionSchemaNode(SchemaNode parent, Object name, Map attributes) {
        super(parent, name, attributes);
    }

    public CollectionSchemaNode(SchemaNode parent, Object name, Map attributes, Object value) {
        super(parent, name, attributes, value);
    }

    public boolean isLeaf() {
        return false;
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        return this;
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map attributes) {
        return false;
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
    }

    public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
        parentBean = parent;
    }

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        String collectionNodeName = (String)name();

        String collectionName = (String)attribute("collection");
        if(collectionName == null) {
            collectionName = collectionNodeName;
        }

        Object property = InvokerHelper.getProperty(parentBean, collectionName);
        if(property != null) {
            if(Collection.class.isAssignableFrom(property.getClass())) {
                ((Collection)property).add(child);
            }
            else if(Map.class.isAssignableFrom(property.getClass())) {
                Closure c = (Closure)attribute("key");
                Object key = c.call(child);
                ((Map)property).put(key, child);
            }
        }
        else {
            InvokerHelper.setProperty(parentBean, collectionNodeName, child);
        }
    }
}