// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ThroughputControlTests extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

//    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
//    public ThroughputControlTests(CosmosClientBuilder clientBuilder) {
//        super(clientBuilder);
//        this.subscriberValidationTimeout = TIMEOUT;
//    }
//
//    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
//    public <T> void readItem() throws Exception {
//        container.enableThroughputLocalControlGroup("group-1", 5, true);
//        ThroughputControlGroup group2 = container.enableThroughputLocalControlGroup("group-2", 0.9);
//
//        TestItem docDefinition = getDocumentDefinition();
//        container.createItem(docDefinition).block(); // since no group is defined, this will fall into the default control group
//
//        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
//
//        // Test read operation which will fall into the default control group, which should be throttled
//        // and be succeeded during retry
//        CosmosItemResponse<TestItem> readItemResponse = container.readItem(docDefinition.getId(),
//            new PartitionKey(docDefinition.getMypk()),
//            requestOptions,
//            TestItem.class).block();
//        this.validateRequestThrottled(
//            readItemResponse.getDiagnostics().toString(),
//            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());
//
//        // Test read operation which will use a different control group, so it will pass
//        requestOptions.setThroughputControlGroupName(group2.getGroupName());
//        CosmosItemResponse<TestItem> readItemResponse2 = container.readItem(docDefinition.getId(),
//            new PartitionKey(docDefinition.getMypk()),
//            requestOptions,
//            TestItem.class).block();
//        this.validateRequestNotThrottled(
//            readItemResponse2.getDiagnostics().toString(),
//            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());
//
//        // Test read operation which will use an undefined control group, it will fall back to default group
//        // but since the throughput usage has been reset, this request will not be throttled
//
//        requestOptions.setThroughputControlGroupName("Undefined");
//        CosmosItemResponse<TestItem> readItemResponse3 = container.readItem(docDefinition.getId(),
//            new PartitionKey(docDefinition.getMypk()),
//            requestOptions,
//            TestItem.class).block();
//        this.validateRequestNotThrottled(
//            readItemResponse3.getDiagnostics().toString(),
//            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());
//    }
//
//    // TODO: add tests for other operations
//    // TODO: add tests for split and collection recreation
//
//    @BeforeClass(groups = { "emulator" }, timeOut = 4 * SETUP_TIMEOUT)
//    public void before_ThroughputBudgetControllerTest() {
//        client = getClientBuilder().buildAsyncClient();
//        container = getSharedMultiPartitionCosmosContainer(client);
//    }
//
//    private static TestItem getDocumentDefinition() {
//        return new TestItem(
//            UUID.randomUUID().toString(),
//            UUID.randomUUID().toString(),
//            UUID.randomUUID().toString()
//        );
//    }
//
//    private void validateRequestThrottled(String cosmosDiagnostics, ConnectionMode connectionMode) {
//        assertThat(cosmosDiagnostics).isNotEmpty();
//
//        if (connectionMode == ConnectionMode.DIRECT) {
//            assertThat(cosmosDiagnostics).contains("\"statusCode\":429");
//            assertThat(cosmosDiagnostics).contains("\"subStatusCode\":10003");
//        } else if (connectionMode == ConnectionMode.GATEWAY) {
//            assertThat(cosmosDiagnostics).contains("\"statusAndSubStatusCodes\":[[429,10003]");
//        }
//    }
//
//    private void validateRequestNotThrottled(String cosmosDiagnostics, ConnectionMode connectionMode) {
//        assertThat(cosmosDiagnostics).isNotEmpty();
//
//        if (connectionMode == ConnectionMode.DIRECT) {
//            assertThat(cosmosDiagnostics).doesNotContain("\"statusCode\":429");
//            assertThat(cosmosDiagnostics).doesNotContain("\"subStatusCode\":10003");
//        } else if (connectionMode == ConnectionMode.GATEWAY) {
//            assertThat(cosmosDiagnostics).doesNotContain("\"statusAndSubStatusCodes\":[[429,10003]");
//        }
//    }
}
