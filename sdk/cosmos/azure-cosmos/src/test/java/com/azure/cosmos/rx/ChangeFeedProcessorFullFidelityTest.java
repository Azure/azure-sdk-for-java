// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.FullFidelityChangeFeedProcessorBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ChangeFeedOperationType;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//  TODO: (kuthapar) - to be removed after testing.
public class ChangeFeedProcessorFullFidelityTest {

    private static final String databaseId = "SampleDatabase";
    private static final String feedContainerId = "GreenTaxiRecords";
    private static final String leaseContainerId = "newleasecontainer";
    private static final String feedContainerPartitionKeyPath = "/myPk";
    private static final String leaseContainerPartitionKeyPath = "/id";
    private static final Logger logger = LoggerFactory.getLogger(ChangeFeedProcessorFullFidelityTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static CosmosAsyncDatabase testDatabase;
    private static CosmosAsyncContainer feedContainer;
    private static CosmosAsyncContainer leaseContainer;

    private static final Map<ChangeFeedOperationType, Integer> changeFeedMap = new ConcurrentHashMap<>();
    private static final Set<Integer> createChangeFeedSet = new HashSet<>();
    private static final Set<Integer> deleteChangeFeedSet = new HashSet<>();
    private static final Set<Integer> replaceChangeFeedSet = new HashSet<>();
    private static final int createCount = 1000;
    private static final int replaceCount = 1000;
    private static final int deleteCount = 1000;

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
        CosmosPagedFlux<Long> longCosmosPagedFlux = feedContainer.queryItems(query, new CosmosQueryRequestOptions(), Long.class);
        FeedResponse<Long> longFeedResponse = longCosmosPagedFlux.byPage().blockLast();
        logger.info("Number of records so far is : {}", longFeedResponse.getResults().get(0));
    }

    private static void checkChangeFeedMapDetails() {
        logger.info("Change feed map details are");
        changeFeedMap.forEach((key, value) -> {
            logger.info("Operation type : {}, number of changes : {}", key, value);
        });
        for (int i = 0; i < createCount; i++) {
            if (!createChangeFeedSet.contains(i)) {
                logger.info("Missing create item id : {}", i);
            }
        }
        for (int i = 0; i < replaceCount; i++) {
            if (!replaceChangeFeedSet.contains(i)) {
                logger.info("Missing replace item id : {}", i);
            }
        }
        for (int i = 0; i < deleteCount; i++) {
            if (!deleteChangeFeedSet.contains(i)) {
                logger.info("Missing delete item id : {}", i);
            }
        }
    }

    private static void deleteItems() {
        Flux.range(0, deleteCount).flatMap(range -> {
            return feedContainer.deleteItem(String.valueOf(range), new PartitionKey(String.valueOf(range)));
        }).blockLast();
        logger.info("Deleting data, waiting for 10 seconds");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Deleted data");
    }

    private static void replaceItems() {
        Flux.range(0, replaceCount).flatMap(range -> {
            Family family = getItem(String.valueOf(range));
            family.setLastName("Updated");
            return feedContainer.replaceItem(family, family.getId(), new PartitionKey(family.getMyPk()));
        }).blockLast();
        logger.info("Updating data, waiting for 10 seconds");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Updated data");
    }

    private static void createItems() {
        Flux.range(0, createCount).flatMap(range -> {
            Family family = getItem(String.valueOf(range));
            return feedContainer.createItem(family);
        }).blockLast();
        logger.info("Generating data, waiting for 10 seconds");
//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        logger.info("Generated data");
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

        logger.info("Deleting feed container");
        feedContainer = testDatabase.getContainer(feedContainerId);
        feedContainer.delete().block();

        logger.info("Deleting lease container");
        leaseContainer = testDatabase.getContainer(leaseContainerId);
        leaseContainer.delete().block();

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(feedContainerId,
            feedContainerPartitionKeyPath);
        cosmosContainerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(5)));
//        cosmosContainerProperties.setDefaultTimeToLiveInSeconds(5);
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(100000);
        testDatabase.createContainer(cosmosContainerProperties, throughputProperties).block();
        logger.info("Feed container created if not existed");

        testDatabase.createContainerIfNotExists(leaseContainerId, leaseContainerPartitionKeyPath).block();
        logger.info("Lease container created if not existed");
    }

    private static void runFullFidelityChangeFeedProcessorFromNow() {
        ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        ChangeFeedProcessor changeFeedProcessor = new FullFidelityChangeFeedProcessorBuilder()
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .options(changeFeedProcessorOptions)
            .hostName("example-host")
            .handleChanges(changeFeedProcessorItems -> {
                for (ChangeFeedProcessorItem item : changeFeedProcessorItems) {
                    try {
//                        logger.info("Item is : {}", item.toString());
                        ChangeFeedOperationType operationType = item.getChangeFeedMetaData().getOperationType();
                        if (!changeFeedMap.containsKey(operationType)) {
                            changeFeedMap.put(operationType, 0);
                        }
                        changeFeedMap.put(operationType, changeFeedMap.get(operationType) + 1);
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
                    }
                    catch (Exception e) {
                        if (item == null)  {
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
            logger.info("{} going to sleep for 10 seconds", Thread.currentThread().getName());
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            logger.error("Error occurred while sleeping", e);
        }
        logger.info("Finished starting change feed processor");
    }

    private static Family getItem(String pk) {
        return new Family(pk, UUID.randomUUID().toString(), UUID.randomUUID().toString(), pk);
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
