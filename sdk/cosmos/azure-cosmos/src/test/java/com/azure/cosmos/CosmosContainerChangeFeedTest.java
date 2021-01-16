/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.guava25.collect.ArrayListMultimap;
import com.azure.cosmos.implementation.guava25.collect.Multimap;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosContainerChangeFeedTest extends TestSuiteBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PARTITION_KEY_FIELD_NAME = "mypk";
    private CosmosClient client;
    private CosmosAsyncContainer createdAsyncContainer;
    private CosmosAsyncDatabase createdAsyncDatabase;
    private CosmosContainer createdContainer;
    private CosmosDatabase createdDatabase;
    private final Multimap<String, ObjectNode> partitionKeyToDocuments = ArrayListMultimap.create();
    private final String preExistingDatabaseId = CosmosDatabaseForTest.generateId();

    @Factory(dataProvider = "clientBuilders")
    public CosmosContainerChangeFeedTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @AfterClass(groups = { "emulator" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @AfterMethod(groups = { "emulator" })
    public void afterTest() throws Exception {
        if (this.createdContainer != null) {
            try {
                this.createdContainer.delete();
            } catch (CosmosException error) {
                if (error.getStatusCode() != 404) {
                    throw error;
                }
            }
        }
    }

    @BeforeMethod(groups = { "emulator" })
    public void beforeTest() throws Exception {
        this.createdContainer = null;
        this.createdAsyncContainer = null;
        this.partitionKeyToDocuments.clear();
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
        createdAsyncDatabase = client.asyncClient().getDatabase(createdDatabase.getId());
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 1000)
    public void asyncChangeFeed_fromBeginning_incremental_forFullRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(20, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);
        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(1, 3);
        };

        final int expectedInitialEventCount =
            20 * 7   //inserted
            + 0       // updates won't show up as extra events in incremental mode
            - 2 * 3;  // updated then deleted documents won't show up at all in incremental mode

        final int expectedEventCountAfterUpdates =
            5 * 2     // event count for initial updates
            - 1 * Math.min(2,3);   // reducing events for 2 of the 3 deleted documents
                                   // (because they have also had been updated)

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange());

        AtomicReference<String> continuation = new AtomicReference<>();
        List<ObjectNode> results = createdAsyncContainer
            .queryChangeFeed(options, ObjectNode.class)
            .handle((r) -> continuation.set(r.getContinuationToken()))
            .collectList()
            .block();

        assertThat(results)
            .isNotNull()
            .size()
            .isEqualTo(expectedInitialEventCount);

        // applying updates
        updateAction.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation.get());

        results = createdAsyncContainer
            .queryChangeFeed(options, ObjectNode.class)
            .handle((r) -> continuation.set(r.getContinuationToken()))
            .collectList()
            .block();

        assertThat(results)
            .isNotNull()
            .size()
            .isEqualTo(expectedEventCountAfterUpdates);
    }

    void insertDocuments(
        int partitionCount,
        int documentCount) {

        List<ObjectNode> docs = new ArrayList<>();

        for (int i = 0; i < partitionCount; i++) {
            String partitionKey = UUID.randomUUID().toString();
            for (int j = 0; j < documentCount; j++) {
                docs.add(getDocumentDefinition(partitionKey));
            }
        }

        ArrayList<Mono<CosmosItemResponse<ObjectNode>>> result = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(createdAsyncContainer
                .createItem(docs.get(i)));
        }

        List<ObjectNode> insertedDocs = Flux.merge(
            Flux.fromIterable(result),
            100)
                   .map(CosmosItemResponse::getItem).collectList().block();

        for (ObjectNode doc : insertedDocs) {
            partitionKeyToDocuments.put(
                doc.get(PARTITION_KEY_FIELD_NAME).textValue(),
                doc);
        }
    }

    void deleteDocuments(
        int partitionCount,
        int documentCount) {

        assertThat(partitionCount)
            .isLessThanOrEqualTo(this.partitionKeyToDocuments.keySet().size());

        Collection<ObjectNode> docs;
        for (int i = 0; i < partitionCount; i++) {
            String partitionKey = this.partitionKeyToDocuments
                .keySet()
                .stream()
                .skip(i)
                .findFirst()
                .get();

            docs = this.partitionKeyToDocuments.get(partitionKey);
            assertThat(docs)
                .isNotNull()
                .size()
                .isGreaterThanOrEqualTo(documentCount);

            for (int j = 0; j < documentCount; j++) {
                ObjectNode docToBeDeleted = docs.stream().findFirst().get();
                createdContainer.deleteItem(docToBeDeleted, null);
                docs.remove(docToBeDeleted);
            }
        }
    }

    void updateDocuments(
        int partitionCount,
        int documentCount) {

        assertThat(partitionCount)
            .isLessThanOrEqualTo(this.partitionKeyToDocuments.keySet().size());

        Collection<ObjectNode> docs;
        for (int i = 0; i < partitionCount; i++) {
            String partitionKey = this.partitionKeyToDocuments
                .keySet()
                .stream()
                .skip(i)
                .findFirst()
                .get();

            docs = this.partitionKeyToDocuments.get(partitionKey);
            assertThat(docs)
                .isNotNull()
                .size()
                .isGreaterThanOrEqualTo(documentCount);

            for (int j = 0; j < documentCount; j++) {
                ObjectNode docToBeUpdated = docs.stream().skip(j).findFirst().get();
                docToBeUpdated.put("someProperty", UUID.randomUUID().toString());
                createdContainer.replaceItem(
                    docToBeUpdated,
                    docToBeUpdated.get("id").textValue(),
                    new PartitionKey(docToBeUpdated.get("mypk").textValue()),
                    null);
            }
        }
    }

    private void createContainer(
        Function<CosmosContainerProperties, CosmosContainerProperties> onInitialization) {

        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        if (onInitialization != null) {
            containerProperties = onInitialization.apply(containerProperties);
        }

        CosmosContainerResponse containerResponse =
            createdDatabase.createContainer(containerProperties, 10100, null);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponse(containerProperties, containerResponse);

        this.createdContainer = createdDatabase.getContainer(collectionName);
        this.createdAsyncContainer = createdAsyncDatabase.getContainer(collectionName);
    }

    private static ObjectNode getDocumentDefinition(String partitionKey) {
        String uuid = UUID.randomUUID().toString();
        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"prop\": \"%s\""
                + "}"
            , uuid, partitionKey, uuid);

        try {
            return
                OBJECT_MAPPER.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid partition key value provided.");
        }
    }

    private void validateContainerResponse(CosmosContainerProperties containerProperties,
                                           CosmosContainerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());

    }
}
