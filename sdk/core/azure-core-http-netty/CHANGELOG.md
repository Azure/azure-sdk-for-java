# Release History

## 1.6.0-beta.1 (Unreleased)


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

