// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.batch;


import com.azure.cosmos.BatchTestBase;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResultBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.test.faultinjection.IFaultInjectionResult;
import com.azure.cosmos.test.implementation.faultinjection.FaultInjectorProvider;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void executeBulk_preserveOrdering() throws InterruptedException {
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
                    new PartitionKey(duplicatePK), CosmosPatchOperations.create().replace("/type", "updated" + i)));
            }
        }

        CosmosItemOperation[] itemOperationsArray =
            new CosmosItemOperation[cosmosItemOperations.size()];
        cosmosItemOperations.toArray(itemOperationsArray);
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setPreserveOrdering(cosmosBulkExecutionOptions, true);

        Flux<CosmosItemOperation> inputFlux = Flux
            .fromArray(itemOperationsArray)
            .delayElements(Duration.ofMillis(100));
        final BulkExecutor<BulkExecutorTest> executor = new BulkExecutor<>(
            container,
            inputFlux,
            cosmosBulkExecutionOptions);
        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponseFlux =
            Flux.deferContextual(context -> executor.execute());

        Mono<List<CosmosBulkOperationResponse<BulkExecutorTest>>> convertToListMono = bulkResponseFlux
            .collect(Collectors.toList());
        List<CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponse = convertToListMono.block();

        assertThat(bulkResponse.size()).isEqualTo(totalRequest);

        for (int i = 0; i < cosmosItemOperations.size(); i++) {
            CosmosBulkOperationResponse<BulkExecutorTest> operationResponse = bulkResponse.get(i);
            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse =
                operationResponse.getResponse();
            if (i == 0) {
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            } else {
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            }
            assertThat(operationResponse.getOperation()).isEqualTo(cosmosItemOperations.get(i));
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

    // tests the partition split retry flow and the regular retry flow
    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 2)
    public void executeBulk_preserveOrdering_OnFailures() throws InterruptedException {
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
                    new PartitionKey(duplicatePK), CosmosPatchOperations.create().replace("/type", "updated" + i)));
            }
        }

        CosmosItemOperation[] itemOperationsArray =
            new CosmosItemOperation[cosmosItemOperations.size()];
        cosmosItemOperations.toArray(itemOperationsArray);
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setPreserveOrdering(cosmosBulkExecutionOptions, true);

//        FaultInjectionRule rule = injectFailure("RequestRateTooLarge", this.container,
//            FaultInjectionOperationType.BATCH_ITEM, FaultInjectionServerErrorType.TOO_MANY_REQUEST, 10);
        FaultInjectionRule rule = injectFailure("PartitionSplit", this.container, FaultInjectionOperationType.BATCH_ITEM, FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, 5);

        Flux<CosmosItemOperation> inputFlux = Flux
            .fromArray(itemOperationsArray)
            .delayElements(Duration.ofMillis(100));
        final BulkExecutor<BulkExecutorTest> executor = new BulkExecutor<>(
            this.container,
            inputFlux,
            cosmosBulkExecutionOptions);
        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponseFlux =
            Flux.deferContextual(context -> executor.execute()).flatMap(response -> {
                System.out.println(response.getResponse().getCosmosDiagnostics());
                return Mono.just(response);
            });


        Mono<List<CosmosBulkOperationResponse<BulkExecutorTest>>> convertToListMono = bulkResponseFlux
            .collect(Collectors.toList());
        List<CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponse = convertToListMono.block();

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
        rule.disable();
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
                    new PartitionKey(duplicatePK), CosmosPatchOperations.create().replace("/type", "updated" + i)));
            }
        }

        CosmosItemOperation[] itemOperationsArray =
            new CosmosItemOperation[cosmosItemOperations.size()];
        cosmosItemOperations.toArray(itemOperationsArray);
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setPreserveOrdering(cosmosBulkExecutionOptions, true);

        FaultInjectionRule rule = injectFailure("ServiceUnavailable", this.container,
            FaultInjectionOperationType.BATCH_ITEM, FaultInjectionServerErrorType.PARTITION_IS_SPLITTING, 10);

        Flux<CosmosItemOperation> inputFlux = Flux
            .fromArray(itemOperationsArray)
            .delayElements(Duration.ofMillis(100));
        final BulkExecutor<BulkExecutorTest> executor = new BulkExecutor<>(
            this.container,
            inputFlux,
            cosmosBulkExecutionOptions);
        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponseFlux =
            Flux.deferContextual(context -> executor.execute());


        Mono<List<CosmosBulkOperationResponse<BulkExecutorTest>>> convertToListMono = bulkResponseFlux
            .collect(Collectors.toList());
        List<CosmosBulkOperationResponse<BulkExecutorTest>> bulkResponse = convertToListMono.block();

        assertThat(bulkResponse.size()).isEqualTo(totalRequest);



        for (int i = 0; i < cosmosItemOperations.size(); i++) {
            CosmosBulkOperationResponse<BulkExecutorTest> operationResponse = bulkResponse.get(i);
            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse =
                operationResponse.getResponse();

            assertThat(cosmosBulkItemResponse).isNull();

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
        rule.disable();
    }

    private FaultInjectionRule injectFailure(String id,
        CosmosAsyncContainer createdContainer,
        FaultInjectionOperationType operationType, FaultInjectionServerErrorType serverErrorType, int hitlimit) {


        FaultInjectionServerErrorResultBuilder faultInjectionResultBuilder = FaultInjectionResultBuilders
             .getResultBuilder(serverErrorType)
             .delay(Duration.ofMillis(1500));


        IFaultInjectionResult result = faultInjectionResultBuilder.build();

        FaultInjectionCondition condition = new FaultInjectionConditionBuilder()
            .operationType(operationType)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .build();

        FaultInjectionRule rule = new FaultInjectionRuleBuilder(id)
            .condition(condition)
            .result(result)
            .startDelay(Duration.ofSeconds(1))
            .hitLimit(hitlimit)
            .build();


        FaultInjectorProvider injectorProvider = (FaultInjectorProvider)  ImplementationBridgeHelpers.CosmosAsyncContainerHelper
            .getCosmosAsyncContainerAccessor()
            .getOrConfigureFaultInjectorProvider(createdContainer, () -> new FaultInjectorProvider(createdContainer));


        injectorProvider.configureFaultInjectionRules(Arrays.asList(rule)).block();

        return rule;
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
