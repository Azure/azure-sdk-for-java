// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorOptions;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncItemResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainerProperties;
import com.azure.cosmos.CosmosContainerRequestOptions;
import com.azure.cosmos.CosmosItemProperties;
import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.SerializationFormattingPolicy;
import com.azure.cosmos.SqlParameter;
import com.azure.cosmos.SqlParameterList;
import com.azure.cosmos.SqlQuerySpec;
import com.azure.cosmos.internal.changefeed.ServiceItemLease;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedProcessorTest extends TestSuiteBase {
    private final static Logger log = LoggerFactory.getLogger(ChangeFeedProcessorTest.class);

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdFeedCollection;
    private CosmosAsyncContainer createdLeaseCollection;
    private List<CosmosItemProperties> createdDocuments;
    private static Map<String, CosmosItemProperties> receivedDocuments;
//    private final String databaseId = "testdb1";
//    private final String hostName = "TestHost1";
    private final String hostName = RandomStringUtils.randomAlphabetic(6);
    private final int FEED_COUNT = 10;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;

    private CosmosAsyncClient client;

    private ChangeFeedProcessor changeFeedProcessor;

    @Factory(dataProvider = "clientBuilders")
    public ChangeFeedProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readFeedDocumentsStartFromBeginning() {
        setupReadFeedDocuments();

        changeFeedProcessor = ChangeFeedProcessor.Builder()
            .setHostName(hostName)
            .setHandleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {}", Thread.currentThread().getId());
                for (CosmosItemProperties item : docs) {
                    processItem(item);
                }
                ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
            })
            .setFeedContainer(createdFeedCollection)
            .setLeaseContainer(createdLeaseCollection)
            .setOptions(new ChangeFeedProcessorOptions()
                .setLeaseRenewInterval(Duration.ofSeconds(20))
                .setLeaseAcquireInterval(Duration.ofSeconds(10))
                .setLeaseExpirationInterval(Duration.ofSeconds(30))
                .setFeedPollDelay(Duration.ofSeconds(2))
                .setLeasePrefix("TEST")
                .setMaxItemCount(10)
                .setStartFromBeginning(true)
                .setMaxScaleCount(0) // unlimited
                .setDiscardExistingLeases(true)
            )
            .build();

        try {
            changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                .timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT))
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
        ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.Builder()
            .setHostName(hostName)
            .setHandleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {}", Thread.currentThread().getId());
                for (CosmosItemProperties item : docs) {
                    processItem(item);
                }
                ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
            })
            .setFeedContainer(createdFeedCollection)
            .setLeaseContainer(createdLeaseCollection)
            .setOptions(new ChangeFeedProcessorOptions()
                .setLeaseRenewInterval(Duration.ofSeconds(20))
                .setLeaseAcquireInterval(Duration.ofSeconds(10))
                .setLeaseExpirationInterval(Duration.ofSeconds(30))
                .setFeedPollDelay(Duration.ofSeconds(1))
                .setLeasePrefix("TEST")
                .setMaxItemCount(10)
                .setStartTime(OffsetDateTime.now().minusDays(1))
                .setMinScaleCount(1)
                .setMaxScaleCount(3)
                .setDiscardExistingLeases(true)
            )
            .build();

        try {
            changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                .timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT))
                .subscribe();
        } catch (Exception ex) {
            log.error("Change feed processor did not start in the expected time", ex);
        }

        setupReadFeedDocuments();

        // Wait for the feed processor to receive and process the documents.
        long remainingWork = FEED_TIMEOUT;
        while (remainingWork > 0 && receivedDocuments.size() < FEED_COUNT) {
            remainingWork -= 100;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }

        assertThat(remainingWork >= 0).as("Failed to receive all the feed documents").isTrue();

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

        ChangeFeedProcessor changeFeedProcessorFirst = ChangeFeedProcessor.Builder()
            .setHostName(ownerFirst)
            .setHandleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                ChangeFeedProcessorTest.log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
            })
            .setFeedContainer(createdFeedCollection)
            .setLeaseContainer(createdLeaseCollection)
            .setOptions(new ChangeFeedProcessorOptions()
                .setLeasePrefix(leasePrefix)
            )
            .build();

        ChangeFeedProcessor changeFeedProcessorSecond = ChangeFeedProcessor.Builder()
            .setHostName(ownerSecond)
            .setHandleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                for (CosmosItemProperties item : docs) {
                    processItem(item);
                }
                ChangeFeedProcessorTest.log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
            })
            .setFeedContainer(createdFeedCollection)
            .setLeaseContainer(createdLeaseCollection)
            .setOptions(new ChangeFeedProcessorOptions()
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
                        "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)",
                        new SqlParameterList(param));

                    FeedOptions feedOptions = new FeedOptions();
                    feedOptions.setEnableCrossPartitionQuery(true);

                    createdLeaseCollection.queryItems(querySpec, feedOptions)
                        .delayElements(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT / 2))
                        .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.getResults()))
                        .flatMap(doc -> {
                            BridgeInternal.setProperty(doc, "Owner", "TEMP_OWNER");
                            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                            options.setPartitionKey(new PartitionKey(doc.getId()));
                            return createdLeaseCollection.getItem(doc.getId(), "/id")
                                .replace(doc, options)
                                .map(CosmosAsyncItemResponse::getProperties);
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
        long remainingWork = 40 * CHANGE_FEED_PROCESSOR_TIMEOUT;
        while (remainingWork > 0 && receivedDocuments.size() < FEED_COUNT) {
            remainingWork -= 100;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }

        assertThat(remainingWork >= 0).as("Failed to receive all the feed documents").isTrue();

        changeFeedProcessorSecond.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        // Wait for the feed processor to shutdown.
        try {
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        receivedDocuments.clear();
    }

    @BeforeMethod(groups = { "emulator" }, timeOut = 2 * SETUP_TIMEOUT, alwaysRun = true)
     public void beforeMethod() {
         createdFeedCollection = createFeedCollection();
         createdLeaseCollection = createLeaseCollection();
     }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();

//        try {
//            client.getDatabase(databaseId).read()
//                .map(cosmosDatabaseResponse -> cosmosDatabaseResponse.database())
//                .flatMap(database -> database.delete())
//                .onErrorResume(throwable -> {
//                    if (throwable instanceof com.azure.cosmos.CosmosClientException) {
//                        com.azure.cosmos.CosmosClientException clientException = (com.azure.cosmos.CosmosClientException) throwable;
//                        if (clientException.statusCode() == 404) {
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

        // Allow some time for the collections and the database to be deleted before exiting.
        try {
            Thread.sleep(500);
        } catch (Exception e){ }
    }

    @AfterClass(groups = { "emulator" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
//        try {
//            client.readAllDatabases()
//                .flatMap(cosmosDatabaseSettingsFeedResponse -> reactor.core.publisher.Flux.fromIterable(cosmosDatabaseSettingsFeedResponse.results()))
//                .flatMap(cosmosDatabaseSettings -> {
//                    CosmosAsyncDatabase cosmosDatabase = client.getDatabase(cosmosDatabaseSettings.id());
//                    return cosmosDatabase.delete();
//                }).blockLast();
//            Thread.sleep(500);
//        } catch (Exception e){ }

        safeClose(client);
    }

    private void setupReadFeedDocuments() {
        receivedDocuments = new ConcurrentHashMap<>();
        List<CosmosItemProperties> docDefList = new ArrayList<>();

        for(int i = 0; i < FEED_COUNT; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments = bulkInsertBlocking(createdFeedCollection, docDefList);
        waitIfNeededForReplicasToCatchUp(clientBuilder());
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

    private CosmosAsyncContainer createFeedCollection() {
        CosmosContainerRequestOptions optionsFeedCollection = new CosmosContainerRequestOptions();
        return createCollection(createdDatabase, getCollectionDefinition(), optionsFeedCollection, 10100);
    }

    private CosmosAsyncContainer createLeaseCollection() {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), "/id");
        return createCollection(createdDatabase, collectionDefinition, options, 400);
    }

    private static synchronized void processItem(CosmosItemProperties item) {
        ChangeFeedProcessorTest.log.info("RECEIVED {}", item.toJson(SerializationFormattingPolicy.INDENTED));
        receivedDocuments.put(item.getId(), item);
    }
}
