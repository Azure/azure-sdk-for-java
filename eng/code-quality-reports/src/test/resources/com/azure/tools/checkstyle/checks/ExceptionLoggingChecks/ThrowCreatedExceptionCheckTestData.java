import com.azure.core.util.logging.ClientLogger;

public class ThrowCreatedExceptionCheckTestData {
    private final ClientLogger logger = new ClientLogger(ThrowCreatedExceptionCheckTestData.class);

    // incorrect
    public void createAndNotThrow() {
        logger.throwableAtError().log("This is a test exception", RuntimeException::new);
    }

    // correct
    public void throwException() {
        throw logger.throwableAtError()
            .addKeyValuePair("foo", "bar")
            .log("This is a test exception", RuntimeException::new);
    }

    // correct
    public void throwException7() {
        throw (RuntimeException)logger.throwableAtError()
            .addKeyValuePair("foo", "bar")
            .log("This is a test exception", IllegalArgumentException::new);
    }

    // correct
    public void logException() {
        Throwable ex = new RuntimeException("This is a test exception");
        logger.atError()
            .addKeyValuePair("foo", "bar")
            .setThrowable(ex)
            .log();
    }
}
