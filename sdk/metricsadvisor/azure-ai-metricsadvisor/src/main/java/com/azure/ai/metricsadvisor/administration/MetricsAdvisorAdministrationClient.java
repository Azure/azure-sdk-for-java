// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.administration.models.DatasourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.ListAnomalyAlertConfigsOptions;
import com.azure.ai.metricsadvisor.administration.models.ListCredentialEntityOptions;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.ListHookOptions;
import com.azure.ai.metricsadvisor.administration.models.ListMetricAnomalyDetectionConfigsOptions;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
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
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeed#DataFeed}
     *
     * @param dataFeed The data feed to be created.
     * @return The created data feed.
     * @throws NullPointerException If {@code dataFeed}, {@code dataFeedName}, {@code dataFeedSource}, {@code metrics},
     * {@code granularityType} or {@code ingestionStartTime} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeed createDataFeed(DataFeed dataFeed) {
        return createDataFeedWithResponse(dataFeed, Context.NONE).getValue();
    }

    /**
     * Create a new data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeedWithResponse#DataFeed-Context}
     *
     * @param dataFeed The data feed to be created.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the created data feed.
     * @throws NullPointerException If {@code dataFeed}, {@code dataFeedName}, {@code dataFeedSource}, {@code metrics},
     * {@code granularityType} or {@code ingestionStartTime} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeed> createDataFeedWithResponse(DataFeed dataFeed, Context context) {
        return client.createDataFeedWithResponse(dataFeed, context).block();
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
        String dataFeedId, ListDataFeedIngestionOptions options) {
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
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfig#String-AnomalyDetectionConfiguration}
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
    public AnomalyDetectionConfiguration createMetricAnomalyDetectionConfig(
        String metricId,
        AnomalyDetectionConfiguration detectionConfiguration) {
        return createMetricAnomalyDetectionConfigWithResponse(metricId,
            detectionConfiguration,
            Context.NONE).getValue();
    }

    /**
     * Create a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createMetricAnomalyDetectionConfigWithResponse#String-AnomalyDetectionConfiguration-Context}
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
    public Response<AnomalyDetectionConfiguration> createMetricAnomalyDetectionConfigWithResponse(
        String metricId,
        AnomalyDetectionConfiguration detectionConfiguration,
        Context context) {
        return client.createMetricAnomalyDetectionConfigWithResponse(metricId, detectionConfiguration,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Get the anomaly detection configuration by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getMetricAnomalyDetectionConfig#String}
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @return The {@link AnomalyDetectionConfiguration} identified by the given id.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyDetectionConfiguration getMetricAnomalyDetectionConfig(
        String detectionConfigurationId) {
        return getMetricAnomalyDetectionConfigWithResponse(detectionConfigurationId, Context.NONE).getValue();
    }

    /**
     * Get the anomaly detection configuration by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getMetricAnomalyDetectionConfigWithResponse#String-Context}
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the {@link AnomalyDetectionConfiguration} for the provided id.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyDetectionConfiguration> getMetricAnomalyDetectionConfigWithResponse(
        String detectionConfigurationId, Context context) {
        return client.getMetricAnomalyDetectionConfigWithResponse(detectionConfigurationId,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Update a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateMetricAnomalyDetectionConfig#AnomalyDetectionConfiguration}
     *
     * @param detectionConfiguration The anomaly detection configuration.
     * @return The updated {@link AnomalyDetectionConfiguration}.
     * @throws NullPointerException thrown if the {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyDetectionConfiguration updateMetricAnomalyDetectionConfig(
        AnomalyDetectionConfiguration detectionConfiguration) {
        return updateMetricAnomalyDetectionConfigWithResponse(detectionConfiguration, Context.NONE)
            .getValue();
    }

    /**
     * Update a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateMetricAnomalyDetectionConfigWithResponse#AnomalyDetectionConfiguration-Context}
     *
     * @param detectionConfiguration The anomaly detection configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the updated {@link AnomalyDetectionConfiguration}.
     * @throws NullPointerException thrown if the {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyDetectionConfiguration> updateMetricAnomalyDetectionConfigWithResponse(
        AnomalyDetectionConfiguration detectionConfiguration,
        Context context) {
        return client.updateMetricAnomalyDetectionConfigWithResponse(detectionConfiguration,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Delete a metric anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteMetricAnomalyDetectionConfig#String}
     *
     * @param detectionConfigurationId The metric anomaly detection configuration unique id.
     *
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteMetricAnomalyDetectionConfig(String detectionConfigurationId) {
        deleteMetricAnomalyDetectionConfigWithResponse(detectionConfigurationId, Context.NONE)
            .getValue();
    }

    /**
     * Delete a metric anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteMetricAnomalyDetectionConfigWithResponse#String-Context}
     *
     * @param detectionConfigurationId The metric anomaly detection configuration unique id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} of containing result of delete operation.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteMetricAnomalyDetectionConfigWithResponse(
        String detectionConfigurationId,
        Context context) {
        return client.deleteMetricAnomalyDetectionConfigWithResponse(
            detectionConfigurationId,
            context == null ? Context.NONE : context).block();
    }

    /**
     * Given a metric id, retrieve all anomaly detection configurations applied to it.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigs#String}
     *
     * @param metricId The metric id.
     * @return The anomaly detection configurations.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyDetectionConfiguration> listMetricAnomalyDetectionConfigs(
        String metricId) {
        return new PagedIterable<>(client.listMetricAnomalyDetectionConfigs(metricId, null,
            Context.NONE));
    }

    /**
     * Given a metric id, retrieve all anomaly detection configurations applied to it.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listMetricAnomalyDetectionConfigs#String-ListMetricAnomalyDetectionConfigsOptions-Context}
     *
     * @param metricId The metric id.
     * @param options th e additional configurable options to specify when querying the result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The anomaly detection configurations.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyDetectionConfiguration> listMetricAnomalyDetectionConfigs(
        String metricId,
        ListMetricAnomalyDetectionConfigsOptions options,
        Context context) {
        return new PagedIterable<>(client.listMetricAnomalyDetectionConfigs(metricId, options,
            context == null ? Context.NONE : context));
    }

    /**
     * Creates a notificationHook that receives anomaly incident alerts.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHook#NotificationHook}
     *
     * @param notificationHook The notificationHook.
     * @return The created {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook}, {@code notificationHook.name},
     * {@code notificationHook.endpoint} (for web notificationHook) is null.
     * @throws IllegalArgumentException If at least one email not present for email notificationHook.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NotificationHook createHook(NotificationHook notificationHook) {
        return createHookWithResponse(notificationHook, Context.NONE).getValue();
    }

    /**
     * Creates a notificationHook that receives anomaly incident alerts.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHookWithResponse#NotificationHook-Context}
     *
     * @param notificationHook The notificationHook.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the created {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook}, {@code notificationHook.name},
     * {@code notificationHook.endpoint} (for web notificationHook) is null.
     * @throws IllegalArgumentException If at least one email not present for email notificationHook.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NotificationHook> createHookWithResponse(NotificationHook notificationHook, Context context) {
        return client.createHookWithResponse(notificationHook, context == null ? Context.NONE : context)
            .block();
    }

    /**
     * Get a hook by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHook#String}
     *
     * @param hookId The hook unique id.
     * @return The {@link NotificationHook} for the provided id.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NotificationHook getHook(String hookId) {
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
     * @return A {@link Response} containing the {@link NotificationHook} for the provided id.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NotificationHook> getHookWithResponse(String hookId, Context context) {
        return client.getHookWithResponse(hookId, context == null ? Context.NONE : context)
            .block();
    }

    /**
     * Update an existing notificationHook.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHook#NotificationHook}
     *
     * @param notificationHook The notificationHook to update.
     * @return The updated {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NotificationHook updateHook(NotificationHook notificationHook) {
        return updateHookWithResponse(notificationHook, Context.NONE).getValue();
    }

    /**
     * Update an existing notificationHook.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHookWithResponse#NotificationHook-Context}
     *
     * @param notificationHook The notificationHook to update.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the updated {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook.id} is null.
     * @throws IllegalArgumentException If {@code notificationHook.Id} does not conform to the UUID format
     * specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NotificationHook> updateHookWithResponse(NotificationHook notificationHook, Context context) {
        return client.updateHookWithResponse(notificationHook, context == null ? Context.NONE : context)
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
     * @return A {@link PagedIterable} containing information of all the {@link NotificationHook} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<NotificationHook> listHooks() {
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
     * @return A {@link PagedIterable} containing information of the {@link NotificationHook} resources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<NotificationHook> listHooks(ListHookOptions options, Context context) {
        return new PagedIterable<>(client.listHooks(options, context == null ? Context.NONE : context));
    }

    /**
     * Create a configuration to trigger alert when anomalies are detected.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAnomalyAlertConfig#AnomalyAlertConfiguration}
     *
     * @param alertConfiguration The anomaly alerting configuration.
     *
     * @return The {@link AnomalyAlertConfiguration} that was created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyAlertConfiguration createAnomalyAlertConfig(
        AnomalyAlertConfiguration alertConfiguration) {
        return createAnomalyAlertConfigWithResponse(alertConfiguration, Context.NONE).getValue();
    }

    /**
     * Create a configuration to trigger alert when anomalies are detected.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAnomalyAlertConfigWithResponse#AnomalyAlertConfiguration-Context}
     *
     * @param alertConfiguration The anomaly alerting configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the created {@link AnomalyAlertConfiguration}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyAlertConfiguration> createAnomalyAlertConfigWithResponse(
        AnomalyAlertConfiguration alertConfiguration, Context context) {
        return client.createAnomalyAlertConfigWithResponse(alertConfiguration, context == null
            ? Context.NONE : context).block();
    }

    /**
     * Get the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAnomalyAlertConfig#String}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     *
     * @return The {@link AnomalyAlertConfiguration} identified by the given id.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyAlertConfiguration getAnomalyAlertConfig(
        String alertConfigurationId) {
        return getAnomalyAlertConfigWithResponse(alertConfigurationId, Context.NONE).getValue();
    }

    /**
     * Get the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAnomalyAlertConfigWithResponse#String-Context}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response response} containing the {@link AnomalyAlertConfiguration} identified by the given id.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyAlertConfiguration> getAnomalyAlertConfigWithResponse(
        String alertConfigurationId, Context context) {
        return client.getAnomalyAlertConfigWithResponse(alertConfigurationId, context == null
            ? Context.NONE : context).block();
    }

    /**
     * Update anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAnomalyAlertConfig#AnomalyAlertConfiguration}
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
    public AnomalyAlertConfiguration updateAnomalyAlertConfig(
        AnomalyAlertConfiguration alertConfiguration) {
        return updateAnomalyAlertConfigWithResponse(alertConfiguration, Context.NONE).getValue();
    }

    /**
     * Update anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAnomalyAlertConfigWithResponse#AnomalyAlertConfiguration-Context}
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
    public Response<AnomalyAlertConfiguration> updateAnomalyAlertConfigWithResponse(
        AnomalyAlertConfiguration alertConfiguration, Context context) {
        return client.updateAnomalyAlertConfigWithResponse(alertConfiguration, context == null
            ? Context.NONE : context).block();
    }

    /**
     * Deletes the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAnomalyAlertConfig#String}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteAnomalyAlertConfig(String alertConfigurationId) {
        deleteAnomalyAlertConfigWithResponse(alertConfigurationId, Context.NONE);
    }

    /**
     * Deletes the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAnomalyAlertConfigWithResponse#String-Context}
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
    public Response<Void> deleteAnomalyAlertConfigWithResponse(String alertConfigurationId, Context context) {
        return client.deleteAnomalyAlertConfigWithResponse(alertConfigurationId, context == null
            ? Context.NONE : context).block();
    }

    /**
     * Fetch the anomaly alert configurations associated with a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAnomalyAlertConfigs#String-ListAnomalyAlertConfigsOptions}
     *
     * @param detectionConfigurationId The id of the detection configuration.
     * @param options th e additional configurable options to specify when querying the result.
     *
     * @return A {@link PagedIterable} containing information of all the
     * {@link AnomalyAlertConfiguration anomaly alert configurations} for the specified detection configuration.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyAlertConfiguration> listAnomalyAlertConfigs(
        String detectionConfigurationId, ListAnomalyAlertConfigsOptions options) {
        return listAnomalyAlertConfigs(detectionConfigurationId, options, Context.NONE);
    }

    /**
     * Fetch the anomaly alert configurations associated with a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAnomalyAlertConfigs#String-ListAnomalyAlertConfigsOptions-Context}
     *
     * @param detectionConfigurationId The id of the detection configuration.
     * @param options th e additional configurable options to specify when querying the result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the
     * {@link AnomalyAlertConfiguration anomaly alert configurations} for the specified detection configuration.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyAlertConfiguration> listAnomalyAlertConfigs(
        String detectionConfigurationId, ListAnomalyAlertConfigsOptions options, Context context) {
        return new PagedIterable<>(client.listAnomalyAlertConfigs(detectionConfigurationId, options,
            context == null ? Context.NONE : context));
    }

    /**
     * Create a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDatasourceCredential#DatasourceCredentialEntity}
     *
     * @param datasourceCredential The credential entity.
     * @return The created {@link DatasourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DatasourceCredentialEntity createDatasourceCredential(DatasourceCredentialEntity datasourceCredential) {
        return createDatasourceCredentialWithResponse(datasourceCredential, Context.NONE).getValue();
    }

    /**
     * Create a data source credential entity with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDatasourceCredentialWithResponse#DatasourceCredentialEntity-Context}
     *
     * @param datasourceCredential The credential entity.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the created {@link DatasourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DatasourceCredentialEntity> createDatasourceCredentialWithResponse(
        DatasourceCredentialEntity datasourceCredential, Context context) {
        return client.createDatasourceCredentialWithResponse(datasourceCredential, context).block();
    }

    /**
     * Get a data source credential entity by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDatasourceCredential#String}
     *
     * @param credentialId The data source credential entity unique id.
     *
     * @return The data source credential entity for the provided id.
     * @throws IllegalArgumentException If {@code credentialId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code credentialId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DatasourceCredentialEntity getDatasourceCredential(String credentialId) {
        return getDatasourceCredentialWithResponse(credentialId, Context.NONE).getValue();
    }

    /**
     * Get a data source credential entity by its id with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDatasourceCredentialWithResponse#String-Context}
     *
     * @param credentialId The data source credential entity unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The data feed for the provided id.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DatasourceCredentialEntity> getDatasourceCredentialWithResponse(String credentialId,
                                                                                    Context context) {
        return client.getDatasourceCredentialWithResponse(credentialId, context).block();
    }

    /**
     * Update a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDatasourceCredential#DatasourceCredentialEntity}
     *
     * @param datasourceCredential The credential entity.
     *
     * @return The updated {@link DatasourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DatasourceCredentialEntity updateDatasourceCredential(DatasourceCredentialEntity datasourceCredential) {
        return updateDatasourceCredentialWithResponse(datasourceCredential, Context.NONE).getValue();
    }

    /**
     * Update a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDatasourceCredentialWithResponse#DatasourceCredentialEntity-Context}
     *
     * @param datasourceCredential The credential entity.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the updated {@link DatasourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DatasourceCredentialEntity> updateDatasourceCredentialWithResponse(
        DatasourceCredentialEntity datasourceCredential, Context context) {
        return client.updateDatasourceCredentialWithResponse(datasourceCredential, context).block();
    }

    /**
     * Delete a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDatasourceCredential#String}
     *
     * @param credentialId The data source credential entity unique id.
     *
     * @throws IllegalArgumentException If {@code credentialId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code credentialId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDatasourceCredential(String credentialId) {
        deleteDatasourceCredentialWithResponse(credentialId, Context.NONE);
    }

    /**
     * Delete a data source credential entity with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDatasourceCredentialWithResponse#String-Context}
     *
     * @param credentialId The data source credential entity unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return a REST Response.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDatasourceCredentialWithResponse(String credentialId, Context context) {
        return client.deleteDataFeedWithResponse(credentialId, context).block();
    }

    /**
     * List information of all data source credential entities on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDatasourceCredentials}
     *
     * @return A {@link PagedIterable} containing information of all the {@link DatasourceCredentialEntity}
     * in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DatasourceCredentialEntity> listDatasourceCredentials() {
        return listDatasourceCredentials(null, Context.NONE);
    }

    /**
     * List information of all data source credential entities on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDatasourceCredentials#ListCredentialEntityOptions-Context}
     *
     * @param options The configurable {@link ListCredentialEntityOptions options} to pass for filtering the output
     * result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the {@link DatasourceCredentialEntity}
     * in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DatasourceCredentialEntity> listDatasourceCredentials(
        ListCredentialEntityOptions options, Context context) {
        return new PagedIterable<>(client.listDatasourceCredentials(options, context));
    }
}
