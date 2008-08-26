package groovytools.builder;

import groovy.util.*;
import groovy.lang.*;

import java.util.*;

/**
 * Adapts a given {@link Closure} such that it can support the {@link Factory#newInstance(FactoryBuilderSupport, Object, Object, Map)}
 * method.  It is important that the {@link Closure} supports the proper number, type and
 * order of arguments as follows:
 * <ol>
 * <li>If no arguments are supported, it is called without arguments.</li>
 * <li>If one argument is supported, it is the node's name.</li>
 * <li>If two arguments are supported, they are the node's name and value.</li>
 * <li>If three arguments are supported, they are the node's name, value, and attributes.</li>
 * </ol>
 *
 * @author didge
 * @version $REV$
 */
public class ClosureFactoryAdapter extends AbstractFactory {
    protected Closure closure;

    public ClosureFactoryAdapter(Closure closure) {
        this.closure = closure;
    }

    /**
     * Invocations of this method are delegated to the {@link Closure} to return an object when called.
     *
     * @param builder
     * @param name
     * @param value
     * @param attributes
     *
     * @return see above
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) throws InstantiationException, IllegalAccessException {
        switch (closure.getMaximumNumberOfParameters()) {
            case 0: return closure.call();
            case 1: return closure.call(new Object[] {name});
            case 2: return closure.call(new Object[] {name, value});
            default: return closure.call(new Object[] {name, value, attributes});
        }
    }
}
