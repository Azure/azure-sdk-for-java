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
Fork and clone https://github.com/Azure/autorest.java 
git checkout v4
git submodule update --init --recursive
mvn package -Dlocal -DskipTests
npm install
npm install -g autorest
```

### Generation

There are two swaggers for Azure Search, `searchindex` and `searchservice`. They always under same package version, e.g. 
`--tag=package-2020-06-Preview-searchindex` and `--tag=package-2020-06-Preview-searchservice`.

```ps
cd <swagger-folder>
autorest --use=C:/work/autorest.java
```

e.g.
```ps
cd <swagger-folder>
autorest --use=C:/work/autorest.java --tag=package-2020-06-Preview-searchindex
autorest --use=C:/work/autorest.java --tag=package-2020-06-Preview-searchservice
```
## Configuration

### Basic Information 
These are the global settings for SearchServiceClient and SearchIndexClient.

``` yaml
opt-in-extensible-enums: true
openapi-type: data-plane
```

### Tag: package-2020-06-Preview-searchindex

These settings apply only when `--tag=package-2020-06-Preview-searchindex` is specified on the command line.

``` yaml $(tag) == 'package-2020-06-Preview-searchindex'
namespace: com.azure.search.documents
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/e6fa7db931a3e5182e5685630971b64987719938/specification/search/data-plane/Azure.Search/preview/2020-06-30-Preview/searchindex.json
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: AnswerResult,AutocompleteItem,AutocompleteMode,AutocompleteOptions,AutocompleteResult,CaptionResult,FacetResult,IndexActionType,QueryAnswer,QueryLanguage,QuerySpeller,QueryType,ScoringStatistics,SearchMode,SuggestOptions
customization-class: src/main/java/SearchIndexCustomizations.java
```

### Tag: package-2020-06-Preview-searchservice

These settings apply only when `--tag=package-2020-06-Preview-searchservice` is specified on the command line.

``` yaml $(tag) == 'package-2020-06-Preview-searchservice'
namespace: com.azure.search.documents.indexes
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/e6fa7db931a3e5182e5685630971b64987719938/specification/search/data-plane/Azure.Search/preview/2020-06-30-Preview/searchservice.json
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: AnalyzedTokenInfo,BlobIndexerDataToExtract,BlobIndexerImageAction,BlobIndexerPdfTextRotationAlgorithm,BlobIndexerParsingMode,BM25SimilarityAlgorithm,CharFilter,CharFilterName,CjkBigramTokenFilterScripts,ClassicSimilarityAlgorithm,CognitiveServicesAccount,CognitiveServicesAccountKey,ConditionalSkill,CorsOptions,CustomEntity,CustomEntityAlias,CustomEntityLookupSkill,CustomEntityLookupSkillLanguage,CustomNormalizer,DataChangeDetectionPolicy,DataDeletionDetectionPolicy,DefaultCognitiveServicesAccount,DistanceScoringFunction,DistanceScoringParameters,DocumentExtractionSkill,EdgeNGramTokenFilterSide,EntityCategory,EntityRecognitionSkill,EntityRecognitionSkillLanguage,FieldMapping,FieldMappingFunction,FreshnessScoringFunction,FreshnessScoringParameters,HighWaterMarkChangeDetectionPolicy,ImageAnalysisSkill,ImageAnalysisSkillLanguage,ImageDetail,IndexerExecutionEnvironment,IndexerExecutionResult,IndexerExecutionStatus,IndexerStatus,IndexingParametersConfiguration,IndexingSchedule,InputFieldMappingEntry,KeyPhraseExtractionSkill,KeyPhraseExtractionSkillLanguage,LanguageDetectionSkill,LexicalAnalyzerName,LexicalNormalizer,LexicalNormalizerName,LexicalTokenizerName,MagnitudeScoringFunction,MagnitudeScoringParameters,MappingCharFilter,MergeSkill,MicrosoftStemmingTokenizerLanguage,MicrosoftTokenizerLanguage,OcrSkill,OcrSkillLanguage,OutputFieldMappingEntry,PatternReplaceCharFilter,PhoneticEncoder,RegexFlags,ResourceCounter,ScoringFunction,ScoringFunctionAggregation,ScoringFunctionInterpolation,ScoringProfile,SearchField,SearchFieldDataType,SearchIndexerDataContainer,SearchIndexerDataSourceType,SearchIndexerError,SearchIndexerLimits,SearchIndexerSkill,SearchIndexerStatus,SearchIndexerWarning,SearchIndexStatistics,SearchServiceCounters,SearchServiceLimits,SearchServiceStatistics,SentimentSkill,SentimentSkillLanguage,ShaperSkill,SimilarityAlgorithm,SnowballTokenFilterLanguage,SoftDeleteColumnDeletionDetectionPolicy,SplitSkill,SplitSkillLanguage,SqlIntegratedChangeTrackingPolicy,StemmerTokenFilterLanguage,StopwordsList,TagScoringFunction,TagScoringParameters,TextSplitMode,TextTranslationSkill,TextTranslationSkillLanguage,TextWeights,TokenCharacterKind,TokenFilterName,VisualFeature,WebApiSkill
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
sync-methods: none
generate-client-interfaces: false
context-client-method-parameter: true
generate-client-as-impl: true
service-interface-as-public: true
required-fields-as-ctor-args: true
license-header: |-
  Copyright (c) Microsoft Corporation. All rights reserved.
  Licensed under the MIT License.
  Code generated by Microsoft (R) AutoRest Code Generator.
  Changes may cause incorrect behavior and will be lost if the code is regenerated.
```

### Set odata.metadata Accept header in operations

searchindex.json needs odata.metadata=none and searchservice.json needs odata.metadata=minimal as the Accept header.

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

``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      delete $.SearchIndex.required;
      delete $.SearchIndexer.required;
      delete $.SearchIndexerDataSource.required;
      delete $.SearchIndexerSkillset.required;
      delete $.SynonymMap.required;
```

### Renames
``` yaml $(java)
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


### Remove SearchServiceCounters's skillsetCount from being required
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.ServiceCounters.required = $.ServiceCounters.required.splice(0,6);
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

### Fix search document result answers to be an array
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.SearchDocumentsResult.properties
    transform: >
      let answers = $["@search.answers"];
      answers.type = answers.additionalProperties.type;
      answers.items = answers.additionalProperties.items;
      delete answers.additionalProperties;
```

### Fix search result captions to be an array
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.SearchResult.properties
    transform: >
      let captions = $["@search.captions"];
      captions.type = captions.additionalProperties.type;
      captions.items = captions.additionalProperties.items;
      delete captions.additionalProperties;
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

### Rename Speller to QuerySpeller
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.paths["/docs"].get.parameters
    transform: >
      $.find(p => p.name === "speller")["x-ms-enum"].name = "QuerySpeller";
```

### Rename Answers to QueryAnswer and Speller to QuerySpeller
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.Answers["x-ms-enum"].name = "QueryAnswer";
      $.Speller["x-ms-enum"].name = "QuerySpeller";
```

### Change Answers to a string in SearchOptions and SearchRequest
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.paths["/docs"].get.parameters
    transform: >
      let param = $.find(p => p.name === "answers");
      param.type = "string";
      delete param.enum;
      delete param["x-ms-enum"];
```

``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      let param = $.SearchRequest.properties.answers;
      param.type = "string";
      param.description = $.Answers.description;
      delete param["$ref"];
```
