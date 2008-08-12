package groovytools.builder;

/**
 * Thrown when {@link MetaBuilder} doesn't recognize an object as an allowable factory.
 */
public class FactoryException extends MetaBuilderException {
    public FactoryException() {
    }

    public FactoryException(String message) {
        super(message);
    }

    public FactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public FactoryException(Throwable cause) {
        super(cause);
    }
}