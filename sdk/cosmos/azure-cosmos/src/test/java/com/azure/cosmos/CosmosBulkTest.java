// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
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

        List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();
            TestDoc testDoc = this.populateTestDoc(partitionKey);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));

            partitionKey = UUID.randomUUID().toString();
            EventDoc eventDoc = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(eventDoc, new PartitionKey(partitionKey)));
        }

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Iterable<com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest>> bulkResponse = bulkContainer
            .executeBulkOperations(cosmosItemOperations, cosmosBulkExecutionOptions);

        int size = 0;
        for (com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse : bulkResponse) {
            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();

            size++;
            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
        }

        assertThat(size).isEqualTo(totalRequest * 2);
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

        List<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponse = bulkContainer
            .executeBulkOperations(cosmosItemOperations);

        assertThat(bulkResponse.size()).isEqualTo(totalRequest);

        for (com.azure.cosmos.models.CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse : bulkResponse) {
            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();

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

        List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

        for (int i = 0; i < totalRequest; i++) {
            String partitionKey = UUID.randomUUID().toString();

            // use i as a identifier for re check.
            TestDoc testDoc = this.populateTestDoc(partitionKey, i, 20);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));
        }
        createItemsAndVerify(cosmosItemOperations);

        List<com.azure.cosmos.models.CosmosItemOperation> deleteCosmosItemOperation = new ArrayList<>();

        for(com.azure.cosmos.models.CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            deleteCosmosItemOperation.add(
                CosmosBulkOperations.getDeleteItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue()));
        }

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Iterable<CosmosBulkOperationResponse<TestDoc>> bulkResponse  = bulkContainer
            .executeBulkOperations(deleteCosmosItemOperation, cosmosBulkExecutionOptions);

        int size = 0;

        for (com.azure.cosmos.models.CosmosBulkOperationResponse<TestDoc> cosmosBulkOperationResponse : bulkResponse) {
            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            size++;

            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
        }
        assertThat(size).isEqualTo(totalRequest);
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

        List<com.azure.cosmos.models.CosmosItemOperation> readCosmosItemOperations = new ArrayList<>();

        for(com.azure.cosmos.models.CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            readCosmosItemOperations.add(
                CosmosBulkOperations.getReadItemOperation(testDoc.getId(), cosmosItemOperation.getPartitionKeyValue()));
        }

        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Iterable<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponse  = bulkContainer
            .executeBulkOperations(readCosmosItemOperations, cosmosBulkExecutionOptions);

        int size = 0;
        for (com.azure.cosmos.models.CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse : bulkResponse) {

            size++;
            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

            // Using cost as list index like we assigned
            TestDoc testDoc = cosmosBulkItemResponse.getItem(TestDoc.class);
            assertThat(testDoc).isEqualTo(cosmosItemOperations.get(testDoc.getCost()).getItem());
        }
        assertThat(size).isEqualTo(totalRequest);
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

        List<com.azure.cosmos.models.CosmosItemOperation> replaceCosmosItemOperations = new ArrayList<>();

        for(com.azure.cosmos.models.CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            TestDoc testDoc = cosmosItemOperation.getItem();
            replaceCosmosItemOperations.add(CosmosBulkOperations.getReplaceItemOperation(
                testDoc.getId(),
                cosmosItemOperation.getItem(),
                cosmosItemOperation.getPartitionKeyValue()));
        }

        List<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponse  = bulkContainer
            .executeBulkOperations(replaceCosmosItemOperations);

        assertThat(bulkResponse.size()).isEqualTo(totalRequest);

        for (com.azure.cosmos.models.CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse : bulkResponse) {

            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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

    private void createItemsAndVerify(List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations) {
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

        Iterable<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponse = bulkContainer
            .executeBulkOperations(cosmosItemOperations, cosmosBulkExecutionOptions);

        int size = 0;
        HashSet<Integer> distinctIndex = new HashSet<>();

        for (com.azure.cosmos.models.CosmosBulkOperationResponse<Object> cosmosBulkOperationResponse : bulkResponse) {

            size++;
            com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
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

        assertThat(size).isEqualTo(cosmosItemOperations.size());

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

            CosmosBulkItemRequestOptions firstReplaceOptions = new CosmosBulkItemRequestOptions();
            firstReplaceOptions.setIfMatchETag(response.getETag());

            List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));
            cosmosItemOperations.add(CosmosBulkOperations.getReplaceItemOperation(
                testDocToReplace.getId(), testDocToReplace, new PartitionKey(this.partitionKey1), firstReplaceOptions));

            List<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer
                .executeBulkOperations(cosmosItemOperations);

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

            CosmosBulkItemRequestOptions replaceOptions = new CosmosBulkItemRequestOptions();
            replaceOptions.setIfMatchETag(String.valueOf(this.getRandom().nextInt()));

            List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
            cosmosItemOperations.add(CosmosBulkOperations.getReplaceItemOperation(
                testDocToReplace.getId(), testDocToReplace, new PartitionKey(this.partitionKey1), replaceOptions));

            List<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer
                .executeBulkOperations(cosmosItemOperations);

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

            CosmosBulkItemRequestOptions readOptions = new CosmosBulkItemRequestOptions();
            readOptions.setIfMatchETag(response.getETag());

            BatchTestBase.TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);

            List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
            cosmosItemOperations.add(CosmosBulkOperations.getReadItemOperation(
                this.TestDocPk1ExistingA.getId(),
                this.getPartitionKey(this.partitionKey1),
                readOptions));
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));

            List<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer
                .executeBulkOperations(cosmosItemOperations);

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
        com.azure.cosmos.models.CosmosItemOperation operation =
            CosmosBulkOperations.getCreateItemOperation(
                this.populateTestDoc(UUID.randomUUID().toString()), new PartitionKey(this.partitionKey1));

        this.runWithError(
            bulkContainer,
            operations -> operations.add(operation),
            HttpResponseStatus.BAD_REQUEST);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void bulkWithReadOfNonExistentEntityTest() {
        com.azure.cosmos.models.CosmosItemOperation operation = CosmosBulkOperations.getReadItemOperation(
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

        CosmosBulkItemRequestOptions staleReplaceOptions = new CosmosBulkItemRequestOptions();
        staleReplaceOptions.setIfMatchETag(UUID.randomUUID().toString());

        com.azure.cosmos.models.CosmosItemOperation operation = CosmosBulkOperations.getReplaceItemOperation(
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

        com.azure.cosmos.models.CosmosItemOperation operation =
            CosmosBulkOperations.getDeleteItemOperation(
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

        com.azure.cosmos.models.CosmosItemOperation operation = CosmosBulkOperations.getCreateItemOperation(
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
        Function<List<com.azure.cosmos.models.CosmosItemOperation>, Boolean> appendOperation,
        HttpResponseStatus expectedFailedOperationStatusCode) {

        TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
        TestDoc anotherTestDocToCreate = this.populateTestDoc(this.partitionKey1);

        List<com.azure.cosmos.models.CosmosItemOperation> operations = new ArrayList<>();
        operations.add(CosmosBulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));

        appendOperation.apply(operations);

        operations.add(CosmosBulkOperations.getCreateItemOperation(anotherTestDocToCreate, new PartitionKey(this.partitionKey1)));

        List<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer.executeBulkOperations(operations);

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

        List<com.azure.cosmos.models.CosmosItemOperation> operations = new ArrayList<>();
        operations.add(
            CosmosBulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));
        operations.add(
            CosmosBulkOperations.getReplaceItemOperation(testDocToReplace.getId(), testDocToReplace, new PartitionKey(this.partitionKey1)));
        operations.add(
            CosmosBulkOperations.getUpsertItemOperation(testDocToUpsert, new PartitionKey(this.partitionKey1)));
        operations.add(
            CosmosBulkOperations.getDeleteItemOperation(this.TestDocPk1ExistingC.getId(), new PartitionKey(this.partitionKey1)));

        List<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer.executeBulkOperations(operations);

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

        CosmosBulkItemRequestOptions contentResponseDisableRequestOption = new CosmosBulkItemRequestOptions()
            .setContentResponseOnWriteEnabled(false);

        List<com.azure.cosmos.models.CosmosItemOperation> operations = new ArrayList<>();
        operations.add(
            CosmosBulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));

        operations.add(
            CosmosBulkOperations.getReplaceItemOperation(
                testDocToReplace.getId(),
                testDocToReplace,
                new PartitionKey(this.partitionKey1),
                contentResponseDisableRequestOption));

        operations.add(
            CosmosBulkOperations.getUpsertItemOperation(
                testDocToUpsert,
                new PartitionKey(this.partitionKey1),
                contentResponseDisableRequestOption));

        operations.add(
            CosmosBulkOperations.getDeleteItemOperation(this.TestDocPk1ExistingC.getId(), new PartitionKey(this.partitionKey1)));

        operations.add(CosmosBulkOperations.getReadItemOperation(
            this.TestDocPk1ExistingD.getId(),
            new PartitionKey(this.partitionKey1),
            contentResponseDisableRequestOption));

        operations.add(CosmosBulkOperations.getReadItemOperation(this.TestDocPk1ExistingB.getId(), new PartitionKey(this.partitionKey1)));

        List<com.azure.cosmos.models.CosmosBulkOperationResponse<Object>> bulkResponses = bulkContainer.executeBulkOperations(operations);
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
