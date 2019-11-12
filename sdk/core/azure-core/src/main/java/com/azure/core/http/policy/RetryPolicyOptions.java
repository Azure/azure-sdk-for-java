// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpResponse;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * Immutable Configuration options for {@link RetryPolicy}.
 */
@Immutable
public class RetryPolicyOptions {

    private final RetryStrategy retryStrategy;
    private final String retryAfterHeader;
    private final ChronoUnit retryAfterTimeUnit;

    /**
     * Creates a default {@link RetryPolicyOptions} used by a {@link RetryPolicy}. This will use
     * {@link ExponentialBackoff} as the {@link #getRetryStrategy retry strategy} and will ignore retry delay headers.
     */
    public RetryPolicyOptions() {
        this(new ExponentialBackoff(), null, null);
    }

    /**
     * Creates the {@link RetryPolicyOptions} with provided {@link RetryStrategy} that will be used when a request is
     * retried. It will ignore retry delay headers.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException if {@code retryStrategy} is {@code null}.
     */
    public RetryPolicyOptions(RetryStrategy retryStrategy) {
        this(retryStrategy, null, null);
    }

    /**
     * Creates the {@link RetryPolicyOptions} with provided {@code retryAfterHeader} and {@code retryAfterTimeUnit}
     * that will be used when a request is retried. This will use {@link ExponentialBackoff} as the
     *  {@link #getRetryStrategy retry strategy}
     *
     * @param retryAfterHeader The HTTP header, such as 'Retry-After' or 'x-ms-retry-after-ms', to lookup for the
     * retry delay. The value {@code null} is valid.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. {@code null} is valid if, and only
     * if, {@code retryAfterHeader} is {@code null}.
     * @throws NullPointerException Only if {@code retryAfterTimeUnit} is {@code null} and {@code retryAfterHeader}
     * is not {@code null}.
     */
    public RetryPolicyOptions(String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this(new ExponentialBackoff(), retryAfterHeader, retryAfterTimeUnit);
    }

    /**
     * Creates the {@link RetryPolicyOptions} with provided {@link RetryStrategy}, {@code retryAfterHeader} and
     * {@code retryAfterTimeUnit} that will be used when a request is retried.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @param retryAfterHeader The HTTP header, such as 'Retry-After' or 'x-ms-retry-after-ms', to lookup for the
     * retry delay.The value {@code null} is valid.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. {@code null} is valid if, and only
     *      * if, {@code retryAfterHeader} is {@code null}.
     * @throws NullPointerException if {@code retryStrategy} is {@code null}. Also when {@code retryAfterTimeUnit}
     * is {@code null} and {@code retryAfterHeader} is not {@code null}.
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
     * @return The {@link RetryStrategy} used when retrying requests.
     */
    public RetryStrategy getRetryStrategy() {
        return retryStrategy;
    }

    /**
     * @return The HTTP header which contains the retry delay returned by the service.
     */
    public String getRetryAfterHeader() {
        return retryAfterHeader;
    }

    /**
     * @return The {@link ChronoUnit} used when applying request retry delays.
     */
    public ChronoUnit getRetryAfterTimeUnit() {
        return retryAfterTimeUnit;
    }

}
