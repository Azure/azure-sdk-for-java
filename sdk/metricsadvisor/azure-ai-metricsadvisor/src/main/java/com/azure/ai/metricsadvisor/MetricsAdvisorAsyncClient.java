// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.implementation.AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2Impl;
import com.azure.ai.metricsadvisor.implementation.models.AlertingResultQuery;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyDimensionQuery;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyFeedback;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyFeedbackValue;
import com.azure.ai.metricsadvisor.implementation.models.ChangePointFeedback;
import com.azure.ai.metricsadvisor.implementation.models.ChangePointFeedbackValue;
import com.azure.ai.metricsadvisor.implementation.models.CommentFeedback;
import com.azure.ai.metricsadvisor.implementation.models.CommentFeedbackValue;
import com.azure.ai.metricsadvisor.implementation.models.DetectionAnomalyFilterCondition;
import com.azure.ai.metricsadvisor.implementation.models.DetectionAnomalyResultQuery;
import com.azure.ai.metricsadvisor.implementation.models.DetectionIncidentFilterCondition;
import com.azure.ai.metricsadvisor.implementation.models.DetectionIncidentResultQuery;
import com.azure.ai.metricsadvisor.implementation.models.DetectionSeriesQuery;
import com.azure.ai.metricsadvisor.implementation.models.DimensionGroupIdentity;
import com.azure.ai.metricsadvisor.implementation.models.EnrichmentStatusQueryOption;
import com.azure.ai.metricsadvisor.implementation.models.FeedbackDimensionFilter;
import com.azure.ai.metricsadvisor.implementation.models.MetricDataQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.MetricDimensionQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.MetricFeedbackFilter;
import com.azure.ai.metricsadvisor.implementation.models.MetricSeriesQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.PeriodFeedback;
import com.azure.ai.metricsadvisor.implementation.models.PeriodFeedbackValue;
import com.azure.ai.metricsadvisor.implementation.models.SeriesIdentity;
import com.azure.ai.metricsadvisor.implementation.util.AnomalyTransforms;
import com.azure.ai.metricsadvisor.implementation.util.DetectionConfigurationTransforms;
import com.azure.ai.metricsadvisor.implementation.util.IncidentHelper;
import com.azure.ai.metricsadvisor.implementation.util.IncidentRootCauseTransforms;
import com.azure.ai.metricsadvisor.implementation.util.IncidentTransforms;
import com.azure.ai.metricsadvisor.implementation.util.MetricEnrichedSeriesDataTransformations;
import com.azure.ai.metricsadvisor.implementation.util.MetricFeedbackTransforms;
import com.azure.ai.metricsadvisor.implementation.util.MetricSeriesDataTransforms;
import com.azure.ai.metricsadvisor.implementation.util.MetricSeriesDefinitionTransforms;
import com.azure.ai.metricsadvisor.models.AnomalyAlert;
import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.DataPointAnomaly;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.EnrichmentStatus;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorResponseException;
import com.azure.ai.metricsadvisor.models.IncidentRootCause;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListAnomalyDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyFeedback;
import com.azure.ai.metricsadvisor.models.MetricChangePointFeedback;
import com.azure.ai.metricsadvisor.models.MetricCommentFeedback;
import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.ai.metricsadvisor.models.MetricPeriodFeedback;
import com.azure.ai.metricsadvisor.models.MetricSeriesData;
import com.azure.ai.metricsadvisor.models.MetricSeriesDefinition;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.implementation.util.Utility.parseOperationId;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Metrics Advisor.
 *
 * <p><strong>Instantiating an asynchronous Metrics Advisor Client</strong></p>
 * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.instantiation -->
 * <pre>
 * MetricsAdvisorAsyncClient metricsAdvisorAsyncClient =
 *     new MetricsAdvisorClientBuilder&#40;&#41;
 *         .credential&#40;new MetricsAdvisorKeyCredential&#40;&quot;&#123;subscription_key&#125;&quot;, &quot;&#123;api_key&#125;&quot;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.instantiation -->
 *
 * @see MetricsAdvisorClientBuilder
 */
@ServiceClient(builder = MetricsAdvisorClientBuilder.class, isAsync = true)
public final class MetricsAdvisorAsyncClient {

    private static final String METRICS_ADVISOR_TRACING_NAMESPACE_VALUE = "Microsoft.CognitiveServices";
    final ClientLogger logger = new ClientLogger(MetricsAdvisorAsyncClient.class);
    private final AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2Impl service;

    /**
     * Create a {@link MetricsAdvisorAsyncClient} that sends requests to the Metrics Advisor
     * service's endpoint. Each service call goes through the
     * {@link MetricsAdvisorClientBuilder#pipeline(HttpPipeline)} http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Metrics Advisor supported by this client library.
     */
    MetricsAdvisorAsyncClient(AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2Impl service,
        MetricsAdvisorServiceVersion serviceVersion) {
        this.service = service;
    }

    /**
     * List series definition for a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-OffsetDateTime -->
     * <pre>
     * String metricId = &quot;b460abfc-7a58-47d7-9d99-21ee21fdfc6e&quot;;
     * final OffsetDateTime activeSince = OffsetDateTime.parse&#40;&quot;2020-07-10T00:00:00Z&quot;&#41;;
     *
     * metricsAdvisorAsyncClient.listMetricSeriesDefinitions&#40;metricId, activeSince&#41;
     *     .subscribe&#40;metricSeriesDefinition -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric id for the retrieved series definition : %s%n&quot;,
     *             metricSeriesDefinition.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;metricSeriesDefinition.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-OffsetDateTime -->
     *
     * @param metricId metric unique id.
     * @param activeSince the start time for querying series ingested after this time.
     *
     * @return A {@link PagedFlux} of the {@link MetricSeriesDefinition metric series definitions}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code activeSince}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricSeriesDefinition> listMetricSeriesDefinitions(
        String metricId,
        OffsetDateTime activeSince) {
        return listMetricSeriesDefinitions(metricId, activeSince, null);
    }

    /**
     * List series definition for a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-OffsetDateTime-ListMetricSeriesDefinitionOptions -->
     * <pre>
     * String metricId = &quot;b460abfc-7a58-47d7-9d99-21ee21fdfc6e&quot;;
     * final OffsetDateTime activeSince = OffsetDateTime.parse&#40;&quot;2020-07-10T00:00:00Z&quot;&#41;;
     * final ListMetricSeriesDefinitionOptions options
     *     = new ListMetricSeriesDefinitionOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;
     *     .setDimensionCombinationToFilter&#40;new HashMap&lt;String, List&lt;String&gt;&gt;&#40;&#41; &#123;&#123;
     *             put&#40;&quot;Dim2&quot;, Collections.singletonList&#40;&quot;Angelfish&quot;&#41;&#41;;
     *         &#125;&#125;&#41;;
     *
     * metricsAdvisorAsyncClient.listMetricSeriesDefinitions&#40;metricId, activeSince, options&#41;
     *     .subscribe&#40;metricSeriesDefinition -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric id for the retrieved series definition : %s%n&quot;,
     *             metricSeriesDefinition.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;metricSeriesDefinition.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-OffsetDateTime-ListMetricSeriesDefinitionOptions -->
     *
     * @param metricId metric unique id.
     * @param activeSince the start time for querying series ingested after this time.
     * @param options the additional filtering attributes that can be provided to query the series.
     *
     * @return A {@link PagedFlux} of the {@link MetricSeriesDefinition metric series definitions}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code activeSince}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricSeriesDefinition> listMetricSeriesDefinitions(
        String metricId,
        OffsetDateTime activeSince, ListMetricSeriesDefinitionOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listMetricSeriesDefinitionSinglePageAsync(metricId, activeSince, options, context)),
                continuationToken ->
                    withContext(context -> listMetricSeriesDefinitionNextPageAsync(continuationToken, activeSince,
                        options, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<MetricSeriesDefinition> listMetricSeriesDefinitions(String metricId,
        OffsetDateTime activeSince, ListMetricSeriesDefinitionOptions options, Context context) {
        return new PagedFlux<>(() -> listMetricSeriesDefinitionSinglePageAsync(metricId, activeSince, options,
            context),
            continuationToken -> listMetricSeriesDefinitionNextPageAsync(continuationToken, activeSince, options,
                context));
    }

    private Mono<PagedResponse<MetricSeriesDefinition>> listMetricSeriesDefinitionSinglePageAsync(String metricId,
        OffsetDateTime activeSince, ListMetricSeriesDefinitionOptions options, Context context) {

        if (activeSince == null) {
            Objects.requireNonNull(options, "'activeSince' is required and cannot be null.");
        }

        if (options == null) {
            options = new ListMetricSeriesDefinitionOptions();
        }

        final MetricSeriesQueryOptions metricSeriesQueryOptions = new MetricSeriesQueryOptions()
            .setActiveSince(activeSince).setDimensionFilter(options.getDimensionCombinationsToFilter());

        return service.getMetricSeriesSinglePageAsync(UUID.fromString(metricId), metricSeriesQueryOptions,
            options.getSkip(), options.getMaxPageSize(), context)
            .doOnRequest(ignoredValue -> logger.info("Listing information metric series definitions"))
            .doOnSuccess(response -> logger.info("Listed metric series definitions - {}", response))
            .doOnError(error -> logger.warning("Failed to list metric series definitions information - {}", error))
            .map(res -> MetricSeriesDefinitionTransforms.fromInnerResponse(res));
    }

    private Mono<PagedResponse<MetricSeriesDefinition>> listMetricSeriesDefinitionNextPageAsync(String nextPageLink,
        OffsetDateTime activeSince, ListMetricSeriesDefinitionOptions options, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        if (activeSince == null) {
            Objects.requireNonNull(options, "'activeSince' is required and cannot be null.");
        }

        if (options == null) {
            options = new ListMetricSeriesDefinitionOptions();
        }
        final MetricSeriesQueryOptions metricSeriesQueryOptions = new MetricSeriesQueryOptions()
            .setActiveSince(activeSince).setDimensionFilter(options.getDimensionCombinationsToFilter());

        return service.getMetricSeriesNextSinglePageAsync(nextPageLink, metricSeriesQueryOptions, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(res -> MetricSeriesDefinitionTransforms.fromInnerResponse(res));
    }

    /**
     * Get time series data from metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String metricId = &quot;2dgfbbbb-41ec-a637-677e77b81455&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     *
     * final List&lt;DimensionKey&gt; seriesKeyFilter
     *     = Arrays.asList&#40;new DimensionKey&#40;&#41;.put&#40;&quot;cost&quot;, &quot;redmond&quot;&#41;&#41;;
     *
     * metricsAdvisorAsyncClient.listMetricSeriesData&#40;metricId, seriesKeyFilter, startTime, endTime&#41;
     *     .subscribe&#40;metricSeriesData -&gt; &#123;
     *         System.out.println&#40;&quot;List of data points for this series:&quot;&#41;;
     *         System.out.println&#40;metricSeriesData.getMetricValues&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Timestamps of the data related to this time series:&quot;&#41;;
     *         System.out.println&#40;metricSeriesData.getTimestamps&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;metricSeriesData.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
     *
     * @param metricId metric unique id.
     * @param seriesKeys the series key to filter.
     * <p>This enables additional filtering of dimension values being queried.
     * For example, let's say we've the dimensions 'category' and 'city',
     * so the api can query value of the dimension 'category', with series key as 'city=redmond'.
     * </p>
     * @param startTime The start time for querying the time series data.
     * @param endTime The end time for querying the time series data.
     *
     * @return A {@link PagedFlux} of the {@link MetricSeriesData metric series data points}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId}, {@code startTime} or {@code endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricSeriesData> listMetricSeriesData(
        String metricId, List<DimensionKey> seriesKeys, OffsetDateTime startTime, OffsetDateTime endTime) {
        try {
            return new PagedFlux<>(() -> withContext(context -> listMetricSeriesDataInternal(metricId, seriesKeys,
                startTime, endTime, context)),
                null);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<MetricSeriesData> listMetricSeriesData(String metricId, List<DimensionKey> seriesKeys,
        OffsetDateTime startTime, OffsetDateTime endTime, Context context) {
        return new PagedFlux<>(() -> listMetricSeriesDataInternal(metricId, seriesKeys, startTime, endTime, context),
            null);
    }

    private Mono<PagedResponse<MetricSeriesData>> listMetricSeriesDataInternal(String metricId,
        List<DimensionKey> seriesKeys, OffsetDateTime startTime,
        OffsetDateTime endTime, Context context) {
        Objects.requireNonNull(metricId, "'metricId' cannot be null.");
        Objects.requireNonNull(startTime, "'startTime' cannot be null.");
        Objects.requireNonNull(endTime, "'endTime' cannot be null.");
        if (CoreUtils.isNullOrEmpty(seriesKeys)) {
            Objects.requireNonNull(seriesKeys, "'seriesKeys' cannot be null or empty.");
        }

        List<Map<String, String>> dimensionList =
            seriesKeys.stream().map(DimensionKey::asMap).collect(Collectors.toList());
        final MetricDataQueryOptions metricDataQueryOptions
            = new MetricDataQueryOptions()
            .setStartTime(startTime)
            .setEndTime(startTime)
            .setSeries(dimensionList);

        return service.getMetricDataWithResponseAsync(UUID.fromString(metricId), metricDataQueryOptions,
            context)
            .map(response -> MetricSeriesDataTransforms.fromInnerResponse(response));
    }

    /**
     * List dimension values from certain metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String -->
     * <pre>
     *
     * metricsAdvisorAsyncClient.listMetricDimensionValues&#40;&quot;metricId&quot;, &quot;dimension1&quot;&#41;
     *     .subscribe&#40;System.out::println&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String -->
     *
     * @param metricId metric unique id.
     * @param dimensionName the query dimension name.
     *
     * @return the {@link PagedFlux} of the dimension values for that metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code dimensionName} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listMetricDimensionValues(
        final String metricId,
        final String dimensionName) {
        return listMetricDimensionValues(metricId, dimensionName, null);
    }

    /**
     * List dimension values from certain metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions -->
     * <pre>
     * metricsAdvisorAsyncClient.listMetricDimensionValues&#40;&quot;metricId&quot;, &quot;dimension1&quot;,
     *     new ListMetricDimensionValuesOptions&#40;&#41;.setDimensionValueToFilter&#40;&quot;value1&quot;&#41;.setMaxPageSize&#40;3&#41;&#41;
     *     .subscribe&#40;System.out::println&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions -->
     *
     * @param metricId metric unique id.
     * @param dimensionName the query dimension name.
     * @param options the additional filtering parameters to specify while querying.
     *
     * @return the {@link PagedFlux} of the dimension values for that metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code dimensionName} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listMetricDimensionValues(
        final String metricId,
        final String dimensionName,
        final ListMetricDimensionValuesOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listMetricDimensionValuesSinglePageAsync(metricId, dimensionName, options, context)),
                continuationToken ->
                    withContext(context -> listMetricDimensionValuesNextPageAsync(continuationToken, dimensionName,
                        options, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<String> listMetricDimensionValues(final String metricId, final String dimensionName,
        final ListMetricDimensionValuesOptions options, final Context context) {
        return new PagedFlux<>(() -> listMetricDimensionValuesSinglePageAsync(metricId, dimensionName,
            options, context),
            continuationToken -> listMetricDimensionValuesNextPageAsync(continuationToken, dimensionName,
                options, context));
    }

    private Mono<PagedResponse<String>> listMetricDimensionValuesSinglePageAsync(String metricId, String dimensionName,
        ListMetricDimensionValuesOptions options, Context context) {
        Objects.requireNonNull(metricId, "'metricId' cannot be null.");
        Objects.requireNonNull(dimensionName, "'dimensionName' cannot be null.");
        if (options == null) {
            options = new ListMetricDimensionValuesOptions();
        }

        final MetricDimensionQueryOptions metricDimensionQueryOptions = new MetricDimensionQueryOptions()
            .setDimensionName(dimensionName).setDimensionValueFilter(options.getDimensionValueToFilter());

        return service.getMetricDimensionSinglePageAsync(UUID.fromString(metricId), metricDimensionQueryOptions,
            options.getSkip(), options.getMaxPageSize(), context)
            .doOnRequest(ignoredValue -> logger.info("Listing all dimension values for a metric"))
            .doOnSuccess(response -> logger.info("Listed all dimension values for a metric"))
            .doOnError(error -> logger.warning("Failed to list all dimension values for a metric information", error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue(),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<String>> listMetricDimensionValuesNextPageAsync(String nextPageLink,
        String dimensionName, ListMetricDimensionValuesOptions options, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        if (options == null) {
            options = new ListMetricDimensionValuesOptions();
        }
        final MetricDimensionQueryOptions metricDimensionQueryOptions = new MetricDimensionQueryOptions()
            .setDimensionName(dimensionName).setDimensionValueFilter(options.getDimensionValueToFilter());

        return service.getMetricDimensionNextSinglePageAsync(nextPageLink, metricDimensionQueryOptions, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue(),
                res.getContinuationToken(),
                null));
    }

    /**
     * List the enrichment status for a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     *
     * metricsAdvisorAsyncClient.listMetricEnrichmentStatus&#40;metricId, startTime, endTime&#41;
     *     .subscribe&#40;enrichmentStatus -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status : %s%n&quot;, enrichmentStatus.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status message: %s%n&quot;, enrichmentStatus.getMessage&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status data slice timestamp : %s%n&quot;,
     *             enrichmentStatus.getTimestamp&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param metricId metric unique id.
     * @param startTime The start time for querying the time series data.
     * @param endTime The end time for querying the time series data.
     *
     * @return the list of enrichment status's for the specified metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if {@code metricId}, {@code startTime} and {@code endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<EnrichmentStatus> listMetricEnrichmentStatus(
        String metricId,
        OffsetDateTime startTime, OffsetDateTime endTime) {
        return listMetricEnrichmentStatus(metricId, startTime, endTime, null);
    }

    /**
     * List the enrichment status for a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime-ListMetricEnrichmentStatusOptions -->
     * <pre>
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final ListMetricEnrichmentStatusOptions options = new ListMetricEnrichmentStatusOptions&#40;&#41;.setMaxPageSize&#40;10&#41;;
     *
     * metricsAdvisorAsyncClient.listMetricEnrichmentStatus&#40;metricId, startTime, endTime, options&#41;
     *     .subscribe&#40;enrichmentStatus -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status : %s%n&quot;, enrichmentStatus.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status message: %s%n&quot;, enrichmentStatus.getMessage&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status data slice timestamp : %s%n&quot;,
     *             enrichmentStatus.getTimestamp&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime-ListMetricEnrichmentStatusOptions -->
     *
     * @param metricId metric unique id.
     * @param startTime The start time for querying the time series data.
     * @param endTime The end time for querying the time series data.
     * @param options th e additional configurable options to specify when querying the result..
     *
     * @return the list of enrichment status's for the specified metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if {@code metricId}, {@code startTime} and {@code endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<EnrichmentStatus> listMetricEnrichmentStatus(
        String metricId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListMetricEnrichmentStatusOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listMetricEnrichmentStatusSinglePageAsync(metricId, startTime, endTime, options, context)),
                continuationToken ->
                    withContext(context -> listMetricEnrichmentStatusNextPageAsync(continuationToken,
                        startTime, endTime, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<EnrichmentStatus> listMetricEnrichmentStatus(
        String metricId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListMetricEnrichmentStatusOptions options, Context context) {
        return new PagedFlux<>(() -> listMetricEnrichmentStatusSinglePageAsync(metricId, startTime, endTime,
            options, context),
            continuationToken -> listMetricEnrichmentStatusNextPageAsync(continuationToken, startTime, endTime,
                context));
    }

    private Mono<PagedResponse<EnrichmentStatus>> listMetricEnrichmentStatusSinglePageAsync(String metricId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListMetricEnrichmentStatusOptions options, Context context) {
        Objects.requireNonNull(metricId, "'metricId' is required.");
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");
        if (options == null) {
            options = new ListMetricEnrichmentStatusOptions();
        }
        final EnrichmentStatusQueryOption enrichmentStatusQueryOption =
            new EnrichmentStatusQueryOption().setStartTime(startTime).setEndTime(endTime);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.getEnrichmentStatusByMetricSinglePageAsync(
            UUID.fromString(metricId),
            enrichmentStatusQueryOption,
            options.getSkip(),
            options.getMaxPageSize(),
            withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing all metric enrichment status values for a metric"))
            .doOnSuccess(response -> logger.info("Listed all metric enrichment status values for a metric - {}",
                response))
            .doOnError(error -> logger.warning("Failed to list all metric enrichment values for a metric information",
                error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue(),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<EnrichmentStatus>> listMetricEnrichmentStatusNextPageAsync(String nextPageLink,
        OffsetDateTime startTime, OffsetDateTime endTime, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");

        final EnrichmentStatusQueryOption enrichmentStatusQueryOption =
            new EnrichmentStatusQueryOption().setStartTime(startTime).setEndTime(endTime);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.getEnrichmentStatusByMetricNextSinglePageAsync(nextPageLink, enrichmentStatusQueryOption,
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue(),
                res.getContinuationToken(),
                null));
    }

    /**
     * Given a list of time series keys, retrieve time series version enriched using
     * a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String detectionConfigurationId = &quot;e87d899d-a5a0-4259-b752-11aea34d5e34&quot;;
     * final DimensionKey seriesKey = new DimensionKey&#40;&#41;
     *     .put&#40;&quot;Dim1&quot;, &quot;Common Lime&quot;&#41;
     *     .put&#40;&quot;Dim2&quot;, &quot;Antelope&quot;&#41;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-08-12T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-12T00:00:00Z&quot;&#41;;
     *
     * PagedFlux&lt;MetricEnrichedSeriesData&gt; enrichedDataFlux
     *     = metricsAdvisorAsyncClient.listMetricEnrichedSeriesData&#40;detectionConfigurationId,
     *     Arrays.asList&#40;seriesKey&#41;,
     *     startTime,
     *     endTime&#41;;
     *
     * enrichedDataFlux.subscribe&#40;enrichedData -&gt; &#123;
     *     System.out.printf&#40;&quot;Series Key %s%n:&quot;, enrichedData.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;List of data points for this series&quot;&#41;;
     *     System.out.println&#40;enrichedData.getMetricValues&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Timestamps of the data related to this time series:&quot;&#41;;
     *     System.out.println&#40;enrichedData.getTimestamps&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;The expected values of the data points calculated by the smart detector:&quot;&#41;;
     *     System.out.println&#40;enrichedData.getExpectedMetricValues&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;The lower boundary values of the data points calculated by smart detector:&quot;&#41;;
     *     System.out.println&#40;enrichedData.getLowerBoundaryValues&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;the periods calculated for the data points in the time series:&quot;&#41;;
     *     System.out.println&#40;enrichedData.getPeriods&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
     *
     * @param detectionConfigurationId The id of the configuration used to enrich the time series
     *     identified by the keys in {@code seriesKeys}.
     * @param seriesKeys The time series key list, each key identifies a specific time series.
     * @param startTime The start time of the time range within which the enriched data is returned.
     * @param endTime The end time of the time range within which the enriched data is returned.
     * @return The enriched time series.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation
     *     or if {@code seriesKeys} is empty.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricEnrichedSeriesData> listMetricEnrichedSeriesData(String detectionConfigurationId,
                                                                            List<DimensionKey> seriesKeys,
                                                                            OffsetDateTime startTime,
                                                                            OffsetDateTime endTime) {
        try {
            return new PagedFlux<>(() -> withContext(context -> listMetricEnrichedSeriesDataInternal(
                detectionConfigurationId,
                seriesKeys,
                startTime, endTime, context)), null);
        } catch (RuntimeException e) {
            return new PagedFlux<>(() -> monoError(logger, e));
        }
    }

    PagedFlux<MetricEnrichedSeriesData> listMetricEnrichedSeriesData(String detectionConfigurationId,
                                                                     List<DimensionKey> seriesKeys,
                                                                     OffsetDateTime startTime,
                                                                     OffsetDateTime endTime,
                                                                     Context context) {
        return new PagedFlux<>(() -> listMetricEnrichedSeriesDataInternal(detectionConfigurationId,
            seriesKeys,
            startTime, endTime, context), null);
    }

    private Mono<PagedResponse<MetricEnrichedSeriesData>>
        listMetricEnrichedSeriesDataInternal(String detectionConfigurationId,
                                             List<DimensionKey> seriesKeys,
                                             OffsetDateTime startTime,
                                             OffsetDateTime endTime,
                                             Context context) {
        Objects.requireNonNull(seriesKeys, "'seriesKeys' is required.");
        if (seriesKeys.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'seriesKeys' cannot be empty."));
        }
        Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");

        final List<SeriesIdentity> innerSeriesKeys = seriesKeys
            .stream()
            .map(seriesId -> new SeriesIdentity().setDimension(seriesId.asMap()))
            .collect(Collectors.toList());

        DetectionSeriesQuery query = new DetectionSeriesQuery()
            .setSeries(innerSeriesKeys)
            .setStartTime(startTime)
            .setEndTime(endTime);

        final Context withTracing
            = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getSeriesByAnomalyDetectionConfigurationWithResponseAsync(
            UUID.fromString(detectionConfigurationId),
            query,
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the EnrichedSeries"))
            .doOnSuccess(response -> logger.info("Retrieved the EnrichedSeries {}", response))
            .doOnError(error -> logger.warning("Failed to retrieve EnrichedSeries", error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                MetricEnrichedSeriesDataTransformations.fromInnerList(res.getValue()),
                null,
                null));
    }

    /**
     * Fetch the anomalies identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     *
     * metricsAdvisorAsyncClient.listAnomaliesForDetectionConfig&#40;detectionConfigurationId,
     *     startTime, endTime&#41;
     *     .subscribe&#40;anomaly -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly AnomalySeverity: %s%n&quot;, anomaly.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         DimensionKey seriesKey = anomaly.getSeriesKey&#40;&#41;;
     *         for &#40;Map.Entry&lt;String, String&gt; dimension : seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;DimensionName: %s DimensionValue:%s%n&quot;,
     *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param startTime The start time of the time range within which the anomalies were detected.
     * @param endTime The end time of the time range within which the anomalies were detected.
     *
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification
     *     or {@code options.filter} is used to set severity but either min or max severity is missing.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code options}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataPointAnomaly> listAnomaliesForDetectionConfig(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime) {
        return listAnomaliesForDetectionConfig(detectionConfigurationId, startTime, endTime, null);
    }

    /**
     * Fetch the anomalies identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListAnomaliesDetectedOptions -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     * final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter&#40;&#41;
     *     .setSeverityRange&#40;AnomalySeverity.LOW, AnomalySeverity.MEDIUM&#41;;
     * final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;
     *     .setFilter&#40;filter&#41;;
     * metricsAdvisorAsyncClient.listAnomaliesForDetectionConfig&#40;detectionConfigurationId,
     *         startTime, endTime, options&#41;
     *     .subscribe&#40;anomaly -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly AnomalySeverity: %s%n&quot;, anomaly.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         DimensionKey seriesKey = anomaly.getSeriesKey&#40;&#41;;
     *         for &#40;Map.Entry&lt;String, String&gt; dimension : seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;DimensionName: %s DimensionValue:%s%n&quot;,
     *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListAnomaliesDetectedOptions -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param startTime The start time of the time range within which the anomalies were detected.
     * @param endTime The end time of the time range within which the anomalies were detected.
     * @param options The additional parameters.
     *
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification
     *     or {@code options.filter} is used to set severity but either min or max severity is missing.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code options}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataPointAnomaly> listAnomaliesForDetectionConfig(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomaliesDetectedOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listAnomaliesForDetectionConfigSinglePageAsync(detectionConfigurationId,
                        startTime, endTime, options, context)),
                continuationToken ->
                    withContext(context -> listAnomaliesForDetectionConfigNextPageAsync(continuationToken,
                        startTime, endTime, options, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<DataPointAnomaly> listAnomaliesForDetectionConfig(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomaliesDetectedOptions options, Context context) {
        return new PagedFlux<>(() ->
            listAnomaliesForDetectionConfigSinglePageAsync(detectionConfigurationId, startTime, endTime, options,
                context),
            continuationToken ->
                listAnomaliesForDetectionConfigNextPageAsync(continuationToken, startTime, endTime, options, context));
    }

    private Mono<PagedResponse<DataPointAnomaly>> listAnomaliesForDetectionConfigSinglePageAsync(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomaliesDetectedOptions options,
        Context context) {
        Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");

        DetectionAnomalyResultQuery query = new DetectionAnomalyResultQuery()
            .setStartTime(startTime)
            .setEndTime(endTime);

        if (options == null) {
            options = new ListAnomaliesDetectedOptions();
        }

        if (options.getFilter() != null) {
            DetectionAnomalyFilterCondition innerFilter = AnomalyTransforms.toInnerFilter(options.getFilter(),
                logger);
            if (innerFilter != null) {
                query.setFilter(innerFilter);
            }
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getAnomaliesByAnomalyDetectionConfigurationSinglePageAsync(
            UUID.fromString(detectionConfigurationId),
            query,
            options.getSkip(),
            options.getMaxPageSize(),
            withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing anomalies detected"))
            .doOnSuccess(response -> logger.info("Listed anomalies {}", response))
            .doOnError(error -> logger.warning("Failed to list the anomalies detected", error))
            .map(response -> AnomalyTransforms.fromInnerPagedResponse(response));
    }

    private Mono<PagedResponse<DataPointAnomaly>> listAnomaliesForDetectionConfigNextPageAsync(
        String nextPageLink,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomaliesDetectedOptions options,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }

        DetectionAnomalyResultQuery query = new DetectionAnomalyResultQuery()
            .setStartTime(startTime)
            .setEndTime(endTime);

        if (options == null) {
            options = new ListAnomaliesDetectedOptions();
        }

        if (options.getFilter() != null) {
            DetectionAnomalyFilterCondition innerFilter = AnomalyTransforms.toInnerFilter(options.getFilter(),
                logger);
            if (innerFilter != null) {
                query.setFilter(innerFilter);
            }
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getAnomaliesByAnomalyDetectionConfigurationNextSinglePageAsync(nextPageLink,
            query,
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {} {}",
                nextPageLink,
                response))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(response -> AnomalyTransforms.fromInnerPagedResponse(response));
    }

    /**
     * Fetch the incidents identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     *
     * PagedFlux&lt;AnomalyIncident&gt; incidentsFlux
     *     = metricsAdvisorAsyncClient.listIncidentsForDetectionConfig&#40;detectionConfigurationId, startTime,
     *     endTime&#41;;
     *
     * incidentsFlux.subscribe&#40;incident -&gt; &#123;
     *     System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, incident.getMetricId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, incident.getDetectionConfigurationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Id: %s%n&quot;, incident.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Start Time: %s%n&quot;, incident.getStartTime&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident AnomalySeverity: %s%n&quot;, incident.getSeverity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Status: %s%n&quot;, incident.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Root DataFeedDimension Key: %s%n&quot;, incident.getRootDimensionKey&#40;&#41;.asMap&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param startTime The start time of the time range within which the incidents were detected.
     * @param endTime The end time of the time range within which the incidents were detected.
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code options}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyIncident> listIncidentsForDetectionConfig(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime) {
        return listIncidentsForDetectionConfig(detectionConfigurationId, startTime, endTime, null);
    }

    /**
     * Fetch the incidents identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListIncidentsDetectedOptions -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     * final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions&#40;&#41;
     *     .setMaxPageSize&#40;1000&#41;;
     *
     * PagedFlux&lt;AnomalyIncident&gt; incidentsFlux
     *     = metricsAdvisorAsyncClient.listIncidentsForDetectionConfig&#40;detectionConfigurationId, startTime,
     *     endTime, options&#41;;
     *
     * incidentsFlux.subscribe&#40;incident -&gt; &#123;
     *     System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, incident.getMetricId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, incident.getDetectionConfigurationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Id: %s%n&quot;, incident.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Start Time: %s%n&quot;, incident.getStartTime&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident AnomalySeverity: %s%n&quot;, incident.getSeverity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Status: %s%n&quot;, incident.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Root DataFeedDimension Key: %s%n&quot;, incident.getRootDimensionKey&#40;&#41;.asMap&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListIncidentsDetectedOptions -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param startTime The start time of the time range within which the incidents were detected.
     * @param endTime The end time of the time range within which the incidents were detected.
     * @param options The additional parameters.
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code options}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyIncident> listIncidentsForDetectionConfig(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListIncidentsDetectedOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listIncidentsForDetectionConfigSinglePageAsync(detectionConfigurationId, startTime, endTime,
                        options,
                        context)),
                continuationToken ->
                    withContext(context -> listIncidentsForDetectionConfigNextPageAsync(continuationToken,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<AnomalyIncident> listIncidentsForDetectionConfig(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListIncidentsDetectedOptions options, Context context) {
        return new PagedFlux<>(() ->
            listIncidentsForDetectionConfigSinglePageAsync(detectionConfigurationId, startTime, endTime, options,
                context),
            continuationToken ->
                listIncidentsForDetectionConfigNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<AnomalyIncident>> listIncidentsForDetectionConfigSinglePageAsync(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListIncidentsDetectedOptions options,
        Context context) {
        Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");

        DetectionIncidentResultQuery query = new DetectionIncidentResultQuery()
            .setStartTime(startTime)
            .setEndTime(endTime);
        if (options == null) {
            options = new ListIncidentsDetectedOptions();
        }
        if (options.getDimensionsToFilter() != null) {
            List<DimensionGroupIdentity> innerDimensionsToFilter = new ArrayList<>();
            for (DimensionKey dimensionToFilter : options.getDimensionsToFilter()) {
                innerDimensionsToFilter.add(new DimensionGroupIdentity()
                    .setDimension(dimensionToFilter.asMap()));
            }
            if (!innerDimensionsToFilter.isEmpty()) {
                query.setFilter(new DetectionIncidentFilterCondition()
                    .setDimensionFilter(innerDimensionsToFilter));
            }
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getIncidentsByAnomalyDetectionConfigurationSinglePageAsync(
            UUID.fromString(detectionConfigurationId),
            query,
            options.getMaxPageSize(),
            withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing incidents detected"))
            .doOnSuccess(response -> logger.info("Listed incidents {}", response))
            .doOnError(error -> logger.warning("Failed to list the incidents detected", error))
            .map(response -> IncidentTransforms.fromInnerPagedResponse(response));
    }

    private Mono<PagedResponse<AnomalyIncident>> listIncidentsForDetectionConfigNextPageAsync(
        String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getIncidentsByAnomalyDetectionConfigurationNextSinglePageAsync(nextPageLink, withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {} {}",
                nextPageLink,
                response))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(response -> IncidentTransforms.fromInnerPagedResponse(response));
    }

    /**
     * List the root causes for an incident.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#String-String -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final String incidentId = &quot;c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d&quot;;
     *
     * metricsAdvisorAsyncClient.listIncidentRootCauses&#40;detectionConfigurationId, incidentId&#41;
     *     .subscribe&#40;incidentRootCause -&gt; &#123;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, incidentRootCause.getDescription&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;incidentRootCause.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Confidence for the detected incident root cause: %.2f%n&quot;,
     *             incidentRootCause.getContributionScore&#40;&#41;&#41;;
     *     &#125;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#String-String -->
     *
     * @param detectionConfigurationId anomaly detection configuration unique id.
     * @param incidentId the incident for which you want to query root causes for.
     *
     * @return the list of root causes for that incident.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code incidentId} is null.
     **/
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IncidentRootCause> listIncidentRootCauses(
        String detectionConfigurationId,
        String incidentId) {
        AnomalyIncident anomalyIncident = new AnomalyIncident();
        IncidentHelper.setId(anomalyIncident, incidentId);
        IncidentHelper.setDetectionConfigurationId(anomalyIncident, detectionConfigurationId);
        try {
            return new PagedFlux<>(() ->
                withContext(context -> listIncidentRootCausesInternal(anomalyIncident, context)),  null);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<IncidentRootCause> listIncidentRootCauses(
        String detectionConfigurationId,
        String incidentId, Context context) {
        try {
            Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
            Objects.requireNonNull(incidentId, "'incidentId' is required.");
            AnomalyIncident anomalyIncident = new AnomalyIncident();
            IncidentHelper.setId(anomalyIncident, incidentId);
            IncidentHelper.setDetectionConfigurationId(anomalyIncident, detectionConfigurationId);
            return new PagedFlux<>(() -> listIncidentRootCausesInternal(anomalyIncident, context), null);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * List the root causes for an anomalyIncident.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#AnomalyIncident -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final ListIncidentsDetectedOptions options
     *     = new ListIncidentsDetectedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;;
     *
     * metricsAdvisorAsyncClient.listIncidentsForDetectionConfig&#40;detectionConfigurationId, startTime, endTime,
     *     options&#41;
     *     .flatMap&#40;incident -&gt; &#123;
     *         return metricsAdvisorAsyncClient.listIncidentRootCauses&#40;incident&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;incidentRootCause -&gt; &#123;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, incidentRootCause.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;incidentRootCause.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#AnomalyIncident -->
     *
     * @param anomalyIncident the anomalyIncident for which you want to query root causes for.
     *
     * @return the list of root causes for that anomalyIncident.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code incidentId} is null.
     **/
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IncidentRootCause> listIncidentRootCauses(AnomalyIncident anomalyIncident) {
        try {
            return new PagedFlux<>(() -> withContext(context ->
                listIncidentRootCausesInternal(anomalyIncident, context)),
                 null);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<IncidentRootCause> listIncidentRootCauses(AnomalyIncident anomalyIncident, Context context) {
        return new PagedFlux<>(() -> listIncidentRootCausesInternal(anomalyIncident, context), null);
    }

    private Mono<PagedResponse<IncidentRootCause>> listIncidentRootCausesInternal(AnomalyIncident anomalyIncident,
        Context context) {
        if (anomalyIncident == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'anomalyIncident' is required."));
        }
        Objects.requireNonNull(anomalyIncident.getDetectionConfigurationId(),
            "'anomalyIncident.detectionConfigurationId' is required.");
        Objects.requireNonNull(anomalyIncident.getId(), "'anomalyIncident.id' is required");
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getRootCauseOfIncidentByAnomalyDetectionConfigurationWithResponseAsync(
            UUID.fromString(anomalyIncident.getDetectionConfigurationId()),
            anomalyIncident.getId(), withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieved the IncidentRootCauses - {}",
                anomalyIncident.getDetectionConfigurationId()))
            .doOnSuccess(response -> logger.info("Retrieved the IncidentRootCauses - {}", response))
            .doOnError(error -> logger.warning("Failed to retrieve the incident root causes - {}",
                anomalyIncident.getDetectionConfigurationId(), error))
            .map(res -> IncidentRootCauseTransforms.fromInnerResponse(res));
    }

    /**
     * Fetch dimension values that have anomalies.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final String dimensionName = &quot;Dim1&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     *
     * metricsAdvisorAsyncClient.listAnomalyDimensionValues&#40;detectionConfigurationId,
     *     dimensionName,
     *     startTime, endTime&#41;
     *     .subscribe&#40;dimensionValue -&gt; &#123;
     *         System.out.printf&#40;&quot;DataFeedDimension Value: %s%n&quot;, dimensionValue&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime -->
     *
     * @param detectionConfigurationId Identifies the configuration used to detect the anomalies.
     * @param dimensionName The dimension name to retrieve the values for.
     * @param startTime The start time of the time range within which the anomalies were identified.
     * @param endTime The end time of the time range within which the anomalies were identified.
     * @return The dimension values with anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code dimensionName}
     *     or {@code options} or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listAnomalyDimensionValues(
        String detectionConfigurationId,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime) {
        return listAnomalyDimensionValues(detectionConfigurationId, dimensionName, startTime, endTime, null);
    }

    /**
     * Fetch dimension values that have anomalies.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime-ListAnomalyDimensionValuesOptions -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final String dimensionName = &quot;Dim1&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final ListAnomalyDimensionValuesOptions options
     *     = new ListAnomalyDimensionValuesOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;;
     *
     * metricsAdvisorAsyncClient.listAnomalyDimensionValues&#40;detectionConfigurationId,
     *     dimensionName,
     *     startTime, endTime, options&#41;
     *     .subscribe&#40;dimensionValue -&gt; &#123;
     *         System.out.printf&#40;&quot;DataFeedDimension Value: %s%n&quot;, dimensionValue&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime-ListAnomalyDimensionValuesOptions -->
     *
     * @param detectionConfigurationId Identifies the configuration used to detect the anomalies.
     * @param dimensionName The dimension name to retrieve the values for.
     * @param startTime The start time of the time range within which the anomalies were identified.
     * @param endTime The end time of the time range within which the anomalies were identified.
     * @param options The additional parameters.
     * @return The dimension values with anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code dimensionName}
     *     or {@code options} or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listAnomalyDimensionValues(
        String detectionConfigurationId,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomalyDimensionValuesOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listAnomalyDimensionValuesSinglePageAsync(detectionConfigurationId,
                        dimensionName,
                        startTime,
                        endTime,
                        options,
                        context)),
                continuationToken ->
                    withContext(context -> listAnomalyDimensionValuesNextPageAsync(continuationToken,
                        dimensionName,
                        startTime,
                        endTime,
                        options,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<String> listAnomalyDimensionValues(
        String detectionConfigurationId,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomalyDimensionValuesOptions options,
        Context context) {
        return new PagedFlux<>(() ->
            listAnomalyDimensionValuesSinglePageAsync(detectionConfigurationId,
                dimensionName,
                startTime,
                endTime,
                options,
                context),
            continuationToken ->
                listAnomalyDimensionValuesNextPageAsync(continuationToken,
                    dimensionName,
                    startTime,
                    endTime,
                    options,
                    context));
    }

    private Mono<PagedResponse<String>> listAnomalyDimensionValuesSinglePageAsync(
        String detectionConfigurationId,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomalyDimensionValuesOptions options,
        Context context) {
        Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
        Objects.requireNonNull(dimensionName, "'dimensionName' is required.");
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");

        AnomalyDimensionQuery query = new AnomalyDimensionQuery();
        query.setDimensionName(dimensionName);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        if (options == null) {
            options = new ListAnomalyDimensionValuesOptions();
        }
        if (options.getDimensionToFilter() != null) {
            query.setDimensionFilter(new DimensionGroupIdentity()
                .setDimension(options.getDimensionToFilter().asMap()));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getDimensionOfAnomaliesByAnomalyDetectionConfigurationSinglePageAsync(
            UUID.fromString(detectionConfigurationId),
            query,
            options.getSkip(),
            options.getMaxPageSize(),
            withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing dimension values with anomalies"))
            .doOnSuccess(response -> logger.info("Listed dimension values with anomalies {}", response))
            .doOnError(error -> logger.warning("Failed to list the dimension values with anomalies", error));
    }

    private Mono<PagedResponse<String>> listAnomalyDimensionValuesNextPageAsync(
        String nextPageLink,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomalyDimensionValuesOptions options,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }

        AnomalyDimensionQuery query = new AnomalyDimensionQuery();
        query.setDimensionName(dimensionName);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        if (options.getDimensionToFilter() != null) {
            query.setDimensionFilter(new DimensionGroupIdentity()
                .setDimension(options.getDimensionToFilter().asMap()));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getDimensionOfAnomaliesByAnomalyDetectionConfigurationNextSinglePageAsync(nextPageLink,
            query,
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error));
    }

    /**
     * Fetch the alerts triggered by an anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;
     *
     * metricsAdvisorAsyncClient.listAlerts&#40;alertConfigurationId, startTime, endTime&#41;
     *     .subscribe&#40;alert -&gt; &#123;
     *         System.out.printf&#40;&quot;Anomaly Alert Id: %s%n&quot;, alert.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Created Time: %s%n&quot;, alert.getCreatedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Modified Time: %s%n&quot;, alert.getModifiedTime&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param startTime The start time of the time range within which the alerts were triggered.
     * @param endTime The end time of the time range within which the alerts were triggered.
     * @return The alerts.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyAlert> listAlerts(
        String alertConfigurationId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return listAlerts(alertConfigurationId, startTime, endTime, null);
    }

    /**
     * Fetch the alerts triggered by an anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-OffsetDateTime-OffsetDateTime-ListAlertOptions -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;
     * final ListAlertOptions options = new ListAlertOptions&#40;&#41;
     *     .setAlertQueryTimeMode&#40;timeMode&#41;
     *     .setMaxPageSize&#40;10&#41;;
     *
     * metricsAdvisorAsyncClient.listAlerts&#40;alertConfigurationId, startTime, endTime, options&#41;
     *     .subscribe&#40;alert -&gt; &#123;
     *         System.out.printf&#40;&quot;Anomaly Alert Id: %s%n&quot;, alert.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Created Time: %s%n&quot;, alert.getCreatedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Modified Time: %s%n&quot;, alert.getModifiedTime&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-OffsetDateTime-OffsetDateTime-ListAlertOptions -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param startTime The start time of the time range within which the alerts were triggered.
     * @param endTime The end time of the time range within which the alerts were triggered.
     * @param options The additional parameters.
     * @return The alerts.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyAlert> listAlerts(
        String alertConfigurationId, OffsetDateTime startTime, OffsetDateTime endTime, ListAlertOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listAlertsSinglePageAsync(alertConfigurationId, startTime, endTime,
                        options, context)),
                continuationToken ->
                    withContext(context -> listAlertsNextPageAsync(continuationToken,
                        startTime, endTime,
                        options,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<AnomalyAlert> listAlerts(
        String alertConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAlertOptions options, Context context) {
        return new PagedFlux<>(() ->
            listAlertsSinglePageAsync(alertConfigurationId, startTime, endTime, options, context),
            continuationToken ->
                listAlertsNextPageAsync(continuationToken, startTime, endTime, options, context));
    }

    private Mono<PagedResponse<AnomalyAlert>> listAlertsSinglePageAsync(
        String alertConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAlertOptions options,
        Context context) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");

        if (options == null) {
            options = new ListAlertOptions();
        }
        AlertingResultQuery query = new AlertingResultQuery();
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setTimeMode(options.getTimeMode());

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getAlertsByAnomalyAlertingConfigurationSinglePageAsync(
            UUID.fromString(alertConfigurationId),
            query,
            options.getSkip(),
            options.getMaxPageSize(),
            withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing alerts"))
            .doOnSuccess(response -> logger.info("Listed alerts {}", response))
            .doOnError(error -> logger.warning("Failed to list the alerts", error));
    }

    private Mono<PagedResponse<AnomalyAlert>> listAlertsNextPageAsync(
        String nextPageLink,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAlertOptions options,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }

        AlertingResultQuery query = new AlertingResultQuery();
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setTimeMode(options.getTimeMode());

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getAlertsByAnomalyAlertingConfigurationNextSinglePageAsync(nextPageLink,
            query,
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error));
    }

    /**
     * Fetch the anomalies in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final String alertId = &quot;1746b031c00&quot;;
     *
     * metricsAdvisorAsyncClient.listAnomaliesForAlert&#40;
     *     alertConfigurationId,
     *     alertId&#41;
     *     .subscribe&#40;anomaly -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, anomaly.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, anomaly.getDetectionConfigurationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Created Time: %s%n&quot;, anomaly.getCreatedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Modified Time: %s%n&quot;, anomaly.getModifiedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly AnomalySeverity: %s%n&quot;, anomaly.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Status: %s%n&quot;, anomaly.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         DimensionKey seriesKey = anomaly.getSeriesKey&#40;&#41;;
     *         for &#40;Map.Entry&lt;String, String&gt; dimension : seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;DimensionName: %s DimensionValue:%s%n&quot;,
     *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     *
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     *     conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataPointAnomaly> listAnomaliesForAlert(
        String alertConfigurationId,
        String alertId) {
        return listAnomaliesForAlert(alertConfigurationId, alertId, null);
    }

    /**
     * Fetch the anomalies in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final String alertId = &quot;1746b031c00&quot;;
     * final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;;
     * metricsAdvisorAsyncClient.listAnomaliesForAlert&#40;
     *     alertConfigurationId,
     *     alertId,
     *     options&#41;
     *     .subscribe&#40;anomaly -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, anomaly.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, anomaly.getDetectionConfigurationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Created Time: %s%n&quot;, anomaly.getCreatedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Modified Time: %s%n&quot;, anomaly.getModifiedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly AnomalySeverity: %s%n&quot;, anomaly.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Status: %s%n&quot;, anomaly.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;anomaly.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     * @param options The additional parameters.
     *
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     *     conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataPointAnomaly> listAnomaliesForAlert(
        String alertConfigurationId,
        String alertId,
        ListAnomaliesAlertedOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listAnomaliesForAlertSinglePageAsync(alertConfigurationId, alertId, options, context)),
                continuationToken ->
                    withContext(context -> listAnomaliesForAlertNextPageAsync(continuationToken,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<DataPointAnomaly> listAnomaliesForAlert(
        String alertConfigurationId,
        String alertId,
        ListAnomaliesAlertedOptions options,
        Context context) {
        return new PagedFlux<>(() ->
            listAnomaliesForAlertSinglePageAsync(alertConfigurationId, alertId, options, context),
            continuationToken ->
                listAnomaliesForAlertNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<DataPointAnomaly>> listAnomaliesForAlertSinglePageAsync(
        String alertConfigurationId,
        String alertId,
        ListAnomaliesAlertedOptions options,
        Context context) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");
        Objects.requireNonNull(alertId, "'alertId' is required.");

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getAnomaliesFromAlertByAnomalyAlertingConfigurationSinglePageAsync(
            UUID.fromString(alertConfigurationId),
            alertId,
            options == null ? null : options.getSkip(),
            options == null ? null : options.getMaxPageSize(),
            withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing anomalies for alert"))
            .doOnSuccess(response -> logger.info("Listed anomalies {}", response))
            .doOnError(error -> logger.warning("Failed to list the anomalies for alert", error))
            .map(response -> AnomalyTransforms.fromInnerPagedResponse(response));
    }

    private Mono<PagedResponse<DataPointAnomaly>> listAnomaliesForAlertNextPageAsync(
        String nextPageLink,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getAnomaliesFromAlertByAnomalyAlertingConfigurationNextSinglePageAsync(nextPageLink,
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {} {}",
                nextPageLink,
                response))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(response -> AnomalyTransforms.fromInnerPagedResponse(response));
    }


    /**
     * Fetch the incidents in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final String alertId = &quot;1746b031c00&quot;;
     * final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;;
     *
     * metricsAdvisorAsyncClient.listIncidentsForAlert&#40;
     *     alertConfigurationId,
     *     alertId,
     *     options&#41;
     *     .subscribe&#40;incident -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, incident.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, incident.getDetectionConfigurationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Id: %s%n&quot;, incident.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Start Time: %s%n&quot;, incident.getStartTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident AnomalySeverity: %s%n&quot;, incident.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Status: %s%n&quot;, incident.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Root DataFeedDimension Key:&quot;&#41;;
     *         DimensionKey rootDimension = incident.getRootDimensionKey&#40;&#41;;
     *         for &#40;Map.Entry&lt;String, String&gt; dimension : rootDimension.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;DimensionName: %s DimensionValue:%s%n&quot;,
     *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     *     conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyIncident> listIncidentsForAlert(
        String alertConfigurationId,
        String alertId) {
        return listIncidentsForAlert(alertConfigurationId, alertId, null);
    }

    /**
     * Fetch the incidents in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final String alertId = &quot;1746b031c00&quot;;
     * final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;;
     *
     * metricsAdvisorAsyncClient.listIncidentsForAlert&#40;
     *     alertConfigurationId,
     *     alertId,
     *     options&#41;
     *     .subscribe&#40;incident -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, incident.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, incident.getDetectionConfigurationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Id: %s%n&quot;, incident.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Start Time: %s%n&quot;, incident.getStartTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident AnomalySeverity: %s%n&quot;, incident.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Status: %s%n&quot;, incident.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Root DataFeedDimension Key:&quot;&#41;;
     *         DimensionKey rootDimension = incident.getRootDimensionKey&#40;&#41;;
     *         for &#40;Map.Entry&lt;String, String&gt; dimension : rootDimension.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;DimensionName: %s DimensionValue:%s%n&quot;,
     *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     * @param options The additional parameters.
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     *     conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyIncident> listIncidentsForAlert(
        String alertConfigurationId,
        String alertId,
        ListIncidentsAlertedOptions options) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listIncidentsForAlertSinglePageAsync(alertConfigurationId, alertId, options, context)),
                continuationToken ->
                    withContext(context -> listIncidentsForAlertNextPageAsync(continuationToken,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<AnomalyIncident> listIncidentsForAlert(
        String alertConfigurationId,
        String alertId,
        ListIncidentsAlertedOptions options, Context context) {
        return new PagedFlux<>(() ->
            listIncidentsForAlertSinglePageAsync(alertConfigurationId, alertId, options, context),
            continuationToken ->
                listIncidentsForAlertNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<AnomalyIncident>> listIncidentsForAlertSinglePageAsync(
        String alertConfigurationId,
        String alertId,
        ListIncidentsAlertedOptions options, Context context) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");
        Objects.requireNonNull(alertId, "'alertId' is required.");

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getIncidentsFromAlertByAnomalyAlertingConfigurationSinglePageAsync(
            UUID.fromString(alertConfigurationId),
            alertId,
            options == null ? null : options.getSkip(),
            options == null ? null : options.getMaxPageSize(),
            withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing incidents for alert"))
            .doOnSuccess(response -> logger.info("Listed incidents {}", response))
            .doOnError(error -> logger.warning("Failed to list the incidents for alert", error))
            .map(response -> IncidentTransforms.fromInnerPagedResponse(response));
    }

    private Mono<PagedResponse<AnomalyIncident>> listIncidentsForAlertNextPageAsync(
        String nextPageLink,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.getIncidentsFromAlertByAnomalyAlertingConfigurationNextSinglePageAsync(nextPageLink,
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(response -> IncidentTransforms.fromInnerPagedResponse(response));
    }

    /**
     * Create a new metric feedback.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.addFeedback#String-MetricFeedback -->
     * <pre>
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final MetricChangePointFeedback metricChangePointFeedback
     *     = new MetricChangePointFeedback&#40;startTime, endTime, ChangePointValue.AUTO_DETECT&#41;;
     *
     * metricsAdvisorAsyncClient.addFeedback&#40;metricId, metricChangePointFeedback&#41;
     *     .subscribe&#40;metricFeedback -&gt; &#123;
     *         MetricChangePointFeedback createdMetricChangePointFeedback = &#40;MetricChangePointFeedback&#41; metricFeedback;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, createdMetricChangePointFeedback.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback change point value: %s%n&quot;,
     *             createdMetricChangePointFeedback.getChangePointValue&#40;&#41;.toString&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback start time: %s%n&quot;,
     *             createdMetricChangePointFeedback.getStartTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback end time: %s%n&quot;,
     *             createdMetricChangePointFeedback.getEndTime&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.addFeedback#String-MetricFeedback -->
     *
     * @param metricId the unique id for which the feedback needs to be submitted.
     * @param metricFeedback the actual metric feedback.
     *
     * @return A {@link Mono} containing the created {@link MetricFeedback metric feedback}.
     * @throws NullPointerException If {@code metricId}, {@code metricFeedback.dimensionFilter} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MetricFeedback> addFeedback(String metricId, MetricFeedback metricFeedback) {
        return addFeedbackWithResponse(metricId, metricFeedback).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a new metric feedback.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.addFeedbackWithResponse#String-MetricFeedback -->
     * <pre>
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final MetricChangePointFeedback metricChangePointFeedback
     *     = new MetricChangePointFeedback&#40;startTime, endTime, ChangePointValue.AUTO_DETECT&#41;;
     *
     * metricsAdvisorAsyncClient.addFeedbackWithResponse&#40;metricId, metricChangePointFeedback&#41;
     *     .subscribe&#40;metricFeedbackResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback creation operation status %s%n&quot;,
     *             metricFeedbackResponse.getStatusCode&#40;&#41;&#41;;
     *         MetricChangePointFeedback createdMetricChangePointFeedback
     *             = &#40;MetricChangePointFeedback&#41; metricFeedbackResponse.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, createdMetricChangePointFeedback.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback change point value: %s%n&quot;,
     *             createdMetricChangePointFeedback.getChangePointValue&#40;&#41;.toString&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback start time: %s%n&quot;,
     *             createdMetricChangePointFeedback.getStartTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback end time: %s%n&quot;,
     *             createdMetricChangePointFeedback.getEndTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback associated dimension filter: %s%n&quot;,
     *             createdMetricChangePointFeedback.getDimensionFilter&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.addFeedbackWithResponse#String-MetricFeedback -->
     *
     * @param metricId the unique id for which the feedback needs to be submitted.
     * @param metricFeedback the actual metric feedback.
     *
     * @return A {@link Response} of a {@link Mono} containing the created {@link MetricFeedback metric feedback}.
     * @throws NullPointerException If {@code metricId}, {@code metricFeedback},
     * {@code metricFeedback.dimensionFilter} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricFeedback>> addFeedbackWithResponse(String metricId,
        MetricFeedback metricFeedback) {
        try {
            return withContext(context -> addFeedbackWithResponse(metricId, metricFeedback, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<MetricFeedback>> addFeedbackWithResponse(String metricId, MetricFeedback metricFeedback,
        Context context) {
        Objects.requireNonNull(metricId, "'metricId' is required.");
        Objects.requireNonNull(metricFeedback, "'metricFeedback' is required.");
        Objects.requireNonNull(metricFeedback.getDimensionFilter(),
            "'metricFeedback.dimensionFilter' is required.");

        com.azure.ai.metricsadvisor.implementation.models.MetricFeedback innerMetricFeedback;
        if (metricFeedback instanceof MetricAnomalyFeedback) {
            MetricAnomalyFeedback metricAnomalyFeedback = (MetricAnomalyFeedback) metricFeedback;
            Objects.requireNonNull(metricAnomalyFeedback.getStartTime(),
                "'metricFeedback.startTime' is required.");
            Objects.requireNonNull(metricAnomalyFeedback.getEndTime(),
                "'metricFeedback.endTime' is required.");
            Objects.requireNonNull(metricAnomalyFeedback.getAnomalyValue(),
                "'metricFeedback.anomalyValue' is required.");

            AnomalyFeedback innerAnomalyFeedback = new AnomalyFeedback()
                .setStartTime(metricAnomalyFeedback.getStartTime())
                .setEndTime(metricAnomalyFeedback.getEndTime())
                .setValue(new AnomalyFeedbackValue().setAnomalyValue(metricAnomalyFeedback.getAnomalyValue()));

            if (metricAnomalyFeedback.getDetectionConfiguration() != null) {
                innerAnomalyFeedback
                    .setAnomalyDetectionConfigurationId(
                        UUID.fromString(metricAnomalyFeedback.getDetectionConfiguration().getId()))
                    .setAnomalyDetectionConfigurationSnapshot(
                        DetectionConfigurationTransforms.toInnerForCreate(logger, metricId,
                            metricAnomalyFeedback.getDetectionConfiguration()));
            }
            innerMetricFeedback = innerAnomalyFeedback
                .setMetricId(UUID.fromString(metricId))
                .setDimensionFilter(new FeedbackDimensionFilter()
                    .setDimension(metricAnomalyFeedback.getDimensionFilter().asMap()));
        } else if (metricFeedback instanceof MetricChangePointFeedback) {
            MetricChangePointFeedback metricChangePointFeedback = (MetricChangePointFeedback) metricFeedback;
            Objects.requireNonNull(metricChangePointFeedback.getStartTime(),
                "'metricFeedback.startTime' is required.");
            Objects.requireNonNull(metricChangePointFeedback.getEndTime(),
                "'metricFeedback.endTime' is required.");
            Objects.requireNonNull(metricChangePointFeedback.getChangePointValue(),
                "'metricFeedback.changePointValue' is required.");
            innerMetricFeedback = new ChangePointFeedback()
                .setStartTime(metricChangePointFeedback.getStartTime())
                .setEndTime(metricChangePointFeedback.getEndTime())
                .setValue(new ChangePointFeedbackValue()
                    .setChangePointValue(metricChangePointFeedback.getChangePointValue()))
                .setMetricId(UUID.fromString(metricId))
                .setDimensionFilter(new FeedbackDimensionFilter()
                    .setDimension(metricChangePointFeedback.getDimensionFilter().asMap()));

        } else if (metricFeedback instanceof MetricPeriodFeedback) {
            MetricPeriodFeedback metricPeriodFeedback = (MetricPeriodFeedback) metricFeedback;
            Objects.requireNonNull(metricPeriodFeedback.getPeriodType(),
                "'metricFeedback.periodType' is required.");
            Objects.requireNonNull(metricPeriodFeedback.getPeriodValue(),
                "'metricFeedback.periodValue' is required.");
            innerMetricFeedback = new PeriodFeedback()
                .setValue(new PeriodFeedbackValue().setPeriodValue(metricPeriodFeedback.getPeriodValue())
                    .setPeriodType(metricPeriodFeedback.getPeriodType()))
                .setMetricId(UUID.fromString(metricId))
                .setDimensionFilter(new FeedbackDimensionFilter()
                    .setDimension(metricPeriodFeedback.getDimensionFilter().asMap()));
        } else if (metricFeedback instanceof MetricCommentFeedback) {
            MetricCommentFeedback metricCommentFeedback = (MetricCommentFeedback) metricFeedback;
            Objects.requireNonNull(metricCommentFeedback.getComment(),
                "'metricFeedback.comment' is required.");
            innerMetricFeedback = new CommentFeedback()
                .setStartTime(metricCommentFeedback.getStartTime())
                .setEndTime(metricCommentFeedback.getEndTime())
                .setValue(new CommentFeedbackValue().setCommentValue(metricCommentFeedback.getComment()))
                .setMetricId(UUID.fromString(metricId))
                .setDimensionFilter(new FeedbackDimensionFilter()
                    .setDimension(metricCommentFeedback.getDimensionFilter().asMap()));
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException("Unknown feedback type."));
        }
        return service.createMetricFeedbackWithResponseAsync(innerMetricFeedback, context)
            .flatMap(createdMetricFeedbackResponse ->
                getFeedbackWithResponse(parseOperationId(createdMetricFeedbackResponse
                    .getDeserializedHeaders().getLocation())));
    }

    /**
     * Get a metric feedback by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getFeedback#String -->
     * <pre>
     *
     * final String feedbackId = &quot;8i3h4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * metricsAdvisorAsyncClient.getFeedback&#40;feedbackId&#41;
     *     .subscribe&#40;metricFeedback -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, metricFeedback.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback associated dimension filter: %s%n&quot;,
     *             metricFeedback.getDimensionFilter&#40;&#41;.asMap&#40;&#41;&#41;;
     *
     *         if &#40;PERIOD.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *             MetricPeriodFeedback createMetricPeriodFeedback
     *                 = &#40;MetricPeriodFeedback&#41; metricFeedback;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback type: %s%n&quot;,
     *                 createMetricPeriodFeedback.getPeriodType&#40;&#41;.toString&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback period value: %d%n&quot;,
     *                 createMetricPeriodFeedback.getPeriodValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getFeedback#String -->
     *
     * @param feedbackId The metric feedback unique id.
     *
     * @return The metric feedback for the provided id.
     * @throws IllegalArgumentException If {@code feedbackId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code feedbackId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MetricFeedback> getFeedback(String feedbackId) {
        return getFeedbackWithResponse(feedbackId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get a metric feedback by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getFeedbackWithResponse#String -->
     * <pre>
     *
     * final String feedbackId = &quot;8i3h4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * metricsAdvisorAsyncClient.getFeedbackWithResponse&#40;feedbackId&#41;
     *     .subscribe&#40;metricFeedbackResponse -&gt; &#123;
     *         final MetricFeedback metricFeedback = metricFeedbackResponse.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, metricFeedback.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback associated dimension filter: %s%n&quot;,
     *             metricFeedback.getDimensionFilter&#40;&#41;.asMap&#40;&#41;&#41;;
     *
     *         if &#40;PERIOD.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *             MetricPeriodFeedback createMetricPeriodFeedback
     *                 = &#40;MetricPeriodFeedback&#41; metricFeedback;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback type: %s%n&quot;,
     *                 createMetricPeriodFeedback.getPeriodType&#40;&#41;.toString&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback period value: %d%n&quot;,
     *                 createMetricPeriodFeedback.getPeriodValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getFeedbackWithResponse#String -->
     *
     * @param feedbackId The metric feedback unique id.
     *
     * @return The metric feedback for the provided id.
     * @throws IllegalArgumentException If {@code feedbackId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code feedbackId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MetricFeedback>> getFeedbackWithResponse(String feedbackId) {
        try {
            return withContext(context -> getFeedbackWithResponse(feedbackId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<MetricFeedback>> getFeedbackWithResponse(String feedbackId, Context context) {
        Objects.requireNonNull(feedbackId, "'feedbackId' is required.");
        return service.getMetricFeedbackWithResponseAsync(UUID.fromString(feedbackId), context)
            .map(metricFeedbackResponse -> new SimpleResponse<>(metricFeedbackResponse,
                MetricFeedbackTransforms.fromInner(metricFeedbackResponse.getValue())));
    }

    /**
     * List information of metrics feedback on the account for a metric Id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listFeedback#String -->
     * <pre>
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * metricsAdvisorAsyncClient.listFeedback&#40;metricId&#41;
     *     .subscribe&#40;metricFeedback -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, metricFeedback.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback associated dimension filter: %s%n&quot;,
     *             metricFeedback.getDimensionFilter&#40;&#41;.asMap&#40;&#41;&#41;;
     *
     *         if &#40;PERIOD.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *             MetricPeriodFeedback periodFeedback
     *                 = &#40;MetricPeriodFeedback&#41; metricFeedback;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback type: %s%n&quot;,
     *                 periodFeedback.getPeriodType&#40;&#41;.toString&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback period value: %d%n&quot;,
     *                 periodFeedback.getPeriodValue&#40;&#41;&#41;;
     *         &#125; else if &#40;ANOMALY.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *             MetricAnomalyFeedback metricAnomalyFeedback
     *                 = &#40;MetricAnomalyFeedback&#41; metricFeedback;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback anomaly value: %s%n&quot;,
     *                 metricAnomalyFeedback.getAnomalyValue&#40;&#41;.toString&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback associated detection configuration: %s%n&quot;,
     *                 metricAnomalyFeedback.getDetectionConfigurationId&#40;&#41;&#41;;
     *         &#125; else if &#40;COMMENT.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *             MetricCommentFeedback metricCommentFeedback
     *                 = &#40;MetricCommentFeedback&#41; metricFeedback;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback comment value: %s%n&quot;,
     *                 metricCommentFeedback.getComment&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listFeedback#String -->
     *
     * @param metricId the unique metric Id.
     *
     * @return A {@link PagedFlux} containing information of all the {@link MetricFeedback metric feedbacks}
     * in the account.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricFeedback> listFeedback(String metricId) {
        return listFeedback(metricId, null);
    }

    /**
     * List information of all metric feedbacks on the metrics advisor account for a metric Id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listFeedback#String-ListMetricFeedbackOptions -->
     * <pre>
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     *
     * metricsAdvisorAsyncClient.listFeedback&#40;metricId,
     *     new ListMetricFeedbackOptions&#40;&#41;
     *         .setFilter&#40;new ListMetricFeedbackFilter&#40;&#41;
     *             .setStartTime&#40;startTime&#41;
     *             .setTimeMode&#40;FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME&#41;
     *             .setEndTime&#40;endTime&#41;&#41;&#41;
     *     .subscribe&#40;metricFeedback -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, metricFeedback.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback associated dimension filter: %s%n&quot;,
     *             metricFeedback.getDimensionFilter&#40;&#41;.asMap&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric feedback created time %s%n&quot;, metricFeedback.getCreatedTime&#40;&#41;&#41;;
     *
     *         if &#40;PERIOD.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *             MetricPeriodFeedback periodFeedback
     *                 = &#40;MetricPeriodFeedback&#41; metricFeedback;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback type: %s%n&quot;,
     *                 periodFeedback.getPeriodType&#40;&#41;.toString&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback period value: %d%n&quot;,
     *                 periodFeedback.getPeriodValue&#40;&#41;&#41;;
     *         &#125; else if &#40;ANOMALY.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *             MetricAnomalyFeedback metricAnomalyFeedback
     *                 = &#40;MetricAnomalyFeedback&#41; metricFeedback;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback anomaly value: %s%n&quot;,
     *                 metricAnomalyFeedback.getAnomalyValue&#40;&#41;.toString&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback associated detection configuration: %s%n&quot;,
     *                 metricAnomalyFeedback.getDetectionConfigurationId&#40;&#41;&#41;;
     *         &#125; else if &#40;COMMENT.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *             MetricCommentFeedback metricCommentFeedback
     *                 = &#40;MetricCommentFeedback&#41; metricFeedback;
     *             System.out.printf&#40;&quot;Data Feed Metric feedback comment value: %s%n&quot;,
     *                 metricCommentFeedback.getComment&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listFeedback#String-ListMetricFeedbackOptions -->
     *
     * @param metricId the unique metric Id.
     * @param options The configurable {@link ListMetricFeedbackOptions options} to pass for filtering the output
     * result.
     *
     * @return A {@link PagedFlux} containing information of all the {@link MetricFeedback metric feedbacks} in
     * the account.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<MetricFeedback> listFeedback(
        String metricId,
        ListMetricFeedbackOptions options) {
        options = options != null ? options : new ListMetricFeedbackOptions();
        try {
            final MetricFeedbackFilter metricFeedbackFilter = MetricFeedbackTransforms.toInnerFilter(metricId, options);
            final ListMetricFeedbackOptions finalOptions = options;
            return new PagedFlux<>(() ->
                withContext(context ->
                    listMetricFeedbacksSinglePage(metricFeedbackFilter, finalOptions.getMaxPageSize(),
                        finalOptions.getSkip(), context)),
                continuationToken ->
                    withContext(context -> listMetricFeedbacksNextPage(continuationToken, metricFeedbackFilter,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<MetricFeedback> listFeedback(String metricId, ListMetricFeedbackOptions options, Context context) {
        options = options != null ? options : new ListMetricFeedbackOptions();
        final MetricFeedbackFilter metricFeedbackFilter = MetricFeedbackTransforms.toInnerFilter(metricId, options);
        final ListMetricFeedbackOptions finalOptions = options;

        return new PagedFlux<>(() ->
            listMetricFeedbacksSinglePage(metricFeedbackFilter, finalOptions.getMaxPageSize(),
                finalOptions.getSkip(), context),
            continuationToken ->
                listMetricFeedbacksNextPage(continuationToken, metricFeedbackFilter,
                    context));
    }

    private Mono<PagedResponse<MetricFeedback>> listMetricFeedbacksSinglePage(MetricFeedbackFilter metricFeedbackFilter,
        Integer maxPageSize, Integer skip, Context context) {
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.listMetricFeedbacksSinglePageAsync(metricFeedbackFilter, skip, maxPageSize, withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all metric feedbacks"))
            .doOnSuccess(response -> logger.info("Listed metric feedbacks - {}", response))
            .doOnError(error -> logger.warning("Failed to list all metric feedbacks information", error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream().map(MetricFeedbackTransforms::fromInner).collect(Collectors.toList()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<MetricFeedback>> listMetricFeedbacksNextPage(String nextPageLink,
        MetricFeedbackFilter metricFeedbackFilter, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.listMetricFeedbacksNextSinglePageAsync(nextPageLink, metricFeedbackFilter, withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream().map(MetricFeedbackTransforms::fromInner).collect(Collectors.toList()),
                res.getContinuationToken(),
                null));
    }

}
