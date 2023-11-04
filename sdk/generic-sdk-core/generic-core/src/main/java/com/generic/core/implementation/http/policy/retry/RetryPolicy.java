// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy.retry;

import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipelineCallContext;
import com.generic.core.http.pipeline.HttpPipelineNextPolicy;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.http.policy.retry.RetryOptions;
import com.generic.core.implementation.http.policy.logging.HttpLoggingPolicy;
import com.generic.core.implementation.util.ImplUtils;
import com.generic.core.implementation.util.LoggingKeys;
import com.generic.core.models.Headers;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.logging.LoggingEventBuilder;

import java.io.IOException;
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
    private final HttpHeaderName retryAfterHeader;
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
        this.retryAfterHeader = HttpHeaderName.fromString(retryAfterHeader);
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
        this(ImplUtils.getRetryStrategyFromOptions(retryOptions), null, null);
    }

    @Override
    public HttpResponse process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attempt(context, next, context.getHttpRequest(), 0, null);
    }

    private HttpResponse attempt(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next,
                                     final HttpRequest originalHttpRequest, final int tryCount,
                                     final List<Throwable> suppressed) {
        context.setHttpRequest(originalHttpRequest.copy());
        context.setData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT, tryCount + 1);
        HttpResponse httpResponse;
        try {
            httpResponse = next.clone().process();
        } catch (RuntimeException err) {
            if (shouldRetryException(retryStrategy, err, tryCount)) {
                logRetryWithError(LOGGER.atVerbose(), tryCount, "Error resume.", err);
                try {
                    Thread.sleep(retryStrategy.calculateRetryDelay(tryCount).toMillis());
                } catch (InterruptedException ie) {
                    throw LOGGER.logThrowableAsError(new RuntimeException(ie));
                }

                List<Throwable> suppressedLocal = suppressed == null ? new LinkedList<>() : suppressed;
                suppressedLocal.add(err);
                return attempt(context, next, originalHttpRequest, tryCount + 1, suppressedLocal);
            } else {
                logRetryWithError(LOGGER.atError(), tryCount, "Retry attempts have been exhausted.", err);
                if (suppressed != null) {
                    suppressed.forEach(err::addSuppressed);
                }

                throw LOGGER.logThrowableAsError(err);
            }
        }

        if (shouldRetry(retryStrategy, httpResponse, tryCount)) {
            final Duration delayDuration = determineDelayDuration(httpResponse, tryCount, retryStrategy,
                retryAfterHeader, retryAfterTimeUnit);
            logRetry(tryCount, delayDuration);

            httpResponse.close();

            try {
                Thread.sleep(retryStrategy.calculateRetryDelay(tryCount).toMillis());
            } catch (InterruptedException ie) {
                throw LOGGER.logThrowableAsError(new RuntimeException(ie));
            }
            return attempt(context, next, originalHttpRequest, tryCount + 1, suppressed);
        } else {
            if (tryCount >= retryStrategy.getMaxRetries()) {
                logRetryExhausted(tryCount);
            }
            return httpResponse;
        }
    }
    private static boolean shouldRetry(RetryStrategy retryStrategy, HttpResponse response, int tryCount) {
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

    private static void logRetryWithError(LoggingEventBuilder loggingEventBuilder, int tryCount, String format,
                                          Throwable throwable) {
        loggingEventBuilder
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .log(format, throwable);
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    static Duration determineDelayDuration(HttpResponse response, int tryCount, RetryStrategy retryStrategy,
                                           HttpHeaderName retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
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
    public static Duration getWellKnownRetryDelay(Headers responseHeaders, int tryCount, RetryStrategy retryStrategy,
                                                  Supplier<OffsetDateTime> nowSupplier) {
        Duration retryDelay = ImplUtils.getRetryAfterFromHeaders(responseHeaders, nowSupplier);
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return the default delay duration.
        return retryStrategy.calculateRetryDelay(tryCount);
    }
}
