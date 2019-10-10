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
}
