import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 4: Setting a timeout
        RequestOptions options4 = new RequestOptions()
            .setBody(BinaryData.fromString("Sample body"));
    }
}
