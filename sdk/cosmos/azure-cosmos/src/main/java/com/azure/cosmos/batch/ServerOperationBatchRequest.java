// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ServerOperationBatchRequest {
    private final PartitionKeyRangeServerBatchRequest batchRequest;
    private final List<ItemBatchOperation<?>> pendingOperations;

    /**
     * Creates a new pair
     * @param batchRequest the {@link ServerBatchRequest batch request}
     * @param operations the {@link List list} of {@link ItemBatchOperation pendingOperations} for the batch request.
     */
    ServerOperationBatchRequest(
        final PartitionKeyRangeServerBatchRequest batchRequest,
        final List<ItemBatchOperation<?>> operations) {

        checkNotNull(batchRequest, "expected non-null batchRequest");
        checkNotNull(operations, "expected non-null pendingOperations");

        this.batchRequest = batchRequest;
        this.pendingOperations = operations;
    }

    /**
     * Gets the PartitionKeyRangeServerBatchRequest.
     * @return key for this pair
     */
    public PartitionKeyRangeServerBatchRequest getBatchRequest() {
        return this.batchRequest;
    }

    /**
     * Gets list of ItemBatchOperation.
     * @return value for this pair
     */
    public List<ItemBatchOperation<?>> getBatchPendingOperations() {
        return this.pendingOperations;
    }

    /**
     * {@link String} representation of this {@link ServerOperationBatchRequest}.
     *
     *  @return {@link String} representation of this {@link ServerOperationBatchRequest}.
     */
    @Override
    public String toString() {
        return batchRequest + "=" + pendingOperations;
    }

    /**
     * Calculates a hash code for this {@link ServerOperationBatchRequest}.
     *
     * @return hash code for this {@link ServerOperationBatchRequest}.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (batchRequest != null ? batchRequest.hashCode() : 0);
        hash = 31 * hash + (pendingOperations != null ? pendingOperations.hashCode() : 0);
        return hash;
    }

    /**
     * Tests this {@link ServerOperationBatchRequest} for equality with another {@link Object}.
     * <p>
     * Two {@link ServerOperationBatchRequest} instances are considered equal if and only if both the batch request and
     * operation lists are equal.
     *
     * @param other the {@link Object} to test for equality with this {@link ServerOperationBatchRequest}.
     *
     * @return {@code true} if the given {@link Object} is equal to this {@link ServerOperationBatchRequest};
     * {@code false} otherwise.
     */
    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ServerOperationBatchRequest that = (ServerOperationBatchRequest) other;

        if (!batchRequest.equals(that.batchRequest)) {
            return false;
        }

        return pendingOperations.equals(that.pendingOperations);
    }
}
