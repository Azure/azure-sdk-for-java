// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.log.implementation.AzureLogAnalyticsImpl;
import com.azure.monitor.query.log.implementation.models.BatchRequest;
import com.azure.monitor.query.log.implementation.models.BatchResponse;
import com.azure.monitor.query.log.implementation.models.LogQueryRequest;
import com.azure.monitor.query.log.implementation.models.QueryBody;
import com.azure.monitor.query.log.implementation.models.QueryResults;
import com.azure.monitor.query.metric.implementation.MonitorManagementClientImpl;
import com.azure.monitor.query.metric.implementation.models.MetricsResponse;
import com.azure.monitor.query.metric.implementation.models.ResultType;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AzureMonitorQueryAsyncClient {

    private final MonitorManagementClientImpl metricsClient;
    private final AzureLogAnalyticsImpl logClient;

    public AzureMonitorQueryAsyncClient(AzureLogAnalyticsImpl loglogClient, MonitorManagementClientImpl metricslogClient) {
        this.logClient = loglogClient;
        this.metricsClient = metricslogClient;
    }

    public Mono<QueryResults> queryLogs(String workspaceId, String query) {
        return logClient.getQueries().executeAsync(workspaceId, new QueryBody(query), null);
    }

    public Mono<Response<QueryResults>> queryLogsWithResponse(String workspaceId, String query,
                                                              QueryLogsRequestOptions options) {

        StringBuilder filter = new StringBuilder();
        if (options.isIncludeRendering()) {
            filter.append("include-render=true");
        }

        if (options.isIncludeStatistics()) {
            if (filter.length() > 0) {
                filter.append(";");
            }
            filter.append("include-statistics=true");
        }

        if (options.getServerTimeout() != null) {
            if (filter.length() > 0) {
                filter.append(";");
            }
            filter.append("wait=");
            filter.append(options.getServerTimeout().getSeconds());
        }
        return logClient.getQueries().executeWithResponseAsync(workspaceId, new QueryBody(query), filter.toString(),
            Context.NONE);
    }

    public BatchResponse queryLogsBatch(String workspaceId, List<String> queries) {
        BatchRequest batchRequest = new BatchRequest();
        AtomicInteger id = new AtomicInteger();
        List<LogQueryRequest> requests = queries.stream()
            .map(query -> new LogQueryRequest().setId(String.valueOf(id.incrementAndGet())).setBody(new QueryBody(query).setWorkspaces(Arrays.asList(workspaceId))).setWorkspace(workspaceId).setPath("/query").setMethod("POST"))
            .collect(Collectors.toList());
        batchRequest.setRequests(requests);
        return logClient.getQueries().batch(batchRequest);
    }

    public Mono<MetricsResponse> queryMetrics(String resourceUri, String timespan, Duration interval,
                                              String metricnames, String aggregation, Integer top, String orderby,
                                              String filter, ResultType resultType, String metricnamespace) {
        return metricsClient.getMetrics().listAsync(resourceUri, timespan, interval, metricnames, aggregation, top,
            orderby, filter, resultType, metricnamespace);
    }
}
