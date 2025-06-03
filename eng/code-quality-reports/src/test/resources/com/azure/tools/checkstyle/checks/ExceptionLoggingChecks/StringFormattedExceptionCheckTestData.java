import com.azure.core.util.logging.ClientLogger;

public class StringFormattedExceptionCheckTestData {
    private final ClientLogger logger = new ClientLogger(StringFormattedExceptionCheckTestData.class);
    private static final String EXCEPTION_MESSAGE = "This is a test exception";
    // incorrect
    public void throwException1() {
        throw logger.throwableAtError()
            .log(String.format("This is a test exception, foo = '%s'", bar), IllegalArgumentException::new);
    }

    // incorrect
    public void throwException1() {
        String bar = "bar";
        throw logger.throwableAtError()
            .log("This is a test exception, foo = " + bar, IllegalArgumentException::new);
    }

    // correct
    public void throwException2() {
        throw logger.throwableAtError()
            .addKeyValue("foo", "bar")
            .log("This is a test exception", RuntimeException::new);
    }

    // correct
    public void throwException3() {
        throw logger.throwableAtError()
            .addKeyValue("foo", "bar")
            .log("This is a test exception " + " with long description", RuntimeException::new);
    }

    // correct
    public void throwException4() {
        throw logger.throwableAtError()
            .addKeyValue("foo", "bar")
            .log(EXCEPTION_MESSAGE, RuntimeException::new);
    }
}
