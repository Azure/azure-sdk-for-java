// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Class to represent a time interval.
 * Time intervals are inclusive at the start time and exclusive at the end time.
 */
@Immutable
public final class MetricsQueryTimeInterval {
    /**
     * Time interval of all time.
     */

    private static final ClientLogger LOGGER = new ClientLogger(MetricsQueryTimeInterval.class);
    private static final String ERROR_MESSAGE = "%s is an invalid time interval. It must be in the "
        + "following ISO 8601 time interval format: startTime/endTime";

    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    /**
     * Creates an instance of {@link MetricsQueryTimeInterval} using the start and end {@link OffsetDateTime OffsetDateTimes}.
     *
     * @param startTime The start time of the interval.
     * @param endTime The end time of the interval.
     */
    public MetricsQueryTimeInterval(OffsetDateTime startTime, OffsetDateTime endTime) {
        this.startTime = Objects.requireNonNull(startTime, "'startTime' cannot be null");
        this.endTime = Objects.requireNonNull(endTime, "'endTime' cannot be null");
    }

    /**
     * Returns the start time of this {@link MetricsQueryTimeInterval} instance.
     *
     * @return the start time of this {@link MetricsQueryTimeInterval} instance.
     */
    public OffsetDateTime getStartTime() {
        if (startTime != null) {
            return startTime;
        }
        return null;
    }

    /**
     * Returns the end time of this {@link MetricsQueryTimeInterval} instance.
     *
     * @return the end time of this {@link MetricsQueryTimeInterval} instance.
     */
    public OffsetDateTime getEndTime() {
        if (endTime != null) {
            return endTime;
        }
        return null;
    }

    /**
     * This method takes an ISO 8601 formatted time interval string and returns an instance of {@link MetricsQueryTimeInterval}.
     *
     * @param value The ISO 8601 formatted time interval string.
     * @return An instance of {@link MetricsQueryTimeInterval}.
     * @throws IllegalArgumentException if {@code value} is not in the correct format.
     */
    public static MetricsQueryTimeInterval parse(String value) {
        Objects.requireNonNull(value);

        String[] parts = value.split("/");
        if (parts.length == 2) {
            OffsetDateTime startTime = parseTime(parts[0]);

            OffsetDateTime endTime = parseTime(parts[1]);
            if (startTime != null && endTime != null) {
                return new MetricsQueryTimeInterval(startTime, endTime);
            }
        }
        throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(ERROR_MESSAGE, value)));
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (startTime != null && endTime != null) {
            sb.append("start time: ").append(startTime).append(", ").append("end time: ").append(endTime);
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
        MetricsQueryTimeInterval that = (MetricsQueryTimeInterval) o;

        return Objects.equals(this.startTime, that.startTime) && Objects.equals(this.endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }

    private static OffsetDateTime parseTime(String value) {
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception exception) {
            return null;
        }
    }
}
