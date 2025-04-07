// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LoggingEvent;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.DateTimeRfc1123;
import io.clientcore.core.utils.configuration.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_RESEND_COUNT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.RETRY_DELAY_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.RETRY_MAX_ATTEMPT_COUNT_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.RETRY_WAS_LAST_ATTEMPT_KEY;
import static io.clientcore.core.implementation.instrumentation.LoggingEventNames.HTTP_RETRY_EVENT_NAME;
import static io.clientcore.core.utils.configuration.Configuration.MAX_RETRY_ATTEMPTS;

/**
 * A pipeline policy that retries when a recoverable HTTP error or exception occurs.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class HttpRetryPolicy implements HttpPipelinePolicy {
    // RetryPolicy is a commonly used policy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpRetryPolicy.class);
    private final int maxRetries;
    private final Function<HttpHeaders, Duration> delayFromHeaders;
    private final Duration baseDelay;
    private final Duration maxDelay;
    private final Duration fixedDelay;
    private final Predicate<HttpRetryCondition> shouldRetryCondition;
    private static final int DEFAULT_MAX_RETRIES;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(8);
    private static final double JITTER_FACTOR = 0.05;

    /**
     * HTTP response status code for {@code Too Many Requests}.
     */
    private static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    static {
        String envDefaultMaxRetries = Configuration.getGlobalConfiguration().get(MAX_RETRY_ATTEMPTS);

        int defaultMaxRetries = 3;
        if (!CoreUtils.isNullOrEmpty(envDefaultMaxRetries)) {
            try {
                defaultMaxRetries = Integer.parseInt(envDefaultMaxRetries);
                if (defaultMaxRetries < 0) {
                    defaultMaxRetries = 3;
                }
            } catch (NumberFormatException ignored) {
                LOGGER.atVerbose()
                    .addKeyValue("property", MAX_RETRY_ATTEMPTS)
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
        Function<HttpHeaders, Duration> delayFromHeaders, Predicate<HttpRetryCondition> shouldRetryCondition) {
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
    public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        return attempt(httpRequest, next, 0, null);
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.RETRY;
    }

    /*
     * Determines the delay duration that should be waited before retrying using the well-known retry headers.
     */
    private Duration getWellKnownRetryDelay(HttpHeaders responseHeaders, int tryCount,
        Supplier<OffsetDateTime> nowSupplier) {
        Duration retryDelay = getRetryAfterFromHeaders(responseHeaders, nowSupplier);
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return the default delay duration.
        return calculateRetryDelay(tryCount);
    }

    private Response<BinaryData> attempt(final HttpRequest httpRequest, final HttpPipelineNextPolicy next,
        final int tryCount, final List<Exception> suppressed) {

        // the tryCount is updated by the caller and represents the number of attempts made so far.
        // It can be used by the policies during the process call.
        HttpRequestAccessHelper.setTryCount(httpRequest, tryCount);

        final InstrumentationContext instrumentationContext = httpRequest.getContext().getInstrumentationContext();

        Response<BinaryData> response;
        ClientLogger logger = getLogger(httpRequest);

        try {
            response = next.copy().process();
        } catch (RuntimeException err) {
            if (shouldRetryException(err, tryCount, suppressed)) {

                Duration delayDuration = calculateRetryDelay(tryCount);
                logRetry(logger.atVerbose(), tryCount, delayDuration, err, false, instrumentationContext);

                boolean interrupted = false;
                long millis = delayDuration.toMillis();

                if (millis > 0) {
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException ie) {
                        interrupted = true;
                        err.addSuppressed(ie);
                    }
                }

                if (interrupted) {
                    throw logger.logThrowableAsError(err);
                }

                List<Exception> suppressedLocal = suppressed == null ? new LinkedList<>() : suppressed;

                suppressedLocal.add(err);

                return attempt(httpRequest, next, tryCount + 1, suppressedLocal);
            } else {
                logRetry(logger.atWarning(), tryCount, null, err, true, instrumentationContext);

                if (suppressed != null) {
                    suppressed.forEach(err::addSuppressed);
                }

                throw logger.logThrowableAsError(err);
            }
        }

        if (shouldRetryResponse(response, tryCount, suppressed)) {
            final Duration delayDuration = determineDelayDuration(response, tryCount, delayFromHeaders);

            logRetry(logger.atVerbose(), tryCount, delayDuration, null, false, instrumentationContext);

            try {
                response.close();
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }

            long millis = delayDuration.toMillis();
            if (millis > 0) {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException ie) {
                    throw LOGGER.logThrowableAsError(new RuntimeException(ie));
                }
            }

            return attempt(httpRequest, next, tryCount + 1, suppressed);
        } else {
            if (tryCount >= maxRetries) {
                // TODO (limolkova): do we have better heuristic to determine if we're retrying because of error
                // or because we got successful response?
                logRetry(logger.atWarning(), tryCount, null, null, true, instrumentationContext);
            }
            return response;
        }
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    private Duration determineDelayDuration(Response<BinaryData> response, int tryCount,
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

    private boolean shouldRetryResponse(Response<BinaryData> response, int tryCount,
        List<Exception> retriedExceptions) {
        if (shouldRetryCondition != null) {
            return tryCount < maxRetries
                && shouldRetryCondition.test(new HttpRetryCondition(response, null, tryCount, retriedExceptions));
        } else {
            return tryCount < maxRetries
                && defaultShouldRetryCondition(new HttpRetryCondition(response, null, tryCount, retriedExceptions));
        }
    }

    private boolean shouldRetryException(Exception exception, int tryCount, List<Exception> retriedExceptions) {
        // Check if there are any retry attempts still available.
        if (tryCount >= maxRetries) {
            return false;
        }

        // Unwrap the throwable.
        Throwable causalThrowable = exception.getCause();
        HttpRetryCondition requestRetryCondition = new HttpRetryCondition(null, exception, tryCount, retriedExceptions);

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

    private void logRetry(LoggingEvent log, int tryCount, Duration delayDuration, Throwable throwable, boolean lastTry,
        InstrumentationContext context) {
        if (log.isEnabled()) {
            log.addKeyValue(HTTP_REQUEST_RESEND_COUNT_KEY, tryCount)
                .addKeyValue(RETRY_MAX_ATTEMPT_COUNT_KEY, maxRetries)
                .addKeyValue(RETRY_WAS_LAST_ATTEMPT_KEY, lastTry)
                .setEventName(HTTP_RETRY_EVENT_NAME)
                .setInstrumentationContext(context);

            if (delayDuration != null) {
                log.addKeyValue(RETRY_DELAY_KEY, delayDuration.toMillis());
            }

            if (throwable != null) {
                log.log(null, throwable);
            } else {
                log.log();
            }
        }
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

    private boolean defaultShouldRetryCondition(HttpRetryCondition requestRetryCondition) {
        if (requestRetryCondition.getResponse() != null) {
            int code = requestRetryCondition.getResponse().getStatusCode();
            return (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
                || code == HTTP_STATUS_TOO_MANY_REQUESTS
                || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
                    && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
                    && code != HttpURLConnection.HTTP_VERSION));
        } else {
            return requestRetryCondition.getException() != null;
        }
    }

    private ClientLogger getLogger(HttpRequest httpRequest) {
        ClientLogger logger = null;

        if (httpRequest.getContext() != null && httpRequest.getContext().getLogger() != null) {
            logger = httpRequest.getContext().getLogger();
        }

        return logger == null ? LOGGER : logger;
    }

    // TODO (alzimmer): This cannot exist in ClientCore as 'x-ms-*' headers are using the Microsoft header extension
    //  prefix. We'll probably want to allow HttpRetryPolicy to configure which headers it looks for when determining
    //  backoff.
    private static final HttpHeaderName RETRY_AFTER_MS_HEADER = HttpHeaderName.fromString("retry-after-ms");
    private static final HttpHeaderName X_MS_RETRY_AFTER_MS_HEADER = HttpHeaderName.fromString("x-ms-retry-after-ms");

    /**
     * Attempts to extract a retry after duration from a given set of {@link HttpHeaders}.
     * <p>
     * This searches for the well-known retry after headers {@code Retry-After}, {@code retry-after-ms}, and
     * {@code x-ms-retry-after-ms}.
     * <p>
     * If no well-known headers are found null will be returned.
     *
     * @param headers The set of headers to search for a well-known retry after header.
     * @param nowSupplier A supplier for the current time used when {@code Retry-After} is using relative retry after
     * time.
     * @return The retry after duration if a well-known retry after header was found, otherwise null.
     */
    private static Duration getRetryAfterFromHeaders(HttpHeaders headers, Supplier<OffsetDateTime> nowSupplier) {
        // Found 'x-ms-retry-after-ms' header, use a Duration of milliseconds based on the value.
        Duration retryDelay = tryGetRetryDelay(headers, X_MS_RETRY_AFTER_MS_HEADER, HttpRetryPolicy::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'retry-after-ms' header, use a Duration of milliseconds based on the value.
        retryDelay = tryGetRetryDelay(headers, RETRY_AFTER_MS_HEADER, HttpRetryPolicy::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'Retry-After' header. First, attempt to resolve it as a Duration of seconds. If that fails, then
        // attempt to resolve it as an HTTP date (RFC1123).
        retryDelay = tryGetRetryDelay(headers, HttpHeaderName.RETRY_AFTER,
            headerValue -> tryParseLongOrDateTime(headerValue, nowSupplier));

        // Either the retry delay will have been found or it'll be null, null indicates no retry after.
        return retryDelay;
    }

    private static Duration tryGetRetryDelay(HttpHeaders headers, HttpHeaderName headerName,
        Function<String, Duration> delayParser) {
        String headerValue = headers.getValue(headerName);

        return CoreUtils.isNullOrEmpty(headerValue) ? null : delayParser.apply(headerValue);
    }

    private static Duration tryGetDelayMillis(String value) {
        long delayMillis = tryParseLong(value);
        return (delayMillis >= 0) ? Duration.ofMillis(delayMillis) : null;
    }

    private static Duration tryParseLongOrDateTime(String value, Supplier<OffsetDateTime> nowSupplier) {
        long delaySeconds;
        try {
            OffsetDateTime retryAfter = new DateTimeRfc1123(value).getDateTime();

            delaySeconds = nowSupplier.get().until(retryAfter, ChronoUnit.SECONDS);
        } catch (DateTimeException ex) {
            delaySeconds = tryParseLong(value);
        }

        return (delaySeconds >= 0) ? Duration.ofSeconds(delaySeconds) : null;
    }

    private static long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
