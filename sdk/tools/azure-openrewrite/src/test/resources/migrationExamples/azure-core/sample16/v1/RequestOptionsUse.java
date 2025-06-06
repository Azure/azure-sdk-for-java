import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 1: Basic usage
        RequestOptions options1 = new RequestOptions();
        options1.setHeader("Custom-Header", "CustomValue");
        options1.setHeader("Another-Header", "AnotherValue");

        options1.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
        options1.setHeader(HttpHeaderName.ACCEPT, "application/json");

        options1.addQueryParam("queryParam1", "value1")
            .addQueryParam("queryParam2", "value2")
            .addQueryParam("queryParam3", "value3");

        RequestOptions options2 = new RequestOptions()
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            .setHeader(HttpHeaderName.ACCEPT, "application/json")
            .addQueryParam("queryParam1", "value1");

    }
}
