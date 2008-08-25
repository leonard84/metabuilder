package groovytools.builder;

/**
 * Thrown when an exception occurs building a collection or adding to a collection. 
 *
 * @author didge
 * @version $REV$
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