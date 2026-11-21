import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpHeadersApis {

    public void exampleUsage() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("name1", "value1");
        headers.add(HttpHeaderName.ACCEPT, "application/json");

        int size = headers.getSize();

        headers.set("test", "value");
        List<String> customHeaderList = new ArrayList<>();
        customHeaderList.add("customHeaderValue1");
        customHeaderList.add("customHeaderValue2");
        headers.set("custom", customHeaderList);

        Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("headerName", customHeaderList);

        headers.setAll(headerMap);

        HttpHeaders clone = new HttpHeaders().setAllHttpHeaders(headers);

        clone.get(HttpHeaderName.ACCEPT);
        clone.get("test");

        clone.remove("custom");
        headers.remove(HttpHeaderName.ACCEPT);

        String[] arrayOfHeaderStrings = headers.getValues("custom");
        String[] anotherArrayListOfHeaderStrings = headers.getValues(HttpHeaderName.fromString("custom"));

    }

}
