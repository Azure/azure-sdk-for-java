// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.monitor.query.metric.implementation.MonitorManagementClientImpl;
import com.azure.monitor.query.metric.implementation.models.MetricsResponse;
import com.azure.monitor.query.metric.implementation.models.ResultType;
import reactor.core.publisher.Mono;

import java.time.Duration;

class AzureMetricQueryAsyncClient {
    private final MonitorManagementClientImpl innerClient;

    public AzureMetricQueryAsyncClient(MonitorManagementClientImpl innerClient) {
        this.innerClient = innerClient;
    }

    public Mono<MetricsResponse> queryMetrics(String resourceUri, String timespan, Duration interval,
                                              String metricnames, String aggregation, Integer top, String orderby,
                                              String filter, ResultType resultType, String metricnamespace) {
        return innerClient.getMetrics().listAsync(resourceUri, timespan, interval, metricnames, aggregation, top,
            orderby, filter, resultType, metricnamespace);
    }
}
