import io.clientcore.core.http.models.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 2: Adding headers
        RequestOptions options2 = new RequestOptions()
            .addHeader("Custom-Header", "HeaderValue");

    }
}
