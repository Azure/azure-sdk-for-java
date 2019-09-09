import com.azure.core.util.logging.ClientLogger;

public class InvalidLoggerName {
    // invalid LOGGER name
    private final ClientLogger LOGGER = new ClientLogger(InvalidLoggerName.class);

    private final ClientLogger logger = new ClientLogger(InvalidLoggerName.class);
}
