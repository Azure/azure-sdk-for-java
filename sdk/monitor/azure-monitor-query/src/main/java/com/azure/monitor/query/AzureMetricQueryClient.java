// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.monitor.query.metric.implementation.models.MetricsResponse;
import com.azure.monitor.query.metric.implementation.models.ResultType;

import java.time.Duration;

class AzureMetricQueryClient {
    private final AzureMetricQueryAsyncClient asyncClient;

    public AzureMetricQueryClient(AzureMetricQueryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }


    public MetricsResponse queryMetrics(String resourceUri, String timespan, Duration interval, String metricnames,
                                        String aggregation, Integer top, String orderby, String filter,
                                        ResultType resultType, String metricnamespace) {
        return asyncClient.queryMetrics(resourceUri, timespan, interval, metricnames, aggregation, top, orderby,
            filter, resultType, metricnamespace).block();
    }


}
