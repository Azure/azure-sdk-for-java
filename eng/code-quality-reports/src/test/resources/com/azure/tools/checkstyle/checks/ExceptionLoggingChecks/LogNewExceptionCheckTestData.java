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

    // check is skipped in generated methods
    @Metadata(properties = {MetadataProperties.GENERATED})
    public void throwExceptionInGeneratedMethod() {
        throw new RuntimeException("This is a test exception");
    }

    // check is skipped in generated methods
    @Metadata(properties = {GENERATED})
    public void throwExceptionInGeneratedMethod2() {
        throw new RuntimeException("This is a test exception");
    }

    // check is skipped in generated methods
    @Metadata(properties = {FLUENT, GENERATED})
    public void throwExceptionInGeneratedMethod3() {
        throw new RuntimeException("This is a test exception");
    }

    @Metadata(properties = {})
    public void benign1() {
    }

    @Metadata(properties = {MetadataProperties.FLUENT})
    public void benign2() {
    }

    // check is skipped in generated methods
    @Metadata(properties = MetadataProperties.GENERATED)
    public void throwExceptionInGeneratedMethod4() {
        throw new RuntimeException("This is a test exception");
    }

    // check is skipped in ServiceInterface
    @ServiceInterface(name = "BarServiceImpl", host = "{fooBaseUrl}")
    public interface BarService {
        static BarService getNewInstance(HttpPipeline pipeline) {
            try {
                Class<?> clazz = Class.forName("foo.BarServiceImpl");
                return (BarService) clazz.getMethod("getNewInstance", HttpPipeline.class)
                    .invoke(null, pipeline);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
