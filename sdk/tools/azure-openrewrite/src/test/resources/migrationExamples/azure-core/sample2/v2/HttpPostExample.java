import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

public class HttpPostExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        HttpHeaders headers = new HttpHeaders().add(HttpHeaderName.CONTENT_TYPE, "application/json");
        String jsonBody = "{\"key\":\"value\"}";
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.POST)
            .setUri("https://example.com")
            .setHeaders(headers)
            .setBody(BinaryData.fromString(jsonBody));

        Response response = client.send(request);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
