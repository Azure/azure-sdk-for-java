// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpPipelineCallContext;

/**
 * TODO (kasobol-msft) add docs.
 */
public final class Contexts {

    private Contexts() {
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @param context The context.
     * @param progressReporter The progress reporter.
     * @return The context.
     */
    public static Context setProgressReporter(Context context, ProgressReporter progressReporter) {
        return context.addData("azure-progress-reporter", progressReporter);
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @param context The context.
     * @return The progress reporter.
     */
    public static ProgressReporter getProgressReporter(Context context) {
        return (ProgressReporter) context.getData("azure-progress-reporter").orElse(null);
    }

    /**
     * TODO (kasobol-msft) add docs.
     * @param context The context.
     * @return The progress reporter.
     */
    public static ProgressReporter getProgressReporter(HttpPipelineCallContext context) {
        return (ProgressReporter) context.getData("azure-progress-reporter").orElse(null);
    }
}
