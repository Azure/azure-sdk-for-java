import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;

public class HttpBearerTokenExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        String token = "your_bearer_token";
        HttpHeaders headers = new HttpHeaders().set("Authorization", "Bearer " + token);
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.com")
            .setHeaders(headers);

    }
}
