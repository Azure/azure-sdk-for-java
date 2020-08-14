// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.EmulatorTest;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.batch.TransactionalBatch;
import com.azure.cosmos.batch.TransactionalBatchResponse;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.models.CosmosItemResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static com.azure.cosmos.batch.BatchRequestResponseConstant.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES;
import static com.azure.cosmos.batch.BatchRequestResponseConstant.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
import static org.testng.Assert.*;

public class BatchSinglePartitionKeyTests extends BatchTestBase {

    @Factory(dataProvider = "simpleClientBuilders")
    public BatchSinglePartitionKeyTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchCrud() throws Exception {
         this.runCrudAsync(super.jsonContainer);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT * 100)
    public void batchGatewayCrud() throws Exception {
        this.runCrudAsync(super.gatewayJsonContainer);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchSharedThroughputCrud() throws Exception {
        this.runCrudAsync(super.sharedThroughputContainer);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchOrdered() {
        CosmosAsyncContainer container = super.jsonContainer;
        this.createJsonTestDocsAsync(container);

        TestDoc firstDoc = this.populateTestDoc(this.partitionKey1);

        TestDoc replaceDoc = this.getTestDocCopy(firstDoc);
        replaceDoc.setCost(replaceDoc.getCost() + 1);

        TransactionalBatchResponse batchResponse = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
            .createItem(firstDoc)
            .replaceItem(replaceDoc.getId(), replaceDoc)
            .executeAsync().block();

        this.verifyBatchProcessed(batchResponse, 2);

        assertEquals(HttpResponseStatus.CREATED, batchResponse.get(0).getStatus());
        assertEquals(HttpResponseStatus.OK, batchResponse.get(1).getStatus());

        // Ensure that the replace overwrote the doc from the first operation
        this.verifyByReadAsync(container, replaceDoc);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchItemETagAsync() {
        CosmosAsyncContainer container = super.jsonContainer;
        this.createJsonTestDocsAsync(container);

        {
            BatchTestBase.TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);

            BatchTestBase.TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);

            CosmosItemResponse<TestDoc> response = container.readItem(
                this.TestDocPk1ExistingA.getId(),
                this.getPartitionKey(this.partitionKey1),
                TestDoc.class).block();

            assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());

            RequestOptions firstReplaceOptions = new RequestOptions();
            firstReplaceOptions.setIfMatchETag(response.getETag());

            TransactionalBatchResponse batchResponse = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                .createItem(testDocToCreate)
                .replaceItem(testDocToReplace.getId(), testDocToReplace, firstReplaceOptions)
                .executeAsync().block();

            this.verifyBatchProcessed(batchResponse, 2);

            assertEquals(HttpResponseStatus.CREATED, batchResponse.get(0).getStatus());
            assertEquals(HttpResponseStatus.OK, batchResponse.get(1).getStatus());

            // Ensure that the replace overwrote the doc from the first operation
            this.verifyByReadAsync(container, testDocToCreate, batchResponse.get(0).getETag());
            this.verifyByReadAsync(container, testDocToReplace, batchResponse.get(1).getETag());
        }

        {
            TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingB);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);

            RequestOptions replaceOptions = new RequestOptions();
            replaceOptions.setIfMatchETag(String.valueOf(this.getRandom().nextInt()));

            TransactionalBatchResponse batchResponse = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                .replaceItem(testDocToReplace.getId(), testDocToReplace, replaceOptions)
                .executeAsync().block();

            this.verifyBatchProcessed(batchResponse, 1, HttpResponseStatus.PRECONDITION_FAILED);

            assertEquals(HttpResponseStatus.PRECONDITION_FAILED, batchResponse.get(0).getStatus());

            // ensure the document was not updated
            this.verifyByReadAsync(container, this.TestDocPk1ExistingB);
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchItemSessionTokenAsync() {
        CosmosAsyncContainer container = this.jsonContainer;
        this.createJsonTestDocsAsync(container);

        TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);

        BatchTestBase.TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
        testDocToReplace.setCost(testDocToReplace.getCost() + 1);

        CosmosItemResponse<TestDoc> readResponse = container.readItem(
            this.TestDocPk1ExistingA.getId(),
            this.getPartitionKey(this.partitionKey1),
            TestDoc.class).block();

        assertEquals(HttpResponseStatus.OK.code(), readResponse.getStatusCode());

        ISessionToken beforeRequestSessionToken = this.getSessionToken(readResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN));

        TransactionalBatchResponse batchResponse = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
            .createItem(testDocToCreate)
            .replaceItem(testDocToReplace.getId(), testDocToReplace)
            .executeAsync().block();

        this.verifyBatchProcessed(batchResponse, 2);

        assertEquals(HttpResponseStatus.CREATED, batchResponse.get(0).getStatus());
        assertEquals(HttpResponseStatus.OK, batchResponse.get(1).getStatus());

        ISessionToken afterRequestSessionToken = this.getSessionToken(batchResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN));
        assertTrue(afterRequestSessionToken.getLSN() > beforeRequestSessionToken.getLSN(), "Response session token should be more than request session token");
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchLargerThanServerRequestAsync() {
        CosmosAsyncContainer container = this.jsonContainer;

        int operationCount = 20;
        int appxDocSize = MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES / operationCount;

        // Increase the doc size by a bit so all docs won't fit in one server request.
        appxDocSize = (int)(appxDocSize * 1.05);
        TransactionalBatch batch = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1));

        for (int i = 0; i < operationCount; i++) {
            TestDoc doc = this.populateTestDoc(this.partitionKey1, appxDocSize);
            batch.createItem(doc);
        }

        TransactionalBatchResponse batchResponse = batch.executeAsync().block();
        assertEquals(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, batchResponse.getResponseStatus());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchWithTooManyOperationsAsync() {
        CosmosAsyncContainer container = this.jsonContainer;
        int operationCount = MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST + 1;

        // Increase the doc size by a bit so all docs won't fit in one server request.
        TransactionalBatch batch = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1));

        for (int i = 0; i < operationCount; i++) {
            batch.readItem("someId");
        }

        TransactionalBatchResponse batchResponse = batch.executeAsync().block();
        assertEquals(HttpResponseStatus.BAD_REQUEST, batchResponse.getResponseStatus());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchServerResponseTooLargeAsync() {

        CosmosAsyncContainer container = this.jsonContainer;
        int operationCount = 10;
        int appxDocSizeInBytes = 1 * 1024 * 1024;

        TestDoc doc = this.createJsonTestDocAsync(container, this.partitionKey1, appxDocSizeInBytes);

        TransactionalBatch batch = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1));
        for (int i = 0; i < operationCount; i++) {
            batch.readItem(doc.getId());
        }

        TransactionalBatchResponse batchResponse = batch.executeAsync().block();

        this.verifyBatchProcessed(
            batchResponse,
            operationCount,
            HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);

        assertEquals(HttpResponseStatus.FAILED_DEPENDENCY, batchResponse.get(0).getStatus());
        assertEquals(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, batchResponse.get(operationCount - 1).getStatus());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchReadsOnlyAsync() throws Exception {
        CosmosAsyncContainer container = this.jsonContainer;
        this.createJsonTestDocsAsync(container);

        TransactionalBatchResponse batchResponse = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
            .readItem(this.TestDocPk1ExistingA.getId())
            .readItem(this.TestDocPk1ExistingB.getId())
            .readItem(this.TestDocPk1ExistingC.getId())
            .executeAsync().block();

        this.verifyBatchProcessed(batchResponse, 3);

        assertEquals(HttpResponseStatus.OK,  batchResponse.get(0).getStatus());
        assertEquals(HttpResponseStatus.OK,  batchResponse.get(1).getStatus());
        assertEquals(HttpResponseStatus.OK,  batchResponse.get(2).getStatus());

        assertEquals(this.TestDocPk1ExistingA, batchResponse.getOperationResultAtIndex(0, TestDoc.class).getResource());
        assertEquals(this.TestDocPk1ExistingB, batchResponse.getOperationResultAtIndex(1, TestDoc.class).getResource());
        assertEquals(this.TestDocPk1ExistingC, batchResponse.getOperationResultAtIndex(2, TestDoc.class).getResource());
    }

    private TransactionalBatchResponse runCrudAsync(CosmosAsyncContainer container) throws Exception {
        this.createJsonTestDocsAsync(container);

        BatchTestBase.TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
        BatchTestBase.TestDoc testDocToUpsert = this.populateTestDoc(this.partitionKey1);

        BatchTestBase.TestDoc anotherTestDocToUpsert = this.getTestDocCopy(this.TestDocPk1ExistingA);
        anotherTestDocToUpsert.setCost(anotherTestDocToUpsert.getCost() + 1);

        BatchTestBase.TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingB);
        testDocToReplace.setCost(testDocToReplace.getCost() + 1);

        // We run CRUD operations where all are expected to return HTTP 2xx.
        TransactionalBatchResponse batchResponse = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
            .createItem(testDocToCreate)
            .readItem(this.TestDocPk1ExistingC.getId())
            .replaceItem(testDocToReplace.getId(), testDocToReplace)
            .upsertItem(testDocToUpsert)
            .upsertItem(anotherTestDocToUpsert)
            .deleteItem(this.TestDocPk1ExistingD.getId())
            .executeAsync().block();

        this.verifyBatchProcessed(batchResponse, 6);

        assertEquals(HttpResponseStatus.CREATED, batchResponse.get(0).getStatus());
        assertEquals(HttpResponseStatus.OK, batchResponse.get(1).getStatus());
        assertEquals(HttpResponseStatus.OK, batchResponse.get(2).getStatus());
        assertEquals(HttpResponseStatus.CREATED, batchResponse.get(3).getStatus());
        assertEquals(HttpResponseStatus.OK, batchResponse.get(4).getStatus());
        assertEquals(HttpResponseStatus.NO_CONTENT, batchResponse.get(5).getStatus());

        assertEquals(this.TestDocPk1ExistingC, batchResponse.getOperationResultAtIndex(1, TestDoc.class).getResource());

        this.verifyByReadAsync(container, testDocToCreate);
        this.verifyByReadAsync(container, testDocToReplace);
        this.verifyByReadAsync(container, testDocToUpsert);
        this.verifyByReadAsync(container, anotherTestDocToUpsert);
        this.verifyNotFoundAsync(container, this.TestDocPk1ExistingD);

        return batchResponse;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchWithCreateConflictAsync() {
        this.runBatchWithCreateConflictAsync(this.jsonContainer);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchWithCreateConflictGatewayAsync() {
        this.runBatchWithCreateConflictAsync(this.gatewayJsonContainer);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchWithCreateConflictSharedThroughputAsync() {
        this.runBatchWithCreateConflictAsync(this.sharedThroughputContainer);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchWithInvalidCreateAsync() {
        CosmosAsyncContainer container = this.jsonContainer;

        // partition key mismatch between doc and and value passed in to the operation
        this.runWithErrorAsync(
            container,
            batch -> batch.createItem(this.populateTestDoc(UUID.randomUUID().toString())),
            HttpResponseStatus.BAD_REQUEST);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchWithReadOfNonExistentEntityAsync() {
        CosmosAsyncContainer container = this.jsonContainer;
        this.runWithErrorAsync(
            container,
            batch -> batch.readItem(UUID.randomUUID().toString()),
            HttpResponseStatus.NOT_FOUND);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchWithReplaceOfStaleEntityAsync() {
        CosmosAsyncContainer container = this.jsonContainer;
        this.createJsonTestDocsAsync(container);

        TestDoc staleTestDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
        staleTestDocToReplace.setCost(staleTestDocToReplace.getCost() + 1);

        RequestOptions staleReplaceOptions = new RequestOptions();
        staleReplaceOptions.setIfMatchETag(UUID.randomUUID().toString());

        this.runWithErrorAsync(
            container,
            batch -> batch.replaceItem(staleTestDocToReplace.getId(), staleTestDocToReplace, staleReplaceOptions),
            HttpResponseStatus.PRECONDITION_FAILED);

        // make sure the stale doc hasn't changed
        this.verifyByReadAsync(container, this.TestDocPk1ExistingA);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchWithDeleteOfNonExistentEntityAsync() {
        CosmosAsyncContainer container = this.jsonContainer;

        this.runWithErrorAsync(
            container,
            batch -> batch.deleteItem(UUID.randomUUID().toString()),
            HttpResponseStatus.NOT_FOUND);
    }

    private void runBatchWithCreateConflictAsync(CosmosAsyncContainer container) {
        this.createJsonTestDocsAsync(container);

        // try to create a doc with id that already exists (should return a Conflict)
        TestDoc conflictingTestDocToCreate = this.getTestDocCopy(this.TestDocPk1ExistingA);
        conflictingTestDocToCreate.setCost(conflictingTestDocToCreate.getCost());

        this.runWithErrorAsync(
            container,
            batch -> batch.createItem(conflictingTestDocToCreate),
            HttpResponseStatus.CONFLICT);

        // make sure the conflicted doc hasn't changed
        this.verifyByReadAsync(container, this.TestDocPk1ExistingA);
    }


    private void runWithErrorAsync(
        CosmosAsyncContainer container,
        Function<TransactionalBatch, TransactionalBatch> appendOperation,
        HttpResponseStatus expectedFailedOperationStatusCode) {

        TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
        TestDoc anotherTestDocToCreate = this.populateTestDoc(this.partitionKey1);

        TransactionalBatch batch = container.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
            .createItem(testDocToCreate);

        appendOperation.apply(batch);

        TransactionalBatchResponse batchResponse = batch
            .createItem(anotherTestDocToCreate)
            .executeAsync().block();

        this.verifyBatchProcessed(batchResponse, 3, expectedFailedOperationStatusCode);

        assertEquals(HttpResponseStatus.FAILED_DEPENDENCY, batchResponse.get(0).getStatus());
        assertEquals(expectedFailedOperationStatusCode, batchResponse.get(1).getStatus());
        assertEquals(HttpResponseStatus.FAILED_DEPENDENCY, batchResponse.get(2).getStatus());

        this.verifyNotFoundAsync(container, testDocToCreate);
        this.verifyNotFoundAsync(container, anotherTestDocToCreate);
    }

    private void verifyBatchProcessed(TransactionalBatchResponse batchResponse, int numberOfOperations) {
        this.verifyBatchProcessed(batchResponse, numberOfOperations, HttpResponseStatus.OK);
    }

    private void verifyBatchProcessed(TransactionalBatchResponse batchResponse, int numberOfOperations, HttpResponseStatus expectedStatusCode) {
        assertNotNull(batchResponse);
        assertEquals(
            expectedStatusCode,
            batchResponse.getResponseStatus(),
            "Batch server response had StatusCode {0} instead of {1} expected and had ErrorMessage {2}");

        assertEquals(numberOfOperations, batchResponse.size());
        assertTrue(batchResponse.getRequestCharge() > 0);

        // Allow a delta since we round both the total charge and the individual operation
        // charges to 2 decimal places.
        assertEquals(
            batchResponse.getRequestCharge(),
            batchResponse.stream().mapToDouble(result -> result.getRequestCharge()).sum(),
            0.1);
    }
}
