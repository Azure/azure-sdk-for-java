import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;

public class HttpPostMultipartExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        HttpHeaders headers = new HttpHeaders().set("Content-Type", "multipart/form-data");
        String multipartData = "--boundary\r\nContent-Disposition: form-data; name=\"key1\"\r\n\r\nvalue1\r\n--boundary--";
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://example.com");
        request.setHeaders(headers);
        request.setBody(multipartData);

    }
}
