import io.clientcore.core.http.models.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 3: Adding query parameters
        RequestOptions options3 = new RequestOptions()
            .addQueryParam("param1", "value1");

    }
}
