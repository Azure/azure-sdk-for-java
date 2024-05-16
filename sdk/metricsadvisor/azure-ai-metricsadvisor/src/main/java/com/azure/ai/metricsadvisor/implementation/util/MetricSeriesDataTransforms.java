// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.MetricDataList;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricSeriesData;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetricSeriesDataTransforms {
    public static PagedResponse<MetricSeriesData> fromInnerResponse(Response<MetricDataList> innerResponse) {
        final MetricDataList innerMetricDataList = innerResponse.getValue();
        if (innerMetricDataList == null || innerMetricDataList.getValue() == null) {
            return new PagedResponseBase<>(
                innerResponse.getRequest(),
                innerResponse.getStatusCode(),
                innerResponse.getHeaders(),
                new ArrayList<>(),
                null,
                null);
        } else {
            final List<MetricSeriesData> metricSeriesDataList = innerMetricDataList.getValue()
                .stream()
                .map(inner -> MetricSeriesDataTransforms.fromInner(inner))
                .collect(Collectors.toList());
            return new PagedResponseBase<>(
                innerResponse.getRequest(),
                innerResponse.getStatusCode(),
                innerResponse.getHeaders(),
                metricSeriesDataList,
                null,
                null);
        }
    }

    public static MetricSeriesData fromInner(com.azure.ai.metricsadvisor.implementation.models.MetricSeriesData inner) {
        MetricSeriesData metricSeriesData = new MetricSeriesData();
        MetricSeriesDataHelper.setMetricId(metricSeriesData, inner.getId().getMetricId().toString());
        MetricSeriesDataHelper.setSeriesKey(metricSeriesData, new DimensionKey(inner.getId().getDimension()));
        MetricSeriesDataHelper.setTimestampList(metricSeriesData, inner.getTimestampList());
        MetricSeriesDataHelper.setValueList(metricSeriesData, inner.getValueList());
        return metricSeriesData;
    }
}
