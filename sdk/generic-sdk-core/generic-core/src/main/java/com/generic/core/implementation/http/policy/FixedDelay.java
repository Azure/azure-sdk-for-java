// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy;

import com.generic.core.http.policy.RetryPolicy;
import com.generic.core.util.ClientLogger;

import java.time.Duration;
import java.util.Objects;

/**
 * A fixed-delay implementation of {@link RetryPolicy.RetryStrategy} that has a fixed delay duration between each retry attempt.
 */
public class FixedDelay implements RetryPolicy.RetryStrategy {
    private static final ClientLogger LOGGER = new ClientLogger(FixedDelay.class);

    private final Duration delay;

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param delay The fixed delay duration between retry attempts.
     * @throws IllegalArgumentException If {@code maxRetries} is negative.
     * @throws NullPointerException If {@code delay} is {@code null}.
     */
    public FixedDelay(Duration delay) {
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
    }

//    /**
//     * Creates an instance of {@link FixedDelay}.
//     *
//     * @param fixedDelayOptions The {@link RetryPolicy.FixedDelayOptions}.
//     */
//    public FixedDelay(RetryPolicy.FixedDelayOptions fixedDelayOptions) {
//        this(
//            Objects.requireNonNull(fixedDelayOptions, "'fixedDelayOptions' cannot be null.").getDelay()
//        );
//    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        return delay;
    }
}
