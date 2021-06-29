// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AnomalyAlert;
import com.azure.ai.metricsadvisor.models.AnomalyIncident;
import com.azure.ai.metricsadvisor.models.DataPointAnomaly;
import com.azure.ai.metricsadvisor.models.ChangePointValue;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.FeedbackQueryTimeMode;
import com.azure.ai.metricsadvisor.models.IncidentRootCause;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedFilter;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListAnomalyDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackFilter;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyFeedback;
import com.azure.ai.metricsadvisor.models.MetricChangePointFeedback;
import com.azure.ai.metricsadvisor.models.MetricCommentFeedback;
import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.ai.metricsadvisor.models.MetricPeriodFeedback;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.administration.models.AnomalySeverity;
import com.azure.ai.metricsadvisor.models.AlertQueryTimeMode;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.IterableStream;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.ai.metricsadvisor.models.FeedbackType.ANOMALY;
import static com.azure.ai.metricsadvisor.models.FeedbackType.COMMENT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.PERIOD;

/**
 * Code snippet for {@link MetricsAdvisorClient}
 */
public class MetricsAdvisorClientJavaDocCodeSnippets {
    MetricsAdvisorClient metricsAdvisorClient =
        new MetricsAdvisorClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link MetricsAdvisorClient}
     */
    public void createMetricsAdvisorAsyncClient() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation
        MetricsAdvisorClient metricsAdvisorClient =
            new MetricsAdvisorClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .buildClient();
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation
    }

    /**
     * Code snippet for creating a {@link MetricsAdvisorClient} with pipeline
     */
    public void createMetricsAdvisorAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        MetricsAdvisorClient metricsAdvisorClient =
            new MetricsAdvisorClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildClient();
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.pipeline.instantiation
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricSeriesDefinitions(String, OffsetDateTime)}
     */
    public void listMetricSeriesDefinitions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-OffsetDateTime
        final OffsetDateTime activeSince = OffsetDateTime.parse("2020-07-10T00:00:00Z");
        metricsAdvisorClient.listMetricSeriesDefinitions(
            "metricId",
            activeSince)
            .forEach(metricSeriesDefinition -> {
                System.out.printf("Data Feed Metric id for the retrieved series definition : %s%n",
                    metricSeriesDefinition.getMetricId());
                System.out.printf("Data Feed Metric dimension: %s%n", metricSeriesDefinition.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-OffsetDateTime
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricSeriesDefinitions(String, OffsetDateTime, ListMetricSeriesDefinitionOptions, Context)}
     */
    public void listMetricSeriesDefinitionsWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-OffsetDateTime-ListMetricSeriesDefinitionOptions-Context
        String metricId = "b460abfc-7a58-47d7-9d99-21ee21fdfc6e";
        final OffsetDateTime activeSince = OffsetDateTime.parse("2020-07-10T00:00:00Z");
        final ListMetricSeriesDefinitionOptions options
            = new ListMetricSeriesDefinitionOptions()
            .setMaxPageSize(10)
            .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                    put("Dim2", Collections.singletonList("Angelfish"));
                }});

        metricsAdvisorClient.listMetricSeriesDefinitions(metricId, activeSince, options, Context.NONE)
            .forEach(metricSeriesDefinition -> {
                System.out.printf("Data Feed Metric id for the retrieved series definition : %s%n",
                    metricSeriesDefinition.getMetricId());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesDefinition.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-OffsetDateTime-ListMetricSeriesDefinitionOptions-Context
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricSeriesData(String, List, OffsetDateTime, OffsetDateTime)}
     */
    public void listMetricSeriesData() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");

        metricsAdvisorClient.listMetricSeriesData("metricId",
            Arrays.asList(new DimensionKey(new HashMap<String, String>() {{
                    put("Dim1", "value1");
                }})), startTime, endTime)
            .forEach(metricSeriesData -> {
                System.out.println("List of data points for this series:");
                System.out.println(metricSeriesData.getMetricValues());
                System.out.println("Timestamps of the data related to this time series:");
                System.out.println(metricSeriesData.getTimestamps());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesData.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricSeriesData(String, List, OffsetDateTime, OffsetDateTime, Context)}
     */
    public void listMetricSeriesDataWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime-Context
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        metricsAdvisorClient.listMetricSeriesData("metricId",
            Arrays.asList(new DimensionKey(new HashMap<String, String>() {{
                    put("Dim1", "value1");
                }})), startTime, endTime)
            .forEach(metricSeriesData -> {
                System.out.printf("Data feed Id: %s%n", metricSeriesData.getMetricId());
                System.out.printf("Data feed description: %s%n", metricSeriesData.getSeriesKey());
                System.out.printf("Data feed source type: %.2f%n", metricSeriesData.getTimestamps());
                System.out.printf("Data feed creator: %.2f%n", metricSeriesData.getMetricValues());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricDimensionValues(String, String)}
     */
    public void listMetricDimensionValues() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String
        final String metricId = "gh3014a0-41ec-a637-677e77b81455";
        metricsAdvisorClient.listMetricDimensionValues(metricId, "category")
            .forEach(System.out::println);
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricDimensionValues(String, String, ListMetricDimensionValuesOptions, Context)}
     */
    public void listMetricDimensionValuesWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions-Context
        final String metricId = "gh3014a0-41ec-a637-677e77b81455";
        metricsAdvisorClient.listMetricDimensionValues(metricId, "category",
            new ListMetricDimensionValuesOptions().setDimensionValueToFilter("Electronics")
                .setMaxPageSize(3), Context.NONE)
            .forEach(System.out::println);
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentsForAlert(String, String)}.
     */
    public void listIncidentsForAlert() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";

        PagedIterable<AnomalyIncident> incidentsIterable = metricsAdvisorClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId);

        Stream<PagedResponse<AnomalyIncident>> incidentsPageStream = incidentsIterable.streamByPage();
        int[] pageCount = new int[1];
        incidentsPageStream.forEach(incidentsPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<AnomalyIncident> incidentsPageItems = incidentsPage.getElements();
            for (AnomalyIncident anomalyIncident : incidentsPageItems) {
                System.out.printf("Data Feed Metric Id: %s%n", anomalyIncident.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomalyIncident.getDetectionConfigurationId());
                System.out.printf("Anomaly Incident Id: %s%n", anomalyIncident.getId());
                System.out.printf("Anomaly Incident Start Time: %s%n", anomalyIncident.getStartTime());
                System.out.printf("Anomaly Incident AnomalySeverity: %s%n", anomalyIncident.getSeverity());
                System.out.printf("Anomaly Incident Status: %s%n", anomalyIncident.getStatus());
                System.out.printf("Root DataFeedDimension Key:");
                DimensionKey rootDimension = anomalyIncident.getRootDimensionKey();
                for (Map.Entry<String, String> dimension : rootDimension.asMap().entrySet()) {
                    System.out.printf("DimensionKey: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentsForAlert(String, String, ListIncidentsAlertedOptions, Context)}.
     */
    public void listIncidentsForAlertWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions-Context
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions()
            .setMaxPageSize(10);

        PagedIterable<AnomalyIncident> incidentsIterable = metricsAdvisorClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId,
            options,
            Context.NONE);

        Stream<PagedResponse<AnomalyIncident>> incidentsPageStream = incidentsIterable.streamByPage();
        int[] pageCount = new int[1];
        incidentsPageStream.forEach(incidentsPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<AnomalyIncident> incidentsPageItems = incidentsPage.getElements();
            for (AnomalyIncident anomalyIncident : incidentsPageItems) {
                System.out.printf("Data Feed Metric Id: %s%n", anomalyIncident.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomalyIncident.getDetectionConfigurationId());
                System.out.printf("Anomaly Incident Id: %s%n", anomalyIncident.getId());
                System.out.printf("Anomaly Incident Start Time: %s%n", anomalyIncident.getStartTime());
                System.out.printf("Anomaly Incident AnomalySeverity: %s%n", anomalyIncident.getSeverity());
                System.out.printf("Anomaly Incident Status: %s%n", anomalyIncident.getStatus());
                System.out.printf("Root DataFeedDimension Key:");
                DimensionKey rootDimension = anomalyIncident.getRootDimensionKey();
                for (Map.Entry<String, String> dimension : rootDimension.asMap().entrySet()) {
                    System.out.printf("DimensionKey: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAnomaliesForAlert(String, String)}
     */
    public void listAnomaliesForAlert() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        PagedIterable<DataPointAnomaly> anomaliesIterable = metricsAdvisorClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId
        );

        for (DataPointAnomaly dataPointAnomaly : anomaliesIterable) {
            System.out.printf("Data Feed Metric Id: %s%n", dataPointAnomaly.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", dataPointAnomaly.getDetectionConfigurationId());
            System.out.printf("DataPoint Anomaly Created Time: %s%n", dataPointAnomaly.getCreatedTime());
            System.out.printf("DataPoint Anomaly Modified Time: %s%n", dataPointAnomaly.getModifiedTime());
            System.out.printf("DataPoint Anomaly AnomalySeverity: %s%n", dataPointAnomaly.getSeverity());
            System.out.printf("DataPoint Anomaly Status: %s%n", dataPointAnomaly.getStatus());
            System.out.printf("Series Key:");
            System.out.println(dataPointAnomaly.getSeriesKey().asMap());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAnomaliesForAlert(String, String, ListAnomaliesAlertedOptions, Context)}.
     */
    public void listAnomaliesForAlertWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions-Context
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions()
            .setMaxPageSize(10);
        PagedIterable<DataPointAnomaly> anomaliesIterable = metricsAdvisorClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId,
            options,
            Context.NONE);

        Stream<PagedResponse<DataPointAnomaly>> anomaliesPageStream = anomaliesIterable.streamByPage();
        int[] pageCount = new int[1];
        anomaliesPageStream.forEach(anomaliesPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<DataPointAnomaly> anomaliesPageItems = anomaliesPage.getElements();
            for (DataPointAnomaly dataPointAnomaly : anomaliesPageItems) {
                System.out.printf("Data Feed Metric Id: %s%n", dataPointAnomaly.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", dataPointAnomaly.getDetectionConfigurationId());
                System.out.printf("DataPoint Anomaly Created Time: %s%n", dataPointAnomaly.getCreatedTime());
                System.out.printf("DataPoint Anomaly Modified Time: %s%n", dataPointAnomaly.getModifiedTime());
                System.out.printf("DataPoint Anomaly AnomalySeverity: %s%n", dataPointAnomaly.getSeverity());
                System.out.printf("DataPoint Anomaly Status: %s%n", dataPointAnomaly.getStatus());
                System.out.printf("Series Key:");
                System.out.println(dataPointAnomaly.getSeriesKey().asMap());
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAlerts(String, OffsetDateTime, OffsetDateTime)}.
     */
    public void listAlertForAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-OffsetDateTime-OffsetDateTime
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");

        PagedIterable<AnomalyAlert> alertsIterable
            = metricsAdvisorClient.listAlerts(alertConfigurationId, startTime, endTime);

        for (AnomalyAlert anomalyAlert : alertsIterable) {
            System.out.printf("Anomaly Alert Id: %s%n", anomalyAlert.getId());
            System.out.printf("Created Time: %s%n", anomalyAlert.getCreatedTime());
            System.out.printf("Modified Time: %s%n", anomalyAlert.getModifiedTime());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAlerts(String, OffsetDateTime, OffsetDateTime, ListAlertOptions, Context)}.
     */
    public void listAlertForAlertConfigurationWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-OffsetDateTime-OffsetDateTime-ListAlertOptions-Context
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions()
            .setAlertQueryTimeMode(timeMode)
            .setMaxPageSize(10);

        PagedIterable<AnomalyAlert> alertsIterable
            = metricsAdvisorClient.listAlerts(alertConfigurationId, startTime, endTime, options, Context.NONE);

        Stream<PagedResponse<AnomalyAlert>> alertsPageStream = alertsIterable.streamByPage();
        int[] pageCount = new int[1];
        alertsPageStream.forEach(alertsPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<AnomalyAlert> alertsPageItems = alertsPage.getElements();
            for (AnomalyAlert anomalyAlert : alertsPageItems) {
                System.out.printf("AnomalyAlert Id: %s%n", anomalyAlert.getId());
                System.out.printf("Created Time: %s%n", anomalyAlert.getCreatedTime());
                System.out.printf("Modified Time: %s%n", anomalyAlert.getModifiedTime());
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-OffsetDateTime-OffsetDateTime-ListAlertOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAnomalyDimensionValues(String, String, OffsetDateTime, OffsetDateTime)}.
     */
    public void listAnomalyDimensionValues() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String dimensionName = "Dim1";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");

        PagedIterable<String> dimensionValueIterable
            = metricsAdvisorClient.listAnomalyDimensionValues(detectionConfigurationId,
            dimensionName,
            startTime, endTime);

        for (String dimensionValue : dimensionValueIterable) {
            System.out.printf("DataFeedDimension Value: %s%n", dimensionValue);
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAnomalyDimensionValues(String, String, OffsetDateTime, OffsetDateTime, ListAnomalyDimensionValuesOptions, Context)}.
     */
    public void listAnomalyDimensionValuesWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime-ListAnomalyDimensionValuesOptions-Context
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String dimensionName = "Dim1";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListAnomalyDimensionValuesOptions options
            = new ListAnomalyDimensionValuesOptions()
            .setMaxPageSize(10);

        PagedIterable<String> dimensionValueIterable
            = metricsAdvisorClient.listAnomalyDimensionValues(detectionConfigurationId,
            dimensionName,
            startTime, endTime, options,
            Context.NONE);

        Stream<PagedResponse<String>> dimensionValuePageStream = dimensionValueIterable.streamByPage();
        int[] pageCount = new int[1];
        dimensionValuePageStream.forEach(dimensionValuePage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<String> dimensionValuePageItems = dimensionValuePage.getElements();
            for (String dimensionValue : dimensionValuePageItems) {
                System.out.printf("DataFeedDimension Value: %s%n", dimensionValue);
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime-ListAnomalyDimensionValuesOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentsForDetectionConfig(String, OffsetDateTime, OffsetDateTime)}.
     */
    public void listIncidentsForDetectionConfig() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");

        PagedIterable<AnomalyIncident> incidentsIterable
            = metricsAdvisorClient.listIncidentsForDetectionConfig(detectionConfigurationId, startTime, endTime);

        for (AnomalyIncident anomalyIncident : incidentsIterable) {
            System.out.printf("Data Feed Metric Id: %s%n", anomalyIncident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", anomalyIncident.getDetectionConfigurationId());
            System.out.printf("Anomaly Incident Id: %s%n", anomalyIncident.getId());
            System.out.printf("Anomaly Incident Start Time: %s%n", anomalyIncident.getStartTime());
            System.out.printf("Anomaly Incident AnomalySeverity: %s%n", anomalyIncident.getSeverity());
            System.out.printf("Anomaly Incident Status: %s%n", anomalyIncident.getStatus());
            System.out.printf("Root DataFeedDimension Key: %s%n", anomalyIncident.getRootDimensionKey().asMap());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentsForDetectionConfig(String, OffsetDateTime, OffsetDateTime, ListIncidentsDetectedOptions, Context)}.
     */
    public void listIncidentsForDetectionConfigWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListIncidentsDetectedOptions-Context
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions()
            .setMaxPageSize(1000);

        PagedIterable<AnomalyIncident> incidentsIterable
            = metricsAdvisorClient.listIncidentsForDetectionConfig(detectionConfigurationId,
                startTime, endTime, options,
            Context.NONE);

        Stream<PagedResponse<AnomalyIncident>> incidentsPageStream = incidentsIterable.streamByPage();

        int[] pageCount = new int[1];
        incidentsPageStream.forEach(incidentsPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<AnomalyIncident> pageElements = incidentsPage.getElements();
            for (AnomalyIncident anomalyIncident : pageElements) {
                System.out.printf("Data Feed Metric Id: %s%n", anomalyIncident.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomalyIncident.getDetectionConfigurationId());
                System.out.printf("Anomaly Incident Id: %s%n", anomalyIncident.getId());
                System.out.printf("Anomaly Incident Start Time: %s%n", anomalyIncident.getStartTime());
                System.out.printf("Anomaly Incident AnomalySeverity: %s%n", anomalyIncident.getSeverity());
                System.out.printf("Anomaly Incident Status: %s%n", anomalyIncident.getStatus());
                System.out.printf("Root DataFeedDimension Key:");
                System.out.printf("Root DataFeedDimension Key: %s%n", anomalyIncident.getRootDimensionKey().asMap());
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListIncidentsDetectedOptions-Context
    }

    /*
     * Code snippet for {@link MetricsAdvisorClient#listAnomaliesForDetectionConfig(String, ListAnomaliesDetectedOptions)}.
     */
    public void listAnomaliesForDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverityRange(AnomalySeverity.LOW, AnomalySeverity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions()
            .setMaxPageSize(10)
            .setFilter(filter);
        PagedIterable<DataPointAnomaly> anomaliesIterable
            = metricsAdvisorClient.listAnomaliesForDetectionConfig(detectionConfigurationId, startTime, endTime,
            options, Context.NONE);

        for (DataPointAnomaly dataPointAnomaly : anomaliesIterable) {
            System.out.printf("DataPointAnomaly AnomalySeverity: %s%n", dataPointAnomaly.getSeverity());
            System.out.printf("Series Key:");
            DimensionKey seriesKey = dataPointAnomaly.getSeriesKey();
            for (Map.Entry<String, String> dimension : seriesKey.asMap().entrySet()) {
                System.out.printf("DimensionName: %s DimensionValue:%s%n",
                    dimension.getKey(), dimension.getValue());
            }
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAnomaliesForDetectionConfig(String, OffsetDateTime, OffsetDateTime, ListAnomaliesDetectedOptions, Context)}.
     */
    public void listAnomaliesForDetectionConfigurationWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListAnomaliesDetectedOptions-Context
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverityRange(AnomalySeverity.LOW, AnomalySeverity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions()
            .setMaxPageSize(10)
            .setFilter(filter);
        PagedIterable<DataPointAnomaly> anomaliesIterable
            = metricsAdvisorClient.listAnomaliesForDetectionConfig(detectionConfigurationId,
                startTime, endTime, options,
            Context.NONE);

        Stream<PagedResponse<DataPointAnomaly>> anomaliesPageStream = anomaliesIterable.streamByPage();
        int[] pageCount = new int[1];
        anomaliesPageStream.forEach(anomaliesPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<DataPointAnomaly> anomaliesPageItems = anomaliesPage.getElements();
            for (DataPointAnomaly dataPointAnomaly : anomaliesPageItems) {
                System.out.printf("DataPoint Anomaly AnomalySeverity: %s%n", dataPointAnomaly.getSeverity());
                System.out.printf("Series Key:");
                DimensionKey seriesKey = dataPointAnomaly.getSeriesKey();
                for (Map.Entry<String, String> dimension : seriesKey.asMap().entrySet()) {
                    System.out.printf("DimensionName: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListAnomaliesDetectedOptions-Context
    }

    /*
     * Code snippet for {@link MetricsAdvisorClient#addFeedback(String, MetricFeedback)}.
     */
    public void createMetricFeedback() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.addFeedback#String-MetricFeedback
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT);

        final MetricFeedback metricFeedback
            = metricsAdvisorClient.addFeedback(metricId, metricChangePointFeedback);

        MetricChangePointFeedback createdMetricChangePointFeedback = (MetricChangePointFeedback) metricFeedback;
        System.out.printf("Data Feed Metric feedback Id: %s%n", createdMetricChangePointFeedback.getId());
        System.out.printf("Data Feed Metric feedback change point value: %s%n",
            createdMetricChangePointFeedback.getChangePointValue().toString());
        System.out.printf("Data Feed Metric feedback start time: %s%n",
            createdMetricChangePointFeedback.getStartTime());
        System.out.printf("Data Feed Metric feedback end time: %s%n",
            createdMetricChangePointFeedback.getEndTime());
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.addFeedback#String-MetricFeedback
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#addFeedbackWithResponse(String, MetricFeedback, Context)}.
     */
    public void createMetricFeedbackWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.addFeedbackWithResponse#String-MetricFeedback-Context
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT);

        final Response<MetricFeedback> metricFeedbackResponse
            = metricsAdvisorClient.addFeedbackWithResponse(metricId, metricChangePointFeedback, Context.NONE);

        System.out.printf("Data Feed Metric feedback creation operation status %s%n",
            metricFeedbackResponse.getStatusCode());
        MetricChangePointFeedback createdMetricChangePointFeedback
            = (MetricChangePointFeedback) metricFeedbackResponse.getValue();
        System.out.printf("Data Feed Metric feedback Id: %s%n", createdMetricChangePointFeedback.getId());
        System.out.printf("Data Feed Metric feedback change point value: %s%n",
            createdMetricChangePointFeedback.getChangePointValue().toString());
        System.out.printf("Data Feed Metric feedback start time: %s%n",
            createdMetricChangePointFeedback.getStartTime());
        System.out.printf("Data Feed Metric feedback end time: %s%n",
            createdMetricChangePointFeedback.getEndTime());
        System.out.printf("Data Feed Metric feedback associated dimension filter: %s%n",
            createdMetricChangePointFeedback.getDimensionFilter().asMap());
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.addFeedbackWithResponse#String-MetricFeedback-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#getFeedback(String)}.
     */
    public void getMetricFeedback() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.getFeedback#String

        final String feedbackId = "8i3h4i4-b804-4ab9-a70f-0da0c89cft3l";
        final MetricFeedback metricFeedback = metricsAdvisorClient.getFeedback(feedbackId);
        System.out.printf("Data Feed Metric feedback Id: %s%n", metricFeedback.getId());
        System.out.printf("Data Feed Metric feedback associated dimension filter: %s%n",
            metricFeedback.getDimensionFilter().asMap());

        if (PERIOD.equals(metricFeedback.getFeedbackType())) {
            MetricPeriodFeedback createMetricPeriodFeedback
                = (MetricPeriodFeedback) metricFeedback;
            System.out.printf("Data Feed Metric feedback type: %s%n",
                createMetricPeriodFeedback.getPeriodType().toString());
            System.out.printf("Data Feed Metric feedback period value: %d%n",
                createMetricPeriodFeedback.getPeriodValue());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.getFeedback#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#getFeedbackWithResponse(String, Context)}.
     */
    public void getMetricFeedbackWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.getFeedbackWithResponse#String-Context

        final String feedbackId = "8i3h4i4-b804-4ab9-a70f-0da0c89cft3l";
        final Response<MetricFeedback> metricFeedbackResponse
            = metricsAdvisorClient.getFeedbackWithResponse(feedbackId, Context.NONE);
        final MetricFeedback metricFeedback = metricFeedbackResponse.getValue();
        System.out.printf("Data Feed Metric feedback Id: %s%n", metricFeedback.getId());
        System.out.printf("Data Feed Metric feedback associated dimension filter: %s%n",
            metricFeedback.getDimensionFilter().asMap());

        if (PERIOD.equals(metricFeedback.getFeedbackType())) {
            MetricPeriodFeedback createMetricPeriodFeedback
                = (MetricPeriodFeedback) metricFeedback;
            System.out.printf("Data Feed Metric feedback type: %s%n",
                createMetricPeriodFeedback.getPeriodType().toString());
            System.out.printf("Data Feed Metric feedback period value: %d%n",
                createMetricPeriodFeedback.getPeriodValue());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.getFeedbackWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listFeedback(String)}.
     */
    public void listMetricFeedbacks() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listFeedback#String
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        metricsAdvisorClient.listFeedback(metricId)
            .forEach(metricFeedback -> {
                System.out.printf("Data Feed Metric feedback Id: %s%n", metricFeedback.getId());
                System.out.printf("Data Feed Metric feedback associated dimension filter: %s%n",
                    metricFeedback.getDimensionFilter().asMap());

                if (PERIOD.equals(metricFeedback.getFeedbackType())) {
                    MetricPeriodFeedback periodFeedback
                        = (MetricPeriodFeedback) metricFeedback;
                    System.out.printf("Data Feed Metric feedback type: %s%n",
                        periodFeedback.getPeriodType().toString());
                    System.out.printf("Data Feed Metric feedback period value: %d%n",
                        periodFeedback.getPeriodValue());
                } else if (ANOMALY.equals(metricFeedback.getFeedbackType())) {
                    MetricAnomalyFeedback metricAnomalyFeedback
                        = (MetricAnomalyFeedback) metricFeedback;
                    System.out.printf("Data Feed Metric feedback anomaly value: %s%n",
                        metricAnomalyFeedback.getAnomalyValue().toString());
                    System.out.printf("Data Feed Metric feedback associated detection configuration: %s%n",
                        metricAnomalyFeedback.getDetectionConfigurationId());
                } else if (COMMENT.equals(metricFeedback.getFeedbackType())) {
                    MetricCommentFeedback metricCommentFeedback
                        = (MetricCommentFeedback) metricFeedback;
                    System.out.printf("Data Feed Metric feedback comment value: %s%n",
                        metricCommentFeedback.getComment());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listFeedback#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listFeedback(String, ListMetricFeedbackOptions, Context)}.
     */
    public void listMetricFeedbacksWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listFeedback#String-ListMetricFeedbackOptions-Context
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");

        metricsAdvisorClient.listFeedback(metricId,
            new ListMetricFeedbackOptions()
                .setFilter(new ListMetricFeedbackFilter()
                    .setStartTime(startTime)
                    .setTimeMode(FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME)
                    .setEndTime(endTime)), Context.NONE)
            .forEach(metricFeedback -> {
                System.out.printf("Data Feed Metric feedback Id: %s%n", metricFeedback.getId());
                System.out.printf("Data Feed Metric feedback associated dimension filter: %s%n",
                    metricFeedback.getDimensionFilter().asMap());
                System.out.printf("Data Feed Metric feedback created time %s%n", metricFeedback.getCreatedTime());

                if (PERIOD.equals(metricFeedback.getFeedbackType())) {
                    MetricPeriodFeedback periodFeedback
                        = (MetricPeriodFeedback) metricFeedback;
                    System.out.printf("Data Feed Metric feedback type: %s%n",
                        periodFeedback.getPeriodType().toString());
                    System.out.printf("Data Feed Metric feedback period value: %d%n",
                        periodFeedback.getPeriodValue());
                } else if (ANOMALY.equals(metricFeedback.getFeedbackType())) {
                    MetricAnomalyFeedback metricAnomalyFeedback
                        = (MetricAnomalyFeedback) metricFeedback;
                    System.out.printf("Data Feed Metric feedback anomaly value: %s%n",
                        metricAnomalyFeedback.getAnomalyValue().toString());
                    System.out.printf("Data Feed Metric feedback associated detection configuration: %s%n",
                        metricAnomalyFeedback.getDetectionConfigurationId());
                } else if (COMMENT.equals(metricFeedback.getFeedbackType())) {
                    MetricCommentFeedback metricCommentFeedback
                        = (MetricCommentFeedback) metricFeedback;
                    System.out.printf("Data Feed Metric feedback comment value: %s%n",
                        metricCommentFeedback.getComment());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listFeedback#String-ListMetricFeedbackOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentRootCauses(String, String)}.
     */
    public void listIncidentRootCauses() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String
        final String detectionConfigurationId = "c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String incidentId = "c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d";

        metricsAdvisorClient.listIncidentRootCauses(detectionConfigurationId, incidentId)
            .forEach(incidentRootCause -> {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.printf("Series Key:");
                System.out.println(incidentRootCause.getSeriesKey().asMap());
                System.out.printf("Confidence for the detected incident root cause %.2f%n",
                    incidentRootCause.getContributionScore());
            });

        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentRootCauses(String, String)}.
     */
    public void listIncidentRootCausesWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String-Context
        final String detectionConfigurationId = "c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String incidentId = "c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d";

        PagedIterable<IncidentRootCause> rootCauseIterable
            = metricsAdvisorClient.listIncidentRootCauses(detectionConfigurationId, incidentId, Context.NONE);
        Stream<PagedResponse<IncidentRootCause>> rootCausePageIterable = rootCauseIterable.streamByPage();
        rootCausePageIterable.forEach(response -> {
            System.out.printf("Response StatusCode: %s%n", response.getStatusCode());
            IterableStream<IncidentRootCause> pageElements = response.getElements();
            for (IncidentRootCause incidentRootCause : pageElements) {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.printf("Series Key:");
                System.out.println(incidentRootCause.getSeriesKey().asMap());
                System.out.printf("Confidence for the detected incident root cause %.2f%n",
                    incidentRootCause.getContributionScore());
            }
        });

        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentRootCauses(AnomalyIncident)}.
     */
    public void listIncidentRootCausesWithIncident() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#AnomalyIncident
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");

        metricsAdvisorClient.listIncidentsForDetectionConfig(detectionConfigurationId, startTime, endTime)
            .forEach(incident -> {
                metricsAdvisorClient.listIncidentRootCauses(incident)
                    .forEach(incidentRootCause -> {
                        System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                        System.out.printf("Series Key:");
                        System.out.println(incidentRootCause.getSeriesKey().asMap());
                        System.out.printf("Confidence for the detected incident root cause %.2f%n",
                            incidentRootCause.getContributionScore());
                    });
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#AnomalyIncident
    }

    /*
     * Code snippet for {@link MetricsAdvisorClient#listMetricEnrichmentStatus(String, ListMetricEnrichmentStatusOptions)}.
     */
    public void listMetricEnrichmentStatus() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";

        metricsAdvisorClient.listMetricEnrichmentStatus(metricId, startTime, endTime)
            .forEach(enrichmentStatus -> {
                System.out.printf("Data Feed Metric enrichment status : %s%n", enrichmentStatus.getStatus());
                System.out.printf("Data Feed Metric enrichment status message: %s%n", enrichmentStatus.getMessage());
                System.out.printf("Data Feed Metric enrichment status data slice timestamp : %s%n",
                    enrichmentStatus.getTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricEnrichmentStatus(String, OffsetDateTime, OffsetDateTime, ListMetricEnrichmentStatusOptions, Context)}.
     */
    public void listMetricEnrichmentStatusWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime-ListMetricEnrichmentStatusOptions-Context
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final ListMetricEnrichmentStatusOptions options = new ListMetricEnrichmentStatusOptions().setMaxPageSize(10);

        metricsAdvisorClient.listMetricEnrichmentStatus(metricId, startTime, endTime, options, Context.NONE)
            .forEach(enrichmentStatus -> {
                System.out.printf("Data Feed Metric enrichment status : %s%n", enrichmentStatus.getStatus());
                System.out.printf("Data Feed Metric enrichment status message: %s%n", enrichmentStatus.getMessage());
                System.out.printf("Data Feed Metric enrichment status data slice timestamp : %s%n",
                    enrichmentStatus.getTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime-ListMetricEnrichmentStatusOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricEnrichedSeriesData(String, List, OffsetDateTime, OffsetDateTime)}.
     */
    public void listEnrichedSeries() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "e87d899d-a5a0-4259-b752-11aea34d5e34";
        final DimensionKey seriesKey = new DimensionKey()
            .put("Dim1", "Common Lime")
            .put("Dim2", "Antelope");
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-08-12T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-12T00:00:00Z");

        PagedIterable<MetricEnrichedSeriesData> enrichedDataIterable
            = metricsAdvisorClient.listMetricEnrichedSeriesData(detectionConfigurationId,
            Arrays.asList(seriesKey),
            startTime,
            endTime);

        for (MetricEnrichedSeriesData enrichedData : enrichedDataIterable) {
            System.out.printf("Series Key %s%n:", enrichedData.getSeriesKey().asMap());
            System.out.println("List of data points for this series");
            System.out.println(enrichedData.getMetricValues());
            System.out.println("Timestamps of the data related to this time series:");
            System.out.println(enrichedData.getTimestamps());
            System.out.println("The expected values of the data points calculated by the smart detector:");
            System.out.println(enrichedData.getExpectedMetricValues());
            System.out.println("The lower boundary values of the data points calculated by smart detector:");
            System.out.println(enrichedData.getLowerBoundaryValues());
            System.out.println("the periods calculated for the data points in the time series:");
            System.out.println(enrichedData.getPeriods());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricEnrichedSeriesData(String, List, OffsetDateTime, OffsetDateTime, Context)}.
     */
    public void listEnrichedSeriesWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime-Context
        final String detectionConfigurationId = "e87d899d-a5a0-4259-b752-11aea34d5e34";
        final DimensionKey seriesKey = new DimensionKey()
            .put("Dim1", "Common Lime")
            .put("Dim2", "Antelope");
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-08-12T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-12T00:00:00Z");

        PagedIterable<MetricEnrichedSeriesData> enrichedDataIterable
            = metricsAdvisorClient.listMetricEnrichedSeriesData(detectionConfigurationId,
            Arrays.asList(seriesKey),
            startTime,
            endTime);

        Stream<PagedResponse<MetricEnrichedSeriesData>> enrichedDataPageStream
            = enrichedDataIterable.streamByPage();
        int[] pageCount = new int[1];
        enrichedDataPageStream.forEach(enrichedDataPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<MetricEnrichedSeriesData> pageElements = enrichedDataPage.getElements();
            for (MetricEnrichedSeriesData enrichedData : pageElements) {
                System.out.printf("Series Key %s%n:", enrichedData.getSeriesKey().asMap());
                System.out.println("List of data points for this series");
                System.out.println(enrichedData.getMetricValues());
                System.out.println("Timestamps of the data related to this time series:");
                System.out.println(enrichedData.getTimestamps());
                System.out.println("The expected values of the data points calculated by the smart detector:");
                System.out.println(enrichedData.getExpectedMetricValues());
                System.out.println("The lower boundary values of the data points calculated by smart detector:");
                System.out.println(enrichedData.getLowerBoundaryValues());
                System.out.println("the periods calculated for the data points in the time series:");
                System.out.println(enrichedData.getPeriods());
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime-Context
    }
}
