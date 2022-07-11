package com.azure.cosmos.rx;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.FullFidelityChangeFeedProcessorBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

public class ChangeFeedProcessorFullFidelityTest {

    private static final String databaseId = "testdb";
    private static final String feedContainerId = "feedcontainer";
    private static final String leaseContainerId = "leasecontainer";
    private static final String feedContainerPartitionKeyPath = "/myPk";
    private static final String leaseContainerPartitionKeyPath = "/id";
    private static final Logger logger = LoggerFactory.getLogger(ChangeFeedProcessorFullFidelityTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static CosmosAsyncDatabase testDatabase;
    private static CosmosAsyncContainer feedContainer;
    private static CosmosAsyncContainer leaseContainer;


    public static void main(String[] args) {
        setup();
        //  runFullFidelityChangeFeedProcessorFromBeginning();
        runFullFidelityChangeFeedProcessorFromNow();
        createItems();
        updateItems();
        deleteItems();
    }

    private static void deleteItems() {
        Flux.range(0, 2).flatMap(range -> {
            return feedContainer.deleteItem(String.valueOf(range), new PartitionKey(String.valueOf(range)));
        }).subscribe();
        logger.info("Deleting data, waiting for 10 seconds");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Deleted data");
    }

    private static void updateItems() {
        Flux.range(0, 5).flatMap(range -> {
            Family family = getItem(String.valueOf(range));
            family.setLastName("Updated");
            return feedContainer.upsertItem(family);
        }).subscribe();
        logger.info("Updating data, waiting for 10 seconds");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Updated data");
    }

    private static void createItems() {
        Flux.range(0, 5).flatMap(range -> {
            Family family = getItem(String.valueOf(range));
            return feedContainer.createItem(family);
        }).subscribe();
        logger.info("Generating data, waiting for 10 seconds");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(feedContainerId, feedContainerPartitionKeyPath);
        cosmosContainerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(5)));
        testDatabase.createContainer(cosmosContainerProperties).block();
        logger.info("Feed container created if not existed");

        testDatabase.createContainer(leaseContainerId, leaseContainerPartitionKeyPath).block();
        logger.info("Lease container created if not existed");
    }

    private static void runFullFidelityChangeFeedProcessorFromBeginning() {
        ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        changeFeedProcessorOptions.setStartFromBeginning(true);
        ChangeFeedProcessor changeFeedProcessor = new FullFidelityChangeFeedProcessorBuilder()
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .options(changeFeedProcessorOptions)
            .hostName("example-host")
            .handleChanges(jsonNodes -> {
                for (JsonNode item : jsonNodes) {
                    logger.info("BEGINNING : Received : {}", item);
                }
            }).buildChangeFeedProcessor();

        logger.info("Starting change feed processor");
        changeFeedProcessor.start().subscribe();
        try {
            logger.info("{} going to sleep for 10 seconds", Thread.currentThread().getName());
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            logger.error("Error occurred while sleeping", e);
        }
        logger.info("Finished starting change feed processor");
    }

    private static void runFullFidelityChangeFeedProcessorFromNow() {
        ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
        ChangeFeedProcessor changeFeedProcessor = new FullFidelityChangeFeedProcessorBuilder()
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .options(changeFeedProcessorOptions)
            .hostName("example-host")
            .handleChanges(jsonNodes -> {
                for (JsonNode item : jsonNodes) {
                    logger.info("NOW : Received : {}", item);
                }
            }).buildChangeFeedProcessor();

        logger.info("Starting change feed processor");
        changeFeedProcessor.start().subscribe();
        try {
            logger.info("{} going to sleep for 10 seconds", Thread.currentThread().getName());
            Thread.sleep(10 * 1000);
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
