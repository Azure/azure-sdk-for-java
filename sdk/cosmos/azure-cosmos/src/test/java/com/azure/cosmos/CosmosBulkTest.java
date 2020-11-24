// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.models.CosmosItemResponse;
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
            cosmosItemOperations.add(BulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));

            partitionKey = UUID.randomUUID().toString();
            EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);
            cosmosItemOperations.add(BulkOperations.getCreateItemOperation(eventDoc, new PartitionKey(partitionKey)));
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
            cosmosItemOperations.add(BulkOperations.getUpsertItemOperation(testDoc, new PartitionKey(partitionKey)));
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
            cosmosItemOperations.add(BulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        }
        createItemsAndVerify(cosmosItemOperations);

        List<CosmosItemOperation> deleteCosmosItemOperation = new ArrayList<>();

        for(CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            deleteCosmosItemOperation.add(
                BulkOperations.getDeleteItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue()));
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
            cosmosItemOperations.add(BulkOperations.getUpsertItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        createItemsAndVerify(cosmosItemOperations);

        List<CosmosItemOperation> readCosmosItemOperations = new ArrayList<>();

        for(CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            readCosmosItemOperations.add(
                BulkOperations.getReadItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue()));
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
            cosmosItemOperations.add(BulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        }

        createItemsAndVerify(cosmosItemOperations);

        List<CosmosItemOperation> replaceCosmosItemOperations = new ArrayList<>();

        for(CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            replaceCosmosItemOperations.add(BulkOperations.getReplaceItemOperation(
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

    // Error tests
    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkETagTest() {
        this.createJsonTestDocs(bulkContainer);

        {
            BatchTestBase.TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);

            BatchTestBase.TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);

            CosmosItemResponse<TestDoc> response = bulkContainer.readItem(
                this.TestDocPk1ExistingA.getId(),
                this.getPartitionKey(this.partitionKey1),
                TestDoc.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

            BulkItemRequestOptions firstReplaceOptions = new BulkItemRequestOptions();
            firstReplaceOptions.setIfMatchETag(response.getETag());

            List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
            cosmosItemOperations.add(BulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));
            cosmosItemOperations.add(BulkOperations.getReplaceItemOperation(
                testDocToReplace.getId(), testDocToReplace, new PartitionKey(this.partitionKey1), firstReplaceOptions));

            List<CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer
                .processBulkOperations(cosmosItemOperations);

            assertThat(bulkResponses.size()).isEqualTo(cosmosItemOperations.size());

            assertThat(bulkResponses.get(0).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(bulkResponses.get(1).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

            // Ensure that the replace overwrote the doc from the first operation
            this.verifyByRead(bulkContainer, testDocToCreate, bulkResponses.get(0).getResponse().getETag());
            this.verifyByRead(bulkContainer, testDocToReplace, bulkResponses.get(1).getResponse().getETag());
        }

        {
            TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingB);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);

            BulkItemRequestOptions replaceOptions = new BulkItemRequestOptions();
            replaceOptions.setIfMatchETag(String.valueOf(this.getRandom().nextInt()));

            List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
            cosmosItemOperations.add(BulkOperations.getReplaceItemOperation(
                testDocToReplace.getId(), testDocToReplace, new PartitionKey(this.partitionKey1), replaceOptions));

            List<CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer
                .processBulkOperations(cosmosItemOperations);

            assertThat(bulkResponses.size()).isEqualTo(cosmosItemOperations.size());

            assertThat(bulkResponses.get(0).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.PRECONDITION_FAILED.code());

            // ensure the item was not updated
            this.verifyByRead(bulkContainer, this.TestDocPk1ExistingB);
        }

        {
            // Not modified case in etag
            CosmosItemResponse<TestDoc> response = bulkContainer.readItem(
                this.TestDocPk1ExistingA.getId(),
                this.getPartitionKey(this.partitionKey1),
                TestDoc.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

            BulkItemRequestOptions readOptions = new BulkItemRequestOptions();
            readOptions.setIfMatchETag(response.getETag());

            BatchTestBase.TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);

            List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
            cosmosItemOperations.add(BulkOperations.getReadItemOperation(
                this.TestDocPk1ExistingA.getId(),
                this.getPartitionKey(this.partitionKey1),
                readOptions));
            cosmosItemOperations.add(BulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));

            List<CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer
                .processBulkOperations(cosmosItemOperations);

            assertThat(bulkResponses.size()).isEqualTo(cosmosItemOperations.size());

            assertThat(bulkResponses.get(0).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.NOT_MODIFIED.code());
            assertThat(bulkResponses.get(1).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(bulkResponses.get(1).getResponse().getItem(TestDoc.class)).isEqualTo(testDocToCreate);

            this.verifyByRead(bulkContainer, testDocToCreate);
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkWithInvalidCreateTest() {
        // partition key mismatch between doc and and value passed in to the operation
        CosmosItemOperation operation =
            BulkOperations.getCreateItemOperation(
                this.populateTestDoc(UUID.randomUUID().toString()), new PartitionKey(this.partitionKey1));

        this.runWithError(
            bulkContainer,
            operations -> operations.add(operation),
            HttpResponseStatus.BAD_REQUEST);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkWithReadOfNonExistentEntityTest() {
        CosmosItemOperation operation = BulkOperations.getReadItemOperation(
            UUID.randomUUID().toString(),
            new PartitionKey(this.partitionKey1));

        this.runWithError(
            bulkContainer,
            operations -> operations.add(operation),
            HttpResponseStatus.NOT_FOUND);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkWithReplaceOfStaleEntity() {
        this.createJsonTestDocs(bulkContainer);

        TestDoc staleTestDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
        staleTestDocToReplace.setCost(staleTestDocToReplace.getCost() + 1);

        BulkItemRequestOptions staleReplaceOptions = new BulkItemRequestOptions();
        staleReplaceOptions.setIfMatchETag(UUID.randomUUID().toString());

        CosmosItemOperation operation = BulkOperations.getReplaceItemOperation(
            staleTestDocToReplace.getId(),
            staleTestDocToReplace,
            new PartitionKey(this.partitionKey1),
            staleReplaceOptions);

        this.runWithError(
            bulkContainer,
            operations -> operations.add(operation),
            HttpResponseStatus.PRECONDITION_FAILED);

        // make sure the stale doc hasn't changed
        this.verifyByRead(bulkContainer, this.TestDocPk1ExistingA);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkWithDeleteOfNonExistentEntity() {

        CosmosItemOperation operation =
            BulkOperations.getDeleteItemOperation(
                UUID.randomUUID().toString(), new PartitionKey(this.partitionKey1));

        this.runWithError(
            bulkContainer,
            operations -> operations.add(operation),
            HttpResponseStatus.NOT_FOUND);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkWithCreateConflict() {
        this.createJsonTestDocs(bulkContainer);

        // try to create a doc with id that already exists (should return a Conflict)
        TestDoc conflictingTestDocToCreate = this.getTestDocCopy(this.TestDocPk1ExistingA);
        conflictingTestDocToCreate.setCost(conflictingTestDocToCreate.getCost());

        CosmosItemOperation operation = BulkOperations.getCreateItemOperation(
            conflictingTestDocToCreate,
            new PartitionKey(this.partitionKey1));

        this.runWithError(
            bulkContainer,
            operations -> operations.add(operation),
            HttpResponseStatus.CONFLICT);

        // make sure the conflicted doc hasn't changed
        this.verifyByRead(bulkContainer, this.TestDocPk1ExistingA);
    }

    private void runWithError(
        CosmosContainer container,
        Function<List<CosmosItemOperation>, Boolean> appendOperation,
        HttpResponseStatus expectedFailedOperationStatusCode) {

        TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
        TestDoc anotherTestDocToCreate = this.populateTestDoc(this.partitionKey1);

        List<CosmosItemOperation> operations = new ArrayList<>();
        operations.add(BulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));

        appendOperation.apply(operations);

        operations.add(BulkOperations.getCreateItemOperation(anotherTestDocToCreate, new PartitionKey(this.partitionKey1)));

        List<CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer.processBulkOperations(operations);

        assertThat(bulkResponses.size()).isEqualTo(operations.size());

        assertThat(bulkResponses.get(0).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(bulkResponses.get(1).getResponse().getStatusCode()).isEqualTo(expectedFailedOperationStatusCode.code());
        assertThat(bulkResponses.get(2).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());

        this.verifyByRead(container, testDocToCreate);
        this.verifyByRead(container, anotherTestDocToCreate);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkSessionTokenTest() {
        this.createJsonTestDocs(bulkContainer);

        CosmosItemResponse<TestDoc> readResponse = bulkContainer.readItem(
            this.TestDocPk1ExistingC.getId(),
            this.getPartitionKey(this.partitionKey1),
            TestDoc.class);

        assertThat(readResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        ISessionToken sessionToken = this.getSessionToken(readResponse.getSessionToken());

        // Batch without Read operation
        TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
        TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
        testDocToReplace.setCost(testDocToReplace.getCost() + 1);
        TestDoc testDocToUpsert = this.populateTestDoc(this.partitionKey1);

        List<CosmosItemOperation> operations = new ArrayList<>();
        operations.add(
            BulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));
        operations.add(
            BulkOperations.getReplaceItemOperation(testDocToReplace.getId(), testDocToReplace, new PartitionKey(this.partitionKey1)));
        operations.add(
            BulkOperations.getUpsertItemOperation(testDocToUpsert, new PartitionKey(this.partitionKey1)));
        operations.add(
            BulkOperations.getDeleteItemOperation(this.TestDocPk1ExistingC.getId(), new PartitionKey(this.partitionKey1)));

        List<CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer.processBulkOperations(operations);

        assertThat(bulkResponses.size()).isEqualTo(operations.size());

        assertThat(bulkResponses.get(0).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(this.getSessionToken((bulkResponses.get(0).getResponse().getSessionToken())).getLSN())
            .isGreaterThan(sessionToken.getLSN());

        assertThat(bulkResponses.get(1).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(this.getSessionToken((bulkResponses.get(1).getResponse().getSessionToken())).getLSN())
            .isGreaterThan(sessionToken.getLSN());

        assertThat(bulkResponses.get(2).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(this.getSessionToken((bulkResponses.get(2).getResponse().getSessionToken())).getLSN())
            .isGreaterThan(sessionToken.getLSN());

        assertThat(bulkResponses.get(3).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
        assertThat(this.getSessionToken((bulkResponses.get(2).getResponse().getSessionToken())).getLSN())
            .isGreaterThan(sessionToken.getLSN());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkContentResponseOnWriteTest() {
        this.createJsonTestDocs(bulkContainer);

        TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
        TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
        testDocToReplace.setCost(testDocToReplace.getCost() + 1);
        TestDoc testDocToUpsert = this.populateTestDoc(this.partitionKey1);

        BulkItemRequestOptions contentResponseDisableRequestOption = new BulkItemRequestOptions()
            .setContentResponseOnWriteEnabled(false);

        List<CosmosItemOperation> operations = new ArrayList<>();
        operations.add(
            BulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));

        operations.add(
            BulkOperations.getReplaceItemOperation(
                testDocToReplace.getId(),
                testDocToReplace,
                new PartitionKey(this.partitionKey1),
                contentResponseDisableRequestOption));

        operations.add(
            BulkOperations.getUpsertItemOperation(
                testDocToUpsert,
                new PartitionKey(this.partitionKey1),
                contentResponseDisableRequestOption));

        operations.add(
            BulkOperations.getDeleteItemOperation(this.TestDocPk1ExistingC.getId(), new PartitionKey(this.partitionKey1)));

        operations.add(BulkOperations.getReadItemOperation(
            this.TestDocPk1ExistingD.getId(),
            new PartitionKey(this.partitionKey1),
            contentResponseDisableRequestOption));

        operations.add(BulkOperations.getReadItemOperation(this.TestDocPk1ExistingB.getId(), new PartitionKey(this.partitionKey1)));

        List<CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer.processBulkOperations(operations);
        assertThat(bulkResponses.size()).isEqualTo(operations.size());

        assertThat(bulkResponses.get(0).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(bulkResponses.get(0).getResponse().getItem(TestDoc.class)).isEqualTo(testDocToCreate);

        assertThat(bulkResponses.get(1).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(bulkResponses.get(1).getResponse().getItem(TestDoc.class)).isNull();

        assertThat(bulkResponses.get(2).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(bulkResponses.get(2).getResponse().getItem(TestDoc.class)).isNull();

        assertThat(bulkResponses.get(3).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
        assertThat(bulkResponses.get(3).getResponse().getItem(TestDoc.class)).isNull(); // by default null

        assertThat(bulkResponses.get(4).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        // Doesn't matter for read, it will still return the response
        assertThat(bulkResponses.get(4).getResponse().getItem(TestDoc.class)).isEqualTo(this.TestDocPk1ExistingD);

        assertThat(bulkResponses.get(5).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(bulkResponses.get(5).getResponse().getItem(TestDoc.class)).isEqualTo(this.TestDocPk1ExistingB);
    }
}
