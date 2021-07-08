// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Type that describes hard-threshold parameters. In hard-threshold mode, metrics advisor
 * watch for any data points that falls out of the boundary and detect such data points as
 * anomalies.
 */
@Fluent
public final class HardThresholdCondition {
    private Double lowerBound;
    private Double upperBound;
    private AnomalyDetectorDirection anomalyDetectorDirection;
    private SuppressCondition suppressCondition;

    /**
     * Create an instance of HardThresholdCondition describing how to identify anomalies using
     * hard-threshold mode.
     *
     * @param detectorDirection a value {@link AnomalyDetectorDirection#BOTH} indicates that any
     *     data point with a value not within the range defined by {@code lowerBound} and {@code upperBound}
     *     should be considered as an anomaly. A value {@link AnomalyDetectorDirection#UP} means a data point
     *     above {@code upperBound} is considered as an anomaly, a value {@link AnomalyDetectorDirection#DOWN}
     *     means a data point below {@code lowerBound} is considered as an anomaly.
     * @param suppressCondition the condition to aggregate the anomaly detection reporting,
     *     suppressing the reporting of individual anomalies helps to avoid noises, especially if the metrics
     *     have fine granularity.
     */
    public HardThresholdCondition(AnomalyDetectorDirection detectorDirection,
                                      SuppressCondition suppressCondition) {
        this.anomalyDetectorDirection = detectorDirection;
        this.suppressCondition = suppressCondition;
    }

    /**
     * Gets the lowerBound value.
     *
     * @return the lowerBound value.
     */
    public Double getLowerBound() {
        return this.lowerBound;
    }

    /**
     * Gets the upperBound value.
     *
     * @return the upperBound value.
     */
    public Double getUpperBound() {
        return this.upperBound;
    }

    /**
     * Gets the direction that detector should use when comparing data point
     * value against lowerBound and/or upperBound.
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
     * Sets the lowerBound value.
     *
     * <p>lowerBound be specified when anomalyDetectorDirection is Both or Down.
     *
     * @param lowerBound the lowerBound value to set.
     * @return the HardThresholdCondition object itself.
     */
    public HardThresholdCondition setLowerBound(Double lowerBound) {
        this.lowerBound = lowerBound;
        return this;
    }

    /**
     * Set the upperBound value.
     *
     * <p>upperBound should be specified when anomalyDetectorDirection is Both or Up.
     *
     * @param upperBound the upperBound value to set.
     * @return the HardThresholdCondition object itself.
     */
    public HardThresholdCondition setUpperBound(Double upperBound) {
        this.upperBound = upperBound;
        return this;
    }

    /**
     * Sets the direction that detector should use when comparing data point value against lowerBound and/or upperBound.
     *
     * @param detectorDirection the detector direction
     *
     * @return the HardThresholdCondition object itself.
     */
    public HardThresholdCondition setAnomalyDetectorDirection(AnomalyDetectorDirection detectorDirection) {
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
    public HardThresholdCondition setSuppressCondition(SuppressCondition suppressCondition) {
        this.suppressCondition = suppressCondition;
        return this;
    }
}
