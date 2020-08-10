# Release History

## 1.8.0-beta.1 (Unreleased)


## 1.7.0 (2020-08-07)

- Updated `reactor-core` version to `3.3.8.RELEASE`.
- Updated `reactor-netty` version to `0.9.10.RELEASE`.
- Updated `netty` version to `4.1.51.Final`.
- Updated `netty-tcnative` version to `2.0.31.Final`.
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
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core/src/samples/java/com/azure/core)

- Initial release. Please see the README and wiki for information on the new design.
