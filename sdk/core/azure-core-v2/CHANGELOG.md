# Release History

## 1.50.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.49.1 (2024-06-06)

### Bugs Fixed

- Fixed a bug where some policies didn't close the connection appropriately. ([#40052](https://github.com/Azure/azure-sdk-for-java/pull/40052))
- Fixed an issue where HTTP response headers would be logged twice. ([#40190](https://github.com/Azure/azure-sdk-for-java/pull/40190))
- Fixed a bug where container types wouldn't resolve to using `JsonSerializable` serialization for values. ([#40112](https://github.com/Azure/azure-sdk-for-java/pull/40112))
- Fixed a bug where a generic `ObjectMapper` couldn't consume `CloudEvent` correctly. ([#40332](https://github.com/Azure/azure-sdk-for-java/pull/40332))

### Other Changes

#### Dependency Updates

- Upgraded Reactor Core from `3.4.36` to `3.4.38`.

## 1.49.0 (2024-05-01)

### Features Added

- Added `SharedExecutorService` which acts as a global thread pool for the SDKs to use. ([#38860](https://github.com/Azure/azure-sdk-for-java/pull/38860))

### Bugs Fixed

- Fixed an issue where HTTP responses would not be logged if the response body was never consumed. ([#39964](https://github.com/Azure/azure-sdk-for-java/pull/39964))

### Other Changes

- Added default time to `CloudEvent` to be the current time if not set. ([#39751](https://github.com/Azure/azure-sdk-for-java/pull/39751))
- Deprecated APIs exposing Jackson types. ([#39563](https://github.com/Azure/azure-sdk-for-java/pull/39563))

## 1.48.0 (2024-04-05)

### Features Added

- Added dependency on `azure-xml` to support `XmlSerializable`, removing the need for Jackson Databind XML to handle
  XML types generated and used by the SDKs.
- Added new methods on `com.azure.core.util.tracing.Tracer` - `isRecording` and `addAttribute(String, Object, Context)`.
- Added `CoreUtils.parseBestOffsetDateTime` which can handle cases when the `dateString` doesn't include a time zone.

### Bugs Fixed

- Fixed a bug where `text/event-stream` content type wasn't being handled correctly.
  Replaced content type exact match `equals` by `startsWith`. ([#39128](https://github.com/Azure/azure-sdk-for-java/issues/39128))

### Other Changes

- Made `azure-json` `requires transitive` in `module-info.java`.

#### Dependency Updates

- Upgraded Reactor Core from `3.4.34` to `3.4.36`.

## 1.47.0 (2024-03-01)

### Features Added

- Added `CoreUtils.addShutdownHookSafely(Thread)` which is a more generic version of 
  `CoreUtils.addShutdownHookSafely(ExecutorService, Duration)`. ([#38730](https://github.com/Azure/azure-sdk-for-java/pull/38730))

## 1.46.0 (2024-02-02)

### Features Added

- Added `SyncPoller.getFinalResult(Duration)` to get the final result of a long-running operation with a timeout
  period. Allows for a single call rather than calling both `SyncPoller.waitForCompletion(Duration)` and
  `SyncPoller.getFinalResult()`.
- Serialization model types now implement `JsonSerializable`. ([#37046](https://github.com/Azure/azure-sdk-for-java/pull/37046))
- Added `CoreUtils.durationToStringWithDays`, exposing how serialization of `Duration` is done. ([#37763](https://github.com/Azure/azure-sdk-for-java/pull/37763))
- Prevent requests that won't retry from being buffered. ([#37871](https://github.com/Azure/azure-sdk-for-java/pull/37871))
- Added `BinaryData.writeTo(OutputStream)`, `BinaryData.writeTo(WriteableByteChannel)`, 
  and `BinaryData.writeToAsync(AsynchronousByteChannel)` to allow writing the content of `BinaryData` to an 
  `OutputStream`, `WriteableByteChannel`, or `AsynchronousByteChannel` respectively. ([#38271](https://github.com/Azure/azure-sdk-for-java/pull/38271))
- Added `HttpRetryOptions.shouldRetryCondition`, `RetryStrategy.shouldRetryCondition` and `RequestRetryCondition` to allow 
  `HttpRetryOptions` to determine which  HTTP responses and exceptions can be retried. ([#38585](https://github.com/Azure/azure-sdk-for-java/pull/38585))
- Added `CoreUtils.addShutdownHookSafely` to add a shutdown hook with possible usage of `AccessController` to add it
  in a privileged manner. ([#38580](https://github.com/Azure/azure-sdk-for-java/pull/38580))

### Breaking Changes

- `SyncPoller.waitForCompletion(Duration)` now throws an exception if the polling operation doesn't complete or reach 
  the status within the give duration.

### Bugs Fixed

- Fixed `RetryPolicy` usage of calculated delay. ([#37788](https://github.com/Azure/azure-sdk-for-java/pull/37788))

### Other Changes

- `HttpLoggingPolicy` uses better defaults for allowed headers and query parameters to log. ([#37686](https://github.com/Azure/azure-sdk-for-java/pull/37686))
- Performance improvements in `LoggingEventBuilder`. ([#37967](https://github.com/Azure/azure-sdk-for-java/pull/37967))
- Performance improvements to HTTP header logging. ([#38492](https://github.com/Azure/azure-sdk-for-java/pull/38492))
- Reduce redacted header logging. ([#38501](https://github.com/Azure/azure-sdk-for-java/pull/38501))

#### Dependency Updates

- Upgraded Reactor Core from `3.4.33` to `3.4.34`.

## 1.45.0 (2023-11-03)

### Features Added

- Added `PollOperationDetails` as details of long-running operations.

### Bugs Fixed

- Checks for HTTPS requirement when using certain credentials now checks for the protocol not being 'https'. ([#37454](https://github.com/Azure/azure-sdk-for-java/pull/37454))

## 1.44.1 (2023-10-17)

### Other Changes

#### Dependency Updates

- Upgraded Reactor Core from `3.4.31` to `3.4.33`.

## 1.44.0 (2023-10-06)

### Features Added

- Added `CoreUtils.getResultWithTimeout` to get a value of a `Future` and cancel it if the `get` times out. ([#37055](https://github.com/Azure/azure-sdk-for-java/pull/37055))
- Added `transfer` and `transferAsync` overloads in `IOUtils` that accept an estimated data size to attempt to better
  optimize transfers to reduce reads and writes. ([#36650](https://github.com/Azure/azure-sdk-for-java/pull/36650))

### Bugs Fixed

- Fixed a bug where `FluxByteBufferContent.toReplayableContent()` didn't eagerly make the `Flux<ByteBuffer>` replayable. ([#36999](https://github.com/Azure/azure-sdk-for-java/pull/36999))

### Other Changes

- REST calls using `SyncRestProxy` no longer logs unexpected HTTP status code responses to align with the asynchronous
  behavior in `AsyncRestProxy`. ([#36680](https://github.com/Azure/azure-sdk-for-java/pull/36680))
- Rewrote internal reflection handling to better support Android. ([#36612](https://github.com/Azure/azure-sdk-for-java/pull/36612))
- Replaced `doFinally` with `Mono.using` and `Flux.using`. ([#36997](https://github.com/Azure/azure-sdk-for-java/pull/36997))

## 1.43.0 (2023-09-07)

### Features Added

- Added `KeyCredentialTrait` interface. Client builders that implement this interface will support key-based authentication.

### Bugs Fixed

- Fixed a blocking call reported by `BlockHound`. ([#36384](https://github.com/Azure/azure-sdk-for-java/pull/36384))

### Other Changes

#### Dependency Updates

- Upgraded Reactor Core from `3.4.30` to `3.4.31`.

## 1.42.0 (2023-08-04)

### Features Added
- Added `setCaeEnabled` and `isCaeEnabled` APIs to `TokenRequestContext` to indicate CAE authentication flow to be used downstream.

### Bugs Fixed

- Fixed a bug where unknown status was treated as complete status, 
  in long-running operation with `OperationResourcePollingStrategy` polling strategy. 
  ([#35628](https://github.com/Azure/azure-sdk-for-java/issues/35628))
- Fixed a bug where JacksonDatabind215 throws AccessControlException under SecurityManager
  ([#36187](https://github.com/Azure/azure-sdk-for-java/issues/36187))
- Prevent FluxUtil from using MappedByteBuffer on Windows to fix a Java runtime behavior bug in Windows
  ([#36168](https://github.com/Azure/azure-sdk-for-java/pull/36168))

## 1.42.0-beta.1 (2023-07-24)

### Features Added
- Added `setEnableCae` and `isCaeEnabled` APIs to `TokenRequestContext` to indicate CAE authentication flow to be used downstream.

## 1.41.0 (2023-07-06)

### Features Added

- Added a `KeyCredential` to support key-based auth.
- `AzureKeyCredential` now to extends from `KeyCredential`.

### Bugs Fixed

- Fixed a bug where `PagedIterable.mapPage` would result in a `NullPointerException`. ([#35123](https://github.com/Azure/azure-sdk-for-java/pull/35123))

## 1.40.0 (2023-06-02)

### Features Added

- Added `TracingOptions` configurations allowing to pick a specific `TracerProvider` implementation if several are resolved by `ServiceLoader`. 
- Added `MetricsOptions` configurations allowing to pick a specific `MeterProvider` implementation if several are resolved by `ServiceLoader`.
- Added `CoreUtils.randomUuid` to replace usage of `UUID.randomUUID`. In some cases `UUID.randomUUID` used a blocking 
  call whereas `CoreUtils.randomUuid` should never block. ([#34790](https://github.com/Azure/azure-sdk-for-java/pull/34790))
- Added support for prefixes in `AzureKeyCredentialPolicy`. ([#35010](https://github.com/Azure/azure-sdk-for-java/pull/35010))
- Added the ability to configure a backoff strategy for `FluxUtil.createRetriableDownloadFlux`. Previous retries 
  wouldn't backoff which could result in requests being sent to a service already at capacity and throttling. ([#35035](https://github.com/Azure/azure-sdk-for-java/pull/35035))

### Bugs Fixed

- Fixed a bug where a known length wasn't passed to `BinaryData.fromStream` resulting it being handled as a 
  non-replayable `BinaryData`. ([#34851](https://github.com/Azure/azure-sdk-for-java/pull/34851))
- Changed the design of how `AsynchronousByteChannel`s were written to limit chances of race conditions between the
  writer thread and the Reactor thread handling `onComplete` and `onError` events. This results in more consistent 
  behavior at the cost of lower throughput, which will be investigated in future releases. ([#35004](https://github.com/Azure/azure-sdk-for-java/pull/35004))

### Other Changes

- Changed how `ResponseError` is deserialized to support cases where the JSON wrapped the `ResponseError` with an 
  `error` property. ([#35052](https://github.com/Azure/azure-sdk-for-java/pull/35052))

### Dependency Updates

- Upgraded Reactor Core from `3.4.27` to `3.4.29`.

## 1.39.0 (2023-05-04)

### Features Added

- Added `HttpHeaders.setAllHttpHeaders(HttpHeaders)` to provide a way to efficiently combine two `HttpHeaders`.
- Added `CoreUtils.parseQueryParameters` to efficient parse query parameters without array overhead found with `String.split`.

### Breaking Changes

- Deprecated `String`-based APIs on `HttpHeaders`, `HttpRequest`, `HttpResponse`, and `RequestOptions`. Use 
  `HttpHeaderName`-based APIs instead as they provide better performance.

### Bugs Fixed

- Fixed an edge case when parsing query parameters without a value (`key&key2=value2`). ([#34459](https://github.com/Azure/azure-sdk-for-java/pull/34459))

### Other Changes

- Changed how handling supporting multiple versions of Jackson happens internally to reduce usage of reflection. ([#34468](https://github.com/Azure/azure-sdk-for-java/pull/34468))
- Improved request and response body logging to reduce memory allocations.

## 1.38.0 (2023-04-07)

### Features Added

- Added new constructor overload to `DefaultPollingStrategy`, `OperationResourcePollingStrategy`, `LocationPollingStrategy`
  and their sync counterparts that allows setting a service version as query parameter of request URLs for polling and 
  getting the final result of a long-running operation.

### Other Changes

- Added dependency on `azure-json` to provide stream-style JSON serialization support for `JsonSerializable`
  implementing classes.

## 1.37.0 (2023-03-02)

### Features Added

- Added `CoreUtils.bytesToHexString` as a common utility for creating hex strings.
- Added `CoreUtils.extractSizeFromContentRange` as a common utility for extracting the entity size from a properly
  formatted `Content-Range` header.
- Added support for `JsonSerializable` and `XmlSerializable` in `JacksonAdapter` serialize and deserialize APIs. ([#33685](https://github.com/Azure/azure-sdk-for-java/pull/33685))

### Breaking Changes

- Removed `jackson-dataformat-xml` as a dependency. XML serialization is still supported when `jackson-dataformat-xml`
  is found on the classpath, if it's not a class not found exception/error will be thrown when attempting to use XML
  serialization. ([#33401](https://github.com/Azure/azure-sdk-for-java/pull/33401))

### Bugs Fixed

- Fixed a bug where a method not found error would be thrown when attempting to reflectively access Jackson Databind 2.12+
  APIs. ([#33448](https://github.com/Azure/azure-sdk-for-java/pull/33448))
- Fixed a bug where HTTP headers and query parameters weren't included in HTTP request and response logging when
  `INFORMATIONAL` log level was used.

### Dependency Updates

- Upgraded Reactor Core from `3.4.26` to `3.4.27`.
- Upgraded Jackson to the latest releases `2.13.5`.

## 1.36.0 (2023-02-01)

### Features Added

- Added explicit support for `text`-based serialization, this is done by adding `SerializerEncoding.TEXT`. Previously,
  `text` was being implicitly supported by using `SerializerEncoding.JSON` but there were edge cases when a `String`
  wasn't a JSON string (`string` vs `"string"`). ([#32277](https://github.com/Azure/azure-sdk-for-java/pull/32277))
- Added `BinaryData.fromListByteBuffer(List<ByteBuffer>)` to support additional ways to create `BinaryData`. ([#32932](https://github.com/Azure/azure-sdk-for-java/pull/32932))
- Added `TracingOptions` to make tracing configurable. ([#32573](https://github.com/Azure/azure-sdk-for-java/pull/32573))
- Added support for links, start timestamp, W3C trace-context propagation, and numerical attributes in tracing.
- Tracing plugins are no longer required to implement `AfterRetryPolicyProvider` as tracing is now handled by
  `InstrumentationPolicy` using the provided `Tracer` implementation.

### Breaking Changes

- Deprecated messaging-specific methods in tracing abstractions.

### Bugs Fixed

- Fixed a bug where `PollingStrategy.getResult` would guard getting results for `POST`-based on containing a `Location`
  header which isn't guaranteed to exist. If the header doesn't exist the body of the last polling operation is used
  as the final result. ([#32815](https://github.com/Azure/azure-sdk-for-java/pull/32815))
- Fixed a bug where `HEAD`-based requests were checking for a body before deserializing. `HEAD` requests shouldn't have
  a body or if they do it should be ignored. ([#32833](https://github.com/Azure/azure-sdk-for-java/pull/32833))

### Other Changes

- Exceptions when deserializing error HTTP responses now include the deserialization exception as the causal exception
  in addition to logging it.
- `ExpandableStringEnum` now uses `MethodHandle` instead of `Constructor` to create subtype instances when using
  `fromString(String, Class<T>)`.

## 1.35.0 (2023-01-05)

### Features Added

- Added corresponding `HttpHeaderName` APIs to `HttpRequest` and `HttpResponse`.
- Enhanced exception based retrying by inspecting the causal exceptions in addition to the thrown exception.

### Bugs Fixed

- Fixed a bug where cancellation would result in an application stall by using `doFinally` instead of `doOnTermination` ([#32727](https://github.com/Azure/azure-sdk-for-java/pull/32727)).

### Other Changes

- Added more details to key exception messages.

### Dependency Updates

- Upgraded Reactor Core from `3.4.23` to `3.4.26`.

## 1.34.0 (2022-11-04)

### Features Added

- Added `HttpHeaderName`, and corresponding methods on `HttpHeaders`, which provides a way of adding, accessing, and 
  removing `HttpHeader`s from `HttpHeaders` without needing to call `String.toLowercase`. ([#30924](https://github.com/Azure/azure-sdk-for-java/pull/30924))
- Added `SyncPollingStrategy`, and implementations of it, to compliment the asynchronous `PollingStrategy`. ([#31923](https://github.com/Azure/azure-sdk-for-java/pull/31923))
- Added a new factory method on `SyncPoller` matching the factory method on `PollerFlux`, except taking `SyncPollingStrategy`
  instead of `PollingStrategy`.

### Bugs Fixed

- Fixed a bug where `void` and `Void` responses would attempt to create a `byte[]` the size of the response 
  `Content-Length`. ([#31865](https://github.com/Azure/azure-sdk-for-java/pull/31865))
- Fixed a bug where `SyncPoller` `waitUntil` or `waitForCompletion` didn't update the terminal poll context correctly. ([#31905](https://github.com/Azure/azure-sdk-for-java/pull/31905))

### Other Changes

- Removed size limit when creating a `BinaryData.fromFlux` when the `Flux<ByteBuffer>` is buffered.
- Deprecated empty argument constructor in `ExpandableStringEnum` subtypes.
- Miscellaneous performance improvements.

#### Dependency Updates

- Upgraded Jackson from `2.13.4` to `2.13.4.2`.

## 1.33.0 (2022-10-07)

### Features Added

- Added configuration options to specify which `HttpClient` implementation to use from the classpath when using 
  `HttpClient.createDefault(HttpClientOptions)`. ([#30894](https://github.com/Azure/azure-sdk-for-java/pull/30894))
- Added `BinaryData.fromByteBuffer(ByteBuffer)`.
- Added `SyncPoller.createPoller(Duration, Function, Function, BiFunction, Function)`. ([#31296](https://github.com/Azure/azure-sdk-for-java/pull/31296))
- Added `TokenCredential.getTokenSync(TokenRequestContext)`. ([#31056](https://github.com/Azure/azure-sdk-for-java/pull/31056))

### Bugs Fixed

- Added a short delay to `AccessTokenCache.getToken()` to avoid an async-busy-loop when the first thread to retrieve a fresh token takes longer than usual and the cache is shared amongst many threads. ([#31110](https://github.com/Azure/azure-sdk-for-java/pull/31110))
- Fixed issue when deserializing InputStream from an HTTP response.

### Other Changes

- Defer creation of `XmlMapper` allowing for non-XML applications to exclude `jackson-dataformat-xml` dependency. ([#30663](https://github.com/Azure/azure-sdk-for-java/pull/30663))
- Miscellaneous performance improvements.

#### Dependency Updates

- Upgraded Jackson from `2.13.3` to `2.13.4`.
- Upgraded Reactor from `3.4.22` to `3.4.23`.

## 1.32.0 (2022-09-01)

### Features Added

- Added new constructor overloads to `PagedIterable` and introduced `PageRetrieverSync`.
- Added `com.azure.core.util.metrics.LongGauge` instrument support to metrics.
- Added `CoreUtils.stringJoin` which optimizes `String.join` for small `List`s.

### Other Changes

- Miscellaneous performance improvements.

#### Dependency Updates

- Upgraded Reactor from `3.4.21` to `3.4.22`.

## 1.31.0 (2022-08-05)

### Features Added

- Added support for relative paths returned by polling operations. ([#29676](https://github.com/Azure/azure-sdk-for-java/pull/29676))
- Added the ability to transfer the body of an `HttpResponse` to an `AsynchronousByteChannel` or `WriteableByteChannel`.
- Added `AZURE_CLIENT_CERTIFICATE_PASSWORD` property to `Configuration`.
- Added `AZURE_METRICS_DISABLED` property to `Configuration`.

### Bugs Fixed

- Fixed bug where `RestProxy` could leak connection if service method returned `Void>` or `void`. ([#30072](https://github.com/Azure/azure-sdk-for-java/pull/30072))
- Fixed bug where query parameters with Base64 encoded values with trailing `=`s would be stripped. ([#30164](https://github.com/Azure/azure-sdk-for-java/pull/30164))

### Other Changes

- Added additional information to log messages and exceptions when requests are retried.
- Removed requirement for `Multi-Release: true` to be included in a manifest when creating an all-in-one JAR including `azure-core`.
- Updated log messages to mention when there is a fallback being used.
- Miscellaneous performance improvements.

#### Dependency Updates

- Upgraded Reactor from `3.4.19` to `3.4.21`.

## 1.30.0 (2022-06-30)

### Features Added

- Added `BinaryData.isReplayable()` to indicate if multiple consumptions of the content are safe.
- Added `BinaryData.toReplayableBinaryData` and `BinaryData.toReplayableBinaryDataAsync` to allow
  transforming `BinaryData` instances into replayable `BinaryData` for all content types.
- Added support for sending synchronous requests using `sendSync` in `HttpPipeline`:
  - Added `HttpPipelinePolicy.processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next)` to allow processing policies synchronously.
  - Added `HttpPipelineSyncPolicy` to represent synchronous `HttpPipelinePolicy`.
  - Added `HttpPipelineNextSyncPolicy` to invoke the next synchronous policy in pipeline. to process synchronous policy pipeline.
  - Added `HttpPipelineCallState` to maintain request specific pipeline and contextual data.
- Added `ProgressReporter` and `ProgressListener` to provide ability to track progress of I/O operations.
- Added `Contexts` utility to manipulate known cross-cutting key-value pairs.
  - Added ability to get and set `ProgressReporter` on `Context`.
- Added `HttpPipelineCallContext.getContext()`.
- Added `com.azure.core.util.metrics` package and metrics abstractions (intended for client libraries):
  `MeterProvider`, `Meter`, `LongCounter` and `DoubleHistogram`.

### Bugs Fixed

- Fixed bug where `BinaryData.fromFile(path).toFluxByteBuffer()` and `BinaryData.fromFile(path).toBytes()`
  could block file deletion on Windows.
- Fixed bug where `Context.getData("key")` throws if the `null` value has been set by calling `Context.addData("key", null)`.

### Other Changes

#### Dependency Updates

- Upgraded Reactor from `3.4.17` to `3.4.19`.
- Upgraded Jackson from `2.13.2.2` to `2.13.3`.

## 1.29.1 (2022-06-03)

### Other changes
- Revert module-info version to Java 11

## 1.29.0 (2022-06-03)

### Features Added

- Added support for `BinaryData` in `HttpRequest`:
  - Added `HttpRequest(HttpMethod, URL, HttpHeaders)` and `HttpRequest(HttpMethod, URL, HttpHeaders, BinaryData)` constructors.
  - Added `HttpRequest.getBodyAsBinaryData()`.
  - Added `HttpRequest.setBody(BinaryData)`.
  - Added `BinaryData.fromFlux(Flux<ByteBuffer>, Long, boolean)` that allows both buffered and non-buffered handling of `Flux<ByteBuffer>`.
- Added `BinaryData.fromFile(Path file, Long position, Long length)` and `BinaryData.fromFile(Path file, Long position, Long length, int chunkSize)`
  that represents slice of the file.

## 1.28.0 (2022-05-06)

### Features Added

- Add `com.azure.core.models.MessageContent`.
- Added support for custom configuration sources and rich configuration properties:
  - `ConfigurationSource` supplies properties from the give source
  - `ConfigurationBuilder` allows to build immutable `Configuration` per-client instances with shared properties sections.
  - `ConfigurationProperty<T>` describes how configuration property is retrieved. `ConfigurationPropertyBuilder` allows
    to conveniently build properties.
  - `Configuration.get(ConfigurationProperty<T>)` allows to retrieve new properties and 
    `Configuration.contains(ConfigurationProperty<T>)` checks if 

### Breaking Changes

- Deprecated `Configuration.put`, `Configuration.remove`, `Configuration.clone`, and default `Configuration` constructor.
  Use `ConfigurationBuilder` to build immutable configuration using `ConfigurationSource`.
- Moved Netty TC Native dependency to `azure-core-http-netty`.

### Other Changes

#### Dependency Updates

- Upgraded Reactor from `3.4.14` to `3.4.17`.
- Upgraded Jackson from `2.13.2.1` to `2.13.2.2`.

## 1.27.0 (2022-04-01)

### Features Added

- Added support for strongly-typed HTTP header objects to be deserialized lazily on a per-field basis rather than all
  at once during construction.
- Added `Context` support for `DefaultPollingStrategy`, `OperationResourcPollingStrategy` and `LocationPollingStrategy`.  

### Other Changes

- Reduced usage of reflection when sending requests and receiving responses in `RestProxy`.
- Improved handling for catching and rethrowing exceptions to reduce wrapping exceptions and to not wrap `Error`s.

#### Dependency Updates

- Upgraded Jackson from `2.13.2` to `2.13.2.1`.

## 1.26.0 (2022-03-04)

### Features Added

- Added `FluxUtil.writeToOutputStream` which provides an optimized way to write a stream of `Flux<ByteBuffer>` to an
  `OutputStream` with minimal overhead. ([#26821](https://github.com/Azure/azure-sdk-for-java/pull/26821))

### Bugs Fixed

- Fixed `com.azure.core.implementation.ReflectionUtils.getLookupToUse` which fails with `java.lang.SecurityException` 
  under `SecurityManager`. ([#27182](https://github.com/Azure/azure-sdk-for-java/pull/27182), thank you @reta!)
- Fixed an issue where converting Azure `Context` to Reactor `Context` could result in an `IndexOutOfBoundsException`. ([#27197](https://github.com/Azure/azure-sdk-for-java/pull/27197))

### Other Changes

- Added `x-ms-request-id`, `MS-CV`, `WWW-Authenticate` as default logged headers and `api-version` as a default logged
  query parameter. ([#26973](https://github.com/Azure/azure-sdk-for-java/pull/26973))
- Updated how `Response` types are constructed in `RestProxy` to reduce the usage of reflection. ([#27207](https://github.com/Azure/azure-sdk-for-java/pull/27207))
- Updated all `ClientLogger`s to be static constants instead of instance variables. ([#27339](https://github.com/Azure/azure-sdk-for-java/pull/27339))
- Updated the usage of `AZURE_LOG_LEVEL` to be constant. ([#27193](https://github.com/Azure/azure-sdk-for-java/pull/27193))

#### Dependency Updates

- Upgraded Reactor from `3.4.13` to `3.4.14`.

## 1.25.0 (2022-02-04)

### Features Added

- Added `AzureKeyCredentialTrait`, `AzureNamedKeyCredentialTrait`, `AzureSasCredentialTrait`, `ConfigurationTrait`,
  `ConnectionStringTrait`, `EndpointTrait`, `HttpTrait`, and `TokenCredentialTrait` interfaces that represent common 
  cross-cutting aspects of functionality offered by libraries in the Azure SDK for Java.
- Added a static method `toRfc1123String` which converts an `OffsetDateTime` to an RFC1123 datetime string.

## 1.24.1 (2022-01-11)

### Other Changes

#### Dependency Updates

- Upgraded Reactor from `3.4.12` to `3.4.13`.

## 1.24.0 (2022-01-06)

### Features Added

- Added `ClientLogger` APIs (`atError`, `atWarning`, `atInfo`, `atVerbose`) that allow adding key-value pairs to log 
  entries and `ClientLogger` constructor overloads that take context to apply to every log entry written with this logger 
  instance. Logger writes entries that have context as JSON similar to `{"az.sdk.message":"on delivery","connectionId":"foo"}`

### Bugs Fixed

- Fixed a bug where the wrong full class name was being used in reflections. ([#25840](https://github.com/Azure/azure-sdk-for-java/pull/25840))
- Fixed a bug where flattened deserialization wouldn't find the correct JSON node. ([#25164](https://github.com/Azure/azure-sdk-for-java/pull/25621))
- Changed how non-proxy hosts was being handled as a regex. ([#25841](https://github.com/Azure/azure-sdk-for-java/pull/25841))
- Fixed a bug where an errant log message would happen when using a newer version of Jackson. ([#26129](https://github.com/Azure/azure-sdk-for-java/pull/26129))
- Fixed a bug where `PagedIterable` wouldn't terminate the same as `PagedFlux`. ([#26139](https://github.com/Azure/azure-sdk-for-java/pull/26139))
- Fixed a bug where `MethodHandle.Lookup` retrieval didn't handle the unnamed module properly. ([#26268](https://github.com/Azure/azure-sdk-for-java/pull/26268))

### Other Changes

- Improved performance of logging.

#### Dependency Updates

- Upgraded Jackson from `2.13.0` to `2.13.1`.

## 1.23.1 (2021-12-07)

### Other Changes

#### Dependency Updates

- Upgraded Jackson from `2.12.5` to `2.13.0`.
- Upgraded Reactor from `3.4.10` to `3.4.12`.

## 1.23.0 (2021-11-22)

### Breaking Changes
- Removed `ErrorOptions`
- Removed `setErrorOptions()` from `RequestOptions`

## 1.22.0 (2021-11-05)

### Features Added

- Added `ReferenceManager` which is capable of performing `Cleaner`-like functionality by allowing a `Runnable` callback
  to be triggered when an object reference is eligible for garbage collection.
- Added `RequestOptions` which allows for a chained set of operations to be applied to an `HttpRequest` before being
  sent through the `HttpPipeline`.
- Added an `ETag` class which represents an HTTP ETag.
- Added `getJavaClass` method to retrieve the representing instance of the `TypeReference` created.
- Added support for HTTP method OPTIONS by adding an `Options` annotation.
- Added a function to `CoreUtils` which merges two `Context`s together.
- Added a new feature flag `AZURE_JACKSON_ADAPTER_USE_ACCESS_HELPER` which indicates to `JacksonAdapter` to wrap 
  serialization calls in `AccessController.doPrivileged` to prevent `SecurityManager` exceptions when `JacksonAdapter`
  has the prerequisite permissions.

### Bugs Fixed

- Fixed a bug where an initial length of 0 wasn't permitted when creating a `ByteBuffer` collector.
- Fixed a bug where an exception type would be instantiated and never used in a hot path, reducing memory usage.
- Fixed a bug where the content length of a serializable request body may return null when it is known (already serialized).

### Other Changes

- Improved performance of operations that merge or retrieve all values of `Context`.

## 1.22.0-beta.1 (2021-10-12)

### Features Added

- Added a new way to create a `PollerFlux` from a `PollingStrategy`, including known strategies to poll Azure resources. ([#22795](https://github.com/Azure/azure-sdk-for-java/pull/22795))

### Other Changes

- Fixed a bug where `BinaryData.getLength` returns `null` when it should return valid length.

## 1.21.0 (2021-10-01)

### Features Added

- Added `ResponseError` which represents a general error response.
- Added `HttpResponse.getBodyAsInputStream` to retrieve the `HttpResponse` body as an `InputStream`.
- Added `HttpHeaders.add` to add an individual header to the `HttpHeaders`.
- Added `setTenantId` and `getTenantId` methods to `TokenRequestContext` class that allows to configure TenantId Challenges.
- Added additional logging when an `HttpClientProvider` is loaded from the classpath.

### Breaking Changes

- Deprecated annotation `ResumeOperation` as it was no longer used.
- Deprecated `JacksonAdapter.simpleMapper` and `JacksonAdapter.serializer` as they should no longer be used.
- Deprecated `CoreUtils.extractAndFetch` as it was no longer used.

### Other Changes

#### Dependency Updates

- Upgraded Jackson from `2.12.4` to `2.12.5`.
- Upgraded Reactor from `3.4.9` to `3.4.10`.


## 1.21.0-beta.1 (2021-09-08)

### Features Added

- Added a new way to create a `PollerFlux` from a `PollingStrategy`, including known strategies to poll Azure resources. ([#22795](https://github.com/Azure/azure-sdk-for-java/pull/22795))

## 1.20.0 (2021-09-07)

### Features Added

- Added new deferred logging APIs to `ClientLogger`. ([#20714](https://github.com/Azure/azure-sdk-for-java/pull/20714)) (Thank you, @tozsvath)
- Added `HttpAuthorization` which supports configuring a generic `Authorization` header on a request. ([#23633](https://github.com/Azure/azure-sdk-for-java/pull/23633))
- Added `RedirectPolicy` to standardize the ability to redirect HTTP requests. ([#23617](https://github.com/Azure/azure-sdk-for-java/pull/23617))
- Added support for additional environment configurations. ([#23435](https://github.com/Azure/azure-sdk-for-java/pull/23435))
- Added `RetryStrategy.shouldRetryException(Throwable throwable)` to allow `RetryStrategy`s to determine which exceptions
  are acceptable to be retried. ([#23472](https://github.com/Azure/azure-sdk-for-java/pull/23472))
- Updated `RetryPolicy` to attempt to lookup well-known retry after headers (`Retry-After`, `retry-after-ms`, and `x-ms-retry-after-ms`)
  when a lookup header isn't supplied. ([#23472](https://github.com/Azure/azure-sdk-for-java/pull/23472))

### Fixed

- Fixed a bug where terminal status on initial poll wasn't respected. ([#23564](https://github.com/Azure/azure-sdk-for-java/pull/23564))
- Fixed a bug where `UserAgentUtil` didn't validate that an `applicationId` was less than 24 characters. ([#23643](https://github.com/Azure/azure-sdk-for-java/pull/23643))

### Other Changes

#### Dependency Updates

- Upgraded Reactor from `3.4.8` to `3.4.9`.

## 1.19.0 (2021-08-06)

### Features Added

- Added `HttpRequestLogger`, `HttpResponseLogger`, and correlating context classes to enable support for custom
  logging in `HttpLoggingPolicy`. ([#16088](https://github.com/Azure/azure-sdk-for-java/pull/16088))
- Added new constructor overload to `AzureSasCredential` to enable passing a `Function` which encodes the SAS
  credential. ([#23033](https://github.com/Azure/azure-sdk-for-java/pull/23033))
- Added a new constructor `HttpHeaders(int initialCapacity)` which allows configuration of the initial backing map 
  capacity may allow short-circuiting scenarios where the map would need to be resized and copied in memory.
- Added Maven profiles to enable the creation of an uber JAR with OS specific dependencies of
  `netty-tcnative-boringssl-static` instead of including all OS dependencies. ([#21223](https://github.com/Azure/azure-sdk-for-java/pull/21223))
- Added support to `@QueryParam` to support "exploded" query parameters. ([#21203](https://github.com/Azure/azure-sdk-for-java/pull/21203))
- Added support to create tracing spans with customizations. ([#23159](https://github.com/Azure/azure-sdk-for-java/pull/23159))

### Fixed

- Fixed a bug where `Tracer.DIAGNOSTIC_ID_KEY`'s value was spelt incorrect.
- Fixed a bug where loading system and Java proxies used `java.net.useSystemProxies` incorrectly. ([#23151](https://github.com/Azure/azure-sdk-for-java/pull/23151))
- Fixed a bug with context propagation through EventHub and ServiceBus between Java and other languages.
- Fixed a bug where classes annotated with `@JsonFlatten` would incorrectly split `Map` keys on `.` and flatten them. ([#22591](https://github.com/Azure/azure-sdk-for-java/pull/22591))
- Fixed a bug where creating a `ClientLogger` would throw `InvalidPathException` when constructing a `DefaultLogger`
  with a name that contained illegal path characters on Windows.
- Fixed a bug where `FluxUtil.writeFile` would attempt to write to `ByteBuffer`s to the same location in file.

### Dependency Updates

- Upgraded Jackson from `2.12.3` to `2.12.4`.
- Upgraded Reactor from `3.4.6` to `3.4.8`.
- Upgraded SLF4J from `1.7.30` to `1.7.32`.

## 1.19.0-beta.1 (2021-07-07)

### Features Added

- Added `RequestOptions` for protocol methods
- Added support for `BinaryData` type as the request body or response body in `RestProxy`

## 1.18.0 (2021-07-01)

### Features Added

- Added additional configurations of `maximumConnectionPoolSize` and `connectionIdleTimeout` to `HttpClientOptions`.
- Added new `addEvent` overload to `Tracer`. 
- Added new constants to `Configuration`.

### Fixed

- Fixed a bug where a negative delay could be used when retrying a request with a delay.
- Fixed a bug where `JsonFlatten` on a property didn't flatten properties annotated with `JsonFlatten`.
- Fixed error messages that didn't properly format format-able message strings.

## 1.17.0 (2021-06-07)

### Features Added

- Added `AsyncCloseable` interface to support closing resources asynchronously.
- Added GeoJSON classes to the models package.
- Added `createRetriableDownloadFlux` to `FluxUtil`.
- Added `HttpRange` to the http package.
- Added the ability to terminate paging using a custom predicate in `ContinuablePagedFlux`.
- Added `getPollInterval` to `PollerFlux`.
- Added `setResponseTimeout` and `setReadTimeout` to `HttpClientOptions`.
- Added support for the `JsonFlatten` annotation to target fields.

### Dependency Updates

- Upgraded Jackson from `2.12.2` to `2.12.3`.
- Upgraded Reactor from `3.4.5` to `3.4.6`.

## 1.16.0 (2021-05-07)

### Features Added

- Added Support for Challenge Based Authentication in `BearerTokenAuthenticationPolicy`.

### Key Bugs Fixed

- Updated logic to eagerly read response bodies to include return types `void` and `Void`. ([#21091](https://github.com/Azure/azure-sdk-for-java/issues/21091))
- Updated URL path appending logic to prevent double slashes (`//`) from occurring. ([#21138](https://github.com/Azure/azure-sdk-for-java/issues/21138))

### Fixed

- Updated `ServiceLoader`s to use the class loader that loaded the class instead of system class loader. (Thank you @ueisele)
- Changed an instance `Map` to static `Map` for resources that are static for the lifetime of an application.

### Dependency Updates

- Upgraded Reactor from `3.4.3` to `3.4.5`.

## 1.15.0 (2021-04-02)

### New Features

- Added `Binary.toByteBuffer` which returns a read-only view of the `BinaryData`.
- Added `ProxyOptions.fromConfiguration(Configuration, boolean)` which allows for configuring if the returned proxy
  is resolved.
- Added a default `JsonSerializer` implementation which is optionally used when creating a `JsonSerializer` with
  `JsonSerializerProviders` by passing the flag `useDefaultIfAbset`.
- Added the ability to configure HTTP logging level without making code changes by configuring environment property
  `AZURE_HTTP_LOG_DETAIL_LEVEL`.
- Added constructor overloads to `PagedFlux` which allows for the paging implements to consume the `byPage` page size value.
- Added `AzureNamedKey` and `AzureNamedKeyCredential` to support authentication using a named key.
- Added overloads to `SerializerAdapter` which use `byte[]` instead of `String` or `InputStream`/`OutputStream`.

### Bug Fixes

- Fixed a bug where Unix timestamps were not being properly deserialized to `OffsetDateTime`.
- Fixed edge cases where response bodies would be eagerly read into a `byte[]` when they shouldn't.

### Dependency Updates

- Upgraded Jackson from `2.12.1` to `2.12.2`.
- Upgraded Netty from `4.1.59.Final` to `4.1.60.Final`.

## 1.14.1 (2021-03-19)

### Bug Fixes

- Fix a bug where `ClassNotFoundException` or `MethodNotFoundException` was thrown when Jackson 2.11 is resolved
  instead of Jackson 2.12. [#19897](https://github.com/Azure/azure-sdk-for-java/issues/19897)

## 1.14.0 (2021-03-08)

### New Features

- Added `Class<T>` overloads of `BinaryData.toObject` and `BinaryData.toObjectAsync`.
- Added defaulted interface API `Tracer.addEvent`.
- Added `FluxUtil.collectBytesInByteBufferStream(Flux, int)` and `FluxUtil.collectBytesFromNetworkResponse(Flux, HttpHeaders)`
  to allow for performance optimizations when the resulting `byte[]` size in known.
- Added handling to collect a `Flux<ByteBuffer>` into a `byte[]` with less array duplications.
- Added default interface API overloads to `ObjectSerializer` which take or return `byte[]` instead of `InputStream` or
  `OutputStream` allowing for performance optimizations by removing array copies.
- Added default interface API `SerializerAdapter.serializeIterable` which handles serializing generic collections.
- Added `CloudEvent` model which conforms to the [Cloud Event Specification](https://github.com/cloudevents/spec/blob/v1.0.1/spec.md).

### Dependency Updates

- Upgraded Jackson from `2.11.3` to `2.12.1`.
- Upgraded Netty from `4.1.54.Final` to `4.1.59.Final`.
- Upgraded Reactor from `3.3.12.RELEASE` to `3.4.3`.
- Upgraded Reactor Netty from `0.9.15.RELEASE` to `1.0.4`.

## 1.13.0 (2021-02-05)

### New Features

- Added `setPollInterval` to `PollerFlux` and `SyncPoller` to allow mutating how often a long-running request is polled.
- Added `HttpClientOptions` to allow for reusable `HttpClient` configurations to be passed into SPIs and client builders.
- Added `CoreUtils.getApplicationId` as a convenience method to determine application ID from `ClientOptions` or `HttpLogOptions`.
- Added additional convenience methods to `HttpHeaders` and `HttpHeader` to better support multi-value headers.
- Added support for claims in `TokenRequestContext`.
- Added the ability to disable tracing for individual network requests.

### Deprecations

- Deprecated `HttpHeaders.put` and replaced with `HttpHeaders.set`.

## 1.12.0 (2021-01-11)

### New Features

- Added `AzureSasCredential` and `AzureSasCredentialPolicy` to standardize the ability to add SAS tokens to HTTP requests.

### Bug Fixes

- Fixed a bug where environment proxy configurations were not sanitizing the non-proxy host string into a valid `Pattern` format. [#18156](https://github.com/Azure/azure-sdk-for-java/issues/18156)

### Dependency Updates

- Updated `reactor-core` from `3.3.11.RELEASE` to `3.3.12.RELEASE`.
- Updated `netty-tcnative-boringssl-static` from `2.0.34.Final` to `2.0.35.Final`.

## 1.11.0 (2020-11-24)

### New Features

- Added `BinaryData` which allows for a format agnostic representation of binary information and supports
 `ObjectSerializer` for serialization and deserialization.
- Added functionality to eagerly read HTTP response bodies into memory when they will be deserialized into a POJO.

## 1.10.0 (2020-10-29)

### New Features

- Added `JsonPatchDocument` to support `json-patch` functionality.
- Added new Identity `Configuration` properties.

### Bug Fixes

- Modified `ContinuablePagedFlux` implementation to prevent `OutOfMemoryError` when retrieving many pages. [#12453](https://github.com/Azure/azure-sdk-for-java/issues/12453)
- Fixed a bug where request retrying didn't consume the network response potentially leading to resource leaking.

## 1.9.0 (2020-10-01)

### New Features

- Added `ServiceClientProtocol` to allow the client to indicate which networking protocol it will use.
- Added `HttpPipelinePosition` which allows `HttpPipelinePolicy`s to indicate their position when used in a client builder.
- Added default interface method `HttpPipelinePolicy.getPipelinePosition` that returns `HttpPipelinePosition.PER_RETRY`.

### Bug Fixes

- Fixed a bug where calling `UrlBuilder.parse` could result in an exception. [#15013](https://github.com/Azure/azure-sdk-for-java/issues/15013)
- Changed `ContinuablePagedIterable` implementation to use a custom iterable to prevent additional, unrequested pages from being retrieved. [#15575](https://github.com/Azure/azure-sdk-for-java/issues/15575)

## 1.8.1 (2020-09-08)

- Fixed a bug where some `HttpRequests` would have their body consumed before being sent resulting in an exception being thrown.

## 1.8.0 (2020-09-03)

- General performance fixes for serialization, URL modification and parsing, and more.
- New `InputStream` and `OutputStream` APIs for serialization and deserialization.
- Added logging for the request attempt count to better correlate when requests are retried.
- Improved request and response body logging performance by using bulk `ByteBuffer` reading instead of byte by byte reading.
- Fixed a bug where header logging checked for a log level of not equals `verbose` instead of equals `verbose`.
- Updated `reactor-core` version to `3.3.9.RELEASE`.
- Updated FasterXML Jackson versions to `2.11.2`.

## 1.7.0 (2020-08-07)

- Updated `reactor-core` version to `3.3.8.RELEASE`.
- Updated handling of `OffsetDateTime` serialization to implicitly convert date strings missing time zone into UTC.
- Updated `PollerFlux` and `SyncPoller` to propagate exceptions when polling instead of only on failed statuses.
- Redesigned `SimpleTokenCache` to gracefully attempt a token refresh 5 minutes before actual expiry
- Added `ObjectSerializer` and `JsonSerializer` APIs to support pluggable serialization within SDKs.
- Added `TypeReference<T>` to enable serialization handling for `Class<T>` and `Type` while retaining generics through a call stack.
- Added `MemberNameConverter` which converts a `Member` type of `Field` or `Method` into its expected serialized JSON property name.

## 1.7.0-beta.2 (2020-07-23)

- Removed `tokenRefreshOptions()` from `TokenCredential`, defaulting token refresh offset to 5 minutes, and a default token refresh retry timeout of 30 seconds.

## 1.7.0-beta.1 (2020-07-08)

- Added `TokenRefreshOptions()` to `TokenCredential`, with a default token refresh offset of 2 minutes, and a default token refresh retry timeout of 30 seconds.

## 1.6.0 (2020-07-02)

- Added utility class `UserAgentUtil` which constructs `User-Agent` headers following SDK guidelines.
- Modified Azure Context to Reactor Context to remove intermediate Map container.

## 1.5.1 (2020-06-08)

- Added handling for more complex `Content-Type` headers such as `text/custom+xml`.

## 1.5.0 (2020-05-04)

- Fixed issue where `FluxUtil.toReactorContext` would include `null` values which aren't allowed in Reactor's `Context`.
- Added `CoreUtils.bomAwareToString` that handles converting a `byte[]` to a String based on a leading byte order mark or using the passed `Content-Type`.
- Updated percent encoding logic to properly handle `UTF-8` characters.
- Added new constructors for `AzureException`, `HttpRequestException`, and `HttpResponseException`.
- Deprecated `ClientLogger.logThowableAsWarning`, replaced with `ClientLogger.logThrowableAsWarning`.
- Added utility method `FluxUtil.toFluxByteBuffer` which converts an `InputStream` into `Flux<ByteBuffer>`.
- Updated Reactor Core dependency.
- Added support for serialization and deserialization of discriminator types.

## 1.4.0 (2020-04-03)

- Added `AzureKeyCredential` and `AzureKeyCredentialPolicy` to support generic key based authorizations.
- Fixed a deserialization issue when a JSON property contained a `.` and the containing class was annotated with `JsonFlatten`.
- Updated `reactor-core` dependency to `3.3.3.RELEASE`.
- Added APIs to `ClientLogger` to log checked exceptions that will be thrown.
- Added simplified APIs to `ClientLogger` where only a message will be logged.
- Fixed URL encoded form request issue where the URL would be encoded improperly.
- Added property to `HttpLogOptions` to enable pretty printing when logging a request or response body.
- Added another `withContext` overload in `FluxUtil`.
- Added additional constants to `Configuration`.

## 1.3.0 (2020-03-06)

- Enhanced and extended 'PagedIterable' implementation to cover additional use cases.
- Added additional constants to 'Tracer'.
- Added a factory method to create 'PollerFlux' that can avoid unnecessary poll if the LRO completed synchronously.
- Fixed race condition when loading 'BeforeRetryPolicyProvider' and 'AfterRetryPolicyProvider' implementations with 'ServiceLoader'.
- Fixed race condition when loading 'Tracer' implementations with 'ServiceLoader'.
- Fixed XML deserialization issue when byte order mark wasn't properly handled.

## 1.3.0-beta.1 (2020-02-11)

- Added default logging implementation for SLF4J.
- Modified checks to determine if logging is allowed.
- Improved logging performance.
- Enhanced and extended PagedFlux implementation to cover additional use cases.
- Enabled loading proxy configuration from the environment.
- Added support for Digest proxy authentication.
- Updated 'BufferedResponse' to deep copy the response to handle scenarios where the underlying stream is reclaimed.

## 1.2.0 (2020-01-07)

- Ignore null headers and allow full url paths
- Add missing HTTP request methods to HttpMethod enum
- Support custom header with AddHeaderPolicy
- Support custom header name in RequestIDPolicy
- Prevent HttpLoggingPolicy Consuming Body
- Hide secret info from log info
- Ensure HTTPS is used when authenticating with tokens
- Reduce Prefetch Limit for PagedIterable and IterableStream
- Add Iterable<T> overload for IterableStream<T>

## 1.1.0 (2019-11-26)

- Added support for creating reactor-netty-http client from an existing client.
- Added UserAgent helper methods for fetching client name and version from pom file.
- Added toReactorContext to FluxUtil.
- Logging exception at warning level, and append stack trace if log level is verbose.
- Fixed HttpLoggingPolicy to take null HttpLogOptions.
- Changed the User agent format.
- Hide the secrets from evnironment variable.
- UserAgentPolicy is using the value stored in the policy no matter what is stored in the passed request. Also, removed the service version from User agent format.
- Added Iterable<T> overload for IterableStream<T>.
- Reduce Prefetch Limit for PagedIterable and IterableStream.
- Ensure HTTPS is used when authenticating with tokens.

## 1.0.0 (2019-10-29)

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core/src/samples/java/com/azure/core)

- Initial release. Please see the README and wiki for information on the new design.
