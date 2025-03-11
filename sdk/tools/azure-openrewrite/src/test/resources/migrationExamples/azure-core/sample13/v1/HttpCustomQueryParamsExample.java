import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;

public class HttpCustomQueryParamsExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        String url = "https://example.com?customParam1=value1&customParam2=value2";
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);

        HttpResponse response = client.sendSync(request, Context.NONE);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
