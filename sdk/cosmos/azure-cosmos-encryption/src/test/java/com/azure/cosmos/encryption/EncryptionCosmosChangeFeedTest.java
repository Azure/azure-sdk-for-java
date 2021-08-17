// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionCosmosChangeFeedTest extends TestSuiteBase {
    private final static Logger logger = LoggerFactory.getLogger(EncryptionCosmosChangeFeedTest.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    private final static String CHANGE_FEED_PK_1 = "changeFeedPK1";
    private final static String CHANGE_FEED_PK_2 = "changeFeedPK2";
    private final static String CHANGE_FEED_PK_3 = "changeFeedPK3";
    private final static String CHANGE_FEED_PROCESSOR_PK = "changeFeedProcessorPK";
    private final int LEASE_COLLECTION_THROUGHPUT = 400;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
    private Map<String, EncryptionPojo> createdItemsForPk1 = new HashMap<>();
    private Map<String, EncryptionPojo> createdItemsForPk2 = new HashMap<>();
    private Map<String, EncryptionPojo> createdItemsForPk3 = new HashMap<>();
    private CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;
    private CosmosEncryptionContainer cosmosEncryptionContainer;
    private CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;

    @Factory(dataProvider = "clientBuilders")
    public EncryptionCosmosChangeFeedTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        CosmosAsyncClient asyncClient = getClientBuilder().buildAsyncClient();
        CosmosClient syncClient = getClientBuilder().buildClient();
        TestEncryptionKeyStoreProvider encryptionKeyStoreProvider = new TestEncryptionKeyStoreProvider();
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(asyncClient,
            encryptionKeyStoreProvider);
        cosmosEncryptionAsyncContainer = getSharedEncryptionContainer(cosmosEncryptionAsyncClient);
        cosmosEncryptionAsyncDatabase = getSharedEncryptionDatabase(cosmosEncryptionAsyncClient);

        CosmosEncryptionClient cosmosEncryptionClient = CosmosEncryptionClient.createCosmosEncryptionClient(syncClient,
            encryptionKeyStoreProvider);
        cosmosEncryptionContainer = getSharedSyncEncryptionContainer(cosmosEncryptionClient);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void changeFeed_fromBeginning() {
        populateItems(createdItemsForPk1, CHANGE_FEED_PK_1);
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(CHANGE_FEED_PK_1)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange);
        changeFeedOption.setMaxItemCount(3);

        CosmosPagedFlux<EncryptionPojo> feedResponseIterator = cosmosEncryptionAsyncContainer
            .queryChangeFeed(changeFeedOption, EncryptionPojo.class);
        List<EncryptionPojo> changeFeedResultList = new ArrayList<>();
        Iterator<FeedResponse<EncryptionPojo>> responseIterator =
            feedResponseIterator.byPage().toIterable().iterator();
        while (responseIterator.hasNext()) {
            FeedResponse<EncryptionPojo> feedResponse = responseIterator.next();
            assertThat(feedResponse.getContinuationToken())
                .as("Response continuation should not be null")
                .isNotNull();
            changeFeedResultList.addAll(feedResponse.getResults());
        }

        assertThat(changeFeedResultList.size()).isEqualTo(createdItemsForPk1.size());
        for (EncryptionPojo pojo : changeFeedResultList) {
            validateResponse(pojo, createdItemsForPk1.get(pojo.getId()));
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void changeFeed_fromNow() {
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(CHANGE_FEED_PK_2)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromNow(feedRange);
        changeFeedOption.setMaxItemCount(3);

        CosmosPagedFlux<EncryptionPojo> feedResponseIterator = cosmosEncryptionAsyncContainer
            .queryChangeFeed(changeFeedOption, EncryptionPojo.class);
        List<EncryptionPojo> changeFeedResultList = new ArrayList<>();
        Iterator<FeedResponse<EncryptionPojo>> responseIterator =
            feedResponseIterator.byPage().toIterable().iterator();
        String continuationToken = null;
        while (responseIterator.hasNext()) {
            FeedResponse<EncryptionPojo> feedResponse = responseIterator.next();
            assertThat(feedResponse.getContinuationToken())
                .as("Response continuation should not be null")
                .isNotNull();
            continuationToken = feedResponse.getContinuationToken();
            changeFeedResultList.addAll(feedResponse.getResults());
        }
        assertThat(changeFeedResultList.size()).isEqualTo(0);

        populateItems(createdItemsForPk2, CHANGE_FEED_PK_2);

        changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuationToken);
        feedResponseIterator = cosmosEncryptionAsyncContainer
            .queryChangeFeed(changeFeedOption, EncryptionPojo.class);
        responseIterator =
            feedResponseIterator.byPage().toIterable().iterator();
        while (responseIterator.hasNext()) {
            FeedResponse<EncryptionPojo> feedResponse = responseIterator.next();
            assertThat(feedResponse.getContinuationToken())
                .as("Response continuation should not be null")
                .isNotNull();
            changeFeedResultList.addAll(feedResponse.getResults());
        }
        assertThat(changeFeedResultList.size()).isEqualTo(10);
        for (EncryptionPojo pojo : changeFeedResultList) {
            validateResponse(pojo, createdItemsForPk2.get(pojo.getId()));
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void syncChangeFeed_fromBeginning() {
        populateItems(createdItemsForPk3, CHANGE_FEED_PK_3);
        FeedRange feedRange = new FeedRangePartitionKeyImpl(
            ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(CHANGE_FEED_PK_3)));
        CosmosChangeFeedRequestOptions changeFeedOption =
            CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRange);
        changeFeedOption.setMaxItemCount(3);

        CosmosPagedIterable<EncryptionPojo> cosmosPagedIterable = cosmosEncryptionContainer
            .queryChangeFeed(changeFeedOption, EncryptionPojo.class);
        List<EncryptionPojo> changeFeedResultList = new ArrayList<>();
        Iterator<FeedResponse<EncryptionPojo>> responseIterator =
            cosmosPagedIterable.iterableByPage().iterator();
        while (responseIterator.hasNext()) {
            FeedResponse<EncryptionPojo> feedResponse = responseIterator.next();
            assertThat(feedResponse.getContinuationToken())
                .as("Response continuation should not be null")
                .isNotNull();
            changeFeedResultList.addAll(feedResponse.getResults());
        }

        assertThat(changeFeedResultList.size()).isEqualTo(createdItemsForPk3.size());
        for (EncryptionPojo pojo : changeFeedResultList) {
            validateResponse(pojo, createdItemsForPk3.get(pojo.getId()));
        }
    }


    @Test(groups = { "emulator" }, timeOut = 2 * TIMEOUT)
    public void pushModel_readFeedDocuments() throws InterruptedException, JsonProcessingException {
        CosmosEncryptionAsyncContainer createdFeedCollection =  createFeedCollection();
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);
        try {
            List<EncryptionPojo> createdDocuments = new ArrayList<>();
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, 10);
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedEncryptionProcessorBuilder()
                .hostName(RandomStringUtils.randomAlphabetic(6))
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(20))
                    .setLeaseAcquireInterval(Duration.ofSeconds(10))
                    .setLeaseExpirationInterval(Duration.ofSeconds(30))
                    .setFeedPollDelay(Duration.ofSeconds(1))
                    .setLeasePrefix("TEST")
                    .setMaxItemCount(10)
                    .setStartFromBeginning(true)
                )
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .subscribe();
            } catch (Exception ex) {
                logger.error("Change feed processor did not start in the expected time", ex);
                throw ex;
            }

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            assertThat(receivedDocuments.size()).isEqualTo(createdDocuments.size());
            for (EncryptionPojo item : createdDocuments) {
                EncryptionPojo receivedEncryptionPojo = OBJECT_MAPPER.treeToValue(receivedDocuments.get(item.getId()), EncryptionPojo.class);
                validateResponse(item, receivedEncryptionPojo);
            }

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdLeaseCollection);
        }
    }

    private void setupReadFeedDocuments(List<EncryptionPojo> createdDocuments,
                                        CosmosEncryptionAsyncContainer feedEncryptionCollection, long count) {
        for (int i = 0; i < count; i++) {
            EncryptionPojo properties = getItem(UUID.randomUUID().toString());
            properties.setMypk(CHANGE_FEED_PROCESSOR_PK);
            CosmosItemResponse<EncryptionPojo> itemResponse = feedEncryptionCollection.createItem(properties,
                new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
            createdDocuments.add(itemResponse.getItem());
        }

        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private CosmosAsyncContainer createLeaseCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "leases_" + UUID.randomUUID(),
            "/id");
        this.cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(collectionDefinition,
            ThroughputProperties.createManualThroughput(provisionedThroughput)).block();
        return this.cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().getContainer(collectionDefinition.getId());
    }

    private CosmosEncryptionAsyncContainer createFeedCollection() {
        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(getPaths());
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionPolicy);
        this.cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties).block();
        return cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);
    }

    private void populateItems(Map<String, EncryptionPojo> createdItems, String pk) {
        for (int i = 0; i < 10; i++) {
            EncryptionPojo properties = getItem(UUID.randomUUID().toString());
            properties.setMypk(pk);
            CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionAsyncContainer.createItem(properties,
                new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions()).block();
            createdItems.put(itemResponse.getItem().getId(), itemResponse.getItem());
        }
    }

    private Consumer<List<JsonNode>> changeFeedProcessorHandler(Map<String, JsonNode> receivedDocuments) {
        return docs -> {
            logger.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                processItem(item, receivedDocuments);
            }
            logger.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private static synchronized void processItem(JsonNode item, Map<String, JsonNode> receivedDocuments) {
        try {
            logger
                .info("RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            logger.error("Failure in processing json [{}]", e.getMessage(), e);
        }
        receivedDocuments.put(item.get("id").asText(), item);
    }
}
