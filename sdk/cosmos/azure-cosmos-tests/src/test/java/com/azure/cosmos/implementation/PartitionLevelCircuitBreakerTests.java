// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;


import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.SessionRetryOptionsBuilder;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.faultinjection.FaultInjectionTestBase;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.circuitBreaker.ConsecutiveExceptionBasedCircuitBreaker;
import com.azure.cosmos.implementation.circuitBreaker.GlobalPartitionEndpointManagerForCircuitBreaker;
import com.azure.cosmos.implementation.circuitBreaker.LocationHealthStatus;
import com.azure.cosmos.implementation.circuitBreaker.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyImpl;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
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
import org.testng.annotations.AfterClass;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;


public class PartitionLevelCircuitBreakerTests extends FaultInjectionTestBase {

    private static final ImplementationBridgeHelpers.CosmosAsyncContainerHelper.CosmosAsyncContainerAccessor containerAccessor
        = ImplementationBridgeHelpers.CosmosAsyncContainerHelper.getCosmosAsyncContainerAccessor();
    private List<String> writeRegions;

    private static final CosmosEndToEndOperationLatencyPolicyConfig noEndToEndTimeout
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofDays(1)).build();

    private static final CosmosEndToEndOperationLatencyPolicyConfig twoSecondEndToEndTimeoutWithThresholdBasedAvailabilityStrategy
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2))
        .availabilityStrategy(new ThresholdBasedAvailabilityStrategy())
        .build();

    private static final CosmosEndToEndOperationLatencyPolicyConfig twoSecondEndToEndTimeoutWithoutAvailabilityStrategy
        = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2))
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

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildServiceUnavailableError
        = PartitionLevelCircuitBreakerTests::buildServiceUnavailableRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildServerGeneratedGoneError
        = PartitionLevelCircuitBreakerTests::buildServerGeneratedGoneRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildTooManyRequestsError
        = PartitionLevelCircuitBreakerTests::buildTooManyRequestsRules;

    private final Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> buildReadWriteSessionNotAvailableRules
        = PartitionLevelCircuitBreakerTests::buildReadWriteSessionNotAvailableRules;

    private static final CosmosRegionSwitchHint noRegionSwitchHint = null;

    private static final Boolean nonIdempotentWriteRetriesEnabled = true;

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

    @BeforeClass(groups = {"multi-master"})
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

            try {
                Thread.sleep(3000);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        } finally {
            logger.debug("beforeClass executed...");
        }
    }

    @DataProvider(name = "partitionLevelCircuitBreakerTestConfigs")
    public Object[][] partitionLevelCircuitBreakerTestConfigs() {

        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateServiceUnavailableRules
            = PartitionLevelCircuitBreakerTests::buildServiceUnavailableRules;

        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateServerGeneratedGoneRules
            = PartitionLevelCircuitBreakerTests::buildServerGeneratedGoneRules;

        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateTransitTimeoutRules
            = PartitionLevelCircuitBreakerTests::buildTransitTimeoutRules;

        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateInternalServerErrorRules
            = PartitionLevelCircuitBreakerTests::buildInternalServerErrorRules;

        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateTooManyRequestsRules
            = PartitionLevelCircuitBreakerTests::buildTooManyRequestsRules;

        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateReadOrWriteSessionNotAvailableRules
            = PartitionLevelCircuitBreakerTests::buildReadWriteSessionNotAvailableRules;

        Function<FaultInjectionRuleParamsWrapper, List<FaultInjectionRule>> generateRetryWithRules
            = PartitionLevelCircuitBreakerTests::buildRetryWithFaultInjectionRules;

        return new Object[][]{
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(11),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                validateResponseHasSuccess,
                validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.UPSERT_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(6),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(6),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.DELETE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(6),
                generateServiceUnavailableRules,

                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.PATCH_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(6),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(6),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(11),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.BATCH_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(6),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in first preferred region.", FaultInjectionOperationType.READ_FEED_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(11),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateServerGeneratedGoneRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.UPSERT_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateServerGeneratedGoneRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateServerGeneratedGoneRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.DELETE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateServerGeneratedGoneRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.PATCH_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateServerGeneratedGoneRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateServerGeneratedGoneRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with server-generated gone in first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateServerGeneratedGoneRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with response delay in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withResponseDelay(Duration.ofSeconds(6)),
                generateTransitTimeoutRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with response delay in first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withResponseDelay(Duration.ofSeconds(6)),
                generateTransitTimeoutRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with internal service error in the first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(11),
                generateInternalServerErrorRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with internal service error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(6),
                generateInternalServerErrorRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with internal service error in the first preferred region.", FaultInjectionOperationType.READ_FEED_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(11),
                generateInternalServerErrorRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with internal service error in the first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(11),
                generateInternalServerErrorRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateTooManyRequestsRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateTooManyRequestsRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateTooManyRequestsRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with read session not available in the first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateReadOrWriteSessionNotAvailableRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with write session not available error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateReadOrWriteSessionNotAvailableRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with retry with service error in the first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateRetryWithRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in all regions.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(11),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasServiceUnavailableError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with service unavailable error in in all regions.", FaultInjectionOperationType.UPSERT_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(6),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasServiceUnavailableError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[] {
                String.format("Test with faulty %s with service unavailable error in all regions.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions)
                    .withHitLimit(11),
                generateServiceUnavailableRules,
                noEndToEndTimeout,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasServiceUnavailableError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with response delay in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withResponseDelay(Duration.ofSeconds(6)),
                generateTransitTimeoutRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with response delay in first preferred region.", FaultInjectionOperationType.REPLACE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withResponseDelay(Duration.ofSeconds(6)),
                generateTransitTimeoutRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.READ_FEED_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_FEED_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(11),
                generateInternalServerErrorRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with internal server error in the first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withHitLimit(11),
                generateInternalServerErrorRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasInternalServerError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                String.format("Test with faulty %s with too many requests error in the first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateTooManyRequestsRules,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with too many requests error in first preferred region.", FaultInjectionOperationType.READ_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateTooManyRequestsRules,
                twoSecondEndToEndTimeoutWithThresholdBasedAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with too many requests error in first preferred region.", FaultInjectionOperationType.CREATE_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateTooManyRequestsRules,
                twoSecondEndToEndTimeoutWithThresholdBasedAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            new Object[]{
                String.format("Test with faulty %s with too many requests error in first preferred region.", FaultInjectionOperationType.QUERY_ITEM),
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1))
                    .withFaultInjectionDuration(Duration.ofSeconds(60)),
                generateTooManyRequestsRules,
                twoSecondEndToEndTimeoutWithThresholdBasedAvailabilityStrategy,
                noRegionSwitchHint,
                !nonIdempotentWriteRetriesEnabled,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstAndSecondPreferredRegions,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
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
            {
                "Test read many operation injected with service unavailable exception in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(11)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildServiceUnavailableError,
                executeReadManyOperation,
                noEndToEndTimeout,
                noRegionSwitchHint,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                "Test read many operation injected with server-generated GONE in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildServerGeneratedGoneError,
                executeReadManyOperation,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                "Test read many operation injected with too many requests error in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildTooManyRequestsError,
                executeReadManyOperation,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                "Test read many operation injected with read/write session not available error in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildReadWriteSessionNotAvailableRules,
                executeReadManyOperation,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                "Test read many operation injected with service unavailable error in all regions.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(11)
                    .withFaultInjectionApplicableRegions(this.writeRegions),
                this.buildServiceUnavailableError,
                executeReadManyOperation,
                noEndToEndTimeout,
                noRegionSwitchHint,
                this.validateResponseHasServiceUnavailableError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            }
        };
    }

    @DataProvider(name = "readAllTestConfigs")
    public Object[][] readAllTestConfigs() {

        Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> executeReadManyOperation = (paramsWrapper) -> {
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
                    .withHitLimit(11)
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildServiceUnavailableError,
                executeReadManyOperation,
                noEndToEndTimeout,
                noRegionSwitchHint,
                this.validateResponseHasSuccess,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                "Test read all operation injected with server-generated GONE in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildServerGeneratedGoneError,
                executeReadManyOperation,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                "Test read all operation injected with too many requests error in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildTooManyRequestsError,
                executeReadManyOperation,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                "Test read all operation injected with read/write session not available error in first preferred region.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionDuration(Duration.ofSeconds(60))
                    .withFaultInjectionApplicableRegions(this.writeRegions.subList(0, 1)),
                this.buildReadWriteSessionNotAvailableRules,
                executeReadManyOperation,
                twoSecondEndToEndTimeoutWithoutAvailabilityStrategy,
                noRegionSwitchHint,
                this.validateResponseHasOperationCancelledException,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasSecondPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            },
            {
                "Test read all operation injected with service unavailable error in all regions.",
                new FaultInjectionRuleParamsWrapper()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withHitLimit(11)
                    .withFaultInjectionApplicableRegions(this.writeRegions),
                this.buildServiceUnavailableError,
                executeReadManyOperation,
                noEndToEndTimeout,
                noRegionSwitchHint,
                this.validateResponseHasServiceUnavailableError,
                this.validateResponseHasSuccess,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                this.validateDiagnosticsContextHasAllRegions,
                this.validateDiagnosticsContextHasFirstPreferredRegionOnly,
                ConnectionMode.DIRECT
            }
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "partitionLevelCircuitBreakerTestConfigs", timeOut = 80 * TIMEOUT)
    public void operationHitsTerminalExceptionAcrossKRegions(
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
        ConnectionMode allowedConnectionMode) {

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() != allowedConnectionMode) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", allowedConnectionMode));
        }

        CosmosAsyncClient asyncClient = null;
        FaultInjectionOperationType faultInjectionOperationType = faultInjectionRuleParamsWrapper.getFaultInjectionOperationType();

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
            15,
            15);
    }

    @Test(groups = {"multi-master"}, dataProvider = "readManyTestConfigs", timeOut = 80 * TIMEOUT)
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
        ConnectionMode allowedConnectionMode) {

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = this.writeRegions.get(0);
        this.secondPreferredRegion = this.writeRegions.get(1);

        CosmosAsyncClient asyncClient = null;

        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
        operationInvocationParamsWrapper.queryType = QueryType.READ_MANY;

        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() != allowedConnectionMode) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", allowedConnectionMode));
        }

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
            15,
            15);
    }

    @Test(groups = {"multi-master"}, dataProvider = "readAllTestConfigs", timeOut = 80 * TIMEOUT)
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
        ConnectionMode allowedConnectionMode) {

        CosmosAsyncClient asyncClient = null;

        OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
        operationInvocationParamsWrapper.queryType = QueryType.READ_ALL;

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(clientBuilder);

        if (connectionPolicy.getConnectionMode() != allowedConnectionMode) {
            throw new SkipException(String.format("Test is not applicable to %s connectivity mode!", connectionPolicy.getConnectionMode()));
        }

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
            15,
            15);
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

            System.setProperty(
                "COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG",
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"COUNT_BASED\","
                    + "\"circuitBreakerFailureTolerance\": \"LOW\"}");


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

                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> faultInjectedFunc = resolveDataPlaneOperation(FaultInjectionOperationType.READ_ITEM);

                for (int i = 1; i <= 15; i++) {
                    paramsWrapper1.createdTestObject = testObjects1.isEmpty() ? null : testObjects1.get(0);
                    paramsWrapper1.asyncContainer = container1;

                    paramsWrapper2.createdTestObject = testObjects1.isEmpty() ? null : testObjects1.get(0);
                    paramsWrapper2.asyncContainer = container2;

                    ResponseWrapper<?> response1 = faultInjectedFunc.apply(paramsWrapper1);
                    ResponseWrapper<?> response2 = faultInjectedFunc.apply(paramsWrapper2);

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

                    ResponseWrapper<?> response1 = faultInjectedFunc.apply(paramsWrapper1);
                    ResponseWrapper<?> response2 = faultInjectedFunc.apply(paramsWrapper2);

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
            System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
            safeDeleteCollection(container1);
            safeDeleteCollection(container2);
            safeClose(client);
        }
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
        int operationIterationCountInFailureFlow,
        int operationIterationCountInRecoveryFlow) {

        logger.info("Checking circuit breaking behavior for test type {}", testId);

        List<String> preferredRegions = this.writeRegions;

        this.firstPreferredRegion = preferredRegions.get(0);
        this.secondPreferredRegion = preferredRegions.get(1);

        CosmosClientBuilder clientBuilder = getClientBuilder().multipleWriteRegionsEnabled(true).preferredRegions(preferredRegions);

        System.setProperty(
            "COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG",
            "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                + "\"circuitBreakerType\": \"COUNT_BASED\","
                + "\"circuitBreakerFailureTolerance\": \"LOW\"}");

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
                        hasReachedCircuitBreakingThreshold = expectedCircuitBreakingThreshold == globalPartitionEndpointManagerForCircuitBreaker.getExceptionCountByPartitionKeyRange(
                            new PartitionKeyRangeWrapper(faultyPartitionKeyRanges.v.get(0), faultyDocumentCollection.v.getResourceId()));
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

                logger.info("Sleep for 90 seconds");
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
                    CosmosQueryRequestOptions queryRequestOptions = paramsWrapper.queryRequestOptions;
                    queryRequestOptions = queryRequestOptions.setFeedRange(paramsWrapper.feedRangeForQuery);

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
                                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(paramsWrapper.feedRangeToDrainForChangeFeed),
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

    @AfterClass(groups = {"multi-master"})
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
        private List<CosmosItemIdentity> itemIdentitiesForReadMany;

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

    private static List<FaultInjectionRule> buildServiceUnavailableRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(paramsWrapper.getFaultInjectionOperationType())
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build())
                .region(applicableRegion)
                .build();

            FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                .build();

            FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("service-unavailable-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .hitLimit(paramsWrapper.getHitLimit())
                .build();

            faultInjectionRules.add(faultInjectionRule);
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildServerGeneratedGoneRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.GONE)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(paramsWrapper.getFaultInjectionOperationType())
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build())
                .region(applicableRegion)
                .build();

            FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("gone-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .duration(paramsWrapper.getFaultInjectionDuration())
                .build();

            faultInjectionRules.add(faultInjectionRule);
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildTransitTimeoutRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(paramsWrapper.getResponseDelay())
            .suppressServiceRequests(false)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(paramsWrapper.getFaultInjectionOperationType())
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build())
                .region(applicableRegion)
                .build();

            FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("response-delay-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .duration(paramsWrapper.getFaultInjectionDuration())
                .build();

            faultInjectionRules.add(faultInjectionRule);
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildReadWriteSessionNotAvailableRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(paramsWrapper.getFaultInjectionOperationType())
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build())
                .region(applicableRegion)
                .build();

            FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("read-session-not-available-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .duration(paramsWrapper.getFaultInjectionDuration())
                .build();

            faultInjectionRules.add(faultInjectionRule);
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildTooManyRequestsRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(paramsWrapper.getFaultInjectionOperationType())
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build())
                .region(applicableRegion)
                .build();

            FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("too-many-requests-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .duration(paramsWrapper.getFaultInjectionDuration())
                .build();

            faultInjectionRules.add(faultInjectionRule);
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildInternalServerErrorRules(FaultInjectionRuleParamsWrapper paramsWrapper) {

        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(paramsWrapper.getFaultInjectionOperationType())
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build())
                .region(applicableRegion)
                .build();

            FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("internal-server-error-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .hitLimit(paramsWrapper.getHitLimit())
                .build();

            faultInjectionRules.add(faultInjectionRule);
        }

        return faultInjectionRules;
    }

    private static List<FaultInjectionRule> buildRetryWithFaultInjectionRules(FaultInjectionRuleParamsWrapper paramsWrapper) {
        FaultInjectionServerErrorResult faultInjectionServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RETRY_WITH)
            .build();

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        for (String applicableRegion : paramsWrapper.getFaultInjectionApplicableRegions()) {

            FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
                .operationType(paramsWrapper.getFaultInjectionOperationType())
                .connectionType(FaultInjectionConnectionType.DIRECT)
                .endpoints(new FaultInjectionEndpointBuilder(paramsWrapper.getFaultInjectionApplicableFeedRange()).build())
                .region(applicableRegion)
                .build();

            FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("retry-with-rule-" + UUID.randomUUID())
                .condition(faultInjectionCondition)
                .result(faultInjectionServerErrorResult)
                .duration(paramsWrapper.getFaultInjectionDuration())
                .build();

            faultInjectionRules.add(faultInjectionRule);
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

    private static boolean requiresClientLevelE2EConfig(FaultInjectionOperationType faultInjectionOperationType) {
        return faultInjectionOperationType == FaultInjectionOperationType.READ_FEED_ITEM;
    }

    private enum QueryType {
        READ_MANY, READ_ALL, QUERY_TEXT_BASED, READ_FEED
    }
}
