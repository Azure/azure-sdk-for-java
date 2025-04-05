# Release History

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
