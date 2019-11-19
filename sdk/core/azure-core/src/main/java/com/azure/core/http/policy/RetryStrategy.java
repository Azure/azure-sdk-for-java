// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * The interface for determining the retry strategy used in {@link RetryPolicy}.
 */
public interface RetryStrategy {

    int HTTP_STATUS_TOO_MANY_REQUESTS = 429;
    String X_MS_RETRY_AFTER_MS_HEADER = "x-ms-retry-after-ms";

    /**
     * Max number of retry attempts to be make.
     *
     * @return The max number of retry attempts.
     */
    int getMaxRetries();

    /**
     * The {@link com.azure.core.http.HttpHeader} to use in {@link HttpResponse} to calculate delay.
     * @return The name of {@link com.azure.core.http.HttpHeader}.
     */
    default String getRetryAfterHeader() {
        return X_MS_RETRY_AFTER_MS_HEADER;
    }

    /**
     * The {@link ChronoUnit} to use to calculate delay.
     * @return time unit of the delay.
     */
    default ChronoUnit getRetryAfterTimeUnit() {
        return ChronoUnit.MILLIS;
    }

    /**
     * Computes the delay between each retry.
     *
     * @param retryAttempts The number of retry attempts completed so far.
     * @return The delay duration before the next retry.
     */
    Duration calculateRetryDelay(int retryAttempts);

    /**
     * Computes the delay between each retry by using {@link HttpResponse} header 'x-ms-retry-after-ms'. In absence of
     * this header, it will use {@code retryAttempts} only. A client can customize this behaviour by overwriting this
     * function.
     *
     * @param httpResponse The {@link HttpResponse} from previous {@link com.azure.core.http.HttpRequest}.
     * @param retryAttempts The number of retry attempts completed so far.
     * @return The delay duration before the next retry.
     */
    default Duration calculateRetryDelay(HttpResponse httpResponse, int retryAttempts) {
        int code = httpResponse.getStatusCode();

        // Response will not have a X_MS_RETRY_AFTER_MS_HEADER header.
        if (code != 429        // too many requests
            && code != 503) {  // service unavailable
            return calculateRetryDelay(retryAttempts);
        }

        String delayStringValue = null;
        if (!CoreUtils.isNullOrEmpty(getRetryAfterHeader())) {
            delayStringValue = httpResponse.getHeaderValue(getRetryAfterHeader());
        }

        return (!CoreUtils.isNullOrEmpty(delayStringValue) && !Objects.isNull(getRetryAfterTimeUnit()))
            ? Duration.of(Long.parseLong(delayStringValue), getRetryAfterTimeUnit())
            : calculateRetryDelay(retryAttempts);
    }

    /**
     * This method is consulted to determine if a retry attempt should be made for the given {@link HttpResponse} if the
     * retry attempts are less than {@link #getMaxRetries()}.
     *
     * @param httpResponse The response from the previous attempt.
     * @return {@code true} if another retry attempt should be made.
     */
    default boolean shouldRetry(HttpResponse httpResponse) {
        int code = httpResponse.getStatusCode();
        return (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
            || code == HTTP_STATUS_TOO_MANY_REQUESTS // HttpUrlConnection does not define HTTP status 429
            || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
            && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
            && code != HttpURLConnection.HTTP_VERSION));
    }
}
