// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.faultinjection.FaultInjectionTestBase;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.circuitBreaker.ConsecutiveExceptionBasedCircuitBreaker;
import com.azure.cosmos.implementation.circuitBreaker.GlobalPartitionEndpointManagerForCircuitBreaker;
import com.azure.cosmos.implementation.circuitBreaker.LocationHealthStatus;
import com.azure.cosmos.implementation.circuitBreaker.LocationSpecificHealthContext;
import com.azure.cosmos.implementation.circuitBreaker.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class PartitionLevelCircuitBreakerTests extends FaultInjectionTestBase {

    private static final ImplementationBridgeHelpers.CosmosAsyncContainerHelper.CosmosAsyncContainerAccessor containerAccessor
        = ImplementationBridgeHelpers.CosmosAsyncContainerHelper.getCosmosAsyncContainerAccessor();
    private List<String> writeRegions;

    private static final CosmosEndToEndOperationLatencyPolicyConfig NO_END_TO_END_TIMEOUT
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofDays(1)).build();

    private static final CosmosEndToEndOperationLatencyPolicyConfig THREE_SECOND_END_TO_END_TIMEOUT_WITH_THRESHOLD_BASED_AVAILABILITY_STRATEGY
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(3))
        .availabilityStrategy(new ThresholdBasedAvailabilityStrategy())
        .build();

    private static final CosmosEndToEndOperationLatencyPolicyConfig THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(3))
        .build();

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasFirstPreferredRegionOnly = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isEqualTo(1);
        assertThat(ctx.getContactedRegionNames().stream().iterator().next()).isEqualTo(this.firstPreferredRegion.toLowerCase(Locale.ROOT));
    };

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasSecondPreferredRegionOnly = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isEqualTo(1);
        assertThat(ctx.getContactedRegionNames().stream().iterator().next()).isEqualTo(this.secondPreferredRegion.toLowerCase(Locale.ROOT));
    };

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasFirstAndSecondPreferredRegions = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isEqualTo(2);
        assertThat(ctx.getContactedRegionNames()).contains(this.firstPreferredRegion.toLowerCase(Locale.ROOT));
        assertThat(ctx.getContactedRegionNames()).contains(this.secondPreferredRegion.toLowerCase(Locale.ROOT));
    };

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasAnyTwoPreferredRegions = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isEqualTo(2);
    };

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasAtMostTwoPreferredRegions = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isLessThanOrEqualTo(2);
    };

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasOnePreferredRegion = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isLessThanOrEqualTo(1);
    };

    Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasAllRegions = (ctx) -> {
        assertThat(ctx).isNotNull();
        assertThat(ctx.getContactedRegionNames()).isNotNull();
        assertThat(ctx.getContactedRegionNames().size()).isEqualTo(this.writeRegions.size());

        for (String region : this.writeRegions) {
            assertThat(ctx.getContactedRegionNames()).contains(region.toLowerCase(Locale.ROOT));
        }
    };

    Consumer<ResponseWrapper<?>> validateResponseHasSuccess = (responseWrapper) -> {

        assertThat(responseWrapper.cosmosException).isNull();

        if (responseWrapper.feedResponse != null) {
            assertThat(responseWrapper.feedResponse.getCosmosDiagnostics()).isNotNull();
            assertThat(responseWrapper.feedResponse.getCosmosDiagnostics().getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = responseWrapper.feedResponse.getCosmosDiagnostics().getDiagnosticsContext();

            assertThat(diagnosticsContext.getStatusCode() == HttpConstants.StatusCodes.OK || diagnosticsContext.getStatusCode() == HttpConstants.StatusCodes.NOT_MODIFIED).isTrue();
        } else if (responseWrapper.cosmosItemResponse != null) {
            assertThat(responseWrapper.cosmosItemResponse.getDiagnostics()).isNotNull();
            assertThat(responseWrapper.cosmosItemResponse.getDiagnostics().getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = responseWrapper.cosmosItemResponse.getDiagnostics().getDiagnosticsContext();

            assertThat(HttpConstants.StatusCodes.OK <= diagnosticsContext.getStatusCode() && diagnosticsContext.getStatusCode() <= HttpConstants.StatusCodes.NO_CONTENT).isTrue();
        } else if (responseWrapper.batchResponse != null) {
            assertThat(responseWrapper.batchResponse.getDiagnostics()).isNotNull();
            assertThat(responseWrapper.batchResponse.getDiagnostics().getDiagnosticsContext()).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = responseWrapper.batchResponse.getDiagnostics().getDiagnosticsContext();

            assertThat(HttpConstants.StatusCodes.OK <= diagnosticsContext.getStatusCode() && diagnosticsContext.getStatusCode() <= HttpConstants.StatusCodes.NO_CONTENT).isTrue();
        }
    };

    Consumer<ResponseWrapper<?>> validateResponseHasOperationCancelledException = (responseWrapper) -> {
        assertThat(responseWrapper.cosmosException).isNotNull();
        assertThat(responseWrapper.cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
        assertThat(responseWrapper.cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT);
    };

    Consumer<ResponseWrapper<?>> validateResponseHasInternalServerError = (responseWrapper) -> {
        assertThat(responseWrapper.cosmosException).isNotNull();
        assertThat(responseWrapper.cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
    };

    Consumer<ResponseWrapper<?>> validateResponseHasServiceUnavailableError = (responseWrapper) -> {
        assertThat(responseWrapper.cosmosException).isNotNull();
        assertThat(responseWrapper.cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
    };

    Consumer<ResponseWrapper<?>> validateResponseHasRequestTimeoutException = (responseWrapper) -> {
        assertThat(responseWrapper.cosmosException).isNotNull();
        assertThat(responseWrapper.cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
        assertThat(responseWrapper.cosmosException.getSubStatusCode()).isNotEqualTo(HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT);
    };

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildServiceUnavailableFaultInjectionRules
        = PartitionLevelCircuitBreakerTests::buildServiceUnavailableFaultInjectionRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildServerGeneratedGoneErrorFaultInjectionRules
        = PartitionLevelCircuitBreakerTests::buildServerGeneratedGoneErrorFaultInjectionRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildTooManyRequestsErrorFaultInjectionRules
        = PartitionLevelCircuitBreakerTests::buildTooManyRequestsErrorFaultInjectionRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildReadWriteSessionNotAvailableFaultInjectionRules
        = PartitionLevelCircuitBreakerTests::buildReadWriteSessionNotAvailableFaultInjectionRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildTransitTimeoutFaultInjectionRules
        = PartitionLevelCircuitBreakerTests::buildTransitTimeoutFaultInjectionRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildInternalServerErrorFaultInjectionRules
        = PartitionLevelCircuitBreakerTests::buildInternalServerErrorFaultInjectionRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildRetryWithFaultInjectionRules
        = PartitionLevelCircuitBreakerTests::buildRetryWithFaultInjectionRules;

    private static final CosmosRegionSwitchHint NO_REGION_SWITCH_HINT = null;

    private static final Boolean NON_IDEMPOTENT_WRITE_RETRIES_ENABLED = true;

    private static final Set<ConnectionMode> ALL_CONNECTION_MODES_INCLUDED = new HashSet<>();

    private static final Set<ConnectionMode> ONLY_DIRECT_MODE = new HashSet<>();

    private static final Set<ConnectionMode> ONLY_GATEWAY_MODE = new HashSet<>();

    private String firstPreferredRegion = null;

    private String secondPreferredRegion = null;

    private String sharedAsyncDatabaseId = null;

    private String sharedMultiPartitionAsyncContainerIdWhereIdIsPartitionKey = null;

    private String sharedMultiPartitionAsyncContainerIdWhereMyPkIsPartitionKey = null;

    private String singlePartitionAsyncContainerId = null;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public PartitionLevelCircuitBreakerTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    @BeforeClass(groups = {"circuit-breaker-misc-gateway", "circuit-breaker-misc-direct", "circuit-breaker-read-all-read-many"})
    public void beforeClass() {
        try (CosmosAsyncClient testClient = getClientBuilder().buildAsyncClient()) {
            RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(testClient);
            GlobalEndpointManager globalEndpointManager = documentClient.getGlobalEndpointManager();

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
            this.writeRegions = new ArrayList<>(this.getRegionMap(databaseAccount, true).keySet());

            CosmosAsyncDatabase sharedAsyncDatabase = getSharedCosmosDatabase(testClient);
            CosmosAsyncContainer sharedMultiPartitionCosmosContainerWithIdAsPartitionKey = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(testClient);
            CosmosAsyncContainer sharedAsyncMultiPartitionContainerWithMyPkAsPartitionKey = getSharedMultiPartitionCosmosContainer(testClient);

            this.sharedAsyncDatabaseId = sharedAsyncDatabase.getId();
            this.sharedMultiPartitionAsyncContainerIdWhereIdIsPartitionKey = sharedMultiPartitionCosmosContainerWithIdAsPartitionKey.getId();
            this.sharedMultiPartitionAsyncContainerIdWhereMyPkIsPartitionKey = sharedAsyncMultiPartitionContainerWithMyPkAsPartitionKey.getId();

            this.singlePartitionAsyncContainerId = UUID.randomUUID().toString();
            sharedAsyncDatabase.createContainerIfNotExists(this.singlePartitionAsyncContainerId, "/id").block();

            ALL_CONNECTION_MODES_INCLUDED.add(ConnectionMode.DIRECT);
            ALL_CONNECTION_MODES_INCLUDED.add(ConnectionMode.GATEWAY);
            ONLY_DIRECT_MODE.add(ConnectionMode.DIRECT);
            ONLY_GATEWAY_MODE.add(ConnectionMode.GATEWAY);

            try {
                Thread.sleep(3000);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        } finally {
            logger.debug("beforeClass executed...");
        }
    }

    @DataProvider(name = "miscellaneousOpTestConfigsDirect")
    public Object[][] miscellaneousOpTestConfigsDirect() {

        // General testing flow:
        // Below tests choose a fault type to inject, regions to inject the fault in
        // and the operation type for which the fault is injected. The idea is to assert
        // what happens when faults are being injected - should an exception bubble up
        // in the process [or] should the operation succeed, region contacted when circuit
        // breaking has kicked in and region contacted when region + partition combination is
        // being marked back as UnhealthyTentative (eligible to accept requests)
        return new Object[][]{
            // Server-generated 503 injected into first preferred region for READ_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                validateResponseHasSuccess,
                validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for UPSERT_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.UPSERT_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for REPLACE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for DELETE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.DELETE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for PATCH_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.PATCH_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to succeed in all runs but include
            // the second preferred region when the first preferred region has been short-circuited.
            // For queries which require a QueryPlan, the first preferred region is contacted (not a data plane request
            // which will hit a data partition so is not eligible for circuit breaking).
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for BATCH_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.BATCH_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for READ_FEED_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.READ_FEED_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 410 injected into first preferred region for READ_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 410 injected into first preferred region for UPSERT_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.UPSERT_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 410 injected into first preferred region for REPLACE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 410 injected into first preferred region for DELETE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.DELETE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 410 injected into first preferred region for PATCH_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.PATCH_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 410 injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Server-generated 410 injected into first preferred region for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited. Even
            // when short-circuiting of first preferred region has kicked in, the first preferred region is contacted
            // to fetch the QueryPlan.
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Response-delay injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException (since end-to-end timeout is configured)
            // and only to succeed when moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with response delay in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withResponseDelay(Duration.ofSeconds(6)),
                this.buildTransitTimeoutFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Response-delay injected into first preferred region for REPLACE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException (since end-to-end timeout is configured)
            // and only to succeed when moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with response delay in first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withResponseDelay(Duration.ofSeconds(6)),
                this.buildTransitTimeoutFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Response-delay injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit RequestTimeoutException (due to network request timeout of 5s kicking in)
            // and because NonIdempotentWriteRetryPolicy isn't enabled
            // and only to succeed when moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with response delay in first preferred region and with no end-to-end operation timeout configured.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(80))
                    .withResponseDelay(Duration.ofSeconds(10)),
                this.buildTransitTimeoutFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasRequestTimeoutException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // Response-delay injected into first preferred region for REPLACE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit RequestTimeoutException (due to network request timeout of 5s kicking in)
            // and because NonIdempotentWriteRetryPolicy isn't enabled
            // and only to succeed when moved over to the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with response delay in first preferred region and with no end-to-end operation timeout configured.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(80))
                    .withResponseDelay(Duration.ofSeconds(10)),
                this.buildTransitTimeoutFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasRequestTimeoutException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
//             500 (internal server error) injected into first preferred region for READ_ITEM operation
//             injected into all replicas of the faulty EPK range.
//             Expectation is for the operation to fail with 500 until short-circuiting kicks in where the operation
//             should see a success from the second preferred region.
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 500 (internal server error) injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to fail with 500 until short-circuiting kicks in where the operation
            // should see a success from the second preferred region.
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildInternalServerErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 500 (internal server error) injected into first preferred region for READ_FEED_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to fail with 500 until short-circuiting kicks in where the operation
            // should see a success from the second preferred region.
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.READ_FEED_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 500 (internal server error) injected into first preferred region for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to fail with 500 until short-circuiting kicks in where the operation
            // should see a success from the second preferred region. Although, after short-circuiting, a query operation
            // will see request for QueryPlan from the short-circuited region.
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 449 injected into first preferred region for READ_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 449 injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 449 injected into first preferred region for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            // QUERY_ITEM operation will see requests hit even for short-circuited region for fetching the QueryPlan.
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 404/1002 injected into first preferred region for READ_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                String.format("Test with faulty %s with read session not available in the first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 404/1002 injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                String.format("Test with faulty %s with write session not available error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 449 injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                String.format("Test with faulty %s with retry with service error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildRetryWithFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 449 injected into first preferred region for REPLACE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                String.format("Test with faulty %s with retry with service error in the first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildRetryWithFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 449 injected into first preferred region for READ_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to see a success for all runs (due to threshold-based availability strategy enabled)
            // and only from the second preferred region when short-circuiting has kicked in for the first preferred region.
            new Object[]{
                String.format("Test with faulty %s with too many requests error in first preferred region with threshold-based availability strategy enabled.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITH_THRESHOLD_BASED_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 449 injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to see a success for all runs (due to threshold-based availability strategy enabled & non-idempotent write retry policy enabled)
            // and only from the second preferred region when short-circuiting has kicked in for the first preferred region.
            new Object[]{
                String.format("Test with faulty %s with too many requests error in first preferred region with threshold-based availability strategy enabled.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITH_THRESHOLD_BASED_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
//             449 injected into first preferred region for QUERY_ITEM operation
//             injected into all replicas of the faulty EPK range.
//             Expectation is for the operation to see a success for all runs (due to threshold-based availability strategy enabled & non-idempotent write retry policy enabled)
//             and will have two regions contacted post circuit breaking (one for QueryPlan and the other for the data plane request).
            new Object[]{
                String.format("Test with faulty %s with too many requests error in first preferred region with threshold-based availability strategy enabled.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITH_THRESHOLD_BASED_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 500 injected into all regions for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to see InternalServerError in all regions
            // and will have one region contacted post circuit breaking (one for QueryPlan and the other for the data plane request).
            new Object[]{
                String.format("Test with faulty %s with internal server error in all preferred regions.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasAtMostTwoPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                this.writeRegions.size(),
                40,
                15
            },
//             500 injected into all regions for READ_ITEM operation
//             injected into all replicas of the faulty EPK range.
//             Expectation is for the operation to see InternalServerError in all regions
//             and will contact one region contacted post circuit breaking.
            new Object[]{
                String.format("Test with faulty %s with internal server error in all preferred regions.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                this.writeRegions.size(),
                40,
                15
            },
            // 500 injected into all regions for UPSERT_ITEM operation
            // injected into all replicas of the faulty EPK range (effectively the primary since it is an upsert (write) operation).
            // Expectation is for the operation to see InternalServerError in all regions
            // and will contact one region contacted post circuit breaking.
            new Object[]{
                String.format("Test with faulty %s with internal server error in all preferred regions.", FaultInjectionOperationType.UPSERT_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(5),
                this.buildInternalServerErrorFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                this.writeRegions.size(),
                25,
                15
            }
        };
    }

    @DataProvider(name = "miscellaneousOpTestConfigsGateway")
    public Object[][] miscellaneousOpTestConfigsGateway() {

        // General testing flow:
        // Below tests choose a fault type to inject, regions to inject the fault in
        // and the operation type for which the fault is injected. The idea is to assert
        // what happens when faults are being injected - should an exception bubble up
        // in the process [or] should the operation succeed, region contacted when circuit
        // breaking has kicked in and region contacted when region + partition combination is
        // being marked back as UnhealthyTentative (eligible to accept requests)
        return new Object[][]{
            // Server-generated 503 injected into first preferred region for READ_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                validateResponseHasSuccess,
                validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for UPSERT_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.UPSERT_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for REPLACE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for DELETE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.DELETE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for PATCH_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.PATCH_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to succeed in all runs but include
            // the second preferred region when the first preferred region has been short-circuited.
            // For queries which require a QueryPlan, the first preferred region is contacted (not a data plane request
            // which will hit a data partition so is not eligible for circuit breaking).
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for BATCH_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.BATCH_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // Server-generated 503 injected into first preferred region for READ_FEED_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.READ_FEED_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
//             500 (internal server error) injected into first preferred region for READ_ITEM operation
//             injected into all replicas of the faulty EPK range.
//             Expectation is for the operation to fail with 500 until short-circuiting kicks in where the operation
//             should see a success from the second preferred region.
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // 500 (internal server error) injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to fail with 500 until short-circuiting kicks in where the operation
            // should see a success from the second preferred region.
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(5),
                this.buildInternalServerErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // 500 (internal server error) injected into first preferred region for READ_FEED_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to fail with 500 until short-circuiting kicks in where the operation
            // should see a success from the second preferred region.
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.READ_FEED_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // 500 (internal server error) injected into first preferred region for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to fail with 500 until short-circuiting kicks in where the operation
            // should see a success from the second preferred region. Although, after short-circuiting, a query operation
            // will see request for QueryPlan from the short-circuited region.
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // 429 injected into first preferred region for READ_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // 429 injected into first preferred region for CREATE_ITEM operation
            // injected into all replicas of the faulty EPK range (although only the primary replica
            // is ever involved - effectively doesn't impact the assertions for this test).
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // 429 injected into first preferred region for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            // QUERY_ITEM operation will see requests hit even for short-circuited region for fetching the QueryPlan.
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                1,
                15,
                15
            },
            // 500 injected into all regions for QUERY_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to see InternalServerError in all regions
            // and will have one region contacted post circuit breaking (one for QueryPlan and the other for the data plane request).
            new Object[]{
                String.format("Test with faulty %s with internal server error in all preferred regions.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasAtMostTwoPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                this.writeRegions.size(),
                40,
                15
            },
            // 500 injected into all regions for READ_ITEM operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to see InternalServerError in all regions
            // and will contact one region contacted post circuit breaking.
            new Object[]{
                String.format("Test with faulty %s with internal server error in all preferred regions.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(10),
                this.buildInternalServerErrorFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                this.writeRegions.size(),
                40,
                15
            },
            // 500 injected into all regions for UPSERT_ITEM operation
            // injected into all replicas of the faulty EPK range (effectively the primary since it is an upsert (write) operation).
            // Expectation is for the operation to see InternalServerError in all regions
            // and will contact one region contacted post circuit breaking.
            new Object[]{
                String.format("Test with faulty %s with internal server error in all preferred regions.", FaultInjectionOperationType.UPSERT_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(5),
                this.buildInternalServerErrorFaultInjectionRules,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                !NON_IDEMPOTENT_WRITE_RETRIES_ENABLED,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_GATEWAY_MODE,
                this.writeRegions.size(),
                25,
                15
            }
        };
    }

    @DataProvider(name = "readManyTestConfigs")
    public Object[][] readManyTestConfigs() {

        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeReadManyOperation = (paramsWrapper) -> {
            CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
            List<CosmosItemIdentity> itemIdentities = paramsWrapper.itemIdentitiesForReadManyOperation;
            CosmosReadManyRequestOptions readManyRequestOptions = paramsWrapper.readManyRequestOptions;

            try {

                FeedResponse<TestObject> response = asyncContainer.readMany(
                        itemIdentities,
                        readManyRequestOptions,
                        TestObject.class)
                    .block();

                return new ResponseWrapper<>(response);
            } catch (Exception ex) {

                if (ex instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                    return new ResponseWrapper<>(cosmosException);
                }

                throw ex;
            }
        };

        return new Object[][]{
            // Server-generated 503 injected into first preferred region for read many operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read many operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(10)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildServiceUnavailableFaultInjectionRules,
                executeReadManyOperation,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                1,
                15,
                15
            },
            // Internal server error injected into first preferred region for read many operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit InternalServerError and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read many operation injected with internal server error injected in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(10)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildInternalServerErrorFaultInjectionRules,
                executeReadManyOperation,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                1,
                15,
                15
            },
            // Server-generated 410 injected into first preferred region for read many operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read many operation injected with server-generated gone in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                executeReadManyOperation,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 429 injected into first preferred region for read many operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read many operation injected with too many requests error in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                executeReadManyOperation,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                1,
                15,
                15
            },
            // 404/1002 injected into first preferred region for read many operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read many operation injected with read session not available error in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                executeReadManyOperation,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 500 injected into all region for read many operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit 503 and only to succeed when
            // fault injection has hit its injection limits. Also, the success is
            // from the first preferred region.
            {
                "Test read many operation injected with internal server error in all preferred regions.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(10)
                    .withFaultInjectionApplicableRegions(this.writeRegions),
                this.buildInternalServerErrorFaultInjectionRules,
                executeReadManyOperation,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                this.writeRegions.size(),
                40,
                15
            },
            // 429 injected into first preferred region for read many operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to see a success for all runs (due to threshold-based availability strategy enabled)
            // and only from the second preferred region when short-circuiting has kicked in for the first preferred region.
            new Object[]{
                "Test faulty read many operation with too many requests error in first preferred region with threshold-based availability strategy enabled.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                executeReadManyOperation,
                THREE_SECOND_END_TO_END_TIMEOUT_WITH_THRESHOLD_BASED_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                1,
                15,
                15
            }
        };
    }

    @DataProvider(name = "readAllTestConfigs")
    public Object[][] readAllTestConfigs() {

        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeReadAllOperation = (paramsWrapper) -> {
            CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
            PartitionKey partitionKey = paramsWrapper.partitionKeyForReadAllOperation;
            CosmosQueryRequestOptions queryRequestOptions = paramsWrapper.queryRequestOptions;

            try {

                FeedResponse<TestObject> response = asyncContainer.readAllItems(
                        partitionKey,
                        queryRequestOptions,
                        TestObject.class)
                    .byPage()
                    .next()
                    .block();

                return new ResponseWrapper<>(response);
            } catch (Exception ex) {

                if (ex instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                    return new ResponseWrapper<>(cosmosException);
                }

                throw ex;
            }
        };

        return new Object[][]{
            // Server-generated 503 injected into first preferred region for read all operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to succeed in all runs but to move over to
            // the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read all operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(10)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildServiceUnavailableFaultInjectionRules,
                executeReadAllOperation,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                1,
                15,
                15
            },
            // Internal server error injected into first preferred region for read all operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit InternalServerError and bubble it from the first preferred region
            // and only to succeed when moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read all operation injected with internal server error injected in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(10)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildInternalServerErrorFaultInjectionRules,
                executeReadAllOperation,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                1,
                15,
                15
            },
            // 410 injected into first preferred region for read all operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read all operation injected with server-generated GONE in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildServerGeneratedGoneErrorFaultInjectionRules,
                executeReadAllOperation,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 429 injected into first preferred region for read all operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read all operation injected with too many requests error in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                executeReadAllOperation,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                1,
                15,
                15
            },
            // 404/1002 injected into first preferred region for read all operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit OperationCancelledException and only to succeed when
            // moved over to the second preferred region when the first preferred region has been short-circuited.
            {
                "Test read all operation injected with read/write session not available error in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                executeReadAllOperation,
                THREE_SECOND_END_TO_END_TIMEOUT_WITHOUT_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ONLY_DIRECT_MODE,
                1,
                15,
                15
            },
            // 500 injected into all region for read all operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to hit 503 and only to succeed when
            // fault injection has hit its injection limits. Also, the success is
            // from the first preferred region.
            {
                "Test read all operation injected with internal server error in all preferred regions.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(10)
                    .withFaultInjectionApplicableRegions(this.writeRegions),
                this.buildInternalServerErrorFaultInjectionRules,
                executeReadAllOperation,
                NO_END_TO_END_TIMEOUT,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasOnePreferredRegion,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                this.writeRegions.size(),
                40,
                15
            },
            // 429 injected into first preferred region for read all operation
            // injected into all replicas of the faulty EPK range.
            // Expectation is for the operation to see a success for all runs (due to threshold-based availability strategy enabled)
            // and only from the second preferred region when short-circuiting has kicked in for the first preferred region.
            new Object[]{
                "Test faulty read all operation with too many requests error in first preferred region with threshold-based availability strategy enabled.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                executeReadAllOperation,
                THREE_SECOND_END_TO_END_TIMEOUT_WITH_THRESHOLD_BASED_AVAILABILITY_STRATEGY,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ALL_CONNECTION_MODES_INCLUDED,
                1,
                15,
                15
            }
        };
    }

    @DataProvider(name = "gatewayRoutedFailureParametersDataProvider_ReadAll")
    public Object[][] gatewayRoutedFailureParametersDataProvider_ReadAll() {

        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeReadAllOperation = (paramsWrapper) -> {
            CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
            PartitionKey partitionKey = paramsWrapper.partitionKeyForReadAllOperation;
            CosmosQueryRequestOptions queryRequestOptions = paramsWrapper.queryRequestOptions;

            try {

                FeedResponse<TestObject> response = asyncContainer.readAllItems(
                        partitionKey,
                        queryRequestOptions,
                        TestObject.class)
                    .byPage()
                    .next()
                    .block();

                return new ResponseWrapper<>(response);
            } catch (Exception ex) {

                if (ex instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                    return new ResponseWrapper<>(cosmosException);
                }

                throw ex;
            }
        };

        return new Object[][]{
            {
                "Test read all operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                executeReadAllOperation,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ALL_CONNECTION_MODES_INCLUDED
            },
            // todo: for read all and read many - collection resolution and pkRange resolution happens
            // todo: outside the document retry loop so the operation fails with 404:1002
            // todo: weird thing is the operation succeeds when the client is in DIRECT connectivity mode
            // todo: track this
//            {
//                "Test read all operation injected with read session not available in first preferred region.",
//                new FaultInjectionRuleParamsWrapper()
//                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
//                    .withOverrideFaultInjectionOperationType(true)
//                    .withHitLimit(3)
//                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
//                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
//                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
//                executeReadAllOperation,
//                NO_REGION_SWITCH_HINT,
//                this.validateResponseHasSuccess,
//                ONLY_DIRECT_MODE
//            },
            {
                "Test read all operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                executeReadAllOperation,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ALL_CONNECTION_MODES_INCLUDED
            }
        };
    }

    @DataProvider(name = "gatewayRoutedFailuresParametersDataProvider_ReadMany")
    public Object[][] gatewayRoutedFailuresParametersDataProvider_ReadMany() {

        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeReadManyOperation = (paramsWrapper) -> {
            CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
            List<CosmosItemIdentity> itemIdentities = paramsWrapper.itemIdentitiesForReadManyOperation;
            CosmosReadManyRequestOptions readManyRequestOptions = paramsWrapper.readManyRequestOptions;

            try {

                FeedResponse<TestObject> response = asyncContainer.readMany(
                        itemIdentities,
                        readManyRequestOptions,
                        TestObject.class)
                    .block();

                return new ResponseWrapper<>(response);
            } catch (Exception ex) {

                if (ex instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                    return new ResponseWrapper<>(cosmosException);
                }

                throw ex;
            }
        };

        return new Object[][]{
            {
                "Test read many operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                executeReadManyOperation,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ALL_CONNECTION_MODES_INCLUDED
            },
            // todo: for read all and read many - collection resolution and pkRange resolution happens
            // todo: outside the document retry loop so the operation fails with 404:1002
            // todo: weird thing is the operation succeeds when the client is in DIRECT connectivity mode
            // todo: track this
//            {
//                "Test read many operation injected with read session not available in first preferred region.",
//                new FaultInjectionRuleParamsWrapper()
//                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
//                    .withOverrideFaultInjectionOperationType(true)
//                    .withHitLimit(3)
//                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
//                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
//                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
//                executeReadManyOperation,
//                NO_REGION_SWITCH_HINT,
//                this.validateResponseHasSuccess,
//                ALL_CONNECTION_MODES_INCLUDED
//            },
            {
                "Test read many operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                executeReadManyOperation,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ALL_CONNECTION_MODES_INCLUDED
            }
        };
    }

    @DataProvider(name = "gatewayRoutedFailuresParametersDataProviderMiscGateway")
    public Object[][] gatewayRoutedFailuresParametersDataProviderMiscGateway() {

        return new Object[][]{
            {
                "Test read operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test read operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test read operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test create operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test create operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test create operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test upsert operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test upsert operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test upsert operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test replace operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test replace operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test replace operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test delete operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test delete operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test delete operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test patch operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test patch operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test patch operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test batch operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test batch operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test batch operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test query operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test query operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test query operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test read feed operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test read feed operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            },
            {
                "Test read feed operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_GATEWAY_MODE
            }
        };
    }

    @DataProvider(name = "gatewayRoutedFailuresParametersDataProviderMiscDirect")
    public Object[][] gatewayRoutedFailuresParametersDataProviderMiscDirect() {

        return new Object[][]{
            {
                "Test read operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test read operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test read operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test create operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test create operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test create operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test upsert operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test upsert operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test upsert operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test replace operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test replace operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test replace operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test delete operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test delete operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test delete operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test patch operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test patch operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test patch operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test batch operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test batch operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test batch operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test query operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test query operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test query operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test read feed operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildServiceUnavailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test read feed operation injected with read session not available in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildReadWriteSessionNotAvailableFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            },
            {
                "Test read feed operation injected with too many requests exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withOverrideFaultInjectionOperationType(true)
                    .withHitLimit(3)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionConnectionType(FaultInjectionConnectionType.GATEWAY),
                this.buildTooManyRequestsErrorFaultInjectionRules,
                NO_REGION_SWITCH_HINT,
                this.validateResponseHasSuccess,
                ONLY_DIRECT_MODE
            }
        };
    }

    @Test(groups = {"circuit-breaker-misc-direct"}, dataProvider = "miscellaneousOpTestConfigsDirect", timeOut = 4 * TIMEOUT)
    public void miscellaneousDocumentOperationHitsTerminalExceptionAcrossKRegionsDirect(
        String testId,
        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
        CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicyCfg,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled,
        Consumer<ResponseWrapper<?>> validateResponseInPresenceOfFaults,
        Consumer<ResponseWrapper<?>> validateResponseInAbsenceOfFaults,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitingHasKickedIn,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenExceptionBubblesUp,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
        Set<ConnectionMode> allowedConnectionModes,
        int expectedRegionCountWithFailures,
        int operationIterationCountInFailureFlow,
        int operationIterationCountInRecoveryFlow) {

        executeMiscOperationHitsTerminalExceptionAcrossKRegions(
            testId,
            faultInjectionRuleParamsWrapper,
            generateFaultInjectionRules,
            e2eLatencyPolicyCfg,
            regionSwitchHint,
            nonIdempotentWriteRetriesEnabled,
            validateResponseInPresenceOfFaults,
            validateResponseInAbsenceOfFaults,
            validateRegionsContactedWhenShortCircuitingHasKickedIn,
            validateRegionsContactedWhenExceptionBubblesUp,
            validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
            allowedConnectionModes,
            expectedRegionCountWithFailures,
            operationIterationCountInFailureFlow,
            operationIterationCountInRecoveryFlow);
    }

    @Test(groups = {"circuit-breaker-misc-gateway"}, dataProvider = "miscellaneousOpTestConfigsGateway", timeOut = 4 * TIMEOUT)
    public void miscellaneousDocumentOperationHitsTerminalExceptionAcrossKRegionsGateway(
        String testId,
        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
        CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicyCfg,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled,
        Consumer<ResponseWrapper<?>> validateResponseInPresenceOfFaults,
        Consumer<ResponseWrapper<?>> validateResponseInAbsenceOfFaults,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitingHasKickedIn,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenExceptionBubblesUp,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
        Set<ConnectionMode> allowedConnectionModes,
        int expectedRegionCountWithFailures,
        int operationIterationCountInFailureFlow,
        int operationIterationCountInRecoveryFlow) {

        executeMiscOperationHitsTerminalExceptionAcrossKRegions(
            testId,
            faultInjectionRuleParamsWrapper,
            generateFaultInjectionRules,
            e2eLatencyPolicyCfg,
            regionSwitchHint,
            nonIdempotentWriteRetriesEnabled,
            validateResponseInPresenceOfFaults,
            validateResponseInAbsenceOfFaults,
            validateRegionsContactedWhenShortCircuitingHasKickedIn,
            validateRegionsContactedWhenExceptionBubblesUp,
            validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
            allowedConnectionModes,
            expectedRegionCountWithFailures,
            operationIterationCountInFailureFlow,
            operationIterationCountInRecoveryFlow);
    }

    private void executeMiscOperationHitsTerminalExceptionAcrossKRegions(
        String testId,
        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
        CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicyCfg,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled,
        Consumer<ResponseWrapper<?>> validateResponseInPresenceOfFaults,
        Consumer<ResponseWrapper<?>> validateResponseInAbsenceOfFaults,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitingHasKickedIn,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenExceptionBubblesUp,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
        Set<ConnectionMode> allowedConnectionModes,
        int expectedRegionCountWithFailures,
        int operationIterationCountInFailureFlow,
        int operationIterationCountInRecoveryFlow) {

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (!allowedConnectionModes.contains(connectionPolicy.getConnectionMode())) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", connectionPolicy.getConnectionMode()));
        }

        CosmosAsyncClient asyncClient = null;
        FaultInjectionOperationType faultInjectionOperationType = faultInjectionRuleParamsWrapper.getFaultInjectionOperationType();
        faultInjectionRuleParamsWrapper.withFaultInjectionConnectionType(evaluateFaultInjectionConnectionType(connectionPolicy.getConnectionMode()));

        try {

            asyncClient = clientBuilder.buildAsyncClient();

            operationInvocationParamsWrapper.itemCountToBootstrapContainerFrom = resolveTestObjectCountToBootstrapFrom(faultInjectionRuleParamsWrapper.getFaultInjectionOperationType(), 15);
            int testObjCountToBootstrapFrom = operationInvocationParamsWrapper.itemCountToBootstrapContainerFrom;

            operationInvocationParamsWrapper.containerIdToTarget = resolveContainerIdByFaultInjectionOperationType(faultInjectionOperationType);

            validateNonEmptyString(operationInvocationParamsWrapper.containerIdToTarget);
            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);

            List<TestObject> testObjects = new ArrayList<>();

            for (int i = 1; i <= testObjCountToBootstrapFrom; i++) {
                TestObject testObject = TestObject.create();
                testObjects.add(testObject);
                asyncContainer.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
            }

            FeedRange faultyFeedRange;

            if (testObjects.size() != 1) {
                faultyFeedRange = FeedRange.forFullRange();
            } else {
                faultyFeedRange = FeedRange.forLogicalPartition(new PartitionKey(testObjects.get(0).getId()));
            }

            operationInvocationParamsWrapper.faultyFeedRange = faultyFeedRange;
            operationInvocationParamsWrapper.testObjectsForDataPlaneOperationToWorkWith = testObjects;

        } catch (Exception ex) {
            logger.error("Test failed with ex :", ex);
            fail(String.format("Test %s failed in bootstrap stage.", testId));
        } finally {
            safeClose(asyncClient);
        }

        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeDataPlaneOperation
            = resolveDataPlaneOperation(faultInjectionOperationType);

        operationInvocationParamsWrapper.itemRequestOptions = new CosmosItemRequestOptions();

        if (e2eLatencyPolicyCfg != null) {
            operationInvocationParamsWrapper.patchItemRequestOptions = new CosmosPatchItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicyCfg);

            operationInvocationParamsWrapper.queryRequestOptions = new CosmosQueryRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicyCfg);

            operationInvocationParamsWrapper.itemRequestOptions
                .setCosmosEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicyCfg);
        }

        if (nonIdempotentWriteRetriesEnabled) {
            operationInvocationParamsWrapper.itemRequestOptions
                .setNonIdempotentWriteRetryPolicy(true, true);
        }

        execute(
            testId,
            faultInjectionRuleParamsWrapper,
            operationInvocationParamsWrapper,
            generateFaultInjectionRules,
            executeDataPlaneOperation,
            regionSwitchHint,
            validateResponseInPresenceOfFaults,
            validateResponseInAbsenceOfFaults,
            validateRegionsContactedWhenShortCircuitingHasKickedIn,
            validateRegionsContactedWhenExceptionBubblesUp,
            validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
            expectedRegionCountWithFailures,
            operationIterationCountInFailureFlow,
            operationIterationCountInRecoveryFlow);
    }

    @Test(groups = {"circuit-breaker-read-all-read-many"}, dataProvider = "readManyTestConfigs", timeOut = 4 * TIMEOUT)
    public void readManyOperationHitsTerminalExceptionAcrossKRegions(
        String testId,
        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeDataPlaneOperation,
        CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicyCfg,
        CosmosRegionSwitchHint regionSwitchHint,
        Consumer<ResponseWrapper<?>> validateResponseInPresenceOfFaults,
        Consumer<ResponseWrapper<?>> validateResponseInAbsenceOfFaults,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitingHasKickedIn,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenExceptionBubblesUp,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
        Set<ConnectionMode> allowedConnectionModes,
        int expectedRegionCountWithFailures,
        int operationIterationCountInFailureFlow,
        int operationIterationCountInRecoveryFlow) {

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = this.writeRegions.get(0);
        this.secondPreferredRegion = this.writeRegions.get(1);

        CosmosAsyncClient asyncClient = null;

        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
        operationInvocationParamsWrapper.queryType = QueryType.READ_MANY;

        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (!allowedConnectionModes.contains(connectionPolicy.getConnectionMode())) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", connectionPolicy.getConnectionMode()));
        }

        faultInjectionRuleParamsWrapper.withFaultInjectionConnectionType(evaluateFaultInjectionConnectionType(connectionPolicy.getConnectionMode()));

        try {

            asyncClient = clientBuilder.buildAsyncClient();

            operationInvocationParamsWrapper.containerIdToTarget = this.sharedMultiPartitionAsyncContainerIdWhereMyPkIsPartitionKey;

            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);

            List<FeedRange> feedRanges = asyncContainer.getFeedRanges().block();

            assertThat(feedRanges).isNotNull().as("feedRanges is not expected to be null!");
            assertThat(feedRanges).isNotEmpty().as("feedRanges is not expected to be empty!");

            Map<String, List<CosmosItemIdentity>> partitionKeyToItemIdentityList = new HashMap<>();
            List<String> partitionKeys = new ArrayList<>();

            for (FeedRange ignored : feedRanges) {
                String pkForFeedRange = UUID.randomUUID().toString();

                partitionKeys.add(pkForFeedRange);
                partitionKeyToItemIdentityList.put(pkForFeedRange, new ArrayList<>());

                for (int i = 0; i < 10; i++) {
                    TestObject testObject = TestObject.create(pkForFeedRange);

                    partitionKeyToItemIdentityList.get(pkForFeedRange).add(new CosmosItemIdentity(new PartitionKey(pkForFeedRange), testObject.getId()));
                    asyncContainer.createItem(testObject, new PartitionKey(testObject.getMypk()), new CosmosItemRequestOptions()).block();
                }
            }

            CosmosReadManyRequestOptions readManyRequestOptions = new CosmosReadManyRequestOptions();

            if (e2eLatencyPolicyCfg != null) {
                readManyRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicyCfg);
            }

            operationInvocationParamsWrapper.readManyRequestOptions = readManyRequestOptions;
            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableAsyncContainer(asyncContainer);

            PartitionKey faultyPartitionKey = new PartitionKey(partitionKeys.get(0));
            FeedRange faultyFeedRange = FeedRange.forLogicalPartition(faultyPartitionKey);

            operationInvocationParamsWrapper.faultyFeedRange = faultyFeedRange;
            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableFeedRange(faultyFeedRange);

            operationInvocationParamsWrapper.itemIdentitiesForReadManyOperation = partitionKeyToItemIdentityList.get(partitionKeys.get(0));
        } catch (Exception ex) {
            logger.error("Test failed with ex :", ex);
            fail(String.format("Test %s failed in bootstrap stage.", testId));
        } finally {
            safeClose(asyncClient);
        }

        execute(
            testId,
            faultInjectionRuleParamsWrapper,
            operationInvocationParamsWrapper,
            generateFaultInjectionRules,
            executeDataPlaneOperation,
            regionSwitchHint,
            validateResponseInPresenceOfFaults,
            validateResponseInAbsenceOfFaults,
            validateRegionsContactedWhenShortCircuitingHasKickedIn,
            validateRegionsContactedWhenExceptionBubblesUp,
            validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
            expectedRegionCountWithFailures,
            operationIterationCountInFailureFlow,
            operationIterationCountInRecoveryFlow);
    }

    @Test(groups = {"circuit-breaker-read-all-read-many"}, dataProvider = "readAllTestConfigs", timeOut = 4 * TIMEOUT)
    public void readAllOperationHitsTerminalExceptionAcrossKRegions(
        String testId,
        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeDataPlaneOperation,
        CosmosEndToEndOperationLatencyPolicyConfig e2eLatencyPolicyCfg,
        CosmosRegionSwitchHint regionSwitchHint,
        Consumer<ResponseWrapper<?>> validateResponseInPresenceOfFaults,
        Consumer<ResponseWrapper<?>> validateResponseInAbsenceOfFaults,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitingHasKickedIn,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenExceptionBubblesUp,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
        Set<ConnectionMode> allowedConnectionModes,
        int expectedRegionCountWithFailures,
        int operationIterationCountInFailureFlow,
        int operationIterationCountInRecoveryFlow) {

        CosmosAsyncClient asyncClient = null;

        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
        operationInvocationParamsWrapper.queryType = QueryType.READ_ALL;

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (!allowedConnectionModes.contains(connectionPolicy.getConnectionMode())) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", connectionPolicy.getConnectionMode()));
        }

        faultInjectionRuleParamsWrapper.withFaultInjectionConnectionType(evaluateFaultInjectionConnectionType(connectionPolicy.getConnectionMode()));

        try {

            asyncClient = clientBuilder.buildAsyncClient();

            operationInvocationParamsWrapper.containerIdToTarget = this.sharedMultiPartitionAsyncContainerIdWhereMyPkIsPartitionKey;

            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);
            deleteAllDocuments(asyncContainer);

            List<FeedRange> feedRanges = asyncContainer.getFeedRanges().block();

            assertThat(feedRanges).isNotNull().as("feedRanges is not expected to be null!");
            assertThat(feedRanges).isNotEmpty().as("feedRanges is not expected to be empty!");

            Map<String, List<CosmosItemIdentity>> partitionKeyToItemIdentityList = new HashMap<>();
            List<String> partitionKeys = new ArrayList<>();

            for (FeedRange ignored : feedRanges) {
                String pkForFeedRange = UUID.randomUUID().toString();

                partitionKeys.add(pkForFeedRange);
                partitionKeyToItemIdentityList.put(pkForFeedRange, new ArrayList<>());

                for (int i = 0; i < 10; i++) {
                    TestObject testObject = TestObject.create(pkForFeedRange);

                    partitionKeyToItemIdentityList.get(pkForFeedRange).add(new CosmosItemIdentity(new PartitionKey(pkForFeedRange), testObject.getId()));
                    asyncContainer.createItem(testObject, new PartitionKey(testObject.getMypk()), new CosmosItemRequestOptions()).block();
                }
            }

            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();

            if (e2eLatencyPolicyCfg != null) {
                queryRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2eLatencyPolicyCfg);
            }

            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableAsyncContainer(asyncContainer);

            PartitionKey faultyPartitionKey = new PartitionKey(partitionKeys.get(0));
            FeedRange faultyFeedRange = FeedRange.forLogicalPartition(faultyPartitionKey);

            operationInvocationParamsWrapper.faultyFeedRange = faultyFeedRange;
            operationInvocationParamsWrapper.partitionKeyForReadAllOperation = faultyPartitionKey;
            operationInvocationParamsWrapper.queryRequestOptions = queryRequestOptions;

            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableFeedRange(faultyFeedRange);
        } catch (Exception ex) {
            logger.error("Test failed with ex :", ex);
            fail(String.format("Test %s failed in bootstrap stage.", testId));
        } finally {
            safeClose(asyncClient);
        }

        execute(
            testId,
            faultInjectionRuleParamsWrapper,
            operationInvocationParamsWrapper,
            generateFaultInjectionRules,
            executeDataPlaneOperation,
            regionSwitchHint,
            validateResponseInPresenceOfFaults,
            validateResponseInAbsenceOfFaults,
            validateRegionsContactedWhenShortCircuitingHasKickedIn,
            validateRegionsContactedWhenExceptionBubblesUp,
            validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
            expectedRegionCountWithFailures,
            operationIterationCountInFailureFlow,
            operationIterationCountInRecoveryFlow);
    }

    private void execute(
        String testId,
        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
        OperationInvocationParamsWrapper operationInvocationParamsWrapper,
        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeDataPlaneOperation,
        CosmosRegionSwitchHint regionSwitchHint,
        Consumer<ResponseWrapper<?>> validateResponseInPresenceOfFailures,
        Consumer<ResponseWrapper<?>> validateResponseInAbsenceOfFailures,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitingHasKickedIn,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenExceptionBubblesUp,
        Consumer<CosmosDiagnosticsContext> validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative,
        int expectedRegionCountWithFailures,
        int operationIterationCountInFailureFlow,
        int operationIterationCountInRecoveryFlow) {

        logger.info("Checking circuit breaking behavior for test type {}", testId);

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);


        boolean shouldInjectEmptyPreferredRegions = ThreadLocalRandom.current().nextBoolean();

        //if (shouldInjectEmptyPreferredRegions) {
            clientBuilder = clientBuilder
                .preferredRegions(Collections.emptyList());
        //}

        System.setProperty(
            "COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG",
            "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                + "\"circuitBreakerType\": \"CONSECUTIVE_EXCEPTION_COUNT_BASED\","
                + "\"consecutiveExceptionCountToleratedForReads\": 10,"
                + "\"consecutiveExceptionCountToleratedForWrites\": 5,"
                + "}");

        if (regionSwitchHint != null) {
            clientBuilder = clientBuilder
                .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(regionSwitchHint).build());
        }

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();

        validateNonEmptyString(this.sharedAsyncDatabaseId);
        CosmosAsyncDatabase database = client.getDatabase(this.sharedAsyncDatabaseId);

        CosmosAsyncContainer container;

        try {

            validateNonEmptyString(operationInvocationParamsWrapper.containerIdToTarget);
            container = database.getContainer(operationInvocationParamsWrapper.containerIdToTarget);

            RxDocumentClientImpl documentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);

            RxCollectionCache collectionCache = ReflectionUtils.getClientCollectionCache(documentClient);
            RxPartitionKeyRangeCache partitionKeyRangeCache = ReflectionUtils.getPartitionKeyRangeCache(documentClient);

            GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
                = documentClient.getGlobalPartitionEndpointManagerForCircuitBreaker();

            Class<?>[] enclosedClasses = GlobalPartitionEndpointManagerForCircuitBreaker.class.getDeclaredClasses();
            Class<?> partitionLevelUnavailabilityInfoClass
                = getClassBySimpleName(enclosedClasses, "PartitionLevelLocationUnavailabilityInfo");
            assertThat(partitionLevelUnavailabilityInfoClass).isNotNull();

            Field partitionKeyRangeToLocationSpecificUnavailabilityInfoField
                = GlobalPartitionEndpointManagerForCircuitBreaker.class.getDeclaredField("partitionKeyRangeToLocationSpecificUnavailabilityInfo");
            partitionKeyRangeToLocationSpecificUnavailabilityInfoField.setAccessible(true);

            Field locationEndpointToLocationSpecificContextForPartitionField
                = partitionLevelUnavailabilityInfoClass.getDeclaredField("locationEndpointToLocationSpecificContextForPartition");
            locationEndpointToLocationSpecificContextForPartitionField.setAccessible(true);

            ConcurrentHashMap<PartitionKeyRangeWrapper, ?> partitionKeyRangeToLocationSpecificUnavailabilityInfo
                = (ConcurrentHashMap<PartitionKeyRangeWrapper, ?>) partitionKeyRangeToLocationSpecificUnavailabilityInfoField.get(globalPartitionEndpointManagerForCircuitBreaker);

            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableFeedRange(operationInvocationParamsWrapper.faultyFeedRange);
            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableAsyncContainer(container);

            Utils.ValueHolder<FeedRangeEpkImpl> faultyFeedRangeEpkImpl = new Utils.ValueHolder<>();
            Utils.ValueHolder<FeedRangePartitionKeyImpl> faultyFeedRangePartitionKeyImpl = new Utils.ValueHolder<>();
            Utils.ValueHolder<List<PartitionKeyRange>> faultyPartitionKeyRanges = new Utils.ValueHolder<>();
            Utils.ValueHolder<DocumentCollection> faultyDocumentCollection = new Utils.ValueHolder<>();

            assertThat(operationInvocationParamsWrapper.faultyFeedRange).isNotNull().as("Argument 'operationInvocationParamsWrapper.faultyFeedRange' cannot be null!");

            if (operationInvocationParamsWrapper.faultyFeedRange instanceof FeedRangeEpkImpl) {

                faultyFeedRangeEpkImpl.v = (FeedRangeEpkImpl) operationInvocationParamsWrapper.faultyFeedRange;

                collectionCache.resolveByNameAsync(null, containerAccessor.getLinkWithoutTrailingSlash(container), null)
                    .flatMap(collection -> {
                        faultyDocumentCollection.v = collection;
                        return partitionKeyRangeCache.tryGetOverlappingRangesAsync(null, collection.getResourceId(), faultyFeedRangeEpkImpl.v.getRange(), true, null);
                    })
                    .flatMap(listValueHolder -> {
                        faultyPartitionKeyRanges.v = listValueHolder.v;
                        return Mono.just(listValueHolder);
                    }).block();
            } else if (operationInvocationParamsWrapper.faultyFeedRange instanceof FeedRangePartitionKeyImpl) {

                faultyFeedRangePartitionKeyImpl.v = (FeedRangePartitionKeyImpl) operationInvocationParamsWrapper.faultyFeedRange;

                collectionCache.resolveByNameAsync(null, containerAccessor.getLinkWithoutTrailingSlash(container), null)
                    .flatMap(collection -> {
                        faultyDocumentCollection.v = collection;
                        return partitionKeyRangeCache.tryGetOverlappingRangesAsync(null, collection.getResourceId(), faultyFeedRangePartitionKeyImpl.v.getEffectiveRange(collection.getPartitionKey()), true, null);
                    })
                    .flatMap(listValueHolder -> {
                        faultyPartitionKeyRanges.v = listValueHolder.v;
                        return Mono.just(listValueHolder);
                    }).block();
            } else {
                fail("Argument 'operationInvocationParamsWrapper.faultyFeedRange' has to be a sub-type of FeedRangeEpkImpl or FeedRangePartitionKeyImpl!");
            }

            validateNonEmptyList(faultyPartitionKeyRanges.v);
            assertThat(faultyDocumentCollection.v).isNotNull();

            List<FaultInjectionRule> faultInjectionRules = generateFaultInjectionRules.apply(faultInjectionRuleParamsWrapper);

            if (faultInjectionRules != null && !faultInjectionRules.isEmpty()) {

                operationInvocationParamsWrapper.asyncContainer = container;
                operationInvocationParamsWrapper.feedRangeToDrainForChangeFeed = operationInvocationParamsWrapper.faultyFeedRange;
                operationInvocationParamsWrapper.feedRangeForQuery = operationInvocationParamsWrapper.faultyFeedRange;

                CosmosFaultInjectionHelper
                    .configureFaultInjectionRules(faultInjectionRuleParamsWrapper.getFaultInjectionApplicableAsyncContainer(), faultInjectionRules)
                    .block();

                boolean hasReachedCircuitBreakingThreshold = false;
                int executionCountAfterCircuitBreakingThresholdBreached = 0;

                List<TestObject> testObjects = operationInvocationParamsWrapper.testObjectsForDataPlaneOperationToWorkWith;
                PartitionKeyRangeWrapper partitionKeyRangeWrapper
                    = new PartitionKeyRangeWrapper(faultyPartitionKeyRanges.v.get(0), faultyDocumentCollection.v.getResourceId());

                for (int i = 1; i <= operationIterationCountInFailureFlow; i++) {

                    if (!(operationInvocationParamsWrapper.queryType == QueryType.READ_MANY || operationInvocationParamsWrapper.queryType == QueryType.READ_ALL)) {
                        operationInvocationParamsWrapper.createdTestObject = testObjects.isEmpty() ? null : testObjects.get(i % testObjects.size());
                    } else if (operationInvocationParamsWrapper.queryType == QueryType.READ_MANY) {
                        validateNonEmptyList(operationInvocationParamsWrapper.itemIdentitiesForReadManyOperation);
                    }

                    ResponseWrapper<?> response = executeDataPlaneOperation.apply(operationInvocationParamsWrapper);

                    ConsecutiveExceptionBasedCircuitBreaker consecutiveExceptionBasedCircuitBreaker
                        = globalPartitionEndpointManagerForCircuitBreaker.getConsecutiveExceptionBasedCircuitBreaker();

                    int expectedCircuitBreakingThreshold
                        = doesOperationHaveWriteSemantics(faultInjectionRuleParamsWrapper.getFaultInjectionOperationType()) ?
                        consecutiveExceptionBasedCircuitBreaker.getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyWithFailures, false) :
                        consecutiveExceptionBasedCircuitBreaker.getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyWithFailures, true);

                    if (!hasReachedCircuitBreakingThreshold) {

                        hasReachedCircuitBreakingThreshold = (expectedCircuitBreakingThreshold - 1) ==
                            getEstimatedFailureCountSeenPerRegionPerPartitionKeyRange(
                                partitionKeyRangeWrapper,
                                partitionKeyRangeToLocationSpecificUnavailabilityInfo,
                                locationEndpointToLocationSpecificContextForPartitionField,
                                expectedCircuitBreakingThreshold - 1,
                                expectedRegionCountWithFailures);
                        validateResponseInPresenceOfFailures.accept(response);
                    } else {
                        executionCountAfterCircuitBreakingThresholdBreached++;
                    }

                    if (executionCountAfterCircuitBreakingThresholdBreached > 1) {
                        validateResponseInAbsenceOfFailures.accept(response);
                    }

                    if (response.cosmosItemResponse != null) {
                        assertThat(response.cosmosItemResponse).isNotNull();
                        assertThat(response.cosmosItemResponse.getDiagnostics()).isNotNull();

                        if (executionCountAfterCircuitBreakingThresholdBreached > 1) {
                            validateRegionsContactedWhenShortCircuitingHasKickedIn.accept(response.cosmosItemResponse.getDiagnostics().getDiagnosticsContext());
                        }
                    } else if (response.feedResponse != null) {
                        assertThat(response.feedResponse).isNotNull();
                        assertThat(response.feedResponse.getCosmosDiagnostics()).isNotNull();

                        if (executionCountAfterCircuitBreakingThresholdBreached > 1) {
                            validateRegionsContactedWhenShortCircuitingHasKickedIn.accept(response.feedResponse.getCosmosDiagnostics().getDiagnosticsContext());
                        }
                    } else if (response.cosmosException != null) {
                        assertThat(response.cosmosException).isNotNull();
                        assertThat(response.cosmosException.getDiagnostics()).isNotNull();

                        if (!hasReachedCircuitBreakingThreshold) {
                            CosmosDiagnosticsContext ctx = response.cosmosException.getDiagnostics().getDiagnosticsContext();

                            validateRegionsContactedWhenExceptionBubblesUp.accept(ctx);
                        }
                    } else if (response.batchResponse != null) {
                        assertThat(response.batchResponse).isNotNull();
                        assertThat(response.batchResponse.getDiagnostics()).isNotNull();

                        if (executionCountAfterCircuitBreakingThresholdBreached > 1) {
                            validateRegionsContactedWhenShortCircuitingHasKickedIn.accept(response.batchResponse.getDiagnostics().getDiagnosticsContext());
                        }
                    }
                }

                logger.info("Sleep for 90 seconds to allow Unavailable partitions to be HealthyTentative");
                Thread.sleep(90_000);

                for (int i = operationIterationCountInFailureFlow + 1; i <= operationIterationCountInFailureFlow + operationIterationCountInRecoveryFlow; i++) {

                    if (!(operationInvocationParamsWrapper.queryType == QueryType.READ_MANY || operationInvocationParamsWrapper.queryType == QueryType.READ_ALL)) {
                        operationInvocationParamsWrapper.createdTestObject = testObjects.isEmpty() ? null : testObjects.get(i % testObjects.size());
                    } else if (operationInvocationParamsWrapper.queryType == QueryType.READ_MANY) {
                        validateNonEmptyList(operationInvocationParamsWrapper.itemIdentitiesForReadManyOperation);
                    }

                    ResponseWrapper<?> response = executeDataPlaneOperation.apply(operationInvocationParamsWrapper);
                    validateResponseInAbsenceOfFailures.accept(response);

                    if (response.cosmosItemResponse != null) {
                        assertThat(response.cosmosItemResponse).isNotNull();
                        assertThat(response.cosmosItemResponse.getDiagnostics()).isNotNull();

                        validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative.accept(response.cosmosItemResponse.getDiagnostics().getDiagnosticsContext());
                    } else if (response.feedResponse != null) {
                        assertThat(response.feedResponse).isNotNull();
                        assertThat(response.feedResponse.getCosmosDiagnostics()).isNotNull();

                        validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative.accept(response.feedResponse.getCosmosDiagnostics().getDiagnosticsContext());
                    } else if (response.cosmosException != null) {
                        assertThat(response.cosmosException).isNotNull();
                        assertThat(response.cosmosException.getDiagnostics()).isNotNull();

                        response.cosmosException.getDiagnostics().getDiagnosticsContext().getContactedRegionNames().forEach(
                            regionContacted -> logger.info("Region contacted : {}", regionContacted)
                        );
                    } else if (response.batchResponse != null) {
                        assertThat(response.batchResponse).isNotNull();
                        assertThat(response.batchResponse.getDiagnostics()).isNotNull();

                        validateRegionsContactedWhenShortCircuitRegionMarkedAsHealthyOrHealthyTentative.accept(response.batchResponse.getDiagnostics().getDiagnosticsContext());
                    }
                }
            }
        } catch (InterruptedException ex) {
            fail("InterruptedException should not have been thrown!");
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Test should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
            safeClose(client);
        }
    }

    private static int resolveTestObjectCountToBootstrapFrom(FaultInjectionOperationType faultInjectionOperationType, int opCount) {
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

    // test whether the operation succeeds when there are availability issues (404:1002, 503, 429) in the primary region
    // for gateway routed requests
    @Test(groups = {"circuit-breaker-read-all-read-many"}, dataProvider = "gatewayRoutedFailureParametersDataProvider_ReadAll", timeOut = 4 * TIMEOUT)
    public void testReadAll_withAllGatewayRoutedOperationFailures(
        String testId,
        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeDataPlaneOperation,
        CosmosRegionSwitchHint regionSwitchHint,
        Consumer<ResponseWrapper<?>> validateResponse,
        Set<ConnectionMode> allowedConnectionModes) {

        System.setProperty(
            "COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG",
            "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                + "\"circuitBreakerType\": \"CONSECUTIVE_EXCEPTION_COUNT_BASED\","
                + "\"consecutiveExceptionCountToleratedForReads\": 10,"
                + "\"consecutiveExceptionCountToleratedForWrites\": 5,"
                + "}");

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (!allowedConnectionModes.contains(connectionPolicy.getConnectionMode())) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", connectionPolicy.getConnectionMode()));
        }

        CosmosAsyncClient asyncClient = null;
        FaultInjectionOperationType faultInjectionOperationType = faultInjectionRuleParamsWrapper.getFaultInjectionOperationType();
        faultInjectionRuleParamsWrapper.withFaultInjectionConnectionType(evaluateFaultInjectionConnectionType(connectionPolicy.getConnectionMode()));
        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
        List<TestObject> testObjects = new ArrayList<>();

        try {

            asyncClient = clientBuilder.buildAsyncClient();

            int testObjCountToBootstrapFrom = resolveTestObjectCountToBootstrapFrom(faultInjectionRuleParamsWrapper.getFaultInjectionOperationType(), 1);

            operationInvocationParamsWrapper.containerIdToTarget = resolveContainerIdByFaultInjectionOperationType(faultInjectionOperationType);

            validateNonEmptyString(operationInvocationParamsWrapper.containerIdToTarget);
            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);

            for (int i = 1; i <= testObjCountToBootstrapFrom; i++) {
                TestObject testObject = TestObject.create();
                testObjects.add(testObject);
                asyncContainer.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
            }

        } catch (Exception ex) {
            logger.error("Test failed with ex :", ex);
            fail(String.format("Test %s failed in bootstrap stage.", testId));
        } finally {
            safeClose(asyncClient);
        }

        try {
            asyncClient = clientBuilder.buildAsyncClient();

            if (regionSwitchHint != null) {
                clientBuilder = clientBuilder
                    .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(regionSwitchHint).build());
            }


            boolean shouldInjectEmptyPreferredRegions = ThreadLocalRandom.current().nextBoolean();

            if (shouldInjectEmptyPreferredRegions) {
                clientBuilder = clientBuilder
                    .preferredRegions(Collections.emptyList());
            }

            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);
            operationInvocationParamsWrapper.asyncContainer = asyncContainer;
            operationInvocationParamsWrapper.partitionKeyForReadAllOperation = new PartitionKey(testObjects.get(0).getMypk());
            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableAsyncContainer(asyncContainer);

            List<FaultInjectionRule> faultInjectionRules = generateFaultInjectionRules.apply(faultInjectionRuleParamsWrapper);

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(faultInjectionRuleParamsWrapper.getFaultInjectionApplicableAsyncContainer(), faultInjectionRules)
                .block();

            ResponseWrapper<?> responseWrapper = executeDataPlaneOperation.apply(operationInvocationParamsWrapper);

            validateResponse.accept(responseWrapper);
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Test should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
            safeClose(asyncClient);
        }
    }

    // test whether the operation succeeds when there are availability issues (404:1002, 503, 429) in the primary region
    // for gateway routed requests
    @Test(groups = {"circuit-breaker-read-all-read-many"}, dataProvider = "gatewayRoutedFailuresParametersDataProvider_ReadMany", timeOut = 4 * TIMEOUT)
    public void testReadMany_withAllGatewayRoutedOperationFailures(String testId,
                                                        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
                                                        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
                                                        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeDataPlaneOperation,
                                                        CosmosRegionSwitchHint regionSwitchHint,
                                                        Consumer<ResponseWrapper<?>> validateResponse,
                                                        Set<ConnectionMode> allowedConnectionModes) {
        System.setProperty(
            "COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG",
            "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                + "\"circuitBreakerType\": \"CONSECUTIVE_EXCEPTION_COUNT_BASED\","
                + "\"consecutiveExceptionCountToleratedForReads\": 10,"
                + "\"consecutiveExceptionCountToleratedForWrites\": 5,"
                + "}");

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        logger.info("Connection mode : {}", connectionPolicy.getConnectionMode());

        if (!allowedConnectionModes.contains(connectionPolicy.getConnectionMode())) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", connectionPolicy.getConnectionMode()));
        }

        CosmosAsyncClient asyncClient = null;
        faultInjectionRuleParamsWrapper.withFaultInjectionConnectionType(evaluateFaultInjectionConnectionType(connectionPolicy.getConnectionMode()));
        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();

        try {
            asyncClient = clientBuilder.buildAsyncClient();

            operationInvocationParamsWrapper.containerIdToTarget = this.sharedMultiPartitionAsyncContainerIdWhereMyPkIsPartitionKey;

            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);

            List<FeedRange> feedRanges = asyncContainer.getFeedRanges().block();

            assertThat(feedRanges).isNotNull().as("feedRanges is not expected to be null!");
            assertThat(feedRanges).isNotEmpty().as("feedRanges is not expected to be empty!");

            Map<String, List<CosmosItemIdentity>> partitionKeyToItemIdentityList = new HashMap<>();
            List<String> partitionKeys = new ArrayList<>();

            for (FeedRange ignored : feedRanges) {
                String pkForFeedRange = UUID.randomUUID().toString();

                partitionKeys.add(pkForFeedRange);
                partitionKeyToItemIdentityList.put(pkForFeedRange, new ArrayList<>());

                for (int i = 0; i < 10; i++) {
                    TestObject testObject = TestObject.create(pkForFeedRange);

                    partitionKeyToItemIdentityList.get(pkForFeedRange).add(new CosmosItemIdentity(new PartitionKey(pkForFeedRange), testObject.getId()));
                    asyncContainer.createItem(testObject, new PartitionKey(testObject.getMypk()), new CosmosItemRequestOptions()).block();
                }
            }

            CosmosReadManyRequestOptions readManyRequestOptions = new CosmosReadManyRequestOptions();

            operationInvocationParamsWrapper.readManyRequestOptions = readManyRequestOptions;
            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableAsyncContainer(asyncContainer);

            operationInvocationParamsWrapper.itemIdentitiesForReadManyOperation = partitionKeyToItemIdentityList.get(partitionKeys.get(0));

        } catch (Exception ex) {
            logger.error("Test failed with ex :", ex);
            fail(String.format("Test %s failed in bootstrap stage.", testId));
        } finally {
            safeClose(asyncClient);
        }

        try {

            if (regionSwitchHint != null) {
                clientBuilder = clientBuilder
                    .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(regionSwitchHint).build());
            }


            boolean shouldInjectEmptyPreferredRegions = ThreadLocalRandom.current().nextBoolean();

            if (shouldInjectEmptyPreferredRegions) {
                clientBuilder = clientBuilder
                    .preferredRegions(Collections.emptyList());
            }

            asyncClient = clientBuilder.buildAsyncClient();
            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);
            operationInvocationParamsWrapper.asyncContainer = asyncContainer;
            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableAsyncContainer(asyncContainer);

            List<FaultInjectionRule> faultInjectionRules = generateFaultInjectionRules.apply(faultInjectionRuleParamsWrapper);

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(faultInjectionRuleParamsWrapper.getFaultInjectionApplicableAsyncContainer(), faultInjectionRules)
                .block();

            ResponseWrapper<?> responseWrapper = executeDataPlaneOperation.apply(operationInvocationParamsWrapper);

            validateResponse.accept(responseWrapper);
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Test should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
            safeClose(asyncClient);
        }
    }

    // test whether the operation succeeds when there are availability issues (404:1002, 503, 429) in the primary region
    // for gateway routed requests
    @Test(groups = {"circuit-breaker-misc-gateway"}, dataProvider = "gatewayRoutedFailuresParametersDataProviderMiscGateway", timeOut = 4 * TIMEOUT)
    public void testMiscOperation_withAllGatewayRoutedOperationFailuresInPrimaryRegion_withGatewayConnectivity(
        String testId,
        FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
        CosmosRegionSwitchHint regionSwitchHint,
        Consumer<ResponseWrapper<?>> validateResponse,
        Set<ConnectionMode> allowedConnectionModes) {

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (!allowedConnectionModes.contains(connectionPolicy.getConnectionMode())) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", connectionPolicy.getConnectionMode()));
        }

        CosmosAsyncClient asyncClient = null;
        FaultInjectionOperationType faultInjectionOperationType = faultInjectionRuleParamsWrapper.getFaultInjectionOperationType();
        faultInjectionRuleParamsWrapper.withFaultInjectionConnectionType(evaluateFaultInjectionConnectionType(connectionPolicy.getConnectionMode()));
        List<TestObject> testObjects = new ArrayList<>();

        try {

            asyncClient = clientBuilder.buildAsyncClient();

            operationInvocationParamsWrapper.itemCountToBootstrapContainerFrom = resolveTestObjectCountToBootstrapFrom(faultInjectionRuleParamsWrapper.getFaultInjectionOperationType(), 15);
            int testObjCountToBootstrapFrom = operationInvocationParamsWrapper.itemCountToBootstrapContainerFrom;

            operationInvocationParamsWrapper.containerIdToTarget = resolveContainerIdByFaultInjectionOperationType(faultInjectionOperationType);

            validateNonEmptyString(operationInvocationParamsWrapper.containerIdToTarget);
            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);

            for (int i = 1; i <= testObjCountToBootstrapFrom; i++) {
                TestObject testObject = TestObject.create();
                testObjects.add(testObject);
                asyncContainer.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
            }

            operationInvocationParamsWrapper.testObjectsForDataPlaneOperationToWorkWith = testObjects;
            operationInvocationParamsWrapper.createdTestObject = testObjects.isEmpty() ? null : testObjects.get(0);

        } catch (Exception ex) {
            logger.error("Test failed with ex :", ex);
            fail(String.format("Test %s failed in bootstrap stage.", testId));
        } finally {
            safeClose(asyncClient);
        }

        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeDataPlaneOperation
            = resolveDataPlaneOperation(faultInjectionOperationType);

        operationInvocationParamsWrapper.itemRequestOptions = new CosmosItemRequestOptions();

        try {

            if (regionSwitchHint != null) {
                clientBuilder = clientBuilder
                    .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(regionSwitchHint).build());
            }

            boolean shouldInjectEmptyPreferredRegions = ThreadLocalRandom.current().nextBoolean();

            if (shouldInjectEmptyPreferredRegions) {
                clientBuilder = clientBuilder
                    .preferredRegions(Collections.emptyList());
            }

            asyncClient = clientBuilder.buildAsyncClient();
            CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);
            operationInvocationParamsWrapper.asyncContainer = asyncContainer;
            faultInjectionRuleParamsWrapper.withFaultInjectionApplicableAsyncContainer(asyncContainer);

            List<FaultInjectionRule> faultInjectionRules = generateFaultInjectionRules.apply(faultInjectionRuleParamsWrapper);

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(faultInjectionRuleParamsWrapper.getFaultInjectionApplicableAsyncContainer(), faultInjectionRules)
                .block();

            ResponseWrapper<?> responseWrapper = executeDataPlaneOperation.apply(operationInvocationParamsWrapper);

            validateResponse.accept(responseWrapper);
        } catch (Exception ex) {
            logger.error("Exception thrown :", ex);
            fail("Test should have passed!");
        } finally {
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
            safeClose(asyncClient);
        }
    }

    // test whether the operation succeeds when there are availability issues (404:1002, 503, 429) in the primary region
    // for gateway routed requests
    @Test(groups = {"circuit-breaker-misc-direct"}, dataProvider = "gatewayRoutedFailuresParametersDataProviderMiscDirect", timeOut = 4 * TIMEOUT)
    public void testMiscOperation_withAllGatewayRoutedOperationFailuresInPrimaryRegion_withDirectConnectivity(String testId,
                                                                                     FaultInjectionRuleParamsWrapper faultInjectionRuleParamsWrapper,
                                                                                     Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateFaultInjectionRules,
                                                                                     CosmosRegionSwitchHint regionSwitchHint,
                                                                                     Consumer<ResponseWrapper<?>> validateResponse,
                                                                                     Set<ConnectionMode> allowedConnectionModes) {
         List<String> preferredRegions = this.writeRegions;

         this.firstPreferredRegion = preferredRegions.get(0);
         this.secondPreferredRegion = preferredRegions.get(1);

         OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
         CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

         ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

         if (!allowedConnectionModes.contains(connectionPolicy.getConnectionMode())) {
             throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", connectionPolicy.getConnectionMode()));
         }

         CosmosAsyncClient asyncClient = null;
         FaultInjectionOperationType faultInjectionOperationType = faultInjectionRuleParamsWrapper.getFaultInjectionOperationType();
         faultInjectionRuleParamsWrapper.withFaultInjectionConnectionType(evaluateFaultInjectionConnectionType(connectionPolicy.getConnectionMode()));
         List<TestObject> testObjects = new ArrayList<>();

         try {

             asyncClient = clientBuilder.buildAsyncClient();

             operationInvocationParamsWrapper.itemCountToBootstrapContainerFrom = resolveTestObjectCountToBootstrapFrom(faultInjectionRuleParamsWrapper.getFaultInjectionOperationType(), 15);
             int testObjCountToBootstrapFrom = operationInvocationParamsWrapper.itemCountToBootstrapContainerFrom;

             operationInvocationParamsWrapper.containerIdToTarget = resolveContainerIdByFaultInjectionOperationType(faultInjectionOperationType);

             validateNonEmptyString(operationInvocationParamsWrapper.containerIdToTarget);
             CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);

             for (int i = 1; i <= testObjCountToBootstrapFrom; i++) {
                 TestObject testObject = TestObject.create();
                 testObjects.add(testObject);
                 asyncContainer.createItem(testObject, new PartitionKey(testObject.getId()), new CosmosItemRequestOptions()).block();
             }

             operationInvocationParamsWrapper.testObjectsForDataPlaneOperationToWorkWith = testObjects;
             operationInvocationParamsWrapper.createdTestObject = testObjects.isEmpty() ? null : testObjects.get(0);

         } catch (Exception ex) {
             logger.error("Test failed with ex :", ex);
             fail(String.format("Test %s failed in bootstrap stage.", testId));
         } finally {
             safeClose(asyncClient);
         }

         Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeDataPlaneOperation
             = resolveDataPlaneOperation(faultInjectionOperationType);

         operationInvocationParamsWrapper.itemRequestOptions = new CosmosItemRequestOptions();

         try {

             if (regionSwitchHint != null) {
                 clientBuilder = clientBuilder
                     .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(regionSwitchHint).build());
             }


             boolean shouldInjectEmptyPreferredRegions = ThreadLocalRandom.current().nextBoolean();

             if (shouldInjectEmptyPreferredRegions) {
                 clientBuilder = clientBuilder
                     .preferredRegions(Collections.emptyList());
             }

             asyncClient = clientBuilder.buildAsyncClient();
             CosmosAsyncContainer asyncContainer = asyncClient.getDatabase(this.sharedAsyncDatabaseId).getContainer(operationInvocationParamsWrapper.containerIdToTarget);
             operationInvocationParamsWrapper.asyncContainer = asyncContainer;
             faultInjectionRuleParamsWrapper.withFaultInjectionApplicableAsyncContainer(asyncContainer);

             List<FaultInjectionRule> faultInjectionRules = generateFaultInjectionRules.apply(faultInjectionRuleParamsWrapper);

             CosmosFaultInjectionHelper
                 .configureFaultInjectionRules(faultInjectionRuleParamsWrapper.getFaultInjectionApplicableAsyncContainer(), faultInjectionRules)
                 .block();

             ResponseWrapper<?> responseWrapper = executeDataPlaneOperation.apply(operationInvocationParamsWrapper);

             validateResponse.accept(responseWrapper);
         } catch (Exception ex) {
             logger.error("Exception thrown :", ex);
             fail("Test should have passed!");
         } finally {
             System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
             safeClose(asyncClient);
         }
     }

    private static Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> resolveDataPlaneOperation(FaultInjectionOperationType faultInjectionOperationType) {

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

                        return new ResponseWrapper<>(readItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
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

                        return new ResponseWrapper<>(upsertItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
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

                        return new ResponseWrapper<>(createItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
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

                        return new ResponseWrapper<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
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

                        return new ResponseWrapper<>(patchItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case QUERY_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    CosmosQueryRequestOptions queryRequestOptions = paramsWrapper.queryRequestOptions == null ? new CosmosQueryRequestOptions() : paramsWrapper.queryRequestOptions;
                    queryRequestOptions = paramsWrapper.feedRangeForQuery == null ? queryRequestOptions.setFeedRange(FeedRange.forFullRange()) : queryRequestOptions.setFeedRange(paramsWrapper.feedRangeForQuery);

                    try {

                        FeedResponse<TestObject> queryItemResponse = asyncContainer.queryItems(
                                "SELECT * FROM C",
                                queryRequestOptions,
                                TestObject.class)
                            .byPage()
                            .blockLast();

                        return new ResponseWrapper<>(queryItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
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

                        return new ResponseWrapper<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
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
                        return new ResponseWrapper<>(batchResponse);
                    } catch (Exception ex) {
                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case READ_FEED_ITEM:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    try {

                        FeedResponse<TestObject> feedResponseFromChangeFeed = asyncContainer.queryChangeFeed(
                                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(paramsWrapper.feedRangeToDrainForChangeFeed == null ? FeedRange.forFullRange() : paramsWrapper.feedRangeToDrainForChangeFeed),
                                TestObject.class)
                            .byPage()
                            .blockLast();

                        return new ResponseWrapper<>(feedResponseFromChangeFeed);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            default:
                throw new UnsupportedOperationException(String.format("Operation of type : %s is not supported", faultInjectionOperationType));
        }
    }

    private String resolveContainerIdByFaultInjectionOperationType(FaultInjectionOperationType faultInjectionOperationType) {
        switch (faultInjectionOperationType) {
            case READ_ITEM:
            case UPSERT_ITEM:
            case REPLACE_ITEM:
            case QUERY_ITEM:
            case PATCH_ITEM:
                return this.sharedMultiPartitionAsyncContainerIdWhereIdIsPartitionKey;
            case DELETE_ITEM:
            case CREATE_ITEM:
            case BATCH_ITEM:
            case READ_FEED_ITEM:
                return this.singlePartitionAsyncContainerId;
            default:
                throw new UnsupportedOperationException(String.format("Operation of type : %s is not supported", faultInjectionOperationType));
        }
    }

    @AfterClass(groups = {"circuit-breaker-misc-gateway", "circuit-breaker-misc-direct", "circuit-breaker-read-all-read-many"})
    public void afterClass() {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode();

        CosmosAsyncClient dummyClient = null;
        if (this.sharedAsyncDatabaseId != null) {
            try {

                dummyClient = clientBuilder.buildAsyncClient();

                CosmosAsyncDatabase sharedAsyncDatabase = dummyClient
                    .getDatabase(this.sharedAsyncDatabaseId);
                CosmosAsyncContainer singlePartitionAsyncContainer =
                    sharedAsyncDatabase.getContainer(this.singlePartitionAsyncContainerId);

                safeDeleteCollection(singlePartitionAsyncContainer);
            } finally {
                safeClose(dummyClient);
            }
        }
    }

    private static class ResponseWrapper<T> {

        private final CosmosItemResponse<T> cosmosItemResponse;
        private final CosmosException cosmosException;
        private final FeedResponse<T> feedResponse;
        private final CosmosBatchResponse batchResponse;

        ResponseWrapper(FeedResponse<T> feedResponse) {
            this.feedResponse = feedResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosItemResponse<T> cosmosItemResponse) {
            this.cosmosItemResponse = cosmosItemResponse;
            this.cosmosException = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosException cosmosException) {
            this.cosmosException = cosmosException;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosBatchResponse batchResponse) {
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
        public CosmosReadManyRequestOptions readManyRequestOptions;
        public CosmosItemRequestOptions patchItemRequestOptions;
        public FeedRange feedRangeToDrainForChangeFeed;
        public FeedRange feedRangeForQuery;
        public List<CosmosItemIdentity> itemIdentitiesForReadManyOperation;
        public PartitionKey partitionKeyForReadAllOperation;
        public String containerIdToTarget;
        public int itemCountToBootstrapContainerFrom;
        public FeedRange faultyFeedRange;
        public List<TestObject> testObjectsForDataPlaneOperationToWorkWith;
        public QueryType queryType;
    }

    private static class FaultInjectionRuleParamsWrapper {

        private CosmosAsyncContainer faultInjectionApplicableAsyncContainer;
        private Integer hitLimit;
        private Duration responseDelay;
        private Duration faultInjectionDuration;
        private List<String> faultInjectionApplicableRegions;
        private FeedRange faultInjectionApplicableFeedRange;
        private FaultInjectionOperationType faultInjectionOperationType;
        private FaultInjectionConnectionType faultInjectionConnectionType;
        private boolean isOverrideFaultInjectionOperationType = false;

        public boolean getIsOverrideFaultInjectionOperationType() {
            return isOverrideFaultInjectionOperationType;
        }

        public FaultInjectionRuleParamsWrapper withOverrideFaultInjectionOperationType(boolean isOverrideFaultInjectionOperationType) {
            this.isOverrideFaultInjectionOperationType = isOverrideFaultInjectionOperationType;
            return this;
        }

        public CosmosAsyncContainer getFaultInjectionApplicableAsyncContainer() {
            return faultInjectionApplicableAsyncContainer;
        }

        public FaultInjectionRuleParamsWrapper withFaultInjectionApplicableAsyncContainer(CosmosAsyncContainer faultInjectionApplicableAsyncContainer) {
            this.faultInjectionApplicableAsyncContainer = faultInjectionApplicableAsyncContainer;
            return this;
        }

        public Integer getHitLimit() {
            return hitLimit;
        }

        public FaultInjectionRuleParamsWrapper withHitLimit(Integer hitLimit) {
            this.hitLimit = hitLimit;
            return this;
        }

        public Duration getResponseDelay() {
            return responseDelay;
        }

        public FaultInjectionRuleParamsWrapper withResponseDelay(Duration responseDelay) {
            this.responseDelay = responseDelay;
            return this;
        }

        public Duration getFaultInjectionDuration() {
            return faultInjectionDuration;
        }

        public FaultInjectionRuleParamsWrapper withFaultInjectionDuration(Duration faultInjectionDuration) {
            this.faultInjectionDuration = faultInjectionDuration;
            return this;
        }

        public List<String> getFaultInjectionApplicableRegions() {
            return faultInjectionApplicableRegions;
        }

        public FaultInjectionRuleParamsWrapper withFaultInjectionApplicableRegions(List<String> faultInjectionApplicableRegions) {
            this.faultInjectionApplicableRegions = faultInjectionApplicableRegions;
            return this;
        }

        public FeedRange getFaultInjectionApplicableFeedRange() {
            return faultInjectionApplicableFeedRange;
        }

        public FaultInjectionRuleParamsWrapper withFaultInjectionApplicableFeedRange(FeedRange faultInjectionApplicableFeedRange) {
            this.faultInjectionApplicableFeedRange = faultInjectionApplicableFeedRange;
            return this;
        }

        public FaultInjectionOperationType getFaultInjectionOperationType() {
            return faultInjectionOperationType;
        }

        public FaultInjectionRuleParamsWrapper withFaultInjectionOperationType(FaultInjectionOperationType faultInjectionOperationType) {
            this.faultInjectionOperationType = faultInjectionOperationType;
            return this;
        }

        public FaultInjectionConnectionType getFaultInjectionConnectionType() {
            return faultInjectionConnectionType;
        }

        public FaultInjectionRuleParamsWrapper withFaultInjectionConnectionType(FaultInjectionConnectionType faultInjectionConnectionType) {
            this.faultInjectionConnectionType = faultInjectionConnectionType;
            return this;
        }
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

    private static List<FaultInjectionRule> buildServiceUnavailableFaultInjectionRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionConditionBuilder faultInjectionConditionBuilder = new FaultInjectionConditionBuilder()
                .connectionType(paramsWrapper.getFaultInjectionConnectionType())
                .region(applicableRegion);

            if (paramsWrapper.getFaultInjectionApplicableFeedRange() != null) {
                faultInjectionConditionBuilder.endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build());
            }

            if (!paramsWrapper.getIsOverrideFaultInjectionOperationType() && paramsWrapper.getFaultInjectionOperationType() != null) {
                faultInjectionConditionBuilder.operationType(paramsWrapper.getFaultInjectionOperationType());
            }

            FaultInjectionCondition faultInjectionCondition = faultInjectionConditionBuilder.build();

            FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                .build();

            FaultInjectionRuleBuilder faultInjectionRuleBuilder = new FaultInjectionRuleBuilder("service-unavailable-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult);

            if (paramsWrapper.getFaultInjectionDuration() != null) {
                faultInjectionRuleBuilder.duration(paramsWrapper.getFaultInjectionDuration());
            }

            if (paramsWrapper.getHitLimit() != null) {
                faultInjectionRuleBuilder.hitLimit(paramsWrapper.getHitLimit());
            }

            faultInjectionRules.add(faultInjectionRuleBuilder.build());
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildServerGeneratedGoneErrorFaultInjectionRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.GONE)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionConditionBuilder faultInjectionConditionBuilder = new FaultInjectionConditionBuilder()
                .connectionType(paramsWrapper.getFaultInjectionConnectionType())
                .region(applicableRegion);

            if (paramsWrapper.getFaultInjectionApplicableFeedRange() != null) {
                faultInjectionConditionBuilder.endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build());
            }

            if (!paramsWrapper.getIsOverrideFaultInjectionOperationType() && paramsWrapper.getFaultInjectionOperationType() != null) {
                faultInjectionConditionBuilder.operationType(paramsWrapper.getFaultInjectionOperationType());
            }

            FaultInjectionCondition faultInjectionCondition = faultInjectionConditionBuilder.build();

            FaultInjectionRuleBuilder faultInjectionRuleBuilder = new FaultInjectionRuleBuilder("gone-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult);

            if (paramsWrapper.getFaultInjectionDuration() != null) {
                faultInjectionRuleBuilder.duration(paramsWrapper.getFaultInjectionDuration());
            }

            if (paramsWrapper.getHitLimit() != null) {
                faultInjectionRuleBuilder.hitLimit(paramsWrapper.getHitLimit());
            }

            faultInjectionRules.add(faultInjectionRuleBuilder.build());
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildTransitTimeoutFaultInjectionRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(paramsWrapper.getResponseDelay())
            .suppressServiceRequests(false)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionConditionBuilder faultInjectionConditionBuilder = new FaultInjectionConditionBuilder()
                .connectionType(paramsWrapper.getFaultInjectionConnectionType())
                .region(applicableRegion);

            if (paramsWrapper.getFaultInjectionApplicableFeedRange() != null) {
                faultInjectionConditionBuilder.endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build());
            }

            if (!paramsWrapper.getIsOverrideFaultInjectionOperationType() && paramsWrapper.getFaultInjectionOperationType() != null) {
                faultInjectionConditionBuilder.operationType(paramsWrapper.getFaultInjectionOperationType());
            }

            FaultInjectionCondition faultInjectionCondition = faultInjectionConditionBuilder.build();

            FaultInjectionRuleBuilder faultInjectionRuleBuilder = new FaultInjectionRuleBuilder("response-delay-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult);

            if (paramsWrapper.getFaultInjectionDuration() != null) {
                faultInjectionRuleBuilder.duration(paramsWrapper.getFaultInjectionDuration());
            }

            if (paramsWrapper.getHitLimit() != null) {
                faultInjectionRuleBuilder.hitLimit(paramsWrapper.getHitLimit());
            }

            faultInjectionRules.add(faultInjectionRuleBuilder.build());
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildReadWriteSessionNotAvailableFaultInjectionRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionConditionBuilder faultInjectionConditionBuilder = new FaultInjectionConditionBuilder()
                .connectionType(paramsWrapper.getFaultInjectionConnectionType())
                .region(applicableRegion);

            if (paramsWrapper.getFaultInjectionApplicableFeedRange() != null) {
                faultInjectionConditionBuilder.endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build());
            }

            if (!paramsWrapper.getIsOverrideFaultInjectionOperationType() && paramsWrapper.getFaultInjectionOperationType() != null) {
                faultInjectionConditionBuilder.operationType(paramsWrapper.getFaultInjectionOperationType());
            }

            FaultInjectionCondition faultInjectionCondition = faultInjectionConditionBuilder.build();

            FaultInjectionRuleBuilder faultInjectionRuleBuilder = new FaultInjectionRuleBuilder("read-session-not-available-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult);

            if (paramsWrapper.getFaultInjectionDuration() != null) {
                faultInjectionRuleBuilder.duration(paramsWrapper.getFaultInjectionDuration());
            }

            if (paramsWrapper.getHitLimit() != null) {
                faultInjectionRuleBuilder.hitLimit(paramsWrapper.getHitLimit());
            }

            faultInjectionRules.add(faultInjectionRuleBuilder.build());
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildTooManyRequestsErrorFaultInjectionRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionConditionBuilder faultInjectionConditionBuilder = new FaultInjectionConditionBuilder()
                .connectionType(paramsWrapper.getFaultInjectionConnectionType())
                .region(applicableRegion);

            if (paramsWrapper.getFaultInjectionApplicableFeedRange() != null) {
                faultInjectionConditionBuilder.endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build());
            }

            if (!paramsWrapper.getIsOverrideFaultInjectionOperationType() && paramsWrapper.getFaultInjectionOperationType() != null) {
                faultInjectionConditionBuilder.operationType(paramsWrapper.getFaultInjectionOperationType());
            }

            FaultInjectionCondition faultInjectionCondition = faultInjectionConditionBuilder.build();

            FaultInjectionRuleBuilder faultInjectionRuleBuilder = new FaultInjectionRuleBuilder("too-many-requests-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult);

            if (paramsWrapper.getFaultInjectionDuration() != null) {
                faultInjectionRuleBuilder.duration(paramsWrapper.getFaultInjectionDuration());
            }

            if (paramsWrapper.getHitLimit() != null) {
                faultInjectionRuleBuilder.hitLimit(paramsWrapper.getHitLimit());
            }

            faultInjectionRules.add(faultInjectionRuleBuilder.build());
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildInternalServerErrorFaultInjectionRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionConditionBuilder faultInjectionConditionBuilder = new FaultInjectionConditionBuilder()
                .connectionType(paramsWrapper.getFaultInjectionConnectionType())
                .region(applicableRegion);

            if (paramsWrapper.getFaultInjectionApplicableFeedRange() != null) {
                faultInjectionConditionBuilder.endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build());
            }

            if (!paramsWrapper.getIsOverrideFaultInjectionOperationType() && paramsWrapper.getFaultInjectionOperationType() != null) {
                faultInjectionConditionBuilder.operationType(paramsWrapper.getFaultInjectionOperationType());
            }

            FaultInjectionCondition faultInjectionCondition = faultInjectionConditionBuilder.build();

            FaultInjectionRuleBuilder faultInjectionRuleBuilder = new FaultInjectionRuleBuilder("internal-server-error-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult);

            if (paramsWrapper.getFaultInjectionDuration() != null) {
                faultInjectionRuleBuilder.duration(paramsWrapper.getFaultInjectionDuration());
            }

            if (paramsWrapper.getHitLimit() != null) {
                faultInjectionRuleBuilder.hitLimit(paramsWrapper.getHitLimit());
            }

            faultInjectionRules.add(faultInjectionRuleBuilder.build());
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildRetryWithFaultInjectionRules(FaultInjectionRuleParamsWrapper paramsWrapper) {
        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RETRY_WITH)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionConditionBuilder faultInjectionConditionBuilder = new FaultInjectionConditionBuilder()
                .connectionType(paramsWrapper.getFaultInjectionConnectionType())
                .region(applicableRegion);

            if (paramsWrapper.getFaultInjectionApplicableFeedRange() != null) {
                faultInjectionConditionBuilder.endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build());
            }

            if (!paramsWrapper.getIsOverrideFaultInjectionOperationType() && paramsWrapper.getFaultInjectionOperationType() != null) {
                faultInjectionConditionBuilder.operationType(paramsWrapper.getFaultInjectionOperationType());
            }

            FaultInjectionCondition faultInjectionCondition = faultInjectionConditionBuilder.build();

            FaultInjectionRuleBuilder faultInjectionRuleBuilder = new FaultInjectionRuleBuilder("retry-with-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult);

            if (paramsWrapper.getFaultInjectionDuration() != null) {
                faultInjectionRuleBuilder.duration(paramsWrapper.getFaultInjectionDuration());
            }

            if (paramsWrapper.getHitLimit() != null) {
                faultInjectionRuleBuilder.hitLimit(paramsWrapper.getHitLimit());
            }

            faultInjectionRules.add(faultInjectionRuleBuilder.build());
        }

        return faultInjectionRules;
    }

    private static boolean doesOperationHaveWriteSemantics(FaultInjectionOperationType faultInjectionOperationType) {
        switch (faultInjectionOperationType) {

            case DELETE_ITEM:
            case PATCH_ITEM:
            case UPSERT_ITEM:
            case BATCH_ITEM:
            case REPLACE_ITEM:
            case CREATE_ITEM:
                return true;
            case READ_ITEM:
            case QUERY_ITEM:
            case READ_FEED_ITEM:
                return false;
            default:
                throw new IllegalArgumentException("Unsupported operation type : " + faultInjectionOperationType);
        }
    }

    private static void validateNonEmptyString(String input) {
        assertThat(input).isNotNull();
        assertThat(input).isNotEmpty();
    }

    private static <T> void validateNonEmptyList(List<T> list) {
        assertThat(list).isNotNull();
        assertThat(list).isNotEmpty();
    }

    private static void deleteAllDocuments(CosmosAsyncContainer asyncContainer) {
        asyncContainer
            .queryItems("SELECT * FROM C", TestObject.class)
            .collectList()
            .flatMapMany(Flux::fromIterable)
            .flatMap(testObject -> asyncContainer.deleteItem(testObject.getId(), new PartitionKey(testObject.getMypk())))
            .blockLast();
    }

    private static Class<?> getClassBySimpleName(Class<?>[] classes, String classSimpleName) {
        for (Class<?> clazz : classes) {
            if (clazz.getSimpleName().equals(classSimpleName)) {
                return clazz;
            }
        }

        logger.warn("Class with simple name {} does not exist!", classSimpleName);
        return null;
    }

    private static double getEstimatedFailureCountSeenPerRegionPerPartitionKeyRange(
        PartitionKeyRangeWrapper partitionKeyRangeWrapper,
        ConcurrentHashMap<PartitionKeyRangeWrapper, ?> partitionKeyRangeToLocationSpecificUnavailabilityInfo,
        Field locationEndpointToLocationSpecificContextForPartitionField,
        int allowedExceptionCountToMaintainHealthyWithFailuresStatus,
        int expectedRegionCountWithFailures) throws IllegalAccessException {

        Object partitionAndLocationSpecificUnavailabilityInfo
            = partitionKeyRangeToLocationSpecificUnavailabilityInfo.get(partitionKeyRangeWrapper);

        if (partitionAndLocationSpecificUnavailabilityInfo == null) {
            return 0d;
        }

        ConcurrentHashMap<URI, LocationSpecificHealthContext> locationEndpointToLocationSpecificContextForPartition
            = (ConcurrentHashMap<URI, LocationSpecificHealthContext>) locationEndpointToLocationSpecificContextForPartitionField.get(partitionAndLocationSpecificUnavailabilityInfo);

        int count = 0;
        boolean failuresExist = false;

        for (LocationSpecificHealthContext locationSpecificHealthContext : locationEndpointToLocationSpecificContextForPartition.values()) {

            if (locationSpecificHealthContext.getLocationHealthStatus() == LocationHealthStatus.Unavailable) {
                count += allowedExceptionCountToMaintainHealthyWithFailuresStatus;
            } else {
                count += locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking() + locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking();
            }

            if (locationSpecificHealthContext.getExceptionCountForReadForCircuitBreaking() + locationSpecificHealthContext.getExceptionCountForWriteForCircuitBreaking() > 0) {
                failuresExist = true;
            }
        }

        if (failuresExist) {
            return (count * 1.0d) / (expectedRegionCountWithFailures * 1.0d);
        }

        return 0d;
    }

    private static FaultInjectionConnectionType evaluateFaultInjectionConnectionType(ConnectionMode connectionMode) {

        if (connectionMode == ConnectionMode.DIRECT) {
            return FaultInjectionConnectionType.DIRECT;
        } else if (connectionMode == ConnectionMode.GATEWAY) {
            return FaultInjectionConnectionType.GATEWAY;
        }

        throw new IllegalArgumentException("Unsupported connection mode : " + connectionMode);
    }

    private enum QueryType {
        READ_MANY, READ_ALL
    }
}
