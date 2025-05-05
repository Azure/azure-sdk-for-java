import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;

public class ResponseApis {
    public static void main(String... args) {

        Response<String> response = null; // Assume this is initialized

        int statusCode = response.getStatusCode();
        HttpHeaders headers = response.getHeaders();
        HttpRequest request = response.getRequest();
        String body = response.getValue();
    }
}
