import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;

public class HttpPutExample {
    public static void main(String... args) {
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        HttpHeaders headers = new HttpHeaders().set("Content-Type", "application/json");
        String jsonBody = "{\"key\":\"value\"}";
        HttpRequest request = new HttpRequest(HttpMethod.PUT, "https://example.com");
        request.setHeaders(headers);
        request.setBody(jsonBody);

        HttpResponse response = client.sendSync(request, Context.NONE);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
