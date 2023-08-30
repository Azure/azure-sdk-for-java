// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class FaultInjectionWithAvailabilityStrategyTests extends TestSuiteBase {

    private final static Logger logger = LoggerFactory.getLogger(FaultInjectionWithAvailabilityStrategyTests.class);

    private List<String> writeableRegions;

    private String testDatabaseId;
    private String testContainerId;


    @Override
    public String resolveTestNameSuffix(Object[] row) {
        if (row == null || row.length == 0) {
            return "";
        }

        return (String)row[0];
    }

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

            this.writeableRegions = new ArrayList<>(writeRegionMap.keySet());
            assertThat(this.writeableRegions).isNotNull();
            assertThat(this.writeableRegions.size()).isGreaterThanOrEqualTo(2);

            CosmosAsyncContainer container = this.createTestContainer(dummyClient);
            this.testDatabaseId = container.getDatabase().getId();
            this.testContainerId = container.getId();

            // Creating a container is an async task - especially with multiple regions it can
            // take some time until the container is available in the remote regions as well
            // When the container does not exist yet, you would see 401 for example for point reads etc.
            // So, adding this delay after container creation to minimize risk of hitting these errors
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } finally {
            safeClose(dummyClient);
        }
    }
    @AfterClass(groups = { "multi-master" })
    public void afterClass() {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode();

        CosmosAsyncClient dummyClient = null;
        if (testDatabaseId != null) {
            try {

                dummyClient = clientBuilder.buildAsyncClient();
                CosmosAsyncDatabase testDatabase = dummyClient
                    .getDatabase(this.testDatabaseId);

                safeDeleteDatabase(testDatabase);
            } finally {
                safeClose(dummyClient);
            }
        }
    }

    private List<String> getFirstRegion() {
        return List.of(this.writeableRegions.get(0));
    }

    private List<String> getAllRegionsExceptFirst() {
        ArrayList<String> regions = new ArrayList<>();
        for (int i = 1; i < this.writeableRegions.size(); i++) {
            regions.add(this.writeableRegions.get(i));
        }

        return regions;
    }

    @DataProvider(name = "testConfigs_readAfterCreation_with_readSessionNotAvailable")
    public Object[][] testConfigs_readAfterCreation_with_readSessionNotAvailable() {
        final String sameDocumentIdJustCreated = null;

        ThresholdBasedAvailabilityStrategy defaultAvailabilityStrategy = new ThresholdBasedAvailabilityStrategy();
        ThresholdBasedAvailabilityStrategy noAvailabilityStrategy = null;
        ThresholdBasedAvailabilityStrategy eagerThresholdAvailabilityStrategy = new ThresholdBasedAvailabilityStrategy(
            Duration.ofMillis(5), Duration.ofMillis(10)
        );


        Consumer<CosmosAsyncContainer> injectReadSessionNotAvailableIntoAllRegions =
            (c) -> injectReadSessionNotAvailableError(c, this.writeableRegions);

        Consumer<CosmosAsyncContainer> injectReadSessionNotAvailableIntoFirstRegionOnly =
            (c) -> injectReadSessionNotAvailableError(c, this.getFirstRegion());

        Consumer<CosmosAsyncContainer> injectReadSessionNotAvailableIntoAllExceptFirstRegion =
            (c) -> injectReadSessionNotAvailableError(c, this.getAllRegionsExceptFirst());

        BiConsumer<Integer, Integer> validateStatusCodeIsReadSessionNotAvailableError =
            (statusCode, subStatusCode) -> {
                assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
            };

        BiConsumer<Integer, Integer> validateStatusCodeIsOperationCancelled =
            (statusCode, subStatusCode) -> {
                assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
                assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT);
            };

        BiConsumer<Integer, Integer> validateStatusCodeIs200Ok =
            (statusCode, subStatusCode) -> assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.OK);

        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForAllRegions =
            (ctx) -> {
                logger.debug(
                    "Diagnostics Context to evaluate: {}",
                    ctx != null ? ctx.toJson() : "NULL");

                assertThat(ctx).isNotNull();
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isEqualTo(this.writeableRegions.size());
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(this.writeableRegions.size());
            };

        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion =
            (ctx) -> {
                logger.info(
                    "Diagnostics Context to evaluate: {}",
                    ctx != null ? ctx.toJson() : "NULL");

                assertThat(ctx).isNotNull();
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isEqualTo (1);
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(1);
                assertThat(ctx.getContactedRegionNames().iterator().next())
                    .isEqualTo(this.writeableRegions.get(0).toLowerCase(Locale.ROOT));
            };

        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover =
            (ctx) -> {
                logger.info(
                    "Diagnostics Context to evaluate: {}",
                    ctx != null ? ctx.toJson() : "NULL");

                assertThat(ctx).isNotNull();
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isEqualTo (1);
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(2);
            };

        return new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout
            //    Availability Strategy used
            //    Region switch hint (404/1002 prefer local or remote retries)
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    Diagnostics context validation callback
            // },
            new Object[] {
                "404-1002_AllRegions_RemotePreferred",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            new Object[] {
                "404-1002_OnlyFirstRegion_RemotePreferred",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            new Object[] {
                "404-1002_AllExceptFirstRegion_RemotePreferred",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllExceptFirstRegion,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            new Object[] {
                "404-1002_AllRegions_LocalPreferred",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            new Object[] {
                "404-1002_OnlyFirstRegion_LocalPreferred",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            new Object[] {
                "404-1002_AllExceptFirstRegion_LocalPreferred",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllExceptFirstRegion,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            new Object[] {
                "404-1002_OnlyFirstRegion_RemotePreferred_NoAvailabilityStrategy",
                Duration.ofSeconds(1),
                null,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok, // First operation will failover from region 1 to region 2 quickly enough
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },
            new Object[] {
                "404-1002_OnlyFirstRegion_LocalPreferred_NoAvailabilityStrategy",
                Duration.ofSeconds(1),
                null,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled, // Too many local retries to allow cross regional failover within e2e timeout
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
        };
    }

    private CosmosAsyncContainer createTestContainer(CosmosAsyncClient clientWithPreferredRegions) {
        String dbId = UUID.randomUUID().toString();
        String containerId = UUID.randomUUID().toString();

        clientWithPreferredRegions.createDatabaseIfNotExists(dbId).block();
        CosmosAsyncDatabase databaseWithSeveralWriteableRegions = clientWithPreferredRegions.getDatabase(dbId);

        // setup db and container and pass their ids accordingly
        // ensure the container has a partition key definition of /mypk

        databaseWithSeveralWriteableRegions
            .createContainerIfNotExists(
                new CosmosContainerProperties(
                    containerId,
                    new PartitionKeyDefinition().setPaths(Arrays.asList("/mypk"))))
            .block();

        return databaseWithSeveralWriteableRegions.getContainer(containerId);
    }

    private static void injectReadSessionNotAvailableError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions) {

        String ruleName = "serverErrorRule-read-session-unavailable-" + UUID.randomUUID();
        FaultInjectionRuleBuilder badSessionTokenRuleBuilder = new FaultInjectionRuleBuilder(ruleName);

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        // inject 404/1002s in all regions
        // configure in accordance with preferredRegions on the client
        for (String writeableRegion : applicableRegions) {
            FaultInjectionCondition faultInjectionConditionForReads =
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region(writeableRegion)
                    .build();

            FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                .build();

            // sustained fault injection
            FaultInjectionRule readSessionUnavailableRule = badSessionTokenRuleBuilder
                .condition(faultInjectionConditionForReads)
                .result(badSessionTokenServerErrorResult)
                .duration(Duration.ofSeconds(120))
                .build();

            faultInjectionRules.add(readSessionUnavailableRule);
        }

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(containerWithSeveralWriteableRegions, faultInjectionRules)
            .block();

        logger.info(
            "FAULT INJECTION - Applied rule '{}' for regions '{}'.",
            ruleName,
            String.join(", ", applicableRegions));
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_readAfterCreation_with_readSessionNotAvailable")
    public void readItemAfterCreatingIt(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        String readItemDocumentIdOverride,
        Consumer<CosmosAsyncContainer> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContext) {

        logger.info("START {}", testCaseId);

        CosmosAsyncClient clientWithPreferredRegions = buildCosmosClient(this.writeableRegions, regionSwitchHint);
        try {
            String documentId = UUID.randomUUID().toString();
            Pair<String, String> idAndPkValPair = new ImmutablePair<>(documentId, documentId);

            CosmosDiagnosticsTest.TestItem createdItem = new CosmosDiagnosticsTest.TestItem(documentId, documentId);
            CosmosAsyncContainer testContainer = clientWithPreferredRegions
                .getDatabase(this.testDatabaseId)
                    .getContainer(this.testContainerId);

            testContainer.createItem(createdItem).block();

            if (faultInjectionCallback != null) {
                faultInjectionCallback.accept(testContainer);
            }

            CosmosEndToEndOperationLatencyPolicyConfigBuilder e2ePolicyBuilder =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(endToEndTimeout)
                    .enable(true);
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
                availabilityStrategy != null
                    ? e2ePolicyBuilder.availabilityStrategy(availabilityStrategy).build()
                    : e2ePolicyBuilder.build();

            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            if (endToEndOperationLatencyPolicyConfig != null) {
                itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
            }

            try {
                CosmosItemResponse<ObjectNode> response = testContainer
                    .readItem(
                        readItemDocumentIdOverride != null ? readItemDocumentIdOverride : idAndPkValPair.getLeft(),
                        new PartitionKey(idAndPkValPair.getRight()),
                        itemRequestOptions,
                        ObjectNode.class)
                    .block();

                validateStatusCode.accept(response.getStatusCode(), null);

                CosmosDiagnosticsContext diagnosticsContext = null;

                if (response != null && response.getDiagnostics() != null) {
                    diagnosticsContext = response.getDiagnostics().getDiagnosticsContext();
                }

                validateDiagnosticsContext.accept(diagnosticsContext);
            } catch (Exception e) {
                if (e instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(e, CosmosException.class);
                    CosmosDiagnosticsContext diagnosticsContext = null;
                    if (cosmosException.getDiagnostics() != null) {
                        diagnosticsContext = cosmosException.getDiagnostics().getDiagnosticsContext();
                    }

                    logger.info("EXCEPTION: ", e);
                    logger.info(
                        "DIAGNOSTICS CONTEXT: {}",
                        diagnosticsContext != null ? diagnosticsContext.toJson(): "NULL");

                    validateStatusCode.accept(cosmosException.getStatusCode(), cosmosException.getSubStatusCode());
                    validateDiagnosticsContext.accept(diagnosticsContext);
                } else {
                    fail("A CosmosException instance should have been thrown.", e);
                }
            }
        } finally {
            safeClose(clientWithPreferredRegions);
        }
    }

    private static CosmosAsyncClient buildCosmosClient(
        List<String> preferredRegions,
        CosmosRegionSwitchHint regionSwitchHint) {

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsHandler(new CosmosDiagnosticsLogger());

        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(preferredRegions)
            .sessionRetryOptions(new SessionRetryOptions(regionSwitchHint))
            .directMode()
            .multipleWriteRegionsEnabled(true)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();
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
