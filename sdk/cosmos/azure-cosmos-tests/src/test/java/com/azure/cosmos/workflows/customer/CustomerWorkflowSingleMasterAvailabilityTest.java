// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowSingleMasterAvailabilityTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public CustomerWorkflowSingleMasterAvailabilityTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-sm-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSingleWriteMultiRegionContainer("Customer single-master workflow tests");
    }

    @AfterClass(groups = {"fi-sm-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @Test(groups = {"fi-sm-customer-workflows"}, timeOut = TIMEOUT)
    public void excludedReadableRegionRoutesReadToRemainingReadableRegion() {
        TestObject item = TestObject.create();
        this.container.createItem(item).block();
        registerForCleanup(item);

        List<String> excludedRegions = excludeFirstReadableRegion();
        CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
            .setExcludedRegions(excludedRegions)
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        // Excluding the preferred readable region forces the read onto the remaining readable region, which may
        // lag behind the just-completed write. Retry until cross-region replication catches up before asserting.
        CosmosItemResponse<TestObject> readResponse = readWithReplicationRetry(item, readOptions);

        assertThat(readResponse).isNotNull();
        CosmosDiagnosticsContext diagnosticsContext = readResponse.getDiagnostics().getDiagnosticsContext();
        assertThat(readResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
        assertThat(diagnosticsContext.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertExcludedRegions(diagnosticsContext, excludedRegions);
        assertDidNotContactExcludedRegions(diagnosticsContext, excludedRegions);
    }

    @Test(groups = {"fi-sm-customer-workflows"}, timeOut = TIMEOUT)
    public void readFaultInPreferredReadableRegionCanUseRemoteReadableRegion() {
        TestObject item = TestObject.create();
        this.container.createItem(item).block();
        registerForCleanup(item);

        FaultInjectionRule readSessionNotAvailableRule = configureServerErrorRule(
            this.container,
            FaultInjectionOperationType.READ_ITEM,
            FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE,
            this.readableRegions.get(0),
            currentFaultInjectionConnectionType(),
            1);

        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
            .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
            .build();

        try {
            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

            CosmosDiagnosticsContext diagnosticsContext = readWithDiagnostics(item, readOptions);

            assertThat(diagnosticsContext).isNotNull();
            assertThat(readSessionNotAvailableRule.getHitCount())
                .as("the injected read-session-not-available fault should have been hit in the preferred readable region")
                .isGreaterThanOrEqualTo(1);
            assertThat(diagnosticsContext.getStatusCode()).isBetween(HttpConstants.StatusCodes.OK, 599);
            assertThat(diagnosticsContext.getContactedRegionNames()).isNotNull();
            if (diagnosticsContext.getStatusCode() < HttpConstants.StatusCodes.BADREQUEST) {
                assertThat(diagnosticsContext.getContactedRegionNames()).isNotEmpty();
            } else {
                assertThat(diagnosticsContext.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(diagnosticsContext.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
            }
        } finally {
            readSessionNotAvailableRule.disable();
        }
    }

    @Test(groups = {"fi-sm-customer-workflows"}, timeOut = TIMEOUT)
    public void writeFaultStaysOnSingleWritableRegion() {
        FaultInjectionRule partitionMigratingRule = configureServerErrorRule(
            this.container,
            FaultInjectionOperationType.CREATE_ITEM,
            FaultInjectionServerErrorType.PARTITION_IS_MIGRATING,
            this.writableRegions.get(0),
            currentFaultInjectionConnectionType(),
            1);

        try {
            CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
                .build();
            CosmosItemRequestOptions createOptions = new CosmosItemRequestOptions()
                .setContentResponseOnWriteEnabled(true)
                .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

            CosmosDiagnosticsContext diagnosticsContext = createWithDiagnostics(TestObject.create(), createOptions);

            assertThat(diagnosticsContext).isNotNull();
            assertThat(partitionMigratingRule.getHitCount())
                .as("the injected write fault should have been hit in the single writable region")
                .isGreaterThanOrEqualTo(1);
            assertThat(diagnosticsContext.getStatusCode()).isBetween(HttpConstants.StatusCodes.OK, 599);
            assertThat(diagnosticsContext.getContactedRegionNames()).isNotNull();

            // A single-write account cannot hedge writes to another region, so even with an availability strategy
            // configured the write must never be routed to a read-only region.
            Set<String> readOnlyRegions = this.readableRegions
                .stream()
                .map(region -> region.toLowerCase(Locale.ROOT))
                .filter(region -> !region.equals(this.writableRegions.get(0).toLowerCase(Locale.ROOT)))
                .collect(Collectors.toSet());
            assertThat(diagnosticsContext.getContactedRegionNames()).doesNotContainAnyElementsOf(readOnlyRegions);
        } finally {
            partitionMigratingRule.disable();
        }
    }

    @DataProvider(name = "singleWriteReadFaultScenarios")
    public Object[][] singleWriteReadFaultScenarios() {
        return new Object[][]{
            {FaultInjectionServerErrorType.GONE},
            {FaultInjectionServerErrorType.TIMEOUT},
            {FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE},
            {FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR},
            {FaultInjectionServerErrorType.SERVICE_UNAVAILABLE}
        };
    }

    @Test(groups = {"fi-sm-customer-workflows"}, dataProvider = "singleWriteReadFaultScenarios", timeOut = TIMEOUT)
    public void singleWriteReadFaultMatrix(FaultInjectionServerErrorType errorType) {
        skipIfFaultTypeUnsupportedOnGateway(errorType, "Customer single-master read fault matrix");

        TestObject item = TestObject.create();
        this.container.createItem(item).block();
        registerForCleanup(item);

        FaultInjectionRule faultRule = configureServerErrorRule(
            this.container,
            FaultInjectionOperationType.READ_ITEM,
            errorType,
            this.readableRegions.get(0),
            currentFaultInjectionConnectionType(),
            1);

        try {
            CosmosItemRequestOptions readOptions = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                        .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
                        .build());

            CosmosDiagnosticsContext diagnosticsContext = readWithDiagnostics(item, readOptions);

            assertFaultInjectedOperation(diagnosticsContext, faultRule);
        } finally {
            faultRule.disable();
        }
    }

    @DataProvider(name = "singleWriteMutationFaultScenarios")
    public Object[][] singleWriteMutationFaultScenarios() {
        return new Object[][]{
            {FaultInjectionServerErrorType.PARTITION_IS_MIGRATING},
            {FaultInjectionServerErrorType.TIMEOUT},
            {FaultInjectionServerErrorType.TOO_MANY_REQUEST},
            {FaultInjectionServerErrorType.RETRY_WITH},
            {FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR},
            {FaultInjectionServerErrorType.SERVICE_UNAVAILABLE}
        };
    }

    @Test(groups = {"fi-sm-customer-workflows"}, dataProvider = "singleWriteMutationFaultScenarios", timeOut = TIMEOUT)
    public void singleWriteCreateFaultMatrix(FaultInjectionServerErrorType errorType) {
        FaultInjectionRule faultRule = configureServerErrorRule(
            this.container,
            FaultInjectionOperationType.CREATE_ITEM,
            errorType,
            this.writableRegions.get(0),
            currentFaultInjectionConnectionType(),
            1);

        try {
            CosmosItemRequestOptions createOptions = new CosmosItemRequestOptions()
                .setContentResponseOnWriteEnabled(true)
                .setCosmosEndToEndOperationLatencyPolicyConfig(
                    new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
                        .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
                        .build());

            CosmosDiagnosticsContext diagnosticsContext = createWithDiagnostics(TestObject.create(), createOptions);

            // The availability strategy cannot hedge writes on a single-write account; the assertion below confirms
            // the injected write fault was still exercised and produced a real HTTP outcome.
            assertFaultInjectedOperation(diagnosticsContext, faultRule);
        } finally {
            faultRule.disable();
        }
    }

    private CosmosDiagnosticsContext readWithDiagnostics(TestObject item, CosmosItemRequestOptions options) {
        try {
            return this.container
                .readItem(item.getId(), partitionKey(item), options, TestObject.class)
                .block()
                .getDiagnostics()
                .getDiagnosticsContext();
        } catch (CosmosException error) {
            return error.getDiagnostics().getDiagnosticsContext();
        }
    }

    private CosmosItemResponse<TestObject> readWithReplicationRetry(TestObject item, CosmosItemRequestOptions options) {
        Duration deadline = Duration.ofSeconds(30);
        long deadlineNanos = System.nanoTime() + deadline.toNanos();
        CosmosException lastNotFound = null;

        while (System.nanoTime() < deadlineNanos) {
            try {
                return this.container
                    .readItem(item.getId(), partitionKey(item), options, TestObject.class)
                    .block();
            } catch (CosmosException error) {
                if (error.getStatusCode() != HttpConstants.StatusCodes.NOTFOUND) {
                    throw error;
                }
                // Item not yet replicated to the remaining readable region - wait and retry.
                lastNotFound = error;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError("Interrupted while waiting for cross-region replication.", interrupted);
                }
            }
        }

        throw new AssertionError("Item was not replicated to the remaining readable region within " + deadline, lastNotFound);
    }

    private CosmosDiagnosticsContext createWithDiagnostics(TestObject item, CosmosItemRequestOptions options) {
        try {
            CosmosDiagnosticsContext diagnosticsContext = this.container
                .createItem(item, new PartitionKey(item.getMypk()), options)
                .block()
                .getDiagnostics()
                .getDiagnosticsContext();

            registerForCleanup(item);
            return diagnosticsContext;
        } catch (CosmosException error) {
            return error.getDiagnostics().getDiagnosticsContext();
        }
    }
}
