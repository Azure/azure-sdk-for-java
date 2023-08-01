// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx.changefeed.epkversion;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: Annie enable it when public API is enabled
public class ChangeFeedProcessorMigrationTests extends TestSuiteBase {
    /*
    private final static Logger logger = LoggerFactory.getLogger(IncrementalChangeFeedProcessorTest.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    private CosmosAsyncDatabase createdDatabase;
    private final String hostName = RandomStringUtils.randomAlphabetic(6);
    private final int FEED_COUNT = 10;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
    private final int FEED_COLLECTION_THROUGHPUT = 10100;
    private final int LEASE_COLLECTION_THROUGHPUT = 400;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public ChangeFeedProcessorMigrationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator", "simple" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_ChangeFeedProcessorMigrateTests() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterClass(groups = { "emulator", "simple" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @Test(groups = { "emulator" }, timeOut = 2 * TIMEOUT)
    public void readFeedDocumentsBootstrapFromPkVersion() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

            ChangeFeedProcessor changeFeedProcessor =
                this.createDefaultChangeFeedProcessorBuilder(hostName, createdFeedCollection, createdLeaseCollection)
                    .handleChanges(handleChangesHandler(receivedDocuments))
                    .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .subscribe();
            } catch (Exception ex) {
                logger.error("Change feed processor did not start in the expected time", ex);
                throw ex;
            }

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            changeFeedProcessor
                .stop()
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT))
                .subscribe();

            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            String leaseItemCountQuery = "select VALUE COUNT(1) from c";
            int leaseContainerItemCount = createdLeaseCollection.queryItems(leaseItemCountQuery, Integer.class)
                .collectList().block().get(0);

            // now switch to use handleLatestVersionChanges, validate it resumes from checkpoint from handChanges
            logger.info("Switch to use handleLatestVersionChanges");

            createdDocuments.clear();
            Map<String, JsonNode> newReceivedDocuments = new ConcurrentHashMap<>();
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);
            ChangeFeedProcessor changeFeedProcessor2 =
                this.createDefaultChangeFeedProcessorBuilder(hostName, createdFeedCollection, createdLeaseCollection)
                    .handleLatestVersionChanges(handleLatestVersionChangesHandler(newReceivedDocuments))
                    .buildChangeFeedProcessor();
            try {
                changeFeedProcessor2.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(5 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .subscribe();
            } catch (Exception ex) {
                logger.error("Change feed processor did not start in the expected time", ex);
                throw ex;
            }

            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
            assertThat(changeFeedProcessor2.isStarted()).as("Change Feed Processor instance is running").isTrue();
            changeFeedProcessor2
                .stop()
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT))
                .subscribe();

            assertThat(newReceivedDocuments.size()).isEqualTo(createdDocuments.size());
            for (InternalObjectNode item : createdDocuments) {
                assertThat(newReceivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // Validate lease container item
            String query = "select * from c";
            List<JsonNode> allItems = createdLeaseCollection.queryItems(query, JsonNode.class)
                .collectList().block();
            assertThat(allItems.size()).isEqualTo(leaseContainerItemCount + 1);
            int infoFileCount = 0;
            for (JsonNode item : allItems) {
                if (item.get("id").asText().endsWith(".info")) {
                    infoFileCount++;
                } else {
                    assertThat(item.get("version").asInt()).isEqualTo(LeaseVersion.EPK_RANGE_BASED_LEASE.getVersionId());
                }
            }

            assertThat(infoFileCount).isEqualTo(2);

            // Wait for the feed processor to shut down.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
        }
    }

    private ChangeFeedProcessorBuilder createDefaultChangeFeedProcessorBuilder(
        String hostName,
        CosmosAsyncContainer feedContainer,
        CosmosAsyncContainer leaseContainer) {
        return new ChangeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .options(new ChangeFeedProcessorOptions()
                .setLeaseRenewInterval(Duration.ofSeconds(20))
                .setLeaseAcquireInterval(Duration.ofSeconds(10))
                .setLeaseExpirationInterval(Duration.ofSeconds(30))
                .setFeedPollDelay(Duration.ofSeconds(2))
                .setLeasePrefix("TEST")
                .setMaxItemCount(10)
                .setStartFromBeginning(true)
                .setMaxScaleCount(0) // unlimited
            );
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

    private void setupReadFeedDocuments(
        List<InternalObjectNode> createdDocuments,
        CosmosAsyncContainer feedCollection,
        long count) {
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


    private Consumer<List<JsonNode>> handleChangesHandler(Map<String, JsonNode> receivedDocuments) {
        return docs -> {
            logger.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                processItem(item, receivedDocuments);
            }
            logger.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private Consumer<List<ChangeFeedProcessorItem>> handleLatestVersionChangesHandler(Map<String, JsonNode> receivedDocuments) {
        return docs -> {
            logger.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                processItem(item.getCurrent(), receivedDocuments);
            }
            logger.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private static synchronized void processItem(JsonNode item, Map<String, JsonNode> receivedDocuments) {
        try {
            logger.info("RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            logger.error("Failure in processing json [{}]", e.getMessage(), e);
        }
        receivedDocuments.put(item.get("id").asText(), item);
    }
    */
}

