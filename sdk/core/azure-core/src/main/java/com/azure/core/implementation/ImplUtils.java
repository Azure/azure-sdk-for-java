// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class containing implementation specific methods.
 */
public final class ImplUtils {
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";
    private static final String X_MS_RETRY_AFTER_MS_HEADER = "x-ms-retry-after-ms";

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
        Duration retryDelay = tryGetRetryDelay(headers, X_MS_RETRY_AFTER_MS_HEADER,
            ImplUtils::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'retry-after-ms' header, use a Duration of milliseconds based on the value.
        retryDelay = tryGetRetryDelay(headers, RETRY_AFTER_MS_HEADER, ImplUtils::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'Retry-After' header. First, attempt to resolve it as a Duration of seconds. If that fails, then
        // attempt to resolve it as an HTTP date (RFC1123).
        retryDelay = tryGetRetryDelay(headers, RETRY_AFTER_HEADER,
            headerValue -> tryParseLongOrDateTime(headerValue, nowSupplier));
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return null to indicate no retry after.
        return null;
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

    private ImplUtils() {
    }
}
