// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.catalog;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class GlossaryBaseClientTests extends PurviewCatalogClientTestBase {
    private GlossaryClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewCatalogClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildGlossaryClient());
    }

    @Test
    public void testListGlossaries() {
        BinaryData binaryData = client.listGlossariesWithResponse(null, null).getValue();
        List<?> glossaries = binaryData.toObject(List.class);
        Assertions.assertEquals(1, glossaries.size());
        Map<?, ?> map = (Map<?, ?>) glossaries.get(0);
        List<?> terms = (List<?>) map.get("terms");
        Assertions.assertEquals(1, terms.size());
    }
}
