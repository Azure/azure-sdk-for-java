// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Type that describes smart-detection parameters. In smart-detection mode, metrics advisor
 * uses multiple ML based algorithms to compute severity value of data points, detector detect
 * anomalies by checking whether those values falls within or outside of the range derived from
 * sensitivity parameter.
 */
@Fluent
public final class SmartDetectionCondition {
    private Double sensitivity;
    private AnomalyDetectorDirection anomalyDetectorDirection;
    private SuppressCondition suppressCondition;

    /**
     * Create an instance of SmartDetectionCondition describing how to identify anomalies using
     * smart-detection mode.
     *
     * @param sensitivity value that adjust the tolerance of anomalies, visually higher the value
     *     narrower the band (lower and upper bounds) around the time series.
     * @param detectorDirection a value {@link AnomalyDetectorDirection#BOTH} indicates that any
     *     data point with severity value not within the range derived from {@code sensitivity}
     *     should be considered as an anomaly. A value {@link AnomalyDetectorDirection#UP} means
     *     a data point severity value above the upper bound of the range is considered as an anomaly,
     *     a value {@link AnomalyDetectorDirection#DOWN} means a data point severity value below lower
     *     bound of the range is considered as an anomaly.
     * @param suppressCondition the condition to aggregate the anomaly detection reporting,
     *     suppressing the reporting of individual anomalies helps to avoid noises, especially
     *     if the metrics have fine granularity.
     */
    public SmartDetectionCondition(double sensitivity,
                                   AnomalyDetectorDirection detectorDirection,
                                   SuppressCondition suppressCondition) {
        this.sensitivity = sensitivity;
        this.anomalyDetectorDirection = detectorDirection;
        this.suppressCondition = suppressCondition;
    }

    /**
     * Gets the sensitivity value.
     *
     * <p> the sensitivity value adjust the tolerance of anomalies, visually higher the value narrower the band
     * (lower and upper bounds) around the time series.
     *
     * @return the sensitivity value.
     */
    public Double getSensitivity() {
        return this.sensitivity;
    }

    /**
     * Gets the direction that detector should use when comparing data point
     * value against range derived from sensitivity .
     *
     * @return the detector direction.
     */
    public AnomalyDetectorDirection getAnomalyDetectorDirection() {
        return this.anomalyDetectorDirection;
    }


    /**
     * Gets the suppress condition.
     *
     * @return the suppress condition value.
     */
    public SuppressCondition getSuppressCondition() {
        return this.suppressCondition;
    }


    /**
     * Sets the sensitivity value, it should be in the range (0, 100].
     *
     * @param sensitivity the sensitivity value to set.
     * @return the SmartDetectionCondition object itself.
     */
    public SmartDetectionCondition setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
        return this;
    }

    /**
     * Sets the direction that detector should use when comparing data point value against range derived
     * from the sensitivity.
     *
     * @param detectorDirection the detector direction
     *
     * @return the HardThresholdCondition object itself.
     */
    public SmartDetectionCondition setAnomalyDetectorDirection(AnomalyDetectorDirection detectorDirection) {
        this.anomalyDetectorDirection = detectorDirection;
        return this;
    }

    /**
     * Sets the suppress condition.
     *
     * @param suppressCondition the suppress condition
     *
     * @return the HardThresholdCondition object itself.
     */
    public SmartDetectionCondition setSuppressCondition(SuppressCondition suppressCondition) {
        this.suppressCondition = suppressCondition;
        return this;
    }
}
