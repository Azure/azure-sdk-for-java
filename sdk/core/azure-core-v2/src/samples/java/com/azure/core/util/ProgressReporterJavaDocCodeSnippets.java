// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * Code snippets for {@link ProgressReporter}.
 */
public class ProgressReporterJavaDocCodeSnippets {

    // BEGIN: com.azure.core.util.ProgressReportingE2ESample
    /**
     * A simple operation that simulates I/O activity.
     * @param progressReporter The {@link ProgressReporter}.
     */
    public static void simpleOperation(ProgressReporter progressReporter) {
        for (long i = 0; i < 100; i++) {
            // Simulate 100 I/Os with 10 progress.
            progressReporter.reportProgress(10);
        }
    }

    /**
     * A complex operation that simulates I/O activity by invoking multiple {@link #simpleOperation(ProgressReporter)}.
     * @param progressReporter The {@link ProgressReporter}.
     */
    public static void complexOperation(ProgressReporter progressReporter) {
        simpleOperation(progressReporter.createChild());
        simpleOperation(progressReporter.createChild());
        simpleOperation(progressReporter.createChild());
    }

    /**
     * The main method.
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        // Execute simpleOperation
        ProgressReporter simpleOperationProgressReporter = ProgressReporter
            .withProgressListener(progress -> System.out.println("Simple operation progress " + progress));
        simpleOperation(simpleOperationProgressReporter);

        // Execute complexOperation
        ProgressReporter complexOperationProgressReporter = ProgressReporter
            .withProgressListener(progress -> System.out.println("Complex operation progress " + progress));
        complexOperation(complexOperationProgressReporter);
    }
    // END: com.azure.core.util.ProgressReportingE2ESample
}
