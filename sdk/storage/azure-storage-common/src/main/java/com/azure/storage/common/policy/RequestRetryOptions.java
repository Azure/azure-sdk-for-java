// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;

import com.azure.storage.common.implementation.StorageImplUtils;

import java.time.Duration;

/**
 * Configuration options for {@link RequestRetryPolicy}.
 */
public final class RequestRetryOptions {
    private final ClientLogger logger = new ClientLogger(RequestRetryOptions.class);

    private final int maxTries;
    private final Duration tryTimeout;
    private final Duration retryDelay;
    private final Duration maxRetryDelay;
    private final RetryPolicyType retryPolicyType;
    private final String secondaryHost;

    /**
     * Configures how the {@link HttpPipeline} should retry requests.
     */
    public RequestRetryOptions() {
        this(RetryPolicyType.EXPONENTIAL, null, (Integer) null, null, null, null);
    }

    /**
     * Configures how the {@link HttpPipeline} should retry requests.
     *
     * @param retryPolicyType Optional. A {@link RetryPolicyType} specifying the type of retry pattern to use, default
     * value is {@link RetryPolicyType#EXPONENTIAL EXPONENTIAL}.
     * @param maxTries Optional. Maximum number of attempts an operation will be retried, default is {@code 4}.
     * @param tryTimeoutInSeconds Optional. Specified the maximum time allowed before a request is cancelled and
     * assumed failed, default is {@link Integer#MAX_VALUE} s.
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
     * <a href=https://docs.microsoft.com/azure/storage/common/storage-designing-ha-apps-with-ragrs>Azure Docs</a>
     * for more information.</p>
     * @throws IllegalArgumentException If {@code getRetryDelayInMs} and {@code getMaxRetryDelayInMs} are not both null
     * or non-null or {@code retryPolicyType} isn't {@link RetryPolicyType#EXPONENTIAL}
     * or {@link RetryPolicyType#FIXED}.
     */
    public RequestRetryOptions(RetryPolicyType retryPolicyType, Integer maxTries, Integer tryTimeoutInSeconds,
        Long retryDelayInMs, Long maxRetryDelayInMs, String secondaryHost) {
        this(retryPolicyType, maxTries, tryTimeoutInSeconds == null ? null : Duration.ofSeconds(tryTimeoutInSeconds),
            retryDelayInMs == null ? null : Duration.ofMillis(retryDelayInMs),
            maxRetryDelayInMs == null ? null : Duration.ofMillis(maxRetryDelayInMs), secondaryHost);
    }

    /**
     * Configures how the {@link HttpPipeline} should retry requests.
     *
     * @param retryPolicyType Optional. A {@link RetryPolicyType} specifying the type of retry pattern to use, default
     * value is {@link RetryPolicyType#EXPONENTIAL EXPONENTIAL}.
     * @param maxTries Optional. Maximum number of attempts an operation will be retried, default is {@code 4}.
     * @param tryTimeout Optional. Specified the maximum time allowed before a request is cancelled and
     * assumed failed, default is {@link Integer#MAX_VALUE}.
     *
     * <p>This value should be based on the bandwidth available to the host machine and proximity to the Storage
     * service, a good starting point may be 60 seconds per MB of anticipated payload size.</p>
     * @param retryDelay Optional. Specifies the amount of delay to use before retrying an operation, default value
     * is {@code 4ms} when {@code retryPolicyType} is {@link RetryPolicyType#EXPONENTIAL EXPONENTIAL} and {@code 30ms}
     * when {@code retryPolicyType} is {@link RetryPolicyType#FIXED FIXED}.
     * @param maxRetryDelay Optional. Specifies the maximum delay allowed before retrying an operation, default
     * value is {@code 120ms}.
     * @param secondaryHost Optional. Specified a secondary Storage account to retry requests against, default is none.
     *
     * <p>Before setting this understand the issues around reading stale and potentially-inconsistent data, view these
     * <a href=https://docs.microsoft.com/azure/storage/common/storage-designing-ha-apps-with-ragrs>Azure Docs</a>
     * for more information.</p>
     * @throws IllegalArgumentException If {@code getRetryDelayInMs} and {@code getMaxRetryDelayInMs} are not both null
     * or non-null or {@code retryPolicyType} isn't {@link RetryPolicyType#EXPONENTIAL}
     * or {@link RetryPolicyType#FIXED}.
     */
    public RequestRetryOptions(RetryPolicyType retryPolicyType, Integer maxTries, Duration tryTimeout,
        Duration retryDelay, Duration maxRetryDelay, String secondaryHost) {
        this.retryPolicyType = retryPolicyType == null ? RetryPolicyType.EXPONENTIAL : retryPolicyType;
        if (maxTries != null) {
            StorageImplUtils.assertInBounds("maxRetries", maxTries, 1, Integer.MAX_VALUE);
            this.maxTries = maxTries;
        } else {
            this.maxTries = 4;
        }

        if (tryTimeout != null) {
            StorageImplUtils.assertInBounds("'tryTimeout' in seconds", tryTimeout.getSeconds(), 1,
                Integer.MAX_VALUE);
            this.tryTimeout = tryTimeout;
        } else {
            /*
            Because this timeout applies to the whole operation, and calculating a meaningful timeout for read/write
            operations must consider the size of the payload, we can't set a meaningful default value for all requests
            and therefore default to no timeout.
             */
            this.tryTimeout = Duration.ofSeconds(Integer.MAX_VALUE);
        }

        if ((retryDelay == null && maxRetryDelay != null)
            || (retryDelay != null && maxRetryDelay == null)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Both retryDelay and maxRetryDelay must be null or neither can be null"));
        }

        if (retryDelay != null) {
            StorageImplUtils.assertInBounds("'maxRetryDelay' in milliseconds", maxRetryDelay.toMillis(), 1,
                Long.MAX_VALUE);
            StorageImplUtils.assertInBounds("'retryDelay' in milliseconds", retryDelay.toMillis(), 1,
                maxRetryDelay.toMillis());
            this.maxRetryDelay = maxRetryDelay;
            this.retryDelay = retryDelay;
        } else {
            switch (this.retryPolicyType) {
                case EXPONENTIAL:
                    this.retryDelay = Duration.ofSeconds(4);
                    break;
                case FIXED:
                    this.retryDelay = Duration.ofSeconds(30);
                    break;
                default:
                    throw logger.logExceptionAsError(new IllegalArgumentException("Invalid 'RetryPolicyType'."));
            }
            this.maxRetryDelay = Duration.ofSeconds(120);
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
     * @deprecated Please use {@link RequestRetryOptions#getTryTimeoutDuration()}
     */
    @Deprecated
    public int getTryTimeout() {
        return (int) this.tryTimeout.getSeconds();
    }

    /**
     * @return the maximum time, in seconds, allowed for a request until it is considered timed out.
     */
    public Duration getTryTimeoutDuration() {
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
     * @deprecated Please use {@link RequestRetryOptions#getTryTimeoutDuration()}
     */
    @Deprecated
    public long getRetryDelayInMs() {
        return retryDelay.toMillis();
    }

    /**
     * @return the delay between each retry attempt.
     */
    public Duration getRetryDelay() {
        return retryDelay;
    }

    /**
     * @return the maximum delay in milliseconds allowed between each retry.
     * @deprecated Please use {@link RequestRetryOptions#getTryTimeoutDuration()}
     */
    @Deprecated
    public long getMaxRetryDelayInMs() {
        return maxRetryDelay.toMillis();
    }

    /**
     * @return the maximum delay allowed between each retry.
     */
    public Duration getMaxRetryDelay() {
        return maxRetryDelay;
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
                delay = (powOfTwo(tryCount - 1) - 1L) * this.retryDelay.toMillis();
                break;

            case FIXED:
                // The first try should have zero delay. Every other try has the fixed value
                delay = tryCount > 1 ? this.retryDelay.toMillis() : 0;
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Invalid retry policy type."));
        }

        return Math.min(delay, this.maxRetryDelay.toMillis());
    }

    private long powOfTwo(int exponent) {
        long result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= 2L;
        }

        return result;
    }
}
