import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.JdkHttpClientBuilder;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import java.io.IOException;

public class HttpPutExample {
    public static void main(String... args) {
        HttpClient client = new JdkHttpClientBuilder().build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaderName.CONTENT_TYPE, "application/json");
        String jsonBody = "{\"key\":\"value\"}";
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.PUT)
            .setUri("https://example.com")
            .setHeaders(headers)
            .setBody(BinaryData.fromString(jsonBody));

        try {
            Response response = client.send(request);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Headers: " + response.getHeaders());
        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}
