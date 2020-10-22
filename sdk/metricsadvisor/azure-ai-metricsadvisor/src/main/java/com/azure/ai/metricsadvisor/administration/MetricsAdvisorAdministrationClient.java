// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.models.DataFeed;
import com.azure.ai.metricsadvisor.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.models.DataFeedSource;
import com.azure.ai.metricsadvisor.models.Hook;
import com.azure.ai.metricsadvisor.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.models.ListDataFeedOptions;
import com.azure.ai.metricsadvisor.models.ListHookOptions;
import com.azure.ai.metricsadvisor.models.AnomalyDetectionConfiguration;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Metrics Advisor.
 * <p><strong>Instantiating a synchronous Metrics Advisor Administration Client</strong></p>
 * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.instantiation}
 *
 * @see MetricsAdvisorAdministrationClientBuilder
 */
@ServiceClient(builder = MetricsAdvisorAdministrationClientBuilder.class)
public final class MetricsAdvisorAdministrationClient {
    final MetricsAdvisorAdministrationAsyncClient client;

    /**
     * Create a {@link MetricsAdvisorAdministrationClient client} that sends requests to the Metrics Advisor service's
     * endpoint.
     * Each service call goes through the {@link MetricsAdvisorAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param client The {@link MetricsAdvisorAdministrationAsyncClient} that the client routes its request through.
     */
    MetricsAdvisorAdministrationClient(MetricsAdvisorAdministrationAsyncClient client) {
        this.client = client;
    }

    /**
     * Create a new data feed.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeed#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions}
     *
     * @param dataFeedName the name of the data feed.
     * @param dataFeedSource the source of the data feed.
     * @param dataFeedGranularity the granularity details of the time series.
     * @param dataFeedSchema the schema detail properties of the data feed.
     * @param dataFeedIngestionSettings the data feed ingestion properties.
     * @param dataFeedOptions the additional options to configure the data feed.
     *
     * @return The created data feed.
     * @throws NullPointerException If {@code dataFeedName}, {@code dataFeedSource}, {@code metricColumns},
     * {@code granularityType} or {@code ingestionStartTime} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeed createDataFeed(String dataFeedName,
        DataFeedSource dataFeedSource,
        DataFeedGranularity dataFeedGranularity,
        DataFeedSchema dataFeedSchema,
        DataFeedIngestionSettings dataFeedIngestionSettings,
        DataFeedOptions dataFeedOptions) {
        return createDataFeedWithResponse(dataFeedName, dataFeedSource, dataFeedGranularity, dataFeedSchema,
            dataFeedIngestionSettings, dataFeedOptions, Context.NONE).getValue();
    }

    /**
     * Create a new data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeedWithResponse#String-DataFeedSource-DataFeedGranularity-DataFeedSchema-DataFeedIngestionSettings-DataFeedOptions-Context}
     *
     * @param dataFeedName the name of the data feed.
     * @param dataFeedSource the source of the data feed.
     * @param dataFeedGranularity the granularity details of the time series.
     * @param dataFeedSchema the schema detail properties of the data feed.
     * @param dataFeedIngestionSettings the data feed ingestion properties.
     * @param dataFeedOptions the additional options to configure the data feed.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the created data feed.
     * @throws NullPointerException If {@code dataFeedName}, {@code dataFeedSource}, {@code metricColumns},
     * {@code granularityType} or {@code ingestionStartTime} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeed> createDataFeedWithResponse(String dataFeedName,
        DataFeedSource dataFeedSource,
        DataFeedGranularity dataFeedGranularity,
        DataFeedSchema dataFeedSchema,
        DataFeedIngestionSettings dataFeedIngestionSettings,
        DataFeedOptions dataFeedOptions, Context context) {
        return client.createDataFeedWithResponse(dataFeedName, dataFeedSource, dataFeedGranularity, dataFeedSchema,
            dataFeedIngestionSettings, dataFeedOptions, context).block();
    }

    /**
     * Get a data feed by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeed#String}
     *
     * @param dataFeedId The data feed unique id.
     *
     * @return The data feed for the provided id.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeed getDataFeed(String dataFeedId) {
        return getDataFeedWithResponse(dataFeedId, Context.NONE).getValue();
    }

    /**
     * Get a data feed by its id with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedWithResponse#String-Context}
     *
     * @param dataFeedId The data feed unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The data feed for the provided id.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeed> getDataFeedWithResponse(String dataFeedId, Context context) {
        return client.getDataFeedWithResponse(dataFeedId, context).block();
    }

    /**
     * Update a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeed#DataFeed}
     *
     * @param dataFeed the data feed that needs to be updated.
     *
     * @return the updated data feed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeed updateDataFeed(DataFeed dataFeed) {
        return updateDataFeedWithResponse(dataFeed, Context.NONE).getValue();
    }

    /**
     * Update a data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeedWithResponse#DataFeed-Context}
     *
     * @param dataFeed the data feed that needs to be updated.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return the updated data feed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeed> updateDataFeedWithResponse(DataFeed dataFeed, Context context) {
        return client.updateDataFeedWithResponse(dataFeed, context).block();
    }

    /**
     * Delete a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeed#String}
     *
     * @param dataFeedId The data feed unique id.
     *
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDataFeed(String dataFeedId) {
        deleteDataFeedWithResponse(dataFeedId, Context.NONE);
    }

    /**
     * Delete a data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeedWithResponse#String-Context}
     *
     * @param dataFeedId The data feed unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return a REST Response.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDataFeedWithResponse(String dataFeedId, Context context) {
        return client.deleteDataFeedWithResponse(dataFeedId, context).block();
    }

    /**
     * List information of all data feeds on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds}
     *
     * @return A {@link PagedIterable} containing information of all the {@link DataFeed data feeds} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataFeed> listDataFeeds() {
        return listDataFeeds(null, Context.NONE);
    }

    /**
     * List information of all data feeds on the metrics advisor account with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds#ListDataFeedOptions-Context}
     *
     * @param options The configurable {@link ListDataFeedOptions options} to pass for filtering the output result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the {@link DataFeed data feeds} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataFeed> listDataFeeds(ListDataFeedOptions options, Context context) {
        return new PagedIterable<>(client.listDataFeeds(options, context));
    }

    /**
     * Fetch the ingestion status of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions}
     *
     * @param dataFeedId The data feed id.
     * @param options The additional parameters.
     * @return The ingestion statuses.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code options}, {@code options.startTime},
     *     {@code options.endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataFeedIngestionStatus> listDataFeedIngestionStatus(
        String dataFeedId,
        ListDataFeedIngestionOptions options) {
        return listDataFeedIngestionStatus(dataFeedId, options, Context.NONE);
    }

    /**
     * Fetch the ingestion status of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions-Context}
     *
     * @param dataFeedId The data feed id.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The ingestion statuses.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code options}, {@code options.startTime},
     *     {@code options.endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataFeedIngestionStatus> listDataFeedIngestionStatus(
        String dataFeedId,
        ListDataFeedIngestionOptions options, Context context) {
        return new PagedIterable<>(client.listDataFeedIngestionStatus(dataFeedId,
            options,
            context == null ? Context.NONE : context));
    }

    /**
     * Refresh data ingestion for a period.
     * <p>
     * The data in the data source for the given period will be reingested
     * and any ingested data for the same period will be overwritten.
     * </p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime}
     *
     * @param dataFeedId The data feed id.
     * @param startTime The start point of the period.
     * @param endTime The end point of of the period.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code startTime}, {@code endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void refreshDataFeedIngestion(
        String dataFeedId,
        OffsetDateTime startTime,
        OffsetDateTime endTime) {
        refreshDataFeedIngestionWithResponse(dataFeedId, startTime, endTime, Context.NONE);
    }

    /**
     * Refresh data ingestion for a period.
     * <p>
     * The data in the data source for the given period will be reingested
     * and any ingested data for the same period will be overwritten.
     * </p>
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime-Context}
     *
     * @param dataFeedId The data feed id.
     * @param startTime The start point of the period.
     * @param endTime The end point of of the period.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The response.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code startTime}, {@code endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> refreshDataFeedIngestionWithResponse(
        String dataFeedId,
        OffsetDateTime startTime,
        OffsetDateTime endTime, Context context) {
        return client.refreshDataFeedIngestionWithResponse(dataFeedId,
            startTime,
            endTime,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Retrieve the ingestion progress of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgress#String}
     *
     * @param dataFeedId The data feed id.
     * @return The {@link DataFeedIngestionProgress} of the data feed.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeedIngestionProgress getDataFeedIngestionProgress(String dataFeedId) {
        return getDataFeedIngestionProgressWithResponse(dataFeedId, Context.NONE)
            .getValue();
    }

    /**
     * Retrieve the ingestion progress of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgressWithResponse#String-Context}
     *
     * @param dataFeedId The data feed id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing {@link DataFeedIngestionProgress} of the data feed.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeedIngestionProgress> getDataFeedIngestionProgressWithResponse(String dataFeedId,
                                                                                        Context context) {
        return client.getDataFeedIngestionProgressWithResponse(dataFeedId,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Create a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfiguration#String-AnomalyDetectionConfiguration}
     *
     * @param metricId The metric id to associate the configuration with.
     * @param detectionConfiguration The anomaly detection configuration.
     * @return The created {@link AnomalyDetectionConfiguration}.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID
     *     format specification, or {@code detectionConfiguration.name} is not set.
     * @throws NullPointerException thrown if the {@code metricId} is null
     *   or {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.wholeSeriesCondition} is null
     *   or {@code seriesKey} is missing for any {@code MetricSingleSeriesDetectionCondition} in the configuration
     *   or {@code seriesGroupKey} is missing for any {@code MetricSeriesGroupDetectionCondition} in the configuration
     *   or {@code crossConditionsOperator} is missing when multiple nested conditions are set in a
     *      {@code MetricSingleSeriesDetectionCondition} or {@code MetricSeriesGroupDetectionCondition}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyDetectionConfiguration createMetricAnomalyDetectionConfiguration(
        String metricId,
        AnomalyDetectionConfiguration detectionConfiguration) {
        return createMetricAnomalyDetectionConfigurationWithResponse(metricId,
            detectionConfiguration,
            Context.NONE).getValue();
    }

    /**
     * Create a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfigurationWithResponse#String-AnomalyDetectionConfiguration-Context}
     *
     * @param metricId The metric id to associate the configuration with.
     * @param detectionConfiguration The anomaly detection configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the created {@link AnomalyDetectionConfiguration}.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID
     *     format specification, or {@code detectionConfiguration.name} is not set.
     * @throws NullPointerException thrown if the {@code metricId} is null
     *   or {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.wholeSeriesCondition} is null
     *   or {@code seriesKey} is missing for any {@code MetricSingleSeriesDetectionCondition} in the configuration
     *   or {@code seriesGroupKey} is missing for any {@code MetricSeriesGroupDetectionCondition} in the configuration
     *   or {@code crossConditionsOperator} is missing when multiple nested conditions are set in a
     *      {@code MetricSingleSeriesDetectionCondition} or {@code MetricSeriesGroupDetectionCondition}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyDetectionConfiguration> createMetricAnomalyDetectionConfigurationWithResponse(
        String metricId,
        AnomalyDetectionConfiguration detectionConfiguration,
        Context context) {
        return client.createMetricAnomalyDetectionConfigurationWithResponse(metricId, detectionConfiguration,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Get the anomaly detection configuration by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getMetricAnomalyDetectionConfiguration#String}
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @return The {@link AnomalyDetectionConfiguration} identified by the given id.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyDetectionConfiguration getMetricAnomalyDetectionConfiguration(
        String detectionConfigurationId) {
        return getMetricAnomalyDetectionConfigurationWithResponse(detectionConfigurationId,
            Context.NONE).getValue();
    }

    /**
     * Get the anomaly detection configuration by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getMetricAnomalyDetectionConfigurationWithResponse#String-Context}
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the {@link AnomalyDetectionConfiguration} for the provided id.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyDetectionConfiguration> getMetricAnomalyDetectionConfigurationWithResponse(
        String detectionConfigurationId, Context context) {
        return client.getMetricAnomalyDetectionConfigurationWithResponse(detectionConfigurationId,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Update a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateMetricAnomalyDetectionConfiguration#AnomalyDetectionConfiguration}
     *
     * @param detectionConfiguration The anomaly detection configuration.
     * @return The updated {@link AnomalyDetectionConfiguration}.
     * @throws NullPointerException thrown if the {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyDetectionConfiguration updateMetricAnomalyDetectionConfiguration(
        AnomalyDetectionConfiguration detectionConfiguration) {
        return updateMetricAnomalyDetectionConfigurationWithResponse(detectionConfiguration, Context.NONE)
            .getValue();
    }

    /**
     * Update a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateMetricAnomalyDetectionConfigurationWithResponse#AnomalyDetectionConfiguration-Context}
     *
     * @param detectionConfiguration The anomaly detection configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the updated {@link AnomalyDetectionConfiguration}.
     * @throws NullPointerException thrown if the {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyDetectionConfiguration> updateMetricAnomalyDetectionConfigurationWithResponse(
        AnomalyDetectionConfiguration detectionConfiguration,
        Context context) {
        return client.updateMetricAnomalyDetectionConfigurationWithResponse(detectionConfiguration,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Delete a metric anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteMetricAnomalyDetectionConfiguration#String}
     *
     * @param detectionConfigurationId The metric anomaly detection configuration unique id.
     *
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteMetricAnomalyDetectionConfiguration(String detectionConfigurationId) {
        deleteMetricAnomalyDetectionConfigurationWithResponse(detectionConfigurationId, Context.NONE)
            .getValue();
    }

    /**
     * Delete a metric anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteMetricAnomalyDetectionConfigurationWithResponse#String-Context}
     *
     * @param detectionConfigurationId The metric anomaly detection configuration unique id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} of containing result of delete operation.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteMetricAnomalyDetectionConfigurationWithResponse(
        String detectionConfigurationId,
        Context context) {
        return client.deleteMetricAnomalyDetectionConfigurationWithResponse(
            detectionConfigurationId,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Given a metric id, retrieve all anomaly detection configurations applied to it.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigurations#String}
     *
     * @param metricId The metric id.
     * @return The anomaly detection configurations.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyDetectionConfiguration> listMetricAnomalyDetectionConfigurations(String metricId) {
        return new PagedIterable<>(client.listMetricAnomalyDetectionConfigurations(metricId,
            Context.NONE));
    }

    /**
     * Given a metric id, retrieve all anomaly detection configurations applied to it.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigurations#String-Context}
     *
     * @param metricId The metric id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The anomaly detection configurations.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyDetectionConfiguration> listMetricAnomalyDetectionConfigurations(String metricId,
                                                                                                  Context context) {
        return new PagedIterable<>(client.listMetricAnomalyDetectionConfigurations(metricId,
            context == null ? Context.NONE : context));
    }

    /**
     * Creates a hook that receives anomaly incident alerts.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHook#Hook}
     *
     * @param hook The hook.
     * @return The created {@link Hook}.
     * @throws NullPointerException If {@code hook}, {@code hook.name}, {@code hook.endpoint} (for web hook) is null.
     * @throws IllegalArgumentException If at least one email not present for email hook.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Hook createHook(Hook hook) {
        return createHookWithResponse(hook, Context.NONE).getValue();
    }

    /**
     * Creates a hook that receives anomaly incident alerts.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHookWithResponse#Hook-Context}
     *
     * @param hook The hook.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the created {@link Hook}.
     * @throws NullPointerException If {@code hook}, {@code hook.name}, {@code hook.endpoint} (for web hook) is null.
     * @throws IllegalArgumentException If at least one email not present for email hook.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Hook> createHookWithResponse(Hook hook, Context context) {
        return client.createHookWithResponse(hook, context == null ? Context.NONE : context)
            .block();
    }

    /**
     * Get a hook by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHook#String}
     *
     * @param hookId The hook unique id.
     * @return The {@link Hook} for the provided id.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Hook getHook(String hookId) {
        return getHookWithResponse(hookId, Context.NONE).getValue();
    }

    /**
     * Get a hook by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHookWithResponse#String-Context}
     *
     * @param hookId The hook unique id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the {@link Hook} for the provided id.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Hook> getHookWithResponse(String hookId, Context context) {
        return client.getHookWithResponse(hookId, context == null ? Context.NONE : context)
            .block();
    }

    /**
     * Update an existing hook.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHook#Hook}
     *
     * @param hook The hook to update.
     * @return The updated {@link Hook}.
     * @throws NullPointerException If {@code hook.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Hook updateHook(Hook hook) {
        return updateHookWithResponse(hook, Context.NONE).getValue();
    }

    /**
     * Update an existing hook.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHookWithResponse#Hook-Context}
     *
     * @param hook The hook to update.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the updated {@link Hook}.
     * @throws NullPointerException If {@code hook.id} is null.
     * @throws IllegalArgumentException If {@code hook.Id} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Hook> updateHookWithResponse(Hook hook, Context context) {
        return client.updateHookWithResponse(hook, context == null ? Context.NONE : context)
            .block();
    }

    /**
     * Delete a hook.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHook#String}
     *
     * @param hookId The hook unique id.
     *
     * @throws NullPointerException thrown if the {@code hookId} is null.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteHook(String hookId) {
        deleteHookWithResponse(hookId, Context.NONE);
    }

    /**
     * Delete a hook.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHookWithResponse#String-Context}
     *
     * @param hookId The hook unique id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response}.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteHookWithResponse(String hookId, Context context) {
        return client.deleteHookWithResponse(hookId, context == null ? Context.NONE : context).block();
    }

    /**
     * List information of hooks on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks}
     *
     * @return A {@link PagedIterable} containing information of all the {@link Hook} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Hook> listHooks() {
        return listHooks(new ListHookOptions(), Context.NONE);
    }

    /**
     * List information of hooks.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks#ListHookOptions-Context}
     *
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing information of the {@link Hook} resources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Hook> listHooks(ListHookOptions options, Context context) {
        return new PagedIterable<>(client.listHooks(options, context == null ? Context.NONE : context));
    }

    /**
     * Create a configuration to trigger alert when anomalies are detected.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAnomalyAlertConfiguration#AnomalyAlertConfiguration}
     *
     * @param alertConfiguration The anomaly alerting configuration.
     *
     * @return The {@link AnomalyAlertConfiguration} that was created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyAlertConfiguration createAnomalyAlertConfiguration(
        AnomalyAlertConfiguration alertConfiguration) {
        return createAnomalyAlertConfigurationWithResponse(alertConfiguration, Context.NONE).getValue();
    }

    /**
     * Create a configuration to trigger alert when anomalies are detected.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration-Context}
     *
     * @param alertConfiguration The anomaly alerting configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the created {@link AnomalyAlertConfiguration}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyAlertConfiguration> createAnomalyAlertConfigurationWithResponse(
        AnomalyAlertConfiguration alertConfiguration, Context context) {
        return client.createAnomalyAlertConfigurationWithResponse(alertConfiguration, context == null
            ? Context.NONE : context).block();
    }

    /**
     * Get the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAnomalyAlertConfiguration#String}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     *
     * @return The {@link AnomalyAlertConfiguration} identified by the given id.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyAlertConfiguration getAnomalyAlertConfiguration(
        String alertConfigurationId) {
        return getAnomalyAlertConfigurationWithResponse(alertConfigurationId, Context.NONE).getValue();
    }

    /**
     * Get the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAnomalyAlertConfigurationWithResponse#String-Context}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response response} containing the {@link AnomalyAlertConfiguration} identified by the given id.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyAlertConfiguration> getAnomalyAlertConfigurationWithResponse(
        String alertConfigurationId, Context context) {
        return client.getAnomalyAlertConfigurationWithResponse(alertConfigurationId, context == null
            ? Context.NONE : context).block();
    }

    /**
     * Update anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAnomalyAlertConfiguration#AnomalyAlertConfiguration}
     *
     * @param alertConfiguration The anomaly alert configuration to update.
     *
     * @return The {@link AnomalyAlertConfiguration} that was updated.
     * @throws NullPointerException thrown if {@code alertConfiguration} or
     * {@code alertConfiguration.metricAnomalyAlertConfigurations} is null or empty.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyAlertConfiguration updateAnomalyAlertConfiguration(
        AnomalyAlertConfiguration alertConfiguration) {
        return updateAnomalyAlertConfigurationWithResponse(alertConfiguration, Context.NONE).getValue();
    }

    /**
     * Update anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAnomalyAlertConfigurationWithResponse#AnomalyAlertConfiguration-Context}
     *
     * @param alertConfiguration The anomaly alert configuration to update.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link AnomalyAlertConfiguration} that was updated.
     * @throws NullPointerException thrown if {@code alertConfiguration} or
     * {@code alertConfiguration.metricAnomalyAlertConfigurations} is null or empty.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyAlertConfiguration> updateAnomalyAlertConfigurationWithResponse(
        AnomalyAlertConfiguration alertConfiguration, Context context) {
        return client.updateAnomalyAlertConfigurationWithResponse(alertConfiguration, context == null
            ? Context.NONE : context).block();
    }

    /**
     * Deletes the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAnomalyAlertConfiguration#String}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteAnomalyAlertConfiguration(String alertConfigurationId) {
        deleteAnomalyAlertConfigurationWithResponse(alertConfigurationId, Context.NONE);
    }

    /**
     * Deletes the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAnomalyAlertConfigurationWithResponse#String-Context}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and headers returned after the operation.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteAnomalyAlertConfigurationWithResponse(String alertConfigurationId, Context context) {
        return client.deleteAnomalyAlertConfigurationWithResponse(alertConfigurationId, context == null
            ? Context.NONE : context).block();
    }

    /**
     * Fetch the anomaly alert configurations associated with a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAnomalyAlertConfigurations#String}
     *
     * @param detectionConfigurationId The id of the detection configuration.
     *
     * @return A {@link PagedIterable} containing information of all the
     * {@link AnomalyAlertConfiguration anomaly alert configurations} for the specified detection configuration.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyAlertConfiguration> listAnomalyAlertConfigurations(
        String detectionConfigurationId) {
        return listAnomalyAlertConfigurations(detectionConfigurationId, Context.NONE);
    }

    /**
     * Fetch the anomaly alert configurations associated with a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAnomalyAlertConfigurations#String-Context}
     *
     * @param detectionConfigurationId The id of the detection configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the
     * {@link AnomalyAlertConfiguration anomaly alert configurations} for the specified detection configuration.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyAlertConfiguration> listAnomalyAlertConfigurations(
        String detectionConfigurationId, Context context) {
        return new PagedIterable<>(client.listAnomalyAlertConfigurations(detectionConfigurationId,
            context == null ? Context.NONE : context));
    }
}
