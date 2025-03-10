import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 8: Setting a custom retry policy
        RequestOptions options8 = new RequestOptions()
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
    }
}
