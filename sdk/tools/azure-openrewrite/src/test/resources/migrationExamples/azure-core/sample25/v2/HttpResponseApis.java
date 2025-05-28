import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

public class HttpResponseApis {
    public static void main(String... args) {

        // Sample 1: Basic usage with GET method
        Response response = null; // Assume this is initialized

        int statusCode = response.getStatusCode();

        String headerValue = response.getHeaders().getValue(HttpHeaderName.fromString("Content-Type"));
        String headerValue2 = response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);
        HttpHeaders headers = response.getHeaders();
        // skipping getBody() since it returns a Reactor API
        BinaryData body = BinaryData.fromObject(response.getValue());
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
