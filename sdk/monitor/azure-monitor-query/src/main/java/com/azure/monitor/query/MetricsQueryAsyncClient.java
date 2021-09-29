// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.ResponseError;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.implementation.logs.models.LogsQueryHelper;
import com.azure.monitor.query.implementation.metrics.models.Metric;
import com.azure.monitor.query.implementation.metrics.MonitorManagementClientImpl;
import com.azure.monitor.query.implementation.metrics.models.MetadataValue;
import com.azure.monitor.query.implementation.metrics.models.MetricsHelper;
import com.azure.monitor.query.implementation.metrics.models.MetricsResponse;
import com.azure.monitor.query.implementation.metrics.models.ResultType;
import com.azure.monitor.query.implementation.metricsdefinitions.MetricsDefinitionsClientImpl;
import com.azure.monitor.query.implementation.metricsdefinitions.models.LocalizableString;
import com.azure.monitor.query.implementation.metricsnamespaces.MetricsNamespacesClientImpl;
import com.azure.monitor.query.models.MetricAvailability;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricResult;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import com.azure.monitor.query.models.QueryTimeInterval;
import com.azure.monitor.query.models.TimeSeriesElement;
import com.azure.monitor.query.models.MetricValue;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The asynchronous client for querying Azure Monitor metrics.
 * <p><strong>Instantiating an asynchronous Metrics query Client</strong></p>
 *
 * {@codesnippet com.azure.monitor.query.MetricsQueryAsyncClient.instantiation}
 */
@ServiceClient(builder = MetricsQueryClientBuilder.class, isAsync = true)
public final class MetricsQueryAsyncClient {
    private final MonitorManagementClientImpl metricsClient;
    private final MetricsNamespacesClientImpl metricsNamespaceClient;
    private final MetricsDefinitionsClientImpl metricsDefinitionsClient;

    MetricsQueryAsyncClient(MonitorManagementClientImpl metricsClient,
                            MetricsNamespacesClientImpl metricsNamespaceClient,
                            MetricsDefinitionsClientImpl metricsDefinitionsClients) {
        this.metricsClient = metricsClient;
        this.metricsNamespaceClient = metricsNamespaceClient;
        this.metricsDefinitionsClient = metricsDefinitionsClients;
    }

    /**
     * Returns all the Azure Monitor metrics requested for the resource.
     *
     * <p><strong>Query metrics for an Azure resource</strong></p>
     *
     * {@codesnippet com.azure.monitor.query.MetricsQueryAsyncClient.query#String-List}
     *
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MetricsQueryResult> query(String resourceUri, List<String> metricsNames) {
        return queryWithResponse(resourceUri, metricsNames, new MetricsQueryOptions()).map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor metrics requested for the resource.
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param options Options to filter the query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricsQueryResult>> queryWithResponse(String resourceUri, List<String> metricsNames,
                                                                MetricsQueryOptions options) {
        return withContext(context -> queryWithResponse(resourceUri, metricsNames, options, context));
    }

    /**
     * Lists all the metrics namespaces created for the resource URI.
     * @param resourceUri The resource URI for which the metrics namespaces are listed.
     * @param startTime The returned list of metrics namespaces are created after the specified start time.
     * @return A {@link PagedFlux paged collection} of metrics namespaces.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    @SuppressWarnings("deprecation")
    public PagedFlux<MetricNamespace> listMetricNamespaces(String resourceUri, OffsetDateTime startTime) {
        return metricsNamespaceClient
                .getMetricNamespaces()
                .listAsync(resourceUri, startTime.toString())
                .mapPage(this::mapMetricNamespace);
    }

    /**
     * Lists all the metrics definitions created for the resource URI.
     * @param resourceUri The resource URI for which the metrics definitions are listed.
     * @return A {@link PagedFlux paged collection} of metrics definitions.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricDefinition> listMetricDefinitions(String resourceUri) {
        return listMetricDefinitions(resourceUri, null);
    }

    /**
     * Lists all the metrics definitions created for the resource URI.
     * @param resourceUri The resource URI for which the metrics definitions are listed.
     * @param metricsNamespace The metrics namespace to which the listed metrics definitions belong.
     * @return A {@link PagedFlux paged collection} of metrics definitions.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    @SuppressWarnings("deprecation")
    public PagedFlux<MetricDefinition> listMetricDefinitions(String resourceUri, String metricsNamespace) {
        return metricsDefinitionsClient
                .getMetricDefinitions()
                .listAsync(resourceUri, metricsNamespace)
                .mapPage(this::mapToMetricDefinition);
    }

    private MetricDefinition mapToMetricDefinition(com.azure.monitor.query.implementation.metricsdefinitions.models.MetricDefinition definition) {
        MetricDefinition metricDefinition = new MetricDefinition();
        List<String> dimensions = null;
        if (!CoreUtils.isNullOrEmpty(definition.getDimensions())) {
            dimensions = definition.getDimensions().stream().map(LocalizableString::getValue)
                    .collect(Collectors.toList());
        }
        MetricsHelper.setMetricDefinitionProperties(metricDefinition,
                definition.isDimensionRequired(), definition.getResourceId(), definition.getNamespace(),
                definition.getName().getValue(), definition.getDisplayDescription(), definition.getCategory(),
                definition.getMetricClass(), definition.getUnit(), definition.getPrimaryAggregationType(),
                definition.getSupportedAggregationTypes(),
                mapMetricAvailabilities(definition.getMetricAvailabilities()), definition.getId(),
                dimensions);
        return metricDefinition;
    }

    private List<MetricAvailability> mapMetricAvailabilities(List<com.azure.monitor.query.implementation.metricsdefinitions.models.MetricAvailability> metricAvailabilities) {
        return metricAvailabilities.stream()
                .map(availabilityImpl -> {
                    MetricAvailability metricAvailability = new MetricAvailability();
                    MetricsHelper.setMetricAvailabilityProperties(metricAvailability, availabilityImpl.getRetention(),
                            availabilityImpl.getTimeGrain());
                    return metricAvailability;
                }).collect(Collectors.toList());
    }

    @SuppressWarnings("deprecation")
    PagedFlux<MetricNamespace> listMetricNamespaces(String resourceUri, OffsetDateTime startTime, Context context) {
        return metricsNamespaceClient
                .getMetricNamespaces()
                .listAsync(resourceUri, startTime.toString(), context)
                .mapPage(this::mapMetricNamespace);
    }

    private MetricNamespace mapMetricNamespace(com.azure.monitor.query.implementation.metricsnamespaces.models.MetricNamespace namespaceImpl) {
        MetricNamespace metricNamespace = new MetricNamespace();
        MetricsHelper.setMetricNamespaceProperties(metricNamespace, namespaceImpl.getClassification(),
                namespaceImpl.getId(), namespaceImpl.getName(),
                namespaceImpl.getProperties() == null ? null : namespaceImpl.getProperties().getMetricNamespaceName(),
                namespaceImpl.getType());

        return metricNamespace;
    }

    @SuppressWarnings("deprecation")
    PagedFlux<MetricDefinition> listMetricDefinitions(String resourceUri, String metricsNamespace, Context context) {
        return metricsDefinitionsClient.getMetricDefinitions()
                .listAsync(resourceUri, metricsNamespace, context)
                .mapPage(this::mapToMetricDefinition);
    }

    Mono<Response<MetricsQueryResult>> queryWithResponse(String resourceUri, List<String> metricsNames,
                                                         MetricsQueryOptions options, Context context) {
        String aggregation = null;
        if (!CoreUtils.isNullOrEmpty(options.getAggregations())) {
            aggregation = options.getAggregations()
                    .stream()
                    .map(type -> String.valueOf(type.ordinal()))
                    .collect(Collectors.joining(","));
        }
        String timespan = options.getTimeInterval() == null ? null
                : LogsQueryHelper.toIso8601Format(options.getTimeInterval());
        return metricsClient
                .getMetrics()
                .listWithResponseAsync(resourceUri, timespan, options.getGranularity(),
                        String.join(",", metricsNames), aggregation, options.getTop(), options.getOrderBy(),
                        options.getFilter(), ResultType.DATA, options.getMetricNamespace(), context)
                .map(response -> convertToMetricsQueryResult(response));
    }

    private Response<MetricsQueryResult> convertToMetricsQueryResult(Response<MetricsResponse> response) {
        MetricsResponse metricsResponse = response.getValue();
        MetricsQueryResult metricsQueryResult = new MetricsQueryResult(
                metricsResponse.getCost(),
                metricsResponse.getTimespan() == null ? null : QueryTimeInterval.parse(metricsResponse.getTimespan()),
                metricsResponse.getInterval(),
                metricsResponse.getNamespace(), metricsResponse.getResourceregion(), mapMetrics(metricsResponse.getValue()));

        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), metricsQueryResult);
    }

    private List<MetricResult> mapMetrics(List<Metric> value) {
        return value.stream()
                .map(metric -> new MetricResult(metric.getId(), metric.getType(), metric.getUnit(), metric.getName().getValue(),
                        mapTimeSeries(metric.getTimeseries()), metric.getDisplayDescription(),
                        new ResponseError(metric.getErrorCode(), metric.getErrorMessage())))
                .collect(Collectors.toList());
    }

    private List<TimeSeriesElement> mapTimeSeries(List<com.azure.monitor.query.implementation.metrics.models.TimeSeriesElement> timeseries) {
        return timeseries.stream()
                .map(timeSeriesElement -> new TimeSeriesElement(mapMetricsData(timeSeriesElement.getData()),
                        mapMetricsMetadata(timeSeriesElement.getMetadatavalues())))
                .collect(Collectors.toList());
    }

    private Map<String, String> mapMetricsMetadata(List<MetadataValue> metadataValues) {
        if (metadataValues == null) {
            return null;
        }
        return metadataValues.stream()
                .collect(Collectors.toMap(value -> value.getName().getValue(), MetadataValue::getValue));
    }

    private List<MetricValue> mapMetricsData(List<com.azure.monitor.query.implementation.metrics.models.MetricValue> data) {
        return data.stream()
                .map(metricValue -> new MetricValue(metricValue.getTimeStamp(),
                        metricValue.getAverage(), metricValue.getMinimum(), metricValue.getMaximum(), metricValue.getTotal(),
                        metricValue.getCount()))
                .collect(Collectors.toList());
    }
}
