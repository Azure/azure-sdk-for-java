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
import com.azure.cosmos.GlobalThroughputControlConfig;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.FailureValidator;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.throughputControl.controller.group.global.GlobalThroughputControlClientItem;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.CosmosItemResponseValidator;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ThroughputControlTests extends TestSuiteBase {
    // Delete collections in emulator is not instant,
    // so to avoid get 500 back, we are adding delay for creating the collection with same name, since in this case we want to test 410/1000
    private final static int COLLECTION_RECREATION_TIME_DELAY = 5000;

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
            { OperationType.Query },
         //   { OperationType.ReadFeed } // changeFeed only go for gateway
        };
    }

    @DataProvider
    public static Object[][] allowRequestToContinueOnInitErrorProvider() {
        return new Object[][]{
                { true },
                { false }
        };
    }

    @Test(groups = {"emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputLocalControl(OperationType operationType) {
        // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-" + UUID.randomUUID())
                .targetThroughput(6)
                .build();
        container.enableLocalThroughputControlGroup(groupConfig);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();
        this.validateRequestNotThrottled(
            createItemResponse.getDiagnostics().toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());

        // second request to group-1. which will get throttled
        CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(this.container, operationType, createdItem, groupConfig.getGroupName());
        this.validateRequestThrottled(
            cosmosDiagnostics.toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());
    }

    @Test(groups = {"emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputGlobalControl(OperationType operationType) {
        String controlContainerId = "throughputControlContainer";
        CosmosAsyncContainer controlContainer = database.getContainer(controlContainerId);
        database.createContainerIfNotExists(controlContainer.getId(), "/groupId").block();

        // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-" + UUID.randomUUID())
                .targetThroughput(6)
                .build();

        GlobalThroughputControlConfig globalControlConfig = this.client.createGlobalThroughputControlConfigBuilder(this.database.getId(), controlContainerId)
            .setControlItemRenewInterval(Duration.ofSeconds(5))
            .setControlItemExpireInterval(Duration.ofSeconds(20))
            .build();
        container.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();
        this.validateRequestNotThrottled(
            createItemResponse.getDiagnostics().toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());

        // second request to same group. which will get throttled
        CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(this.container, operationType, createdItem, groupConfig.getGroupName());
        this.validateRequestThrottled(
            cosmosDiagnostics.toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());
    }

    @Test(groups = {"emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputLocalControlForContainerCreateDeleteWithSameName(OperationType operationType) throws InterruptedException {
        ConnectionMode connectionMode = BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode();
        if (connectionMode == ConnectionMode.GATEWAY) {
            // for gateway connection mode, gateway will handle the 410/1000 and retry. Hence the collection cache and container controller will not be refreshed.
            // There is no point for this tests for gateway mode.
            return;
        }

        // step1: create container
        String testContainerId = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(testContainerId);
        CosmosAsyncContainer createdContainer = createCollection(this.database, containerProperties, new CosmosContainerRequestOptions());

        // The create document in this test usually takes around 6.29RU,
        // pick a RU super small here so we know it will throttle requests for several cycles/seconds
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-" + UUID.randomUUID())
                .targetThroughput(1)
                .build();
        container.enableLocalThroughputControlGroup(groupConfig);
        createdContainer.enableLocalThroughputControlGroup(groupConfig);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

        // Step2: first request to group-1, which will not get throttled, but will consume all the rus of the throughput control group.
        CosmosItemResponse<TestItem> createItemResponse = createdContainer.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();
        this.validateRequestNotThrottled(
            createItemResponse.getDiagnostics().toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());

        // Step 3: delete the container
        safeDeleteCollection(createdContainer);
        Thread.sleep(COLLECTION_RECREATION_TIME_DELAY);

        // step 4: recreate the container with the same name
        createdContainer = createCollection(this.database, containerProperties, new CosmosContainerRequestOptions());

        // Step 5: operation which will trigger cache refresh and a new container controller to be built
        createdItem = createdContainer.createItem(getDocumentDefinition()).block().getItem();

        // Step 6: second request to group-1. which will not get throttled because new container controller will be built.
        CosmosDiagnostics cosmosDiagnostics = performDocumentOperation(createdContainer, operationType, createdItem, groupConfig.getGroupName());
        this.validateRequestNotThrottled(
            cosmosDiagnostics.toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());

        // Step 7: third request to group-1, which will get throttled
        cosmosDiagnostics = performDocumentOperation(createdContainer, operationType, createdItem, groupConfig.getGroupName());
        this.validateRequestThrottled(
            cosmosDiagnostics.toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void throughputLocalControl_createItem() throws InterruptedException {
        // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-" + UUID.randomUUID())
                .targetThroughput(6)
                .build();

        container.enableLocalThroughputControlGroup(groupConfig);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        this.validateRequestNotThrottled(
            createItemResponse.getDiagnostics().toString(),
            BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode());

        // second request to same group. which will get throttled
        TestItem itemGetThrottled = getDocumentDefinition(createItemResponse.getItem().getMypk());
        FailureValidator failureValidator = new FailureValidator.Builder().statusCode(429).build();
        validateItemFailure(container.createItem(itemGetThrottled, requestOptions), failureValidator);

        Thread.sleep(500);

        // third request to create same item in step2
        // Make sure the request really get blocked in step2
        CosmosItemResponseValidator successValidator = new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestItem>>()
            .withId(itemGetThrottled.getId())
            .build();
        validateItemSuccess(container.createItem(itemGetThrottled), successValidator);
    }

    @Test(groups = {"emulator"}, dataProvider = "allowRequestToContinueOnInitErrorProvider", timeOut = TIMEOUT)
    public void throughputControlContinueOnInitError(boolean continueOnInitError) {
        // Purposely not creating the throughput control container so to test allowRequestContinueOnInitError
        String controlContainerId = "throughputControlContainer";
        GlobalThroughputControlConfig globalControlConfig =
                this.client.createGlobalThroughputControlConfigBuilder(this.database.getId(), controlContainerId)
                        .setControlItemRenewInterval(Duration.ofSeconds(5))
                        .setControlItemExpireInterval(Duration.ofSeconds(20))
                        .build();

        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        CosmosItemResponseValidator successValidator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                        .build();

        ThroughputControlGroupConfig groupConfig =
                new ThroughputControlGroupConfigBuilder()
                        .groupName("group-" + UUID.randomUUID())
                        .targetThroughput(6)
                        .continueOnInitError(continueOnInitError)
                        .build();

        container.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

        if (continueOnInitError) {
            validateItemSuccess(
                    container.createItem(TestItem.createNewItem(), requestOptions),
                    successValidator);
        } else {
            CosmosAsyncContainer fakeContainer = client.getDatabase(database.getId()).getContainer("fakeContainer");
            validateItemFailure(
                    fakeContainer.createItem(TestItem.createNewItem(), requestOptions),
                    notFoundValidator);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT * 4)
    public void throughputGlobalControlMultipleClients() throws InterruptedException {
        List<CosmosAsyncClient> clients = new ArrayList<>();
        try{
            // and do not enable ttl on the container so to test how many items are created.
            String controlContainerId = "throughputControlContainer";
            CosmosAsyncContainer controlContainer = database.getContainer(controlContainerId);
            database.createContainerIfNotExists(controlContainerId, "/groupId").block();
            ThroughputControlGroupConfig groupConfig =
                    new ThroughputControlGroupConfigBuilder()
                            .groupName("group-" + UUID.randomUUID())
                            .targetThroughput(6)
                            .build();

            int clientCount = 3;
            for (int i = 0; i < clientCount; i++) {
                CosmosAsyncClient testClient = new CosmosClientBuilder()
                        .endpoint(TestConfigurations.HOST)
                        .key(TestConfigurations.MASTER_KEY)
                        .buildAsyncClient();

                clients.add(testClient);

                CosmosAsyncContainer testContainer = testClient.getDatabase(this.database.getId()).getContainer(container.getId());
                GlobalThroughputControlConfig globalControlConfig1 = testClient.createGlobalThroughputControlConfigBuilder(this.database.getId(), controlContainerId)
                        .setControlItemRenewInterval(Duration.ofSeconds(5))
                        .setControlItemExpireInterval(Duration.ofSeconds(20))
                        .build();
                testContainer.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig1);

                CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
                requestOptions.setContentResponseOnWriteEnabled(true);
                requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

                testContainer.createItem(getDocumentDefinition(), requestOptions).block();
            }

            String query = "SELECT * FROM c WHERE CONTAINS(c.groupId, @GROUPID) AND CONTAINS(c.groupId, @CLIENTITEMSUFFIX)";
            List<SqlParameter> parameters = new ArrayList<>();
            parameters.add(new SqlParameter("@GROUPID", groupConfig.getGroupName()));
            parameters.add(new SqlParameter("@CLIENTITEMSUFFIX", ".client"));
            SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

            List<GlobalThroughputControlClientItem> clientItems = controlContainer.queryItems(querySpec, GlobalThroughputControlClientItem.class)
                    .collectList()
                    .block();
            assertThat(clientItems.size()).isEqualTo(clientCount);

        } finally {
            for (CosmosAsyncClient client : clients) {
                if (client != null) {
                    client.close();
                }
            }
        }
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
        assertThat(cosmosDiagnostics).contains("\"statusCode\":429");
        assertThat(cosmosDiagnostics).contains("\"subStatusCode\":10003");
    }

    private void validateRequestNotThrottled(String cosmosDiagnostics, ConnectionMode connectionMode) {
        assertThat(cosmosDiagnostics).isNotEmpty();
        assertThat(cosmosDiagnostics).doesNotContain("\"statusCode\":429");
        assertThat(cosmosDiagnostics).doesNotContain("\"subStatusCode\":10003");
    }

    private CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestItem createdItem,
        String throughputControlGroup) {
        try {
            if (operationType == OperationType.Query) {
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                if (!StringUtils.isEmpty(throughputControlGroup)) {
                    queryRequestOptions.setThroughputControlGroupName(throughputControlGroup);
                }

                String query = String.format("SELECT * from c where c.mypk = '%s'", createdItem.getMypk());
                FeedResponse<TestItem> itemFeedResponse =
                    cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst();

                return itemFeedResponse.getCosmosDiagnostics();
            }

            if (operationType == OperationType.ReadFeed) {
                CosmosChangeFeedRequestOptions changeFeedRequestOptions = CosmosChangeFeedRequestOptions
                    .createForProcessingFromBeginning(FeedRange.forFullRange());
                if (!StringUtils.isEmpty(throughputControlGroup)) {
                    changeFeedRequestOptions.setThroughputControlGroupName(throughputControlGroup);
                }

                FeedResponse<TestItem> itemFeedResponse = cosmosAsyncContainer.queryChangeFeed(changeFeedRequestOptions, TestItem.class).byPage().blockFirst();
                return itemFeedResponse.getCosmosDiagnostics();
            }

            if (operationType == OperationType.Read
                || operationType == OperationType.Delete
                || operationType == OperationType.Replace
                || operationType == OperationType.Create) {
                CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
                if (!StringUtils.isEmpty((throughputControlGroup))) {
                    itemRequestOptions.setThroughputControlGroupName(throughputControlGroup);
                }

                if (operationType == OperationType.Read) {
                    return cosmosAsyncContainer.readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk()),
                        itemRequestOptions,
                        TestItem.class).block().getDiagnostics();
                }

                if (operationType == OperationType.Replace) {
                    return cosmosAsyncContainer.replaceItem(
                        createdItem,
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk()),
                        itemRequestOptions).block().getDiagnostics();
                }

                if (operationType == OperationType.Delete) {
                    return cosmosAsyncContainer.deleteItem(createdItem, itemRequestOptions).block().getDiagnostics();
                }

                if (operationType == OperationType.Create) {
                    TestItem newItem = getDocumentDefinition(createdItem.getMypk());
                    return cosmosAsyncContainer.createItem(newItem, itemRequestOptions).block().getDiagnostics();
                }
            }

            throw new IllegalArgumentException("The operation type is not supported");
        } catch (CosmosException cosmosException) {
            return cosmosException.getDiagnostics();
        }
    }

    // TODO: add tests split
}
