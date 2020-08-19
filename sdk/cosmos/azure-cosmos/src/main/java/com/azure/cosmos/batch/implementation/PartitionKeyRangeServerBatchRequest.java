// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class PartitionKeyRangeServerBatchRequest extends ServerBatchRequest {

    private final String partitionKeyRangeId;

    /**
     * Initializes a new instance of the {@link PartitionKeyRangeServerBatchRequest} class.
     *
     * @param partitionKeyRangeId The partition key range id associated with all requests.
     * @param maxBodyLength Maximum length allowed for the request body.
     * @param maxOperationCount Maximum number of operations allowed in the request.
     */
    PartitionKeyRangeServerBatchRequest(
        final String partitionKeyRangeId,
        int maxBodyLength,
        int maxOperationCount) {

        super(maxBodyLength, maxOperationCount);
        checkNotNull(partitionKeyRangeId, "expected non-null partitionKeyRangeId");
        this.partitionKeyRangeId = partitionKeyRangeId;
    }

    /**
     * Creates an instance of {@link PartitionKeyRangeServerBatchRequest}. In case of direct mode requests, all the
     * operations are expected to belong to the same PartitionKeyRange. The body of the request is populated with
     * operations till it reaches the provided maxBodyLength.
     *
     * @param partitionKeyRangeId The partition key range id associated with all requests.
     * @param operations Operations to be added into this batch request.
     * @param maxBodyLength Desired maximum length of the request body.
     * @param maxOperationCount Maximum number of operations allowed in the request.
     * @param ensureContinuousOperationIndexes Whether to stop adding operations to the request once there is
     * non-continuity in the operation indexes.
     *
     * @return A newly created instance of {@link PartitionKeyRangeServerBatchRequest} and the overflow
     * ItemBatchOperation not being processed.
     */
    public static CompletableFuture<ServerOperationBatchRequest> createAsync(
        final String partitionKeyRangeId,
        final List<ItemBatchOperation<?>> operations,
        final int maxBodyLength,
        final int maxOperationCount,
        final boolean ensureContinuousOperationIndexes) {

        final PartitionKeyRangeServerBatchRequest request = new PartitionKeyRangeServerBatchRequest(
            partitionKeyRangeId,
            maxBodyLength,
            maxOperationCount);

        request.setAtomicBatch(false);
        request.setShouldContinueOnError(true);

        return CompletableFuture.completedFuture(request.createBodyStreamAsync(operations, ensureContinuousOperationIndexes))
                .thenApplyAsync(pendingOperations -> new ServerOperationBatchRequest(request, pendingOperations));
    }

    /**
     * Gets the PartitionKeyRangeId that applies to all operations in this request.
     *
     * @return PartitionKeyRangeId that applies to all operations in this request.
     */
    public String getPartitionKeyRangeId() {
        return this.partitionKeyRangeId;
    }
}
