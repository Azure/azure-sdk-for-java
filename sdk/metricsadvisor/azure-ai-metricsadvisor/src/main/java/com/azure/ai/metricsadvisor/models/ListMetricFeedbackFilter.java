// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * Additional properties to filter result for metric feedback list operations.
 */
@Fluent
public final class ListMetricFeedbackFilter {
    private DimensionKey dimensionFilter;
    private FeedbackType feedbackType;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private FeedbackQueryTimeMode timeMode;

    /**
     * Get the dimension filter set on the feedback.
     *
     * @return the dimensionFilter value.
     */
    public DimensionKey getDimensionFilter() {
        return this.dimensionFilter;
    }

    /**
     * Get the type of the metric feedback.
     *
     * @return the type of the metric feedback.
     */
    public FeedbackType getFeedbackType() {
        return this.feedbackType;
    }

    /**
     * Get the start time of the time range within which the alerts were triggered.
     *
     * @return The start time.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Get the end time of the time range within which the alerts were triggered.
     *
     * @return The end time.
     */
    public OffsetDateTime getEndTime() {
        return this.endTime;
    }

    /**
     * Get the feedback query time mode to filter feedback.
     *
     * @return The feedback query time mode to filter feedback.
     */
    public FeedbackQueryTimeMode getTimeMode() {
        return this.timeMode;
    }

    /**
     * Set the feedback dimension filter to filter the feedbacks.
     *
     * @param dimensionToFilter the dimensionToFilter value to set.
     *
     * @return the ListMetricFeedbackFilter object itself.
     */
    public ListMetricFeedbackFilter setDimensionFilter(DimensionKey dimensionToFilter) {
        this.dimensionFilter = dimensionToFilter;
        return this;
    }

    /**
     * Set the feedback type value to filter feedbacks by type.
     *
     * @param feedbackType the feedbackType value to set.
     *
     * @return the ListMetricFeedbackFilter object itself.
     */
    public ListMetricFeedbackFilter setFeedbackType(FeedbackType feedbackType) {
        this.feedbackType = feedbackType;
        return this;
    }

    /**
     * Set the start time filter under chosen time mode.
     *
     * @param startTime the startTime value to set.
     *
     * @return the ListMetricFeedbackFilter object itself.
     */
    public ListMetricFeedbackFilter setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Set the end time filter under chosen time mode.
     *
     * @param endTime the endTime value to set.
     *
     * @return the ListMetricFeedbackFilter object itself.
     */
    public ListMetricFeedbackFilter setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Set the feedback query time mode to filter feedback.
     *
     * @param timeMode the timeMode value to set.
     *
     * @return the ListMetricFeedbackFilter object itself.
     */
    public ListMetricFeedbackFilter setTimeMode(FeedbackQueryTimeMode timeMode) {
        this.timeMode = timeMode;
        return this;
    }
}
