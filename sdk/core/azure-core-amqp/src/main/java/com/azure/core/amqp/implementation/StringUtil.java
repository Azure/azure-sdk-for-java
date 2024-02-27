// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.CoreUtils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Locale;

/**
 * Utility class to help with String-based tasks.
 */
public final class StringUtil {
    /**
     * Gets a random string.
     *
     * @param prefix The prefix to use for the random string.
     * @return A random string.
     */
    public static String getRandomString(String prefix) {
        return String.format(Locale.US, "%s_%s_%s", prefix, CoreUtils.randomUuid().toString().substring(0, 6),
            Instant.now().toEpochMilli());
    }

    /**
     * Formats the exception and its stack trace into a String.
     *
     * @param exception The exception to format.
     * @param customErrorMessage The custom error message to prepend to the exception's message.
     *
     * @return The formatted exception and its stack trace.
     */
    public static String toStackTraceString(final Throwable exception, final String customErrorMessage) {
        final StringBuilder builder = new StringBuilder();

        if (!CoreUtils.isNullOrEmpty(customErrorMessage)) {
            builder.append(customErrorMessage);
            builder.append(System.lineSeparator());
        }

        builder.append(exception.getMessage());
        final StackTraceElement[] stackTraceElements = exception.getStackTrace();
        for (final StackTraceElement ste : stackTraceElements) {
            builder.append(System.lineSeparator());
            builder.append(ste.toString());
        }

        final Throwable innerException = exception.getCause();
        if (innerException != null) {
            builder.append("Cause: ").append(innerException.getMessage());
            final StackTraceElement[] innerStackTraceElements = innerException.getStackTrace();
            for (final StackTraceElement ste : innerStackTraceElements) {
                builder.append(System.lineSeparator());
                builder.append(ste.toString());
            }
        }

        return builder.toString();
    }

    /**
     * Gets a random UUID and the current time to log.
     *
     * @return A String with the random UUID and current time.
     */
    public static String getTrackingIdAndTimeToLog() {
        return String.format(Locale.US, "TrackingId: %s, at: %s", CoreUtils.randomUuid(), ZonedDateTime.now());
    }
}
