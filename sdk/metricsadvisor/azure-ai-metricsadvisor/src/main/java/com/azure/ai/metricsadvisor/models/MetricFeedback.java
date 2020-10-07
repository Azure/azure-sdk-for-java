// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.time.OffsetDateTime;

/**
 * The abstract MetricFeedback class.
 */
public abstract class MetricFeedback {
    private String id;
    private String metricId;
    private OffsetDateTime createdTime;
    private String userPrincipal;
    private FeedbackType feedbackType;
    private DimensionKey dimensionFilter;

    /**
     * Get the feedback unique id.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the feedback created time.
     *
     * @return the createdTime value.
     */
    public OffsetDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * Get the user who gives this feedback.
     *
     * @return the userPrincipal value.
     */
    public String getUserPrincipal() {
        return this.userPrincipal;
    }

    /**
     * Get the metric unique id.
     *
     * @return the metricId value.
     */
    public String getMetricId() {
        return this.metricId;
    }

    /**
     * Get the type of the feedback.
     *
     * @return the feedbackType value.
     */
    public FeedbackType getFeedbackType() {
        return this.feedbackType;
    }

    /**
     * Get the dimensionFilter.
     *
     * @return the dimensionFilter value.
     */
    public DimensionKey getDimensionFilter() {
        return this.dimensionFilter;
    }

    /**
     * Set the series keys value for the feedback.
     *
     * @param dimensionFilter the dimensionFilter value.
     *
     * @return the MetricFeedback object itself.
     */
    public MetricFeedback setDimensionFilter(final DimensionKey dimensionFilter) {
        this.dimensionFilter = dimensionFilter;
        return this;
    }
}
