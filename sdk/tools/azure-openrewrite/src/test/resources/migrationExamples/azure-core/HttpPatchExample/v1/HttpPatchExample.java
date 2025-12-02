import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;

public class HttpPatchExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        HttpHeaders headers = new HttpHeaders().set("Content-Type", "application/json");
        String jsonBody = "{\"key\":\"value\"}";
        HttpRequest request = new HttpRequest(HttpMethod.PATCH, "https://example.com");
        request.setHeaders(headers);
        request.setBody(jsonBody);

    }
}
