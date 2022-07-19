// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.implementation.logging.LoggingKeys;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Supplier;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * A pipeline policy that retries when a recoverable HTTP error or exception occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    // RetryPolicy is a commonly used policy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(RetryPolicy.class);

    private final RetryStrategy retryStrategy;
    private final String retryAfterHeader;
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
     * @throws NullPointerException If {@code retryStrategy} is null or when {@code retryAfterTimeUnit} is null and
     * {@code retryAfterHeader} is not null.
     */
    public RetryPolicy(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryAfterHeader = retryAfterHeader;
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        if (!isNullOrEmpty(retryAfterHeader)) {
            Objects.requireNonNull(retryAfterTimeUnit, "'retryAfterTimeUnit' cannot be null.");
        }
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException If {@code retryStrategy} is null.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        this(retryStrategy, null, null);
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryOptions}.
     *
     * @param retryOptions The {@link RetryOptions} used to configure this {@link RetryPolicy}.
     * @throws NullPointerException If {@code retryOptions} is null.
     */
    public RetryPolicy(RetryOptions retryOptions) {
        this(
            getRetryStrategyFromOptions(
                Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.")),
            null, null);
    }

    private static RetryStrategy getRetryStrategyFromOptions(RetryOptions retryOptions) {
        if (retryOptions.getExponentialBackoffOptions() != null) {
            return new ExponentialBackoff(retryOptions.getExponentialBackoffOptions());
        } else if (retryOptions.getFixedDelayOptions() != null) {
            return new FixedDelay(retryOptions.getFixedDelayOptions());
        } else {
            // This should never happen.
            throw new IllegalArgumentException("'retryOptions' didn't define any retry strategy options");
        }
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptAsync(context, next, context.getHttpRequest(), 0);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return attemptSync(context, next, context.getHttpRequest(), 0);
    }


    private Mono<HttpResponse> attemptAsync(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next,
        final HttpRequest originalHttpRequest, final int tryCount) {
        context.setHttpRequest(originalHttpRequest.copy());
        context.setData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT, tryCount + 1);
        return next.clone().process()
            .flatMap(httpResponse -> {
                if (shouldRetry(httpResponse, tryCount)) {
                    final Duration delayDuration = determineDelayDuration(httpResponse, tryCount, retryStrategy,
                        retryAfterHeader, retryAfterTimeUnit);
                    logRetry(tryCount, delayDuration);

                    httpResponse.close();

                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(delayDuration);
                } else {
                    if (tryCount >= retryStrategy.getMaxRetries()) {
                        logRetryExhausted(tryCount);
                    }
                    return Mono.just(httpResponse);
                }
            })
            .onErrorResume(Exception.class, err -> {
                if (shouldRetryException(err, tryCount)) {
                    logRetryWithError(LOGGER.atVerbose(), tryCount, "Error resume.", err);
                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(retryStrategy.calculateRetryDelay(tryCount));
                } else {
                    logRetryWithError(LOGGER.atError(), tryCount, "Retry attempts have been exhausted.", err);
                    return Mono.error(err);
                }
            });
    }

    private HttpResponse attemptSync(final HttpPipelineCallContext context, final HttpPipelineNextSyncPolicy next,
                                     final HttpRequest originalHttpRequest, final int tryCount) {
        context.setHttpRequest(originalHttpRequest.copy());
        context.setData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT, tryCount + 1);
        HttpResponse httpResponse;
        try {
            httpResponse = next.clone().processSync();
        } catch (RuntimeException err) {
            Throwable throwable = Exceptions.unwrap(err);
            Throwable cause = throwable.getCause();
            if (shouldRetryException(throwable, tryCount) || shouldRetryException(cause, tryCount)) {
                logRetryWithError(LOGGER.atVerbose(), tryCount, "Error resume.", throwable);
                try {
                    Thread.sleep(retryStrategy.calculateRetryDelay(tryCount).toMillis());
                } catch (InterruptedException ie) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(ie));
                }
                return attemptSync(context, next, originalHttpRequest, tryCount + 1);
            } else {
                logRetryWithError(LOGGER.atError(), tryCount, "Retry attempts have been exhausted.", throwable);
                throw LOGGER.logExceptionAsError(err);
            }
        }
        if (shouldRetry(httpResponse, tryCount)) {
            final Duration delayDuration = determineDelayDuration(httpResponse, tryCount, retryStrategy,
                retryAfterHeader, retryAfterTimeUnit);
            logRetry(tryCount, delayDuration);

            httpResponse.close();

            try {
                Thread.sleep(retryStrategy.calculateRetryDelay(tryCount).toMillis());
            } catch (InterruptedException ie) {
                throw LOGGER.logExceptionAsError(new RuntimeException(ie));
            }
            return attemptSync(context, next, originalHttpRequest, tryCount + 1);
        } else {
            if (tryCount >= retryStrategy.getMaxRetries()) {
                logRetryExhausted(tryCount);
            }
            return httpResponse;
        }
    }
    private boolean shouldRetry(HttpResponse response, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries() && retryStrategy.shouldRetry(response);
    }

    private boolean shouldRetryException(Throwable throwable, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries() && retryStrategy.shouldRetryException(throwable);
    }

    private void logRetry(int tryCount, Duration delayDuration) {
        LOGGER.atVerbose()
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .addKeyValue(LoggingKeys.DURATION_MS_KEY, delayDuration.toMillis())
            .log("Retrying.");
    }

    private void logRetryExhausted(int tryCount) {
        LOGGER.atInfo()
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .log("Retry attempts have been exhausted.");
    }

    private void logRetryWithError(LoggingEventBuilder loggingEventBuilder, int tryCount, String format,
        Throwable throwable) {
        loggingEventBuilder
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .log(format, throwable);
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    static Duration determineDelayDuration(HttpResponse response, int tryCount, RetryStrategy retryStrategy,
        String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        // If the retry after header hasn't been configured, attempt to look up the well-known headers.
        if (isNullOrEmpty(retryAfterHeader)) {
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
    static Duration getWellKnownRetryDelay(HttpHeaders responseHeaders, int tryCount, RetryStrategy retryStrategy,
        Supplier<OffsetDateTime> nowSupplier) {
        Duration retryDelay = ImplUtils.getRetryAfterFromHeaders(responseHeaders, nowSupplier);
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return the default delay duration.
        return retryStrategy.calculateRetryDelay(tryCount);
    }
}
