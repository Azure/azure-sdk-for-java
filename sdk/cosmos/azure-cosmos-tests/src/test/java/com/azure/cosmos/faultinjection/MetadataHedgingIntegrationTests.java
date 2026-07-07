// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Multi-region integration tests for cross-region metadata hedging (port of .NET PR #5999).
 * <p>
 * The PartitionKeyRange routing-map read is forced to re-read (via a {@code PARTITION_IS_SPLITTING} fault on a
 * data operation) while a primary-region {@code RESPONSE_DELAY} is injected on the metadata read. With hedging
 * enabled, the SDK issues a hedge to a second region -- surfaced as a {@code METADATA_HEDGE} diagnostics datum and
 * the secondary region being contacted; with hedging disabled, no hedge is issued.
 * <p>
 * Requires a multi-region account (>= 2 read regions) via {@code COSMOSDB_MULTI_REGION} / {@code ACCOUNT_HOST} +
 * {@code ACCOUNT_KEY}.
 */
public class MetadataHedgingIntegrationTests extends FaultInjectionTestBase {

    private static final String METADATA_HEDGING_ENABLED_PROPERTY = "COSMOS.metadataHedgingEnabled";
    private static final String METADATA_HEDGING_THRESHOLD_PROPERTY = "COSMOS.metadataHedgingThresholdInMs";
    private static final String METADATA_HEDGE_DATUM = "METADATA_HEDGE";
    // Above the (lowered-for-test) hedge threshold, below the gateway request timeout.
    private static final Duration PRIMARY_DELAY = Duration.ofSeconds(4);

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> writePreferredLocations;

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public MetadataHedgingIntegrationTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @BeforeClass(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void beforeClass() {
        // Lower the threshold so a hedge fires quickly once the primary is delayed.
        System.setProperty(METADATA_HEDGING_THRESHOLD_PROPERTY, "500");

        this.client = getClientBuilder().buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(this.client);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        AccountLevelLocationContext writeable = getAccountLevelLocationContext(databaseAccount, true);
        assertThat(writeable.serviceOrderedWriteableRegions.size())
            .as("Metadata hedging tests require a multi-region (multi-master) account with >= 2 regions")
            .isGreaterThanOrEqualTo(2);

        this.writePreferredLocations = writeable.serviceOrderedWriteableRegions;
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);
    }

    @AfterClass(groups = {"multi-region"}, timeOut = TIMEOUT, alwaysRun = true)
    public void afterClass() {
        System.clearProperty(METADATA_HEDGING_ENABLED_PROPERTY);
        System.clearProperty(METADATA_HEDGING_THRESHOLD_PROPERTY);
        safeClose(this.client);
    }

    @Test(groups = {"multi-region"}, timeOut = 4 * TIMEOUT)
    public void partitionKeyRangeHedged_whenEnabledAndPrimarySlow() {
        runPartitionKeyRangeScenario(true);
    }

    @Test(groups = {"multi-region"}, timeOut = 4 * TIMEOUT)
    public void partitionKeyRangeNotHedged_whenDisabled() {
        runPartitionKeyRangeScenario(false);
    }

    private void runPartitionKeyRangeScenario(boolean hedgingEnabled) {
        System.setProperty(METADATA_HEDGING_ENABLED_PROPERTY, Boolean.toString(hedgingEnabled));
        CosmosAsyncClient testClient = getClientBuilder()
            .contentResponseOnWriteEnabled(true)
            .preferredRegions(this.writePreferredLocations)
            .buildAsyncClient();

        CosmosAsyncContainer container = testClient
            .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
            .getContainer(this.cosmosAsyncContainer.getId());

        // Delay the PartitionKeyRange metadata read in the primary region.
        FaultInjectionRule pkRangesDelayRule =
            new FaultInjectionRuleBuilder("pkrange-delay-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.writePreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.METADATA_REQUEST_PARTITION_KEY_RANGES)
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .delay(PRIMARY_DELAY)
                        .times(1)
                        .build())
                .duration(Duration.ofMinutes(5))
                .build();

        // Use a partition split on a write to force a routing-map (PartitionKeyRange) refresh AFTER the rule is active.
        FaultInjectionRule splitRule =
            new FaultInjectionRuleBuilder("split-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .region(this.writePreferredLocations.get(0))
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.PARTITION_IS_SPLITTING)
                        .times(1)
                        .build())
                .duration(Duration.ofMinutes(5))
                .build();

        try {
            // Populate the collection + PartitionKeyRange caches first.
            for (int i = 0; i < 5; i++) {
                container.createItem(TestObject.create()).block();
            }

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(container, Arrays.asList(pkRangesDelayRule, splitRule))
                .block();

            // This write hits PARTITION_IS_SPLITTING -> forces a PartitionKeyRange refresh -> hits the primary delay.
            CosmosDiagnostics diagnostics =
                container.createItem(TestObject.create()).block().getDiagnostics();

            assertHedging(diagnostics, hedgingEnabled);
        } catch (CosmosException e) {
            throw new AssertionError("createItem should have succeeded. " + e.getDiagnostics(), e);
        } finally {
            pkRangesDelayRule.disable();
            splitRule.disable();
            System.clearProperty(METADATA_HEDGING_ENABLED_PROPERTY);
            safeClose(testClient);
        }
    }

    private void assertHedging(CosmosDiagnostics diagnostics, boolean hedgingEnabled) {
        assertThat(diagnostics).isNotNull();
        String json = diagnostics.toString();

        if (hedgingEnabled) {
            assertThat(json.contains(METADATA_HEDGE_DATUM))
                .as("expected a Metadata Hedge datum when hedging is enabled and the primary is slow; diagnostics: %s", json)
                .isTrue();
            assertThat(json.contains("\"hedgeFired\":true"))
                .as("expected the hedge to have fired; diagnostics: %s", json)
                .isTrue();
        } else {
            assertThat(json.contains(METADATA_HEDGE_DATUM))
                .as("no Metadata Hedge datum expected when hedging is disabled; diagnostics: %s", json)
                .isFalse();
        }
    }

    private static AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions,
            regionMap);
    }

    private static class AccountLevelLocationContext {
        private final List<String> serviceOrderedReadableRegions;
        private final List<String> serviceOrderedWriteableRegions;
        private final Map<String, String> regionNameToEndpoint;

        AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions,
            Map<String, String> regionNameToEndpoint) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
            this.regionNameToEndpoint = regionNameToEndpoint;
        }
    }
}
