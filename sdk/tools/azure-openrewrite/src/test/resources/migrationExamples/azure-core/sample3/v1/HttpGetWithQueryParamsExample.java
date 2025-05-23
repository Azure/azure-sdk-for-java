import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;

public class HttpGetWithQueryParamsExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        String url = "https://example.com?param1=value1&param2=value2";
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);

    }
}
