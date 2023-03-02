/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosMultiHashTest extends TestSuiteBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final JsonNodeFactory JSON_NODE_FACTORY_INSTANCE = JsonNodeFactory.withExactBigDecimals(true);

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;
    private CosmosContainer createdMultiHashContainer;

    @Factory(dataProvider = "clientBuilders")
    public CosmosMultiHashTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosMultiHashTest() {
        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
        String collectionName = UUID.randomUUID().toString();

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setKind(PartitionKind.MULTI_HASH);
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/city");
        paths.add("/zipcode");
        partitionKeyDefinition.setPaths(paths);

        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName, partitionKeyDefinition);

        //MultiHash collection create
        createdDatabase.createContainer(containerProperties);

        //MultiHash collection read
        createdMultiHashContainer = createdDatabase.getContainer(collectionName);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting cleanup....");
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void itemCRUD() throws Exception {

        List<String> pkIds = new ArrayList<>();
        pkIds.add("Redmond");
        pkIds.add("98052");

        PartitionKey partitionKey =
            new PartitionKeyBuilder()
                .add(pkIds.get(0))
                .add(pkIds.get(1))
                .build();

        String documentId = UUID.randomUUID().toString();
        ObjectNode properties = getItem(documentId, pkIds);
        createdMultiHashContainer.createItem(properties);

        CosmosItemResponse<ObjectNode> readResponse = createdMultiHashContainer.readItem(
            documentId, partitionKey, ObjectNode.class);
        validateIdOfItemResponse(documentId, readResponse);
        assertThat(readResponse.getItem().equals(properties));
        createdMultiHashContainer.deleteItem(documentId, partitionKey, new CosmosItemRequestOptions());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void invalidPartitionKeyDepth() throws CosmosException {
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setKind(PartitionKind.MULTI_HASH);
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/country");
        paths.add("/city");
        paths.add("/zipcode");
        paths.add("/street");
        partitionKeyDefinition.setPaths(paths);

        CosmosContainerProperties containerProperties = getCollectionDefinition(UUID.randomUUID().toString(),
            partitionKeyDefinition);

        try {
            createdDatabase.createContainer(containerProperties);
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Too many partition key paths (4) specified. A maximum of 3 is allowed.")).isTrue();
        }
    }

    private ObjectNode getItem(String documentId, List<String> pkIds) throws JsonProcessingException {
        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"city\": \"%s\", "
                + "\"zipcode\": \"%s\" "
                + "}"
            , documentId, pkIds.get(0), pkIds.get(1));
        return
            OBJECT_MAPPER.readValue(json, ObjectNode.class);
    }

    private void validateItemResponse(ObjectNode itemProperties,
                                      CosmosItemResponse<ObjectNode> createResponse) {
        //Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(itemProperties.get("id").asText());
    }

    private void validateIdOfItemResponse(String expectedId, CosmosItemResponse<ObjectNode> createResponse) {
        //Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(expectedId);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    private void validateDocCRUDandQuery() throws Exception {

        ArrayList<ObjectNode> docs = createItems();
        ObjectNode doc = docs.get(0);

        //Document Create - Negative tests
        //Using incomplete partition key in method params
        PartitionKey partitionKey =
            new PartitionKeyBuilder()
                .add("Redmond")
                .build();
        try {
            createdMultiHashContainer.createItem(doc, partitionKey, new CosmosItemRequestOptions());
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Using incomplete partition key in item body
        ObjectNode wrongDoc = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        wrongDoc.set("id", new TextNode(UUID.randomUUID().toString()));
        wrongDoc.set("city", new TextNode("Redmond"));
        try {
            createdMultiHashContainer.createItem(wrongDoc);
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("PartitionKey extracted from document doesn't match the one specified in the header.")).isTrue();
        }

        //Document Read
        for (int i = 0; i < docs.size(); i++) {
            ObjectNode doc_current = docs.get(i);
            partitionKey = new PartitionKeyBuilder()
                .add(doc_current.get("city").asText())
                .add(doc_current.get("zipcode").asText())
                .build();
            CosmosItemResponse<ObjectNode> response = createdMultiHashContainer.readItem(doc_current.get("id").asText(), partitionKey, ObjectNode.class);
            validateItemResponse(doc_current, response);
        }

        //Document Read - Negative tests
        //Using incomplete partition key
        PartitionKey partialPK = new PartitionKeyBuilder().add(doc.get("city").asText()).build();
        try {
            createdMultiHashContainer.readItem(doc.get("id").asText(), partialPK, ObjectNode.class);
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Read Many - single item
        List<Pair<String, PartitionKey>> pairList = new ArrayList<>();
        List<CosmosItemIdentity> itemList = new ArrayList<>();
        pairList.add(Pair.of(doc.get("id").asText(),
            new PartitionKeyBuilder().add(doc.get("city").asText()).add(doc.get("zipcode").asText()).build()));
        PartitionKey pkToUse = new PartitionKeyBuilder().add(doc.get("city").asText()).add(doc.get("zipcode").asText()).build();
        itemList.add(new CosmosItemIdentity(pkToUse, doc.get("id").asText()));

        FeedResponse<ObjectNode> documentFeedResponse = createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        assertThat(documentFeedResponse.getResults().size()).isEqualTo(pairList.size());
        assertThat(documentFeedResponse.getResults().stream().map(jsonNode -> jsonNode.get("id").textValue()).collect(Collectors.toList()))
            .containsAll(pairList.stream().map(p -> p.getLeft()).collect(Collectors.toList()));

        //Read Many - several items
        pairList = new ArrayList<>();
        itemList = new ArrayList<>();
        for (ObjectNode jsonNodes : docs) {
            pairList.add(Pair.of(jsonNodes.get("id").asText(),
                new PartitionKeyBuilder().add(jsonNodes.get("city").asText()).add(jsonNodes.get("zipcode").asText()).build()));
            pkToUse = new PartitionKeyBuilder().add(jsonNodes.get("city").asText()).add(jsonNodes.get("zipcode").asText()).build();
            itemList.add(new CosmosItemIdentity(pkToUse, jsonNodes.get("id").asText()));
        }
        documentFeedResponse = createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        assertThat(documentFeedResponse.getResults().size()).isEqualTo(pairList.size());
        assertThat(documentFeedResponse.getResults().stream().map(jsonNode -> jsonNode.get("id").textValue()).collect(Collectors.toList()))
            .containsAll(pairList.stream().map(p -> p.getLeft()).collect(Collectors.toList()));

        //Negative test - read many using single item with incomplete partition key
        itemList = new ArrayList<>();
        pkToUse = new PartitionKeyBuilder().add(doc.get("city").asText()).build();
        itemList.add(new CosmosItemIdentity(pkToUse, doc.get("id").asText()));
        try {
            createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        } catch (IllegalArgumentException e) {
            //Thrown internally by SDK when incomplete keys are used for readMany
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Negative test - read many using several items with incomplete partition keys
        itemList = new ArrayList<>();
        for (ObjectNode jsonNodes : docs) {
            pkToUse = new PartitionKeyBuilder().add(jsonNodes.get("city").asText()).build();
            itemList.add(new CosmosItemIdentity(pkToUse, jsonNodes.get("id").asText()));
        }
        try {
            createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        } catch (IllegalArgumentException e) {
            //Thrown internally by SDK when incomplete keys are used for readMany
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Read All
        partitionKey = new PartitionKeyBuilder()
            .add("Redmond")
            .add("98053")
            .build();
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setPartitionKey(partitionKey);
        CosmosPagedIterable<ObjectNode> readAllResults =
            createdMultiHashContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);
        assertThat(readAllResults.stream().toArray().length).isEqualTo(2);

        //Read All - prefix partition key
        partialPK = new PartitionKeyBuilder().add("Redmond").build();
        cosmosQueryRequestOptions.setPartitionKey(partialPK);
        readAllResults = createdMultiHashContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);
        assertThat(readAllResults.stream().toArray().length).isEqualTo(3);

        //Negative test - read all with non-prefix partition key
        partialPK = new PartitionKeyBuilder().add("98053").build();
        cosmosQueryRequestOptions.setPartitionKey(partialPK);
        readAllResults = createdMultiHashContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);
        assertThat(readAllResults.stream().toArray().length).isEqualTo(0);

        //Document Upsert
        TextNode version = new TextNode(UUID.randomUUID().toString());
        ObjectNode doc6 = docs.get(6);
        doc6.set("version", version);
        createdMultiHashContainer.upsertItem(doc6);
        partitionKey = new PartitionKeyBuilder()
            .add(doc6.get("city").asText())
            .add(doc6.get("zipcode").asText())
            .build();
        CosmosItemResponse<ObjectNode> readResponse = createdMultiHashContainer.readItem(doc6.get("id").asText(), partitionKey, ObjectNode.class);
        assertThat(readResponse.getItem().get("version")).isEqualTo(version);

        //Negative test - upsert item with incomplete partition key
        ObjectNode badDoc = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        badDoc.set("id", new TextNode(UUID.randomUUID().toString()));
        badDoc.set("city", new TextNode("Stonybrook"));
        try {
            createdMultiHashContainer.upsertItem(wrongDoc);
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("PartitionKey extracted from document doesn't match the one specified in the header.")).isTrue();
        }

        //Document Replace
        ObjectNode doc5 = docs.get(5);
        doc5.set("version", version);
        partitionKey = new PartitionKeyBuilder()
            .add(doc5.get("city").asText())
            .add(doc5.get("zipcode").asText())
            .build();
        CosmosItemResponse<ObjectNode> replaceResponse = createdMultiHashContainer.replaceItem(doc5, doc5.get("id").asText(), partitionKey, new CosmosItemRequestOptions());
        assertThat(replaceResponse.getItem().get("version")).isEqualTo(version);

        //Delete Item
        ObjectNode doc1 = docs.get(1);
        CosmosItemResponse<?> deleteResponse = createdMultiHashContainer.deleteItem(doc1, new CosmosItemRequestOptions());
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

        ObjectNode doc2 = docs.get(2);
        deleteResponse = createdMultiHashContainer.deleteItem(doc2.get("id").asText(),
            new PartitionKeyBuilder()
                .add(doc2.get("city").asText())
                .add(doc2.get("zipcode").asText())
                .build(), new CosmosItemRequestOptions());
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

        //Negative test - incomplete partition key
        try {
            ObjectNode doc3 = docs.get(3);
            createdMultiHashContainer.deleteItem(doc3.get("id").asText(),
                new PartitionKeyBuilder()
                    .add(doc3.get("city").asText())
                    .build(), new CosmosItemRequestOptions());
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Delete by partition key - needs to be turned on at subscription level for account
        //Works with emulator
        deleteResponse = createdMultiHashContainer.deleteAllItemsByPartitionKey(
            new PartitionKeyBuilder()
                .add(doc5.get("city").asText())
                .add(doc5.get("zipcode").asText())
                .build(), new CosmosItemRequestOptions());
        assertThat(deleteResponse.getStatusCode()).isEqualTo(200);

        //Negative test - partial partition key
        //Can't be done since partial partitions can exist over multiple physical partitions and BE does not support
        //these distributed transaction semantics
        try {
            createdMultiHashContainer.deleteAllItemsByPartitionKey(new PartitionKeyBuilder()
                .add(doc6.get("city").asText())
                .build(), new CosmosItemRequestOptions());
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }
        deleteAllItems();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    private void multiHashQueryTests() throws Exception {
        ArrayList<ObjectNode> docs = createItems();

        //Query Tests
        PartitionKey partitionKey;
        for (int i = 0; i < docs.size(); i++) {
            ObjectNode doc_current = docs.get(i);
            //Build the partition key
            partitionKey = new PartitionKeyBuilder()
                .add(doc_current.get("city").asText())
                .add(doc_current.get("zipcode").asText())
                .build();

            //Build the query request options
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setPartitionKey(partitionKey);

            //Run the query.
            String query = String.format("SELECT * from c where c.id = '%s'", doc_current.get("id").asText());

            CosmosPagedIterable<ObjectNode> feedResponseIterator =
                createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
            assertThat(feedResponseIterator.iterator().hasNext()).isTrue();

            query = String.format("SELECT * from c where c.id = '%s'", doc_current.get("id").asText());
            queryRequestOptions = new CosmosQueryRequestOptions();
            feedResponseIterator =
                createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
            assertThat(feedResponseIterator.iterator().hasNext()).isTrue();
        }

        partitionKey = new PartitionKeyBuilder()
            .add("Redmond")
            .add("98053")
            .build();

        String query = "SELECT * from c";
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setPartitionKey(partitionKey);
        CosmosPagedIterable<ObjectNode> feedResponseIterator =
            createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator.stream().count()).isEqualTo(2);

        query = String.format("SELECT * from c where c.city = '%s'", docs.get(2).get("city").asText());
        queryRequestOptions = new CosmosQueryRequestOptions();
        feedResponseIterator = createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator.stream().count()).isEqualTo(3);

        query = String.format("SELECT * from c where c.city = '%s'", docs.get(0).get("city").asText());
        partitionKey =
            new PartitionKeyBuilder()
                .add("Redmond")
                .add("98053")
                .build();
        queryRequestOptions.setPartitionKey(partitionKey);
        feedResponseIterator = createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator.stream().count()).isEqualTo(2);

        //Prefix partition key in query options tests
        query = "SELECT * from c";
        partitionKey =
            new PartitionKeyBuilder()
                .add("Redmond")
                .build();
        queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setPartitionKey(partitionKey);
        feedResponseIterator = createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator.stream().toArray().length).isEqualTo(3);

        //Using distinct
        query = "Select distinct c.zipcode from c";
        feedResponseIterator = createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator.stream().toArray().length).isEqualTo(2);

        //Using paging/ order by
        query = "SELECT * FROM c ORDER BY c.zipcode ASC";
        CosmosPagedIterable<ObjectNode> cosmosPagedIterable = createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        Iterable<FeedResponse<ObjectNode>> feedResponses = cosmosPagedIterable.iterableByPage(2);
        FeedResponse<ObjectNode> feedResponse = feedResponses.iterator().next();
        assertThat(feedResponse.getResults().size()).isEqualTo(2);
        assertThat(feedResponse.getResults().get(0).get("zipcode").asInt() < feedResponse.getResults().get(1).get("zipcode").asInt()).isTrue();

        //Using continuation token
        testPartialPKContinuationToken();

        //Negative test - using non-prefix partial partition key returns no results
        query = "SELECT * from c";
        partitionKey = new PartitionKeyBuilder().add("98053").build(); //pk definition is state/zipcode, so zipcode fails
        queryRequestOptions.setPartitionKey(partitionKey);
        feedResponseIterator = createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator.stream().toArray().length).isEqualTo(0);
        deleteAllItems();
    }

    private ArrayList<ObjectNode> createItems() {

        ArrayList<ObjectNode> docs = new ArrayList<>();

        ObjectNode doc = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc.set("id", new TextNode(UUID.randomUUID().toString()));
        doc.set("city", new TextNode("Redmond"));
        doc.set("zipcode", new TextNode("98053"));
        docs.add(doc);

        ObjectNode doc1 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc1.set("id", new TextNode(UUID.randomUUID().toString()));
        doc1.set("city", new TextNode("Pittsburgh"));
        doc1.set("zipcode", new TextNode("15232"));
        docs.add(doc1);

        ObjectNode doc2 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc2.set("id", new TextNode(UUID.randomUUID().toString()));
        doc2.set("city", new TextNode("Stonybrook"));
        doc2.set("zipcode", new TextNode("11790"));
        docs.add(doc2);

        ObjectNode doc3 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc3.set("id", new TextNode(UUID.randomUUID().toString()));
        doc3.set("city", new TextNode("Stonybrook"));
        doc3.set("zipcode", new TextNode("11794"));
        docs.add(doc3);

        ObjectNode doc4 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc4.set("id", new TextNode(UUID.randomUUID().toString()));
        doc4.set("city", new TextNode("Stonybrook"));
        doc4.set("zipcode", new TextNode("11791"));
        docs.add(doc4);

        ObjectNode doc5 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc5.set("id", new TextNode(UUID.randomUUID().toString()));
        doc5.set("city", new TextNode("Redmond"));
        doc5.set("zipcode", new TextNode("98053"));
        docs.add(doc5);

        ObjectNode doc6 = new ObjectNode(JSON_NODE_FACTORY_INSTANCE);
        doc6.set("id", new TextNode(UUID.randomUUID().toString()));
        doc6.set("city", new TextNode("Redmond"));
        doc6.set("zipcode", new TextNode("12345"));
        docs.add(doc6);

        //Document Create
        for (int i = 0; i < docs.size(); i++) {
            createdMultiHashContainer.createItem(docs.get(i));
        }
        return docs;
    }

    private void deleteAllItems() {
        String query = "SELECT * from c";
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedIterable<ObjectNode> feedResponseIterator =
            createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        for (Object item : feedResponseIterator.stream().toArray()) {
            createdMultiHashContainer.deleteItem(item, new CosmosItemRequestOptions());
        }
    }

    private void testPartialPKContinuationToken() throws Exception {
        String requestContinuation = null;
        List<ObjectNode> receivedDocuments = new ArrayList<>();
        CosmosAsyncClient asyncClient = getClientBuilder().buildAsyncClient();
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(createdDatabase.getId(), asyncClient);
        CosmosAsyncContainer cosmosAsyncContainer = new CosmosAsyncContainer(createdMultiHashContainer.getId(), cosmosAsyncDatabase);
        String query = "SELECT * FROM c ORDER BY c.zipcode ASC";
        PartitionKey partitionKey =
            new PartitionKeyBuilder()
                .add("Redmond")
                .build();
        do {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setPartitionKey(partitionKey);

            options.setMaxDegreeOfParallelism(2);
            CosmosPagedFlux<ObjectNode> queryObservable = cosmosAsyncContainer.queryItems(query, options, ObjectNode.class);

            TestSubscriber<FeedResponse<ObjectNode>> testSubscriber = new TestSubscriber<>();
            queryObservable.byPage(requestContinuation, 1).subscribe(testSubscriber);
            testSubscriber.awaitTerminalEvent(TIMEOUT, TimeUnit.MILLISECONDS);
            testSubscriber.assertNoErrors();
            testSubscriber.assertComplete();

            @SuppressWarnings("unchecked")
            FeedResponse<ObjectNode> firstPage = (FeedResponse<ObjectNode>) testSubscriber.getEvents().get(0).get(0);
            requestContinuation = firstPage.getContinuationToken();
            receivedDocuments.addAll(firstPage.getResults());
            assertThat(firstPage.getResults().size()).isEqualTo(1);
        } while (requestContinuation != null);
        assertThat(receivedDocuments.size()).isEqualTo(3);
        asyncClient.close();
    }

}
