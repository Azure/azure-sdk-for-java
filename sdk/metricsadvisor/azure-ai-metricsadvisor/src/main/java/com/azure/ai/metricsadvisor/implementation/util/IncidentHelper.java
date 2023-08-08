// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.AnomalyIncidentStatus;
import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.AnomalyIncident;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link AnomalyIncident} instance.
 */
public final class IncidentHelper {
    private static IncidentAccessor accessor;

    private IncidentHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnomalyIncident} instance.
     */
    public interface IncidentAccessor {
        void setId(AnomalyIncident incident, String id);
        void setDataFeedId(AnomalyIncident incident, String dataFeedId);
        void setMetricId(AnomalyIncident incident, String metricId);
        void setDetectionConfigurationId(AnomalyIncident incident, String detectionConfigurationId);
        void setRootDimensionKey(AnomalyIncident incident, DimensionKey rootDimensionKey);
        void setValue(AnomalyIncident incident, Double value);
        void setExpectedValue(AnomalyIncident incident, Double value);
        void setSeverity(AnomalyIncident incident, AnomalySeverity severity);
        void setStatus(AnomalyIncident incident, AnomalyIncidentStatus status);
        void setStartTime(AnomalyIncident incident, OffsetDateTime startTime);
        void setLastTime(AnomalyIncident incident, OffsetDateTime lastTime);
    }

    /**
     * The method called from {@link AnomalyIncident} to set it's accessor.
     *
     * @param incidentAccessor The accessor.
     */
    public static void setAccessor(final IncidentAccessor incidentAccessor) {
        accessor = incidentAccessor;
    }

    public static void setId(AnomalyIncident incident, String id) {
        accessor.setId(incident, id);
    }

    static void setDataFeedId(AnomalyIncident incident, String dataFeedId) {
        accessor.setDataFeedId(incident, dataFeedId);
    }

    static void setMetricId(AnomalyIncident incident, String metricId) {
        accessor.setMetricId(incident, metricId);
    }

    public static void setDetectionConfigurationId(AnomalyIncident incident, String detectionConfigurationId) {
        accessor.setDetectionConfigurationId(incident, detectionConfigurationId);
    }

    static void setRootDimensionKey(AnomalyIncident incident, DimensionKey rootDimensionKey) {
        accessor.setRootDimensionKey(incident, rootDimensionKey);
    }

    static void setValue(AnomalyIncident incident, Double value) {
        accessor.setValue(incident, value);
    }

    static void setExpectedValue(AnomalyIncident incident, Double value) {
        accessor.setExpectedValue(incident, value);
    }

    static void setSeverity(AnomalyIncident incident, AnomalySeverity severity) {
        accessor.setSeverity(incident, severity);
    }

    static void setStatus(AnomalyIncident incident, AnomalyIncidentStatus status) {
        accessor.setStatus(incident, status);
    }

    static void setStartTime(AnomalyIncident incident, OffsetDateTime startTime) {
        accessor.setStartTime(incident, startTime);
    }

    static void setLastTime(AnomalyIncident incident, OffsetDateTime lastTime) {
        accessor.setLastTime(incident, lastTime);
    }
}
