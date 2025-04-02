import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

public class ResponseApis {
    public static void main(String... args) {

        Response<String> response = null; // Assume this is initialized

        int statusCode = response.getStatusCode();
        HttpHeaders headers = response.getHeaders();
        HttpRequest request = response.getRequest();
        String body = response.getValue();
    }
}
