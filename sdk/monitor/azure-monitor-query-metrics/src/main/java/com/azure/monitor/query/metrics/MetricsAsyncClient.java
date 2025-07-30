// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import static com.azure.core.util.FluxUtil.monoError;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsUtils;
import static com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsUtils.getSubscriptionFromResourceId;
import static com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsUtils.mapToMetricsQueryResult;
import com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsAsyncClient;
import com.azure.monitor.query.metrics.implementation.models.MetricResultsResponse;
import com.azure.monitor.query.metrics.implementation.models.MetricResultsResponseValuesItem;
import com.azure.monitor.query.metrics.implementation.models.ResourceIdList;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesOptions;
import com.azure.monitor.query.metrics.models.MetricsQueryResourcesResult;
import com.azure.monitor.query.metrics.models.MetricsQueryResult;

import reactor.core.publisher.Mono;

/**
 * This class provides an asynchronous client that contains all the query operations that use batch requests to retrieve
 * metrics for multiple resources.
 */
public final class MetricsAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(MetricsAsyncClient.class);

    private final MonitorQueryMetricsAsyncClient serviceClient;

    MetricsAsyncClient(MonitorQueryMetricsAsyncClient serviceClient) {
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
    public Mono<MetricsQueryResourcesResult> queryResources(List<String> resourceIds, List<String> metricsNames,
        String metricsNamespace) {
        return this
            .queryResourcesWithResponse(resourceIds, metricsNames, metricsNamespace, new MetricsQueryResourcesOptions())
            .map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor metrics requested for the batch of resources.
     *
     * @param resourceIds The resource ids for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param metricsNamespace The namespace of the metrics to query.
     * @param options The {@link MetricsQueryResourcesOptions} to include for the request.
     * @return A time-series metrics result for the requested metric names.
     * @throws IllegalArgumentException thrown if {@code resourceIds}, {@code metricsNames} or {@code metricsNamespace} are empty.
     * @throws NullPointerException thrown if {@code resourceIds}, {@code metricsNames} or {@code metricsNamespace} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricsQueryResourcesResult>> queryResourcesWithResponse(List<String> resourceIds,
        List<String> metricsNames, String metricsNamespace, MetricsQueryResourcesOptions options) {

        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(resourceIds, "'resourceIds cannot be null."))) {
            return monoError(LOGGER, new IllegalArgumentException("resourceIds cannot be empty"));
        }

        ResourceIdList resourceIdList = new ResourceIdList();
        resourceIdList.setResourceids(resourceIds);

        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(metricsNames, "metricsNames cannot be null"))) {
            return monoError(LOGGER, new IllegalArgumentException("metricsNames cannot be empty"));
        }

        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(metricsNamespace, "metricsNamespace cannot be null"))) {
            return monoError(LOGGER, new IllegalArgumentException("metricsNamespace cannot be empty"));
        }

        RequestOptions requestOptions = MonitorQueryMetricsUtils.mapToRequestOptions(options);

        String subscriptionId;
        subscriptionId = getSubscriptionFromResourceId(resourceIds.get(0));

        Mono<Response<BinaryData>> responseMono;
        responseMono = this.serviceClient.queryResourcesWithResponse(subscriptionId, metricsNamespace, metricsNames,
            BinaryData.fromObject(resourceIdList), requestOptions);

        return responseMono.map(new Function<Response<BinaryData>, Response<MetricsQueryResourcesResult>>() {
            @Override
            public Response<MetricsQueryResourcesResult> apply(Response<BinaryData> response) {
                MetricResultsResponse valueResponse = response.getValue().toObject(MetricResultsResponse.class);
                List<MetricResultsResponseValuesItem> values = valueResponse.getValues();
                List<MetricsQueryResult> metricsQueryResults
                    = values.stream().map(result -> mapToMetricsQueryResult(result)).collect(Collectors.toList());
                MetricsQueryResourcesResult metricsQueryResourcesResult
                    = new MetricsQueryResourcesResult(metricsQueryResults);
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    metricsQueryResourcesResult);
            }
        });
    }
}
