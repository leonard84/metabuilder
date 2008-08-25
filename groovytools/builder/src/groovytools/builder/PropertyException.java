package groovytools.builder;

/**
 * Thrown when accessing or setting a property.
 *
 * @author didge
 * @version $REV$
 */
public class PropertyException extends MetaBuilderException {
    public PropertyException() {
    }

    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyException(Throwable cause) {
        super(cause);
    }
}