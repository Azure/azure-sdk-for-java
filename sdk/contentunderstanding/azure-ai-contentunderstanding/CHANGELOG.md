# Release History

## 1.1.0-beta.3 (Unreleased)

### Features Added

- Added GitHub Copilot skills under `.github/skills/` to help users
  iteratively author custom analyzers in VS Code with Copilot:
  - **`cu-sdk-author-analyzer`** — author and refine a custom analyzer
    for a single document type (layout extraction → schema drafting →
    validation → batch test → agent review → refine cycle). Document
    modality only today; audio/video/image are planned.
  - **`cu-sdk-author-analyzer-classify-route`** — author and refine a
    classify-and-route pipeline for mixed-document packets (e.g.
    invoice + bank statement + loan application in one PDF), with
    per-category agent review.

  Both skills delegate to a small `cu-skill` Maven tool under
  `.github/skills/_shared/` that exposes three subcommands —
  `extract-layout`, `create-and-test`, and `create-and-test-router` —
  and a pure-Java `SchemaValidator` that catches structural mistakes
  (unknown `baseAnalyzerId`, missing `fieldSchema`, malformed
  `contentCategories` routes) before a service round-trip.

### Breaking Changes

### Bugs Fixed

### Other Changes

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
