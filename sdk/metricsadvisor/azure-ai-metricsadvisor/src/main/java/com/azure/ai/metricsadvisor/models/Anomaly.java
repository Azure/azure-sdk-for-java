// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.time.OffsetDateTime;

/**
 * Describes an anomaly detected in a metric series.
 */
public final class Anomaly {
    private String metricId;
    private DimensionKey seriesKey;
    private String detectionConfigurationId;
    private Severity severity;
    private AnomalyStatus status;
    private OffsetDateTime timeStamp;
    private OffsetDateTime createdTime;
    private OffsetDateTime modifiedTime;

    /**
     * Gets the metric id.
     *
     * @return The metric id.
     */
    public String getMetricId() {
        return this.metricId;
    }

    /**
     * Gets the id of the configuration used to detect the anomaly.
     *
     * @return The anomaly detection configuration id.
     */
    public String getDetectionConfigurationId() {
        return this.detectionConfigurationId;
    }

    /**
     * Gets the key of the series in which anomaly detected.
     *
     * @return key of the series.
     */
    public DimensionKey getSeriesKey() {
        return this.seriesKey;
    }

    /**
     * Gets the severity of the anomaly.
     *
     * @return The severity.
     */
    public Severity getSeverity() {
        return this.severity;
    }

    /**
     * Gets the anomaly status.
     *
     * @return The anomaly status.
     */
    public AnomalyStatus getStatus() {
        return this.status;
    }

    /**
     * Gets the anomaly time.
     *
     * @return The anomaly time.
     */
    public OffsetDateTime getTimestamp() {
        return this.timeStamp;
    }

    /**
     * Gets the time in which alert for this anomaly created.
     *
     * @return The start time.
     */
    public OffsetDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * Gets the time in which alert for this anomaly modified.
     *
     * @return The modified time.
     */
    public OffsetDateTime getModifiedTime() {
        return this.modifiedTime;
    }
}
