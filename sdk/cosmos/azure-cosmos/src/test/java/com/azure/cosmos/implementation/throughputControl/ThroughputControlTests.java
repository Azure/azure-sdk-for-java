// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

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

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ThroughputControlTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public <T> void readItem() throws Exception {
        ThroughputControlGroup group1 =
            new ThroughputControlGroup()
                .groupName("group-1")
                .targetContainer(container)
                .targetThroughput(1) // Pick a value relatively slow so for the second requests, we know it is going to be throttled
                .localControlMode()
                .useByDefault();

        ThroughputControlGroup group2 =
            new ThroughputControlGroup()
                .groupName("group-2")
                .targetContainer(container)
                .targetThroughput(5)
                .localControlMode();

        this.client.enableThroughputControl(group1, group2);
        TestItem docDefinition = getDocumentDefinition();
        container.createItem(docDefinition).block(); // since not group is defined, this will fall into the default control group

        // Test read operation which will fall into the default control group, which should be throttled
        // and be succeeded during retry
        CosmosItemResponse<TestItem> readItemResponse = container.readItem(docDefinition.getId(),
            new PartitionKey(docDefinition.getMypk()),
            new CosmosItemRequestOptions(),
            TestItem.class).block();
        String cosmosDiagnostics = readItemResponse.getDiagnostics().toString();
        assertThat(cosmosDiagnostics).contains("\"statusCode\":429");
        assertThat(cosmosDiagnostics).contains("\"subStatusCode\":10003");


        // Test read operation which will use a different control group, so it will pass
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setThroughputControlGroupName(group2.getGroupName());

        CosmosItemResponse<TestItem> readItemResponse2 = container.readItem(docDefinition.getId(),
            new PartitionKey(docDefinition.getMypk()),
            requestOptions,
            TestItem.class).block();

        cosmosDiagnostics = readItemResponse2.getDiagnostics().toString();
        assertThat(cosmosDiagnostics).doesNotContain("\"statusCode\":429");
        assertThat(cosmosDiagnostics).doesNotContain("\"subStatusCode\":10003");
    }

    // TODO: add tests for other operations
    // TODO: add tests for split and collection recreation

    @BeforeClass(groups = { "emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_ThroughputBudgetControllerTest() {
        client = getClientBuilder().buildAsyncClient();
        container = getSharedMultiPartitionCosmosContainer(client);
    }

    private static TestItem getDocumentDefinition() {
        return new TestItem(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
    }
}
