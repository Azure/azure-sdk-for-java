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

    public AzureNettyHttpClientContext(Long responseTimeoutOverride, ProgressReporter progressReporter) {
        this.responseTimeoutOverride = responseTimeoutOverride;
        this.progressReporter = progressReporter;
    }

    public Long getResponseTimeoutOverride() {
        return responseTimeoutOverride;
    }

    public ProgressReporter getProgressReporter() {
        return progressReporter;
    }
}
