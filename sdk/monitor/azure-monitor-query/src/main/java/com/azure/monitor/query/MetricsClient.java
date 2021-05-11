// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.query.models.MetricsDefinition;
import com.azure.monitor.query.models.MetricsNamespace;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;

import java.time.OffsetDateTime;
import java.util.List;

/**
 *
 */
@ServiceClient(builder = MetricsClientBuilder.class)
public final class MetricsClient {
    private final MetricsAsyncClient asyncClient;

    MetricsClient(MetricsAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }



    /**
     * @param resourceUri
     * @param metricsNames
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricsQueryResult queryMetrics(String resourceUri, List<String> metricsNames) {
        return queryMetricsWithResponse(resourceUri, metricsNames, new MetricsQueryOptions(), Context.NONE).getValue();
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
    public PagedIterable<MetricsNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime) {
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
    public PagedIterable<MetricsNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime,
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
    public PagedIterable<MetricsDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace) {
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
    public PagedIterable<MetricsDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace,
                                                                  Context context) {
        return new PagedIterable<>(asyncClient.listMetricsDefinition(resourceUri, metricsNamespace, context));
    }
}
