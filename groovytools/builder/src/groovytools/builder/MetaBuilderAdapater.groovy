package groovytools.builder
/**
 */
class MetaBuilderAdapter {
    def builder

    public MetaBuilderAdapter(def builder) {
        this.builder = builder
    }

    public Object doInvokeMethod(Object name, Map attributes, Object value) {
        return builder.invokeMethod(name, [ attributes, value ].toArray());
    }
}