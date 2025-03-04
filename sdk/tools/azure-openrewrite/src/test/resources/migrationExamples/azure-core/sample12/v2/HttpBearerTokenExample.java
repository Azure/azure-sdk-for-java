import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import java.io.IOException;

public class HttpBearerTokenExample {
    public static void main(String... args) {
        HttpClient client = new JdkHttpClientBuilder().build();
        String token = "your_bearer_token";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaderName.AUTHORIZATION, "Bearer " + token);
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://example.com")
            .setHeaders(headers);

        try {
            Response response = client.send(request);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Headers: " + response.getHeaders());
        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}
