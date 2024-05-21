// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;


import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.faultinjection.FaultInjectionTestBase;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;


public class PartitionLevelCircuitBreakerTests extends FaultInjectionTestBase {

    private List<String> writeRegions;

    private static final CosmosEndToEndOperationLatencyPolicyConfig TWO_SECOND_TIMEOUT_WITH_THRESHOLD_BASED_AVAILABILITY_STRATEGY
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2))
        .availabilityStrategy(new ThresholdBasedAvailabilityStrategy())
        .build();

    private static final CosmosEndToEndOperationLatencyPolicyConfig TWO_SECOND_TIMEOUT
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2))
        .build();

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public PartitionLevelCircuitBreakerTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    @BeforeClass(groups = {"multi-master"})
    public void beforeClass() {
        try (CosmosAsyncClient testClient = getClientBuilder().buildAsyncClient()) {
            RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(testClient);
            GlobalEndpointManager globalEndpointManager = documentClient.getGlobalEndpointManager();

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
            this.writeRegions = new ArrayList<>(this.getRegionMap(databaseAccount, true).keySet());
        } finally {
            logger.debug("beforeClass executed...");
        }
    }

    @DataProvider(name = "partitionLevelCircuitBreakerTestConfigs")
    public Object[][] partitionLevelCircuitBreakerTestConfigs() {
        return new Object[][] {
            {FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.UPSERT_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.REPLACE_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.DELETE_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.PATCH_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.BATCH_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.READ_FEED_ITEM, FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionOperationType.READ_ITEM, FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, false},
            {FaultInjectionOperationType.UPSERT_ITEM, FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, false},
            {FaultInjectionOperationType.REPLACE_ITEM, FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, false},
            {FaultInjectionOperationType.DELETE_ITEM, FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, false},
            {FaultInjectionOperationType.PATCH_ITEM, FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, false},
            {FaultInjectionOperationType.CREATE_ITEM, FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, false},
            {FaultInjectionOperationType.QUERY_ITEM, FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, false}
        };
    }

    @DataProvider(name = "readManyTestConfigs")
    public Object[][] readManyTestConfigs() {
        return new Object[][] {
            {FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofMinutes(6), false, false},
            {FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 11, Duration.ofSeconds(0), false, false},
            {FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, false},
            {FaultInjectionServerErrorType.GONE, Integer.MIN_VALUE, Duration.ofSeconds(60), true, true},
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "partitionLevelCircuitBreakerTestConfigs")
    public void operationHitsTerminalExceptionInFirstPreferredRegion(
        FaultInjectionOperationType faultInjectionOperationType,
        FaultInjectionServerErrorType faultInjectionServerErrorType,
        int faultInjectionHitCount,
        Duration faultInjectionDuration,
        boolean shouldEndToEndTimeoutBeInjected,
        boolean shouldThresholdBasedAvailabilityStrategyBeEnabled) {

        logger.info("Checking circuit breaking behavior for {}", faultInjectionOperationType);

        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("Test is not applicable to GATEWAY connectivity mode!");
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String multiPartitionContainerId = UUID.randomUUID() + "-multi-partition-test-container";

        CosmosAsyncContainer container = null;
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(multiPartitionContainerId, "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(getProvisionedThroughputForContainer(faultInjectionOperationType));

        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties, throughputProperties).block();
            container = database.getContainer(multiPartitionContainerId);

            Thread.sleep(10_000);

            int testObjCountToBootstrapFrom = getTestObjectCountToBootstrapFrom(faultInjectionOperationType, 15);
            List<TestObject> testObjects = new ArrayList<>();

            for (int i = 1; i <= testObjCountToBootstrapFrom; i++) {
                TestObject testObject = TestObject.create();
                testObjects.add(testObject);
                container.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
            }

            FeedRange faultyFeedRange;

            if (testObjects.size() != 1) {
                faultyFeedRange = FeedRange.forFullRange();
            } else {
                faultyFeedRange = FeedRange.forLogicalPartition(new PartitionKey(testObjects.get(0).getId()));
            }

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(faultInjectionOperationType)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(faultyFeedRange).build())
                .region(preferredRegions.get(0))
                .build();

            FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(faultInjectionServerErrorType)
                .build();

            FaultInjectionRule faultInjectionRule = null;

            if (faultInjectionServerErrorType == FaultInjectionServerErrorType.GONE) {
                faultInjectionRule = new FaultInjectionRuleBuilder("gone-rule-" + UUID.randomUUID())
                    .condition(faultInjectionCondition)
                    .result(faultInjectionServerErrorResult)
                    .duration(faultInjectionDuration)
                    .build();
            } else if (faultInjectionServerErrorType == FaultInjectionServerErrorType.SERVICE_UNAVAILABLE) {
                faultInjectionRule = new FaultInjectionRuleBuilder("service-unavailable-rule-" + UUID.randomUUID())
                    .condition(faultInjectionCondition)
                    .result(faultInjectionServerErrorResult)
                    .hitLimit(faultInjectionHitCount)
                    .build();
            }

            if (faultInjectionRule != null) {

                Function<OperationInvocationParamsWrapper, OperationExecutionResult<?>> faultInjectedFunc =
                    generateOperation(faultInjectionOperationType);

                assertThat(faultInjectedFunc).isNotNull().as("faultInjectedFunc cannot be null!");


                if (shouldEndToEndTimeoutBeInjected) {

                    CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicyCfg = (shouldThresholdBasedAvailabilityStrategyBeEnabled) ?
                        TWO_SECOND_TIMEOUT_WITH_THRESHOLD_BASED_AVAILABILITY_STRATEGY :
                        TWO_SECOND_TIMEOUT;

                    operationInvocationParamsWrapper.itemRequestOptions = new CosmosItemRequestOptions()
                        .setCosmosEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicyCfg);

                    operationInvocationParamsWrapper.queryRequestOptions = new CosmosQueryRequestOptions()
                        .setCosmosEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicyCfg);

                    operationInvocationParamsWrapper.patchItemRequestOptions = new CosmosPatchItemRequestOptions()
                        .setCosmosEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicyCfg);
                }

                operationInvocationParamsWrapper.asyncContainer = container;
                operationInvocationParamsWrapper.feedRangeToDrainForChangeFeed = faultyFeedRange;

                CosmosFaultInjectionHelper
                    .configureFaultInjectionRules(operationInvocationParamsWrapper.asyncContainer, Arrays.asList(faultInjectionRule))
                    .block();

                for (int i = 1; i <= 15; i++) {
                    operationInvocationParamsWrapper.createdTestObject = testObjects.isEmpty() ? null : testObjects.get(i % testObjects.size());
                    OperationExecutionResult<?> response = faultInjectedFunc.apply(operationInvocationParamsWrapper);
                    logger.info("Hit count : {}", faultInjectionRule.getHitCount());

                    if (response.cosmosItemResponse != null) {
                        assertThat(response.cosmosItemResponse).isNotNull();
                        assertThat(response.cosmosItemResponse.getDiagnostics()).isNotNull();

                        response.cosmosItemResponse.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response.feedResponse != null) {
                        assertThat(response.feedResponse).isNotNull();
                        assertThat(response.feedResponse.getCosmosDiagnostics()).isNotNull();

                        response.feedResponse.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response.cosmosException != null) {
                        assertThat(response.cosmosException).isNotNull();
                        assertThat(response.cosmosException.getDiagnostics()).isNotNull();

                        response.cosmosException.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response.batchResponse != null) {
                        assertThat(response.batchResponse).isNotNull();
                        assertThat(response.batchResponse.getDiagnostics()).isNotNull();

                        response.batchResponse.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    }
                }

                logger.info("Sleep for 120 seconds");
                Thread.sleep(120_000);

                for (int i = 16; i <= 30; i++) {
                    operationInvocationParamsWrapper.createdTestObject = testObjects.isEmpty() ? null : testObjects.get(i % testObjects.size());
                    OperationExecutionResult<?> response = faultInjectedFunc.apply(operationInvocationParamsWrapper);

                    logger.info("Hit count : {}", faultInjectionRule.getHitCount());

                    if (response.cosmosItemResponse != null) {
                        assertThat(response.cosmosItemResponse).isNotNull();
                        assertThat(response.cosmosItemResponse.getDiagnostics()).isNotNull();

                        response.cosmosItemResponse.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response.feedResponse != null) {
                        assertThat(response.feedResponse).isNotNull();
                        assertThat(response.feedResponse.getCosmosDiagnostics()).isNotNull();

                        response.feedResponse.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response.cosmosException != null) {
                        assertThat(response.cosmosException).isNotNull();
                        assertThat(response.cosmosException.getDiagnostics()).isNotNull();

                        response.cosmosException.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response.batchResponse != null) {
                        assertThat(response.batchResponse).isNotNull();
                        assertThat(response.batchResponse.getDiagnostics()).isNotNull();

                        response.batchResponse.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    }
                }
            }

            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Test should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"}, dataProvider = "readManyTestConfigs")
    public void readManyOperationHitsTerminalExceptionInFirstPreferredRegion(
        FaultInjectionServerErrorType faultInjectionServerErrorType,
        int faultInjectionHitCount,
        Duration faultInjectionDuration,
        boolean shouldEndToEndTimeoutBeInjected,
        boolean shouldThresholdBasedAvailabilityStrategyBeEnabled) {

        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("queryWithNoThresholdBasedAvailabilityStrategyHits408InFirstPreferredRegion test is not applicable to GATEWAY connectivity mode!");
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String multiPartitionContainerId = UUID.randomUUID() + "-multi-partition-test-container";

        CosmosAsyncContainer container = null;
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(multiPartitionContainerId, "/mypk");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(12_000);

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties, throughputProperties).block();
            container = database.getContainer(multiPartitionContainerId);

            Thread.sleep(10_000);

            List<FeedRange> feedRanges = container.getFeedRanges().block();

            assertThat(feedRanges).isNotNull().as("feedRanges is not expected to be null!");
            assertThat(feedRanges).isNotEmpty().as("feedRanges is not expected to be empty!");

            Map<String, List<CosmosItemIdentity>> partitionKeyToItemIdentityList = new HashMap<>();
            List<String> partitionKeys = new ArrayList<>();

            for (FeedRange feedRange : feedRanges) {
                String pkForFeedRange = UUID.randomUUID().toString();
                partitionKeys.add(pkForFeedRange);
                partitionKeyToItemIdentityList.put(pkForFeedRange, new ArrayList<>());
                for (int i = 0; i < 10; i++) {
                    TestObject testObject = TestObject.create(pkForFeedRange);
                    partitionKeyToItemIdentityList.get(pkForFeedRange).add(new CosmosItemIdentity(new PartitionKey(pkForFeedRange), testObject.getId()));
                    container.createItem(testObject, new PartitionKey(pkForFeedRange), new CosmosItemRequestOptions()).block();
                }
            }

            PartitionKey faultyPartitionKey = new PartitionKey(partitionKeys.get(0));

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.QUERY_ITEM)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forLogicalPartition(faultyPartitionKey)).build())
                .region(preferredRegions.get(0))
                .build();

            FaultInjectionRule faultInjectionRule = null;

            if (faultInjectionServerErrorType == FaultInjectionServerErrorType.SERVICE_UNAVAILABLE) {
                FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                    .build();

                faultInjectionRule = new FaultInjectionRuleBuilder("service-unavailable-rule-" + UUID.randomUUID())
                    .condition(faultInjectionCondition)
                    .result(faultInjectionServerErrorResult)
                    .hitLimit(faultInjectionHitCount)
                    .build();
            } else if (faultInjectionServerErrorType == FaultInjectionServerErrorType.GONE) {
                FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.GONE)
                    .build();

                faultInjectionRule = new FaultInjectionRuleBuilder("gone-exception" + UUID.randomUUID())
                    .condition(faultInjectionCondition)
                    .result(faultInjectionServerErrorResult)
                    .duration(Duration.ofMinutes(7))
                    .build();
            }

            if (faultInjectionRule != null) {

                CosmosFaultInjectionHelper
                    .configureFaultInjectionRules(container, Arrays.asList(faultInjectionRule))
                    .block();

                for (int i = 1; i <= 15; i++) {
                    List<CosmosItemIdentity> itemIdentities = partitionKeyToItemIdentityList.get(partitionKeys.get(0));

                    FeedResponse<TestObject> response = container
                        .readMany(itemIdentities, TestObject.class)
                        .onErrorResume(throwable -> {
                            if (throwable instanceof OperationCancelledException) {
                                logger.error("OperationCancelledException thrown!");
                            }

                            return Mono.empty();
                        })
                        .block();

                    logger.info("Hit count : {}", faultInjectionRule.getHitCount());

                    if (response != null) {
                        assertThat(response).isNotNull();
                        assertThat(response.getCosmosDiagnostics()).isNotNull();

                        response.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    }
                }

                logger.info("Sleep for 120 seconds");
                Thread.sleep(120_000);

                for (int i = 16; i <= 30; i++) {

                    List<CosmosItemIdentity> itemIdentities = partitionKeyToItemIdentityList.get(partitionKeys.get(0));

                    FeedResponse<TestObject> response = container
                        .readMany(itemIdentities, TestObject.class)
                        .onErrorResume(throwable -> {
                            if (throwable instanceof OperationCancelledException) {
                                logger.error("OperationCancelledException thrown!");
                            }

                            return Mono.empty();
                        })
                        .block();

                    logger.info("Hit count : {}", faultInjectionRule.getHitCount());

                    if (response != null) {
                        assertThat(response).isNotNull();
                        assertThat(response.getCosmosDiagnostics()).isNotNull();

                        response.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    }
                }
            }

            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Query operations should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container);
            safeClose(client);
        }
    }

    @Test(groups = {"multi-master"})
    public void operationHitsTerminalExceptionInMultipleContainers() {
        logger.info("Checking circuit breaking behavior for {}", FaultInjectionOperationType.READ_ITEM);

        List<String> preferredRegions = this.writeRegions;
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("Test is not applicable to GATEWAY connectivity mode!");
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncDatabase database = getSharedCosmosDatabase(client);

        String multiPartitionContainerId1 = UUID.randomUUID() + "-multi-partition-test-container";
        String multiPartitionContainerId2 = UUID.randomUUID() + "-multi-partition-test-container";

        CosmosAsyncContainer container1 = null;
        CosmosContainerProperties containerProperties1 = new CosmosContainerProperties(multiPartitionContainerId1, "/id");
        ThroughputProperties throughputProperties1 = ThroughputProperties.createManualThroughput(12_000);

        CosmosAsyncContainer container2 = null;
        CosmosContainerProperties containerProperties2 = new CosmosContainerProperties(multiPartitionContainerId2, "/id");
        ThroughputProperties throughputProperties2 = ThroughputProperties.createManualThroughput(12_000);

        try {

            System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED", "true");

            database.createContainerIfNotExists(containerProperties1, throughputProperties1).block();
            container1 = database.getContainer(multiPartitionContainerId1);

            database.createContainerIfNotExists(containerProperties2, throughputProperties2).block();
            container2 = database.getContainer(multiPartitionContainerId2);

            Thread.sleep(10_000);

            int testObjCountToBootstrapFrom = 2;
            List<TestObject> testObjects1 = new ArrayList<>();

            for (int i = 1; i <= testObjCountToBootstrapFrom; i++) {
                TestObject testObject = TestObject.create();
                testObjects1.add(testObject);
                container1.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
                container2.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
            }

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forLogicalPartition(new PartitionKey(testObjects1.get(0).getId()))).build())
                .region(preferredRegions.get(0))
                .build();

            FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                .build();

            FaultInjectionRule faultInjectionRule1 = new FaultInjectionRuleBuilder("service-unavailable-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .hitLimit(11)
                .build();

            FaultInjectionRule faultInjectionRule2 = new FaultInjectionRuleBuilder("service-unavailable-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .hitLimit(11)
                .build();

            if (faultInjectionRule1 != null && faultInjectionRule2 != null) {

                CosmosFaultInjectionHelper
                    .configureFaultInjectionRules(container1, Arrays.asList(faultInjectionRule1))
                    .block();

                CosmosFaultInjectionHelper
                    .configureFaultInjectionRules(container2, Arrays.asList(faultInjectionRule2))
                    .block();

                OperationInvocationParamsWrapper paramsWrapper1 = new OperationInvocationParamsWrapper();
                OperationInvocationParamsWrapper paramsWrapper2 = new OperationInvocationParamsWrapper();

                Function<OperationInvocationParamsWrapper, OperationExecutionResult<?>> faultInjectedFunc = generateOperation(FaultInjectionOperationType.READ_ITEM);

                for (int i = 1; i <= 15; i++) {
                    paramsWrapper1.createdTestObject = testObjects1.isEmpty() ? null : testObjects1.get(0);
                    paramsWrapper1.asyncContainer = container1;

                    paramsWrapper2.createdTestObject = testObjects1.isEmpty() ? null : testObjects1.get(0);
                    paramsWrapper2.asyncContainer = container2;

                    OperationExecutionResult<?> response1 = faultInjectedFunc.apply(paramsWrapper1);
                    OperationExecutionResult<?> response2 = faultInjectedFunc.apply(paramsWrapper2);

                    logger.info("Hit count : {}", faultInjectionRule1.getHitCount());
                    logger.info("Hit count : {}", faultInjectionRule2.getHitCount());

                    if (response1.cosmosItemResponse != null) {
                        assertThat(response1.cosmosItemResponse).isNotNull();
                        assertThat(response1.cosmosItemResponse.getDiagnostics()).isNotNull();

                        response1.cosmosItemResponse.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response1.feedResponse != null) {
                        assertThat(response1.feedResponse).isNotNull();
                        assertThat(response1.feedResponse.getCosmosDiagnostics()).isNotNull();

                        response1.feedResponse.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response1.cosmosException != null) {
                        assertThat(response1.cosmosException).isNotNull();
                        assertThat(response1.cosmosException.getDiagnostics()).isNotNull();

                        response1.cosmosException.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    }
                    if (response2.cosmosItemResponse != null) {
                        assertThat(response2.cosmosItemResponse).isNotNull();
                        assertThat(response2.cosmosItemResponse.getDiagnostics()).isNotNull();

                        response2.cosmosItemResponse.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response2.feedResponse != null) {
                        assertThat(response2.feedResponse).isNotNull();
                        assertThat(response2.feedResponse.getCosmosDiagnostics()).isNotNull();

                        response2.feedResponse.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response2.cosmosException != null) {
                        assertThat(response2.cosmosException).isNotNull();
                        assertThat(response2.cosmosException.getDiagnostics()).isNotNull();

                        response2.cosmosException.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    }
                }

                logger.info("Sleep for 120 seconds");
                Thread.sleep(120_000);

                for (int i = 16; i <= 30; i++) {
                    paramsWrapper1.createdTestObject = testObjects1.isEmpty() ? null : testObjects1.get(0);
                    paramsWrapper1.asyncContainer = container1;

                    paramsWrapper2.createdTestObject = testObjects1.isEmpty() ? null : testObjects1.get(0);
                    paramsWrapper2.asyncContainer = container2;

                    OperationExecutionResult<?> response1 = faultInjectedFunc.apply(paramsWrapper1);
                    OperationExecutionResult<?> response2 = faultInjectedFunc.apply(paramsWrapper2);

                    logger.info("Hit count : {}", faultInjectionRule1.getHitCount());
                    logger.info("Hit count : {}", faultInjectionRule2.getHitCount());

                    if (response1.cosmosItemResponse != null) {
                        assertThat(response1.cosmosItemResponse).isNotNull();
                        assertThat(response1.cosmosItemResponse.getDiagnostics()).isNotNull();

                        response1.cosmosItemResponse.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response1.feedResponse != null) {
                        assertThat(response1.feedResponse).isNotNull();
                        assertThat(response1.feedResponse.getCosmosDiagnostics()).isNotNull();

                        response1.feedResponse.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response1.cosmosException != null) {
                        assertThat(response1.cosmosException).isNotNull();
                        assertThat(response1.cosmosException.getDiagnostics()).isNotNull();

                        response1.cosmosException.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    }
                    if (response2.cosmosItemResponse != null) {
                        assertThat(response2.cosmosItemResponse).isNotNull();
                        assertThat(response2.cosmosItemResponse.getDiagnostics()).isNotNull();

                        response2.cosmosItemResponse.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response2.feedResponse != null) {
                        assertThat(response2.feedResponse).isNotNull();
                        assertThat(response2.feedResponse.getCosmosDiagnostics()).isNotNull();

                        response2.feedResponse.getCosmosDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response2.cosmosException != null) {
                        assertThat(response2.cosmosException).isNotNull();
                        assertThat(response2.cosmosException.getDiagnostics()).isNotNull();

                        response2.cosmosException.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    }
                }
            }

            logger.info("End test");
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Test should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_ENABLED");
            safeDeleteCollection(container1);
            safeDeleteCollection(container2);
            safeClose(client);
        }
    }

    private static int getTestObjectCountToBootstrapFrom(FaultInjectionOperationType faultInjectionOperationType, int opCount) {
        switch (faultInjectionOperationType) {
            case READ_ITEM:
            case UPSERT_ITEM:
            case REPLACE_ITEM:
            case QUERY_ITEM:
            case PATCH_ITEM:
            case READ_FEED_ITEM:
                return 1;
            case DELETE_ITEM:
                return 2 * opCount;
            case CREATE_ITEM:
            case BATCH_ITEM:
                return 0;
            default:
                throw new UnsupportedOperationException(String.format("Operation of type : %s is not supported", faultInjectionOperationType));
        }
    }

    private static Function<OperationInvocationParamsWrapper, OperationExecutionResult<?>> generateOperation(FaultInjectionOperationType faultInjectionOperationType) {

        switch (faultInjectionOperationType) {
            case READ_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> readItemResponse = asyncContainer.readItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions,
                                TestObject.class)
                            .block();

                        return new OperationExecutionResult<>(readItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case UPSERT_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> upsertItemResponse = asyncContainer.upsertItem(
                                createdTestObject,
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new OperationExecutionResult<>(upsertItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case CREATE_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = TestObject.create();
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> createItemResponse = asyncContainer.createItem(
                                createdTestObject,
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new OperationExecutionResult<>(createItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case DELETE_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<Object> deleteItemResponse = asyncContainer.deleteItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new OperationExecutionResult<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case PATCH_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosPatchItemRequestOptions patchItemRequestOptions = (CosmosPatchItemRequestOptions) paramsWrapper.patchItemRequestOptions;

                    CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/number", 555);

                    try {

                        CosmosItemResponse<TestObject> patchItemResponse = asyncContainer.patchItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                patchOperations,
                                patchItemRequestOptions,
                                TestObject.class)
                            .block();

                        return new OperationExecutionResult<>(patchItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case QUERY_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    CosmosQueryRequestOptions queryRequestOptions = paramsWrapper.queryRequestOptions;

                    try {

                        FeedResponse<TestObject> queryItemResponse = asyncContainer.queryItems(
                                "SELECT * FROM C",
                                queryRequestOptions,
                                TestObject.class)
                            .byPage()
                            .blockLast();

                        return new OperationExecutionResult<>(queryItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case REPLACE_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestObject createdTestObject = paramsWrapper.createdTestObject;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> deleteItemResponse = asyncContainer.replaceItem(
                                createdTestObject,
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new OperationExecutionResult<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case BATCH_ITEM:
                return (paramsWrapper) -> {

                    TestObject testObject = TestObject.create();
                    CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(testObject.getId()));
                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    batch.createItemOperation(testObject);
                    batch.readItemOperation(testObject.getId());

                    try {
                        CosmosBatchResponse batchResponse = asyncContainer.executeCosmosBatch(batch).block();
                        return new OperationExecutionResult<>(batchResponse);
                    } catch (Exception ex) {
                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case READ_FEED_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    try {

                        FeedResponse<TestObject> feedResponseFromChangeFeed = asyncContainer.queryChangeFeed(
                                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(paramsWrapper.feedRangeToDrainForChangeFeed),
                                TestObject.class)
                            .byPage()
                            .blockLast();

                        return new OperationExecutionResult<>(feedResponseFromChangeFeed);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new OperationExecutionResult<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            default:
                throw new UnsupportedOperationException(String.format("Operation of type : %s is not supported", faultInjectionOperationType));
        }
    }

    private static int getProvisionedThroughputForContainer(FaultInjectionOperationType faultInjectionOperationType) {
        switch (faultInjectionOperationType) {
            case READ_ITEM:
            case UPSERT_ITEM:
            case REPLACE_ITEM:
            case QUERY_ITEM:
            case PATCH_ITEM:
                return 12_000;
            case DELETE_ITEM:
            case CREATE_ITEM:
            case BATCH_ITEM:
            case READ_FEED_ITEM:
                return 6_000;
            default:
                throw new UnsupportedOperationException(String.format("Operation of type : %s is not supported", faultInjectionOperationType));
        }
    }

    @Test(groups = {"multi-master"})
    public void operationHitsServiceUnavailableInSecondPreferredRegion() {}

    private static class OperationExecutionResult<T> {

        private final CosmosItemResponse<T> cosmosItemResponse;
        private final CosmosException cosmosException;
        private final FeedResponse<T> feedResponse;
        private final CosmosBatchResponse batchResponse;

        OperationExecutionResult(FeedResponse<T> feedResponse) {
            this.feedResponse = feedResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.batchResponse = null;
        }

        OperationExecutionResult(CosmosItemResponse<T> cosmosItemResponse) {
            this.cosmosItemResponse = cosmosItemResponse;
            this.cosmosException = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        OperationExecutionResult(CosmosException cosmosException) {
            this.cosmosException = cosmosException;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        OperationExecutionResult(CosmosBatchResponse batchResponse) {
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = batchResponse;
        }
    }

    private static class OperationInvocationParamsWrapper {
        public CosmosAsyncContainer asyncContainer;
        public TestObject createdTestObject;
        public CosmosItemRequestOptions itemRequestOptions;
        public CosmosQueryRequestOptions queryRequestOptions;
        public CosmosItemRequestOptions patchItemRequestOptions;
        public FeedRange feedRangeToDrainForChangeFeed;
    }

    private static Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }
}
