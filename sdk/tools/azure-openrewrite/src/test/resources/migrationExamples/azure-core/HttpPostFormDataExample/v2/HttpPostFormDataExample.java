import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.models.binarydata.BinaryData;

public class HttpPostFormDataExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.fromString("Content-Type"), "application/x-www-form-urlencoded");
        String formData = "key1=value1&key2=value2";
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.POST)
            .setUri("https://example.com")
            .setHeaders(headers)
            .setBody(BinaryData.fromString(formData));

    }
}
