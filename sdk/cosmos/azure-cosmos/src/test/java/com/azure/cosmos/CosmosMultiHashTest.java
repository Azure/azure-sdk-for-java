/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
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
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(itemProperties.get("id").asText());
    }

    private void validateIdOfItemResponse(String expectedId, CosmosItemResponse<ObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(expectedId);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    private void validateDocCRUDandQuery() throws Exception {

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

        //Read Many
        List<Pair<String, PartitionKey>> pairList = new ArrayList<>();
        List<CosmosItemIdentity> itemList = new ArrayList<>();
        for (ObjectNode jsonNodes : docs) {
            pairList.add(Pair.of(jsonNodes.get("id").asText(),
                new PartitionKeyBuilder().add(jsonNodes.get("city").asText()).add(jsonNodes.get("zipcode").asText()).build()));
            PartitionKey pkToUse = new PartitionKeyBuilder().add(jsonNodes.get("city").asText()).add(jsonNodes.get("zipcode").asText()).build();
            itemList.add(new CosmosItemIdentity(pkToUse, jsonNodes.get("id").asText()));
        }
        FeedResponse<ObjectNode> documentFeedResponse = createdMultiHashContainer.readMany(itemList, ObjectNode.class);
        assertThat(documentFeedResponse.getResults().size()).isEqualTo(pairList.size());
        assertThat(documentFeedResponse.getResults().stream().map(jsonNode -> jsonNode.get("id").textValue()).collect(Collectors.toList()))
            .containsAll(pairList.stream().map(p -> p.getLeft()).collect(Collectors.toList()));

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

        //Read All - Negative tests
        //Using incomplete partition key
        try {
            readAllResults = createdMultiHashContainer.readAllItems(partialPK, ObjectNode.class);
            readAllResults.stream().toArray();
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Document Update
        TextNode version = new TextNode(UUID.randomUUID().toString());
        doc6.set("version", version);
        createdMultiHashContainer.upsertItem(doc6);
        partitionKey = new PartitionKeyBuilder()
            .add(doc6.get("city").asText())
            .add(doc6.get("zipcode").asText())
            .build();
        CosmosItemResponse<ObjectNode> x = createdMultiHashContainer.readItem(doc6.get("id").asText(), partitionKey, ObjectNode.class);
        assertThat(x.getItem().get("version")).isEqualTo(version);

        // Query Tests
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
            .add(doc5.get("city").asText())
            .add(doc5.get("zipcode").asText())
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

        //Negative test - query option fails with incomplete partition key
        //Not supported in query pipeline yet
        query = String.format("SELECT * from c where c.city = '%s'", docs.get(0).get("city").asText());
        partitionKey =
            new PartitionKeyBuilder()
                .add("Redmond")
                .build();
        queryRequestOptions.setPartitionKey(partitionKey);
        try {
            feedResponseIterator = createdMultiHashContainer.queryItems(query, queryRequestOptions, ObjectNode.class);
            feedResponseIterator.stream().toArray();
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Delete Item
        CosmosItemResponse<?> deleteResponse = createdMultiHashContainer.deleteItem(doc1, new CosmosItemRequestOptions());
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

        deleteResponse = createdMultiHashContainer.deleteItem(doc2.get("id").asText(),
            new PartitionKeyBuilder()
                .add(doc2.get("city").asText())
                .add(doc2.get("zipcode").asText())
                .build(), new CosmosItemRequestOptions());
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

        // Negative test - incomplete partition key
        try {
            createdMultiHashContainer.deleteItem(doc3.get("id").asText(),
                new PartitionKeyBuilder()
                    .add(doc3.get("city").asText())
                    .build(), new CosmosItemRequestOptions());
        } catch (CosmosException e) {
            assertThat(e.getStatusCode()).isEqualTo(400);
            assertThat(e.getMessage().contains("Partition key provided either doesn't correspond to definition in the collection or doesn't match partition key field values specified in the document.")).isTrue();
        }

        //Delete by partition key - needs to be turned on at subscription level for account being used
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
    }
}
