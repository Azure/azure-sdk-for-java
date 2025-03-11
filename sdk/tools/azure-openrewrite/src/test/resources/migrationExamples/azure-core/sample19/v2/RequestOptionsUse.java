import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.models.binarydata.BinaryData;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 4: Setting a timeout
        RequestOptions options4 = new RequestOptions()
            .setBody(BinaryData.fromString("Sample body"));
    }
}
