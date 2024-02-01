// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy;

import com.azure.core.http.HttpResponse;

import java.util.Collections;
import java.util.List;

/**
 * Information about the request that failed, used to determine whether a retry should be attempted.
 */
public final class RequestRetryInfomation {
    private final HttpResponse response;
    private final Throwable throwable;
    private final int tryCount;
    private final List<Throwable> retriedExceptions;

    /**
     * Creates a new ShouldRetryInfo object.
     *
     * @param response The HTTP response of the request that failed.
     * @param throwable The exception of the request that failed.
     * @param tryCount The number of tries that have been attempted.
     * @param retriedExceptions The list of exceptions that have been encountered during retries.
     */
    RequestRetryInfomation(HttpResponse response, Throwable throwable, int tryCount,
        List<Throwable> retriedExceptions) {
        this.response = response;
        this.throwable = throwable;
        this.tryCount = tryCount;
        this.retriedExceptions = retriedExceptions == null
            ? Collections.emptyList() : Collections.unmodifiableList(retriedExceptions);
    }

    /**
     * Gets the HTTP response of the request that failed.
     *
     * @return The HTTP response of the request that failed.
     */
    public HttpResponse getResponse() {
        return response;
    }

    /**
     * Gets the exception of the request that failed.
     *
     * @return The exception of the request that failed.
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
