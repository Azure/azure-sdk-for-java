// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosBulkTransactionalBatchResponse {
    private final CosmosBatch cosmosBatch;
    private final CosmosBatchResponse response;
    private final Exception exception;

    public CosmosBulkTransactionalBatchResponse(
        CosmosBatch cosmosBatch,
        CosmosBatchResponse response,
        Exception exception) {

        checkNotNull(cosmosBatch, "Argument 'cosmosBatch' can not be null");
        this.cosmosBatch = cosmosBatch;
        this.response = response;
        this.exception = exception;
    }


    public CosmosBatch getCosmosBatch() {
        return cosmosBatch;
    }

    public Exception getException() {
        return exception;
    }

    public CosmosBatchResponse getResponse() {
        return response;
    }
}
