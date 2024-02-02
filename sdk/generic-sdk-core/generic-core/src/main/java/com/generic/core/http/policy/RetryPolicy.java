// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.models.RetryOptions;
import com.generic.core.http.pipeline.HttpPipelineNextPolicy;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.implementation.http.policy.ExponentialBackoffDelay;
import com.generic.core.implementation.util.ImplUtils;
import com.generic.core.implementation.util.LoggingKeys;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.configuration.Configuration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.generic.core.implementation.util.CoreUtils.isNullOrEmpty;
import static com.generic.core.util.configuration.Configuration.PROPERTY_REQUEST_RETRY_COUNT;

/**
 * A pipeline policy that retries when a recoverable HTTP error or exception occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    // RetryPolicy is a commonly used policy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(RetryPolicy.class);
    private int maxRetries;
    private RetryStrategy retryStrategy;
    private final Map<HeaderName, Duration> retryHeaders;

    private static final int DEFAULT_MAX_RETRIES;

    static {
        String envDefaultMaxRetries = Configuration.getGlobalConfiguration().get(PROPERTY_REQUEST_RETRY_COUNT);

        int defaultMaxRetries = 3;
        if (!isNullOrEmpty(envDefaultMaxRetries)) {
            try {
                defaultMaxRetries = Integer.parseInt(envDefaultMaxRetries);
                if (defaultMaxRetries < 0) {
                    defaultMaxRetries = 3;
                }
            } catch (NumberFormatException ignored) {
                LOGGER.log(ClientLogger.LogLevel.VERBOSE, () -> PROPERTY_REQUEST_RETRY_COUNT + " was loaded but is an invalid "
                    + "number. Using 3 retries as the maximum.");
            }
        }

        DEFAULT_MAX_RETRIES = defaultMaxRetries;
    }

    /**
     * Creates {@link RetryPolicy} using {@link ExponentialBackoffDelay} as the {@link RetryStrategy} and defaulting to
     * three retries.
     */
    public RetryPolicy() {
        this(new ExponentialBackoffDelay(), DEFAULT_MAX_RETRIES, null);
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryOptions}.
     *
     * @param retryOptions The {@link RetryOptions} used to configure this {@link RetryPolicy}.
     * @throws NullPointerException If {@code retryOptions} is null.
     */
    RetryPolicy(RetryOptions retryOptions) {
        this(ImplUtils.getRetryStrategyFromOptions(retryOptions), retryOptions.getMaxRetries(), retryOptions.getRetryHeaders());
    }

    /**
     * Creates {@link RetryPolicy} with the provided {@link RetryStrategy} and default {@link ExponentialBackoffDelay} as
     * {@link RetryStrategy}. It will use provided {@code retryAfterHeader} in {@link HttpResponse} headers for
     * calculating retry delay.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @param maxRetries The maximum number of retry attempts to be made.
     * @param retryHeaders The header set to look for retry after duration.
     * @throws NullPointerException If {@code retryStrategy} is null or when {@code retryAfterTimeUnit} is null and
     * {@code retryAfterHeader} is not null.
     */
    RetryPolicy(RetryStrategy retryStrategy, int maxRetries, Map<HeaderName, Duration> retryHeaders) {
        this.maxRetries = maxRetries;
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryHeaders = retryHeaders;
    }

    @Override
    public HttpResponse process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        return attempt(httpRequest, next, 0, null);
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
     * The interface for determining the retry strategy of clients.
     */
    @FunctionalInterface
    public interface RetryStrategy {

        /**
         * Computes the delay between each retry.
         *
         * @param retryAttempts The number of retry attempts completed so far.
         * @return The delay duration before the next retry.
         */
        Duration calculateRetryDelay(int retryAttempts);

        default boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition) {
            if (requestRetryCondition.getResponse() != null) {
                int code = requestRetryCondition.getResponse().getStatusCode();
                return (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
                    || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
                    && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
                    && code != HttpURLConnection.HTTP_VERSION));
            } else {
                return requestRetryCondition.getThrowable() instanceof Exception;
            }
        }
    }

    private HttpResponse attempt(final HttpRequest httpRequest, final HttpPipelineNextPolicy next,
                                 final int tryCount, final List<Throwable> suppressed) {
        httpRequest.getMetadata().setRetryCount(tryCount + 1);

        HttpResponse httpResponse;

        try {
            httpResponse = next.clone().process();
        } catch (RuntimeException err) {
            if (shouldRetryException(retryStrategy, err, tryCount, suppressed)) {
                logRetryWithError(LOGGER.atVerbose(), tryCount, "Error resume.", err);

                try {
                    Thread.sleep(retryStrategy.calculateRetryDelay(tryCount).toMillis());
                } catch (InterruptedException ie) {
                    throw LOGGER.logThrowableAsError(new RuntimeException(ie));
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

        if (shouldRetryResponse(retryStrategy, httpResponse, tryCount, suppressed)) {
            final Duration delayDuration =
                determineDelayDuration(httpResponse, tryCount, retryStrategy, retryHeaders);

            logRetry(tryCount, delayDuration);

            httpResponse.close();

            try {
                Thread.sleep(retryStrategy.calculateRetryDelay(tryCount).toMillis());
            } catch (InterruptedException ie) {
                throw LOGGER.logThrowableAsError(new RuntimeException(ie));
            }

            return attempt(httpRequest, next, tryCount + 1, suppressed);
        } else {
            if (tryCount >= maxRetries) {
                logRetryExhausted(tryCount);
            }

            return httpResponse;
        }
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    private static Duration determineDelayDuration(HttpResponse response, int tryCount, RetryStrategy retryStrategy,
                                                   Map<HeaderName, Duration> retryHeaders) {
        // If the retry after header hasn't been configured, attempt to look up the well-known headers.
        if (isNullOrEmpty(retryHeaders)) {
            return getWellKnownRetryDelay(response.getHeaders(), tryCount, retryStrategy, OffsetDateTime::now);
        }
        AtomicReference<Duration> retryHeaderValue = new AtomicReference<>();
        retryHeaders.forEach((key, value) -> {
            if (response.getHeaderValue(key) != null) {
                retryHeaderValue.set(value);
            }
        });

        // Retry header is missing or empty, return the default delay duration.
        if (retryHeaderValue.get() != null) {
            return retryStrategy.calculateRetryDelay(tryCount);
        }

        // Use the response delay duration, the server returned it for a reason.
        return retryHeaderValue.get();
    }


    private boolean shouldRetryResponse(RetryStrategy retryStrategy, HttpResponse response, int tryCount,
                                       List<Throwable> retriedExceptions) {
        return tryCount < maxRetries && retryStrategy.shouldRetryCondition(
            new RequestRetryCondition(response, null, tryCount, retriedExceptions));
    }

    private boolean shouldRetryException(RetryStrategy retryStrategy, Throwable throwable, int tryCount,
                                                List<Throwable> retriedExceptions) {
        // Check if there are any retry attempts still available.
        if (tryCount >= maxRetries) {
            return false;
        }

        // Unwrap the throwable.
        Throwable causalThrowable = throwable.getCause();
        RequestRetryCondition requestRetryCondition = new RequestRetryCondition(null, causalThrowable, tryCount,
            retriedExceptions);

        // Check all causal exceptions in the exception chain.
        while (causalThrowable instanceof IOException || causalThrowable instanceof TimeoutException) {
            if (retryStrategy.shouldRetryCondition(requestRetryCondition)) {
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

    private static void logRetryWithError(ClientLogger.LoggingEventBuilder loggingEventBuilder, int tryCount, String format,
                                          Throwable throwable) {
        loggingEventBuilder
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .log(format, throwable);
    }
}
