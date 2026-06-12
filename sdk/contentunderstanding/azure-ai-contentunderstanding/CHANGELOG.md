# Release History

## 1.1.0-beta.2 (2026-06-02)

### Features Added

- `DocumentSource` now parses polygons with any number of points (three or more pairs) instead of requiring exactly four, and supports the page-only `D(page)` form. When only a page number is available, `getPolygon()` and `getBoundingBox()` return `null`.
- Added `Sample_Advanced_ContentSource` and `Sample_Advanced_ContentSourceAsync` samples demonstrating how to read document grounding sources and render field highlight overlays.

- `Sample16_CreateAnalyzerWithLabels`: aligned with the .NET parity sample. The labeled-receipt field schema now uses `TotalPrice` (was `Total`), and the sample supports auto-uploading the bundled label files via `DefaultAzureCredential` (Option B \u2014 set `CONTENTUNDERSTANDING_TRAINING_DATA_STORAGE_ACCOUNT` and `CONTENTUNDERSTANDING_TRAINING_DATA_CONTAINER`) in addition to the existing pre-generated SAS URL flow (Option A \u2014 `CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL`). When neither option is configured the sample now prints a clear `DEMO MODE` banner.

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
