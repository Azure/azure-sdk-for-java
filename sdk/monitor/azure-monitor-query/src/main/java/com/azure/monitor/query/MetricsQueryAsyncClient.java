// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.models.ResponseError;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.implementation.logs.models.LogsQueryHelper;
import com.azure.monitor.query.implementation.metrics.MonitorManagementClientImpl;
import com.azure.monitor.query.implementation.metrics.models.ErrorResponseException;
import com.azure.monitor.query.implementation.metrics.models.MetricsHelper;
import com.azure.monitor.query.implementation.metrics.models.ResultType;
import com.azure.monitor.query.implementation.metricsdefinitions.MetricsDefinitionsClientImpl;
import com.azure.monitor.query.implementation.metricsnamespaces.MetricsNamespacesClientImpl;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.monitor.query.implementation.metrics.models.MetricsHelper.convertToMetricsQueryResult;

/**
 * The asynchronous client for querying Azure Monitor metrics.
 * <p><strong>Instantiating an asynchronous Metrics query Client</strong></p>
 *
 * <!-- src_embed com.azure.monitor.query.MetricsQueryAsyncClient.instantiation -->
 * <pre>
 * MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.MetricsQueryAsyncClient.instantiation -->
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
     * <!-- src_embed com.azure.monitor.query.MetricsQueryAsyncClient.query#String-List -->
     * <pre>
     * Mono&lt;MetricsQueryResult&gt; response = metricsQueryAsyncClient
     *         .queryResource&#40;&quot;&#123;resource-id&#125;&quot;, Arrays.asList&#40;&quot;&#123;metric-1&#125;&quot;, &quot;&#123;metric-2&#125;&quot;&#41;&#41;;
     *
     * response.subscribe&#40;result -&gt; &#123;
     *     for &#40;MetricResult metricResult : result.getMetrics&#40;&#41;&#41; &#123;
     *         System.out.println&#40;&quot;Metric name &quot; + metricResult.getMetricName&#40;&#41;&#41;;
     *         metricResult.getTimeSeries&#40;&#41;.stream&#40;&#41;
     *                 .flatMap&#40;timeSeriesElement -&gt; timeSeriesElement.getValues&#40;&#41;.stream&#40;&#41;&#41;
     *                 .forEach&#40;metricValue -&gt;
     *                         System.out.println&#40;&quot;Time stamp: &quot; + metricValue.getTimeStamp&#40;&#41; + &quot;; Total:  &quot;
     *                                 + metricValue.getTotal&#40;&#41;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.query.MetricsQueryAsyncClient.query#String-List -->
     *
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MetricsQueryResult> queryResource(String resourceUri, List<String> metricsNames) {
        return queryResourceWithResponse(resourceUri, metricsNames, new MetricsQueryOptions()).map(Response::getValue);
    }

    /**
     * Returns all the Azure Monitor metrics requested for the resource.
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param options Options to filter the query.
     * @return A time-series metrics result for the requested metric names.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricsQueryResult>> queryResourceWithResponse(String resourceUri, List<String> metricsNames,
                                                                        MetricsQueryOptions options) {
        return withContext(context -> queryResourceWithResponse(resourceUri, metricsNames, options, context));
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
                .listAsync(resourceUri, startTime == null ? null : startTime.toString())
                .mapPage(MetricsHelper::mapMetricNamespace);
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
                .mapPage(MetricsHelper::mapToMetricDefinition);
    }


    @SuppressWarnings("deprecation")
    PagedFlux<MetricNamespace> listMetricNamespaces(String resourceUri, OffsetDateTime startTime, Context context) {
        return metricsNamespaceClient
                .getMetricNamespaces()
                .listAsync(resourceUri, startTime == null ? null : startTime.toString(), context)
                .mapPage(MetricsHelper::mapMetricNamespace);
    }



    @SuppressWarnings("deprecation")
    PagedFlux<MetricDefinition> listMetricDefinitions(String resourceUri, String metricsNamespace, Context context) {
        return metricsDefinitionsClient.getMetricDefinitions()
                .listAsync(resourceUri, metricsNamespace, context)
                .mapPage(MetricsHelper::mapToMetricDefinition);
    }

    Mono<Response<MetricsQueryResult>> queryResourceWithResponse(String resourceUri, List<String> metricsNames,
                                                                 MetricsQueryOptions options, Context context) {
        String aggregation = null;
        if (!CoreUtils.isNullOrEmpty(options.getAggregations())) {
            aggregation = options.getAggregations()
                    .stream()
                    .map(type -> type.toString())
                    .collect(Collectors.joining(","));
        }
        String timespan = options.getTimeInterval() == null ? null
                : LogsQueryHelper.toIso8601Format(options.getTimeInterval());
        return metricsClient
                .getMetrics()
                .listWithResponseAsync(resourceUri, timespan, options.getGranularity(),
                        String.join(",", metricsNames), aggregation, options.getTop(), options.getOrderBy(),
                        options.getFilter(), ResultType.DATA, options.getMetricNamespace(), context)
                .map(response -> convertToMetricsQueryResult(response))
                .onErrorMap(ErrorResponseException.class, ex -> {
                    return new HttpResponseException(ex.getMessage(), ex.getResponse(),
                            new ResponseError(ex.getValue().getCode(), ex.getValue().getMessage()));
                });
    }

}
