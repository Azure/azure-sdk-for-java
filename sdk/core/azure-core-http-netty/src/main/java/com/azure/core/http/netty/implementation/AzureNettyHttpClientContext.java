// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.ProgressReporter;

/**
 * A holder for all context that may be passed through the Netty HttpClient.
 */
public final class AzureNettyHttpClientContext {
    public static final String KEY = "azure-sdk-pipeline-data";
    private final Long responseTimeoutOverride;
    private final ProgressReporter progressReporter;

    /**
     * Creates an instance of AzureNettyHttpClientContext.
     *
     * @param responseTimeoutOverride The response timeout override.
     * @param progressReporter The progress reporter.
     */
    public AzureNettyHttpClientContext(Long responseTimeoutOverride, ProgressReporter progressReporter) {
        this.responseTimeoutOverride = responseTimeoutOverride;
        this.progressReporter = progressReporter;
    }

    /**
     * Gets the response timeout override.
     *
     * @return The response timeout override.
     */
    public Long getResponseTimeoutOverride() {
        return responseTimeoutOverride;
    }

    /**
     * Gets the progress reporter.
     *
     * @return The progress reporter.
     */
    public ProgressReporter getProgressReporter() {
        return progressReporter;
    }
}
