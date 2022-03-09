---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-cognitive-services
  - azure-metrics-advisor	
urlFragment: metricsadvisor-java-samples
---

# Azure Metrics Advisor client library samples for Java
Azure Metrics Advisor samples are a set of self-contained Java programs that demonstrate interacting with Azure Metrics Advisor service
using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
The following sections provide code samples covering common scenario operations with the Azure Metrics Advisor client library.

All of these samples need the endpoint to your Metrics Advisor resource ([instructions on how to get endpoint][get-endpoint-instructions]), and your Metrics Advisor API key ([instructions on how to get key][get-key-instructions]).

|**File Name**|**Description**|
|----------------|-------------|
|[DataFeedSample][data_feed_sample]
& [DataFeedAsyncSample][data_feed_async_sample] | Create, list, get, update, and delete data feeds|
|[AnomalyDetectionConfigurationSample][anomaly_detection_sample] 
& [AnomalyDetectionConfigurationAsyncSample][anomaly_detection_async_sample] | Create, list, get, update, and delete anomaly detection configurations|
|[DatafeedIngestionSample][data_feed_ingestion_sample] 
& [DatafeedIngestionSampleAsyncSample][data_feed_ingestion_async_sample] | List, get progress, and refresh data feed ingestion|
|[HookSample][hook_sample]
& [HookAsyncSample][hook_async_sample] | Create, list, get, update, and delete hooks||[MetricFeedbackSample][metric_feedback_sample]
|[MetricFeedbackSample][metric_feedback_sample]
& [MetricFeedbackAsyncSample][metric_feedback_async_sample] | Create, get and list feedbacks|

Querying API's

|**File Name**|**Description**|
|----------------|-------------|
|[ListAlertsSample][list_alerts_sample]
& [ListAlertsAsyncSample][list_alerts_async_sample] | List alerts produced by an AlertConfiguration|
|[ListDimensionValuesForMetricSample][list_dimension_values_sample]
& [ListDimensionValuesForMetricAsyncSample][list_dimension_values_async_sample] | List dimension values for a metric|
|[ListEnrichedSeriesSample][list_enrichment_series_sample]
& [ListEnrichedSeriesAsyncSample][list_enrichment_series_async_sample] | List enriched time series|
|[ListEnrichmentStatusForMetricSample][list_enrichment_status_sample]
& [ListEnrichmentStatusForMetricAsyncSample][list_enrichment_status_async_sample] | List enrichment statuses for a metric|
|[ListIncidentRootCausesSample][list_incident_root_causes_sample]
& [ListIncidentRootCausesAsyncSample][list_incident_root_causes_async_sample] | List root causes for an incident|
|[ListIncidentsAlertedSample][list_incidents_alerted_sample]
& [ListIncidentsAlertedAsyncSample][list_incidents_alerted_async_sample] | List incidents in an anomaly alert|
|[ListIncidentsDetectedSample][list_incidents_sample]
& [ListIncidentsDetectedAsyncSample][list_incidents_async_sample] | List incidents detected by a detection configuration|
|[ListsAnomaliesForAlertsSample][list_anomaly_alert_sample]
& [ListsAnomaliesForAlertsAsyncSample][list_anomaly_alert_async_sample] | List anomalies triggered for an anomaly alert|
|[ListsAnomaliesForDetectionConfigSample][list_anomalies_detection_config_sample]
& [ListsAnomaliesForDetectionConfigAsyncSample][list_anomalies_detection_config_async_sample] | List anomalies identified by a detection configuration|
|[ListSeriesDataForMetricAsyncSample][list_series_data_sample]
& [ListSeriesDataForMetricSample][list_series_data_async_sample] | List metric series data for a metric|
|[ListSeriesDefinitionsForMetricAsyncSample][list_series_def_sample]
& [ListSeriesDefinitionsForMetricAsyncSample][list_series_def_async_sample] | List series definition for a metric|
|[ListIncidentsDetectedSample][list_incidents_sample]
& [ListIncidentsDetectedAsyncSample][list_incidents_async_sample] | List incidents detected by a detection configuration|

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Check out the [API reference documentation][java_ma_ref_docs] to learn more about
what you can do with the Azure Metrics Advisor client library.

## Contributing
If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines][SDK_README_CONTRIBUTING] for more information.

<!-- LINKS -->
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor#key-concepts
[SDK_README_DEPENDENCY]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor#include-the-package
[SDK_README_NEXT_STEPS]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor#next-steps
[java_ma_ref_docs]: https://aka.ms/azsdk-java-metricsadvisor-ref-docs
[data_feed_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/administration/DatafeedSample.java
[data_feed_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/administration/DatafeedAsyncSample.java
[anomaly_detection_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/administration/AnomalyDetectionConfigurationSample.java
[anomaly_detection_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/administration/AnomalyDetectionConfigurationAsyncSample.java
[data_feed_ingestion_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/administration/DataFeedIngestionSample.java
[data_feed_ingestion_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/administration/DataFeedIngestionAsyncSample.java
[hook_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/administration/HookSample.java
[hook_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/administration/HookAsyncSample.java
[metric_feedback_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/MetricFeedbackSample.java
[metric_feedback_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/MetricFeedbackAsyncSample.java
[list_alerts_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListAlertsSample.java
[list_alerts_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListAlertsAsyncSample.java
[list_dimension_values_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListEnrichedSeriesAsyncSample.java
[list_dimension_values_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListDimensionValuesForMetricAsyncSample.java
[list_enrichment_series_sample]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListEnrichedSeriesSample.java
[list_enrichment_series_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListEnrichmentStatusForMetricAsyncSample.java
[list_enrichment_status_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListEnrichmentStatusForMetricSample.java
[list_enrichment_status_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListEnrichmentStatusForMetricAsyncSample.java
[list_incident_root_causes_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListIncidentRootCausesSample.java
[list_incident_root_causes_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListIncidentRootCausesAsyncSample.java
[list_incidents_alerted_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListIncidentsAlertedSample.java
[list_incidents_alerted_async_sample]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListIncidentsAlertedAsyncSample.java
[list_incidents_sample]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListIncidentsDetectedSample.java
[list_incidents_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListIncidentsDetectedAsyncSample.java
[list_series_def_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListSeriesDataForMetricSample.java
[list_series_def_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListSeriesDataForMetricAsyncSample.java
[list_series_data_sample]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListSeriesDefinitionsForMetricSample.java
[list_series_data_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListSeriesDefinitionsForMetricAsyncSample.java
[list_anomalies_detection_config_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListsAnomaliesForDetectionConfigSample.java
[list_anomalies_detection_config_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListsAnomaliesForDetectionConfigAsyncSample.java
[list_anomaly_alert_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListsAnomaliesForAlertsAsyncSample.java
[list_anomaly_alert_async_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/java/com/azure/ai/metricsadvisor/ListsAnomaliesForAlertsSample.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmetricsadvisor%2Fazure-ai-metricsadvisor%2FREADME.png)
