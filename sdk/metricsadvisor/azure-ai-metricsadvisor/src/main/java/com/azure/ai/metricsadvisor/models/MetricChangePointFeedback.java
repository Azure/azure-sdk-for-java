// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * The Feedback that allows the user to mark the exact change point when the time series has
 * a trend change. This helps the anomaly detector in future analysis.
 */
@Fluent
public final class MetricChangePointFeedback extends MetricFeedback {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private final ChangePointValue changePointValue;

    /**
     * Creates an instance of MetricChangePointFeedback.
     *
     * @param startTime the start timestamp of feedback timerange
     * @param endTime the end timestamp of feedback timerange, when equals to startTime means
     * only one timestamp
     * @param changePointValue the value of the change point feedback.
     */
    public MetricChangePointFeedback(OffsetDateTime startTime,
        OffsetDateTime endTime,
        ChangePointValue changePointValue) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.changePointValue = changePointValue;
    }

    /**
     * Set the series keys value for the feedback.
     *
     * @param dimensionFilter the dimensionFilter value to set.
     *
     * @return the MetricChangePointFeedback object itself.
     */
    @Override
    public MetricChangePointFeedback setDimensionFilter(final DimensionKey dimensionFilter) {
        super.setDimensionFilter(dimensionFilter);
        return this;
    }

    /**
     * Get the change point feedback value.
     *
     * @return the changePointValue value.
     */
    public ChangePointValue getChangePointValue() {
        return this.changePointValue;
    }

    /**
     * Get the start timestamp of feedback timerange.
     *
     * @return the startTime value.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Get the end timestamp of feedback timerange, when equals to startTime means only one
     * timestamp.
     *
     * @return the endTime value.
     */
    public OffsetDateTime getEndTime() {
        return this.endTime;
    }

}
