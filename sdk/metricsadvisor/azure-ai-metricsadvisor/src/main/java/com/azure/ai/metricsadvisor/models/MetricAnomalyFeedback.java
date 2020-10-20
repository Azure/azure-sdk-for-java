// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * The MetricAnomalyFeedback class.
 */
@Fluent
public final class MetricAnomalyFeedback extends MetricFeedback {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private final AnomalyValue anomalyValue;
    private AnomalyDetectionConfiguration detectionConfiguration;
    private String detectionConfigurationId;

    /**
     * Creates an instance of MetricAnomalyFeedback.
     *
     * @param startTime the start timestamp of feedback timerange
     * @param endTime the end timestamp of feedback timerange, when equals to startTime means
     * only one timestamp
     * @param anomalyValue the value of the anomaly.
     */
    public MetricAnomalyFeedback(OffsetDateTime startTime,
        OffsetDateTime endTime,
        AnomalyValue anomalyValue) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.anomalyValue = anomalyValue;
    }

    /**
     * Get the value of the anomaly..
     *
     * @return the value value.
     */
    public AnomalyValue getAnomalyValue() {
        return this.anomalyValue;
    }

    /**
     * Get the start timestamp of feedback timerange.
     *
     * @return the startTime value.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Get the end timestamp of feedback timerange, when equals to startTime means only one
     * timestamp.
     *
     * @return the endTime value.
     */
    public OffsetDateTime getEndTime() {
        return this.endTime;
    }

    /**
     * Get the corresponding anomaly detection configuration of this
     * feedback.
     *
     * @return the detectionConfiguration value.
     */
    public AnomalyDetectionConfiguration getDetectionConfiguration() {
        return this.detectionConfiguration;
    }

    /**
     * Set the corresponding anomaly detection configuration Id for this feedback.
     *
     * @param detectionConfigurationId the detectionConfigurationId value to set.
     * @return the MetricAnomalyFeedback object itself.
     */
    public MetricAnomalyFeedback setDetectionConfigurationId(
        final String detectionConfigurationId) {
        this.detectionConfigurationId = detectionConfigurationId;
        return this;
    }

    /**
     * Get the corresponding anomaly detection configuration Id of this
     * feedback.
     *
     * @return the detectionConfigurationId value.
     */
    public String getDetectionConfigurationId() {
        return detectionConfigurationId;
    }
}
