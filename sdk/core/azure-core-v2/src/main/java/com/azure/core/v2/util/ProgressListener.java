// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util;

/**
 * A {@link ProgressListener} is an interface that can be used to listen to the progress of the I/O transfers.
 * The {@link #handleProgress(long)} method will be called periodically with the total progress accumulated
 * at the given point of time.
 *
 * <p>
 * <strong>Code samples</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.util.ProgressReportingE2ESample -->
 * <pre>
 * &#47;**
 *  * A simple operation that simulates I&#47;O activity.
 *  * &#64;param progressReporter The &#123;&#64;link ProgressReporter&#125;.
 *  *&#47;
 * public static void simpleOperation&#40;ProgressReporter progressReporter&#41; &#123;
 *     for &#40;long i = 0; i &lt; 100; i++&#41; &#123;
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
 *         .withProgressListener&#40;progress -&gt; System.out.println&#40;&quot;Simple operation progress &quot; + progress&#41;&#41;;
 *     simpleOperation&#40;simpleOperationProgressReporter&#41;;
 *
 *     &#47;&#47; Execute complexOperation
 *     ProgressReporter complexOperationProgressReporter = ProgressReporter
 *         .withProgressListener&#40;progress -&gt; System.out.println&#40;&quot;Complex operation progress &quot; + progress&#41;&#41;;
 *     complexOperation&#40;complexOperationProgressReporter&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.util.ProgressReportingE2ESample -->
 */
@FunctionalInterface
public interface ProgressListener {
    /**
     * The callback function invoked as progress is reported.
     *
     * <p>
     * The callback can be called concurrently from multiple threads if reporting spans across multiple
     * requests. The implementor must not perform thread blocking operations in the handler code.
     * </p>
     *
     * @param progress The total progress at the current point of time.
     */
    void handleProgress(long progress);
}
