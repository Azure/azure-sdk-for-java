// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpResponse;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * Configuration options for {@link RetryPolicy}.
 */
@Fluent
public class RetryPolicyOptions {

    private RetryStrategy retryStrategy;
    private String retryAfterHeader;
    private ChronoUnit retryAfterTimeUnit;

    /**
     * Creates a default {@link ExponentialBackoff} for retry policy. It will not use any {@code retryAfterHeader}
     * in {@link HttpResponse}.
     */
    public RetryPolicyOptions() {
        setRetryOptions(new ExponentialBackoff(), null, null);
    }

    /**
     * Sets RetryPolicyOptions with the provided {@link RetryStrategy}. It will not use any
     * {@code retryAfterHeader} in {@link HttpResponse}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException if {@code retryStrategy} is {@code null}.
     */
    public RetryPolicyOptions setRetryStrategy(RetryStrategy retryStrategy) {
        return setRetryOptions(retryStrategy, null, null);
    }

    /**
     * Sets default {@link ExponentialBackoff} retry policy along with provided {@code retryAfterHeader} and
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
    public RetryPolicyOptions setRetryAfterHeader(String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        return setRetryOptions(new ExponentialBackoff(), retryAfterHeader, retryAfterTimeUnit);
    }

    private RetryPolicyOptions setRetryOptions(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryAfterHeader = retryAfterHeader;
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        if (!isNullOrEmpty(retryAfterHeader)) {
            Objects.requireNonNull(retryAfterTimeUnit, "'retryAfterTimeUnit' cannot be null.");
        }
        return this;
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
