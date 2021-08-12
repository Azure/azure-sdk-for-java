package com.azure.analytics.purview.account;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Disabled;

import java.util.List;
import java.util.stream.Collectors;

public class CollectionsClientTests extends PurviewAccountClientTestBase {
    private CollectionsClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewAccountClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildCollectionsClient());
    }

    @Disabled("{\"error\":{\"code\":\"Unauthorized\",\"message\":\"Not authorized to access account\"}}")
    public void testCollections() {
        PagedIterable<BinaryData> response = client.listByAccount(null);
        List<BinaryData> list = response.stream().collect(Collectors.toList());
        System.out.println(list);
    }
}
