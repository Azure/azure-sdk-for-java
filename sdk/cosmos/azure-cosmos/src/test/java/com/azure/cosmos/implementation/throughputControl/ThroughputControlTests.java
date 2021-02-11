// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThroughputControlGroup;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ThroughputControlTests extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "simpleClientBuildersForDirectTcpWithoutRetryOnThrottledRequests")
    public ThroughputControlTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            { OperationType.Read },
            { OperationType.Replace },
            { OperationType.Create },
            { OperationType.Delete },
        };
    }

    @Test(groups = {"emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputLocalControl(OperationType operationType) {

        // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
        ThroughputControlGroup group = container.enableThroughputLocalControlGroup("group-" + UUID.randomUUID(), 10);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(group.getGroupName()); // since can not find the match group, will fall back to the default one

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();
        this.validateRequestNotThrottled(
            createItemResponse.getDiagnostics().toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());

        // second request to group-1. which will get throttled
        CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(operationType, createdItem, group.getGroupName());
        this.validateRequestThrottled(
            cosmosDiagnostics.toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());
    }

    @Test(groups = {"emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputGlobalControl(OperationType operationType) throws InterruptedException {
        CosmosAsyncContainer controlContainer = database.getContainer("throughputControlContainer");
        database.createContainerIfNotExists(controlContainer.getId(), "/group").block();

        // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
        ThroughputControlGroup group = container.enableThroughputGlobalControlGroup(
            "group-" + UUID.randomUUID(),
            10,
            controlContainer,
            Duration.ofSeconds(5),
            Duration.ofSeconds(10));

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(group.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();
        this.validateRequestNotThrottled(
            createItemResponse.getDiagnostics().toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());

        // second request to same group. which will get throttled
        CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(operationType, createdItem, group.getGroupName());
        this.validateRequestThrottled(
            cosmosDiagnostics.toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());
    }

    @BeforeClass(groups = { "emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_ThroughputBudgetControllerTest() {
        client = getClientBuilder().buildAsyncClient();
        database = getSharedCosmosDatabase(client);
        container = getSharedMultiPartitionCosmosContainer(client);
    }

    private static TestItem getDocumentDefinition() {
        return getDocumentDefinition(null);
    }

    private static TestItem getDocumentDefinition(String partitionKey) {
        return new TestItem(
            UUID.randomUUID().toString(),
            StringUtils.isEmpty(partitionKey) ? UUID.randomUUID().toString() : partitionKey,
            UUID.randomUUID().toString()
        );
    }

    private void validateRequestThrottled(String cosmosDiagnostics, ConnectionMode connectionMode) {
        assertThat(cosmosDiagnostics).isNotEmpty();

        if (connectionMode == ConnectionMode.DIRECT) {
            assertThat(cosmosDiagnostics).contains("\"statusCode\":429");
            assertThat(cosmosDiagnostics).contains("\"subStatusCode\":10003");
        } else if (connectionMode == ConnectionMode.GATEWAY) {
            assertThat(cosmosDiagnostics).contains("\"statusAndSubStatusCodes\":[[429,10003]");
        }
    }

    private void validateRequestNotThrottled(String cosmosDiagnostics, ConnectionMode connectionMode) {
        assertThat(cosmosDiagnostics).isNotEmpty();

        if (connectionMode == ConnectionMode.DIRECT) {
            assertThat(cosmosDiagnostics).doesNotContain("\"statusCode\":429");
            assertThat(cosmosDiagnostics).doesNotContain("\"subStatusCode\":10003");
        } else if (connectionMode == ConnectionMode.GATEWAY) {
            assertThat(cosmosDiagnostics).doesNotContain("\"statusAndSubStatusCodes\":[[429,10003]");
        }
    }

    private CosmosDiagnostics performDocumentOperation(OperationType operationType, TestItem createdItem, String throughputControlGroup) {
        if (operationType == OperationType.Query) {
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            if (!StringUtils.isEmpty(throughputControlGroup)) {
                queryRequestOptions.setThroughputControlGroupName(throughputControlGroup);
            }

            String query = String.format("SELECT * from c where c.mypk = '%s'", createdItem.getMypk());
            FeedResponse<TestItem> itemFeedResponse =
                container.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst();

            return itemFeedResponse.getCosmosDiagnostics();
        }

        if (operationType == OperationType.Read
            || operationType == OperationType.Delete
            || operationType == OperationType.Replace
            || operationType == OperationType.Create) {
            try {
                CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
                if (!StringUtils.isEmpty((throughputControlGroup))) {
                    itemRequestOptions.setThroughputControlGroupName(throughputControlGroup);
                }

                if (operationType == OperationType.Read) {
                    return container.readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk()),
                        itemRequestOptions,
                        TestItem.class).block().getDiagnostics();
                }

                if (operationType == OperationType.Replace) {
                    return container.replaceItem(
                        createdItem,
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk()),
                        itemRequestOptions).block().getDiagnostics();
                }

                if (operationType == OperationType.Delete) {
                    return container.deleteItem(createdItem, itemRequestOptions).block().getDiagnostics();
                }

                if (operationType == OperationType.Create) {
                    TestItem newItem = getDocumentDefinition(createdItem.getMypk());
                    return container.createItem(newItem, itemRequestOptions).block().getDiagnostics();
                }
            } catch (CosmosException cosmosException) {
                return cosmosException.getDiagnostics();
            }
        }


        throw new IllegalArgumentException("The operation type is not supported");
    }

    // TODO: add tests for query, changeFeed, split and collection recreation
}
