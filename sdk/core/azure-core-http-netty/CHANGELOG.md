# Release History

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
- 
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

