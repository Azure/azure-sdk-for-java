# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

- Added `LlmInputHelper.toLlmInput()` static helper that converts an `AnalysisResult` into LLM-ready text
  (YAML front matter + markdown). Supports all content types (documents, images, audio, video),
  multi-segment results, and classification hierarchies.
- Added `ToLlmInputOptions` for controlling output (fields-only, markdown-only, custom metadata).

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2026-03-02)

### Features Added

- Initial GA release of the Azure Content Understanding client library for Java.
- Support for creating, getting, listing, and deleting analyzers.
- Support for analyzing content with `beginAnalyze` long-running operations.
- Support for getting and deleting analysis results.
- Synchronous and asynchronous client support via `ContentUnderstandingClient` and `ContentUnderstandingAsyncClient`.
- Builder pattern for client construction with `ContentUnderstandingClientBuilder`.
