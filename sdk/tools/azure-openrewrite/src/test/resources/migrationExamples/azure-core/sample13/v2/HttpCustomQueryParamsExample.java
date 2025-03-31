import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

public class HttpCustomQueryParamsExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        String url = "https://example.com?customParam1=value1&customParam2=value2";
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(url);

        Response response = client.send(request);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
