# Azure Search Documents for Java
    
> see https://aka.ms/autorest

This is the AutoRest configuration file for SearchServiceClient and SearchIndexClient.
---
## Getting Started 

To build the SDK for SearchServiceClient and SearchIndexClient, simply [Install AutoRest](https://aka.ms/autorest) and 
in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

There are two swaggers for Azure Search, `searchindex` and `searchservice`. They always under same package version, e.g. 
`--tag=searchindex` and `--tag=searchservice`.

```ps
cd <swagger-folder>
autorest
```

e.g.
```ps
cd <swagger-folder>
autorest --tag=searchindex
autorest --tag=searchservice
```

## Manual Changes

This section outlines all the checks that should be done to a newly generated Swagger as Azure Search Documents for Java
contains manual translations of generated code to public API.

### SearchPagedFlux, SearchPagedIterable, and SearchPagedResponse

New properties added to `SearchPagedResponse` need to be exposed as getter properties on `SearchPagedFlux` and
`SearchPagedIterable`. Only the first `SearchPagedResponse` properties are exposed on `SearchPagedFlux` and 
`SearchPagedIterable`.

### Converters

There are a set of `*Converter` classes in the package `com.azure.search.documents.implementation.converters` that will
need to be updated if any of the models that get converted have new properties added. The converted model types are
`AnalyzeRequest`, `IndexAction`, `SearchResult`, and `SuggestResult`.

### SearchOptions

There is `SearchOptions` in both implementation and public API, any time new properties are added to the implementation
`SearchOptions` they need to be included in the public API model. Additionally, `List`-based properties use varargs
setters instead of `List` setters in the public API and `QueryAnswerType` and `QueryCaptionType` properties need special
handling. `QueryAnswerType` and `QueryCaptionType` are defined as `ExpandableStringEnum`s but they have special 
configurations based on the String value that Autorest cannot generate, `QueryAnswerType` has special configurations
`answerCount` and `answerThreshold` and `QueryCaptionType` has special configuration `highlight` that need to be added 
as additional properties on the public `SearchOptions`.

### AutocompleteOptions and SuggestOptions

`AutocompleteOptions` and `SuggestOptions` have converters that need to be updated with new properties are added so they
match `AutocompleteRequest` or `SuggestRequest`. The options-based and request-based models are code generated but only
the options-based models are generated into the public API.

## Configuration

### Basic Information 
These are the global settings for SearchServiceClient and SearchIndexClient.

``` yaml
opt-in-extensible-enums: true
openapi-type: data-plane
```

### Tag: searchindex

These settings apply only when `--tag=searchindex` is specified on the command line.

``` yaml $(tag) == 'searchindex'
namespace: com.azure.search.documents
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0cfd102a6ecb172f04ec915732bd8ca6f6b2a7af/specification/search/data-plane/Azure.Search/preview/2023-07-01-Preview/searchindex.json
models-subpackage: models
custom-types-subpackage: implementation.models
custom-types: AutocompleteRequest,IndexAction,IndexBatch,RequestOptions,SearchDocumentsResult,SearchError,SearchErrorException,SearchOptions,SearchRequest,SearchResult,SuggestDocumentsResult,SuggestRequest,SuggestResult
customization-class: src/main/java/SearchIndexCustomizations.java
```

### Tag: searchservice

These settings apply only when `--tag=searchservice` is specified on the commandSearchServiceCounters line.

``` yaml $(tag) == 'searchservice'
namespace: com.azure.search.documents.indexes
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0cfd102a6ecb172f04ec915732bd8ca6f6b2a7af/specification/search/data-plane/Azure.Search/preview/2023-07-01-Preview/searchservice.json
models-subpackage: models
custom-types-subpackage: implementation.models
custom-types: AnalyzeRequest,AnalyzeResult,AzureActiveDirectoryApplicationCredentials,DataSourceCredentials,DocumentKeysOrIds,EdgeNGramTokenFilterV1,EdgeNGramTokenFilterV2,EntityRecognitionSkillV1,EntityRecognitionSkillV3,KeywordTokenizerV1,KeywordTokenizerV2,ListAliasesResult,ListDataSourcesResult,ListIndexersResult,ListIndexesResult,ListSkillsetsResult,ListSynonymMapsResult,LuceneStandardTokenizerV1,LuceneStandardTokenizerV2,NGramTokenFilterV1,NGramTokenFilterV2,RequestOptions,SearchError,SearchErrorException,SentimentSkillV1,SentimentSkillV3,SkillNames
customization-class: src/main/java/SearchServiceCustomizations.java
directive:
    - rename-model:
        from: ClassicSimilarity
        to: ClassicSimilarityAlgorithm
    - rename-model:
        from: BM25Similarity
        to: BM25SimilarityAlgorithm
    - rename-model:
        from: Similarity
        to: SimilarityAlgorithm
    - rename-model:
        from: GetIndexStatisticsResult
        to: SearchIndexStatistics
    - rename-model:
        from: Suggester
        to: SearchSuggester
    - rename-model:
        from: PIIDetectionSkill
        to: PiiDetectionSkill
    - rename-model:
        from: EntityRecognitionSkill
        to: EntityRecognitionSkillV1
    - rename-model:
        from: SentimentSkill
        to: SentimentSkillV1
    - rename-model:
        from: EdgeNGramTokenFilter
        to: EdgeNGramTokenFilterV1
    - rename-model:
        from: NGramTokenFilter
        to: NGramTokenFilterV1
    - rename-model:
        from: PathHierarchyTokenizerV2
        to: PathHierarchyTokenizer
    - rename-model:
        from: LuceneStandardTokenizer
        to: LuceneStandardTokenizerV1
    - rename-model:
        from: KeywordTokenizer
        to: KeywordTokenizerV1
    - rename-model:
        from: SearchIndexerDataSource
        to: SearchIndexerDataSourceConnection
```

---
# Code Generation

!!! READ THIS !!!
This swagger is ready for C# and Java.
!!! READ THIS !!!

## Java

``` yaml
output-folder: ../
java: true
use: '@autorest/java@4.1.17'
enable-sync-stack: true
generate-client-interfaces: false
context-client-method-parameter: true
generate-client-as-impl: true
service-interface-as-public: true
required-fields-as-ctor-args: true
license-header: MICROSOFT_MIT_SMALL_NO_VERSION
disable-client-builder: true
require-x-ms-flattened-to-flatten: true
pass-discriminator-to-child-deserialization: true
stream-style-serialization: true
include-read-only-in-constructor-args: true
```

### Set odata.metadata Accept header in operations

searchindex.json needs odata.metadata=none and searchservice.json needs odata.metadata=minimal as the "Accept" header.

``` yaml $(java)
directive:
  - from: swagger-document
    where: $.paths
    transform: >
      for (var path in $) {
        for (var opName in $[path]) {
          let accept = "application/json; odata.metadata=";
          accept += path.startsWith("/docs") ? "none" : "minimal";

          let op = $[path][opName];
          let param = op.parameters.find(p => p.name === "Accept");
          if (param === null) {
            param.enum = [ accept ];
          } else {
            op.parameters.push({
              name: "Accept",
              "in": "header",
              required: true,
              type: "string",
              enum: [ accept ],
              "x-ms-parameter-location": "method"
            });
          }
        }
      }

      return $;
```

### Remove required from properties that are optional

``` yaml $(tag) == 'searchservice'
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.SearchIndex.required = $.SearchIndex.required.filter(required => required === 'name');
      $.SearchIndexer.required = $.SearchIndexer.required.filter(required => required === 'name');
      $.SearchIndexerDataSourceConnection.required = $.SearchIndexerDataSourceConnection.required.filter(required => required === 'name');
      $.SearchIndexerSkillset.required = $.SearchIndexerSkillset.required.filter(required => required === 'name');
      delete $.SynonymMap.required;
      $.ServiceCounters.required = $.ServiceCounters.required.filter(required => required !== 'aliasesCount' && required !== 'skillsetCount' && required !== 'vectorIndexSize');
```

### Renames 
``` yaml $(tag) == 'searchservice'
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.ServiceCounters["x-ms-client-name"] = "SearchServiceCounters";
      $.ServiceLimits["x-ms-client-name"] = "SearchServiceLimits";
      $.ServiceStatistics["x-ms-client-name"] = "SearchServiceStatistics";
```

``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.PdfTextRotationAlgorithm
    transform: >
      $["x-ms-enum"].name = "BlobIndexerPdfTextRotationAlgorithm";
```

### Add serialization discriminator to LexicalNormalizer
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.LexicalNormalizer
    transform: >
      $.discriminator = "@odata.type";
```

### Change SearchField retrievable to hidden
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.SearchField.properties
    transform: >
      $.retrievable["x-ms-client-name"] = "hidden";
      $.retrievable.description = "A value indicating whether the field will be returned in a search result. This property must be false for key fields, and must be null for complex fields. You can hide a field from search results if you want to use it only as a filter, for sorting, or for scoring. This property can also be changed on existing fields and enabling it does not cause an increase in index storage requirements.";
      $.analyzer["x-ms-client-name"] = "analyzerName";
      $.searchAnalyzer["x-ms-client-name"] = "searchAnalyzerName";
      $.indexAnalyzer["x-ms-client-name"] = "indexAnalyzerName";
      $.normalizer["x-ms-client-name"] = "normalizerName";
      $.synonymMaps["x-ms-client-name"] = "synonymMapNames";
```

### Rename includeTotalResultCount to includeTotalCount
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.paths["/docs"].get.parameters
    transform: >
      let param = $.find(p => p.name === "$count");
      param["x-ms-client-name"] = "includeTotalCount";
```

### Rename Speller to QuerySpellerType
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.paths["/docs"].get.parameters
    transform: >
      $.find(p => p.name === "speller")["x-ms-enum"].name = "QuerySpellerType";
```

### Rename Answers to QueryAnswerType, Captions to QueryCaptionType, and Speller to QuerySpellerType
``` yaml $(tag) == 'searchindex'
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.Answers["x-ms-enum"].name = "QueryAnswerType";
      $.Captions["x-ms-enum"].name = "QueryCaptionType";
      $.Speller["x-ms-enum"].name = "QuerySpellerType";
```

### Change Answers and Captions to a string in SearchOptions and SearchRequest
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.paths["/docs"].get.parameters
    transform: >
      let param = $.find(p => p.name === "answers");
      param.type = "string";
      delete param.enum;
      delete param["x-ms-enum"];
     
      param = $.find(p => p.name == "captions");
      param.type = "string";
      delete param.enum;
      delete param["x-ms-enum"];
```

``` yaml $(tag) == 'searchindex'
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      let param = $.SearchRequest.properties.answers;
      param.type = "string";
      param.description = $.Answers.description;
      delete param["$ref"];
      
      param = $.SearchRequest.properties.captions;
      param.type = "string";
      param.description = $.Captions.description;
      delete param["$ref"];
```

### Remove applicationId from being required in AzureActiveDirectoryApplicationCredentials
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.AzureActiveDirectoryApplicationCredentials
    transform: >
      delete $.required;
```

### Client side rename of SearchResourceEncryptionKey's vaultUri to vaultUrl
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.SearchResourceEncryptionKey
    transform: >
      $.properties.keyVaultUri["x-ms-client-name"] = "vaultUrl";
```

### Remove Suggester's SearchMode from being required
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.SearchSuggester
    transform: >
      $.required = [ "name", "sourceFields" ];
```

### Rename PIIDetectionSkillMaskingMode to PiiDetectionSkillMaskingMode
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.PIIDetectionSkillMaskingMode
    transform: >
      $["x-ms-enum"].name = "PiiDetectionSkillMaskingMode";
```

### Rename client parameter names
``` yaml $(tag) == 'searchservice'
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.CommonGramTokenFilter.properties.ignoreCase["x-ms-client-name"] = "caseIgnored";
      $.CommonGramTokenFilter.properties.queryMode["x-ms-client-name"] = "queryModeUsed";
      $.DictionaryDecompounderTokenFilter.properties.onlyLongestMatch["x-ms-client-name"] = "onlyLongestMatched";
      $.KeywordMarkerTokenFilter.properties.ignoreCase["x-ms-client-name"] = "caseIgnored";
      $.LimitTokenFilter.properties.consumeAllTokens["x-ms-client-name"] = "allTokensConsumed";
      $.MicrosoftLanguageStemmingTokenizer.properties.isSearchTokenizer["x-ms-client-name"] = "isSearchTokenizerUsed";
      $.PathHierarchyTokenizer.properties.reverse["x-ms-client-name"] = "tokenOrderReversed";
      $.PhoneticTokenFilter.properties.replace["x-ms-client-name"] = "originalTokensReplaced";
      $.StopwordsTokenFilter.properties.ignoreCase["x-ms-client-name"] = "caseIgnored";
      $.StopwordsTokenFilter.properties.removeTrailing["x-ms-client-name"] = "trailingStopWordsRemoved";
      $.SynonymTokenFilter.properties.ignoreCase["x-ms-client-name"] = "caseIgnored";
      $.WordDelimiterTokenFilter.properties.catenateWords["x-ms-client-name"] = "wordsCatenated";
      $.WordDelimiterTokenFilter.properties.catenateNumbers["x-ms-client-name"] = "numbersCatenated";
```

### Rename Dimensions

To ensure alignment with `VectorSearchConfiguration`, rename the `Dimensions` to `VectorSearchDimensions`.

```yaml $(tag) == 'searchservice'
directive:
- from: swagger-document
  where: $.definitions.SearchField.properties.dimensions
  transform: $["x-ms-client-name"] = "vectorSearchDimensions";
```

### Add `arm-id` format for `AuthResourceId`

Add `"format": "arm-id"` for `AuthResourceId` to generate as [Azure.Core.ResourceIdentifier](https://learn.microsoft.com/dotnet/api/azure.core.resourceidentifier?view=azure-dotnet).

```yaml $(tag) == 'searchservice'
directive:
- from: swagger-document
  where: $.definitions.WebApiSkill.properties.authResourceId
  transform: $["x-ms-format"] = "arm-id";
```

### Rename Vector property `K`

Rename Vector property `K` to `KNearestNeighborsCount`

```yaml $(tag) == 'searchindex'
directive:
- from: swagger-document
  where: $.definitions.Vector.properties.k
  transform: $["x-ms-client-name"] = "KNearestNeighborsCount";
```

### Rename Vector property `Vector`

Rename Vector property `Vector` to `SearchQueryVector`

```yaml $(tag) == 'searchindex'
directive:
- from: swagger-document
  where: $.definitions.Vector
  transform: $["x-ms-client-name"] = "SearchQueryVector";
```

### Rename QueryResultDocumentSemanticFieldState

Simplify `QueryResultDocumentSemanticFieldState` name by renaming it to `SemanticFieldState`

```yaml $(tag) == 'searchindex'
directive:
- from: swagger-document
  where: $.definitions.QueryResultDocumentSemanticFieldState
  transform: $["x-ms-enum"].name = "SemanticFieldState";
```
