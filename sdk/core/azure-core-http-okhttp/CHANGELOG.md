# Release History

## 1.5.0-beta.1 (Unreleased)


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
