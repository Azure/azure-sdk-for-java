// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.models.CosmosItemOperation;
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
    private List<CosmosItemOperation> operations;
    private boolean isAtomicBatch = false;
    private boolean shouldContinueOnError = false;

    /**
     * Initializes a new {@link ServerBatchRequest request} instance.
     *
     * @param maxBodyLength Maximum length allowed for the request body.
     * @param maxOperationCount Maximum number of operations allowed in the request.
     */
    ServerBatchRequest(int maxBodyLength, int maxOperationCount) {
        this.maxBodyLength = maxBodyLength;
        this.maxOperationCount = maxOperationCount;
    }

    /**
     * Adds as many operations as possible from the given list of operations.
     * TODO(rakkuma): Similarly for hybrid row, request needs to be parsed to create a request body in any form.
     * Issue: https://github.com/Azure/azure-sdk-for-java/issues/15856
     *
     * Operations are added in order while ensuring the request body never exceeds {@link #maxBodyLength}.
     *
     * @param operations operations to be added; read-only.
     *
     * @return Any pending operations that were not included in the request.
     */
    final List<CosmosItemOperation> createBodyOfBatchRequest(final List<CosmosItemOperation> operations) {

        checkNotNull(operations, "expected non-null operations");

        int totalSerializedLength = 0;
        int totalOperationCount = 0;

        final ArrayNode arrayNode =  Utils.getSimpleObjectMapper().createArrayNode();

        for(CosmosItemOperation operation : operations) {
            JsonSerializable operationJsonSerializable;

            if (operation instanceof ItemBatchOperation<?>) {
                operationJsonSerializable = ((ItemBatchOperation<?>) operation).serializeOperation();
            } else if (operation instanceof ItemBulkOperation<?, ?>) {
                operationJsonSerializable = ((ItemBulkOperation<?, ?>) operation).serializeOperation();
            } else {
                throw new UnsupportedOperationException("Unknown CosmosItemOperation.");
            }

            int operationSerializedLength = getOperationSerializedLength(operationJsonSerializable);

            if (totalOperationCount != 0 &&
                (totalSerializedLength + operationSerializedLength > this.maxBodyLength || totalOperationCount + 1 > this.maxOperationCount)) {
                // Apply the limit only if at least there is one operation in selected operations.
                break;
            }

            totalSerializedLength += operationSerializedLength;
            totalOperationCount++;

            arrayNode.add(operationJsonSerializable.getPropertyBag());
        }

        // TODO(rakkuma): This should change to byte array later as optimisation.
        // Issue: https://github.com/Azure/azure-sdk-for-java/issues/16112
        this.requestBody = arrayNode.toString();

        this.operations = operations.subList(0, totalOperationCount);
        return operations.subList(totalOperationCount, operations.size());
    }

    public final String getRequestBody() {
        checkState(this.requestBody != null, "expected non-null body");

        return this.requestBody;
    }

    /**
     * Gets the list of {@link CosmosItemOperation operations} in this {@link ServerBatchRequest batch request}.
     *
     * The list returned by this method is unmodifiable.
     *
     * @return the list of {@link CosmosItemOperation operations} in this {@link ServerBatchRequest batch request}.
     */
    public final List<CosmosItemOperation> getOperations() {
        return UnmodifiableList.unmodifiableList(this.operations);
    }

    public boolean isAtomicBatch() {
        return this.isAtomicBatch;
    }

    void setAtomicBatch(boolean atomicBatch) {
        this.isAtomicBatch = atomicBatch;
    }

    public boolean isShouldContinueOnError() {
        return this.shouldContinueOnError;
    }

    void setShouldContinueOnError(boolean shouldContinueOnError) {
        this.shouldContinueOnError = shouldContinueOnError;
    }

    private int getOperationSerializedLength(JsonSerializable operationSerializable) {
        String serializedValue = operationSerializable.toString();

        return serializedValue.codePointCount(0, serializedValue.length());
    }
}
