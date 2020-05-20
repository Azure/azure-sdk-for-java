import com.azure.core.util.logging.ClientLogger;

public class DirectThrowExceptionTestData {
    private final ClientLogger logger = new ClientLogger(DirectThrowExceptionTestData.class);

    // Skip check on constructor
    DirectThrowExceptionTestData() {
        throw new RuntimeException("Error Messages.");
    }

    public void directThrowException () {
        throw new RuntimeException("Error Messages.");
    }

    public void correctThrowException() {
        throw logger.logExceptionAsWarning(Exceptions.propagate(new IllegalStateException("Error Messages")));
    }

    public static skipCheckOnStaticMethod() {
        throw new RuntimeException("Error Messages.");
    }

    // Skip check on static class
    static class SkipCheckOnStaticClass {
        public void directThrowException () {
            throw new RuntimeException("Error Messages.");
        }
    }

    public void validLogExceptionAsError() {
        throw logger.logExceptionAsError(new RuntimeException("Error message."));
    }

    public void validLogThrowableAsError() {
        throw logger.logThrowableAsError(new RuntimeException("Error message."));
    }

    public void validLogExceptionAsWarning() {
        throw logger.logExceptionAsWarning(new RuntimeException("Error message."));
    }

    public void validLogThrowableAsWarning() {
        throw logger.logThrowableAsWarning(new RuntimeException("Error message."));
    }
}
