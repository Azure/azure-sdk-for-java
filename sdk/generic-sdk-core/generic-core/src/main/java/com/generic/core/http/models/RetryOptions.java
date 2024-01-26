// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.util.ClientLogger;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Options to configure the retry policy's behavior.
 */
public class RetryOptions {
    private static final ClientLogger LOGGER = new ClientLogger(RetryOptions.class);
    private Integer maxRetries;
    private Predicate<HttpResponse> shouldRetry;
    private Predicate<Throwable> shouldRetryException;
    private Duration baseDelay;
    private Duration maxDelay;
    private Duration fixedDelay;

    /**
     * Creates an instance of {@link RetryOptions} with values for {@code baseDelay} and {@code maxDelay}. Use this
     * constructor for exponential retry delay strategy.
     * @param baseDelay The base delay duration for retry.
     * @param maxDelay The max delay duration for retry.
     */
    public RetryOptions(Duration baseDelay, Duration maxDelay) {
        Objects.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Objects.requireNonNull(maxDelay, "'maxDelay' cannot be null.");
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
    }

    /**
     * Creates an instance of {@link RetryOptions} with values for {@code fixedDelay}. Use this constructor for
     * fixed retry delay strategy.
     * @param fixedDelay The fixed delay duration between retry attempts.
     */
    public RetryOptions(Duration fixedDelay) {
        Objects.requireNonNull(fixedDelay, "'fixedDelay' cannot be null.");
        this.fixedDelay = fixedDelay;
    }


    /**
     * Get the maximum number of retry attempts to be made.
     * @return the max retry attempts.
     */
    public Integer getMaxRetries() {
        return maxRetries;
    }

    /**
     * Set the maximum number of retry attempts to be made.
     * @param maxRetries the max retry attempts.
     * @return the updated {@link RetryOptions} object.
     * @throws IllegalArgumentException if {@code maxRetries} is less than 0.
     */
    public RetryOptions setMaxRetries(Integer maxRetries) {
        if (maxRetries < 0) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Get the base delay duration for retry.
     * @return the base delay duration.
     */
    public Duration getBaseDelay() {
        return baseDelay;
    }

    /**
     * Get the max delay duration for retry.
     * @return the max delay duration.
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Get the fixed delay duration between retry attempts.
     * @return the fixed delay duration.
     */
    public Duration getFixedDelay() {
        return fixedDelay;
    }

    /**
     * Gets the predicate that determines if a retry should be attempted for the given {@link HttpResponse}.
     * <p>
     * If null, the default behavior is to retry HTTP responses with status codes 408, 429, and any 500 status code that
     * isn't 501 or 505.
     *
     * @return The predicate that determines if a retry should be attempted for the given {@link HttpResponse}.
     */
    public Predicate<HttpResponse> getShouldRetry() {
        return shouldRetry;
    }

    /**
     * Sets the predicate that determines if a retry should be attempted for the given {@link HttpResponse}.
     * <p>
     * If null, the default behavior is to retry HTTP responses with status codes 408, 429, and any 500 status code that
     * isn't 501 or 505.
     *
     * @param shouldRetry The predicate that determines if a retry should be attempted for the given
     * {@link HttpResponse}.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions setShouldRetry(Predicate<HttpResponse> shouldRetry) {
        this.shouldRetry = shouldRetry;
        return this;
    }

    /**
     * Gets the predicate that determines if a retry should be attempted for the given {@link Throwable}.
     * <p>
     * If null, the default behavior is to retry any {@link Exception}.
     *
     * @return The predicate that determines if a retry should be attempted for the given {@link Throwable}.
     */
    public Predicate<Throwable> getShouldRetryException() {
        return shouldRetryException;
    }

    /**
     * Sets the predicate that determines if a retry should be attempted for the given {@link Throwable}.
     * <p>
     * If null, the default behavior is to retry any {@link Exception}.
     *
     * @param shouldRetryException The predicate that determines if a retry should be attempted for the given
     * {@link Throwable}.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions setShouldRetryException(Predicate<Throwable> shouldRetryException) {
        this.shouldRetryException = shouldRetryException;
        return this;
    }
}
