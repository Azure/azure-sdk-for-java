// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * {@link ProgressReporter} offers a convenient way to add progress tracking to I/O operations.
 * <p>
 * The {@link ProgressReporter} can be used to track a single operation as well as the progress of
 * complex operations that involve multiple sub-operations. In the latter case {@link ProgressReporter}
 * forms a tree where child nodes track the progress of sub-operations and report to the parent which in turn
 * aggregates the total progress. The reporting tree can have arbitrary level of nesting.
 *
 * <!-- src_embed com.azure.core.util.ProgressReporter -->
 * <pre>
 * &#47;**
 *  * A simple operation that simulates I&#47;O activity.
 *  * &#64;param progressReporter The &#123;&#64;link ProgressReporter&#125;.
 *  *&#47;
 * public static void simpleOperation&#40;ProgressReporter progressReporter&#41; &#123;
 *     for &#40;long i = 0 ; i &lt; 100; i++&#41; &#123;
 *         &#47;&#47; Simulate 100 I&#47;Os with 10 progress.
 *         progressReporter.reportProgress&#40;10&#41;;
 *     &#125;
 * &#125;
 *
 * &#47;**
 *  * A complex operation that simulates I&#47;O activity by invoking multiple &#123;&#64;link #simpleOperation&#40;ProgressReporter&#41;&#125;.
 *  * &#64;param progressReporter The &#123;&#64;link ProgressReporter&#125;.
 *  *&#47;
 * public static void complexOperation&#40;ProgressReporter progressReporter&#41; &#123;
 *     simpleOperation&#40;progressReporter.createChild&#40;&#41;&#41;;
 *     simpleOperation&#40;progressReporter.createChild&#40;&#41;&#41;;
 *     simpleOperation&#40;progressReporter.createChild&#40;&#41;&#41;;
 * &#125;
 *
 * &#47;**
 *  * The main method.
 *  * &#64;param args Program arguments.
 *  *&#47;
 * public static void main&#40;String[] args&#41; &#123;
 *     &#47;&#47; Execute simpleOperation
 *     ProgressReporter simpleOperationProgressReporter = ProgressReporter
 *         .withProgressReceiver&#40;progress -&gt; System.out.println&#40;&quot;Simple operation progress &quot; + progress&#41;&#41;;
 *     simpleOperation&#40;simpleOperationProgressReporter&#41;;
 *
 *     &#47;&#47; Execute complexOperation
 *     ProgressReporter complexOperationProgressReporter = ProgressReporter
 *         .withProgressReceiver&#40;progress -&gt; System.out.println&#40;&quot;Complex operation progress &quot; + progress&#41;&#41;;
 *     complexOperation&#40;complexOperationProgressReporter&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.util.ProgressReporter -->
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
     * Resets progress to zero and notifies.
     * <p>
     * If this is a root {@link ProgressReporter} then attached {@link ProgressReceiver} is notified.
     * Otherwise, already accumulated progress is subtracted from the parent {@link ProgressReporter}'s progress.
     * </p>
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
     * Accumulates the provided {@code progress} and notifies.
     *
     * <p>
     * If this is a root {@link ProgressReporter}
     * then attached {@link ProgressReceiver} is notified about accumulated progress.
     * Otherwise, the provided {@code progress} is reported to the parent {@link ProgressReporter}.
     * </p>
     *
     * @param progress The number to be accumulated.
     */
    public void reportProgress(long progress) {
        long totalProgress = PROGRESS_ATOMIC_UPDATER.addAndGet(this, progress);
        if (parent != null) {
            parent.reportProgress(progress);
        }
        if (progressReceiver != null) {
            progressReceiver.reportProgress(totalProgress);
        }
    }
}
