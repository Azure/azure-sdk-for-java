// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.util.Beta;
import reactor.core.publisher.Flux;

/**
 * Request, response and the exception(if any) for a {@link CosmosItemOperation} request when processed using Bulk by calling
 * {@link CosmosAsyncContainer#processBulkOperations(Flux, BulkProcessingOptions)}.
 */
@Beta(Beta.SinceVersion.V4_9_0)
public final class CosmosBulkOperationResponse<TContext> {

    private final CosmosItemOperation operation;
    private final CosmosBulkItemResponse response;
    private final Throwable throwable;
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
        this.throwable = null;
        this.batchContext = batchContext;
    }

    /**
     * Initialises a new instance of {@link CosmosBulkOperationResponse}.
     *
     * @param operation the {@link CosmosItemOperation} for which this response object has values.
     * @param throwable the {@link Throwable} for this request.
     * @param batchContext the context of this bulk request.
     */
    CosmosBulkOperationResponse(CosmosItemOperation operation, Throwable throwable, TContext batchContext) {
        this.operation = operation;
        this.response = null;
        this.throwable = throwable;
        this.batchContext = batchContext;
    }

    public CosmosItemOperation getOperation() {
        return operation;
    }

    public CosmosBulkItemResponse getResponse() {
        return response;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public TContext getBatchContext() {
        return batchContext;
    }
}
