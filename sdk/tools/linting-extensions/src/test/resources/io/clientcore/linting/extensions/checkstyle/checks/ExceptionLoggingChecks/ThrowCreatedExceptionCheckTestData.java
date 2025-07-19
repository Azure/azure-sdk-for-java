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
            .addKeyValue("foo", "bar")
            .log("This is a test exception", RuntimeException::new);
    }

    // correct
    public void throwException7() {
        throw (RuntimeException)logger.throwableAtError()
            .addKeyValue("foo", "bar")
            .log("This is a test exception", IllegalArgumentException::new);
    }

    // correct
    public void logException() {
        Throwable ex = new RuntimeException("This is a test exception");
        logger.atError()
            .addKeyValue("foo", "bar")
            .setThrowable(ex)
            .log();
    }

    // correct
    public void logException() {
        ExceptionLoggingEvent loggedException
                = logger.throwableAtError();

        loggedException.addKeyValue("foo", "bar");
        loggedException.log("This is a test exception", RuntimeException::new);
    }
}
