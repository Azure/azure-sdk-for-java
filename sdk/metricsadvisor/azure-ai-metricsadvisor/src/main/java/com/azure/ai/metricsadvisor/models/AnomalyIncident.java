// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.IncidentHelper;

import java.time.OffsetDateTime;

/**
 * Describes an incident detected in a time series or a time series group.
 */
public final class AnomalyIncident {
    private String id;
    private String metricId;
    private String detectionConfigurationId;
    private DimensionKey rootDimensionKey;
    private AnomalySeverity severity;
    private AnomalyIncidentStatus status;
    private OffsetDateTime startTime;
    private OffsetDateTime lastTime;

    static {
        IncidentHelper.setAccessor(new IncidentHelper.IncidentAccessor() {
            @Override
            public void setId(AnomalyIncident incident, String id) {
                incident.setId(id);
            }

            @Override
            public void setMetricId(AnomalyIncident incident, String metricId) {
                incident.setMetricId(metricId);
            }

            @Override
            public void setDetectionConfigurationId(AnomalyIncident incident, String detectionConfigurationId) {
                incident.setDetectionConfigurationId(detectionConfigurationId);
            }

            @Override
            public void setRootDimensionKey(AnomalyIncident incident, DimensionKey rootDimensionKey) {
                incident.setRootDimensionKey(rootDimensionKey);
            }

            @Override
            public void setSeverity(AnomalyIncident incident, AnomalySeverity severity) {
                incident.setSeverity(severity);
            }

            @Override
            public void setStatus(AnomalyIncident incident, AnomalyIncidentStatus status) {
                incident.setStatus(status);
            }

            @Override
            public void setStartTime(AnomalyIncident incident, OffsetDateTime startTime) {
                incident.setStartTime(startTime);
            }

            @Override
            public void setLastTime(AnomalyIncident incident, OffsetDateTime lastTime) {
                incident.setLastTime(lastTime);
            }
        });
    }
    /**
     * Gets the incident id.
     *
     * @return The incident id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the metric id.
     *
     * @return The metric id.
     */
    public String getMetricId() {
        return this.metricId;
    }

    /**
     * Get the id of the configuration used to detect the anomalies
     * that resulted in the incident.
     *
     * @return The anomaly detection configuration id.
     */
    public String getDetectionConfigurationId() {
        return this.detectionConfigurationId;
    }

    /**
     * Out of all dimension nodes in the dimension combination tree,
     * get the dimension node that is identified as the root node where
     * the incident anomalies are logically aggregated.
     *
     * @return id of the root dimension node.
     */
    public DimensionKey getRootDimensionKey() {
        return this.rootDimensionKey;
    }

    /**
     * Gets the severity of the incident.
     *
     * @return The severity.
     */
    public AnomalySeverity getSeverity() {
        return this.severity;
    }

    /**
     * Gets the incident status.
     *
     * @return The incident status.
     */
    public AnomalyIncidentStatus getStatus() {
        return this.status;
    }

    /**
     * Gets the time in which incident started.
     *
     * @return The start time.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the most recent time in which incident occurred.
     *
     * @return The last time.
     */
    public OffsetDateTime getLastTime() {
        return this.lastTime;
    }

    void setId(String id) {
        this.id = id;
    }

    void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    void setDetectionConfigurationId(String detectionConfigurationId) {
        this.detectionConfigurationId = detectionConfigurationId;
    }

    void setRootDimensionKey(DimensionKey rootDimensionKey) {
        this.rootDimensionKey = rootDimensionKey;
    }

    void setSeverity(AnomalySeverity severity) {
        this.severity = severity;
    }

    void setStatus(AnomalyIncidentStatus status) {
        this.status = status;
    }

    void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    void setLastTime(OffsetDateTime lastTime) {
        this.lastTime = lastTime;
    }
}
