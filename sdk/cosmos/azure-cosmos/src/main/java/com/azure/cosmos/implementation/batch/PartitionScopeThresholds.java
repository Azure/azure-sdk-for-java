// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BulkProcessingOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PartitionScopeThresholds<TContext> {
    private final static Logger logger = LoggerFactory.getLogger(PartitionScopeThresholds.class);

    private final String pkRangeId;
    private final BulkProcessingOptions<TContext> options;
    private int targetMicroBatchSize;
    private int targetMicroBatchConcurrency;
    private final AtomicLong totalOperationCount;
    private final AtomicReference<CurrentIntervalThresholds> currentThresholds;
    private final String identifier = UUID.randomUUID().toString();

    public PartitionScopeThresholds(String pkRangeId, BulkProcessingOptions<TContext> options) {
        checkNotNull(pkRangeId, "expected non-null pkRangeId");
        checkNotNull(options, "expected non-null options");

        this.pkRangeId = pkRangeId;
        this.options = options;
        this.targetMicroBatchSize = options.getMaxMicroBatchSize();
        this.targetMicroBatchConcurrency = options.getMaxMicroBatchConcurrency();
        this.totalOperationCount = new AtomicLong(0);
        this.currentThresholds = new AtomicReference<>(new CurrentIntervalThresholds());
    }

    public String getPartitionKeyRangeId() {
        return this.pkRangeId;
    }

    private boolean shouldReevaluateThresholds(long totalSnapshot, long currentSnapshot) {
        if (totalSnapshot < 1_000) {
            return currentSnapshot == 100;
        }

        if (totalSnapshot < 10_000) {
            return currentSnapshot == 1_000;
        }

        if (totalSnapshot < 1_000_000) {
            return currentSnapshot == 10_000;
        }

        return currentSnapshot == 100_000;
    }

    private void recordOperation(boolean isRetry) {
        long totalSnapshot = this.totalOperationCount.incrementAndGet();
        CurrentIntervalThresholds currentThresholdsSnapshot = this.currentThresholds.get();
        long currentTotalCountSnapshot = currentThresholdsSnapshot.currentOperationCount.incrementAndGet();
        long currentRetryCountSnapshot;
        if (isRetry) {
            currentRetryCountSnapshot = currentThresholdsSnapshot.currentRetriedOperationCount.incrementAndGet();
        } else {
            currentRetryCountSnapshot = currentThresholdsSnapshot.currentRetriedOperationCount.get();
        }

        double retryRate = (double)currentRetryCountSnapshot / currentTotalCountSnapshot;
        if (this.shouldReevaluateThresholds(totalSnapshot, currentTotalCountSnapshot) &&
            this.currentThresholds.compareAndSet(currentThresholdsSnapshot, new CurrentIntervalThresholds())) {

            this.reevaluateThresholds(totalSnapshot, currentTotalCountSnapshot, currentRetryCountSnapshot, retryRate);
        }
    }

    private synchronized void reevaluateThresholds(
        long totalCount,
        long currentCount,
        long retryCount,
        double retryRate) {

        int microBatchSizeBefore = this.targetMicroBatchSize;
        int microBatchConcurrencyBefore = this.targetMicroBatchConcurrency;

        if (retryRate == 0) {
            if (this.targetMicroBatchSize < this.options.getMaxMicroBatchSize()) {
                this.targetMicroBatchSize = Math.min(
                    Math.max(
                        this.targetMicroBatchSize + 1,
                        (int)(this.targetMicroBatchSize * (1 + this.options.getMaxMicroBatchRetryRate()))),
                    this.options.getMaxMicroBatchSize());
            } else if(this.targetMicroBatchConcurrency < this.options.getMaxMicroBatchConcurrency()) {
                this.targetMicroBatchConcurrency++;
            }
        } else if (retryRate > this.options.getMaxMicroBatchRetryRate()) {
            if (this.targetMicroBatchConcurrency > 1) {
                this.targetMicroBatchConcurrency--;
            } else if (this.targetMicroBatchSize > 1) {
                double deltaRate = retryRate - this.options.getMaxMicroBatchRetryRate();
                this.targetMicroBatchSize = Math.max(1, (int)(this.targetMicroBatchSize * (1 - deltaRate)));
            }
        }

        // TODO @fabianm - change to DEBUG after testing
        logger.info(
            "Reevaluated thresholds for PKRange '{}#{}' (TotalCount: {}, CurrentCount: {}, CurrentRetryCount: {}, " +
                "CurrentRetryRate: {} - BatchSize {} -> {}, Concurrency {} -> {}",
            this.pkRangeId,
            this.identifier,
            totalCount,
            currentCount,
            retryCount,
            retryRate,
            microBatchSizeBefore,
            this.targetMicroBatchSize,
            microBatchConcurrencyBefore,
            this.targetMicroBatchConcurrency);
    }

    public void recordSuccessfulOperation() {
        this.recordOperation(false);
    }

    public void recordEnqueuedRetry() {
        this.recordOperation(true);
    }

    private static class CurrentIntervalThresholds {
        public final AtomicLong currentOperationCount = new AtomicLong(0);
        public final AtomicLong currentRetriedOperationCount = new AtomicLong(0);
    }
}
