# Release History

## 1.19.0 (2021-08-06)

### Feature Added

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
