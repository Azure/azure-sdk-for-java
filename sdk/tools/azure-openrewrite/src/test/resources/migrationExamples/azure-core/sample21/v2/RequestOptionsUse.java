import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;

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
