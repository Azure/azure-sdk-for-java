// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ProgressReporter} offers a convenient way to add progress tracking to I/O operations.
 * <p>
 * The {@link ProgressReporter} can be used to track a single operation as well as the progress of
 * complex operations that involve multiple sub-operations. In the latter case {@link ProgressReporter}
 * forms a tree where child nodes track the progress of sub-operations and report to the parent which in turn
 * aggregates the total progress. The reporting tree can have arbitrary level of nesting.
 *
 * <p>
 * <strong>Code samples</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.util.ProgressReportingE2ESample -->
 * <!-- end com.azure.core.util.ProgressReportingE2ESample -->
 */
public final class ProgressReporter {

    private final ProgressListener progressListener;
    private final Lock listenerLock;
    private final ProgressReporter parent;

    private static final AtomicLongFieldUpdater<ProgressReporter> PROGRESS_ATOMIC_UPDATER
        = AtomicLongFieldUpdater.newUpdater(ProgressReporter.class, "progress");
    private volatile long progress;

    /**
     * Creates top level {@link ProgressReporter}.
     * Only top level {@link ProgressReporter} can have {@link ProgressListener}.
     * @param progressListener The {@link ProgressListener} to be notified about progress.
     */
    private ProgressReporter(ProgressListener progressListener) {
        this.progressListener = Objects.requireNonNull(progressListener, "'progressListener' must not be null");
        this.listenerLock = new ReentrantLock();
        this.parent = null;
    }

    /**
     * Creates child {@link ProgressReporter}. It tracks it's own progress and reports to parent.
     * @param parent The parent {@link ProgressReporter}. Must not be null.
     */
    private ProgressReporter(ProgressReporter parent) {
        this.parent = Objects.requireNonNull(parent, "'parent' must not be null");
        this.progressListener = null;
        this.listenerLock = null;
    }

    /**
     * Creates a {@link ProgressReporter} that notifies {@link ProgressListener}.
     * @param progressListener The {@link ProgressListener} to be notified about progress. Must not be null.
     * @return The {@link ProgressReporter} instance.
     * @throws NullPointerException If {@code progressReceiver} is null.
     */
    public static ProgressReporter withProgressListener(ProgressListener progressListener) {
        return new ProgressReporter(progressListener);
    }

    /**
     * Creates child {@link ProgressReporter} that can be used to track sub-progress when tracked activity spans
     * across concurrent processes. Child {@link ProgressReporter} notifies parent about progress and
     * parent notifies {@link ProgressListener}.
     * @return The child {@link ProgressReporter}.
     */
    public ProgressReporter createChild() {
        return new ProgressReporter(this);
    }

    /**
     * Resets progress to zero and notifies.
     * <p>
     * If this is a root {@link ProgressReporter} then attached {@link ProgressListener} is notified.
     * Otherwise, already accumulated progress is subtracted from the parent {@link ProgressReporter}'s progress.
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
                progressListener.handleProgress(0L);
            }
        } finally {
            if (listenerLock != null) {
                listenerLock.unlock();
            }
        }
    }

    /**
     * Accumulates the provided {@code progress} and notifies.
     *
     * <p>
     * If this is a root {@link ProgressReporter}
     * then attached {@link ProgressListener} is notified about accumulated progress.
     * Otherwise, the provided {@code progress} is reported to the parent {@link ProgressReporter}.
     * </p>
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
                progressListener.handleProgress(totalProgress);
            }
        } finally {
            if (listenerLock != null) {
                listenerLock.unlock();
            }
        }
    }
}
