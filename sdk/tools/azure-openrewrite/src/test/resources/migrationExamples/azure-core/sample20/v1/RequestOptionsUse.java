import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 5: Adding context
        RequestOptions options5 = new RequestOptions()
            .setContext(new Context("key", "value"));
    }
}
