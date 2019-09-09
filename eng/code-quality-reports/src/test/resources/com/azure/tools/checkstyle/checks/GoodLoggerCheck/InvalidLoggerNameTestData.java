import com.azure.core.util.logging.ClientLogger;

public class InvalidLoggerNameTestData {
    // invalid LOGGER name
    private final ClientLogger LOGGER = new ClientLogger(InvalidLoggerNameTestData.class);

    private final ClientLogger logger = new ClientLogger(InvalidLoggerNameTestData.class);
}
