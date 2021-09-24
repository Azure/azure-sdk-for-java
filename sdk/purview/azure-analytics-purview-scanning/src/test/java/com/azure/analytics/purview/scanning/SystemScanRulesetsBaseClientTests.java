// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.core.experimental.http.DynamicResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemScanRulesetsBaseClientTests extends PurviewScanningClientTestBase {
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
        PagedIterable<BinaryData> response = client.listAll(null);
        List<BinaryData> list = response.stream().collect(Collectors.toList());

        assertTrue(list.size() > 0);
    }
}
