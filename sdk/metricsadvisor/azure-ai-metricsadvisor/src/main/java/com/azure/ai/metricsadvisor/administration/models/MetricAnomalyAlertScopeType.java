// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines the supported alert scopes.
 */
public final class MetricAnomalyAlertScopeType extends ExpandableStringEnum<MetricAnomalyAlertScopeType> {
    /**
     * Indicate that alert is scoped to the whole time series of a metric.
     */
    public static final MetricAnomalyAlertScopeType WHOLE_SERIES = fromString("WholeSeries");
    /**
     * Indicate that alert is scoped to a specific time series group of a metric.
     */
    public static final MetricAnomalyAlertScopeType SERIES_GROUP = fromString("SeriesGroup");
    /**
     * Indicate that alert is scoped to TopN time series.
     */
    public static final MetricAnomalyAlertScopeType TOP_N = fromString("TopN");

    /**
     * Creates or finds a MetricAnomalyAlertScopeType from its string representation.
     *
     * @param name The string name to look for.
     * @return The corresponding MetricAnomalyAlertScopeType.
     */
    public static MetricAnomalyAlertScopeType fromString(String name) {
        return fromString(name, MetricAnomalyAlertScopeType.class);
    }

    /**
     * Gets the collections of all MetricAnomalyAlertScopeType values.
     *
     * @return The collections of all MetricAnomalyAlertScopeType values.
     */
    public static Collection<MetricAnomalyAlertScopeType> values() {
        return values(MetricAnomalyAlertScopeType.class);
    }
}
