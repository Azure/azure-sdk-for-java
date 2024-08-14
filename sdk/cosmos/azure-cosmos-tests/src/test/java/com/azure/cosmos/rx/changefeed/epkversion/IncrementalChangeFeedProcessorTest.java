// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx.changefeed.epkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.azure.cosmos.BridgeInternal.extractContainerSelfLink;
import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class IncrementalChangeFeedProcessorTest extends TestSuiteBase {
    private final static Logger logger = LoggerFactory.getLogger(IncrementalChangeFeedProcessorTest.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private CosmosAsyncDatabase createdDatabase;
    private final String hostName = RandomStringUtils.randomAlphabetic(6);
    private final int FEED_COUNT = 10;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
    private final int REPLICA_IN_SATELLITE_REGION_CATCH_UP_TIME = 10000;
    private final int FEED_COLLECTION_THROUGHPUT = 400;
    private final int FEED_COLLECTION_THROUGHPUT_FOR_SPLIT = 10100;
    private final int LEASE_COLLECTION_THROUGHPUT = 400;
    private final String MULTI_WRITE_DATABASE_NAME = "multi-write-test-database" + UUID.randomUUID();
    private final String MULTI_WRITE_MONITORED_COLLECTION_NAME = "multi-write-test-monitored-container" + UUID.randomUUID();
    private final String MULTI_WRITE_LEASE_COLLECTION_NAME = "multi-write-test-lease-container" + UUID.randomUUID();

    private CosmosAsyncClient client;

    private ChangeFeedProcessor changeFeedProcessor;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public IncrementalChangeFeedProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "getCurrentStateTestConfigs")
    public Object[] getCurrentStateTestConfigs() {
        return new Object[] {
            FaultInjectionServerErrorType.PARTITION_IS_SPLITTING,
            FaultInjectionServerErrorType.GONE,
            FaultInjectionServerErrorType.PARTITION_IS_MIGRATING
        };
    }

    @DataProvider(name = "throughputControlArgProvider")
    public static Object[][] throughputControlArgProvider() {
        return new Object[][]{
            // throughput control enabled
            { true },
            { false }
        };
    }

    @Test(groups = {"query" }, timeOut = 2 * TIMEOUT)
    public void readFeedDocumentsStartFromBeginning() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocuments))
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

            changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

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

    @Test(groups = { "query" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsStartFromCustomDate() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges((List<ChangeFeedProcessorItem> docs) -> {
                    logger.info("START processing from thread {}", Thread.currentThread().getId());
                    for (ChangeFeedProcessorItem item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    logger.info("END processing from thread {}", Thread.currentThread().getId());
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
                changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .subscribe();
            } catch (Exception ex) {
                logger.error("Change feed processor did not start in the expected time", ex);
                throw ex;
            }

            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            // Wait for the feed processor to receive and process the documents.
            waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

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

    @Test(groups = {"multi-master", "multi-master-circuit-breaker"}, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsStartFromCustomDateForMultiWrite_test() throws InterruptedException {
        CosmosClientBuilder clientBuilder = getClientBuilder();

        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
        GlobalEndpointManager globalEndpointManager = rxDocumentClient.getGlobalEndpointManager();
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        Iterator<DatabaseAccountLocation> databaseAccountLocationIterator = databaseAccount.getWritableLocations().iterator();
        List<String> preferredRegions = new ArrayList<>();

        while (databaseAccountLocationIterator.hasNext()) {
            DatabaseAccountLocation databaseAccountLocation = databaseAccountLocationIterator.next();
            preferredRegions.add(databaseAccountLocation.getName());
        }

        assertThat(preferredRegions).isNotNull();
        assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(2);

        CosmosAsyncClient cosmosAsyncClient = clientBuilder
            .multipleWriteRegionsEnabled(true)
            .endpointDiscoveryEnabled(true)
            .preferredRegions(preferredRegions)
            .buildAsyncClient();

        CosmosAsyncContainer createdFeedCollection = null;
        CosmosAsyncContainer createdLeaseCollection = null;
        CosmosAsyncDatabase cosmosAsyncDatabase = null;

        try {

            cosmosAsyncClient.createDatabaseIfNotExists(MULTI_WRITE_DATABASE_NAME).block();
            cosmosAsyncDatabase = cosmosAsyncClient.getDatabase(MULTI_WRITE_DATABASE_NAME);
            cosmosAsyncDatabase.createContainerIfNotExists(MULTI_WRITE_MONITORED_COLLECTION_NAME, "/id", ThroughputProperties.createManualThroughput(400)).block();
            cosmosAsyncDatabase.createContainerIfNotExists(MULTI_WRITE_LEASE_COLLECTION_NAME, "/id", ThroughputProperties.createManualThroughput(400)).block();

            createdFeedCollection = cosmosAsyncDatabase.getContainer(MULTI_WRITE_MONITORED_COLLECTION_NAME);
            createdLeaseCollection = cosmosAsyncDatabase.getContainer(MULTI_WRITE_LEASE_COLLECTION_NAME);

            try {
                List<InternalObjectNode> createdDocuments = new ArrayList<>();
                Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
                setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

                // create a gap between previously written documents
                Thread.sleep(3000);

                ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                    .hostName(hostName)
                    .feedContainer(createdFeedCollection)
                    .leaseContainer(createdLeaseCollection)
                    .handleLatestVersionChanges((List<ChangeFeedProcessorItem> docs) -> {
                        logger.info("START processing from thread {}", Thread.currentThread().getId());
                        for (ChangeFeedProcessorItem item : docs) {
                            processItem(item, receivedDocuments);
                        }
                        logger.info("END processing from thread {}", Thread.currentThread().getId());
                    })
                    .options(new ChangeFeedProcessorOptions()
                        .setLeaseRenewInterval(Duration.ofSeconds(20))
                        .setLeaseAcquireInterval(Duration.ofSeconds(10))
                        .setLeaseExpirationInterval(Duration.ofSeconds(30))
                        .setFeedPollDelay(Duration.ofSeconds(1))
                        .setLeasePrefix("TEST")
                        .setMaxItemCount(10)
                        .setStartTime(ZonedDateTime.now(ZoneOffset.UTC).toInstant())
                        .setMinScaleCount(1)
                        .setMaxScaleCount(3)
                    )
                    .buildChangeFeedProcessor();

                try {
                    changeFeedProcessor.start().publishOn(Schedulers.boundedElastic()).subscribe();
                } catch (Exception ex) {
                    logger.error("Change feed processor did not start in the expected time", ex);
                    throw ex;
                }

                Thread.sleep(1000);
                List<InternalObjectNode> createdDocumentsAfterCFPStart = new ArrayList<>();
                setupReadFeedDocuments(createdDocumentsAfterCFPStart, receivedDocuments, createdFeedCollection, FEED_COUNT);

                // Wait for the feed processor to receive and process the documents.
                waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

                assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

                changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                assertThat(receivedDocuments.keySet().size()).isEqualTo(createdDocumentsAfterCFPStart.size());

                for (InternalObjectNode item : createdDocumentsAfterCFPStart) {
                    assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
                }

                // Wait for the feed processor to shutdown.
                Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
            }
            catch (Exception exception) {
                logger.error("Error in creating the ChangeFeedProcessor...");
            }
        } catch (Exception exception) {
            logger.error("Error in creating containers...");
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            safeDeleteDatabase(cosmosAsyncDatabase);
            safeClose(cosmosAsyncClient);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = {"multi-master", "multi-master-circuit-breaker"}, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsStartFromCustomDateForMultiWrite_WithCFPReadFromSatelliteRegion_test() throws InterruptedException {
        CosmosClientBuilder clientBuilder = getClientBuilder();

        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
        GlobalEndpointManager globalEndpointManager = rxDocumentClient.getGlobalEndpointManager();
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        Iterator<DatabaseAccountLocation> databaseAccountLocationIterator = databaseAccount.getWritableLocations().iterator();
        List<String> preferredRegions = new ArrayList<>();

        while (databaseAccountLocationIterator.hasNext()) {
            DatabaseAccountLocation databaseAccountLocation = databaseAccountLocationIterator.next();
            preferredRegions.add(databaseAccountLocation.getName());
        }

        assertThat(preferredRegions).isNotNull();
        assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(2);

        CosmosAsyncClient cosmosAsyncClientForLocalRegion = clientBuilder
            .multipleWriteRegionsEnabled(true)
            .endpointDiscoveryEnabled(true)
            .preferredRegions(preferredRegions)
            .buildAsyncClient();

        CosmosAsyncClient cosmosAsyncClientForSatelliteRegion = clientBuilder
            .multipleWriteRegionsEnabled(true)
            .endpointDiscoveryEnabled(true)
            .preferredRegions(Arrays.asList(preferredRegions.get(1)))
            .buildAsyncClient();


        CosmosAsyncContainer createdFeedCollectionLocalRegion = null;
        CosmosAsyncContainer createdLeaseCollectionLocalRegion = null;
        CosmosAsyncContainer createdFeedCollectionSatelliteRegion = null;
        CosmosAsyncContainer createdLeaseCollectionSatelliteRegion = null;
        CosmosAsyncDatabase cosmosAsyncDatabaseRegionOne = null;

        String dbId = UUID.randomUUID().toString();
        String feedCollectionId = UUID.randomUUID().toString();
        String leaseCollectionId = UUID.randomUUID().toString();

        try {

            cosmosAsyncClientForLocalRegion.createDatabaseIfNotExists(dbId).block();
            cosmosAsyncDatabaseRegionOne = cosmosAsyncClientForLocalRegion.getDatabase(dbId);
            cosmosAsyncDatabaseRegionOne.createContainerIfNotExists(feedCollectionId, "/id", ThroughputProperties.createManualThroughput(400)).block();
            cosmosAsyncDatabaseRegionOne.createContainerIfNotExists(leaseCollectionId, "/id", ThroughputProperties.createManualThroughput(400)).block();

            createdFeedCollectionLocalRegion = cosmosAsyncDatabaseRegionOne.getContainer(feedCollectionId);
            createdLeaseCollectionLocalRegion = cosmosAsyncDatabaseRegionOne.getContainer(leaseCollectionId);

            CosmosAsyncDatabase cosmosAsyncDatabaseRegionTwo = cosmosAsyncClientForSatelliteRegion.getDatabase(dbId);
            createdFeedCollectionSatelliteRegion = cosmosAsyncDatabaseRegionTwo.getContainer(feedCollectionId);
            createdLeaseCollectionSatelliteRegion = cosmosAsyncDatabaseRegionTwo.getContainer(leaseCollectionId);

            // allow some time to ensure collection is available for read
            Thread.sleep(5_000);

            try {

                List<InternalObjectNode> createdDocuments = new ArrayList<>();
                Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
                setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollectionLocalRegion, FEED_COUNT);

                // create a gap between previously written documents
                Thread.sleep(3000);

                ChangeFeedProcessor changeFeedProcessorSatelliteRegion = new ChangeFeedProcessorBuilder()
                    .hostName(hostName)
                    .feedContainer(createdFeedCollectionSatelliteRegion)
                    .leaseContainer(createdLeaseCollectionSatelliteRegion)
                    .handleLatestVersionChanges((List<ChangeFeedProcessorItem> docs) -> {
                        logger.info("START processing from thread {}", Thread.currentThread().getId());
                        for (ChangeFeedProcessorItem item : docs) {
                            processItem(item, receivedDocuments);
                        }
                        logger.info("END processing from thread {}", Thread.currentThread().getId());
                    })
                    .options(new ChangeFeedProcessorOptions()
                        .setLeaseRenewInterval(Duration.ofSeconds(20))
                        .setLeaseAcquireInterval(Duration.ofSeconds(10))
                        .setLeaseExpirationInterval(Duration.ofSeconds(30))
                        .setFeedPollDelay(Duration.ofSeconds(1))
                        .setLeasePrefix("TEST")
                        .setMaxItemCount(10)
                        .setStartTime(ZonedDateTime.now(ZoneOffset.UTC).toInstant())
                        .setMinScaleCount(1)
                        .setMaxScaleCount(3)
                    )
                    .buildChangeFeedProcessor();

                try {
                    changeFeedProcessorSatelliteRegion.start().publishOn(Schedulers.boundedElastic()).subscribe();
                } catch (Exception ex) {
                    logger.error("Change feed processor did not start in the expected time", ex);
                    throw ex;
                }

                // allow some time to ensure CFP instance has started
                Thread.sleep(1000);

                List<InternalObjectNode> createdDocumentsAfterCFPStart = new ArrayList<>();
                setupReadFeedDocuments(createdDocumentsAfterCFPStart, receivedDocuments, createdFeedCollectionLocalRegion, FEED_COUNT);

                Thread.sleep(REPLICA_IN_SATELLITE_REGION_CATCH_UP_TIME);

                // Wait for the feed processor to receive and process the documents.
                waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

                assertThat(changeFeedProcessorSatelliteRegion.isStarted()).as("Change Feed Processor instance is running").isTrue();

                changeFeedProcessorSatelliteRegion.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                for (InternalObjectNode item : createdDocumentsAfterCFPStart) {
                    assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
                }

                // Wait for the feed processor to shutdown.
                Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
            }
            catch (Exception exception) {
                logger.error("Error in creating the ChangeFeedProcessor...");
            }
        } finally {
            safeDeleteCollection(createdFeedCollectionLocalRegion);
            safeDeleteCollection(createdLeaseCollectionLocalRegion);
            safeDeleteDatabase(cosmosAsyncDatabaseRegionOne);
            safeClose(cosmosAsyncClientForLocalRegion);
            safeClose(cosmosAsyncClientForSatelliteRegion);
            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = {"multi-master", "multi-master-circuit-breaker"}, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsStartFromCustomDateForMultiWrite_WithCFPReadSwitchToSatelliteRegion_test() throws InterruptedException {
        CosmosClientBuilder clientBuilder = getClientBuilder();

        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);
        GlobalEndpointManager globalEndpointManager = rxDocumentClient.getGlobalEndpointManager();
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        Iterator<DatabaseAccountLocation> databaseAccountLocationIterator = databaseAccount.getWritableLocations().iterator();
        List<String> preferredRegions = new ArrayList<>();

        while (databaseAccountLocationIterator.hasNext()) {
            DatabaseAccountLocation databaseAccountLocation = databaseAccountLocationIterator.next();
            preferredRegions.add(databaseAccountLocation.getName());
        }

        assertThat(preferredRegions).isNotNull();
        assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(2);

        CosmosAsyncClient cosmosAsyncClientLocalRegion = clientBuilder
            .multipleWriteRegionsEnabled(true)
            .endpointDiscoveryEnabled(true)
            .preferredRegions(preferredRegions)
            .buildAsyncClient();

        CosmosAsyncClient cosmosAsyncClientRemoteRegion = clientBuilder
            .multipleWriteRegionsEnabled(true)
            .endpointDiscoveryEnabled(true)
            .preferredRegions(Arrays.asList(preferredRegions.get(1)))
            .buildAsyncClient();

        CosmosAsyncContainer createdFeedCollectionLocalRegion = null;
        CosmosAsyncContainer createdLeaseCollectionLocalRegion = null;
        CosmosAsyncContainer createdFeedCollectionSatelliteRegion = null;
        CosmosAsyncContainer createdLeaseCollectionSatelliteRegion = null;
        CosmosAsyncDatabase cosmosAsyncDatabaseRegionOne = null;

        String dbId = UUID.randomUUID().toString();
        String feedContainerId = UUID.randomUUID().toString();
        String leaseContainerId = UUID.randomUUID().toString();

        try {

            cosmosAsyncClientLocalRegion.createDatabaseIfNotExists(dbId).block();
            cosmosAsyncDatabaseRegionOne = cosmosAsyncClientLocalRegion.getDatabase(dbId);
            cosmosAsyncDatabaseRegionOne.createContainerIfNotExists(feedContainerId, "/id", ThroughputProperties.createManualThroughput(400)).block();
            cosmosAsyncDatabaseRegionOne.createContainerIfNotExists(leaseContainerId, "/id", ThroughputProperties.createManualThroughput(400)).block();

            createdFeedCollectionLocalRegion = cosmosAsyncDatabaseRegionOne.getContainer(feedContainerId);
            createdLeaseCollectionLocalRegion = cosmosAsyncDatabaseRegionOne.getContainer(leaseContainerId);

            CosmosAsyncDatabase cosmosAsyncDatabaseRegionTwo = cosmosAsyncClientRemoteRegion.getDatabase(dbId);
            createdFeedCollectionSatelliteRegion = cosmosAsyncDatabaseRegionTwo.getContainer(feedContainerId);
            createdLeaseCollectionSatelliteRegion = cosmosAsyncDatabaseRegionTwo.getContainer(leaseContainerId);

            // allow some time to ensure collection is available for read
            Thread.sleep(5_000);

            try {
                Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
                Instant cfpStartTimeSnapshot = ZonedDateTime.now(ZoneOffset.UTC).toInstant();

                ChangeFeedProcessor changeFeedProcessorLocalRegion = new ChangeFeedProcessorBuilder()
                    .hostName(RandomStringUtils.randomAlphabetic(6))
                    .feedContainer(createdFeedCollectionLocalRegion)
                    .leaseContainer(createdLeaseCollectionLocalRegion)
                    .handleLatestVersionChanges((List<ChangeFeedProcessorItem> docs) -> {
                        logger.info("START processing from thread {}", Thread.currentThread().getId());
                        for (ChangeFeedProcessorItem item : docs) {
                            processItem(item, receivedDocuments);
                        }
                        logger.info("END processing from thread {}", Thread.currentThread().getId());
                    })
                    .options(new ChangeFeedProcessorOptions()
                        .setLeaseRenewInterval(Duration.ofSeconds(20))
                        .setLeaseAcquireInterval(Duration.ofSeconds(10))
                        .setLeaseExpirationInterval(Duration.ofSeconds(30))
                        .setFeedPollDelay(Duration.ofSeconds(1))
                        .setLeasePrefix("TEST-1")
                        .setMaxItemCount(10)
                        .setStartTime(cfpStartTimeSnapshot)
                        .setMinScaleCount(1)
                        .setMaxScaleCount(3)
                    )
                    .buildChangeFeedProcessor();

                ChangeFeedProcessor changeFeedProcessorSatelliteRegion = new ChangeFeedProcessorBuilder()
                    .hostName(RandomStringUtils.randomAlphabetic(6))
                    .feedContainer(createdFeedCollectionSatelliteRegion)
                    .leaseContainer(createdLeaseCollectionSatelliteRegion)
                    .handleLatestVersionChanges((List<ChangeFeedProcessorItem> docs) -> {
                        logger.info("START processing from thread {}", Thread.currentThread().getId());
                        for (ChangeFeedProcessorItem item : docs) {
                            processItem(item, receivedDocuments);
                        }
                        logger.info("END processing from thread {}", Thread.currentThread().getId());
                    })
                    .options(new ChangeFeedProcessorOptions()
                        .setLeaseRenewInterval(Duration.ofSeconds(20))
                        .setLeaseAcquireInterval(Duration.ofSeconds(10))
                        .setLeaseExpirationInterval(Duration.ofSeconds(30))
                        .setFeedPollDelay(Duration.ofSeconds(1))
                        .setLeasePrefix("TEST-2")
                        .setMaxItemCount(10)
                        .setStartTime(cfpStartTimeSnapshot)
                        .setMinScaleCount(1)
                        .setMaxScaleCount(3)
                    )
                    .buildChangeFeedProcessor();

                try {
                    // enforce both CFP instances are started
                    changeFeedProcessorLocalRegion
                        .start()
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .publishOn(Schedulers.boundedElastic())
                        .block();
                    changeFeedProcessorSatelliteRegion
                        .start()
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .publishOn(Schedulers.boundedElastic())
                        .block();
                } catch (Exception ex) {
                    logger.error("Change feed processor did not start in the expected time", ex);
                    throw ex;
                }

                assertThat(changeFeedProcessorLocalRegion.isStarted()).as("Change feed processor instance is running...").isTrue();
                assertThat(changeFeedProcessorSatelliteRegion.isStarted()).as("Change feed processor instance is running...").isTrue();

                List<InternalObjectNode> createdDocumentsAfterCFPStart = new ArrayList<>();
                setupReadFeedDocuments(createdDocumentsAfterCFPStart, receivedDocuments, createdFeedCollectionLocalRegion, FEED_COUNT);

                // abruptly initiate stopping the CFP instance for the local region
                changeFeedProcessorLocalRegion.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                Thread.sleep(REPLICA_IN_SATELLITE_REGION_CATCH_UP_TIME);

                // Wait for the feed processor to receive and process the documents.
                waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

                setupReadFeedDocuments(createdDocumentsAfterCFPStart, receivedDocuments, createdFeedCollectionLocalRegion, FEED_COUNT);

                Thread.sleep(REPLICA_IN_SATELLITE_REGION_CATCH_UP_TIME);
                // Wait for the feed processor to receive and process the documents.
                waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, 2 * FEED_COUNT);

                changeFeedProcessorSatelliteRegion.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                for (InternalObjectNode item : createdDocumentsAfterCFPStart) {
                    assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
                }

                // Wait for the feed processor to shutdown.
                Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
            }
            catch (Exception exception) {
                logger.error("Error in creating the ChangeFeedProcessor...");
            }
        } finally {
            safeDeleteCollection(createdFeedCollectionLocalRegion);
            safeDeleteCollection(createdLeaseCollectionLocalRegion);
            safeDeleteDatabase(cosmosAsyncDatabaseRegionOne);
            safeClose(cosmosAsyncClientLocalRegion);
            safeClose(cosmosAsyncClientRemoteRegion);
            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "query" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getCurrentState() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessorMain = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges((List<ChangeFeedProcessorItem> docs) -> {
                    logger.info("START processing from thread {}", Thread.currentThread().getId());
                    for (ChangeFeedProcessorItem item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    logger.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new ChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .handleLatestVersionChanges((List<ChangeFeedProcessorItem> docs) -> {
                    fail("ERROR - we should not execute this handler");
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessorMain.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .then(Mono.just(changeFeedProcessorMain)
                        .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .flatMap(value -> changeFeedProcessorMain.stop()
                            .subscribeOn(Schedulers.boundedElastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        ))
                    .subscribe();
            } catch (Exception ex) {
                logger.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            List<ChangeFeedProcessorState> cfpCurrentState = changeFeedProcessorMain.getCurrentState()
                .map(state -> {
                    try {
                        logger.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        logger.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            assertThat(cfpCurrentState.size()).isNotZero().as("Change Feed Processor number of leases should not be 0.");

            int totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Main estimated total lag at start");

            // check the side cart CFP instance
            List<ChangeFeedProcessorState> cfpCurrentStateSideCart = changeFeedProcessorSideCart.getCurrentState()
                .map(state -> {
                    try {
                        logger.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        logger.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            assertThat(cfpCurrentStateSideCart.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentStateSideCart) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Side Cart estimated total lag at start");


            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            cfpCurrentState = changeFeedProcessorMain.getCurrentState()
                .map(state -> {
                    try {
                        logger.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        logger.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            cfpCurrentStateSideCart = changeFeedProcessorSideCart.getCurrentState()
                .map(state -> {
                    try {
                        logger.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        logger.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            assertThat(cfpCurrentStateSideCart.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentStateSideCart) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Side Cart estimated total lag");


        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "query" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
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
                .handleLatestVersionChanges(docs -> {
                    logger.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                    logger.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix(leasePrefix)
                )
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSecond = new ChangeFeedProcessorBuilder()
                .hostName(ownerSecond)
                .handleLatestVersionChanges((List<ChangeFeedProcessorItem> docs) -> {
                    logger.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                    for (ChangeFeedProcessorItem item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    logger.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
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
                changeFeedProcessorFirst.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .then(Mono.just(changeFeedProcessorFirst)
                        .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .flatMap(value -> changeFeedProcessorFirst.stop()
                            .subscribeOn(Schedulers.boundedElastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        ))
                    .doOnSuccess(aVoid -> {
                        try {
                            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Interrupted exception", e);
                        }
                        logger.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");

                        SqlParameter param = new SqlParameter();
                        param.setName("@PartitionLeasePrefix");
                        param.setValue(leasePrefix);
                        SqlQuerySpec querySpec = new SqlQuerySpec(
                            "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)", Collections.singletonList(param));

                        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

                        createdLeaseCollection.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class).byPage()
                            .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.getResults()))
                            .flatMap(doc -> {
                                ServiceItemLeaseV1 leaseDocument = ServiceItemLeaseV1.fromDocument(doc);
                                leaseDocument.setOwner("TEMP_OWNER");
                                CosmosItemRequestOptions options = new CosmosItemRequestOptions();

                                return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                                    .map(CosmosItemResponse::getItem);
                            })
                            .map(leaseDocument -> {
                                logger.info("QueryItems after Change feed processor processing; found host {}", leaseDocument.getOwner());
                                return leaseDocument;
                            })
                            .last()
                            .flatMap(leaseDocument -> {
                                logger.info("Start creating documents");
                                List<InternalObjectNode> docDefList = new ArrayList<>();

                                for (int i = 0; i < FEED_COUNT; i++) {
                                    docDefList.add(getDocumentDefinition());
                                }

                                return bulkInsert(createdFeedCollection, docDefList, FEED_COUNT)
                                    .last()
                                    .delayElement(Duration.ofMillis(1000))
                                    .flatMap(cosmosItemResponse -> {
                                        logger.info("Start second Change feed processor");
                                        return changeFeedProcessorSecond.start().subscribeOn(Schedulers.boundedElastic())
                                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT));
                                    });
                            })
                            .subscribe();
                    })
                    .subscribe();
            } catch (Exception ex) {
                logger.error("First change feed processor did not start in the expected time", ex);
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

            changeFeedProcessorSecond.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            // Wait for the feed processor to shutdown.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "query" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void ownerNullAcquiring() throws InterruptedException {
        final String ownerFirst = "Owner_First";
        final String leasePrefix = "TEST";
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

            ChangeFeedProcessor changeFeedProcessorFirst = new ChangeFeedProcessorBuilder()
                .hostName(ownerFirst)
                .handleLatestVersionChanges(docs -> {
                    logger.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                    for (ChangeFeedProcessorItem item : docs) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                        processItem(item, receivedDocuments);
                    }
                    logger.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setStartFromBeginning(true)
                    .setLeasePrefix(leasePrefix)
                    .setLeaseRenewInterval(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setLeaseAcquireInterval(Duration.ofMillis(5 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setLeaseExpirationInterval(Duration.ofMillis(6 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setFeedPollDelay(Duration.ofSeconds(5))
                )
                .buildChangeFeedProcessor();

            try {
                logger.info("Start more creating documents");
                List<InternalObjectNode> docDefList = new ArrayList<>();

                for (int i = 0; i < FEED_COUNT; i++) {
                    docDefList.add(getDocumentDefinition());
                }

                bulkInsert(createdFeedCollection, docDefList, FEED_COUNT)
                    .last()
                    .flatMap(cosmosItemResponse -> {
                        logger.info("Start first Change feed processor");
                        return changeFeedProcessorFirst.start().subscribeOn(Schedulers.boundedElastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT));
                    })
                    .then(
                        Mono.just(changeFeedProcessorFirst)
                            .flatMap( value -> {
                                logger.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");
                                try {
                                    Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
                                } catch (InterruptedException ignored) {
                                }

                                logger.info("QueryItems before Change feed processor processing");

                                SqlParameter param1 = new SqlParameter();
                                param1.setName("@PartitionLeasePrefix");
                                param1.setValue(leasePrefix);
                                SqlParameter param2 = new SqlParameter();
                                param2.setName("@Owner");
                                param2.setValue(ownerFirst);

                                SqlQuerySpec querySpec = new SqlQuerySpec(
                                    "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix) AND c.Owner=@Owner", Arrays.asList(param1, param2));

                                CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

                                return createdLeaseCollection.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class).byPage()
                                    .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.getResults()))
                                    .flatMap(doc -> {
                                        ServiceItemLeaseV1 leaseDocument = ServiceItemLeaseV1.fromDocument(doc);
                                        leaseDocument.setOwner(null);
                                        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                                        return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                                            .map(CosmosItemResponse::getItem);
                                    })
                                    .map(leaseDocument -> {
                                        logger.info("QueryItems after Change feed processor processing; current Owner is'{}'", leaseDocument.getOwner());
                                        return leaseDocument;
                                    })
                                    .last()
                                    .flatMap(leaseDocument -> {
                                        logger.info("Start creating more documents");
                                        List<InternalObjectNode> docDefList1 = new ArrayList<>();

                                        for (int i = 0; i < FEED_COUNT; i++) {
                                            docDefList1.add(getDocumentDefinition());
                                        }

                                        return bulkInsert(createdFeedCollection, docDefList1, FEED_COUNT)
                                            .last();
                                    });
                            }))
                    .subscribe();
            } catch (Exception ex) {
                logger.error("First change feed processor did not start in the expected time", ex);
                throw ex;
            }

            long remainingWork = 20 * CHANGE_FEED_PROCESSOR_TIMEOUT;
            while (remainingWork > 0 && !changeFeedProcessorFirst.isStarted()) {
                remainingWork -= 100;
                Thread.sleep(100);
            }

            // Wait for the feed processor to receive and process the documents.
            waitToReceiveDocuments(receivedDocuments, 30 * CHANGE_FEED_PROCESSOR_TIMEOUT, 2 * FEED_COUNT);

            assertThat(changeFeedProcessorFirst.isStarted()).as("Change Feed Processor instance is running").isTrue();

            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            changeFeedProcessorFirst.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            // Wait for the feed processor to shutdown.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "cfp-split" }, dataProvider = "throughputControlArgProvider", timeOut = 160 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsAfterSplit(boolean throughputControlEnabled) throws InterruptedException {
        CosmosAsyncContainer createdFeedCollectionForSplit = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(2 * LEASE_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseMonitorCollection = createLeaseMonitorCollection(LEASE_COLLECTION_THROUGHPUT);

        CosmosAsyncClient clientWithStaleCache = null;

        try {

            clientWithStaleCache = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();

            CosmosAsyncDatabase databaseFromStaleClient =
                clientWithStaleCache.getDatabase(createdFeedCollectionForSplit.getDatabase().getId());
            CosmosAsyncContainer feedCollectionFromStaleClient =
                databaseFromStaleClient.getContainer(createdFeedCollectionForSplit.getId());
            CosmosAsyncContainer leaseCollectionFromStaleClient =
                databaseFromStaleClient.getContainer(createdLeaseCollection.getId());

            ChangeFeedProcessor staleChangeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .feedContainer(feedCollectionFromStaleClient)
                .leaseContainer(leaseCollectionFromStaleClient)
                .handleLatestVersionChanges(changeFeedProcessorItems -> {
                })
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(100)
                    .setLeaseExpirationInterval(Duration.ofMillis(10 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setFeedPollDelay(Duration.ofMillis(200))
                )
                .buildChangeFeedProcessor();

            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            LeaseStateMonitor leaseStateMonitor = new LeaseStateMonitor();

            // create a monitoring CFP for ensuring the leases are updating as expected
            ChangeFeedProcessor leaseMonitoringChangeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(leasesChangeFeedProcessorHandler(leaseStateMonitor))
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

            ChangeFeedProcessorOptions changeFeedProcessorOptions =
                new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(10)
                    .setLeaseRenewInterval(Duration.ofSeconds(2));
            if (throughputControlEnabled) {
                changeFeedProcessorOptions.setFeedPollThroughputControlConfig(
                    new ThroughputControlGroupConfigBuilder()
                        .groupName("splitTest-" + UUID.randomUUID())
                        .targetThroughputThreshold(1.0)
                        .build()
                );
            }
            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollectionForSplit)
                .leaseContainer(createdLeaseCollection)
                .options(changeFeedProcessorOptions)
                .buildChangeFeedProcessor();

            leaseMonitoringChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(200 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .onErrorResume(throwable -> {
                    logger.error("Change feed processor for lease monitoring did not start in the expected time", throwable);
                    return Mono.error(throwable);
                })
                .then(
                    changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .onErrorResume(throwable -> {
                            logger.error("Change feed processor did not start in the expected time", throwable);
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
                                .readThroughput().subscribeOn(Schedulers.boundedElastic())
                                .flatMap(currentThroughput ->
                                    createdFeedCollectionForSplit
                                        .replaceThroughput(ThroughputProperties.createManualThroughput(FEED_COLLECTION_THROUGHPUT_FOR_SPLIT))
                                        .subscribeOn(Schedulers.boundedElastic())
                                )
                                .onErrorResume(throwable -> {
                                    System.out.println(throwable);
                                    return Mono.empty();
                                })
                                .then()
                        )
                )
                .subscribe();

            // this call populates the pkRangeCache being the first data-plane request
            // this will force using a stale pkRangeCache
            // in the getCurrentState call after the split
            staleChangeFeedProcessor.getCurrentState().block();

            // Wait for the feed processor to receive and process the first batch of documents and apply throughput change.
            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);


            // Retrieve the latest continuation token value.
            long continuationToken = Long.MAX_VALUE;
            for (JsonNode item : leaseStateMonitor.receivedLeases.values()) {
                JsonNode tempToken = item.get("ContinuationToken");

                long continuationTokenValue = 0;
                if (tempToken != null && StringUtils.isNotEmpty(tempToken.asText())) {
                    ChangeFeedState changeFeedState = ChangeFeedState.fromString(tempToken.asText());
                    continuationTokenValue =
                        Long.parseLong(changeFeedState.getContinuation().getCurrentContinuationToken().getToken().replace("\"", ""));
                }
                if (tempToken == null || continuationTokenValue == 0) {
                    logger.error("Found unexpected lease with continuation token value of null or 0");
                    try {
                        logger.info("ERROR LEASE FOUND {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                    } catch (JsonProcessingException e) {
                        logger.error("Failure in processing json [{}]", e.getMessage(), e);
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
                logger.error("Could not find any valid lease documents");
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
            Flux.just(1).subscribeOn(Schedulers.boundedElastic())
                .flatMap(value -> {
                    logger.warn("Reading current throughput change.");
                    return contextClient.readPartitionKeyRanges(partitionKeyRangesPath, (CosmosQueryRequestOptions) null);
                })
                .map(partitionKeyRangeFeedResponse -> {
                    int count = partitionKeyRangeFeedResponse.getResults().size();

                    if (count < 2) {
                        logger.warn("Throughput change is pending.");
                        throw new RuntimeException("Throughput change is not done.");
                    }
                    return count;
                })
                // this will timeout approximately after 30 minutes
                .retryWhen(Retry.max(40).filter(throwable -> {
                    try {
                        logger.warn("Retrying...");
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

            changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();
            leaseMonitoringChangeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            int leaseCount = changeFeedProcessor.getCurrentState() .map(List::size).block();
            assertThat(leaseCount > 1).as("Found %d leases", leaseCount).isTrue();

            int leaseCountFromStaleCfp = staleChangeFeedProcessor.getCurrentState().map(List::size).block();
            assertThat(leaseCountFromStaleCfp).isEqualTo(leaseCount);

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
            System.out.println("Start to delete FeedCollectionForSplit");
            safeDeleteCollection(createdFeedCollectionForSplit);
            safeDeleteCollection(createdLeaseCollection);
            safeClose(clientWithStaleCache);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "cfp-split" }, timeOut = 160 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsAfterSplit_maxScaleCount() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollectionForSplit = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(2 * LEASE_COLLECTION_THROUGHPUT);

        ChangeFeedProcessor changeFeedProcessor1;
        ChangeFeedProcessor changeFeedProcessor2;
        String changeFeedProcessor1HostName = RandomStringUtils.randomAlphabetic(6);
        String changeFeedProcessor2HostName = RandomStringUtils.randomAlphabetic(6);

        try {
            // Set up the maxScaleCount to be equal to the current partition count
            int partitionCountBeforeSplit = createdFeedCollectionForSplit.getFeedRanges().block().size();
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

            // generate a first batch of documents
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            changeFeedProcessor1 = new ChangeFeedProcessorBuilder()
                .hostName(changeFeedProcessor1HostName)
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollectionForSplit)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(10)
                    .setLeaseAcquireInterval(Duration.ofSeconds(1))
                    .setMaxScaleCount(partitionCountBeforeSplit) // set to match the partition count
                    .setLeaseRenewInterval(Duration.ofSeconds(2))
                )
                .buildChangeFeedProcessor();

            changeFeedProcessor1
                .start()
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .onErrorResume(throwable -> {
                    logger.error("Change feed processor did not start in the expected time", throwable);
                    return Mono.error(throwable);
                })
                .block();

            // Wait for the feed processor to receive and process the second batch of documents.
            waitToReceiveDocuments(receivedDocuments, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            // increase throughput to force a single partition collection to go through a split
            createdFeedCollectionForSplit
                .readThroughput()
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(currentThroughput ->
                    createdFeedCollectionForSplit
                        .replaceThroughput(ThroughputProperties.createManualThroughput(FEED_COLLECTION_THROUGHPUT_FOR_SPLIT))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .block();

            // wait for the split to finish
            ThroughputResponse throughputResponse = createdFeedCollectionForSplit.readThroughput().block();
            while (true) {
                assert throughputResponse != null;
                if (!throughputResponse.isReplacePending()) {
                    break;
                }
                logger.info("Waiting for split to complete");
                Thread.sleep(10 * 1000);
                throughputResponse = createdFeedCollectionForSplit.readThroughput().block();
            }

            // generate the second batch of documents
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            // wait for the change feed processor to receive some documents
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            String leaseQuery = "select * from c where not contains(c.id, \"info\")";
            List<JsonNode> leaseDocuments =
                createdLeaseCollection
                    .queryItems(leaseQuery, JsonNode.class)
                    .byPage()
                    .blockFirst()
                    .getResults();

            long host1Leases = leaseDocuments.stream().filter(lease -> lease.get("Owner").asText().equals(changeFeedProcessor1HostName)).count();
            assertThat(host1Leases).isEqualTo(partitionCountBeforeSplit);

            // now starts a new change feed processor
            changeFeedProcessor2 = new ChangeFeedProcessorBuilder()
                .hostName(changeFeedProcessor2HostName)
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollectionForSplit)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(10)
                    .setLeaseAcquireInterval(Duration.ofSeconds(1))
                    .setMaxScaleCount(partitionCountBeforeSplit) // set to match the partition count
                    .setLeaseRenewInterval(Duration.ofSeconds(2))
                )
                .buildChangeFeedProcessor();

            changeFeedProcessor2
                .start()
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .onErrorResume(throwable -> {
                    logger.error("Change feed processor did not start in the expected time", throwable);
                    return Mono.error(throwable);
                })
                .subscribe();

            // Wait for the feed processor to receive and process the second batch of documents.
            waitToReceiveDocuments(receivedDocuments, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT*2);

            changeFeedProcessor1
                .stop()
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .onErrorResume(throwable -> {
                    logger.error("Change feed processor1 did not stop in the expected time", throwable);
                    return Mono.empty();
                })
                .block();
            changeFeedProcessor2
                .stop()
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .onErrorResume(throwable -> {
                    logger.error("Change feed processor2 did not stop in the expected time", throwable);
                    return Mono.empty();
                })
                .block();

        } finally {
            safeDeleteCollection(createdFeedCollectionForSplit);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "query" }, timeOut = 20 * TIMEOUT)
    public void inactiveOwnersRecovery() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            String leasePrefix = "TEST";

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(1))
                    .setLeaseAcquireInterval(Duration.ofSeconds(1))
                    .setLeaseExpirationInterval(Duration.ofSeconds(5))
                    .setFeedPollDelay(Duration.ofSeconds(1))
                    .setLeasePrefix(leasePrefix)
                    .setMaxItemCount(100)
                    .setStartFromBeginning(true)
                    .setMaxScaleCount(0) // unlimited
                    //.setScheduler(Schedulers.boundedElastic())
                    .setScheduler(Schedulers.newParallel("CFP parallel",
                        10 * Schedulers.DEFAULT_POOL_SIZE,
                        true))
                )
                .buildChangeFeedProcessor();

            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments,2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);

            logger.info("Update leases with random owners");

            SqlParameter param1 = new SqlParameter();
            param1.setName("@PartitionLeasePrefix");
            param1.setValue(leasePrefix);

            SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)", Arrays.asList(param1));

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            createdLeaseCollection.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class).byPage()
                .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.getResults()))
                .flatMap(doc -> {
                    ServiceItemLeaseV1 leaseDocument = ServiceItemLeaseV1.fromDocument(doc);
                    leaseDocument.setOwner(RandomStringUtils.randomAlphabetic(10));
                    CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                    return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                        .map(CosmosItemResponse::getItem);
                })
                .flatMap(leaseDocument -> createdLeaseCollection.readItem(leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), InternalObjectNode.class))
                .map(doc -> {
                    ServiceItemLeaseV1 leaseDocument = ServiceItemLeaseV1.fromDocument(doc.getItem());
                    logger.info("Change feed processor current Owner is'{}'", leaseDocument.getOwner());
                    return leaseDocument;
                })
                .blockLast();

            createdDocuments.clear();
            receivedDocuments.clear();
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 2 * TIMEOUT)
    public void readFeedDocuments_pollDelay() throws InterruptedException {
        // This test is used to test that changeFeedProcessor will keep exhausting all available changes before delay based on pollDelay
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);
        int maxItemCount = 1;
        int pollDelayInSeconds = 10;
        int feedCount = 2;

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            List<Instant> invokeTimeList = new ArrayList<>();
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, feedCount);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(
                    changeFeedProcessorHandlerWithCallback(
                        receivedDocuments,
                        () -> invokeTimeList.add(Instant.now())))
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(20))
                    .setLeaseAcquireInterval(Duration.ofSeconds(10))
                    .setLeaseExpirationInterval(Duration.ofSeconds(30))
                    .setFeedPollDelay(Duration.ofSeconds(pollDelayInSeconds)) // use a relative high value here
                    .setLeasePrefix("TEST")
                    .setMaxItemCount(maxItemCount) // use 1 here so that it requires two pages to read all changes
                    .setStartFromBeginning(true)
                    .setMaxScaleCount(0) // unlimited
                )
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

            // assert the time between each invoke time is less than pollDelay
            assertThat(invokeTimeList.size()).isGreaterThanOrEqualTo(feedCount / maxItemCount);
            for (int i = 1; i < invokeTimeList.size(); i++) {
                long timeDifferenceInMillis = Duration.between(invokeTimeList.get(i - 1), invokeTimeList.get(i)).toMillis();
                assertThat(timeDifferenceInMillis).isLessThan(pollDelayInSeconds * 1000);
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

    @Test(groups = {"query" }, timeOut = 2 * TIMEOUT)
    public void endToEndTimeoutConfigShouldBeSuppressed() throws InterruptedException {
        CosmosAsyncClient clientWithE2ETimeoutConfig = null;
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            clientWithE2ETimeoutConfig = this.getClientBuilder()
                .endToEndOperationLatencyPolicyConfig(new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofMillis(1)).build())
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();

            CosmosAsyncDatabase testDatabase = clientWithE2ETimeoutConfig.getDatabase(this.createdDatabase.getId());
            CosmosAsyncContainer createdFeedCollectionDuplicate = testDatabase.getContainer(createdFeedCollection.getId());
            CosmosAsyncContainer createdLeaseCollectionDuplicate = testDatabase.getContainer(createdLeaseCollection.getId());

            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollectionDuplicate)
                .leaseContainer(createdLeaseCollectionDuplicate)
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

            changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            // reset the endToEnd config
            this.getClientBuilder().endToEndOperationLatencyPolicyConfig(null);
            safeClose(clientWithE2ETimeoutConfig);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = {"query"}, dataProvider = "getCurrentStateTestConfigs")
    public void getCurrentStateWithFaultInjection(FaultInjectionServerErrorType faultInjectionServerErrorType) throws InterruptedException {

        if (BridgeInternal.getContextClient(this.client).getConnectionPolicy().getConnectionMode()
            == ConnectionMode.GATEWAY) {
            throw new SkipException("Fault injected is not valid in the gateway connectivity mode.");
        }

        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {

            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocuments))
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

            changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            FaultInjectionServerErrorResult serverErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(faultInjectionServerErrorType)
                .build();

            FaultInjectionRuleBuilder faultInjectionRuleBuilder = new FaultInjectionRuleBuilder("faultInjectionRule-" + UUID.randomUUID());

            FaultInjectionCondition faultInjectionConditionForRegion = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_FEED_ITEM)
                .build();

            FaultInjectionRule faultInjectionRule = faultInjectionRuleBuilder
                .condition(faultInjectionConditionForRegion)
                .result(serverErrorResult)
                .duration(Duration.ofSeconds(10))
                .build();

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(createdFeedCollection, Arrays.asList(faultInjectionRule))
                .block();

            int leaseCount = changeFeedProcessor.getCurrentState().map(List::size).block();

            assertThat(leaseCount).isEqualTo(1);
            assertThat(faultInjectionRule.getHitCount()).isGreaterThanOrEqualTo(1);

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

    @Test(groups = {"query" }, timeOut = 2 * TIMEOUT)
    public void readFeedDocumentsWithThroughputControl() throws InterruptedException {
        // Create a separate client as throughput control group will be applied to it
        CosmosAsyncClient clientWithThroughputControl =
            getClientBuilder()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();

        CosmosAsyncDatabase database = clientWithThroughputControl.getDatabase(this.createdDatabase.getId());

        CosmosAsyncContainer createdFeedCollection = createFeedCollection(database, 4000); // using a large value as we plan to create more docs
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(database, LEASE_COLLECTION_THROUGHPUT);

        ThroughputControlGroupConfig throughputControlGroupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("changeFeedProcessor")
                .targetThroughput(1)
                .build();

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

            int maxItemCount = 100; // force the RU usage per requests > 1
            int feedCount = maxItemCount * 2; // force to do two fetches
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, feedCount);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(20))
                    .setLeaseAcquireInterval(Duration.ofSeconds(10))
                    .setLeaseExpirationInterval(Duration.ofSeconds(30))
                    .setFeedPollDelay(Duration.ofSeconds(2))
                    .setFeedPollThroughputControlConfig(throughputControlGroupConfig)
                    .setLeasePrefix("TEST")
                    .setMaxItemCount(maxItemCount)
                    .setStartFromBeginning(true)
                    .setMaxScaleCount(0) // unlimited
                )
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

            changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            safeClose(clientWithThroughputControl);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    void validateChangeFeedProcessing(ChangeFeedProcessor changeFeedProcessor, List<InternalObjectNode> createdDocuments, Map<String, JsonNode> receivedDocuments, int sleepTime) throws InterruptedException {
        try {
            changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .subscribe();
        } catch (Exception ex) {
            logger.error("Change feed processor did not start in the expected time", ex);
            throw ex;
        }

        // Wait for the feed processor to receive and process the documents.
        Thread.sleep(sleepTime);

        assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

        List<ChangeFeedProcessorState> cfpCurrentState = changeFeedProcessor
            .getCurrentState()
            .map(state -> {
                try {
                    logger.info(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(state));
                } catch (JsonProcessingException ex) {
                    logger.error("Unexpected", ex);
                }
                return state;
            })
            .block();

        assertThat(cfpCurrentState).isNotNull().as("Change Feed Processor current state");

        for (ChangeFeedProcessorState item : cfpCurrentState) {
            assertThat(item.getHostName()).isEqualTo(hostName).as("Change Feed Processor ownership");
        }

        changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

        for (InternalObjectNode item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
        }
    }

    private Consumer<List<ChangeFeedProcessorItem>> changeFeedProcessorHandler(Map<String, JsonNode> receivedDocuments) {
        return docs -> {
            logger.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                processItem(item, receivedDocuments);
            }
            logger.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private Consumer<List<ChangeFeedProcessorItem>> changeFeedProcessorHandlerWithCallback(
        Map<String, JsonNode> receivedDocuments,
        Callable<Boolean> callBackFunc) {

        return docs -> {

            if (callBackFunc != null) {
                try {
                    callBackFunc.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            logger.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                processItem(item, receivedDocuments);
            }
            logger.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private void waitToReceiveDocuments(Map<String, JsonNode> receivedDocuments, long timeoutInMillisecond, long count) throws InterruptedException {
        long remainingWork = timeoutInMillisecond;
        while (remainingWork > 0 && receivedDocuments.size() < count) {
            remainingWork -= 100;
            Thread.sleep(100);
        }

        assertThat(remainingWork > 0).as("Failed to receive all the feed documents").isTrue();
    }

    private Consumer<List<ChangeFeedProcessorItem>> leasesChangeFeedProcessorHandler(LeaseStateMonitor leaseStateMonitor) {
        return docs -> {
            logger.info("LEASES processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                try {
                    logger
                        .debug("LEASE RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                } catch (JsonProcessingException e) {
                    logger.error("Failure in processing json [{}]", e.getMessage(), e);
                }

                if (item.getCurrent() == null) {
                    try {
                        System.out.println("how possible " + Utils.getSimpleObjectMapper().writeValueAsString(item));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
                JsonNode leaseToken = item.getCurrent().get("LeaseToken");

                if (leaseToken != null) {
                    JsonNode continuationTokenNode = item.getCurrent().get("ContinuationToken");
                    if (continuationTokenNode == null) {
                        // Something catastrophic went wrong and the lease is malformed.
                        logger.error("Found invalid lease document");
                        leaseStateMonitor.isContinuationTokenAdvancing = false;
                    }
                    else {
                        logger.info("LEASE {} with continuation {}", leaseToken.asText(), continuationTokenNode.asText());
                        if (leaseStateMonitor.isAfterLeaseInitialization) {
                            ChangeFeedState changeFeedState = ChangeFeedState.fromString(continuationTokenNode.asText());
                            long continuationToken = Long.parseLong(changeFeedState.getContinuation().getCurrentContinuationToken().getToken().replace("\"", ""));
                            if (leaseStateMonitor.parentContinuationToken > continuationToken) {
                                logger.error("Found unexpected continuation token that did not advance after the split; parent: {}, current: {}");
                                leaseStateMonitor.isContinuationTokenAdvancing = false;
                            }
                        }
                    }
                    leaseStateMonitor.receivedLeases.put(item.getCurrent().get("id").asText(), item.getCurrent());
                }
            }
            logger.info("LEASES processing from thread {}", Thread.currentThread().getId());
        };
    }

    @BeforeMethod(groups = { "query", "cfp-split" }, timeOut = 2 * SETUP_TIMEOUT, alwaysRun = true)
    public void beforeMethod() {
    }

    @BeforeClass(groups = { "query", "cfp-split" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_ChangeFeedProcessorTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterMethod(groups = { "query", "cfp-split" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterMethod() {
    }

    @AfterClass(groups = { "query", "cfp-split" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
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
        return createFeedCollection(createdDatabase, provisionedThroughput);
    }

    private CosmosAsyncContainer createFeedCollection(CosmosAsyncDatabase database, int provisionedThroughput) {
        CosmosContainerRequestOptions optionsFeedCollection = new CosmosContainerRequestOptions();
        return createCollection(database, getCollectionDefinition(), optionsFeedCollection, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseCollection(int provisionedThroughput) {
        return createLeaseCollection(createdDatabase, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseCollection(CosmosAsyncDatabase database, int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "leases_" + UUID.randomUUID(),
            "/id");
        return createCollection(database, collectionDefinition, options, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseMonitorCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "monitor_" + UUID.randomUUID(),
            "/id");
        return createCollection(createdDatabase, collectionDefinition, options, provisionedThroughput);
    }

    private static synchronized void processItem(ChangeFeedProcessorItem item, Map<String, JsonNode> receivedDocuments) {
        try {
            logger.info("RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            logger.error("Failure in processing json [{}]", e.getMessage(), e);
        }
        if (item.getCurrent() == null) {
            try {
                System.out.println("Null result " + Utils.getSimpleObjectMapper().writeValueAsString(item));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        receivedDocuments.put(item.getCurrent().get("id").asText(), item.getCurrent());
    }

    class LeaseStateMonitor {
        public Map<String, JsonNode> receivedLeases = new ConcurrentHashMap<>();
        public volatile boolean isAfterLeaseInitialization = false;
        public volatile boolean isAfterSplits = false;
        public volatile long parentContinuationToken = 0;
        public volatile boolean isContinuationTokenAdvancing = true;
    }
}
