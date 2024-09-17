# Release History

## 1.16.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.15.4 (2024-09-12)

### Bugs Fixed

- Fixed a bug where logging the Netty versions could throw an exception when `SecurityManager` is present. ([#41484](https://github.com/Azure/azure-sdk-for-java/pull/41484))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to `1.52.0`.

## 1.15.3 (2024-07-31)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.50.0` to `1.51.0`.

## 1.15.2 (2024-07-12)

### Other Changes

#### Dependency Updates

- Upgraded Netty version logging (#40733)[https://github.com/Azure/azure-sdk-for-java/pull/40733]

## 1.15.1 (2024-06-06)

### Other Changes

- Changed the log message for mismatched Netty versions to not state an expected version, instead just that versions
  weren't aligned. ([#40134](https://github.com/Azure/azure-sdk-for-java/pull/40134))

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to `1.49.1`.
- Upgraded `reactor-netty-http` from `1.0.43` to `1.0.45`.
- Upgraded Netty dependencies from `4.1.108.Final` to `4.1.110.Final`.

## 1.15.0 (2024-05-01)

### Bugs Fixed

- Fixed a bug where mismatch Netty versions were always being reported, even if they were correct. ([#39591](https://github.com/Azure/azure-sdk-for-java/pull/39591))

### Other Changes

- If a Reactor Netty `HttpClient` is passed and has a `LoggingHandler` configured the 
  `NettyAsyncHttpClientBuilder.wiretap` value is ignored, the builder method is now deprecated as well. ([#39976](https://github.com/Azure/azure-sdk-for-java/pull/39976))

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to `1.49.0`.

## 1.14.2 (2024-04-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to `1.48.0`.
- Upgraded `reactor-netty-http` from `1.0.40` to `1.0.43`.
- Upgraded Netty dependencies from `4.1.101.Final` to `4.1.108.Final`.
- Upgraded Netty TcNative dependencies from `2.0.62.Final` to `2.0.65.Final`.

## 1.14.1 (2024-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to `1.47.0`.

## 1.14.0 (2024-02-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to `1.46.0`.
- Upgraded `reactor-netty-http` from `1.0.39` to `1.0.40`.

## 1.13.10 (2023-11-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.

## 1.13.9 (2023-10-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to `1.44.1`.
- Upgraded `netty-handler` from `4.1.94.Final` to `4.1.100.Final`.
- Upgraded `netty-handler-proxy` from `4.1.94.Final` to `4.1.100.Final`.
- Upgraded `netty-buffer` from `4.1.94.Final` to `4.1.100.Final`.
- Upgraded `netty-codec` from `4.1.94.Final` to `4.1.100.Final`.
- Upgraded `netty-codec-http` from `4.1.94.Final` to `4.1.100.Final`.
- Upgraded `netty-codec-http2` from `4.1.94.Final` to `4.1.100.Final`.
- Upgraded `netty-transport-native-unix-common` from `4.1.94.Final` to `4.1.100.Final`.
- Upgraded `netty-common` from `4.1.94.Final` to `4.1.100.Final`.
- Upgraded `reactor-netty-http` from `1.0.34` to `1.0.38`.

## 1.13.8 (2023-10-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to `1.44.0`.

## 1.13.7 (2023-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.43.0`.

## 1.13.6 (2023-08-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to `1.42.0`.

## 1.14.0-beta.1 (2023-07-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to `1.42.0-beta.1`.

## 1.13.5 (2023-07-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 1.13.4 (2023-06-02)

### Bugs Fixed

- Fixed a bug where `NettyAsyncHttpResponse.writeBodyTo` and `writeBodyToAsync` could have a race condition when writing
  to the `WritableByteChannel`/`AsynchronousByteChannel` while tracking the bytes written when a network error happens
  during consumption of the HTTP response body. ([#35004](https://github.com/Azure/azure-sdk-for-java/pull/35004))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to `1.40.0`.

## 1.13.3 (2023-05-04)

### Other Changes

- Changed how timeout handlers are added to the request pipeline.

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to `1.39.0`.

## 1.13.2 (2023-04-07)

### Bugs Fixed

- Fixed a bug where sending an `http` request with an empty body created from `BinaryData.fromFile` would throw an error. ([#34294](https://github.com/Azure/azure-sdk-for-java/pull/34294))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to `1.38.0`.

## 1.13.1 (2023-03-02)

### Bugs Fixed

- Fixed a bug where IP-style URLs wouldn't have a host part when used as a `URI`. ([#33673](https://github.com/Azure/azure-sdk-for-java/pull/33673))

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to `1.37.0`.
- Upgraded Netty from `4.1.87.Final` to `4.1.89.Final`.
- Upgraded Reactor Netty from `1.0.27` to `1.0.28`.

## 1.13.0 (2023-02-01)

### Features Added

- Added override for `HttpClient.sendSync` in `NettyAsyncHttpClient`. This is done to propagate any exceptions through
  a synchronous stack rather than through Reactor's error stream, allowing for better handling of exceptions that
  wouldn't be thrown until the reactive stream was blocked further up the callstack. ([#32840](https://github.com/Azure/azure-sdk-for-java/pull/32840))

### Other Changes

- Added a log message when `ConnectionProvider` is set to a non-default `ConnectionProvider` to allow for easier debugging
  if `PollAcquirePendingLimitException` is seen. ([#32826](https://github.com/Azure/azure-sdk-for-java/pull/32826))

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to `1.36.0`.
- Upgraded Netty from `4.1.86.Final` to `4.1.87.Final`.
- Upgraded Reactor Netty from `1.0.26` to `1.0.27`.

## 1.12.8 (2023-01-05)

### Bugs Fixed

- Fixed a bug where an exception would be logged during the first attempt to connect to an authenticated
  proxy without using credentials (done to prevent any potential credential leaking). ([#30274](https://github.com/Azure/azure-sdk-for-java/pull/30274))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to `1.35.0`.
- Upgraded Netty from `4.1.82.Final` to `4.1.86.Final`.
- Upgraded Reactor Netty from `1.0.23` to `1.0.26`.

## 1.12.7 (2022-11-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to `1.34.0`.

## 1.12.6 (2022-10-07)

### Bugs Fixed

- Fixed a bug where `HttpClientOptions.connectTimeout` wasn't being passed when using `HttpClientProvider(ClientOptions)`. ([#31079](https://github.com/Azure/azure-sdk-for-java/pull/31079))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to `1.33.0`.
- Upgraded Netty from `4.1.79.Final` to `4.1.82.Final`.
- Upgraded Reactor Netty from `1.0.22` to `1.0.23`.

## 1.12.5 (2022-09-01)

### Bugs Fixed

- Fixed a bug where `HttpResponse.writeBodyTo` could leak `ByteBuf`s. ([#30670](https://github.com/Azure/azure-sdk-for-java/pull/30670))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to `1.32.0`.
- Upgraded Reactor Netty from `1.0.21` to `1.0.22`.

## 1.12.4 (2022-08-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to `1.31.0`.
- Upgraded Netty from `4.1.78.Final` to `4.1.79.Final`.
- Upgraded Reactor Netty from `1.0.20` to `1.0.21`.

## 1.12.3 (2022-06-30)

### Features Added

- Added ability to track progress by passing `ProgressReporter` in the `Context`. For example:
  ```java
  HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();
  ProgressReporter progressReporter = ProgressReporter.withProgressListener(progress -> System.out.println(progress));
  Context context = Contexts.empty().setHttpRequestProgressReporter(progressReporter).getContext();
  HttpRequest request = new HttpRequest(
      HttpMethod.PUT, new URL("http://example.com"), new HttpHeaders(), BinaryData.fromString("sample body"))
  httpClient.send(request, context).subscribe();
  ```

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to `1.30.0`.
- Upgraded Netty from `4.1.76.Final` to `4.1.78.Final`.
- Upgraded Reactor Netty from `1.0.18` to `1.0.20`.

## 1.12.2 (2022-06-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.0` to `1.29.1`.

## 1.12.1 (2022-06-03)

### Other Changes

- Added specialized consumption for `HttpRequest.getBodyAsBinaryData()`.

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to `1.29.0`.

## 1.12.0 (2022-05-06)

### Features Added

- The `NettyAsyncHttpClientProvider.createInstance()` now has the option to share a single shared `HttpClient`.
  Set the environment variable `AZURE_ENABLE_HTTP_CLIENT_SHARING` to `true` before starting the process to use
  the shared `HttpClient`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to `1.28.0`.
- Upgraded Netty from `4.1.73.Final` to `4.1.76.Final`.
- Upgraded Reactor Netty from `1.0.15` to `1.0.18`.

## 1.11.9 (2022-04-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to `1.27.0`.

## 1.11.8 (2022-03-04)

### Other Changes

- Updated all `ClientLogger`s to be static constants instead of instance variables. ([#27339](https://github.com/Azure/azure-sdk-for-java/pull/27339))

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to `1.26.0`.
- Upgraded Netty from `4.1.72.Final` to `4.1.73.Final`.
- Upgraded Reactor Netty from `1.0.14` to `1.0.15`.

## 1.11.7 (2022-02-04)

### Bugs Fixed

- Fixed a bug where proxying clients wouldn't use the no-op resolver. ([#26611](https://github.com/Azure/azure-sdk-for-java/pull/26611))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to `1.25.0`.

## 1.11.6 (2022-01-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.0` to `1.24.1`.
- Upgraded Netty from `4.1.70.Final` to `4.1.72.Final`.
- Upgraded Reactor Netty from `1.0.13` to `1.0.14`.

## 1.11.5 (2022-01-06)

### Bugs Fixed
- Set default `maxConnections` value to match the default used in `reactor-netty` when `HttpClientOptions` is set but 
  `maximumConnectionPoolSize` is not specified. ([#26083](https://github.com/Azure/azure-sdk-for-java/pull/26083))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.23.1` to `1.24.0`.

## 1.11.4 (2021-12-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to `1.23.1`.
- Upgraded Netty from `4.1.68.Final` to `4.1.70.Final`.
- Upgraded Reactor Netty from `1.0.11` to `1.0.13`.

## 1.11.3 (2021-11-23)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.22.0` to `1.23.0`.

## 1.11.2 (2021-11-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to `1.22.0`.

## 1.11.1 (2021-10-01)

### Bugs Fixed

- Fixed a bug where `HttpResponse.close` wouldn't drain the response stream if it wasn't already consumed. ([#23855](https://github.com/Azure/azure-sdk-for-java/pull/23855)) 

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.20.0` to `1.21.0`.
- Upgraded Netty from `4.1.67.Final` to `4.1.68.Final`.
- Upgraded Reactor Netty from `1.0.10` to `1.0.11`.
- 
## 1.11.0 (2021-09-07)

### Features Added

- Added the ability to configure HTTP connect timeout. ([#23435](https://github.com/Azure/azure-sdk-for-java/pull/23435))
- Added support for additional environment configurations. ([#23435](https://github.com/Azure/azure-sdk-for-java/pull/23435))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.19.0` to `1.20.0`.
- Upgraded Netty from `4.1.66.Final` to `4.1.67.Final`.
- Upgraded Reactor Netty from `1.0.9` to `1.0.10`.

## 1.10.2 (2021-08-06)

### Features Added

- Added support for setting per-call response timeouts by passing `azure-response-timeout` in the request's `Context`. ([#23244](https://github.com/Azure/azure-sdk-for-java/pull/23244))

### Fixed

- Fixed a bug where `NullPointerException` would be thrown when `HttpClientOptions` was used in `NettyAsyncClientProvider`
  without a connection pool size configured. ([#23357](https://github.com/Azure/azure-sdk-for-java/pull/23357))
- Fixed a bug where the fix `CONNECT` request made after the creation of an `HttpClient` instance would time out. ([#22661](https://github.com/Azure/azure-sdk-for-java/pull/22661))

### Dependency Updates

- Upgraded `azure-core` from `1.18.0` to `1.19.0`.
- Upgraded Netty from `4.1.65.Final` to `4.1.66.Final`.
- Upgraded Reactor Netty from `1.0.8` to `1.0.9`.

## 1.10.1 (2021-07-01)

### Features Added

- Added support for new `HttpClientOptions` configurations.

### Dependency Updates

- Upgraded `azure-core` from `1.17.0` to `1.18.0`.

## 1.10.0 (2021-06-07)

### Dependency Updates

- Upgraded `azure-core` from `1.16.0` to `1.17.0`.
- Upgraded Netty from `4.1.63.Final` to `4.1.65.Final`.
- Upgraded Reactor Netty from `1.0.6` to `1.0.7`.

## 1.9.2 (2021-05-07)

### Fixed

- Fixed a bug where `ProxyConnectException`s weren't eagerly being retried.
- Updated how `ProxyConnectException`s are propagated to include response headers.

### Dependency Updates

- Upgraded `azure-core` from `1.15.0` to `1.16.0`.
- Upgraded Netty from `4.1.60.Final` to `4.1.63.Final`.
- Upgraded Reactor Netty from `1.0.4` to `1.0.6`.

## 1.9.1 (2021-04-02)

### Bug Fixes

- Fixed a bug where a proxy's address is only resolved during construction of the client, now it is resolved per connection. [#19497](https://github.com/Azure/azure-sdk-for-java/issues/19497)
- Fixed a bug where `Proxy Authentication Required` bubbled back up to a `RetryPolicy` leading to more time taken to
  connect to a proxy when authentication information was supplied. [#19415](https://github.com/Azure/azure-sdk-for-java/issues/19415)

### Dependency Updates

- Upgraded `azure-core` from `1.14.0` to `1.15.0`.
- Upgraded Netty from `4.1.59.Final` to `4.1.60.Final`.

## 1.9.0 (2021-03-08)

### Dependency Updates

- Upgraded `azure-core` from `1.13.0` to `1.14.0`.
- Upgraded Netty from `4.1.54.Final` to `4.1.59.Final`.
- Upgraded Reactor Netty from `0.9.15.RELEASE` to `1.0.4`.

## 1.8.0 (2021-02-05)

### New Features

- Exposed service provider interfaces used to create `HttpClient` instances.

### Bug Fixes

- Fixed a bug where authenticated proxies would use different DNS resolution than non-authenticated proxies. [#17930](https://github.com/Azure/azure-sdk-for-java/issues/17930)

## 1.7.1 (2021-01-11)

### Bug Fixes

- Fixed a bug where environment proxy configurations were not sanitizing the non-proxy host string into a valid `Pattern` format. [#18156](https://github.com/Azure/azure-sdk-for-java/issues/18156)

### Dependency Updates

- Upgraded Netty from `4.1.53.Final` to `4.1.54.Final`.
- Upgraded `reactor-netty` from `0.9.13.RELEASE` to `0.9.15.RELEASE`.

## 1.7.0 (2020-11-24)

### New Features

- Added functionality to eagerly read HTTP response bodies into memory when they will be deserialized into a POJO.

### Bug Fixes

- Fixed a bug where a connection would remain active when timed out instead of being closed.

## 1.6.3 (2020-10-29)

### Dependency Updates

- Updated `azure-core` to `1.10.0`.

## 1.6.2 (2020-10-01)

- Updated `azure-core` version.

## 1.6.1 (2020-09-08)

- Updated `azure-core` version to pickup bug fix.

## 1.6.0 (2020-09-03)

- Added new APIs to configure request write timeout, response timeout, and response body read timeout.
- Changed default timeouts from infinite to 60 seconds.
- Updated `reactor-core` version to `3.3.9.RELEASE`.
- Updated `reactor-netty` version to `0.9.11.RELEASE`.

## 1.5.4 (2020-08-07)

- Updated `reactor-core` version to `3.3.8.RELEASE`.
- Updated `reactor-netty` version to `0.9.10.RELEASE`.
- Updated `netty` version to `4.1.51.Final`.
- Updated `netty-tcnative` version to `2.0.31.Final`.
- Fixed a bug where connections weren't being re-used when using a proxy which lead to a new TCP and SSL session for each request.
- Fixed a bug where a non-shareable proxy handler could be added twice into a `ChannelPipeline`.

## 1.5.3 (2020-07-02)

- Updated Azure Core dependency.

## 1.5.2 (2020-06-08)

- Fix bug where environment proxy wasn't inferred properly when it didn't use authentication.
- Updated Azure Core dependency.

## 1.5.1 (2020-05-04)

- Updated default retrieval of response body as a `String` to use `CoreUtils.bomAwareToString`.
- Updated Reactor Netty and Netty dependencies.

## 1.5.0 (2020-04-03)

- Generalized configuration for `NioEventLoopGroup` to `EventLoopGroup`. Deprecated `NioEventLoopGroup` setter.
- Updated Netty dependencies to `4.1.45.FINAL` and Netty Reactor to `0.9.5.RELEASE`.

## 1.4.0 (2020-03-06)

- Updated to latest version of Azure Core.

## 1.4.0-beta.1 (2020-02-11)

- Added support for Digest proxy authentication.
- Added ability to implicitly read proxy configurations from the environment.

## 1.3.0 (2020-02-10)

- Updated `NettyAsyncHttpClient` to deep copy ByteBuffer in `getBody()` response.
- Added option in `NettyAsyncHttpClientBuilder` to disable deep copy.

## 1.2.0 (2020-01-07)

- Upgrade netty dependencies to latest version

## Version 1.1.0 (2019-11-26)

- Switch to JUnit version 5.

## Version 1.0.0 (2019-10-29)

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-http-netty_1.0.0/sdk/core/azure-core-http-netty/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-core-http-netty_1.0.0/sdk/core/azure-core-http-netty/src/samples/java/com/azure/core/http/netty)

