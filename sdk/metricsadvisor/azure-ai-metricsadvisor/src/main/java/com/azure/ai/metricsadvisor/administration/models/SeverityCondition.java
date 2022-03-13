// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Type that describes severity range.
 */
@Fluent
public final class SeverityCondition {
    private AnomalySeverity minAlertSeverity;
    private AnomalySeverity maxAlertSeverity;

    /**
     * Create a SeverityCondition with the range defined by the provided {@code min} and {@code max}.
     *
     * @param min the minimum severity value
     * @param max the maximum severity value
     */
    public SeverityCondition(AnomalySeverity min, AnomalySeverity max) {
        this.minAlertSeverity = min;
        this.maxAlertSeverity = max;
    }

    /**
     * Gets the minimum severity value.
     *
     * @return the minimum severity value.
     */
    public AnomalySeverity getMinAlertSeverity() {
        return this.minAlertSeverity;
    }

    /**
     * Gets the maximum severity value.
     *
     * @return the maximum severity value.
     */
    public AnomalySeverity getMaxAlertSeverity() {
        return this.maxAlertSeverity;
    }

    /**
     * Sets the minimum severity value.
     *
     * @param min the minimum severity value.
     * @return the SeverityCondition object itself.
     */
    public SeverityCondition setMinAlertSeverity(AnomalySeverity min) {
        this.minAlertSeverity = min;
        return this;
    }



    /**
     * Sets the maximum severity value.
     *
     * @param max the minimum severity value.
     * @return the SeverityCondition object itself.
     */
    public SeverityCondition setMaxAlertSeverity(AnomalySeverity max) {
        this.maxAlertSeverity = max;
        return this;
    }

}
