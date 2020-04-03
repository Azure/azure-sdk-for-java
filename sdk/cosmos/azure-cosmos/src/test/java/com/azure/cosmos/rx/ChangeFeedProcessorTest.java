// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.ServiceItemLease;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.azure.cosmos.BridgeInternal.extractContainerSelfLink;
import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedProcessorTest extends TestSuiteBase {
    private final static Logger log = LoggerFactory.getLogger(ChangeFeedProcessorTest.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdFeedCollection;
    private CosmosAsyncContainer createdFeedCollectionForSplit;
    private CosmosAsyncContainer createdLeaseCollection;
    private List<CosmosItemProperties> createdDocuments;
    private static Map<String, JsonNode> receivedDocuments;
//    private final String databaseId = "testdb1";
//    private final String hostName = "TestHost1";
    private final String hostName = RandomStringUtils.randomAlphabetic(6);
    private final int FEED_COUNT = 10;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
    private final int FEED_COLLECTION_THROUGHPUT = 10100;
    private final int FEED_COLLECTION_THROUGHPUT_FOR_SPLIT = 400;
    private final int LEASE_COLLECTION_THROUGHPUT = 400;

    private CosmosAsyncClient client;

    private ChangeFeedProcessor changeFeedProcessor;

    @Factory(dataProvider = "clientBuilders")
    public ChangeFeedProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readFeedDocumentsStartFromBeginning() {
        setupReadFeedDocuments(createdFeedCollection, FEED_COUNT);

        changeFeedProcessor = ChangeFeedProcessor.changeFeedProcessorBuilder()
            .hostName(hostName)
            .handleChanges(changeFeedProcessorHandler())
            .feedContainer(createdFeedCollection)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                .setLeaseRenewInterval(Duration.ofSeconds(20))
                .setLeaseAcquireInterval(Duration.ofSeconds(10))
                .setLeaseExpirationInterval(Duration.ofSeconds(30))
                .setFeedPollDelay(Duration.ofSeconds(2))
                .setLeasePrefix("TEST")
                .setMaxItemCount(10)
                .setStartFromBeginning(true)
                .setMaxScaleCount(0) // unlimited
                .setExistingLeasesDiscarded(true)
            )
            .build();

        try {
            changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .subscribe();
        } catch (Exception ex) {
            log.error("Change feed processor did not start in the expected time", ex);
        }

        // Wait for the feed processor to receive and process the documents.
        try {
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

        changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        for (CosmosItemProperties item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
        }

        // Wait for the feed processor to shutdown.
        try {
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        receivedDocuments.clear();
     }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readFeedDocumentsStartFromCustomDate() {
        ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.changeFeedProcessorBuilder()
            .hostName(hostName)
            .handleChanges((List<JsonNode> docs) -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {}", Thread.currentThread().getId());
                for (JsonNode item : docs) {
                    processItem(item);
                }
                ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
            })
            .feedContainer(createdFeedCollection)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                .setLeaseRenewInterval(Duration.ofSeconds(20))
                .setLeaseAcquireInterval(Duration.ofSeconds(10))
                .setLeaseExpirationInterval(Duration.ofSeconds(30))
                .setFeedPollDelay(Duration.ofSeconds(1))
                .setLeasePrefix("TEST")
                .setMaxItemCount(10)
                .setStartTime(OffsetDateTime.now().minusDays(1))
                .setMinScaleCount(1)
                .setMaxScaleCount(3)
                .setExistingLeasesDiscarded(true)
            )
            .build();

        try {
            changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                .timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT))
                .subscribe();
        } catch (Exception ex) {
            log.error("Change feed processor did not start in the expected time", ex);
        }

        setupReadFeedDocuments(createdFeedCollection, FEED_COUNT);

        // Wait for the feed processor to receive and process the documents.
        waitToReceiveDocuments(40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

        assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

        changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        for (CosmosItemProperties item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
        }

        // Wait for the feed processor to shutdown.
        try {
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        receivedDocuments.clear();
    }

    @Test(groups = { "emulator" }, timeOut = 40 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void staledLeaseAcquiring() {
        final String ownerFirst = "Owner_First";
        final String ownerSecond = "Owner_Second";
        final String leasePrefix = "TEST";

        ChangeFeedProcessor changeFeedProcessorFirst = ChangeFeedProcessor.changeFeedProcessorBuilder()
            .hostName(ownerFirst)
            .handleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                ChangeFeedProcessorTest.log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
            })
            .feedContainer(createdFeedCollection)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                .setLeasePrefix(leasePrefix)
            )
            .build();

        ChangeFeedProcessor changeFeedProcessorSecond = ChangeFeedProcessor.changeFeedProcessorBuilder()
            .hostName(ownerSecond)
            .handleChanges((List<JsonNode> docs) -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                for (JsonNode item : docs) {
                    processItem(item);
                }
                ChangeFeedProcessorTest.log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
            })
            .feedContainer(createdFeedCollection)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(10))
                    .setLeaseAcquireInterval(Duration.ofSeconds(5))
                    .setLeaseExpirationInterval(Duration.ofSeconds(20))
                    .setFeedPollDelay(Duration.ofSeconds(2))
                    .setLeasePrefix(leasePrefix)
                    .setMaxItemCount(10)
                    .setStartFromBeginning(true)
                    .setMaxScaleCount(0) // unlimited
            )
            .build();

        receivedDocuments = new ConcurrentHashMap<>();

        try {
            changeFeedProcessorFirst.start().subscribeOn(Schedulers.elastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .then(Mono.just(changeFeedProcessorFirst)
                    .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .flatMap( value -> changeFeedProcessorFirst.stop()
                        .subscribeOn(Schedulers.elastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    ))
                .then(Mono.just(changeFeedProcessorFirst)
                    .delayElement(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT))
                )
                .doOnSuccess(aVoid -> {
                    try {
                        Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT / 2);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                    ChangeFeedProcessorTest.log.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");

                    SqlParameter param = new SqlParameter();
                    param.setName("@PartitionLeasePrefix");
                    param.setValue(leasePrefix);
                    SqlQuerySpec querySpec = new SqlQuerySpec(
                        "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)", Collections.singletonList(param));

                    FeedOptions feedOptions = new FeedOptions();

                    createdLeaseCollection.queryItems(querySpec, feedOptions, CosmosItemProperties.class).byPage()
                        .delayElements(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT / 2))
                        .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.getResults()))
                        .flatMap(doc -> {
                            BridgeInternal.setProperty(doc, "Owner", "TEMP_OWNER");
                            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                            return createdLeaseCollection.replaceItem(doc, doc.getId(), new PartitionKey(doc.getId()), options)
                                .map(itemResponse -> BridgeInternal.getProperties(itemResponse));
                        })
                        .map(ServiceItemLease::fromDocument)
                        .map(leaseDocument -> {
                            ChangeFeedProcessorTest.log.info("QueryItems after Change feed processor processing; found host {}", leaseDocument.getOwner());
                            return leaseDocument;
                        })
                        .last()
                        .delayElement(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT / 2))
                        .flatMap(leaseDocument -> {
                            ChangeFeedProcessorTest.log.info("Start creating documents");
                            List<CosmosItemProperties> docDefList = new ArrayList<>();

                            for(int i = 0; i < FEED_COUNT; i++) {
                                docDefList.add(getDocumentDefinition());
                            }

                            return bulkInsert(createdFeedCollection, docDefList, FEED_COUNT)
                                .last()
                                .delayElement(Duration.ofMillis(1000))
                                .flatMap(cosmosItemResponse -> {
                                    ChangeFeedProcessorTest.log.info("Start second Change feed processor");
                                    return changeFeedProcessorSecond.start().subscribeOn(Schedulers.elastic())
                                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT));
                                });
                        })
                        .subscribe();
                })
                .subscribe();
        } catch (Exception ex) {
            log.error("First change feed processor did not start in the expected time", ex);
        }

        // Wait for the feed processor to receive and process the documents.
        waitToReceiveDocuments(40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

        changeFeedProcessorSecond.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        // Wait for the feed processor to shutdown.
        try {
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        receivedDocuments.clear();
    }

    @Test(groups = { "emulator" }, timeOut = CHANGE_FEED_PROCESSOR_TIMEOUT * 50)
    public void readFeedDocumentsAfterSplit() {
        createdFeedCollectionForSplit = createFeedCollection(FEED_COLLECTION_THROUGHPUT_FOR_SPLIT);

        // generate a first batch of documents
        setupReadFeedDocuments(createdFeedCollectionForSplit, FEED_COUNT);

        changeFeedProcessor = ChangeFeedProcessor.changeFeedProcessorBuilder()
            .hostName(hostName)
            .handleChanges(changeFeedProcessorHandler())
            .feedContainer(createdFeedCollectionForSplit)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                .setLeasePrefix("TEST")
                .setStartFromBeginning(true)
                .setMaxItemCount(10)
            )
            .build();

        changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
            .onErrorResume(throwable -> {
                log.error("Change feed processor did not start in the expected time", throwable);
                return Mono.error(throwable);
            })
            .doOnSuccess(aVoid -> {
                // Wait for the feed processor to receive and process the first batch of documents.
                waitToReceiveDocuments(2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);
            })
            .then(
                // increase throughput to force a single partition collection to go through a split
                createdFeedCollectionForSplit.readProvisionedThroughput().subscribeOn(Schedulers.elastic())
                    .flatMap(currentThroughput ->
                        createdFeedCollectionForSplit.replaceProvisionedThroughput(FEED_COLLECTION_THROUGHPUT).subscribeOn(Schedulers.elastic())
                    )
                .then()
            )
            .subscribe();

//        // Wait for the feed processor to receive and process the first batch of documents and apply throughput change.
        try {
            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        String partitionKeyRangesPath = extractContainerSelfLink(createdFeedCollectionForSplit);
        FeedOptions feedOptions = new FeedOptions();
        feedOptions.setRequestContinuation(null);

        AsyncDocumentClient contextClient = getContextClient(createdDatabase);
        Flux.just(1).subscribeOn(Schedulers.elastic())
            .flatMap( value -> {
                log.warn("Reading current hroughput change.");
                return contextClient.readPartitionKeyRanges(partitionKeyRangesPath, feedOptions);
            })
            .map(partitionKeyRangeFeedResponse -> {
                int count = partitionKeyRangeFeedResponse.getResults().size();
                if ( count < 2) {
                    log.warn("Throughput change is pending.");
                    throw new RuntimeException("Throughput change is not done.");
                }
                return count;
            })
            .retry(40, throwable -> {
                try {
                    log.warn("Retrying...");
                    Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
                return true;
            })
            .last().block();

        assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

        // generate the second batch of documents
        createReadFeedDocuments(createdFeedCollectionForSplit, FEED_COUNT);

        // Wait for the feed processor to receive and process the second batch of documents.
        waitToReceiveDocuments(2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT * 2);

        changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        for (CosmosItemProperties item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
        }

        // Wait for the feed processor to shutdown.
        try {
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        receivedDocuments.clear();
    }

    private Consumer<List<JsonNode>> changeFeedProcessorHandler() {
        return docs -> {
            ChangeFeedProcessorTest.log.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                processItem(item);
            }
            ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private void waitToReceiveDocuments(long timeoutInMillisecond, long count) {
        long remainingWork = timeoutInMillisecond;
        while (remainingWork > 0 && receivedDocuments.size() < count) {
            remainingWork -= 100;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }

        assertThat(remainingWork >= 0).as("Failed to receive all the feed documents").isTrue();
    }

    @BeforeMethod(groups = { "emulator" }, timeOut = 2 * SETUP_TIMEOUT, alwaysRun = true)
     public void beforeMethod() {
         createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
         createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);
     }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_ChangeFeedProcessorTest() {
        client = getClientBuilder().buildAsyncClient();

//        try {
//            client.getDatabase(databaseId).read()
//                .map(cosmosDatabaseResponse -> cosmosDatabaseResponse.getDatabase())
//                .flatMap(database -> database.delete())
//                .onErrorResume(throwable -> {
//                    if (throwable instanceof com.azure.cosmos.CosmosClientException) {
//                        com.azure.cosmos.CosmosClientException clientException = (com.azure.cosmos.CosmosClientException) throwable;
//                        if (clientException.getStatusCode() == 404) {
//                            return Mono.empty();
//                        }
//                    }
//                    return Mono.error(throwable);
//                }).block();
//            Thread.sleep(500);
//        } catch (Exception e){
//            log.warn("Database delete", e);
//        }
//        createdDatabase = createDatabase(client, databaseId);
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterMethod(groups = { "emulator" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterMethod() {
        safeDeleteCollection(createdFeedCollection);
        safeDeleteCollection(createdLeaseCollection);
        safeDeleteCollection(createdFeedCollectionForSplit);

        // Allow some time for the collections and the database to be deleted before exiting.
        try {
            Thread.sleep(500);
        } catch (Exception e){ }
    }

    @AfterClass(groups = { "emulator" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
//        try {
//            client.readAllDatabases()
//                .flatMap(cosmosDatabaseProperties -> {
//                    CosmosAsyncDatabase cosmosDatabase = client.getDatabase(cosmosDatabaseProperties.getId());
//                    return cosmosDatabase.delete();
//                }).blockLast();
//            Thread.sleep(500);
//        } catch (Exception e){ }

        safeClose(client);
    }

    private void setupReadFeedDocuments(CosmosAsyncContainer feedCollection, long count) {
        receivedDocuments = new ConcurrentHashMap<>();
        List<CosmosItemProperties> docDefList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments = bulkInsertBlocking(feedCollection, docDefList);
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private void createReadFeedDocuments(CosmosAsyncContainer feedCollection, long count) {
        List<CosmosItemProperties> docDefList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments = bulkInsertBlocking(feedCollection, docDefList);
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private CosmosItemProperties getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemProperties doc = new CosmosItemProperties(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , uuid, uuid));
        return doc;
    }

    private CosmosAsyncContainer createFeedCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions optionsFeedCollection = new CosmosContainerRequestOptions();
        return createCollection(createdDatabase, getCollectionDefinition(), optionsFeedCollection, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties("leases_" + createdDatabase.getId(), "/id");
        return createCollection(createdDatabase, collectionDefinition, options, provisionedThroughput);
    }

    private static synchronized void processItem(JsonNode item) {
        try {
            ChangeFeedProcessorTest.log
                .info("RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            log.error("Failure in processing json [{}]", e.getMessage(), e);
        }
        receivedDocuments.put(item.get("id").asText(), item);
    }
}
