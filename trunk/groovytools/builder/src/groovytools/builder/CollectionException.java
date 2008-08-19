package groovytools.builder;

/**
 * Thrown when
 */
public class CollectionException extends MetaBuilderException {
    public CollectionException() {
    }

    public CollectionException(String message) {
        super(message);
    }

    public CollectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionException(Throwable cause) {
        super(cause);
    }
}