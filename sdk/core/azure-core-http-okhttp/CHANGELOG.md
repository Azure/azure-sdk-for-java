# Release History

## 1.13.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.12.1 (2024-07-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to `1.50.0`.

## 1.12.0 (2024-06-06)

### Features Added

- Added support for response timeout and per-request response timeout. ([#40017](https://github.com/Azure/azure-sdk-for-java/pull/40017))

### Other Changes

- Fixed errant warning about `okio` being included on the module path multiple times. ([#40115](https://github.com/Azure/azure-sdk-for-java/pull/40155))

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to `1.49.1`.

## 1.11.21 (2024-05-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to `1.49.0`.

## 1.11.20 (2024-04-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to `1.48.0`.

## 1.11.19 (2024-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to `1.47.0`.

## 1.11.18 (2024-02-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to `1.46.0`.

## 1.11.16 (2023-11-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.
- Upgraded OkHttp from `4.10.0` to `4.12.0`.

## 1.11.15 (2023-10-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to `1.44.1`.

## 1.11.14 (2023-10-06)

### Bugs Fixed

- Fixed a bug where the network connection wasn't being explicitly closed after the response body was consumed.

### Other Changes

- Changed buffer read size from `4096` to `8192` when returning `Flux<ByteBuffer>` in `HttpResponse` to reduce number
  of reads and elements emitted by the `Flux`.

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to `1.44.0`.

## 1.11.13 (2023-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.43.0`.

## 1.11.12 (2023-08-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to `1.42.0`.
- 
## 1.11.11 (2023-07-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 1.11.10 (2023-06-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to `1.40.0`.

## 1.11.9 (2023-05-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to `1.39.0`.

## 1.11.8 (2023-04-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to `1.38.0`.

## 1.11.7 (2023-03-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to `1.37.0`.

## 1.11.6 (2023-02-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to `1.36.0`.

## 1.11.5 (2023-01-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to `1.35.0`.

## 1.11.4 (2022-11-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to `1.34.0`.

## 1.11.3 (2022-10-07)

### Bugs Fixed

- Fixed a bug where `HttpClientOptions.connectTimeout` wasn't being passed when using `HttpClientProvider(ClientOptions)`. ([#31079](https://github.com/Azure/azure-sdk-for-java/pull/31079))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to `1.33.0`.

## 1.11.2 (2022-09-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to `1.32.0`.

## 1.11.1 (2022-08-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to `1.31.0`.

## 1.11.0 (2022-06-30)

### Features Added

- Added ability to track progress by passing `ProgressReporter` in the `Context`. For example:
  ```java
  HttpClient httpClient = new OkHttpAsyncHttpClientBuilder().build();
  ProgressReporter progressReporter = ProgressReporter.withProgressListener(progress -> System.out.println(progress));
  Context context = Contexts.empty().setHttpRequestProgressReporter(progressReporter).getContext();
  HttpRequest request = new HttpRequest(
      HttpMethod.PUT, new URL("http://example.com"), new HttpHeaders(), BinaryData.fromString("sample body"))
  httpClient.send(request, context).subscribe();
  ```

### Other Changes

- Added synchronous implementation of `HttpClient.sendSync`.

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to `1.30.0`.
- Upgraded OkHttp from `4.9.2` to `4.10.0`.

## 1.10.1 (2022-06-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.0` to `1.29.1`.

## 1.10.0 (2022-06-03)

### Features Added
- Added `callTimeout` method to OkHttpAsyncHttpClientBuilder.
- This client can now stream bodies larger than 2GB. The buffering for `Flux<ByteBuffer>` request bodies has been removed.

### Other Changes

- Added specialized consumption for `HttpRequest.getBodyAsBinaryData()`.

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to `1.29.0`.

## 1.9.0 (2022-05-06)

### Features Added

- The `OkHttpAsyncClientProvider.createInstance()` now has the option to share a single shared `HttpClient`.
  Set the environment variable `AZURE_ENABLE_HTTP_CLIENT_SHARING` to `true` before starting the process to use
  the shared `HttpClient`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to `1.28.0`.

## 1.8.0 (2022-04-01)

### Features Added
 - Added `followRedirects` property on the `OkHttpClientBuilder`.

### Breaking Changes

- Okhttp-backed `HttpClient` client will no longer follow redirects automatically. ([#27960](https://github.com/Azure/azure-sdk-for-java/pull/27960)).
  <br>To get the older behavior please create an instance of `HttpClient` as follows

    ```java
    HttpClient client = new OkHttpAsyncHttpClientBuilder()
        .followRedirects(true)
        .build();
    ```

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to `1.27.0`.

## 1.7.10 (2022-03-04)

### Other Changes

- Updated all `ClientLogger`s to be static constants instead of instance variables. ([#27339](https://github.com/Azure/azure-sdk-for-java/pull/27339))

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to `1.26.0`.

## 1.7.9 (2022-02-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to `1.25.0`.

## 1.7.8 (2022-01-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.0` to `1.24.1`.

## 1.7.7 (2022-01-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.23.1` to `1.24.0`.

## 1.7.6 (2021-12-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to `1.23.1`.
- Upgraded OkHttp from `4.8.1` to `4.9.2`.

## 1.7.5 (2021-11-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to `1.22.0`.

## 1.7.4 (2021-10-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.20.0` to `1.21.0`.
- 
## 1.7.3 (2021-09-07)

### Features Added

- Added support for additional environment configurations. ([#23435](https://github.com/Azure/azure-sdk-for-java/pull/23435))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.19.0` to `1.20.0`.

## 1.7.2 (2021-08-06)

### Fixed

- Fixed a bug where OkHttp connections would occasionally deadlock when reading the response. ([#23183](https://github.com/Azure/azure-sdk-for-java/pull/23183))
- Fixed a bug where `NullPointerException` would be thrown when `HttpClientOptions` was used in `OkHttpAsyncClientProvider`
  without a connection pool size configured. ([#23357](https://github.com/Azure/azure-sdk-for-java/pull/23357))

### Dependency Updates

- Upgraded `azure-core` from `1.18.0` to `1.19.0`.

## 1.7.1 (2021-07-01)

### Features Added

- Added support for new `HttpClientOptions` configurations.

### Dependency Updates

- Upgraded `azure-core` from `1.17.0` to `1.18.0`.

## 1.7.0 (2021-06-07)

### Dependency Updates

- Upgraded `azure-core` from `1.16.0` to `1.17.0`.

## 1.6.2 (2021-05-07)

### Dependency Updates

- Upgraded `azure-core` from `1.15.0` to `1.16.0`.

## 1.6.1 (2021-04-02)

### Bug Fixes

- Fixed a bug where a proxy's address is only resolved during construction of the client, now it is resolved per connection. [#19497](https://github.com/Azure/azure-sdk-for-java/issues/19497)

### Dependency Updates

- Upgraded `azure-core` from `1.14.0` to `1.15.0`.

## 1.6.0 (2021-03-08)

### Dependency Updates

- Upgraded `azure-core` from `1.13.0` to `1.14.0`.

## 1.5.0 (2021-02-05)

### New Features

- Exposed service provider interfaces used to create `HttpClient` instances.

## 1.4.1 (2021-01-11)

### Bug Fixes

- Fixed a bug where environment proxy configurations were not sanitizing the non-proxy host string into a valid `Pattern` format. [#18156](https://github.com/Azure/azure-sdk-for-java/issues/18156)

## 1.4.0 (2020-11-24)

### New Features

- Added functionality to eagerly read HTTP response bodies into memory when they will be deserialized into a POJO.

## 1.3.3 (2020-10-29)

### Dependency updates

- Updated `azure-core` to `1.10.0`.

## 1.3.2 (2020-10-01)

- Updated `azure-core` version.

## 1.3.1 (2020-09-08)

- Updated `azure-core` version to pickup bug fix.

## 1.3.0 (2020-09-03)

- Updated `okhttp` dependency from `4.2.2` to `4.8.1`.
- Fixed bug where `Configuration` proxy would lead to a `NullPointerException` when set.
- Added request timeout configuration.
- Changed default connect timeout from 60 seconds to 10 and default read timeout from 120 seconds to 60 seconds.

## 1.2.5 (2020-08-07)

- Updated `azure-core` dependency.

## 1.2.4 (2020-07-02)

- Updated `azure-core` dependency.

## 1.2.3 (2020-06-08)

- Updated `azure-core` dependency.

## 1.2.2 (2020-05-04)

- Updated default retrieval of response body as a `String` to use `CoreUtils.bomAwareToString`.

## 1.2.1 (2020-04-03)

- Fixed issue where the body stream would be prematurely closed.

## 1.2.0 (2020-03-06)

- Updated `azure-core` dependency.

## 1.2.0-beta.1 (2020-02-11)

- Added support for Digest proxy authentication.
- Added ability to implicitly read proxy configurations from the environment.
- Removed setting 'Content-Type' to 'application/octet-stream' when null.

## 1.1.0 (2020-01-07)

- Updated versions of dependent libraries.

## Version 1.0.0 (2019-10-29)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core_1.0.0/sdk/core/azure-core-http-okhttp/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-core_1.0.0/sdk/core/azure-core-http-okhttp/src/samples/java/com/azure/core/http/okhttp)

- Initial release. Please see the README and wiki for information on the new design.
