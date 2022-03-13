// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import com.azure.core.experimental.http.DynamicResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

/**
 * Sample for creating a farm with FarmsBaseClient.
 */
public class CreateFarms {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) throws Exception {
        String farmerId = "<existing-farmer-id>";
        String farmId = "<new-farm-id>";

        FarmsBaseClient client = new FarmBeatsClientBuilder()
                .endpoint("https://<farmbeats resource name>.farmbeats-dogfood.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildFarmsBaseClient();

        JsonObject farm = Json.createObjectBuilder()
                .add("name", farmId)
                .add("description", "A sample farm.")
                .build();

        System.out.println("Creating farm " + farmId + "...");
        DynamicResponse res = client.createOrUpdate(farmerId, farmId)
                .setBody(farm.toString()).send();
        JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
        JsonObject result = jsonReader.readObject();
        String status = result.getString("status");

        while (res.getStatusCode() == 201 || "running".equalsIgnoreCase(status)) {
            System.out.println("Waiting for resource to be provisioned...");
            res = client.get(farmerId, farmId).send();
            Thread.sleep(10000);
        }

        jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
        result = jsonReader.readObject();
        System.out.println("New farm " + farmId + " successfully created at " + result.getString("createdDateTime"));
    }
}
