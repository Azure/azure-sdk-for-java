import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

public class SimpleResponseApis {
    public static void main(String... args) {

        Response<String> response = new Response(new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("http://localhost"), 200, new HttpHeaders(), "body");

        HttpRequest request = response.getRequest();
        int statusCode = response.getStatusCode();
        HttpHeaders headers = response.getHeaders();
        String body = response.getValue();
    }
}
