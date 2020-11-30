// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * The MetricChangePointFeedback class.
 */
@Immutable
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
