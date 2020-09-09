# Release History

## 11.1.0-beta.2 (Unreleased)

- Replaced `ObjectSerializer` setters in builders with `JsonSerializer` to better represent the type requirement.

## 11.1.0-beta.1 (2020-08-12)

- Added `buildSearchFields` API to `SearchIndexClient` and `SearchIndexAsyncClient` to aid in creating `SearchField`s from the passed `Class`.
- Added `SearchableFieldProperty`, `SimpleFieldProperty`, and `FieldBuilderIgnore` to annotate `Class`es passed into `buildSearchFields`.
- Added `getDefaultLogOptions` to `SearchClientBuilder`, `SearchIndexCleintBuilder`, and `SearchIndexerClientBuilder`. Updated client construction to use default log options by default.
- Added the ability for clients to accept a `JsonSerializer` to specify a custom JSON serialization layer when dealing with Search documents.

## 11.0.0 (2020-07-13)

- Changed version to 11.0.0.
- Removed preview version `SearchClientOptions.ServiceVersion.V2019_05_06_Preview` and added version `SearchClientOptions.ServiceVersion.V2020_06_30`.

### New Features

- Added `IndexDocumentsOptions` used to configure document operations.

### Breaking Changes

- Moved search result metadata to `SearchPagedFlux` and `SearchPagedIterable` from `SearchPagedResponse`.
- Changed many model classes from fluent setter pattern to immutable constructor pattern.
- Removed `RequestOptions` from APIs, instead use pipeline context to pass per method contextual information.
- Removed strongly type GeoJSON classes.

### Bug Fixes

- Removed `implementation` classes from APIs.

## 1.0.0-beta.4 (2020-06-09)
- Split `SearchServiceClient` into two clients `SearchIndexClient`, `SearchIndexerClient`.
- Split `SearchServiceAsyncClient` into two clients `SearchIndexAsyncClient`, `SearchIndexerAsyncClient`.
- Added `SearchIndexClientBuilder` to build sync client `SearchIndexClient` and async client `SearchIndexAsyncClient`.
- Added `SearchIndexerClientBuilder` to build sync client `SearchIndexerClient` and async client `SearchIndexerAsyncClient`.
- Removed `SearchServiceClientBuilder`.
- Renamed `SearchIndexClient` to `SearchClient` and `SearchIndexAsyncClient` to `SearchAsyncClient`.
- Put all models used `SearchIndexClient` and `SearchIndexerClient` (same for async clients) under `com.azure.search.documents.indexes`.
- Removed `SearchIndexerDataSource` to `SearchIndexerDataSourceConnection`, 
- Renamed methods on `SearchIndexerClient` and `SearchIndexerAsyncClient` idiomatically matching "DataSource" to "DataSourceConnection".
- Removed `DataSourceCredential` and `AzureActiveDirectoryApplicationCredentials`, 
and uplifted the properties to `SearchIndexerDataSourceConnection` and `SearchResourceEncryptionKey` respectively.
- Removed `select` parameter from list service resource APIs.
- Added list names APIs for each search service resource. (e.g. `listSearchIndexNames`, `listSearchIndexerNames`, `listDataSourceNames`, `listSkillsetNames`, `listSynonymMapNames`)
- Removed deprecated versions and removed the V2 suffix. SDK is currently having `EdgeNGramTokenFilter`, `KeywordTokenizer`, `LuceneStandardTokenizer`,
`NGramTokenFilter`, and `PathHierarchyTokenizer`.
- Renamed `Similarity` to `SimilarityAlgorithm`.
- Renamed `Suggester` to `SearchSuggester`.
- Renamed fields `synonymMaps` to `synonymMapNames`, `analyzer` to `analyzerName`, 
`searchAnalyzer` to `searchAnalyzerName` and `indexAnalyzer` to `indexAnalyzerName` 
in `SearchField`, `SearchableField`.
- Renamed `SimpleField` to `SimpleFieldBuilder`, `SearchableField` to `SearchableFieldBuilder`
and `ComplexField` to `ComplexFieldBuilder`.

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
