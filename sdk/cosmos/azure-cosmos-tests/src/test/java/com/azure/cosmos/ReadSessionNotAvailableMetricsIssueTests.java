package com.azure.cosmos;

import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosMetricCategory;
import com.azure.cosmos.models.CosmosMetricTagName;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadSessionNotAvailableMetricsIssueTests {

    private final static Logger logger = LoggerFactory.getLogger(ReadSessionNotAvailableMetricsIssueTests.class);

    @Test(groups = {"simple"})
    public void meters_missing4041002_test() throws InterruptedException {
        System.setProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED", String.valueOf(2));

        MeterRegistry meterRegistry = createMeterRegistry();
        CosmosMicrometerMetricsOptions inputMetricOptions = createMicrometerMetricsOptions(meterRegistry);
        CosmosClientTelemetryConfig clientTelemetryConfig = createClientTelemetryConfig(inputMetricOptions);

        CosmosAsyncClient clientWithPreferredRegions = null;

        try {
            clientWithPreferredRegions = buildCosmosClient(clientTelemetryConfig);

            CosmosAsyncContainer containerForClientWithPreferredRegions = clientWithPreferredRegions
                .getDatabase("test-db")
                .getContainer("test-container");

            String documentId = UUID.randomUUID().toString();
            InternalObjectNode createdItem = getDocumentDefinition(documentId);
            containerForClientWithPreferredRegions.createItem(createdItem).block();

            FaultInjectionRuleBuilder badSessionTokenRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-bad"
                + "-session-token-" + UUID.randomUUID());

            // inject 404/1002s in two regions
            FaultInjectionCondition faultInjectionConditionForReadsInPrimaryRegion =
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region("East US")
                    .build();

            FaultInjectionCondition faultInjectionConditionForReadsInSecondaryRegion =
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region("West US")
                    .build();

            FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                .build();

            // longer fault injection in region 1 / east us
            FaultInjectionRule badSessionTokenRulePrimaryRegion = badSessionTokenRuleBuilder
                .condition(faultInjectionConditionForReadsInPrimaryRegion)
                .result(badSessionTokenServerErrorResult)
                .duration(Duration.ofSeconds(10))
                .build();

            // limit hit count to 3 in region 2 / west us
            FaultInjectionRule badSessionTokenRuleSecondaryRegion = badSessionTokenRuleBuilder
                .condition(faultInjectionConditionForReadsInSecondaryRegion)
                .result(badSessionTokenServerErrorResult)
                .duration(Duration.ofSeconds(10))
                .build();

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(containerForClientWithPreferredRegions,
                    Arrays.asList(badSessionTokenRulePrimaryRegion, badSessionTokenRuleSecondaryRegion))
                .block();

            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2)).build();
            CosmosItemResponse<InternalObjectNode> itemResponse = containerForClientWithPreferredRegions.readItem(documentId, new PartitionKey(documentId), new CosmosItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig), InternalObjectNode.class).block();

//            Thread.sleep(10_000);

        } catch (Exception e) {
//            Thread.sleep(10_000);
        } finally {

            // check if we have 404/1002s for all replicas in region 1 / east us
            // check if we have 404/1002s for 3 replicas in region 2 / west us
            // check if we have 201/0 in east us
            // check if we have 200/0 in west us
            for (Meter meter : meterRegistry.getMeters()) {
                logger.info(meter.getId().toString());
            }

            System.clearProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED");
            clientWithPreferredRegions.close();
        }
    }

    @Test(groups = {"simple"})
    public void metersMissing_4041002_sustainedWorkload_test() {
        System.setProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED", String.valueOf(2));

        MeterRegistry meterRegistry = createMeterRegistry();
        CosmosMicrometerMetricsOptions inputMetricOptions = createMicrometerMetricsOptions(meterRegistry);
        CosmosClientTelemetryConfig clientTelemetryConfig = createClientTelemetryConfig(inputMetricOptions);

        CosmosAsyncClient clientWithPreferredRegions = null;

        try {
            clientWithPreferredRegions = buildCosmosClient(clientTelemetryConfig);

            CosmosAsyncContainer containerForClientWithPreferredRegions = clientWithPreferredRegions
                .getDatabase("test-db")
                .getContainer("test-container");

            String documentId = UUID.randomUUID().toString();
            Pair<String, String> idAndPkValPair = new ImmutablePair<>(documentId, documentId);

            InternalObjectNode createdItem = getDocumentDefinition(documentId);
            containerForClientWithPreferredRegions.createItem(createdItem).block();

            FaultInjectionRuleBuilder badSessionTokenRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-bad"
                + "-session-token-" + UUID.randomUUID());

            // inject 404/1002s in two regions
            FaultInjectionCondition faultInjectionConditionForReadsInPrimaryRegion =
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region("East US")
                    .build();

            FaultInjectionCondition faultInjectionConditionForReadsInSecondaryRegion =
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region("West US")
                    .build();

            FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                .build();

            // longer fault injection in region 1 / east us
            FaultInjectionRule badSessionTokenRulePrimaryRegion = badSessionTokenRuleBuilder
                .condition(faultInjectionConditionForReadsInPrimaryRegion)
                .result(badSessionTokenServerErrorResult)
                .duration(Duration.ofSeconds(30))
                .build();

            // limit hit count to 3 in region 2 / west us
            FaultInjectionRule badSessionTokenRuleSecondaryRegion = badSessionTokenRuleBuilder
                .condition(faultInjectionConditionForReadsInSecondaryRegion)
                .result(badSessionTokenServerErrorResult)
                .duration(Duration.ofSeconds(30))
                .build();

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(containerForClientWithPreferredRegions,
                    Arrays.asList(badSessionTokenRulePrimaryRegion, badSessionTokenRuleSecondaryRegion))
                .block();

            int threadCount = 1;

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            AtomicBoolean isStopped = new AtomicBoolean(false);

            Duration workloadExecutionDuration = Duration.ofSeconds(30);

            Flux
                .just(1)
                .delayElements(workloadExecutionDuration)
                .doOnComplete(() -> isStopped.compareAndSet(false, true))
                .subscribe();

            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(2)).build();

            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> execute(
                    containerForClientWithPreferredRegions,
                    idAndPkValPair,
                    isStopped,
                    endToEndOperationLatencyPolicyConfig)
                );
            }

            try {
                executorService.awaitTermination(30, TimeUnit.SECONDS);
                executorService.shutdown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {

        } finally {

            // check if we have 404/1002s for all replicas in region 1 / east us
            // check if we have 404/1002s for 3 replicas in region 2 / west us
            // check if we have 201/0 in east us
            // check if we have 200/0 in west us
//            for (Meter meter : meterRegistry.getMeters()) {
//                logger.info(meter.getId().toString());
//            }

            System.clearProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED");
            clientWithPreferredRegions.close();
        }
    }

    private void execute(
        CosmosAsyncContainer container,
        Pair<String, String> idAndPkValPair,
        AtomicBoolean isStopped,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {

        while (!isStopped.get()) {
            try {
                CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
                itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
                CosmosItemResponse<ObjectNode> response = container.readItem(idAndPkValPair.getLeft(), new PartitionKey(idAndPkValPair.getRight()), itemRequestOptions, ObjectNode.class).block();
            } catch (Exception e) {
                if (e instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(e, CosmosException.class);
                    if (cosmosException.getSubStatusCode() == HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT) {
                        // logger.error(e.toString());
                    }
                }
            }
        }
    }

    private static CosmosClientTelemetryConfig createClientTelemetryConfig(CosmosMicrometerMetricsOptions inputMetricsOptions) {
        return new CosmosClientTelemetryConfig()
            .metricsOptions(inputMetricsOptions);
    }

    private static CosmosAsyncClient buildCosmosClient(CosmosClientTelemetryConfig clientTelemetryConfig) {

        List<String> preferredRegions = Arrays.asList("East US", "West US");

        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(preferredRegions)
            .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED).build())
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



    private static InternalObjectNode getDocumentDefinition(String documentId) {
        return
            new InternalObjectNode(String.format("{ "
                    + "\"id\": \"%s\", "
                    + "\"mypk\": \"%s\", "
                    + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                    + "}"
                , documentId, documentId));
    }
}
