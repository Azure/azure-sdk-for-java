import com.azure.core.util.logging.ClientLogger;

public class InvalidLoggerNameTestData {
    // invalid logger name
    private final ClientLogger wrongLoggerName = new ClientLogger(InvalidLoggerNameTestData.class);

    private final ClientLogger logger = new ClientLogger(InvalidLoggerNameTestData.class);
}
