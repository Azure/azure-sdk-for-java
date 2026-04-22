// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BatchTestBase;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.CosmosBulkExecutionOptionsImpl;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionErrorType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkExecutorTest extends BatchTestBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;
    private CosmosAsyncDatabase database;
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();

    @Factory(dataProvider = "simpleClientBuildersWithJustDirectTcp")
    public BulkExecutorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @AfterClass(groups = { "emulator" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeDeleteDatabase(database);
        safeClose(client);
    }

    @AfterMethod(groups = { "emulator" })
    public void afterTest() throws Exception {
        if (this.container != null) {
            try {
                this.container.delete().block();
            } catch (CosmosException error) {
                if (error.getStatusCode() != 404) {
                    throw error;
                }
            }
        }
    }

    @BeforeMethod(groups = { "emulator" })
    public void beforeTest() throws Exception {
        this.container = null;
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        client = getClientBuilder().buildAsyncClient();
        database = createDatabase(client, preExistingDatabaseId);
    }

    static protected CosmosAsyncContainer createContainer(CosmosAsyncDatabase database) {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        database.createContainer(containerProperties).block();
        return database.getContainer(collectionName);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void executeBulk_cancel() throws InterruptedException {
        int totalRequest = 100;
        this.container = createContainer(database);

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();
            BatchTestBase.TestDoc testDoc = this.populateTestDoc(partitionKey);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc,
                new PartitionKey(partitionKey)));

            partitionKey = UUID.randomUUID().toString();
            BatchTestBase.EventDoc eventDoc = new BatchTestBase.EventDoc(UUID.randomUUID().toString(), 2, 4, "type1",
                partitionKey);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(eventDoc,
                new PartitionKey(partitionKey)));
        }

        CosmosItemOperation[] itemOperationsArray =
            new CosmosItemOperation[cosmosItemOperations.size()];
        cosmosItemOperations.toArray(itemOperationsArray);
        CosmosBulkExecutionOptionsImpl cosmosBulkExecutionOptions = new CosmosBulkExecutionOptionsImpl();
        Flux<CosmosItemOperation> inputFlux = Flux
            .fromArray(itemOperationsArray)
            .delayElements(Duration.ofMillis(100));
        final BulkExecutor<BulkExecutorTest> executor = new BulkExecutor<>(
            container,
            inputFlux,
            cosmosBulkExecutionOptions);
        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponseFlux =
            Flux.deferContextual(context -> executor.execute());

        Disposable disposable = bulkResponseFlux.subscribe();
        disposable.dispose();

        int iterations = 0;
        while (true) {
            assertThat(iterations < 100);
            if (executor.isDisposed()) {
                break;
            }

            Thread.sleep(10);
            iterations++;
        }
    }

    // Write operations should not be retried on a gone exception because the operation might have succeeded.
    @Test(groups = { "emulator" }, timeOut =  TIMEOUT)
    public void executeBulk_OnGoneFailure() {
        this.container = createContainer(database);
        if (!ImplementationBridgeHelpers
            .CosmosAsyncClientHelper
            .getCosmosAsyncClientAccessor()
            .getConnectionMode(this.client)
            .equals(ConnectionMode.DIRECT.toString())) {
            throw new SkipException("Failure injection for gone exception only supported for DIRECT mode");
        }

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        String duplicatePK = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();

        BatchTestBase.EventDoc eventDoc = new BatchTestBase.EventDoc(id, 2, 4, "type1",
            duplicatePK);
        CosmosItemOperation createOperation = (CosmosBulkOperations.getCreateItemOperation(eventDoc,
            new PartitionKey(duplicatePK)));
        cosmosItemOperations.add(createOperation);

        // configure fault injection rules
        // using the combination of connection close and response delay to simulate a client generated gone for write operations
        FaultInjectionRule connectionCloseRule =
            new FaultInjectionRuleBuilder("connectionClose-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.BATCH_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionConnectionErrorType.CONNECTION_CLOSE)
                        .interval(Duration.ofMillis(200))
                        .build()
                )
                .duration(Duration.ofSeconds(10))
                .build();

        FaultInjectionRule serverResponseDelayRule =
            new FaultInjectionRuleBuilder("serverResponseDelay-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.BATCH_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .delay(Duration.ofSeconds(1))
                        .build()
                )
                .duration(Duration.ofSeconds(10))
                .build();

        final BulkExecutor<BulkExecutorTest> executor = new BulkExecutor<>(
            this.container,
            Flux.fromIterable(cosmosItemOperations),
            new CosmosBulkExecutionOptionsImpl());

        try {
            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(container, Arrays.asList(connectionCloseRule, serverResponseDelayRule))
                .block();

            List<CosmosBulkOperationResponse<BulkExecutorTest>>  bulkResponse =
                Flux
                    .deferContextual(context -> executor.execute())
                    .collectList()
                    .block();

            assertThat(bulkResponse.size()).isEqualTo(1);

            CosmosBulkOperationResponse<BulkExecutorTest> operationResponse = bulkResponse.get(0);
            CosmosBulkItemResponse cosmosBulkItemResponse = operationResponse.getResponse();
            assertThat(cosmosBulkItemResponse).isNull();
        } finally {
            if (executor != null && !executor.isDisposed()) {
                executor.dispose();
            }
            connectionCloseRule.disable();
            serverResponseDelayRule.disable();
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void executeBulk_complete() throws InterruptedException {
        int totalRequest = 10;
        this.container = createContainer(database);

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();
            BatchTestBase.TestDoc testDoc = this.populateTestDoc(partitionKey);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc,
                new PartitionKey(partitionKey)));

            partitionKey = UUID.randomUUID().toString();
            BatchTestBase.EventDoc eventDoc = new BatchTestBase.EventDoc(UUID.randomUUID().toString(), 2, 4, "type1",
                partitionKey);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(eventDoc,
                new PartitionKey(partitionKey)));
        }

        CosmosItemOperation[] itemOperationsArray =
            new CosmosItemOperation[cosmosItemOperations.size()];
        cosmosItemOperations.toArray(itemOperationsArray);
        CosmosBulkExecutionOptionsImpl cosmosBulkExecutionOptions = new CosmosBulkExecutionOptionsImpl();
        final BulkExecutor<BulkExecutorTest> executor = new BulkExecutor<>(
            container,
            Flux.fromArray(itemOperationsArray),
            cosmosBulkExecutionOptions);
        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponseFlux =
            Flux.deferContextual(context -> executor.execute());

        Mono<List<CosmosBulkOperationResponse<BulkExecutorTest>>> convertToListMono = bulkResponseFlux
            .collect(Collectors.toList());
        List<CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponse = convertToListMono.block();

        assertThat(bulkResponse.size()).isEqualTo(totalRequest * 2);

        for (com.azure.cosmos.models.CosmosBulkOperationResponse<BulkExecutorTest> cosmosBulkOperationResponse :
            bulkResponse) {
            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse =
                cosmosBulkOperationResponse.getResponse();

            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
        }

        int iterations = 0;
        while (true) {
            assertThat(iterations < 100);
            if (executor.isDisposed()) {
                break;
            }

            Thread.sleep(10);
            iterations++;
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void executeBulk_tooManyRequest_recordInThresholds() throws Exception {
        this.container = createContainer(database);

        String pkValue = UUID.randomUUID().toString();
        TestDoc testDoc = this.populateTestDoc(pkValue);
        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(pkValue)));

        FaultInjectionRule tooManyRequestRule =
            new FaultInjectionRuleBuilder("ttrs-" + UUID.randomUUID())
                .condition(new FaultInjectionConditionBuilder().operationType(FaultInjectionOperationType.BATCH_ITEM).build())
                .result(FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST).times(1).build())
                .duration(Duration.ofSeconds(30))
                .hitLimit(1)
                .build();

        CosmosBulkExecutionOptionsImpl cosmosBulkExecutionOptions = new CosmosBulkExecutionOptionsImpl();
        final BulkExecutor<Object> executor = new BulkExecutor<>(
            container,
            Flux.fromArray(cosmosItemOperations.toArray(new CosmosItemOperation[0])),
            cosmosBulkExecutionOptions);

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(tooManyRequestRule)).block();

            List<CosmosBulkOperationResponse<Object>> responses = executor.execute().collectList().block();

            assertThat(responses.size()).isEqualTo(1);

            // inspect partitionScopeThresholds via reflection and verify a retry was recorded
            Field mapField = BulkExecutor.class.getDeclaredField("partitionScopeThresholds");
            mapField.setAccessible(true);
            Map<?, ?> thresholdsMap = (Map<?, ?>) mapField.get(executor);

            assertThat(thresholdsMap).isNotEmpty();
            Object thresholdsObj = thresholdsMap.values().iterator().next();
            PartitionScopeThresholds thresholds = (PartitionScopeThresholds) thresholdsObj;

            PartitionScopeThresholds.CurrentIntervalThresholds current = thresholds.getCurrentThresholds();
            long retried = current.currentRetriedOperationCount.get();

            assertThat(retried).isEqualTo(1);

        } finally {
            tooManyRequestRule.disable();
            if (executor != null && !executor.isDisposed()) {
                executor.dispose();
            }
        }
    }
}
