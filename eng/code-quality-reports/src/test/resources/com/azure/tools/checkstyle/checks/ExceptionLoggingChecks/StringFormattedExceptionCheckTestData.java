import com.azure.core.util.logging.ClientLogger;

public class StringFormattedExceptionCheckTestData {
    private final ClientLogger logger = new ClientLogger(StringFormattedExceptionCheckTestData.class);

    // incorrect
    public void throwException1() {
        throw logger.throwableAtError()
            .log(String.format("This is a test exception, foo = '%s'", bar), IllegalArgumentException::new);
    }

    // correct
    public void throwException2() {
        throw logger.throwableAtError()
            .addKeyValuePair("foo", "bar")
            .log("This is a test exception", RuntimeException::new);
    }
}
