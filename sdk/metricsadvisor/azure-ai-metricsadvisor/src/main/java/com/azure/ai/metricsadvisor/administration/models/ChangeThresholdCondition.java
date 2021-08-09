// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Type that describes change-threshold parameters. In change-threshold mode, metrics advisor watch for
 * any data points that changes over a change percentage compared to the previous data points and detect
 * such data points as anomalies.
 */
@Fluent
public final class ChangeThresholdCondition {
    private Double changePercentage;
    private Integer shiftPoint;
    private Boolean isWithinRange;
    private AnomalyDetectorDirection anomalyDetectorDirection;
    private SuppressCondition suppressCondition;

    /**
     * Create an instance of ChangeThresholdCondition describing how to identify anomalies using
     * change-threshold mode.
     *
     * @param changePercentage the percentage of change that will considered as an anomaly,
     *     the data point will be compared with previously captured data points
     *     for computing the change.
     * @param shiftPoint the number of data points that detector should look back for comparison.
     * @param isWithinRage when set to true, data point is an anomaly when the value falls in the range;
     *     in this case, detectorDirection must be Both. When set false, the data point
     *     is an anomaly when the value falls out of the range.
     * @param detectorDirection must be {@link AnomalyDetectorDirection#BOTH} when {@code isWithinRage} is true.
     *     When {@code isWithinRage} is false, An {@link AnomalyDetectorDirection#UP} value indicates that
     *     the data point should be considered as an anomaly, if its changes (compared to previous data points)
     *     more than the {@code changePercentage}. {@link AnomalyDetectorDirection#DOWN} value means a data point
     *     should be considered as anomaly, if the change (compared to previous data points) falls below negated
     *     {@code changePercentage} value.
     * @param suppressCondition the condition to aggregate the anomaly detection reporting,
     *     suppressing the reporting of individual anomalies helps to avoid noises, especially if the metrics
     *     have fine granularity.
     */
    public ChangeThresholdCondition(double changePercentage,
                                    int shiftPoint,
                                    boolean isWithinRage,
                                    AnomalyDetectorDirection detectorDirection,
                                    SuppressCondition suppressCondition) {
        this.changePercentage = changePercentage;
        this.shiftPoint = shiftPoint;
        this.isWithinRange = isWithinRage;
        this.anomalyDetectorDirection = detectorDirection;
        this.suppressCondition = suppressCondition;
    }

    /**
     * Gets the percentage of change that will consider a data point as an anomaly.
     *
     * @return the change percentage value.
     */
    public Double getChangePercentage() {
        return changePercentage;
    }

    /**
     * Gets the number of data points that detector should look back for comparison.
     *
     * @return the shift point value.
     */
    public Integer getShiftPoint() {
        return shiftPoint;
    }

    /**
     * Gets the flag indicating whether the data point falls within or out of the range is considered as anomaly.
     *
     * @return the withinRange value.
     */
    public Boolean isWithinRange() {
        return isWithinRange;
    }

    /**
     * Gets the direction that detector should use when comparing the data point change with change threshold.
     *
     * @return the detector direction.
     */
    public AnomalyDetectorDirection getAnomalyDetectorDirection() {
        return anomalyDetectorDirection;
    }

    /**
     * Gets the suppress condition.
     *
     * @return the suppress condition value.
     */
    public SuppressCondition getSuppressCondition() {
        return suppressCondition;
    }

    /**
     * Sets the percentage of change that will consider a data point as an anomaly.
     *
     * @param changePercentage the change percentage value.
     *
     * @return the ChangeThresholdCondition object itself.
     */
    public ChangeThresholdCondition setChangePercentage(Double changePercentage) {
        this.changePercentage = changePercentage;
        return this;
    }

    /**
     * Sets the number of data points that detector should look back for comparison.
     *
     * @param shiftPoint the shift point value.
     *
     * @return the ChangeThresholdCondition object itself.
     */
    public ChangeThresholdCondition setShiftPoint(Integer shiftPoint) {
        this.shiftPoint = shiftPoint;
        return this;
    }

    /**
     * Sets the flag indicating whether the data point falls within or out of the range is considered as anomaly.
     *
     * @param withinRange the withinRange value.
     *
     * @return the ChangeThresholdCondition object itself.
     */
    public ChangeThresholdCondition setWithinRage(Boolean withinRange) {
        this.isWithinRange = withinRange;
        return this;
    }

    /**
     * Sets the direction that detector should use when comparing the data point change with change threshold.
     *
     * @param detectorDirection the detector direction
     *
     * @return the ChangeThresholdCondition object itself.
     */
    public ChangeThresholdCondition setAnomalyDetectorDirection(AnomalyDetectorDirection detectorDirection) {
        this.anomalyDetectorDirection = detectorDirection;
        return this;
    }

    /**
     * Sets the suppress condition.
     *
     * @param suppressCondition the suppress condition
     *
     * @return the ChangeThresholdCondition object itself.
     */
    public ChangeThresholdCondition setSuppressCondition(SuppressCondition suppressCondition) {
        this.suppressCondition = suppressCondition;
        return this;
    }
}
