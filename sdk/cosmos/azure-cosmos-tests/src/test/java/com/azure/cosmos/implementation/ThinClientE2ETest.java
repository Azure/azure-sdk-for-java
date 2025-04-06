// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ThinClientE2ETest {
    @Test(groups = {"thinclient"})
    public void testThinclientHttp2() {
        try {
            System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            System.setProperty("COSMOS.HTTP2_ENABLED", "true");

            String thinclientTestEndpoint = System.getProperty("COSMOS.THINCLIENT_ENDPOINT");
            String thinclientTestKey = System.getProperty("COSMOS_THINCLIENT_KEY");

            CosmosAsyncClient client  = new CosmosClientBuilder()
                .key(thinclientTestEndpoint)
                .endpoint(thinclientTestKey)
                .gatewayMode()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();

            CosmosAsyncContainer container = client.getDatabase("updatedd-thin-client-test-db").getContainer("thin-client-test-container-1");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode doc = mapper.createObjectNode();
            String idValue = UUID.randomUUID().toString();
            doc.put("id", idValue);
            doc.put("pk", idValue);
            CosmosItemResponse<ObjectNode> createResponse = container.createItem(doc).block();
            assertThat(createResponse.getStatusCode()).isBetween(199, 300);
            assertThat(createResponse.getRequestCharge()).isGreaterThan(0.0);
            CosmosItemResponse<ObjectNode> readResponse = container.readItem(idValue, new PartitionKey(idValue), ObjectNode.class).block();
            assertThat(readResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThat(readResponse.getStatusCode()).isBetween(199, 300);
            CosmosItemResponse<Object> deleteResponse = container.deleteItem(idValue, new PartitionKey(idValue)).block();
            assertThat(deleteResponse.getRequestCharge()).isGreaterThan(0.0);
            assertThat(deleteResponse.getStatusCode()).isBetween(199, 300);
            client.close();
        } finally {
            System.clearProperty("COSMOS.THINCLIENT_ENABLED");
            System.clearProperty("COSMOS.HTTP2_ENABLED");
        }

    }
}
