// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

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
        CosmosAsyncContainer container = this.batchAsyncContainer;
        this.createJsonTestDocs(container);

        TestDoc firstDoc = this.populateTestDoc(this.partitionKey1);
        TestDoc replaceDoc = this.getTestDocCopy(firstDoc);
        replaceDoc.setCost(replaceDoc.getCost() + 1);

        Mono<TransactionalBatchResponse> batchResponseMono = batchAsyncContainer.executeTransactionalBatch(
            TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                .createItem(firstDoc)
                .replaceItem(replaceDoc.getId(), replaceDoc));

        TransactionalBatchResponse batchResponse1 = batchResponseMono.block();
        this.verifyBatchProcessed(batchResponse1, 2);

        assertThat(batchResponse1.get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse1.get(1).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

        // Block again.
        TransactionalBatchResponse batchResponse2 = batchResponseMono.block();
        assertThat(batchResponse2.getStatusCode()).isEqualTo(HttpResponseStatus.CONFLICT.code());
        assertThat(batchResponse2.get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CONFLICT.code());
        assertThat(batchResponse2.get(1).getStatusCode()).isEqualTo(HttpResponseStatus.FAILED_DEPENDENCY.code());
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

            TransactionalBatchResponse batchResponse = container.executeTransactionalBatch(
                TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                    .createItem(testDocToCreate)
                    .replaceItem(testDocToReplace.getId(), testDocToReplace)
                    .upsertItem(testDocToUpsert)
                    .deleteItem(this.TestDocPk1ExistingC.getId()), new TransactionalBatchRequestOptions().setSessionToken(invalidSessionToken)).block();

            this.verifyBatchProcessed(batchResponse, 4);

            assertThat(batchResponse.get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(batchResponse.get(1).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            assertThat(batchResponse.get(2).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(batchResponse.get(3).getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
        }

        {
            // Batch with Read operation
            TestDoc testDocToCreate = this.populateTestDoc(this.partitionKey1);
            TestDoc testDocToReplace = this.getTestDocCopy(this.TestDocPk1ExistingB);
            testDocToReplace.setCost(testDocToReplace.getCost() + 1);
            TestDoc testDocToUpsert = this.populateTestDoc(this.partitionKey1);

            try {
                container.executeTransactionalBatch(
                    TransactionalBatch.createTransactionalBatch(this.getPartitionKey(this.partitionKey1))
                        .createItem(testDocToCreate)
                        .replaceItem(testDocToReplace.getId(), testDocToReplace)
                        .upsertItem(testDocToUpsert)
                        .deleteItem(this.TestDocPk1ExistingD.getId())
                        .readItem(this.TestDocPk1ExistingA.getId()), new TransactionalBatchRequestOptions().setSessionToken(invalidSessionToken)).block();

                Assertions.fail("Should throw NOT_FOUND/READ_SESSION_NOT_AVAILABLE exception");
            } catch (CosmosException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.NOT_FOUND.code());
                assertThat(ex.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
            }
        }
    }
}
