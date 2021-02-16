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
@Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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

    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getMaxMicroBatchSize() {
        return maxMicroBatchSize;
    }

    /**
     * The batching size for bulk operations. This value determines number of operations executed in one request.
     * There is an upper limit on both number of operations and sum of size of operations. Any overflow is internally
     * retried(without keeping any count).
     *
     * Always good to select a value such that there is less un-necessary retry as much as possible.
     * For eg. If max operation count supported is 100, and user passes the value to be 120, 20 operations will be retried
     * all the time(without any execution i.e. in the client logic). So it's better to choose a value of 100 in this case.
     *
     * Another instance is: Currently we support a max limit of 200KB, and user select batch size to be 100 and individual
     * documents are of size 20KB, approximately 90 operations will always be retried. So it's better to choose a batch
     * size of 10 here if user is aware of there workload. If sizes are totally unknown and user cannot put a number on it
     * then retries are handled, so no issues as such.
     *
     * @param maxMicroBatchSize batching size.
     *
     * @return the bulk processing options.
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkProcessingOptions<TContext> setMaxMicroBatchSize(int maxMicroBatchSize) {
        this.maxMicroBatchSize = maxMicroBatchSize;
        return this;
    }

    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getMaxMicroBatchConcurrency() {
        return maxMicroBatchConcurrency;
    }

    /**
     * The maximum concurrency for executing requests for a partition key range.
     *
     * @param maxMicroBatchConcurrency maximum concurrency.
     *
     * @return the bulk processing options.
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkProcessingOptions<TContext> setMaxMicroBatchConcurrency(int maxMicroBatchConcurrency) {
        this.maxMicroBatchConcurrency = maxMicroBatchConcurrency;
        return this;
    }

    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getMaxMicroBatchInterval() {
        return maxMicroBatchInterval;
    }

    /**
     * The flush interval for bulk operations.
     *
     * @param maxMicroBatchInterval duration after which operations will be flushed to form a new batch to be executed.
     *
     * @return the bulk processing options.
     */
    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public BulkProcessingOptions<TContext> setMaxMicroBatchInterval(Duration maxMicroBatchInterval) {
        this.maxMicroBatchInterval = maxMicroBatchInterval;
        return this;
    }

    @Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public TContext getBatchContext() {
        return batchContext;
    }
}
