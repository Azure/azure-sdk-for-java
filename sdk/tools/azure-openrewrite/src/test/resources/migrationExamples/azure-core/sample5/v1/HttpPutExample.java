import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

public class HttpPutExample {
    public static void main(String... args) {
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        HttpHeaders headers = new HttpHeaders().set("Content-Type", "application/json");
        String jsonBody = "{\"key\":\"value\"}";
        HttpRequest request = new HttpRequest(HttpMethod.PUT, "https://example.com", headers, jsonBody);

        HttpResponse response = client.send(request).block();
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
