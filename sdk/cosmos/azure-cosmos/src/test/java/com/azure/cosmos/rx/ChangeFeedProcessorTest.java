// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.ServiceItemLease;
import com.azure.cosmos.models.ThroughputProperties;
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
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    @Test(groups = { "emulator" }, timeOut = 2 * TIMEOUT)
    public void readFeedDocumentsStartFromBeginning() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
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
                )
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .subscribe();
            } catch (Exception ex) {
                log.error("Change feed processor did not start in the expected time", ex);
                throw ex;
            }

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
     }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsStartFromCustomDate() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    ChangeFeedProcessorTest.log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
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
                    .setStartTime(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).toInstant())
                    .setMinScaleCount(1)
                    .setMaxScaleCount(3)
                )
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .subscribe();
            } catch (Exception ex) {
                log.error("Change feed processor did not start in the expected time", ex);
                throw ex;
            }

            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            // Wait for the feed processor to receive and process the documents.
            waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getEstimatedLag() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    ChangeFeedProcessorTest.log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .then(Mono.just(changeFeedProcessor)
                        .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .flatMap(value -> changeFeedProcessor.stop()
                            .subscribeOn(Schedulers.elastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        ))
                    .subscribe();
            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            Map<String, Integer> estimatedLagResult = changeFeedProcessor.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            int totalLag = 0;
            for (int lag : estimatedLagResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor estimated total lag at start");

            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            estimatedLagResult = changeFeedProcessor.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            totalLag = 0;
            for (int lag : estimatedLagResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor estimated total lag");
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getCurrentState() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    ChangeFeedProcessorTest.log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .then(Mono.just(changeFeedProcessor)
                        .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .flatMap(value -> changeFeedProcessor.stop()
                            .subscribeOn(Schedulers.elastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        ))
                    .subscribe();
            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            List<ChangeFeedProcessorState> cfpCurrentState = changeFeedProcessor.getCurrentState()
                .map(state -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            int totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor estimated total lag at start");

            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            cfpCurrentState = changeFeedProcessor.getCurrentState()
                .map(state -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor estimated total lag");
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void staledLeaseAcquiring() throws InterruptedException {
        final String ownerFirst = "Owner_First";
        final String ownerSecond = "Owner_Second";
        final String leasePrefix = "TEST";
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

            ChangeFeedProcessor changeFeedProcessorFirst = new ChangeFeedProcessorBuilder()
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
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSecond = new ChangeFeedProcessorBuilder()
                .hostName(ownerSecond)
                .handleChanges((List<JsonNode> docs) -> {
                    ChangeFeedProcessorTest.log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
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
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessorFirst.start().subscribeOn(Schedulers.elastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .then(Mono.just(changeFeedProcessorFirst)
                        .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .flatMap(value -> changeFeedProcessorFirst.stop()
                            .subscribeOn(Schedulers.elastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        ))
                    .doOnSuccess(aVoid -> {
                        try {
                            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Interrupted exception", e);
                        }
                        ChangeFeedProcessorTest.log.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");

                        SqlParameter param = new SqlParameter();
                        param.setName("@PartitionLeasePrefix");
                        param.setValue(leasePrefix);
                        SqlQuerySpec querySpec = new SqlQuerySpec(
                            "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)", Collections.singletonList(param));

                        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

                        createdLeaseCollection.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class).byPage()
                                              .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.getResults()))
                                              .flatMap(doc -> {
                                ServiceItemLease leaseDocument = ServiceItemLease.fromDocument(doc);
                                leaseDocument.setOwner("TEMP_OWNER");
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
                                              .flatMap(leaseDocument -> {
                                ChangeFeedProcessorTest.log.info("Start creating documents");
                                List<InternalObjectNode> docDefList = new ArrayList<>();

                                for (int i = 0; i < FEED_COUNT; i++) {
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
                throw ex;
            }

            long remainingWork = 10 * CHANGE_FEED_PROCESSOR_TIMEOUT;
            while (remainingWork > 0 && changeFeedProcessorFirst.isStarted() && !changeFeedProcessorSecond.isStarted()) {
                remainingWork -= 100;
                Thread.sleep(100);
            }

            // Wait for the feed processor to receive and process the documents.
            waitToReceiveDocuments(receivedDocuments, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            assertThat(changeFeedProcessorSecond.isStarted()).as("Change Feed Processor instance is running").isTrue();

            changeFeedProcessorSecond.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            // Wait for the feed processor to shutdown.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "simple" }, timeOut = 160 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsAfterSplit() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollectionForSplit = createFeedCollection(FEED_COLLECTION_THROUGHPUT_FOR_SPLIT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(2 * LEASE_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseMonitorCollection = createLeaseMonitorCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            LeaseStateMonitor leaseStateMonitor = new LeaseStateMonitor();

            // create a monitoring CFP for ensuring the leases are updating as expected
            ChangeFeedProcessor leaseMonitoringChangeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(leasesChangeFeedProcessorHandler(leaseStateMonitor))
                .feedContainer(createdLeaseCollection)
                .leaseContainer(createdLeaseMonitorCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("MONITOR")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(100)
                    .setLeaseExpirationInterval(Duration.ofMillis(10 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setFeedPollDelay(Duration.ofMillis(200))
                )
                .buildChangeFeedProcessor();

            // generate a first batch of documents
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollectionForSplit)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(10)
                    .setLeaseRenewInterval(Duration.ofSeconds(2))
                )
                .buildChangeFeedProcessor();

            leaseMonitoringChangeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .onErrorResume(throwable -> {
                    log.error("Change feed processor for lease monitoring did not start in the expected time", throwable);
                    return Mono.error(throwable);
                })
                .then(
                    changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .onErrorResume(throwable -> {
                            log.error("Change feed processor did not start in the expected time", throwable);
                            return Mono.error(throwable);
                        })
                        .doOnSuccess(aVoid -> {
                            // Wait for the feed processor to receive and process the first batch of documents.
                            try {
                                waitToReceiveDocuments(receivedDocuments, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);
                            } catch (InterruptedException e) {
                                throw new RuntimeException("Interrupted exception", e);
                            }
                        })
                        .then(
                            // increase throughput to force a single partition collection to go through a split
                            createdFeedCollectionForSplit
                                .readThroughput().subscribeOn(Schedulers.elastic())
                                .flatMap(currentThroughput ->
                                    createdFeedCollectionForSplit
                                        .replaceThroughput(ThroughputProperties.createManualThroughput(FEED_COLLECTION_THROUGHPUT))
                                        .subscribeOn(Schedulers.elastic())
                                )
                                .then()
                        )
                )
                .subscribe();

            // Wait for the feed processor to receive and process the first batch of documents and apply throughput change.
            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Retrieve the latest continuation token value.
            long continuationToken = Long.MAX_VALUE;
            for (JsonNode item : leaseStateMonitor.receivedLeases.values()) {
                JsonNode tempToken = item.get("ContinuationToken");
                long continuationTokenValue = 0;
                if (tempToken != null && !tempToken.asText().replaceAll("[^0-9]", "").isEmpty()) {
                    continuationTokenValue = Long.parseLong(tempToken.asText().replaceAll("[^0-9]", ""));
                }
                if (tempToken == null || continuationTokenValue == 0) {
                    log.error("Found unexpected lease with continuation token value of null or 0");
                    try {
                        log.info("ERROR LEASE FOUND {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                    } catch (JsonProcessingException e) {
                        log.error("Failure in processing json [{}]", e.getMessage(), e);
                    }
                    leaseStateMonitor.isContinuationTokenAdvancing = false;
                }
                else {
                    // keep the lowest continuation token value
                    if (continuationToken > continuationTokenValue) {
                        continuationToken = continuationTokenValue;
                    }
                }
            }
            if (continuationToken == Long.MAX_VALUE) {
                // something went wrong; we could not find any valid leases.
                log.error("Could not find any valid lease documents");
                leaseStateMonitor.isContinuationTokenAdvancing = false;
            }
            else {
                leaseStateMonitor.parentContinuationToken = continuationToken;
            }
            leaseStateMonitor.isAfterLeaseInitialization = true;

            // Loop through reading the current partition count until we get a split
            //   This can take up to two minute or more.
            String partitionKeyRangesPath = extractContainerSelfLink(createdFeedCollectionForSplit);

            AsyncDocumentClient contextClient = getContextClient(createdDatabase);
            Flux.just(1).subscribeOn(Schedulers.elastic())
                .flatMap(value -> {
                    log.warn("Reading current throughput change.");
                    return contextClient.readPartitionKeyRanges(partitionKeyRangesPath, null);
                })
                .map(partitionKeyRangeFeedResponse -> {
                    int count = partitionKeyRangeFeedResponse.getResults().size();

                    if (count < 2) {
                        log.warn("Throughput change is pending.");
                        throw new RuntimeException("Throughput change is not done.");
                    }
                    return count;
                })
                // this will timeout approximately after 30 minutes
                .retryWhen(Retry.max(40).filter(throwable -> {
                    try {
                        log.warn("Retrying...");
                        // Splits are taking longer, so increasing sleep time between retries
                        Thread.sleep(10 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted exception", e);
                    }
                    return true;
                }))
                .last()
                .doOnSuccess(partitionCount -> {
                    leaseStateMonitor.isAfterSplits = true;
                })
                .block();

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            // generate the second batch of documents
            createReadFeedDocuments(createdDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            // Wait for the feed processor to receive and process the second batch of documents.
            waitToReceiveDocuments(receivedDocuments, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT * 2);

            changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();
            leaseMonitoringChangeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            int leaseCount = changeFeedProcessor.getCurrentState() .map(List::size).block();
            assertThat(leaseCount > 1).as("Found %d leases", leaseCount).isTrue();

            assertThat(receivedDocuments.size()).isEqualTo(createdDocuments.size());
            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // check the continuation tokens have advanced after splits
            assertThat(leaseStateMonitor.isContinuationTokenAdvancing && leaseStateMonitor.parentContinuationToken > 0)
                .as("Continuation tokens for the leases after split should advance from parent value; parent: %d", leaseStateMonitor.parentContinuationToken).isTrue();

            // Wait for the feed processor to shutdown.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            safeDeleteCollection(createdFeedCollectionForSplit);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    private Consumer<List<JsonNode>> changeFeedProcessorHandler(Map<String, JsonNode> receivedDocuments) {
        return docs -> {
            ChangeFeedProcessorTest.log.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                processItem(item, receivedDocuments);
            }
            ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private void waitToReceiveDocuments(Map<String, JsonNode> receivedDocuments, long timeoutInMillisecond, long count) throws InterruptedException {
        long remainingWork = timeoutInMillisecond;
        while (remainingWork > 0 && receivedDocuments.size() < count) {
            remainingWork -= 100;
            Thread.sleep(100);
        }

        assertThat(remainingWork >= 0).as("Failed to receive all the feed documents").isTrue();
    }

    private Consumer<List<JsonNode>> leasesChangeFeedProcessorHandler(LeaseStateMonitor leaseStateMonitor) {
        return docs -> {
            log.info("LEASES processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                try {
                    log
                        .debug("LEASE RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                } catch (JsonProcessingException e) {
                    log.error("Failure in processing json [{}]", e.getMessage(), e);
                }

                JsonNode leaseToken = item.get("LeaseToken");

                if (leaseToken != null) {
                    JsonNode continuationTokenNode = item.get("ContinuationToken");
                    if (continuationTokenNode == null) {
                        // Something catastrophic went wrong and the lease is malformed.
                        log.error("Found invalid lease document");
                        leaseStateMonitor.isContinuationTokenAdvancing = false;
                    }
                    else {
                        log.info("LEASE {} with continuation {}", leaseToken.asText(), continuationTokenNode.asText());
                        if (leaseStateMonitor.isAfterLeaseInitialization) {
                            String value = continuationTokenNode.asText().replaceAll("[^0-9]", "");
                            if (value.isEmpty()) {
                                log.error("Found unexpected continuation token that does not conform to the expected format");
                                leaseStateMonitor.isContinuationTokenAdvancing = false;
                            }
                            long continuationToken = Long.parseLong(value);
                            if (leaseStateMonitor.parentContinuationToken > continuationToken) {
                                log.error("Found unexpected continuation token that did not advance after the split; parent: {}, current: {}");
                                leaseStateMonitor.isContinuationTokenAdvancing = false;
                            }
                        }
                    }
                    leaseStateMonitor.receivedLeases.put(item.get("id").asText(), item);
                }
            }
            log.info("LEASES processing from thread {}", Thread.currentThread().getId());
        };
    }

    @BeforeMethod(groups = { "emulator", "simple" }, timeOut = 2 * SETUP_TIMEOUT, alwaysRun = true)
     public void beforeMethod() {
     }

    @BeforeClass(groups = { "emulator", "simple" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_ChangeFeedProcessorTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);

        // Following is code that when enabled locally it allows for a predicted database/collection name that can be
        // checked in the Azure Portal
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
    }

    @AfterMethod(groups = { "emulator", "simple" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterMethod() {
    }

    @AfterClass(groups = { "emulator", "simple" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
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

    private void setupReadFeedDocuments(List<InternalObjectNode> createdDocuments, Map<String, JsonNode> receivedDocuments, CosmosAsyncContainer feedCollection, long count) {
        List<InternalObjectNode> docDefList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments.addAll(bulkInsertBlocking(feedCollection, docDefList));
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private void createReadFeedDocuments(List<InternalObjectNode> createdDocuments, CosmosAsyncContainer feedCollection, long count) {
        List<InternalObjectNode> docDefList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments.addAll(bulkInsertBlocking(feedCollection, docDefList));
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
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
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "leases_" + UUID.randomUUID(),
            "/id");
        return createCollection(createdDatabase, collectionDefinition, options, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseMonitorCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "monitor_" + UUID.randomUUID(),
            "/id");
        return createCollection(createdDatabase, collectionDefinition, options, provisionedThroughput);
    }

    private static synchronized void processItem(JsonNode item, Map<String, JsonNode> receivedDocuments) {
        try {
            ChangeFeedProcessorTest.log
                .info("RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            log.error("Failure in processing json [{}]", e.getMessage(), e);
        }
        receivedDocuments.put(item.get("id").asText(), item);
    }

    class LeaseStateMonitor {
        public Map<String, JsonNode> receivedLeases = new ConcurrentHashMap<>();
        public volatile boolean isAfterLeaseInitialization = false;
        public volatile boolean isAfterSplits = false;
        public volatile long parentContinuationToken = 0;
        public volatile boolean isContinuationTokenAdvancing = true;
    }
}
