# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2025-02-13)

### Features Added
- Introduced Union Type [#43778](https://github.com/Azure/azure-sdk-for-java/pull/43778)
- Added support for convenience APIs for generic instrumentation plumbing when no special conventions defined [#44006](https://github.com/Azure/azure-sdk-for-java/pull/44006)
- Added metrics support and reporting http request duration in instrumentation policy [#43957](https://github.com/Azure/azure-sdk-for-java/pull/43957)

### Breaking Changes
- Streamline APIs used when creating an HttpPipeline using HttpPipelineBuilder to help guide users through creating a high quality HttpPipeline. [#43504](https://github.com/Azure/azure-sdk-for-java/pull/43504)

## 1.0.0-beta.3 (2025-01-27)

### Breaking Changes
- Downgraded clientcore baseline from Java 17 to Java 8.
- Updated clientcore into a multi-release JAR. The Java 8 version of the JAR will be used for Java 8-16, and the Java 17 version of the JAR will be used for Java 17+.

## 1.0.0-beta.2 (2025-01-17)

### Features Added

- Added `PagedResponse`, `PagedOptions`, and `PagedIterable`, for supporting pagination.

## 1.0.0-beta.1 (2024-12-20)

### Features Added

- Initial release. Please see the README and wiki for more information.
  This package contains core types for building Java client libraries for accessing web services.
