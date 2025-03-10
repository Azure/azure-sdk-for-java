import com.azure.core.http.HttpMethod;
import com.azure.core.http.rest.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 6: Using POST method with body
        RequestOptions options6 = new RequestOptions()
            .addRequestCallback( request -> {;
                request.setHttpMethod(HttpMethod.POST);
                request.setBody("Sample body");
            });

    }
}
