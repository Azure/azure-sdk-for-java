# Release History

## 1.0.0-beta.3 (2025-04-25)

### Features Added
- Add diagnostic/logging messages for annotation processor [#45116](https://github.com/Azure/azure-sdk-for-java/pull/45116)
- Add support for parameterized host in annotation-processor [#45099](https://github.com/Azure/azure-sdk-for-java/pull/45099)
- Add support for static headers and query params in compile time codegen [#44750](https://github.com/Azure/azure-sdk-for-java/pull/44750)

## 1.0.0-beta.2 (2025-04-03)

### Features Added
- Added encoding support for query parameters and path parameters.
- Added support for `multipleQueryParam` in the `QueryParam` annotation.

### Bugs Fixed
- Fixed non-null path parameter values being appended in URLs.
- Fixed setting appropriate content type and request body based on header parameter/body parameter if provided, and body setting according to its type.

## 1.0.0-beta.1 (2025-03-12)

### Features Added

- Initial release. Please see the README and wiki for more information.
  This package includes annotation-processing code for constructing service layer implementations for Java client libraries.
