import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;

public class HttpRedirectExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://example.com/redirect");

    }
}
