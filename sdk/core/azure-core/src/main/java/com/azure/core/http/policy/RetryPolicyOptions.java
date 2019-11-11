// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpResponse;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * Configuration options for {@link RetryPolicy}.
 */
public class RetryPolicyOptions {

    private final RetryStrategy retryStrategy;
    private final String retryAfterHeader;
    private final ChronoUnit retryAfterTimeUnit;

    /**
     * Creates a default {@link ExponentialBackoff} for retry policy. It will not use any {@code retryAfterHeader}
     * in {@link HttpResponse}.
     */
    public RetryPolicyOptions() {
        this(new ExponentialBackoff(), null, null);
    }

    /**
     * Creates a RetryPolicyOptions with the provided {@link RetryStrategy}. It will not use any
     * {@code retryAfterHeader} in {@link HttpResponse}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException if {@code retryStrategy} is {@code null}.
     */
    public RetryPolicyOptions(RetryStrategy retryStrategy) {
        this(retryStrategy, null, null);
    }

    /**
     * Creates a default {@link ExponentialBackoff} retry policy along with provided {@code retryAfterHeader} and
     * {@code retryAfterTimeUnit}.
     *
     * @param retryAfterHeader The 'retry-after' HTTP header name to lookup for the retry duration.The value
     * {@code null} is valid.
     * @param retryAfterTimeUnit The time unit to use while applying retry based on value specified in
     * {@code retryAfterHeader} in {@link HttpResponse}.The value {@code null} is valid only in case when
     * {@code retryAfterHeader} is empty or {@code null}.
     * @throws NullPointerException Only if {@code retryAfterTimeUnit} is {@code null} and {@code retryAfterHeader}
     * is not {@code null}.
     */
    public RetryPolicyOptions(String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this(new ExponentialBackoff(), retryAfterHeader, retryAfterTimeUnit);
    }

    /**
     * Creates a {@link RetryPolicyOptions} with the provided {@link RetryStrategy}, {@code retryAfterHeader}  and
     * {@code retryAfterTimeUnit}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @param retryAfterHeader The 'retry-after' HTTP header name to lookup for the retry duration. The value
     * {@code null} is valid.
     * @param retryAfterTimeUnit The time unit to use while applying retry based on value specified in
     * {@code retryAfterHeader} in {@link HttpResponse}.The value {@code null} is valid only in case when
     * {@code retryAfterHeader} is empty or {@code null}.
     * @throws NullPointerException if {@code retryStrategy} is {@code null}.Also when {@code retryAfterTimeUnit} is
     * {@code null} and {@code retryAfterHeader} is not {@code null}.
     */
    public RetryPolicyOptions(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryAfterHeader = retryAfterHeader;
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        if (!isNullOrEmpty(retryAfterHeader)) {
            Objects.requireNonNull(retryAfterTimeUnit, "'retryAfterTimeUnit' cannot be null.");
        }
    }

    /**
     * @return {@link RetryStrategy} to be used  in this {@link RetryPolicyOptions}.
     */
    public RetryStrategy getRetryStrategy() {
        return retryStrategy;
    }

    /**
     * @return {@code retryAfterHeader} to be used  in this {@link RetryPolicyOptions}.
     */
    public String getRetryAfterHeader() {
        return retryAfterHeader;
    }

    /**
     * @return {@link ChronoUnit} to be used  for {@code retryAfterHeader}.
     */
    public ChronoUnit getRetryAfterTimeUnit() {
        return retryAfterTimeUnit;
    }

}
