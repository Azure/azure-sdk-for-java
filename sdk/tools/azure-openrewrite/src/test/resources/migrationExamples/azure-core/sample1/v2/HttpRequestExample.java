import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

public class HttpRequestExample {
    public static void main(String... args) {
        HttpClient client = new JdkHttpClientBuilder().build();
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://example.com");

        Response response = client.send(request);
        System.out.println("Status code: " + response.getStatusCode());
    }
}
