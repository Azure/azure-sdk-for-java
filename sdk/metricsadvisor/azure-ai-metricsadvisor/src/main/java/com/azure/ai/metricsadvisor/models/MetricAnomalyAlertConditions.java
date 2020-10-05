// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * Defines conditions to decide whether the detected anomalies should be
 * included in an alert or not.
 */
public final class MetricAnomalyAlertConditions {
    private MetricBoundaryCondition boundaryCondition;
    private SeverityCondition severityCondition;

    /**
     * Gets the boundary condition, an anomaly will be included in the alert
     * if it's value is not within the boundary.
     *
     * @return The boundary condition.
     */
    public MetricBoundaryCondition getMetricBoundaryCondition() {
        return this.boundaryCondition;
    }

    /**
     * Gets the severity range based condition, an anomaly will be included in the alert
     * only if it's severity falls in the range.
     *
     * @return The severity condition.
     */
    public SeverityCondition getSeverityCondition() {
        return this.severityCondition;
    }

    /**
     * Gets the boundary condition, an anomaly will be included in the alert
     * only if it's value is not within the boundary.
     *
     * @param boundaryCondition The boundary condition.
     * @return The MetricAnomalyAlertConditions object itself.
     */
    public MetricAnomalyAlertConditions setMetricBoundaryCondition(MetricBoundaryCondition boundaryCondition) {
        this.boundaryCondition = boundaryCondition;
        return this;
    }

    /**
     * Sets the severity range based condition, an anomaly will be included in the alert
     * only if it's severity falls in the range.
     *
     * @param min The lower bound of severity range.
     * @param max The upper bound of severity range.
     * @return The MetricAnomalyAlertConditions object itself.
     */
    public MetricAnomalyAlertConditions setSeverityCondition(Severity min, Severity max) {
        return setSeverityCondition(new SeverityCondition()
            .setMinAlertSeverity(min)
            .setMaxAlertSeverity(max));
    }

    /**
     * Sets the severity range based condition, an anomaly will be included in the alert
     * only if it's severity falls in the range.
     *
     * @param severityCondition The condition based on severity of anomalies.
     * @return The MetricAnomalyAlertConditions object itself.
     */
    public MetricAnomalyAlertConditions setSeverityCondition(SeverityCondition severityCondition) {
        this.severityCondition = severityCondition;
        return this;
    }
}
