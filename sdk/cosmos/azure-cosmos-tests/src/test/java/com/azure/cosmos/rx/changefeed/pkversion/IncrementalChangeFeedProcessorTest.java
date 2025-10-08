// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.changefeed.pkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.SplitTestsRetryAnalyzer;
import com.azure.cosmos.SplitTimeoutException;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
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
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.cosmos.BridgeInternal.extractContainerSelfLink;
import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.testng.Assert.assertThrows;

public class IncrementalChangeFeedProcessorTest extends TestSuiteBase {
    private final static Logger log = LoggerFactory.getLogger(IncrementalChangeFeedProcessorTest.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private CosmosAsyncDatabase createdDatabase;
//    private final String databaseId = "testdb1";
//    private final String hostName = "TestHost1";
    private final String hostName = RandomStringUtils.randomAlphabetic(6);
    private final int FEED_COUNT = 10;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
    private final int REPLICA_IN_SATELLITE_REGION_CATCH_UP_TIME = 10000;
    private final int FEED_COLLECTION_THROUGHPUT = 400;
    private final int FEED_COLLECTION_THROUGHPUT_FOR_SPLIT = 10100;
    private final int LEASE_COLLECTION_THROUGHPUT = 400;
    private final String MULTI_WRITE_DATABASE_NAME = "multi-write-test-database"+ UUID.randomUUID();
    private final String MULTI_WRITE_MONITORED_COLLECTION_NAME = "multi-write-test-monitored-container"+ UUID.randomUUID();
    private final String MULTI_WRITE_LEASE_COLLECTION_NAME = "multi-write-test-lease-container"+ UUID.randomUUID();

    private CosmosAsyncClient client;

    private ChangeFeedProcessor changeFeedProcessor;

    @DataProvider(name = "throughputControlConfigArgProvider")
    public static Object[][] throughputControlConfigArgProvider() {
        return new Object[][]{
            // throughput control config enabled
            { true },
            { false }
        };
    }

    @DataProvider(name = "parsingErrorArgProvider")
    public static Object[][] parsingErrorArgProvider() {
        return new Object[][]{
            {
                new StreamConstraintsException("A StreamConstraintsException has been hit!"),
                -1
            },
            {
                new JacksonException("A JacksonException has been hit!") {
                    @Override
                    public JsonLocation getLocation() {
                        return null;
                    }

                    @Override
                    public String getOriginalMessage() {
                        return "";
                    }

                    @Override
                    public Object getProcessor() {
                        return null;
                    }
                },
                -1
            },
            {
                new StreamConstraintsException("A StreamConstraintsException has been hit!"),
                10
            },
            {
                new JacksonException("A JacksonException has been hit!") {
                    @Override
                    public JsonLocation getLocation() {
                        return null;
                    }

                    @Override
                    public String getOriginalMessage() {
                        return "";
                    }

                    @Override
                    public Object getProcessor() {
                        return null;
                    }
                },
                10
            }
        };
    }

    @DataProvider
    public Object[] incrementalChangeFeedModeStartFromSetting() {
        return new Object[] { true, false };
    }

    @Factory(dataProvider = "clientBuilders")
    public IncrementalChangeFeedProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "long-emulator" }, timeOut = 4 * TIMEOUT)
    public void readFeedDocumentsStartFromBeginning() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

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

            startChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            safeStopChangeFeedProcessor(changeFeedProcessor);
            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);

            // restart the change feed processor and verify it can start successfully
            startChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to start
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            safeStopChangeFeedProcessor(changeFeedProcessor);
            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
     }

    @Test(groups = { "long-emulator" }, enabled = false, timeOut = 12 * TIMEOUT)
    public void readFeedDocumentsStartFromBeginningWithPkRangeThrottles() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection
            = client.getDatabase("TestDb").getContainer("TestFeedContainer");
        CosmosAsyncContainer createdLeaseCollection
            = client.getDatabase("TestDb").getContainer("TestLeaseContainer");

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

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

            FeedRange fullRange = FeedRange.forFullRange();

            FaultInjectionServerErrorResult pkRangeThrottledError = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
                .suppressServiceRequests(false)
                .build();

            FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.METADATA_REQUEST_PARTITION_KEY_RANGES)
                .build();

            String pkRangeThrottledId = String.format("pkrange-throttled-error-%s", UUID.randomUUID());

            FaultInjectionRuleBuilder ruleBuilder = new FaultInjectionRuleBuilder(pkRangeThrottledId)
                .condition(condition)
                .result(pkRangeThrottledError);

            FaultInjectionRule pkRangeThrottledFIErrorRule = ruleBuilder.build();

            CosmosFaultInjectionHelper.configureFaultInjectionRules(createdFeedCollection, Arrays.asList(pkRangeThrottledFIErrorRule)).block();

            startChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(20000 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            safeStopChangeFeedProcessor(changeFeedProcessor);
            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);

            // restart the change feed processor and verify it can start successfully
            startChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to start
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            safeStopChangeFeedProcessor(changeFeedProcessor);
            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
//            safeDeleteCollection(createdFeedCollection);
//            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.            Thread.sleep(500);
        }
    }

    @Test(groups = { "long-emulator" }, dataProvider = "parsingErrorArgProvider", timeOut = 12 * TIMEOUT)
    public void readFeedDocumentsStartFromBeginningWithJsonProcessingErrors(Exception exceptionType, int maxItemCount) throws InterruptedException {

        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);
        Callable<Void> responseInterceptor = null;

        // Response Interceptor Properties
        AtomicInteger pageCounter = new AtomicInteger(0);
        AtomicInteger exceptionCounter = new AtomicInteger(0);
        AtomicInteger totalExceptionHits = new AtomicInteger(0);

        if (exceptionType instanceof StreamConstraintsException) {
            responseInterceptor = () -> {
                // inject when certain no. of pages have been processed
                if (pageCounter.get() > 1 && pageCounter.get() % 2 == 0) {
                    if (exceptionCounter.get() < 3) {
                        exceptionCounter.incrementAndGet();
                        totalExceptionHits.incrementAndGet();
                        throw exceptionType;
                    } else {
                        exceptionCounter.set(0);
                    }
                }

                return null;
            };
        } else {
            responseInterceptor = () -> {
                // inject when certain no. of pages have been processed
                if (pageCounter.get() > 1 && pageCounter.get() % 2 == 0) {
                    if (exceptionCounter.get() < 2) {
                        exceptionCounter.incrementAndGet();
                        totalExceptionHits.incrementAndGet();
                        throw exceptionType;
                    } else {
                        exceptionCounter.set(0);
                    }
                }

                return null;
            };
        }

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, 100);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }

                    pageCounter.incrementAndGet();
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(20))
                    .setLeaseAcquireInterval(Duration.ofSeconds(10))
                    .setLeaseExpirationInterval(Duration.ofSeconds(30))
                    .setFeedPollDelay(Duration.ofSeconds(2))
                    .setLeasePrefix("TEST")
                    .setMaxItemCount(maxItemCount)
                    .setStartFromBeginning(true)
                    .setMaxScaleCount(0) // unlimited
                    .setResponseInterceptor(responseInterceptor)
                )
                .buildChangeFeedProcessor();

            startChangeFeedProcessor(changeFeedProcessor);

            for (int i = 0; i < 5; i++) {
                setupReadFeedDocuments(createdDocuments, createdFeedCollection, 100);
                Thread.sleep(10_000);
            }

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(20 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            safeStopChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);

            logger.warn("Total documents received: {}", receivedDocuments.size());
            logger.warn("Total created documents : {}", createdDocuments.size());
            logger.warn("Total exception hits : {}", totalExceptionHits.get());

            assertThat(totalExceptionHits.get()).isGreaterThan(0);

            if (exceptionType instanceof StreamConstraintsException) {
                assertThat(receivedDocuments.size()).isEqualTo(createdDocuments.size());

                for (InternalObjectNode item : createdDocuments) {
                    assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
                }
            } else {
                assertThat(receivedDocuments.size()).isEqualTo(createdDocuments.size() - totalExceptionHits.get() / 2);
            }
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "long-emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void readFeedDocumentsStartFromCustomDate() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
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

            startChangeFeedProcessor(changeFeedProcessor);

            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

            // Wait for the feed processor to receive and process the documents.
            waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            safeStopChangeFeedProcessor(changeFeedProcessor);

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

    @Test(groups = {"multi-master"}, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
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
                setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

                // create a gap between previously written documents
                Thread.sleep(3000);

                ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                    .hostName(hostName)
                    .feedContainer(createdFeedCollection)
                    .leaseContainer(createdLeaseCollection)
                    .handleChanges((List<JsonNode> docs) -> {
                        logger.info("START processing from thread {}", Thread.currentThread().getId());
                        for (JsonNode item : docs) {
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

                startChangeFeedProcessor(changeFeedProcessor);

                Thread.sleep(1000);
                List<InternalObjectNode> createdDocumentsAfterCFPStart = new ArrayList<>();
                setupReadFeedDocuments(createdDocumentsAfterCFPStart, createdFeedCollection, FEED_COUNT);

                // Wait for the feed processor to receive and process the documents.
                waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

                assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

                safeStopChangeFeedProcessor(changeFeedProcessor);

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

    @Test(groups = {"multi-master"}, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
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

        try {

            cosmosAsyncClientForLocalRegion.createDatabaseIfNotExists(MULTI_WRITE_DATABASE_NAME).block();
            cosmosAsyncDatabaseRegionOne = cosmosAsyncClientForLocalRegion.getDatabase(MULTI_WRITE_DATABASE_NAME);
            cosmosAsyncDatabaseRegionOne.createContainerIfNotExists(MULTI_WRITE_MONITORED_COLLECTION_NAME, "/id", ThroughputProperties.createManualThroughput(400)).block();
            cosmosAsyncDatabaseRegionOne.createContainerIfNotExists(MULTI_WRITE_LEASE_COLLECTION_NAME, "/id", ThroughputProperties.createManualThroughput(400)).block();

            createdFeedCollectionLocalRegion = cosmosAsyncDatabaseRegionOne.getContainer(MULTI_WRITE_MONITORED_COLLECTION_NAME);
            createdLeaseCollectionLocalRegion = cosmosAsyncDatabaseRegionOne.getContainer(MULTI_WRITE_LEASE_COLLECTION_NAME);

            CosmosAsyncDatabase cosmosAsyncDatabaseRegionTwo = cosmosAsyncClientForSatelliteRegion.getDatabase(MULTI_WRITE_DATABASE_NAME);
            createdFeedCollectionSatelliteRegion = cosmosAsyncDatabaseRegionTwo.getContainer(MULTI_WRITE_MONITORED_COLLECTION_NAME);
            createdLeaseCollectionSatelliteRegion = cosmosAsyncDatabaseRegionTwo.getContainer(MULTI_WRITE_LEASE_COLLECTION_NAME);

            // allow some time to ensure collection is available for read
            Thread.sleep(5_000);

            try {

                List<InternalObjectNode> createdDocuments = new ArrayList<>();
                Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
                setupReadFeedDocuments(createdDocuments, createdFeedCollectionLocalRegion, FEED_COUNT);

                // create a gap between previously written documents
                Thread.sleep(3000);

                ChangeFeedProcessor changeFeedProcessorSatelliteRegion = new ChangeFeedProcessorBuilder()
                    .hostName(hostName)
                    .feedContainer(createdFeedCollectionSatelliteRegion)
                    .leaseContainer(createdLeaseCollectionSatelliteRegion)
                    .handleChanges((List<JsonNode> docs) -> {
                        logger.info("START processing from thread {}", Thread.currentThread().getId());
                        for (JsonNode item : docs) {
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

                startChangeFeedProcessor(changeFeedProcessorSatelliteRegion);

                // allow some time to ensure CFP instance has started
                Thread.sleep(1000);

                List<InternalObjectNode> createdDocumentsAfterCFPStart = new ArrayList<>();
                setupReadFeedDocuments(createdDocumentsAfterCFPStart, createdFeedCollectionLocalRegion, FEED_COUNT);

                Thread.sleep(REPLICA_IN_SATELLITE_REGION_CATCH_UP_TIME);

                // Wait for the feed processor to receive and process the documents.
                waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

                assertThat(changeFeedProcessorSatelliteRegion.isStarted()).as("Change Feed Processor instance is running").isTrue();

                safeStopChangeFeedProcessor(changeFeedProcessorSatelliteRegion);

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

    @Test(groups = {"multi-master"}, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
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
                    .handleChanges((List<JsonNode> docs) -> {
                        logger.info("START processing from thread {}", Thread.currentThread().getId());
                        for (JsonNode item : docs) {
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
                    .handleChanges((List<JsonNode> docs) -> {
                        logger.info("START processing from thread {}", Thread.currentThread().getId());
                        for (JsonNode item : docs) {
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

                // enforce both CFP instances are started
                startChangeFeedProcessor(changeFeedProcessorLocalRegion);
                startChangeFeedProcessor(changeFeedProcessorSatelliteRegion);

                assertThat(changeFeedProcessorLocalRegion.isStarted()).as("Change feed processor instance is running...").isTrue();
                assertThat(changeFeedProcessorSatelliteRegion.isStarted()).as("Change feed processor instance is running...").isTrue();

                List<InternalObjectNode> createdDocumentsAfterCFPStart = new ArrayList<>();
                setupReadFeedDocuments(createdDocumentsAfterCFPStart, createdFeedCollectionLocalRegion, FEED_COUNT);

                // abruptly initiate stopping the CFP instance for the local region
                safeStopChangeFeedProcessor(changeFeedProcessorLocalRegion);

                Thread.sleep(REPLICA_IN_SATELLITE_REGION_CATCH_UP_TIME);

                // Wait for the feed processor to receive and process the documents.
                waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

                setupReadFeedDocuments(createdDocumentsAfterCFPStart, createdFeedCollectionLocalRegion, FEED_COUNT);

                Thread.sleep(REPLICA_IN_SATELLITE_REGION_CATCH_UP_TIME);
                // Wait for the feed processor to receive and process the documents.
                waitToReceiveDocuments(receivedDocuments, 40 * CHANGE_FEED_PROCESSOR_TIMEOUT, 2 * FEED_COUNT);

                safeStopChangeFeedProcessor(changeFeedProcessorSatelliteRegion);

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

    @Test(groups = { "long-emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getEstimatedLag() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessorMain = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new ChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .handleChanges((List<JsonNode> docs) -> {
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
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            Map<String, Integer> estimatedLagResult = changeFeedProcessorMain.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            assertThat(estimatedLagResult.size()).isNotZero().as("Change Feed Processor number of leases should not be 0.");

            int totalLag = 0;
            for (int lag : estimatedLagResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).as("Change Feed Processor Main estimated total lag at start").isEqualTo(0);

            // check the side cart CFP instance
            Map<String, Integer> estimatedLagSideCartResult = changeFeedProcessorSideCart.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            assertThat(estimatedLagSideCartResult.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (int lag : estimatedLagSideCartResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).as("Change Feed Processor Side Cart estimated total lag at start").isEqualTo(0);


            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

            estimatedLagResult = changeFeedProcessorMain.getEstimatedLag()
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

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            estimatedLagSideCartResult = changeFeedProcessorSideCart.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            totalLag = 0;
            for (int lag : estimatedLagSideCartResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Side Cart estimated total lag");


        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "long-emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getCurrentState() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessorMain = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new ChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .handleChanges((List<JsonNode> docs) -> {
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
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            List<ChangeFeedProcessorState> cfpCurrentState = changeFeedProcessorMain.getCurrentState()
                .map(state -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
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
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
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
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

            cfpCurrentState = changeFeedProcessorMain.getCurrentState()
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

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            cfpCurrentStateSideCart = changeFeedProcessorSideCart.getCurrentState()
                .map(state -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
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

    @Test(groups = { "long-emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
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
                    log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                    log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
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
                    log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
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
                        log.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");

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

                                return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                                    .map(CosmosItemResponse::getItem);
                            })
                            .map(leaseDocument -> {
                                log.info("QueryItems after Change feed processor processing; found host {}", leaseDocument.getOwner());
                                return leaseDocument;
                            })
                                              .last()
                                              .flatMap(leaseDocument -> {
                                log.info("Start creating documents");
                                List<InternalObjectNode> docDefList = new ArrayList<>();

                                for (int i = 0; i < FEED_COUNT; i++) {
                                    docDefList.add(getDocumentDefinition());
                                }

                                return bulkInsert(createdFeedCollection, docDefList, FEED_COUNT)
                                    .last()
                                    .delayElement(Duration.ofMillis(1000))
                                    .flatMap(cosmosItemResponse -> {
                                        log.info("Start second Change feed processor");
                                        return changeFeedProcessorSecond.start().subscribeOn(Schedulers.boundedElastic())
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

            safeStopChangeFeedProcessor(changeFeedProcessorSecond);

            // Wait for the feed processor to shutdown.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "long-emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
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
                .handleChanges(docs -> {
                    log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                    for (JsonNode item : docs) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
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
                log.info("Start more creating documents");
                List<InternalObjectNode> docDefList = new ArrayList<>();

                for (int i = 0; i < FEED_COUNT; i++) {
                    docDefList.add(getDocumentDefinition());
                }

                bulkInsert(createdFeedCollection, docDefList, FEED_COUNT)
                    .last()
                    .flatMap(cosmosItemResponse -> {
                        log.info("Start first Change feed processor");
                        return changeFeedProcessorFirst.start().subscribeOn(Schedulers.boundedElastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT));

                    })
                    .then(
                        Mono.just(changeFeedProcessorFirst)
                            .flatMap( value -> {
                                log.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");
                                try {
                                    Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
                                } catch (InterruptedException ignored) {
                                }

                                log.info("QueryItems before Change feed processor processing");

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
                                        ServiceItemLease leaseDocument = ServiceItemLease.fromDocument(doc);
                                        leaseDocument.setOwner(null);
                                        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                                        return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                                            .map(CosmosItemResponse::getItem);
                                    })
                                    .map(leaseDocument -> {
                                        log.info("QueryItems after Change feed processor processing; current Owner is'{}'", leaseDocument.getOwner());
                                        return leaseDocument;
                                    })
                                    .last()
                                    .flatMap(leaseDocument -> {
                                        log.info("Start creating more documents");
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
                log.error("First change feed processor did not start in the expected time", ex);
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

            safeStopChangeFeedProcessor(changeFeedProcessorFirst);

            // Wait for the feed processor to shutdown.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    // TODO reenable when investigating/fixing https://github.com/Azure/azure-sdk-for-java/issues/44115
    @Test(groups = { "cfp-split" }, dataProvider = "throughputControlConfigArgProvider", timeOut = 160 * CHANGE_FEED_PROCESSOR_TIMEOUT, enabled = false)
    public void readFeedDocumentsAfterSplit(boolean throughputControlConfigEnabled) throws InterruptedException {
        CosmosAsyncContainer createdFeedCollectionForSplit = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(2 * LEASE_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseMonitorCollection = createLeaseMonitorCollection(LEASE_COLLECTION_THROUGHPUT);

        ChangeFeedProcessor leaseMonitoringChangeFeedProcessor = null;

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            LeaseStateMonitor leaseStateMonitor = new LeaseStateMonitor();

            // create a monitoring CFP for ensuring the leases are updating as expected
            leaseMonitoringChangeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(leasesChangeFeedProcessorHandler(leaseStateMonitor))
                .feedContainer(createdLeaseCollection)
                .leaseContainer(createdLeaseMonitorCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("MONITOR")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(100)
                    .setLeaseExpirationInterval(Duration.ofMillis(10 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setFeedPollDelay(Duration.ofMillis(200)))
                .buildChangeFeedProcessor();

            // generate a first batch of documents
            setupReadFeedDocuments(createdDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            ChangeFeedProcessorOptions options =
                new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(10)
                    .setLeaseRenewInterval(Duration.ofSeconds(2));

            if (throughputControlConfigEnabled) {
                options.setFeedPollThroughputControlConfig(
                    new ThroughputControlGroupConfigBuilder()
                        .groupName("splitTest-" + UUID.randomUUID())
                        .targetThroughputThreshold(1.0) // just to make sure enabling throughput control config will not cause exceptions/errors
                        .build()
                );
            }

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollectionForSplit)
                .leaseContainer(createdLeaseCollection)
                .options(options)
                .buildChangeFeedProcessor();

            leaseMonitoringChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .onErrorResume(throwable -> {
                    log.error("Change feed processor for lease monitoring did not start in the expected time", throwable);
                    return Mono.error(throwable);
                })
                .then(
                    changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
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
                                .readThroughput().subscribeOn(Schedulers.boundedElastic())
                                .flatMap(currentThroughput ->
                                    createdFeedCollectionForSplit
                                        .replaceThroughput(ThroughputProperties.createManualThroughput(FEED_COLLECTION_THROUGHPUT_FOR_SPLIT))
                                        .subscribeOn(Schedulers.boundedElastic())
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
            Flux.just(1).subscribeOn(Schedulers.boundedElastic())
                .flatMap(value -> {
                    log.warn("Reading current throughput change.");
                    return contextClient.readPartitionKeyRanges(partitionKeyRangesPath, (CosmosQueryRequestOptions) null);
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

            safeStopChangeFeedProcessor(changeFeedProcessor);
            leaseMonitoringChangeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

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
            safeStopChangeFeedProcessor(leaseMonitoringChangeFeedProcessor);
            safeStopChangeFeedProcessor(changeFeedProcessor);

            safeDeleteCollection(createdFeedCollectionForSplit);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "query" }, timeOut = 20 * TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readPartitionKeyRangesWithSuppressedPageSize() {

        AsyncDocumentClient contextClient = BridgeInternal.getContextClient(this.client);
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client);
        String containerLink = BridgeInternal.getLink(asyncContainer);

        try {
            System.setProperty("COSMOS.MAX_ITEM_COUNT_READ_FEED_PK_RANGE", "1");
            contextClient
                .readPartitionKeyRanges(containerLink, (CosmosQueryRequestOptions) null)
                .doOnNext(feedResponse -> {
                    logger.info("[PAGE SIZE CHECK]: feedResponse size: {}", feedResponse.getResults().size());
                    assertThat(feedResponse.getResults().size() <= 1).isTrue();
                })
                .blockLast();

        } catch (RuntimeException e) {
            fail("readPartitionKeyRangesWithSuppressedPageSize failed which was expected to succeed!", e);
        } finally {
            System.clearProperty("COSMOS.MAX_ITEM_COUNT_READ_FEED_PK_RANGE");
        }

        try {
            System.setProperty("COSMOS.MAX_ITEM_COUNT_READ_FEED_PK_RANGE", "-1");
            contextClient
                .readPartitionKeyRanges(containerLink, (CosmosQueryRequestOptions) null)
                .doOnNext(feedResponse -> {
                    logger.info("[PAGE SIZE CHECK]: feedResponse size: {}", feedResponse.getResults().size());
                    assertThat(feedResponse.getResults().size() > 1).isTrue();
                })
                .blockLast();

        } catch (RuntimeException e) {
            fail("readPartitionKeyRangesWithSuppressedPageSize failed which was expected to succeed!", e);
        } finally {
            System.clearProperty("COSMOS.MAX_ITEM_COUNT_READ_FEED_PK_RANGE");
        }

        try {
            contextClient
                .readPartitionKeyRanges(containerLink, (CosmosQueryRequestOptions) null)
                .doOnNext(feedResponse -> {
                    logger.info("[PAGE SIZE CHECK]: feedResponse size: {}", feedResponse.getResults().size());
                    assertThat(feedResponse.getResults().size() > 1).isTrue();
                })
                .blockLast();

        } catch (RuntimeException e) {
            fail("readPartitionKeyRangesWithSuppressedPageSize failed which was expected to succeed!", e);
        } finally {
            System.clearProperty("COSMOS.MAX_ITEM_COUNT_READ_FEED_PK_RANGE");
        }
    }

    @Test(groups = { "cfp-split" }, timeOut = 160 * CHANGE_FEED_PROCESSOR_TIMEOUT, retryAnalyzer = SplitTestsRetryAnalyzer.class)
    public void readFeedDocumentsAfterSplit_maxScaleCount() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollectionForSplit = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(2 * LEASE_COLLECTION_THROUGHPUT);

        ChangeFeedProcessor changeFeedProcessor1 = null;
        ChangeFeedProcessor changeFeedProcessor2 = null;
        String changeFeedProcessor1HostName = RandomStringUtils.randomAlphabetic(6);
        String changeFeedProcessor2HostName = RandomStringUtils.randomAlphabetic(6);
        logger.info("readFeedDocumentsAfterSplit_maxScaleCount changeFeedProcessor1 name {}", changeFeedProcessor1HostName);
        logger.info("readFeedDocumentsAfterSplit_maxScaleCount changeFeedProcessor2 name {}", changeFeedProcessor2HostName);

        try {
            // Set up the maxScaleCount to be equal to the current partition count
            int partitionCountBeforeSplit = createdFeedCollectionForSplit.getFeedRanges().block().size();
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

            // generate a first batch of documents
            setupReadFeedDocuments(createdDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            changeFeedProcessor1 = new ChangeFeedProcessorBuilder()
                .hostName(changeFeedProcessor1HostName)
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
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

            startChangeFeedProcessor(changeFeedProcessor1);

            // Wait for the feed processor to receive and process the second batch of documents.
            waitToReceiveDocuments(receivedDocuments, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            // increase throughput to force a single partition collection to go through a split
            triggerAndWaitForPartitionSplit(createdFeedCollectionForSplit, FEED_COLLECTION_THROUGHPUT_FOR_SPLIT, CHANGE_FEED_PROCESSOR_TIMEOUT);

            // generate the second batch of documents
            setupReadFeedDocuments(createdDocuments, createdFeedCollectionForSplit, FEED_COUNT);

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
            for (JsonNode lease : leaseDocuments) {
                logger.info("readFeedDocumentsAfterSplit_maxScaleCount lease {} {}", lease.get("Owner").asText(), lease);
            }
            assertThat(host1Leases).isEqualTo(partitionCountBeforeSplit);

            // now starts a new change feed processor
            changeFeedProcessor2 = new ChangeFeedProcessorBuilder()
                .hostName(changeFeedProcessor2HostName)
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
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

            startChangeFeedProcessor(changeFeedProcessor2);

            // Wait for the feed processor to receive and process the second batch of documents.
            waitToReceiveDocuments(receivedDocuments, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT*2);

        } finally {
            safeStopChangeFeedProcessor(changeFeedProcessor1);
            safeStopChangeFeedProcessor(changeFeedProcessor2);

            safeDeleteCollection(createdFeedCollectionForSplit);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "long-emulator" }, timeOut = 20 * TIMEOUT)
    public void inactiveOwnersRecovery() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            String leasePrefix = "TEST";

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
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

            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

            validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments,2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);

            log.info("Update leases with random owners");

            SqlParameter param1 = new SqlParameter();
            param1.setName("@PartitionLeasePrefix");
            param1.setValue(leasePrefix);

            SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)", Arrays.asList(param1));

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            createdLeaseCollection.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class).byPage()
                .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.getResults()))
                .flatMap(doc -> {
                    ServiceItemLease leaseDocument = ServiceItemLease.fromDocument(doc);
                    leaseDocument.setOwner(RandomStringUtils.randomAlphabetic(10));
                    CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                    return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                        .map(CosmosItemResponse::getItem);
                })
                .flatMap(leaseDocument -> createdLeaseCollection.readItem(leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), InternalObjectNode.class))
                .map(doc -> {
                    ServiceItemLease leaseDocument = ServiceItemLease.fromDocument(doc.getItem());
                    log.info("Change feed processor current Owner is'{}'", leaseDocument.getOwner());
                    return leaseDocument;
                })
                .blockLast();

            createdDocuments.clear();
            receivedDocuments.clear();
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

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

    @Test(groups = { "long-emulator" }, timeOut = 2 * TIMEOUT)
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
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, feedCount);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(
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

            startChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();
            safeStopChangeFeedProcessor(changeFeedProcessor);

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

    @Test(groups = { "long-emulator" }, timeOut = 2 * TIMEOUT)
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
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
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

            startChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            safeStopChangeFeedProcessor(changeFeedProcessor);

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

    @Test(groups = { "long-emulator" }, timeOut = 2 * TIMEOUT)
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
            // using the original client to create the docs to isolate possible throttling
            setupReadFeedDocuments(
                createdDocuments,
                this.createdDatabase.getContainer(createdFeedCollection.getId()),
                feedCount);

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
                    .setFeedPollThroughputControlConfig(throughputControlGroupConfig)
                    .setLeasePrefix("TEST")
                    .setMaxItemCount(maxItemCount)
                    .setStartFromBeginning(true)
                    .setMaxScaleCount(0) // unlimited
                )
                .buildChangeFeedProcessor();

            startChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            safeStopChangeFeedProcessor(changeFeedProcessor);

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

    // Steps followed in this test
    //  1. Ingest 10 documents into the feed container.
    //  2. Start the LatestVersion / INCREMENTAL ChangeFeedProcessor with either startFromBeginning set to 'true' or 'false'.
    //  3. Stop the LatestVersion / INCREMENTAL ChangeFeedProcessor once the 10 documents are processed or 0 documents are processed (stop after some delay).
    //  4. Start the AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor.
    //  5. Validate IllegalStateException
    //        a) If the LatestVersion / INCREMENTAL ChangeFeedProcessor left behind a lease with a valid continuation,
    //           then AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor should throw the exception.
    //         b) If not, then AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor should be able to reuse the lease
    //            left behind and use its own continuation.
    @Test(groups = { "query" }, dataProvider = "incrementalChangeFeedModeStartFromSetting")
    public void incrementalChangeFeedModeToFullFidelityChangeFeedMode(boolean isStartFromBeginning) throws InterruptedException, JsonProcessingException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocumentsByIncrementalCfp = new ConcurrentHashMap<>();
            Map<String, ChangeFeedProcessorItem> receivedDocumentsByFullFidelityCfp = new ConcurrentHashMap<>();

            ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                .options(new ChangeFeedProcessorOptions()
                    .setStartFromBeginning(isStartFromBeginning)
                    .setMaxItemCount(10)
                    .setLeasePrefix("TEST"))
                .hostName(hostName)
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection);

            changeFeedProcessorBuilder = changeFeedProcessorBuilder
                .handleChanges(changeFeedProcessorHandler(receivedDocumentsByIncrementalCfp));

            ChangeFeedProcessor incrementalChangeFeedProcessor = changeFeedProcessorBuilder.buildChangeFeedProcessor();

            logger.info("Set up read feed documents");
            setupReadFeedDocuments(createdDocuments, createdFeedCollection, FEED_COUNT);

            try {
                if (isStartFromBeginning) {
                    validateChangeFeedProcessing(
                        incrementalChangeFeedProcessor,
                        createdDocuments,
                        receivedDocumentsByIncrementalCfp,
                        2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                } else {
                    validateChangeFeedProcessing(
                        incrementalChangeFeedProcessor,
                        new ArrayList<>(),
                        receivedDocumentsByIncrementalCfp,
                        2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                }

                String leaseQuery = "select * from c";
                List<JsonNode> leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                int leaseDocumentsWithNonNullContinuationToken = 0;

                logger.info("Leases container documents created by INCREMENTAL ChangeFeedProcessor fetched...");
                for (JsonNode lease : leaseDocuments) {

                    JsonNode continuationTokenJsonNode = lease.get("ContinuationToken");

                    if (continuationTokenJsonNode != null) {
                        leaseDocumentsWithNonNullContinuationToken++;

                        String continuationToken = continuationTokenJsonNode.asText();

                        logger.info("Lease document : {}",  OBJECT_MAPPER.writeValueAsString(lease));
                        logger.info("ContinuationToken : {}", continuationToken);
                    } else {
                        logger.info("Lease container document : {}", OBJECT_MAPPER.writeValueAsString(lease));
                    }
                }

                ChangeFeedProcessorBuilder fullFidelityChangeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                    .options(new ChangeFeedProcessorOptions()
                        .setMaxItemCount(1)
                        .setLeasePrefix("TEST"))
                    .hostName(hostName)
                    .feedContainer(createdFeedCollection)
                    .leaseContainer(createdLeaseCollection)
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandlerForAllVersionsAndDeletes(receivedDocumentsByFullFidelityCfp));

                ChangeFeedProcessor fullFidelityChangeFeedProcessor = fullFidelityChangeFeedProcessorBuilder
                    .buildChangeFeedProcessor();

                if (leaseDocumentsWithNonNullContinuationToken > 0) {
                    assertThrows(IllegalStateException.class, () -> fullFidelityChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .block());
                } else {

                    fullFidelityChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .doOnSuccess(ignore -> logger.info("Started FULL_FIDELITY ChangeFeedProcessor successfully!"))
                        .block();

                    createdDocuments = new ArrayList<>();

                    // wait for FULL_FIDELITY ChangeFeedProcessor to start and acquire left behind lease
                    Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                    setupReadFeedDocumentsForAllVersionsAndDeletes(createdDocuments, receivedDocumentsByFullFidelityCfp, createdFeedCollection, 10);

                    // wait for FULL_FIDELITY ChangeFeedProcessor to process all ingested documents
                    Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                    fullFidelityChangeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .block();

                    validateChangeFeedProcessing(createdDocuments, receivedDocumentsByFullFidelityCfp, FEED_COUNT);

                    leaseDocuments =
                        createdLeaseCollection
                            .queryItems(leaseQuery, JsonNode.class)
                            .byPage()
                            .blockLast()
                            .getResults();

                    logger.info("Leases container documents fetched after starting FULL_FIDELITY ChangeFeedProcessor...");
                    for (JsonNode lease : leaseDocuments) {

                        if (lease.get("ContinuationToken") != null) {
                            logger.info("Lease document : {}",  OBJECT_MAPPER.writeValueAsString(lease));
                            logger.info("ContinuationToken : {}", new String(Base64.getUrlDecoder().decode(lease.get("ContinuationToken").asText())));
                        } else {
                            logger.info("Lease container document : {}", OBJECT_MAPPER.writeValueAsString(lease));
                        }
                    }
                }

            } catch (Exception ex) {
                logger.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "cfp-split" }, timeOut = 160 * CHANGE_FEED_PROCESSOR_TIMEOUT, retryAnalyzer = SplitTestsRetryAnalyzer.class)
    public void verifyLeasesOnRestart_AfterSplit() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollectionForSplit = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(2 * LEASE_COLLECTION_THROUGHPUT);

        ChangeFeedProcessor changeFeedProcessor = null;

        try {
            List<FeedRange> feedRanges  = createdFeedCollectionForSplit.getFeedRanges().block();
            assertThat(feedRanges.size()).isEqualTo(1);

            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            // even though CFP is at lease once delivery, but in the test, we did not expect any load balancer would cause the duplicate processing
            // so we should expect each item will only be delivered once
            // using list instead of map to confirm
            List<JsonNode> receivedDocuments = new ArrayList<>();

            // generate a first batch of documents
            setupReadFeedDocuments(createdDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName("verifyLeasesOnRestart_AfterSplit-" + UUID.randomUUID())
                .handleChanges(changeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollectionForSplit)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(true)
                    .setLeaseVerificationEnabledOnRestart(true)
                )
                .buildChangeFeedProcessor();
            startChangeFeedProcessor(changeFeedProcessor);

            // Wait for the feed processor to receive and process the second batch of documents.
            waitToReceiveDocuments(receivedDocuments, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            // stop the change feed processor and trigger split
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
            safeStopChangeFeedProcessor(changeFeedProcessor);

            triggerAndWaitForPartitionSplit(createdFeedCollectionForSplit, FEED_COLLECTION_THROUGHPUT_FOR_SPLIT, CHANGE_FEED_PROCESSOR_TIMEOUT);

            feedRanges  = createdFeedCollectionForSplit.getFeedRanges().block();
            assertThat(feedRanges.size()).isGreaterThan(1);

            // restart the change feed processor
            startChangeFeedProcessor(changeFeedProcessor);

            // generate the second batch of documents
            setupReadFeedDocuments(createdDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            // wait for the change feed processor to receive some documents
            waitToReceiveDocuments(receivedDocuments, 30 * CHANGE_FEED_PROCESSOR_TIMEOUT, 2 * FEED_COUNT);

            assertThat(receivedDocuments.size()).isEqualTo(createdDocuments.size());
            Map<String, JsonNode> receivedDocumentsMap =
                receivedDocuments.stream().collect(Collectors.toMap(item -> item.get("id").asText(), item -> item));
            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocumentsMap.containsKey(item.getId())).isTrue();
            }

        } finally {
            safeStopChangeFeedProcessor(changeFeedProcessor);

            safeDeleteCollection(createdFeedCollectionForSplit);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    private void triggerAndWaitForPartitionSplit(
        CosmosAsyncContainer container,
        int throughputForSplit,
        int checkingIntervalInMillis) throws InterruptedException {
        // increase throughput to force a single partition collection to go through a split
        container
            .readThroughput()
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(currentThroughput ->
                container
                    .replaceThroughput(ThroughputProperties.createManualThroughput(throughputForSplit))
                    .subscribeOn(Schedulers.boundedElastic())
            )
            .block();

        // wait for the split to finish
        ThroughputResponse throughputResponse = container.readThroughput().block();
        int i = 0;
        // Only wait for 10 minutes for the split to complete
        // If backend does not finish split within 10 minutes
        // something is off in the backend
        // it could be due to limits on how many splits can be executed concurrently etc.
        // nothing that can really be done in the SDK
        while (i < 120 && throughputResponse.isReplacePending()) {
            logger.info("Waiting for split to complete");
            Thread.sleep(2 * checkingIntervalInMillis);
            throughputResponse = container.readThroughput().block();
            i += 2;
        }

        if (throughputResponse.isReplacePending()) {
            throw new SplitTimeoutException(
                "Backend did not finish split for container '"
                    + getEndpoint() + "/"
                    + container.getDatabase().getId() + "/"
                    + container.getId()
                    + "' - skipping this test case");
        }
    }

    private void startChangeFeedProcessor(ChangeFeedProcessor changeFeedProcessor) {
        changeFeedProcessor
            .start()
            .subscribeOn(Schedulers.boundedElastic())
            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
            .onErrorResume(throwable -> {
                log.error("Change feed processor did not start in the expected time", throwable);
                return Mono.error(throwable);
            })
            .block();
    }

    private void safeStopChangeFeedProcessor(ChangeFeedProcessor changeFeedProcessor) {
        if (changeFeedProcessor != null && changeFeedProcessor.isStarted()) {
            changeFeedProcessor
                .stop()
                .timeout(Duration.ofMinutes(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .onErrorResume(throwable -> {
                    logger.warn("Stop changeFeedProcessor failed", throwable);
                    return Mono.empty();
                })
                .block();
        }
    }

    void validateChangeFeedProcessing(ChangeFeedProcessor changeFeedProcessor, List<InternalObjectNode> createdDocuments, Map<String, JsonNode> receivedDocuments, int sleepTime) throws InterruptedException {
        startChangeFeedProcessor(changeFeedProcessor);

        // Wait for the feed processor to receive and process the documents.
        Thread.sleep(sleepTime);

        assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

        List<ChangeFeedProcessorState> cfpCurrentState = changeFeedProcessor
            .getCurrentState()
            .map(state -> {
                try {
                    log.info(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(state));
                } catch (JsonProcessingException ex) {
                    log.error("Unexpected", ex);
                }
                return state;
            })
            .block();

        assertThat(cfpCurrentState).isNotNull().as("Change Feed Processor current state");

        for (ChangeFeedProcessorState item : cfpCurrentState) {
            assertThat(item.getHostName()).isEqualTo(hostName).as("Change Feed Processor ownership");
        }

        safeStopChangeFeedProcessor(changeFeedProcessor);

        for (InternalObjectNode item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
        }
    }

    void validateChangeFeedProcessing(List<InternalObjectNode> createdDocuments,
                                      Map<String, ChangeFeedProcessorItem> receivedDocuments,
                                      int expectedFeedCount) {

        // Added this validation for now to verify received list has something - easy way to see size not being 10
        assertThat(receivedDocuments.size()).isEqualTo(expectedFeedCount);

        Set<String> createdDocIds =
            createdDocuments.stream()
                .map(createdDocument -> createdDocument.get("id").toString()).collect(Collectors.toSet());

        for (ChangeFeedProcessorItem item : receivedDocuments.values()) {
            assertThat(createdDocIds.contains(item.toJsonNode().get("id").asText())).as("Document with getId: " + item.toJsonNode().get("id").asText()).isTrue();
        }
    }

    private Consumer<List<JsonNode>> changeFeedProcessorHandler(Map<String, JsonNode> receivedDocuments) {
        return changeFeedProcessorHandlerWithCallback(receivedDocuments, null);
    }

    private Consumer<List<JsonNode>> changeFeedProcessorHandler(List<JsonNode> receivedDocuments) {
        return docs -> {
            log.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                receivedDocuments.add(item);
            }
            log.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private Consumer<List<JsonNode>> changeFeedProcessorHandlerWithCallback(
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

            log.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                processItem(item, receivedDocuments);
            }
            log.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private Consumer<List<ChangeFeedProcessorItem>> changeFeedProcessorHandlerForAllVersionsAndDeletes(Map<String, ChangeFeedProcessorItem> receivedDocuments) {
        return docs -> {

            log.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                processItem(item, receivedDocuments);
            }
            log.info("END processing from thread {}", Thread.currentThread().getId());
        };    }

    private void waitToReceiveDocuments(Map<String, JsonNode> receivedDocuments, long timeoutInMillisecond, long count) throws InterruptedException {
        long remainingWork = timeoutInMillisecond;
        while (remainingWork > 0 && receivedDocuments.size() < count) {
            remainingWork -= 100;
            Thread.sleep(100);
        }

        assertThat(remainingWork > 0).as("Failed to receive all the feed documents").isTrue();
    }

    private void waitToReceiveDocuments(
        List<JsonNode> receivedDocuments,
        long timeoutInMillisecond,
        long count) throws InterruptedException {
        long remainingWork = timeoutInMillisecond;
        while (remainingWork > 0 && receivedDocuments.size() < count) {
            remainingWork -= 100;
            Thread.sleep(100);
        }

        assertThat(remainingWork > 0).as("Failed to receive all the feed documents").isTrue();
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

    @BeforeMethod(groups = { "long-emulator", "cfp-split" }, timeOut = 2 * SETUP_TIMEOUT, alwaysRun = true)
     public void beforeMethod() {
     }

    @BeforeClass(groups = { "long-emulator", "cfp-split" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
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

    @AfterMethod(groups = { "long-emulator", "cfp-split" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterMethod() {
    }

    @AfterClass(groups = { "long-emulator", "cfp-split" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
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

    private void setupReadFeedDocuments(List<InternalObjectNode> createdDocuments, CosmosAsyncContainer feedCollection, long count) {
        List<InternalObjectNode> docDefList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments.addAll(bulkInsertBlocking(feedCollection, docDefList));
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private void setupReadFeedDocumentsForAllVersionsAndDeletes(List<InternalObjectNode> createdDocuments, Map<String, ChangeFeedProcessorItem> receivedDocuments, CosmosAsyncContainer feedCollection, long count) {
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

    private static synchronized void processItem(JsonNode item, Map<String, JsonNode> receivedDocuments) {
        try {
            IncrementalChangeFeedProcessorTest.log
                .info("RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            log.error("Failure in processing json [{}]", e.getMessage(), e);
        }
        receivedDocuments.put(item.get("id").asText(), item);
    }

    private static synchronized void processItem(ChangeFeedProcessorItem item, Map<String, ChangeFeedProcessorItem> receivedDocuments) {
        try {
            IncrementalChangeFeedProcessorTest.log
                .info("RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            log.error("Failure in processing json [{}]", e.getMessage(), e);
        }
        receivedDocuments.put(item.toJsonNode().get("id").asText(), item);
    }

    class LeaseStateMonitor {
        public Map<String, JsonNode> receivedLeases = new ConcurrentHashMap<>();
        public volatile boolean isAfterLeaseInitialization = false;
        public volatile boolean isAfterSplits = false;
        public volatile long parentContinuationToken = 0;
        public volatile boolean isContinuationTokenAdvancing = true;
    }
}
