// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.ProgressReporter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A holder for all context that may be passed through the Netty HttpClient.
 */
public final class AzureNettyHttpClientContext {
    public static final String KEY = "azure-sdk-pipeline-data";
    private final Long responseTimeoutOverride;
    private final ProgressReporter progressReporter;

    // Optional caller-provided holder, set to true when an interim "100 Continue" response is observed for the
    // request. The holder is placed in the request Context by a caller (for example a pipeline policy) that wants to
    // observe the "Expect: 100-continue" handshake. May be null.
    private final AtomicBoolean expectContinueReceivedHolder;

    /**
     * Creates an instance of AzureNettyHttpClientContext.
     *
     * @param responseTimeoutOverride The response timeout override.
     * @param progressReporter The progress reporter.
     * @param expectContinueReceivedHolder Optional caller-provided holder set to {@code true} when a
     * {@code 100 Continue} response is observed. May be null.
     */
    public AzureNettyHttpClientContext(Long responseTimeoutOverride, ProgressReporter progressReporter,
        AtomicBoolean expectContinueReceivedHolder) {
        this.responseTimeoutOverride = responseTimeoutOverride;
        this.progressReporter = progressReporter;
        this.expectContinueReceivedHolder = expectContinueReceivedHolder;
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

    /**
     * Gets the optional holder that is set to {@code true} when an interim {@code 100 Continue} response is observed
     * for this request. May be null.
     *
     * @return The holder, or null if the caller did not supply one.
     */
    public AtomicBoolean getExpectContinueReceivedHolder() {
        return expectContinueReceivedHolder;
    }
}
