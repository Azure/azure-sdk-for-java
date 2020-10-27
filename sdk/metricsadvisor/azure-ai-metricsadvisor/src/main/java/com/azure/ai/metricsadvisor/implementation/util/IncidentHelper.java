// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.Incident;
import com.azure.ai.metricsadvisor.models.IncidentStatus;
import com.azure.ai.metricsadvisor.models.Severity;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link Incident} instance.
 */
public final class IncidentHelper {
    private static IncidentAccessor accessor;

    private IncidentHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link Incident} instance.
     */
    public interface IncidentAccessor {
        void setId(Incident incident, String id);
        void setMetricId(Incident incident, String metricId);
        void setDetectionConfigurationId(Incident incident, String detectionConfigurationId);
        void setRootDimensionKey(Incident incident, DimensionKey rootDimensionKey);
        void setSeverity(Incident incident, Severity severity);
        void setStatus(Incident incident, IncidentStatus status);
        void setStartTime(Incident incident, OffsetDateTime startTime);
        void setLastTime(Incident incident, OffsetDateTime lastTime);
    }

    /**
     * The method called from {@link Incident} to set it's accessor.
     *
     * @param incidentAccessor The accessor.
     */
    public static void setAccessor(final IncidentAccessor incidentAccessor) {
        accessor = incidentAccessor;
    }

    public static void setId(Incident incident, String id) {
        accessor.setId(incident, id);
    }

    static void setMetricId(Incident incident, String metricId) {
        accessor.setMetricId(incident, metricId);
    }

    public static void setDetectionConfigurationId(Incident incident, String detectionConfigurationId) {
        accessor.setDetectionConfigurationId(incident, detectionConfigurationId);
    }

    static void setRootDimensionKey(Incident incident, DimensionKey rootDimensionKey) {
        accessor.setRootDimensionKey(incident, rootDimensionKey);
    }

    static void setSeverity(Incident incident, Severity severity) {
        accessor.setSeverity(incident, severity);
    }

    static void setStatus(Incident incident, IncidentStatus status) {
        accessor.setStatus(incident, status);
    }

    static void setStartTime(Incident incident, OffsetDateTime startTime) {
        accessor.setStartTime(incident, startTime);
    }

    static void setLastTime(Incident incident, OffsetDateTime lastTime) {
        accessor.setLastTime(incident, lastTime);
    }
}
