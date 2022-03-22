// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.catalog;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GlossaryClientTests extends PurviewCatalogClientTestBase {
    private GlossaryClient client;

    @Override
    protected void beforeTest() {
        client = builderSetUp().endpoint(getEndpoint()).buildClient();
    }

    @Test
    public void testListGlossaries() {
        BinaryData binaryData = client.listGlossariesWithResponse(null).getValue();
        List<?> list = binaryData.toObject(List.class);
        System.out.println(list);
    }
}
