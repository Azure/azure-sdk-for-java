// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Type that describes configuration for snoozing anomaly alerts.
 */
@Fluent
public final class MetricAnomalyAlertSnoozeCondition {
    private Integer autoSnooze;
    private SnoozeScope snoozeScope;
    private Boolean onlyForSuccessive;

    /**
     * Create an instance of MetricAnomalyAlertSnoozeCondition describing anomaly alert snooze configuration.
     *
     * @param autoSnooze the snooze data point count.
     * @param snoozeScope the scope in which anomalies to be snoozed may appear.
     * @param onlyForSuccessive true to snooze successive anomalies, false otherwise.
     */
    public MetricAnomalyAlertSnoozeCondition(int autoSnooze,
                                             SnoozeScope snoozeScope,
                                             boolean onlyForSuccessive) {
        this.autoSnooze = autoSnooze;
        this.snoozeScope = snoozeScope;
        this.onlyForSuccessive = onlyForSuccessive;
    }

    /**
     * Gets the snooze data point count.
     *
     * @return the snooze data point count.
     */
    public Integer getAutoSnooze() {
        return this.autoSnooze;
    }

    /**
     * Gets the scope in which anomalies to be snoozed may appear.
     *
     * @return the scope.
     */
    public SnoozeScope getSnoozeScope() {
        return this.snoozeScope;
    }

    /**
     * Gets boolean indicating whether to snooze successive anomalies or not.
     *
     * @return the onlyForSuccessive value.
     */
    public Boolean isOnlyForSuccessive() {
        return this.onlyForSuccessive;
    }

    /**
     * Sets the value indicating the snooze point count, the value range is : [0, +∞).
     *
     * @param autoSnooze the the snooze point count.
     * @return the MetricAnomalyAlertSnoozeCondition object itself.
     */
    public MetricAnomalyAlertSnoozeCondition setAutoSnooze(Integer autoSnooze) {
        this.autoSnooze = autoSnooze;
        return this;
    }

    /**
     * Sets the scope in which anomalies to be snoozed may appear.
     *
     * @param snoozeScope the snooze scope to set.
     * @return the MetricAnomalyAlertSnoozeCondition object itself.
     */
    public MetricAnomalyAlertSnoozeCondition setSnoozeScope(SnoozeScope snoozeScope) {
        this.snoozeScope = snoozeScope;
        return this;
    }

    /**
     * Sets the flag indicating whether to snooze the successive anomalies.
     *
     * @param onlyForSuccessive true to snooze the successive anomalies, false otherwise.
     * @return the MetricAnomalyAlertSnoozeCondition object itself.
     */
    public MetricAnomalyAlertSnoozeCondition setOnlyForSuccessive(Boolean onlyForSuccessive) {
        this.onlyForSuccessive = onlyForSuccessive;
        return this;
    }
}
