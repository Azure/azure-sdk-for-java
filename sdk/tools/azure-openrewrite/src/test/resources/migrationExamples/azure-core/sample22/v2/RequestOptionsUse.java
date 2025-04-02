import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 7: Adding multiple headers and query parameters
        RequestOptions options7 = new RequestOptions()
            .setHeader(HttpHeaderName.fromString("Header1"), "Value1");


    }
}
