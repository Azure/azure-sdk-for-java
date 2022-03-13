// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.core.annotation.Fluent;

/**
 * Conditions to detect anomalies in a group of time series.
 */
@Fluent
public final class MetricSeriesGroupDetectionCondition {
    private final DimensionKey seriesGroupKey;
    private DetectionConditionOperator conditionOperator;
    private SmartDetectionCondition smartDetectionCondition;
    private HardThresholdCondition hardThresholdCondition;
    private ChangeThresholdCondition changeThresholdCondition;

    /**
     * Create an instance of MetricSeriesGroupAnomalyDetectionCondition.
     *
     * @param seriesGroupKey The time series key that identifies a group of series to apply the detection condition.
     */
    public MetricSeriesGroupDetectionCondition(DimensionKey seriesGroupKey) {
        super();
        this.seriesGroupKey = seriesGroupKey;
    }

    /**
     * Get the time series key that identifies a group of series to apply the detection condition.
     *
     * @return The time series group id.
     */
    public DimensionKey getSeriesGroupKey() {
        return this.seriesGroupKey;
    }

    /**
     * Gets the logical operator applied across conditions.
     *
     * @return The logical operator applied across conditions.
     */
    public DetectionConditionOperator getConditionOperator() {
        return this.conditionOperator;
    }

    /**
     * Gets the anomaly smart detection condition.
     *
     * <p>
     * The smart detection condition defines the numerical value to adjust the tolerance of
     * the anomalies, the higher the value, the narrower the band (upper/lower bounds) around
     * time series. The series data points those are not within such boundaries are detected
     * as anomalies.
     * </p>
     *
     * @return The Smart detection condition.
     */
    public SmartDetectionCondition getSmartDetectionCondition() {
        return this.smartDetectionCondition;
    }

    /**
     * Gets the hard threshold condition to detect anomalies.
     *
     * <p>
     * The smart detection condition defines the numerical value to adjust the tolerance of
     * the anomalies, the higher the value, the narrower the band (upper/lower bounds) around
     * time series. The series data points those are not within the boundaries are detected
     * as anomalies.
     * </p>
     *
     * @return The hard threshold condition.
     */
    public HardThresholdCondition getHardThresholdCondition() {
        return this.hardThresholdCondition;
    }

    /**
     * Gets the change threshold condition to detect anomalies.
     *
     * <p>
     * The change threshold condition defines change percentage; the value of a data point is compared
     * with previous data points; if the change percentage of the value is in or out of the range,
     * then that data point is detected as anomalies.
     * </p>
     *
     * @return The change threshold condition.
     */
    public ChangeThresholdCondition getChangeThresholdCondition() {
        return this.changeThresholdCondition;
    }


    /**
     * Sets the logical operator to apply across conditions.
     *
     * @param conditionOperator The logical operator.
     * @return The MetricSeriesGroupDetectionCondition object itself.
     */
    public MetricSeriesGroupDetectionCondition setConditionOperator(
        DetectionConditionOperator conditionOperator) {
        this.conditionOperator = conditionOperator;
        return this;
    }

    /**
     * Sets the smart detection condition.
     *
     * <p>
     * The smart detection condition defines the numerical value to adjust the tolerance of
     * the anomalies, the higher the value, the narrower the band (upper/lower bounds) around
     * time series. The series data points those are not within such boundaries are detected
     * as anomalies.
     * </p>
     *
     * @param smartDetectionCondition The smart detection condition.
     * @return The MetricSeriesGroupDetectionCondition object itself.
     */
    public MetricSeriesGroupDetectionCondition setSmartDetectionCondition(
        SmartDetectionCondition smartDetectionCondition) {
        this.smartDetectionCondition = smartDetectionCondition;
        return this;
    }

    /**
     * Sets the hard threshold condition.
     *
     * <p>
     * The hard threshold condition defines boundaries, the series data points
     * those are not within the boundaries are detected as anomalies.
     * </p>
     *
     * @param hardThresholdCondition The hard threshold condition.
     * @return The MetricSeriesGroupDetectionCondition object itself.
     */
    public MetricSeriesGroupDetectionCondition setHardThresholdCondition(
        HardThresholdCondition hardThresholdCondition) {
        this.hardThresholdCondition = hardThresholdCondition;
        return this;
    }

    /**
     * Sets the change threshold condition.
     *
     * <p>
     * The change threshold condition defines change percentage; the value of a data point is compared
     * with previous data points; if the change percentage of the value is in or out of the range,
     * then that data point is detected as anomalies.
     * </p>
     *
     * @param changeThresholdCondition The change threshold condition.
     * @return The MetricSeriesGroupDetectionCondition object itself.
     */
    public MetricSeriesGroupDetectionCondition setChangeThresholdCondition(ChangeThresholdCondition
                                                                    changeThresholdCondition) {
        this.changeThresholdCondition = changeThresholdCondition;
        return this;
    }
}
