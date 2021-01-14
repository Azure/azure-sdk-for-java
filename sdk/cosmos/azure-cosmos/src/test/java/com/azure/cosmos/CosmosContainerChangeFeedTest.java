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
import java.util.List;
import java.util.UUID;
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
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
        createdAsyncDatabase = client.asyncClient().getDatabase(createdDatabase.getId());
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 1000)
    public void asyncChangeFeed_fromBeginning_forFullRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange());

        List<ObjectNode> results = createdAsyncContainer
            .queryChangeFeed(options, ObjectNode.class)
            .collectList()
            .block();

        assertThat(results)
            .isNotNull()
            .size()
            .isEqualTo(getExpectedDocumentCount(null));
    }

    void populateDocuments() {
        partitionKeyToDocuments.clear();

        List<ObjectNode> docs = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            String partitionKey = UUID.randomUUID().toString();
            for (int j = 0; j < 7; j++) {
                docs.add(getDocumentDefinition(partitionKey));
            }
        }

        List<ObjectNode> insertedDocs = ingestDocuments(docs);
        for (ObjectNode doc : insertedDocs) {
            partitionKeyToDocuments.put(
                doc.get(PARTITION_KEY_FIELD_NAME).textValue(),
                doc);
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

        this.populateDocuments();
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

    private int getExpectedDocumentCount(String partitionKey) {
        if (partitionKey != null) {
            return partitionKeyToDocuments.get(partitionKey).size();
        }

        int count = 0;
        for (String currentPK : partitionKeyToDocuments.keySet()) {
            count += partitionKeyToDocuments.get(currentPK).size();
        }

        return count;
    }

    private <T> List<T> ingestDocuments(List<T> docs) {
        ArrayList<Mono<CosmosItemResponse<T>>> result = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(createdAsyncContainer
                .createItem(docs.get(i)));
        }

        return Flux.merge(
            Flux.fromIterable(result),
            100)
                   .map(CosmosItemResponse::getItem).collectList().block();
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
