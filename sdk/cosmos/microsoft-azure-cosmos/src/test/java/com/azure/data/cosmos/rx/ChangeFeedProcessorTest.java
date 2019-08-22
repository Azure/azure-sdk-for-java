// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ChangeFeedProcessor;
import com.azure.data.cosmos.ChangeFeedProcessorOptions;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosContainerRequestOptions;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.SerializationFormattingPolicy;
import com.azure.data.cosmos.SqlParameter;
import com.azure.data.cosmos.SqlParameterList;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.changefeed.ServiceItemLease;
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

    private CosmosDatabase createdDatabase;
    private CosmosContainer createdFeedCollection;
    private CosmosContainer createdLeaseCollection;
    private List<CosmosItemProperties> createdDocuments;
    private static volatile Map<String, CosmosItemProperties> receivedDocuments;
//    private final String databaseId = "testdb1";
//    private final String hostName = "TestHost1";
    private final String hostName = RandomStringUtils.randomAlphabetic(6);
    private final int FEED_COUNT = 10;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;

    private CosmosClient client;

    private ChangeFeedProcessor changeFeedProcessor;

    @Factory(dataProvider = "clientBuilders")
    public ChangeFeedProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readFeedDocumentsStartFromBeginning() {
        setupReadFeedDocuments();

        changeFeedProcessor = ChangeFeedProcessor.Builder()
            .hostName(hostName)
            .handleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {}", Thread.currentThread().getId());
                for (CosmosItemProperties item : docs) {
                    processItem(item);
                }
                ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
            })
            .feedContainer(createdFeedCollection)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                .leaseRenewInterval(Duration.ofSeconds(20))
                .leaseAcquireInterval(Duration.ofSeconds(10))
                .leaseExpirationInterval(Duration.ofSeconds(30))
                .feedPollDelay(Duration.ofSeconds(2))
                .leasePrefix("TEST")
                .maxItemCount(10)
                .startFromBeginning(true)
                .maxScaleCount(0) // unlimited
                .discardExistingLeases(true)
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
            e.printStackTrace();
        }

        changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        for (CosmosItemProperties item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.id())).as("Document with id: " + item.id()).isTrue();
        }

        // Wait for the feed processor to shutdown.
        try {
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receivedDocuments.clear();
     }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readFeedDocumentsStartFromCustomDate() {
        ChangeFeedProcessor changeFeedProcessor = ChangeFeedProcessor.Builder()
            .hostName(hostName)
            .handleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {}", Thread.currentThread().getId());
                for (CosmosItemProperties item : docs) {
                    processItem(item);
                }
                ChangeFeedProcessorTest.log.info("END processing from thread {}", Thread.currentThread().getId());
            })
            .feedContainer(createdFeedCollection)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                .leaseRenewInterval(Duration.ofSeconds(20))
                .leaseAcquireInterval(Duration.ofSeconds(10))
                .leaseExpirationInterval(Duration.ofSeconds(30))
                .feedPollDelay(Duration.ofSeconds(1))
                .leasePrefix("TEST")
                .maxItemCount(10)
                .startTime(OffsetDateTime.now().minusDays(1))
                .minScaleCount(1)
                .maxScaleCount(3)
                .discardExistingLeases(true)
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
                e.printStackTrace();
            }
        }

        assertThat(remainingWork >= 0).as("Failed to receive all the feed documents").isTrue();

        changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        for (CosmosItemProperties item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.id())).as("Document with id: " + item.id()).isTrue();
        }

        // Wait for the feed processor to shutdown.
        try {
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receivedDocuments.clear();
    }

    @Test(groups = { "emulator" }, timeOut = 40 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void staledLeaseAcquiring() {
        final String ownerFirst = "Owner_First";
        final String ownerSecond = "Owner_Second";
        final String leasePrefix = "TEST";

        ChangeFeedProcessor changeFeedProcessorFirst = ChangeFeedProcessor.Builder()
            .hostName(ownerFirst)
            .handleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                ChangeFeedProcessorTest.log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
            })
            .feedContainer(createdFeedCollection)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                .leasePrefix(leasePrefix)
            )
            .build();

        ChangeFeedProcessor changeFeedProcessorSecond = ChangeFeedProcessor.Builder()
            .hostName(ownerSecond)
            .handleChanges(docs -> {
                ChangeFeedProcessorTest.log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                for (CosmosItemProperties item : docs) {
                    processItem(item);
                }
                ChangeFeedProcessorTest.log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
            })
            .feedContainer(createdFeedCollection)
            .leaseContainer(createdLeaseCollection)
            .options(new ChangeFeedProcessorOptions()
                    .leaseRenewInterval(Duration.ofSeconds(10))
                    .leaseAcquireInterval(Duration.ofSeconds(5))
                    .leaseExpirationInterval(Duration.ofSeconds(20))
                    .feedPollDelay(Duration.ofSeconds(2))
                    .leasePrefix(leasePrefix)
                    .maxItemCount(10)
                    .startFromBeginning(true)
                    .maxScaleCount(0) // unlimited
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
                        e.printStackTrace();
                    }
                    ChangeFeedProcessorTest.log.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");

                    SqlParameter param = new SqlParameter();
                    param.name("@PartitionLeasePrefix");
                    param.value(leasePrefix);
                    SqlQuerySpec querySpec = new SqlQuerySpec(
                        "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)",
                        new SqlParameterList(param));

                    FeedOptions feedOptions = new FeedOptions();
                    feedOptions.enableCrossPartitionQuery(true);

                    createdLeaseCollection.queryItems(querySpec, feedOptions)
                        .delayElements(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT / 2))
                        .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.results()))
                        .flatMap(doc -> {
                            BridgeInternal.setProperty(doc, "Owner", "TEMP_OWNER");
                            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                            options.partitionKey(new PartitionKey(doc.id()));
                            return createdLeaseCollection.getItem(doc.id(), "/id")
                                .replace(doc, options)
                                .map(CosmosItemResponse::properties);
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
                e.printStackTrace();
            }
        }

        assertThat(remainingWork >= 0).as("Failed to receive all the feed documents").isTrue();

        changeFeedProcessorSecond.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        // Wait for the feed processor to shutdown.
        try {
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        client = clientBuilder().build();

//        try {
//            client.getDatabase(databaseId).read()
//                .map(cosmosDatabaseResponse -> cosmosDatabaseResponse.database())
//                .flatMap(database -> database.delete())
//                .onErrorResume(throwable -> {
//                    if (throwable instanceof com.azure.data.cosmos.CosmosClientException) {
//                        com.azure.data.cosmos.CosmosClientException clientException = (com.azure.data.cosmos.CosmosClientException) throwable;
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
//                    CosmosDatabase cosmosDatabase = client.getDatabase(cosmosDatabaseSettings.id());
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

    private CosmosContainer createFeedCollection() {
        CosmosContainerRequestOptions optionsFeedCollection = new CosmosContainerRequestOptions();
        return createCollection(createdDatabase, getCollectionDefinition(), optionsFeedCollection, 10100);
    }

    private CosmosContainer createLeaseCollection() {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(UUID.randomUUID().toString(), "/id");
        return createCollection(createdDatabase, collectionDefinition, options, 400);
    }

    private static synchronized void processItem(CosmosItemProperties item) {
        ChangeFeedProcessorTest.log.info("RECEIVED {}", item.toJson(SerializationFormattingPolicy.INDENTED));
        receivedDocuments.put(item.id(), item);
    }
}
