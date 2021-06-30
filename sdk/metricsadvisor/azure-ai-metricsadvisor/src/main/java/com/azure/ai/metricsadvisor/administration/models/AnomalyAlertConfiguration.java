// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.AnomalyAlertConfigurationHelper;
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
    private MetricAlertConfigurationsOperator crossMetricsOperator;
    private List<MetricAlertConfiguration> metricAnomalyAlertConfigurations;
    private List<String> hookIds;
    private List<String> splitAlertByDimensions;

    static {
        AnomalyAlertConfigurationHelper.setAccessor(
            new AnomalyAlertConfigurationHelper.AnomalyAlertConfigurationAccessor() {
                @Override
                public void setId(AnomalyAlertConfiguration configuration, String id) {
                    configuration.setId(id);
                }

                @Override
                public List<String> getHookIdsToAlertRaw(AnomalyAlertConfiguration configuration) {
                    return configuration.getHookIdsToAlertRaw();
                }

                @Override
                public List<String> getDimensionsToSplitAlertRaw(AnomalyAlertConfiguration configuration) {
                    return configuration.getDimensionsToSplitAlertRaw();
                }
            });
    }

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
     * metric level {@link MetricAlertConfiguration} in the alert configuration.
     */
    public AnomalyAlertConfiguration(String name,
        MetricAlertConfigurationsOperator crossMetricsOperator) {
        this.name = name;
        this.crossMetricsOperator = crossMetricsOperator;
        this.metricAnomalyAlertConfigurations = new ArrayList<>();
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
    public MetricAlertConfigurationsOperator getCrossMetricsOperator() {
        return this.crossMetricsOperator;
    }

    /**
     * Gets all metric level alert configurations.
     *
     * @return The metric level alert configurations.
     */
    public List<MetricAlertConfiguration> getMetricAlertConfigurations() {
        return Collections.unmodifiableList(this.metricAnomalyAlertConfigurations);
    }

    /**
     * Gets id of all hooks that receives alerts triggered by this configuration.
     *
     * @return The hook ids.
     */
    public List<String> getHookIdsToAlert() {
        if (this.hookIds != null) {
            return Collections.unmodifiableList(this.hookIds);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Gets the dimensions names used to split a single alert into multiple ones.
     *
     * @return The dimension names.
     */
    public List<String> getDimensionsToSplitAlert() {
        return Collections.unmodifiableList(this.splitAlertByDimensions);
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
        MetricAlertConfigurationsOperator crossMetricsOperator) {
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
    public AnomalyAlertConfiguration addMetricAlertConfiguration(MetricAlertConfiguration configuration) {
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
        List<MetricAlertConfiguration> configurations) {
        if (configurations == null) {
            this.metricAnomalyAlertConfigurations = new ArrayList<>();
        } else {
            this.metricAnomalyAlertConfigurations = configurations;
        }
        return this;
    }

    /**
     * Sets the hooks to receives alerts triggered by this configuration.
     *
     * @param hookIds The hook ids.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration setHookIdsToAlert(List<String> hookIds) {
        this.hookIds = hookIds;
        return this;
    }

    /**
     * Sets the dimensions names used to split a single alert into multiple ones.
     *
     * @param dimensions The hook ids.
     *
     * @return The AnomalyAlertConfiguration object itself.
     */
    public AnomalyAlertConfiguration setDimensionsToSplitAlert(List<String> dimensions) {
        this.splitAlertByDimensions = dimensions;
        return this;
    }

    private void setId(String id) {
        this.id = id;
    }

    private List<String> getHookIdsToAlertRaw() {
        // Getter that won't translate null hookIds to empty-list.
        return this.hookIds;
    }

    private List<String> getDimensionsToSplitAlertRaw() {
        // Getter that won't translate null splitAlertByDimensions to empty-list.
        return this.splitAlertByDimensions;
    }
}
