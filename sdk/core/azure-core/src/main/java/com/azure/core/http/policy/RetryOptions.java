// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpResponse;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * The {@code RetryOptions} class provides configuration options for retry strategies. It supports both
 * {@link ExponentialBackoffOptions} and {@link FixedDelayOptions}.
 *
 * <p>This class is useful when you need to customize the behavior of retries in the HTTP pipeline. It allows you to
 * specify the type of retry strategy and its options.</p>
 *
 * <p>Here's a code sample of how to use this class:</p>
 *
 * <p>In these examples, {@code RetryOptions} is created with either {@code ExponentialBackoffOptions} or
 * {@code FixedDelayOptions}. These options can then be used to configure a retry policy in the HTTP pipeline.</p>
 *
 * <pre>
 * {@code
 * // Using ExponentialBackoffOptions
 * ExponentialBackoffOptions exponentialOptions = new ExponentialBackoffOptions()
 *     .setMaxRetries(5)
 *     .setBaseDelay(Duration.ofSeconds(1))
 *     .setMaxDelay(Duration.ofSeconds(10));
 * RetryOptions retryOptions = new RetryOptions(exponentialOptions);
 *
 * // Using FixedDelayOptions
 * FixedDelayOptions fixedOptions = new FixedDelayOptions(3, Duration.ofSeconds(1));
 * RetryOptions retryOptions = new RetryOptions(fixedOptions);
 * }
 * </pre>
 *
 * @see com.azure.core.http.policy.RetryPolicy
 * @see com.azure.core.http.policy.ExponentialBackoffOptions
 * @see com.azure.core.http.policy.FixedDelayOptions
 */
public class RetryOptions {
    private final ExponentialBackoffOptions exponentialBackoffOptions;
    private final FixedDelayOptions fixedDelayOptions;

    private Predicate<RequestRetryCondition> shouldRetryCondition;

    /**
     * Creates a new instance that uses {@link ExponentialBackoffOptions}.
     *
     * @param exponentialBackoffOptions The {@link ExponentialBackoffOptions}.
     */
    public RetryOptions(ExponentialBackoffOptions exponentialBackoffOptions) {
        this.exponentialBackoffOptions
            = Objects.requireNonNull(exponentialBackoffOptions, "'exponentialBackoffOptions' cannot be null.");
        fixedDelayOptions = null;
    }

    /**
     * Creates a new instance that uses {@link FixedDelayOptions}.
     *
     * @param fixedDelayOptions The {@link FixedDelayOptions}.
     */
    public RetryOptions(FixedDelayOptions fixedDelayOptions) {
        this.fixedDelayOptions = Objects.requireNonNull(fixedDelayOptions, "'fixedDelayOptions' cannot be null.");
        exponentialBackoffOptions = null;
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

    /**
     * Gets the predicate that determines if a retry should be attempted.
     * <p>
     * If null, the default behavior is to retry HTTP responses with status codes 408, 429, and any 500 status code that
     * isn't 501 or 505. And to retry any {@link Exception}.
     *
     * @return The predicate that determines if a retry should be attempted.
     */
    public Predicate<RequestRetryCondition> getShouldRetryCondition() {
        return shouldRetryCondition;
    }

    /**
     * Sets the predicate that determines if a retry should be attempted.
     * <p>
     * If null, the default behavior is to retry HTTP responses with status codes 408, 429, and any 500 status code that
     * isn't 501 or 505. And to retry any {@link Exception}.
     *
     * @param shouldRetryCondition The predicate that determines if a retry should be attempted for the given
     * {@link HttpResponse}.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions setShouldRetryCondition(Predicate<RequestRetryCondition> shouldRetryCondition) {
        this.shouldRetryCondition = shouldRetryCondition;
        return this;
    }
}
