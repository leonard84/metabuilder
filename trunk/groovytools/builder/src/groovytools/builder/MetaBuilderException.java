package groovytools.builder;

/**
 * Base class for all {@link MetaBuilder} exceptions.
 *
 * @author didge
 * @version $REV$
 */
public class MetaBuilderException extends RuntimeException {
    public MetaBuilderException() {
    }

    public MetaBuilderException(String message) {
        super(message);
    }

    public MetaBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetaBuilderException(Throwable cause) {
        super(cause);
    }
}
