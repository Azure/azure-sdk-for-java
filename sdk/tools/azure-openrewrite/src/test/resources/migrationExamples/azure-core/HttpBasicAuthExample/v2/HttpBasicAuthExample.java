import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;

import java.util.Base64;

public class HttpBasicAuthExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.getSharedInstance();
        String auth = "username:password";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.fromString("Authorization"), "Basic " + encodedAuth);
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://example.com")
            .setHeaders(headers);

    }
}
