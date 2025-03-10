import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;

public class HttpResponseBodyExample {
    public static void main(String... args) {
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.com");
        HttpResponse response = client.sendSync(request, Context.NONE);
        System.out.println("Status code: " + response.getStatusCode());
    }
}
