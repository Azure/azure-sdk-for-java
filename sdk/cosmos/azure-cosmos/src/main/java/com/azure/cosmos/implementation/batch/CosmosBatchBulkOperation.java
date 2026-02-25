// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.PartitionKey;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Wraps a {@link CosmosBatch} with a {@link BulkOperationStatusTracker} for tracking
 * status codes during bulk transactional execution.
 */
public final class CosmosBatchBulkOperation {

    private final CosmosBatch cosmosBatch;
    private final BulkOperationStatusTracker statusTracker;
    private TransactionalBatchRetryPolicy retryPolicy;

    public CosmosBatchBulkOperation(CosmosBatch cosmosBatch) {
        checkNotNull(cosmosBatch, "Argument 'cosmosBatch' must not be null.");
        this.cosmosBatch = cosmosBatch;
        this.statusTracker = new BulkOperationStatusTracker();
    }

    public CosmosBatch getCosmosBatch() {
        return this.cosmosBatch;
    }

    public PartitionKey getPartitionKeyValue() {
        return this.cosmosBatch.getPartitionKeyValue();
    }

    public int getOperationSize() {
        return this.cosmosBatch.getOperations().size();
    }

    public BulkOperationStatusTracker getStatusTracker() {
        return this.statusTracker;
    }

    public void setRetryPolicy(TransactionalBatchRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public TransactionalBatchRetryPolicy getRetryPolicy() {
        return this.retryPolicy;
    }
}
