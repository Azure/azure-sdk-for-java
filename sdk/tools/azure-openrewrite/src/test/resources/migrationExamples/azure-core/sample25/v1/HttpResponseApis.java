import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;

public class HttpResponseApis {
    public static void main(String... args) {

        // Sample 1: Basic usage with GET method
        HttpResponse response = null; // Assume this is initialized

        int statusCode = response.getStatusCode();

        String headerValue = response.getHeaderValue("Content-Type");
        String headerValue2 = response.getHeaderValue(HttpHeaderName.CONTENT_TYPE);
        HttpHeaders headers = response.getHeaders();
        // skipping getBody() since it returns a Reactor API
        BinaryData body = response.getBodyAsBinaryData();
        // skipping getBodyAsByteArray() since it returns a Reactor API
        // skipping getBodyAsString() since it returns a Reactor API
        // skipping getBodyAsString(Charset) since it returns a Reactor API
        // skipping getBodyAsInputStream() since it returns a Reactor API
        HttpRequest request = response.getRequest();
        // deprioritized: buffer()
        // skipping writeBodyToAsync() since it returns a Reactor API
        // deprioritized: writeBodyTo(java.nio.channels.WritableByteChannel)

    }
}
