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
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.throughputControl.sdk.controller.group.global.GlobalThroughputControlClientItem;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PriorityLevel;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.CosmosItemResponseValidator;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.testng.Assert.fail;

public class ThroughputControlTests extends TestSuiteBase {
    // Delete collections in emulator is not instant,
    // so to avoid get 500 back, we are adding delay for creating the collection with same name, since in this case we want to test 410/1000
    private final static int COLLECTION_RECREATION_TIME_DELAY = 5000;

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "simpleClientBuildersWithoutRetryOnThrottledRequests")
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

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputLocalControl_requestOptions(OperationType operationType) {
        logger.info("Starting test throughputLocalControl_requestOptions with operationType: {}", operationType);
        this.ensureContainer();
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

        String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
        this.validateRequestNotThrottled(cosmosDiagnosticsString);

        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            groupConfig.getGroupName(),
            String.format("(name=%s, default=%s, targetRU=%s)", groupConfig.getGroupName(), false, 6)
        );

        // second request to group-1. which will get throttled
        cosmosDiagnosticsString = performDocumentOperation(this.container, operationType, createdItem, groupConfig.getGroupName()).toString();
        this.validateRequestThrottled(cosmosDiagnosticsString);
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            groupConfig.getGroupName(),
            String.format("(name=%s, default=%s, targetRU=%s)", groupConfig.getGroupName(), false, 6)
        );
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputLocalControl_default(OperationType operationType) {
        this.ensureContainer();

        CosmosAsyncClient cosmosAsyncClient = null;
        try {
            cosmosAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();

            CosmosAsyncContainer testContainer = cosmosAsyncClient.getDatabase(database.getId()).getContainer(container.getId());
            // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
            ThroughputControlGroupConfig groupConfig =
                new ThroughputControlGroupConfigBuilder()
                    .groupName("group-" + UUID.randomUUID())
                    .targetThroughput(6)
                    .defaultControlGroup(true)
                    .build();
            testContainer.enableLocalThroughputControlGroup(groupConfig);

            CosmosItemResponse<TestItem> createItemResponse = testContainer.createItem(getDocumentDefinition()).block();
            TestItem createdItem = createItemResponse.getItem();

            String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
            this.validateRequestNotThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), true, 6)
            );

            // second request to group-1. which will get throttled
            cosmosDiagnosticsString =
                performDocumentOperation(testContainer, operationType, createdItem, groupConfig.getGroupName()).toString();
            this.validateRequestThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), true, 6)
            );
        } finally {
            safeClose(cosmosAsyncClient);
        }
    }

    @Test(groups = {"long-emulator"}, timeOut = TIMEOUT)
    public void throughputLocalControlWithThroughputQuery() {
        // Will need to use a new client here to make sure the throughput query mono will be passed down to throughputContainerController
        CosmosAsyncClient cosmosAsyncClient = null;
        try {
            cosmosAsyncClient = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .buildAsyncClient();

            CosmosAsyncContainer testContainer = cosmosAsyncClient.getDatabase(database.getId()).getContainer(container.getId());

            // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
            ThroughputControlGroupConfig groupConfig =
                new ThroughputControlGroupConfigBuilder()
                    .groupName("group-" + UUID.randomUUID())
                    .targetThroughputThreshold(0.9)
                    .build();

            AtomicInteger throughputQueryMonoCalledCount = new AtomicInteger(0);
            Mono<Integer> throughputQueryMono =
                Mono.just(6).doOnSuccess(throughput -> throughputQueryMonoCalledCount.incrementAndGet());

            ImplementationBridgeHelpers
                .CosmosAsyncContainerHelper
                .getCosmosAsyncContainerAccessor()
                .enableLocalThroughputControlGroup(testContainer, groupConfig, throughputQueryMono);

            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setContentResponseOnWriteEnabled(true);
            requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

            CosmosItemResponse<TestItem> createItemResponse = testContainer.createItem(getDocumentDefinition(), requestOptions).block();
            TestItem createdItem = createItemResponse.getItem();
            String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
            this.validateRequestNotThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRUThreshold=%s)", groupConfig.getGroupName(), false, 0.9)
            );

            // second request to group-1. which will get throttled
            cosmosDiagnosticsString =
                performDocumentOperation(testContainer, OperationType.Read, createdItem, groupConfig.getGroupName()).toString();
            this.validateRequestThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRUThreshold=%s)", groupConfig.getGroupName(), false, 0.9)
            );

            assertThat(throughputQueryMonoCalledCount.get()).isGreaterThanOrEqualTo(1);
        } finally {
            safeClose(cosmosAsyncClient);
        }
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputLocalControlPriorityLevel(OperationType operationType) {
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-" + UUID.randomUUID())
                .priorityLevel(PriorityLevel.LOW)
                .build();

        container.enableLocalThroughputControlGroup(groupConfig);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();
        String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            groupConfig.getGroupName(),
            String.format("(name=%s, default=%s, priorityLevel=%s, targetRU=%s)", groupConfig.getGroupName(), false, PriorityLevel.LOW, Integer.MAX_VALUE)
        );

        cosmosDiagnosticsString = performDocumentOperation(container, operationType, createdItem, groupConfig.getGroupName()).toString();
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            groupConfig.getGroupName(),
            String.format("(name=%s, default=%s, priorityLevel=%s, targetRU=%s)", groupConfig.getGroupName(), false, PriorityLevel.LOW, Integer.MAX_VALUE)
        );
        assertThat(groupConfig.getTargetThroughput()).isEqualTo(Integer.MAX_VALUE);
        assertThat(createItemResponse.getStatusCode()).isEqualTo(201);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputGlobalControl(OperationType operationType) {
        this.ensureContainer();
        String controlContainerId = "tcc" + UUID.randomUUID();
        CosmosAsyncContainer controlContainer = database.getContainer(controlContainerId);

        this.createThroughputControlContainerIfNotExists(database, controlContainerId, 10100);

        try {
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

            String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
            this.validateRequestNotThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), false, 6)
            );

            // second request to same group. which will get throttled
            cosmosDiagnosticsString =
                performDocumentOperation(this.container, operationType, createdItem, groupConfig.getGroupName()).toString();
            this.validateRequestThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), false, 6)
            );
        } finally {
            controlContainer
                .delete()
                .block();
        }
    }

    @Test(groups = {"long-emulator"}, timeOut = TIMEOUT)
    public void throughputGlobalControlWithThroughputQuery() {
        this.ensureContainer();
        // Will need to use a new client here to make sure the throughput query mono will be passed down to throughputContainerController
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        CosmosAsyncContainer testContainer = cosmosAsyncClient.getDatabase(database.getId()).getContainer(container.getId());

        String controlContainerId = "tcc" + UUID.randomUUID();
        this.createThroughputControlContainerIfNotExists(database, controlContainerId, 10100);

        try {
            // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
            ThroughputControlGroupConfig groupConfig =
                new ThroughputControlGroupConfigBuilder()
                    .groupName("group-" + UUID.randomUUID())
                    .targetThroughputThreshold(0.9)
                    .build();

            GlobalThroughputControlConfig globalControlConfig = this.client.createGlobalThroughputControlConfigBuilder(this.database.getId(), controlContainerId)
                .setControlItemRenewInterval(Duration.ofSeconds(5))
                .setControlItemExpireInterval(Duration.ofSeconds(20))
                .build();

            AtomicInteger throughputQueryMonoCalledCount = new AtomicInteger(0);
            Mono<Integer> throughputQueryMono =
                Mono.just(6).doOnSuccess(throughput -> throughputQueryMonoCalledCount.incrementAndGet());
            ImplementationBridgeHelpers
                .CosmosAsyncContainerHelper
                .getCosmosAsyncContainerAccessor()
                .enableGlobalThroughputControlGroup(testContainer, groupConfig, globalControlConfig, throughputQueryMono);

            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setContentResponseOnWriteEnabled(true);
            requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

            CosmosItemResponse<TestItem> createItemResponse = testContainer.createItem(getDocumentDefinition(), requestOptions).block();
            TestItem createdItem = createItemResponse.getItem();
            String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
            this.validateRequestNotThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRUThreshold=%s)",  groupConfig.getGroupName(), false, 0.9)
            );

            // second request to same group. which will get throttled
            cosmosDiagnosticsString =
                performDocumentOperation(testContainer, OperationType.Create, createdItem, groupConfig.getGroupName()).toString();
            this.validateRequestThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRUThreshold=%s)",  groupConfig.getGroupName(), false, 0.9)
            );

            assertThat(throughputQueryMonoCalledCount.get()).isGreaterThanOrEqualTo(1);
        } finally {
            safeDeleteCollection(database.getContainer(controlContainerId));
            safeClose(cosmosAsyncClient);
        }
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputGlobalControlCanUpdateConfig(OperationType operationType) {
        this.ensureContainer();
        String controlContainerId = "tcc" + UUID.randomUUID();
        this.createThroughputControlContainerIfNotExists(database, controlContainerId, 10100);

        try {
            List<Pair<Integer, Boolean>> testCases = new ArrayList<>(
                Arrays.asList(
                    Pair.of(6, true),
                    Pair.of(100, false)
                )
            );

            UUID randomId = UUID.randomUUID();

            for (Pair<Integer, Boolean> testCase : testCases) {
                int targetThroughput = testCase.getLeft();
                boolean shouldThrottleSecondRequest = testCase.getRight();

                logger.info(
                    "TESTCASE - TargetThroughput: {}. ShouldThrottleSecondRequest: {}",
                    targetThroughput,
                    shouldThrottleSecondRequest);

                // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
                ThroughputControlGroupConfig groupConfig =
                    new ThroughputControlGroupConfigBuilder()
                        .groupName("group-" + randomId)
                        .targetThroughput(targetThroughput)
                        .build();

                GlobalThroughputControlConfig globalControlConfig =
                    this.client.createGlobalThroughputControlConfigBuilder(this.database.getId(), controlContainerId)
                               .setControlItemRenewInterval(Duration.ofSeconds(5))
                               .setControlItemExpireInterval(Duration.ofSeconds(20))
                               .build();
                container.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig);

                CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
                requestOptions.setContentResponseOnWriteEnabled(true);
                requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

                CosmosItemResponse<TestItem> createItemResponse = container
                    .createItem(getDocumentDefinition(), requestOptions)
                    .block();
                TestItem createdItem = createItemResponse.getItem();

                String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
                this.validateRequestNotThrottled(cosmosDiagnosticsString);
                this.validateThroughputControlDiagnostics(
                    cosmosDiagnosticsString,
                    groupConfig.getGroupName(),
                    String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), false, targetThroughput)
                );

                cosmosDiagnosticsString =
                    performDocumentOperation(this.container, operationType, createdItem, groupConfig.getGroupName()).toString();

                if (shouldThrottleSecondRequest) {
                    this.validateRequestThrottled(cosmosDiagnosticsString);
                } else {
                    this.validateRequestNotThrottled(cosmosDiagnosticsString);
                }

                this.validateThroughputControlDiagnostics(
                    cosmosDiagnosticsString,
                    groupConfig.getGroupName(),
                    String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), false, targetThroughput)
                );
            }
        } finally {
            safeDeleteCollection(database.getContainer(controlContainerId));
        }
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputLocalControlForContainerCreateDeleteWithSameName(OperationType operationType) throws InterruptedException {
        this.ensureContainer();
        ConnectionMode connectionMode = BridgeInternal.getContextClient(client).getConnectionPolicy().getConnectionMode();
        if (connectionMode == ConnectionMode.GATEWAY) {
            // for gateway connection mode, gateway will handle the 410/1000 and retry. Hence the collection cache and container controller will not be refreshed.
            // There is no point for this tests for gateway mode.
            return;
        }

        // step1: create container
        String testContainerId = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(testContainerId);
        CosmosAsyncContainer createdContainer = createCollection(
            this.database,
            containerProperties,
            new CosmosContainerRequestOptions(),
            10000);

        try {
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

            String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
            this.validateRequestNotThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRU=%s)", groupConfig.getGroupName(), false, 1)
            );

            // Step 3: delete the container
            safeDeleteCollection(createdContainer);
            Thread.sleep(COLLECTION_RECREATION_TIME_DELAY);

            // step 4: recreate the container with the same name
            createdContainer = createCollection(
                this.database, containerProperties, new CosmosContainerRequestOptions(), 10000);

            // Step 5: operation which will trigger cache refresh and a new container controller to be built
            createdItem = createdContainer.createItem(getDocumentDefinition()).block().getItem();

            // Step 6: second request to group-1. which will not get throttled because new container controller will be built.
            cosmosDiagnosticsString =
                performDocumentOperation(createdContainer, operationType, createdItem, groupConfig.getGroupName()).toString();
            this.validateRequestNotThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), false, 1)
            );

            // Step 7: third request to group-1, which will get throttled
            cosmosDiagnosticsString =
                performDocumentOperation(createdContainer, operationType, createdItem, groupConfig.getGroupName()).toString();
            this.validateRequestThrottled(cosmosDiagnosticsString);
            this.validateThroughputControlDiagnostics(
                cosmosDiagnosticsString,
                groupConfig.getGroupName(),
                String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), false, 1)
            );
        } finally {
            createdContainer.delete().block();
        }
    }

    @Test(groups = {"long-emulator"}, timeOut = TIMEOUT)
    public void throughputLocalControl_createItem() throws InterruptedException {
        this.ensureContainer();
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

        String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
        this.validateRequestNotThrottled(cosmosDiagnosticsString);
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            groupConfig.getGroupName(),
            String.format("(name=%s, default=%s, targetRU=%s)",  groupConfig.getGroupName(), false, 6)
        );

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

    @Test(groups = {"long-emulator"}, dataProvider = "allowRequestToContinueOnInitErrorProvider", timeOut = TIMEOUT)
    public void throughputControlContinueOnInitError(boolean continueOnInitError) {
        this.ensureContainer();
        // Purposely not creating the throughput control container so to test allowRequestContinueOnInitError
        String controlContainerId = "tcc" + UUID.randomUUID();
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

    @Test(groups = {"long-emulator"}, timeOut = TIMEOUT * 4)
    public void throughputGlobalControlMultipleClients() throws InterruptedException {
        this.ensureContainer();
        List<CosmosAsyncClient> cosmosAsyncClients = new ArrayList<>();
        // and do not enable ttl on the container so to test how many items are created.
        String controlContainerId = "tcc" + UUID.randomUUID();
        this.createThroughputControlContainerIfNotExists(database, controlContainerId, 10100);

        try {
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

                cosmosAsyncClients.add(testClient);

                CosmosAsyncContainer testContainer =
                    testClient.getDatabase(this.database.getId()).getContainer(container.getId());
                GlobalThroughputControlConfig globalControlConfig1 =
                    testClient
                        .createGlobalThroughputControlConfigBuilder(this.database.getId(), controlContainerId)
                        .setControlItemRenewInterval(Duration.ofSeconds(5))
                        .setControlItemExpireInterval(Duration.ofSeconds(20))
                        .build();
                testContainer.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig1);

                CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
                requestOptions.setContentResponseOnWriteEnabled(true);
                requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

                testContainer.createItem(getDocumentDefinition(), requestOptions).block();
            }

            List<GlobalThroughputControlClientItem> clientItems =
                this.getClientItems(groupConfig.getGroupName(), database.getContainer(controlContainerId));
            assertThat(clientItems.size()).isEqualTo(clientCount);

        } finally {
            safeDeleteCollection(database.getContainer(controlContainerId));

            for (CosmosAsyncClient client : cosmosAsyncClients) {
                safeClose(client);
            }
        }
    }

    @Test(groups = {"long-emulator"}, timeOut = TIMEOUT * 4)
    public void enableSameGroupMultipleTimes() {
        this.ensureContainer();

        // This test is to validate even though same groups have been enabled multiple times, no new client item will be created

        String controlContainerId = "tcc" + UUID.randomUUID();
        this.createThroughputControlContainerIfNotExists(database, controlContainerId, 10100);
        CosmosAsyncContainer controlContainer = database.getContainer(controlContainerId);

        try {
            UUID randomId = UUID.randomUUID();

            ThroughputControlGroupConfig groupConfig =
                new ThroughputControlGroupConfigBuilder()
                    .groupName("group-" + randomId)
                    .targetThroughputThreshold(1)
                    .build();

            GlobalThroughputControlConfig globalControlConfig = this.client.createGlobalThroughputControlConfigBuilder(this.database.getId(), controlContainerId)
                                                                           .setControlItemRenewInterval(Duration.ofSeconds(5))
                                                                           .setControlItemExpireInterval(Duration.ofSeconds(20))
                                                                           .build();
            container.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig);

            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setContentResponseOnWriteEnabled(true);
            requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());
            container.createItem(getDocumentDefinition(), requestOptions).block();

            List<GlobalThroughputControlClientItem> clientItems = this.getClientItems(groupConfig.getGroupName(), controlContainer);
            assertThat(clientItems.size()).isEqualTo(1);
            String clientId = clientItems.get(0).getId();

            // now enable the same group again
            container.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig);
            container.createItem(getDocumentDefinition(), requestOptions).block();
            clientItems = this.getClientItems(groupConfig.getGroupName(), controlContainer);

            // validate no new client item being created
            assertThat(clientItems.size()).isEqualTo(1);
            assertThat(clientItems.get(0).getId()).isEqualTo(clientId);

            // create group with same name but different target throughput
            ThroughputControlGroupConfig groupConfigDifferentTargetThroughput =
                new ThroughputControlGroupConfigBuilder()
                    .groupName("group-" + randomId)
                    .targetThroughputThreshold(0.99)
                    .build();

            // now enable the group with same name but different target throughput
            container.enableGlobalThroughputControlGroup(groupConfigDifferentTargetThroughput, globalControlConfig);
            container.createItem(getDocumentDefinition(), requestOptions).block();
            clientItems = this.getClientItems(groupConfigDifferentTargetThroughput.getGroupName(), controlContainer);

            // validate that a new client item gets created for the group with same name but modified target throughput
            assertThat(clientItems.size()).isEqualTo(2);

            // validate that only one client has the original clientId
            assertThat(
                clientItems.stream().filter(clientItem -> clientItem.getId().equals(clientId)).count())
                .isEqualTo(1);

            // validate that both clientIds have even different prefix (besides the random UUID suffix)
            // because the target throughput s encoded in the id prefix
            String clientIdPrefix = clientId.substring(0, clientId.length() - randomId.toString().length());
            assertThat(
                clientItems.stream().filter(clientItem -> clientItem.getId().startsWith(clientIdPrefix)).count())
                .isEqualTo(1);
        } finally {
            safeDeleteCollection(controlContainer);
        }
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void serverThroughputControl_throughputBucket(OperationType operationType) {
        // TODO: currently there is no easy way to do e2e testing, so the testing here is to just verify that
        // server throughput can be enabled on the container with throughput bucket
        this.ensureContainer();

        ThroughputControlGroupConfig serverThroughputControlGroup =
            new ThroughputControlGroupConfigBuilder()
                .groupName("serverThroughputControl")
                .throughputBucket(2)
                .build();
        container.enableServerThroughputControlGroup(serverThroughputControlGroup);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(serverThroughputControlGroup.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();

        String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
        this.validateRequestNotThrottled(cosmosDiagnosticsString);
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            serverThroughputControlGroup.getGroupName(),
            String.format("(name=%s, default=%s, throughputBucket=%s)", serverThroughputControlGroup.getGroupName(), false, 2)
        );

        cosmosDiagnosticsString = performDocumentOperation(
            this.container,
            operationType,
            createdItem,
            serverThroughputControlGroup.getGroupName()).toString();
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            serverThroughputControlGroup.getGroupName(),
            String.format("(name=%s, default=%s, throughputBucket=%s)", serverThroughputControlGroup.getGroupName(), false, 2)
        );
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void serverThroughputControl_priorityLevel(OperationType operationType) {
        // TODO: currently there is no easy way to do e2e testing, so the testing here is to just verify that
        // server throughput can be enabled on the container with priority level
        this.ensureContainer();

        ThroughputControlGroupConfig serverThroughputControlGroup =
            new ThroughputControlGroupConfigBuilder()
                .groupName("serverThroughputControl")
                .priorityLevel(PriorityLevel.LOW)
                .build();
        container.enableServerThroughputControlGroup(serverThroughputControlGroup);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(serverThroughputControlGroup.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();

        String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
        this.validateRequestNotThrottled(cosmosDiagnosticsString);
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            serverThroughputControlGroup.getGroupName(),
            String.format("(name=%s, default=%s, priorityLevel=%s)", serverThroughputControlGroup.getGroupName(), false, PriorityLevel.LOW)
        );

        cosmosDiagnosticsString = performDocumentOperation(
            this.container,
            operationType,
            createdItem,
            serverThroughputControlGroup.getGroupName()).toString();
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            serverThroughputControlGroup.getGroupName(),
            String.format("(name=%s, default=%s, priorityLevel=%s)", serverThroughputControlGroup.getGroupName(), false, PriorityLevel.LOW)
        );
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void serverThroughputControl_priorityLevel_throughputBucket(OperationType operationType) {
        // TODO: currently there is no easy way to do e2e testing, so the testing here is to just verify that
        // server throughput can be enabled on the container with priority level and throughput bucket
        this.ensureContainer();

        ThroughputControlGroupConfig serverThroughputControlGroup =
            new ThroughputControlGroupConfigBuilder()
                .groupName("serverThroughputControl")
                .priorityLevel(PriorityLevel.LOW)
                .throughputBucket(3)
                .build();
        container.enableServerThroughputControlGroup(serverThroughputControlGroup);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(serverThroughputControlGroup.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();

        String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
        this.validateRequestNotThrottled(cosmosDiagnosticsString);
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            serverThroughputControlGroup.getGroupName(),
            String.format(
                "(name=%s, default=%s, priorityLevel=%s, throughputBucket=%s)",
                serverThroughputControlGroup.getGroupName(),
                false,
                PriorityLevel.LOW,
                3)
        );

        cosmosDiagnosticsString = performDocumentOperation(
            this.container,
            operationType,
            createdItem,
            serverThroughputControlGroup.getGroupName()).toString();
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            serverThroughputControlGroup.getGroupName(),
            String.format(
                "(name=%s, default=%s, priorityLevel=%s, throughputBucket=%s)",
                serverThroughputControlGroup.getGroupName(),
                false,
                PriorityLevel.LOW,
                3)
        );
    }

    @Test(groups = {"long-emulator"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void throughputControl_LocalAndServer_requestOptions(OperationType operationType) {
        this.ensureContainer();

        // This test is verify that SDK throughput control group and server throughput control group can be enabled at the same time

        // The create document in this test usually takes around 6.29RU, pick a RU here relatively close, so to test throttled scenario
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-sdk" + UUID.randomUUID())
                .targetThroughput(6)
                .build();
        container.enableLocalThroughputControlGroup(groupConfig);

        ThroughputControlGroupConfig serverGroupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-server" + UUID.randomUUID())
                .throughputBucket(3)
                .build();
        container.enableServerThroughputControlGroup(serverGroupConfig);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setContentResponseOnWriteEnabled(true);
        requestOptions.setThroughputControlGroupName(groupConfig.getGroupName());

        CosmosItemResponse<TestItem> createItemResponse = container.createItem(getDocumentDefinition(), requestOptions).block();
        TestItem createdItem = createItemResponse.getItem();

        String cosmosDiagnosticsString = createItemResponse.getDiagnostics().toString();
        this.validateRequestNotThrottled(cosmosDiagnosticsString);
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            groupConfig.getGroupName(),
            String.format("(name=%s, default=%s, targetRU=%s)", groupConfig.getGroupName(), false, 6)
        );

        // second request to group-1. which will get throttled
        cosmosDiagnosticsString =
            performDocumentOperation(this.container, operationType, createdItem, groupConfig.getGroupName()).toString();
        this.validateRequestThrottled(cosmosDiagnosticsString);
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            groupConfig.getGroupName(),
            String.format("(name=%s, default=%s, targetRU=%s)", groupConfig.getGroupName(), false, 6)
        );

        // third request to server group, which will not get throttled
        cosmosDiagnosticsString =
            performDocumentOperation(this.container, operationType, createdItem, serverGroupConfig.getGroupName()).toString();
        this.validateRequestNotThrottled(cosmosDiagnosticsString);
        this.validateThroughputControlDiagnostics(
            cosmosDiagnosticsString,
            serverGroupConfig.getGroupName(),
            String.format("(name=%s, default=%s, throughputBucket=%s)", serverGroupConfig.getGroupName(), false, 3)
        );
    }

    @Test(groups = {"long-emulator"}, timeOut = TIMEOUT)
    public void throughputControlDefaultGroup_LocalAndServer_requestOptions() {
        this.ensureContainer();

        // This test is verify that only one default throughput control group can be defined across sdk and server control group
        ThroughputControlGroupConfig groupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-sdk" + UUID.randomUUID())
                .targetThroughput(6)
                .defaultControlGroup(true)
                .build();
        container.enableLocalThroughputControlGroup(groupConfig);

        ThroughputControlGroupConfig serverGroupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("group-server" + UUID.randomUUID())
                .throughputBucket(3)
                .defaultControlGroup(true)
                .build();

        assertThatThrownBy(
                () ->container.enableServerThroughputControlGroup(serverGroupConfig))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("A default group already exists");
    }

    @Test(groups = {"long-emulator"}, timeOut = TIMEOUT)
    public void throughputControl_noThroughputControlGroupEnabled() {
        this.ensureContainer();

        // This test is verify that request will succeed if no throughput control group defined
        String throughputControlGroupName = "throughputControlGroup-" + UUID.randomUUID();
        CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
        itemRequestOptions.setThroughputControlGroupName(throughputControlGroupName);
        String cosmosDiagnosticsString =
            container
                .createItem(getDocumentDefinition(), itemRequestOptions)
                .block()
                .getDiagnostics()
                .toString();
        this.validateRequestNotThrottled(cosmosDiagnosticsString);
        assertThat(cosmosDiagnosticsString).contains("requestTCG");
        assertThat(cosmosDiagnosticsString).contains(throughputControlGroupName);
        assertThat(cosmosDiagnosticsString).doesNotContain("requestTCGConfig");
    }

    @BeforeClass(groups = { "long-emulator" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_ThroughputControllerTest() {
        this.ensureContainer();
    }

    private void ensureContainer() {
        safeClose(this.client);
        client = getClientBuilder().buildAsyncClient();
        database = getSharedCosmosDatabase(client);
        container = getSharedMultiPartitionCosmosContainer(client);

        try {
            try {
                container.read().block();
            } catch (CosmosException error) {
                if (error.getStatusCode() == 404) {
                    this.beforeSuite();
                }

                throw error;
            }
        } catch (CosmosException stillError) {
            logger.error("Can't get shared container '{}'", container.getId(), stillError);
            fail("Can't get shared container.");
        }
    }

    @AfterClass(groups = {"long-emulator"}, timeOut = TIMEOUT, alwaysRun = true)
    public void after_ThroughputControllerTest() {
        safeClose(this.client);
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

    private void validateRequestThrottled(String cosmosDiagnostics) {
        assertThat(cosmosDiagnostics).isNotEmpty();
        assertThat(cosmosDiagnostics).contains("\"statusCode\":429");
        assertThat(cosmosDiagnostics).contains("\"subStatusCode\":10003");
    }

    private void validateRequestNotThrottled(String cosmosDiagnostics) {
        assertThat(cosmosDiagnostics).isNotEmpty();
        assertThat(cosmosDiagnostics).doesNotContain("\"statusCode\":429");
        assertThat(cosmosDiagnostics).doesNotContain("\"subStatusCode\":10003");
    }

    private void validateThroughputControlDiagnostics(
        String cosmosDiagnostics,
        String groupName,
        String groupConfig) {

        assertThat(cosmosDiagnostics).isNotEmpty();
        assertThat(cosmosDiagnostics).contains("requestTCG");
        assertThat(cosmosDiagnostics).contains(groupName);
        assertThat(cosmosDiagnostics).contains("requestTCGConfig");
        assertThat(cosmosDiagnostics).contains(groupConfig);
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

    private List<GlobalThroughputControlClientItem> getClientItems(String groupName, CosmosAsyncContainer controlContainer) {
        String query = "SELECT * FROM c WHERE CONTAINS(c.groupId, @GROUPID) AND CONTAINS(c.groupId, @CLIENTITEMSUFFIX)";
        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(new SqlParameter("@GROUPID", groupName));
        parameters.add(new SqlParameter("@CLIENTITEMSUFFIX", ".client"));
        SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

        return controlContainer.queryItems(querySpec, GlobalThroughputControlClientItem.class)
                .collectList()
                .block();
    }

    private List<GlobalThroughputControlClientItem> getClientItems(CosmosAsyncContainer controlContainer) {
        String query = "SELECT * FROM c WHERE CONTAINS(c.groupId, @CLIENTITEMSUFFIX)";
        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(new SqlParameter("@CLIENTITEMSUFFIX", ".client"));
        SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

        return controlContainer.queryItems(querySpec, GlobalThroughputControlClientItem.class)
                               .collectList()
                               .block();
    }

    private void createThroughputControlContainerIfNotExists(
        CosmosAsyncDatabase database,
        String controlContainerId,
        int throughput) {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(controlContainerId, "/groupId");
        containerProperties.setDefaultTimeToLiveInSeconds(-1);

        database.createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(throughput)).block();
    }

    // TODO: add tests split
}
