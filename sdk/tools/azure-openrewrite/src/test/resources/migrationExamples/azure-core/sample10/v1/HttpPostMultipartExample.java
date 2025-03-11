import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;

public class HttpPostMultipartExample {
    public static void main(String... args) {
        HttpClient client = HttpClient.createDefault();
        HttpHeaders headers = new HttpHeaders().set("Content-Type", "multipart/form-data");
        String multipartData = "--boundary\r\nContent-Disposition: form-data; name=\"key1\"\r\n\r\nvalue1\r\n--boundary--";
        HttpRequest request = new HttpRequest(HttpMethod.POST, "https://example.com");
        request.setHeaders(headers);
        request.setBody(multipartData);

        HttpResponse response = client.sendSync(request, Context.NONE);
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Headers: " + response.getHeaders());
    }
}
