// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.models.DataPointAnomaly;
import com.azure.ai.metricsadvisor.models.AnomalyStatus;
import com.azure.ai.metricsadvisor.models.DimensionKey;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link DataPointAnomaly} instance.
 */
public final class AnomalyHelper {
    private static AnomalyAccessor accessor;

    private AnomalyHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DataPointAnomaly} instance.
     */
    public interface AnomalyAccessor {
        void setDataFeedId(DataPointAnomaly anomaly, String dataFeedId);
        void setMetricId(DataPointAnomaly anomaly, String metricId);
        void setSeriesKey(DataPointAnomaly anomaly, DimensionKey seriesKey);
        void setDetectionConfigurationId(DataPointAnomaly anomaly, String detectionConfigurationId);
        void setSeverity(DataPointAnomaly anomaly, AnomalySeverity severity);
        void setStatus(DataPointAnomaly anomaly, AnomalyStatus status);
        void setTimeStamp(DataPointAnomaly anomaly, OffsetDateTime timeStamp);
        void setCreatedTime(DataPointAnomaly anomaly, OffsetDateTime createdTime);
        void setModifiedTime(DataPointAnomaly anomaly, OffsetDateTime modifiedTime);
        void setValue(DataPointAnomaly anomaly, Double value);
        void setExpectedValue(DataPointAnomaly anomaly, Double value);
    }

    /**
     * The method called from {@link DataPointAnomaly} to set it's accessor.
     *
     * @param anomalyAccessor The accessor.
     */
    public static void setAccessor(final AnomalyAccessor anomalyAccessor) {
        accessor = anomalyAccessor;
    }

    static void setDataFeedId(DataPointAnomaly anomaly, String dataFeedId) {
        accessor.setDataFeedId(anomaly, dataFeedId);
    }

    static void setMetricId(DataPointAnomaly anomaly, String metricId) {
        accessor.setMetricId(anomaly, metricId);
    }

    static void setSeriesKey(DataPointAnomaly anomaly, DimensionKey seriesKey) {
        accessor.setSeriesKey(anomaly, seriesKey);
    }

    static void setDetectionConfigurationId(DataPointAnomaly anomaly, String detectionConfigurationId) {
        accessor.setDetectionConfigurationId(anomaly, detectionConfigurationId);
    }

    static void setSeverity(DataPointAnomaly anomaly, AnomalySeverity severity) {
        accessor.setSeverity(anomaly, severity);
    }

    static void setStatus(DataPointAnomaly anomaly, AnomalyStatus status) {
        accessor.setStatus(anomaly, status);
    }

    static void setTimeStamp(DataPointAnomaly anomaly, OffsetDateTime timeStamp) {
        accessor.setTimeStamp(anomaly, timeStamp);
    }

    static void setCreatedTime(DataPointAnomaly anomaly, OffsetDateTime createdTime) {
        accessor.setCreatedTime(anomaly, createdTime);
    }

    static void setModifiedTime(DataPointAnomaly anomaly, OffsetDateTime modifiedTime) {
        accessor.setModifiedTime(anomaly, modifiedTime);
    }

    static void setValue(DataPointAnomaly anomaly, Double value) {
        accessor.setValue(anomaly, value);
    }

    static void setExpectedValue(DataPointAnomaly anomaly, Double value) {
        accessor.setExpectedValue(anomaly, value);
    }
}
