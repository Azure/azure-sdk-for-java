/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteAllItemsByPartitionKeyTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public DeleteAllItemsByPartitionKeyTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_DeleteAllItemsByPartitionKeyTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void deleteAllItemsByPartitionKey() throws Exception {
        String pkValue1 = UUID.randomUUID().toString();
        String pkValue2 = UUID.randomUUID().toString();

        // item 1
        ObjectNode properties1 = getDocumentDefinition(UUID.randomUUID().toString(), pkValue1);
        CosmosItemResponse<ObjectNode> itemResponse1 = container.createItem(properties1);

        // item 2
        ObjectNode properties2 = getDocumentDefinition(UUID.randomUUID().toString(), pkValue1);
        CosmosItemResponse<ObjectNode> itemResponse2 = container.createItem(properties2);


        // item 3
        ObjectNode properties3 = getDocumentDefinition(UUID.randomUUID().toString(), pkValue2);
        CosmosItemResponse<ObjectNode> itemResponse3 = container.createItem(properties3);


        // delete the items with partition key pk1
        CosmosItemResponse<?> deleteResponse = container.deleteAllItemsByPartitionKey(
            new PartitionKey(pkValue1), new CosmosItemRequestOptions());

        assertThat(deleteResponse.getStatusCode()).isEqualTo(200);
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();

        // verify that the items with partition key pkValue1 are deleted
        CosmosPagedIterable<ObjectNode> feedResponseIterator1 =
            container.readAllItems(
                new PartitionKey(pkValue1),
                new CosmosQueryRequestOptions(),
                ObjectNode.class);
        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isFalse();

        //verify that the item with the other partition Key pkValue2 is not deleted
        CosmosPagedIterable<ObjectNode> feedResponseIterator2 =
            container.readAllItems(
                new PartitionKey(pkValue2),
                new CosmosQueryRequestOptions(),
                ObjectNode.class);
        // Very basic validation
        assertThat(feedResponseIterator2.iterator().hasNext()).isTrue();
    }

    private ObjectNode getDocumentDefinition(String documentId, String pkId) throws JsonProcessingException {

        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , documentId, pkId);
        return
            OBJECT_MAPPER.readValue(json, ObjectNode.class);
    }
}
