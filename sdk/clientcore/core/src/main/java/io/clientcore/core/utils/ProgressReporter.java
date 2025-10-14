// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongConsumer;

/**
 * {@link ProgressReporter} offers a convenient way to add progress tracking to I/O operations.
 * <p>
 * The {@link ProgressReporter} can be used to track a single operation as well as the progress of complex operations
 * that involve multiple sub-operations. In the latter case {@link ProgressReporter} forms a tree where child nodes
 * track the progress of sub-operations and report to the parent which in turn aggregates the total progress. The
 * reporting tree can have arbitrary level of nesting.
 */
public final class ProgressReporter {

    private final LongConsumer progressListener;
    private final Lock listenerLock;
    private final ProgressReporter parent;

    private static final AtomicLongFieldUpdater<ProgressReporter> PROGRESS_ATOMIC_UPDATER
        = AtomicLongFieldUpdater.newUpdater(ProgressReporter.class, "progress");
    private volatile long progress;

    /**
     * Creates top level {@link ProgressReporter}.
     * <p>
     * Only top level {@link ProgressReporter} can have a {@link LongConsumer}.
     *
     * @param progressListener The {@link LongConsumer} to be notified about progress.
     * @throws NullPointerException If {@code progressListener} is null.
     */
    private ProgressReporter(LongConsumer progressListener) {
        this.progressListener = Objects.requireNonNull(progressListener, "'progressListener' must not be null");
        this.listenerLock = new ReentrantLock();
        this.parent = null;
    }

    /**
     * Creates child {@link ProgressReporter}. It tracks its own progress and reports to parent.
     *
     * @param parent The parent {@link ProgressReporter}. Must not be null.
     * @throws NullPointerException If {@code parent} is null.
     */
    private ProgressReporter(ProgressReporter parent) {
        this.parent = Objects.requireNonNull(parent, "'parent' must not be null");
        this.progressListener = null;
        this.listenerLock = null;
    }

    /**
     * Creates a {@link ProgressReporter} that notifies a {@link LongConsumer} about progress.
     *
     * @param progressListener The {@link LongConsumer} to be notified about progress. Must not be null.
     * @return A new {@link ProgressReporter} instance.
     * @throws NullPointerException If {@code progressReceiver} is null.
     */
    public static ProgressReporter withProgressListener(LongConsumer progressListener) {
        return new ProgressReporter(progressListener);
    }

    /**
     * Creates a child {@link ProgressReporter} that can be used to track sub-progress when the tracked activity spans
     * across concurrent processes. The child {@link ProgressReporter} notifies parent about progress and the parent
     * notifies the {@link LongConsumer}.
     *
     * @return The child {@link ProgressReporter}.
     */
    public ProgressReporter createChild() {
        return new ProgressReporter(this);
    }

    /**
     * Resets progress to zero and notifies.
     * <p>
     * If this is the root {@link ProgressReporter} the attached {@link LongConsumer} is notified. Otherwise, already
     * accumulated progress is subtracted from the parent {@link ProgressReporter}'s progress.
     * </p>
     */
    public void reset() {
        try {
            if (listenerLock != null) {
                listenerLock.lock();
            }
            long accumulated = PROGRESS_ATOMIC_UPDATER.getAndSet(this, 0L);
            if (parent != null) {
                parent.reportProgress(-1L * accumulated);
            }
            if (progressListener != null) {
                progressListener.accept(0L);
            }
        } finally {
            if (listenerLock != null) {
                listenerLock.unlock();
            }
        }
    }

    /**
     * Accumulates the provided {@code progress} and notifies.
     * <p>
     * If this is the root {@link ProgressReporter} the attached {@link LongConsumer} is notified about accumulated
     * progress. Otherwise, the provided {@code progress} is reported to the parent {@link ProgressReporter}.
     *
     * @param progress The number to be accumulated.
     */
    public void reportProgress(long progress) {
        try {
            if (listenerLock != null) {
                listenerLock.lock();
            }
            long totalProgress = PROGRESS_ATOMIC_UPDATER.addAndGet(this, progress);
            if (parent != null) {
                parent.reportProgress(progress);
            }
            if (progressListener != null) {
                progressListener.accept(totalProgress);
            }
        } finally {
            if (listenerLock != null) {
                listenerLock.unlock();
            }
        }
    }
}
