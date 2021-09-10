package com.azure.monitor.opentelemetry.exporter.utils;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class FormattedDuration {
    private static final ThreadLocal<StringBuilder> reusableStringBuilder =
        ThreadLocal.withInitial(StringBuilder::new);

    public static String getFormattedDuration(Duration duration) {
        StringBuilder sb = reusableStringBuilder.get();
        sb.setLength(0);
        appendDaysHoursMinutesSeconds(sb, duration.toDays(), duration.toHours(), duration.toMinutes(), duration.toSeconds());
        appendMinSixDigits(sb, NANOSECONDS.toMicros(duration.toNanos()));
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
