import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.RequestContext;

public class RequestOptionsUse {
    public static void main(String... args) {

        // Sample 1: Basic usage
        RequestContext options1 = RequestContext.none();
        options1 = options1.toBuilder().setHeader(HttpHeaderName.fromString("Custom-Header"), "CustomValue").build();
        options1 = options1.toBuilder().setHeader(HttpHeaderName.fromString("Another-Header"), "AnotherValue").build();

        options1 = options1.toBuilder().setHeader(HttpHeaderName.CONTENT_TYPE, "application/json").build();
        options1 = options1.toBuilder().setHeader(HttpHeaderName.ACCEPT, "application/json").build();

        options1 = options1.toBuilder().addQueryParam("queryParam1", "value1")
            .addQueryParam("queryParam2", "value2")
            .addQueryParam("queryParam3", "value3").build();

        RequestContext options2 = RequestContext.builder()
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json")
            .setHeader(HttpHeaderName.ACCEPT, "application/json")
            .addQueryParam("queryParam1", "value1").build();

    }
}
