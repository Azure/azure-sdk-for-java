// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.core.annotation.Immutable;

/**
 * Defines scope for anomaly alert. An alert can be scoped to whole
 * series of a metric, a specific time series group of a metric
 * or a metric TopN time series.
 */
@Immutable
public final class MetricAnomalyAlertScope {
    private final MetricAnomalyAlertScopeType scopeType;
    private final DimensionKey seriesGroupId;
    private final TopNGroupScope topNGroup;

    private MetricAnomalyAlertScope() {
        this.scopeType = MetricAnomalyAlertScopeType.WHOLE_SERIES;
        this.seriesGroupId = null;
        this.topNGroup = null;
    }

    private MetricAnomalyAlertScope(DimensionKey seriesGroupId) {
        this.scopeType = MetricAnomalyAlertScopeType.SERIES_GROUP;
        this.seriesGroupId = seriesGroupId;
        this.topNGroup = null;
    }

    private MetricAnomalyAlertScope(TopNGroupScope topNGroup) {
        this.scopeType = MetricAnomalyAlertScopeType.TOP_N;
        this.topNGroup = topNGroup;
        this.seriesGroupId = null;
    }

    /**
     * Gets the scope type.
     *
     * @return The scope type.
     */
    public MetricAnomalyAlertScopeType getScopeType() {
        return this.scopeType;
    }

    /**
     * Gets the id of the time series group if alert is scoped to that group.
     *
     * @return The time series group id.
     */
    public DimensionKey getSeriesGroupInScope() {
        return this.seriesGroupId;
    }

    /**
     * Gets the TopN scope value if alert is scoped to TopN time series.
     *
     * @return The TopN scope value.
     */
    public TopNGroupScope getTopNGroupInScope() {
        return this.topNGroup;
    }

    /**
     * Creates an MetricAnomalyAlertScope indicating that alert should be generated when
     * anomalies are detected in any of the time series of a metric.
     *
     * @return The MetricAnomalyAlertScope.
     */
    public static MetricAnomalyAlertScope forWholeSeries() {
        return new MetricAnomalyAlertScope();
    }

    /**
     * Creates an MetricAnomalyAlertScope indicating that alert should be generated when
     * anomalies are detected in a specific time series group of a metric.
     *
     * @param seriesGroupId the specific series keys.
     * @return The MetricAnomalyAlertScope.
     */
    public static MetricAnomalyAlertScope forSeriesGroup(DimensionKey seriesGroupId) {
        return new MetricAnomalyAlertScope(seriesGroupId);
    }

    /**
     * Creates an MetricAnomalyAlertScope based on TopN scoping rule.
     *
     * @param top Defines the top rank for anomalies, An alert is generated when rank of
     *          {@code minTopCount} data-sets are in {@code top}.
     * @param period The number of latest data-set to consider for ranking. One data-set
     *               consists of data-points with the same timestamp across multiple
     *               time series, i.e, one data-point from one time-series.
     * @param minTopCount The anomalies occurred in the {@code period} data-sets are ranked,
     *                    An alert is generated when rank of {@code minTopCount} data-sets
     *                   are in {@code top}.
     *
     * @return The MetricAnomalyAlertScope.
     */
    public static MetricAnomalyAlertScope forTopNGroup(int top, int period, int minTopCount) {
        return new MetricAnomalyAlertScope(new TopNGroupScope(top, period, minTopCount));
    }

    /**
     * Creates an MetricAnomalyAlertScope based on TopN scope.
     *
     * The TopN defines scope based on ranking of anomaly in data-sets.
     * The computation of TopN scope consists of 3 parameters, top, period and
     * minTopCount. the top defines the top rank for anomalies, period defines
     * the number of latest data-set to consider for ranking and minTopCount
     * indicate that an alert should be generated when rank of minTopCount
     * data-sets are in top.
     *
     * @param topNGroup Defines the top, period and minTopCount values.
     *
     * @return The MetricAnomalyAlertScope.
     */
    public static MetricAnomalyAlertScope forTopNGroup(TopNGroupScope topNGroup) {
        return new MetricAnomalyAlertScope(topNGroup);
    }
}
