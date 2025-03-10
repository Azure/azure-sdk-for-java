import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;

public class HttpCustomHeadersExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        HttpHeaders headers = new HttpHeaders().set("Custom-Header", "CustomValue");
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.com");
        request.setHeaders(headers);

        HttpResponse response = client.sendSync(request, Context.NONE);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
