// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ChangeFeedMode;
import com.azure.cosmos.models.ChangeFeedOperationType;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ChangeFeedProcessorFullFidelityTest {

    private static final String databaseId = "SampleDatabase";
    private static final String feedContainerId = "GreenTaxiRecords";
    private static final String leaseContainerId = "newleasecontainer";
    private static final String feedContainerPartitionKeyPath = "/lastName";
    private static final String leaseContainerPartitionKeyPath = "/id";
    private static final Logger logger = LoggerFactory.getLogger(ChangeFeedProcessorFullFidelityTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static CosmosAsyncDatabase testDatabase;
    private static CosmosAsyncContainer feedContainer;
    private static CosmosAsyncContainer leaseContainer;

    private static final Map<ChangeFeedOperationType, AtomicInteger> changeFeedMap = new ConcurrentHashMap<>();
    private static final Set<Integer> createChangeFeedSet = ConcurrentHashMap.newKeySet();
    private static final Set<Integer> deleteChangeFeedSet = ConcurrentHashMap.newKeySet();
    private static final Set<Integer> replaceChangeFeedSet = ConcurrentHashMap.newKeySet();
    private static final int createCount = 10;
    private static final int replaceCount = 10;
    private static final int deleteCount = 10;

    public static void main(String[] args) {
        setup();
        runFullFidelityChangeFeedProcessorFromNow();
        createItems();
        countNumberOfRecords();
        replaceItems();
        countNumberOfRecords();
        deleteItems();
        countNumberOfRecords();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(ChangeFeedProcessorFullFidelityTest::checkChangeFeedMapDetails,
            30, 30, TimeUnit.SECONDS);
    }

    private static void countNumberOfRecords() {
        String query = "Select value count(1) from c";
        CosmosPagedFlux<Long> longCosmosPagedFlux = feedContainer.queryItems(query, new CosmosQueryRequestOptions(),
            Long.class);
        FeedResponse<Long> longFeedResponse = longCosmosPagedFlux.byPage().blockLast();
        logger.info("Number of records so far is : {}", longFeedResponse.getResults().get(0));
    }

    private static void checkChangeFeedMapDetails() {
        logger.info("Change feed map details are");
        changeFeedMap.forEach((key, value) -> {
            logger.info("Operation type : {}, number of changes : {}", key, value.get());
        });
        int missingItems = 0;
        for (int i = 0; i < createCount; i++) {
            if (!createChangeFeedSet.contains(i)) {
                missingItems++;
            }
        }
        logger.info("Missing create items : {}", missingItems);

        missingItems = 0;
        for (int i = 0; i < replaceCount; i++) {
            if (!replaceChangeFeedSet.contains(i)) {
                missingItems++;
            }
        }
        logger.info("Missing replace items : {}", missingItems);

        missingItems = 0;
        for (int i = 0; i < deleteCount; i++) {
            if (!deleteChangeFeedSet.contains(i)) {
                missingItems++;
            }
        }
        logger.info("Missing delete items : {}", missingItems);
    }

    private static void deleteItems() {
        Flux.range(0, deleteCount).flatMap(range -> {
            return feedContainer.deleteItem(String.valueOf(range), new PartitionKey("lastName"));
        }).blockLast();
        logger.info("Deleting items, waiting for 10 seconds");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Deleted items");
    }

    private static void replaceItems() {
        Flux.range(0, replaceCount).flatMap(range -> {
            Family family = getItem(String.valueOf(range));
            family.setFirstName("Updated");
            return feedContainer.replaceItem(family, family.getId(), new PartitionKey(family.getLastName()));
        }).blockLast();
        logger.info("Replacing items, waiting for 10 seconds");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Replaced items");
    }

    private static void createItems() {
        Flux.range(0, createCount).flatMap(range -> {
            Family family = getItem(String.valueOf(range));
            return feedContainer.createItem(family);
        }).blockLast();
        logger.info("Creating items, waiting for 10 seconds");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Created items");
    }

    private static void setup() {
        logger.info("Setting up");

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

        cosmosAsyncClient.createDatabaseIfNotExists(databaseId).block();
        logger.info("Test database created if not existed");
        testDatabase = cosmosAsyncClient.getDatabase(databaseId);

        feedContainer = testDatabase.getContainer(feedContainerId);
        logger.info("Deleting feed container");
        try {
            feedContainer.delete().block();
        } catch (CosmosException cosmosException) {
            logger.error("Error occurred while deleting feed container", cosmosException);
            if (!Exceptions.isNotFound(cosmosException)) {
                throw cosmosException;
            }
        }

        leaseContainer = testDatabase.getContainer(leaseContainerId);
        logger.info("Deleting lease container");
        try {
            leaseContainer.delete().block();
        } catch (CosmosException cosmosException) {
            logger.error("Error occurred while deleting lease container", cosmosException);
            if (!Exceptions.isNotFound(cosmosException)) {
                throw cosmosException;
            }
        }

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(feedContainerId,
            feedContainerPartitionKeyPath);
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(100000);
        cosmosContainerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(60)));
        testDatabase.createContainerIfNotExists(cosmosContainerProperties, throughputProperties).block();
//        cosmosContainerProperties = feedContainer.read().block().getProperties();
//        cosmosContainerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(5)));
//        feedContainer.replace(cosmosContainerProperties).block();
        logger.info("Feed container created if not existed");

        testDatabase.createContainerIfNotExists(leaseContainerId, leaseContainerPartitionKeyPath).block();
        logger.info("Lease container created if not existed");
    }

    private static void runFullFidelityChangeFeedProcessorFromNow() {
        ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        changeFeedProcessorOptions.setMaxItemCount(1000);
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .options(changeFeedProcessorOptions)
            .hostName("example-host")
            .changeFeedMode(ChangeFeedMode.FULL_FIDELITY)
            .handleCFPItemChanges(changeFeedProcessorItems -> {
                for (ChangeFeedProcessorItem item : changeFeedProcessorItems) {
                    try {
//                        logger.info("Item is : {}", item.toString());
                        ChangeFeedOperationType operationType = item.getChangeFeedMetaData().getOperationType();
                        changeFeedMap.computeIfAbsent(operationType, changeFeedOperationType -> new AtomicInteger(0));
                        changeFeedMap.get(operationType).incrementAndGet();
                        switch (operationType) {
                            case CREATE:
                                createChangeFeedSet.add(item.getCurrent().get("id").asInt());
                                break;
                            case DELETE:
                                deleteChangeFeedSet.add(item.getPrevious().get("id").asInt());
                                break;
                            case REPLACE:
                                replaceChangeFeedSet.add(item.getCurrent().get("id").asInt());
                                break;
                            default:
                                throw new IllegalStateException("Operation type : " + operationType + " not supported");
                        }
                    } catch (Exception e) {
                        if (item == null) {
                            logger.error("Received null item ", e);
                        } else {
                            logger.error("Error occurred for item : {}", item, e);
                        }
                    }
                }
            }).buildChangeFeedProcessor();

        logger.info("Starting change feed processor");
        changeFeedProcessor.start().subscribe();
        try {
            logger.info("{} going to sleep for 30 seconds", Thread.currentThread().getName());
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            logger.error("Error occurred while sleeping", e);
        }
        logger.info("Finished starting change feed processor");
    }

    private static Family getItem(String id) {
        return new Family(id, "firstName", "lastName", id);
    }


    private static class Family {
        private String id;
        private String firstName;
        private String lastName;
        private String myPk;

        public Family(String id, String firstName, String lastName, String myPk) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.myPk = myPk;
        }

        public Family() {
        }

        public String getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getMyPk() {
            return myPk;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setMyPk(String myPk) {
            this.myPk = myPk;
        }

        @Override
        public String toString() {
            return "Family{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", myPk='" + myPk + '\'' +
                '}';
        }
    }
}
