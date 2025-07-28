// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics;

import java.time.Duration;
import java.util.List;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.query.metrics.implementation.MonitorQueryMetricsClient;

public class MetricsQueryClient {
    private static final ClientLogger LOGGER = new ClientLogger(MetricsQueryClient.class);

    private final MonitorQueryMetricsClient serviceClient;

    MetricsQueryClient(MonitorQueryMetricsClient serviceClient) {
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

        String filter = null;
        Duration granularity = null;
        String aggregations = null;
        String startTime = null;
        Integer top = null;
        String orderBy = null;
        String endTime = null;
        String rollupBy = null;
        if (options != null) {
            rollupBy = options.getRollupBy();
            filter = options.getFilter();
            granularity = options.getGranularity();

            if (options.getAggregations() != null) {
                aggregations = options.getAggregations()
                    .stream()
                    .map(AggregationType::toString)
                    .collect(Collectors.joining(","));
            }

            if (options.getTimeInterval() != null) {
                if (options.getTimeInterval().getDuration() != null) {
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                        "Duration is not a supported time interval for batch query. Use startTime and endTime instead."));
                }
                if (options.getTimeInterval().getStartTime() != null) {
                    startTime = options.getTimeInterval().getStartTime().toString();
                }
                if (options.getTimeInterval().getEndTime() != null) {
                    endTime = options.getTimeInterval().getEndTime().toString();
                }
            }

            top = options.getTop();
            orderBy = options.getOrderBy();
        }
        String subscriptionId = getSubscriptionFromResourceId(resourceIds.get(0));
        ResourceIdList resourceIdList = new ResourceIdList();
        resourceIdList.setResourceids(resourceIds);
        Response<MetricResultsResponse> response = this.serviceClient.getMetricsBatches()
            .batchWithResponse(subscriptionId, metricsNamespace, metricsNames, resourceIdList, startTime, endTime,
                granularity, aggregations, top, orderBy, filter, rollupBy, context);
        MetricResultsResponse value = response.getValue();
        List<MetricResultsResponseValuesItem> values = value.getValues();
        List<MetricsQueryResult> metricsQueryResults
            = values.stream().map(result -> mapToMetricsQueryResult(result)).collect(Collectors.toList());
        MetricsQueryResourcesResult metricsQueryResourcesResult = new MetricsQueryResourcesResult(metricsQueryResults);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            metricsQueryResourcesResult);

    }

}
