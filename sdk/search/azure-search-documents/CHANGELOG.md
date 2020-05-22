# Release History

## 1.0.0-beta.4 (Unreleased)


## 1.0.0-beta.3 (2020-05-05)
- Replaced `isRetrievable` API with `isHidden`, parameter name changed from `retrievable` to `hidden`.
- Changed Azure Search service version from `2019-05-06` to `2019-05-06-Preview`
- Changed `createOrUpdate` and `delete` APIs in `SearchServiceClient` to use boolean `onlyIfUnchanged` instead of `MatchConditions`.
- Updated reactor core to `3.3.5.RELEASE`.
- Added helper class `FieldBuilder` which converts a strongly-typed model class to `List<Field>`. 
- Added annotations `FieldIgnore`, `SimpleFieldProperty`, and `SearchableFieldProperty` to define the `Field` on model properties.
- Added fluent class `SimpleField`, `SearchableField`, and `ComplexField` to build `Field`.

## 1.0.0-beta.2 (2020-04-06)

Version 1.0.0-beta.2 is the consecutive beta version of 11.0.0-beta.1. The version is made because we renamed 
the search client library module name and namespace.

- Renamed the azure-search module to azure-search-documents.
- Changed the namespace com.azure.search to com.azure.search.documents.
- Added support for continuation tokens to resume server-side paging.
- Replaced `SearchApiKeyCredential` with `AzureKeyCredential`.
- Moved `AzureKeyCredentialPolicy` to Azure Core.
- Fixed a bug where the Date header wouldn't be updated with a new value on request retry.
- Changed the field type of `CustomAnalyzer`.
- Made `RangeFacetResult` and `ValueFacetResult` object strongly typed.
- Added helper function for IndexBatchException. 
- Added ScoringParameter class.
- Refactored some boolean field getter.
- Made `IndexDocumentsBatch` APIs plurality.

## 11.0.0-beta.1 (2020-03-10)

Version 11.0.0-beta.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic 
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide 
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

- Initial release. Please see the README and wiki for information on the new design.
