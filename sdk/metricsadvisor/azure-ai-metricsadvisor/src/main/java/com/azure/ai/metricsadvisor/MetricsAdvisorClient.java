// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
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
import com.azure.ai.metricsadvisor.models.ListIncidentsAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.ListAnomalyDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.ai.metricsadvisor.models.MetricSeriesData;
import com.azure.ai.metricsadvisor.models.MetricSeriesDefinition;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;
import java.util.List;

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

    private final MetricsAdvisorAsyncClient client;

    /**
     * Create a {@link MetricsAdvisorClient client} that sends requests to the Metrics Advisor service's
     * endpoint.
     * Each service call goes through the {@link MetricsAdvisorAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link MetricsAdvisorAsyncClient} that the
     * client routes its request through.
     */
    MetricsAdvisorClient(MetricsAdvisorAsyncClient client) {
        this.client = client;
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
        return new PagedIterable<>(client.listMetricSeriesDefinitions(metricId, activeSince, options,
            context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listMetricSeriesData(metricId, seriesKeys, startTime, endTime,
                context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listMetricEnrichmentStatus(metricId, startTime, endTime, options,
            context == null ? Context.NONE : context));
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
        return listMetricEnrichedSeriesData(detectionConfigurationId,
            seriesKeys,
            startTime,
            endTime,
            Context.NONE);
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
        return new PagedIterable<>(client.listMetricEnrichedSeriesData(detectionConfigurationId,
            seriesKeys,
            startTime,
            endTime,
            context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listAnomaliesForDetectionConfig(detectionConfigurationId,
            startTime,
            endTime,
            options,
            context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listIncidentsForDetectionConfig(detectionConfigurationId,
            startTime, endTime, options,
            context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listIncidentRootCauses(detectionConfigurationId, incidentId));
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
        return new PagedIterable<>(client.listIncidentRootCauses(detectionConfigurationId, incidentId, context));
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
        return new PagedIterable<>(client.listIncidentRootCauses(anomalyIncident, Context.NONE));
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
        return new PagedIterable<>(client.listAnomalyDimensionValues(detectionConfigurationId,
            dimensionName, startTime, endTime, options, context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listAlerts(alertConfigurationId, startTime, endTime, options,
            context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listAnomaliesForAlert(alertConfigurationId,
            alertId,
            options,
            context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listIncidentsForAlert(alertConfigurationId,
            alertId,
            options,
            context == null ? Context.NONE : context));
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
        return client.addFeedbackWithResponse(metricId, metricFeedback, context).block();
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
        return client.getFeedbackWithResponse(feedbackId, context).block();
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
        return new PagedIterable<>(client.listFeedback(metricId, options,
            context == null ? Context.NONE : context));
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
        return new PagedIterable<>(client.listMetricDimensionValues(metricId, dimensionName, options,
            context == null ? Context.NONE : context));
    }
}
