// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;

/**
 * This class represents a server batch request.
 */
public abstract class ServerBatchRequest {

    private final int maxBodyLength;
    private final int maxOperationCount;

    private String requestBody;
    private List<ItemBatchOperation<?>> operations;
    private boolean isAtomicBatch = false;
    private boolean shouldContinueOnError = false;

    /**
     * Initializes a new {@link ServerBatchRequest request} instance.
     *
     * @param maxBodyLength Maximum length allowed for the request body.
     * @param maxOperationCount Maximum number of operations allowed in the request.
     */
    protected ServerBatchRequest(int maxBodyLength, int maxOperationCount) {
        this.maxBodyLength = maxBodyLength;
        this.maxOperationCount = maxOperationCount;
    }

    /**
     * Adds as many operations as possible from the given list of operations.
     * <p>
     * Operations are added in order while ensuring the request stream never exceeds {@link #maxBodyLength}.
     *
     * @param operations Operations to be added; read-only.
     *
     * @return Any pending operations that were not included in the request.
     */
    public final List<ItemBatchOperation<?>> createBodyStreamAsync(
        final List<ItemBatchOperation<?>> operations) {
        return createBodyStreamAsync(operations, false);
    }

    /**
     * Adds as many operations as possible from the given list of operations.
     * TODO(rakkuma): Similarly for hybrid row, request needs to be parsed to create a request body in any form.
     *
     * <p>
     * Operations are added in order while ensuring the request body never exceeds {@link #maxBodyLength}.
     *
     * @param operations operations to be added; read-only.
     * @param ensureContinuousOperationIndexes specifies whether to stop adding operations to the request once there is
     * non-continuity in the operation indexes.
     *
     * @return Any pending operations that were not included in the request.
     */
    protected final List<ItemBatchOperation<?>> createBodyStreamAsync(
        final List<ItemBatchOperation<?>> operations,
        final boolean ensureContinuousOperationIndexes) {

        checkNotNull(operations, "expected non-null operations");

        int totalSerializedLength = 0;
        int totalOperationCount = 0;

        ArrayNode arrayNode =  Utils.getSimpleObjectMapper().createArrayNode();

        for(ItemBatchOperation<?> operation : operations) {

            operation.materializeResource();
            JsonSerializable operationJsonSerializable = ItemBatchOperation.writeOperation(operation);

            if (totalSerializedLength + operationJsonSerializable.toString().length() > this.maxBodyLength || totalOperationCount + 1 > this.maxOperationCount) {
                break;
            }

            totalSerializedLength += operationJsonSerializable.toString().length();
            totalOperationCount++;

            arrayNode.add(operationJsonSerializable.getPropertyBag());
        }

        this.requestBody = arrayNode.toString();
        this.operations = operations.subList(0, totalOperationCount);
        List<ItemBatchOperation<?>> pendingOperations = operations.subList(totalOperationCount, operations.size());
        return pendingOperations;
    }

    public final String transferRequestBody() {

        checkState(this.requestBody != null, "expected non-null body stream");

        String requestBody = this.requestBody;
        this.requestBody = null;

        return requestBody;
    }

    /**
     * Gets the list of {@link ItemBatchOperation operations} in this {@link ServerBatchRequest batch request}.
     *
     * The list returned by this method is unmodifiable.
     *
     * @return the list of {@link ItemBatchOperation operations} in this {@link ServerBatchRequest batch request}.
     */
    public final List<ItemBatchOperation<?>> getOperations() {
        return UnmodifiableList.unmodifiableList(this.operations);
    }

    public boolean isAtomicBatch() {
        return isAtomicBatch;
    }

    public void setAtomicBatch(boolean atomicBatch) {
        isAtomicBatch = atomicBatch;
    }

    public boolean isShouldContinueOnError() {
        return shouldContinueOnError;
    }

    public void setShouldContinueOnError(boolean shouldContinueOnError) {
        this.shouldContinueOnError = shouldContinueOnError;
    }
}
