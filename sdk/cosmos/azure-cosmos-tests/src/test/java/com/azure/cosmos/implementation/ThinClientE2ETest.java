// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ThinClientE2ETest extends com.azure.cosmos.rx.TestSuiteBase {
    @Test(groups = {"thinclient"})
    public void testThinClientDocumentPointOperations() {
        CosmosAsyncClient client = null;
        try {
            System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            System.setProperty("COSMOS.HTTP2_ENABLED", "true");

            String thinclientTestEndpoint = System.getProperty("COSMOS.THINCLIENT_ENDPOINT");
            String thinclientTestKey = System.getProperty("COSMOS.THINCLIENT_KEY");

            client  = new CosmosClientBuilder()
                .endpoint(thinclientTestEndpoint)
                .key(thinclientTestKey)
                .gatewayMode()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();

            CosmosAsyncContainer container = client.getDatabase("updatedd-thin-client-test-db").getContainer("thin-client-test-container-1");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode doc = mapper.createObjectNode();
            String idValue = UUID.randomUUID().toString();
            doc.put("id", idValue);
            doc.put("pk", idValue);

            // create
            // todo: test other overloads for create
            CosmosItemResponse<ObjectNode> createResponse = container.createItem(doc).block();
            assertThat(createResponse.getStatusCode()).isEqualTo(201);
            assertThat(createResponse.getRequestCharge()).isGreaterThan(0.0);

            // read
            // todo: test other overloads for read
            CosmosItemResponse<ObjectNode> readResponse = container.readItem(idValue, new PartitionKey(idValue), ObjectNode.class).block();
            assertThat(readResponse.getStatusCode()).isEqualTo(200);
            assertThat(readResponse.getRequestCharge()).isGreaterThan(0.0);

            ObjectNode doc2 = mapper.createObjectNode();
            String idValue2 = UUID.randomUUID().toString();
            doc2.put("id", idValue2);
            doc2.put("pk", idValue);

            // replace
            // todo: test other overloads for replace
            CosmosItemResponse<ObjectNode> replaceResponse = container.replaceItem(doc2, idValue, new PartitionKey(idValue)).block();
            assertThat(replaceResponse.getStatusCode()).isEqualTo(200);
            assertThat(replaceResponse.getRequestCharge()).isGreaterThan(0.0);
            CosmosItemResponse<ObjectNode> readAfterReplaceResponse = container.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            assertThat(readAfterReplaceResponse.getStatusCode()).isEqualTo(200);
            ObjectNode replacedItemFromRead = readAfterReplaceResponse.getItem();
            assertThat(replacedItemFromRead.get("id").asText()).isEqualTo(idValue2);
            assertThat(replacedItemFromRead.get("pk").asText()).isEqualTo(idValue);

            ObjectNode doc3 = mapper.createObjectNode();
            doc3.put("id", idValue2);
            doc3.put("pk", idValue);
            doc3.put("newField", "newValue");

            // upsert
            // todo: test other overloads for upsert
            CosmosItemResponse<ObjectNode> upsertResponse = container.upsertItem(doc3, new PartitionKey(idValue), new CosmosItemRequestOptions()).block();
            assertThat(upsertResponse.getStatusCode()).isEqualTo(200);
            assertThat(upsertResponse.getRequestCharge()).isGreaterThan(0.0);
            CosmosItemResponse<ObjectNode> readAfterUpsertResponse = container.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            ObjectNode upsertedItemFromRead = readAfterUpsertResponse.getItem();
            assertThat(upsertedItemFromRead.get("id").asText()).isEqualTo(idValue2);
            assertThat(upsertedItemFromRead.get("pk").asText()).isEqualTo(idValue);
            assertThat(upsertedItemFromRead.get("newField").asText()).isEqualTo("newValue");

            // patch
            // todo: test other overloads for patch
            CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
            patchOperations.add("/anotherNewField", "anotherNewValue");
            patchOperations.replace("/newField", "patchedNewField");
            CosmosItemResponse<ObjectNode> patchResponse = container.patchItem(idValue2, new PartitionKey(idValue), patchOperations, ObjectNode.class).block();
            assertThat(patchResponse.getStatusCode()).isEqualTo(200);
            assertThat(patchResponse.getRequestCharge()).isGreaterThan(0.0);
            CosmosItemResponse<ObjectNode> readAfterPatchResponse = container.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            ObjectNode patchedItemFromRead = readAfterPatchResponse.getItem();
            assertThat(patchedItemFromRead.get("id").asText()).isEqualTo(idValue2);
            assertThat(patchedItemFromRead.get("pk").asText()).isEqualTo(idValue);
            assertThat(patchedItemFromRead.get("newField").asText()).isEqualTo("patchedNewField");
            assertThat(patchedItemFromRead.get("anotherNewField").asText()).isEqualTo("anotherNewValue");

            // delete
            // todo: test other overloads for delete
            CosmosItemResponse<Object> deleteResponse = container.deleteItem(idValue2, new PartitionKey(idValue)).block();
            assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
            assertThat(deleteResponse.getRequestCharge()).isGreaterThan(0.0);
        } finally {
            System.clearProperty("COSMOS.THINCLIENT_ENABLED");
            System.clearProperty("COSMOS.HTTP2_ENABLED");
            client.close();
        }
    }
}
