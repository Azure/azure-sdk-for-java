// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.models.CosmosBatchResponse;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosBulkTransactionalBatchResponse {
    private final CosmosBatchBulkOperation cosmosBatchBulkOperation;
    private final CosmosBatchResponse response;
    private final Exception exception;

    public CosmosBulkTransactionalBatchResponse(
        CosmosBatchBulkOperation cosmosBatchBulkOperation,
        CosmosBatchResponse response,
        Exception exception) {

        checkNotNull(cosmosBatchBulkOperation, "Argument 'cosmosBatchBulkOperation' can not be null");
        this.cosmosBatchBulkOperation = cosmosBatchBulkOperation;
        this.response = response;
        this.exception = exception;
    }


    public CosmosBatchBulkOperation getCosmosBatchBulkOperation() {
        return cosmosBatchBulkOperation;
    }

    public Exception getException() {
        return exception;
    }

    public CosmosBatchResponse getResponse() {
        return response;
    }
}
