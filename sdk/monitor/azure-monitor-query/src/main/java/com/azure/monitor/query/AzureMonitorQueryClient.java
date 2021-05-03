// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.models.LogsQueryBatch;
import com.azure.monitor.query.models.LogsQueryBatchResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeSpan;
import com.azure.monitor.query.rest.AzureMonitorQueryRestClient;

import java.time.OffsetDateTime;
import java.util.List;

/**
 *
 */
@ServiceClient(builder = AzureMonitorQueryClientBuilder.class)
public final class AzureMonitorQueryClient {
    private final AzureMonitorQueryAsyncClient asyncClient;

    AzureMonitorQueryClient(AzureMonitorQueryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /*
    LOGS
     */


    /**
     * @return
     */
    public AzureMonitorQueryRestClient getRestClient() {
        return new AzureMonitorQueryRestClient();
    }

    /**
     * @param workspaceId
     * @param query
     * @param timeSpan
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsQueryResult queryLogs(String workspaceId, String query, QueryTimeSpan timeSpan) {
        return asyncClient.queryLogs(workspaceId, query, timeSpan).block();
    }

    /**
     * @param options
     * @param context
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsQueryResult> queryLogsWithResponse(LogsQueryOptions options, Context context) {
        return asyncClient.queryLogsWithResponse(options, context).block();
    }

    /**
     * @param workspaceId
     * @param queries
     * @param timeSpan
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LogsQueryBatchResultCollection queryLogsBatch(String workspaceId, List<String> queries, QueryTimeSpan timeSpan) {
        return asyncClient.queryLogsBatch(workspaceId, queries, timeSpan).block();
    }

    /**
     * @param logsQueryBatch
     * @param context
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LogsQueryBatchResultCollection> queryLogsBatchWithResponse(LogsQueryBatch logsQueryBatch, Context context) {
        return asyncClient.queryLogsBatchWithResponse(logsQueryBatch, context).block();
    }

    /**
     * @param resourceUri
     * @param metricsNames
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricsQueryResult queryMetrics(String resourceUri, List<String> metricsNames) {
        return queryMetricsWithResponse(resourceUri, metricsNames, null, Context.NONE).getValue();
    }

    /**
     * @param resourceUri
     * @param metricsNames
     * @param options
     * @param context
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricsQueryResult> queryMetricsWithResponse(String resourceUri, List<String> metricsNames,
                                                                 MetricsQueryOptions options, Context context) {
        return asyncClient.queryMetricsWithResponse(resourceUri, metricsNames, options, context).block();
    }


    /**
     * @param resourceUri
     * @param startTime
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime) {
        return listMetricsNamespace(resourceUri, startTime, Context.NONE);
    }

    /**
     * @param resourceUri
     * @param startTime
     * @param context
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime,
                                                               Context context) {
        return new PagedIterable<>(asyncClient.listMetricsNamespace(resourceUri, startTime, context));
    }

    /**
     * @param resourceUri
     * @param metricsNamespace
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<MetricDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace) {
        return listMetricsDefinition(resourceUri, metricsNamespace, Context.NONE);
    }

    /**
     * @param resourceUri
     * @param metricsNamespace
     * @param context
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<MetricDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace,
                                                           Context context) {
        return new PagedIterable<>(asyncClient.listMetricsDefinition(resourceUri, metricsNamespace, context));
    }
}
