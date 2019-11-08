// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;

import com.azure.storage.common.implementation.StorageImplUtils;
import java.util.concurrent.TimeUnit;

/**
 * Configuration options for {@link RequestRetryPolicy}.
 */
public final class RequestRetryOptions {
    private final ClientLogger logger = new ClientLogger(RequestRetryOptions.class);

    private final int maxTries;
    private final int tryTimeout;
    private final long retryDelayInMs;
    private final long maxRetryDelayInMs;
    private final RetryPolicyType retryPolicyType;
    private final String secondaryHost;

    /**
     * Configures how the {@link HttpPipeline} should retry requests.
     */
    public RequestRetryOptions() {
        this(RetryPolicyType.EXPONENTIAL, null,
            null, null, null, null);
    }

    /**
     * Configures how the {@link HttpPipeline} should retry requests.
     *
     * @param retryPolicyType Optional. A {@link RetryPolicyType} specifying the type of retry pattern to use, default
     * value is {@link RetryPolicyType#EXPONENTIAL EXPONENTIAL}.
     * @param maxTries Optional. Maximum number of attempts an operation will be retried, default is {@code 4}.
     * @param tryTimeout Optional. Specified the maximum time allowed before a request is cancelled and assumed failed,
     * default is {@link Integer#MAX_VALUE}.
     *
     * <p>This value should be based on the bandwidth available to the host machine and proximity to the Storage
     * service, a good starting point may be 60 seconds per MB of anticipated payload size.</p>
     * @param retryDelayInMs Optional. Specifies the amount of delay to use before retrying an operation, default value
     * is {@code 4ms} when {@code retryPolicyType} is {@link RetryPolicyType#EXPONENTIAL EXPONENTIAL} and {@code 30ms}
     * when {@code retryPolicyType} is {@link RetryPolicyType#FIXED FIXED}.
     * @param maxRetryDelayInMs Optional. Specifies the maximum delay allowed before retrying an operation, default
     * value is {@code 120ms}.
     * @param secondaryHost Optional. Specified a secondary Storage account to retry requests against, default is none.
     *
     * <p>Before setting this understand the issues around reading stale and potentially-inconsistent data, view these
     * <a href=https://docs.microsoft.com/en-us/azure/storage/common/storage-designing-ha-apps-with-ragrs>Azure Docs</a>
     * for more information.</p>
     * @throws IllegalArgumentException If {@code getRetryDelayInMs} and {@code getMaxRetryDelayInMs} are not both null
     * or non-null or {@code retryPolicyType} isn't {@link RetryPolicyType#EXPONENTIAL}
     * or {@link RetryPolicyType#FIXED}.
     */
    public RequestRetryOptions(RetryPolicyType retryPolicyType, Integer maxTries, Integer tryTimeout,
        Long retryDelayInMs, Long maxRetryDelayInMs, String secondaryHost) {
        this.retryPolicyType = retryPolicyType == null ? RetryPolicyType.EXPONENTIAL : retryPolicyType;
        if (maxTries != null) {
            StorageImplUtils.assertInBounds("maxRetries", maxTries, 1, Integer.MAX_VALUE);
            this.maxTries = maxTries;
        } else {
            this.maxTries = 4;
        }

        if (tryTimeout != null) {
            StorageImplUtils.assertInBounds("tryTimeout", tryTimeout, 1, Integer.MAX_VALUE);
            this.tryTimeout = tryTimeout;
        } else {
            this.tryTimeout = Integer.MAX_VALUE;
        }

        if ((retryDelayInMs == null && maxRetryDelayInMs != null)
            || (retryDelayInMs != null && maxRetryDelayInMs == null)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Both retryDelay and maxRetryDelay must be null or neither can be null"));
        }

        if (retryDelayInMs != null) {
            StorageImplUtils.assertInBounds("maxRetryDelayInMs", maxRetryDelayInMs, 1, Long.MAX_VALUE);
            StorageImplUtils.assertInBounds("retryDelayInMs", retryDelayInMs, 1, maxRetryDelayInMs);
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
                    throw logger.logExceptionAsError(new IllegalArgumentException("Invalid 'RetryPolicyType'."));
            }
            this.maxRetryDelayInMs = TimeUnit.SECONDS.toMillis(120);
        }
        this.secondaryHost = secondaryHost;
    }

    /**
     * @return the maximum number of retries that will be attempted.
     */
    public int getMaxTries() {
        return this.maxTries;
    }

    /**
     * @return the maximum time, in seconds, allowed for a request until it is considered timed out.
     */
    public int getTryTimeout() {
        return this.tryTimeout;
    }

    /**
     * @return the URI of the secondary host where retries are attempted. If this is null then there is no secondary
     * host and all retries are attempted against the original host.
     */
    public String getSecondaryHost() {
        return this.secondaryHost;
    }

    /**
     * @return the delay in milliseconds between each retry attempt.
     */
    public long getRetryDelayInMs() {
        return retryDelayInMs;
    }

    /**
     * @return the maximum delay in milliseconds allowed between each retry.
     */
    public long getMaxRetryDelayInMs() {
        return maxRetryDelayInMs;
    }

    /**
     * Calculates how long to delay before sending the next request.
     *
     * @param tryCount An {@code int} indicating which try we are on.
     * @return A {@code long} value of how many milliseconds to delay.
     */
    long calculateDelayInMs(int tryCount) {
        long delay;
        switch (this.retryPolicyType) {
            case EXPONENTIAL:
                delay = (powOfTwo(tryCount - 1) - 1L) * this.retryDelayInMs;
                break;

            case FIXED:
                // The first try should have zero delay. Every other try has the fixed value
                delay = tryCount > 1 ? this.retryDelayInMs : 0;
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Invalid retry policy type."));
        }

        return Math.min(delay, this.maxRetryDelayInMs);
    }

    private long powOfTwo(int exponent) {
        long result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= 2L;
        }

        return result;
    }
}
