import com.azure.core.util.logging.ClientLogger;

public class NonStaticLoggerTestData {
    // static logger
    private static final ClientLogger LOGGER = new ClientLogger(NonStaticLoggerTestData.class);

    public abstract void run();

    void invalidLoggerUse() {
        this.getClass();
        int x = 1;
        // invalid logger use
        LOGGER.logExceptionAsError(new RuntimeException("Invalid use of static logger in a non-static method"));
    }
}
