// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.AnomalyHelper;

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

    static {
        AnomalyHelper.setAccessor(new AnomalyHelper.AnomalyAccessor() {
            @Override
            public void setMetricId(Anomaly anomaly, String metricId) {
                anomaly.setMetricId(metricId);
            }

            @Override
            public void setSeriesKey(Anomaly anomaly, DimensionKey seriesKey) {
                anomaly.setSeriesKey(seriesKey);
            }

            @Override
            public void setDetectionConfigurationId(Anomaly anomaly, String detectionConfigurationId) {
                anomaly.setDetectionConfigurationId(detectionConfigurationId);
            }

            @Override
            public void setSeverity(Anomaly anomaly, Severity severity) {
                anomaly.setSeverity(severity);
            }

            @Override
            public void setStatus(Anomaly anomaly, AnomalyStatus status) {
                anomaly.setStatus(status);
            }

            @Override
            public void setTimeStamp(Anomaly anomaly, OffsetDateTime timeStamp) {
                anomaly.setTimeStamp(timeStamp);
            }

            @Override
            public void setCreatedTime(Anomaly anomaly, OffsetDateTime createdTime) {
                anomaly.setCreatedTime(createdTime);
            }

            @Override
            public void setModifiedTime(Anomaly anomaly, OffsetDateTime modifiedTime) {
                anomaly.setModifiedTime(modifiedTime);
            }
        });
    }

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


    void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    void setSeriesKey(DimensionKey seriesKey) {
        this.seriesKey = seriesKey;
    }

    void setDetectionConfigurationId(String detectionConfigurationId) {
        this.detectionConfigurationId = detectionConfigurationId;
    }

    void setSeverity(Severity severity) {
        this.severity = severity;
    }

    void setStatus(AnomalyStatus status) {
        this.status = status;
    }

    void setTimeStamp(OffsetDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    void setCreatedTime(OffsetDateTime createdTime) {
        this.createdTime = createdTime;
    }

    void setModifiedTime(OffsetDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
