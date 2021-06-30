// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.AlertQueryTimeMode;
import com.azure.ai.metricsadvisor.models.ChangePointValue;
import com.azure.ai.metricsadvisor.models.DimensionKey;
import com.azure.ai.metricsadvisor.models.FeedbackQueryTimeMode;
import com.azure.ai.metricsadvisor.models.AnomalyIncident;
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
    MetricsAdvisorAsyncClient metricsAdvisorAsyncClient =
        new MetricsAdvisorClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link MetricsAdvisorAsyncClient}
     */
    public void createMetricAdvisorAsyncClient() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.instantiation
        MetricsAdvisorAsyncClient metricsAdvisorAsyncClient =
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

        MetricsAdvisorAsyncClient metricsAdvisorAsyncClient =
            new MetricsAdvisorClientBuilder()
                .credential(new MetricsAdvisorKeyCredential("{subscription_key}", "{api_key}"))
                .endpoint("{endpoint}")
                .pipeline(pipeline)
                .buildAsyncClient();
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorAsyncClient#listMetricSeriesDefinitions(String, OffsetDateTime, ListMetricSeriesDefinitionOptions)}
     */

    public void listMetricSeriesDefinitions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-OffsetDateTime
        String metricId = "b460abfc-7a58-47d7-9d99-21ee21fdfc6e";
        final OffsetDateTime activeSince = OffsetDateTime.parse("2020-07-10T00:00:00Z");

        metricsAdvisorAsyncClient.listMetricSeriesDefinitions(metricId, activeSince)
            .subscribe(metricSeriesDefinition -> {
                System.out.printf("Data Feed Metric id for the retrieved series definition : %s%n",
                    metricSeriesDefinition.getMetricId());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesDefinition.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-OffsetDateTime
    }

    public void listMetricSeriesDefinitionsWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-OffsetDateTime-ListMetricSeriesDefinitionOptions
        String metricId = "b460abfc-7a58-47d7-9d99-21ee21fdfc6e";
        final OffsetDateTime activeSince = OffsetDateTime.parse("2020-07-10T00:00:00Z");
        final ListMetricSeriesDefinitionOptions options
            = new ListMetricSeriesDefinitionOptions()
            .setMaxPageSize(10)
            .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                    put("Dim2", Collections.singletonList("Angelfish"));
                }});

        metricsAdvisorAsyncClient.listMetricSeriesDefinitions(metricId, activeSince, options)
            .subscribe(metricSeriesDefinition -> {
                System.out.printf("Data Feed Metric id for the retrieved series definition : %s%n",
                    metricSeriesDefinition.getMetricId());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesDefinition.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesDefinitions#String-OffsetDateTime-ListMetricSeriesDefinitionOptions
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorAsyncClient#listMetricSeriesData(String, List, OffsetDateTime, OffsetDateTime)}
     */
    public void listMetricSeriesData() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime
        final String metricId = "2dgfbbbb-41ec-a637-677e77b81455";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");

        final List<DimensionKey> seriesKeyFilter
            = Arrays.asList(new DimensionKey().put("cost", "redmond"));

        metricsAdvisorAsyncClient.listMetricSeriesData(metricId, seriesKeyFilter, startTime, endTime)
            .subscribe(metricSeriesData -> {
                System.out.println("List of data points for this series:");
                System.out.println(metricSeriesData.getMetricValues());
                System.out.println("Timestamps of the data related to this time series:");
                System.out.println(metricSeriesData.getTimestamps());
                System.out.printf("Series Key:");
                System.out.println(metricSeriesData.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listMetricDimensionValues(String, String)}
     */
    public void listMetricDimensionValues() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String

        metricsAdvisorAsyncClient.listMetricDimensionValues("metricId", "dimension1")
            .subscribe(System.out::println);
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String
    }

    /**
     * Code snippet for
     * {@link MetricsAdvisorAsyncClient#listMetricDimensionValues(String, String, ListMetricDimensionValuesOptions)}
     */
    public void listMetricDimensionValuesWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions
        metricsAdvisorAsyncClient.listMetricDimensionValues("metricId", "dimension1",
            new ListMetricDimensionValuesOptions().setDimensionValueToFilter("value1").setMaxPageSize(3))
            .subscribe(System.out::println);
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricDimensionValues#String-String-ListMetricDimensionValuesOptions
    }

    public void listIncidentsForAlert() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";

        metricsAdvisorAsyncClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId)
            .subscribe(incident -> {
                System.out.printf("Data Feed Metric Id: %s%n", incident.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
                System.out.printf("Anomaly Incident Id: %s%n", incident.getId());
                System.out.printf("Anomaly Incident Start Time: %s%n", incident.getStartTime());
                System.out.printf("Anomaly Incident AnomalySeverity: %s%n", incident.getSeverity());
                System.out.printf("Anomaly Incident Status: %s%n", incident.getStatus());
                System.out.printf("Root DataFeedDimension Key:");
                DimensionKey rootDimension = incident.getRootDimensionKey();
                for (Map.Entry<String, String> dimension : rootDimension.asMap().entrySet()) {
                    System.out.printf("DimensionName: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listIncidentsForAlert(String, String, ListIncidentsAlertedOptions)}.
     */
    public void listIncidentsForAlertWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForAlert#String-String-ListIncidentsAlertedOptions
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final String alertId = "1746b031c00";
        final ListIncidentsAlertedOptions options = new ListIncidentsAlertedOptions()
            .setMaxPageSize(10);

        metricsAdvisorAsyncClient.listIncidentsForAlert(
            alertConfigurationId,
            alertId,
            options)
            .subscribe(incident -> {
                System.out.printf("Data Feed Metric Id: %s%n", incident.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
                System.out.printf("Anomaly Incident Id: %s%n", incident.getId());
                System.out.printf("Anomaly Incident Start Time: %s%n", incident.getStartTime());
                System.out.printf("Anomaly Incident AnomalySeverity: %s%n", incident.getSeverity());
                System.out.printf("Anomaly Incident Status: %s%n", incident.getStatus());
                System.out.printf("Root DataFeedDimension Key:");
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

        metricsAdvisorAsyncClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId)
            .subscribe(anomaly -> {
                System.out.printf("Data Feed Metric Id: %s%n", anomaly.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomaly.getDetectionConfigurationId());
                System.out.printf("DataPoint Anomaly Created Time: %s%n", anomaly.getCreatedTime());
                System.out.printf("DataPoint Anomaly Modified Time: %s%n", anomaly.getModifiedTime());
                System.out.printf("DataPoint Anomaly AnomalySeverity: %s%n", anomaly.getSeverity());
                System.out.printf("DataPoint Anomaly Status: %s%n", anomaly.getStatus());
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
            .setMaxPageSize(10);
        metricsAdvisorAsyncClient.listAnomaliesForAlert(
            alertConfigurationId,
            alertId,
            options)
            .subscribe(anomaly -> {
                System.out.printf("Data Feed Metric Id: %s%n", anomaly.getMetricId());
                System.out.printf("Detection Configuration Id: %s%n", anomaly.getDetectionConfigurationId());
                System.out.printf("DataPoint Anomaly Created Time: %s%n", anomaly.getCreatedTime());
                System.out.printf("DataPoint Anomaly Modified Time: %s%n", anomaly.getModifiedTime());
                System.out.printf("DataPoint Anomaly AnomalySeverity: %s%n", anomaly.getSeverity());
                System.out.printf("DataPoint Anomaly Status: %s%n", anomaly.getStatus());
                System.out.printf("Series Key:");
                System.out.println(anomaly.getSeriesKey().asMap());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForAlert#String-String-ListAnomaliesAlertedOptions
    }

    public void listAlertForAlertConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-OffsetDateTime-OffsetDateTime
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;

        metricsAdvisorAsyncClient.listAlerts(alertConfigurationId, startTime, endTime)
            .subscribe(alert -> {
                System.out.printf("Anomaly Alert Id: %s%n", alert.getId());
                System.out.printf("Created Time: %s%n", alert.getCreatedTime());
                System.out.printf("Modified Time: %s%n", alert.getModifiedTime());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listAlerts(String, OffsetDateTime, OffsetDateTime, ListAlertOptions)}.
     */
    public void listAlertForAlertConfigurationWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-OffsetDateTime-OffsetDateTime-ListAlertOptions
        final String alertConfigurationId = "ff3014a0-bbbb-41ec-a637-677e77b81299";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final AlertQueryTimeMode timeMode = AlertQueryTimeMode.ANOMALY_TIME;
        final ListAlertOptions options = new ListAlertOptions()
            .setAlertQueryTimeMode(timeMode)
            .setMaxPageSize(10);

        metricsAdvisorAsyncClient.listAlerts(alertConfigurationId, startTime, endTime, options)
            .subscribe(alert -> {
                System.out.printf("Anomaly Alert Id: %s%n", alert.getId());
                System.out.printf("Created Time: %s%n", alert.getCreatedTime());
                System.out.printf("Modified Time: %s%n", alert.getModifiedTime());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAlerts#String-OffsetDateTime-OffsetDateTime-ListAlertOptions
    }

    public void listAnomalyDimensionValues() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String dimensionName = "Dim1";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");

        metricsAdvisorAsyncClient.listAnomalyDimensionValues(detectionConfigurationId,
            dimensionName,
            startTime, endTime)
            .subscribe(dimensionValue -> {
                System.out.printf("DataFeedDimension Value: %s%n", dimensionValue);
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime
    }
    public void listAnomalyDimensionValuesWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime-ListAnomalyDimensionValuesOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String dimensionName = "Dim1";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListAnomalyDimensionValuesOptions options
            = new ListAnomalyDimensionValuesOptions()
            .setMaxPageSize(10);

        metricsAdvisorAsyncClient.listAnomalyDimensionValues(detectionConfigurationId,
            dimensionName,
            startTime, endTime, options)
            .subscribe(dimensionValue -> {
                System.out.printf("DataFeedDimension Value: %s%n", dimensionValue);
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomalyDimensionValues#String-String-OffsetDateTime-OffsetDateTime-ListAnomalyDimensionValuesOptions
    }

    public void listIncidentsForDetectionConfig() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");

        PagedFlux<AnomalyIncident> incidentsFlux
            = metricsAdvisorAsyncClient.listIncidentsForDetectionConfig(detectionConfigurationId, startTime,
            endTime);

        incidentsFlux.subscribe(incident -> {
            System.out.printf("Data Feed Metric Id: %s%n", incident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
            System.out.printf("Anomaly Incident Id: %s%n", incident.getId());
            System.out.printf("Anomaly Incident Start Time: %s%n", incident.getStartTime());
            System.out.printf("Anomaly Incident AnomalySeverity: %s%n", incident.getSeverity());
            System.out.printf("Anomaly Incident Status: %s%n", incident.getStatus());
            System.out.printf("Root DataFeedDimension Key: %s%n", incident.getRootDimensionKey().asMap());
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listIncidentsForDetectionConfig(String, OffsetDateTime, OffsetDateTime, ListIncidentsDetectedOptions)}.
     */
    public void listIncidentsForDetectionConfigWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListIncidentsDetectedOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListIncidentsDetectedOptions options = new ListIncidentsDetectedOptions()
            .setMaxPageSize(1000);

        PagedFlux<AnomalyIncident> incidentsFlux
            = metricsAdvisorAsyncClient.listIncidentsForDetectionConfig(detectionConfigurationId, startTime,
            endTime, options);

        incidentsFlux.subscribe(incident -> {
            System.out.printf("Data Feed Metric Id: %s%n", incident.getMetricId());
            System.out.printf("Detection Configuration Id: %s%n", incident.getDetectionConfigurationId());
            System.out.printf("Anomaly Incident Id: %s%n", incident.getId());
            System.out.printf("Anomaly Incident Start Time: %s%n", incident.getStartTime());
            System.out.printf("Anomaly Incident AnomalySeverity: %s%n", incident.getSeverity());
            System.out.printf("Anomaly Incident Status: %s%n", incident.getStatus());
            System.out.printf("Root DataFeedDimension Key: %s%n", incident.getRootDimensionKey().asMap());
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentsForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListIncidentsDetectedOptions
    }

    public void listAnomaliesForDetectionConfiguration() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");

        metricsAdvisorAsyncClient.listAnomaliesForDetectionConfig(detectionConfigurationId,
            startTime, endTime)
            .subscribe(anomaly -> {
                System.out.printf("DataPoint Anomaly AnomalySeverity: %s%n", anomaly.getSeverity());
                System.out.printf("Series Key:");
                DimensionKey seriesKey = anomaly.getSeriesKey();
                for (Map.Entry<String, String> dimension : seriesKey.asMap().entrySet()) {
                    System.out.printf("DimensionName: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listAnomaliesForDetectionConfig(String, OffsetDateTime, OffsetDateTime, ListAnomaliesDetectedOptions)}.
     */
    public void listAnomaliesForDetectionConfigurationWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListAnomaliesDetectedOptions
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T12:00:00Z");
        final ListAnomaliesDetectedFilter filter = new ListAnomaliesDetectedFilter()
            .setSeverityRange(AnomalySeverity.LOW, AnomalySeverity.MEDIUM);
        final ListAnomaliesDetectedOptions options = new ListAnomaliesDetectedOptions()
            .setMaxPageSize(10)
            .setFilter(filter);
        metricsAdvisorAsyncClient.listAnomaliesForDetectionConfig(detectionConfigurationId,
                startTime, endTime, options)
            .subscribe(anomaly -> {
                System.out.printf("DataPoint Anomaly AnomalySeverity: %s%n", anomaly.getSeverity());
                System.out.printf("Series Key:");
                DimensionKey seriesKey = anomaly.getSeriesKey();
                for (Map.Entry<String, String> dimension : seriesKey.asMap().entrySet()) {
                    System.out.printf("DimensionName: %s DimensionValue:%s%n",
                        dimension.getKey(), dimension.getValue());
                }
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime-ListAnomaliesDetectedOptions
    }

    /*
     * Code snippet for {@link MetricsAdvisorAsyncClient#addFeedback(String, MetricFeedback)}.
     */
    public void createMetricFeedback() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.addFeedback#String-MetricFeedback
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT);

        metricsAdvisorAsyncClient.addFeedback(metricId, metricChangePointFeedback)
            .subscribe(metricFeedback -> {
                MetricChangePointFeedback createdMetricChangePointFeedback = (MetricChangePointFeedback) metricFeedback;
                System.out.printf("Data Feed Metric feedback Id: %s%n", createdMetricChangePointFeedback.getId());
                System.out.printf("Data Feed Metric feedback change point value: %s%n",
                    createdMetricChangePointFeedback.getChangePointValue().toString());
                System.out.printf("Data Feed Metric feedback start time: %s%n",
                    createdMetricChangePointFeedback.getStartTime());
                System.out.printf("Data Feed Metric feedback end time: %s%n",
                    createdMetricChangePointFeedback.getEndTime());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.addFeedback#String-MetricFeedback
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#addFeedbackWithResponse(String, MetricFeedback)}.
     */
    public void createMetricFeedbackWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.addFeedbackWithResponse#String-MetricFeedback
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final MetricChangePointFeedback metricChangePointFeedback
            = new MetricChangePointFeedback(startTime, endTime, ChangePointValue.AUTO_DETECT);

        metricsAdvisorAsyncClient.addFeedbackWithResponse(metricId, metricChangePointFeedback)
            .subscribe(metricFeedbackResponse -> {
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
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.addFeedbackWithResponse#String-MetricFeedback
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#getFeedback(String)}.
     */
    public void getMetricFeedback() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getFeedback#String

        final String feedbackId = "8i3h4i4-b804-4ab9-a70f-0da0c89cft3l";
        metricsAdvisorAsyncClient.getFeedback(feedbackId)
            .subscribe(metricFeedback -> {
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
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getFeedback#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#getFeedbackWithResponse(String)}.
     */
    public void getMetricFeedbackWithResponse() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getFeedbackWithResponse#String

        final String feedbackId = "8i3h4i4-b804-4ab9-a70f-0da0c89cft3l";
        metricsAdvisorAsyncClient.getFeedbackWithResponse(feedbackId)
            .subscribe(metricFeedbackResponse -> {
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
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.getFeedbackWithResponse#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listFeedback(String)}.
     */
    public void listMetricFeedbacks() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listFeedback#String
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        metricsAdvisorAsyncClient.listFeedback(metricId)
            .subscribe(metricFeedback -> {
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
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listFeedback#String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listFeedback(String, ListMetricFeedbackOptions)}.
     */
    public void listMetricFeedbacksWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listFeedback#String-ListMetricFeedbackOptions
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");

        metricsAdvisorAsyncClient.listFeedback(metricId,
            new ListMetricFeedbackOptions()
                .setFilter(new ListMetricFeedbackFilter()
                    .setStartTime(startTime)
                    .setTimeMode(FeedbackQueryTimeMode.FEEDBACK_CREATED_TIME)
                    .setEndTime(endTime)))
            .subscribe(metricFeedback -> {
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
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listFeedback#String-ListMetricFeedbackOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listIncidentRootCauses(String, String)}.
     */
    public void listIncidentRootCauses() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#String-String
        final String detectionConfigurationId = "c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8";
        final String incidentId = "c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d";

        metricsAdvisorAsyncClient.listIncidentRootCauses(detectionConfigurationId, incidentId)
            .subscribe(incidentRootCause -> {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.println("Series Key:");
                System.out.println(incidentRootCause.getSeriesKey().asMap());
                System.out.printf("Confidence for the detected incident root cause: %.2f%n",
                    incidentRootCause.getContributionScore());
            });

        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#String-String
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listIncidentRootCauses(AnomalyIncident)}.
     */
    public void listIncidentRootCausesWithIncident() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#AnomalyIncident
        final String detectionConfigurationId = "c0f2539f-b804-4ab9-a70f-0da0c89c76d8";
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final ListIncidentsDetectedOptions options
            = new ListIncidentsDetectedOptions()
            .setMaxPageSize(10);

        metricsAdvisorAsyncClient.listIncidentsForDetectionConfig(detectionConfigurationId, startTime, endTime,
            options)
            .flatMap(incident -> {
                return metricsAdvisorAsyncClient.listIncidentRootCauses(incident);
            })
            .subscribe(incidentRootCause -> {
                System.out.printf("Description: %s%n", incidentRootCause.getDescription());
                System.out.printf("Series Key:");
                System.out.println(incidentRootCause.getSeriesKey().asMap());
            });

        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listIncidentRootCauses#AnomalyIncident
    }

    public void listMetricEnrichmentStatus() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";

        metricsAdvisorAsyncClient.listMetricEnrichmentStatus(metricId, startTime, endTime)
            .subscribe(enrichmentStatus -> {
                System.out.printf("Data Feed Metric enrichment status : %s%n", enrichmentStatus.getStatus());
                System.out.printf("Data Feed Metric enrichment status message: %s%n", enrichmentStatus.getMessage());
                System.out.printf("Data Feed Metric enrichment status data slice timestamp : %s%n",
                    enrichmentStatus.getTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listMetricEnrichmentStatus(String, OffsetDateTime, OffsetDateTime, ListMetricEnrichmentStatusOptions)}.
     */
    public void listMetricEnrichmentStatusWithOptions() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime-ListMetricEnrichmentStatusOptions
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
        final String metricId = "d3gh4i4-b804-4ab9-a70f-0da0c89cft3l";
        final ListMetricEnrichmentStatusOptions options = new ListMetricEnrichmentStatusOptions().setMaxPageSize(10);

        metricsAdvisorAsyncClient.listMetricEnrichmentStatus(metricId, startTime, endTime, options)
            .subscribe(enrichmentStatus -> {
                System.out.printf("Data Feed Metric enrichment status : %s%n", enrichmentStatus.getStatus());
                System.out.printf("Data Feed Metric enrichment status message: %s%n", enrichmentStatus.getMessage());
                System.out.printf("Data Feed Metric enrichment status data slice timestamp : %s%n",
                    enrichmentStatus.getTimestamp());
            });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichmentStatus#String-OffsetDateTime-OffsetDateTime-ListMetricEnrichmentStatusOptions
    }

    /**
     * Code snippet for {@link MetricsAdvisorAsyncClient#listMetricEnrichedSeriesData(String, List, OffsetDateTime, OffsetDateTime)}.
     */
    public void listEnrichedSeries() {
        // BEGIN: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime
        final String detectionConfigurationId = "e87d899d-a5a0-4259-b752-11aea34d5e34";
        final DimensionKey seriesKey = new DimensionKey()
            .put("Dim1", "Common Lime")
            .put("Dim2", "Antelope");
        final OffsetDateTime startTime = OffsetDateTime.parse("2020-08-12T00:00:00Z");
        final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-12T00:00:00Z");

        PagedFlux<MetricEnrichedSeriesData> enrichedDataFlux
            = metricsAdvisorAsyncClient.listMetricEnrichedSeriesData(detectionConfigurationId,
            Arrays.asList(seriesKey),
            startTime,
            endTime);

        enrichedDataFlux.subscribe(enrichedData -> {
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
        });
        // END: com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricEnrichedSeriesData#String-List-OffsetDateTime-OffsetDateTime
    }
}
