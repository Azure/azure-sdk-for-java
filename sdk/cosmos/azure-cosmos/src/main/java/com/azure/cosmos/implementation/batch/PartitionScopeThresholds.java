// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PartitionScopeThresholds {
    private final static Logger logger = LoggerFactory.getLogger(PartitionScopeThresholds.class);

    private final String pkRangeId;
    private final CosmosBulkExecutionOptions options;
    private final AtomicInteger targetMicroBatchSize;
    private final AtomicLong totalOperationCount;
    private final AtomicReference<CurrentIntervalThresholds> currentThresholds;
    private final String identifier = UUID.randomUUID().toString();
    private final double minRetryRate;
    private final double maxRetryRate;
    private final double avgRetryRate;

    public PartitionScopeThresholds(String pkRangeId, CosmosBulkExecutionOptions options) {
        checkNotNull(pkRangeId, "expected non-null pkRangeId");
        checkNotNull(options, "expected non-null options");

        this.pkRangeId = pkRangeId;
        this.options = options;
        this.targetMicroBatchSize = new AtomicInteger(options.getMaxMicroBatchSize());
        this.totalOperationCount = new AtomicLong(0);
        this.currentThresholds = new AtomicReference<>(new CurrentIntervalThresholds());
        this.minRetryRate = options.getMinTargetedMicroBatchRetryRate();
        this.maxRetryRate = options.getMaxTargetedMicroBatchRetryRate();
        this.avgRetryRate = ((this.maxRetryRate + this.minRetryRate)/2);
    }

    public String getPartitionKeyRangeId() {
        return this.pkRangeId;
    }

    private Pair<Boolean, Boolean> shouldReevaluateThresholds(long totalSnapshot, long currentSnapshot) {
        if (totalSnapshot < 1_000) {

            return Pair.of(currentSnapshot == 100, false);
        }

        if (totalSnapshot < 10_000) {
            return Pair.of(currentSnapshot == 1_000, false);
        }

        return Pair.of(currentSnapshot % 1_000 == 0, currentSnapshot % 10_000 == 0);
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

        Pair<Boolean, Boolean> shouldReevaluateResult =
            this.shouldReevaluateThresholds(totalSnapshot, currentTotalCountSnapshot);
        boolean shouldReevaluate = shouldReevaluateResult.getLeft();
        if (shouldReevaluate) {
            boolean onlyUpscale = shouldReevaluateResult.getRight();
            if (onlyUpscale ||
                this.currentThresholds.compareAndSet(currentThresholdsSnapshot, new CurrentIntervalThresholds())) {

                this.reevaluateThresholds(
                    totalSnapshot,
                    currentTotalCountSnapshot,
                    currentRetryCountSnapshot,
                    shouldReevaluateResult.getRight());
            }
        }
    }

    private void reevaluateThresholds(
        long totalCount,
        long currentCount,
        long retryCount,
        boolean onlyUpscale) {

        double retryRate = currentCount == 0 ? 0 : (double)retryCount / currentCount;
        int microBatchSizeBefore = this.targetMicroBatchSize.get();
        int microBatchSizeAfter = microBatchSizeBefore;

        if (retryRate < this.minRetryRate && microBatchSizeBefore < this.options.getMaxMicroBatchSize()) {
            int targetedNewBatchSize = Math.min(
                Math.min(
                    microBatchSizeBefore * 2,
                    microBatchSizeBefore + (int)(this.options.getMaxMicroBatchSize() * this.avgRetryRate)),
                this.options.getMaxMicroBatchSize());
            if (this.targetMicroBatchSize.compareAndSet(microBatchSizeBefore, targetedNewBatchSize)) {
                microBatchSizeAfter = targetedNewBatchSize;
            }
        } else if (!onlyUpscale && retryRate > this.maxRetryRate && microBatchSizeBefore > 1) {
            double deltaRate = retryRate - this.avgRetryRate;
            int targetedNewBatchSize = Math.max(1, (int) (microBatchSizeBefore * (1 - deltaRate)));
            if (this.targetMicroBatchSize.compareAndSet(microBatchSizeBefore, targetedNewBatchSize)) {
                microBatchSizeAfter = targetedNewBatchSize;
            }
        }

        logger.debug(
            "Reevaluated thresholds for PKRange '{}#{}' (TotalCount: {}, CurrentCount: {}, CurrentRetryCount: {}, " +
                "CurrentRetryRate: {} - BatchSize {} -> {}, OnlyUpscale: {})",
            this.pkRangeId,
            this.identifier,
            totalCount,
            currentCount,
            retryCount,
            retryRate,
            microBatchSizeBefore,
            microBatchSizeAfter,
            onlyUpscale);
    }

    public void recordSuccessfulOperation() {
        this.recordOperation(false);
    }

    public void recordEnqueuedRetry() {
        this.recordOperation(true);
    }

    public int  getTargetMicroBatchSizeSnapshot() {
        return this.targetMicroBatchSize.get();
    }

    private static class CurrentIntervalThresholds {
        public final AtomicLong currentOperationCount = new AtomicLong(0);
        public final AtomicLong currentRetriedOperationCount = new AtomicLong(0);
    }
}
