// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

import java.time.Duration;
import java.util.List;

/**
 * Class to hold the result for a retry of an ServerSentEvent content type request.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class ServerSentResult {
    private final String lastEventId;
    private final Duration retryAfter;
    private final CoreException exception;
    private final List<String> data;

    /**
     * Creates a new instance of the {@link ServerSentResult} class.
     *
     * @param exception The exception that occurred during the request.
     * @param lastEventId The last event id from the text event stream.
     * @param retryAfter The retry time duration.
     * @param data The accumulated event data from the text event stream.
     */
    public ServerSentResult(Throwable exception, String lastEventId, Duration retryAfter, List<String> data) {
        this.exception = CoreException.from(exception);
        this.lastEventId = lastEventId;
        this.retryAfter = retryAfter;
        this.data = data;
    }

    /**
     * Get the accumulated event data from the text event stream.
     *
     * @return The accumulated event data from the text event stream.
     */
    public List<String> getData() {
        return data;
    }

    /**
     * Get the exception that occurred during the request.
     *
     * @return The exception that occurred during the request.
     */
    public CoreException getException() {
        return exception;
    }

    /**
     * Get the last event id from the text event stream.
     *
     * @return The last event id from the text event stream.
     */
    public String getLastEventId() {
        return lastEventId;
    }

    /**
     * Get the retry time duration.
     *
     * @return The retry time duration.
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
