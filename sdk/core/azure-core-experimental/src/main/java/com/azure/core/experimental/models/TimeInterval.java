// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Class to represent a time interval.
 */
@Immutable
public class TimeInterval {
    public static final TimeInterval ALL = new TimeInterval(OffsetDateTime.MIN, OffsetDateTime.MAX);

    public static final TimeInterval LAST_5_MINUTES = new TimeInterval(Duration.ofMinutes(5));
    public static final TimeInterval LAST_30_MINUTES = new TimeInterval(Duration.ofMinutes(30));

    public static final TimeInterval LAST_1_HOUR = new TimeInterval(Duration.ofHours(1));
    public static final TimeInterval LAST_4_HOURS = new TimeInterval(Duration.ofHours(4));
    public static final TimeInterval LAST_12_HOURS = new TimeInterval(Duration.ofHours(12));

    public static final TimeInterval LAST_DAY = new TimeInterval(Duration.ofDays(1));
    public static final TimeInterval LAST_2_DAYS = new TimeInterval(Duration.ofDays(2));
    public static final TimeInterval LAST_3_DAYS = new TimeInterval(Duration.ofDays(3));
    public static final TimeInterval LAST_7_DAYS = new TimeInterval(Duration.ofDays(7));

    private static final ClientLogger LOGGER = new ClientLogger(TimeInterval.class);
    private static final String ERROR_MESSAGE = "%s is an invalid time interval. It must be in one of the "
            + "following ISO 8601 time interval formats: duration, startDuration/endTime, "
            + "startTime/endTime, startTime/endDuration";

    private final Duration duration;
    private final Duration startDuration;
    private final Duration endDuration;
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    /**
     * Creates an instance of {@link TimeInterval} using the provided duration.
     * @param duration the duration for this query time span.
     */
    public TimeInterval(Duration duration) {
        this.duration = Objects.requireNonNull(duration, "'duration' cannot be null");
        this.startDuration = null;
        this.endDuration = null;
        this.startTime = null;
        this.endTime = null;
    }

    /**
     * Creates an instance of {@link TimeInterval} using the start and end {@link OffsetDateTime OffsetDateTimes}.
     * @param startTime The start time of the interval.
     * @param endTime The end time of the interval.
     */
    public TimeInterval(OffsetDateTime startTime, OffsetDateTime endTime) {
        this.startTime = Objects.requireNonNull(startTime, "'startTime' cannot be null");
        this.endTime = Objects.requireNonNull(endTime, "'endTime' cannot be null");
        this.duration = null;
        this.startDuration = null;
        this.endDuration = null;
    }

    /**
     * Creates an instance of {@link TimeInterval} using the start and end duration of the interval.
     * @param startTime The start time of the interval.
     * @param endDuration The end duration of the interval.
     */
    public TimeInterval(OffsetDateTime startTime, Duration endDuration) {
        this.startTime = Objects.requireNonNull(startTime, "'startTime' cannot be null");
        this.endDuration = Objects.requireNonNull(endDuration, "'endDuration' cannot be null");
        this.duration = null;
        this.endTime = null;
        this.startDuration = null;
    }

    /**
     * Creates an instance of {@link TimeInterval} using the start and end duration of the interval.
     * @param startDuration The duration of the interval.
     * @param endTime The end time of the interval.
     */
    public TimeInterval(Duration startDuration, OffsetDateTime endTime) {
        this.endTime = Objects.requireNonNull(endTime, "'endTime' cannot be null");
        this.startDuration = Objects.requireNonNull(startDuration, "'startDuration' cannot be null");
        this.duration = null;
        this.startTime = null;
        this.endDuration = null;
    }

    /**
     * Returns the duration of this {@link TimeInterval} instance.
     * @return the duration of this {@link TimeInterval} instance.
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Returns the start duration of this {@link TimeInterval} instance.
     * @return the start duration of this {@link TimeInterval} instance.
     */
    public Duration getStartDuration() {
        return startDuration;
    }

    /**
     * Returns the end duration of this {@link TimeInterval} instance.
     * @return the end duration of this {@link TimeInterval} instance.
     */
    public Duration getEndDuration() {
        return endDuration;
    }

    /**
     * Returns the start time of this {@link TimeInterval} instance.
     * @return the start time of this {@link TimeInterval} instance.
     */
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time of this {@link TimeInterval} instance.
     * @return the end time of this {@link TimeInterval} instance.
     */
    public OffsetDateTime getEndTime() {
        return endTime;
    }

    /**
     * Returns this {@link TimeInterval} in ISO 8601 string format.
     * @return ISO 8601 formatted string representation of this {@link TimeInterval} instance.
     */
    public String toIso8601Format() {
        if (startTime != null && endTime != null) {
            return startTime + "/" + endTime;
        }

        if (startTime != null && endDuration != null) {
            return startTime + "/" + endDuration;
        }

        if (startDuration != null && endTime != null) {
            return startDuration + "/" + endTime;
        }

        if (duration != null) {
            return duration.toString();
        }
        // should not reach here
        throw LOGGER.logExceptionAsError(new IllegalStateException("Invalid date time range"));
    }

    /**
     * This method takes an ISO 8601 formatted time interval string and returns an instance of {@link TimeInterval}.
     * @param value The ISO 8601 formatted time interval string.
     * @return An instance of {@link TimeInterval}.
     * @throws IllegalArgumentException if {@code value} is not in the correct format.
     */
    public static TimeInterval parse(String value) {
        Objects.requireNonNull(value);

        String[] parts = value.split("/");
        if (parts.length == 1) {
            // duration
            Duration duration = parseDuration(parts[0]);
            if (duration == null || parts[0].length() + 1 == value.length()) {
                // input strings like "PT24H/" are invalid
                throw LOGGER.logExceptionAsError(
                        new IllegalArgumentException(String.format(ERROR_MESSAGE, value)));
            }

            return new TimeInterval(duration);
        }

        if (parts.length == 2) {
            Duration startDuration = parseDuration(parts[0]);
            OffsetDateTime startTime = parseTime(parts[0]);

            Duration endDuration = parseDuration(parts[1]);
            OffsetDateTime endTime = parseTime(parts[1]);

            if (startDuration != null && endTime != null) {
                return new TimeInterval(startDuration, endTime);
            }
            if (startTime != null && endTime != null) {
                return new TimeInterval(startTime, endTime);
            }
            if (startTime != null && endDuration != null) {
                return new TimeInterval(startTime, endDuration);
            }
        }
        throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(String.format(ERROR_MESSAGE, value)));
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (duration != null) {
            return sb.append("duration: ").append(duration).append("]").toString();
        }

        if (startTime != null) {
            sb.append("start time: ").append(startTime);
        }
        if (startDuration != null) {
            sb.append("start duration: ").append(startDuration);
        }
        sb.append(", ");
        if (endTime != null) {
            sb.append("end time: ").append(endTime);
        }
        if (endDuration != null) {
            sb.append("end duration: ").append(endDuration);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeInterval that = (TimeInterval) o;

        return Objects.equals(this.duration, that.duration)
                && Objects.equals(this.startDuration, that.startDuration)
                && Objects.equals(this.endDuration, that.endDuration)
                && Objects.equals(this.startTime, that.startTime)
                && Objects.equals(this.endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, startDuration, endDuration, startTime, endTime);
    }

    private static OffsetDateTime parseTime(String value) {
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception exception) {
            return null;
        }
    }

    private static Duration parseDuration(String value) {
        try {
            return Duration.parse(value);
        } catch (Exception exception) {
            return null;
        }
    }
}
