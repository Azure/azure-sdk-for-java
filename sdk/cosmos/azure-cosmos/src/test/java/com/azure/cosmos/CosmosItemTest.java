/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosItemTest extends TestSuiteBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosItemTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_alreadyExists() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);

        // Test for conflict
        try {
            container.createItem(properties, new CosmosItemRequestOptions());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosException.class);
            assertThat(((CosmosException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
                                                                                    new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                                                    new CosmosItemRequestOptions(),
                                                                                    InternalObjectNode.class);
        validateItemResponse(properties, readResponse1);

    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItemWithEventualConsistency() throws Exception {

        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());

        String idAndPkValue = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(idAndPkValue, idAndPkValue);
        CosmosItemResponse<ObjectNode> itemResponse = container.createItem(properties);

        CosmosItemResponse<ObjectNode> readResponse1 = container.readItem(
            idAndPkValue,
            new PartitionKey(idAndPkValue),
            new CosmosItemRequestOptions()
                // generate an invalid session token large enough to cause an error in Gateway
                // due to header being too long
                .setSessionToken(StringUtils.repeat("SomeManualInvalidSessionToken", 2000))
                .setConsistencyLevel(ConsistencyLevel.EVENTUAL),
            ObjectNode.class);

        logger.info("REQUEST DIAGNOSTICS: {}", readResponse1.getDiagnostics().toString());
        validateIdOfItemResponse(idAndPkValue, readResponse1);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem() throws Exception{
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        validateItemResponse(properties, itemResponse);
        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        ModelBridgeInternal.setPartitionKey(options, new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")));
        // replace document
        CosmosItemResponse<InternalObjectNode> replace = container.replaceItem(properties,
                                                              properties.getId(),
                                                              new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                              options);
        assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(BridgeInternal.getProperties(replace), "newProp")).isEqualTo(newPropValue);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
                                                                    new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                                    options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteAllItemsByPartitionKeyAsync() throws Exception {
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
        CosmosItemResponse<?> deleteResponse = container.deleteAllItemsByPartitionKeyAsync(
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

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItemUsingEntity() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        CosmosItemResponse<?> deleteResponse = container.deleteItem(itemResponse.getItem(), options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItems() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItems() throws Exception{
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
                container.queryItems(query, cosmosQueryRequestOptions, InternalObjectNode.class);

        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItemsWithEventualConsistency() throws Exception{

        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());

        String idAndPkValue = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(idAndPkValue, idAndPkValue);
        CosmosItemResponse<ObjectNode> itemResponse = container.createItem(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", idAndPkValue);
        CosmosQueryRequestOptions cosmosQueryRequestOptions =
            new CosmosQueryRequestOptions()
                // generate an invalid session token large enough to cause an error in Gateway
                // due to header being too long
                .setSessionToken(StringUtils.repeat("SomeManualInvalidSessionToken", 2000))
                .setConsistencyLevel(ConsistencyLevel.EVENTUAL);

        CosmosPagedIterable<ObjectNode> feedResponseIterator1 =
            container.queryItems(query, cosmosQueryRequestOptions, ObjectNode.class);
        feedResponseIterator1.handle(
            (r) -> logger.info("Query RequestDiagnostics: {}", r.getCosmosDiagnostics().toString()));

        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();
        assertThat(feedResponseIterator1.stream().count() == 1);

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<ObjectNode> feedResponseIterator3 =
            container.queryItems(querySpec, cosmosQueryRequestOptions, ObjectNode.class);
        feedResponseIterator3.handle(
            (r) -> logger.info("Query RequestDiagnostics: {}", r.getCosmosDiagnostics().toString()));
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
        assertThat(feedResponseIterator3.stream().count() == 1);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItemsWithContinuationTokenAndPageSize() throws Exception{
        List<String> actualIds = new ArrayList<>();
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.getId());
        properties = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.getId());
        properties = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.getId());


        String query = String.format("SELECT * from c where c.id in ('%s', '%s', '%s')", actualIds.get(0), actualIds.get(1), actualIds.get(2));
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
            container.queryItems(query, cosmosQueryRequestOptions, InternalObjectNode.class);

        do {
            Iterable<FeedResponse<InternalObjectNode>> feedResponseIterable =
                feedResponseIterator1.iterableByPage(continuationToken, pageSize);
            for (FeedResponse<InternalObjectNode> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while(continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);

    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsOfLogicalPartition() throws Exception{
        String pkValue = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString(), pkValue);
        CosmosItemResponse<ObjectNode> itemResponse = container.createItem(properties);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<ObjectNode> feedResponseIterator1 =
            container.readAllItems(
                new PartitionKey(pkValue),
                cosmosQueryRequestOptions,
                ObjectNode.class);
        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        CosmosPagedIterable<ObjectNode> feedResponseIterator3 =
            container.readAllItems(
                new PartitionKey(pkValue),
                cosmosQueryRequestOptions,
                ObjectNode.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsOfLogicalPartitionWithContinuationTokenAndPageSize() throws Exception{
        String pkValue = UUID.randomUUID().toString();
        List<String> actualIds = new ArrayList<>();
        ObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString(), pkValue);
        container.createItem(properties);

        properties = getDocumentDefinition(UUID.randomUUID().toString(), pkValue);
        container.createItem(properties);

        properties = getDocumentDefinition(UUID.randomUUID().toString(), pkValue);
        container.createItem(properties);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
            container.readAllItems(
                new PartitionKey(pkValue),
                cosmosQueryRequestOptions,
                InternalObjectNode.class);

        do {
            Iterable<FeedResponse<InternalObjectNode>> feedResponseIterable =
                feedResponseIterator1.iterableByPage(continuationToken, pageSize);
            for (FeedResponse<InternalObjectNode> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while(continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties =
            new InternalObjectNode(String.format("{ "
                                                       + "\"id\": \"%s\", "
                                                       + "\"mypk\": \"%s\", "
                                                       + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                       + "}"
                , documentId, uuid));
        return properties;
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

    private void validateItemResponse(InternalObjectNode containerProperties,
                                      CosmosItemResponse<InternalObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateIdOfItemResponse(String expectedId, CosmosItemResponse<ObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(expectedId);
    }
}
