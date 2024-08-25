# Release History

## 11.7.0 (2024-07-17)

### Features Added

- Added support for `2024-07-01` service version.
- `SemanticSearchOptions` now supports `semanticQuery`, which allows for specifying a semantic query that is only used
  for semantic reranking.
- `VectorQuery` now supports `oversampling` and `weight`, which allows for specifying richer configurations on how
  vector queries affect search results.
- Added support for `VectorizableTextQuery`, which allows for passing a text-based query that is vectorized service-side
  by `VectorSearchVectorizer`s configured on the index so that vectorization doesn't need to happen before querying.
- Added support for "bring your own endpoint" with `VectorSearchVectorizer`, with implementations `AzureOpenAIVectorizer`
  and `WebApiVectorizer`. This enables the service to use a user-provided configuration for vectorizing text, rather 
  than requiring all client-side calls to vectorize before querying, allowing for easier standardization of vectorization.
- Added support for compression with `VectorSearchCompression`, with implementations `BinaryQuantizationCompression`
  and `ScalarQuantizationCompression`. This allows for reducing the size of vectors in the index, which can reduce
  storage costs and improve querying performance.
- Added support for `VectorEncodingFormat`, which allows for specifying the encoding format of the vector data.
- Added support for `AzureOpenAIEmbeddingSkill`, which is a skill that uses the Azure OpenAI service to create text 
  embeddings during indexing.
- Added support for index projections with `SearchIndexerIndexProjection`, which allows for specifying how indexed 
  documents are projected in the index (or indexes).
- Added support for "narrow" types in `SearchFieldDataType`. This allows for specifying smaller types for vector fields
  to reduce storage costs and improve querying performance.
- Added support for `SearchIndexerDataIdentity`, which allows for specifying the identity for the data source for the 
  indexer.
- `SearchField` and `SearchableField` now support `stored` and `vectorEncodingFormat` configurations. `stored` allows
  for specifying behaviors on how the index will retain vector data (enabling the ability to reduce storage costs), and
  `vectorEncodingFormat` allows for specifying the encoding format of the vector data.
- `OcrSkill` now supports `OcrLineEnding`, which allows for specifying the line ending character used by the OCR skill.
- `SplitSkill` now supports `maximumPagesToTake` and `pageOverlapLength`, which allows for specifying how the split
  skill behaves when splitting documents into pages.
- `SearchServiceLimits` now supports `maxStoragePerIndexInBytes`, which shows the maximum storage allowed per index.

### Breaking Changes

- All service concepts that have been in preview but not included in the `2024-07-01` GA have been removed. This
  includes concepts such as index aliases, normalizers, Azure Machine Learning skills, hybrid search, and more.

### Other Changes

- All polymorphic models now expose their discriminator value through a getter, where the getter is named after the
  discriminator property such as `getOdataType()`. Super most polymorphic classes are no longer `abstract` to allow for
  direct instantiation of the class.

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.
- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.13` to version `1.4.14`.

## 11.6.6 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-serializer-json-jackson` from `1.4.12` to version `1.4.13`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.

## 11.6.5 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-serializer-json-jackson` from `1.4.11` to version `1.4.12`.
- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.

## 11.7.0-beta.3 (2024-05-07)

### Features Added

- Added support for `Byte[]` and `List<Byte>` in `FieldBuilder`
- Added support for `HybridSearch`
- Index models added: `AIServicesVisionParameters`, `AIServicesVisionVectorizer`, `AIStudioModelCatalogName`,
  `AzureMachineLearningParameters`, `AzureMachineLearningVectorizer`, `AzureOpenAIModelName`, `VectorEncodingFormat`,
 `VisionVectorizeSkill`
- Search models added: `HybridCountAndFacetMode`, `HybridSearch`, `SearchScoreThreshold`, `VectorSimilarityThreshold`, 
 `VectorThreshold`, `VectorThresholdKind`, `VectorizableImageBinaryQuery`, `VectorizableImageUrlQuery`

### Other Changes

- Sample added for creating a vector fields index with reduced dimensions.

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.11` to version `1.4.12`.

## 11.6.4 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.10` to version `1.4.11`.

## 11.6.3 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-serializer-json-jackson` from `1.4.9` to version `1.4.10`.
- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.

## 11.7.0-beta.2 (2024-03-11)

### Features Added

- Added support for the java types `Byte` and `Short` to FieldBuilder.
- Added support for "isStored" annotation to FieldBuilder.
- Added `VectorSearchCompressionConfiguration` to `VectorSearch`

### Breaking Changes

- Replaced `SearchErrorException` with `ErrorResponseException`

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-serializer-json-jackson` to version `1.4.10`.
- Upgraded `azure-core-http-netty` to version `1.14.1`.
- Upgraded `azure-core` to version `1.47.0`.

## 11.6.2 (2024-02-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.

## 11.7.0-beta.1 (2024-02-09)

### Features Added

- Exposing HttpPipeline within clients.

### Other Changes

- Rebranding Azure Cognitive Search to Azure AI Search within the SDK.
- Updated JavaDoc to assist with developer experience.
- Sample added to show how to maintain a consistent sessionId across multiple requests.

## 11.6.1 (2023-12-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-serializer-json-jackson` from `1.4.7` to version `1.4.8`.
- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.

## 11.6.0 (2023-11-13)

### Features Added

- Support for `VectorQuery` and configuring a `SearchIndex` for vector query and vector `SearchField`.
- Support for semantic search and configuring a `SearchIndex` semantic searching.
- Added `EntityLinkingSkill`, `PiiDetectionSkill`, a new version of `EntityRecognitionSkill` and support for setting 
  which version to use, and a new version of `SentimentSkill` and support for setting which version to use.

### Breaking Changes

- Deprecated `V1` version of `EntityRecognitionSkill` and `SentimentSkill` as these are deprecated on the service.
- Replaced Jackson Databind annotations with `azure-json` functionality for Search service models.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.6` to version `1.4.7`.
- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.

## 11.5.12 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.4` to version `1.4.6`.
- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.

## 11.6.0-beta.10 (2023-10-12)

### Features Added

- `SemanticQuery` has been added to `SearchOptions`, allowing the setting of a separate search query that will be solely 
  used for semantic reranking, semantic captions and semantic answers.

### Breaking Changes

- `SearchQueryVector` model has been replaced by `VectorQuery`.
- `SearchOptions` instance method `SearchOptions setVectors(List<SearchQueryVector>)` has been replaced by `SearchOptions setVectorQueries(List<VectorQuery>)`.
- `SearchOptions` instance method `SearchOptions setVectors(SearchQueryVector...)` has been replaced by `SearchOptions setVectorQueries(VectorQuery...)`.
- `SearchOptions` instance method `List<SearchQueryVector> getVectors()` has been replaced by `List<VectorQuery> getVectorQueries()`.
- `VectorSearch` instance property `List<VectorSearchAlgorithmConfiguration> algorithmConfigurations` has been replaced by properties `List<VectorSearchProfile> profiles`, 
  `List<VectorSearchAlgorithmConfiguration> algorithms`, and `List<VectorSearchVectorizer> vectorizers`.

### Other Changes

- Added additional models to support vector search.
- Updated vector search samples to use `VectorQuery`.

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.8`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.4` to version `1.4.5`.
- Upgraded `azure-core` from `1.43.0` to version `1.44.0`.

## 11.5.11 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.3` to version `1.4.4`.
- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.

## 11.6.0-beta.9 (2023-09-15)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.3` to version `1.4.4`.
- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.

## 11.5.10 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-serializer-json-jackson` from `1.4.2` to version `1.4.3`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 11.6.0-beta.8 (2023-08-11)

### Features Added

- Added FieldBuilder support for float data types.
- Added support for multi-vector searches.
- Adjusted method for setting `fields` in `SearchQueryVector` from `SearchQueryVector setFields(String)` to `SearchQueryVector setFields(String...)` for convenience.

### Breaking Changes

- `SearchOptions` method `SearchOptions setVector(SearchQueryVector)` has been replaced with `SearchOptions setVectors(SearchQueryVector...)` and `SearchOptions setVectors(List<SearchQueryVector>)`
- `SearchOptions` method `SearchQueryVector getVector()` has been replaced with `List<SearchQueryVector> getVectors()`

### Other Changes

- Added sample for multi-vector search.

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.2` to version `1.4.3`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 11.5.9 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.1` to version `1.4.2`.
- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.


## 11.6.0-beta.7 (2023-07-11)

### Features Added

- Added support for [Vector Search](https://learn.microsoft.com/azure/search/vector-search-overview) ([Examples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/search/azure-search-documents/src/samples/java/com/azure/search/documents/VectorSearchExample.java)).

### Breaking Changes

- Deprecated `EntityRecognitionSkillVersion.V1` and `SentimentSkillVersion.V1`, and corresponding constructors in
  `EntityRecognitionSkill` and `SentimentSkill`, use `EntityRecognitionSkillVersion.V3` and `SentimentSkillVersion.V3`
  instead. See [Cognitive Search skill deprecated](https://learn.microsoft.com/azure/search/cognitive-search-skill-deprecated) 
  for more details.

### Other Changes

- Migrate test recordings to assets repo.

## 11.5.8 (2023-06-09)

### Other Changes

- Properly set Index and Search Analyzer names in FieldBuilder

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-serializer-json-jackson` from `1.4.0` to version `1.4.1`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 11.5.7 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.
- Upgraded `azure-core-serializer-json-jackson` from `1.3.1` to version `1.4.0`.

## 11.6.0-beta.6 (2023-05-15)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.
- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core-serializer-json-jackson` from `1.3.1` to version `1.4.0`.
- Upgraded `azure-json` from `1.0.0` to version `1.0.1`.

## 11.5.6 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `azure-core-serializer-json-jackson` from `1.3.0` to version `1.3.1`.

## 11.5.5 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.25` to version `1.3.0`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 11.6.0-beta.5 (2023-02-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.24` to version `1.2.25`.

## 11.5.4 (2023-02-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.24` to version `1.2.25`.

## 11.6.0-beta.4 (2023-01-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.23` to version `1.2.24`.

## 11.5.3 (2023-01-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.23` to version `1.2.24`.

## 11.6.0-beta.3 (2022-11-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.22` to version `1.2.23`.

## 11.5.2 (2022-11-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.22` to version `1.2.23`.

## 11.6.0-beta.2 (2022-10-12)

### Bugs Fixed

- [Bug fix for incorrect JSON property names](https://github.com/Azure/azure-sdk-for-java/pull/31388)
- 
### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.21` to version `1.2.22`.

## 11.5.1 (2022-10-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.21` to version `1.2.22`.

## 11.6.0-beta.1 (2022-09-06)

### Features Added

- All features from the `11.5.x` betas that weren't included in `11.5.0`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.20` to version `1.2.21`.

## 11.5.0 (2022-09-06)

### Features Added

- Added multi-cloud support via `SearchAudience` to allow users to select the Azure cloud where the resource is located. ([#30331](https://github.com/Azure/azure-sdk-for-java/pull/30331))
- Added interfaces from `com.azure.core.client.traits` to `SearchIndexerClientBuilder`, `SearchIndexClientBuilder`
  and `SearchClientBuilder`.

### Other Changes

- Doesn't include other features added in the `11.5.x` betas. To continue using those features upgrade to `11.6.0-beta.1`.

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.20` to version `1.2.21`.

## 11.4.13 (2022-08-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-core-http-netty` from `1.12.3` to version `1.12.4`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.19` to version `1.2.20`.

## 11.5.0-beta.12 (2022-08-17)

### Features Added

- Added multi-cloud support via `SearchAudience` to allow users to select the Azure cloud where the resource is located. ([#30331](https://github.com/Azure/azure-sdk-for-java/pull/30331))
- Added new languages to `ImageAnalysisSkillLanguage` and `OcrSkillLanguage`. ([#30363](https://github.com/Azure/azure-sdk-for-java/pull/30363))

### Other Changes

- Added fluent setter overloads to subclasses that return the subclass type instead of the super class type
  allowing for fluent calls to maintain type instead of requiring a type cast.

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-core-http-netty` from `1.12.3` to version `1.12.4`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.19` to version `1.2.20`.

## 11.5.0-beta.11 (2022-07-01)

### Features Added

- Added support for `LexicalNormalizer`s in `SimpleField` and `SearchableField` annotations.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-core-http-netty` from `1.12.2` to version `1.12.3`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.18` to version `1.2.19`.

## 11.4.12 (2022-07-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-core-http-netty` from `1.12.2` to version `1.12.3`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.18` to version `1.2.19`.

## 11.5.0-beta.10 (2022-06-07)

### Bugs Fixed

- Fixed a bug where a custom `ObjectSerializer`'s output wasn't being respected in regard to null field values. ([#29238](https://github.com/Azure/azure-sdk-for-java/pull/29238))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.16` to version `1.2.18`.

## 11.4.11 (2022-06-07)

### Bugs Fixed

- Fixed a bug where a custom `ObjectSerializer`'s output wasn't being respected in regard to null field values. ([#29238](https://github.com/Azure/azure-sdk-for-java/pull/29238))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.16` to version `1.2.18`.

## 11.4.10 (2022-05-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.15` to version `1.2.16`.

## 11.5.0-beta.9 (2022-05-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.15` to version `1.2.16`.

## 11.5.0-beta.8 (2022-04-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.14` to version `1.2.15`.

## 11.4.9 (2022-04-08)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.14` to version `1.2.15`.

## 11.5.0-beta.7 (2022-03-10)

### Features Added

- Added support to create, update and delete aliases via the `SearchIndexClient`.

### Other Changes

- Updated all `ClientLogger`s to be static constants instead of instance variables.

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to version `1.26.0`.
- Upgraded `azure-core-http-netty` from `1.11.7` to version `1.11.8`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.13` to version `1.2.14`.

## 11.4.8 (2022-03-10)

### Other Changes

- Updated all `ClientLogger`s to be static constants instead of instance variables.

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to version `1.26.0`.
- Upgraded `azure-core-http-netty` from `1.11.7` to version `1.11.8`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.13` to version `1.2.14`.

## 11.5.0-beta.6 (2022-02-08)

### Features Added

- Added `Unk` as an `OcrSkillLanguage` value. The values are used to set the default language code for the 
  [OCR cognitive skill](https://docs.microsoft.com/azure/search/cognitive-search-skill-ocr).
- Support for [`AzureMachineLearningSkill`](https://docs.microsoft.com/azure/search/cognitive-search-aml-skill). The 
  AML skill allows you to extend AI enrichment with a custom [Azure Machine Learning](https://docs.microsoft.com/azure/machine-learning/overview-what-is-azure-machine-learning) 
  (AML) model. Once an AML model is [trained and deployed](https://docs.microsoft.com/azure/machine-learning/concept-azure-machine-learning-architecture#workspace), 
  an AML skill integrates it into AI enrichment.
- Added interfaces from `com.azure.core.client.traits` to `SearchIndexerClientBuilder`, `SearchIndexClientBuilder` 
  and `SearchClientBuilder`

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.12` to version `1.2.13`.

## 11.4.7 (2022-02-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.12` to version `1.2.13`.

## 11.4.6 (2022-01-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to version `1.24.1`.
- Upgraded `azure-core-http-netty` from `1.11.2` to version `1.11.6`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.9` to version `1.2.12`.

## 11.5.0-beta.5 (2021-11-24)

### Features Added

- Added support for [Semantic Search](https://docs.microsoft.com/azure/search/semantic-search-overview). `SearchOptions` 
  now support specifying `SemanticSettings` to influence the search behavior.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to version `1.22.0`.
- Upgraded `azure-core-http-netty` from `1.11.1` to version `1.11.2`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.8` to version `1.2.9`.

## 11.4.5 (2021-11-15)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to version `1.22.0`.
- Upgraded `azure-core-http-netty` from `1.11.1` to version `1.11.2`.
- Upgraded `azure-core-serializer-json-jackson` from `1.2.8` to version `1.2.9`.

## 11.4.4 (2021-10-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` and other dependencies for the library.
- 
## 11.5.0-beta.4 (2021-10-12)

### Features Added

- Added APIs to [reset documents](https://docs.microsoft.com/azure/search/search-howto-run-reset-indexers#reset-docs-preview) 
  and [skills](https://docs.microsoft.com/azure/search/search-howto-run-reset-indexers#reset-skills-preview).

### Breaking Changes

- Renamed `QueryAnswer` to `QueryAnswerType`, `QueryCaption` to `QueryCaptionType`, and `QuerySpeller` to `QuerySpellerType`.
- Renamed `SearchOptions` `queryCaptionHighlight` to `queryCaptionHighlightEnabled`.

### Other Changes

#### Dependency Updates

- Updated `azure-core` from `1.20.0` to `1.21.0`.
- Updated `azure-core-http-netty` from `1.11.0` to `1.11.1`.
- Updated `azure-core-serializer-json-jackson` from `1.2.7` to `1.2.8`.

## 11.4.3 (2021-09-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` and other dependencies for the library.

## 11.5.0-beta.3 (2021-09-08)

### Features Added

- Added `CreateOrUpdateDataSourceConnectionOptions`, `CreateOrUpdateIndexerOptions`, and `CreateOrUpdateSkillsetOptions`
  to support passing new parameters to REST API calls.
- Added support for `LexicalNormalizerName` in `AnalyzeTextOptions`.
- Added `queryCaptionHighlight` to `SearchOptions`.

### Breaking Changes

- Renamed `Captions` to `QueryCaption`.
- Exceptions are now thrown in `EntityRecognitionSkill` and `SentimentSkill` when properties are being set in unsupported
  skill versions.

### Other Changes

#### Dependency Updates

- Updated `azure-core` from `1.19.0` to `1.20.0`.
- Updated `azure-core-http-netty` from `1.10.2` to `1.11.0`.
- Updated `azure-core-serializer-json-jackson` from `1.2.6` to `1.2.7`.

## 11.5.0-beta.2 (2021-08-11)

### Features Added

- Added support for indexer caching.

### Dependency Updates

- Updated `azure-core` from `1.18.0` to `1.19.0`.
- Updated `azure-core-http-netty` from `1.10.1` to `1.10.2`.
- Updated `azure-core-serializer-json-jackson` from `1.2.5` to `1.2.6`.

## 11.4.2 (2021-08-11)

### Dependency Updates

- Updated `azure-core` from `1.18.0` to `1.19.0`.
- Updated `azure-core-http-netty` from `1.10.1` to `1.10.2`.
- Updated `azure-core-serializer-json-jackson` from `1.2.5` to `1.2.6`.

## 11.4.1 (2021-08-06)

### Dependency Updates

- Updated `azure-core` from `1.17.0` to `1.18.0`.
- Updated `azure-core-http-netty` from `1.10.0` to `1.10.1`.
- Updated `azure-core-serializer-json-jackson` from `1.2.4` to `1.2.5`.

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
- Added `getDefaultLogOptions` to `SearchClientBuilder`, `SearchIndexClientBuilder`, and `SearchIndexerClientBuilder`. Updated client construction to use default log options by default.
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
