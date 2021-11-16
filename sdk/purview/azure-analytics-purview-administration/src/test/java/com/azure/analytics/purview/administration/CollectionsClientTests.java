// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

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

    @Test
    public void testCollections() {
        PagedIterable<BinaryData> response = client.listCollections(null);
        List<BinaryData> list = response.stream().collect(Collectors.toList());
        System.out.println(list);
    }
}
