// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.CoreUtils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;

public final class StringUtil {
    public static String getRandomString(String prefix) {
        return String.format(Locale.US, "%s_%s_%s", prefix, UUID.randomUUID().toString().substring(0, 6),
            Instant.now().toEpochMilli());
    }

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

    public static String getTrackingIDAndTimeToLog() {
        return String.format(Locale.US, "TrackingId: %s, at: %s", UUID.randomUUID().toString(), ZonedDateTime.now());
    }
}
