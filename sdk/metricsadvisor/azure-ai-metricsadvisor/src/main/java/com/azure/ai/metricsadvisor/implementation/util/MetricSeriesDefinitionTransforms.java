// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.implementation.util;

import com.azure.ai.metricsadvisor.implementation.models.MetricSeriesItem;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.MetricSeriesDefinition;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MetricSeriesDefinitionTransforms {
    public static PagedResponse<MetricSeriesDefinition> fromInnerResponse(
        PagedResponse<MetricSeriesItem> innerResponse) {
        final List<MetricSeriesItem> innerMetricSeriesDefinitionList = innerResponse.getValue();
        if (innerMetricSeriesDefinitionList == null) {
            return new PagedResponseBase<>(
                innerResponse.getRequest(),
                innerResponse.getStatusCode(),
                innerResponse.getHeaders(),
                new ArrayList<>(),
                innerResponse.getContinuationToken(),
                null);
        } else {
            final List<MetricSeriesDefinition> metricSeriesDefinitionList = innerMetricSeriesDefinitionList.stream()
                .map(inner -> MetricSeriesDefinitionTransforms.fromInner(inner))
                .collect(Collectors.toList());
            return new PagedResponseBase<>(
                innerResponse.getRequest(),
                innerResponse.getStatusCode(),
                innerResponse.getHeaders(),
                metricSeriesDefinitionList,
                innerResponse.getContinuationToken(),
                null);
        }
    }

    public static MetricSeriesDefinition fromInner(MetricSeriesItem inner) {
        MetricSeriesDefinition seriesDefinition = new MetricSeriesDefinition();
        MetricSeriesDefinitionHelper.setMetricId(seriesDefinition, inner.getMetricId().toString());
        MetricSeriesDefinitionHelper.setSeriesKey(seriesDefinition, new DimensionKey(inner.getDimension()));
        return seriesDefinition;
    }
}
