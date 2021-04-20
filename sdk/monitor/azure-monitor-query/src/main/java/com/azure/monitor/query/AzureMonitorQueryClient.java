// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.monitor.query.log.implementation.models.BatchResponse;
import com.azure.monitor.query.log.implementation.models.QueryResults;
import com.azure.monitor.query.metric.implementation.models.MetricsResponse;
import com.azure.monitor.query.metric.implementation.models.ResultType;

import java.time.Duration;
import java.util.List;

public class AzureMonitorQueryClient {
    private final AzureMonitorQueryAsyncClient asyncClient;

    public AzureMonitorQueryClient(AzureMonitorQueryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public QueryResults queryLogs(String workspaceId, String query){
        return asyncClient.queryLogs(workspaceId, query).block();
    }

    public Response<QueryResults> queryLogs(String workspaceId, String query, QueryLogsRequestOptions options) {
        return null;
    }

    public Response<QueryResults> queryLogsWithResponse(String workspaceId, String query,
                                                        QueryLogsRequestOptions options){
        return asyncClient.queryLogsWithResponse(workspaceId, query, options).block();
    }

    public BatchResponse queryLogsBatch(String workspaceId, List<String> queries) {
        return asyncClient.queryLogsBatch(workspaceId, queries);
    }

    public MetricsResponse queryMetrics(String resourceUri, String timespan, Duration interval, String metricnames,
                                        String aggregation, Integer top, String orderby, String filter,
                                        ResultType resultType, String metricnamespace) {
        return asyncClient.queryMetrics(resourceUri, timespan, interval, metricnames, aggregation, top, orderby,
            filter, resultType, metricnamespace).block();
    }

}
