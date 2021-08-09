// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The synchronous client for querying Azure Monitor metrics.
 */
@ServiceClient(builder = MetricsQueryClientBuilder.class)
public final class MetricsQueryClient {
    private final MetricsQueryAsyncClient asyncClient;

    MetricsQueryClient(MetricsQueryAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }


    /**
     * Returns all the Azure Monitor metrics requested for the resource.
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricsQueryResult queryMetrics(String resourceUri, List<String> metricsNames) {
        return queryMetricsWithResponse(resourceUri, metricsNames, new MetricsQueryOptions(), Context.NONE).getValue();
    }

    /**
     * Returns all the Azure Monitor metrics requested for the resource.
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param options Options to filter the query.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricsQueryResult> queryMetricsWithResponse(String resourceUri, List<String> metricsNames,
                                                                 MetricsQueryOptions options, Context context) {
        return asyncClient.queryMetricsWithResponse(resourceUri, metricsNames, options, context).block();
    }


    /**
     * Lists all the metrics namespaces created for the resource URI.
     * @param resourceUri The resource URI for which the metrics namespaces are listed.
     * @param startTime The returned list of metrics namespaces are created after the specified start time.
     * @return List of metrics namespaces.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime) {
        return listMetricsNamespace(resourceUri, startTime, Context.NONE);
    }

    /**
     * Lists all the metrics namespaces created for the resource URI.
     * @param resourceUri The resource URI for which the metrics namespaces are listed.
     * @param startTime The returned list of metrics namespaces are created after the specified start time.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return List of metrics namespaces.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime,
                                                               Context context) {
        return new PagedIterable<>(asyncClient.listMetricsNamespace(resourceUri, startTime, context));
    }

    /**
     * Lists all the metrics definitions created for the resource URI.
     * @param resourceUri The resource URI for which the metrics definitions are listed.
     * @param metricsNamespace The metrics namespace to which the listed metrics definitions belong.
     * @return List of metrics definitions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<MetricDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace) {
        return listMetricsDefinition(resourceUri, metricsNamespace, Context.NONE);
    }

    /**
     * Lists all the metrics definitions created for the resource URI.
     * @param resourceUri The resource URI for which the metrics definitions are listed.
     * @param metricsNamespace The metrics namespace to which the listed metrics definitions belong.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return List of metrics definitions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<MetricDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace,
                                                                 Context context) {
        return new PagedIterable<>(asyncClient.listMetricsDefinition(resourceUri, metricsNamespace, context));
    }
}
