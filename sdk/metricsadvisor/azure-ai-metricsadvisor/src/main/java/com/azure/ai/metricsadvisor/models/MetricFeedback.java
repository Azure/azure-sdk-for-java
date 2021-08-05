// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.MetricFeedbackHelper;
import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * Users can submit various feedback for the anomaly detection that the service
 * performed. The {@link MetricFeedback} represents the base type for different
 * feedback. The feedback will be applied for the future anomaly detection
 * processing of the same time series.
 *
 * @see MetricAnomalyFeedback
 * @see MetricChangePointFeedback
 * @see MetricCommentFeedback
 * @see MetricPeriodFeedback
 */
@Fluent
public abstract class MetricFeedback {
    private String id;
    private String metricId;
    private OffsetDateTime createdTime;
    private String userPrincipal;
    private FeedbackType feedbackType;
    private DimensionKey dimensionFilter;

    static {
        MetricFeedbackHelper.setAccessor(new MetricFeedbackHelper.MetricFeedbackAccessor() {
            @Override
            public void setId(MetricFeedback feedback, String id) {
                feedback.setId(id);
            }

            @Override
            public void setMetricId(MetricFeedback feedback, String metricId) {
                feedback.setMetricId(metricId);
            }

            @Override
            public void setCreatedTime(MetricFeedback feedback, OffsetDateTime createdTime) {
                feedback.setCreatedTime(createdTime);
            }

            @Override
            public void setUserPrincipal(MetricFeedback feedback, String userPrincipal) {
                feedback.setUserPrincipal(userPrincipal);
            }

            @Override
            public void setFeedbackType(MetricFeedback feedback, FeedbackType feedbackType) {
                feedback.setFeedbackType(feedbackType);
            }
        });
    }

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
     * Get the series keys value for the feedback.
     *
     * @return the dimensionFilter value.
     */
    public DimensionKey getDimensionFilter() {
        return this.dimensionFilter;
    }

    /**
     * Set the series keys value for the feedback.
     *
     * @param dimensionFilter the dimensionFilter value to set.
     *
     * @return the MetricFeedback object itself.
     */
    public MetricFeedback setDimensionFilter(final DimensionKey dimensionFilter) {
        this.dimensionFilter = dimensionFilter;
        return this;
    }

    void setId(String id) {
        this.id = id;
    }

    void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    void setCreatedTime(OffsetDateTime createdTime) {
        this.createdTime = createdTime;
    }

    void setUserPrincipal(String userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    void setFeedbackType(FeedbackType feedbackType) {
        this.feedbackType = feedbackType;
    }
}
