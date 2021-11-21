// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * The feedback that allows adding comments in plain text providing more
 * context about the data.
 */
@Fluent
public final class MetricCommentFeedback extends MetricFeedback {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private final String comment;

    /**
     * Creates an instance of MetricCommentFeedback.
     *
     * @param startTime the start timestamp of feedback timerange
     * @param endTime the end timestamp of feedback timerange, when equals to startTime means
     * only one timestamp
     * @param comment the value of the comment.
     */
    public MetricCommentFeedback(OffsetDateTime startTime,
        OffsetDateTime endTime,
        String comment) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
    }

    /**
     * Set the series keys value for the feedback.
     *
     * @param dimensionFilter the dimensionFilter value to set.
     *
     * @return the MetricCommentFeedback object itself.
     */
    @Override
    public MetricCommentFeedback setDimensionFilter(final DimensionKey dimensionFilter) {
        super.setDimensionFilter(dimensionFilter);
        return this;
    }

    /**
     * Get the comment value.
     *
     * @return the comment value.
     */
    public String getComment() {
        return this.comment;
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
