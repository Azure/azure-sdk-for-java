// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.v2.http.policy;

import io.clientcore.core.http.models.Response;

import java.util.Collections;
import java.util.List;

/**
 * Information about the request that failed, used to determine whether a retry should be attempted.
 */
public final class RequestRetryCondition {
    private final Response<?> response;
    private final Throwable throwable;
    private final int tryCount;
    private final List<Throwable> retriedExceptions;

    /**
     * Creates a new ShouldRetryInfo object.
     *
     * @param response The HTTP response of the request that failed.
     * @param throwable The throwable of the request that failed.
     * @param tryCount The number of tries that have been attempted.
     * @param retriedThrowables The list of throwables that have been encountered during retries.
     */
    RequestRetryCondition(Response<?> response, Throwable throwable, int tryCount, List<Throwable> retriedThrowables) {
        this.response = response;
        this.throwable = throwable;
        this.tryCount = tryCount;
        this.retriedExceptions
            = retriedThrowables == null ? Collections.emptyList() : Collections.unmodifiableList(retriedThrowables);
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
     * Gets the throwable of the request that failed.
     * <p>
     * This may be null if the request failed with a response and no throwable was received.
     *
     * @return The throwable of the request that failed.
     */
    public Throwable getThrowable() {
        return throwable;
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
     * Gets the unmodifiable list of throwables that have been encountered during retries.
     *
     * @return The unmodifiable list of throwables that have been encountered during retries.
     */
    public List<Throwable> getRetriedThrowables() {
        return retriedExceptions;
    }
}
