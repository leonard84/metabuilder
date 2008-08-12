package groovytools.builder
/**
 * Wraps an object that supports the {@link BuilderSupport} methods and provides a public interface so that its
 * methods can be delegated to by another builder.
 */
class BuilderSuppportProxy extends BuilderSupport {

    def builder

    public BuilderSuppportProxy(builder) {
        this.builder = builder
    }

    public void setParent(Object parent, Object child) {
        builder.setParent(parent, child)
    }

    public Object createNode(Object name) {
        return builder.createNode(name)
    }

    public Object createNode(Object name, Object value) {
        return builder.createNode(name, value)
    }

    public Object createNode(Object name, Map attributes) {
        return builder.createNode(name, attributes)
    }

    public Object createNode(Object name, Map attributes, Object value) {
        return builder.createNode(name, attributes, value)
    }

    public void nodeCompleted(Object parent, Object node) {
        builder.nodeCompleted(parent, node)
    }

    protected void setClosureDelegate(Closure closure, Object node) {
        builder.setClosureDelegate(closure, node);
    }

    protected Object getName(String methodName) {
        return builder.getName(methodName);
    }

    protected Object postNodeCompletion(Object parent, Object node) {
        return builder.postNodeCompletion(parent, node);
    }

    protected Object getCurrent() {
        return builder.getCurrent();
    }

    protected void setCurrent(Object current) {
        builder.setCurrent(current);
    }
}