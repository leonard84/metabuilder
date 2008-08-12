package groovytools.builder;

/**
 * Thrown when {@link MetaBuilder} can't resolve a class name.
 */
public class ClassNameNotFoundException extends MetaBuilderException {
    public ClassNameNotFoundException() {
    }

    public ClassNameNotFoundException(String message) {
        super(message);
    }

    public ClassNameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassNameNotFoundException(Throwable cause) {
        super(cause);
    }
}
