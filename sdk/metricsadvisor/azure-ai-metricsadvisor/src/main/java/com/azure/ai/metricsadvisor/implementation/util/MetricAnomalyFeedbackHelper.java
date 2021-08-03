// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.models.MetricAnomalyFeedback;

/**
 * The helper class to set the non-public properties of an {@link MetricAnomalyFeedback} instance.
 */
public final class MetricAnomalyFeedbackHelper {
    private static MetricAnomalyFeedbackAccessor accessor;

    private MetricAnomalyFeedbackHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MetricAnomalyFeedback} instance.
     */
    public interface MetricAnomalyFeedbackAccessor {
        void setDetectionConfiguration(MetricAnomalyFeedback feedback, AnomalyDetectionConfiguration configuration);
    }

    /**
     * The method called from {@link MetricAnomalyFeedback} to set it's accessor.
     *
     * @param feedbackAccessor The accessor.
     */
    public static void setAccessor(final MetricAnomalyFeedbackAccessor feedbackAccessor) {
        accessor = feedbackAccessor;
    }

    static void setDetectionConfiguration(MetricAnomalyFeedback feedback,
                                          AnomalyDetectionConfiguration configuration) {
        accessor.setDetectionConfiguration(feedback, configuration);
    }
}
