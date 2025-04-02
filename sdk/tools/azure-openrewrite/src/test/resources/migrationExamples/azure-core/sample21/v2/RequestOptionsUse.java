import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.models.binarydata.BinaryData;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 6: Using POST method with body
        RequestOptions options6 = new RequestOptions()
            .addRequestCallback( request -> {;
                request.setMethod(HttpMethod.POST);
                request.setBody(BinaryData.fromString("Sample body"));
            });

    }
}
