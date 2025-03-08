import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

public class HttpDeleteExample {
    public static void main(String... args) {
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, "https://example.com");

        HttpResponse response = client.send(request).block();
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
