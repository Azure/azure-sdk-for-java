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
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionErrorType;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResultBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.IFaultInjectionResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkExecutorTest extends BatchTestBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;
    private CosmosAsyncDatabase database;
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
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
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
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
    public void executeBulk_OnGoneFailure() throws InterruptedException {
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
            new CosmosBulkExecutionOptions());

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(container, Arrays.asList(connectionCloseRule, serverResponseDelayRule))
            .block();

        List<CosmosBulkOperationResponse<BulkExecutorTest>>  bulkResponse =
                Flux
                    .deferContextual(context -> executor.execute())
                    .collectList()
                    .block();

        try {
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

    @DataProvider()
    public Object[][] faultInjectionProvider() {
        return new Object[][]{
            {null},
            {injectBatchFailure("RequestRateTooLarge", FaultInjectionServerErrorType.TOO_MANY_REQUEST, 10)},
            {injectBatchFailure("PartitionSplit", FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, 2)}
        };
    }

    // tests preserving order in the regular retry flow and when a partition split happens
    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 25, dataProvider = "faultInjectionProvider")
    public void executeBulk_preserveOrdering_OnFaults(FaultInjectionRule rule) throws InterruptedException {
        int totalRequest = 100;
        this.container = createContainer(database);

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        String duplicatePK = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        for (int i = 0; i < totalRequest; i++) {
            if (i == 0) {
                BatchTestBase.EventDoc eventDoc = new BatchTestBase.EventDoc(id, 2, 4, "type1",
                    duplicatePK);
                cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(eventDoc,
                    new PartitionKey(duplicatePK)));
            } else {
                cosmosItemOperations.add(CosmosBulkOperations.getPatchItemOperation(id,
                    new PartitionKey(duplicatePK),
                    CosmosPatchOperations.create().replace("/type", "updated" + i)));
            }
        }

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setOrderingPreserved(cosmosBulkExecutionOptions, true);


        Flux<CosmosItemOperation> inputFlux = Flux
            .fromIterable(cosmosItemOperations)
            .delayElements(Duration.ofMillis(100));
        final BulkExecutorWithOrderingPreserved<BulkExecutorTest> executor = new BulkExecutorWithOrderingPreserved<>(
            this.container,
            inputFlux,
            cosmosBulkExecutionOptions);

        if (rule != null) {
            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(this.container,
                    Arrays.asList(rule))
                .block();
        }


        List<CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponse =
            Flux.deferContextual(context -> executor.execute()).collect(Collectors.toList()).block();

        try {

            assertThat(bulkResponse.size()).isEqualTo(totalRequest);

            for (int i = 0; i < cosmosItemOperations.size(); i++) {
                CosmosBulkOperationResponse<BulkExecutorTest> operationResponse = bulkResponse.get(i);
                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse =
                    operationResponse.getResponse();

                assertThat(operationResponse.getOperation()).isEqualTo(cosmosItemOperations.get(i));
                if (i == 0) {
                    assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                } else {
                    assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
                }
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();

            }
        } finally {
            if (executor != null && !executor.isDisposed()) {
                executor.dispose();
            }
            if (rule != null) {
                rule.disable();
            }
        }
    }

    // Tests No Retry Exception flow
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void executeBulk_preserveOrdering_OnServiceUnAvailable() throws InterruptedException {
        int totalRequest = 100;
        this.container = createContainer(database);

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        String duplicatePK = UUID.randomUUID().toString();
        String duplicateId = UUID.randomUUID().toString();
        PartitionKey duplicatePartitionKey = new PartitionKey(duplicatePK);
        for (int i = 0; i < totalRequest; i++) {
            if (i == 0) {
                BatchTestBase.EventDoc eventDoc = new BatchTestBase.EventDoc(duplicateId, 2, 4, "type1",
                    duplicatePK);
                cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(eventDoc,
                    duplicatePartitionKey));
            } else {
                cosmosItemOperations.add(CosmosBulkOperations.getPatchItemOperation(duplicateId,
                    new PartitionKey(duplicatePK),
                    CosmosPatchOperations.create().replace("/type", "updated" + i)));
            }
        }

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setOrderingPreserved(cosmosBulkExecutionOptions, true);

        FaultInjectionRule rule = injectBatchFailure("ServiceUnavailable", FaultInjectionServerErrorType.SERVICE_UNAVAILABLE, 1);

        Flux<CosmosItemOperation> inputFlux = Flux
            .fromIterable(cosmosItemOperations)
            .delayElements(Duration.ofMillis(100));
        final BulkExecutorWithOrderingPreserved<BulkExecutorTest> executor = new BulkExecutorWithOrderingPreserved<>(
            this.container,
            inputFlux,
            cosmosBulkExecutionOptions);

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(this.container,
                Arrays.asList(rule))
            .block();
        List<CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponse =
            Flux.deferContextual(context -> executor.execute()).collect(Collectors.toList()).block();

        try {
            assertThat(bulkResponse.size()).isEqualTo(totalRequest);


            for (int i = 0; i < cosmosItemOperations.size(); i++) {
                CosmosBulkOperationResponse<BulkExecutorTest> operationResponse = bulkResponse.get(i);
                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse =
                    operationResponse.getResponse();
                assertThat(cosmosBulkItemResponse).isNull();

            }

        } finally {
            if (executor != null && !executor.isDisposed()) {
                executor.dispose();
            }
            rule.disable();
        }
    }

    private FaultInjectionRule injectBatchFailure(String id, FaultInjectionServerErrorType serverErrorType, int hitLimit) {


        FaultInjectionServerErrorResultBuilder faultInjectionResultBuilder = FaultInjectionResultBuilders
             .getResultBuilder(serverErrorType)
             .delay(Duration.ofMillis(1500));


        IFaultInjectionResult result = faultInjectionResultBuilder.build();

        FaultInjectionConnectionType connectionType = FaultInjectionConnectionType.GATEWAY;
        if (ImplementationBridgeHelpers
            .CosmosAsyncClientHelper
            .getCosmosAsyncClientAccessor()
            .getConnectionMode(this.client)
            .equals(ConnectionMode.DIRECT.toString())) {
            connectionType = FaultInjectionConnectionType.DIRECT;
        }

        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(FaultInjectionOperationType.BATCH_ITEM)
            .connectionType(connectionType)
            .build();

        return new FaultInjectionRuleBuilder(id)
            .condition(condition)
            .result(result)
            .startDelay(Duration.ofSeconds(1))
            .hitLimit(hitLimit)
            .build();
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
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
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
}
