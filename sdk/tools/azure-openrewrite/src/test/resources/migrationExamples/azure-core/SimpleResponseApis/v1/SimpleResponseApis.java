import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.SimpleResponse;

public class SimpleResponseApis {
    public static void main(String... args) {

        SimpleResponse<String> response = new SimpleResponse(new HttpRequest(HttpMethod.GET, "http://localhost"), 200, new HttpHeaders(), "body");

        HttpRequest request = response.getRequest();
        int statusCode = response.getStatusCode();
        HttpHeaders headers = response.getHeaders();
        String body = response.getValue();
    }
}
