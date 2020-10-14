// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines anomaly alerting configuration.
 */
@Fluent
public final class AnomalyAlertConfiguration {
    private String id;
    private final String name;
    private String description;
    private MetricAnomalyAlertConfigurationsOperator crossMetricsOperator;
    private List<MetricAnomalyAlertConfiguration> metricAnomalyAlertConfigurations;
    private List<String> hookIds;

    /**
     * Create a new instance of AnomalyAlertConfiguration.
     *
     * @param name The configuration name.
     */
    public AnomalyAlertConfiguration(String name) {
        this(name, null);
    }

    /**
     * Create a new instance of AnomalyAlertConfiguration.
     *
     * @param name The configuration name.
     * @param crossMetricsOperator The logical operator to apply across multiple
     * metric level {@link MetricAnomalyAlertConfiguration} in the alert configuration.
     */
    public AnomalyAlertConfiguration(String name,
        MetricAnomalyAlertConfigurationsOperator crossMetricsOperator) {
        this.name = name;
        this.crossMetricsOperator = crossMetricsOperator;
        this.metricAnomalyAlertConfigurations = new ArrayList<>();
        this.hookIds = new ArrayList<>();
    }

    /**
     * Gets the alert configuration id.
     *
     * @return The configuration id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the alert configuration name.
     *
     * @return The configuration name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the alert configuration description.
     *
     * @return The configuration description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the logical operator to apply across multiple metric level alert configurations.
     *
     * @return The cross metric alert configuration operator.
     */
    public MetricAnomalyAlertConfigurationsOperator getCrossMetricsOperator() {
        return this.crossMetricsOperator;
    }

    /**
     * Gets all metric level alert configurations.
     *
     * @return The metric level alert configurations.
     */
    public List<MetricAnomalyAlertConfiguration> getMetricAlertConfigurations() {
        return Collections.unmodifiableList(this.metricAnomalyAlertConfigurations);
    }

    /**
     * Gets id of all hooks that receives alerts triggered by this configuration.
     *
     * @return The hook ids.
     */
    public List<String> getIdOfHooksToAlert() {
        return Collections.unmodifiableList(this.hookIds);
    }

    /**
     * Sets the description for the configuration.
     *
     * @param description The configuration description.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the logical operator to apply across across multiple metric level alert configurations.
     *
     * @param crossMetricsOperator The cross metric alert configuration operator.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration setCrossMetricsOperator(
        MetricAnomalyAlertConfigurationsOperator crossMetricsOperator) {
        this.crossMetricsOperator = crossMetricsOperator;
        return this;
    }

    /**
     * Adds a new metric level alert configuration.
     *
     * @param configuration The configuration.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration addMetricAlertConfiguration(MetricAnomalyAlertConfiguration configuration) {
        this.metricAnomalyAlertConfigurations.add(configuration);
        return this;
    }

    /**
     * Sets the metric level alert configurations.
     *
     * @param configurations The configuration.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration setMetricAlertConfigurations(
        List<MetricAnomalyAlertConfiguration> configurations) {
        if (configurations == null) {
            this.metricAnomalyAlertConfigurations = new ArrayList<>();
        } else {
            this.metricAnomalyAlertConfigurations = configurations;
        }
        return this;
    }

    /**
     * Adds a new hook to receive the alert.
     *
     * @param hookId The hook id.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration addIdOfHookToAlert(String hookId) {
        this.hookIds.add(hookId);
        return this;
    }

    /**
     * Removes a hook from the alert list.
     *
     * @param hookId The hook id.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration removeHookToAlert(String hookId) {
        this.hookIds.remove(hookId);
        return this;
    }

    /**
     * Sets the hooks to receives alerts triggered by this configuration.
     *
     * @param hookIds The hook ids.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration setIdOfHooksToAlert(List<String> hookIds) {
        if (hookIds == null) {
            this.hookIds = new ArrayList<>();
        } else {
            this.hookIds = hookIds;
        }
        return this;
    }
}
