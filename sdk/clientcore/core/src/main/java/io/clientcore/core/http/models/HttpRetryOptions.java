// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.http.pipeline.HttpRequestRetryCondition;
import io.clientcore.core.util.ClientLogger;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Options to configure the retry policy's behavior.
 */
public final class HttpRetryOptions {
    private static final ClientLogger LOGGER = new ClientLogger(HttpRetryOptions.class);
    private final int maxRetries;
    private final Duration baseDelay;
    private final Duration maxDelay;
    private final Duration fixedDelay;
    private Predicate<HttpRequestRetryCondition> shouldRetryCondition;
    private Function<HttpHeaders, Duration> delayFromHeaders;

    /**
     * Creates an instance of {@link HttpRetryOptions} with values for {@code baseDelay} and {@code maxDelay}. Use this
     * constructor for exponential retry delay strategy.
     *
     * @param maxRetries The maximum number of retry attempts to be made.
     * @param baseDelay The base delay duration for retry.
     * @param maxDelay The max delay duration for retry.
     */
    public HttpRetryOptions(int maxRetries, Duration baseDelay, Duration maxDelay) {
        Objects.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Objects.requireNonNull(maxDelay, "'maxDelay' cannot be null.");
        if (maxRetries < 0) {
            LOGGER.atVerbose()
                .log("Max retries cannot be less than 0. Using 3 retries as the maximum.");
            maxRetries = 3;
        }
        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
        this.maxRetries = maxRetries;
        this.fixedDelay = null;
    }

    /**
     * Creates an instance of {@link HttpRetryOptions} with values for {@code fixedDelay}. Use this constructor for
     * fixed retry delay strategy.
     *
     * @param maxRetries The maximum number of retry attempts to be made.
     * @param fixedDelay The fixed delay duration between retry attempts.
     */
    public HttpRetryOptions(int maxRetries, Duration fixedDelay) {
        Objects.requireNonNull(fixedDelay, "'fixedDelay' cannot be null.");
        if (maxRetries < 0) {
            LOGGER.atVerbose()
                .log("Max retries cannot be less than 0. Using 3 retries as the maximum.");
            maxRetries = 3;
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
    public Predicate<HttpRequestRetryCondition> getShouldRetryCondition() {
        return shouldRetryCondition;
    }

    /**
     * Sets the predicate that determines if a retry should be attempted.
     * <p>
     * If null, the default behavior is to retry HTTP responses with status codes 408, 429, and any 500 status code that
     * isn't 501 or 505. And to retry any {@link Exception}.
     *
     * @param shouldRetryCondition The predicate that determines if a retry should be attempted for the given
     * {@link Response}.
     * @return The updated {@link HttpRetryOptions} object.
     */
    public HttpRetryOptions setShouldRetryCondition(Predicate<HttpRequestRetryCondition> shouldRetryCondition) {
        this.shouldRetryCondition = shouldRetryCondition;
        return this;
    }

    /**
     * Gets the headers that will be added to a retry request.
     * @return The headers that will be added to a retry request.
     */
    public Function<HttpHeaders, Duration> getDelayFromHeaders() {
        return delayFromHeaders;
    }

    /**
     * Sets the headers that will be added to a retry request.
     * @param delayFromHeaders the map of headers to add to a retry request.
     * @return The updated {@link HttpRetryOptions} object.
     */
    public HttpRetryOptions setDelayFromHeaders(Function<HttpHeaders, Duration> delayFromHeaders) {
        this.delayFromHeaders = delayFromHeaders;
        return this;
    }
}
