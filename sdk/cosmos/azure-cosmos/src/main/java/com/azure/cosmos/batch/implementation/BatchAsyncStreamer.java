// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Handles operation queueing and dispatching.
 * <p>
 * Fills batches efficiently and maintains a timer for early dispatching in case of partially-filled batches and to
 * optimize for throughput.
 *
 * There is always one batch at a time being filled. Locking is in place to avoid concurrent
 * threads trying to add operations while the timer might be Dispatching the current batch. The current batch is
 * dispatched and a new one is readied to be filled by new operations, the dispatched batch runs independently through a
 * fire and forget pattern.
 * <p>
 * {@link BatchAsyncBatcher}
 */
public class BatchAsyncStreamer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(BatchAsyncStreamer.class);

    private final Object dispatchLimiter = new Object();
    private final int maxBatchByteSize;
    private final int maxBatchOperationCount;
    private final BatchAsyncBatcherExecutor executor;
    private final BatchAsyncBatcherRetrier retrier;
    private final int dispatchTimerInMilliseconds = 100;

    private final int congestionIncreaseFactor = 1;
    private final int congestionControllerDelayInSeconds = 1;
    private final int congestionDecreaseFactor = 5;
    private final int maxDegreeOfConcurrency;

    private volatile BatchAsyncBatcher currentBatcher;
    private final HashedWheelTimer timer;

    private volatile Timeout currentTimeout;
    private volatile Timeout congestionControlTimeout;
    private Semaphore limiter;

    private int congestionDegreeOfConcurrency = 1;
    private long congestionWaitTimeInMilliseconds = 100;
    private BatchPartitionMetric oldPartitionMetric;
    private BatchPartitionMetric partitionMetric;


    public BatchAsyncStreamer(
        final int maxBatchOperationCount,
        final int maxBatchByteSize,
        final HashedWheelTimer timer,
        final Semaphore limiter,
        final int maxDegreeOfConcurrency,
        final BatchAsyncBatcherExecutor executor,
        final BatchAsyncBatcherRetrier retrier) {

        checkArgument(maxBatchOperationCount > 0, "expected maxBatchOperationCount > 0, not %s", maxBatchOperationCount);
        checkArgument(maxBatchByteSize > 0, "expected maxBatchByteSize > 0, not %s", maxBatchByteSize);
        checkNotNull(timer, "expected non-null timer");
        checkNotNull(limiter, "expected non-null limiter");
        checkNotNull(executor, "expected non-null executor");
        checkNotNull(retrier, "expected non-null retrier");

        this.executor = executor;
        this.maxBatchByteSize = maxBatchByteSize;
        this.maxBatchOperationCount = maxBatchOperationCount;
        this.retrier = retrier;
        this.timer = timer;
        this.currentBatcher = this.createBatchAsyncBatcher();
        this.resetTimer();

        this.limiter = limiter;
        this.oldPartitionMetric = new BatchPartitionMetric();
        this.partitionMetric = new BatchPartitionMetric();
        this.maxDegreeOfConcurrency = maxDegreeOfConcurrency;

        this.startCongestionControlTimer();
    }

    public final void add(ItemBatchOperation<?> operation) {

        BatchAsyncBatcher toDispatch = null;

        synchronized (this.dispatchLimiter) {
            while (!this.currentBatcher.tryAdd(operation)) {
                // Batcher is full
                toDispatch = this.getBatchToDispatchAndCreate();
            }
        }

        if (toDispatch != null) {
            toDispatch.dispatchBatch(this.partitionMetric);  // result discarded for fire and forget
        }
    }

    public final void close() {
        this.currentTimeout.cancel();
        this.currentTimeout = null;

        this.congestionControlTimeout.cancel();
        this.congestionControlTimeout = null;
    }

    private BatchAsyncBatcher createBatchAsyncBatcher() {
        return new BatchAsyncBatcher(
            this.maxBatchOperationCount,
            this.maxBatchByteSize,
            this.executor,
            this.retrier);
    }

    private void dispatchTimer() {

        final BatchAsyncBatcher toDispatch;

        synchronized (this.dispatchLimiter) {
            toDispatch = this.getBatchToDispatchAndCreate();
        }

        if (toDispatch != null) {
            // Completable future runs in a async manner and leave this thread very fast
            toDispatch.dispatchBatch(this.partitionMetric);  // discarded for fire and forget
        }

        this.resetTimer();
    }

    private BatchAsyncBatcher getBatchToDispatchAndCreate() {

        if (this.currentBatcher.isEmpty()) {
            return null;
        }

        final BatchAsyncBatcher previousBatcher = this.currentBatcher;
        this.currentBatcher = this.createBatchAsyncBatcher();
        return previousBatcher;
    }

    private void resetTimer() {
        this.currentTimeout = this.timer.newTimeout(
            timeout -> this.dispatchTimer(),
            dispatchTimerInMilliseconds,
            TimeUnit.MILLISECONDS);
    }

    private void startCongestionControlTimer() {
        this.congestionControlTimeout = this.timer.newTimeout(
            timeout -> this.runCongestionControl(),
            congestionControllerDelayInSeconds,
            TimeUnit.SECONDS);
    }

    private void runCongestionControl() {

        while (true) {
            long elapsedTimeInMilliseconds = this.partitionMetric.getTimeTakenInMilliseconds() - this.oldPartitionMetric.getTimeTakenInMilliseconds();

            if (elapsedTimeInMilliseconds >= this.congestionWaitTimeInMilliseconds) {

                long diffThrottle = this.partitionMetric.getNumberOfThrottles() - this.oldPartitionMetric.getNumberOfThrottles();
                long changeItemsCount = this.partitionMetric.getNumberOfItemsOperatedOn() - this.oldPartitionMetric.getNumberOfItemsOperatedOn();
                this.oldPartitionMetric.add(changeItemsCount, elapsedTimeInMilliseconds, diffThrottle);

                if (diffThrottle > 0) {
                    // Decrease should not lead to degreeOfConcurrency 0 as this will just block the thread here and no one would release it.
                    int decreaseCount = Math.min(this.congestionDecreaseFactor, this.congestionDegreeOfConcurrency / 2);

                    // We got a throttle so we need to back off on the degree of concurrency.
                    try {
                        for (int i = 0; i < decreaseCount; i++) {
                            this.limiter.acquire(decreaseCount);
                        }
                    } catch (Exception ex) {
                        logger.error("Congestion control limiter acquire failed {}", ex.getMessage());
                    }

                    this.congestionDegreeOfConcurrency -= decreaseCount;
                    // In case of throttling increase the wait time, so as to converge max degreeOfConcurrency
                    this.congestionWaitTimeInMilliseconds += 100;
                }

                if (changeItemsCount > 0 && diffThrottle == 0) {
                    if (this.congestionDegreeOfConcurrency + this.congestionIncreaseFactor <= this.maxDegreeOfConcurrency) {
                        // We aren't getting throttles, so we should bump up the degree of concurrency.
                        this.limiter.release(this.congestionIncreaseFactor);
                        this.congestionDegreeOfConcurrency += this.congestionIncreaseFactor;
                    }
                }
            } else {
                break;
            }
        }

        this.startCongestionControlTimer();
    }
}
