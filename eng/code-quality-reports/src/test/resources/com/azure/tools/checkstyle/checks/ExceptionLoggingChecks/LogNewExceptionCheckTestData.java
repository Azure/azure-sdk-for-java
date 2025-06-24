import com.azure.core.util.logging.ClientLogger;

public class LogNewExceptionCheckTestData {
    private final ClientLogger logger = new ClientLogger(LogNewExceptionCheckTestData.class);

    // incorrect
    public void throwException1() {
        throw new RuntimeException("This is a test exception");
    }

    // incorrect
    public void throwException2() {
        throw CoreException.from("This is a test exception");
    }

    // correct
    public void throwException3() {
        throw logger.throwableAtError()
            .log("This is a test exception.", CoreException::from);
    }

    // incorrect, but we can't really detect it
    public void throwException4() {
        throw from("This is a test exception");
    }

    // correct, trivial param validation does not need a check
    public void validateParams() {
        throw new IllegalArgumentException("'foo' must not be null or empty.");
        throw new IllegalStateException("'foo' must be provided along with 'bar'.");
        throw new NullPointerException("'foo' must not be null.");
        throw new UnsupportedOperationException("'foo' operation is not supported.");
    }

    // correct
    public void throwException5() {
        throw logger.throwableAtError()
            .addKeyValue("foo", "bar")
            .log("This is a test exception", RuntimeException::new);
    }

    // correct
    public void throwException6() {
        throw (RuntimeException)logger.throwableAtError()
            .addKeyValue("foo", "bar")
            .log("This is a test exception", IllegalArgumentException::new);
    }
}
