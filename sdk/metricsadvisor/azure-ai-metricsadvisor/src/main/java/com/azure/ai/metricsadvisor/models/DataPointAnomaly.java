// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.implementation.util.AnomalyHelper;

import java.time.OffsetDateTime;

/**
 * Describes an anomaly detected in a metric series.
 */
public final class DataPointAnomaly {
    private String dataFeedId;
    private String metricId;
    private DimensionKey seriesKey;
    private String detectionConfigurationId;
    private AnomalySeverity severity;
    private AnomalyStatus status;
    private OffsetDateTime timeStamp;
    private OffsetDateTime createdTime;
    private OffsetDateTime modifiedTime;
    private Double value;
    private Double expectedValue;

    static {
        AnomalyHelper.setAccessor(new AnomalyHelper.AnomalyAccessor() {
            @Override
            public void setDataFeedId(DataPointAnomaly anomaly, String dataFeedId) {
                anomaly.setDataFeedId(dataFeedId);
            }

            @Override
            public void setMetricId(DataPointAnomaly anomaly, String metricId) {
                anomaly.setMetricId(metricId);
            }

            @Override
            public void setSeriesKey(DataPointAnomaly anomaly, DimensionKey seriesKey) {
                anomaly.setSeriesKey(seriesKey);
            }

            @Override
            public void setDetectionConfigurationId(DataPointAnomaly anomaly, String detectionConfigurationId) {
                anomaly.setDetectionConfigurationId(detectionConfigurationId);
            }

            @Override
            public void setSeverity(DataPointAnomaly anomaly, AnomalySeverity severity) {
                anomaly.setSeverity(severity);
            }

            @Override
            public void setStatus(DataPointAnomaly anomaly, AnomalyStatus status) {
                anomaly.setStatus(status);
            }

            @Override
            public void setTimeStamp(DataPointAnomaly anomaly, OffsetDateTime timeStamp) {
                anomaly.setTimeStamp(timeStamp);
            }

            @Override
            public void setCreatedTime(DataPointAnomaly anomaly, OffsetDateTime createdTime) {
                anomaly.setCreatedTime(createdTime);
            }

            @Override
            public void setModifiedTime(DataPointAnomaly anomaly, OffsetDateTime modifiedTime) {
                anomaly.setModifiedTime(modifiedTime);
            }

            @Override
            public void setValue(DataPointAnomaly anomaly, Double value) {
                anomaly.setValue(value);
            }

            @Override
            public void setExpectedValue(DataPointAnomaly anomaly, Double value) {
                anomaly.setExpectedValue(value);
            }
        });
    }

    /**
     * Get the data feed id.
     *
     * @return The data feed id.
     */
    public String getDataFeedId() {
        return this.dataFeedId;
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
    public AnomalySeverity getSeverity() {
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

    /**
     * Gets the value of the anomaly.
     *
     * @return The anomaly value.
     */
    public Double getValue() {
        return this.value;
    }

    /**
     * Gets the expected value of the data point computed by the smart-detector
     * had it not an anomaly.
     *
     * @return The expected value.
     */
    public Double getExpectedValue() {
        return this.expectedValue;
    }

    void setDataFeedId(String dataFeedId) {
        this.dataFeedId = dataFeedId;
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

    void setSeverity(AnomalySeverity severity) {
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

    void setValue(Double value) {
        this.value = value;
    }

    void setExpectedValue(Double value) {
        this.expectedValue = value;
    }
}
