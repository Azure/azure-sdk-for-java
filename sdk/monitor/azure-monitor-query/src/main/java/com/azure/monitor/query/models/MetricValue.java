// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * Represents a metric value.
 */
@Immutable
public final class MetricValue {
    private final OffsetDateTime timeStamp;
    private final Double average;
    private final Double minimum;
    private final Double maximum;
    private final Double total;
    private final Double count;

    /**
     * Creates an instance of {@link MetricValue}.
     * @param timeStamp the timestamp for the metric value in ISO 8601 format.
     * @param average the average value in the time range.
     * @param minimum the least value in the time range.
     * @param maximum the greatest value in the time range.
     * @param total the sum of all of the values in the time range.
     * @param count the number of samples in the time range.
     */
    public MetricValue(OffsetDateTime timeStamp, Double average, Double minimum, Double maximum, Double total,
                       Double count) {
        this.timeStamp = timeStamp;
        this.average = average;
        this.minimum = minimum;
        this.maximum = maximum;
        this.total = total;
        this.count = count;
    }

    /**
     * Returns the timestamp for the metric value in ISO 8601 format.
     * @return the timestamp for the metric value in ISO 8601 format.
     */
    public OffsetDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns the average value in the time range.
     * @return the average value in the time range.
     */
    public Double getAverage() {
        return average;
    }

    /**
     * Returns the least value in the time range.
     * @return the least value in the time range.
     */
    public Double getMinimum() {
        return minimum;
    }

    /**
     * Returns the greatest value in the time range.
     * @return the greatest value in the time range.
     */
    public Double getMaximum() {
        return maximum;
    }

    /**
     * Returns the sum of all of the values in the time range.
     * @return the sum of all of the values in the time range.
     */
    public Double getTotal() {
        return total;
    }

    /**
     * Returns the number of samples in the time range.
     * @return the number of samples in the time range.
     */
    public Double getCount() {
        return count;
    }
}
