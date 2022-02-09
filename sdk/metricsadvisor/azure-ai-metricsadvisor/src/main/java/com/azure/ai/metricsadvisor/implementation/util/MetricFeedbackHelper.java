// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.FeedbackType;
import com.azure.ai.metricsadvisor.models.MetricFeedback;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link MetricFeedback} instance.
 */
public final class MetricFeedbackHelper {
    private static MetricFeedbackAccessor accessor;

    private MetricFeedbackHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MetricFeedback} instance.
     */
    public interface MetricFeedbackAccessor {
        void setId(MetricFeedback feedback, String id);
        void setMetricId(MetricFeedback feedback, String metricId);
        void setCreatedTime(MetricFeedback feedback, OffsetDateTime createdTime);
        void setUserPrincipal(MetricFeedback feedback, String userPrincipal);
        void setFeedbackType(MetricFeedback feedback, FeedbackType feedbackType);
    }

    /**
     * The method called from {@link MetricFeedback} to set it's accessor.
     *
     * @param feedbackAccessor The accessor.
     */
    public static void setAccessor(final MetricFeedbackAccessor feedbackAccessor) {
        accessor = feedbackAccessor;
    }

    static void setId(MetricFeedback feedback, String id) {
        accessor.setId(feedback, id);
    }

    static void setMetricId(MetricFeedback feedback, String metricId) {
        accessor.setMetricId(feedback, metricId);
    }

    static void setCreatedTime(MetricFeedback feedback, OffsetDateTime createdTime) {
        accessor.setCreatedTime(feedback, createdTime);
    }

    static void setUserPrincipal(MetricFeedback feedback, String userPrincipal) {
        accessor.setUserPrincipal(feedback, userPrincipal);
    }

    static void setFeedbackType(MetricFeedback feedback, FeedbackType feedbackType) {
        accessor.setFeedbackType(feedback, feedbackType);
    }
}
