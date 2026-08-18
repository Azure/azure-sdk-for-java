# Release History

## 1.1.0-beta.3 (2026-08-18)

### Features Added

- Added support for service API version `2026-06-01-preview`, which is the default for this beta package.
- Added synchronous and asynchronous minimal and options-based `analyzeInline` and `analyzeBinaryInline` APIs that return
  `ContentAnalyzerInlineResponse` without long-running operation polling, preserving the inline status,
  `AnalysisResult`, and `UsageDetails`. See the
	[analysis guidance](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/README.md#asynchronous-operations),
	[Sample 19](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample19_AnalyzeInline.java), and
	[Sample 20](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample20_AnalyzeBinaryInline.java).
- Added `AnalyzeOptions` and `AnalyzeBinaryOptions` overloads for model deployment overrides, input truncation,
	processing location, content range, and content type. See the
	[analysis options guidance](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/README.md#asynchronous-operations),
	[Sample 01](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample01_AnalyzeBinary.java),
	[Sample 19](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample19_AnalyzeInline.java), and
	[Sample 20](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample20_AnalyzeBinaryInline.java).
- Added analyzer workflow selection via `ContentAnalyzerConfig.setWorkflow` and `ContentAnalyzerWorkflow`. See
  [Sample 17](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample17_CreateAnalyzerWorkflow.java).
- Added semantic chunking via `ContentAnalyzerConfig.setChunkingStrategy` and `SemanticChunkingStrategy`, with
  `DocumentChunk` results available from `DocumentContent.getChunks`. See
  [Sample 18](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample18_AnalyzeChunking.java).
- Added in-page segmentation via `ContentAnalyzerConfig.setAllowInPageSegments`. See
  [Classify in-page segments](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_ClassifyInPageSegments.java).
- Added signature detection via `DocumentSignature` and `DocumentContent.getSignatures`. See
  [Detect signatures](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_DetectSignatures.java).
- Added embedded document metadata via `AnalysisContent.getMetadata`. See
  [Extract document metadata](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_ExtractDocumentMetadata.java).
- Added analysis diagnostics via `AnalysisResult.getInfos`. See
  [Analysis diagnostics](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_AnalysisDiagnostics.java).
- Added expanded `UsageDetails` values for long-running and inline analysis. See the
  [usage guidance](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/README.md#asynchronous-operations),
  [Sample 03](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample03_AnalyzeInvoice.java),
  [Sample 19](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample19_AnalyzeInline.java), and
  [Sample 20](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample20_AnalyzeBinaryInline.java).

### Other Changes

- Preserved source and binary compatibility with the official `1.0.0` Java API while adding preview capabilities,
	including the canonical `Analysis*` and `Content*Field` models and typed `ContentRange` binary overload.
- Updated `LlmInputHelper.toLlmInput` front matter with detected MIME type, separate caller and service metadata,
	stable warning details, nested null/empty values, and multiline YAML scalar handling. See
	[ToLlmInput](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_ToLlmInput.java).

## 1.1.0-beta.2 (2026-06-11)

### Features Added

- `DocumentSource` now parses polygons with any number of points (three or more pairs) instead of requiring exactly four, and supports the page-only `D(page)` form. When only a page number is available, `getPolygon()` and `getBoundingBox()` return `null`.
- Added `Sample_Advanced_ContentSource` and `Sample_Advanced_ContentSourceAsync` samples demonstrating how to read document grounding sources and render field highlight overlays.

### Bugs Fixed

- Filtered service-emitted `LLMStats:` telemetry entries from the rendered `rai_warnings` front matter in `LlmInputHelper.toLlmInput`.

### Other Changes

- `Sample16_CreateAnalyzerWithLabels`: aligned with the .NET parity sample. The labeled-receipt field schema now uses `TotalPrice` (was `Total`), and the sample supports auto-uploading the bundled label files via `DefaultAzureCredential` (Option B — set `CONTENTUNDERSTANDING_TRAINING_DATA_STORAGE_ACCOUNT` and `CONTENTUNDERSTANDING_TRAINING_DATA_CONTAINER`) in addition to the existing pre-generated SAS URL flow (Option A — `CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL`). When neither option is configured the sample now prints a clear `DEMO MODE` banner.
- Updated `LlmInputHelper.toLlmInput` page markers from `<!-- page N -->` to `<!-- InputPageNumber: N -->` and avoided duplicate marker injection when the service markdown already includes `InputPageNumber` markers.

## 1.1.0-beta.1 (2026-05-01)

### Features Added

- Added `toLlmInput` helper that converts `AnalysisResult` into LLM-friendly text with YAML front matter and markdown content. Supports documents, audio/video, and classification hierarchies.

## 1.0.0 (2026-03-02)

### Features Added

- Initial GA release of the Azure Content Understanding client library for Java.
- Support for creating, getting, listing, and deleting analyzers.
- Support for analyzing content with `beginAnalyze` long-running operations.
- Support for getting and deleting analysis results.
- Synchronous and asynchronous client support via `ContentUnderstandingClient` and `ContentUnderstandingAsyncClient`.
- Builder pattern for client construction with `ContentUnderstandingClientBuilder`.
