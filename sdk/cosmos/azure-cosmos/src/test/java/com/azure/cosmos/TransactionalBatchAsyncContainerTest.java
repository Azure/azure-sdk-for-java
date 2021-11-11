// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionalBatchAsyncContainerTest extends BatchTestBase {

    private CosmosAsyncClient batchClient;
    private CosmosAsyncContainer batchAsyncContainer;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public TransactionalBatchAsyncContainerTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_TransactionalBatchAsyncContainerTest() {
        assertThat(this.batchClient).isNull();
        this.batchClient = getClientBuilder().buildAsyncClient();
        batchAsyncContainer = getSharedMultiPartitionCosmosContainer(this.batchClient);
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeCloseAsync(this.batchClient);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchExecutionRepeat() {
        TestDoc firstDoc = this.populateTestDoc(this.partitionKey1);
        TestDoc replaceDoc = this.getTestDocCopy(firstDoc);
        replaceDoc.setCost(replaceDoc.getCost() + 1);
        CosmosBatch batch = CosmosBatch.createCosmosBatch(this.getPartitionKey(this.partitionKey1));
        batch.createItemOperation(firstDoc);
        batch.replaceItemOperation(replaceDoc.getId(), replaceDoc);

        Mono<CosmosBatchResponse> batchResponseMono = batchAsyncContainer.executeCosmosBatch(batch);

        CosmosBatchResponse batchResponse1 = batchResponseMono.block();
        this.verifyBatchProcessed(batchResponse1, 2);

        assertThat(batchResponse1.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse1.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

        // Block again.
        CosmosBatchResponse batchResponse2 = batchResponseMono.block();
        assertThat(batchResponse2.getStatusCode()).isEqualTo(HttpResponseStatus.CONFLICT.code());
        assertThat(batchResponse2.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CONFLICT.code());
        assertThat(batchResponse2.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.FAILED_DEPENDENCY.code());
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT * 100)
    public void batchInvalidSessionToken() throws Exception {
        CosmosAsyncContainer container = batchAsyncContainer;
        this.createJsonTestDocs(container);

        CosmosItemResponse<TestDoc> readResponse = container.readItem(
            this.TestDocPk1ExistingC.getId(),
            this.getPartitionKey(this.partitionKey1),
            TestDoc.class).block();

        assertThat(readResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        String invalidSessionToken = this.getDifferentLSNToken(readResponse.getSessionToken(), 2000);

        {
            // Batch without Read operation
            TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
            TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingA);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);
            TestDoc testDocToUpsert = this.populateTestDoc(this.partitionKey1);

            CosmosBatch batch = CosmosBatch.createCosmosBatch(this.getPartitionKey(this.partitionKey1));
            batch.createItemOperation(testDocToCreate);
            batch.replaceItemOperation(testDocToReplace.getId(), testDocToReplace);
            batch.upsertItemOperation(testDocToUpsert);
            batch.deleteItemOperation(this.TestDocPk1ExistingC.getId());

            CosmosBatchResponse batchResponse = container.executeCosmosBatch(
                batch, new CosmosBatchRequestOptions().setSessionToken(invalidSessionToken)).block();

            this.verifyBatchProcessed(batchResponse, 4);

            assertThat(batchResponse.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(batchResponse.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            assertThat(batchResponse.getResults().get(2).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(batchResponse.getResults().get(3).getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());

            List<CosmosItemOperation> batchOperations = batch.getOperations();
            for (int index = 0; index < batchOperations.size(); index++) {
                assertThat(batchResponse.getResults().get(index).getOperation()).isEqualTo(batchOperations.get(index));
            }
        }

        {
            // Batch with Read operation
            TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
            TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingB);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);
            TestDoc testDocToUpsert = this.populateTestDoc(this.partitionKey1);

            CosmosBatch batch = CosmosBatch.createCosmosBatch(this.getPartitionKey(this.partitionKey1));
            batch.createItemOperation(testDocToCreate);
            batch.replaceItemOperation(testDocToReplace.getId(), testDocToReplace);
            batch.upsertItemOperation(testDocToUpsert);
            batch.deleteItemOperation(this.TestDocPk1ExistingD.getId());
            batch.readItemOperation(this.TestDocPk1ExistingA.getId());

            try {
                container.executeCosmosBatch(
                    batch, new CosmosBatchRequestOptions().setSessionToken(invalidSessionToken)).block();

                Assertions.fail("Should throw NOT_FOUND/READ_SESSION_NOT_AVAILABLE exception");
            } catch (CosmosException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.NOT_FOUND.code());
                assertThat(ex.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
            }
        }
    }
}
