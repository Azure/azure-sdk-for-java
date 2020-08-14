// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class PartitionKeyRangeBatchExecutionResultTests {

    private static final int TIMEOUT = 40000;

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void containsSplitIsTrue() {
        assertTrue (this.containsSplitIsTrueInternal(HttpResponseStatus.GONE, HttpConstants.SubStatusCodes.COMPLETING_SPLIT));
        assertTrue(this.containsSplitIsTrueInternal(HttpResponseStatus.GONE, HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION));
        assertTrue(this.containsSplitIsTrueInternal(HttpResponseStatus.GONE, HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE));
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void containsSplitIsFalse() {
        assertFalse(this.containsSplitIsTrueInternal(HttpResponseStatus.OK, HttpConstants.SubStatusCodes.UNKNOWN));
        assertFalse(this.containsSplitIsTrueInternal(HttpResponseStatus.TOO_MANY_REQUESTS, HttpConstants.SubStatusCodes.UNKNOWN));
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void statusCodesAreSetThroughResponseAsync() {
        List<TransactionalBatchOperationResult<?>> results = new ArrayList<TransactionalBatchOperationResult<?>>();
        List<ItemBatchOperation<?>> arrayOperations = new ArrayList<>();

        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Read,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        results.add(new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK));

        arrayOperations.add(operation);

        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createAsync(
            PartitionKey.NONE,
            arrayOperations);

        StoreResponse storeResponse = new StoreResponse(
            HttpResponseStatus.OK.code(),
            new ArrayList<>(),
            responseContent.getBytes(StandardCharsets.UTF_8));

        TransactionalBatchResponse batchresponse = TransactionalBatchResponse.fromResponseMessageAsync(
            new RxDocumentServiceResponse(storeResponse),
            batchRequest,
            true).block();

        PartitionKeyRangeBatchResponse response = new PartitionKeyRangeBatchResponse(
            arrayOperations.size(),
            batchresponse);
        assertEquals(HttpResponseStatus.OK, response.getResponseStatus());
    }

    private boolean containsSplitIsTrueInternal(HttpResponseStatus statusCode, int subStatusCode) {
        List<TransactionalBatchOperationResult<?>> results = new ArrayList<>();
        List<ItemBatchOperation<?>> arrayOperations = new ArrayList<>();

        ItemBatchOperation<?> operation = new ItemBatchOperation.Builder<Object>(OperationType.Read,0)
            .partitionKey(PartitionKey.NONE)
            .id("0")
            .build();

        results.add(new TransactionalBatchOperationResult<Object>(HttpResponseStatus.OK));

        arrayOperations.add(operation);

        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createAsync(
            PartitionKey.NONE,
            arrayOperations);

        StoreResponseBuilder storeResponseBuilder = new StoreResponseBuilder();
        storeResponseBuilder.withStatus(statusCode.code());
        storeResponseBuilder.withHeader(WFConstants.BackendHeaders.SUB_STATUS, String.valueOf(subStatusCode));
        storeResponseBuilder.withContent(responseContent);

        RxDocumentServiceResponse response = new RxDocumentServiceResponse(storeResponseBuilder.build());

        TransactionalBatchResponse batchresponse = TransactionalBatchResponse.fromResponseMessageAsync(
            response,
            batchRequest,
            true).block();

        PartitionKeyRangeBatchExecutionResult result = new PartitionKeyRangeBatchExecutionResult("0", arrayOperations, batchresponse);

        return result.isSplit();
    }

}
