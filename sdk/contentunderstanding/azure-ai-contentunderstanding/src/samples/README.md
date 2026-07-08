---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-cognitive-services
urlFragment: ai-content-understanding-samples
---

# Azure AI Content Understanding Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

# Azure AI Content Understanding client library samples for Java
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://central.sonatype.com/artifact/com.azure/azure-ai-contentunderstanding).

## Examples
   Following section document various examples.

1. [Update Defaults][sample00]: Update default model deployments for Content Understanding.
1. [Analyze Binary][sample01]: Analyze a document from binary data.
1. [Analyze URL][sample02]: Analyze documents, images, audio, and video from URLs.
1. [Analyze Invoice][sample03]: Extract structured fields from an invoice.
1. [Create Analyzer][sample04]: Create a custom analyzer with field schema.
1. [Create Classifier][sample05]: Create a classifier analyzer with training data.
1. [Get Analyzer][sample06]: Retrieve analyzer details.
1. [List Analyzers][sample07]: List all analyzers in the resource.
1. [Update Analyzer][sample08]: Update an existing analyzer.
1. [Delete Analyzer][sample09]: Delete an analyzer.
1. [Analyze with Configs][sample10]: Analyze with additional feature extraction (charts, formulas, etc.).
1. [Analyze and Return Raw JSON][sample11]: Capture raw JSON response via pipeline policy.
1. [Get Result File][sample12]: Retrieve result files (e.g., keyframe images from video analysis).
1. [Delete Result][sample13]: Delete analysis results to clean up server-side resources.
1. [Copy Analyzer][sample14]: Copy an analyzer within the same or across resources.
1. [Grant Copy Authorization][sample15]: Grant authorization for cross-resource analyzer copy.
1. [Create Analyzer with Labels][sample16]: Create an analyzer using labeled training data.

## Troubleshooting
When interacting with Azure AI Content Understanding using this Java client library, errors returned by the service
correspond to the same HTTP status codes returned for [REST API][error_codes] requests. For example, if you try to
retrieve an analyzer that doesn't exist, a `404` error is returned, indicating `Not Found`.

## Next steps
Start using Azure AI Content Understanding Java SDK in your solutions. Our SDK details could be found at [SDK README][SDK_README].

### Additional Documentation
For more extensive documentation on Azure AI Content Understanding, see the [API reference documentation][api_reference].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/README.md#getting-started
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/README.md#key-concepts
[api_reference]: https://learn.microsoft.com/azure/ai-services/content-understanding/
[error_codes]: https://learn.microsoft.com/azure/ai-services/content-understanding/
[sample00]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample00_UpdateDefaults.java
[sample01]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample01_AnalyzeBinary.java
[sample02]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample02_AnalyzeUrl.java
[sample03]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample03_AnalyzeInvoice.java
[sample04]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample04_CreateAnalyzer.java
[sample05]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample05_CreateClassifier.java
[sample06]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample06_GetAnalyzer.java
[sample07]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample07_ListAnalyzers.java
[sample08]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample08_UpdateAnalyzer.java
[sample09]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample09_DeleteAnalyzer.java
[sample10]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample10_AnalyzeConfigs.java
[sample11]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample11_AnalyzeReturnRawJson.java
[sample12]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample12_GetResultFile.java
[sample13]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample13_DeleteResult.java
[sample14]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample14_CopyAnalyzer.java
[sample15]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample15_GrantCopyAuth.java
[sample16]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample16_CreateAnalyzerWithLabels.java
