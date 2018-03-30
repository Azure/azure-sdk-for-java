/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

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
    public int getMaxRetryAttemptsOnThrottledRequests() {
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
     */
    public void setMaxRetryAttemptsOnThrottledRequests(int maxRetryAttemptsOnThrottledRequests) {
        if (maxRetryAttemptsOnThrottledRequests < 0) {
            throw new IllegalArgumentException("maxRetryAttemptsOnThrottledRequests value must be a positive integer.");
        }

        this.maxRetryAttemptsOnThrottledRequests = maxRetryAttemptsOnThrottledRequests;
    }

    /**
     * Gets the maximum retry time in seconds.
     *
     * @return the maximum retry time in seconds.
     */
    public int getMaxRetryWaitTimeInSeconds() {
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
     */
    public void setMaxRetryWaitTimeInSeconds(int maxRetryWaitTimeInSeconds) {
        if (maxRetryWaitTimeInSeconds < 0 || maxRetryWaitTimeInSeconds > Integer.MAX_VALUE / 1000) {
            throw new IllegalArgumentException(
                    "value must be a positive integer between the range of 0 to " + Integer.MAX_VALUE / 1000);
        }

        this.maxRetryWaitTimeInSeconds = maxRetryWaitTimeInSeconds;
    }
}
