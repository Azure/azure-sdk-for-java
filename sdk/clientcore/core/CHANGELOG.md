# Release History

## 1.0.0-beta.12 (Unreleased)

### Features Added

### Breaking Changes
- Changed `HttpRetryOptions.delayFromHeaders` to `HttpRetryOptions.delayFromRetryCondition`. The `Function` is now a
  `Function<HttpRetryCondition, Duration>` instead of `Function<HttpHeaders, Duration>`. This allows richer inspection
  of the reason the request failed and is being retried when calculating the delay. ([#46384](https://github.com/Azure/azure-sdk-for-java/pull/46384))

### Bugs Fixed

### Other Changes

## 1.0.0-beta.11 (2025-07-21)

### Features Added
- Add HTTP2 support to JDK HTTP client (#[45758](https://github.com/Azure/azure-sdk-for-java/pull/45758))
- Add support for no auth and scopes override in Client Core (#[45635](https://github.com/Azure/azure-sdk-for-java/pull/45635))

## 1.0.0-beta.10 (2025-06-02)

### Features Added
- Added new logging exception checks (#[45433](https://github.com/Azure/azure-sdk-for-java/pull/45433))
- Added support for serializing lists of BinaryData (#[45423](https://github.com/Azure/azure-sdk-for-java/pull/45423))
- Added support for Java 24 (#[45265](https://github.com/Azure/azure-sdk-for-java/pull/45265))

## 1.0.0-beta.9 (2025-04-30)

### Features Added
- Added RestProxy back [#45149](https://github.com/Azure/azure-sdk-for-java/pull/45149)
- Added CoreException type as the base type for all exceptions [#44892](https://github.com/Azure/azure-sdk-for-java/pull/44892)

## 1.0.0-beta.8 (2025-04-03)

### Breaking Changes
- Redesign RequestOptions - merge with context [#44535](https://github.com/Azure/azure-sdk-for-java/pull/44535)
- API updates to refactor [#44655](https://github.com/Azure/azure-sdk-for-java/pull/44655)
- Remove ResponseBodyMode [#44635](https://github.com/Azure/azure-sdk-for-java/pull/44635)
- Remove RestProxy code [#44660](https://github.com/Azure/azure-sdk-for-java/pull/44660)

### Bugs Fixed
- Fix vNext Bearer Token Policy [#44803](https://github.com/Azure/azure-sdk-for-java/pull/44803/)

## 1.0.0-beta.7 (2025-03-12)

### Breaking Changes
 - API updates to refactor and cleanup public APIs [#44565](https://github.com/Azure/azure-sdk-for-java/pull/44565), [#44555](https://github.com/Azure/azure-sdk-for-java/pull/44555) and [#44592](https://github.com/Azure/azure-sdk-for-java/pull/44592)

## 1.0.0-beta.6 (2025-03-10)

### Breaking Changes

- `JsonNumber` previously would use `float` when the floating point number was small enough to fit in `float` but it
  now aligns behavior with `JsonReader.readUntyped()` where `double` will be the smallest floating point type used.
  This aligns with floating point number behavior in Java where `double` is the default if no type is specified.
- Support for special numeric `INF`, `-INF`, and `+INF` values have been removed to align with behaviors of `Float`
  and `Double` in Java where only the `Infinity` variants are supported.

### Bugs Fixed

- `JsonReader.readUntyped()` had incomplete support for untyped numerics. Numerics too large for `double` and `long` are
  now supported and a bug where exponents were not being parsed correctly is fixed.

## 1.0.0-beta.5 (2025-02-14)

### Features Added
- Support for deserialization of list of `JsonSerializable` types [#44208](https://github.com/Azure/azure-sdk-for-java/pull/44208)

### Breaking Changes
- API updates to refactor packages and remove unused APIs [#44210](https://github.com/Azure/azure-sdk-for-java/pull/44210)

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
