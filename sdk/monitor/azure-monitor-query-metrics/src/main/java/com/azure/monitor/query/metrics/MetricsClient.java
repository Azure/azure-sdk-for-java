// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsClient;
import static com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsUtils.getSubscriptionFromResourceId;
import static com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsUtils.mapToMetricsQueryResult;
import static com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsUtils.mapToRequestOptions;
import com.azure.monitor.query.metrics.implementation.models.MetricResultsResponse;
import com.azure.monitor.query.metrics.implementation.models.MetricResultsResponseValuesItem;
import com.azure.monitor.query.metrics.implementation.models.ResourceIdList;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesResult;
import com.azure.monitor.query.metrics.models.MetricsQueryResult;

/**
 * This class provides synchronous client that contains all the query operations that use batch requests to retrieve
 * metrics for multiple resources.
 */
public final class MetricsClient {
    private static final ClientLogger LOGGER = new ClientLogger(MetricsClient.class);

    private final MonitorQueryMetricsClient serviceClient;

    MetricsClient(MonitorQueryMetricsClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Returns all the Azure Monitor metrics requested for the batch of resources.
     *
     * @param resourceIds The resource ids for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param metricsNamespace The namespace of the metrics to query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricsQueryResourcesResult queryResources(List<String> resourceIds, List<String> metricsNames,
        String metricsNamespace) {
        return this
            .queryResourcesWithResponse(resourceIds, metricsNames, metricsNamespace, new MetricsQueryResourcesOptions(),
                Context.NONE)
            .getValue();
    }

    /**
     * Returns all the Azure Monitor metrics requested for the batch of resources.
     *
     * @param resourceIds The resource ids for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param metricsNamespace The namespace of the metrics to query.
     * @param options The {@link MetricsQueryResourcesOptions} to include for the request.
     * @param context The context to associate with this operation.
     * @return A time-series metrics result for the requested metric names.
     * @throws IllegalArgumentException thrown if {@code resourceIds}, {@code metricsNames} or {@code metricsNamespace} are empty.
     * @throws NullPointerException thrown if {@code resourceIds}, {@code metricsNames} or {@code metricsNamespace} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricsQueryResourcesResult> queryResourcesWithResponse(List<String> resourceIds,
        List<String> metricsNames, String metricsNamespace, MetricsQueryResourcesOptions options, Context context) {
        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(resourceIds, "'resourceIds cannot be null."))) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("resourceIds cannot be empty"));
        }

        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(metricsNames, "metricsNames cannot be null"))) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("metricsNames cannot be empty"));
        }

        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(metricsNamespace, "metricsNamespace cannot be null"))) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("metricsNamespace cannot be empty"));
        }

        String subscriptionId = getSubscriptionFromResourceId(resourceIds.get(0));
        ResourceIdList resourceIdList = new ResourceIdList();
        resourceIdList.setResourceids(resourceIds);
        Response<BinaryData> response = this.serviceClient.queryResourcesWithResponse(subscriptionId, metricsNamespace,
            metricsNames, BinaryData.fromObject(resourceIdList), mapToRequestOptions(options));
        MetricResultsResponse valueResponse = response.getValue().toObject(MetricResultsResponse.class);
        List<MetricResultsResponseValuesItem> values = valueResponse.getValues();
        List<MetricsQueryResult> metricsQueryResults
            = values.stream().map(result -> mapToMetricsQueryResult(result)).collect(Collectors.toList());
        MetricsQueryResourcesResult metricsQueryResourcesResult = new MetricsQueryResourcesResult(metricsQueryResults);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            metricsQueryResourcesResult);

    }

}
