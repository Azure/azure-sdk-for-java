// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.AnomalyFeedback;
import com.azure.ai.metricsadvisor.implementation.models.ChangePointFeedback;
import com.azure.ai.metricsadvisor.implementation.models.CommentFeedback;
import com.azure.ai.metricsadvisor.implementation.models.FeedbackDimensionFilter;
import com.azure.ai.metricsadvisor.implementation.models.FeedbackQueryTimeMode;
import com.azure.ai.metricsadvisor.implementation.models.MetricFeedbackFilter;
import com.azure.ai.metricsadvisor.implementation.models.PeriodFeedback;
import com.azure.ai.metricsadvisor.models.AnomalyValue;
import com.azure.ai.metricsadvisor.models.ChangePointValue;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.FeedbackType;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackFilter;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackOptions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyFeedback;
import com.azure.ai.metricsadvisor.models.MetricChangePointFeedback;
import com.azure.ai.metricsadvisor.models.MetricCommentFeedback;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.ai.metricsadvisor.models.MetricPeriodFeedback;
import com.azure.ai.metricsadvisor.models.PeriodType;
import com.azure.core.util.logging.ClientLogger;

import java.util.UUID;


/**
 * Expose transformation methods to transform {@link MetricFeedback} model to
 * REST API wire model and vice-versa.
 */
public final class MetricFeedbackTransforms {
    private static final ClientLogger LOGGER = new ClientLogger(MetricFeedbackTransforms.class);

    private MetricFeedbackTransforms() {
    }

    /**
     * Transform configuration wire model to {@link MetricFeedback}.
     *
     * @param metricFeedbackValue The wire model instance.
     *
     * @return The custom model instance.
     */
    public static MetricFeedback fromInner(
        com.azure.ai.metricsadvisor.implementation.models.MetricFeedback metricFeedbackValue) {
        MetricFeedback metricFeedback;
        if (metricFeedbackValue instanceof AnomalyFeedback) {
            final AnomalyFeedback anomalyFeedback = (AnomalyFeedback) metricFeedbackValue;
            metricFeedback = new MetricAnomalyFeedback(
                anomalyFeedback.getStartTime(),
                anomalyFeedback.getEndTime(),
                AnomalyValue.fromString(anomalyFeedback.getValue().getAnomalyValue().toString()))
                .setDetectionConfigurationId(
                    anomalyFeedback.getAnomalyDetectionConfigurationId() != null
                    ? anomalyFeedback.getAnomalyDetectionConfigurationId().toString()
                    : null);

            if (anomalyFeedback.getAnomalyDetectionConfigurationSnapshot() != null) {
                MetricAnomalyFeedbackHelper.setDetectionConfiguration((MetricAnomalyFeedback) metricFeedback,
                    DetectionConfigurationTransforms
                        .fromInner(anomalyFeedback.getAnomalyDetectionConfigurationSnapshot()));
            }
            MetricFeedbackHelper.setFeedbackType(metricFeedback, FeedbackType.ANOMALY);
        } else if (metricFeedbackValue instanceof ChangePointFeedback) {
            final ChangePointFeedback changePointFeedback = (ChangePointFeedback) metricFeedbackValue;
            metricFeedback = new MetricChangePointFeedback(
                changePointFeedback.getStartTime(),
                changePointFeedback.getEndTime(),
                ChangePointValue.fromString(changePointFeedback.getValue().getChangePointValue().toString()));
            MetricFeedbackHelper.setFeedbackType(metricFeedback, FeedbackType.CHANGE_POINT);
        } else if (metricFeedbackValue instanceof PeriodFeedback) {
            final PeriodFeedback periodFeedback = (PeriodFeedback) metricFeedbackValue;
            metricFeedback = new MetricPeriodFeedback(
                PeriodType.fromString(periodFeedback.getValue().getPeriodType().toString()),
                periodFeedback.getValue().getPeriodValue());
            MetricFeedbackHelper.setFeedbackType(metricFeedback, FeedbackType.PERIOD);
        } else if (metricFeedbackValue instanceof CommentFeedback) {
            final CommentFeedback commentFeedback = (CommentFeedback) metricFeedbackValue;
            metricFeedback = new MetricCommentFeedback(
                commentFeedback.getStartTime(),
                commentFeedback.getEndTime(),
                commentFeedback.getValue().getCommentValue());
            MetricFeedbackHelper.setFeedbackType(metricFeedback, FeedbackType.COMMENT);
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown feedback type."));
        }
        MetricFeedbackHelper.setId(metricFeedback, metricFeedbackValue.getFeedbackId().toString());
        MetricFeedbackHelper.setMetricId(metricFeedback, metricFeedbackValue.getMetricId().toString());
        MetricFeedbackHelper.setCreatedTime(metricFeedback, metricFeedbackValue.getCreatedTime());
        MetricFeedbackHelper.setUserPrincipal(metricFeedback, metricFeedbackValue.getUserPrincipal());
        metricFeedback.setDimensionFilter(new DimensionKey(metricFeedbackValue.getDimensionFilter().getDimension()));
        return metricFeedback;
    }

    /**
     * Transform the filter options to wire model {@link MetricFeedbackFilter}.
     *
     * @param metricId the metric unique Id.
     * @param options the configurable options to set when listing metric feedbacks.
     *
     * @return the wire model instance.
     */
    public static MetricFeedbackFilter toInnerFilter(String metricId,
                                                     ListMetricFeedbackOptions options) {
        final ListMetricFeedbackFilter listMetricFeedbackFilter =
            options.getFilter() == null ? new ListMetricFeedbackFilter() : options.getFilter();

        MetricFeedbackFilter metricFeedbackFilter = new MetricFeedbackFilter()
            .setFeedbackType(listMetricFeedbackFilter.getFeedbackType())
            .setMetricId(UUID.fromString(metricId))
            .setStartTime(listMetricFeedbackFilter.getStartTime())
            .setEndTime(listMetricFeedbackFilter.getEndTime())
            .setTimeMode(listMetricFeedbackFilter.getTimeMode() == null
                ? null : FeedbackQueryTimeMode.fromString(listMetricFeedbackFilter.getTimeMode().toString()));

        if (listMetricFeedbackFilter.getDimensionFilter() != null) {
            metricFeedbackFilter.setDimensionFilter(new FeedbackDimensionFilter()
                .setDimension(listMetricFeedbackFilter.getDimensionFilter().asMap()));
        }

        return metricFeedbackFilter;
    }
}
