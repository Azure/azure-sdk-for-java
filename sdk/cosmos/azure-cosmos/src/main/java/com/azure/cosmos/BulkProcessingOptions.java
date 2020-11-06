// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.util.Beta;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Encapsulates options for executing a bulk. This is immutable once
 * {@link CosmosAsyncContainer#processBulkOperations(Flux, BulkProcessingOptions)} is called, changing it will have no affect.
 */
@Beta(Beta.SinceVersion.V4_8_0)
public final class BulkProcessingOptions<TContext> {
    private int maxMicroBatchSize = BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
    private int maxMicroBatchConcurrency = BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_CONCURRENCY;
    private Duration maxMicroBatchInterval = Duration.ofMillis(BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_INTERVAL_IN_MILLISECONDS);
    private final TContext batchContext;

    public BulkProcessingOptions(TContext batchContext) {
        this.batchContext = batchContext;
    }

    public BulkProcessingOptions() {
        this(null);
    }

    public int getMaxMicroBatchSize() {
        return maxMicroBatchSize;
    }

    public BulkProcessingOptions<TContext> setMaxMicroBatchSize(int maxMicroBatchSize) {
        this.maxMicroBatchSize = maxMicroBatchSize;
        return this;
    }

    public int getMaxMicroBatchConcurrency() {
        return maxMicroBatchConcurrency;
    }

    public BulkProcessingOptions<TContext> setMaxMicroBatchConcurrency(int maxMicroBatchConcurrency) {
        this.maxMicroBatchConcurrency = maxMicroBatchConcurrency;
        return this;
    }

    public Duration getMaxMicroBatchInterval() {
        return maxMicroBatchInterval;
    }

    public BulkProcessingOptions<TContext> setMaxMicroBatchInterval(Duration maxMicroBatchInterval) {
        this.maxMicroBatchInterval = maxMicroBatchInterval;
        return this;
    }

    public TContext getBatchContext() {
        return batchContext;
    }
}
