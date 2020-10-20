// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.time.OffsetDateTime;

/**
 * Describes an incident detected in a time series or a time series group.
 */
public final class Incident {
    private String id;
    private String metricId;
    private String detectionConfigurationId;
    private DimensionKey rootDimensionKey;
    private Severity severity;
    private IncidentStatus status;
    private OffsetDateTime startTime;
    private OffsetDateTime lastTime;

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
    public Severity getSeverity() {
        return this.severity;
    }

    /**
     * Gets the incident status.
     *
     * @return The incident status.
     */
    public  IncidentStatus getStatus() {
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
}
