// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.monitor.query.implementation.metrics.models.MetricsHelper;

import java.time.Duration;

/**
 * Metric availability specifies the granularity and the retention period for that
 * granularity.
 */
public final class MetricAvailability {
    private Duration retention;
    private Duration granularity;

    static {
        MetricsHelper.setMetricAvailabilityAccessor(MetricAvailability::setMetricAvailabilityProperties);
    }

    private void setMetricAvailabilityProperties(Duration retention, Duration granularity) {
        this.retention = retention;
        this.granularity = granularity;
    }

    /**
     * Returns the retention period for the metric at the specified granularity.
     * @return the retention period for the metric at the specified granularity.
     */
    public Duration getRetention() {
        return retention;
    }

    /**
     * Returns  the granularity specifies the aggregation interval for the metric.
     * @return the granularity specifies the aggregation interval for the metric.
     */
    public Duration getGranularity() {
        return granularity;
    }
}
