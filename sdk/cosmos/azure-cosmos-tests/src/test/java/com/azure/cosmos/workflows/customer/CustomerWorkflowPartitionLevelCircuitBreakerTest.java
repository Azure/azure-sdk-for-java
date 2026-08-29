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
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowPartitionLevelCircuitBreakerTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public CustomerWorkflowPartitionLevelCircuitBreakerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer PCLB workflow tests");
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = 2 * TIMEOUT)
    public void pointOperationCircuitBreakerAndQueryPlanWorkflow() {
        TestObject item = TestObject.create();
        this.container.createItem(item).block();
        registerForCleanup(item);

        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(3))
            .availabilityStrategy(new ThresholdBasedAvailabilityStrategy(Duration.ofMillis(100), Duration.ofMillis(200)))
            .build();

        FaultInjectionRule readFaultRule = configureServerErrorRule(
            this.container,
            FaultInjectionOperationType.READ_ITEM,
            FaultInjectionServerErrorType.SERVICE_UNAVAILABLE,
            1);

        try {
            CosmosDiagnosticsContext readDiagnostics = readWithPolicy(item, e2ePolicy);

            assertFaultInjectedOperation(readDiagnostics, readFaultRule);
        } finally {
            readFaultRule.disable();
        }

        CosmosDiagnosticsContext queryDiagnostics = queryWithPolicy(item, e2ePolicy);
        assertThat(queryDiagnostics).isNotNull();
        assertThat(queryDiagnostics.getStatusCode()).isBetween(200, 599);
        assertThat(queryDiagnostics.getContactedRegionNames()).isNotNull();
        assertThat(queryDiagnostics.toJson()).contains("queryPlanDiagnosticsContext");

        CosmosPatchItemRequestOptions patchOptions = new CosmosPatchItemRequestOptions();
        patchOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);
        CosmosItemResponse<TestObject> patchResponse = this.container
            .patchItem(
                item.getId(),
                partitionKey(item),
                CosmosPatchOperations.create().set("/stringProp", "pclb-patched-" + item.getStringProp()),
                patchOptions,
                TestObject.class)
            .block();

        assertThat(patchResponse).isNotNull();
        assertThat(patchResponse.getDiagnostics()).isNotNull();
    }

    private CosmosDiagnosticsContext readWithPolicy(TestObject item, CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy) {
        try {
            CosmosItemRequestOptions options = new CosmosItemRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

            return this.container
                .readItem(item.getId(), partitionKey(item), options, TestObject.class)
                .block()
                .getDiagnostics()
                .getDiagnosticsContext();
        } catch (CosmosException error) {
            return error.getDiagnostics().getDiagnosticsContext();
        }
    }

    private CosmosDiagnosticsContext queryWithPolicy(TestObject item, CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy) {
        try {
            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions()
                .setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy)
                .setQueryName("PclbCustomerWorkflowQuery");

            // ORDER BY forces the gateway query-plan round-trip so the queryPlanDiagnosticsContext is always present,
            // independent of single-partition / ServiceInterop query-plan optimizations.
            FeedResponse<TestObject> response = this.container
                .queryItems(
                    String.format("SELECT * FROM c WHERE c.id = '%s' ORDER BY c.id", item.getId()),
                    queryOptions,
                    TestObject.class)
                .byPage()
                .blockFirst();

            return response.getCosmosDiagnostics().getDiagnosticsContext();
        } catch (CosmosException error) {
            return error.getDiagnostics().getDiagnosticsContext();
        }
    }
}
