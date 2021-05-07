// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import com.azure.core.experimental.http.DynamicResponse;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FarmsBaseClientTests extends FarmBeatsClientTestBase {
    private FarmsBaseClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new FarmBeatsClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildFarmsBaseClient());
    }

    @Test
    public void testCreateOrUpdate() {
        String farmerId = Configuration.getGlobalConfiguration().get("FARMBEATS_FARMER_ID");
        String farmId = testResourceNamer.randomName("javafarm", 16);

        try {
            JsonObject farm = Json.createObjectBuilder()
                    .add("name", farmId)
                    .add("description", "A test farm.")
                    .build();
            DynamicResponse res = client.createOrUpdate(farmerId, farmId)
                    .setBody(farm.toString()).send();

            assertEquals(201, res.getStatusCode());

            // GET
            res = client.get(farmerId, farmId).send();
            JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
            JsonObject result = jsonReader.readObject();
            assertEquals(farmerId, result.getString("farmerId"));
            assertEquals(farmId, result.getString("name"));

            // LIST

            res = client.listByFarmerId(farmerId).send();
            assertEquals(200, res.getStatusCode());

            jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
            result = jsonReader.readObject();
            assertTrue(result.containsKey("value"));

            JsonArray value = result.getJsonArray("value");
            assertTrue(value.size() > 0);
        } finally {
            try {
                client.delete(farmerId, farmId).send();
            } catch (Throwable t) {
                // ignore
            }
        }
    }
}
