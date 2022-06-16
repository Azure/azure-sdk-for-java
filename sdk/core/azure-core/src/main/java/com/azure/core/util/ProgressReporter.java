// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * {@link ProgressReporter} offers a convenient way to add progress tracking to network operations.
 */
public final class ProgressReporter {

    private final ProgressReceiver progressReceiver;
    private final ProgressReporter parent;

    private static final AtomicLongFieldUpdater<ProgressReporter> PROGRESS_ATOMIC_UPDATER =
        AtomicLongFieldUpdater.newUpdater(ProgressReporter.class, "progress");
    private volatile long progress;

    /**
     * Creates top level {@link ProgressReporter}.
     * Only top level {@link ProgressReporter} can have {@link ProgressReceiver}.
     * @param progressReceiver The {@link ProgressReceiver} to be notified about progress.
     */
    private ProgressReporter(ProgressReceiver progressReceiver) {
        this.progressReceiver = Objects.requireNonNull(progressReceiver,
            "'progressReceiver' must not be null");
        this.parent = null;
    }

    /**
     * Creates child {@link ProgressReporter}. It tracks it's own progress and reports to parent.
     * @param parent The parent {@link ProgressReporter}. Must not be null.
     */
    private ProgressReporter(ProgressReporter parent) {
        this.parent = Objects.requireNonNull(parent,
            "'parent' must not be null");
        this.progressReceiver = null;
    }

    /**
     * Creates a {@link ProgressReporter} that notifies {@link ProgressReceiver}.
     * @param progressReceiver The {@link ProgressReceiver} to be notified about progress. Must not be null.
     * @return The {@link ProgressReporter} instance.
     * @throws NullPointerException If {@code progressReceiver} is null.
     */
    public static ProgressReporter withProgressReceiver(ProgressReceiver progressReceiver) {
        return new ProgressReporter(progressReceiver);
    }

    /**
     * Creates child {@link ProgressReporter} that can be used to track sub-progress when tracked activity spans
     * across concurrent processes. Child {@link ProgressReporter} notifies parent about progress and
     * parent notifies {@link ProgressReceiver}.
     * @return The child {@link ProgressReporter}.
     */
    public ProgressReporter createChild() {
        return new ProgressReporter(this);
    }

    /**
     * Resets progress to zero.
     */
    public void reset() {
        long accumulated = PROGRESS_ATOMIC_UPDATER.getAndSet(this, 0L);
        if (parent != null) {
            parent.reportProgress(-1L * accumulated);
        }
        if (progressReceiver != null) {
            progressReceiver.reportProgress(0L);
        }
    }

    /**
     * Accumulates provided number of transferred bytes.
     * @param bytesTransferred The number of bytes to be accumulated.
     */
    public void reportProgress(long bytesTransferred) {
        long totalProgress = PROGRESS_ATOMIC_UPDATER.addAndGet(this, bytesTransferred);
        if (parent != null) {
            parent.reportProgress(bytesTransferred);
        }
        if (progressReceiver != null) {
            progressReceiver.reportProgress(totalProgress);
        }
    }
}
