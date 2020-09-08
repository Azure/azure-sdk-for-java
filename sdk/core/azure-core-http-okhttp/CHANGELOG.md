# Release History

## 1.3.0-beta.1 (Unreleased)


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
