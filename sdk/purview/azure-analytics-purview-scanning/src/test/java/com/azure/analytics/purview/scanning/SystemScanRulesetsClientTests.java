// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemScanRulesetsClientTests extends PurviewScanningClientTestBase {
    private SystemScanRulesetsClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewScanningClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(httpPipeline)
                .buildSystemScanRulesetsClient());
    }

    @Test
    public void testListAll() {
        PagedIterable<BinaryData> response = client.listAll(null, Context.NONE);
        List<BinaryData> list = response.stream().collect(Collectors.toList());

        assertTrue(list.size() > 0);
    }
}
