// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 *
 */
@Immutable
public final class MetricsValue {
    private final OffsetDateTime timeStamp;
    private final Double average;
    private final Double minimum;
    private final Double maximum;
    private final Double total;
    private final Double count;

    /**
     * @param timeStamp
     * @param average
     * @param minimum
     * @param maximum
     * @param total
     * @param count
     */
    public MetricsValue(OffsetDateTime timeStamp, Double average, Double minimum, Double maximum, Double total, Double count) {
        this.timeStamp = timeStamp;
        this.average = average;
        this.minimum = minimum;
        this.maximum = maximum;
        this.total = total;
        this.count = count;
    }

    /**
     * @return
     */
    public OffsetDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return
     */
    public Double getAverage() {
        return average;
    }

    /**
     * @return
     */
    public Double getMinimum() {
        return minimum;
    }

    /**
     * @return
     */
    public Double getMaximum() {
        return maximum;
    }

    /**
     * @return
     */
    public Double getTotal() {
        return total;
    }

    /**
     * @return
     */
    public Double getCount() {
        return count;
    }
}
