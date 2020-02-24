// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * Encapsulates retry options in the Azure Cosmos DB database service.
 */
public class RetryOptions {
    private int maxRetryAttemptsOnThrottledRequests;
    private int maxRetryWaitTimeInSeconds;

    /**
     * Creates a new instance of the RetryOptions class and initializes all
     * properties to default values.
     */
    public RetryOptions() {
        this.maxRetryAttemptsOnThrottledRequests = 9;
        this.maxRetryWaitTimeInSeconds = 30;
    }

    /**
     * Gets the maximum number of retries in the case where the request fails
     * because the service has applied rate limiting on the client.
     *
     * @return the maximum number of retries.
     */
    public int maxRetryAttemptsOnThrottledRequests() {
        return this.maxRetryAttemptsOnThrottledRequests;
    }

    /**
     * Sets the maximum number of retries in the case where the request fails
     * because the service has applied rate limiting on the client.
     * <p>
     * When a client is sending requests faster than the allowed rate, the
     * service will return HttpStatusCode 429 (Too Many Request) to throttle the
     * client. The current implementation in the SDK will then wait for the
     * amount of time the service tells it to wait and retry after the time has
     * elapsed.
     * <p>
     * The default value is 9. This means in the case where the request is
     * throttled, the same request will be issued for a maximum of 10 times to
     * the server before an error is returned to the application.
     *
     * @param maxRetryAttemptsOnThrottledRequests the max number of retry attempts on failed requests due to a
     *                                            throttle error.
     * @return the RetryOptions.
     */
    public RetryOptions maxRetryAttemptsOnThrottledRequests(int maxRetryAttemptsOnThrottledRequests) {
        if (maxRetryAttemptsOnThrottledRequests < 0) {
            throw new IllegalArgumentException("maxRetryAttemptsOnThrottledRequests value must be a positive integer.");
        }

        this.maxRetryAttemptsOnThrottledRequests = maxRetryAttemptsOnThrottledRequests;
        return this;
    }

    /**
     * Gets the maximum retry time in seconds.
     *
     * @return the maximum retry time in seconds.
     */
    public int maxRetryWaitTimeInSeconds() {
        return this.maxRetryWaitTimeInSeconds;
    }

    /**
     * Sets the maximum retry time in seconds.
     * <p>
     * When a request fails due to a throttle error, the service sends back a
     * response that contains a value indicating the client should not retry
     * before the time period has elapsed (Retry-After). The MaxRetryWaitTime
     * flag allows the application to set a maximum wait time for all retry
     * attempts. If the cumulative wait time exceeds the MaxRetryWaitTime, the
     * SDK will stop retrying and return the error to the application.
     * <p>
     * The default value is 30 seconds.
     *
     * @param maxRetryWaitTimeInSeconds the maximum number of seconds a request will be retried.
     * @return the RetryOptions.
     */
    public RetryOptions maxRetryWaitTimeInSeconds(int maxRetryWaitTimeInSeconds) {
        if (maxRetryWaitTimeInSeconds < 0 || maxRetryWaitTimeInSeconds > Integer.MAX_VALUE / 1000) {
            throw new IllegalArgumentException(
                    "value must be a positive integer between the range of 0 to " + Integer.MAX_VALUE / 1000);
        }

        this.maxRetryWaitTimeInSeconds = maxRetryWaitTimeInSeconds;
        return this;
    }

    @Override
    public String toString() {
        return "RetryOptions{" +
                "maxRetryAttemptsOnThrottledRequests=" + maxRetryAttemptsOnThrottledRequests +
                ", maxRetryWaitTimeInSeconds=" + maxRetryWaitTimeInSeconds +
                '}';
    }
}
