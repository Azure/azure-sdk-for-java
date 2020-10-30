// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.AnomalyStatus;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.Severity;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link Anomaly} instance.
 */
public final class AnomalyHelper {
    private static AnomalyAccessor accessor;

    private AnomalyHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link Anomaly} instance.
     */
    public interface AnomalyAccessor {
        void setMetricId(Anomaly anomaly, String metricId);
        void setSeriesKey(Anomaly anomaly, DimensionKey seriesKey);
        void setDetectionConfigurationId(Anomaly anomaly, String detectionConfigurationId);
        void setSeverity(Anomaly anomaly, Severity severity);
        void setStatus(Anomaly anomaly, AnomalyStatus status);
        void setTimeStamp(Anomaly anomaly, OffsetDateTime timeStamp);
        void setCreatedTime(Anomaly anomaly, OffsetDateTime createdTime);
        void setModifiedTime(Anomaly anomaly, OffsetDateTime modifiedTime);
    }

    /**
     * The method called from {@link Anomaly} to set it's accessor.
     *
     * @param anomalyAccessor The accessor.
     */
    public static void setAccessor(final AnomalyAccessor anomalyAccessor) {
        accessor = anomalyAccessor;
    }

    static void setMetricId(Anomaly anomaly, String metricId) {
        accessor.setMetricId(anomaly, metricId);
    }

    static void setSeriesKey(Anomaly anomaly, DimensionKey seriesKey) {
        accessor.setSeriesKey(anomaly, seriesKey);
    }

    static void setDetectionConfigurationId(Anomaly anomaly, String detectionConfigurationId) {
        accessor.setDetectionConfigurationId(anomaly, detectionConfigurationId);
    }

    static void setSeverity(Anomaly anomaly, Severity severity) {
        accessor.setSeverity(anomaly, severity);
    }

    static void setStatus(Anomaly anomaly, AnomalyStatus status) {
        accessor.setStatus(anomaly, status);
    }

    static void setTimeStamp(Anomaly anomaly, OffsetDateTime timeStamp) {
        accessor.setTimeStamp(anomaly, timeStamp);
    }

    static void setCreatedTime(Anomaly anomaly, OffsetDateTime createdTime) {
        accessor.setCreatedTime(anomaly, createdTime);
    }

    static void setModifiedTime(Anomaly anomaly, OffsetDateTime modifiedTime) {
        accessor.setModifiedTime(anomaly, modifiedTime);
    }
}
