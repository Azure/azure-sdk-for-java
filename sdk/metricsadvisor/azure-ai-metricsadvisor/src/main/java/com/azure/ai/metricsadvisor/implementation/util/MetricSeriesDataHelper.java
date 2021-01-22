// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricSeriesData;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link MetricSeriesData} instance.
 */
public final class MetricSeriesDataHelper {
    private static MetricSeriesDataAccessor accessor;

    private MetricSeriesDataHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MetricSeriesData} instance.
     */
    public interface MetricSeriesDataAccessor {
        void setMetricId(MetricSeriesData seriesData, String metricId);
        void setSeriesKey(MetricSeriesData seriesData, DimensionKey seriesKey);
        void setTimestampList(MetricSeriesData seriesData, List<OffsetDateTime> timestampList);
        void setValueList(MetricSeriesData seriesData, List<Double> valueList);
    }

    /**
     * The method called from {@link MetricSeriesData} to set it's accessor.
     *
     * @param seriesDataAccessor The accessor.
     */
    public static void setAccessor(final MetricSeriesDataAccessor seriesDataAccessor) {
        accessor = seriesDataAccessor;
    }

    static void setMetricId(MetricSeriesData seriesData, String metricId) {
        accessor.setMetricId(seriesData, metricId);
    }

    static void setSeriesKey(MetricSeriesData seriesData, DimensionKey seriesKey) {
        accessor.setSeriesKey(seriesData, seriesKey);
    }

    static void setTimestampList(MetricSeriesData seriesData, List<OffsetDateTime> timestampList) {
        accessor.setTimestampList(seriesData, timestampList);
    }

    static void setValueList(MetricSeriesData seriesData, List<Double> valueList) {
        accessor.setValueList(seriesData, valueList);
    }
}
