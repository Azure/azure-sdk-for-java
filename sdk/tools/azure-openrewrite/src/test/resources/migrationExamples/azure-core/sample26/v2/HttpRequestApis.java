import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.URL;

public class HttpRequestApis {
    public static void main(String... args) throws Exception {

        HttpRequest request1 = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://example.com");

        HttpRequest request2 = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(new URL("https://example.com").toURI());

        HttpRequest request3 = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(new URL("https://example.com").toURI())
            .setHeaders(new HttpHeaders(5));

        HttpRequest request4 = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(new URL("https://example.com").toURI())
            .setHeaders(new HttpHeaders(5))
            .setBody(BinaryData.fromString("test"));

        HttpMethod method = request1.getHttpMethod();
        request2 = request2.setMethod(HttpMethod.POST);
        URL url = request3.getUri().toURL();
        request4 = request4.setUri(new URL("https://example.com").toURI());
        HttpHeaders headers = request1.getHeaders();
        request2 = request2.setHeaders(new HttpHeaders(5));
        request1
            .setHeaders(request1.getHeaders().set(HttpHeaderName.fromString("key"), "value"))
            .setHeaders(request1.getHeaders().set(HttpHeaderName.ACCEPT, "application/json"));

        BinaryData body = request2.getBody();
        request3 = request3.setBody(BinaryData.fromString("new Body"));
        request4 = request4.setBody(BinaryData.fromBytes(new byte[4]));
        request1 = request1.setBody(BinaryData.fromString("test"));
        HttpRequest request5 = new HttpRequest()
            .setMethod(request1.getHttpMethod())
            .setUri(request1.getUri())
            .setHeaders(new HttpHeaders(request1.getHeaders()))
            .setBody(request1.getBody());

    }
}
