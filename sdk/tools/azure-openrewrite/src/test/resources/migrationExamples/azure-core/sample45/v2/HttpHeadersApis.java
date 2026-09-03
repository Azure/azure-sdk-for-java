import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpHeadersApis {

    public void exampleUsage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaderName.fromString("name1"), "value1");
        headers.add(HttpHeaderName.ACCEPT, "application/json");

        int size = headers.getSize();

        headers.set(HttpHeaderName.fromString("test"), "value");
        List<String> customHeaderList = new ArrayList<>();
        customHeaderList.add("customHeaderValue1");
        customHeaderList.add("customHeaderValue2");
        headers.set(HttpHeaderName.fromString("custom"), customHeaderList);

        Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("headerName", customHeaderList);

        headers.setAll(headerMap.entrySet().stream().collect(
            HttpHeaders::new,
            (newHeaders, entry) -> newHeaders.set(HttpHeaderName.fromString(entry.getKey()), entry.getValue()),
            HttpHeaders::setAll
        ));

        HttpHeaders clone = new HttpHeaders().setAll(headers);

        clone.get(HttpHeaderName.ACCEPT);
        clone.get(HttpHeaderName.fromString("test"));

        clone.remove(HttpHeaderName.fromString("custom"));
        headers.remove(HttpHeaderName.ACCEPT);

        String[] arrayOfHeaderStrings = headers.getValues(HttpHeaderName.fromString("custom")).toArray(new String[0]);
        String[] anotherArrayListOfHeaderStrings = headers.getValues(HttpHeaderName.fromString("custom")).toArray(new String[0]);

    }

}
