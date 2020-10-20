// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ChangePointValue;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.FeedbackQueryTimeMode;
import com.azure.ai.metricsadvisor.models.Incident;
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
import com.azure.core.http.rest.PagedFlux;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.ai.metricsadvisor.models.FeedbackType.ANOMALY;
import static com.azure.ai.metricsadvisor.models.FeedbackType.COMMENT;
import static com.azure.ai.metricsadvisor.models.FeedbackType.PERIOD;

/**
 * Code snippet for {@link MetricsAdvisorAsyncClient}
 */
public class MetricsAdvisorAsyncClientJavaDocCodeSnippets {
    MetricsAdvisorAsyncClient metricAdvisorAsyncClient =
        new MetricsAdvisorClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link MetricsAdvisorAsyncClient}
     */
    public void createMetricAdvisorAsyncClient() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.instantiation
        MetricsAdvisorAsyncClient metricAdvisorAdministrationAsyncClient =
            new MetricsAdvisorClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .buildAsyncClient();
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.instantiation
    }

    /**
     * Code snippet for creating a {@link MetricsAdvisorAsyncClient} with pipeline
     */
    public void createMetricAdvisorAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        MetricsAdvisorAsyncClient metricAdvisorAdministrationAsyncClient =
            new MetricsAdvisorClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorAsyncClient#listMetricSeriesDefinitions(String, ListMetricSeriesDefinitionOptions)}
     */
    public void listMetricSeriesDefinitions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-ListMetricSeriesDefinitionOptions
        String metricId = "b460abfc-7a58-47d7-9d99-21ee21fdfc6e";
        final ListMetricSeriesDefinitionOptions options
            = new ListMetricSeriesDefinitionOptions(OffsetDateTime.parse("2020-07-10T00:00:00Z"))
            .setTop(10)
            .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                    put("Dim2", Collections.singletonList("Angelfish"));
                }});

        metricAdvisorAsyncClient.listMetricSeriesDefinitions(metricId, options)
            .subscribe(metricSeriesDefinition -> {
                System.out.printf("Metric id for the retrieved series definition : %s%n",
                    metricSeriesDefinition.getMetricId());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesDefinition.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-ListMetricSeriesDefinitionOptions
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorAsyncClient#listMetricSeriesData(String, List, ListMetricSeriesDataOptions)}
     */
    public void listMetricSeriesData() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesData#String-List-ListMetricSeriesDataOptions
        final String metricId = "2dgfbbbb-41ec-a637-677e77b81455";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");

        final ListMetricSeriesDataOptions options = new ListMetricSeriesDataOptions(startTime, endTime)
            .setTop(10);
        final List<DimensionKey> seriesKeyFilter
            = Arrays.asList(new DimensionKey().put("cost", "redmond"));

        metricAdvisorAsyncClient.listMetricSeriesData(metricId, seriesKeyFilter, options)
            .subscribe(metricSeriesData -> {
                System.out.println("List of data points for this series:");
                System.out.println(metricSeriesData.getValueList());
                System.out.println("Timestamps of the data related to this time series:");
                System.out.println(metricSeriesData.getTimestampList());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesData.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesData#String-List-ListMetricSeriesDataOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listMetricDimensionValues(String, String)}
     */
    public void listMetricDimensionValues() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String

        metricAdvisorAsyncClient.listMetricDimensionValues("metricId", "dimension1")
            .subscribe(System.out::println);
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorAsyncClient#listMetricDimensionValues(String, String, ListMetricDimensionValuesOptions)}
     */
    public void listMetricDimensionValuesWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions
        metricAdvisorAsyncClient.listMetricDimensionValues("metricId", "dimension1",
            new ListMetricDimensionValuesOptions().setDimensionValueToFilter("value1").setTop(3))
            .subscribe(System.out::println);
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listIncidentsForAlert(String, String, ListIncidentsAlertedOptions)}.
     */
    public void listIncidentsForAlert() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions()
            .setTop(10);

        metricAdvisorAsyncClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId,
            options)
            .subscribe(incident -> {
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
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listAnomaliesForAlert(String, String)}.
     */
    public void listAnomaliesForAlert() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";

        metricAdvisorAsyncClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId)
            .subscribe(anomaly -> {
                System.out.printf("Metric Id: %s%n", anomaly.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomaly.getDetectionConfigurationId());
                System.out.printf("Anomaly Created Time: %s%n", anomaly.getCreatedTime());
                System.out.printf("Anomaly Modified Time: %s%n", anomaly.getModifiedTime());
                System.out.printf("Anomaly Severity: %s%n", anomaly.getSeverity());
                System.out.printf("Anomaly Status: %s%n", anomaly.getStatus());
                System.out.printf("Series Key:");
                DimensionKey seriesKey = anomaly.getSeriesKey();
                for (Map.Entry<String, String> dimension : seriesKey.asMap().entrySet()) {
                    System.out.printf("DimensionName: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listAnomaliesForAlert(String, String, ListAnomaliesAlertedOptions)}.
     */
    public void listAnomaliesForAlertWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListAnomaliesAlertedOptions options = new ListAnomaliesAlertedOptions()
            .setTop(10);
        metricAdvisorAsyncClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId,
            options)
            .subscribe(anomaly -> {
                System.out.printf("Metric Id: %s%n", anomaly.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomaly.getDetectionConfigurationId());
                System.out.printf("Anomaly Created Time: %s%n", anomaly.getCreatedTime());
                System.out.printf("Anomaly Modified Time: %s%n", anomaly.getModifiedTime());
                System.out.printf("Anomaly Severity: %s%n", anomaly.getSeverity());
                System.out.printf("Anomaly Status: %s%n", anomaly.getStatus());
                System.out.printf("Series Key:");
                System.out.println(anomaly.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listAlerts(String, ListAlertOptions)}.
     */
    public void listAlertForAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-ListAlertOptions
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final TimeMode timeMode = TimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions(startTime, endTime, timeMode)
            .setTop(10);

        metricAdvisorAsyncClient.listAlerts(alertConfigurationId, options)
            .subscribe(alert -> {
                System.out.printf("Alert Id: %s%n", alert.getId());
                System.out.printf("Created Time: %s%n", alert.getCreatedTime());
                System.out.printf("Modified Time: %s%n", alert.getModifiedTime());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-ListAlertOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listValuesOfDimensionWithAnomalies(String, String, ListValuesOfDimensionWithAnomaliesOptions)}.
     */
    public void listValuesOfDimensionWithAnomalies() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listValuesOfDimensionWithAnomalies#String-String-ListValuesOfDimensionWithAnomaliesOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String dimensionName = "Dim1";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListValuesOfDimensionWithAnomaliesOptions options
            = new ListValuesOfDimensionWithAnomaliesOptions(startTime, endTime)
            .setTop(10);

        metricAdvisorAsyncClient.listValuesOfDimensionWithAnomalies(detectionConfigurationId,
            dimensionName,
            options)
            .subscribe(dimensionValue -> {
                System.out.printf("Dimension Value: %s%n", dimensionValue);
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listValuesOfDimensionWithAnomalies#String-String-ListValuesOfDimensionWithAnomaliesOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listIncidentsForDetectionConfiguration(String, ListIncidentsDetectedOptions)}.
     */
    public void listIncidentsForDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfiguration#String-ListIncidentsDetectedOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions(startTime, endTime)
            .setTop(1000);

        PagedFlux<Incident> incidentsFlux
            = metricAdvisorAsyncClient.listIncidentsForDetectionConfiguration(detectionConfigurationId, options);

        incidentsFlux.subscribe(incident -> {
            System.out.printf("Metric Id: %s%n", incident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
            System.out.printf("Incident Id: %s%n", incident.getId());
            System.out.printf("Incident Start Time: %s%n", incident.getStartTime());
            System.out.printf("Incident Severity: %s%n", incident.getSeverity());
            System.out.printf("Incident Status: %s%n", incident.getStatus());
            System.out.printf("Root Dimension Key: %s%n", incident.getRootDimensionKey().asMap());
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfiguration#String-ListIncidentsDetectedOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listAnomaliesForDetectionConfiguration(String, ListAnomaliesDetectedOptions)}.
     */
    public void listAnomaliesForDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfiguration#String-ListAnomaliesDetectedOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverity(Severity.LOW, Severity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions(startTime, endTime)
            .setTop(10)
            .setFilter(filter);
        metricAdvisorAsyncClient.listAnomaliesForDetectionConfiguration(detectionConfigurationId,
            options)
            .subscribe(anomaly -> {
                System.out.printf("Anomaly Severity: %s%n", anomaly.getSeverity());
                System.out.printf("Series Key:");
                DimensionKey seriesKey = anomaly.getSeriesKey();
                for (Map.Entry<String, String> dimension : seriesKey.asMap().entrySet()) {
                    System.out.printf("DimensionName: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfiguration#String-ListAnomaliesDetectedOptions
    }

    /*
     * Code snippet for {@link MetricsAdvisorAsyncClient#createMetricFeedback(String, MetricFeedback)}.
     */
    public void createMetricFeedback() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.createMetricFeedback#String-MetricFeedback
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT);

        metricAdvisorAsyncClient.createMetricFeedback(metricId, metricChangePointFeedback)
            .subscribe(metricFeedback -> {
                MetricChangePointFeedback createdMetricChangePointFeedback = (MetricChangePointFeedback) metricFeedback;
                System.out.printf("Metric feedback Id: %s%n", createdMetricChangePointFeedback.getId());
                System.out.printf("Metric feedback change point value: %s%n",
                    createdMetricChangePointFeedback.getChangePointValue().toString());
                System.out.printf("Metric feedback start time: %s%n",
                    createdMetricChangePointFeedback.getStartTime());
                System.out.printf("Metric feedback end time: %s%n",
                    createdMetricChangePointFeedback.getEndTime());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.createMetricFeedback#String-MetricFeedback
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#createMetricFeedbackWithResponse(String, MetricFeedback)}.
     */
    public void createMetricFeedbackWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.createMetricFeedbackWithResponse#String-MetricFeedback
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT);

        metricAdvisorAsyncClient.createMetricFeedbackWithResponse(metricId, metricChangePointFeedback)
            .subscribe(metricFeedbackResponse -> {
                System.out.printf("Metric feedback creation operation status %s%n",
                    metricFeedbackResponse.getStatusCode());
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
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.createMetricFeedbackWithResponse#String-MetricFeedback
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#getMetricFeedback(String)}.
     */
    public void getMetricFeedback() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getMetricFeedback#String

        final String feedbackId = "8i3h4i4-b804-4ab9-a70f-0da0c89cft3l";
        metricAdvisorAsyncClient.getMetricFeedback(feedbackId)
            .subscribe(metricFeedback -> {
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
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getMetricFeedback#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#getMetricFeedbackWithResponse(String)}.
     */
    public void getMetricFeedbackWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getMetricFeedbackWithResponse#String

        final String feedbackId = "8i3h4i4-b804-4ab9-a70f-0da0c89cft3l";
        metricAdvisorAsyncClient.getMetricFeedbackWithResponse(feedbackId)
            .subscribe(metricFeedbackResponse -> {
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
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getMetricFeedbackWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listMetricFeedbacks(String)}.
     */
    public void listMetricFeedbacks() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricFeedbacks#String
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        metricAdvisorAsyncClient.listMetricFeedbacks(metricId)
            .subscribe(metricFeedback -> {
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
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricFeedbacks#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listMetricFeedbacks(String, ListMetricFeedbackOptions)}.
     */
    public void listMetricFeedbacksWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricFeedbacks#String-ListMetricFeedbackOptions
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");

        metricAdvisorAsyncClient.listMetricFeedbacks(metricId,
            new ListMetricFeedbackOptions()
                .setFilter(new ListMetricFeedbackFilter()
                    .setStartTime(startTime)
                    .setTimeMode(FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME)
                    .setEndTime(endTime)))
            .subscribe(metricFeedback -> {
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
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricFeedbacks#String-ListMetricFeedbackOptions
    }

    /**
<<<<<<< HEAD
     * Code snippet for {@link MetricsAdvisorAsyncClient#listIncidentRootCauses(String, String)}.
     */
    public void listIncidentRootCauses() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#String-String
        final String detectionConfigurationId = "c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String incidentId = "c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d";

        metricAdvisorAsyncClient.listIncidentRootCauses(detectionConfigurationId, incidentId)
            .subscribe(incidentRootCause -> {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.println("Series Key:");
                System.out.println(incidentRootCause.getSeriesKey().asMap());
                System.out.printf("Confidence for the detected incident root cause: %.2f%n",
                    incidentRootCause.getConfidenceScore());
            });

        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#String-String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listIncidentRootCauses(Incident)}.
     */
    public void listIncidentRootCausesWithIncident() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#Incident
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListIncidentsDetectedOptions options
            = new ListIncidentsDetectedOptions(startTime, endTime)
            .setTop(10);

        metricAdvisorAsyncClient.listIncidentsForDetectionConfiguration(detectionConfigurationId, options)
            .flatMap(incident -> {
                return metricAdvisorAsyncClient.listIncidentRootCauses(incident);
            })
            .subscribe(incidentRootCause -> {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.printf("Series Key:");
                System.out.println(incidentRootCause.getSeriesKey().asMap());
            });

        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#Incident
    }

    /*
     * Code snippet for {@link MetricsAdvisorAsyncClient#listMetricEnrichmentStatus(String, ListMetricEnrichmentStatusOptions)}.
     */
    public void listMetricEnrichmentStatus() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-ListMetricEnrichmentStatusOptions
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final ListMetricEnrichmentStatusOptions options = new ListMetricEnrichmentStatusOptions(startTime, endTime);

        metricAdvisorAsyncClient.listMetricEnrichmentStatus(metricId, options)
            .subscribe(enrichmentStatus -> {
                System.out.printf("Metric enrichment status : %s%n", enrichmentStatus.getStatus());
                System.out.printf("Metric enrichment status message: %s%n", enrichmentStatus.getMessage());
                System.out.printf("Metric enrichment status data slice timestamp : %s%n",
                    enrichmentStatus.getTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-ListMetricEnrichmentStatusOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listMetricEnrichedSeriesData(List, String, OffsetDateTime, OffsetDateTime)}.
     */
    public void listEnrichedSeries() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichedSeriesData#List-String-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "e87d899d-a5a0-4259-b752-11aea34d5e34";
        final DimensionKey seriesKey = new DimensionKey()
            .put("Dim1", "Common Lime")
            .put("Dim2", "Antelope");
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-08-12T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-12T00:00:00Z");

        PagedFlux<MetricEnrichedSeriesData> enrichedDataFlux
            = metricAdvisorAsyncClient.listMetricEnrichedSeriesData(Arrays.asList(seriesKey),
            detectionConfigurationId,
            startTime,
            endTime);

        enrichedDataFlux.subscribe(enrichedData -> {
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
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichedSeriesData#List-String-OffsetDateTime-OffsetDateTime
    }
}
