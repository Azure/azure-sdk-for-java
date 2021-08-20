// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpResponse;

import java.net.HttpURLConnection;
import java.time.Duration;

/**
 * The interface for determining the retry strategy used in {@link RetryPolicy}.
 */
public interface RetryStrategy {

    int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    /**
     * Max number of retry attempts to be make.
     *
     * @return The max number of retry attempts.
     */
    int getMaxRetries();

    /**
     * Computes the delay between each retry.
     *
     * @param retryAttempts The number of retry attempts completed so far.
     * @return The delay duration before the next retry.
     */
    Duration calculateRetryDelay(int retryAttempts);

    /**
     * This method is consulted to determine if a retry attempt should be made for the given {@link HttpResponse} if the
     * retry attempts are less than {@link #getMaxRetries()}.
     *
     * @param httpResponse The response from the previous attempt.
     * @return Whether a retry should be attempted.
     */
    default boolean shouldRetry(HttpResponse httpResponse) {
        int code = httpResponse.getStatusCode();
        return (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
            || code == HTTP_STATUS_TOO_MANY_REQUESTS // HttpUrlConnection does not define HTTP status 429
            || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
            && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
            && code != HttpURLConnection.HTTP_VERSION));
    }

    /**
     * This method is consulted to determine if a retry attempt should be made for the given {@link Throwable}
     * propagated when the request failed to send.
     *
     * @param throwable The {@link Throwable} thrown during the previous attempt.
     * @return Whether a retry should be attempted.
     */
    default boolean shouldRetryException(Throwable throwable) {
        return true;
    }
}
