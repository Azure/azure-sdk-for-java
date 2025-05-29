import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;

import java.net.URL;

public class HttpRequestApis {
    public static void main(String... args) throws Exception {

        HttpRequest request1 = new HttpRequest(HttpMethod.GET, "https://example.com");

        HttpRequest request2 = new HttpRequest(HttpMethod.GET, new URL("https://example.com"));

        HttpRequest request3 = new HttpRequest(HttpMethod.GET, new URL("https://example.com"), new HttpHeaders(5));

        HttpRequest request4 = new HttpRequest(HttpMethod.GET, new URL("https://example.com"), new HttpHeaders(5), BinaryData.fromString("test"));

        HttpMethod method = request1.getHttpMethod();
        request2 = request2.setHttpMethod(HttpMethod.POST);
        URL url = request3.getUrl();
        request4 = request4.setUrl(new URL("https://example.com"));
        HttpHeaders headers = request1.getHeaders();
        request2 = request2.setHeaders(new HttpHeaders(5));
        request1
            .setHeader("key", "value")
            .setHeader(HttpHeaderName.ACCEPT, "application/json");

        BinaryData body = request2.getBodyAsBinaryData();
        request3 = request3.setBody("new Body");
        request4 = request4.setBody(new byte[4]);
        request1 = request1.setBody(BinaryData.fromString("test"));
        HttpRequest request5 = request1.copy();

    }
}
