// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.batch.hybridrow.HybridRowBatchCodec;
import com.azure.cosmos.models.CosmosItemOperation;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private byte[] hybridRowRequestBody;
    private boolean hybridRow;
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
     *
     * Operations are added in order while ensuring the encoded request body never exceeds
     * {@link #maxBodyLength}.
     *
     * @param operations operations to be added; read-only.
     *
     * @return Any pending operations that were not included in the request.
     */
    final List<CosmosItemOperation> createBodyOfBatchRequest(
        final List<CosmosItemOperation> operations,
        final CosmosItemSerializer effectiveItemSerializer,
        final boolean hybridRow) {

        checkNotNull(operations, "expected non-null operations");

        int totalSerializedLength = hybridRow ? 10 : 0;
        int totalOperationCount = 0;
        List<byte[]> hybridRowOperations = hybridRow ? new ArrayList<>() : null;

        final ArrayNode arrayNode = hybridRow ? null : Utils.getSimpleObjectMapper().createArrayNode();

        for(CosmosItemOperation operation : operations) {
            JsonSerializable operationJsonSerializable;
            int operationSerializedLength;
            byte[] hybridRowOperation = null;

            if (operation instanceof CosmosItemOperationBase) {
                if (hybridRow) {
                    operationJsonSerializable = null;
                    hybridRowOperation = HybridRowBatchMapper.encodeOperation(operation, effectiveItemSerializer);
                    operationSerializedLength = hybridRowOperation.length + 13;
                } else {
                    operationJsonSerializable =
                        ((CosmosItemOperationBase) operation).getSerializedOperation(effectiveItemSerializer);
                    operationSerializedLength = ((CosmosItemOperationBase) operation)
                        .getSerializedLength(effectiveItemSerializer);
                }
            } else {
                throw new UnsupportedOperationException("Unknown CosmosItemOperation.");
            }

            if (totalOperationCount != 0 &&
                (totalSerializedLength + operationSerializedLength > this.maxBodyLength || totalOperationCount + 1 > this.maxOperationCount)) {
                // Apply the limit only if at least there is one operation in selected operations.
                break;
            }

            totalSerializedLength += operationSerializedLength;
            totalOperationCount++;

            if (hybridRow) {
                hybridRowOperations.add(hybridRowOperation);
            } else {
                arrayNode.add(operationJsonSerializable.getPropertyBag());
            }
        }

        // TODO(rakkuma): The JSON path should change to byte array later as optimisation.
        // Issue: https://github.com/Azure/azure-sdk-for-java/issues/16112
        this.requestBody = hybridRow ? null : arrayNode.toString();

        this.operations = operations.subList(0, totalOperationCount);
        this.hybridRow = hybridRow;
        if (hybridRow) {
            this.hybridRowRequestBody = HybridRowBatchCodec.encodeRecordIo(hybridRowOperations);
        }
        return operations.subList(totalOperationCount, operations.size());
    }

    public final byte[] getRequestBody() {
        if (hybridRow) {
            checkState(this.hybridRowRequestBody != null, "expected non-null HybridRow body");
            return this.hybridRowRequestBody.clone();
        }
        checkState(this.requestBody != null, "expected non-null JSON body");
        return this.requestBody.getBytes(StandardCharsets.UTF_8);
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
}
