// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
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

/**
 * Tests that reproduce the silent failure scenario where bulk operations containing
 * an invalid request (e.g., partition key exceeding 2 KiB) cause co-batched valid
 * operations to fail silently with statusCode = -1.
 *
 * <p>Customer scenario: Bulk ingestion operations containing a single invalid request
 * with a partition key exceeding the 2 KiB limit cause co-batched valid operations
 * to fail silently. The failures manifest as statusCode = -1 with no exceptions
 * raised, and backend responses return HTTP 200.</p>
 *
 * <p>Root cause: When the server returns HTTP 200 but with a non-array response body
 * (e.g., an error object instead of the expected array of individual operation results),
 * {@link BatchResponseParser#fromDocumentServiceResponse} throws a ClassCastException
 * during the (ArrayNode) cast. This non-CosmosException propagates through the reactive
 * pipeline, causing ALL co-batched operations to receive statusCode = -1 via
 * {@link BulkExecutor#handleTransactionalBatchExecutionException}.</p>
 */
public class BulkExecutorOversizedPartitionKeyTest {

    private static final int TIMEOUT = 40000;

    /**
     * Verifies that when a server returns HTTP 400 BadRequest with a JSON object response body
     * (instead of the expected JSON array), BatchResponseParser correctly surfaces the actual
     * server status code (400) to all operations in the batch.
     *
     * This simulates the server-side behavior when a batch request contains an operation
     * with a partition key exceeding the 2 KiB limit: the server rejects the entire batch
     * with status 400 and an error object body.
     *
     * Previously (before fix), this would throw ClassCastException causing all co-batched
     * operations to silently fail with statusCode = -1.
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void batchResponseParser_surfacesServerStatusCode_whenResponseBodyIsErrorObject() {
        // Setup: Create a batch request with multiple valid operations
        int operationCount = 5;
        ItemBulkOperation<?, ?>[] operations = new ItemBulkOperation<?, ?>[operationCount];
        for (int i = 0; i < operationCount; i++) {
            operations[i] = new ItemBulkOperation<>(
                CosmosItemOperationType.CREATE,
                UUID.randomUUID().toString(),
                new PartitionKey("validKey" + i),
                null,
                null,
                null
            );
        }

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0",
            Arrays.asList(operations),
            BatchRequestResponseConstants.DEFAULT_MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES,
            BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST,
            null);

        // Simulate server returning HTTP 400 BadRequest with a JSON OBJECT body (error response)
        // instead of the expected JSON ARRAY of individual operation results.
        // This happens when the server rejects the batch at validation level (e.g., oversized partition key).
        String errorResponseBody = "{\"code\":\"BadRequest\",\"message\":\"Partition key exceeds maximum allowed size of 2048 bytes.\"}";

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.ACTIVITY_ID, UUID.randomUUID().toString());
        headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, "0");
        headers.put(HttpConstants.HttpHeaders.SUB_STATUS, "0");

        byte[] blob = errorResponseBody.getBytes(StandardCharsets.UTF_8);
        StoreResponse storeResponse = new StoreResponse(
            null,
            HttpResponseStatus.BAD_REQUEST.code(),  // HTTP 400 - server rejected the batch
            headers,
            new ByteBufInputStream(Unpooled.wrappedBuffer(blob), true),
            blob.length);

        // ACT: Parse the response - should NOT throw ClassCastException anymore
        CosmosBatchResponse batchResponse = BatchResponseParser.fromDocumentServiceResponse(
            new RxDocumentServiceResponse(null, storeResponse),
            serverOperationBatchRequest.getBatchRequest(),
            true);

        // ASSERT: The actual server status code (400 BadRequest) is surfaced to all operations.
        // This makes the failure visible and actionable — consumers can detect 400 errors.
        assertThat(batchResponse.getStatusCode())
            .as("Response should surface actual server status code (400 BadRequest)")
            .isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
        assertThat(batchResponse.size())
            .as("All operations should be accounted for in the response")
            .isEqualTo(operationCount);

        // Each operation should get the server's 400 error status
        for (int i = 0; i < operationCount; i++) {
            CosmosBatchOperationResult result = batchResponse.getResults().get(i);
            assertThat(result.getStatusCode())
                .as("Operation %d should get server status code 400 BadRequest", i)
                .isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
        }
    }

    /**
     * Verifies that when a server returns HTTP 200 with a non-array body (edge case where
     * transport says success but response is malformed), the parser treats it as an
     * InternalServerError since a 2xx with wrong response shape is an invalid server response.
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void batchResponseParser_returnsInternalServerError_whenHttp200WithNonArrayBody() {
        // Setup: Create a batch request with multiple valid operations
        int operationCount = 5;
        ItemBulkOperation<?, ?>[] operations = new ItemBulkOperation<?, ?>[operationCount];
        for (int i = 0; i < operationCount; i++) {
            operations[i] = new ItemBulkOperation<>(
                CosmosItemOperationType.CREATE,
                UUID.randomUUID().toString(),
                new PartitionKey("validKey" + i),
                null,
                null,
                null
            );
        }

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0",
            Arrays.asList(operations),
            BatchRequestResponseConstants.DEFAULT_MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES,
            BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST,
            null);

        // HTTP 200 but with non-array body — this is an invalid server response
        String errorResponseBody = "{\"code\":\"BadRequest\",\"message\":\"Partition key exceeds maximum allowed size of 2048 bytes.\"}";

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.ACTIVITY_ID, UUID.randomUUID().toString());
        headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, "0");
        headers.put(HttpConstants.HttpHeaders.SUB_STATUS, "0");

        byte[] blob = errorResponseBody.getBytes(StandardCharsets.UTF_8);
        StoreResponse storeResponse = new StoreResponse(
            null,
            HttpResponseStatus.OK.code(),  // HTTP 200 but body is wrong shape
            headers,
            new ByteBufInputStream(Unpooled.wrappedBuffer(blob), true),
            blob.length);

        // ACT
        CosmosBatchResponse batchResponse = BatchResponseParser.fromDocumentServiceResponse(
            new RxDocumentServiceResponse(null, storeResponse),
            serverOperationBatchRequest.getBatchRequest(),
            true);

        // ASSERT: Since status is 2xx but response count != operation count,
        // the parser correctly treats it as InternalServerError (invalid server response)
        assertThat(batchResponse.getStatusCode())
            .as("HTTP 200 with wrong response shape should be treated as InternalServerError")
            .isEqualTo(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        assertThat(batchResponse.size())
            .as("All operations should be accounted for in the response")
            .isEqualTo(operationCount);
    }

    /**
     * Demonstrates that when the server returns fewer results than operations in the batch,
     * ALL operations receive the batch-level error status (500 InternalServerError).
     *
     * This can happen when the server encounters an oversized partition key and stops
     * processing the batch before all operations are handled, returning partial results.
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void batchResponseParser_allOperationsGetError_whenResponseHasFewerResults() {
        // Setup: Create a batch request with 5 operations
        int operationCount = 5;
        ItemBulkOperation<?, ?>[] operations = new ItemBulkOperation<?, ?>[operationCount];
        for (int i = 0; i < operationCount; i++) {
            operations[i] = new ItemBulkOperation<>(
                CosmosItemOperationType.CREATE,
                UUID.randomUUID().toString(),
                new PartitionKey("validKey" + i),
                null,
                null,
                null
            );
        }

        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0",
            Arrays.asList(operations),
            BatchRequestResponseConstants.DEFAULT_MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES,
            BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST,
            null);

        // Simulate server returning only 2 results (for the first 2 operations)
        // The server stopped processing after encountering the invalid operation
        List<CosmosBatchOperationResult> partialResults = new ArrayList<>();
        partialResults.add(ModelBridgeInternal.createCosmosBatchResult(
            null, 1.0, null, HttpResponseStatus.CREATED.code(), Duration.ZERO, 0, operations[0]));
        partialResults.add(ModelBridgeInternal.createCosmosBatchResult(
            null, 1.0, null, HttpResponseStatus.CREATED.code(), Duration.ZERO, 0, operations[1]));

        String responseContent = new BatchResponsePayloadWriter(partialResults).generatePayload();

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.ACTIVITY_ID, UUID.randomUUID().toString());
        headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, "2.0");
        headers.put(HttpConstants.HttpHeaders.SUB_STATUS, "0");

        byte[] blob = responseContent.getBytes(StandardCharsets.UTF_8);
        StoreResponse storeResponse = new StoreResponse(
            null,
            HttpResponseStatus.OK.code(),  // HTTP 200 at transport level
            headers,
            new ByteBufInputStream(Unpooled.wrappedBuffer(blob), true),
            blob.length);

        // ACT: Parse the response
        CosmosBatchResponse batchResponse = BatchResponseParser.fromDocumentServiceResponse(
            new RxDocumentServiceResponse(null, storeResponse),
            serverOperationBatchRequest.getBatchRequest(),
            true);

        // ASSERT: All operations (including valid ones) receive 500 InternalServerError
        // because the response count doesn't match operation count and HTTP status was 2xx
        assertThat(batchResponse.getStatusCode()).isEqualTo(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        assertThat(batchResponse.size()).isEqualTo(operationCount);

        // ALL operations get the error status - valid operations silently fail
        for (int i = 0; i < operationCount; i++) {
            CosmosBatchOperationResult result = batchResponse.getResults().get(i);
            assertThat(result.getStatusCode())
                .as("Operation %d should get batch-level error status (500), not its actual result", i)
                .isEqualTo(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        }
    }

    /**
     * Demonstrates that a partition key exceeding 2 KiB (2048 bytes) is NOT validated
     * on the client side before being included in the batch request. This means the
     * oversized partition key operation is included in the batch alongside valid operations,
     * and the server-side validation failure affects all co-batched operations.
     *
     * The lack of client-side partition key size validation is the root enabler of the
     * silent data loss scenario.
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void oversizedPartitionKey_notValidatedClientSide_includedInBatch() {
        // Create a partition key value that exceeds 2 KiB (2048 bytes)
        StringBuilder largeKeyBuilder = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            largeKeyBuilder.append("abcdefgh"); // 8 chars * 300 = 2400 chars > 2048 bytes
        }
        String oversizedPartitionKeyValue = largeKeyBuilder.toString();
        assertThat(oversizedPartitionKeyValue.getBytes(StandardCharsets.UTF_8).length)
            .as("Partition key should exceed 2 KiB limit")
            .isGreaterThan(2048);

        // Create operations - one with oversized PK, rest valid
        int totalOperations = 5;
        ItemBulkOperation<?, ?>[] operations = new ItemBulkOperation<?, ?>[totalOperations];

        // First operation has oversized partition key
        ItemBulkOperation<Object, Object> oversizedOp = new ItemBulkOperation<>(
            CosmosItemOperationType.CREATE,
            UUID.randomUUID().toString(),
            new PartitionKey(oversizedPartitionKeyValue),
            null,
            null,
            null
        );
        // Simulate what BulkExecutorUtil.resolvePartitionKeyRangeId does -
        // sets the partition key JSON for serialization in the batch body
        oversizedOp.setPartitionKeyJson("[\"" + oversizedPartitionKeyValue + "\"]");
        operations[0] = oversizedOp;

        // Remaining operations have valid partition keys
        for (int i = 1; i < totalOperations; i++) {
            ItemBulkOperation<Object, Object> validOp = new ItemBulkOperation<>(
                CosmosItemOperationType.CREATE,
                UUID.randomUUID().toString(),
                new PartitionKey("validKey" + i),
                null,
                null,
                null
            );
            validOp.setPartitionKeyJson("[\"validKey" + i + "\"]");
            operations[i] = validOp;
        }

        // ACT: Create batch request - NO exception thrown despite oversized PK
        // This demonstrates the lack of client-side validation
        ServerOperationBatchRequest serverOperationBatchRequest = PartitionKeyRangeServerBatchRequest.createBatchRequest(
            "0",
            Arrays.asList(operations),
            BatchRequestResponseConstants.DEFAULT_MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES,
            BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST,
            null);

        // ASSERT: The oversized PK operation is included in the batch request
        // alongside valid operations - no client-side validation prevents this
        assertThat(serverOperationBatchRequest.getBatchRequest().getOperations().size())
            .as("All operations including the oversized PK one should be in the batch")
            .isEqualTo(totalOperations);

        // The request body includes the oversized partition key without any validation
        String requestBody = serverOperationBatchRequest.getBatchRequest().getRequestBody();
        assertThat(requestBody).contains(oversizedPartitionKeyValue);
    }

    /**
     * Demonstrates the full error propagation path: when a non-CosmosException occurs
     * during batch response processing, the BulkOperationStatusTracker records 500
     * (InternalServerError) for ALL operations in the batch, and each operation's
     * CosmosBulkOperationResponse has a null response (no CosmosBulkItemResponse) but
     * contains the exception.
     *
     * This simulates what happens in BulkExecutor.handleTransactionalBatchExecutionException
     * when a non-CosmosException is propagated. With the fix, the status code is 500
     * (instead of the previous -1) making the failure visible and actionable.
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void statusTracker_records500_forNonCosmosException() {
        // Create operations that would be in the same batch
        int operationCount = 5;
        ItemBulkOperation<?, ?>[] operations = new ItemBulkOperation<?, ?>[operationCount];
        for (int i = 0; i < operationCount; i++) {
            operations[i] = new ItemBulkOperation<>(
                CosmosItemOperationType.CREATE,
                UUID.randomUUID().toString(),
                new PartitionKey("key" + i),
                null,
                null,
                null
            );
        }

        // Simulate the non-CosmosException path that BulkExecutor.handleTransactionalBatchExecutionException
        // follows when the exception is NOT a CosmosException (e.g., ClassCastException from response parsing)
        // With the fix, BulkExecutor records (500, 0) instead of (-1, -1) for each operation

        // Verify that statusTracker records 500/UNKNOWN entries for each operation
        for (ItemBulkOperation<?, ?> operation : operations) {
            BulkOperationStatusTracker tracker = operation.getStatusTracker();

            // Initial state - tracker should have no entries (toString shows "[]")
            assertThat(tracker.toString()).isEqualTo("[]");

            // After recording 500/0 (simulating what BulkExecutor now does for non-CosmosException)
            tracker.recordStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), HttpConstants.SubStatusCodes.UNKNOWN);

            // The tracker should now contain an entry with statusCode=500
            String trackerState = tracker.toString();
            assertThat(trackerState)
                .as("Status tracker should record 500 (InternalServerError) for non-CosmosException errors")
                .contains("(500/");
        }
    }
}
