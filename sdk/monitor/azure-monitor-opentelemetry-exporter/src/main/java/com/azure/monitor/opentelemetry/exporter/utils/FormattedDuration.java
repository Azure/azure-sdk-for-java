// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.utils;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This class contains several utility functions to format duration
 */
public class FormattedDuration {
    private static final long NANOSECONDS_PER_DAY = DAYS.toNanos(1);
    private static final long NANOSECONDS_PER_HOUR = HOURS.toNanos(1);
    private static final long NANOSECONDS_PER_MINUTE = MINUTES.toNanos(1);
    private static final long NANOSECONDS_PER_SECOND = SECONDS.toNanos(1);

    private static final ThreadLocal<StringBuilder> REUSABLE_STRING_BUILDER =
        ThreadLocal.withInitial(StringBuilder::new);

    /**
     * This method generates a formatted string based on input duration in nano seconds.
     */
    public static String getFormattedDuration(long durationNanos) {
        long remainingNanos = durationNanos;

        long days = remainingNanos / NANOSECONDS_PER_DAY;
        remainingNanos = remainingNanos % NANOSECONDS_PER_DAY;

        long hours = remainingNanos / NANOSECONDS_PER_HOUR;
        remainingNanos = remainingNanos % NANOSECONDS_PER_HOUR;

        long minutes = remainingNanos / NANOSECONDS_PER_MINUTE;
        remainingNanos = remainingNanos % NANOSECONDS_PER_MINUTE;

        long seconds = remainingNanos / NANOSECONDS_PER_SECOND;
        remainingNanos = remainingNanos % NANOSECONDS_PER_SECOND;

        StringBuilder sb = REUSABLE_STRING_BUILDER.get();
        sb.setLength(0);
        appendDaysHoursMinutesSeconds(sb, days, hours, minutes, seconds);
        appendMinSixDigits(sb, NANOSECONDS.toMicros(remainingNanos));
        return sb.toString();
    }

    private static void appendDaysHoursMinutesSeconds(StringBuilder sb, long days, long hours, long minutes, long seconds) {
        if (days > 0) {
            sb.append(days);
            sb.append('.');
        }
        appendMinTwoDigits(sb, hours);
        sb.append(':');
        appendMinTwoDigits(sb, minutes);
        sb.append(':');
        appendMinTwoDigits(sb, seconds);
        sb.append('.');
    }

    private static void appendMinTwoDigits(StringBuilder sb, long value) {
        if (value < 10) {
            sb.append('0');
        }
        sb.append(value);
    }

    private static void appendMinSixDigits(StringBuilder sb, long value) {
        if (value < 100000) {
            sb.append('0');
        }
        if (value < 10000) {
            sb.append('0');
        }
        if (value < 1000) {
            sb.append('0');
        }
        if (value < 100) {
            sb.append('0');
        }
        if (value < 10) {
            sb.append('0');
        }
        sb.append(value);
    }
}
