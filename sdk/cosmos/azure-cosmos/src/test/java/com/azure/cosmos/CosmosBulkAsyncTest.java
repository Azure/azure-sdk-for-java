// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

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
        this.bulkClient = getClientBuilder().buildAsyncClient();
        bulkAsyncContainer = getSharedMultiPartitionCosmosContainer(this.bulkClient);
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeCloseAsync(this.bulkClient);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_withBulk() {
        int totalRequest = getTotalRequest();

        Flux<CosmosItemOperation> cosmosItemOperationFlux = Flux.merge(
            Flux.range(0, totalRequest).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                TestDoc testDoc = this.populateTestDoc(partitionKey);

                return BulkOperations.newCreateItemOperation(testDoc, new PartitionKey(partitionKey));
            }),
            Flux.range(0, totalRequest).map(i -> {
                String partitionKey = UUID.randomUUID().toString();
                EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);

                return BulkOperations.newCreateItemOperation(eventDoc, new PartitionKey(partitionKey));
            }));

        BulkProcessingOptions<CosmosBulkAsyncTest> bulkProcessingOptions = new BulkProcessingOptions<>();
        bulkProcessingOptions.setMaxMicroBatchSize(100);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(5);

        Flux<CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux = bulkAsyncContainer
            .processBulkOperations(cosmosItemOperationFlux, bulkProcessingOptions);

        AtomicInteger processedDoc = new AtomicInteger(0);
        responseFlux
            .flatMap((CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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

    @Test(groups = {"simple"}, timeOut = TIMEOUT * 100)
    public void createItemWithError_withBulk() {
        int totalRequest = 2;

        logger.info("Creating total request {}", totalRequest);

        Flux<CosmosItemOperation> cosmosItemOperationFlux = Flux.range(0, totalRequest).flatMap(i -> {

            String partitionKey = UUID.randomUUID().toString();
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            if(i == 1) {
                return Mono.error(new Exception("ex"));
            }

            return Mono.just(BulkOperations.newCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        });

        BulkProcessingOptions<CosmosBulkAsyncTest> bulkProcessingOptions = new BulkProcessingOptions<>();
        bulkProcessingOptions.setMaxMicroBatchSize(100);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(15);

        Flux<CosmosBulkOperationResponse<CosmosBulkAsyncTest>> responseFlux = bulkAsyncContainer
            .processBulkOperations(cosmosItemOperationFlux, bulkProcessingOptions);

        AtomicInteger processedDoc = new AtomicInteger(0);
        AtomicInteger erroredDoc = new AtomicInteger(0);
        responseFlux
            .flatMap((CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse) -> {

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();

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
        assertThat(processedDoc.get()).isEqualTo(1);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void upsertItem_withbulk() {
        int totalRequest = getTotalRequest();

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(BulkOperations.newUpsertItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        BulkProcessingOptions<Object> bulkProcessingOptions = new BulkProcessingOptions<>();
        bulkProcessingOptions.setMaxMicroBatchSize(100);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(1);

        Flux<CosmosBulkOperationResponse<Object>> responseFlux = bulkAsyncContainer
            .processBulkOperations(Flux.fromIterable(cosmosItemOperations));

        AtomicInteger processedDoc = new AtomicInteger(0);
        responseFlux
            .flatMap((CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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
        int totalRequest = getTotalRequest();

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(BulkOperations.newCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        }
        createItemsAndVerify(cosmosItemOperations);

        Flux<CosmosItemOperation> deleteCosmosItemOperationFlux =
            Flux.fromIterable(cosmosItemOperations).map((CosmosItemOperation cosmosItemOperation) -> {
                TestDoc testDoc = cosmosItemOperation.getItem();
                return BulkOperations.newDeleteItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue());
            });

        BulkProcessingOptions<TestDoc> bulkProcessingOptions = new BulkProcessingOptions<>();
        bulkProcessingOptions.setMaxMicroBatchSize(30);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(1);

        AtomicInteger processedDoc = new AtomicInteger(0);
        bulkAsyncContainer
            .processBulkOperations(deleteCosmosItemOperationFlux, bulkProcessingOptions)
            .flatMap((CosmosBulkOperationResponse<TestDoc> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(BulkOperations.newUpsertItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        createItemsAndVerify(cosmosItemOperations);

        Flux<CosmosItemOperation> readCosmosItemOperationFlux =
            Flux.fromIterable(cosmosItemOperations).map((CosmosItemOperation cosmosItemOperation) -> {
                TestDoc testDoc = cosmosItemOperation.getItem();
                return BulkOperations.newReadItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue());
            });

        BulkProcessingOptions<Object> bulkProcessingOptions = new BulkProcessingOptions<>(Object.class);
        bulkProcessingOptions.setMaxMicroBatchSize(30);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(5);

        AtomicInteger processedDoc = new AtomicInteger(0);
        bulkAsyncContainer
            .processBulkOperations(readCosmosItemOperationFlux, bulkProcessingOptions)
            .flatMap((CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);

            cosmosItemOperations.add(BulkOperations.newUpsertItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        createItemsAndVerify(cosmosItemOperations);

        Flux<CosmosItemOperation> readCosmosItemOperationFlux =
            Flux.fromIterable(cosmosItemOperations).map((CosmosItemOperation cosmosItemOperation) -> {
                TestDoc testDoc = cosmosItemOperation.getItem();
                return BulkOperations.newReadItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue());
            });

        BulkProcessingOptions<Object> bulkProcessingOptions = new BulkProcessingOptions<>(Object.class);
        bulkProcessingOptions.setMaxMicroBatchSize(30);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(5);

        HashSet<TestDoc> distinctDocs = new HashSet<>();
        AtomicInteger processedDoc = new AtomicInteger(0);

        Flux<CosmosBulkOperationResponse<Object>> readResponseFlux = bulkAsyncContainer
            .processBulkOperations(readCosmosItemOperationFlux, bulkProcessingOptions)
            .flatMap((CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);
            cosmosItemOperations.add(BulkOperations.newCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        createItemsAndVerify(cosmosItemOperations);

        Flux<CosmosItemOperation> replaceCosmosItemOperationFlux =
            Flux.fromIterable(cosmosItemOperations).map((CosmosItemOperation cosmosItemOperation) -> {
                TestDoc testDoc = cosmosItemOperation.getItem();
                return BulkOperations.newReplaceItemOperation(
                    testDoc.getId(),
                    cosmosItemOperation.getItem(),
                    cosmosItemOperation.getPartitionKeyValue());
            });

        AtomicInteger processedDoc = new AtomicInteger(0);
        bulkAsyncContainer
            .processBulkOperations(replaceCosmosItemOperationFlux)
            .flatMap((CosmosBulkOperationResponse<?> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();

                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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

    private void createItemsAndVerify(List<CosmosItemOperation> cosmosItemOperations) {
        BulkProcessingOptions<Object> bulkProcessingOptions = new BulkProcessingOptions<>(Object.class);
        bulkProcessingOptions.setMaxMicroBatchSize(100);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(5);

        Flux<CosmosBulkOperationResponse<Object>> createResonseFlux = bulkAsyncContainer
            .processBulkOperations(Flux.fromIterable(cosmosItemOperations), bulkProcessingOptions);

        HashSet<Integer> distinctIndex = new HashSet<>();
        AtomicInteger processedDoc = new AtomicInteger(0);
        createResonseFlux
            .flatMap((CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse) -> {

                processedDoc.incrementAndGet();
                CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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

    private int getTotalRequest() {
        int countRequest = new Random().nextInt(100) + 120;
        logger.info("Total count of request for this test case: " + countRequest);

        return countRequest;
    }
}
