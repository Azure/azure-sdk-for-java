// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.models;

import java.io.IOException;
import java.time.Duration;

/**
 * Class to hold the result for a retry of an SSE request
 */
public class RetrySSEResult {
    private final long lastEventId;
    private final Duration retryAfter;
    private final IOException ioException;

    public RetrySSEResult(IOException e, long lastEventId, Duration retryAfter) {
        this.ioException = e;
        this.lastEventId = lastEventId;
        this.retryAfter = retryAfter;
    }

    public long getLastEventId() {
        return lastEventId;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }

    public IOException getException() {
        return ioException;
    }
}
