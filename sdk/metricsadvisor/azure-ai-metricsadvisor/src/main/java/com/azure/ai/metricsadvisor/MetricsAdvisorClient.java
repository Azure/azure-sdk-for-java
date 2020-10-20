// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder;
import com.azure.ai.metricsadvisor.models.Alert;
import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.EnrichmentStatus;
import com.azure.ai.metricsadvisor.models.ErrorCodeException;
import com.azure.ai.metricsadvisor.models.Incident;
import com.azure.ai.metricsadvisor.models.IncidentRootCause;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDataOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.ListValuesOfDimensionWithAnomaliesOptions;
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
 * <p><strong>Instantiating an synchronous Metric Advisor Client</strong></p>
 * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation}
 *
 * @see MetricsAdvisorClientBuilder
 */
@ServiceClient(builder = MetricsAdvisorClientBuilder.class)
public class MetricsAdvisorClient {

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
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-ListMetricSeriesDefinitionOptions}
     *
     * @param metricId metric unique id.
     * @param options the additional filtering attributes that can be provided to query the series.
     *
     * @return A {@link PagedIterable} of the {@link MetricSeriesDefinition metric series definitions}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code options.activeSince}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricSeriesDefinition> listMetricSeriesDefinitions(
        String metricId,
        ListMetricSeriesDefinitionOptions options) {
        return listMetricSeriesDefinitions(metricId, options, Context.NONE);
    }

    /**
     * List series (dimension combinations) from metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-ListMetricSeriesDefinitionOptions-Context}
     *
     * @param metricId metric unique id.
     * @param options the additional filtering attributes that can be provided to query the series.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} of the {@link MetricSeriesDefinition metric series definitions}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} or {@code options.activeSince}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricSeriesDefinition> listMetricSeriesDefinitions(
        String metricId,
        ListMetricSeriesDefinitionOptions options, Context context) {
        return new PagedIterable<>(client.listMetricSeriesDefinitions(metricId, options,
            context == null ? Context.NONE : context));
    }

    /**
     * Get time series data from metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-ListMetricSeriesDataOptions}
     *
     * @param metricId metric unique id.
     * @param seriesKeys the series key to filter.
     * <p>This enables additional filtering of dimension values being queried.
     * For example, let's say we've the dimensions 'category' and 'city',
     * so the api can query value of the dimension 'category', with series key as 'city=redmond'.
     * </p>
     * @param options query time series data condition.
     *
     * @return A {@link PagedIterable} of the {@link MetricSeriesData metric series data points}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws NullPointerException thrown if the {@code metricId}, {@code options.startTime} or {@code options.endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricSeriesData> listMetricSeriesData(String metricId,
        List<DimensionKey> seriesKeys, ListMetricSeriesDataOptions options) {
        return listMetricSeriesData(metricId, seriesKeys, options, Context.NONE);
    }

    /**
     * Get time series data from metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-ListMetricSeriesDataOptions-Context}
     *
     * @param metricId metric unique id.
     * @param seriesKeys the series key to filter.
     * <p>This enables additional filtering of dimension values being queried.
     * For example, let's say we've the dimensions 'category' and 'city',
     * so the api can query value of the dimension 'category', with series key as 'city=redmond'.
     * </p>
     * @param options query time series data condition.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} of the {@link MetricSeriesData metric series data points}.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws NullPointerException thrown if the {@code metricId}, {@code options.startTime} or {@code options.endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricSeriesData> listMetricSeriesData(String metricId, List<DimensionKey> seriesKeys,
        ListMetricSeriesDataOptions options, Context context) {
        return new PagedIterable<>(client.listMetricSeriesData(metricId, seriesKeys, options,
            context == null ? Context.NONE : context));
    }

    /**
     * List the enrichment status for a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-ListMetricEnrichmentStatusOptions}
     *
     * @param metricId metric unique id.
     * @param options th e additional configurable options to specify when querying the result..
     *
     * @return the list of enrichment status's for the specified metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if {@code metricId}, {@code options.startTime} and {@code options.endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<EnrichmentStatus> listMetricEnrichmentStatus(
        String metricId,
        ListMetricEnrichmentStatusOptions options) {
        return listMetricEnrichmentStatus(metricId, options, Context.NONE);
    }

    /**
     * List the enrichment status for a metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-ListMetricEnrichmentStatusOptions-Context}
     *
     * @param metricId metric unique id.
     * @param options th e additional configurable options to specify when querying the result..
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the list of enrichment status's for the specified metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if {@code metricId}, {@code options.startTime} and {@code options.endTime}
     * is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<EnrichmentStatus> listMetricEnrichmentStatus(
        String metricId,
        ListMetricEnrichmentStatusOptions options, Context context) {
        return new PagedIterable<>(client.listMetricEnrichmentStatus(metricId, options,
            context == null ? Context.NONE : context));
    }

    /**
     * Given a list of time series keys, retrieve time series version enriched using
     * a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#List-String-OffsetDateTime-OffsetDateTime}
     *
     * @param seriesKeys The time series key list, each key identifies a specific time series.
     * @param detectionConfigurationId The id of the configuration used to enrich the time series
     *     identified by the keys in {@code seriesKeys}.
     * @param startTime The start time.
     * @param endTime The end time.
     * @return The enriched time series.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation
     *     or if {@code seriesKeys} is empty.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricEnrichedSeriesData> listMetricEnrichedSeriesData(List<DimensionKey> seriesKeys,
                                                                                String detectionConfigurationId,
                                                                                OffsetDateTime startTime,
                                                                                OffsetDateTime endTime) {
        return listMetricEnrichedSeriesData(seriesKeys,
            detectionConfigurationId,
            startTime,
            endTime,
            Context.NONE);
    }

    /**
     * Given a list of time series keys, retrieve time series version enriched using
     * a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#List-String-OffsetDateTime-OffsetDateTime-Context}
     *
     * @param seriesKeys The time series key list, each key identifies a specific time series.
     * @param detectionConfigurationId The id of the configuration used to enrich the time series
     *     identified by the keys in {@code seriesKeys}.
     * @param startTime The start time.
     * @param endTime The end time.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The enriched time series.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation
     *     or if {@code seriesKeys} is empty.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId}
     *     or {@code startTime} or {@code endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricEnrichedSeriesData> listMetricEnrichedSeriesData(
        List<DimensionKey> seriesKeys,
        String detectionConfigurationId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Context context) {
        return new PagedIterable<>(client.listMetricEnrichedSeriesData(seriesKeys,
            detectionConfigurationId,
            startTime,
            endTime,
            context == null ? Context.NONE : context));
    }

    /**
     * Fetch the anomalies identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfiguration#String-ListAnomaliesDetectedOptions}
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param options The additional parameters.
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification
     *     or {@code options.filter} is used to set severity but either min or max severity is missing.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code options}
     *     or {@code options.startTime} or {@code options.endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Anomaly> listAnomaliesForDetectionConfiguration(
        String detectionConfigurationId,
        ListAnomaliesDetectedOptions options) {
        return listAnomaliesForDetectionConfiguration(detectionConfigurationId, options, Context.NONE);
    }

    /**
     * Fetch the anomalies identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfiguration#String-ListAnomaliesDetectedOptions-Context}
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification
     *     or {@code options.filter} is used to set severity but either min or max severity is missing.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code options}
     *     or {@code options.startTime} or {@code options.endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Anomaly> listAnomaliesForDetectionConfiguration(
        String detectionConfigurationId,
        ListAnomaliesDetectedOptions options, Context context) {
        return new PagedIterable<>(client.listAnomaliesForDetectionConfiguration(detectionConfigurationId,
            options,
            context == null ? Context.NONE : context));
    }

    /**
     * Fetch the incidents identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfiguration#String-ListIncidentsDetectedOptions}
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param options The additional parameters.
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code options}
     *     or {@code options.startTime} or {@code options.endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Incident> listIncidentsForDetectionConfiguration(
        String detectionConfigurationId,
        ListIncidentsDetectedOptions options) {
        return listIncidentsForDetectionConfiguration(detectionConfigurationId, options, Context.NONE);
    }

    /**
     * Fetch the incidents identified by an anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfiguration#String-ListIncidentsDetectedOptions-Context}
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     *     to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code options}
     *     or {@code options.startTime} or {@code options.endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Incident> listIncidentsForDetectionConfiguration(
        String detectionConfigurationId,
        ListIncidentsDetectedOptions options, Context context) {
        return new PagedIterable<>(client.listIncidentsForDetectionConfiguration(detectionConfigurationId,
            options,
            context == null ? Context.NONE : context));
    }

    /**
     * List the root causes for an incident.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String}
     *
     * @param detectionConfigurationId anomaly detection configuration unique id.
     * @param incidentId the incident for which you want to query root causes for.
     *
     * @return the list of root causes for that incident.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
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
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String-Context}
     *
     * @param detectionConfigurationId anomaly detection configuration unique id.
     * @param incidentId the incident for which you want to query root causes for.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return the list of root causes for that incident.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code incidentId} is null.
     **/
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IncidentRootCause> listIncidentRootCauses(
        String detectionConfigurationId,
        String incidentId, Context context) {
        return new PagedIterable<>(client.listIncidentRootCauses(detectionConfigurationId, incidentId, context));
    }

    /**
     * List the root causes for an incident.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#Incident}
     *
     * @param incident the incident for which you want to query root causes for.
     *
     * @return the list of root causes for that incident.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code incidentId} is null.
     **/
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<IncidentRootCause> listIncidentRootCauses(Incident incident) {
        return new PagedIterable<>(client.listIncidentRootCauses(incident, Context.NONE));
    }

    /**
     * Fetch the values of a dimension that have anomalies.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listValuesOfDimensionWithAnomalies#String-String-ListValuesOfDimensionWithAnomaliesOptions}
     *
     * @param detectionConfigurationId Identifies the configuration used to detect the anomalies.
     * @param dimensionName The dimension name to retrieve the values for.
     * @param options The additional parameters.
     *
     * @return The dimension values with anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     * to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code dimensionName}
     * or {@code options} or {@code options.startTime} or {@code options.endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listValuesOfDimensionWithAnomalies(
        String detectionConfigurationId,
        String dimensionName,
        ListValuesOfDimensionWithAnomaliesOptions options) {
        return listValuesOfDimensionWithAnomalies(detectionConfigurationId,
            dimensionName, options, Context.NONE);
    }

    /**
     * Fetch the values of a dimension that have anomalies.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listValuesOfDimensionWithAnomalies#String-String-ListValuesOfDimensionWithAnomaliesOptions-Context}
     *
     * @param detectionConfigurationId Identifies the configuration used to detect the anomalies.
     * @param dimensionName The dimension name to retrieve the values for.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The dimension values with anomalies.
     * @throws IllegalArgumentException thrown if {@code detectionConfigurationId} does not conform
     * to the UUID format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} or {@code dimensionName}
     * or {@code options} or {@code options.startTime} or {@code options.endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<String> listValuesOfDimensionWithAnomalies(
        String detectionConfigurationId,
        String dimensionName,
        ListValuesOfDimensionWithAnomaliesOptions options, Context context) {
        return new PagedIterable<>(client.listValuesOfDimensionWithAnomalies(detectionConfigurationId,
            dimensionName, options, context == null ? Context.NONE : context));
    }

    /**
     * Fetch the alerts triggered by an anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-ListAlertOptions}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param options The additional parameters.
     *
     * @return The alerts.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} does not conform
     * to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code options}
     * or {@code options.startTime} or {@code options.endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Alert> listAlerts(
        String alertConfigurationId,
        ListAlertOptions options) {
        return listAlerts(alertConfigurationId, options, Context.NONE);
    }

    /**
     * Fetch the alerts triggered by an anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-ListAlertOptions-Context}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return The alerts.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} does not conform
     * to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code options}
     * or {@code options.startTime} or {@code options.endTime} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Alert> listAlerts(
        String alertConfigurationId,
        ListAlertOptions options, Context context) {
        return new PagedIterable<>(client.listAlerts(alertConfigurationId, options,
            context == null ? Context.NONE : context));
    }

    /**
     * Fetch the anomalies in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     * @return The anomalies.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     * conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Anomaly> listAnomaliesForAlert(
        String alertConfigurationId,
        String alertId) {
        return listAnomaliesForAlert(alertConfigurationId, alertId, null, Context.NONE);
    }

    /**
     * Fetch the anomalies in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions-Context}
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
    public PagedIterable<Anomaly> listAnomaliesForAlert(
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
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions}
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param alertId The alert id.
     * @param options The additional parameters.
     *
     * @return The incidents.
     * @throws IllegalArgumentException thrown if {@code alertConfigurationId} or {@code alertId} does not
     * conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} or {@code alertId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Incident> listIncidentsForAlert(
        String alertConfigurationId,
        String alertId,
        ListIncidentsAlertedOptions options) {
        return listIncidentsForAlert(alertConfigurationId, alertId, options, Context.NONE);
    }

    /**
     * Fetch the incidents in an alert.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions-Context}
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
    public PagedIterable<Incident> listIncidentsForAlert(
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
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.createMetricFeedback#String-MetricFeedback}
     *
     * @param metricId the unique id for which the feedback needs to be submitted.
     * @param metricFeedback the actual metric feedback.
     *
     * @return the created {@link MetricFeedback metric feedback}.
     * @throws NullPointerException If {@code metricId}, {@code metricFeedback.dimensionFilter} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricFeedback createMetricFeedback(String metricId, MetricFeedback metricFeedback) {
        return createMetricFeedbackWithResponse(metricId, metricFeedback, Context.NONE).getValue();
    }

    /**
     * Create a new metric feedback.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.createMetricFeedbackWithResponse#String-MetricFeedback-Context}
     *
     * @param metricId the unique id for which the feedback needs to be submitted.
     * @param metricFeedback the actual metric feedback.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the created {@link MetricFeedback metric feedback}.
     * @throws NullPointerException If {@code metricId}, {@code metricFeedback.dimensionFilter} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricFeedback> createMetricFeedbackWithResponse(String metricId, MetricFeedback metricFeedback,
        Context context) {
        return client.createMetricFeedbackWithResponse(metricId, metricFeedback, context).block();
    }

    /**
     * Get a metric feedback by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.getMetricFeedback#String}
     *
     * @param feedbackId The metric feedback unique id.
     *
     * @return The metric feedback for the provided id.
     * @throws IllegalArgumentException If {@code feedbackId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code feedbackId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public MetricFeedback getMetricFeedback(String feedbackId) {
        return getMetricFeedbackWithResponse(feedbackId, Context.NONE).getValue();
    }

    /**
     * Get a metric feedback by its id.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.getMetricFeedbackWithResponse#String-Context}
     *
     * @param feedbackId The metric feedback unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The metric feedback for the provided id.
     * @throws IllegalArgumentException If {@code feedbackId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code feedbackId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<MetricFeedback> getMetricFeedbackWithResponse(String feedbackId, Context context) {
        return client.getMetricFeedbackWithResponse(feedbackId, context).block();
    }

    /**
     * List information of all metric feedbacks on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricFeedbacks#String}
     *
     * @param metricId the unique metric Id.
     *
     * @return A {@link PagedIterable} containing information of all the {@link MetricFeedback metric feedbacks}
     * in the account.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricFeedback> listMetricFeedbacks(
        String metricId) {
        return listMetricFeedbacks(metricId, null, Context.NONE);
    }

    /**
     * List information of all metric feedbacks on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricFeedbacks#String-ListMetricFeedbackOptions-Context}
     *
     * @param metricId the unique metric Id.
     * @param options The configurable {@link ListMetricFeedbackOptions options} to pass for filtering the output
     * result.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the {@link MetricFeedback metric feedbacks}
     * in the account.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<MetricFeedback> listMetricFeedbacks(
        String metricId,
        ListMetricFeedbackOptions options, Context context) {
        return new PagedIterable<>(client.listMetricFeedbacks(metricId, options,
            context == null ? Context.NONE : context));
    }

    /**
     * List dimension values from certain metric.
     *
     * <p><strong>Code sample</strong></p>
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String}
     *
     * @param metricId metric unique id.
     * @param dimensionName the query dimension name.
     *
     * @return the {@link PagedIterable} of the dimension values for that metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
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
     * {@codesnippet com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions-Context}
     *
     * @param metricId metric unique id.
     * @param dimensionName the query dimension name.
     * @param options the additional parameters to specify while querying.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return the {@link PagedIterable} of the dimension values for that metric.
     * @throws IllegalArgumentException thrown if {@code metricId} fail the UUID format validation.
     * @throws ErrorCodeException thrown if the request is rejected by server.
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
