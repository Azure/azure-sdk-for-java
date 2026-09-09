# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added
- Added support for `FormParam`, header collections, server-sent event listeners, and nested generic response types.
- Added support for text responses, standard Base64 responses, optional path parameters, and pre-serialized multipart
  request bodies.

### Breaking Changes

### Bugs Fixed
- Fixed response lifecycle handling so eagerly decoded responses are closed exactly once while streaming responses
  remain open for callers.
- Fixed request body handling for unknown-length `BinaryData`, direct and sliced `ByteBuffer` values, dynamic content
  types, and JSON media types with parameters.
- Fixed dynamic headers replacing static headers and collection query parameters honoring their expansion setting.

### Other Changes

## 1.0.0-beta.4 (2025-07-21)

### Bugs Fixed
- Use response HTTP Header for deserialization of response body (#[45901](https://github.com/Azure/azure-sdk-for-java/pull/45901))

## 1.0.0-beta.3 (2025-06-02)

### Features Added
- Add support for Base64 URI response handling (#[45491](https://github.com/Azure/azure-sdk-for-java/pull/45491))
- Use HttpResponseException in annotation-processor (#[45053](https://github.com/Azure/azure-sdk-for-java/pull/45053))
- Use UriBuilder when creating HttpRequest.setUri in annotation-processor (#[45201](https://github.
  com/Azure/azure-sdk-for-java/pull/45201))
- Add try-with-resources to the annotation-processor (#[45193](https://github.com/Azure/azure-sdk-for-java/pull/45193))

### Bugs Fixed
- Fix the issue with the nextLink when the host is also provided.
- Fix ResponseHandler for return of generic BinaryData types (#[45299](https://github.com/Azure/azure-sdk-for-java/pull/45299))
- Fix Uri for host substitution when the path is / (#[45314](https://github.com/Azure/azure-sdk-for-java/pull/45314))
- Fix body optionality for contentType and request body setting (#[45528](https://github.com/Azure/azure-sdk-for-java/pull/45528))

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
