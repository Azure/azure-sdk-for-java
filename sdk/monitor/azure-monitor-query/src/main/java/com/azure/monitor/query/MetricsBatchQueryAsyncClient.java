package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.implementation.metricsbatch.AzureMonitorMetricBatch;
import com.azure.monitor.query.implementation.metricsbatch.models.MetricResultsResponse;
import com.azure.monitor.query.implementation.metricsbatch.models.MetricResultsResponseValuesItem;
import com.azure.monitor.query.implementation.metricsbatch.models.ResourceIdList;
import com.azure.monitor.query.models.MetricsBatchResult;
import com.azure.monitor.query.models.MetricsQueryResult;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.monitor.query.implementation.metrics.models.MetricsHelper.getSubscriptionFromResourceId;
import static com.azure.monitor.query.implementation.metrics.models.MetricsHelper.mapToMetricsQueryResult;

/**
 * The asynchronous client for querying Azure Monitor metrics.
 */
@ServiceClient(builder = MetricsBatchQueryClientBuilder.class, isAsync = true)
public final class MetricsBatchQueryAsyncClient {
    private final AzureMonitorMetricBatch metricsBatchClient;

    /**
     * @param metricsBatchClient The proxy service used to perform REST calls.
     */
    MetricsBatchQueryAsyncClient(AzureMonitorMetricBatch metricsBatchClient) {
        this.metricsBatchClient = metricsBatchClient;
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
    public Mono<MetricsBatchResult> queryBatch(List<String> resourceUris, List<String> metricsNames, String metricsNamespace) {
        return this.queryBatchWithResponse(resourceUris, metricsNames, metricsNamespace, Context.NONE)
            .map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor metrics requested for the batch of resources.
     *
     * @param resourceUris The resource URIs for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param metricsNamespace The namespace of the metrics to query.
     * @param context The context to associate with this operation.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricsBatchResult>> queryBatchWithResponse(List<String> resourceUris, List<String> metricsNames,
                                                                     String metricsNamespace, Context context) {

        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(resourceUris, "'resourceUris cannot be null."))) {
            throw new IllegalArgumentException("resourceUris cannot be empty");
        }

        if (CoreUtils.isNullOrEmpty(Objects.requireNonNull(metricsNames, "metricsNames cannot be null"))) {
            throw new IllegalArgumentException("metricsNames cannot be empty");
        }

        String subscriptionId = getSubscriptionFromResourceId(resourceUris.get(0));
        ResourceIdList resourceIdList = new ResourceIdList();
        resourceIdList.setResourceids(resourceUris);
        Mono<Response<MetricResultsResponse>> responseMono = this.metricsBatchClient.getMetrics()
            .batchWithResponseAsync(subscriptionId, metricsNamespace, metricsNames, resourceIdList, null,
                null, null, null, null, null, null, context);


        return responseMono.map(response -> {
            MetricResultsResponse value = response.getValue();
            List<MetricResultsResponseValuesItem> values = value.getValues();
            List<MetricsQueryResult> metricsQueryResults = values.stream()
                .map(result -> mapToMetricsQueryResult(result))
                .collect(Collectors.toList());
            MetricsBatchResult metricsBatchResult = new MetricsBatchResult(metricsQueryResults);
            return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), metricsBatchResult);
        });
    }
}
