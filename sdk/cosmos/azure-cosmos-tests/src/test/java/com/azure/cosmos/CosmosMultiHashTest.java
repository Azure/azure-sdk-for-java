/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.reactivex.subscribers.TestSubscriber;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosMultiHashTest extends TestSuiteBase {

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
        paths.add("/areaCode"); // expecting int value type
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
    public void itemCRUD() {
        CityItem cityItem = new CityItem(UUID.randomUUID().toString(), "Redmond", "98052", 1);

        PartitionKey partitionKey =
            new PartitionKeyBuilder()
                .add(cityItem.getCity())
                .add(cityItem.getZipcode())
                .add(cityItem.getAreaCode())
                .build();

        createdMultiHashContainer.createItem(cityItem);

        CosmosItemResponse<CityItem> readResponse = createdMultiHashContainer.readItem(
            cityItem.getId(), partitionKey, CityItem.class);

        assertThat(readResponse.getItem().toString()).isEqualTo(cityItem.toString());
        createdMultiHashContainer.deleteItem(cityItem.getId(), partitionKey, new CosmosItemRequestOptions());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void invalidPartitionKeys() throws CosmosException {
        //Try to use an invalid depth in partition key definition
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

        //Try to build a partition key with PartitionKey.None and other paths
        try {
            new PartitionKeyBuilder().addNoneValue().add("test-value").build();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage().contains("PartitionKey.None can't be used with multiple paths")).isTrue();
        }
    }

    private void validateResponse(FeedResponse<ObjectNode> response,
                                  List<CosmosItemIdentity> cosmosItemIdentityList) {
        assertThat(response.getResults().size()).isEqualTo(cosmosItemIdentityList.size());
        assertThat(
                response
                    .getResults()
                    .stream()
                    .map(item -> item.get("id").asText())
                    .collect(Collectors.toList())
        ).containsAll(
                cosmosItemIdentityList
                    .stream()
                    .map(itemIdentity -> itemIdentity.getId())
                    .collect(Collectors.toList())
        );
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    private void validateDocCRUDAndQuery() throws InterruptedException {

        ArrayList<CityItem> docs = createItems();
        CityItem doc = docs.get(0);

        //Document Create - Negative tests
        //Using incomplete partition key in method params
        logger.info("validateDocCRUDAndQuery-negative tests-using - using incomplete partition key in method params for create");
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
        logger.info("validateDocCRUDAndQuery-negative tests-using - using incomplete partition key in object for create");
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
        logger.info("validateDocCRUDAndQuery-document read using full partition key");
        for (int i = 0; i < docs.size(); i++) {
            CityItem doc_current = docs.get(i);
            partitionKey = new PartitionKeyBuilder()
                .add(doc_current.getCity())
                .add(doc_current.getZipcode())
                .add(doc_current.getAreaCode())
                .build();
            CosmosItemResponse<ObjectNode> response = createdMultiHashContainer.readItem(doc_current.getId(), partitionKey, ObjectNode.class);
            assertThat(response.getItem().get("id").asText()).isEqualTo(doc_current.getId());
        }

        //Document Read - Negative tests
        //Using incomplete partition key
        logger.info("validateDocCRUDAndQuery-negative tests - document read using partial partition key");
        PartitionKey partialPK = new PartitionKeyBuilder().add(doc.getCity()).build();
        try {
            createdMultiHashContainer.readItem(doc.getId(), partialPK, ObjectNode.class);
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Read Many - single item
        logger.info("validateDocCRUDAndQuery-readMany for single item");
        List<CosmosItemIdentity> itemList = new ArrayList<>();
        PartitionKey pkToUse = new PartitionKeyBuilder().add(doc.getCity()).add(doc.getZipcode()).add(doc.getAreaCode()).build();
        itemList.add(new CosmosItemIdentity(pkToUse, doc.getId()));

        FeedResponse<ObjectNode> documentFeedResponse = createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        validateResponse(documentFeedResponse, itemList);

        //Read Many - several items
        logger.info("validateDocCRUDAndQuery-readMany for multiple items");
        itemList = new ArrayList<>();
        for (CityItem cityItem : docs) {
            pkToUse = new PartitionKeyBuilder().add(cityItem.getCity()).add(cityItem.getZipcode()).add(cityItem.getAreaCode()).build();
            itemList.add(new CosmosItemIdentity(pkToUse, cityItem.getId()));
        }
        documentFeedResponse = createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        validateResponse(documentFeedResponse, itemList);


        //Negative test - read many using single item with incomplete partition key
        logger.info("validateDocCRUDAndQuery-negative tests-readMany for single item with incomplete partition key");
        itemList = new ArrayList<>();
        pkToUse = new PartitionKeyBuilder().add(doc.getCity()).build();
        itemList.add(new CosmosItemIdentity(pkToUse, doc.getId()));
        try {
            createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        } catch (IllegalArgumentException e) {
            //Thrown internally by SDK when incomplete keys are used for readMany
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Negative test - read many using several items with incomplete partition keys
        logger.info("validateDocCRUDAndQuery-negative tests-readMany for multiple items with incomplete partition key");
        itemList = new ArrayList<>();
        for (CityItem cityItem : docs) {
            pkToUse = new PartitionKeyBuilder().add(cityItem.getCity()).build();
            itemList.add(new CosmosItemIdentity(pkToUse, cityItem.getId()));
        }
        try {
            createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        } catch (IllegalArgumentException e) {
            //Thrown internally by SDK when incomplete keys are used for readMany
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Read All with full partition key
        logger.info("validateDocCRUDAndQuery-readAll with full partition key");
        partitionKey = new PartitionKeyBuilder()
            .add("Redmond")
            .add("98053")
            .add(1)
            .build();

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setPartitionKey(partitionKey);
        CosmosPagedIterable<ObjectNode> readAllResults =
            createdMultiHashContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);
        assertThat(readAllResults.stream().toArray().length).isEqualTo(2);

        readAllResults = createdMultiHashContainer.readAllItems(partitionKey, ObjectNode.class);
        assertThat(readAllResults.stream().toArray().length).isEqualTo(2);

        //Read All - prefix partition key
        logger.info("validateDocCRUDAndQuery-readAll with prefix partition key");
        partialPK = new PartitionKeyBuilder().add("Redmond").build();
        cosmosQueryRequestOptions.setPartitionKey(partialPK);
        readAllResults = createdMultiHashContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);
        assertThat(readAllResults.stream().toArray().length).isEqualTo(3);

        readAllResults = createdMultiHashContainer.readAllItems(partialPK, ObjectNode.class);
        assertThat(readAllResults.stream().toArray().length).isEqualTo(3);

        //Negative test - read all with non-prefix partition key
        logger.info("validateDocCRUDAndQuery-negative tests - readAll with non-prefix partition key");
        partialPK = new PartitionKeyBuilder().add("98053").build();
        cosmosQueryRequestOptions.setPartitionKey(partialPK);
        readAllResults = createdMultiHashContainer.readAllItems(cosmosQueryRequestOptions, ObjectNode.class);
        assertThat(readAllResults.stream().toArray().length).isEqualTo(0);

        //Document Upsert
        logger.info("validateDocCRUDAndQuery- upserts with full partition key");
        CityItem doc6 = docs.get(6);
        String newVersion = doc6.getVersion() + ".1";
        doc6.setVersion(newVersion);
        createdMultiHashContainer.upsertItem(doc6);
        CosmosItemResponse<CityItem> readResponse = createdMultiHashContainer.upsertItem(doc6);
        assertThat(readResponse.getItem().getVersion()).isEqualTo(newVersion);

        //Negative test - upsert item with incomplete partition key
        logger.info("validateDocCRUDAndQuery-upserts with incomplete partition key");
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
        logger.info("validateDocCRUDAndQuery-replace with full partition key");
        CityItem doc5 = docs.get(5);
        doc5.setVersion(newVersion);
        String newVersionForDoc5 = doc5.getVersion() + ".1";
        doc5.setVersion(newVersionForDoc5);
        partitionKey = new PartitionKeyBuilder()
            .add(doc5.getCity())
            .add(doc5.getZipcode())
            .add(doc5.getAreaCode())
            .build();
        CosmosItemResponse<CityItem> replaceResponse =
                createdMultiHashContainer.replaceItem(doc5, doc5.getId(), partitionKey, new CosmosItemRequestOptions());
        assertThat(replaceResponse.getItem().getVersion()).isEqualTo(newVersionForDoc5);

        //Delete Item
        logger.info("validateDocCRUDAndQuery-delete with full partition key");
        CityItem doc1 = docs.get(1);
        CosmosItemResponse<?> deleteResponse = createdMultiHashContainer.deleteItem(doc1, new CosmosItemRequestOptions());
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

        CityItem doc2 = docs.get(2);
        deleteResponse = createdMultiHashContainer.deleteItem(
                doc2.getId(),
                new PartitionKeyBuilder()
                    .add(doc2.getCity())
                    .add(doc2.getZipcode())
                    .add(doc2.getAreaCode())
                    .build(),
                new CosmosItemRequestOptions());
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

        //Negative test - incomplete partition key
        logger.info("validateDocCRUDAndQuery-negative tests - delete with incomplete partition key");
        try {
            CityItem doc3 = docs.get(3);
            createdMultiHashContainer.deleteItem(
                    doc3.getId(),
                    new PartitionKeyBuilder()
                        .add(doc3.getCity())
                        .build(),
                    new CosmosItemRequestOptions());
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Delete by partition key - needs to be turned on at subscription level for account
        //Works with emulator
        logger.info("validateDocCRUDAndQuery-delete by partition key with full partition key");
        deleteResponse = createdMultiHashContainer.deleteAllItemsByPartitionKey(
            new PartitionKeyBuilder()
                .add(doc5.getCity())
                .add(doc5.getZipcode())
                .add(doc5.getAreaCode())
                .build(),
            new CosmosItemRequestOptions());
        assertThat(deleteResponse.getStatusCode()).isEqualTo(200);

        //Negative test - partial partition key
        //Can't be done since partial partitions can exist over multiple physical partitions and BE does not support
        //these distributed transaction semantics
        logger.info("validateDocCRUDAndQuery-delete by partition key with incomplete partition key");
        try {
            createdMultiHashContainer.deleteAllItemsByPartitionKey(
                new PartitionKeyBuilder()
                    .add(doc6.getCity())
                    .build(),
                new CosmosItemRequestOptions());
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //delete by partition key runs as a background task, give it some time to finish
        Thread.sleep(500);

        logger.info("validateDocCRUDAndQuery-delete all items");
        deleteAllItems();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    private void multiHashQueryTests() {
        ArrayList<CityItem> docs = createItems();

        //Query Tests
        PartitionKey partitionKey;
        for (int i = 0; i < docs.size(); i++) {
            CityItem doc_current = docs.get(i);
            //Build the partition key
            partitionKey = new PartitionKeyBuilder()
                .add(doc_current.getCity())
                .add(doc_current.getZipcode())
                .add(doc_current.getAreaCode())
                .build();

            //Build the query request options
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setPartitionKey(partitionKey);

            //Run the query.
            String query = String.format("SELECT * from c where c.id = '%s'", doc_current.getId());

            CosmosPagedIterable<ObjectNode> feedResponseIterator =
                createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
            assertThat(feedResponseIterator.iterator().hasNext()).isTrue();

            query = String.format("SELECT * from c where c.id = '%s'", doc_current.getId());
            queryRequestOptions = new CosmosQueryRequestOptions();
            feedResponseIterator =
                createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
            assertThat(feedResponseIterator.iterator().hasNext()).isTrue();
        }

        partitionKey = new PartitionKeyBuilder()
            .add("Redmond")
            .add("98053")
            .add(1)
            .build();

        String query = "SELECT * from c";
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setPartitionKey(partitionKey);
        CosmosPagedIterable<ObjectNode> feedResponseIterator =
            createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator.stream().count()).isEqualTo(2);

        query = String.format("SELECT * from c where c.city = '%s'", docs.get(2).getCity());
        queryRequestOptions = new CosmosQueryRequestOptions();
        feedResponseIterator = createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
        assertThat(feedResponseIterator.stream().count()).isEqualTo(3);

        query = String.format("SELECT * from c where c.city = '%s'", docs.get(0).getCity());
        partitionKey =
            new PartitionKeyBuilder()
                .add("Redmond")
                .add("98053")
                .add(1)
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

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    private void fromObjectArrayTests() {
        CityItem cityItem = new CityItem(UUID.randomUUID().toString(), "Redmond", "98052", 1);

        // Test functionality
        Object[] values = new Object[3];
        values[0] = cityItem.getCity();
        values[1] = cityItem.getZipcode();
        values[2] = cityItem.getAreaCode();
        PartitionKey test = PartitionKey.fromObjectArray(values, false);
        assertThat(test.toString()).isEqualTo("[\"Redmond\",\"98052\",1.0]");

        // Test invalid input for values
        try {
            PartitionKey testError = PartitionKey.fromObjectArray(null, false);
            Assert.fail();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("Argument 'values' must not be null.");
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    private void extractPartitionKeyFromDocumentTests() {
        CityItem cityItem = new CityItem(UUID.randomUUID().toString(), "Redmond", "98052", 1);

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setKind(PartitionKind.MULTI_HASH);
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/city");
        paths.add("/zipcode");
        paths.add("/areaCode");
        partitionKeyDefinition.setPaths(paths);

        // Test functionality
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> mapObject = (Map<String, Object>)om.convertValue(cityItem,
            new ConcurrentHashMap<String, Object>().getClass());
        PartitionKey test = PartitionKey.fromItem(mapObject, partitionKeyDefinition);
        assertThat(test.toString()).isEqualTo("[\"Redmond\",\"98052\",1.0]");

        // Test invalid input for item
        try {
            PartitionKey testDocumentError = PartitionKey.fromItem(null, partitionKeyDefinition);
            Assert.fail();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("Argument 'item' must not be null.");
        }

        // Test invalid input for partitionKeyDefinition
        try {
            PartitionKey testDocumentError = PartitionKey.fromItem(mapObject, null);
            Assert.fail();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("Argument 'partitionKeyDefinition' must not be null.");
        }
    }

    private ArrayList<CityItem> createItems() {

        ArrayList<CityItem> docs = new ArrayList<>();
        docs.add(new CityItem(UUID.randomUUID().toString(), "Redmond", "98053", 1));
        docs.add(new CityItem(UUID.randomUUID().toString(), "Pittsburgh", "15232", 2));
        docs.add(new CityItem(UUID.randomUUID().toString(), "Stonybrook", "11790", 3));
        docs.add(new CityItem(UUID.randomUUID().toString(), "Stonybrook", "11794", 3));
        docs.add(new CityItem(UUID.randomUUID().toString(), "Stonybrook", "11791", 3));
        docs.add(new CityItem(UUID.randomUUID().toString(), "Redmond", "98053", 1));
        docs.add(new CityItem(UUID.randomUUID().toString(), "Redmond", "12345", 1));

        for (CityItem cityItem : docs) {
            createdMultiHashContainer.createItem(cityItem);
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

    private void testPartialPKContinuationToken() {
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

    private static class CityItem {
        private String id;
        private String city;
        private String zipcode;
        private int areaCode;
        private String version;

        public CityItem(String id, String city, String zipcode, int areaCode) {
            this.id = id;
            this.city = city;
            this.zipcode = zipcode;
            this.areaCode = areaCode;
            this.version = "v1";
        }

        public CityItem() {}

        public String getId() {
            return id;
        }

        public String getCity() {
            return city;
        }

        public String getZipcode() {
            return zipcode;
        }

        public int getAreaCode() {
            return areaCode;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "CityItem{" +
                    "id='" + id + '\'' +
                    ", city='" + city + '\'' +
                    ", zipcode='" + zipcode + '\'' +
                    ", areaCode=" + areaCode +
                    ", version='" + version + '\'' +
                    '}';
        }
    }

}
