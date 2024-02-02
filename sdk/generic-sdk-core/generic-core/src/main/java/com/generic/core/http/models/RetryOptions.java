// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.http.policy.RequestRetryCondition;
import com.generic.core.models.HeaderName;
import com.generic.core.util.ClientLogger;

import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Options to configure the retry policy's behavior.
 */
public class RetryOptions {
    private static final ClientLogger LOGGER = new ClientLogger(RetryOptions.class);
    private int maxRetries;
    private final Duration baseDelay;
    private final Duration maxDelay;
    private final Duration fixedDelay;
    private Predicate<RequestRetryCondition> shouldRetryCondition;
    private HashMap<HeaderName, Duration> retryHeaders = new HashMap<>();

    /**
     * Creates an instance of {@link RetryOptions} with values for {@code baseDelay} and {@code maxDelay}. Use this
     * constructor for exponential retry delay strategy.
     *
     * @param maxRetries The maximum number of retry attempts to be made.
     * @param baseDelay The base delay duration for retry.
     * @param maxDelay The max delay duration for retry.
     */
    public RetryOptions(int maxRetries, Duration baseDelay, Duration maxDelay) {
        Objects.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Objects.requireNonNull(maxDelay, "'maxDelay' cannot be null.");
        if (maxRetries < 0) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
        this.fixedDelay = null;
    }

    /**
     * Creates an instance of {@link RetryOptions} with values for {@code fixedDelay}. Use this constructor for
     * fixed retry delay strategy.
     *
     * @param maxRetries The maximum number of retry attempts to be made.
     * @param fixedDelay The fixed delay duration between retry attempts.
     */
    public RetryOptions(int maxRetries, Duration fixedDelay) {
        Objects.requireNonNull(fixedDelay, "'fixedDelay' cannot be null.");
        if (maxRetries < 0) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.fixedDelay = fixedDelay;
        this.baseDelay = null;
        this.maxDelay = null;
    }

    /**
     * Get the maximum number of retry attempts to be made.
     * @return the max retry attempts.
     */
    public int getMaxRetries() {
        return maxRetries;
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


    /**
     * Gets the headers that will be added to a retry request.
     * @return The headers that will be added to a retry request.
     */
    public HashMap<HeaderName, Duration> getRetryHeaders() {
        return retryHeaders;
    }

    /**
     * Sets the headers that will be added to a retry request.
     * @param retryHeaders the map of headers to add to a retry request.
     * @return The updated {@link RetryOptions} object.
     */
    public RetryOptions setRetryHeaders(HashMap<HeaderName, Duration> retryHeaders) {
        this.retryHeaders = retryHeaders;
        return this;
    }
}
