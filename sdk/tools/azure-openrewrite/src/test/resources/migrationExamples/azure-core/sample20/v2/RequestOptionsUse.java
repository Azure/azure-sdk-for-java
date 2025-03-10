import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.utils.Context;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 5: Adding context
        RequestOptions options5 = new RequestOptions()
            .setContext(new Context("key", "value"));
    }
}
