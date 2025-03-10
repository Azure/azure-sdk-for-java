import com.azure.core.http.rest.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 7: Adding multiple headers and query parameters
        RequestOptions options7 = new RequestOptions()
            .setHeader("Header1", "Value1");


    }
}
