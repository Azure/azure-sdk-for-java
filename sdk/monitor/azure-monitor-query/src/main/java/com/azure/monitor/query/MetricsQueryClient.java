// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.monitor.query.implementation.logs.models.LogsQueryHelper;
import com.azure.monitor.query.implementation.metrics.MonitorManagementClientImpl;
import com.azure.monitor.query.implementation.metrics.models.MetricsHelper;
import com.azure.monitor.query.implementation.metrics.models.MetricsResponse;
import com.azure.monitor.query.implementation.metrics.models.ResultType;
import com.azure.monitor.query.implementation.metricsdefinitions.MetricsDefinitionsClientImpl;
import com.azure.monitor.query.implementation.metricsnamespaces.MetricsNamespacesClientImpl;
import com.azure.monitor.query.models.MetricDefinition;
import com.azure.monitor.query.models.MetricNamespace;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.models.MetricsQueryResult;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.monitor.query.implementation.metrics.models.MetricsHelper.convertToMetricsQueryResult;

/**
 * The synchronous client for querying Azure Monitor metrics.
 *
 * <p><strong>Instantiating a synchronous Metrics query Client</strong></p>
 * <!-- src_embed com.azure.monitor.query.MetricsQueryClient.instantiation -->
 * <pre>
 * MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.MetricsQueryClient.instantiation -->
 */
@ServiceClient(builder = MetricsQueryClientBuilder.class)
public final class MetricsQueryClient {

    private final MonitorManagementClientImpl metricsClient;
    private final MetricsNamespacesClientImpl metricsNamespaceClient;
    private final MetricsDefinitionsClientImpl metricsDefinitionsClient;

    MetricsQueryClient(MonitorManagementClientImpl metricsClient,
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
     * <!-- src_embed com.azure.monitor.query.MetricsQueryClient.query#String-List -->
     * <pre>
     * MetricsQueryResult response = metricsQueryClient.queryResource&#40;&quot;&#123;resource-id&#125;&quot;,
     *         Arrays.asList&#40;&quot;&#123;metric-1&#125;&quot;, &quot;&#123;metric-2&#125;&quot;&#41;&#41;;
     * for &#40;MetricResult metricResult : response.getMetrics&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;Metric name &quot; + metricResult.getMetricName&#40;&#41;&#41;;
     *
     *     metricResult.getTimeSeries&#40;&#41;.stream&#40;&#41;
     *             .flatMap&#40;timeSeriesElement -&gt; timeSeriesElement.getValues&#40;&#41;.stream&#40;&#41;&#41;
     *             .forEach&#40;metricValue -&gt;
     *                     System.out.println&#40;&quot;Time stamp: &quot; + metricValue.getTimeStamp&#40;&#41; + &quot;; Total:  &quot;
     *                             + metricValue.getTotal&#40;&#41;&#41;&#41;;
     * &#125;
     *
     * </pre>
     * <!-- end com.azure.monitor.query.MetricsQueryClient.query#String-List -->
     *
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @return A time-series metrics result for the requested metric names.
     * @throws NullPointerException if {@code resourceUri} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricsQueryResult queryResource(String resourceUri, List<String> metricsNames) {
        return queryResourceWithResponse(resourceUri, metricsNames, new MetricsQueryOptions(), Context.NONE).getValue();
    }

    /**
     * Returns all the Azure Monitor metrics requested for the resource.
     *
     * @param resourceUri The resource URI for which the metrics is requested.
     * @param metricsNames The names of the metrics to query.
     * @param options Options to filter the query.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return A time-series metrics result for the requested metric names.
     * @throws NullPointerException if {@code resourceUri} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricsQueryResult> queryResourceWithResponse(String resourceUri, List<String> metricsNames,
                                                                  MetricsQueryOptions options, Context context) {
        Objects.requireNonNull(resourceUri, "'resourceUri' cannot be null");

        String aggregation = null;
        if (options != null && !CoreUtils.isNullOrEmpty(options.getAggregations())) {
            aggregation = options.getAggregations()
                .stream()
                .map(type -> type.toString())
                .collect(Collectors.joining(","));
        }
        String timespan = options == null || options.getTimeInterval() == null ? null
            : LogsQueryHelper.toIso8601Format(options.getTimeInterval());
        Duration granularity = options == null ? null : options.getGranularity();
        Integer top = options == null ? null : options.getTop();
        String orderBy = options == null ? null : options.getOrderBy();
        String filter = options == null ? null : options.getFilter();
        String metricNamespace = options == null ? null : options.getMetricNamespace();

        Response<MetricsResponse> metricsResponseResponse = metricsClient
            .getMetrics()
            .listWithResponse(resourceUri, timespan, granularity, String.join(",", metricsNames),
                aggregation, top, orderBy, filter, ResultType.DATA, metricNamespace, context);
        return convertToMetricsQueryResult(metricsResponseResponse);
    }

    /**
     * Lists all the metrics namespaces created for the resource URI.
     *
     * @param resourceUri The resource URI for which the metrics namespaces are listed.
     * @param startTime The returned list of metrics namespaces are created after the specified start time.
     * @return A {@link PagedIterable paged collection} of metrics namespaces.
     * @throws NullPointerException if {@code resourceUri} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricNamespace> listMetricNamespaces(String resourceUri, OffsetDateTime startTime) {
        return listMetricNamespaces(resourceUri, startTime, Context.NONE);
    }

    /**
     * Lists all the metrics namespaces created for the resource URI.
     *
     * @param resourceUri The resource URI for which the metrics namespaces are listed.
     * @param startTime The returned list of metrics namespaces are created after the specified start time.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return A {@link PagedIterable paged collection} of metrics namespaces.
     * @throws NullPointerException if {@code resourceUri} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricNamespace> listMetricNamespaces(String resourceUri, OffsetDateTime startTime,
                                                               Context context) {
        Objects.requireNonNull(resourceUri, "'resourceUri' cannot be null");
        PagedResponse<com.azure.monitor.query.implementation.metricsnamespaces.models.MetricNamespace> response = metricsNamespaceClient.getMetricNamespaces().listSinglePage(resourceUri,
            startTime == null ? null : startTime.toString(), context);
        List<MetricNamespace> metricNamespaces = response.getValue()
            .stream()
            .map(MetricsHelper::mapMetricNamespace)
            .collect(Collectors.toList());


        return new PagedIterable<>(() -> new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), metricNamespaces, response.getContinuationToken(), null));
    }

    /**
     * Lists all the metrics definitions created for the resource URI.
     *
     * @param resourceUri The resource URI for which the metrics definitions are listed.
     * @return A {@link PagedIterable paged collection} of metrics definitions.
     * @throws NullPointerException if {@code resourceUri} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricDefinition> listMetricDefinitions(String resourceUri) {
        return listMetricDefinitions(resourceUri, null, Context.NONE);
    }

    /**
     * Lists all the metrics definitions created for the resource URI.
     *
     * @param resourceUri The resource URI for which the metrics definitions are listed.
     * @param metricsNamespace The metrics namespace to which the listed metrics definitions belong.
     * @param context Additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return A {@link PagedIterable paged collection} of metrics definitions.
     * @throws NullPointerException if {@code resourceUri} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricDefinition> listMetricDefinitions(String resourceUri, String metricsNamespace,
                                                                 Context context) {
        Objects.requireNonNull(resourceUri, "'resourceUri' cannot be null");
        PagedResponse<com.azure.monitor.query.implementation.metricsdefinitions.models.MetricDefinition> response = metricsDefinitionsClient.getMetricDefinitions().listSinglePage(resourceUri, metricsNamespace, context);
        List<MetricDefinition> metricDefinitions = response.getValue()
            .stream()
            .map(MetricsHelper::mapToMetricDefinition)
            .collect(Collectors.toList());

        return new PagedIterable<>(() -> new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), metricDefinitions, response.getContinuationToken(), null));
    }
}
