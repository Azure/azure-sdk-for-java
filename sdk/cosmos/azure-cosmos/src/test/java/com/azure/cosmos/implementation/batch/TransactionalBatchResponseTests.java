// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.TransactionalBatchResponse;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.models.PartitionKey;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionalBatchResponseTests {

    private static final int TIMEOUT = 40000;

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void validateAllSetValuesInResponse() {
        List<TransactionalBatchOperationResult> results = new ArrayList<>();
        ItemBatchOperation<?>[] arrayOperations = new ItemBatchOperation<?>[1];

        ItemBatchOperation<?> operation = new ItemBatchOperation<>(
            CosmosItemOperationType.READ,
            "0",
            PartitionKey.NONE,
            null,
            null
        );

        arrayOperations[0] = operation;
        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createBatchRequest(
            PartitionKey.NONE,
            Arrays.asList(arrayOperations));

        // Create dummy result
        TransactionalBatchOperationResult transactionalBatchOperationResult = BridgeInternal.createTransactionBatchResult(
            operation.getId(),
            5.0,
            null,
            HttpResponseStatus.NOT_MODIFIED.code(),
            Duration.ofMillis(100),
            HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH,
            operation
        );

        results.add(transactionalBatchOperationResult);
        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        // TransactionalBatchResponse headers
        String activityId = UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.ACTIVITY_ID, activityId);
        headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, "4.5");
        headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, "token123");
        headers.put(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, "1234");
        headers.put(HttpConstants.HttpHeaders.SUB_STATUS, String.valueOf(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE));

        StoreResponse storeResponse = new StoreResponse(
            HttpResponseStatus.OK.code(),
            new ArrayList<>(headers.entrySet()),
            responseContent.getBytes(StandardCharsets.UTF_8));

        TransactionalBatchResponse batchResponse = BatchResponseParser.fromDocumentServiceResponse(
            new RxDocumentServiceResponse(null, storeResponse),
            batchRequest,
            true);

        // Validate response fields
        assertThat(batchResponse.getActivityId()).isEqualTo(activityId);
        assertThat(batchResponse.getRequestCharge()).isEqualTo(4.5);
        assertThat(batchResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.getSessionToken()).isEqualTo("token123");
        assertThat(batchResponse.getResponseHeaders()).isEqualTo(headers);
        assertThat(batchResponse.getRetryAfterDuration()).isEqualTo(Duration.ofMillis(1234));
        assertThat(batchResponse.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);

        // Validate result fields
        assertThat(batchResponse.getResults().get(0).getETag()).isEqualTo(operation.getId());
        assertThat(batchResponse.getResults().get(0).getRequestCharge()).isEqualTo(5.0);
        assertThat(batchResponse.getResults().get(0).getRetryAfterDuration()).isEqualTo(Duration.ofMillis(100));
        assertThat(batchResponse.getResults().get(0).getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH);
        assertThat(batchResponse.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.NOT_MODIFIED.code());
        assertThat(batchResponse.getResults().get(0).getOperation()).isEqualTo(operation);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void validateEmptyHeaderInResponse() {
        List<TransactionalBatchOperationResult> results = new ArrayList<>();
        ItemBatchOperation<?>[] arrayOperations = new ItemBatchOperation<?>[1];

        ItemBatchOperation<?> operation = new ItemBatchOperation<>(
            CosmosItemOperationType.READ,
            "0",
            PartitionKey.NONE,
            null,
            null
        );

        arrayOperations[0] = operation;
        SinglePartitionKeyServerBatchRequest batchRequest = SinglePartitionKeyServerBatchRequest.createBatchRequest(
            PartitionKey.NONE,
            Arrays.asList(arrayOperations));

        // Create dummy result
        TransactionalBatchOperationResult transactionalBatchOperationResult = BridgeInternal.createTransactionBatchResult(
            null,
            5.0,
            null,
            HttpResponseStatus.NOT_MODIFIED.code(),
            null,
            0,
            operation
        );

        results.add(transactionalBatchOperationResult);
        String responseContent = new BatchResponsePayloadWriter(results).generatePayload();

        StoreResponse storeResponse = new StoreResponse(
            HttpResponseStatus.OK.code(),
            new ArrayList<>(),
            responseContent.getBytes(StandardCharsets.UTF_8));

        TransactionalBatchResponse batchResponse = BatchResponseParser.fromDocumentServiceResponse(
            new RxDocumentServiceResponse(null, storeResponse),
            batchRequest,
            true);

        // Validate response fields
        assertThat(batchResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.getActivityId()).isNull();
        assertThat(batchResponse.getRequestCharge()).isEqualTo(0);
        assertThat(batchResponse.getSessionToken()).isNull();
        assertThat(batchResponse.getResponseHeaders()).isEmpty();
        assertThat(batchResponse.getRetryAfterDuration()).isEqualTo(Duration.ZERO);
        assertThat(batchResponse.getSubStatusCode()).isEqualTo(0);

        // Validate result fields
        assertThat(batchResponse.getResults().get(0).getETag()).isNull();
        assertThat(batchResponse.getResults().get(0).getRequestCharge()).isEqualTo(5.0);
        assertThat(batchResponse.getResults().get(0).getRetryAfterDuration()).isEqualTo(Duration.ZERO);
        assertThat(batchResponse.getResults().get(0).getSubStatusCode()).isEqualTo(0);
        assertThat(batchResponse.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.NOT_MODIFIED.code());
        assertThat(batchResponse.getResults().get(0).getOperation()).isEqualTo(operation);
    }
}
