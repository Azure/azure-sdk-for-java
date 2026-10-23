---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-cognitive-services
urlFragment: ai-content-understanding-samples
---

# Azure AI Content Understanding client library samples for Java

This document explains the available samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://central.sonatype.com/artifact/com.azure/azure-ai-contentunderstanding).

## Examples
The following section documents the available examples. Each sample also has an asynchronous version with an
`Async` suffix.

1. [Update Defaults][sample00]: Update default model deployments for Content Understanding.
1. [Analyze Binary][sample01]: Analyze a document from binary data.
1. [Analyze URL][sample02]: Analyze URL input across modalities with the corresponding RAG analyzer and long-running operations.
1. [Analyze Invoice][sample03]: Extract structured fields from an invoice.
1. [Create Analyzer][sample04]: Create a custom analyzer with a field schema and inspect returned field confidence and grounding information.
1. [Create Classifier][sample05]: Create a classifier analyzer with training data.
1. [Get Analyzer][sample06]: Retrieve analyzer details.
1. [List Analyzers][sample07]: List all analyzers in the resource.
1. [Update Analyzer][sample08]: Update an existing analyzer.
1. [Delete Analyzer][sample09]: Delete an analyzer.
1. [Analyze with Configs][sample10]: Analyze with additional feature extraction (charts, formulas, etc.).
1. [Analyze and Return Raw JSON][sample11]: Capture raw JSON response via pipeline policy.
1. [Get Result File][sample12]: Retrieve result files (e.g., keyframe images from video analysis).
1. [Delete Result][sample13]: Delete analysis results to clean up server-side resources.
1. [Copy Analyzer][sample14]: Copy an analyzer within the same resource.
1. [Grant Copy Authorization][sample15]: Grant authorization for cross-resource analyzer copy using the SDK's default service API version.
1. [Create Analyzer with Labels][sample16]: Create an analyzer using labeled training data.
1. [Create Analyzer Workflow][sample17]: Compare default extraction with agentic reasoning for answers built from evidence, including latency and billing considerations.
1. [Analyze Chunking][sample18]: Configure semantic chunks for retrieval and resolve each chunk's spans into Markdown.
1. [Analyze Inline][sample19]: Compare inline URL analysis with LRO analysis, including supported analyzers, persistence, limits, and failure behavior.
1. [Analyze Binary Inline][sample20]: Analyze binary input inline, select a page range, and handle the five-page input limit.
1. [Analysis Diagnostics][sample_advanced_diagnostics]: Inspect human-readable diagnostics returned in `infos`.
1. [Classify In-Page Segments][sample_advanced_in_page]: Classify sub-page documents such as supplemental statements and inspect confidence, source expressions, and spans.
1. [Detect Signatures][sample_advanced_signatures]: Inspect signatures detected in document content.
1. [Extract Document Metadata][sample_advanced_metadata]: Inspect metadata extracted from PDF and DOCX files.
1. [Content Source][sample_advanced_content_source]: Work with source expressions returned for analyzed content.
1. [Convert to LLM Input][sample_advanced_llm_input]: Convert analysis results and metadata into LLM-ready YAML and Markdown.

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
[sample17]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample17_CreateAnalyzerWorkflow.java
[sample18]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample18_AnalyzeChunking.java
[sample19]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample19_AnalyzeInline.java
[sample20]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample20_AnalyzeBinaryInline.java
[sample_advanced_diagnostics]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_AnalysisDiagnostics.java
[sample_advanced_in_page]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_ClassifyInPageSegments.java
[sample_advanced_signatures]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_DetectSignatures.java
[sample_advanced_metadata]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_ExtractDocumentMetadata.java
[sample_advanced_content_source]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_ContentSource.java
[sample_advanced_llm_input]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_ToLlmInput.java
