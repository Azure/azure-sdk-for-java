// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.query.implementation.metricsbatch.AzureMonitorMetricBatch;
import com.azure.monitor.query.implementation.metricsbatch.models.BatchMetricResultsResponse;
import com.azure.monitor.query.implementation.metricsbatch.models.BatchMetricResultsResponseValuesItem;
import com.azure.monitor.query.implementation.metricsbatch.models.MetricBatchResourceIdList;
import com.azure.monitor.query.models.AggregationType;
import com.azure.monitor.query.models.MetricsBatchResult;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.monitor.query.implementation.metrics.models.MetricsHelper.getSubscriptionFromResourceId;
import static com.azure.monitor.query.implementation.metrics.models.MetricsHelper.mapToMetricsQueryResult;

/**
 * This class provides an asynchronous client that contains all the query operations that use batch requests to retrieve
 * metrics for multiple resources.
 */
@ServiceClient(builder = MetricsBatchQueryClientBuilder.class)
public final class MetricsBatchQueryClient {
    private static final ClientLogger LOGGER = new ClientLogger(MetricsBatchQueryClient.class);

    private final AzureMonitorMetricBatch serviceClient;

    MetricsBatchQueryClient(AzureMonitorMetricBatch azureMonitorMetricBatch) {
        this.serviceClient = azureMonitorMetricBatch;
    }

    /**
     * Returns all the Azure Monitor metrics requested for the batch of resources.
     *
     * @param resourceUris The resource URIs for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param metricsNamespace The namespace of the metrics to query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricsBatchResult queryBatch(List<String> resourceUris, List<String> metricsNames, String metricsNamespace) {
        return this.queryBatchWithResponse(resourceUris, metricsNames, metricsNamespace, new MetricsQueryOptions(),
            Context.NONE).getValue();
    }

    /**
     * Returns all the Azure Monitor metrics requested for the batch of resources.
     *
     * @param resourceUris The resource URIs for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param metricsNamespace The namespace of the metrics to query.
     * @param options The {@link MetricsQueryOptions} to include for the request.
     * @param context The context to associate with this operation.
     * @return A time-series metrics result for the requested metric names.
     * @throws IllegalArgumentException thrown if {@code resourceUris}, {@code metricsNames} or {@code metricsNamespace} are empty.
     * @throws NullPointerException thrown if {@code resourceUris}, {@code metricsNames} or {@code metricsNamespace} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricsBatchResult> queryBatchWithResponse(List<String> resourceUris, List<String> metricsNames,
                                                               String metricsNamespace, MetricsQueryOptions options, Context context) {
        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(resourceUris, "'resourceUris cannot be null."))) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("resourceUris cannot be empty"));
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
        if (options != null) {
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
                    throw LOGGER.logExceptionAsError(new IllegalArgumentException("Duration is not a supported time interval for batch query. Use startTime and endTime instead."));
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
        String subscriptionId = getSubscriptionFromResourceId(resourceUris.get(0));
        MetricBatchResourceIdList resourceIdList = new MetricBatchResourceIdList();
        resourceIdList.setResourceids(resourceUris);
        Response<BatchMetricResultsResponse> response = this.serviceClient.getMetrics()
            .batchWithResponse(subscriptionId, metricsNamespace, metricsNames, resourceIdList, startTime,
                endTime, granularity, aggregations, top, orderBy, filter, null, context);
        BatchMetricResultsResponse value = response.getValue();
        List<BatchMetricResultsResponseValuesItem> values = value.getValues();
        List<MetricsQueryResult> metricsQueryResults = values.stream()
            .map(result -> mapToMetricsQueryResult(result))
            .collect(Collectors.toList());
        MetricsBatchResult metricsBatchResult = new MetricsBatchResult(metricsQueryResults);

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), metricsBatchResult);

    }

}
