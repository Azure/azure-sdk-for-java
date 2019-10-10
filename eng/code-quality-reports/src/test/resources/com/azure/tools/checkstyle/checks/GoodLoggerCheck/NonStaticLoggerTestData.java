import com.azure.core.util.logging.ClientLogger;

public class NonStaticLoggerTestData {
    // invalid static logger
    private static final ClientLogger logger = new ClientLogger(NonStaticLoggerTestData.class);
}
