# Release History

## 11.5.0-beta.2 (Unreleased)


## 11.5.0-beta.1 (2021-07-20)

### Features Added

- Added support for service version `2021-04-30-Preview`. Default version is now `2021-04-30-Preview`.
- Added Semantic Search capability to `SearchClient` and `SearchAsyncClient`.
- Added the ability to configure AAD authentication in client builders to enable AAD authentication in clients.
- Added support for Normalizers in `SearchField` and `SearchIndex` with `CustomNormalizer` and `LexicalNormalizer`.
- Added new versions of `EntityRecognitionSkill` and `SentimentSkill`. Added `PiiDetectionSkill`.
- Added support for indexer data identity.

### Dependency Updates

- Updated `azure-core` from `1.17.0` to `1.18.0`.
- Updated `azure-core-http-netty` from `1.10.0` to `1.10.1`.
- Updated `azure-core-serializer-json-jackson` from `1.2.4` to `1.2.5`.

## 11.4.0 (2021-06-08)

### Features Added

- Added the ability to configure Knowledge Store in skillsets.
- Added factory method to `SynonymMap` to enable creation from a file.
- Added support for `Edm.GeographyPoint` in `FieldBuilder` when property has type `GeoPoint`.
- Added support for geography based filtering in `SearchFilter` when `GeoPosition`, `GeoPoint`, `GeoLineString`, or
  `GeoPolygon` are used as formatting arguments.
- Added new skills `CustomEntityLookupSkill` and `DocumentExtractionSkill` and new skill versions for
  `KeyPhraseExtractionSkill` and `LanguageDetectionSkill`.
- Added support for the ADLS Gen 2 Indexer data source type.
- Added skillset counts to `SearchServiceCounters`.
- Added additional log messages to `SearchIndexingBufferedSender` and `SearchIndexingBufferedAsyncSender`.

### Breaking Changes

- Removed support for service version `2020-06-30-Preview`. Default version is now `2020-06-30`.
- Removed Semantic Search capability to `SearchClient` and `SearchAsyncClient`.
- Removed support for Normalizers in `SearchField` and `SearchIndex` with `CustomNormalizer` and `LexicalNormalizer`.

### Dependency Updates

- Updated `azure-core` from `1.16.0` to `1.17.0`.
- Updated `azure-core-http-netty` from `1.9.2` to `1.10.0`.
- Updated `azure-core-serializer-json-jackson` from `1.2.3` to `1.2.4`.
- Updated Jackson from `2.12.2` to `2.12.3`.
- Updated Reactor from `3.4.5` to `3.4.6`.
- Updated Reactor Netty from `1.0.6` to `1.0.7`.

## 11.3.2 (2021-05-11)

### Dependency Updates

- Updated `azure-core` from `1.15.0` to `1.16.0`.
- Updated `azure-core-http-netty` from `1.9.1` to `1.9.2`.
- Updated `azure-core-serializer-json-jackson` from `1.2.2` to `1.2.3`.

## 11.4.0-beta.2 (2021-05-10)

### Features Added

- Added Semantic Search capability to `SearchClient` and `SearchAsyncClient`.
- Added the ability to configure Knowledge Store in skillsets.
- Added factory method to `SynonymMap` to enable creation from a file.

### Dependency Updates

- Updated `azure-core` from `1.15.0` to `1.16.0`.
- Updated `azure-core-http-netty` from `1.9.1` to `1.9.2`.
- Updated `azure-core-serializer-json-jackson` from `1.2.2` to `1.2.3`.

## 11.3.1 (2021-04-08)

### Dependency Updates

- Updated `azure-core` from `1.14.0` to `1.15.0`.
- Updated `azure-core-http-netty` from `1.9.0` to `1.9.1`.
- Updated `azure-core-serializer-json-jackson` from `1.2.0` to `1.2.2`.
- Updated Jackson from `2.12.1` to `2.12.2`.

## 11.4.0-beta.1 (2021-04-06)

### New Features

- Clients now default to using service version `2020-06-30-Preview`.
- Added support for `Edm.GeographyPoint` in `FieldBuilder` when property has type `GeoPoint`.
- Added support for geography based filtering in `SearchFilter` when `GeoPosition`, `GeoPoint`, `GeoLineString`, or
  `GeoPolygon` are used as formatting arguments.
- Added support for Normalizers in `SearchField` and `SearchIndex` with `CustomNormalizer` and `LexicalNormalizer`.
- Added new skills `CustomEntityLookupSkill` and `DocumentExtractionSkill` and new skill versions for 
  `KeyPhraseExtractionSkill` and `LanguageDetectionSkill`.
- Added support for the ADLS Gen 2 Indexer data source type.
- Added skillset counts to `SearchServiceCounters`.  
- Added additional log messages to `SearchIndexingBufferedSender` and `SearchIndexingBufferedAsyncSender`.

### Dependency Updates

- Updated `azure-core` from `1.14.0` to `1.15.0`.
- Updated Jackson from `2.12.1` to `2.12.2`.

### Breaking Changes

- Updated Jackson annotations to include `required = true` when service must receive or return the property.

## 11.3.0 (2021-03-10)

### Dependency Updates

- Updated `azure-core` from `1.13.0` to `1.14.0`.
- Updated Jackson from `2.11.3` to `2.12.1`.
- Updated Reactor from `3.3.12.RELEASE` to `3.4.3`.
- Updated Reactor Netty from `0.9.15.RELEASE` to `1.0.4`.

## 11.2.0 (2021-02-10)

### New Features

- Added a builder, `SearchIndexingBufferedSenderBuilder<T>`, to configure and construct `SearchIndexingBufferedSender<T>`.
- Added `SearchClientBuilder.bufferedSender(TypeReference)` to create a `SearchIndexingBufferedSenderBuilder<T>` with
  base configuration passed from `SearchClientBuilder`.
- Added `OnActionAddedOptions<T>`, `OnActionErrorOptions<T>`, `OnActionSentOptions<T>`, and `OnActionSucceededOptions<T>`
  as request options for the on action callback methods in `SearchIndexingBufferedSender`.
- Added `ClientOptions` APIs to all builders to allow re-using common client configurations.
- All changes from the 11.2.0-beta.3, 11.2.0-beta.2, and 11.2.0-beta.1 releases listed below.

### Breaking Changes

- Removed `SearchIndexingBufferedSenderOptions` and `SearchClient.getSearchIndexingBufferedSender` and 
  `SearchAsyncClient.getSearchIndexingBufferedSender`.
- Changed buffered sender configuration options from `autoFlushWindow` to `autoFlushInterval`, 
  `maxRetries` to `maxRetriesPerAction`, `retryDelay` to `throttlingDelay`, `maxRetryDelay` to `maxThrottlingDelay`
  and `onActionErrorBiConsumer` to `onActionErrorConsumer`.
- Renamed `BlobIndexerPDFTextRotationAlgorithm` to `BlobIndexerPdfTextRotationAlgorithm`.

### Dependency updates

- Updated `azure-core` to `1.13.0`.
- Updated `azure-core-http-netty` to `1.8.0`.

## 11.1.3 (2021-01-15)

### Dependency updates

- Updated `azure-core` to `1.12.0`.
- Updated `azure-core-http-netty` to `1.7.1`.

## 11.2.0-beta.3 (2020-11-10)

### New Features

- Added encryption key to `SearchIndexer`, `SearchIndexerDataSourceConnection`, and `SearchIndexerSkillset`.
- Added ability to configure initial batch size and retry back-offs to `SearchIndexingBufferedSenderOptions`.

### Breaking Changes

- Removed `SearchIndexingBufferedSender.getBatchSize()`.
- `SearchIndexingBufferedSenderOptions` now throws on invalid values instead of falling back to default.

## 11.1.2 (2020-11-10)

### Dependency updates

- Updated `azure-core` version.

## 11.2.0-beta.2 (2020-10-06)

### New Features

- Added `SearchFilter` to help aid creation of OData filter expressions.
- Added required parameter `documentKeyRetriever` to `SearchIndexingBufferedSender` to better correlate response documents to sent documents.
- Added `ClientOptions` to all builders to support setting `applicationId` in `User-Agent` string and headers that need to be applied to each request.
- Added support for `HttpPipelinePosition` in client builders to determine when an `HttpPipelinePolicy` will be invoked.

### Breaking Changes

- Renamed `SearchBatchClient` and `SearchBatchAsyncClient` to `SearchIndexingBufferedSender` and `SearchIndexingBufferedAsyncSender`.
- Removed `SearchBatchClientBuilder` for options bag `SearchIndexingBufferedSenderOptions`.
- Renamed `getSearchBatchClient` to `getSearchIndexingBufferedSender` in `SearchClient`.
- Made `SearchIdexingBufferedSender` generic typed.
- Removed `IndexingHooks` in favor of individual callbacks.
- Removed the ability to configure `batchSize` on buffered sender and changed the default to 500 instead of 1000.
- Changed `onActionRemoved` to `onActionSent`.

## 11.1.1 (2020-10-02)

### Dependency Updates

- Updated `azure-core` version.

## 11.2.0-beta.1 (2020-09-10)

### New Features

- Added `SearchBatchClient` and `SearchBatchAsyncClient` which handle automatically creating and sending document batches.
- Added `IndexingHook` interface to provide callback functionality when indexing documents with batching clients.
- Added `IndexingParametersConfiguration`, and related enums, to offer strongly type configuration for `IndexingParameters`.
- Added `ScoringStatistics` and `SessionId` to `SearchOptions`.

### Breaking Changes

- Updated Jackson annotations to include `required = true` when service must receive or return the property.

### Bug Fixes

- Changed `Fluent` annotations to `Immutable` when the class is immutable.

## 11.1.0 (2020-09-09)

### New Features

- GA release of `buildSearchFields` on `SearchIndexClient` and `SearchIndexAsyncClient`.
- GA release of `JsonSerializer` functionality for `SearchClient` and `SearchAsyncClient`.
- GA release of default `HttpLogOptions` on client builders.

### Breaking Changes

- Renamed `SearchableFieldProperty` to `SearchableField` and `SimpleFieldProperty` to `SimpleField`.
- Renamed `FieldBuilderOptions.setConverter` to `FieldBuilderOptions.setJsonSerializer`.
- Replaced `ObjectSerializer` setters in builders with `JsonSerializer` to better represent the type requirement.

### Bug Fixes

- Deprecated getter `OcrSkill.setShouldDetectOrientation()` and replaced with correct Javabeans named `isShouldDetectOrientation()`.

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
- Removed `SearchIndexerDataSource` to `SearchIndexerDataSourceConnection`.
- Renamed methods on `SearchIndexerClient` and `SearchIndexerAsyncClient` idiomatically matching "DataSource" to "DataSourceConnection".
- Removed `DataSourceCredential` and `AzureActiveDirectoryApplicationCredentials`
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
- Changed Azure Search service version from `2019-05-06` to `2019-05-06-Preview`.
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
