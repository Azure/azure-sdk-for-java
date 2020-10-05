// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * Defines the boundary conditions for the anomaly (abnormal data points)
 * to be included in the alert.
 */
public final class MetricBoundaryCondition {
    private BoundaryDirection boundaryDirection;
    private Double lowerBoundary;
    private Double upperBoundary;
    private String companionMetricId;
    private Boolean alertIfMissing;

    /**
     * Gets the boundary direction.
     *
     * @return The boundary direction.
     */
    public BoundaryDirection getDirection() {
        return this.boundaryDirection;
    }

    /**
     * Gets the lower boundary, if the detected abnormal data point
     * is below this value then it will be included in the alert.
     *
     * This boundary is taken into consideration only if the direction
     * is either {@link BoundaryDirection#LOWER} or {@link BoundaryDirection#BOTH}.
     *
     * @return The lower boundary.
     */
    public Double getLowerBoundary() {
        return this.lowerBoundary;
    }

    /**
     * Gets the upper boundary. if the detected abnormal data point
     * is above this value then it will be included in the alert.
     *
     * This boundary is taken into consideration only if the direction
     * is either {@link BoundaryDirection#UPPER} or {@link BoundaryDirection#BOTH}.
     *
     * @return The upper boundary.
     */
    public Double getUpperBoundary() {
        return this.upperBoundary;
    }

    /**
     * Gets the id of the companion metric.
     *
     * When the companion-metric is set for a metric, the abnormal data points
     * detected in the original metric will be included in the alert only if data points
     * values of corresponding series in companion-metric are not within the boundary.
     *
     * @return The companion metric id.
     */
    public String getCompanionMetricId() {
        return this.companionMetricId;
    }

    /**
     * Sets either upper or lower boundary.
     *
     * @param direction The boundary direction.
     * @param boundaryValue The boundary value.
     * @return The MetricBoundaryCondition object itself.
     */
    public MetricBoundaryCondition setSingleBoundary(SingleBoundaryDirection direction,
                                                     double boundaryValue) {
        if (direction == SingleBoundaryDirection.LOWER) {
            this.boundaryDirection = BoundaryDirection.LOWER;
            this.lowerBoundary = boundaryValue;
            this.upperBoundary = null;
        } else {
            this.boundaryDirection = BoundaryDirection.UPPER;
            this.upperBoundary = boundaryValue;
            this.lowerBoundary = null;
        }
        return this;
    }

    /**
     * Sets both upper and lower boundary.
     *
     * @param lower The lower boundary value.
     * @param upper The upper boundary value.
     * @return The MetricBoundaryCondition object itself.
     */
    public MetricBoundaryCondition setBothBoundary(double lower, double upper) {
        this.lowerBoundary = lower;
        this.upperBoundary = upper;
        this.boundaryDirection = BoundaryDirection.BOTH;
        return this;
    }

    /**
     * Sets the companion metric id.
     *
     * When the companion-metric is set for a metric, an anomaly detected in the original
     * metric will be included in the alert only if data points values of corresponding
     * series in companion-metric are not within the boundary.
     *
     * @param companionMetricId The companion metric id.
     * @return The MetricBoundaryCondition object itself.
     */
    public MetricBoundaryCondition setCompanionMetricId(String companionMetricId) {
        this.companionMetricId = companionMetricId;
        return this;
    }

    /**
     * True if alert will be triggered when the companion-metric data-points are out
     * of boundary but the corresponding data-point is missing for the original metric.
     *
     * @return True if alert is triggered for missing data-points, false otherwise.
     */
    public boolean shouldAlertIfDataPointMissing() {
        if (this.alertIfMissing == null) {
            return false;
        }
        return this.alertIfMissing;
    }

    /**
     * Sets the companion metric id.
     *
     * When the companion-metric is set for a metric, an anomaly detected in the original
     * metric series will be included in the alert only if data points values of corresponding
     * series in companion-metric are not within the boundary.
     *
     * @param companionMetricId The companion metric id.
     * @param alertIfMissing Indicate whether or not alert should be triggered when
     *     the companion-metric data-points are out of boundary but the corresponding data-point
     *     is missing for the original metric.
     *
     * @return The MetricBoundaryCondition object itself.
     */
    public MetricBoundaryCondition setCompanionMetricId(String companionMetricId,
                                                        boolean alertIfMissing) {
        this.companionMetricId = companionMetricId;
        this.alertIfMissing = alertIfMissing;
        return this;
    }
}
