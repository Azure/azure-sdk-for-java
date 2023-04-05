// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
import com.azure.ai.metricsadvisor.implementation.MetricsAdvisorImpl;
import com.azure.ai.metricsadvisor.implementation.models.AlertingResultQuery;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyDimensionQuery;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyFeedback;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyFeedbackValue;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyResult;
import com.azure.ai.metricsadvisor.implementation.models.ChangePointFeedback;
import com.azure.ai.metricsadvisor.implementation.models.ChangePointFeedbackValue;
import com.azure.ai.metricsadvisor.implementation.models.CommentFeedback;
import com.azure.ai.metricsadvisor.implementation.models.CommentFeedbackValue;
import com.azure.ai.metricsadvisor.implementation.models.CreateMetricFeedbackResponse;
import com.azure.ai.metricsadvisor.implementation.models.DetectionAnomalyResultQuery;
import com.azure.ai.metricsadvisor.implementation.models.DetectionIncidentResultQuery;
import com.azure.ai.metricsadvisor.implementation.models.DetectionSeriesQuery;
import com.azure.ai.metricsadvisor.implementation.models.EnrichmentStatusQueryOption;
import com.azure.ai.metricsadvisor.implementation.models.FeedbackDimensionFilter;
import com.azure.ai.metricsadvisor.implementation.models.IncidentResult;
import com.azure.ai.metricsadvisor.implementation.models.MetricDataList;
import com.azure.ai.metricsadvisor.implementation.models.MetricDataQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.MetricDimensionQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.MetricFeedbackFilter;
import com.azure.ai.metricsadvisor.implementation.models.MetricSeriesItem;
import com.azure.ai.metricsadvisor.implementation.models.MetricSeriesQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.PeriodFeedback;
import com.azure.ai.metricsadvisor.implementation.models.PeriodFeedbackValue;
import com.azure.ai.metricsadvisor.implementation.models.RootCauseList;
import com.azure.ai.metricsadvisor.implementation.models.SeriesIdentity;
import com.azure.ai.metricsadvisor.implementation.models.SeriesResultList;
import com.azure.ai.metricsadvisor.implementation.models.TimeMode;
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
import com.azure.ai.metricsadvisor.models.MetricsAdvisorResponseException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.implementation.util.Utility.getEnrichmentStatusQueryOptions;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.getListAnomaliesDetectedOptions;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.getListAnomalyDimensionValuesOptions;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.getListIncidentsDetectedOptions;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.getMetricDataQueryOptions;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.getMetricDimensionQueryOptions;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.getMetricSeriesQueryOptions;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.parseOperationId;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.toStringOrNull;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateActiveSinceInput;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateAddFeedbackInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateAnomalyDimensionValuesInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateAnomalyIncidentRootCausesInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateIncidentsForDetectionConfigInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateListAlertsInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateListAnomaliesInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateMetricEnrichedSeriesInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateMetricEnrichmentStatusInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateMetricSeriesInputs;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.validateStartEndTime;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Metrics Advisor.
 *
 * <p><strong>Instantiating an synchronous DataFeedMetric Advisor Client</strong></p>
 * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation -->
 * <pre>
 * MetricsAdvisorClient metricsAdvisorClient =
 *     new MetricsAdvisorClientBuilder&#40;&#41;
 *         .credential&#40;new MetricsAdvisorKeyCredential&#40;&quot;&#123;subscription_key&#125;&quot;, &quot;&#123;api_key&#125;&quot;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation -->
 *
 * @see MetricsAdvisorClientBuilder
 */
@ServiceClient(builder = MetricsAdvisorClientBuilder.class)
public final class MetricsAdvisorClient {
    final ClientLogger logger = new ClientLogger(MetricsAdvisorClient.class);

    private final MetricsAdvisorImpl service;

    /**
     * Create a {@link MetricsAdvisorClient client} that sends requests to the Metrics Advisor service's
     * endpoint.
     * Each service call goes through the {@link MetricsAdvisorAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * client routes its request through.
     * @param serviceVersion The versions of Azure Metrics Advisor supported by this client library.
     */
    MetricsAdvisorClient(MetricsAdvisorImpl service,
                         MetricsAdvisorServiceVersion serviceVersion) {
        this.service = service;
    }
    /**
     * List series (dimension combinations) from metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-OffsetDateTime -->
     * <pre>
     * final OffsetDateTime activeSince = OffsetDateTime.parse&#40;&quot;2020-07-10T00:00:00Z&quot;&#41;;
     * metricsAdvisorClient.listMetricSeriesDefinitions&#40;
     *     &quot;metricId&quot;,
     *     activeSince&#41;
     *     .forEach&#40;metricSeriesDefinition -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric id for the retrieved series definition : %s%n&quot;,
     *             metricSeriesDefinition.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric dimension: %s%n&quot;, metricSeriesDefinition.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-OffsetDateTime -->
     *
     * @param metricId metric unique id.
     * @param activeSince the start time for querying series ingested after this time.
     * @return A {@link PagedIterable} of the {@link MetricSeriesDefinition metric series definitions}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code activeSince}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricSeriesDefinition> listMetricSeriesDefinitions(
        String metricId, OffsetDateTime activeSince) {
        return listMetricSeriesDefinitions(metricId, activeSince, null, Context.NONE);
    }

    /**
     * List series (dimension combinations) from metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-OffsetDateTime-ListMetricSeriesDefinitionOptions-Context -->
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
     * metricsAdvisorClient.listMetricSeriesDefinitions&#40;metricId, activeSince, options, Context.NONE&#41;
     *     .forEach&#40;metricSeriesDefinition -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric id for the retrieved series definition : %s%n&quot;,
     *             metricSeriesDefinition.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;metricSeriesDefinition.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-OffsetDateTime-ListMetricSeriesDefinitionOptions-Context -->
     *
     * @param metricId metric unique id.
     * @param activeSince the start time for querying series ingested after this time.
     * @param options the additional filtering attributes that can be provided to query the series.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} of the {@link MetricSeriesDefinition metric series definitions}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code activeSince}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricSeriesDefinition> listMetricSeriesDefinitions(String metricId,
        OffsetDateTime activeSince, ListMetricSeriesDefinitionOptions options, Context context) {
        return listMetricSeriesDefinitionsSync(metricId, activeSince, options, context);
    }

    private PagedIterable<MetricSeriesDefinition> listMetricSeriesDefinitionsSync(String metricId,
                                                                  OffsetDateTime activeSince, ListMetricSeriesDefinitionOptions options, Context context) {
        return new PagedIterable<>(() -> listMetricSeriesDefinitionSinglePageSync(metricId, activeSince, options,
            context),
            continuationToken -> listMetricSeriesDefinitionNextPageSync(continuationToken, activeSince, options,
                context));
    }

    private PagedResponse<MetricSeriesDefinition> listMetricSeriesDefinitionSinglePageSync(String metricId,
                                                                                                  OffsetDateTime activeSince, ListMetricSeriesDefinitionOptions options, Context context) {

        validateActiveSinceInput(activeSince, logger);

        if (options == null) {
            options = new ListMetricSeriesDefinitionOptions();
        }

        final MetricSeriesQueryOptions metricSeriesQueryOptions =
            getMetricSeriesQueryOptions(activeSince, options);

        PagedResponse<MetricSeriesItem> res =
            service.getMetricSeriesSinglePage(UUID.fromString(metricId), metricSeriesQueryOptions,
                options.getSkip(), options.getMaxPageSize(), context);
        return MetricSeriesDefinitionTransforms.fromInnerResponse(res);
    }

    private PagedResponse<MetricSeriesDefinition> listMetricSeriesDefinitionNextPageSync(String nextPageLink,
                                                                                         OffsetDateTime activeSince, ListMetricSeriesDefinitionOptions options, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        validateActiveSinceInput(activeSince, logger);

        if (options == null) {
            options = new ListMetricSeriesDefinitionOptions();
        }
        final MetricSeriesQueryOptions metricSeriesQueryOptions =
            getMetricSeriesQueryOptions(activeSince, options);

        PagedResponse<MetricSeriesItem> res
            = service.getMetricSeriesNextSinglePage(nextPageLink, metricSeriesQueryOptions, context);
        return MetricSeriesDefinitionTransforms.fromInnerResponse(res);
    }

    /**
     * Get time series data from metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     *
     * metricsAdvisorClient.listMetricSeriesData&#40;&quot;metricId&quot;,
     *     Arrays.asList&#40;new DimensionKey&#40;new HashMap&lt;String, String&gt;&#40;&#41; &#123;&#123;
     *             put&#40;&quot;Dim1&quot;, &quot;value1&quot;&#41;;
     *         &#125;&#125;&#41;&#41;, startTime, endTime&#41;
     *     .forEach&#40;metricSeriesData -&gt; &#123;
     *         System.out.println&#40;&quot;List of data points for this series:&quot;&#41;;
     *         System.out.println&#40;metricSeriesData.getMetricValues&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Timestamps of the data related to this time series:&quot;&#41;;
     *         System.out.println&#40;metricSeriesData.getTimestamps&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;metricSeriesData.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
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
     * @return A {@link PagedIterable} of the {@link MetricSeriesData metric series data points}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws NullPointerException thrown if the {@code metricId}, {@code startTime} or {@code endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricSeriesData> listMetricSeriesData(String metricId,
        List<DimensionKey> seriesKeys, OffsetDateTime startTime, OffsetDateTime endTime) {
        return listMetricSeriesData(metricId, seriesKeys, startTime, endTime, Context.NONE);
    }

    /**
     * Get time series data from metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime-Context -->
     * <pre>
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     * metricsAdvisorClient.listMetricSeriesData&#40;&quot;metricId&quot;,
     *     Arrays.asList&#40;new DimensionKey&#40;new HashMap&lt;String, String&gt;&#40;&#41; &#123;&#123;
     *             put&#40;&quot;Dim1&quot;, &quot;value1&quot;&#41;;
     *         &#125;&#125;&#41;&#41;, startTime, endTime&#41;
     *     .forEach&#40;metricSeriesData -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, metricSeriesData.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, metricSeriesData.getSeriesKey&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %.2f%n&quot;, metricSeriesData.getTimestamps&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %.2f%n&quot;, metricSeriesData.getMetricValues&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime-Context -->
     *
     * @param metricId metric unique id.
     * @param seriesKeys the series key to filter.
     * <p>This enables additional filtering of dimension values being queried.
     * For example, let's say we've the dimensions 'category' and 'city',
     * so the api can query value of the dimension 'category', with series key as 'city=redmond'.
     * </p>
     * @param startTime The start time for querying the time series data.
     * @param endTime The end time for querying the time series data.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} of the {@link MetricSeriesData metric series data points}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws NullPointerException thrown if the {@code metricId}, {@code startTime} or {@code endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricSeriesData> listMetricSeriesData(String metricId, List<DimensionKey> seriesKeys,
        OffsetDateTime startTime, OffsetDateTime endTime, Context context) {
        return listMetricSeriesDataSync(metricId, seriesKeys, startTime, endTime, context);
    }

    private PagedIterable<MetricSeriesData> listMetricSeriesDataSync(String metricId, List<DimensionKey> seriesKeys,
                                                     OffsetDateTime startTime, OffsetDateTime endTime, Context context) {
        return new PagedIterable<>(() -> listMetricSeriesDataInternal(metricId, seriesKeys, startTime, endTime, context),
            null);
    }

    private PagedResponse<MetricSeriesData> listMetricSeriesDataInternal(String metricId,
                                                                         List<DimensionKey> seriesKeys, OffsetDateTime startTime,
                                                                         OffsetDateTime endTime, Context context) {
        validateMetricSeriesInputs(metricId, seriesKeys, startTime, endTime, logger);

        List<Map<String, String>> dimensionList =
            seriesKeys.stream().map(DimensionKey::asMap).collect(Collectors.toList());
        final MetricDataQueryOptions metricDataQueryOptions =
            getMetricDataQueryOptions(startTime, dimensionList);

        Response<MetricDataList> response =
            service.getMetricDataWithResponse(UUID.fromString(metricId), metricDataQueryOptions, context);
        return MetricSeriesDataTransforms.fromInnerResponse(response);
    }

    /**
     * List the enrichment status for a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     *
     * metricsAdvisorClient.listMetricEnrichmentStatus&#40;metricId, startTime, endTime&#41;
     *     .forEach&#40;enrichmentStatus -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status : %s%n&quot;, enrichmentStatus.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status message: %s%n&quot;, enrichmentStatus.getMessage&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status data slice timestamp : %s%n&quot;,
     *             enrichmentStatus.getTimestamp&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param metricId metric unique id.
     * @param startTime The start time for querying the time series data.
     * @param endTime The end time for querying the time series data.
     * @return the list of enrichment status's for the specified metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if {@code metricId}, {@code startTime} and {@code endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<EnrichmentStatus> listMetricEnrichmentStatus(
        String metricId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return listMetricEnrichmentStatus(metricId, startTime, endTime, null, Context.NONE);
    }

    /**
     * List the enrichment status for a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime-ListMetricEnrichmentStatusOptions-Context -->
     * <pre>
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final ListMetricEnrichmentStatusOptions options = new ListMetricEnrichmentStatusOptions&#40;&#41;.setMaxPageSize&#40;10&#41;;
     *
     * metricsAdvisorClient.listMetricEnrichmentStatus&#40;metricId, startTime, endTime, options, Context.NONE&#41;
     *     .forEach&#40;enrichmentStatus -&gt; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status : %s%n&quot;, enrichmentStatus.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status message: %s%n&quot;, enrichmentStatus.getMessage&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data Feed Metric enrichment status data slice timestamp : %s%n&quot;,
     *             enrichmentStatus.getTimestamp&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime-ListMetricEnrichmentStatusOptions-Context -->
     *
     * @param metricId metric unique id.
     * @param startTime The start time for querying the time series data.
     * @param endTime The end time for querying the time series data.
     * @param options the additional configurable options to specify when querying the result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the list of enrichment status's for the specified metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if {@code metricId}, {@code startTime} and {@code endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<EnrichmentStatus> listMetricEnrichmentStatus(
        String metricId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListMetricEnrichmentStatusOptions options, Context context) {
        return listMetricEnrichmentStatusSync(metricId, startTime, endTime, options, context);
    }

    private PagedIterable<EnrichmentStatus> listMetricEnrichmentStatusSync(
        String metricId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListMetricEnrichmentStatusOptions options, Context context) {
        return new PagedIterable<>(() -> listMetricEnrichmentStatusSinglePageSync(metricId, startTime, endTime,
            options, context),
            continuationToken -> listMetricEnrichmentStatusNextPageSync(continuationToken, startTime, endTime,
                context));
    }

    private PagedResponse<EnrichmentStatus> listMetricEnrichmentStatusSinglePageSync(String metricId,
                                                                                            OffsetDateTime startTime, OffsetDateTime endTime, ListMetricEnrichmentStatusOptions options, Context context) {
        validateMetricEnrichmentStatusInputs(metricId, "'metricId' is required.", startTime, endTime);
        if (options == null) {
            options = new ListMetricEnrichmentStatusOptions();
        }
        final EnrichmentStatusQueryOption enrichmentStatusQueryOption =
            getEnrichmentStatusQueryOptions(startTime, endTime);

        PagedResponse<EnrichmentStatus> res = service.getEnrichmentStatusByMetricSinglePage(
            UUID.fromString(metricId),
            enrichmentStatusQueryOption,
            options.getSkip(),
            options.getMaxPageSize(),
            context);
        return new PagedResponseBase<>(
            res.getRequest(),
            res.getStatusCode(),
            res.getHeaders(),
            res.getValue(),
            res.getContinuationToken(),
            null);
    }
    private PagedResponse<EnrichmentStatus> listMetricEnrichmentStatusNextPageSync(String nextPageLink,
                                                                                   OffsetDateTime startTime, OffsetDateTime endTime, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        validateStartEndTime(startTime, endTime);

        final EnrichmentStatusQueryOption enrichmentStatusQueryOption =
            getEnrichmentStatusQueryOptions(startTime, endTime);

        PagedResponse<EnrichmentStatus> res =
            service.getEnrichmentStatusByMetricNextSinglePage(nextPageLink, enrichmentStatusQueryOption,
                context);
        return new PagedResponseBase<>(
            res.getRequest(),
            res.getStatusCode(),
            res.getHeaders(),
            res.getValue(),
            res.getContinuationToken(),
            null);
    }

    /**
     * Given a list of time series keys, retrieve time series version enriched using
     * a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String detectionConfigurationId = &quot;e87d899d-a5a0-4259-b752-11aea34d5e34&quot;;
     * final DimensionKey seriesKey = new DimensionKey&#40;&#41;
     *     .put&#40;&quot;Dim1&quot;, &quot;Common Lime&quot;&#41;
     *     .put&#40;&quot;Dim2&quot;, &quot;Antelope&quot;&#41;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-08-12T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-12T00:00:00Z&quot;&#41;;
     *
     * PagedIterable&lt;MetricEnrichedSeriesData&gt; enrichedDataIterable
     *     = metricsAdvisorClient.listMetricEnrichedSeriesData&#40;detectionConfigurationId,
     *     Arrays.asList&#40;seriesKey&#41;,
     *     startTime,
     *     endTime&#41;;
     *
     * for &#40;MetricEnrichedSeriesData enrichedData : enrichedDataIterable&#41; &#123;
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
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
     *
     * @param detectionConfigurationId The id of the configuration used to enrich the time series
     * identified by the keys in {@code seriesKeys}.
     * @param seriesKeys The time series key list, each key identifies a specific time series.
     * @param startTime The start time.
     * @param endTime The end time.
     * @return The enriched time series.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation
     * or if {@code seriesKeys} is empty.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId}
     * or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricEnrichedSeriesData> listMetricEnrichedSeriesData(String detectionConfigurationId,
                                                                                List<DimensionKey> seriesKeys,
                                                                                OffsetDateTime startTime,
                                                                                OffsetDateTime endTime) {
        return listMetricEnrichedSeriesDataSync(detectionConfigurationId,
            seriesKeys,
            startTime,
            endTime,
            Context.NONE);
    }

    private PagedIterable<MetricEnrichedSeriesData> listMetricEnrichedSeriesDataSync(String detectionConfigurationId,
                                                                     List<DimensionKey> seriesKeys,
                                                                     OffsetDateTime startTime,
                                                                     OffsetDateTime endTime,
                                                                     Context context) {
        return new PagedIterable<>(() -> listMetricEnrichedSeriesDataInternal(detectionConfigurationId,
            seriesKeys,
            startTime, endTime, context), null);
    }

    private PagedResponse<MetricEnrichedSeriesData>
        listMetricEnrichedSeriesDataInternal(String detectionConfigurationId,
                                         List<DimensionKey> seriesKeys,
                                         OffsetDateTime startTime,
                                         OffsetDateTime endTime,
                                         Context context) {
        validateMetricEnrichedSeriesInputs(detectionConfigurationId, seriesKeys, startTime, endTime, logger);

        final List<SeriesIdentity> innerSeriesKeys = seriesKeys
            .stream()
            .map(seriesId -> new SeriesIdentity().setDimension(seriesId.asMap()))
            .collect(Collectors.toList());

        DetectionSeriesQuery query = new DetectionSeriesQuery()
            .setSeries(innerSeriesKeys)
            .setStartTime(startTime)
            .setEndTime(endTime);

        Response<SeriesResultList> res =
            service.getSeriesByAnomalyDetectionConfigurationWithResponse(
                UUID.fromString(detectionConfigurationId),
                query,
                context);
        return new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                MetricEnrichedSeriesDataTransformations.fromInnerList(res.getValue()),
                null,
                null);
    }

    /**
     * Given a list of time series keys, retrieve time series version enriched using
     * a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime-Context -->
     * <pre>
     * final String detectionConfigurationId = &quot;e87d899d-a5a0-4259-b752-11aea34d5e34&quot;;
     * final DimensionKey seriesKey = new DimensionKey&#40;&#41;
     *     .put&#40;&quot;Dim1&quot;, &quot;Common Lime&quot;&#41;
     *     .put&#40;&quot;Dim2&quot;, &quot;Antelope&quot;&#41;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-08-12T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-12T00:00:00Z&quot;&#41;;
     *
     * PagedIterable&lt;MetricEnrichedSeriesData&gt; enrichedDataIterable
     *     = metricsAdvisorClient.listMetricEnrichedSeriesData&#40;detectionConfigurationId,
     *     Arrays.asList&#40;seriesKey&#41;,
     *     startTime,
     *     endTime&#41;;
     *
     * Stream&lt;PagedResponse&lt;MetricEnrichedSeriesData&gt;&gt; enrichedDataPageStream
     *     = enrichedDataIterable.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * enrichedDataPageStream.forEach&#40;enrichedDataPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     IterableStream&lt;MetricEnrichedSeriesData&gt; pageElements = enrichedDataPage.getElements&#40;&#41;;
     *     for &#40;MetricEnrichedSeriesData enrichedData : pageElements&#41; &#123;
     *         System.out.printf&#40;&quot;Series Key %s%n:&quot;, enrichedData.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;List of data points for this series&quot;&#41;;
     *         System.out.println&#40;enrichedData.getMetricValues&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;Timestamps of the data related to this time series:&quot;&#41;;
     *         System.out.println&#40;enrichedData.getTimestamps&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;The expected values of the data points calculated by the smart detector:&quot;&#41;;
     *         System.out.println&#40;enrichedData.getExpectedMetricValues&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;The lower boundary values of the data points calculated by smart detector:&quot;&#41;;
     *         System.out.println&#40;enrichedData.getLowerBoundaryValues&#40;&#41;&#41;;
     *         System.out.println&#40;&quot;the periods calculated for the data points in the time series:&quot;&#41;;
     *         System.out.println&#40;enrichedData.getPeriods&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime-Context -->
     *
     * @param detectionConfigurationId The id of the configuration used to enrich the time series
     * identified by the keys in {@code seriesKeys}.
     * @param seriesKeys The time series key list, each key identifies a specific time series.
     * @param startTime The start time of the time range within which the enriched data is returned.
     * @param endTime The end time of the time range within which the enriched data is returned.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The enriched time series.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation
     * or if {@code seriesKeys} is empty.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId}
     * or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricEnrichedSeriesData> listMetricEnrichedSeriesData(
        String detectionConfigurationId,
        List<DimensionKey> seriesKeys,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Context context) {
        return listMetricEnrichedSeriesDataSync(detectionConfigurationId,
            seriesKeys,
            startTime,
            endTime,
            context);
    }

    /**
     * Fetch the anomalies identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     * final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter&#40;&#41;
     *     .setSeverityRange&#40;AnomalySeverity.LOW, AnomalySeverity.MEDIUM&#41;;
     * final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;
     *     .setFilter&#40;filter&#41;;
     * PagedIterable&lt;DataPointAnomaly&gt; anomaliesIterable
     *     = metricsAdvisorClient.listAnomaliesForDetectionConfig&#40;detectionConfigurationId, startTime, endTime,
     *     options, Context.NONE&#41;;
     *
     * for &#40;DataPointAnomaly dataPointAnomaly : anomaliesIterable&#41; &#123;
     *     System.out.printf&#40;&quot;DataPointAnomaly AnomalySeverity: %s%n&quot;, dataPointAnomaly.getSeverity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *     DimensionKey seriesKey = dataPointAnomaly.getSeriesKey&#40;&#41;;
     *     for &#40;Map.Entry&lt;String, String&gt; dimension : seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;DimensionName: %s DimensionValue:%s%n&quot;,
     *             dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param startTime The start time of the time range within which the anomalies were detected.
     * @param endTime The end time of the time range within which the anomalies were detected.
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification
     *     or {@code options.filter} is used to set severity but either min or max severity is missing.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or
     * {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataPointAnomaly> listAnomaliesForDetectionConfig(
        String detectionConfigurationId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return listAnomaliesForDetectionConfig(detectionConfigurationId, startTime, endTime, null, Context.NONE);
    }

    /**
     * Fetch the anomalies identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListAnomaliesDetectedOptions-Context -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     * final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter&#40;&#41;
     *     .setSeverityRange&#40;AnomalySeverity.LOW, AnomalySeverity.MEDIUM&#41;;
     * final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;
     *     .setFilter&#40;filter&#41;;
     * PagedIterable&lt;DataPointAnomaly&gt; anomaliesIterable
     *     = metricsAdvisorClient.listAnomaliesForDetectionConfig&#40;detectionConfigurationId,
     *         startTime, endTime, options,
     *     Context.NONE&#41;;
     *
     * Stream&lt;PagedResponse&lt;DataPointAnomaly&gt;&gt; anomaliesPageStream = anomaliesIterable.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * anomaliesPageStream.forEach&#40;anomaliesPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     IterableStream&lt;DataPointAnomaly&gt; anomaliesPageItems = anomaliesPage.getElements&#40;&#41;;
     *     for &#40;DataPointAnomaly dataPointAnomaly : anomaliesPageItems&#41; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly AnomalySeverity: %s%n&quot;, dataPointAnomaly.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         DimensionKey seriesKey = dataPointAnomaly.getSeriesKey&#40;&#41;;
     *         for &#40;Map.Entry&lt;String, String&gt; dimension : seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;DimensionName: %s DimensionValue:%s%n&quot;,
     *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListAnomaliesDetectedOptions-Context -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param startTime The start time of the time range within which the anomalies were detected.
     * @param endTime The end time of the time range within which the anomalies were detected.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification
     *     or {@code options.filter} is used to set severity but either min or max severity is missing.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code startTime} or
     * {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataPointAnomaly> listAnomaliesForDetectionConfig(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomaliesDetectedOptions options, Context context) {
        return listAnomaliesForDetectionConfigSync(detectionConfigurationId,
            startTime,
            endTime,
            options,
            context);
    }

    private PagedIterable<DataPointAnomaly> listAnomaliesForDetectionConfigSync(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomaliesDetectedOptions options, Context context) {
        return new PagedIterable<>(() ->
            listAnomaliesForDetectionConfigSinglePageSync(detectionConfigurationId, startTime, endTime, options,
                context),
            continuationToken ->
                listAnomaliesForDetectionConfigNextPageSync(continuationToken, startTime, endTime, options, context));
    }

    private PagedResponse<DataPointAnomaly> listAnomaliesForDetectionConfigSinglePageSync(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomaliesDetectedOptions options,
        Context context) {
        validateMetricEnrichmentStatusInputs(detectionConfigurationId, "'detectionConfigurationId' is required.", startTime, endTime);

        DetectionAnomalyResultQuery query = new DetectionAnomalyResultQuery()
            .setStartTime(startTime)
            .setEndTime(endTime);

        options = getListAnomaliesDetectedOptions(options, query, logger);

        PagedResponse<AnomalyResult> response =
            service.getAnomaliesByAnomalyDetectionConfigurationSinglePage(
                UUID.fromString(detectionConfigurationId),
                query,
                options.getSkip(),
                options.getMaxPageSize(),
                context);
        return AnomalyTransforms.fromInnerPagedResponse(response);
    }

    private PagedResponse<DataPointAnomaly> listAnomaliesForDetectionConfigNextPageSync(
        String nextPageLink,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomaliesDetectedOptions options,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }

        DetectionAnomalyResultQuery query = new DetectionAnomalyResultQuery()
            .setStartTime(startTime)
            .setEndTime(endTime);

        getListAnomaliesDetectedOptions(options, query, logger);

        PagedResponse<AnomalyResult> response =
            service.getAnomaliesByAnomalyDetectionConfigurationNextSinglePage(nextPageLink, query, context);
        return AnomalyTransforms.fromInnerPagedResponse(response);
    }

    /**
     * Fetch the incidents identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     *
     * PagedIterable&lt;AnomalyIncident&gt; incidentsIterable
     *     = metricsAdvisorClient.listIncidentsForDetectionConfig&#40;detectionConfigurationId, startTime, endTime&#41;;
     *
     * for &#40;AnomalyIncident anomalyIncident : incidentsIterable&#41; &#123;
     *     System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, anomalyIncident.getMetricId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, anomalyIncident.getDetectionConfigurationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Id: %s%n&quot;, anomalyIncident.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Start Time: %s%n&quot;, anomalyIncident.getStartTime&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident AnomalySeverity: %s%n&quot;, anomalyIncident.getSeverity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Anomaly Incident Status: %s%n&quot;, anomalyIncident.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Root DataFeedDimension Key: %s%n&quot;, anomalyIncident.getRootDimensionKey&#40;&#41;.asMap&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
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
    public PagedIterable<AnomalyIncident> listIncidentsForDetectionConfig(
        String detectionConfigurationId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return listIncidentsForDetectionConfig(detectionConfigurationId, startTime, endTime, null, Context.NONE);
    }

    /**
     * Fetch the incidents identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListIncidentsDetectedOptions-Context -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
     * final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions&#40;&#41;
     *     .setMaxPageSize&#40;1000&#41;;
     *
     * PagedIterable&lt;AnomalyIncident&gt; incidentsIterable
     *     = metricsAdvisorClient.listIncidentsForDetectionConfig&#40;detectionConfigurationId,
     *         startTime, endTime, options,
     *     Context.NONE&#41;;
     *
     * Stream&lt;PagedResponse&lt;AnomalyIncident&gt;&gt; incidentsPageStream = incidentsIterable.streamByPage&#40;&#41;;
     *
     * int[] pageCount = new int[1];
     * incidentsPageStream.forEach&#40;incidentsPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     IterableStream&lt;AnomalyIncident&gt; pageElements = incidentsPage.getElements&#40;&#41;;
     *     for &#40;AnomalyIncident anomalyIncident : pageElements&#41; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, anomalyIncident.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, anomalyIncident.getDetectionConfigurationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Id: %s%n&quot;, anomalyIncident.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Start Time: %s%n&quot;, anomalyIncident.getStartTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident AnomalySeverity: %s%n&quot;, anomalyIncident.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Status: %s%n&quot;, anomalyIncident.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Root DataFeedDimension Key:&quot;&#41;;
     *         System.out.printf&#40;&quot;Root DataFeedDimension Key: %s%n&quot;, anomalyIncident.getRootDimensionKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListIncidentsDetectedOptions-Context -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param startTime The start time of the time range within which the incidents were detected.
     * @param endTime The end time of the time range within which the incidents were detected.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code startTime} or
     * {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyIncident> listIncidentsForDetectionConfig(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListIncidentsDetectedOptions options, Context context) {
        return listIncidentsForDetectionConfigSync(detectionConfigurationId,
            startTime, endTime, options, context);
    }

    private PagedIterable<AnomalyIncident> listIncidentsForDetectionConfigSync(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListIncidentsDetectedOptions options, Context context) {
        return new PagedIterable<>(() ->
            listIncidentsForDetectionConfigSinglePageSync(detectionConfigurationId, startTime, endTime, options,
                context),
            continuationToken ->
                listIncidentsForDetectionConfigNextPageSync(continuationToken, context));
    }

    private PagedResponse<AnomalyIncident> listIncidentsForDetectionConfigSinglePageSync(
        String detectionConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListIncidentsDetectedOptions options,
        Context context) {
        validateIncidentsForDetectionConfigInputs(detectionConfigurationId, startTime, endTime);

        DetectionIncidentResultQuery query = new DetectionIncidentResultQuery()
            .setStartTime(startTime)
            .setEndTime(endTime);
        options = getListIncidentsDetectedOptions(options, query);


        PagedResponse<IncidentResult> response =
            service.getIncidentsByAnomalyDetectionConfigurationSinglePage(
                UUID.fromString(detectionConfigurationId),
                query,
                options.getMaxPageSize(),
                context);
        return IncidentTransforms.fromInnerPagedResponse(response);
    }

    private PagedResponse<AnomalyIncident> listIncidentsForDetectionConfigNextPageSync(
        String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        PagedResponse<IncidentResult> response =
            service.getIncidentsByAnomalyDetectionConfigurationNextSinglePage(nextPageLink, context);
        return IncidentTransforms.fromInnerPagedResponse(response);
    }

    /**
     * List the root causes for an incident.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final String incidentId = &quot;c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d&quot;;
     *
     * metricsAdvisorClient.listIncidentRootCauses&#40;detectionConfigurationId, incidentId&#41;
     *     .forEach&#40;incidentRootCause -&gt; &#123;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, incidentRootCause.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;incidentRootCause.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Confidence for the detected incident root cause %.2f%n&quot;,
     *             incidentRootCause.getContributionScore&#40;&#41;&#41;;
     *     &#125;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String -->
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
    public PagedIterable<IncidentRootCause> listIncidentRootCauses(
        String detectionConfigurationId,
        String incidentId) {
        return listIncidentRootCauses(detectionConfigurationId, incidentId, Context.NONE);
    }

    /**
     * List the root causes for an incident.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String-Context -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final String incidentId = &quot;c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d&quot;;
     *
     * PagedIterable&lt;IncidentRootCause&gt; rootCauseIterable
     *     = metricsAdvisorClient.listIncidentRootCauses&#40;detectionConfigurationId, incidentId, Context.NONE&#41;;
     * Stream&lt;PagedResponse&lt;IncidentRootCause&gt;&gt; rootCausePageIterable = rootCauseIterable.streamByPage&#40;&#41;;
     * rootCausePageIterable.forEach&#40;response -&gt; &#123;
     *     System.out.printf&#40;&quot;Response StatusCode: %s%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *     IterableStream&lt;IncidentRootCause&gt; pageElements = response.getElements&#40;&#41;;
     *     for &#40;IncidentRootCause incidentRootCause : pageElements&#41; &#123;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, incidentRootCause.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;incidentRootCause.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Confidence for the detected incident root cause %.2f%n&quot;,
     *             incidentRootCause.getContributionScore&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     *
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String-Context -->
     *
     * @param detectionConfigurationId anomaly detection configuration unique id.
     * @param incidentId the incident for which you want to query root causes for.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the list of root causes for that incident.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code incidentId} is null.
     **/
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IncidentRootCause> listIncidentRootCauses(
        String detectionConfigurationId,
        String incidentId, Context context) {
        return listIncidentRootCausesSync(detectionConfigurationId, incidentId, context);
    }

    private PagedIterable<IncidentRootCause> listIncidentRootCausesSync(
        String detectionConfigurationId,
        String incidentId, Context context) {
        Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
        Objects.requireNonNull(incidentId, "'incidentId' is required.");
        AnomalyIncident anomalyIncident = new AnomalyIncident();
        IncidentHelper.setId(anomalyIncident, incidentId);
        IncidentHelper.setDetectionConfigurationId(anomalyIncident, detectionConfigurationId);
        return new PagedIterable<>(() -> listIncidentRootCausesInternal(anomalyIncident, context), null);
    }

    /**
     * List the root causes for an anomalyIncident.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#AnomalyIncident -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     *
     * metricsAdvisorClient.listIncidentsForDetectionConfig&#40;detectionConfigurationId, startTime, endTime&#41;
     *     .forEach&#40;incident -&gt; &#123;
     *         metricsAdvisorClient.listIncidentRootCauses&#40;incident&#41;
     *             .forEach&#40;incidentRootCause -&gt; &#123;
     *                 System.out.printf&#40;&quot;Description: %s%n&quot;, incidentRootCause.getDescription&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *                 System.out.println&#40;incidentRootCause.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Confidence for the detected incident root cause %.2f%n&quot;,
     *                     incidentRootCause.getContributionScore&#40;&#41;&#41;;
     *             &#125;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#AnomalyIncident -->
     *
     * @param anomalyIncident the anomalyIncident for which you want to query root causes for.
     *
     * @return the list of root causes for that anomalyIncident.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code incidentId} is null.
     **/
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IncidentRootCause> listIncidentRootCauses(AnomalyIncident anomalyIncident) {
        return listIncidentRootCausesSync(anomalyIncident, Context.NONE);
    }

    private PagedIterable<IncidentRootCause> listIncidentRootCausesSync(AnomalyIncident anomalyIncident, Context context) {
        return new PagedIterable<>(() -> listIncidentRootCausesInternal(anomalyIncident, context), null);
    }

    private PagedResponse<IncidentRootCause> listIncidentRootCausesInternal(AnomalyIncident anomalyIncident,
                                                                                  Context context) {
        validateAnomalyIncidentRootCausesInputs(anomalyIncident, logger);

        Response<RootCauseList> res =
            service.getRootCauseOfIncidentByAnomalyDetectionConfigurationWithResponse(
                UUID.fromString(anomalyIncident.getDetectionConfigurationId()),
                anomalyIncident.getId(), context);

        return IncidentRootCauseTransforms.fromInnerResponse(res);
    }

    /**
     * Fetch dimension values that have anomalies.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final String dimensionName = &quot;Dim1&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     *
     * PagedIterable&lt;String&gt; dimensionValueIterable
     *     = metricsAdvisorClient.listAnomalyDimensionValues&#40;detectionConfigurationId,
     *     dimensionName,
     *     startTime, endTime&#41;;
     *
     * for &#40;String dimensionValue : dimensionValueIterable&#41; &#123;
     *     System.out.printf&#40;&quot;DataFeedDimension Value: %s%n&quot;, dimensionValue&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime -->
     *
     * @param detectionConfigurationId Identifies the configuration used to detect the anomalies.
     * @param dimensionName The dimension name to retrieve the values for.
     * @param startTime The start time of the time range within which the anomalies were identified.
     * @param endTime The end time of the time range within which the anomalies were identified.
     * @return The dimension values with anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     * to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code dimensionName}
     * or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listAnomalyDimensionValues(
        String detectionConfigurationId,
        String dimensionName, OffsetDateTime startTime, OffsetDateTime endTime) {
        return listAnomalyDimensionValues(detectionConfigurationId,
            dimensionName, startTime, endTime, null, Context.NONE);
    }

    /**
     * Fetch dimension values that have anomalies.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime-ListAnomalyDimensionValuesOptions-Context -->
     * <pre>
     * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
     * final String dimensionName = &quot;Dim1&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final ListAnomalyDimensionValuesOptions options
     *     = new ListAnomalyDimensionValuesOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;;
     *
     * PagedIterable&lt;String&gt; dimensionValueIterable
     *     = metricsAdvisorClient.listAnomalyDimensionValues&#40;detectionConfigurationId,
     *     dimensionName,
     *     startTime, endTime, options,
     *     Context.NONE&#41;;
     *
     * Stream&lt;PagedResponse&lt;String&gt;&gt; dimensionValuePageStream = dimensionValueIterable.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * dimensionValuePageStream.forEach&#40;dimensionValuePage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     IterableStream&lt;String&gt; dimensionValuePageItems = dimensionValuePage.getElements&#40;&#41;;
     *     for &#40;String dimensionValue : dimensionValuePageItems&#41; &#123;
     *         System.out.printf&#40;&quot;DataFeedDimension Value: %s%n&quot;, dimensionValue&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime-ListAnomalyDimensionValuesOptions-Context -->
     *
     * @param detectionConfigurationId Identifies the configuration used to detect the anomalies.
     * @param dimensionName The dimension name to retrieve the values for.
     * @param startTime The start time of the time range within which the anomalies were identified.
     * @param endTime The end time of the time range within which the anomalies were identified.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The dimension values with anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     * to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code dimensionName}
     * or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listAnomalyDimensionValues(
        String detectionConfigurationId,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime,
        ListAnomalyDimensionValuesOptions options, Context context) {
        return listAnomalyDimensionValuesSync(detectionConfigurationId,
            dimensionName, startTime, endTime, options, context);
    }

    private PagedIterable<String> listAnomalyDimensionValuesSync(
        String detectionConfigurationId,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomalyDimensionValuesOptions options,
        Context context) {
        return new PagedIterable<>(() ->
            listAnomalyDimensionValuesSinglePageSync(detectionConfigurationId,
                dimensionName,
                startTime,
                endTime,
                options,
                context),
            continuationToken ->
                listAnomalyDimensionValuesNextPageSync(continuationToken,
                    dimensionName,
                    startTime,
                    endTime,
                    options,
                    context));
    }

    private PagedResponse<String> listAnomalyDimensionValuesSinglePageSync(
        String detectionConfigurationId,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomalyDimensionValuesOptions options,
        Context context) {
        validateAnomalyDimensionValuesInputs(detectionConfigurationId, dimensionName, startTime, endTime);

        AnomalyDimensionQuery query = new AnomalyDimensionQuery();
        query.setDimensionName(dimensionName);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        options = getListAnomalyDimensionValuesOptions(options, query);

        return service.getDimensionOfAnomaliesByAnomalyDetectionConfigurationSinglePage(
            UUID.fromString(detectionConfigurationId),
            query,
            options.getSkip(),
            options.getMaxPageSize(),
            context);
    }

    private PagedResponse<String> listAnomalyDimensionValuesNextPageSync(
        String nextPageLink,
        String dimensionName,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAnomalyDimensionValuesOptions options,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }

        AnomalyDimensionQuery query = new AnomalyDimensionQuery();
        query.setDimensionName(dimensionName);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        getListAnomalyDimensionValuesOptions(options, query);

        return service.getDimensionOfAnomaliesByAnomalyDetectionConfigurationNextSinglePage(nextPageLink,
                query,
                context);
    }

    /**
     * Fetch the alerts triggered by an anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     *
     * PagedIterable&lt;AnomalyAlert&gt; alertsIterable
     *     = metricsAdvisorClient.listAlerts&#40;alertConfigurationId, startTime, endTime&#41;;
     *
     * for &#40;AnomalyAlert anomalyAlert : alertsIterable&#41; &#123;
     *     System.out.printf&#40;&quot;Anomaly Alert Id: %s%n&quot;, anomalyAlert.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Created Time: %s%n&quot;, anomalyAlert.getCreatedTime&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Modified Time: %s%n&quot;, anomalyAlert.getModifiedTime&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param startTime The start time of the time range within which the alerts were triggered.
     * @param endTime The end time of the time range within which the alerts were triggered.
     * @return The alerts.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} does not conform
     * to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId}
     * or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyAlert> listAlerts(
        String alertConfigurationId, OffsetDateTime startTime, OffsetDateTime endTime) {
        return listAlerts(alertConfigurationId, startTime, endTime, null, Context.NONE);
    }

    /**
     * Fetch the alerts triggered by an anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-OffsetDateTime-OffsetDateTime-ListAlertOptions-Context -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;
     * final ListAlertOptions options = new ListAlertOptions&#40;&#41;
     *     .setAlertQueryTimeMode&#40;timeMode&#41;
     *     .setMaxPageSize&#40;10&#41;;
     *
     * PagedIterable&lt;AnomalyAlert&gt; alertsIterable
     *     = metricsAdvisorClient.listAlerts&#40;alertConfigurationId, startTime, endTime, options, Context.NONE&#41;;
     *
     * Stream&lt;PagedResponse&lt;AnomalyAlert&gt;&gt; alertsPageStream = alertsIterable.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * alertsPageStream.forEach&#40;alertsPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     IterableStream&lt;AnomalyAlert&gt; alertsPageItems = alertsPage.getElements&#40;&#41;;
     *     for &#40;AnomalyAlert anomalyAlert : alertsPageItems&#41; &#123;
     *         System.out.printf&#40;&quot;AnomalyAlert Id: %s%n&quot;, anomalyAlert.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Created Time: %s%n&quot;, anomalyAlert.getCreatedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Modified Time: %s%n&quot;, anomalyAlert.getModifiedTime&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-OffsetDateTime-OffsetDateTime-ListAlertOptions-Context -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param startTime The start time of the time range within which the alerts were triggered.
     * @param endTime The end time of the time range within which the alerts were triggered.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The alerts.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} does not conform
     * to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId}
     * or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyAlert> listAlerts(
        String alertConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAlertOptions options, Context context) {
        return listAlertsSync(alertConfigurationId, startTime, endTime, options, context);
    }

    private PagedIterable<AnomalyAlert> listAlertsSync(
        String alertConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAlertOptions options, Context context) {
        return new PagedIterable<>(() ->
            listAlertsSinglePageSync(alertConfigurationId, startTime, endTime, options, context),
            continuationToken ->
                listAlertsNextPageSync(continuationToken, startTime, endTime, options, context));
    }

    private PagedResponse<AnomalyAlert> listAlertsSinglePageSync(
        String alertConfigurationId,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAlertOptions options,
        Context context) {
        validateListAlertsInputs(alertConfigurationId, startTime, endTime);

        if (options == null) {
            options = new ListAlertOptions();
        }
        AlertingResultQuery query = new AlertingResultQuery();
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setTimeMode(TimeMode.fromString(toStringOrNull(options.getTimeMode())));

        return service.getAlertsByAnomalyAlertingConfigurationSinglePage(
                UUID.fromString(alertConfigurationId),
                query,
                options.getSkip(),
                options.getMaxPageSize(),
                context);
    }

    private PagedResponse<AnomalyAlert> listAlertsNextPageSync(
        String nextPageLink,
        OffsetDateTime startTime, OffsetDateTime endTime, ListAlertOptions options,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }

        AlertingResultQuery query = new AlertingResultQuery();
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setTimeMode(TimeMode.fromString(toStringOrNull(options.getTimeMode())));

        return service.getAlertsByAnomalyAlertingConfigurationNextSinglePage(nextPageLink,
            query,
            context);
    }

    /**
     * Fetch the anomalies in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final String alertId = &quot;1746b031c00&quot;;
     * PagedIterable&lt;DataPointAnomaly&gt; anomaliesIterable = metricsAdvisorClient.listAnomaliesForAlert&#40;
     *     alertConfigurationId,
     *     alertId
     * &#41;;
     *
     * for &#40;DataPointAnomaly dataPointAnomaly : anomaliesIterable&#41; &#123;
     *     System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, dataPointAnomaly.getMetricId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, dataPointAnomaly.getDetectionConfigurationId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;DataPoint Anomaly Created Time: %s%n&quot;, dataPointAnomaly.getCreatedTime&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;DataPoint Anomaly Modified Time: %s%n&quot;, dataPointAnomaly.getModifiedTime&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;DataPoint Anomaly AnomalySeverity: %s%n&quot;, dataPointAnomaly.getSeverity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;DataPoint Anomaly Status: %s%n&quot;, dataPointAnomaly.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *     System.out.println&#40;dataPointAnomaly.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     * conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataPointAnomaly> listAnomaliesForAlert(
        String alertConfigurationId,
        String alertId) {
        return listAnomaliesForAlert(alertConfigurationId, alertId, null, Context.NONE);
    }

    /**
     * Fetch the anomalies in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions-Context -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final String alertId = &quot;1746b031c00&quot;;
     * final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;;
     * PagedIterable&lt;DataPointAnomaly&gt; anomaliesIterable = metricsAdvisorClient.listAnomaliesForAlert&#40;
     *     alertConfigurationId,
     *     alertId,
     *     options,
     *     Context.NONE&#41;;
     *
     * Stream&lt;PagedResponse&lt;DataPointAnomaly&gt;&gt; anomaliesPageStream = anomaliesIterable.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * anomaliesPageStream.forEach&#40;anomaliesPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     IterableStream&lt;DataPointAnomaly&gt; anomaliesPageItems = anomaliesPage.getElements&#40;&#41;;
     *     for &#40;DataPointAnomaly dataPointAnomaly : anomaliesPageItems&#41; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, dataPointAnomaly.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, dataPointAnomaly.getDetectionConfigurationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Created Time: %s%n&quot;, dataPointAnomaly.getCreatedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Modified Time: %s%n&quot;, dataPointAnomaly.getModifiedTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly AnomalySeverity: %s%n&quot;, dataPointAnomaly.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly Status: %s%n&quot;, dataPointAnomaly.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
     *         System.out.println&#40;dataPointAnomaly.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions-Context -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     * conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataPointAnomaly> listAnomaliesForAlert(
        String alertConfigurationId,
        String alertId,
        ListAnomaliesAlertedOptions options, Context context) {
        return listAnomaliesForAlertSync(alertConfigurationId,
            alertId,
            options,
            context);
    }

    private PagedIterable<DataPointAnomaly> listAnomaliesForAlertSync(
        String alertConfigurationId,
        String alertId,
        ListAnomaliesAlertedOptions options,
        Context context) {
        return new PagedIterable<>(() ->
            listAnomaliesForAlertSinglePageSync(alertConfigurationId, alertId, options, context),
            continuationToken ->
                listAnomaliesForAlertNextPageSync(continuationToken, context));
    }

    private PagedResponse<DataPointAnomaly> listAnomaliesForAlertSinglePageSync(
        String alertConfigurationId,
        String alertId,
        ListAnomaliesAlertedOptions options,
        Context context) {
        validateListAnomaliesInputs(alertConfigurationId, alertId);

        PagedResponse<AnomalyResult> response = service.getAnomaliesFromAlertByAnomalyAlertingConfigurationSinglePage(
            UUID.fromString(alertConfigurationId),
            alertId,
            options == null ? null : options.getSkip(),
            options == null ? null : options.getMaxPageSize(),
            context);

        return AnomalyTransforms.fromInnerPagedResponse(response);
    }

    private PagedResponse<DataPointAnomaly> listAnomaliesForAlertNextPageSync(
        String nextPageLink,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }

        PagedResponse<AnomalyResult> response =
            service.getAnomaliesFromAlertByAnomalyAlertingConfigurationNextSinglePage(nextPageLink, context);
        return AnomalyTransforms.fromInnerPagedResponse(response);
    }

    /**
     * Fetch the incidents in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final String alertId = &quot;1746b031c00&quot;;
     *
     * PagedIterable&lt;AnomalyIncident&gt; incidentsIterable = metricsAdvisorClient.listIncidentsForAlert&#40;
     *     alertConfigurationId,
     *     alertId&#41;;
     *
     * Stream&lt;PagedResponse&lt;AnomalyIncident&gt;&gt; incidentsPageStream = incidentsIterable.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * incidentsPageStream.forEach&#40;incidentsPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     IterableStream&lt;AnomalyIncident&gt; incidentsPageItems = incidentsPage.getElements&#40;&#41;;
     *     for &#40;AnomalyIncident anomalyIncident : incidentsPageItems&#41; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, anomalyIncident.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, anomalyIncident.getDetectionConfigurationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Id: %s%n&quot;, anomalyIncident.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Start Time: %s%n&quot;, anomalyIncident.getStartTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident AnomalySeverity: %s%n&quot;, anomalyIncident.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Status: %s%n&quot;, anomalyIncident.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Root DataFeedDimension Key:&quot;&#41;;
     *         DimensionKey rootDimension = anomalyIncident.getRootDimensionKey&#40;&#41;;
     *         for &#40;Map.Entry&lt;String, String&gt; dimension : rootDimension.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;DimensionKey: %s DimensionValue:%s%n&quot;,
     *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     *
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     * conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyIncident> listIncidentsForAlert(
        String alertConfigurationId,
        String alertId) {
        return this.listIncidentsForAlert(alertConfigurationId,
            alertId,
            null,
            Context.NONE);
    }

    /**
     * Fetch the incidents in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions-Context -->
     * <pre>
     * final String alertConfigurationId = &quot;ff3014a0-bbbb-41ec-a637-677e77b81299&quot;;
     * final String alertId = &quot;1746b031c00&quot;;
     * final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions&#40;&#41;
     *     .setMaxPageSize&#40;10&#41;;
     *
     * PagedIterable&lt;AnomalyIncident&gt; incidentsIterable = metricsAdvisorClient.listIncidentsForAlert&#40;
     *     alertConfigurationId,
     *     alertId,
     *     options,
     *     Context.NONE&#41;;
     *
     * Stream&lt;PagedResponse&lt;AnomalyIncident&gt;&gt; incidentsPageStream = incidentsIterable.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * incidentsPageStream.forEach&#40;incidentsPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     IterableStream&lt;AnomalyIncident&gt; incidentsPageItems = incidentsPage.getElements&#40;&#41;;
     *     for &#40;AnomalyIncident anomalyIncident : incidentsPageItems&#41; &#123;
     *         System.out.printf&#40;&quot;Data Feed Metric Id: %s%n&quot;, anomalyIncident.getMetricId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Detection Configuration Id: %s%n&quot;, anomalyIncident.getDetectionConfigurationId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Id: %s%n&quot;, anomalyIncident.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Start Time: %s%n&quot;, anomalyIncident.getStartTime&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident AnomalySeverity: %s%n&quot;, anomalyIncident.getSeverity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Anomaly Incident Status: %s%n&quot;, anomalyIncident.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Root DataFeedDimension Key:&quot;&#41;;
     *         DimensionKey rootDimension = anomalyIncident.getRootDimensionKey&#40;&#41;;
     *         for &#40;Map.Entry&lt;String, String&gt; dimension : rootDimension.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
     *             System.out.printf&#40;&quot;DimensionKey: %s DimensionValue:%s%n&quot;,
     *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
     *         &#125;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions-Context -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     * conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyIncident> listIncidentsForAlert(
        String alertConfigurationId,
        String alertId,
        ListIncidentsAlertedOptions options, Context context) {
        return listIncidentsForAlertSync(alertConfigurationId,
            alertId,
            options, context);
    }

    private PagedIterable<AnomalyIncident> listIncidentsForAlertSync(
        String alertConfigurationId,
        String alertId,
        ListIncidentsAlertedOptions options, Context context) {
        return new PagedIterable<>(() ->
            listIncidentsForAlertSinglePageSync(alertConfigurationId, alertId, options, context),
            continuationToken ->
                listIncidentsForAlertNextPageSync(continuationToken, context));
    }

    private PagedResponse<AnomalyIncident> listIncidentsForAlertSinglePageSync(
        String alertConfigurationId,
        String alertId,
        ListIncidentsAlertedOptions options, Context context) {
        validateListAnomaliesInputs(alertConfigurationId, alertId);

        PagedResponse<IncidentResult> response = service.getIncidentsFromAlertByAnomalyAlertingConfigurationSinglePage(
            UUID.fromString(alertConfigurationId),
            alertId,
            options == null ? null : options.getSkip(),
            options == null ? null : options.getMaxPageSize(),
            context);
        return IncidentTransforms.fromInnerPagedResponse(response);
    }

    private PagedResponse<AnomalyIncident> listIncidentsForAlertNextPageSync(
        String nextPageLink,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }

        PagedResponse<IncidentResult> response =
            service.getIncidentsFromAlertByAnomalyAlertingConfigurationNextSinglePage(nextPageLink, context);
        return IncidentTransforms.fromInnerPagedResponse(response);
    }

    /**
     * Create a new metric feedback.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.addFeedback#String-MetricFeedback -->
     * <pre>
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final MetricChangePointFeedback metricChangePointFeedback
     *     = new MetricChangePointFeedback&#40;startTime, endTime, ChangePointValue.AUTO_DETECT&#41;;
     *
     * final MetricFeedback metricFeedback
     *     = metricsAdvisorClient.addFeedback&#40;metricId, metricChangePointFeedback&#41;;
     *
     * MetricChangePointFeedback createdMetricChangePointFeedback = &#40;MetricChangePointFeedback&#41; metricFeedback;
     * System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, createdMetricChangePointFeedback.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback change point value: %s%n&quot;,
     *     createdMetricChangePointFeedback.getChangePointValue&#40;&#41;.toString&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback start time: %s%n&quot;,
     *     createdMetricChangePointFeedback.getStartTime&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback end time: %s%n&quot;,
     *     createdMetricChangePointFeedback.getEndTime&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.addFeedback#String-MetricFeedback -->
     *
     * @param metricId the unique id for which the feedback needs to be submitted.
     * @param metricFeedback the actual metric feedback.
     *
     * @return the created {@link MetricFeedback metric feedback}.
     * @throws NullPointerException If {@code metricId}, {@code metricFeedback.dimensionFilter} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricFeedback addFeedback(String metricId, MetricFeedback metricFeedback) {
        return addFeedbackWithResponse(metricId, metricFeedback, Context.NONE).getValue();
    }

    /**
     * Create a new metric feedback.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.addFeedbackWithResponse#String-MetricFeedback-Context -->
     * <pre>
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final MetricChangePointFeedback metricChangePointFeedback
     *     = new MetricChangePointFeedback&#40;startTime, endTime, ChangePointValue.AUTO_DETECT&#41;;
     *
     * final Response&lt;MetricFeedback&gt; metricFeedbackResponse
     *     = metricsAdvisorClient.addFeedbackWithResponse&#40;metricId, metricChangePointFeedback, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Data Feed Metric feedback creation operation status %s%n&quot;,
     *     metricFeedbackResponse.getStatusCode&#40;&#41;&#41;;
     * MetricChangePointFeedback createdMetricChangePointFeedback
     *     = &#40;MetricChangePointFeedback&#41; metricFeedbackResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, createdMetricChangePointFeedback.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback change point value: %s%n&quot;,
     *     createdMetricChangePointFeedback.getChangePointValue&#40;&#41;.toString&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback start time: %s%n&quot;,
     *     createdMetricChangePointFeedback.getStartTime&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback end time: %s%n&quot;,
     *     createdMetricChangePointFeedback.getEndTime&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback associated dimension filter: %s%n&quot;,
     *     createdMetricChangePointFeedback.getDimensionFilter&#40;&#41;.asMap&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.addFeedbackWithResponse#String-MetricFeedback-Context -->
     *
     * @param metricId the unique id for which the feedback needs to be submitted.
     * @param metricFeedback the actual metric feedback.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the created {@link MetricFeedback metric feedback}.
     * @throws NullPointerException If {@code metricId}, {@code metricFeedback.dimensionFilter} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricFeedback> addFeedbackWithResponse(String metricId, MetricFeedback metricFeedback,
                                                            Context context) {
        return addFeedbackWithResponseSync(metricId, metricFeedback, context);
    }

    private Response<MetricFeedback> addFeedbackWithResponseSync(String metricId, MetricFeedback metricFeedback,
                                                           Context context) {
        validateAddFeedbackInputs(metricId, metricFeedback);

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
        CreateMetricFeedbackResponse createdMetricFeedbackResponse =
            service.createMetricFeedbackWithResponse(innerMetricFeedback, context);
        return getFeedbackWithResponse(parseOperationId(createdMetricFeedbackResponse
                    .getDeserializedHeaders().getLocation()), context);
    }

    /**
     * Get a metric feedback by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.getFeedback#String -->
     * <pre>
     *
     * final String feedbackId = &quot;8i3h4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final MetricFeedback metricFeedback = metricsAdvisorClient.getFeedback&#40;feedbackId&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, metricFeedback.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback associated dimension filter: %s%n&quot;,
     *     metricFeedback.getDimensionFilter&#40;&#41;.asMap&#40;&#41;&#41;;
     *
     * if &#40;PERIOD.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *     MetricPeriodFeedback createMetricPeriodFeedback
     *         = &#40;MetricPeriodFeedback&#41; metricFeedback;
     *     System.out.printf&#40;&quot;Data Feed Metric feedback type: %s%n&quot;,
     *         createMetricPeriodFeedback.getPeriodType&#40;&#41;.toString&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Data Feed Metric feedback period value: %d%n&quot;,
     *         createMetricPeriodFeedback.getPeriodValue&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.getFeedback#String -->
     *
     * @param feedbackId The metric feedback unique id.
     *
     * @return The metric feedback for the provided id.
     * @throws IllegalArgumentException If {@code feedbackId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code feedbackId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricFeedback getFeedback(String feedbackId) {
        return getFeedbackWithResponse(feedbackId, Context.NONE).getValue();
    }

    /**
     * Get a metric feedback by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.getFeedbackWithResponse#String-Context -->
     * <pre>
     *
     * final String feedbackId = &quot;8i3h4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final Response&lt;MetricFeedback&gt; metricFeedbackResponse
     *     = metricsAdvisorClient.getFeedbackWithResponse&#40;feedbackId, Context.NONE&#41;;
     * final MetricFeedback metricFeedback = metricFeedbackResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback Id: %s%n&quot;, metricFeedback.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data Feed Metric feedback associated dimension filter: %s%n&quot;,
     *     metricFeedback.getDimensionFilter&#40;&#41;.asMap&#40;&#41;&#41;;
     *
     * if &#40;PERIOD.equals&#40;metricFeedback.getFeedbackType&#40;&#41;&#41;&#41; &#123;
     *     MetricPeriodFeedback createMetricPeriodFeedback
     *         = &#40;MetricPeriodFeedback&#41; metricFeedback;
     *     System.out.printf&#40;&quot;Data Feed Metric feedback type: %s%n&quot;,
     *         createMetricPeriodFeedback.getPeriodType&#40;&#41;.toString&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Data Feed Metric feedback period value: %d%n&quot;,
     *         createMetricPeriodFeedback.getPeriodValue&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.getFeedbackWithResponse#String-Context -->
     *
     * @param feedbackId The metric feedback unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The metric feedback for the provided id.
     * @throws IllegalArgumentException If {@code feedbackId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code feedbackId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricFeedback> getFeedbackWithResponse(String feedbackId, Context context) {
        return getFeedbackWithResponseSync(feedbackId, context);
    }

    private Response<MetricFeedback> getFeedbackWithResponseSync(String feedbackId, Context context) {
        Objects.requireNonNull(feedbackId, "'feedbackId' is required.");
        Response<com.azure.ai.metricsadvisor.implementation.models.MetricFeedback> metricFeedbackResponse =
            service.getMetricFeedbackWithResponse(UUID.fromString(feedbackId), context);
        return new SimpleResponse<>(metricFeedbackResponse,
                MetricFeedbackTransforms.fromInner(metricFeedbackResponse.getValue()));
    }

    /**
     * List information of all metric feedbacks on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listFeedback#String -->
     * <pre>
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * metricsAdvisorClient.listFeedback&#40;metricId&#41;
     *     .forEach&#40;metricFeedback -&gt; &#123;
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
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listFeedback#String -->
     *
     * @param metricId the unique metric Id.
     *
     * @return A {@link PagedIterable} containing information of all the {@link MetricFeedback metric feedbacks}
     * in the account.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricFeedback> listFeedback(
        String metricId) {
        return listFeedback(metricId, null, Context.NONE);
    }

    /**
     * List information of all metric feedbacks on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listFeedback#String-ListMetricFeedbackOptions-Context -->
     * <pre>
     * final String metricId = &quot;d3gh4i4-b804-4ab9-a70f-0da0c89cft3l&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     *
     * metricsAdvisorClient.listFeedback&#40;metricId,
     *     new ListMetricFeedbackOptions&#40;&#41;
     *         .setFilter&#40;new ListMetricFeedbackFilter&#40;&#41;
     *             .setStartTime&#40;startTime&#41;
     *             .setTimeMode&#40;FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME&#41;
     *             .setEndTime&#40;endTime&#41;&#41;, Context.NONE&#41;
     *     .forEach&#40;metricFeedback -&gt; &#123;
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
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listFeedback#String-ListMetricFeedbackOptions-Context -->
     *
     * @param metricId the unique metric Id.
     * @param options The configurable {@link ListMetricFeedbackOptions options} to pass for filtering the output
     * result.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the {@link MetricFeedback metric feedbacks}
     * in the account.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricFeedback> listFeedback(
        String metricId,
        ListMetricFeedbackOptions options, Context context) {
        return listFeedbackSync(metricId, options, context);
    }

    private PagedIterable<MetricFeedback> listFeedbackSync(String metricId, ListMetricFeedbackOptions options, Context context) {
        if (options == null) {
            options = new ListMetricFeedbackOptions();
        }
        final MetricFeedbackFilter metricFeedbackFilter = MetricFeedbackTransforms.toInnerFilter(metricId, options);

        ListMetricFeedbackOptions finalOptions = options;
        return new PagedIterable<>(() ->
            listMetricFeedbacksSinglePageSync(metricFeedbackFilter, finalOptions.getMaxPageSize(),
                finalOptions.getSkip(), context),
            continuationToken ->
                listMetricFeedbacksNextPageSync(continuationToken, metricFeedbackFilter,
                    context));
    }
    private PagedResponse<MetricFeedback> listMetricFeedbacksSinglePageSync(MetricFeedbackFilter metricFeedbackFilter,
                                                                              Integer maxPageSize, Integer skip, Context context) {

        PagedResponse<com.azure.ai.metricsadvisor.implementation.models.MetricFeedback> res =
            service.listMetricFeedbacksSinglePage(metricFeedbackFilter, skip, maxPageSize, context);
        return new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream().map(MetricFeedbackTransforms::fromInner).collect(Collectors.toList()),
                res.getContinuationToken(),
                null);
    }
    private PagedResponse<MetricFeedback> listMetricFeedbacksNextPageSync(String nextPageLink,
                                                                            MetricFeedbackFilter metricFeedbackFilter, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }

        PagedResponse<com.azure.ai.metricsadvisor.implementation.models.MetricFeedback> res =
            service.listMetricFeedbacksNextSinglePage(nextPageLink, metricFeedbackFilter, context);
        return new PagedResponseBase<>(
            res.getRequest(),
            res.getStatusCode(),
            res.getHeaders(),
            res.getValue().stream().map(MetricFeedbackTransforms::fromInner).collect(Collectors.toList()),
            res.getContinuationToken(),
            null);
    }

    /**
     * List dimension values from certain metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String -->
     * <pre>
     * final String metricId = &quot;gh3014a0-41ec-a637-677e77b81455&quot;;
     * metricsAdvisorClient.listMetricDimensionValues&#40;metricId, &quot;category&quot;&#41;
     *     .forEach&#40;System.out::println&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String -->
     *
     * @param metricId metric unique id.
     * @param dimensionName the query dimension name.
     *
     * @return the {@link PagedIterable} of the dimension values for that metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code dimensionName} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listMetricDimensionValues(
        String metricId,
        String dimensionName) {
        return listMetricDimensionValues(metricId, dimensionName, null, Context.NONE);
    }

    /**
     * List dimension values from certain metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions-Context -->
     * <pre>
     * final String metricId = &quot;gh3014a0-41ec-a637-677e77b81455&quot;;
     * metricsAdvisorClient.listMetricDimensionValues&#40;metricId, &quot;category&quot;,
     *     new ListMetricDimensionValuesOptions&#40;&#41;.setDimensionValueToFilter&#40;&quot;Electronics&quot;&#41;
     *         .setMaxPageSize&#40;3&#41;, Context.NONE&#41;
     *     .forEach&#40;System.out::println&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions-Context -->
     *
     * @param metricId metric unique id.
     * @param dimensionName the query dimension name.
     * @param options the additional parameters to specify while querying.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return the {@link PagedIterable} of the dimension values for that metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws MetricsAdvisorResponseException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code dimensionName} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listMetricDimensionValues(
        String metricId,
        String dimensionName,
        ListMetricDimensionValuesOptions options, Context context) {
        return listMetricDimensionValuesSync(metricId, dimensionName, options, context);
    }

    private PagedIterable<String> listMetricDimensionValuesSync(final String metricId, final String dimensionName,
                                                final ListMetricDimensionValuesOptions options, final Context context) {
        return new PagedIterable<>(() -> listMetricDimensionValuesSinglePageAsync(metricId, dimensionName,
            options, context),
            continuationToken -> listMetricDimensionValuesNextPageAsync(continuationToken, dimensionName,
                options, context));
    }

    private PagedResponse<String> listMetricDimensionValuesSinglePageAsync(String metricId, String dimensionName,
                                                                                 ListMetricDimensionValuesOptions options, Context context) {
        Objects.requireNonNull(metricId, "'metricId' cannot be null.");
        Objects.requireNonNull(dimensionName, "'dimensionName' cannot be null.");
        if (options == null) {
            options = new ListMetricDimensionValuesOptions();
        }

        final MetricDimensionQueryOptions metricDimensionQueryOptions =
            getMetricDimensionQueryOptions(dimensionName, options);

        PagedResponse<String> res =
            service.getMetricDimensionSinglePage(UUID.fromString(metricId), metricDimensionQueryOptions,
                options.getSkip(), options.getMaxPageSize(), context);
        return new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue(),
                res.getContinuationToken(),
                null);
    }

    private PagedResponse<String> listMetricDimensionValuesNextPageAsync(String nextPageLink,
                                                                               String dimensionName, ListMetricDimensionValuesOptions options, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        final MetricDimensionQueryOptions metricDimensionQueryOptions =
            getMetricDimensionQueryOptions(dimensionName, options);

        PagedResponse<String> res =
            service.getMetricDimensionNextSinglePage(nextPageLink, metricDimensionQueryOptions, context);
        return new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue(),
                res.getContinuationToken(),
                null);
    }
}
