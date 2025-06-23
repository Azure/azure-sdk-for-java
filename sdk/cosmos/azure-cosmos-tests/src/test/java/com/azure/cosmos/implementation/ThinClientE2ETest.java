// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// End to end sanity tests for basic thin client functionality.
public class ThinClientE2ETest {
    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientQuery() {
        CosmosAsyncClient client = null;
        try {
            // If running locally, uncomment these lines
            //System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            //System.setProperty("COSMOS.HTTP2_ENABLED", "true");

            client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .gatewayMode()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();

            CosmosAsyncContainer container = client.getDatabase("db1").getContainer("c2");
            String idName = "id";
            String partitionKeyName = "partitionKey";
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode doc = mapper.createObjectNode();
            String idValue = UUID.randomUUID().toString();
            doc.put(idName, idValue);
            doc.put(partitionKeyName, idValue);

            container.createItem(doc, new PartitionKey(idValue), null).block();

            String query = "select * from c WHERE c." + partitionKeyName + "=@id";
            SqlQuerySpec querySpec = new SqlQuerySpec(query);
            querySpec.setParameters(Arrays.asList(new SqlParameter("@id", idValue)));
            CosmosQueryRequestOptions requestOptions =
                new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(idValue));
            FeedResponse<ObjectNode> response = container
                .queryItems(querySpec, requestOptions, ObjectNode.class)
                .byPage()
                .blockFirst();

            ObjectNode docFromResponse = response.getResults().get(0);
            assertThat(docFromResponse.get(partitionKeyName).textValue()).isEqualTo(idValue);
            assertThat(docFromResponse.get(idName).textValue()).isEqualTo(idValue);

        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test(groups = {"thinclient"}, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void testThinClientDocumentPointOperations() {
        CosmosAsyncClient client = null;
        try {
            // if running locally, uncomment these lines
            // System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");
            // System.setProperty("COSMOS.HTTP2_ENABLED", "true");

            client  = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .gatewayMode()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();

            String idName = "id";
            String partitionKeyName = "partitionKey";

            client.createDatabaseIfNotExists("db1").block();

            CosmosContainerProperties containerDef =
                new CosmosContainerProperties("c2", "/" + partitionKeyName);
            ThroughputProperties ruCfg = ThroughputProperties.createManualThroughput(35_000);

            client.getDatabase("db1").createContainerIfNotExists(containerDef, ruCfg).block();

            CosmosAsyncContainer container = client.getDatabase("db1").getContainer("c2");

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode doc = mapper.createObjectNode();
            String idValue = UUID.randomUUID().toString();
            doc.put(idName, idValue);
            doc.put(partitionKeyName, idValue);

            // create
            CosmosItemResponse<ObjectNode> createResponse = container.createItem(doc).block();
            assertThat(createResponse.getStatusCode()).isEqualTo(201);
            assertThat(createResponse.getRequestCharge()).isGreaterThan(0.0);

            // read
            CosmosItemResponse<ObjectNode> readResponse = container.readItem(idValue, new PartitionKey(idValue), ObjectNode.class).block();
            assertThat(readResponse.getStatusCode()).isEqualTo(200);
            assertThat(readResponse.getRequestCharge()).isGreaterThan(0.0);

            ObjectNode doc2 = mapper.createObjectNode();
            String idValue2 = UUID.randomUUID().toString();
            doc2.put(idName, idValue2);
            doc2.put(partitionKeyName, idValue);

            // replace
            CosmosItemResponse<ObjectNode> replaceResponse = container.replaceItem(doc2, idValue, new PartitionKey(idValue)).block();
            assertThat(replaceResponse.getStatusCode()).isEqualTo(200);
            assertThat(replaceResponse.getRequestCharge()).isGreaterThan(0.0);
            CosmosItemResponse<ObjectNode> readAfterReplaceResponse = container.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            assertThat(readAfterReplaceResponse.getStatusCode()).isEqualTo(200);
            ObjectNode replacedItemFromRead = readAfterReplaceResponse.getItem();
            assertThat(replacedItemFromRead.get(idName).asText()).isEqualTo(idValue2);
            assertThat(replacedItemFromRead.get(partitionKeyName).asText()).isEqualTo(idValue);

            ObjectNode doc3 = mapper.createObjectNode();
            doc3.put(idName, idValue2);
            doc3.put(partitionKeyName, idValue);
            doc3.put("newField", "newValue");

            // upsert
            CosmosItemResponse<ObjectNode> upsertResponse = container.upsertItem(doc3, new PartitionKey(idValue), new CosmosItemRequestOptions()).block();
            assertThat(upsertResponse.getStatusCode()).isEqualTo(200);
            assertThat(upsertResponse.getRequestCharge()).isGreaterThan(0.0);
            CosmosItemResponse<ObjectNode> readAfterUpsertResponse = container.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            ObjectNode upsertedItemFromRead = readAfterUpsertResponse.getItem();
            assertThat(upsertedItemFromRead.get(idName).asText()).isEqualTo(idValue2);
            assertThat(upsertedItemFromRead.get(partitionKeyName).asText()).isEqualTo(idValue);
            assertThat(upsertedItemFromRead.get("newField").asText()).isEqualTo("newValue");

            // patch
            CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
            patchOperations.add("/anotherNewField", "anotherNewValue");
            patchOperations.replace("/newField", "patchedNewField");
            CosmosItemResponse<ObjectNode> patchResponse = container.patchItem(idValue2, new PartitionKey(idValue), patchOperations, ObjectNode.class).block();
            assertThat(patchResponse.getStatusCode()).isEqualTo(200);
            assertThat(patchResponse.getRequestCharge()).isGreaterThan(0.0);
            CosmosItemResponse<ObjectNode> readAfterPatchResponse = container.readItem(idValue2, new PartitionKey(idValue), ObjectNode.class).block();
            ObjectNode patchedItemFromRead = readAfterPatchResponse.getItem();
            assertThat(patchedItemFromRead.get(idName).asText()).isEqualTo(idValue2);
            assertThat(patchedItemFromRead.get(partitionKeyName).asText()).isEqualTo(idValue);
            assertThat(patchedItemFromRead.get("newField").asText()).isEqualTo("patchedNewField");
            assertThat(patchedItemFromRead.get("anotherNewField").asText()).isEqualTo("anotherNewValue");

            // delete
            CosmosItemResponse<Object> deleteResponse = container.deleteItem(idValue2, new PartitionKey(idValue)).block();
            assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
            assertThat(deleteResponse.getRequestCharge()).isGreaterThan(0.0);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
