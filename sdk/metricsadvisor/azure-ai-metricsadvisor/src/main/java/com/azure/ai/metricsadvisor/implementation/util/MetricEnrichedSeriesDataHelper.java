// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link MetricEnrichedSeriesData} instance.
 */
public final class MetricEnrichedSeriesDataHelper {
    private static MetricEnrichedSeriesDataAccessor accessor;

    private MetricEnrichedSeriesDataHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MetricEnrichedSeriesData} instance.
     */
    public interface MetricEnrichedSeriesDataAccessor {
        void setSeriesKey(MetricEnrichedSeriesData seriesData, DimensionKey seriesKey);
        void setTimestamps(MetricEnrichedSeriesData seriesData, List<OffsetDateTime> timestamps);
        void setMetricValues(MetricEnrichedSeriesData seriesData, List<Double> metricValues);
        void setIsAnomalyList(MetricEnrichedSeriesData seriesData, List<Boolean> isAnomaly);
        void setPeriods(MetricEnrichedSeriesData seriesData, List<Integer> periods);
        void setExpectedMetricValues(MetricEnrichedSeriesData seriesData, List<Double> expectedMetricValues);
        void setLowerBoundaryValues(MetricEnrichedSeriesData seriesData, List<Double> lowerBoundaryValues);
        void setUpperBoundaryValues(MetricEnrichedSeriesData seriesData, List<Double> upperBoundaryValues);
    }

    /**
     * The method called from {@link MetricEnrichedSeriesData} to set it's accessor.
     *
     * @param seriesDataAccessor The accessor.
     */
    public static void setAccessor(final MetricEnrichedSeriesDataAccessor seriesDataAccessor) {
        accessor = seriesDataAccessor;
    }

    static void setSeriesKey(MetricEnrichedSeriesData seriesData, DimensionKey seriesKey) {
        accessor.setSeriesKey(seriesData, seriesKey);
    }

    static void setTimestamps(MetricEnrichedSeriesData seriesData, List<OffsetDateTime> timestamps) {
        accessor.setTimestamps(seriesData, timestamps);
    }

    static void setMetricValues(MetricEnrichedSeriesData seriesData, List<Double> metricValues) {
        accessor.setMetricValues(seriesData, metricValues);
    }

    static void setIsAnomalyList(MetricEnrichedSeriesData seriesData, List<Boolean> isAnomaly) {
        accessor.setIsAnomalyList(seriesData, isAnomaly);
    }

    static void setPeriods(MetricEnrichedSeriesData seriesData, List<Integer> periods) {
        accessor.setPeriods(seriesData, periods);
    }

    static void setExpectedMetricValues(MetricEnrichedSeriesData seriesData, List<Double> expectedMetricValues) {
        accessor.setExpectedMetricValues(seriesData, expectedMetricValues);
    }

    static void setLowerBoundaryValues(MetricEnrichedSeriesData seriesData, List<Double> lowerBoundaryValues) {
        accessor.setLowerBoundaryValues(seriesData, lowerBoundaryValues);
    }

    static void setUpperBoundaryValues(MetricEnrichedSeriesData seriesData, List<Double> upperBoundaryValues) {
        accessor.setUpperBoundaryValues(seriesData, upperBoundaryValues);
    }
}
