// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpRetryOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.implementation.util.LoggingKeys;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.configuration.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;
import static io.clientcore.core.util.configuration.Configuration.PROPERTY_REQUEST_RETRY_COUNT;

/**
 * A pipeline policy that retries when a recoverable HTTP error or exception occurs.
 */
public class HttpRetryPolicy implements HttpPipelinePolicy {
    // RetryPolicy is a commonly used policy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpRetryPolicy.class);
    private final int maxRetries;
    private final Function<HttpHeaders, Duration> delayFromHeaders;
    private final Duration baseDelay;
    private final Duration maxDelay;
    private final Duration fixedDelay;
    private final Predicate<HttpRequestRetryCondition> shouldRetryCondition;
    private static final int DEFAULT_MAX_RETRIES;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(8);
    private static final double JITTER_FACTOR = 0.05;

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
                LOGGER.atVerbose()
                    .addKeyValue("property", PROPERTY_REQUEST_RETRY_COUNT)
                    .log("Invalid property value. Using 3 retries as the maximum.");
            }
        }

        DEFAULT_MAX_RETRIES = defaultMaxRetries;
    }

    /**
     * Creates {@link HttpRetryPolicy} using exponential backoff delay, defaulting to
     * three retries, a base delay of 800 milliseconds, and a maximum delay of 8 seconds.
     */
    public HttpRetryPolicy() {
        this(DEFAULT_BASE_DELAY, DEFAULT_MAX_DELAY, null, DEFAULT_MAX_RETRIES, null, null);
    }

    /**
     * Creates a {@link HttpRetryPolicy} with the provided {@link HttpRetryOptions}.
     *
     * @param retryOptions The {@link HttpRetryOptions} used to configure this {@link HttpRetryPolicy}.
     * @throws NullPointerException If {@code retryOptions} is null.
     */
    public HttpRetryPolicy(HttpRetryOptions retryOptions) {
        this(retryOptions.getBaseDelay(), retryOptions.getMaxDelay(), retryOptions.getFixedDelay(),
            retryOptions.getMaxRetries(), retryOptions.getDelayFromHeaders(), retryOptions.getShouldRetryCondition());
    }

    /**
     * Creates {@link HttpRetryPolicy} with the provided {@link HttpRetryOptions}.
     * <p>
     * By default, the retry policy uses an exponential backoff delay. If both 'fixedDelay' and 'baseDelay' are null,
     * the 'baseDelay' is set to 800 milliseconds and the 'maxDelay' if not provided is set to 8 seconds.
     * </p>
     * @param baseDelay The base delay duration for retry.
     * @param maxDelay The max delay duration for retry.
     * @param fixedDelay The fixed delay duration between retry attempts.
     * @param maxRetries The maximum number of retry attempts to be made.
     * @param delayFromHeaders The header set to look for retry after duration.
     * @param shouldRetryCondition The condition that determines if a request should be retried.
     * @throws NullPointerException If {@code retryStrategy} is null or when {@code retryAfterTimeUnit} is null and
     * {@code retryAfterHeader} is not null.
     */
    HttpRetryPolicy(Duration baseDelay, Duration maxDelay, Duration fixedDelay, int maxRetries,
                    Function<HttpHeaders, Duration> delayFromHeaders, Predicate<HttpRequestRetryCondition> shouldRetryCondition) {
        if (fixedDelay == null && baseDelay == null) {
            this.baseDelay = DEFAULT_BASE_DELAY;
            this.maxDelay = DEFAULT_MAX_DELAY;
        } else {
            this.baseDelay = baseDelay;
            this.maxDelay = maxDelay;
        }
        this.fixedDelay = fixedDelay;
        this.maxRetries = maxRetries;
        this.delayFromHeaders = delayFromHeaders;
        this.shouldRetryCondition = shouldRetryCondition;
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        return attempt(httpRequest, next, 0, null);
    }

    /*
     * Determines the delay duration that should be waited before retrying using the well-known retry headers.
     */
    private Duration getWellKnownRetryDelay(HttpHeaders responseHeaders, int tryCount, Supplier<OffsetDateTime> nowSupplier) {
        Duration retryDelay = ImplUtils.getRetryAfterFromHeaders(responseHeaders, nowSupplier);
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return the default delay duration.
        return calculateRetryDelay(tryCount);
    }

    private Response<?> attempt(final HttpRequest httpRequest, final HttpPipelineNextPolicy next, final int tryCount,
                                final List<Exception> suppressed) {
        HttpRequestAccessHelper.setRetryCount(httpRequest, tryCount + 1);

        Response<?> response;

        try {
            response = next.clone().process();
        } catch (RuntimeException err) {
            if (shouldRetryException(err, tryCount, suppressed)) {
                logRetryWithError(LOGGER.atVerbose(), tryCount, "Error resume.", err);

                boolean interrupted = false;
                try {
                    Thread.sleep(calculateRetryDelay(tryCount).toMillis());
                } catch (InterruptedException ie) {
                    interrupted = true;
                    err.addSuppressed(ie);
                }

                if (interrupted) {
                    throw LOGGER.logThrowableAsError(err);
                }

                List<Exception> suppressedLocal = suppressed == null ? new LinkedList<>() : suppressed;

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

        if (shouldRetryResponse(response, tryCount, suppressed)) {
            final Duration delayDuration = determineDelayDuration(response, tryCount,
                delayFromHeaders);

            logRetry(tryCount, delayDuration);

            try {
                response.close();
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }

            try {
                Thread.sleep(calculateRetryDelay(tryCount).toMillis());
            } catch (InterruptedException ie) {
                throw LOGGER.logThrowableAsError(new RuntimeException(ie));
            }

            return attempt(httpRequest, next, tryCount + 1, suppressed);
        } else {
            if (tryCount >= maxRetries) {
                logRetryExhausted(tryCount);
            }

            return response;
        }
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    private Duration determineDelayDuration(Response<?> response, int tryCount,
                                            Function<HttpHeaders, Duration> delayFromHeaders) {
        // If the retry after header hasn't been configured, attempt to look up the well-known headers.
        if (delayFromHeaders == null) {
            return getWellKnownRetryDelay(response.getHeaders(), tryCount, OffsetDateTime::now);
        }

        Duration delay = delayFromHeaders.apply(response.getHeaders());
        if (delay != null) {
            return delay;
        }

        // Retry header is missing or empty, return the default delay duration.
        return calculateRetryDelay(tryCount);
    }

    private boolean shouldRetryResponse(Response<?> response, int tryCount,
                                        List<Exception> retriedExceptions) {
        if (shouldRetryCondition != null) {
            return tryCount < maxRetries && shouldRetryCondition.test(new HttpRequestRetryCondition(response, null, tryCount,
                retriedExceptions));
        } else {
            return tryCount < maxRetries && defaultShouldRetryCondition(new HttpRequestRetryCondition(response, null, tryCount,
                retriedExceptions));
        }
    }

    private boolean shouldRetryException(Exception exception, int tryCount,
                                         List<Exception> retriedExceptions) {
        // Check if there are any retry attempts still available.
        if (tryCount >= maxRetries) {
            return false;
        }

        // Unwrap the throwable.
        Throwable causalThrowable = exception.getCause();
        HttpRequestRetryCondition requestRetryCondition = new HttpRequestRetryCondition(null, exception, tryCount,
            retriedExceptions);

        // Check all causal exceptions in the exception chain.
        while (causalThrowable instanceof IOException || causalThrowable instanceof TimeoutException) {
            if (shouldRetryCondition != null) {
                if (shouldRetryCondition.test(requestRetryCondition)) {
                    return true;
                }
            } else {
                return defaultShouldRetryCondition(requestRetryCondition);
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
        LOGGER.atInfo().addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount).log("Retry attempts have been exhausted.");
    }

    private static void logRetryWithError(ClientLogger.LoggingEventBuilder loggingEventBuilder, int tryCount,
                                          String message, Throwable throwable) {
        loggingEventBuilder.addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount).log(message, throwable);
    }

    private Duration calculateRetryDelay(int retryAttempts) {
        // Return fixed delay if it is set
        if (fixedDelay != null) {
            return fixedDelay;
        }

        // Otherwise, calculate exponential delay
        long baseDelayNanos = baseDelay.toNanos();
        long maxDelayNanos = maxDelay.toNanos();
        // Introduce a small amount of jitter to base delay
        long delayWithJitterInNanos = ThreadLocalRandom.current()
            .nextLong((long) (baseDelayNanos * (1 - JITTER_FACTOR)), (long) (baseDelayNanos * (1 + JITTER_FACTOR)));
        return Duration.ofNanos(Math.min((1L << retryAttempts) * delayWithJitterInNanos, maxDelayNanos));
    }

    private boolean defaultShouldRetryCondition(HttpRequestRetryCondition requestRetryCondition) {
        if (requestRetryCondition.getResponse() != null) {
            int code = requestRetryCondition.getResponse().getStatusCode();
            return (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
                && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED && code != HttpURLConnection.HTTP_VERSION));
        } else {
            return requestRetryCondition.getException() instanceof Exception;
        }
    }
}
