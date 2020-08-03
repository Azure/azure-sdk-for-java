// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.cosmos.batch.EmulatorTest.BatchTestBase.BATCH_TEST_TIMEOUT;
import static org.testng.AssertJUnit.assertEquals;

public class PartitionKeyBatchResponseTests {

    @Test(groups = {"simple"}, timeOut = BATCH_TEST_TIMEOUT)
    public void statusCodesAreSetThroughResponseAsync() {
        List<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        ItemBatchOperation<?>[] arrayOperations = new ItemBatchOperation<?>[1];

        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Read,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        TransactionalBatchOperationResult<?> transactionalBatchOperationResult = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK);
        transactionalBatchOperationResult.setETag(operation.getId());

        results.add(transactionalBatchOperationResult);

        arrayOperations[0] = operation;

        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createAsync(
            PartitionKey.NONE,
            Arrays.asList(arrayOperations));

        StoreResponse storeResponse = new StoreResponse(
            HttpResponseStatus.OK.code(),
            new ArrayList<>(),
            responseContent.getBytes(StandardCharsets.UTF_8));

        TransactionalBatchResponse batchresponse = TransactionalBatchResponse.fromResponseMessageAsync(
            new RxDocumentServiceResponse(storeResponse),
            batchRequest,
            true).block();

        PartitionKeyRangeBatchResponse response = new PartitionKeyRangeBatchResponse(
            arrayOperations.length,
            batchresponse);

        assertEquals(HttpResponseStatus.OK, response.getResponseStatus());
    }
}
