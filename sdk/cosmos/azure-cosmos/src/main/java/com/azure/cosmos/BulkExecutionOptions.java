// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.util.Beta;

import java.time.Duration;

/**
 * @deprecated forRemoval = true, since = "4.19"
 * This class is not necessary anymore and will be removed.
 * Please use {@link com.azure.cosmos.models.CosmosBulkExecutionOptions}
 */
@Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
@Deprecated() //forRemoval = true, since = "4.19"
@SuppressWarnings("DeprecatedIsStillUsed")
public final class BulkExecutionOptions {
    private int maxMicroBatchSize = BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
    private int maxMicroBatchConcurrency = BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_CONCURRENCY;
    private double maxMicroBatchRetryRate = BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_RETRY_RATE;
    private double minMicroBatchRetryRate = BatchRequestResponseConstants.DEFAULT_MIN_MICRO_BATCH_RETRY_RATE;
    private Duration maxMicroBatchInterval = Duration.ofMillis(
        BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_INTERVAL_IN_MILLISECONDS);
    private final Object legacyBatchScopedContext;
    private final BulkExecutionThresholds thresholds;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;

    /**
     * Constructor
     * @param thresholds thresholds
     */
    BulkExecutionOptions(Object legacyBatchScopedContext, BulkExecutionThresholds thresholds) {
        this.legacyBatchScopedContext = legacyBatchScopedContext;
        if (thresholds == null) {
            this.thresholds = new BulkExecutionThresholds();
        } else {
            this.thresholds = thresholds;
        }
    }

    /**
     * Constructor
     * @param thresholds thresholds
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public BulkExecutionOptions(BulkExecutionThresholds thresholds) {
        this(null, thresholds);
    }

    /**
     * Constructor
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public BulkExecutionOptions() {
        this(null);
    }

    /**
     * The maximum batching size for bulk operations. This value determines number of operations executed in one
     * request. There is an upper limit on both number of operations and sum of size of operations. Any overflow is
     * internally retried.
     *
     * Another instance is: Currently we support a max limit of 200KB, and user select batch size to be 100 and individual
     * documents are of size 20KB, approximately 90 operations will always be retried. So it's better to choose a batch
     * size of 10 here if user is aware of there workload. If sizes are totally unknown and user cannot put a number on it
     * then retries are handled, so no issues as such.
     *
     * If the retry rate exceeds `getMaxMicroBatchInterval` the micro batch size gets dynamically reduced at runtime
     * @return micro batch size
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public int getMaxMicroBatchSize() {
        return maxMicroBatchSize;
    }

    /**
     * The maximum batching size for bulk operations. This value determines number of operations executed in one
     * request. There is an upper limit on both number of operations and sum of size of operations. Any overflow is
     * internally retried.
     *
     * Another instance is: Currently we support a max limit of 200KB, and user select batch size to be 100 and individual
     * documents are of size 20KB, approximately 90 operations will always be retried. So it's better to choose a batch
     * size of 10 here if user is aware of there workload. If sizes are totally unknown and user cannot put a number on it
     * then retries are handled, so no issues as such.
     *
     * If the retry rate exceeds `getMaxMicroBatchInterval` the micro batch size gets dynamically reduced at runtime
     *
     * @param maxMicroBatchSize batching size.
     *
     * @return the bulk processing options.
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public BulkExecutionOptions setMaxMicroBatchSize(int maxMicroBatchSize) {
        this.maxMicroBatchSize = maxMicroBatchSize;
        return this;
    }

    /**
     * The maximum concurrency for executing requests for a partition key range.
     *
     * @return max micro batch concurrency
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
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
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public BulkExecutionOptions setMaxMicroBatchConcurrency(int maxMicroBatchConcurrency) {
        this.maxMicroBatchConcurrency = maxMicroBatchConcurrency;
        return this;
    }

    /**
     * The flush interval for bulk operations.
     *
     * @return max micro batch interval
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
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
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public BulkExecutionOptions setMaxMicroBatchInterval(Duration maxMicroBatchInterval) {
        this.maxMicroBatchInterval = maxMicroBatchInterval;
        return this;
    }

    /**
     * The maximum acceptable retry rate bandwidth. This value determines how aggressively the actual micro batch size
     * gets reduced or increased if the number of retries (for example due to 429 - Throttling or because the total
     * request size exceeds the payload limit) is higher or lower that the targeted range.
     *
     * @return max targeted micro batch retry rate
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public double getMaxTargetedMicroBatchRetryRate() {
        return this.maxMicroBatchRetryRate;
    }

    /**
     * The acceptable retry rate bandwidth. This value determines how aggressively the actual micro batch size
     * gets reduced or increased if the number of retries (for example due to 429 - Throttling or because the total
     * request size exceeds the payload limit) is higher or lower that the targeted range.
     *
     * @param minRetryRate minimum targeted retry rate of batch requests. If the retry rate is
     *                     lower than this threshold the micro batch size will be dynamically increased over time
     * @param maxRetryRate maximum retry rate of batch requests that is treated as acceptable. If the retry rate is
     *                     higher than this threshold the micro batch size will be dynamically reduced over time
     *
     * @return the bulk processing options.
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public BulkExecutionOptions setTargetedMicroBatchRetryRate(double minRetryRate, double maxRetryRate) {
        if (minRetryRate < 0) {
            throw new IllegalArgumentException("The maxRetryRate must not be a negative value");
        }

        if (minRetryRate > maxRetryRate) {
            throw new IllegalArgumentException("The minRetryRate must not exceed the maxRetryRate");
        }

        this.maxMicroBatchRetryRate = maxRetryRate;
        this.minMicroBatchRetryRate = minRetryRate;
        return this;
    }

    /**
     * The minimum acceptable retry rate bandwidth. This value determines how aggressively the actual micro batch size
     * gets reduced or increased if the number of retries (for example due to 429 - Throttling or because the total
     * request size exceeds the payload limit) is higher or lower that the targeted range.
     *
     * @return min targeted micro batch retry rate
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public double getMinTargetedMicroBatchRetryRate() {
        return this.minMicroBatchRetryRate;
    }

    /**
     * Returns batch context
     * @return batch context
     */
    Object getLegacyBatchScopedContext() {
        return this.legacyBatchScopedContext;
    }

    /**
     * Returns thresholds
     * @return thresholds
     */
    @Beta(value = Beta.SinceVersion.V4_18_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    @Deprecated() //forRemoval = true, since = "4.19"
    public BulkExecutionThresholds getThresholds() {
        return this.thresholds;
    }

    OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.operationContextAndListenerTuple;
    }

    void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.operationContextAndListenerTuple = operationContextAndListenerTuple;
    }

    CosmosBulkExecutionOptions toCosmosBulkExecutionOptions() {
        CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions(
            this.thresholds.toCosmosBulkExecutionThresholdsState());

        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setMaxMicroBatchSize(new CosmosBulkExecutionOptions(), this.maxMicroBatchSize);
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setTargetedMicroBatchRetryRate(
                cosmosBulkExecutionOptions,
                this.minMicroBatchRetryRate,
                this.maxMicroBatchRetryRate);
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper
            .getCosmosBulkExecutionOptionsAccessor()
            .setOperationContext(cosmosBulkExecutionOptions, this.operationContextAndListenerTuple);
        return cosmosBulkExecutionOptions;
    }
}
