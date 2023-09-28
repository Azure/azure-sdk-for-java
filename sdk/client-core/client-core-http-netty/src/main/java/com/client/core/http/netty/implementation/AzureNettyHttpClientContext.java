// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.netty.implementation;

import com.client.core.util.ProgressReporter;

/**
 * A holder for all context that may be passed through the Netty HttpClient.
 */
public final class ClientNettyHttpClientContext {
    public static final String KEY = "client-sdk-pipeline-data";
    private final Long responseTimeoutOverride;
    private final ProgressReporter progressReporter;

    public ClientNettyHttpClientContext(Long responseTimeoutOverride, ProgressReporter progressReporter) {
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
