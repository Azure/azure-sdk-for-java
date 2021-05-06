// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.metrics.implementation.MonitorManagementClientImpl;
import com.azure.monitor.query.metrics.implementation.models.Metric;
import com.azure.monitor.query.metrics.implementation.models.MetricValue;
import com.azure.monitor.query.metrics.implementation.models.MetricsResponse;
import com.azure.monitor.query.metrics.implementation.models.ResultType;
import com.azure.monitor.query.metrics.implementation.models.TimeSeriesElement;
import com.azure.monitor.query.metricsdefinitions.implementation.MetricsDefinitionsClientImpl;
import com.azure.monitor.query.metricsnamespaces.implementation.MetricsNamespacesClientImpl;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.Metrics;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.MetricsTimeSeriesElement;
import com.azure.monitor.query.models.MetricsValue;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 *
 */
@ServiceClient(builder = MetricsClientBuilder.class, isAsync = true)
public final class MetricsAsyncClient {
    private final MonitorManagementClientImpl metricsClient;
    private final MetricsNamespacesClientImpl metricsNamespaceClient;
    private final MetricsDefinitionsClientImpl metricsDefinitionsClient;

    /**
     * @param metricsClient
     * @param metricsNamespaceClient
     * @param metricsDefinitionsClients
     */
    MetricsAsyncClient(MonitorManagementClientImpl metricsClient,
                       MetricsNamespacesClientImpl metricsNamespaceClient,
                       MetricsDefinitionsClientImpl metricsDefinitionsClients) {
        this.metricsClient = metricsClient;
        this.metricsNamespaceClient = metricsNamespaceClient;
        this.metricsDefinitionsClient = metricsDefinitionsClients;
    }

    /**
     * @param resourceUri
     * @param metricsNames
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MetricsQueryResult> queryMetrics(String resourceUri, List<String> metricsNames) {
        return queryMetricsWithResponse(resourceUri, metricsNames, new MetricsQueryOptions()).map(Response::getValue);
    }

    /**
     * @param resourceUri
     * @param metricsNames
     * @param options
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricsQueryResult>> queryMetricsWithResponse(String resourceUri, List<String> metricsNames,
                                                                       MetricsQueryOptions options) {
        return withContext(context -> queryMetricsWithResponse(resourceUri, metricsNames, options, context));
    }

    /**
     * @param resourceUri
     * @param startTime
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime) {
        return metricsNamespaceClient
            .getMetricNamespaces()
            .listAsync(resourceUri, startTime.toString());
    }

    /**
     * @param resourceUri
     * @param metricsNamespace
     *
     * @return
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace) {
        return metricsDefinitionsClient
            .getMetricDefinitions()
            .listAsync(resourceUri, metricsNamespace);
    }

    PagedFlux<MetricNamespace> listMetricsNamespace(String resourceUri, OffsetDateTime startTime, Context context) {
        return metricsNamespaceClient
            .getMetricNamespaces()
            .listAsync(resourceUri, startTime.toString(), context);
    }

    PagedFlux<MetricDefinition> listMetricsDefinition(String resourceUri, String metricsNamespace, Context context) {
        return metricsDefinitionsClient.getMetricDefinitions()
            .listAsync(resourceUri, metricsNamespace, context);
    }

    Mono<Response<MetricsQueryResult>> queryMetricsWithResponse(String resourceUri, List<String> metricsNames,
                                                                MetricsQueryOptions options, Context context) {
        String aggregation = null;
        if (!CoreUtils.isNullOrEmpty(options.getAggregation())) {
            aggregation = options.getAggregation()
                .stream()
                .map(type -> String.valueOf(type.ordinal()))
                .collect(Collectors.joining(","));
        }
        return metricsClient
            .getMetrics()
            .listWithResponseAsync(resourceUri, options.getTimespan(), options.getInterval(),
                String.join(",", metricsNames), aggregation, options.getTop(), options.getOrderby(),
                options.getFilter(), ResultType.DATA, options.getMetricsNamespace(), context)
            .map(response -> convertToMetricsQueryResult(response));
    }

    private Response<MetricsQueryResult> convertToMetricsQueryResult(Response<MetricsResponse> response) {
        MetricsResponse metricsResponse = response.getValue();
        MetricsQueryResult metricsQueryResult = new MetricsQueryResult(
            metricsResponse.getCost(), metricsResponse.getTimespan(), metricsResponse.getInterval(),
            metricsResponse.getNamespace(), metricsResponse.getResourceregion(), mapMetrics(metricsResponse.getValue()));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), metricsQueryResult);
    }

    private List<Metrics> mapMetrics(List<Metric> value) {
        return value.stream()
            .map(metric -> new Metrics(metric.getId(), metric.getType(), metric.getUnit(), metric.getName().getValue(),
                mapTimeSeries(metric.getTimeseries())))
            .collect(Collectors.toList());
    }

    private List<MetricsTimeSeriesElement> mapTimeSeries(List<TimeSeriesElement> timeseries) {
        return timeseries.stream()
            .map(timeSeriesElement -> new MetricsTimeSeriesElement(mapMetricsData(timeSeriesElement.getData())))
            .collect(Collectors.toList());
    }

    private List<MetricsValue> mapMetricsData(List<MetricValue> data) {
        return data.stream()
            .map(metricValue -> new MetricsValue(metricValue.getTimeStamp(),
                metricValue.getAverage(), metricValue.getMinimum(), metricValue.getMaximum(), metricValue.getTotal(),
                metricValue.getCount()))
            .collect(Collectors.toList());
    }
}
