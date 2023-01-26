// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.ProgressReporter;
import io.netty.util.AttributeKey;

/**
 * A holder for all attributes that may be passed through the Netty HttpClient.
 */
public final class AzureNettyHttpClientAttr {
    public static final AttributeKey<AzureNettyHttpClientAttr> ATTRIBUTE_KEY =
        AttributeKey.newInstance("azure-sdk-pipeline-data");
    private final Long responseTimeoutOverride;
    private final ProgressReporter progressReporter;

    public AzureNettyHttpClientAttr(Long responseTimeoutOverride, ProgressReporter progressReporter) {
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
