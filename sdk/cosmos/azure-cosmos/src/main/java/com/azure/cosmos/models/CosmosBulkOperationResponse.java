// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosAsyncContainer;
import reactor.core.publisher.Flux;

/**
 * Request, response and the exception(if any) for a {@link CosmosItemOperation} request when processed using Bulk by calling
 * {@link CosmosAsyncContainer#executeBulkOperations(Flux, CosmosBulkExecutionOptions)}.
 */
public final class CosmosBulkOperationResponse<TContext> {

    private final CosmosItemOperation operation;
    private final CosmosBulkItemResponse response;
    private final Exception exception;
    private final TContext batchContext;

    /**
     * Initialises a new instance of {@link CosmosBulkOperationResponse}.
     *
     * @param operation the {@link CosmosItemOperation} for which this response object has values.
     * @param response the {@link CosmosBulkItemResponse} the bulk response.
     * @param batchContext the context of this bulk request.
     */
    CosmosBulkOperationResponse(CosmosItemOperation operation, CosmosBulkItemResponse response, TContext batchContext) {
        this.operation = operation;
        this.response = response;
        this.exception = null;
        this.batchContext = batchContext;
    }

    /**
     * Initialises a new instance of {@link CosmosBulkOperationResponse}.
     *
     * @param operation the {@link CosmosItemOperation} for which this response object has values.
     * @param exception the {@link Throwable} for this request.
     * @param batchContext the context of this bulk request.
     */
    CosmosBulkOperationResponse(CosmosItemOperation operation, Exception exception, TContext batchContext) {
        this.operation = operation;
        this.response = null;
        this.exception = exception;
        this.batchContext = batchContext;
    }

    CosmosBulkOperationResponse(CosmosItemOperation operation, CosmosBulkItemResponse response, Exception exception, TContext batchContext) {
        this.operation = operation;
        this.response = response;
        this.exception = exception;
        this.batchContext = batchContext;
    }

    /**
     * Get {@link CosmosItemOperation}
     * @return CosmosItemOperation
     */
    public CosmosItemOperation getOperation() {
        return operation;
    }

    /**
     * Get {@link CosmosBulkItemResponse}
     * @return CosmosBulkItemResponse
     */
    public CosmosBulkItemResponse getResponse() {
        return response;
    }

    /**
     * Get Exception
     * @return Exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Get BatchContext
     * @return BatchContext
     */
    public TContext getBatchContext() {
        return batchContext;
    }
}
