import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

public class HttpBearerTokenExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        String token = "your_bearer_token";
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.AUTHORIZATION, "Bearer " + token);
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://example.com")
            .setHeaders(headers);

        Response response = client.send(request);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
