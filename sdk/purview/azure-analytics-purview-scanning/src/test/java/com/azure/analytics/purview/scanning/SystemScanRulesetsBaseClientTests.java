// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.scanning;

import com.azure.core.experimental.http.DynamicResponse;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemScanRulesetsBaseClientTests extends PurviewScanningClientTestBase {
    private SystemScanRulesetsBaseClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewScanningClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(httpPipeline)
                .buildSystemScanRulesetsBaseClient());
    }

    @Test
    public void testListAll() {
        DynamicResponse res = client.listAll().send();
        assertEquals(200, res.getStatusCode());

        JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
        JsonObject result = jsonReader.readObject();
        assertTrue(result.containsKey("value"));

        JsonArray value = result.getJsonArray("value");
        assertTrue(value.size() > 0);
    }
}
