// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

import java.util.Collections;
import java.util.List;

/**
 * Information about the request that failed, used to determine whether a retry should be attempted.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class HttpRetryCondition {
    private final Response<BinaryData> response;
    private final Exception exception;
    private final int tryCount;
    private final List<Exception> retriedExceptions;

    /**
     * Creates a new ShouldRetryInfo object.
     *
     * @param response The HTTP response of the request that failed.
     * @param exception The exception that caused the request to fail.
     * @param tryCount The number of tries that have been already attempted.
     * @param retriedExceptions The list of exceptions that have been encountered during retries.
     */
    HttpRetryCondition(Response<BinaryData> response, Exception exception, int tryCount,
        List<Exception> retriedExceptions) {
        this.response = response;
        this.exception = exception;
        this.tryCount = tryCount;
        this.retriedExceptions
            = retriedExceptions == null ? Collections.emptyList() : Collections.unmodifiableList(retriedExceptions);
    }

    /**
     * Gets the HTTP response of the request that failed.
     * <p>
     * This may be null if the request failed with a exception and no response was received.
     *
     * @return The HTTP response of the request that failed.
     */
    public Response<BinaryData> getResponse() {
        return response;
    }

    /**
     * Gets the exception that caused the request to fail.
     *
     * @return The exception that caused the request to fail.
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
