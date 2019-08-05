// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import java.util.concurrent.TimeUnit;

/**
 * Options for configuring the {@link RequestRetryPolicy}. Please refer to the Factory for more information. Note
 * that there is no option for overall operation timeout. This is because Rx object have a timeout field which provides
 * this functionality.
 */
public final class RequestRetryOptions {

    private final int maxTries;
    private final int tryTimeout;
    private final long retryDelayInMs;
    private final long maxRetryDelayInMs;
    /**
     * A {@link RetryPolicyType} telling the pipeline what kind of retry policy to use.
     */
    private RetryPolicyType retryPolicyType;
    private String secondaryHost;

    /**
     * Constructor with default retry values: Exponential backoff, maxTries=4, tryTimeout=30, retryDelayInMs=4000,
     * maxRetryDelayInMs=120000, secondaryHost=null.
     */
    public RequestRetryOptions() {
        this(RetryPolicyType.EXPONENTIAL, null,
                null, null, null, null);
    }

    /**
     * Configures how the {@link com.azure.core.http.HttpPipeline} should retry requests.
     *
     * @param retryPolicyType
     *         A {@link RetryPolicyType} specifying the type of retry pattern to use. A value of {@code null} accepts
     *         the default.
     * @param maxTries
     *         Specifies the maximum number of attempts an operation will be tried before producing an error. A value of
     *         {@code null} means that you accept our default policy. A value of 1 means 1 try and no retries.
     * @param tryTimeout
     *         Indicates the maximum time allowed for any single try of an HTTP request. A value of {@code null} means
     *         that you accept our default. NOTE: When transferring large amounts of data, the default TryTimeout will
     *         probably not be sufficient. You should override this value based on the bandwidth available to the host
     *         machine and proximity to the Storage service. A good starting point may be something like (60 seconds per
     *         MB of anticipated-payload-size).
     * @param retryDelayInMs
     *         Specifies the amount of delay to use before retrying an operation. A value of {@code null} means you
     *         accept the default value. The delay increases (exponentially or linearly) with each retry up to a maximum
     *         specified by MaxRetryDelay. If you specify {@code null}, then you must also specify {@code null} for
     *         MaxRetryDelay.
     * @param maxRetryDelayInMs
     *         Specifies the maximum delay allowed before retrying an operation. A value of {@code null} means you
     *         accept the default value. If you specify {@code null}, then you must also specify {@code null} for
     *         RetryDelay.
     * @param secondaryHost
     *         If a secondaryHost is specified, retries will be tried against this host. If secondaryHost is
     *         {@code null} (the default) then operations are not retried against another host. NOTE: Before setting
     *         this field, make sure you understand the issues around reading stale and potentially-inconsistent data at
     *         <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-designing-ha-apps-with-ragrs>this webpage</a>
     *
     * <p><strong>Sample Code</strong></p>
     *
     * <p>For more samples, please see the <a href="https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java">samples file</a></p>
     * @throws IllegalArgumentException If one of the following case exists:
     * <ul>
     *      <li> There is only one null value for retryDelay and maxRetryDelay.<li/>
     *      <li> Unrecognized retry policy type.<li/>
     * </ul>
     */
    public RequestRetryOptions(RetryPolicyType retryPolicyType, Integer maxTries, Integer tryTimeout,
            Long retryDelayInMs, Long maxRetryDelayInMs, String secondaryHost) {
        this.retryPolicyType = retryPolicyType == null ? RetryPolicyType.EXPONENTIAL : retryPolicyType;
        if (maxTries != null) {
            assertInBounds("maxRetries", maxTries, 1, Integer.MAX_VALUE);
            this.maxTries = maxTries;
        } else {
            this.maxTries = 4;
        }

        if (tryTimeout != null) {
            assertInBounds("tryTimeout", tryTimeout, 1, Integer.MAX_VALUE);
            this.tryTimeout = tryTimeout;
        } else {
            this.tryTimeout = 60;
        }

        if ((retryDelayInMs == null && maxRetryDelayInMs != null)
                || (retryDelayInMs != null && maxRetryDelayInMs == null)) {
            throw new IllegalArgumentException("Both retryDelay and maxRetryDelay must be null or neither can be null");
        }

        if (retryDelayInMs != null && maxRetryDelayInMs != null) {
            assertInBounds("maxRetryDelayInMs", maxRetryDelayInMs, 1, Long.MAX_VALUE);
            assertInBounds("retryDelayInMs", retryDelayInMs, 1, maxRetryDelayInMs);
            this.maxRetryDelayInMs = maxRetryDelayInMs;
            this.retryDelayInMs = retryDelayInMs;
        } else {
            switch (this.retryPolicyType) {
                case EXPONENTIAL:
                    this.retryDelayInMs = TimeUnit.SECONDS.toMillis(4);
                    break;
                case FIXED:
                    this.retryDelayInMs = TimeUnit.SECONDS.toMillis(30);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognize retry policy type.");
            }
            this.maxRetryDelayInMs = TimeUnit.SECONDS.toMillis(120);
        }

        this.secondaryHost = secondaryHost;
    }

    int maxTries() {
        return this.maxTries;
    }

    int tryTimeout() {
        return this.tryTimeout;
    }

    String secondaryHost() {
        return this.secondaryHost;
    }

    long retryDelayInMs() {
        return retryDelayInMs;
    }

    long maxRetryDelayInMs() {
        return maxRetryDelayInMs;
    }

    /**
     * Calculates how long to delay before sending the next request.
     *
     * @param tryCount
     *         An {@code int} indicating which try we are on.
     *
     * @return A {@code long} value of how many milliseconds to delay.
     */
    long calculateDelayInMs(int tryCount) {
        long delay = 0;
        switch (this.retryPolicyType) {
            case EXPONENTIAL:
                delay = (pow(2L, tryCount - 1) - 1L) * this.retryDelayInMs;
                break;

            case FIXED:
                delay = this.retryDelayInMs;
                break;
            default:
                throw new IllegalArgumentException("Invalid retry policy type.");
        }

        return Math.min(delay, this.maxRetryDelayInMs);
    }

    private long pow(long number, int exponent) {
        long result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= number;
        }

        return result;
    }

    private static void assertInBounds(final String param, final long value, final long min, final long max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(String.format("The value of the parameter '%s' should be between %s and %s.", param, min, max));
        }
    }
}
