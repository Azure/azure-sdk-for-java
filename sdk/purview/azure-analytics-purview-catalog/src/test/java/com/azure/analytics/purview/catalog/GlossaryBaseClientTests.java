// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.catalog;

import com.azure.core.experimental.http.DynamicResponse;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GlossaryBaseClientTests extends PurviewCatalogClientTestBase {
    private GlossaryBaseClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewCatalogClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(httpPipeline)
                .buildGlossaryBaseClient());
    }

    @Test
    public void testListGlossaries() {
        DynamicResponse res = client.listGlossaries().send();
        assertEquals(200, res.getStatusCode());

        JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
        JsonArray glossaries = jsonReader.readArray();
        assertEquals(1, glossaries.size());

        JsonObject glossary = glossaries.get(0).asJsonObject();
        assertEquals("Glossary", glossary.getString("name"));

        JsonArray terms = glossary.getJsonArray("terms");
        assertEquals(1, terms.size());
    }
}
