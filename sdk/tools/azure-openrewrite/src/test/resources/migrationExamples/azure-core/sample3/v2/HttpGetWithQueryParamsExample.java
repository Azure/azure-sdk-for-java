import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;

public class HttpGetWithQueryParamsExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        String url = "https://example.com?param1=value1&param2=value2";
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(url);

    }
}
