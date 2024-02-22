// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy;

import com.generic.core.http.policy.RequestRetryCondition;
import com.generic.core.http.policy.RetryPolicy;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A fixed-delay implementation of {@link RetryPolicy.RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryPolicy.RetryStrategy {
    private final Duration delay;
    private final Predicate<RequestRetryCondition> shouldRetryCondition;

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param delay The fixed delay duration between retry attempts.
     * @throws NullPointerException If {@code delay} is {@code null}.
     */
    public FixedDelay(Duration delay) {
        this(delay, null);
    }

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param delay The fixed delay duration between retry attempts.
     * @throws NullPointerException If {@code delay} is {@code null}.
     */
    public FixedDelay(Duration delay, Predicate<RequestRetryCondition> shouldRetryCondition) {
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
        this.shouldRetryCondition = shouldRetryCondition;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        return delay;
    }

    @Override
    public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition) {
        return shouldRetryCondition == null
            ? RetryPolicy.RetryStrategy.super.shouldRetryCondition(requestRetryCondition)
            : shouldRetryCondition.test(requestRetryCondition);
    }
}
