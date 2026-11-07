# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

- Added `PublicApiTypeCheck`, combining implementation-type and external-dependency validation.
- Added `ExceptionHandlingCheck`, combining exception logging, unthrown logging events, and caught-cause validation,
  with separate Azure Core and ClientCore logging policies.

### Breaking Changes

- Removed the unused `JavadocInlineTagCheck` and `JavadocCodeSnippetCheck` classes.
- Removed the consolidated rules' previous classes: `NoImplInPublicAPI`, `ExternalDependencyExposedCheck`,
  `ExceptionCreatedButNotThrownCheck`, `ThrowFromClientLoggerCheck`, `RawExceptionThrowCheck`, and
  `UseCaughtExceptionCauseCheck`. Configure `PublicApiTypeCheck` and `ExceptionHandlingCheck` instead.
- Diagnostics and suppression `checks` selectors now use the merged rule names. Existing suppressions must migrate
  to `PublicApiTypeCheck` or `ExceptionHandlingCheck` and apply to the entire merged rule.

### Bugs Fixed

- Preserve `StepVerifierCheck` diagnostics in test sources and reset its static-import state between files.
- Recognize throwable logging chains with intermediate builder calls consistently in exception checks.

### Other Changes

- Reduce repeated method-signature analysis, import classification, catch-alias allocations, and logging-call
  identifier construction. Skip filtered test and sample sources before expensive production-rule analysis.

## 1.0.0-beta.2 (2026-03-17)

### Features Added

- Added `SerializableMethodsCheck` to validate that `JsonSerializable` and `XmlSerializable` implementations have the required methods for serialization and deserialization.

## 1.0.0-beta.1 (2025-07-22)

Initial release of the SDK Linting Extensions.

### Features Added

- Added Checkstyle extensions for enforcing SDK guidelines.
- Added RevApi extensions for API compatibility checks.
