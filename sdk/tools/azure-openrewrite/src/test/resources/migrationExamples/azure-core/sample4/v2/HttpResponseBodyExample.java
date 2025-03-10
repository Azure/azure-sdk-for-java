import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

public class HttpResponseBodyExample {
    public static void main(String... args) {
        HttpClient client = new JdkHttpClientBuilder().build();
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://example.com");
        Response response = client.send(request);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("Body: " + response.getBody().toString());
    }
}
