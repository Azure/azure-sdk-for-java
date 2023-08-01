// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Defines alerting settings for anomalies detected by a detection
 * configuration.
 */
@Fluent
public final class MetricAlertConfiguration {
    private final String detectionConfigurationId;
    private boolean negationOperation;
    private MetricAnomalyAlertScope alertScope;
    private MetricAnomalyAlertConditions alertConditions;
    private MetricAnomalyAlertSnoozeCondition snoozeCondition;

    /**
     * Creates a new instance of MetricAlertConfiguration.
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param alertScope The scope for the alert.
     */
    public MetricAlertConfiguration(String detectionConfigurationId,
                                    MetricAnomalyAlertScope alertScope) {
        this(detectionConfigurationId, alertScope, false);
    }

    /**
     * Creates a new instance of MetricAlertConfiguration.
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param alertScope The scope for the alert.
     * @param negationOperation if true anomalies in this configuration will not be included in alerts,
     *     instead result of applying the configuration become an expression to take part in the operation
     *     performed among corresponding series in other configurations. The result of expression is true
     *     only if there is no anomaly found in the series.
     */
    public MetricAlertConfiguration(String detectionConfigurationId,
                                    MetricAnomalyAlertScope alertScope,
                                    boolean negationOperation) {
        this.detectionConfigurationId = detectionConfigurationId;
        this.negationOperation = negationOperation;
        this.alertScope =  alertScope;
    }

    /**
     * Gets the anomaly detection configuration id.
     *
     * @return The detection configuration id.
     */
    public String getDetectionConfigurationId() {
        return this.detectionConfigurationId;
    }

    /**
     * True if this configuration is used only as an expression to take part in the operation
     * performed among corresponding series in other configurations.
     *
     * @return The negation operation value.
     */
    public Boolean isNegationOperationEnabled() {
        return this.negationOperation;
    }

    /**
     * Gets the alert scope.
     *
     * @return The alert scope.
     */
    public MetricAnomalyAlertScope getAlertScope() {
        return this.alertScope;
    }

    /**
     * Gets the conditions applied on the detected anomalies to decide whether it should be included
     * in the alert or not.
     *
     * @return The alert conditions.
     */
    public MetricAnomalyAlertConditions getAlertConditions() {
        return this.alertConditions;
    }

    /**
     * Gets the condition to snooze the inclusion of anomalies in the upcoming alerts.
     *
     * @return The snooze condition.
     */
    public MetricAnomalyAlertSnoozeCondition getAlertSnoozeCondition() {
        return this.snoozeCondition;
    }

    /**
     * Sets the negation value.
     *
     * if true anomalies in this configuration will not be included in alerts,
     * instead result of applying the configuration become an expression to take part in the operation
     * performed among corresponding series in other configurations. The result of expression is true
     * only if there is no anomaly found in the series.
     *
     * @param negationOperation The negation operation value.
     * @return The MetricAlertConfiguration object itself.
     */
    public MetricAlertConfiguration setNegationOperation(boolean negationOperation) {
        this.negationOperation = negationOperation;
        return this;
    }

    /**
     * Sets the alert scope.
     *
     * @param alertScope The alert scope.
     * @return The MetricAlertConfiguration object itself.
     */
    public MetricAlertConfiguration setScopeOfAlertTo(MetricAnomalyAlertScope alertScope) {
        this.alertScope = alertScope;
        return this;
    }

    /**
     * Sets the conditions to be applied on the detected anomalies to decide whether it should be included
     * in the alert or not.
     *
     * @param alertConditions The alert conditions.
     * @return The MetricAlertConfiguration object itself.
     */
    public MetricAlertConfiguration setAlertConditions(MetricAnomalyAlertConditions alertConditions) {
        this.alertConditions = alertConditions;
        return this;
    }

    /**
     * Sets the snooze condition. Once anomalies are alerted, this condition will be used to snooze the inclusion
     * of anomalies in upcoming alerts.
     *
     * @param snoozeCondition The snooze condition.
     * @return The MetricAlertConfiguration object itself.
     */
    public MetricAlertConfiguration setAlertSnoozeCondition(MetricAnomalyAlertSnoozeCondition snoozeCondition) {
        this.snoozeCondition = snoozeCondition;
        return this;
    }
}
