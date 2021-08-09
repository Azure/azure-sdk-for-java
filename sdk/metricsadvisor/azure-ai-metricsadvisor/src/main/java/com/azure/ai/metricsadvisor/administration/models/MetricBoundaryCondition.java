// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.MetricBoundaryConditionHelper;
import com.azure.core.annotation.Fluent;

/**
 * Defines the boundary conditions for the anomaly (abnormal data points) to be included in the alert.
 */
@Fluent
public final class MetricBoundaryCondition {
    private BoundaryDirection boundaryDirection;
    private Double lowerBoundary;
    private Double upperBoundary;
    private String companionMetricId;
    private Boolean alertIfMissing;
    private BoundaryMeasureType measureType;

    static {
        MetricBoundaryConditionHelper.setAccessor(new MetricBoundaryConditionHelper.MetricBoundaryConditionAccessor() {
            @Override
            public void setLowerBoundary(MetricBoundaryCondition condition, Double lowerBoundary) {
                condition.setLowerBoundary(lowerBoundary);
            }

            @Override
            public void setUpperBoundary(MetricBoundaryCondition condition, Double upperBoundary) {
                condition.setUpperBoundary(upperBoundary);
            }

            @Override
            public void setBoundaryDirection(MetricBoundaryCondition condition, BoundaryDirection boundaryDirection) {
                condition.setBoundaryDirection(boundaryDirection);
            }
        });
    }

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
     * True if alert will be triggered when the companion-metric data-points are out
     * of boundary but the corresponding data-point is missing for the original metric.
     *
     * @return True if alert is triggered for missing data-points, false otherwise.
     */
    public Boolean shouldAlertIfDataPointMissing() {
        return this.alertIfMissing;
    }

    /**
     * Gets the measure type that detector should use for measuring data-points.
     *
     * @return the measure type.
     */
    public BoundaryMeasureType getMeasureType() {
        return this.measureType;
    }

    /**
     * Sets the boundary.
     *
     * @param direction Both {@code lowerBoundary} and {@code upperBoundary} must be specified
     *     when the direction is {@link BoundaryDirection#BOTH}. The {@code lowerBoundary}
     *     must be specified for {@link BoundaryDirection#LOWER}, similarly {@code upperBoundary}
     *     must set specified for {@link BoundaryDirection#UPPER}.
     * @param lowerBoundary The lower boundary value.
     * @param upperBoundary The upper boundary value.
     * @return The MetricBoundaryCondition object itself.
     */
    public MetricBoundaryCondition setBoundary(BoundaryDirection direction,
                                               Double lowerBoundary,
                                               Double upperBoundary) {
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
        this.boundaryDirection = direction;
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

    /**
     * Sets the measure type that detector should use for measuring data-points.
     *
     * @param measureType the type of measure to use.
     *
     * @return The MetricBoundaryCondition object itself.
     */
    public MetricBoundaryCondition setMeasureType(BoundaryMeasureType measureType) {
        this.measureType = measureType;
        return this;
    }

    void setLowerBoundary(Double lowerBoundary) {
        this.lowerBoundary = lowerBoundary;
    }

    void setUpperBoundary(Double upperBoundary) {
        this.upperBoundary = upperBoundary;
    }

    void setBoundaryDirection(BoundaryDirection boundaryDirection) {
        this.boundaryDirection = boundaryDirection;
    }
}
