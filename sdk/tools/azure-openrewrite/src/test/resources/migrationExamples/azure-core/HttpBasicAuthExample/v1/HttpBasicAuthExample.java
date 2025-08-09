import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;

import java.util.Base64;

public class HttpBasicAuthExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        String auth = "username:password";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        HttpHeaders headers = new HttpHeaders().set("Authorization", "Basic " + encodedAuth);
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.com")
            .setHeaders(headers);

    }
}
