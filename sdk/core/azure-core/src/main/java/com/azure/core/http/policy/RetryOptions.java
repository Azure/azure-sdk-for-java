// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * The configuration for retries.
 */
public class RetryOptions {
    private final ExponentialBackoffOptions exponentialBackoffOptions;
    private final FixedDelayOptions fixedDelayOptions;
    private String retryAfterHeader;
    private ChronoUnit retryAfterTimeUnit;

    /**
     * Creates a new instance that uses {@link ExponentialBackoffOptions}.
     *
     * @param exponentialBackoffOptions The {@link ExponentialBackoffOptions}.
     */
    public RetryOptions(ExponentialBackoffOptions exponentialBackoffOptions) {
        this.exponentialBackoffOptions = Objects.requireNonNull(
            exponentialBackoffOptions, "'exponentialBackoffOptions' cannot be null.");
        fixedDelayOptions = null;
    }

    /**
     * Creates a new instance that uses {@link FixedDelayOptions}.
     *
     * @param fixedDelayOptions The {@link FixedDelayOptions}.
     */
    public RetryOptions(FixedDelayOptions fixedDelayOptions) {
        this.fixedDelayOptions = Objects.requireNonNull(
            fixedDelayOptions, "'fixedDelayOptions' cannot be null.");
        exponentialBackoffOptions = null;
    }

    /**
     * Gets the HTTP header, such as {@code Retry-After} or {@code x-ms-retry-after-ms}, to lookup
     * for the retry delay.
     *
     * @return The HTTP header, such as {@code Retry-After} or {@code x-ms-retry-after-ms}, to lookup
     * for the retry delay.
     */
    public String getRetryAfterHeader() {
        return retryAfterHeader;
    }

    /**
     * Sets the HTTP header, such as {@code Retry-After} or {@code x-ms-retry-after-ms}, to lookup
     * for the retry delay. If the value is null, {@link RetryStrategy#calculateRetryDelay(int)} will compute the delay
     * and ignore the delay provided in response header.
     *
     * @param retryAfterHeader The HTTP header, such as {@code Retry-After} or {@code x-ms-retry-after-ms}, to lookup
     * for the retry delay. If the value is null, {@link RetryStrategy#calculateRetryDelay(int)} will compute the delay
     * and ignore the delay provided in response header.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions setRetryAfterHeader(String retryAfterHeader) {
        this.retryAfterHeader = retryAfterHeader;
        return this;
    }

    /**
     * Gets the time unit to use when applying the retry delay.
     *
     * @return The time unit to use when applying the retry delay.
     */
    public ChronoUnit getRetryAfterTimeUnit() {
        return retryAfterTimeUnit;
    }

    /**
     * Sets the time unit to use when applying the retry delay. Null is valid if, and only if,
     * {@code retryAfterHeader} is null.
     *
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. Null is valid if, and only if,
     * {@code retryAfterHeader} is null.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions setRetryAfterTimeUnit(ChronoUnit retryAfterTimeUnit) {
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        return this;
    }

    /**
     * Gets the configuration for exponential backoff if configured.
     *
     * @return The {@link ExponentialBackoffOptions}.
     */
    public ExponentialBackoffOptions getExponentialBackoffOptions() {
        return exponentialBackoffOptions;
    }

    /**
     * Gets the configuration for exponential backoff if configured.
     *
     * @return The {@link FixedDelayOptions}.
     */
    public FixedDelayOptions getFixedDelayOptions() {
        return fixedDelayOptions;
    }
}
