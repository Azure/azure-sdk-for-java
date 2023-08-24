// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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

            this.writeableRegions = new ArrayList<>(writeRegionMap.keySet());
        } finally {
            safeClose(dummyClient);
        }
    }

    @DataProvider(name = "testConfigs")
    public Object[][] testConfigs() {
        return new Object[][] {
            // check if OperationCancelledException bubbles up
            // check if 408 (op-level) & 404/1002 (req-level) metrics are tracked
            //new Object[] {Duration.ofSeconds(2), CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED},
            // check if NotFoundException bubbles up
            // check if 404 (op-level) & 404/1002 (req-level) metrics are tracked
            new Object[] {Duration.ofSeconds(2), CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED}
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

    private void injectReadSessionNotAvailableErrorIntoAllRegions(CosmosAsyncContainer containerWithSeveralWriteableRegions) {
        FaultInjectionRuleBuilder badSessionTokenRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-"
            + "read-session-unavailable-" + UUID.randomUUID());

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        // inject 404/1002s in all regions
        // configure in accordance with preferredRegions on the client
        for (String writeableRegion : this.writeableRegions) {
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
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs")
    public void exceptionHandling_And_MetricsRecording_With_AllRegionsFailing_Test(Duration endToEndTimeout, CosmosRegionSwitchHint regionSwitchHint) {
        execute(endToEndTimeout, regionSwitchHint, this::injectReadSessionNotAvailableErrorIntoAllRegions);
    }

    public void execute(
        Duration endToEndTimeout,
        CosmosRegionSwitchHint regionSwitchHint,
        Consumer<CosmosAsyncContainer> faultInjectionCallback) {
        CosmosAsyncClient clientWithPreferredRegions = null;

        assertThat(this.writeableRegions).isNotNull();
        assertThat(this.writeableRegions.size()).isGreaterThanOrEqualTo(2);

        CosmosAsyncContainer containerWithSeveralWriteableRegions = null;

        try {
            clientWithPreferredRegions = buildCosmosClient(this.writeableRegions, regionSwitchHint);
            containerWithSeveralWriteableRegions = createTestContainer(clientWithPreferredRegions);

            String documentId = UUID.randomUUID().toString();
            Pair<String, String> idAndPkValPair = new ImmutablePair<>(documentId, documentId);

            CosmosDiagnosticsTest.TestItem createdItem = new CosmosDiagnosticsTest.TestItem(documentId, documentId);
            containerWithSeveralWriteableRegions.createItem(createdItem).block();

            if (faultInjectionCallback != null) {
                faultInjectionCallback.accept(containerWithSeveralWriteableRegions);
            }

            ThresholdBasedAvailabilityStrategy thresholdStrategy = new ThresholdBasedAvailabilityStrategy();
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(endToEndTimeout).
                    enable(true).availabilityStrategy(thresholdStrategy).build();

            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            if (endToEndOperationLatencyPolicyConfig != null) {
                itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
            }

            try {
                containerWithSeveralWriteableRegions
                    .readItem(
                        idAndPkValPair.getLeft(),
                        new PartitionKey(idAndPkValPair.getRight()),
                        itemRequestOptions,
                        ObjectNode.class)
                    .block();

                fail("This operation is expected to fail because of the injected 404/1002");
            } catch (Exception e) {
                if (e instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(e, CosmosException.class);
                    logger.info("Expected CosmosException: ", cosmosException);
                    logger.info("Diagnostics Context: {}", cosmosException.getDiagnostics().getDiagnosticsContext().toJson());
                } else {
                    fail("A CosmosException instance should have been thrown.", e);
                }
            }
        } finally {
            if (containerWithSeveralWriteableRegions != null) {
                safeDeleteDatabase(containerWithSeveralWriteableRegions.getDatabase());
            }
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
