// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosMetricCategory;
import com.azure.cosmos.models.CosmosMetricTagName;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class FaultInjectionWithAvailabilityStrategyTests extends TestSuiteBase {

    private final static Logger logger = LoggerFactory.getLogger(FaultInjectionWithAvailabilityStrategyTests.class);

    private List<String> writeableRegions;

    @BeforeClass(groups = { "multi-master" })
    public void beforeClass() {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode();

        CosmosAsyncClient dummyClient = null;

        try {

            dummyClient = clientBuilder.buildAsyncClient();

            AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(dummyClient);
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            GlobalEndpointManager globalEndpointManager =
                ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            Map<String, String> writeRegionMap = this.getRegionMap(databaseAccount, true);

            this.writeableRegions = writeRegionMap.keySet().stream().collect(Collectors.toList());
        } finally {
            safeClose(dummyClient);
        }
    }

    @DataProvider(name = "testConfigs")
    public Object[][] testConfigs() {
        return new Object[][] {
            // check if OperationCancelledException bubbles up
            // check if 408 (op-level) & 404/1002 (req-level) metrics are tracked
            new Object[] {Duration.ofSeconds(2), CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED},
            // check if NotFoundException bubbles up
            // check if 404 (op-level) & 404/1002 (req-level) metrics are tracked
            new Object[] {Duration.ofSeconds(2), CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED}
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs")
    public void exceptionHandling_And_MetricsRecording_With_AllRegionsFailing_Test(Duration endToEndTimeout, CosmosRegionSwitchHint regionSwitchHint) {
        System.setProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED", String.valueOf(1));

        MeterRegistry meterRegistry = createMeterRegistry();
        CosmosMicrometerMetricsOptions inputMetricOptions = createMicrometerMetricsOptions(meterRegistry);
        CosmosClientTelemetryConfig clientTelemetryConfig = createClientTelemetryConfig(inputMetricOptions);

        CosmosAsyncClient clientWithPreferredRegions = null;

        assertThat(this.writeableRegions).isNotNull();
        assertThat(this.writeableRegions.size()).isGreaterThanOrEqualTo(2);

        CosmosAsyncDatabase databaseWithSeveralWriteableRegions = null;
        CosmosAsyncContainer containerWithSeveralWriteableRegions = null;

        try {
            clientWithPreferredRegions = buildCosmosClient(clientTelemetryConfig, this.writeableRegions, regionSwitchHint);

            // setup db and container and pass their ids accordingly
            // ensure the container has a partition key definition of /mypk

            String dbId = UUID.randomUUID().toString();
            String containerId = UUID.randomUUID().toString();

            clientWithPreferredRegions.createDatabaseIfNotExists(dbId).block();
            databaseWithSeveralWriteableRegions = clientWithPreferredRegions.getDatabase(dbId);

            databaseWithSeveralWriteableRegions.createContainerIfNotExists(
                new CosmosContainerProperties(containerId, new PartitionKeyDefinition().setPaths(Arrays.asList("/mypk")))).block();
            containerWithSeveralWriteableRegions = databaseWithSeveralWriteableRegions.getContainer(containerId);

            String documentId = UUID.randomUUID().toString();
            Pair<String, String> idAndPkValPair = new ImmutablePair<>(documentId, documentId);

            CosmosDiagnosticsTest.TestItem createdItem = new CosmosDiagnosticsTest.TestItem(documentId, documentId);
            CosmosItemResponse<CosmosDiagnosticsTest.TestItem> writeResponse = containerWithSeveralWriteableRegions.createItem(createdItem).block();

            FaultInjectionRuleBuilder badSessionTokenRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-"
                + "read-session-unavailable-" + UUID.randomUUID());

            // inject 404/1002s in two regions
            // configure in accordance with preferredRegions on the client
            FaultInjectionCondition faultInjectionConditionForReadsInPrimaryRegion =
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region(this.writeableRegions.get(0))
                    .build();

            FaultInjectionCondition faultInjectionConditionForReadsInSecondaryRegion =
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region(this.writeableRegions.get(1))
                    .build();

            FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                .build();

            // sustained fault injection
            FaultInjectionRule readSessionUnavailableRulePrimaryRegion = badSessionTokenRuleBuilder
                .condition(faultInjectionConditionForReadsInPrimaryRegion)
                .result(badSessionTokenServerErrorResult)
                .duration(Duration.ofSeconds(120))
                .build();

            // sustained fault injection
            FaultInjectionRule readSessionUnavailableRuleSecondaryRegion = badSessionTokenRuleBuilder
                .condition(faultInjectionConditionForReadsInSecondaryRegion)
                .result(badSessionTokenServerErrorResult)
                .duration(Duration.ofSeconds(120))
                .build();

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(containerWithSeveralWriteableRegions,
                    Arrays.asList(readSessionUnavailableRulePrimaryRegion, readSessionUnavailableRuleSecondaryRegion))
                .block();

            int threadCount = 1;

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            AtomicBoolean isStopped = new AtomicBoolean(false);

            Duration workloadExecutionDuration = Duration.ofSeconds(60);

            Flux
                .just(1)
                .delayElements(workloadExecutionDuration)
                .doOnComplete(() -> isStopped.compareAndSet(false, true))
                .subscribe();
            ThresholdBasedAvailabilityStrategy thresholdStrategy = new ThresholdBasedAvailabilityStrategy();
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(endToEndTimeout).
                    enable(true).availabilityStrategy(thresholdStrategy).build();
            for (int i = 0; i < threadCount; i++) {
                CosmosAsyncContainer finalAsyncContainer = containerWithSeveralWriteableRegions;
                executorService.submit(() -> execute(
                    finalAsyncContainer,
                    idAndPkValPair,
                    isStopped,
                    endToEndOperationLatencyPolicyConfig,
                    writeResponse.getSessionToken())
                );
            }

            try {
                executorService.awaitTermination(60, TimeUnit.SECONDS);
                executorService.shutdown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            logger.error("Error occurred", e);
        } finally {
            System.clearProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED");
            safeDeleteCollection(containerWithSeveralWriteableRegions);
            safeDeleteDatabase(databaseWithSeveralWriteableRegions);
            safeClose(clientWithPreferredRegions);
        }
    }

    private static void execute(
        CosmosAsyncContainer container,
        Pair<String, String> idAndPkValPair,
        AtomicBoolean isStopped,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig,
        String invalidSessionToken) {

        while (!isStopped.get()) {
            try {
                CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

                if (endToEndOperationLatencyPolicyConfig != null) {
                    itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
                }

                if (invalidSessionToken != null && !invalidSessionToken.isEmpty()) {
                    itemRequestOptions.setSessionToken(invalidSessionToken);
                    logger.debug("Set an invalid session token : {}", invalidSessionToken);
                }

                CosmosItemResponse<ObjectNode> response = container.readItem(idAndPkValPair.getLeft(),
                    new PartitionKey(idAndPkValPair.getRight()), itemRequestOptions, ObjectNode.class).block();
            } catch (Exception e) {
                if (e instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(e, CosmosException.class);
                } else {
                   fail("A CosmosException instance should have been thrown");
                }
            }
        }
    }

    private static CosmosClientTelemetryConfig createClientTelemetryConfig(CosmosMicrometerMetricsOptions inputMetricsOptions) {
        return new CosmosClientTelemetryConfig()
            .metricsOptions(inputMetricsOptions);
    }

    private static CosmosAsyncClient buildCosmosClient(
        CosmosClientTelemetryConfig clientTelemetryConfig,
        List<String> preferredRegions,
        CosmosRegionSwitchHint regionSwitchHint) {

        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(preferredRegions)
            .sessionRetryOptions(new SessionRetryOptions(regionSwitchHint))
            .directMode()
            .multipleWriteRegionsEnabled(true)
            .clientTelemetryConfig(clientTelemetryConfig)
            .buildAsyncClient();
    }

    private static MeterRegistry createMeterRegistry() {
        return ConsoleLoggingRegistryFactory.create(1);
    }

    private static CosmosMicrometerMetricsOptions createMicrometerMetricsOptions(MeterRegistry meterRegistry) {
        return new CosmosMicrometerMetricsOptions()
            .setEnabled(true)
            .meterRegistry(meterRegistry)
            .addMetricCategories(CosmosMetricCategory.OPERATION_DETAILS)
            .addMetricCategories(CosmosMetricCategory.REQUEST_DETAILS)
            .enableHistogramsByDefault(false)
            .configureDefaultPercentiles(0.99)
            .configureDefaultTagNames(CosmosMetricTagName.SERVICE_ADDRESS, CosmosMetricTagName.REGION_NAME);
    }

    private Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
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
