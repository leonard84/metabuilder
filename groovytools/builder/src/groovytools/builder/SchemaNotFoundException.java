package groovytools.builder;

/**
 * Thrown when a schema could not be found.
 *
 * @author didge
 * @version $REV$
 */
public class SchemaNotFoundException extends MetaBuilderException {
    public SchemaNotFoundException() {
    }

    public SchemaNotFoundException(String message) {
        super(message);
    }

    public SchemaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaNotFoundException(Throwable cause) {
        super(cause);
    }
}
