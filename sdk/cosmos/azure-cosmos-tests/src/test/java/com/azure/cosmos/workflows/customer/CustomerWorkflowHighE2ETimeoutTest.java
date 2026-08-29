// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.ThresholdBasedAvailabilityStrategy;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowHighE2ETimeoutTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public CustomerWorkflowHighE2ETimeoutTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer high E2E timeout workflow tests");
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @DataProvider(name = "timeoutWorkflowOperations")
    public Object[][] timeoutWorkflowOperations() {
        return new Object[][]{
            {"create", FaultInjectionOperationType.CREATE_ITEM},
            {"read", FaultInjectionOperationType.READ_ITEM},
            {"query", FaultInjectionOperationType.QUERY_ITEM},
            {"readMany", FaultInjectionOperationType.QUERY_ITEM},
            {"upsert", FaultInjectionOperationType.UPSERT_ITEM},
            {"batch", FaultInjectionOperationType.BATCH_ITEM},
            {"patch", FaultInjectionOperationType.PATCH_ITEM}
        };
    }

    @Test(groups = {"fi-customer-workflows"}, dataProvider = "timeoutWorkflowOperations", timeOut = 2 * TIMEOUT)
    public void responseDelayWithAvailabilityStrategyWorkflow(String operation, FaultInjectionOperationType faultInjectionOperationType) {
        TestObject item = TestObject.create();
        if (!"create".equals(operation)) {
            this.container.createItem(item).block();
            registerForCleanup(item);
        }

        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(4))
            .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
            .build();

        // readMany resolves to a point read for a single item, so the QUERY_ITEM data-provider value alone would not
        // exercise the fault - inject the delay for both the point-read and query operation types.
        List<FaultInjectionRule> delayRules = new ArrayList<>();
        if ("readMany".equals(operation)) {
            delayRules.add(configureResponseDelayRule(this.container, FaultInjectionOperationType.READ_ITEM, Duration.ofMillis(1500), 1));
            delayRules.add(configureResponseDelayRule(this.container, FaultInjectionOperationType.QUERY_ITEM, Duration.ofMillis(1500), 1));
        } else {
            delayRules.add(configureResponseDelayRule(this.container, faultInjectionOperationType, Duration.ofMillis(1500), 1));
        }

        try {
            CosmosDiagnosticsContext diagnosticsContext = executeWithE2EPolicy(operation, item, e2ePolicy);

            assertFaultInjectedOperation(diagnosticsContext, delayRules);
            assertThat(diagnosticsContext.getDuration()).isLessThan(Duration.ofSeconds(10));
        } finally {
            delayRules.forEach(FaultInjectionRule::disable);
        }
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = 2 * TIMEOUT)
    public void partitionMigratingFaultWithE2EPolicyWorkflow() {
        TestObject item = TestObject.create();
        this.container.createItem(item).block();
        registerForCleanup(item);

        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(4))
            .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
            .build();

        FaultInjectionRule migratingRule = configureServerErrorRule(
            this.container,
            FaultInjectionOperationType.READ_ITEM,
            FaultInjectionServerErrorType.PARTITION_IS_MIGRATING,
            1);

        try {
            CosmosDiagnosticsContext diagnosticsContext = executeWithE2EPolicy("read", item, e2ePolicy);

            assertFaultInjectedOperation(diagnosticsContext, migratingRule);
            assertThat(diagnosticsContext.getDuration()).isLessThan(Duration.ofSeconds(10));
        } finally {
            migratingRule.disable();
        }
    }

    private CosmosDiagnosticsContext executeWithE2EPolicy(
        String operation,
        TestObject item,
        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy) {

        try {
            if ("create".equals(operation)) {
                TestObject createdItem = TestObject.create();
                CosmosItemRequestOptions options = new CosmosItemRequestOptions()
                    .setContentResponseOnWriteEnabled(true)
                    .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

                CosmosItemResponse<TestObject> response = this.container
                    .createItem(createdItem, options)
                    .block();

                registerForCleanup(createdItem);
                return response.getDiagnostics().getDiagnosticsContext();
            }

            if ("read".equals(operation)) {
                CosmosItemRequestOptions options = new CosmosItemRequestOptions()
                    .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

                return this.container
                    .readItem(item.getId(), partitionKey(item), options, TestObject.class)
                    .block()
                    .getDiagnostics()
                    .getDiagnosticsContext();
            }

            if ("query".equals(operation)) {
                CosmosQueryRequestOptions options = new CosmosQueryRequestOptions()
                    .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy)
                    .setQueryName("HighE2ETimeoutWorkflowQuery");

                FeedResponse<TestObject> response = this.container
                    .queryItems(String.format("SELECT * FROM c WHERE c.id = '%s'", item.getId()), options, TestObject.class)
                    .byPage()
                    .blockFirst();

                return response.getCosmosDiagnostics().getDiagnosticsContext();
            }

            if ("readMany".equals(operation)) {
                CosmosReadManyRequestOptions options = new CosmosReadManyRequestOptions()
                    .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

                FeedResponse<TestObject> response = this.container
                    .readMany(Collections.singletonList(new CosmosItemIdentity(partitionKey(item), item.getId())), options, TestObject.class)
                    .block();

                return response.getCosmosDiagnostics().getDiagnosticsContext();
            }

            if ("upsert".equals(operation)) {
                item.setStringProp("timeout-upsert-" + item.getStringProp());
                CosmosItemRequestOptions options = new CosmosItemRequestOptions()
                    .setContentResponseOnWriteEnabled(true)
                    .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

                return this.container
                    .upsertItem(item, options)
                    .block()
                    .getDiagnostics()
                    .getDiagnosticsContext();
            }

            if ("batch".equals(operation)) {
                TestObject batchItem = TestObject.create("timeout-batch");
                CosmosBatch batch = CosmosBatch.createCosmosBatch(partitionKey(batchItem));
                batch.createItemOperation(batchItem);
                batch.readItemOperation(batchItem.getId());

                CosmosBatchRequestOptions batchOptions = new CosmosBatchRequestOptions();
                ImplementationBridgeHelpers.CosmosBatchRequestOptionsHelper
                    .getCosmosBatchRequestOptionsAccessor()
                    .setEndToEndOperationLatencyPolicyConfig(batchOptions, e2ePolicy);

                CosmosBatchResponse response = this.container.executeCosmosBatch(batch, batchOptions).block();

                registerForCleanup(batchItem);
                return response.getDiagnostics().getDiagnosticsContext();
            }

            CosmosPatchItemRequestOptions options = new CosmosPatchItemRequestOptions();
            options.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

            CosmosItemResponse<TestObject> response = this.container
                .patchItem(
                    item.getId(),
                    partitionKey(item),
                    CosmosPatchOperations.create().set("/stringProp", "timeout-patched-" + item.getStringProp()),
                    options,
                    TestObject.class)
                .block();

            return response.getDiagnostics().getDiagnosticsContext();
        } catch (CosmosException error) {
            return error.getDiagnostics().getDiagnosticsContext();
        }
    }
}
