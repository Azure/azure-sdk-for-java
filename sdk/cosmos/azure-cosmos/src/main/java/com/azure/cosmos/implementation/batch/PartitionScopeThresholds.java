// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosBulkExecutionOptionsImpl;
import com.azure.cosmos.implementation.CosmosTransactionalBulkExecutionOptionsImpl;
import com.azure.cosmos.implementation.UUIDs;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PartitionScopeThresholds {
    private final static Logger logger = LoggerFactory.getLogger(PartitionScopeThresholds.class);

    private final String pkRangeId;
    private final AtomicInteger targetMicroBatchSize;
    private final AtomicLong totalOperationCount;
    private final AtomicReference<CurrentIntervalThresholds> currentThresholds;
    private final String identifier = UUIDs.nonBlockingRandomUUID().toString();
    private final double minRetryRate;
    private final double maxRetryRate;
    private final double avgRetryRate;
    private final int maxMicroBatchSize;
    private final int minTargetMicroBatchSize;

    private static CosmosBulkExecutionOptionsImpl validateOptions(CosmosBulkExecutionOptionsImpl options) {
        checkNotNull(options, "expected non-null options");
        return options;
    }

    private static CosmosTransactionalBulkExecutionOptionsImpl validateOptions(CosmosTransactionalBulkExecutionOptionsImpl options) {
        checkNotNull(options, "expected non-null options");
        return options;
    }

    public PartitionScopeThresholds(String pkRangeId, CosmosBulkExecutionOptionsImpl options) {
        this(
            pkRangeId,
            validateOptions(options).getMinTargetedMicroBatchRetryRate(),
            validateOptions(options).getMaxTargetedMicroBatchRetryRate(),
            validateOptions(options).getInitialMicroBatchSize(),
            validateOptions(options).getMaxMicroBatchSize(),
            validateOptions(options).getMinTargetMicroBatchSize());

    }

    public PartitionScopeThresholds(String pkRangeId, CosmosTransactionalBulkExecutionOptionsImpl options) {
        this(
            pkRangeId,
            validateOptions(options).getMinBatchRetryRate(),
            validateOptions(options).getMaxBatchRetryRate(),
            1, // for transactional batch, we start with small batch size to avoid sudden RU spike
            validateOptions(options).getMaxOperationsConcurrency(),
            1);
    }

    PartitionScopeThresholds(
        String pkRangeId,
        double minRetryRate,
        double maxRetryRate,
        int initialMicroBatchSize,
        int maxMicroBatchSize,
        int minMicroBatchSize) {
        checkNotNull(pkRangeId, "expected non-null pkRangeId");

        this.pkRangeId = pkRangeId;
        this.totalOperationCount = new AtomicLong(0);
        this.currentThresholds = new AtomicReference<>(new CurrentIntervalThresholds());

        this.minRetryRate = minRetryRate;
        this.maxRetryRate = maxRetryRate;
        this.avgRetryRate = ((this.maxRetryRate + this.minRetryRate)/2);
        this.maxMicroBatchSize = Math.min(
            maxMicroBatchSize,
            BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST);
        this.minTargetMicroBatchSize = Math.max(
            minMicroBatchSize,
            Configs.getMinTargetBulkMicroBatchSize()
        );
        this.targetMicroBatchSize =
            new AtomicInteger(
                Math.max(
                    Math.min(initialMicroBatchSize, this.maxMicroBatchSize),
                    Math.min(this.minTargetMicroBatchSize,  this.maxMicroBatchSize)));
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

        if (retryRate < this.minRetryRate && microBatchSizeBefore < maxMicroBatchSize) {
            int targetedNewBatchSize =
                Math.min(
                    Math.max(
                        Math.min(
                            microBatchSizeBefore * 2,
                            microBatchSizeBefore + (int)(maxMicroBatchSize * this.avgRetryRate)),
                        this.minTargetMicroBatchSize),
                    this.maxMicroBatchSize);
            if (this.targetMicroBatchSize.compareAndSet(microBatchSizeBefore, targetedNewBatchSize)) {
                microBatchSizeAfter = targetedNewBatchSize;
            }
        } else if (!onlyUpscale && retryRate > this.maxRetryRate && microBatchSizeBefore > 1) {
            double deltaRate = retryRate - this.avgRetryRate;
            int targetedNewBatchSize =
                Math.min(
                    Math.max(
                        this.minTargetMicroBatchSize,
                        (int) (microBatchSizeBefore * (1 - deltaRate))),
                    this.maxMicroBatchSize);
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

    public CurrentIntervalThresholds getCurrentThresholds() {
        return this.currentThresholds.get();
    }

    public long getTotalOperationCountSnapshot() {
        return this.totalOperationCount.longValue();
    }

    static class CurrentIntervalThresholds {
        public final AtomicLong currentOperationCount = new AtomicLong(0);
        public final AtomicLong currentRetriedOperationCount = new AtomicLong(0);
    }
}
