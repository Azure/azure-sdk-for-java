// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.SeriesResult;
import com.azure.ai.metricsadvisor.implementation.models.SeriesResultList;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class MetricEnrichedSeriesDataTransformations {
    public static List<MetricEnrichedSeriesData> fromInnerList(SeriesResultList innerEnrichedSeriesDataList) {
        List<MetricEnrichedSeriesData> enrichedSeriesDataList = new ArrayList<>();
        if (innerEnrichedSeriesDataList != null && !innerEnrichedSeriesDataList.getValue().isEmpty()) {
            for (SeriesResult innerEnrichedSeriesData : innerEnrichedSeriesDataList.getValue()) {
                enrichedSeriesDataList.add(fromInner(innerEnrichedSeriesData));
            }
        }
        return enrichedSeriesDataList;
    }

    private static MetricEnrichedSeriesData fromInner(SeriesResult innerEnrichedSeriesData) {
        final MetricEnrichedSeriesData enrichedSeriesData = new MetricEnrichedSeriesData();

        DimensionKey seriesKey;
        if (innerEnrichedSeriesData.getSeries().getDimension() != null) {
            seriesKey = new DimensionKey(innerEnrichedSeriesData.getSeries().getDimension());
        } else {
            seriesKey = new DimensionKey();
        }
        MetricEnrichedSeriesDataHelper.setSeriesKey(enrichedSeriesData, seriesKey);

        List<OffsetDateTime> timestampList = innerEnrichedSeriesData.getTimestampList();
        if (timestampList == null) {
            timestampList = new ArrayList<>();
        }
        MetricEnrichedSeriesDataHelper.setTimestamps(enrichedSeriesData, timestampList);

        List<Double> valueList = innerEnrichedSeriesData.getValueList();
        if (valueList == null) {
            valueList = new ArrayList<>();
        }
        MetricEnrichedSeriesDataHelper.setMetricValues(enrichedSeriesData, valueList);

        List<Boolean> isAnomalyList = innerEnrichedSeriesData.getIsAnomalyList();
        if (isAnomalyList == null) {
            isAnomalyList = new ArrayList<>();
        }
        MetricEnrichedSeriesDataHelper.setIsAnomalyList(enrichedSeriesData, isAnomalyList);

        List<Integer> periodList = innerEnrichedSeriesData.getPeriodList();
        if (periodList == null) {
            periodList = new ArrayList<>();
        }
        MetricEnrichedSeriesDataHelper.setPeriods(enrichedSeriesData, periodList);

        List<Double> expectedValueList = innerEnrichedSeriesData.getExpectedValueList();
        if (expectedValueList == null) {
            expectedValueList = new ArrayList<>();
        }
        MetricEnrichedSeriesDataHelper.setExpectedMetricValues(enrichedSeriesData, expectedValueList);

        List<Double> lowerBoundList = innerEnrichedSeriesData.getLowerBoundaryList();
        if (lowerBoundList == null) {
            lowerBoundList = new ArrayList<>();
        }
        MetricEnrichedSeriesDataHelper.setLowerBoundaryValues(enrichedSeriesData, lowerBoundList);

        List<Double> upperBoundList = innerEnrichedSeriesData.getUpperBoundaryList();
        if (upperBoundList == null) {
            upperBoundList = new ArrayList<>();
        }
        MetricEnrichedSeriesDataHelper.setUpperBoundaryValues(enrichedSeriesData, upperBoundList);

        return enrichedSeriesData;
    }
}
