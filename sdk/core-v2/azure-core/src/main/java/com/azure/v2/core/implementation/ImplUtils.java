// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.implementation;

import com.azure.v2.core.http.polling.Poller;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.DateTimeRfc1123;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class containing implementation specific methods.
 */
public final class ImplUtils {
    private static final ClientLogger POLLER_LOGGER = new ClientLogger(Poller.class);

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
    public static Duration getRetryAfterFromHeaders(HttpHeaders headers, Supplier<OffsetDateTime> nowSupplier) {
        // Found 'x-ms-retry-after-ms' header, use a Duration of milliseconds based on the value.
        Duration retryDelay
            = tryGetRetryDelay(headers, HttpHeaderName.fromString("x-ms-retry-after-ms"), ImplUtils::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'retry-after-ms' header, use a Duration of milliseconds based on the value.
        retryDelay
            = tryGetRetryDelay(headers, HttpHeaderName.fromString("retry-after-ms"), ImplUtils::tryGetDelayMillis);
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

    /**
     * Creates a new {@link URL} from the given {@code urlString}.
     * <p>
     * This is a temporary method that will be removed once all usages of {@link URL#URL(String)} are migrated to
     * {@link URI}-based methods given the deprecation of the URL methods in Java 20.
     *
     * @param urlString The string to convert to a {@link URL}.
     * @return The {@link URL} representing the {@code urlString}.
     * @throws MalformedURLException If the {@code urlString} isn't a valid {@link URL}.
     */
    @SuppressWarnings("deprecation")
    public static URL createUrl(String urlString) throws MalformedURLException {
        return new URL(urlString);
    }

    /**
     * Calls {@link Future#get(long, TimeUnit)} and returns the value if the {@code future} completes before the timeout
     * is triggered. If the timeout is triggered, the {@code future} is {@link Future#cancel(boolean) cancelled}
     * interrupting the execution of the task that the {@link Future} represented.
     * <p>
     * If the timeout is zero or is negative then the timeout will be ignored and an infinite timeout will be used.
     *
     * @param <T> The type of value returned by the {@code future}.
     * @param future The {@link Future} to get the value from.
     * @param timeoutInMillis The timeout value. If the timeout is zero or is negative then the timeout will be ignored
     * and an infinite timeout will be used.
     * @return The value from the {@code future}.
     * @throws NullPointerException If {@code future} is null.
     * @throws CancellationException If the computation was cancelled.
     * @throws ExecutionException If the computation threw an exception.
     * @throws InterruptedException If the current thread was interrupted while waiting.
     * @throws TimeoutException If the wait timed out.
     * @throws RuntimeException If the {@code future} threw an exception during processing.
     * @throws Error If the {@code future} threw an {@link Error} during processing.
     */
    public static <T> T getResultWithTimeout(Future<T> future, long timeoutInMillis)
        throws InterruptedException, ExecutionException, TimeoutException {
        Objects.requireNonNull(future, "'future' cannot be null.");

        if (timeoutInMillis <= 0) {
            return future.get();
        }

        try {
            return future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw e;
            }
        }
    }

    public static ClientLogger getPollerLogger() {
        return POLLER_LOGGER;
    }

    private ImplUtils() {
    }
}
