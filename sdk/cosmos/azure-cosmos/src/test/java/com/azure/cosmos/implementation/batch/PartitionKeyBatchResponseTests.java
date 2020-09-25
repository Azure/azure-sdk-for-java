// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.TransactionalBatchResponse;
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

import static org.testng.AssertJUnit.assertEquals;

public class PartitionKeyBatchResponseTests {

    private static final int TIMEOUT = 40000;

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void statusCodesAreSetThroughResponseAsync() {
        List<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        ItemBatchOperation<?>[] arrayOperations = new ItemBatchOperation<?>[1];

        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Read,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        TransactionalBatchOperationResult<?> transactionalBatchOperationResult = new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK.code());
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

        TransactionalBatchResponse batchresponse = BatchResponseParser.fromDocumentServiceResponseAsync(
            new RxDocumentServiceResponse(storeResponse),
            batchRequest,
            true).block();

        PartitionKeyRangeBatchResponse response = new PartitionKeyRangeBatchResponse(
            arrayOperations.length,
            batchresponse);

        assertEquals(HttpResponseStatus.OK.code(), response.getResponseStatus());
    }
}
