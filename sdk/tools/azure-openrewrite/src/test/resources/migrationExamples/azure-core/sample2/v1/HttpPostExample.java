import com.azure.core.http.*;
import com.azure.core.util.Context;


public class HttpPostExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        HttpHeaders headers = new HttpHeaders().set("Content-Type", "application/json");
        String jsonBody = "{\"key\":\"value\"}";
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://example.com")
                .setHeaders(headers)
                .setBody(jsonBody);
        HttpResponse response = client.sendSync(request, Context.NONE);
        System.out.println("Status code: " + response.getStatusCode());
    }
}
