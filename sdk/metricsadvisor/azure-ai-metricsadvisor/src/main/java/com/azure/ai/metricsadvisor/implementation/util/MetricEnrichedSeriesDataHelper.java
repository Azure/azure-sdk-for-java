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
        void setTimestampList(MetricEnrichedSeriesData seriesData, List<OffsetDateTime> timestampList);
        void setValueList(MetricEnrichedSeriesData seriesData, List<Double> valueList);
        void setIsAnomalyList(MetricEnrichedSeriesData seriesData, List<Boolean> isAnomalyList);
        void setPeriodList(MetricEnrichedSeriesData seriesData, List<Integer> periodList);
        void setExpectedValueList(MetricEnrichedSeriesData seriesData, List<Double> expectedValueList);
        void setLowerBoundaryList(MetricEnrichedSeriesData seriesData, List<Double> lowerBoundaryList);
        void setUpperBoundaryList(MetricEnrichedSeriesData seriesData, List<Double> upperBoundaryList);
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

    static void setTimestampList(MetricEnrichedSeriesData seriesData, List<OffsetDateTime> timestampList) {
        accessor.setTimestampList(seriesData, timestampList);
    }

    static void setValueList(MetricEnrichedSeriesData seriesData, List<Double> valueList) {
        accessor.setValueList(seriesData, valueList);
    }

    static void setIsAnomalyList(MetricEnrichedSeriesData seriesData, List<Boolean> isAnomalyList) {
        accessor.setIsAnomalyList(seriesData, isAnomalyList);
    }

    static void setPeriodList(MetricEnrichedSeriesData seriesData, List<Integer> periodList) {
        accessor.setPeriodList(seriesData, periodList);
    }

    static void setExpectedValueList(MetricEnrichedSeriesData seriesData, List<Double> expectedValueList) {
        accessor.setExpectedValueList(seriesData, expectedValueList);
    }

    static void setLowerBoundaryList(MetricEnrichedSeriesData seriesData, List<Double> lowerBoundaryList) {
        accessor.setLowerBoundaryList(seriesData, lowerBoundaryList);
    }

    static void setUpperBoundaryList(MetricEnrichedSeriesData seriesData, List<Double> upperBoundaryList) {
        accessor.setUpperBoundaryList(seriesData, upperBoundaryList);
    }
}
