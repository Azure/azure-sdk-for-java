import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

public class HttpPostMultipartExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "multipart/form-data");
        String multipartData = "--boundary\r\nContent-Disposition: form-data; name=\"key1\"\r\n\r\nvalue1\r\n--boundary--";
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.POST)
            .setUri("https://example.com");
        request.setHeaders(headers);
        request.setBody(BinaryData.fromString(multipartData));

        Response response = client.send(request);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
