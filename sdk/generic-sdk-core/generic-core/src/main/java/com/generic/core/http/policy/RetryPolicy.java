// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.models.HeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipelineNextPolicy;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.implementation.http.policy.ExponentialBackoff;
import com.generic.core.implementation.http.policy.RetryStrategy;
import com.generic.core.implementation.util.ImplUtils;
import com.generic.core.implementation.util.LoggingKeys;
import com.generic.core.models.Headers;
import com.generic.core.util.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static com.generic.core.implementation.util.CoreUtils.isNullOrEmpty;

/**
 * A pipeline policy that retries when a recoverable HTTP error or exception occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    // RetryPolicy is a commonly used policy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(RetryPolicy.class);

    private final RetryStrategy retryStrategy;
    private final HeaderName retryAfterHeader;
    private final ChronoUnit retryAfterTimeUnit;

    /**
     * Creates {@link RetryPolicy} using {@link ExponentialBackoff#ExponentialBackoff()} as the {@link RetryStrategy}.
     */
    public RetryPolicy() {
        this(new ExponentialBackoff(), null, null);
    }

    /**
     * Creates {@link RetryPolicy} using {@link ExponentialBackoff#ExponentialBackoff()} as the {@link RetryStrategy}
     * and uses {@code retryAfterHeader} to look up the wait period in the returned {@link HttpResponse} to calculate
     * the retry delay when a recoverable HTTP error is returned.
     *
     * @param retryAfterHeader The HTTP header, such as {@code Retry-After} or {@code x-ms-retry-after-ms}, to lookup
     * for the retry delay. If the value is null, {@link RetryStrategy#calculateRetryDelay(int)} will compute the delay
     * and ignore the delay provided in response header.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. Null is valid if, and only if,
     * {@code retryAfterHeader} is null.
     *
     * @throws NullPointerException When {@code retryAfterTimeUnit} is null and {@code retryAfterHeader} is not null.
     */
    public RetryPolicy(String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this(new ExponentialBackoff(), retryAfterHeader, retryAfterTimeUnit);
    }

    /**
     * Creates {@link RetryPolicy} with the provided {@link RetryStrategy} and default {@link ExponentialBackoff} as
     * {@link RetryStrategy}. It will use provided {@code retryAfterHeader} in {@link HttpResponse} headers for
     * calculating retry delay.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @param retryAfterHeader The HTTP header, such as 'Retry-After' or 'x-ms-retry-after-ms', to lookup for the retry
     * delay. If the value is null, {@link RetryPolicy} will use the retry strategy to compute the delay and ignore the
     * delay provided in response header.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. null is valid if, and only if,
     * {@code retryAfterHeader} is null.
     *
     * @throws NullPointerException If {@code retryStrategy} is null or when {@code retryAfterTimeUnit} is null and
     * {@code retryAfterHeader} is not null.
     */
    public RetryPolicy(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryAfterHeader = HeaderName.fromString(retryAfterHeader);
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        if (!isNullOrEmpty(retryAfterHeader)) {
            Objects.requireNonNull(retryAfterTimeUnit, "'retryAfterTimeUnit' cannot be null.");
        }
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     *
     * @throws NullPointerException If {@code retryStrategy} is null.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        this(retryStrategy, null, null);
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryOptions}.
     *
     * @param retryOptions The {@link RetryOptions} used to configure this {@link RetryPolicy}.
     *
     * @throws NullPointerException If {@code retryOptions} is null.
     */
    public RetryPolicy(RetryOptions retryOptions) {
        this(ImplUtils.getRetryStrategyFromOptions(retryOptions), null, null);
    }

    @Override
    public HttpResponse<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        try {
            return attempt(httpRequest, next, 0, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpResponse<?> attempt(final HttpRequest httpRequest, final HttpPipelineNextPolicy next,
                                    final int tryCount, final List<Throwable> suppressed) throws IOException {
        httpRequest.getMetadata().setRetryCount(tryCount + 1);

        HttpResponse<?> httpResponse;

        try {
            httpResponse = next.clone().process();
        } catch (RuntimeException err) {
            if (shouldRetryException(retryStrategy, err, tryCount)) {
                logRetryWithError(LOGGER.atVerbose(), tryCount, "Error resume.", err);

                try {
                    Thread.sleep(retryStrategy.calculateRetryDelay(tryCount).toMillis());
                } catch (InterruptedException ie) {
                    err.addSuppressed(ie);
                    throw LOGGER.logThrowableAsError(err);
                }

                List<Throwable> suppressedLocal = suppressed == null ? new LinkedList<>() : suppressed;

                suppressedLocal.add(err);

                return attempt(httpRequest, next, tryCount + 1, suppressedLocal);
            } else {
                logRetryWithError(LOGGER.atError(), tryCount, "Retry attempts have been exhausted.", err);

                if (suppressed != null) {
                    suppressed.forEach(err::addSuppressed);
                }

                throw LOGGER.logThrowableAsError(err);
            }
        }

        if (shouldRetry(retryStrategy, httpResponse, tryCount)) {
            final Duration delayDuration =
                determineDelayDuration(httpResponse, tryCount, retryStrategy, retryAfterHeader, retryAfterTimeUnit);

            logRetry(tryCount, delayDuration);

            httpResponse.close();

            try {
                Thread.sleep(retryStrategy.calculateRetryDelay(tryCount).toMillis());
            } catch (InterruptedException ie) {
                throw LOGGER.logThrowableAsError(new RuntimeException(ie));
            }

            return attempt(httpRequest, next, tryCount + 1, suppressed);
        } else {
            if (tryCount >= retryStrategy.getMaxRetries()) {
                logRetryExhausted(tryCount);
            }

            return httpResponse;
        }
    }

    private static boolean shouldRetry(RetryStrategy retryStrategy, HttpResponse<?> response, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries() && retryStrategy.shouldRetry(response);
    }

    private static boolean shouldRetryException(RetryStrategy retryStrategy, Exception exception, int tryCount) {
        // Check if there are any retry attempts still available.
        if (tryCount >= retryStrategy.getMaxRetries()) {
            return false;
        }

        // Unwrap the throwable.
        Throwable causalThrowable = exception.getCause();

        // Check all causal exceptions in the exception chain.
        while (causalThrowable instanceof IOException || causalThrowable instanceof TimeoutException) {
            if (retryStrategy.shouldRetryException((Exception) causalThrowable)) {
                return true;
            }

            causalThrowable = causalThrowable.getCause();
        }

        // Finally just return false as this can't be retried.
        return false;
    }

    private static void logRetry(int tryCount, Duration delayDuration) {
        LOGGER.atVerbose()
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .addKeyValue(LoggingKeys.DURATION_MS_KEY, delayDuration.toMillis())
            .log("Retrying.");
    }

    private static void logRetryExhausted(int tryCount) {
        LOGGER.atInfo()
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .log("Retry attempts have been exhausted.");
    }

    private static void logRetryWithError(ClientLogger.LoggingEventBuilder loggingEventBuilder, int tryCount,
                                          String format, Throwable throwable) {
        loggingEventBuilder
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .log(format, throwable);
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    static Duration determineDelayDuration(HttpResponse<?> response, int tryCount, RetryStrategy retryStrategy,
                                           HeaderName retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        // If the retry after header hasn't been configured, attempt to look up the well-known headers.
        if (retryAfterHeader == null) {
            return getWellKnownRetryDelay(response.getHeaders(), tryCount, retryStrategy, OffsetDateTime::now);
        }

        String retryHeaderValue = response.getHeaderValue(retryAfterHeader);

        // Retry header is missing or empty, return the default delay duration.
        if (isNullOrEmpty(retryHeaderValue)) {
            return retryStrategy.calculateRetryDelay(tryCount);
        }

        // Use the response delay duration, the server returned it for a reason.
        return Duration.of(Integer.parseInt(retryHeaderValue), retryAfterTimeUnit);
    }

    /*
     * Determines the delay duration that should be waited before retrying using the well-known retry headers.
     */
    static Duration getWellKnownRetryDelay(Headers responseHeaders, int tryCount, RetryStrategy retryStrategy,
        Supplier<OffsetDateTime> nowSupplier) {
        Duration retryDelay = ImplUtils.getRetryAfterFromHeaders(responseHeaders, nowSupplier);
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return the default delay duration.
        return retryStrategy.calculateRetryDelay(tryCount);
    }

    /**
     * The configuration for retries.
     */
    public static class RetryOptions {
        private final ExponentialBackoffOptions exponentialBackoffOptions;
        private final FixedDelayOptions fixedDelayOptions;

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

    /**
     * The configuration for a fixed-delay retry that has a fixed delay duration between each retry attempt.
     */
    public static class FixedDelayOptions {
        private static final ClientLogger LOGGER = new ClientLogger(FixedDelayOptions.class);
        private final int maxRetries;
        private final Duration delay;

        /**
         * Creates an instance of {@link FixedDelayOptions}.
         *
         * @param maxRetries The max number of retry attempts that can be made.
         * @param delay The fixed delay duration between retry attempts.
         * @throws IllegalArgumentException If {@code maxRetries} is negative.
         * @throws NullPointerException If {@code delay} is {@code null}.
         */
        public FixedDelayOptions(int maxRetries, Duration delay) {
            if (maxRetries < 0) {
                throw LOGGER.logThrowableAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
            }
            this.maxRetries = maxRetries;
            this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
        }

        /**
         * Gets the max retry attempts that can be made.
         *
         * @return The max retry attempts that can be made.
         */
        public int getMaxRetries() {
            return maxRetries;
        }

        /**
         * Gets the max retry attempts that can be made.
         *
         * @return The max retry attempts that can be made.
         */
        public Duration getDelay() {
            return delay;
        }
    }

    /**
     * The configuration for exponential backoff that has a delay duration that exponentially
     * increases with each retry attempt until an upper bound is reached after which every retry attempt is delayed by
     * the provided max delay duration.
     */
    public static class ExponentialBackoffOptions {
        private static final ClientLogger LOGGER = new ClientLogger(ExponentialBackoffOptions.class);

        private Integer maxRetries;
        private Duration baseDelay;
        private Duration maxDelay;

        /**
         * Creates a new instance of {@link ExponentialBackoffOptions}.
         */
        public ExponentialBackoffOptions() {
        }

        /**
         * Gets the max retry attempts that can be made.
         *
         * @return The max retry attempts that can be made.
         */
        public Integer getMaxRetries() {
            return maxRetries;
        }

        /**
         * Sets the max retry attempts that can be made.
         *
         * @param maxRetries the max retry attempts that can be made.
         * @throws IllegalArgumentException if {@code maxRetries} is less than 0.
         * @return The updated {@link ExponentialBackoffOptions}
         */
        public ExponentialBackoffOptions setMaxRetries(Integer maxRetries) {
            if (maxRetries != null && maxRetries < 0) {
                throw LOGGER.logThrowableAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
            }
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Gets the base delay duration for retry.
         *
         * @return The base delay duration for retry.
         */
        public Duration getBaseDelay() {
            return baseDelay;
        }

        /**
         * Sets the base delay duration for retry.
         *
         * @param baseDelay the base delay duration for retry.
         * @throws IllegalArgumentException if {@code baseDelay} is less than or equal
         * to 0 or {@code maxDelay} has been set and is less than {@code baseDelay}.
         * @return The updated {@link ExponentialBackoffOptions}
         */
        public ExponentialBackoffOptions setBaseDelay(Duration baseDelay) {
            validateDelays(baseDelay, maxDelay);
            this.baseDelay = baseDelay;
            return this;
        }

        /**
         * Gets the max delay duration for retry.
         *
         * @return The max delay duration for retry.
         */
        public Duration getMaxDelay() {
            return maxDelay;
        }

        /**
         * Sets the max delay duration for retry.
         *
         * @param maxDelay the max delay duration for retry.
         * @throws IllegalArgumentException if {@code maxDelay} is less than or equal
         * to 0 or {@code baseDelay} has been set and is more than {@code maxDelay}.
         * @return The updated {@link ExponentialBackoffOptions}
         */
        public ExponentialBackoffOptions setMaxDelay(Duration maxDelay) {
            validateDelays(baseDelay, maxDelay);
            this.maxDelay = maxDelay;
            return this;
        }

        private void validateDelays(Duration baseDelay, Duration maxDelay) {
            if (baseDelay != null && (baseDelay.isZero() || baseDelay.isNegative())) {
                throw LOGGER.logThrowableAsError(new IllegalArgumentException("'baseDelay' cannot be negative or 0."));
            }
            if (maxDelay != null && (maxDelay.isZero() || maxDelay.isNegative())) {
                throw LOGGER.logThrowableAsError(new IllegalArgumentException("'maxDelay' cannot be negative or 0."));
            }

            if (baseDelay != null && maxDelay != null && baseDelay.compareTo(maxDelay) > 0) {
                throw LOGGER.logThrowableAsError(
                    new IllegalArgumentException("'baseDelay' cannot be greater than 'maxDelay'."));
            }
        }
    }
}
