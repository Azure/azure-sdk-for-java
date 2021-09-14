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
git checkout main
git submodule update --init --recursive
mvn package -Dlocal -DskipTests
npm install
npm install -g autorest
```

### Generation

There are two swaggers for Azure Search, `searchindex` and `searchservice`. They always under same package version, e.g. 
`--tag=package-2021-04-30-Preview-searchindex` and `--tag=package-2021-04-30-Preview-searchservice`.

```ps
cd <swagger-folder>
autorest --use=C:/work/autorest.java
```

e.g.
```ps
cd <swagger-folder>
autorest --use=C:/work/autorest.java --tag=package-2021-04-30-Preview-searchindex
autorest --use=C:/work/autorest.java --tag=package-2021-04-30-Preview-searchservice
```
## Configuration

### Basic Information 
These are the global settings for SearchServiceClient and SearchIndexClient.

``` yaml
opt-in-extensible-enums: true
openapi-type: data-plane
```

### Tag: package-2021-04-30-Preview-searchindex

These settings apply only when `--tag=package-2021-04-30-Preview-searchindex` is specified on the command line.

``` yaml $(tag) == 'package-2021-04-30-Preview-searchindex'
namespace: com.azure.search.documents
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/c99fbb96d7993daec8135a40681d9d807e3f5751/specification/search/data-plane/Azure.Search/preview/2021-04-30-Preview/searchindex.json
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: AnswerResult,AutocompleteItem,AutocompleteMode,AutocompleteOptions,AutocompleteResult,CaptionResult,FacetResult,IndexActionType,QueryAnswer,QueryCaption,QueryLanguage,QuerySpeller,QueryType,ScoringStatistics,SearchMode,SuggestOptions
customization-class: src/main/java/SearchIndexCustomizations.java
```

### Tag: package-2021-04-30-Preview-searchservice

These settings apply only when `--tag=package-2021-04-30-Preview-searchservice` is specified on the command line.

``` yaml $(tag) == 'package-2021-04-30-Preview-searchservice'
namespace: com.azure.search.documents.indexes
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/c99fbb96d7993daec8135a40681d9d807e3f5751/specification/search/data-plane/Azure.Search/preview/2021-04-30-Preview/searchservice.json
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: AnalyzedTokenInfo,BlobIndexerDataToExtract,BlobIndexerImageAction,BlobIndexerPdfTextRotationAlgorithm,BlobIndexerParsingMode,BM25SimilarityAlgorithm,CharFilter,CharFilterName,CjkBigramTokenFilterScripts,ClassicSimilarityAlgorithm,CognitiveServicesAccount,CognitiveServicesAccountKey,ConditionalSkill,CorsOptions,CustomAnalyzer,CustomEntity,CustomEntityAlias,CustomEntityLookupSkill,CustomEntityLookupSkillLanguage,CustomNormalizer,DataChangeDetectionPolicy,DataDeletionDetectionPolicy,DefaultCognitiveServicesAccount,DistanceScoringFunction,DistanceScoringParameters,DocumentExtractionSkill,EdgeNGramTokenFilterSide,EntityCategory,EntityLinkingSkill,EntityRecognitionSkill,EntityRecognitionSkillLanguage,FieldMapping,FieldMappingFunction,FreshnessScoringFunction,FreshnessScoringParameters,HighWaterMarkChangeDetectionPolicy,ImageAnalysisSkill,ImageAnalysisSkillLanguage,ImageDetail,IndexerExecutionEnvironment,IndexerExecutionResult,IndexerExecutionStatus,IndexerStatus,IndexingParametersConfiguration,IndexingSchedule,InputFieldMappingEntry,KeyPhraseExtractionSkill,KeyPhraseExtractionSkillLanguage,LanguageDetectionSkill,LexicalAnalyzer,LexicalAnalyzerName,LexicalNormalizer,LexicalNormalizerName,LexicalTokenizerName,LineEnding,LuceneStandardAnalyzer,MagnitudeScoringFunction,MagnitudeScoringParameters,MappingCharFilter,MergeSkill,MicrosoftStemmingTokenizerLanguage,MicrosoftTokenizerLanguage,OcrSkill,OcrSkillLanguage,OutputFieldMappingEntry,PatternAnalyzer,PatternReplaceCharFilter,PiiDetectionSkill,PiiDetectionSkillMaskingMode,PhoneticEncoder,RegexFlags,ResourceCounter,ScoringFunction,ScoringFunctionAggregation,ScoringFunctionInterpolation,ScoringProfile,SearchField,SearchFieldDataType,SearchIndexerCache,SearchIndexerDataContainer,SearchIndexerDataIdentity,SearchIndexerDataNoneIdentity,SearchIndexerDataSourceType,SearchIndexerDataUserAssignedIdentity,SearchIndexerError,SearchIndexerKnowledgeStore,SearchIndexerKnowledgeStoreBlobProjectionSelector,SearchIndexerKnowledgeStoreFileProjectionSelector,SearchIndexerKnowledgeStoreObjectProjectionSelector,SearchIndexerKnowledgeStoreProjection,SearchIndexerKnowledgeStoreProjectionSelector,SearchIndexerKnowledgeStoreTableProjectionSelector,SearchIndexerLimits,SearchIndexerSkill,SearchIndexerSkillset,SearchIndexerStatus,SearchIndexerWarning,SearchIndexStatistics,SearchResourceEncryptionKey,SearchServiceCounters,SearchServiceLimits,SearchServiceStatistics,SearchSuggester,SentimentSkill,SentimentSkillLanguage,ShaperSkill,SimilarityAlgorithm,SnowballTokenFilterLanguage,SoftDeleteColumnDeletionDetectionPolicy,SplitSkill,SplitSkillLanguage,SqlIntegratedChangeTrackingPolicy,StemmerTokenFilterLanguage,StopAnalyzer,StopwordsList,SynonymMap,TagScoringFunction,TagScoringParameters,TextSplitMode,TextTranslationSkill,TextTranslationSkillLanguage,TextWeights,TokenCharacterKind,TokenFilterName,VisualFeature,WebApiSkill
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
require-x-ms-flattened-to-flatten: true
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

``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      delete $.SearchIndex.required;
      delete $.SearchIndexer.required;
      delete $.SearchIndexerDataSource.required;
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

### Rename Speller to QuerySpeller
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.paths["/docs"].get.parameters
    transform: >
      $.find(p => p.name === "speller")["x-ms-enum"].name = "QuerySpeller";
```

### Rename Answers to QueryAnswer, Captions to QueryCaption, and Speller to QuerySpeller
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.Answers["x-ms-enum"].name = "QueryAnswer";
      $.Captions["x-ms-enum"].name = "QueryCaption";
      $.Speller["x-ms-enum"].name = "QuerySpeller";
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

``` yaml $(java)
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