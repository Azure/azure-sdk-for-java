// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricSeriesDefinition;

/**
 * The helper class to set the non-public properties of an {@link MetricSeriesDefinition} instance.
 */
public final class MetricSeriesDefinitionHelper {
    private static MetricSeriesDefinitionAccessor accessor;

    private MetricSeriesDefinitionHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MetricSeriesDefinition} instance.
     */
    public interface MetricSeriesDefinitionAccessor {
        void setMetricId(MetricSeriesDefinition seriesDefinition, String metricId);
        void setSeriesKey(MetricSeriesDefinition seriesDefinition, DimensionKey seriesKey);
    }

    /**
     * The method called from {@link MetricSeriesDefinition} to set it's accessor.
     *
     * @param seriesDefinitionAccessor The accessor.
     */
    public static void setAccessor(final MetricSeriesDefinitionAccessor seriesDefinitionAccessor) {
        accessor = seriesDefinitionAccessor;
    }

    static void setMetricId(MetricSeriesDefinition seriesDefinition, String metricId) {
        accessor.setMetricId(seriesDefinition, metricId);
    }

    static void setSeriesKey(MetricSeriesDefinition seriesDefinition, DimensionKey seriesKey) {
        accessor.setSeriesKey(seriesDefinition, seriesKey);
    }
}
