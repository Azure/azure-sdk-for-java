// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.guava25.collect.ArrayListMultimap;
import com.azure.cosmos.implementation.guava25.collect.Multimap;
import com.azure.cosmos.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class FullFidelityChangeFeedSample {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PARTITION_KEY_FIELD_NAME = "mypk";
    public static CosmosClient client;
    public static CosmosAsyncClient clientAsync;
    private CosmosAsyncContainer createdAsyncContainer;
    private CosmosAsyncDatabase createdAsyncDatabase;
    private CosmosContainer createdContainer;
    private static CosmosDatabase createdDatabase;
    private final Multimap<String, ObjectNode> partitionKeyToDocuments = ArrayListMultimap.create();

    public static final String DATABASE_NAME = "db";
    public static final String COLLECTION_NAME = "ffcf";
    protected static Logger logger = LoggerFactory.getLogger(FullFidelityChangeFeedSample.class);

    public static void main(String[] args) {
        logger.info("BEGIN Sample");
        // FullFidelityChangeFeedSample demo = new FullFidelityChangeFeedSample();
        client = FullFidelityChangeFeedSample.getCosmosClient();
        clientAsync = FullFidelityChangeFeedSample.getCosmosAsyncClient();

        try {
            logger.info("-->RUN asyncChangeFeed_fromNow_fullFidelity_forFullRange");

            FullFidelityChangeFeedSample demo = new FullFidelityChangeFeedSample();
            demo.asyncChangeFeed_fromNow_fullFidelity_forFullRange();

            logger.info("-->DELETE sample's database: " + DATABASE_NAME);

            deleteDatabase(createdDatabase);

            Thread.sleep(500);

        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("END Sample");
    }

    public void asyncChangeFeed_fromNow_fullFidelity_forFullRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(60))));
        insertDocuments(8, 15);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);

        Runnable updateAction1 = () -> {
            insertDocuments(5, 9);
            updateDocuments(3, 5);
            deleteDocuments(2, 3);
        };

        Runnable updateAction2 = () -> {
            updateDocuments(5, 2);
            deleteDocuments(2, 3);
            insertDocuments(10, 5);
        };

        final int expectedInitialEventCount = 0;

        final int expectedEventCountAfterFirstSetOfUpdates = 5 * 9 // events for inserts
            + 3 * 5 // event count for updates
            + 2 * 3; // plus deletes (which are all included in FF CF)

        final int expectedEventCountAfterSecondSetOfUpdates = 10 * 5 // events for inserts
            + 5 * 2 // event count for updates
            + 2 * 3; // plus deletes (which are all included in FF CF)

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());
        options.fullFidelity();

        String continuation = drainAndValidateChangeFeedResults(options, null, expectedInitialEventCount);

        // applying first set of updates
        updateAction1.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation);

        continuation = drainAndValidateChangeFeedResults(
            options,
            null,
            expectedEventCountAfterFirstSetOfUpdates);

        // applying first set of updates
        updateAction2.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation);

        drainAndValidateChangeFeedResults(
            options,
            null,
            expectedEventCountAfterSecondSetOfUpdates);
    }

    private String drainAndValidateChangeFeedResults(
        CosmosChangeFeedRequestOptions changeFeedRequestOptions,
        Function<CosmosChangeFeedRequestOptions, CosmosChangeFeedRequestOptions> onNewRequestOptions,
        int expectedEventCount) {

        return drainAndValidateChangeFeedResults(
            Arrays.asList(changeFeedRequestOptions),
            onNewRequestOptions,
            expectedEventCount).get(0);
    }

    private Map<Integer, String> drainAndValidateChangeFeedResults(
        List<CosmosChangeFeedRequestOptions> changeFeedRequestOptions,
        Function<CosmosChangeFeedRequestOptions, CosmosChangeFeedRequestOptions> onNewRequestOptions,
        int expectedTotalEventCount) {

        Map<Integer, String> continuations = new HashMap<>();

        int totalRetrievedEventCount = 0;

        boolean isFinished = false;
        int emptyResultCount = 0;

        while (!isFinished) {
            for (Integer i = 0; i < changeFeedRequestOptions.size(); i++) {
                List<ItemWithMetaData> results;

                CosmosChangeFeedRequestOptions effectiveOptions;
                if (continuations.containsKey(i)) {
                    logger.info(String.format(
                        "Continuation BEFORE: %s",
                        new String(
                            Base64.getUrlDecoder().decode(continuations.get(i)),
                            StandardCharsets.UTF_8)));
                    effectiveOptions = CosmosChangeFeedRequestOptions
                        .createForProcessingFromContinuation(continuations.get(i));
                    if (onNewRequestOptions != null) {
                        effectiveOptions = onNewRequestOptions.apply(effectiveOptions);
                    }
                } else {
                    effectiveOptions = changeFeedRequestOptions.get(i);
                }
                final Integer index = i;
                results = createdAsyncContainer
                    .queryChangeFeed(effectiveOptions, ItemWithMetaData.class)
                    // NOTE - in real app you would need delaying persisting the
                    // continuation until you retrieve the next one
                    .handle((r) -> continuations.put(index, r.getContinuationToken()))
                    .collectList()
                    .block();

                logger.info(
                    String.format(
                        "Continuation AFTER: %s, records retrieved: %d",
                        new String(
                            Base64.getUrlDecoder().decode(continuations.get(i)),
                            StandardCharsets.UTF_8),
                        results.size()));

                for (ItemWithMetaData doc : results) {
                    logger.info("doc id:" + doc.getId());
                }

                totalRetrievedEventCount += results.size();
                if (totalRetrievedEventCount >= expectedTotalEventCount) {
                    isFinished = true;
                    break;
                }

                if (results.size() == 0) {
                    emptyResultCount += 1;

                    // if (emptyResultCount > 10){
                    // isFinished = true;
                    // }
                    logger.info(
                        String.format("No more docs....",
                            totalRetrievedEventCount,
                            expectedTotalEventCount,
                            emptyResultCount));

                    try {
                        Thread.sleep(500 / changeFeedRequestOptions.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    emptyResultCount = 0;
                }

            }
        }

        return continuations;
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
            10)
                                            .map(CosmosItemResponse::getItem).collectList().block();

        for (ObjectNode doc : insertedDocs) {
            partitionKeyToDocuments.put(
                doc.get(PARTITION_KEY_FIELD_NAME).textValue(),
                doc);
        }
        logger.info("FINISHED INSERT");
    }

    void deleteDocuments(
        int partitionCount,
        int documentCount) {

        Collection<ObjectNode> docs;
        for (int i = 0; i < partitionCount; i++) {
            String partitionKey = this.partitionKeyToDocuments
                .keySet()
                .stream()
                .skip(i)
                .findFirst()
                .get();

            docs = this.partitionKeyToDocuments.get(partitionKey);

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

        Collection<ObjectNode> docs;
        for (int i = 0; i < partitionCount; i++) {
            String partitionKey = this.partitionKeyToDocuments
                .keySet()
                .stream()
                .skip(i)
                .findFirst()
                .get();

            docs = this.partitionKeyToDocuments.get(partitionKey);

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

    private static ObjectNode getDocumentDefinition(String partitionKey) {
        String uuid = UUID.randomUUID().toString();
        String json = String.format("{ "
            + "\"id\": \"%s\", "
            + "\"mypk\": \"%s\", "
            + "\"prop\": \"%s\""
            + "}", uuid, partitionKey, uuid);

        try {
            return OBJECT_MAPPER.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid partition key value provided.");
        }
    }

    public static CosmosClient getCosmosClient() {

        return new CosmosClientBuilder()
            .endpoint(SampleConfigurations.HOST)
            .key(SampleConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildClient();
    }

    public static CosmosAsyncClient getCosmosAsyncClient() {

        return new CosmosClientBuilder()
            .endpoint(SampleConfigurations.HOST)
            .key(SampleConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();
    }

    public static CosmosAsyncDatabase createNewDatabase(CosmosAsyncClient client, String databaseName) {
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName).block();
        return client.getDatabase(databaseResponse.getProperties().getId());
    }

    public static void deleteDatabase(CosmosDatabase createdDatabase2) {
        createdDatabase2.delete();
    }

    public CosmosContainerProperties createNewCollection(CosmosClient client2, String databaseName,
                                                         String collectionName) {
        CosmosDatabaseResponse databaseResponse = client2.createDatabaseIfNotExists(databaseName);
        CosmosDatabase database = client2.getDatabase(databaseResponse.getProperties().getId());

        CosmosContainerProperties containerSettings = new CosmosContainerProperties(collectionName, "/mypk");

        containerSettings.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(60)));

        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(10000);

        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerSettings,
            throughputProperties);
        this.createdDatabase = client2.getDatabase(database.getId());
        this.createdAsyncDatabase = clientAsync.getDatabase(createdDatabase.getId());
        this.createdContainer = client2.getDatabase(DATABASE_NAME).getContainer(COLLECTION_NAME);
        this.createdAsyncContainer = clientAsync.getDatabase(DATABASE_NAME).getContainer(COLLECTION_NAME);
        return containerResponse.getProperties();
    }

    private void createContainer(
        Function<CosmosContainerProperties, CosmosContainerProperties> onInitialization) {

        CosmosContainerProperties containerProperties = createNewCollection(client, DATABASE_NAME, COLLECTION_NAME);

        if (onInitialization != null) {
            containerProperties = onInitialization.apply(containerProperties);
        }

        this.createdContainer = createdDatabase.getContainer(COLLECTION_NAME);
        this.createdAsyncContainer = createdAsyncDatabase.getContainer(COLLECTION_NAME);
    }

    public static final class Item {

        public Item() {
        }

        public String getId() {
            return id;
        }

        public String id;
        public String myPk;
    }

    public static final class Metadata {

        public String getOperationType() {
            return operationType;
        }
        public Boolean getTimeToLiveExpired() {
            return timeToLiveExpired;
        }
        public Item getPreviousImage() {
            return previousImage;
        }
        public String operationType;
        public Boolean timeToLiveExpired;
        public Item previousImage;

    }

    public static final class ItemWithMetaData {

        public ItemWithMetaData() {
        }

        public String getId() {
            return id;
        }
        public String getMyPk() {
            return myPk;
        }
        public Metadata getMetadata() {
            return _metadata;
        }

        public String id;
        public String myPk;
        public Metadata _metadata;

    }

    public static final class SampleConfigurations {
        // REPLACE MASTER_KEY and HOST with values from your Azure Cosmos DB account.
        // The default values are credentials of the local emulator, which are not used in any production environment.
        // <!--[SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine")]-->
        public static String MASTER_KEY =
            "<YourKey>>";

        public static String HOST = "<YourEndpoint>>";
    }
}
