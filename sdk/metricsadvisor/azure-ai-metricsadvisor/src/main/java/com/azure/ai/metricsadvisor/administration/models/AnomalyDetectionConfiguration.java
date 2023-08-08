// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.AnomalyDetectionConfigurationHelper;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration to detect anomalies in metric time series.
 */
@Fluent
public final class AnomalyDetectionConfiguration {
    private String id;
    private String metricId;
    private String name;
    private String description;
    private MetricWholeSeriesDetectionCondition wholeSeriesCondition;
    private final List<MetricSeriesGroupDetectionCondition> seriesGroupConditions;
    private final List<MetricSingleSeriesDetectionCondition> seriesConditions;

    static {
        AnomalyDetectionConfigurationHelper
            .setAccessor(new AnomalyDetectionConfigurationHelper.AnomalyDetectionConfigurationAccessor() {
                @Override
                public void setId(AnomalyDetectionConfiguration configuration, String id) {
                    configuration.setId(id);
                }

                @Override
                public void setMetricId(AnomalyDetectionConfiguration configuration, String metricId) {
                    configuration.setMetricId(metricId);
                }
            });
    }

    /**
     * Create a new instance of MetricAnomalyDetectionConfiguration.
     *
     * @param name The configuration name.
     */
    public AnomalyDetectionConfiguration(String name) {
        this.name = name;
        this.seriesGroupConditions = new ArrayList<>();
        this.seriesConditions = new ArrayList<>();
    }

    /**
     * Gets the configuration id.
     *
     * @return The configuration id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the id of metric for which the configuration is applied.
     *
     * @return The metric id.
     */
    public String getMetricId() {
        return this.metricId;
    }

    /**
     * Gets the configuration name.
     *
     * @return The configuration name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the configuration description.
     *
     * @return The configuration description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the common anomaly detection conditions for all time series in the metric.
     *
     * @return The detection conditions for all time series.
     */
    public MetricWholeSeriesDetectionCondition getWholeSeriesDetectionCondition() {
        return this.wholeSeriesCondition;
    }

    /**
     * Gets the list of anomaly detection conditions, where each list entry describes
     * detection conditions for a group of time series.
     *
     * @return The list of anomaly detection conditions for time series group.
     */
    public List<MetricSeriesGroupDetectionCondition> getSeriesGroupDetectionConditions() {
        return Collections.unmodifiableList(this.seriesGroupConditions);
    }

    /**
     * Gets the list of anomaly detection conditions, where each list entry describes
     * detection conditions for a specific time series.
     *
     * @return The list of anomaly detection conditions for time series.
     */
    public List<MetricSingleSeriesDetectionCondition> getSeriesDetectionConditions() {
        return Collections.unmodifiableList(this.seriesConditions);
    }

    /**
     * Sets the configuration name.
     *
     * @param name The configuration name.
     * @return The MetricAnomalyDetectionConfiguration object itself.
     */
    public AnomalyDetectionConfiguration setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the configuration description.
     *
     * @param description The configuration description.
     * @return The MetricAnomalyDetectionConfiguration object itself.
     */
    public AnomalyDetectionConfiguration setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the common anomaly detection conditions for all time series of the metric.
     *
     * @param wholeSeriesCondition The detection conditions for all time series,
     *     a {@code null} value for this parameter is ignored.
     * @return The MetricAnomalyDetectionConfiguration object itself.
     */
    public AnomalyDetectionConfiguration setWholeSeriesDetectionCondition(
        MetricWholeSeriesDetectionCondition wholeSeriesCondition) {
        if (wholeSeriesCondition == null) {
            return this;
        }
        this.wholeSeriesCondition = wholeSeriesCondition;
        return this;
    }

    /**
     * Adds anomaly detection condition for a specific group of time series.
     *
     * @param groupCondition The detection conditions for a group of time series,
     *     a {@code null} value for this parameter is ignored.
     * @return The MetricAnomalyDetectionConfiguration object itself.
     */
    public AnomalyDetectionConfiguration addSeriesGroupDetectionCondition(
        MetricSeriesGroupDetectionCondition groupCondition) {
        if (groupCondition == null) {
            return this;
        }
        this.seriesGroupConditions.add(groupCondition);
        return this;
    }

    /**
     * Removes anomaly detection condition for a specific group of time series.
     *
     * @param seriesGroupKey Identifies the time series group to remove the conditions for,
     *     {@code null} value for this parameter is ignored.
     * @return The MetricAnomalyDetectionConfiguration object itself.
     */
    public AnomalyDetectionConfiguration removeSeriesGroupDetectionCondition(
        DimensionKey seriesGroupKey) {
        if (seriesGroupKey == null) {
            return this;
        }
        int idx = 0;
        for (MetricSeriesGroupDetectionCondition condition : this.seriesGroupConditions) {
            if (condition.getSeriesGroupKey().equals(seriesGroupKey)) {
                break;
            }
            idx++;
        }
        if (idx != this.seriesGroupConditions.size()) {
            this.seriesGroupConditions.remove(idx);
        }
        return this;
    }

    /**
     * Adds anomaly detection condition for a specific time series.
     *
     * @param seriesCondition The detection conditions for a specific time series,
     *     a {@code null} value for this parameter is ignored.
     * @return The MetricAnomalyDetectionConfiguration object itself.
     */
    public AnomalyDetectionConfiguration
        addSingleSeriesDetectionCondition(MetricSingleSeriesDetectionCondition seriesCondition) {
        if (seriesCondition == null) {
            return this;
        }
        this.seriesConditions.add(seriesCondition);
        return this;
    }

    /**
     * Removes anomaly detection condition for a specific time series.
     *
     * @param seriesKey The key identifying the time series,
     *     a {@code null} value for this parameter is ignored.
     * @return The MetricAnomalyDetectionConfiguration object itself.
     */
    public AnomalyDetectionConfiguration removeSingleSeriesDetectionCondition(
        DimensionKey seriesKey) {
        if (seriesKey == null) {
            return this;
        }
        int idx = 0;
        for (MetricSingleSeriesDetectionCondition condition : this.seriesConditions) {
            if (condition.getSeriesKey().equals(seriesKey)) {
                break;
            }
            idx++;
        }
        if (idx != this.seriesConditions.size()) {
            this.seriesConditions.remove(idx);
        }
        return this;
    }

    void setId(String id) {
        this.id = id;
    }

    void setMetricId(String metricId) {
        this.metricId = metricId;
    }
}
