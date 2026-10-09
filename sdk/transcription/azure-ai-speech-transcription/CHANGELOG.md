# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

- Documented that `locales` is now honored in Enhanced Mode. The service operates in multi-lingual mode by default; if specified, the first locale is used as a hint to guide recognition.

## 1.0.0 (2026-05-18)

First stable release of the Azure AI Speech Transcription client library for Java.

### Breaking Changes

- Replaced the single-argument `transcribeWithResponse(TranscriptionOptions)` convenience overload on `TranscriptionClient` and `TranscriptionAsyncClient` with `transcribeWithResponse(TranscriptionOptions, RequestOptions)`, aligning with the Azure SDK for Java guideline that the maximal `*WithResponse` overload must accept `RequestOptions`. Callers can pass `null` to use defaults.
- `TranscriptionDiarizationOptions` no longer has a no-arg constructor. Callers must now explicitly pass an `enabled` flag via the new `TranscriptionDiarizationOptions(boolean enabled)` constructor, allowing diarization to be set to either `true` or `false`. The `isEnabled()` getter is retained.
- Removed `TranscriptionContent` from the public API. It was an internal multipart-request-body wrapper that was never accepted or returned by any public method; the public `transcribe` / `transcribeWithResponse` overloads now build the multipart body internally.
- Changed the return type of `TranscribedPhrase.getOffset()` and `TranscribedWord.getOffset()` from `int` (milliseconds) to `java.time.Duration` to align with the idiomatic Java type already used by `getDuration()` and to let callers easily convert/compare across units.

## 1.0.0-beta.3 (2026-04-22)

### Other Changes

- Updated package metadata (display name and description) to align with Azure SDK guidelines.
- Internal cleanup and lint fixes. No public API changes.

## 1.0.0-beta.2 (2026-02-05)

### Bugs Fixed

- Fixed `EnhancedModeOptions` to properly serialize the `enabled` field in JSON requests, enabling translation and other enhanced mode features to work correctly.

### Other Changes

- Updated enhanced mode samples to demonstrate transcribe, translate, prompt tuning, and diarization scenarios.

## 1.0.0-beta.1 (2026-01-05)

### Features Added

- Initial release of Azure AI Speech Transcription client library for Java.
