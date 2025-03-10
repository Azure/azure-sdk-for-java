import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

public class HttpRedirectExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://example.com/redirect");

        Response response = client.send(request);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("Final URL: " + response.getRequest().getUri());
    }
}
