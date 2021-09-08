// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.batch.BatchRequestResponseConstants;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.util.Beta;

import java.time.Duration;

/**
 * Encapsulates options that can be specified for operations used in Bulk execution.
 * It can be passed while processing bulk operations.
 */
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosBulkExecutionOptions {
    private int maxMicroBatchSize = BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST;
    private int maxMicroBatchConcurrency = BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_CONCURRENCY;
    private double maxMicroBatchRetryRate = BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_RETRY_RATE;
    private double minMicroBatchRetryRate = BatchRequestResponseConstants.DEFAULT_MIN_MICRO_BATCH_RETRY_RATE;
    private Duration maxMicroBatchInterval = Duration.ofMillis(
        BatchRequestResponseConstants.DEFAULT_MAX_MICRO_BATCH_INTERVAL_IN_MILLISECONDS);
    private final Object legacyBatchScopedContext;
    private final CosmosBulkExecutionThresholdsState thresholds;
    private OperationContextAndListenerTuple operationContextAndListenerTuple;

    /**
     * Constructor
     * @param thresholdsState thresholds
     */
    CosmosBulkExecutionOptions(Object legacyBatchScopedContext, CosmosBulkExecutionThresholdsState thresholdsState) {
        this.legacyBatchScopedContext = legacyBatchScopedContext;
        if (thresholdsState == null) {
            this.thresholds = new CosmosBulkExecutionThresholdsState();
        } else {
            this.thresholds = thresholdsState;
        }
    }

    /**
     * Constructor
     * @param thresholdsState thresholds
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBulkExecutionOptions(CosmosBulkExecutionThresholdsState thresholdsState) {
        this(null, thresholdsState);
    }

    /**
     * Constructor
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBulkExecutionOptions() {
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
    int getMaxMicroBatchSize() {
        return maxMicroBatchSize;
    }

    /**
     * The maximum concurrency for executing requests for a partition key range.
     *
     * @return max micro batch concurrency
     */
    int getMaxMicroBatchConcurrency() {
        return maxMicroBatchConcurrency;
    }

    /**
     * The flush interval for bulk operations.
     *
     * @return max micro batch interval
     */
    Duration getMaxMicroBatchInterval() {
        return maxMicroBatchInterval;
    }

    /**
     * The maximum acceptable retry rate bandwidth. This value determines how aggressively the actual micro batch size
     * gets reduced or increased if the number of retries (for example due to 429 - Throttling or because the total
     * request size exceeds the payload limit) is higher or lower that the targeted range.
     *
     * @return max targeted micro batch retry rate
     */
    double getMaxTargetedMicroBatchRetryRate() {
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
    CosmosBulkExecutionOptions setTargetedMicroBatchRetryRate(double minRetryRate, double maxRetryRate) {
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
    double getMinTargetedMicroBatchRetryRate() {
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
     * Returns threshold state that can be passed to other CosmosBulkExecutionOptions in the future
     * @return thresholds
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBulkExecutionThresholdsState getThresholdsState() {
        return this.thresholds;
    }

    OperationContextAndListenerTuple getOperationContextAndListenerTuple() {
        return this.operationContextAndListenerTuple;
    }

    void setOperationContextAndListenerTuple(OperationContextAndListenerTuple operationContextAndListenerTuple) {
        this.operationContextAndListenerTuple = operationContextAndListenerTuple;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.setCosmosBulkExecutionOptionsAccessor(
            new ImplementationBridgeHelpers.CosmosBulkExecutionOptionsHelper.CosmosBulkExecutionOptionsAccessor() {

                @Override
                public void setOperationContext(CosmosBulkExecutionOptions options,
                                                OperationContextAndListenerTuple operationContextAndListenerTuple) {
                    options.setOperationContextAndListenerTuple(operationContextAndListenerTuple);
                }

                @Override
                public OperationContextAndListenerTuple getOperationContext(CosmosBulkExecutionOptions options) {
                    return options.getOperationContextAndListenerTuple();
                }

                @Override
                @SuppressWarnings({"unchecked"})
                public <T> T getLegacyBatchScopedContext(CosmosBulkExecutionOptions options) {
                    return (T)options.getLegacyBatchScopedContext();
                }

                @Override
                public double getMinTargetedMicroBatchRetryRate(CosmosBulkExecutionOptions options) {
                    return options.getMinTargetedMicroBatchRetryRate();
                }

                @Override
                public double getMaxTargetedMicroBatchRetryRate(CosmosBulkExecutionOptions options) {
                    return options.getMaxTargetedMicroBatchRetryRate();
                }

                @Override
                public int getMaxMicroBatchSize(CosmosBulkExecutionOptions options) {
                    return options.getMaxMicroBatchSize();
                }

                @Override
                public int getMaxMicroBatchConcurrency(CosmosBulkExecutionOptions options) {
                    return options.getMaxMicroBatchConcurrency();
                }

                @Override
                public Duration getMaxMicroBatchInterval(CosmosBulkExecutionOptions options) {
                    return options.getMaxMicroBatchInterval();
                }

                @Override
                public CosmosBulkExecutionOptions setTargetedMicroBatchRetryRate(
                    CosmosBulkExecutionOptions options,
                    double minRetryRate,
                    double maxRetryRate) {

                    return options.setTargetedMicroBatchRetryRate(minRetryRate, maxRetryRate);
                }


            });
    }
}
