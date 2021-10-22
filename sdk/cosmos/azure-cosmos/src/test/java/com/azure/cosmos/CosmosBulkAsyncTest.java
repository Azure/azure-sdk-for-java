// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosBulkAsyncTest extends BatchTestBase {

    private final static Logger logger = LoggerFactory.getLogger(CosmosBulkAsyncTest.class);

    private CosmosAsyncClient bulkClient;
    private CosmosAsyncContainer bulkAsyncContainer;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public CosmosBulkAsyncTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosBulkAsyncTest() {
        assertThat(this.bulkClient).isNull();
        ThrottlingRetryOptions throttlingOptions = new ThrottlingRetryOptions()
            .setMaxRetryAttemptsOnThrottledRequests(1000000)
            .setMaxRetryWaitTime(Duration.ofDays(1));
        this.bulkClient = getClientBuilder().throttlingRetryOptions(throttlingOptions).buildAsyncClient();
        bulkAsyncContainer = getSharedMultiPartitionCosmosContainer(this.bulkClient);
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeCloseAsync(this.bulkClient);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT * 2)
    public void createItem_withBulkAndThroughputControl() throws InterruptedException {
        int totalRequest = getTotalRequest(180, 200);

        PartitionKeyDefinition pkDefinition = new PartitionKeyDefinition();
        pkDefinition.setPaths(Collections.singletonList("/mypk"));
        CosmosAsyncContainer bulkAsyncContainerWithThroughputControl = createCollection(
            this.bulkClient,
            bulkAsyncContainer.getDatabase().getId(),
            new CosmosContainerProperties(UUID.randomUUID().toString(), pkDefinition));

        ThroughputControlGroupConfig groupConfig = new ThroughputControlGroupConfigBuilder()
            .setGroupName("test-group")
            .setTargetThroughputThreshold(0.2)
            .setDefault(true)
            .build();
        bulkAsyncContainerWithThroughputControl.enableLocalThroughputControlGroup(groupConfig);

        Flux<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperationFlux = Flux.merge(
            Flux.range(0, totalRequest).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                TestDoc testDoc = this.populateTestDoc(partitionKey);

                return CosmosBulkOperations.getUpsertItemOperation(testDoc, new PartitionKey(partitionKey));
            }),
            Flux.range(0, totalRequest).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);

                return CosmosBulkOperations.getUpsertItemOperation(eventDoc, new PartitionKey(partitionKey));
            }));

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        try {
            Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux = bulkAsyncContainerWithThroughputControl
                .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions);

            Thread.sleep(1000);

            AtomicInteger processedDoc = new AtomicInteger(0);
            responseFlux
                .flatMap((com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                    processedDoc.incrementAndGet();

                    com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                    if (cosmosBulkOperationResponse.getException() != null) {
                        logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                        fail(cosmosBulkOperationResponse.getException().toString());
                    }
                    assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                    assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                    assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                    assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                    assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                    assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                    return Mono.just(cosmosBulkItemResponse);
                }).blockLast();

            assertThat(processedDoc.get()).isEqualTo(totalRequest * 2);
        } finally {
            bulkAsyncContainerWithThroughputControl.delete().block();
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_withBulk() {
        int totalRequest = getTotalRequest();

        Flux<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperationFlux = Flux.merge(
            Flux.range(0, totalRequest).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                TestDoc testDoc = this.populateTestDoc(partitionKey);

                return CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey));
            }),
            Flux.range(0, totalRequest).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);

                return CosmosBulkOperations.getCreateItemOperation(eventDoc, new PartitionKey(partitionKey));
            }));

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux = bulkAsyncContainer
            .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions);

        AtomicInteger processedDoc = new AtomicInteger(0);
        responseFlux
            .flatMap((com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }

                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest * 2);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_withBulk_and_operationLevelContext() {
        int totalRequest = getTotalRequest();

        Flux<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperationFlux = Flux.merge(
            Flux.range(0, totalRequest).map(i -> {

                String randomId = UUID.randomUUID().toString();
                String partitionKey = randomId;
                TestDoc testDoc = this.populateTestDoc(partitionKey);
                ItemOperationContext ctx = new ItemOperationContext(randomId);

                return CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey), ctx);
            }),
            Flux.range(0, totalRequest).map(i -> {
                String randomId = UUID.randomUUID().toString();
                String partitionKey = randomId;
                EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);

                ItemOperationContext ctx = new ItemOperationContext(randomId);
                return CosmosBulkOperations.getCreateItemOperation(eventDoc, new PartitionKey(partitionKey), ctx);
            }));

        CosmosBulkExecutionOptions bulkExecutionOptions = new CosmosBulkExecutionOptions();
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setTargetedMicroBatchRetryRate(
                bulkExecutionOptions,
                0.25,
                0.5);

        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux = bulkAsyncContainer
            .executeBulkOperations(cosmosItemOperationFlux, bulkExecutionOptions);

        AtomicInteger processedDoc = new AtomicInteger(0);
        responseFlux
            .flatMap((com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }

                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
                ItemOperationContext ctx = cosmosBulkOperationResponse.getOperation().getContext();
                assertThat(cosmosBulkOperationResponse.getOperation().getPartitionKeyValue().toString())
                    .isEqualTo(new PartitionKey(ctx.getCorrelationId()).toString());

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest * 2);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItemMultipleTimesWithOperationOnFly_withBulk() {
        int totalRequest = getTotalRequest();

        Flux<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperationFlux = Flux.merge(
            Flux.range(0, totalRequest).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                TestDoc testDoc = this.populateTestDoc(partitionKey);

                return CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey));
            }),
            Flux.range(0, totalRequest).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);

                return CosmosBulkOperations.getCreateItemOperation(eventDoc, new PartitionKey(partitionKey));
            }));

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux = bulkAsyncContainer
            .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions);

        HashSet<Object> distinctDocs = new HashSet<>();
        AtomicInteger processedDoc = new AtomicInteger(0);

        // Subscribe first time
        responseFlux
            .flatMap((com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                Object testDoc = cosmosBulkItemResponse.getItem(Object.class);
                distinctDocs.add(testDoc);

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest * 2);
        assertThat(distinctDocs.size()).isEqualTo(totalRequest * 2);

        // Subscribe second time should again return 201 as it will get new documents
        responseFlux
            .flatMap((com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                Object testDoc = cosmosBulkItemResponse.getItem(Object.class);
                distinctDocs.add(testDoc);

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest * 4);
        assertThat(distinctDocs.size()).isEqualTo(totalRequest * 4);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void runCreateItemMultipleTimesWithFixedOperations_withBulk() {
        int totalRequest = getTotalRequest();

        List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        HashSet<Object> distinctDocs = new HashSet<>();
        AtomicInteger processedDoc = new AtomicInteger(0);

        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> createResponseFlux = bulkAsyncContainer
            .executeBulkOperations(Flux.fromIterable(cosmosItemOperations), cosmosBulkExecutionOptions);

        // Subscribing first time should return 201(CREATED) status code for all
        createResponseFlux
            .flatMap(cosmosBulkOperationResponse -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                // Using cost as list index like we assigned
                TestDoc testDoc = cosmosBulkItemResponse.getItem(TestDoc.class);
                distinctDocs.add(testDoc);
                assertThat(testDoc).isEqualTo(cosmosItemOperations.get(testDoc.getCost()).getItem());

                return Mono.just(cosmosBulkOperationResponse);
            })
            .blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
        assertThat(distinctDocs.size()).isEqualTo(totalRequest);

        // Subscribing second time should return 409(conflict) for all
        createResponseFlux
            .flatMap(cosmosBulkOperationResponse -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CONFLICT.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                return Mono.just(cosmosBulkOperationResponse);
            })
            .blockLast();

        assertThat(processedDoc.get()).isEqualTo(2 * totalRequest);
        assertThat(distinctDocs.size()).isEqualTo(totalRequest);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItemWithError_withBulk() {
        int totalRequest = getTotalRequest();

        Flux<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperationFlux = Flux.range(0, totalRequest).flatMap(i -> {

            String partitionKey = UUID.randomUUID().toString();
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            if (i == 20 || i == 40 || i == 60) {
                // Three errors
                return Mono.error(new Exception("ex"));
            }

            return Mono.just(CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        });

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux = bulkAsyncContainer
            .executeBulkOperations(cosmosItemOperationFlux, cosmosBulkExecutionOptions);

        AtomicInteger processedDoc = new AtomicInteger(0);
        AtomicInteger erroredDoc = new AtomicInteger(0);
        responseFlux
            .flatMap((com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();

                if(cosmosBulkItemResponse == null) {

                    erroredDoc.incrementAndGet();
                    return Mono.empty();

                } else {
                    processedDoc.incrementAndGet();

                    assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                    assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                    assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                    assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                    assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                    assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                    return Mono.just(cosmosBulkItemResponse);
                }

            }).blockLast();

        // Right now we are eating up the error signals in input.
        assertThat(erroredDoc.get()).isEqualTo(0);
        assertThat(processedDoc.get()).isEqualTo(totalRequest - 3);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void upsertItem_withbulk() {
        int totalRequest = getTotalRequest();

        List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(CosmosBulkOperations.getUpsertItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> responseFlux = bulkAsyncContainer
            .executeBulkOperations(Flux.fromIterable(cosmosItemOperations), cosmosBulkExecutionOptions);

        AtomicInteger processedDoc = new AtomicInteger(0);
        responseFlux
            .flatMap(cosmosBulkOperationResponse -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                // Using cost as list index like we assigned
                TestDoc testDoc = cosmosBulkItemResponse.getItem(TestDoc.class);
                assertThat(cosmosBulkOperationResponse.getOperation()).isEqualTo(cosmosItemOperations.get(testDoc.getCost()));
                assertThat(testDoc).isEqualTo(cosmosBulkOperationResponse.getOperation().getItem());
                assertThat(testDoc).isEqualTo(cosmosItemOperations.get(testDoc.getCost()).getItem());

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteItem_withBulk() {
        int totalRequest = Math.min(getTotalRequest(), 20);

        List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        }
        createItemsAndVerify(cosmosItemOperations);

        Flux<com.azure.cosmos.models.CosmosItemOperation> deleteCosmosItemOperationFlux =
            Flux.fromIterable(cosmosItemOperations).map((com.azure.cosmos.models.CosmosItemOperation cosmosItemOperation) -> {
                TestDoc testDoc = cosmosItemOperation.getItem();
                return CosmosBulkOperations.getDeleteItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue());
            });

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        AtomicInteger processedDoc = new AtomicInteger(0);
        bulkAsyncContainer
            .executeBulkOperations(deleteCosmosItemOperationFlux, cosmosBulkExecutionOptions)
            .flatMap(cosmosBulkOperationResponse -> {
                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readItem_withBulk() {
        int totalRequest = getTotalRequest();

        List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(CosmosBulkOperations.getUpsertItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        createItemsAndVerify(cosmosItemOperations);

        Flux<com.azure.cosmos.models.CosmosItemOperation> readCosmosItemOperationFlux =
            Flux.fromIterable(cosmosItemOperations).map((com.azure.cosmos.models.CosmosItemOperation cosmosItemOperation) -> {
                TestDoc testDoc = cosmosItemOperation.getItem();
                return CosmosBulkOperations.getReadItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue());
            });

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        AtomicInteger processedDoc = new AtomicInteger(0);
        bulkAsyncContainer
            .executeBulkOperations(readCosmosItemOperationFlux, cosmosBulkExecutionOptions)
            .flatMap(cosmosBulkOperationResponse -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                // Using cost as list index like we assigned
                TestDoc testDoc = cosmosBulkItemResponse.getItem(TestDoc.class);
                assertThat(testDoc).isEqualTo(cosmosItemOperations.get(testDoc.getCost()).getItem());

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readItemMultipleTimes_withBulk() {
        int totalRequest = getTotalRequest();

        List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(CosmosBulkOperations.getUpsertItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        createItemsAndVerify(cosmosItemOperations);

        Flux<com.azure.cosmos.models.CosmosItemOperation> readCosmosItemOperationFlux =
            Flux.fromIterable(cosmosItemOperations).map((com.azure.cosmos.models.CosmosItemOperation cosmosItemOperation) -> {
                TestDoc testDoc = cosmosItemOperation.getItem();
                return CosmosBulkOperations.getReadItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue());
            });

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        HashSet<TestDoc> distinctDocs = new HashSet<>();
        AtomicInteger processedDoc = new AtomicInteger(0);

        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> readResponseFlux = bulkAsyncContainer
            .executeBulkOperations(readCosmosItemOperationFlux, cosmosBulkExecutionOptions)
            .flatMap(cosmosBulkOperationResponse -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                // Using cost as list index like we assigned
                TestDoc testDoc = cosmosBulkItemResponse.getItem(TestDoc.class);
                distinctDocs.add(testDoc);
                assertThat(testDoc).isEqualTo(cosmosItemOperations.get(testDoc.getCost()).getItem());

                return Mono.just(cosmosBulkOperationResponse);
            });

        // Subscribe first time
        readResponseFlux
            .blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
        assertThat(distinctDocs.size()).isEqualTo(totalRequest);

        // Subscribe second time
        readResponseFlux
            .blockLast();

        assertThat(processedDoc.get()).isEqualTo(2 * totalRequest);
        assertThat(distinctDocs.size()).isEqualTo(totalRequest);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceItem_withBulk() {
        int totalRequest = getTotalRequest();

        List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        createItemsAndVerify(cosmosItemOperations);

        Flux<com.azure.cosmos.models.CosmosItemOperation> replaceCosmosItemOperationFlux =
            Flux.fromIterable(cosmosItemOperations).map((com.azure.cosmos.models.CosmosItemOperation cosmosItemOperation) -> {
                TestDoc testDoc = cosmosItemOperation.getItem();
                return CosmosBulkOperations.getReplaceItemOperation(
                    testDoc.getId(),
                    cosmosItemOperation.getItem(),
                    cosmosItemOperation.getPartitionKeyValue());
            });

        AtomicInteger processedDoc = new AtomicInteger(0);
        bulkAsyncContainer
            .executeBulkOperations(replaceCosmosItemOperationFlux)
            .flatMap((com.azure.cosmos.models.CosmosBulkOperationResponse<?> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                // Using cost as list index like we assigned
                TestDoc testDoc = cosmosBulkItemResponse.getItem(TestDoc.class);
                assertThat(testDoc).isEqualTo(cosmosBulkOperationResponse.getOperation().getItem());
                assertThat(testDoc).isEqualTo(cosmosItemOperations.get(testDoc.getCost()).getItem());

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
    }

    private void createItemsAndVerify(List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations) {
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Flux<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> createResonseFlux = bulkAsyncContainer
            .executeBulkOperations(Flux.fromIterable(cosmosItemOperations), cosmosBulkExecutionOptions);

        HashSet<Integer> distinctIndex = new HashSet<>();
        AtomicInteger processedDoc = new AtomicInteger(0);
        createResonseFlux
            .flatMap(cosmosBulkOperationResponse -> {

                processedDoc.incrementAndGet();
                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
                if (cosmosBulkOperationResponse.getException() != null) {
                    logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                    fail(cosmosBulkOperationResponse.getException().toString());
                }
                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

                // Using cost as list index like we assigned
                TestDoc testDoc = cosmosBulkItemResponse.getItem(TestDoc.class);
                distinctIndex.add(testDoc.getCost());

                assertThat(cosmosBulkOperationResponse.getOperation()).isEqualTo(cosmosItemOperations.get(testDoc.getCost()));
                assertThat(testDoc).isEqualTo(cosmosBulkOperationResponse.getOperation().getItem());
                assertThat(testDoc).isEqualTo(cosmosItemOperations.get(testDoc.getCost()).getItem());

                return Mono.just(cosmosBulkItemResponse);
            }).blockLast();

        // Verify if all are distinct and count is equal to request count.
        assertThat(processedDoc.get()).isEqualTo(cosmosItemOperations.size());
        assertThat(distinctIndex.size()).isEqualTo(cosmosItemOperations.size());
    }

    private int getTotalRequest(int min, int max) {
        int countRequest = new Random().nextInt(max - min) + min;
        logger.info("Total count of request for this test case: " + countRequest);

        return countRequest;
    }

    private int getTotalRequest() {
        return getTotalRequest(200, 300);
    }

    private static class ItemOperationContext {
        private final String correlationId;

        public ItemOperationContext(String correlationId) {
            this.correlationId = correlationId;
        }

        public String getCorrelationId() {
            return this.correlationId;
        }
    }
}
