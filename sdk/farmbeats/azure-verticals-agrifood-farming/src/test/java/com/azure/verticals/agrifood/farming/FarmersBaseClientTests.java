// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

public class FarmersBaseClientTests extends FarmBeatsClientTestBase {
    private FarmersClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new FarmBeatsClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildFarmersClient());
    }

//    @Test
//    public void testList() {
//        DynamicResponse res = client.list().send();
//        assertEquals(200, res.getStatusCode());
//
//        JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
//        JsonObject result = jsonReader.readObject();
//        assertTrue(result.containsKey("value"));
//
//        JsonArray value = result.getJsonArray("value");
//        assertTrue(value.size() > 0);
//    }
}
