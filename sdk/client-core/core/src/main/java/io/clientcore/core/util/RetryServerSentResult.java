// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import java.io.IOException;
import java.time.Duration;

/**
 * Class to hold the result for a retry of an ServerSentEvent content type request.
 */
public final class RetryServerSentResult {
    private final long lastEventId;
    private final Duration retryAfter;
    private final IOException ioException;

    RetryServerSentResult(IOException e, long lastEventId, Duration retryAfter) {
        this.ioException = e;
        this.lastEventId = lastEventId;
        this.retryAfter = retryAfter;
    }

    long getLastEventId() {
        return lastEventId;
    }

    Duration getRetryAfter() {
        return retryAfter;
    }

    IOException getException() {
        return ioException;
    }
}
