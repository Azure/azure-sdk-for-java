import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import java.io.IOException;

public class HttpGetWithQueryParamsExample {
    public static void main(String... args) {
        HttpClient client = new JdkHttpClientBuilder().build();
        String url = "https://example.com?param1=value1&param2=value2";
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(url);

        try {
            Response response = client.send(request);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Headers: " + response.getHeaders());
        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}
