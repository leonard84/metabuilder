package groovytools.builder;

import groovy.lang.*;
import groovy.util.*;
import org.codehaus.groovy.runtime.*;

import java.util.*;

/**
 */
public class CollectionSchemaNode extends SchemaNode implements Factory {
    /**
     * Holder for the actual parent object.
     */
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
        if(value != null) {
            throw new CollectionException("Collection '" + name + "': collections may not be specified with a value.  Use a property instead.");
        }
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

    protected Object getKey(Object keyAttr, Object child) {
        Object key = null;
        if(keyAttr instanceof Closure) {
            Closure keyClosure = (Closure)keyAttr;
            key = keyClosure.call(child);
        }
        else if(keyAttr instanceof String) {
            key = InvokerHelper.getProperty(child, (String)keyAttr);
        }
        return key;
    }

    /**
     * Sets the <code>child</code> on the <code>parent</code>.
     * <p>
     * By default, reflection is used to find collection on the <code>parent</code> using the
     * name of the schema.  For example, if the schema's name is <code>foos</code> then this method will attempt to access
     * the collection using the method, <code>foos()</code>.  The access method of the collection may be overridden with the
     * attribute, <code>collection</code>.  If <code>collection</code> is a {@link String}, then the collection is accessed
     * as a property of the <code>parent</code> using the <code>collection</code> value as the property's name.  However, if the
     * <code>collection</code> attribute value is a {@link Closure} where the first argument is the <code>parent</code>,
     * then the closure will be used to access the collection.
     * <p>
     * When the collection object is not accessible or updateable, the <code>add</code> attribute must be used to specify the name of an
     * alternate method accepting a single argument (the <code>child</code>) or a {@link Closure} accepting two arguments (the <code>parent</code> and the <code>child</code>).
     * <p>
     * For example, if <code>foos()</code> is not available or does not return an updateable collection, then 'add' maybe set to either
     * <code>addFoo</code> or <code>{&nbsp;p,&nbsp;c&nbsp;->&nbsp;p.addFoo(c)&nbsp;}</code>.
     * <p>
     * When using the <code>attribute</code> the <code>collection</code> attribute is ignored since it is superflous.
     * <p>
     * In either case, if the collection is a {@link Map}, then the <code>key</code> attribute must be specified in order
     * to retrieve the <code>child</code>'s key.  The <code>key</code> may either specify a property name or a {@link Closure} accepting one argument (the <code>child</code>).
     * <p>
     * The value returned by calling <code>key</code> on the <code>child</code>, if it exists, is to put the
     * <code>child</code> into the <code>parent</code>'s collection.
     * <p>
     * The following shows the different ways in which to use the attributes described above:
     * <pre>
     * parent {
     *  collections {
     *      listOfChildren { // simple example of a collection of child objects
     *          child()
     *      }
     *      listOfChildren2(collection: 'listOfChildren') { // uses the collection above
     *          child()
     *      }
     *      listOfChildren3(collection: { p -> p.getListOfChildren() } ) {
     *          child()
     *      }
     *      listOfChildren4(add: 'addChild' ) {
     *          child()
     *      }
     *      listOfChildren5(add: { p, c -> p.addChild(c) } ) {
     *          child()
     *      }
     *      mapOfChildren(key: 'name') { // simple example of a Map of child objects, using getName() as the key
     *          child(name: 'Jer')
     *      }
     *      mapOfChildren2(collection: 'mapOfChildren', key: 'name') {
     *          child(name: 'Joe')
     *      }
     *      mapOfChildren3(collection: { p -> getMapOfChildren() }, key: 'name') {
     *          child(name: 'Jen')
     *      }
     *      mapOfChildren4(add: 'addChild', key: 'name') {  // note, addChild called like this: p.addChild(key, child)
     *          child(name: 'Jay')
     *      }
     *      mapOfChildren5(add: { p, k, c -> p.addChild(k, c) }, key: 'name') {
     *          child(name: 'Jan')
     *      }
     *      mapOfChildren6(add: { p, c -> p.addChild(c.getName(), c) }) {
     *          child(name: 'Jon')
     *      }
     * }
     * </pre>
     * @param builder
     * @param parent
     * @param child
     */
    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        Object addAttr = attribute("add");
        Object keyAttr = attribute("key");

        try {
            if(addAttr != null) {
                if(addAttr instanceof Closure) {
                    Closure addClosure = (Closure)addAttr;
                    if(keyAttr != null) {
                        Object key = getKey(keyAttr, child);
                        addClosure.call(new Object[]{parentBean, key, child});
                    }
                    else {
                        addClosure.call(new Object[]{parentBean, child});
                    }
                }
                else if(addAttr instanceof String) {
                    if(keyAttr != null) {
                        Object key = getKey(keyAttr, child);
                        InvokerHelper.invokeMethod(parentBean, (String)addAttr, new Object[]{key, child});
                    }
                    else {
                        InvokerHelper.invokeMethod(parentBean, (String)addAttr, child);
                    }
                }
            }
            else {
                Object collectionAttr = attribute("collection");
                Object property = null;

                if(collectionAttr == null) {
                    collectionAttr = name();
                }

                if(collectionAttr instanceof Closure) {
                    Closure collectionClosure = (Closure)collectionAttr;
                    property = collectionClosure.call(parentBean);
                }
                else if(collectionAttr instanceof String) {
                    property = InvokerHelper.getProperty(parentBean, (String)collectionAttr);
                }

                if(property != null) {
                    if(Collection.class.isAssignableFrom(property.getClass())) {
                        ((Collection)property).add(child);
                    }
                    else if(Map.class.isAssignableFrom(property.getClass())) {
                        Object key = getKey(keyAttr, child);
                        ((Map)property).put(key, child);
                    }
                }
                else {
                    InvokerHelper.setProperty(parentBean, (String)collectionAttr, child);
                }
            }
        }
        catch(Exception e) {
            throw new CollectionException((String)name());
        }
    }
}