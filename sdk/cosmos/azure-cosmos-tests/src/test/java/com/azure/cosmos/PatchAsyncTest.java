// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkItemRequestOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosBulkPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PatchAsyncTest extends BatchTestBase {
    private CosmosAsyncClient client;
    private CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuilders")
    public PatchAsyncTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_PatchAsyncTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().contentResponseOnWriteEnabled(true).buildAsyncClient();
        container = getSharedMultiPartitionCosmosContainer(this.client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {  "emulator"  }, timeOut = TIMEOUT)
    public void patchInBatch() {
        BatchTestBase.TestDoc testDoc = this.populateTestDoc(this.partitionKey1);
        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations = com.azure.cosmos.models.CosmosPatchOperations.create();
        cosmosPatchOperations.set("/cost", testDoc.getCost() + 12);

        CosmosBatch batch = CosmosBatch.createCosmosBatch(this.getPartitionKey(this.partitionKey1));
        batch.createItemOperation(testDoc);
        batch.patchItemOperation(testDoc.getId(), cosmosPatchOperations);

        CosmosBatchResponse batchResponse = container.executeCosmosBatch(batch).block();

        this.verifyBatchProcessed(batchResponse, 2);

        assertThat(batchResponse.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse.getResults().get(0).getItem(TestDoc.class)).isEqualTo(testDoc);

        assertThat(batchResponse.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

        testDoc.setCost(testDoc.getCost() + 12);

        // Modified test doc should be equal to patched test doc
        assertThat(batchResponse.getResults().get(1).getItem(TestDoc.class)).isEqualTo(testDoc);

        this.verifyByRead(container, testDoc);
    }

    @Test(groups = {  "emulator"  }, timeOut = TIMEOUT)
    public void conditionalPatchInBatch() {
        BatchTestBase.TestDoc testDoc = this.populateTestDoc(this.partitionKey1);

        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperationsFirst = com.azure.cosmos.models.CosmosPatchOperations.create();
        int costValue = testDoc.getCost() + 12;
        cosmosPatchOperationsFirst.set("/cost", costValue);

        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperationsSecond = com.azure.cosmos.models.CosmosPatchOperations.create();
        cosmosPatchOperationsSecond.set("/cost", 0);

        com.azure.cosmos.models.CosmosBatch batchFail = com.azure.cosmos.models.CosmosBatch.createCosmosBatch(this.getPartitionKey(this.partitionKey1));
        batchFail.createItemOperation(testDoc);
        batchFail.patchItemOperation(testDoc.getId(), cosmosPatchOperationsFirst);

        CosmosBatchPatchItemRequestOptions transactionalBatchPatchItemRequestOptionsFail = new CosmosBatchPatchItemRequestOptions();
        int costFalse = costValue-1;
        transactionalBatchPatchItemRequestOptionsFail.setFilterPredicate("from root where root.cost = " + costFalse);
        batchFail.patchItemOperation(testDoc.getId(), cosmosPatchOperationsSecond, transactionalBatchPatchItemRequestOptionsFail);

        CosmosBatchResponse batchResponseFail = container.executeCosmosBatch(batchFail).onErrorMap(throwable -> {
            return throwable;
        }).block();

        assertThat(batchResponseFail.getStatusCode() == HttpResponseStatus.PRECONDITION_FAILED.code());
        assertThat(batchResponseFail.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.FAILED_DEPENDENCY.code());
        assertThat(batchResponseFail.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.FAILED_DEPENDENCY.code());
        assertThat(batchResponseFail.getResults().get(2).getStatusCode()).isEqualTo(HttpResponseStatus.PRECONDITION_FAILED.code());

        CosmosBatchPatchItemRequestOptions transactionalBatchPatchItemRequestOptionsTrue = new CosmosBatchPatchItemRequestOptions();
        transactionalBatchPatchItemRequestOptionsTrue.setFilterPredicate("from root where root.cost = " + costValue);

        com.azure.cosmos.models.CosmosBatch batchPass = com.azure.cosmos.models.CosmosBatch.createCosmosBatch(this.getPartitionKey(this.partitionKey1));
        batchPass.createItemOperation(testDoc);
        batchPass.patchItemOperation(testDoc.getId(), cosmosPatchOperationsFirst);
        batchPass.patchItemOperation(testDoc.getId(), cosmosPatchOperationsSecond, transactionalBatchPatchItemRequestOptionsTrue);

        CosmosBatchResponse batchResponsePass = container.executeCosmosBatch(batchPass).onErrorMap(throwable -> {
            return throwable;
        }).block();

        this.verifyBatchProcessed(batchResponsePass, 3);

        assertThat(batchResponsePass.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponsePass.getResults().get(0).getItem(TestDoc.class)).isEqualTo(testDoc);

        testDoc.setCost(costValue);
        assertThat(batchResponsePass.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        // Modified test doc should be equal to patched test doc
        assertThat(batchResponsePass.getResults().get(1).getItem(TestDoc.class)).isEqualTo(testDoc);

        testDoc.setCost(0);
        assertThat(batchResponsePass.getResults().get(2).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        // Modified test doc should be equal to patched test doc
        assertThat(batchResponsePass.getResults().get(2).getItem(TestDoc.class)).isEqualTo(testDoc);

        this.verifyByRead(container, testDoc);
    }

    @Test(groups = {  "emulator"  }, timeOut = TIMEOUT)
    public void patchInBulk() {
        List<CosmosItemOperation> operations = new ArrayList<>();

        this.createJsonTestDocs(container);
        TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
        TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
        testDocToReplace.setCost(testDocToReplace.getCost() + 1);

        BatchTestBase.TestDoc testDocForPatch = this.populateTestDoc(this.partitionKey1);
        CosmosItemResponse<TestDoc> createItemResponse =
            container.createItem(testDocForPatch, new PartitionKey(this.partitionKey1), null).block();
        assertThat(createItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());

        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations1 = com.azure.cosmos.models.CosmosPatchOperations.create();
        int costInterimValue = 100;
        cosmosPatchOperations1.replace("/cost", costInterimValue);

        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations2 = com.azure.cosmos.models.CosmosPatchOperations.create();
        cosmosPatchOperations2.replace("/description", "xx");

        CosmosBulkPatchItemRequestOptions contentResponseDisableRequestOption = new CosmosBulkPatchItemRequestOptions()
            .setContentResponseOnWriteEnabled(false);

        CosmosBulkItemRequestOptions contentItemResponseDisableRequestOption = new CosmosBulkItemRequestOptions()
            .setContentResponseOnWriteEnabled(false);

        com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperationsContinue = com.azure.cosmos.models.CosmosPatchOperations.create();
        int costFinalValue = 200;
        cosmosPatchOperationsContinue.replace("/cost", costFinalValue);

        int costFalseValue = costInterimValue - 1;
        CosmosBulkPatchItemRequestOptions conditionFailureRequestOption = new CosmosBulkPatchItemRequestOptions()
            .setFilterPredicate("from root where root.cost = " + costFalseValue);

        CosmosBulkPatchItemRequestOptions conditionSuccessRequestOption = new CosmosBulkPatchItemRequestOptions()
            .setFilterPredicate("from root where root.cost = " + costInterimValue);

        operations.add(
            CosmosBulkOperations.getCreateItemOperation(testDocToCreate, new PartitionKey(this.partitionKey1)));

        operations.add(
            CosmosBulkOperations.getReplaceItemOperation(
                testDocToReplace.getId(),
                testDocToReplace,
                new PartitionKey(this.partitionKey1),
                contentItemResponseDisableRequestOption));

        CosmosBulkPatchItemRequestOptions patchContentResponseDisableRequestOption = new CosmosBulkPatchItemRequestOptions()
            .setContentResponseOnWriteEnabled(false);

        operations.add(
            CosmosBulkOperations.getPatchItemOperation(
                testDocForPatch.id,
                new PartitionKey(this.partitionKey1),
                cosmosPatchOperations1,
                patchContentResponseDisableRequestOption));

        operations.add(
            CosmosBulkOperations.getPatchItemOperation(
                testDocForPatch.id,
                new PartitionKey(this.partitionKey1),
                cosmosPatchOperations2));

        operations.add(
            CosmosBulkOperations.getPatchItemOperation(
                testDocForPatch.id,
                new PartitionKey(this.partitionKey1),
                cosmosPatchOperationsContinue,
                conditionFailureRequestOption
            )
        );

        operations.add(
            CosmosBulkOperations.getPatchItemOperation(
                testDocForPatch.id,
                new PartitionKey(this.partitionKey1),
                cosmosPatchOperationsContinue,
                conditionSuccessRequestOption
            )
        );

        List<CosmosBulkOperationResponse<Object>> bulkResponses =
            container.executeBulkOperations(Flux.fromIterable(operations)).collectList().block();
        assertThat(bulkResponses.size()).isEqualTo(operations.size());

        assertThat(bulkResponses.get(0).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(bulkResponses.get(0).getResponse().getItem(TestDoc.class)).isEqualTo(testDocToCreate);

        assertThat(bulkResponses.get(1).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(bulkResponses.get(1).getResponse().getItem(TestDoc.class)).isNull();

        // Patch with no content returned
        assertThat(bulkResponses.get(2).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(bulkResponses.get(2).getResponse().getItem(TestDoc.class)).isNull();

        // patch with content returned
        assertThat(bulkResponses.get(3).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

        // The content should at least have description set as in request. Can't check both as we don't know the order
        // in which both patches were applies in backend. Can only check for this one.
        TestDoc testDocResponsePatch = bulkResponses.get(3).getResponse().getItem(TestDoc.class);
        assertThat(testDocResponsePatch.getDescription()).isEqualTo("xx");

        // patch fails as precondition fails.
        assertThat(bulkResponses.get(4).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.PRECONDITION_FAILED.code());
        assertThat(bulkResponses.get(4).getResponse().getItem(TestDoc.class)).isNull();

        // patch with condition met.
        assertThat(bulkResponses.get(5).getResponse().getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        TestDoc testDocResponsePatchCondition = bulkResponses.get(5).getResponse().getItem(TestDoc.class);
        assertThat(testDocResponsePatchCondition.getCost()).isEqualTo(costFinalValue);

        // Verify if the document is updated with both patch operations.
        testDocForPatch.setCost(costFinalValue);
        testDocForPatch.setDescription("xx");
        this.verifyByRead(container, testDocForPatch);
    }

    @Test(groups = {  "emulator"  }, timeOut = TIMEOUT)
    public void itemPatchSuccessForNullValue() {
        // Null value should be allowed for add, set, and replace

        ObjectNode objectNode = Utils.getSimpleObjectMapper().createObjectNode();
        String id = UUID.randomUUID().toString();
        String pkValue = "mypk";

        String stringColumnName = "stringColumn";
        String stringColumnName2 = "stringColumn2";
        String newStringColumnName = "newStringColumn";

        objectNode.put("id", id);
        objectNode.put("mypk", pkValue);
        objectNode.put(stringColumnName, UUID.randomUUID().toString());
        objectNode.put(stringColumnName2, UUID.randomUUID().toString());

        this.container.createItem(objectNode).block();

        CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
        patchOperations.add("/" + newStringColumnName, null);
        patchOperations.replace("/" + stringColumnName, null);
        patchOperations.set("/" + stringColumnName2, null);

        ObjectNode patchedItem = this.container.patchItem(id, new PartitionKey(pkValue), patchOperations, ObjectNode.class).block().getItem();
        assertThat(patchedItem).isNotNull();
        assert(patchedItem.get(newStringColumnName)).isNull();
        assert(patchedItem.get(stringColumnName)).isNull();
        assert(patchedItem.get(stringColumnName2)).isNull();
    }
}
