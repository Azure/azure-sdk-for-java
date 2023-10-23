// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy;

import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.pipeline.HttpPipelineNextPolicy;
import com.generic.core.http.pipeline.HttpPipelineCallContext;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.policy.retry.ExponentialBackoff;
import com.generic.core.http.policy.retry.FixedDelay;
import com.generic.core.http.policy.logging.HttpLoggingPolicy;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.http.policy.retry.RetryOptions;
import com.generic.core.http.policy.retry.RetryStrategy;
import com.generic.core.models.Headers;
import com.generic.core.util.CoreUtils;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.logging.LoggingEventBuilder;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A pipeline policy that retries when a recoverable HTTP error or exception occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(RetryPolicy.class);

    private static final String TRY_COUNT_KEY = "tryCount";
    private static final String DURATION_MS_KEY = "durationMs";

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
     * @param retryAfterHeader The HTTP header, such as {@code Retry-After}, to lookup for the retry delay. If the
     * value is null, {@link RetryStrategy#calculateRetryDelay(int)} will compute the delay and ignore the delay
     * provided in response header.
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
     * @param retryAfterHeader The HTTP header, such as 'Retry-After', to lookup for the retry delay. If the value is
     * null, {@link RetryPolicy} will use the retry strategy to compute the delay and ignore the delay provided in
     * response header.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. null is valid if, and only if,
     * {@code retryAfterHeader} is null.
     *
     * @throws NullPointerException If {@code retryStrategy} is null or when {@code retryAfterTimeUnit} is null and
     * {@code retryAfterHeader} is not null.
     */
    public RetryPolicy(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryAfterHeader = HttpHeaderName.RETRY_AFTER;
        // change
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        if (!CoreUtils.isNullOrEmpty(retryAfterHeader)) {
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
        this(getRetryStrategyFromOptions(retryOptions), null, null);
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

    private static boolean shouldRetryException(RetryStrategy retryStrategy, Throwable causalThrowable, int tryCount) {
        // Check if there are any retry attempts still available.
        if (tryCount >= retryStrategy.getMaxRetries()) {
            return false;
        }

        // Check all causal exceptions in the exception chain.
//        while (causalThrowable != null) {
//            if (retryStrategy.shouldRetryException(causalThrowable)) {
//                return true;
//            }
//
//            causalThrowable = causalThrowable.getCause();
//        }

        // Finally just return false as this can't be retried.
        return false;
    }

    private static void logRetry(int tryCount, Duration delayDuration) {
        LOGGER.atVerbose()
            .addKeyValue(TRY_COUNT_KEY, tryCount)
            .addKeyValue(DURATION_MS_KEY, delayDuration.toMillis())
            .log("Retrying.");
    }

    private static void logRetryExhausted(int tryCount) {
        LOGGER.atInfo()
            .addKeyValue(TRY_COUNT_KEY, tryCount)
            .log("Retry attempts have been exhausted.");
    }

    private static void logRetryWithError(LoggingEventBuilder loggingEventBuilder, int tryCount, String format,
                                          Throwable throwable) {
        loggingEventBuilder
            .addKeyValue(TRY_COUNT_KEY, tryCount)
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
        if (CoreUtils.isNullOrEmpty(retryHeaderValue)) {
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
        Duration retryDelay = getRetryAfterFromHeaders(responseHeaders, nowSupplier);
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return the default delay duration.
        return retryStrategy.calculateRetryDelay(tryCount);
    }

    public static Duration getRetryAfterFromHeaders(Headers headers, Supplier<OffsetDateTime> nowSupplier) {
        // Seek for the 'Retry-After' header. First, attempt to resolve it as a Duration of seconds. If that fails, then
        // attempt to resolve it as an HTTP date (RFC1123). Either the retry delay will have been found or it'll be
        // null, null indicates no retry after.
        return tryGetRetryDelay(headers, HttpHeaderName.RETRY_AFTER,
            headerValue -> tryParseLongOrDateTime(headerValue, nowSupplier));
    }

    private static Duration tryGetRetryDelay(Headers headers, HttpHeaderName headerName,
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
            OffsetDateTime retryAfter = parseOffsetDateTime(value);

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

    /**
     * Converts the {@link RetryOptions} into a {@link RetryStrategy} so it can be more easily consumed.
     *
     * @param retryOptions The retry options.
     * @return The retry strategy based on the retry options.
     */
    public static RetryStrategy getRetryStrategyFromOptions(RetryOptions retryOptions) {
        Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");

        if (retryOptions.getExponentialBackoffOptions() != null) {
            return new ExponentialBackoff(retryOptions.getExponentialBackoffOptions());
        } else if (retryOptions.getFixedDelayOptions() != null) {
            return new FixedDelay(retryOptions.getFixedDelayOptions());
        } else {
            // This should never happen.
            throw new IllegalArgumentException("'retryOptions' didn't define any retry strategy options");
        }
    }

    /**
     * Parses the RFC1123 format datetime string into OffsetDateTime.
     *
     * @param date The datetime string in RFC1123 format
     *
     * @return The underlying OffsetDateTime.
     *
     * @throws DateTimeException If the processing character is not a digit character.
     * @throws IllegalArgumentException if the given character is not recognized in the pattern of Month. such as
     * 'Jan'.
     * @throws IndexOutOfBoundsException if the {@code beginIndex} is negative, or beginIndex is larger than length of
     * {@code date}.
     */
    private static OffsetDateTime parseOffsetDateTime(final String date) {
        try {
            return OffsetDateTime.of(
                LocalDateTime.of(
                    parseOffsetDateTimeInt(date, 12, 16),  // year
                    parseOffsetDateTimeMonth(date),        // month
                    parseOffsetDateTimeInt(date, 5, 7),    // dayOfMonth
                    parseOffsetDateTimeInt(date, 17, 19),  // hour
                    parseOffsetDateTimeInt(date, 20, 22),  // minute
                    parseOffsetDateTimeInt(date, 23, 25),  // second
                    0                        // nanoOfSecond
                ),
                ZoneOffset.UTC);
        } catch (DateTimeException | IllegalArgumentException | IndexOutOfBoundsException e) {
            return OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME);
        }
    }

    /**
     * Parses the specified substring of datetime to a 'int' value.
     *
     * @param date The datetime string in RFC1123 format.
     * @param beginIndex The beginning index, inclusive.
     * @param endIndex The ending index, exclusive.
     *
     * @return The specified substring.
     *
     * @throws DateTimeException If the processing character is not digit character.
     */
    private static int parseOffsetDateTimeInt(final String date, final int beginIndex, final int endIndex) {
        int num = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            final char c = date.charAt(i);
            if (c < '0' || c > '9') {
                throw LOGGER.logThrowableAsError(new DateTimeException("Invalid date time: " + date));
            }
            num = num * 10 + (c - '0');
        }

        return num;
    }

    /**
     * Parses the specified month substring of date time to a {@link Month}.
     * <p>
     * Previously this was implemented to return the integer representing the month ({@code 1-12}) but using an integer
     * to create {@link LocalDateTime} incurs a range validation check. Now this is implemented to return {@link Month}
     * which removes the range validation check.
     *
     * @param date The date time string in RFC1123 format.
     *
     * @return The {@link Month} value which represents the month of year.
     *
     * @throws IllegalArgumentException if the given character is not recognized in the pattern of Month. such as
     * 'Jan'.
     * @throws IndexOutOfBoundsException if the {@code beginIndex} is negative, or beginIndex is larger than length of
     * {@code date}.
     */
    private static Month parseOffsetDateTimeMonth(final CharSequence date) {
        switch (date.charAt(8)) {
            case 'J':
                // Jan, Jun, Jul
                switch (date.charAt(9)) {
                    case 'a': return Month.JANUARY;
                    case 'u':
                        switch (date.charAt(10)) {
                            case 'n': return Month.JUNE;
                            case 'l': return Month.JULY;
                            default: throw LOGGER.logThrowableAsError(
                                new IllegalArgumentException("Unknown month " + date));
                        }
                    default: throw LOGGER.logThrowableAsError(new IllegalArgumentException("Unknown month " + date));
                }
            case 'F': return Month.FEBRUARY;
            case 'M':
                // Mar, May
                switch (date.charAt(10)) {
                    case 'r': return Month.MARCH;
                    case 'y': return Month.MAY;
                    default: throw LOGGER.logThrowableAsError(new IllegalArgumentException("Unknown month " + date));
                }
            case 'A':
                // Apr, Aug
                switch (date.charAt(10)) {
                    case 'r': return Month.APRIL;
                    case 'g': return Month.AUGUST;
                    default: throw LOGGER.logThrowableAsError(new IllegalArgumentException("Unknown month " + date));
                }
            case 'S': return Month.SEPTEMBER;
            case 'O': return Month.OCTOBER;
            case 'N': return Month.NOVEMBER;
            case 'D': return Month.DECEMBER;
            default: throw LOGGER.logThrowableAsError(new IllegalArgumentException("Unknown month " + date));
        }
    }
}
