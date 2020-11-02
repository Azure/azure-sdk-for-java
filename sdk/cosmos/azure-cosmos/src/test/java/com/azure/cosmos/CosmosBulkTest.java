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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosBulkTest  extends BatchTestBase {

    private final static Logger logger = LoggerFactory.getLogger(CosmosBulkAsyncTest.class);

    private CosmosClient bulkClient;
    private CosmosContainer bulkContainer;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public CosmosBulkTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosBulkTest() {
        assertThat(this.bulkClient).isNull();
        this.bulkClient = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.bulkClient.asyncClient());
        bulkContainer = bulkClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.bulkClient).isNotNull();
        this.bulkClient.close();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_withBulk() {
        int totalRequest = getTotalRequest();

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();
            TestDoc testDoc = this.populateTestDoc(partitionKey);
            cosmosItemOperations.add(BulkOperations.newCreateItemOperation(testDoc, new PartitionKey(partitionKey)));

            partitionKey = UUID.randomUUID().toString();
            EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);
            cosmosItemOperations.add(BulkOperations.newCreateItemOperation(eventDoc, new PartitionKey(partitionKey)));
        }

        BulkProcessingOptions<CosmosBulkAsyncTest> bulkProcessingOptions = new BulkProcessingOptions<>();
        bulkProcessingOptions.setMaxMicroBatchSize(100);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(5);

        List<CosmosBulkOperationResponse<CosmosBulkAsyncTest>> bulkResponse = bulkContainer
            .processBulkOperations(cosmosItemOperations, bulkProcessingOptions);

        assertThat(bulkResponse.size()).isEqualTo(totalRequest * 2);

        for (CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse : bulkResponse) {
            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();

            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
        }
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

        List<CosmosBulkOperationResponse<Object>> bulkResponse = bulkContainer
            .processBulkOperations(cosmosItemOperations);

        assertThat(bulkResponse.size()).isEqualTo(totalRequest);

        for (CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse : bulkResponse) {
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
        }
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

        List<CosmosItemOperation> deleteCosmosItemOperation = new ArrayList<>();

        for(CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            deleteCosmosItemOperation.add(
                BulkOperations.newDeleteItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue()));
        }

        BulkProcessingOptions<TestDoc> bulkProcessingOptions = new BulkProcessingOptions<>();
        bulkProcessingOptions.setMaxMicroBatchSize(30);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(1);

        List<CosmosBulkOperationResponse<TestDoc>> bulkResponse  = bulkContainer
            .processBulkOperations(deleteCosmosItemOperation, bulkProcessingOptions);

        assertThat(bulkResponse.size()).isEqualTo(totalRequest);

        for (CosmosBulkOperationResponse<TestDoc> cosmosBulkOperationResponse : bulkResponse) {
            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();

            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
        }
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

        List<CosmosItemOperation> readCosmosItemOperations = new ArrayList<>();

        for(CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            readCosmosItemOperations.add(
                BulkOperations.newReadItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue()));
        }

        BulkProcessingOptions<Object> bulkProcessingOptions = new BulkProcessingOptions<>(Object.class);
        bulkProcessingOptions.setMaxMicroBatchSize(30);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(5);

        List<CosmosBulkOperationResponse<Object>> bulkResponse  = bulkContainer
            .processBulkOperations(readCosmosItemOperations, bulkProcessingOptions);

        assertThat(bulkResponse.size()).isEqualTo(totalRequest);

        for (CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse : bulkResponse) {

            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

            // Using cost as list index like we assigned
            TestDoc testDoc = cosmosBulkItemResponse.getItem(TestDoc.class);
            assertThat(testDoc).isEqualTo(cosmosItemOperations.get(testDoc.getCost()).getItem());
        }
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

        List<CosmosItemOperation> replaceCosmosItemOperations = new ArrayList<>();

        for(CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            replaceCosmosItemOperations.add(BulkOperations.newReplaceItemOperation(
                testDoc.getId(),
                cosmosItemOperation.getItem(),
                cosmosItemOperation.getPartitionKeyValue()));
        }

        List<CosmosBulkOperationResponse<Object>> bulkResponse  = bulkContainer
            .processBulkOperations(replaceCosmosItemOperations);

        assertThat(bulkResponse.size()).isEqualTo(totalRequest);

        for (CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse : bulkResponse) {

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

        }
    }

    private void createItemsAndVerify(List<CosmosItemOperation> cosmosItemOperations) {
        BulkProcessingOptions<Object> bulkProcessingOptions = new BulkProcessingOptions<>(Object.class);
        bulkProcessingOptions.setMaxMicroBatchSize(100);
        bulkProcessingOptions.setMaxMicroBatchConcurrency(5);

        List<CosmosBulkOperationResponse<Object>> bulkResponse = bulkContainer
            .processBulkOperations(cosmosItemOperations, bulkProcessingOptions);

        assertThat(bulkResponse.size()).isEqualTo(cosmosItemOperations.size());

        HashSet<Integer> distinctIndex = new HashSet<>();

        for (CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse : bulkResponse) {

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
        }

        // Verify if all are distinct and count is equal to request count.
        assertThat(distinctIndex.size()).isEqualTo(cosmosItemOperations.size());
    }

    private int getTotalRequest() {
        int countRequest = new Random().nextInt(100) + 120;
        logger.info("Total count of request for this test case: " + countRequest);

        return countRequest;
    }
}
