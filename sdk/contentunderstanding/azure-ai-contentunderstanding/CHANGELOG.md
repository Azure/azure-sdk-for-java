# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

- Filtered service-emitted `LLMStats:` telemetry entries from the rendered `rai_warnings` front matter in `LlmInputHelper.toLlmInput`.

### Other Changes

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
