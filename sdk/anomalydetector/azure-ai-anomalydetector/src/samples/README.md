---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-cognitive-services
  - azure-anomaly-detector
urlFragment: anomalydetector-java-samples
---

# Azure Anomaly Detector client library samples for Java

Azure Anomaly Detector samples are a set of self-contained Java programs that demonstrate interacting with Azure self-contained service using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts

Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started

Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples

The following sections provide code samples covering common scenario operations with the Azure Anomaly Detector client library.

All of these samples need the endpoint to your Anomaly Detector resource, and your Anomaly Detector API key.

|**File Name**|**Description**|
|----------------|-------------|
|[DetectAnomaliesEntireSeries.java][detect_anomaly_entire]|Detect anomalies as a batch|
|[DetectAnomaliesLastPoint.java][detect_anomaly_last]|Detect if last point is anomaly|
|[DetectChangePoints.java][detect_change_point]|Detect change points in series|

## Troubleshooting

Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps

See [Next steps][SDK_README_NEXT_STEPS].

## Contributing

If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines][SDK_README_CONTRIBUTING] for more information.

<!-- LINKS -->
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/README.md#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#include-the-package
[SDK_README_NEXT_STEPS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/README.md#next-steps

[detect_anomaly_entire]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/java/com/azure/ai/anomalydetector/DetectAnomaliesEntireSeries.java
[detect_anomaly_last]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/java/com/azure/ai/anomalydetector/DetectAnomaliesLastPoint.java
[detect_change_point]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/java/com/azure/ai/anomalydetector/DetectChangePoints.java
