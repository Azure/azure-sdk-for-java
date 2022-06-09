// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * TODO (kasobol-msft) add docs.
 */
public class ProgressReporter {
    private final AtomicLong progress = new AtomicLong();

    private final Consumer<Long> listener;
    private final ProgressReporter parent;

    /**
     * TODO (kasobol-msft) add docs.
     */
    public ProgressReporter() {
        listener = null;
        parent = null;
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @param listener The listener.
     */
    public ProgressReporter(Consumer<Long> listener) {
        this.listener = listener;
        parent = null;
    }

    private ProgressReporter(ProgressReporter parent) {
        this.parent = parent;
        this.listener = null;
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @param bytesTransferred bytes transferred
     */
    public void reportProgress(long bytesTransferred) {
        long totalProgress = progress.addAndGet(bytesTransferred);
        if (parent != null) {
            parent.reportProgress(bytesTransferred);
        }
        if (listener != null) {
            listener.accept(totalProgress);
        }
    }

    /**
     * TODO (kasobol-msft) add docs.
     */
    public void reset() {
        long accumulated = progress.getAndSet(0);
        if (parent != null) {
            parent.progress.addAndGet(-1L * accumulated);
        }
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @return total progress.
     */
    public long getProgress() {
        return progress.get();
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @return child.
     */
    public ProgressReporter subProgress() {
        return new ProgressReporter(this);
    }
}
