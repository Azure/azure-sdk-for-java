// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.Response;

import java.util.Collections;
import java.util.List;

/**
 * Information about the request that failed, used to determine whether a retry should be attempted.
 */
public final class HttpRequestRetryCondition {
    private final Response<?> response;
    private final Exception exception;
    private final int tryCount;
    private final List<Exception> retriedExceptions;

    /**
     * Creates a new ShouldRetryInfo object.
     *
     * @param response The HTTP response of the request that failed.
     * @param exception The exception of the request that failed.
     * @param tryCount The number of tries that have been attempted.
     * @param retriedExceptions The list of exceptions that have been encountered during retries.
     */
    HttpRequestRetryCondition(Response<?> response, Exception exception, int tryCount, List<Exception> retriedExceptions) {
        this.response = response;
        this.exception = exception;
        this.tryCount = tryCount;
        this.retriedExceptions = retriedExceptions == null
            ? Collections.emptyList() : Collections.unmodifiableList(retriedExceptions);
    }

    /**
     * Gets the HTTP response of the request that failed.
     * <p>
     * This may be null if the request failed with a throwable and no response was received.
     *
     * @return The HTTP response of the request that failed.
     */
    public Response<?> getResponse() {
        return response;
    }

    /**
     * Gets the exception of the request that failed.
     *
     * @return The throwable of the request that failed.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Gets the number of tries that have been attempted.
     *
     * @return The number of tries that have been attempted.
     */
    public int getTryCount() {
        return tryCount;
    }

    /**
     * Gets the unmodifiable list of exceptions that have been encountered during retries.
     *
     * @return The unmodifiable list of exceptions that have been encountered during retries.
     */
    public List<Exception> getRetriedExceptions() {
        return retriedExceptions;
    }
}
