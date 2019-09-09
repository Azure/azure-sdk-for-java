import com.azure.core.util.logging.ClientLogger;

public class NonStaticLogger {
    // invalid static logger
    private static final ClientLogger logger = new ClientLogger(NonStaticLogger.class);
}
