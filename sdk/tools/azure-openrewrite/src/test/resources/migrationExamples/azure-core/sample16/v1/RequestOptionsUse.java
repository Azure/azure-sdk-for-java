import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 1: Basic usage
        RequestOptions options1 = new RequestOptions();
        options1.addHeader("Custom-Header", "CustomValue");
        options1.addHeader("Another-Header", "AnotherValue");

        options1.addHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
        options1.addHeader(HttpHeaderName.ACCEPT, "application/json");

        options1.addQueryParam("queryParam1", "value1")
            .addQueryParam("queryParam2", "value2")
            .addQueryParam("queryParam3", "value3");

        RequestOptions options2 = new RequestOptions()
            .addHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            .addHeader(HttpHeaderName.ACCEPT, "application/json")
            .addQueryParam("queryParam1", "value1");

    }
}
