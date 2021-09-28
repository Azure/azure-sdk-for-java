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
        client = clientSetup(httpPipeline -> new PurviewCatalogClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(httpPipeline)
                .buildGlossaryClient());
    }

    @Test
    public void testListGlossaries() {
        BinaryData binaryData = client.listGlossariesWithResponse(null, null).getValue();
        List<?> list = binaryData.toObject(List.class);
        System.out.println(list);
//        DynamicResponse res = client.listGlossaries().send();
//        assertEquals(200, res.getStatusCode());
//
//        JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
//        JsonArray glossaries = jsonReader.readArray();
//        assertEquals(1, glossaries.size());
//
//        JsonObject glossary = glossaries.get(0).asJsonObject();
//        assertEquals("Glossary", glossary.getString("name"));
//
//        JsonArray terms = glossary.getJsonArray("terms");
//        assertEquals(1, terms.size());
    }

    @Test
    public void testImport() {

    }
}
