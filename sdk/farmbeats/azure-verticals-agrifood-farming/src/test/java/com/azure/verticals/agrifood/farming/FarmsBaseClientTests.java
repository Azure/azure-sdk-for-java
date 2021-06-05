// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

public class FarmsBaseClientTests extends FarmBeatsClientTestBase {
    private FarmsClient farmsClient;
    private FarmersClient farmersClient;

    @Override
    protected void beforeTest() {
        farmsClient = clientSetup(httpPipeline -> new FarmBeatsClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(httpPipeline)
                .buildFarmsClient());
        farmersClient = clientSetup(httpPipeline -> new FarmBeatsClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(httpPipeline)
                .buildFarmersClient());
    }

//    @Test
//    public void testCreateOrUpdate() {
//        DynamicResponse res = farmersClient.list().send();
//        JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
//        JsonObject result = jsonReader.readObject();
//        assertTrue(result.containsKey("value"));
//
//        JsonArray value = result.getJsonArray("value");
//        assertTrue(value.size() > 0);
//
//        String farmerId = value.get(0).asJsonObject().getString("id");
//
//        String farmId = testResourceNamer.randomName("javafarm", 16);
//
//        try {
//            JsonObject farm = Json.createObjectBuilder()
//                    .add("name", farmId)
//                    .add("description", "A test farm.")
//                    .build();
//            res = farmsClient.createOrUpdate(farmerId, farmId)
//                    .setBody(farm.toString()).send();
//
//            assertEquals(201, res.getStatusCode());
//
//            // GET
//            res = farmsClient.get(farmerId, farmId).send();
//            jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
//            result = jsonReader.readObject();
//            assertEquals(farmerId, result.getString("farmerId"));
//            assertEquals(farmId, result.getString("name"));
//
//            // LIST
//
//            res = farmsClient.listByFarmerId(farmerId).send();
//            assertEquals(200, res.getStatusCode());
//
//            jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
//            result = jsonReader.readObject();
//            assertTrue(result.containsKey("value"));
//
//            value = result.getJsonArray("value");
//            assertTrue(value.size() > 0);
//        } finally {
//            try {
//                farmsClient.delete(farmerId, farmId).send();
//            } catch (Throwable t) {
//                // ignore
//            }
//        }
//    }
}
