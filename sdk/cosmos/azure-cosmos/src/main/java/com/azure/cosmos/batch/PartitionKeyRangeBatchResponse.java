// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.Utils;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Response of a cross partition key batch request.
 */
public class PartitionKeyRangeBatchResponse extends TransactionalBatchResponse {

    // Results sorted in the order operations had been added.
    private final TransactionalBatchOperationResult<?>[] resultsByOperationIndex;
    private final TransactionalBatchResponse serverResponse;

    /**
     * Initializes a new instance of the {@link PartitionKeyRangeBatchResponse} class.
     *
     * @param originalOperationsCount Original operations that generated the server responses.
     * @param serverResponse Response from the server.
     */
    public PartitionKeyRangeBatchResponse(
        final int originalOperationsCount,
        final TransactionalBatchResponse serverResponse) {

        super(
            serverResponse.getResponseStatus(),
            serverResponse.getSubStatusCode(),
            serverResponse.getErrorMessage(),
            serverResponse.getResponseHeaders(),
            serverResponse.getCosmosDiagnostics(),
            serverResponse.getBatchOperations());

        this.serverResponse = serverResponse;
        this.resultsByOperationIndex = new TransactionalBatchOperationResult<?>[originalOperationsCount];

        // We expect number of results == number of operations here
        for (int index = 0; index < serverResponse.getBatchOperations().size(); index++) {

            final int operationIndex = serverResponse.getBatchOperations().get(index).getOperationIndex();
            final TransactionalBatchOperationResult<?> result = this.resultsByOperationIndex[operationIndex];

            if (result == null || result.getResponseStatus() == HttpResponseStatus.TOO_MANY_REQUESTS) {
                this.resultsByOperationIndex[operationIndex] = serverResponse.get(index);
            }
        }
    }

    /**
     * Gets the result of the operation at the provided index in the batch - the returned result has a Resource of
     * provided type.
     *
     * @param <T> the type parameter.
     * @param index 0-based index of the operation in the batch whose result needs to be returned.
     * de-serialized, when present.
     * @param type class type for which deserialization is needed.
     *
     * @return TransactionalBatchOperationResult containing the individual result of operation.
     * @throws IOException if the body of the resource stream cannot be read.
     */
    @Override
    public <T> TransactionalBatchOperationResult<T> getOperationResultAtIndex(
        final int index,
        final Class<T> type) throws IOException {

        checkArgument(0 <= index && index < this.size(), "expected index in range [0, %s), not %s",
            this.size(),
            index);

        checkNotNull(type, "expected non-null type");

        TransactionalBatchOperationResult<?> result = this.resultsByOperationIndex[index];

        T resource = null;
        if (result.getResourceObject() != null) {
            resource = Utils.getSimpleObjectMapper().readValue(result.getResourceObject().toString(), type);
        }

        return new TransactionalBatchOperationResult<T>(result, resource);
    }

    /**
     * Gets the ActivityId that identifies the server request made to execute the batch request.
     */
    @Override
    public String getActivityId() {
        return this.serverResponse.getActivityId();
    }

    @Override
    public void close() {
        if (this.serverResponse != null) {
            this.serverResponse.close();
        }
        super.close();
    }

    @Override
    public TransactionalBatchOperationResult<?> get(int index) {
        return this.resultsByOperationIndex[index];
    }

    /**
     * Gets an enumerator over the operation results.
     *
     * @return Enumerator over the operation results.
     */
    @Override
    public Iterator<TransactionalBatchOperationResult<?>> iterator() {
        return Stream.of(this.resultsByOperationIndex).iterator();
    }

    /**
     * Gets the number of operation results.
     */
    @Override
    public int size() {
        return this.resultsByOperationIndex.length;
    }
}
