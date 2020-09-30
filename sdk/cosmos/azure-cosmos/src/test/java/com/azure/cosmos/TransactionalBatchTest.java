// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.models.CosmosItemResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.data.Offset;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static com.azure.cosmos.implementation.batch.BatchRequestResponseConstant.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionalBatchTest extends BatchTestBase {

    private CosmosAsyncClient batchClient;
    private CosmosAsyncContainer batchContainer;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public TransactionalBatchTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_TransactionalBatchTest() {
        assertThat(this.batchClient).isNull();
        this.batchClient = getClientBuilder().buildAsyncClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.batchClient);
        batchContainer = batchClient.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.batchClient).isNotNull();
        this.batchClient.close();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchCrud() throws Exception {
         this.runCrudAsync(batchContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchOrdered() {
        CosmosAsyncContainer container = batchContainer;
        this.createJsonTestDocsAsync(container);

        TestDoc firstDoc = this.populateTestDoc(this.partitionKey1);

        TestDoc replaceDoc = this.getTestDocCopy(firstDoc);
        replaceDoc.setCost(replaceDoc.getCost() + 1);

        TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(
            TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                .createItem(firstDoc)
                .replaceItem(replaceDoc.getId(), replaceDoc))
            .block();

        this.verifyBatchProcessed(batchResponse, 2);

        assertThat(batchResponse.get(0).getResponseStatus()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse.get(1).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());

        // Ensure that the replace overwrote the doc from the first operation
        this.verifyByReadAsync(container, replaceDoc);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchItemETagAsync() {
        CosmosAsyncContainer container = batchContainer;
        this.createJsonTestDocsAsync(container);

        {
            BatchTestBase.TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);

            BatchTestBase.TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);

            CosmosItemResponse<TestDoc> response = container.readItem(
                this.TestDocPk1ExistingA.getId(),
                this.getPartitionKey(this.partitionKey1),
                TestDoc.class).block();

            assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

            TransactionalBatchItemRequestOptions firstReplaceOptions = new TransactionalBatchItemRequestOptions();
            firstReplaceOptions.setIfMatchETag(response.getETag());

            TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(
                TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                    .createItem(testDocToCreate)
                    .replaceItem(testDocToReplace.getId(), testDocToReplace, firstReplaceOptions))
                .block();

            this.verifyBatchProcessed(batchResponse, 2);

            assertThat(batchResponse.get(0).getResponseStatus()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(batchResponse.get(1).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());

            // Ensure that the replace overwrote the doc from the first operation
            this.verifyByReadAsync(container, testDocToCreate, batchResponse.get(0).getETag());
            this.verifyByReadAsync(container, testDocToReplace, batchResponse.get(1).getETag());
        }

        {
            TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingB);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);

            TransactionalBatchItemRequestOptions replaceOptions = new TransactionalBatchItemRequestOptions();
            replaceOptions.setIfMatchETag(String.valueOf(this.getRandom().nextInt()));

            TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(
                TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                    .replaceItem(testDocToReplace.getId(), testDocToReplace, replaceOptions))
                .block();

            this.verifyBatchProcessed(batchResponse, 1, HttpResponseStatus.PRECONDITION_FAILED);

            assertThat(batchResponse.get(0).getResponseStatus()).isEqualTo(HttpResponseStatus.PRECONDITION_FAILED.code());

            // ensure the document was not updated
            this.verifyByReadAsync(container, this.TestDocPk1ExistingB);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchItemSessionTokenAsync() {
        CosmosAsyncContainer container = batchContainer;
        this.createJsonTestDocsAsync(container);

        TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);

        BatchTestBase.TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
        testDocToReplace.setCost(testDocToReplace.getCost() + 1);

        CosmosItemResponse<TestDoc> readResponse = container.readItem(
            this.TestDocPk1ExistingA.getId(),
            this.getPartitionKey(this.partitionKey1),
            TestDoc.class).block();

        assertThat(readResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

        ISessionToken beforeRequestSessionToken = this.getSessionToken(readResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN));

        TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(
            TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                .createItem(testDocToCreate)
                .replaceItem(testDocToReplace.getId(), testDocToReplace))
            .block();

        this.verifyBatchProcessed(batchResponse, 2);

        assertThat(batchResponse.get(0).getResponseStatus()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse.get(1).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());

        ISessionToken afterRequestSessionToken = this.getSessionToken(batchResponse.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN));
        assertThat(afterRequestSessionToken.getLSN())
            .as("Response session token should be more than request session token")
            .isGreaterThan(beforeRequestSessionToken.getLSN());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchWithTooManyOperationsAsync() {
        CosmosAsyncContainer container = batchContainer;
        int operationCount = MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST + 1;

        // Increase the doc size by a bit so all docs won't fit in one server request.
        TransactionalBatch batch = TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1));

        for (int i = 0; i < operationCount; i++) {
            batch.readItem("someId");
        }

        TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(batch).block();
        assertThat(batchResponse.getResponseStatus()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchReadsOnlyAsync() throws Exception {
        CosmosAsyncContainer container = batchContainer;
        this.createJsonTestDocsAsync(container);

        TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(
            TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                .readItem(this.TestDocPk1ExistingA.getId())
                .readItem(this.TestDocPk1ExistingB.getId())
                .readItem(this.TestDocPk1ExistingC.getId()))
            .block();

        this.verifyBatchProcessed(batchResponse, 3);

        assertThat(batchResponse.get(0).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.get(1).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.get(2).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());

        assertThat(batchResponse.getOperationResultAtIndex(0, TestDoc.class).getItem()).isEqualTo(this.TestDocPk1ExistingA);
        assertThat(batchResponse.getOperationResultAtIndex(1, TestDoc.class).getItem()).isEqualTo(this.TestDocPk1ExistingB);
        assertThat(batchResponse.getOperationResultAtIndex(2, TestDoc.class).getItem()).isEqualTo(this.TestDocPk1ExistingC);
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
        TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(
            TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                .createItem(testDocToCreate)
                .readItem(this.TestDocPk1ExistingC.getId())
                .replaceItem(testDocToReplace.getId(), testDocToReplace)
                .upsertItem(testDocToUpsert)
                .upsertItem(anotherTestDocToUpsert)
                .deleteItem(this.TestDocPk1ExistingD.getId()))
            .block();

        this.verifyBatchProcessed(batchResponse, 6);

        assertThat(batchResponse.get(0).getResponseStatus()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse.get(1).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.get(2).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.get(3).getResponseStatus()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse.get(4).getResponseStatus()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.get(5).getResponseStatus()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());

        assertThat(batchResponse.getOperationResultAtIndex(1, TestDoc.class).getItem()).isEqualTo(this.TestDocPk1ExistingC);

        this.verifyByReadAsync(container, testDocToCreate);
        this.verifyByReadAsync(container, testDocToReplace);
        this.verifyByReadAsync(container, testDocToUpsert);
        this.verifyByReadAsync(container, anotherTestDocToUpsert);
        this.verifyNotFoundAsync(container, this.TestDocPk1ExistingD);

        return batchResponse;
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchWithCreateConflictAsync() {
        this.runBatchWithCreateConflictAsync(batchContainer);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchWithInvalidCreateAsync() {
        CosmosAsyncContainer container = batchContainer;

        // partition key mismatch between doc and and value passed in to the operation
        this.runWithErrorAsync(
            container,
            batch -> batch.createItem(this.populateTestDoc(UUID.randomUUID().toString())),
            HttpResponseStatus.BAD_REQUEST);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchWithReadOfNonExistentEntityAsync() {
        CosmosAsyncContainer container = batchContainer;
        this.runWithErrorAsync(
            container,
            batch -> batch.readItem(UUID.randomUUID().toString()),
            HttpResponseStatus.NOT_FOUND);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchWithReplaceOfStaleEntityAsync() {
        CosmosAsyncContainer container = batchContainer;
        this.createJsonTestDocsAsync(container);

        TestDoc staleTestDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
        staleTestDocToReplace.setCost(staleTestDocToReplace.getCost() + 1);

        TransactionalBatchItemRequestOptions staleReplaceOptions = new TransactionalBatchItemRequestOptions();
        staleReplaceOptions.setIfMatchETag(UUID.randomUUID().toString());

        this.runWithErrorAsync(
            container,
            batch -> batch.replaceItem(staleTestDocToReplace.getId(), staleTestDocToReplace, staleReplaceOptions),
            HttpResponseStatus.PRECONDITION_FAILED);

        // make sure the stale doc hasn't changed
        this.verifyByReadAsync(container, this.TestDocPk1ExistingA);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void batchWithDeleteOfNonExistentEntityAsync() {
        CosmosAsyncContainer container = batchContainer;

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

        TransactionalBatch batch = TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
            .createItem(testDocToCreate);

        appendOperation.apply(batch);

        TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(
            batch.createItem(anotherTestDocToCreate))
            .block();

        this.verifyBatchProcessed(batchResponse, 3, expectedFailedOperationStatusCode);

        assertThat(batchResponse.get(0).getResponseStatus()).isEqualTo(HttpResponseStatus.FAILED_DEPENDENCY.code());
        assertThat(batchResponse.get(1).getResponseStatus()).isEqualTo(expectedFailedOperationStatusCode.code());
        assertThat(batchResponse.get(2).getResponseStatus()).isEqualTo(HttpResponseStatus.FAILED_DEPENDENCY.code());

        this.verifyNotFoundAsync(container, testDocToCreate);
        this.verifyNotFoundAsync(container, anotherTestDocToCreate);
    }

    private void verifyBatchProcessed(TransactionalBatchResponse batchResponse, int numberOfOperations) {
        this.verifyBatchProcessed(batchResponse, numberOfOperations, HttpResponseStatus.OK);
    }

    private void verifyBatchProcessed(TransactionalBatchResponse batchResponse, int numberOfOperations, HttpResponseStatus expectedStatusCode) {
        assertThat(batchResponse).isNotNull();
        assertThat(batchResponse.getResponseStatus())
            .as("Batch server response had StatusCode {0} instead of {1} expected and had ErrorMessage {2}")
            .isEqualTo(expectedStatusCode.code());

        assertThat(batchResponse.size()).isEqualTo(numberOfOperations);
        assertThat(batchResponse.getRequestCharge()).isPositive();
        assertThat(batchResponse.getCosmosDiagnostics().toString()).isNotEmpty();

        // Allow a delta since we round both the total charge and the individual operation
        // charges to 2 decimal places.
        assertThat(batchResponse.getRequestCharge())
            .isCloseTo(batchResponse.getResults().stream().mapToDouble(TransactionalBatchOperationResult::getRequestCharge).sum(),
            Offset.offset(0.1));
    }
}
