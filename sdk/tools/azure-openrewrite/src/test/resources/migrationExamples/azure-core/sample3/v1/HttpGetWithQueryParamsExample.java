import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

public class HttpGetWithQueryParamsExample {
    public static void main(String... args) {
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        String url = "https://example.com?param1=value1&param2=value2";
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);

        HttpResponse response = client.send(request).block();
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
