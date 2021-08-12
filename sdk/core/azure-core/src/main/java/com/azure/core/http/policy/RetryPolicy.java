// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * A pipeline policy that retries when a recoverable HTTP error or exception occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";
    private static final String X_MS_RETRY_AFTER_MS_HEADER = "x-ms-retry-after-ms";

    private final ClientLogger logger = new ClientLogger(RetryPolicy.class);

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

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptAsync(context, next, context.getHttpRequest(), 0);
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
                    logger.verbose("[Retrying] Try count: {}, Delay duration in seconds: {}", tryCount,
                        delayDuration.getSeconds());

                    Flux<ByteBuffer> responseBody = httpResponse.getBody();
                    if (responseBody == null) {
                        return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                            .delaySubscription(delayDuration);
                    } else {
                        return httpResponse.getBody()
                            .ignoreElements()
                            .then(attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                                .delaySubscription(delayDuration));
                    }
                } else {
                    if (tryCount >= retryStrategy.getMaxRetries()) {
                        logger.info("Retry attempts have been exhausted after {} attempts.", tryCount);
                    }
                    return Mono.just(httpResponse);
                }
            })
            .onErrorResume(err -> {
                if (shouldRetryException(err, tryCount)) {
                    logger.verbose("[Error Resume] Try count: {}, Error: {}", tryCount, err);
                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(retryStrategy.calculateRetryDelay(tryCount));
                } else {
                    logger.info("Retry attempts have been exhausted after {} attempts.", tryCount, err);
                    return Mono.error(err);
                }
            });
    }

    private boolean shouldRetry(HttpResponse response, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries() && retryStrategy.shouldRetry(response);
    }

    private boolean shouldRetryException(Throwable throwable, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries() && retryStrategy.shouldRetryException(throwable);
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    static Duration determineDelayDuration(HttpResponse response, int tryCount, RetryStrategy retryStrategy,
        String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        // If the retry after header hasn't been configured, attempt to look up the well-known headers.
        if (isNullOrEmpty(retryAfterHeader)) {
            return getWellKnownRetryDelay(response.getHeaders(), tryCount, retryStrategy);
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
    static Duration getWellKnownRetryDelay(HttpHeaders responseHeaders, int tryCount, RetryStrategy retryStrategy) {
        // Found 'x-ms-retry-after-ms' header, use a Duration of milliseconds based on the value.
        Duration retryDelay = tryGetRetryDelay(responseHeaders, X_MS_RETRY_AFTER_MS_HEADER,
            RetryPolicy::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'retry-after-ms' header, use a Duration of milliseconds based on the value.
        retryDelay = tryGetRetryDelay(responseHeaders, RETRY_AFTER_MS_HEADER, RetryPolicy::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'Retry-After' header. First, attempt to resolve it as a Duration of seconds. If that fails, then
        // attempt to resolve it as an HTTP date (RFC1123).
        retryDelay = tryGetRetryDelay(responseHeaders, RETRY_AFTER_HEADER, RetryPolicy::tryParseLongOrDateTime);
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return the default delay duration.
        return retryStrategy.calculateRetryDelay(tryCount);
    }

    private static Duration tryGetRetryDelay(HttpHeaders headers, String headerName,
        Function<String, Duration> delayParser) {
        String headerValue = headers.getValue(headerName);

        return CoreUtils.isNullOrEmpty(headerValue) ? null : delayParser.apply(headerValue);
    }

    private static Duration tryGetDelayMillis(String value) {
        long delayMillis = tryParseLong(value);
        return (delayMillis >= 0) ? Duration.ofMillis(delayMillis) : null;
    }

    private static Duration tryParseLongOrDateTime(String value) {
        try {
            OffsetDateTime retryAfter = new DateTimeRfc1123(value).getDateTime();

            return Duration.between(OffsetDateTime.now(), retryAfter);
        } catch (DateTimeException ex) {
            long delaySeconds = tryParseLong(value);
            return (delaySeconds >= 0) ? Duration.ofSeconds(delaySeconds) : null;
        }
    }

    private static long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
