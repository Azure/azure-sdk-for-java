// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.Alert;
import com.azure.ai.metricsadvisor.models.Anomaly;
import com.azure.ai.metricsadvisor.models.ChangePointValue;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.FeedbackQueryTimeMode;
import com.azure.ai.metricsadvisor.models.Incident;
import com.azure.ai.metricsadvisor.models.IncidentRootCause;
import com.azure.ai.metricsadvisor.models.ListAlertOptions;
import com.azure.ai.metricsadvisor.models.ListAnomaliesAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedFilter;
import com.azure.ai.metricsadvisor.models.ListAnomaliesDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsAlertedOptions;
import com.azure.ai.metricsadvisor.models.ListIncidentsDetectedOptions;
import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.ListMetricEnrichmentStatusOptions;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackFilter;
import com.azure.ai.metricsadvisor.models.ListMetricFeedbackOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDataOptions;
import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.ListValuesOfDimensionWithAnomaliesOptions;
import com.azure.ai.metricsadvisor.models.MetricAnomalyFeedback;
import com.azure.ai.metricsadvisor.models.MetricChangePointFeedback;
import com.azure.ai.metricsadvisor.models.MetricCommentFeedback;
import com.azure.ai.metricsadvisor.models.MetricEnrichedSeriesData;
import com.azure.ai.metricsadvisor.models.MetricFeedback;
import com.azure.ai.metricsadvisor.models.MetricPeriodFeedback;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.ai.metricsadvisor.models.Severity;
import com.azure.ai.metricsadvisor.models.TimeMode;
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
    MetricsAdvisorClient metricAdvisorClient =
        new MetricsAdvisorClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link MetricsAdvisorClient}
     */
    public void createMetricAdvisorAsyncClient() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation
        MetricsAdvisorClient metricAdvisorAdministrationClient =
            new MetricsAdvisorClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .buildClient();
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation
    }

    /**
     * Code snippet for creating a {@link MetricsAdvisorClient} with pipeline
     */
    public void createMetricAdvisorAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        MetricsAdvisorClient metricAdvisorAdministrationClient =
            new MetricsAdvisorClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildClient();
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.pipeline.instantiation
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricSeriesDefinitions(String, ListMetricSeriesDefinitionOptions)}
     */
    public void listMetricSeriesDefinitions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-ListMetricSeriesDefinitionOptions

        metricAdvisorClient.listMetricSeriesDefinitions(
            "metricId",
            new ListMetricSeriesDefinitionOptions(OffsetDateTime.now())
                .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                        put("Dimension1", Arrays.asList("value1", "value2"));
                    }})
                .setTop(3))
            .forEach(metricSeriesDefinition -> {
                System.out.printf("Metric id for the retrieved series definition : %s%n",
                    metricSeriesDefinition.getMetricId());
                System.out.printf("Metric dimension: %s%n", metricSeriesDefinition.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-ListMetricSeriesDefinitionOptions
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricSeriesDefinitions(String, ListMetricSeriesDefinitionOptions, Context)}
     */
    public void listMetricSeriesDefinitionsWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-ListMetricSeriesDefinitionOptions-Context
        String metricId = "b460abfc-7a58-47d7-9d99-21ee21fdfc6e";
        final ListMetricSeriesDefinitionOptions options
            = new ListMetricSeriesDefinitionOptions(OffsetDateTime.parse("2020-07-10T00:00:00Z"))
            .setTop(10)
            .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                    put("Dim2", Collections.singletonList("Angelfish"));
                }});

        metricAdvisorClient.listMetricSeriesDefinitions(metricId, options, Context.NONE)
            .forEach(metricSeriesDefinition -> {
                System.out.printf("Metric id for the retrieved series definition : %s%n",
                    metricSeriesDefinition.getMetricId());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesDefinition.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesDefinitions#String-ListMetricSeriesDefinitionOptions-Context
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricSeriesData(String, List, ListMetricSeriesDataOptions)}
     */
    public void listMetricSeriesData() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-ListMetricSeriesDataOptions
        metricAdvisorClient.listMetricSeriesData("metricId",
            Arrays.asList(new DimensionKey(new HashMap<String, String>() {{
                    put("Dim1", "value1");
                }})),
            new ListMetricSeriesDataOptions(OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                OffsetDateTime.parse("2020-09-09T00:00:00Z")))
            .forEach(metricSeriesData -> {
                System.out.println("List of data points for this series:");
                System.out.println(metricSeriesData.getValueList());
                System.out.println("Timestamps of the data related to this time series:");
                System.out.println(metricSeriesData.getTimestampList());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesData.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-ListMetricSeriesDataOptions
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorClient#listMetricSeriesData(String, List, ListMetricSeriesDataOptions, Context)}
     */
    public void listMetricSeriesDataWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-ListMetricSeriesDataOptions-Context
        metricAdvisorClient.listMetricSeriesData("metricId",
            Arrays.asList(new DimensionKey(new HashMap<String, String>() {{
                    put("Dim1", "value1");
                }})),
            new ListMetricSeriesDataOptions(OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                OffsetDateTime.parse("2020-09-09T00:00:00Z")))
            .forEach(metricSeriesData -> {
                System.out.printf("Data feed Id: %s%n", metricSeriesData.getMetricId());
                System.out.printf("Data feed description: %s%n", metricSeriesData.getSeriesKey());
                System.out.printf("Data feed source type: %.2f%n", metricSeriesData.getTimestampList());
                System.out.printf("Data feed creator: %.2f%n", metricSeriesData.getValueList());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-ListMetricSeriesDataOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricDimensionValues(String, String)}
     */
    public void listMetricDimensionValues() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String
        final String metricId = "gh3014a0-41ec-a637-677e77b81455";
        metricAdvisorClient.listMetricDimensionValues(metricId, "category")
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
        metricAdvisorClient.listMetricDimensionValues(metricId, "category",
            new ListMetricDimensionValuesOptions().setDimensionValueToFilter("Electronics")
                .setTop(3), Context.NONE)
            .forEach(System.out::println);
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentsForAlert(String, String, ListIncidentsAlertedOptions)}.
     */
    public void listIncidentsForAlert() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions()
            .setTop(10);

        PagedIterable<Incident> incidentsIterable = metricAdvisorClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId,
            options);

        for (Incident incident : incidentsIterable) {
            System.out.printf("Metric Id: %s%n", incident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
            System.out.printf("Incident Id: %s%n", incident.getId());
            System.out.printf("Incident Start Time: %s%n", incident.getStartTime());
            System.out.printf("Incident Severity: %s%n", incident.getSeverity());
            System.out.printf("Incident Status: %s%n", incident.getStatus());
            System.out.printf("Root Dimension Key:");
            DimensionKey rootDimension = incident.getRootDimensionKey();
            for (Map.Entry<String, String> dimension : rootDimension.asMap().entrySet()) {
                System.out.printf("DimensionName: %s DimensionValue:%s%n",
                    dimension.getKey(), dimension.getValue());
            }
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentsForAlert(String, String, ListIncidentsAlertedOptions, Context)}.
     */
    public void listIncidentsForAlertWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions-Context
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions()
            .setTop(10);

        PagedIterable<Incident> incidentsIterable = metricAdvisorClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId,
            options,
            Context.NONE);

        Stream<PagedResponse<Incident>> incidentsPageStream = incidentsIterable.streamByPage();
        int[] pageCount = new int[1];
        incidentsPageStream.forEach(incidentsPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<Incident> incidentsPageItems = incidentsPage.getElements();
            for (Incident incident : incidentsPageItems) {
                System.out.printf("Metric Id: %s%n", incident.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
                System.out.printf("Incident Id: %s%n", incident.getId());
                System.out.printf("Incident Start Time: %s%n", incident.getStartTime());
                System.out.printf("Incident Severity: %s%n", incident.getSeverity());
                System.out.printf("Incident Status: %s%n", incident.getStatus());
                System.out.printf("Root Dimension Key:");
                DimensionKey rootDimension = incident.getRootDimensionKey();
                for (Map.Entry<String, String> dimension : rootDimension.asMap().entrySet()) {
                    System.out.printf("DimensionKey: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAnomaliesForAlert(String, String)}.
     */
    public void listAnomaliesForAlert() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions()
            .setTop(10);
        PagedIterable<Anomaly> anomaliesIterable = metricAdvisorClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId
        );

        for (Anomaly anomaly : anomaliesIterable) {
            System.out.printf("Metric Id: %s%n", anomaly.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", anomaly.getDetectionConfigurationId());
            System.out.printf("Anomaly Created Time: %s%n", anomaly.getCreatedTime());
            System.out.printf("Anomaly Modified Time: %s%n", anomaly.getModifiedTime());
            System.out.printf("Anomaly Severity: %s%n", anomaly.getSeverity());
            System.out.printf("Anomaly Status: %s%n", anomaly.getStatus());
            System.out.printf("Series Key:");
            System.out.println(anomaly.getSeriesKey().asMap());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAnomaliesForAlert(String, String, ListAnomaliesAlertedOptions, Context)}.
     */
    public void listAnomaliesForAlertWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions-Context
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions()
            .setTop(10);
        PagedIterable<Anomaly> anomaliesIterable = metricAdvisorClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId,
            options,
            Context.NONE);

        Stream<PagedResponse<Anomaly>> anomaliesPageStream = anomaliesIterable.streamByPage();
        int[] pageCount = new int[1];
        anomaliesPageStream.forEach(anomaliesPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<Anomaly> anomaliesPageItems = anomaliesPage.getElements();
            for (Anomaly anomaly : anomaliesPageItems) {
                System.out.printf("Metric Id: %s%n", anomaly.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomaly.getDetectionConfigurationId());
                System.out.printf("Anomaly Created Time: %s%n", anomaly.getCreatedTime());
                System.out.printf("Anomaly Modified Time: %s%n", anomaly.getModifiedTime());
                System.out.printf("Anomaly Severity: %s%n", anomaly.getSeverity());
                System.out.printf("Anomaly Status: %s%n", anomaly.getStatus());
                System.out.printf("Series Key:");
                System.out.println(anomaly.getSeriesKey().asMap());
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAlerts(String, ListAlertOptions)}.
     */
    public void listAlertForAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-ListAlertOptions
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final TimeMode timeMode = TimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions(startTime, endTime, timeMode)
            .setTop(10);

        PagedIterable<Alert> alertsIterable
            = metricAdvisorClient.listAlerts(alertConfigurationId, options);

        for (Alert alert : alertsIterable) {
            System.out.printf("Alert Id: %s%n", alert.getId());
            System.out.printf("Created Time: %s%n", alert.getCreatedTime());
            System.out.printf("Modified Time: %s%n", alert.getModifiedTime());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-ListAlertOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAlerts(String, ListAlertOptions, Context)}.
     */
    public void listAlertForAlertConfigurationWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-ListAlertOptions-Context
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final TimeMode timeMode = TimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions(startTime, endTime, timeMode)
            .setTop(10);

        PagedIterable<Alert> alertsIterable
            = metricAdvisorClient.listAlerts(alertConfigurationId, options, Context.NONE);

        Stream<PagedResponse<Alert>> alertsPageStream = alertsIterable.streamByPage();
        int[] pageCount = new int[1];
        alertsPageStream.forEach(alertsPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<Alert> alertsPageItems = alertsPage.getElements();
            for (Alert alert : alertsPageItems) {
                System.out.printf("Alert Id: %s%n", alert.getId());
                System.out.printf("Created Time: %s%n", alert.getCreatedTime());
                System.out.printf("Modified Time: %s%n", alert.getModifiedTime());
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAlerts#String-ListAlertOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listValuesOfDimensionWithAnomalies(String, String, ListValuesOfDimensionWithAnomaliesOptions)}.
     */
    public void listValuesOfDimensionWithAnomalies() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listValuesOfDimensionWithAnomalies#String-String-ListValuesOfDimensionWithAnomaliesOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String dimensionName = "Dim1";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListValuesOfDimensionWithAnomaliesOptions options
            = new ListValuesOfDimensionWithAnomaliesOptions(startTime, endTime)
            .setTop(10);

        PagedIterable<String> dimensionValueIterable
            = metricAdvisorClient.listValuesOfDimensionWithAnomalies(detectionConfigurationId,
            dimensionName,
            options);

        for (String dimensionValue : dimensionValueIterable) {
            System.out.printf("Dimension Value: %s%n", dimensionValue);
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listValuesOfDimensionWithAnomalies#String-String-ListValuesOfDimensionWithAnomaliesOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listValuesOfDimensionWithAnomalies(String, String, ListValuesOfDimensionWithAnomaliesOptions, Context)}.
     */
    public void listValuesOfDimensionWithAnomaliesWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listValuesOfDimensionWithAnomalies#String-String-ListValuesOfDimensionWithAnomaliesOptions-Context
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String dimensionName = "Dim1";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListValuesOfDimensionWithAnomaliesOptions options
            = new ListValuesOfDimensionWithAnomaliesOptions(startTime, endTime)
            .setTop(10);

        PagedIterable<String> dimensionValueIterable
            = metricAdvisorClient.listValuesOfDimensionWithAnomalies(detectionConfigurationId,
            dimensionName,
            options,
            Context.NONE);

        Stream<PagedResponse<String>> dimensionValuePageStream = dimensionValueIterable.streamByPage();
        int[] pageCount = new int[1];
        dimensionValuePageStream.forEach(dimensionValuePage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<String> dimensionValuePageItems = dimensionValuePage.getElements();
            for (String dimensionValue : dimensionValuePageItems) {
                System.out.printf("Dimension Value: %s%n", dimensionValue);
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listValuesOfDimensionWithAnomalies#String-String-ListValuesOfDimensionWithAnomaliesOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentsForDetectionConfiguration(String, ListIncidentsDetectedOptions)}.
     */
    public void listIncidentsForDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfiguration#String-ListIncidentsDetectedOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions(startTime, endTime)
            .setTop(1000);

        PagedIterable<Incident> incidentsIterable
            = metricAdvisorClient.listIncidentsForDetectionConfiguration(detectionConfigurationId, options);

        for (Incident incident : incidentsIterable) {
            System.out.printf("Metric Id: %s%n", incident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
            System.out.printf("Incident Id: %s%n", incident.getId());
            System.out.printf("Incident Start Time: %s%n", incident.getStartTime());
            System.out.printf("Incident Severity: %s%n", incident.getSeverity());
            System.out.printf("Incident Status: %s%n", incident.getStatus());
            System.out.printf("Root Dimension Key: %s%n", incident.getRootDimensionKey().asMap());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfiguration#String-ListIncidentsDetectedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentsForDetectionConfiguration(String, ListIncidentsDetectedOptions, Context)}.
     */
    public void listIncidentsForDetectionConfigurationWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfiguration#String-ListIncidentsDetectedOptions-Context
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions(startTime, endTime)
            .setTop(1000);

        PagedIterable<Incident> incidentsIterable
            = metricAdvisorClient.listIncidentsForDetectionConfiguration(detectionConfigurationId,
            options,
            Context.NONE);

        Stream<PagedResponse<Incident>> incidentsPageStream = incidentsIterable.streamByPage();

        int[] pageCount = new int[1];
        incidentsPageStream.forEach(incidentsPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<Incident> pageElements = incidentsPage.getElements();
            for (Incident incident : pageElements) {
                System.out.printf("Metric Id: %s%n", incident.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
                System.out.printf("Incident Id: %s%n", incident.getId());
                System.out.printf("Incident Start Time: %s%n", incident.getStartTime());
                System.out.printf("Incident Severity: %s%n", incident.getSeverity());
                System.out.printf("Incident Status: %s%n", incident.getStatus());
                System.out.printf("Root Dimension Key:");
                System.out.printf("Root Dimension Key: %s%n", incident.getRootDimensionKey().asMap());
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentsForDetectionConfiguration#String-ListIncidentsDetectedOptions-Context
    }

    /*
     * Code snippet for {@link MetricsAdvisorClient#listAnomaliesForDetectionConfiguration(String, ListAnomaliesDetectedOptions)}.
     */
    public void listAnomaliesForDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfiguration#String-ListAnomaliesDetectedOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverity(Severity.LOW, Severity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions(startTime, endTime)
            .setTop(10)
            .setFilter(filter);
        PagedIterable<Anomaly> anomaliesIterable
            = metricAdvisorClient.listAnomaliesForDetectionConfiguration(detectionConfigurationId, options);

        for (Anomaly anomaly : anomaliesIterable) {
            System.out.printf("Anomaly Severity: %s%n", anomaly.getSeverity());
            System.out.printf("Series Key:");
            DimensionKey seriesKey = anomaly.getSeriesKey();
            for (Map.Entry<String, String> dimension : seriesKey.asMap().entrySet()) {
                System.out.printf("DimensionName: %s DimensionValue:%s%n",
                    dimension.getKey(), dimension.getValue());
            }
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfiguration#String-ListAnomaliesDetectedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listAnomaliesForDetectionConfiguration(String, ListAnomaliesDetectedOptions, Context)}.
     */
    public void listAnomaliesForDetectionConfigurationWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfiguration#String-ListAnomaliesDetectedOptions-Context
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverity(Severity.LOW, Severity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions(startTime, endTime)
            .setTop(10)
            .setFilter(filter);
        PagedIterable<Anomaly> anomaliesIterable
            = metricAdvisorClient.listAnomaliesForDetectionConfiguration(detectionConfigurationId,
            options,
            Context.NONE);

        Stream<PagedResponse<Anomaly>> anomaliesPageStream = anomaliesIterable.streamByPage();
        int[] pageCount = new int[1];
        anomaliesPageStream.forEach(anomaliesPage -> {
            System.out.printf("Page: %d%n", pageCount[0]++);
            IterableStream<Anomaly> anomaliesPageItems = anomaliesPage.getElements();
            for (Anomaly anomaly : anomaliesPageItems) {
                System.out.printf("Anomaly Severity: %s%n", anomaly.getSeverity());
                System.out.printf("Series Key:");
                DimensionKey seriesKey = anomaly.getSeriesKey();
                for (Map.Entry<String, String> dimension : seriesKey.asMap().entrySet()) {
                    System.out.printf("DimensionName: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listAnomaliesForDetectionConfiguration#String-ListAnomaliesDetectedOptions-Context
    }

    /*
     * Code snippet for {@link MetricsAdvisorClient#createMetricFeedback(String, MetricFeedback)}.
     */
    public void createMetricFeedback() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.createMetricFeedback#String-MetricFeedback
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT);

        final MetricFeedback metricFeedback
            = metricAdvisorClient.createMetricFeedback(metricId, metricChangePointFeedback);

        MetricChangePointFeedback createdMetricChangePointFeedback = (MetricChangePointFeedback) metricFeedback;
        System.out.printf("Metric feedback Id: %s%n", createdMetricChangePointFeedback.getId());
        System.out.printf("Metric feedback change point value: %s%n",
            createdMetricChangePointFeedback.getChangePointValue().toString());
        System.out.printf("Metric feedback start time: %s%n",
            createdMetricChangePointFeedback.getStartTime());
        System.out.printf("Metric feedback end time: %s%n",
            createdMetricChangePointFeedback.getEndTime());
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.createMetricFeedback#String-MetricFeedback
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#createMetricFeedbackWithResponse(String, MetricFeedback, Context)}.
     */
    public void createMetricFeedbackWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.createMetricFeedbackWithResponse#String-MetricFeedback-Context
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT);

        final Response<MetricFeedback> metricFeedbackResponse
            = metricAdvisorClient.createMetricFeedbackWithResponse(metricId, metricChangePointFeedback, Context.NONE);

        System.out.printf("Metric feedback creation operation status %s%n", metricFeedbackResponse.getStatusCode());
        MetricChangePointFeedback createdMetricChangePointFeedback
            = (MetricChangePointFeedback) metricFeedbackResponse.getValue();
        System.out.printf("Metric feedback Id: %s%n", createdMetricChangePointFeedback.getId());
        System.out.printf("Metric feedback change point value: %s%n",
            createdMetricChangePointFeedback.getChangePointValue().toString());
        System.out.printf("Metric feedback start time: %s%n",
            createdMetricChangePointFeedback.getStartTime());
        System.out.printf("Metric feedback end time: %s%n",
            createdMetricChangePointFeedback.getEndTime());
        System.out.printf("Metric feedback associated dimension filter: %s%n",
            createdMetricChangePointFeedback.getDimensionFilter().asMap());
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.createMetricFeedbackWithResponse#String-MetricFeedback-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#getMetricFeedback(String)}.
     */
    public void getMetricFeedback() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.getMetricFeedback#String

        final String feedbackId = "8i3h4i4-b804-4ab9-a70f-0da0c89cft3l";
        final MetricFeedback metricFeedback = metricAdvisorClient.getMetricFeedback(feedbackId);
        System.out.printf("Metric feedback Id: %s%n", metricFeedback.getId());
        System.out.printf("Metric feedback associated dimension filter: %s%n",
            metricFeedback.getDimensionFilter().asMap());

        if (PERIOD.equals(metricFeedback.getFeedbackType())) {
            MetricPeriodFeedback createMetricPeriodFeedback
                = (MetricPeriodFeedback) metricFeedback;
            System.out.printf("Metric feedback type: %s%n",
                createMetricPeriodFeedback.getPeriodType().toString());
            System.out.printf("Metric feedback period value: %f%n",
                createMetricPeriodFeedback.getPeriodValue());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.getMetricFeedback#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#getMetricFeedbackWithResponse(String, Context)}.
     */
    public void getMetricFeedbackWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.getMetricFeedbackWithResponse#String-Context

        final String feedbackId = "8i3h4i4-b804-4ab9-a70f-0da0c89cft3l";
        final Response<MetricFeedback> metricFeedbackResponse
            = metricAdvisorClient.getMetricFeedbackWithResponse(feedbackId, Context.NONE);
        final MetricFeedback metricFeedback = metricFeedbackResponse.getValue();
        System.out.printf("Metric feedback Id: %s%n", metricFeedback.getId());
        System.out.printf("Metric feedback associated dimension filter: %s%n",
            metricFeedback.getDimensionFilter().asMap());

        if (PERIOD.equals(metricFeedback.getFeedbackType())) {
            MetricPeriodFeedback createMetricPeriodFeedback
                = (MetricPeriodFeedback) metricFeedback;
            System.out.printf("Metric feedback type: %s%n",
                createMetricPeriodFeedback.getPeriodType().toString());
            System.out.printf("Metric feedback period value: %f%n",
                createMetricPeriodFeedback.getPeriodValue());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.getMetricFeedbackWithResponse#String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricFeedbacks(String)}.
     */
    public void listMetricFeedbacks() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricFeedbacks#String
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        metricAdvisorClient.listMetricFeedbacks(metricId)
            .forEach(metricFeedback -> {
                System.out.printf("Metric feedback Id: %s%n", metricFeedback.getId());
                System.out.printf("Metric feedback associated dimension filter: %s%n",
                    metricFeedback.getDimensionFilter().asMap());

                if (PERIOD.equals(metricFeedback.getFeedbackType())) {
                    MetricPeriodFeedback periodFeedback
                        = (MetricPeriodFeedback) metricFeedback;
                    System.out.printf("Metric feedback type: %s%n",
                        periodFeedback.getPeriodType().toString());
                    System.out.printf("Metric feedback period value: %f%n",
                        periodFeedback.getPeriodValue());
                } else if (ANOMALY.equals(metricFeedback.getFeedbackType())) {
                    MetricAnomalyFeedback metricAnomalyFeedback
                        = (MetricAnomalyFeedback) metricFeedback;
                    System.out.printf("Metric feedback anomaly value: %s%n",
                        metricAnomalyFeedback.getAnomalyValue().toString());
                    System.out.printf("Metric feedback associated detection configuration: %s%n",
                        metricAnomalyFeedback.getDetectionConfigurationId());
                } else if (COMMENT.equals(metricFeedback.getFeedbackType())) {
                    MetricCommentFeedback metricCommentFeedback
                        = (MetricCommentFeedback) metricFeedback;
                    System.out.printf("Metric feedback comment value: %s%n",
                        metricCommentFeedback.getComment());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricFeedbacks#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricFeedbacks(String, ListMetricFeedbackOptions, Context)}.
     */
    public void listMetricFeedbacksWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricFeedbacks#String-ListMetricFeedbackOptions-Context
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");

        metricAdvisorClient.listMetricFeedbacks(metricId,
            new ListMetricFeedbackOptions()
                .setFilter(new ListMetricFeedbackFilter()
                    .setStartTime(startTime)
                    .setTimeMode(FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME)
                    .setEndTime(endTime)), Context.NONE)
            .forEach(metricFeedback -> {
                System.out.printf("Metric feedback Id: %s%n", metricFeedback.getId());
                System.out.printf("Metric feedback associated dimension filter: %s%n",
                    metricFeedback.getDimensionFilter().asMap());
                System.out.printf("Metric feedback created time %s%n", metricFeedback.getCreatedTime());

                if (PERIOD.equals(metricFeedback.getFeedbackType())) {
                    MetricPeriodFeedback periodFeedback
                        = (MetricPeriodFeedback) metricFeedback;
                    System.out.printf("Metric feedback type: %s%n",
                        periodFeedback.getPeriodType().toString());
                    System.out.printf("Metric feedback period value: %f%n",
                        periodFeedback.getPeriodValue());
                } else if (ANOMALY.equals(metricFeedback.getFeedbackType())) {
                    MetricAnomalyFeedback metricAnomalyFeedback
                        = (MetricAnomalyFeedback) metricFeedback;
                    System.out.printf("Metric feedback anomaly value: %s%n",
                        metricAnomalyFeedback.getAnomalyValue().toString());
                    System.out.printf("Metric feedback associated detection configuration: %s%n",
                        metricAnomalyFeedback.getDetectionConfigurationId());
                } else if (COMMENT.equals(metricFeedback.getFeedbackType())) {
                    MetricCommentFeedback metricCommentFeedback
                        = (MetricCommentFeedback) metricFeedback;
                    System.out.printf("Metric feedback comment value: %s%n",
                        metricCommentFeedback.getComment());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricFeedbacks#String-ListMetricFeedbackOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentRootCauses(String, String)}.
     */
    public void listIncidentRootCauses() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String
        final String detectionConfigurationId = "c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String incidentId = "c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d";

        metricAdvisorClient.listIncidentRootCauses(detectionConfigurationId, incidentId)
            .forEach(incidentRootCause -> {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.printf("Series Key:");
                System.out.println(incidentRootCause.getSeriesKey().asMap());
                System.out.printf("Confidence for the detected incident root cause %.2f%n",
                    incidentRootCause.getConfidenceScore());
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
            = metricAdvisorClient.listIncidentRootCauses(detectionConfigurationId, incidentId, Context.NONE);
        Stream<PagedResponse<IncidentRootCause>> rootCausePageIterable = rootCauseIterable.streamByPage();
        rootCausePageIterable.forEach(response -> {
            System.out.printf("Response StatusCode: %s%n", response.getStatusCode());
            IterableStream<IncidentRootCause> pageElements = response.getElements();
            for (IncidentRootCause incidentRootCause : pageElements) {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.printf("Series Key:");
                System.out.println(incidentRootCause.getSeriesKey().asMap());
                System.out.printf("Confidence for the detected incident root cause %.2f%n",
                    incidentRootCause.getConfidenceScore());
            }
        });

        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listIncidentRootCauses(Incident)}.
     */
    public void listIncidentRootCausesWithIncident() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#Incident
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListIncidentsDetectedOptions options
            = new ListIncidentsDetectedOptions(startTime, endTime)
            .setTop(10);

        metricAdvisorClient.listIncidentsForDetectionConfiguration(detectionConfigurationId, options)
            .forEach(incident -> {
                metricAdvisorClient.listIncidentRootCauses(incident)
                    .forEach(incidentRootCause -> {
                        System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                        System.out.printf("Series Key:");
                        System.out.println(incidentRootCause.getSeriesKey().asMap());
                        System.out.printf("Confidence for the detected incident root cause %.2f%n",
                            incidentRootCause.getConfidenceScore());
                    });
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#Incident
    }

    /*
     * Code snippet for {@link MetricsAdvisorClient#listMetricEnrichmentStatus(String, ListMetricEnrichmentStatusOptions)}.
     */
    public void listMetricEnrichmentStatus() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-ListMetricEnrichmentStatusOptions
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final ListMetricEnrichmentStatusOptions options = new ListMetricEnrichmentStatusOptions(startTime, endTime);
        metricAdvisorClient.listMetricEnrichmentStatus(metricId, options)
            .forEach(enrichmentStatus -> {
                System.out.printf("Metric enrichment status : %s%n", enrichmentStatus.getStatus());
                System.out.printf("Metric enrichment status message: %s%n", enrichmentStatus.getMessage());
                System.out.printf("Metric enrichment status data slice timestamp : %s%n",
                    enrichmentStatus.getTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-ListMetricEnrichmentStatusOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricEnrichmentStatus(String, ListMetricEnrichmentStatusOptions, Context)}.
     */
    public void listMetricEnrichmentStatusWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-ListMetricEnrichmentStatusOptions-Context
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final ListMetricEnrichmentStatusOptions options = new ListMetricEnrichmentStatusOptions(startTime, endTime);

        metricAdvisorClient.listMetricEnrichmentStatus(metricId, options, Context.NONE)
            .forEach(enrichmentStatus -> {
                System.out.printf("Metric enrichment status : %s%n", enrichmentStatus.getStatus());
                System.out.printf("Metric enrichment status message: %s%n", enrichmentStatus.getMessage());
                System.out.printf("Metric enrichment status data slice timestamp : %s%n",
                    enrichmentStatus.getTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichmentStatus#String-ListMetricEnrichmentStatusOptions-Context
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricEnrichedSeriesData(List, String, OffsetDateTime, OffsetDateTime)}.
     */
    public void listEnrichedSeries() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#List-String-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "e87d899d-a5a0-4259-b752-11aea34d5e34";
        final DimensionKey seriesKey = new DimensionKey()
            .put("Dim1", "Common Lime")
            .put("Dim2", "Antelope");
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-08-12T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-12T00:00:00Z");

        PagedIterable<MetricEnrichedSeriesData> enrichedDataIterable
            = metricAdvisorClient.listMetricEnrichedSeriesData(Arrays.asList(seriesKey),
            detectionConfigurationId,
            startTime,
            endTime);

        for (MetricEnrichedSeriesData enrichedData : enrichedDataIterable) {
            System.out.printf("Series Key %s%n:", enrichedData.getSeriesKey().asMap());
            System.out.println("List of data points for this series");
            System.out.println(enrichedData.getValueList());
            System.out.println("Timestamps of the data related to this time series:");
            System.out.println(enrichedData.getTimestampList());
            System.out.println("The expected values of the data points calculated by the smart detector:");
            System.out.println(enrichedData.getExpectedValueList());
            System.out.println("The lower boundary values of the data points calculated by smart detector:");
            System.out.println(enrichedData.getLowerBoundaryList());
            System.out.println("the periods calculated for the data points in the time series:");
            System.out.println(enrichedData.getPeriodList());
        }
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#List-String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorClient#listMetricEnrichedSeriesData(List, String, OffsetDateTime, OffsetDateTime, Context)}.
     */
    public void listEnrichedSeriesWithContext() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#List-String-OffsetDateTime-OffsetDateTime-Context
        final String detectionConfigurationId = "e87d899d-a5a0-4259-b752-11aea34d5e34";
        final DimensionKey seriesKey = new DimensionKey()
            .put("Dim1", "Common Lime")
            .put("Dim2", "Antelope");
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-08-12T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-12T00:00:00Z");

        PagedIterable<MetricEnrichedSeriesData> enrichedDataIterable
            = metricAdvisorClient.listMetricEnrichedSeriesData(Arrays.asList(seriesKey),
            detectionConfigurationId,
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
                System.out.println(enrichedData.getValueList());
                System.out.println("Timestamps of the data related to this time series:");
                System.out.println(enrichedData.getTimestampList());
                System.out.println("The expected values of the data points calculated by the smart detector:");
                System.out.println(enrichedData.getExpectedValueList());
                System.out.println("The lower boundary values of the data points calculated by smart detector:");
                System.out.println(enrichedData.getLowerBoundaryList());
                System.out.println("the periods calculated for the data points in the time series:");
                System.out.println(enrichedData.getPeriodList());
            }
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricEnrichedSeriesData#List-String-OffsetDateTime-OffsetDateTime-Context
    }
}
