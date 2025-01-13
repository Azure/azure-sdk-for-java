// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class FormattedDuration {

    private static final long NANOSECONDS_PER_DAY = DAYS.toNanos(1);
    private static final long NANOSECONDS_PER_HOUR = HOURS.toNanos(1);
    private static final long NANOSECONDS_PER_MINUTE = MINUTES.toNanos(1);
    private static final long NANOSECONDS_PER_SECOND = SECONDS.toNanos(1);

    private static final ThreadLocal<StringBuilder> reusableStringBuilder = ThreadLocal.withInitial(StringBuilder::new);

    public static String fromNanos(long durationNanos) {
        long remainingNanos = durationNanos;

        long days = remainingNanos / NANOSECONDS_PER_DAY;
        remainingNanos = remainingNanos % NANOSECONDS_PER_DAY;

        long hours = remainingNanos / NANOSECONDS_PER_HOUR;
        remainingNanos = remainingNanos % NANOSECONDS_PER_HOUR;

        long minutes = remainingNanos / NANOSECONDS_PER_MINUTE;
        remainingNanos = remainingNanos % NANOSECONDS_PER_MINUTE;

        long seconds = remainingNanos / NANOSECONDS_PER_SECOND;
        remainingNanos = remainingNanos % NANOSECONDS_PER_SECOND;

        // TODO (trask) optimization: even better than reusing string builder would be to write this
        //  directly to json stream during json serialization
        StringBuilder sb = reusableStringBuilder.get();
        sb.setLength(0);

        appendDaysHoursMinutesSeconds(sb, days, hours, minutes, seconds);
        appendMinSixDigits(sb, NANOSECONDS.toMicros(remainingNanos));

        return sb.toString();
    }

    // Returns duration in microseconds
    public static long getDurationFromTelemetryItemDurationString(String duration) {
        // duration in format: DD.HH:MM:SS.MMMMMM. Must be less than 1000 days.
        try {
            String[] parts = duration.split("\\.");
            int days = 0;
            String hms;
            long microseconds;
            if (parts.length == 3) {
                days = Integer.parseInt(parts[0]);
                hms = parts[1];
                microseconds = Integer.parseInt(parts[2]);
            } else { //length 2
                hms = parts[0];
                microseconds = Integer.parseInt(parts[1]);
            }
            String[] hmsParts = hms.split(":");
            int hours = Integer.parseInt(hmsParts[0]);
            int minutes = Integer.parseInt(hmsParts[1]);
            int seconds = Integer.parseInt(hmsParts[2]);
            return (days * 24L * 60L * 60L * 1000000L) + (hours * 60L * 60L * 1000000L) + (minutes * 60L * 1000000L)
                + (seconds * 1000000L) + microseconds;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void appendDaysHoursMinutesSeconds(StringBuilder sb, long days, long hours, long minutes,
        long seconds) {
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

    private FormattedDuration() {
    }
}
