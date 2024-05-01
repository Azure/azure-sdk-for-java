// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Class to hold the result for a retry of an ServerSentEvent content type request.
 */
public final class ServerSentResult {
    private final String lastEventId;
    private final Duration retryAfter;
    private final IOException ioException;
    private final List<String> data;

    ServerSentResult(IOException exception, String lastEventId, Duration retryAfter, List<String> data) {
        this.ioException = exception;
        this.lastEventId = lastEventId;
        this.retryAfter = retryAfter;
        this.data = data;
    }

    /**
     * Get the accumulated event data from the text event stream.
     * @return The accumulated event data from the text event stream.
     */
    public List<String> getData() {
        return data;
    }

    /**
     * Get the exception that occurred during the request.
     * @return The exception that occurred during the request.
     */
    public IOException getException() {
        return ioException;
    }

    /**
     * Get the last event id from the text event stream.
     * @return The last event id from the text event stream.
     */
    public String getLastEventId() {
        return lastEventId;
    }

    /**
     * Get the retry time duration.
     * @return The retry time duration.
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
