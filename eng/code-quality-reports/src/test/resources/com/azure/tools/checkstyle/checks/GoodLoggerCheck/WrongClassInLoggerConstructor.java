import com.azure.core.util.logging.ClientLogger;

public class WrongClassInLoggerConstructor {
    // wrong class in ClientLogger constructor
    private final ClientLogger logger = new ClientLogger(XXXXXX.class);
}
