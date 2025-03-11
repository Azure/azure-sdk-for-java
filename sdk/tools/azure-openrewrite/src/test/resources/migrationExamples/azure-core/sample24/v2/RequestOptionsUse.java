import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 1: Basic usage with GET method
        RequestOptions options1 = new RequestOptions()
            .addHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
}
