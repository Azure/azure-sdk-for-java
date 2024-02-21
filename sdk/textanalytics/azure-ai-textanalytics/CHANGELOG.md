# Release History

## 5.4.3 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.


## 5.4.2 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 5.4.1 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.


## 5.4.0 (2023-10-19)

### Features Added
- Added new default constructor to existing models for compatible with JDK 21. [#36371](https://github.com/Azure/azure-sdk-for-java/pull/36371/files#diff-97ab179febbd379931e540173f1655dca32fb9794ff5acb90c593e08bca67939R37)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.
- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.

## 5.3.3 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.
- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.

## 5.3.2 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 5.3.1 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.
- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.


## 5.3.0 (2023-06-15)

This version of the client library defaults to the service API version 2023-04-01.

### Features Added
- Added `values()` methods to all ExpandableStringEnum models.
- Added more values to enum class: `HealthcareEntityCategory` and `HealthcareEntityRelationType`.

### Breaking Changes
> Note: The following changes are only breaking from the previous beta. They are not breaking against previous stable versions.
- Removed `Auto Language Detection`, `Dynamic Classification`, `Entity Resoluton`, and `Healthcare FHIR` features,
  which were introduced in the previous beta releases.
- Renamed class:
  `SummaryContext` to `AbstractiveSummaryContext`,
  `SummarySentence` to `ExtractiveSummarySentence`,
  `SummarySentencesOrder` to `ExtractiveSummarySentencesOrder`,
  `ExtractSummaryOptions` to `ExtractiveSummaryOptions`,
  `ExtractSummaryAction` to `ExtractiveSummaryAction`,
  `ExtractSummaryActionResult` to `ExtractiveSummaryActionResult`,
  `ExtractSummaryResult` to `ExtractiveSummaryResult`,
  `ExtractSummaryResultCollection` to `ExtractiveSummaryResultCollection`,
  `ExtractSummaryOperationDetail` to `ExtractiveSummaryOperationDetail`,
  `ExtractSummaryPagedFlux` to `ExtractiveSummaryPagedFlux`,
  `ExtractSummaryPagedIterable` to `ExtractiveSummaryPagedIterable`,
  `AbstractSummaryOptions` to `AbstractiveSummaryOptions`,
  `AbstractSummaryAction` to `AbstractiveSummaryAction`,
  `AbstractSummaryActionResult` to `AbtractiveSummaryActionResult`,
  `AbstractSummaryResultCollection` to `AbstractiveSummaryResultCollection`
  `AbstractSummaryResult` to `AbstractiveSummaryResult`,
  `AbstractSummaryOperationDetail` to `AbstractiveSummaryOperationDetail`,
  `AbstractSummaryPagedFlux` to `AbstractiveSummaryPagedFlux`,
  `AbstractSummaryPagedIterable` to `AbstractiveSummaryPagedIterable`,
- Renamed methods:
  `getExtractSummaryActions()` to `getExtractiveSummaryActions()`,
  `getAbstractSummaryActions()` to `getAbstractiveSummaryActions()`,
  `setAbstractSummaryActions(AbstractSummaryAction... abstractSummaryActions)` to 
  `setAbstractiveSummaryActions(AbstractiveSummaryAction... abstractiveSummaryActions)`,
  `setExtractSummaryActions(ExtractSummaryAction... extractSummaryActions)` to
  `setExtractiveSummaryActions(ExtractiveSummaryAction... extractiveSummaryActions)`,
  `getAbstractSummaryResults` to `getAbstractiveSummaryResults`,
  `getExtractSummaryResults` to `getExtractiveSummaryResults`

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 5.2.7 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 5.2.6 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.

## 5.2.5 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 5.3.0-beta.2 (2023-03-07)

### Features Added
- Added the following methods for performing abstractive summarization and extractive summarization actions:
    - `beginAbstractSummary(...)`
    - `beginExtractSummary(...)`
- Added the following types for the newly added methods
    - `AbstractSummaryOperationDetail`, `AbstractSummaryOptions`, `AbstractSummaryPagedFlux`, `AbstractSummaryPagedIterable`
    - `ExtractSummaryOperationDetail`, `ExtractSummaryOptions`, `ExtractSummaryPagedFlux`, `ExtractSummaryPagedIterable`

### Breaking Changes
- Changed `dynamic classify categories` as a required parameter in dynamic text classification methods.
- Renamed naming phrase `DynamicClassficationXXX` to `DynamicClassifyXXX` in class name, method name.
- Changed `BaseResolution` to an abstract class.
- Removed class `BooleanResolution` and enum value `BooleanResolution` in the class `ResolutionKind`.
- Renamed `maxSentenceCount` to `sentenceCount` in abstractive summarization.

### Other Changes
- Integrate synchronous workflow for sync clients so that they do not block on async client APIs. 
  It simplifies stack traces and improves debugging experience.

## 5.2.4 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.

## 5.2.3 (2023-01-11)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.35.0`.
- Updated `azure-core-http-netty` to `1.12.8`.

## 5.3.0-beta.1 (2022-11-18)

### Features Added
- This version of the client library marks a beta release and defaults to the service API version `2022-10-01-preview`.
- Added properties `fhirVersion` and `documentType` to `AnalyzeHealthcareEntitiesOptions` and `AnalyzeHealthcareEntitiesAction`.
- Added property `fhirBundle` to `AnalyzeHealthcareEntitiesResult`.
- Added property `confidenceScore` to `HealthcareEntityRelation`.
- Added enum `HealthcareDocumentType` and `FhirVersion`.
- Added property `resolutions` to `CategorizedEntity`.
- Added models and enums related to resolutions: `BaseResolution`, `ResolutionKind`, `AgeResolution`, `AreaResolution`,
  `BooleanResolution`, `CurrencyResolution`, `DateTimeResolution`, `InformationResolution`, `LengthResolution`,
  `NumberResolution`, `NumericRangeResolution`, `OrdinalResolution`, `SpeedResolution`, `TemperatureResolution`,
  `TemporalSpanResolution`, `VolumeResolution`, `WeightResolution`, `AgeUnit`, `AreaUnit`, `TemporalModifier`,
  `InformationUnit`, `LengthUnit`, `NumberKind`, `RangeKind`, `RelativeTo`, `SpeedUnit`, `TemperatureUnit`,
  `VolumeUnit`, and `WeightUnit`.
- Added the Extractive Summarization feature and related models: `ExtractSummaryAction`, `ExtractSummaryActionResult`,
  `ExtractSummaryResultCollection`, `ExtractSummaryResult`, `SummarySentence` and `SummarySentencesOrder`. 
  Access the feature through the `beginAnalyzeActions` API.
- Added the Abstractive Summarization feature and related models: `AbstractSummaryAction`, `AbstractSummaryActionResult`,
 `AbstractSummaryResultCollection`, `AbstractSummaryResult`, `AbstractiveSummary`, and `SummaryContext`. 
  Access the feature through the `beginAnalyzeActions` API.
- Added the dynamic text classification on documents without needing to train a model. The feature can be used by calling:
    - Synchronous API: `Response<DynamicClassifyDocumentResultCollection> dynamicClassificationBatchWithResponse(Iterable<TextDocumentInput> documents, DynamicClassificationOptions options, Context context)`
    - Asynchronous API: `Mono<Response<DynamicClassifyDocumentResultCollection>> dynamicClassificationBatchWithResponse(Iterable<TextDocumentInput> documents, DynamicClassificationOptions options)`.
    - Added new models: `ClassificationType`, `DynamicClassificationOptions` and `DynamicClassifyDocumentResultCollection`.
- Added automatic language detection to long-running operation APIs. Pass `auto` into the document `language` hint to use this feature.
- Added property `detectedLanguage` to `RecognizeEntitiesResult`, `RecognizePiiEntitiesResult`, `AnalyzeHealthcareEntitiesResult`,
  `ExtractKeyPhrasesResult`, `RecognizeLinkedEntitiesResult`, `AnalyzeSentimentResult`, `RecognizeCustomEntitiesResult`,
  `ClassifyDocumentResult`, `ExtractSummaryResult`, and `AbstractSummaryResult` to indicate the language detected by automatic language detection.
- Added property `script` to `DetectedLanguage` to indicate the script of the input document, and new enum model `ScriptKind`.

## 5.2.2 (2022-11-09)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.34.0`.
- Updated `azure-core-http-netty` to `1.12.7`.

## 5.2.1 (2022-10-12)

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.33.0`.
- Updated `azure-core-http-netty` to `1.12.6`.

## 5.2.0 (2022-09-08)

### Features Added
- This version of the client library marks a stable release and defaults to the service API version `2022-05-01`.
- Added overload methods to take only required input parameter for all existing long-running operations:
  - `beginAnalyzeActions(Iterable<String> documents, TextAnalyticsActions actions)`
  - `beginAnalyzeHealthcareEntities(Iterable<String> documents)`
  - `beginMultiLabelClassify(Iterable<String> documents, String projectName, String deploymentName)`
  - `beginRecognizeCustomEntities(Iterable<String> documents, String projectName, String deploymentName)`
  - `beginSingleLabelClassify(Iterable<String> documents, String projectName, String deploymentName)`
- Added `displayName` property which is the name of long-running operation, to the following classes to 
  set the optional display name:
    - `AnalyzeHealthcareEntitiesOptions`
    - `MultiLabelClassifyOptions`
    - `RecognizeCustomEntitiesOptions`
    - `SingleLabelClassifyOptions`
- Added `displayName` property to the following operations to read the optional display name set on options classes above:
    - `AnalyzeHealthcareEntitiesOperationDetail` from `AnalyzeHealthcareEntitiesOptions`
    - `ClassifyDocumentOperationDetail` from `MultiLabelClassifyOptions` and `SingleLabelClassifyOptions`
    - `RecognizeCustomEntitiesOperationDetail` from `RecognizeCustomEntitiesOptions`

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.32.0`.
- Updated `azure-core-http-netty` to `1.12.5`.

## 5.2.0-beta.4 (2022-08-12)
### Features Added
- We are now targeting the service's `2022-05-01` API as the default.
- Added bespoke methods for the custom entity recognition, and single-label and multi-label classification features,
  such as, `beginRecognizeCustomEntities()`, `beginSingleLabelClassify()` and `beginMultiLabelClassify()`.

### Breaking Changes
- Removed support for `Healthcare FHIR`, and `Extractive Summarization` features.
- Renamed
  `SingleCategoryClassifyAction` to `SingleLabelClassifyAction`,
  `MultiCategoryClassifyAction` to `MultiLabelClassifyAction`.
- Merged
  `SingleCategoryClassifyResultCollection` and `SingleCategoryClassifyResultCollection` to `ClassifyDocumentResultCollection`,
  `MultiCategoryClassifyResult` and `SingleCategoryClassifyResult` to `ClassifyDocumentResult`.

## 5.1.12 (2022-08-11)
### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.31.0`.
- Updated `azure-core-http-netty` to `1.12.4`.

## 5.1.11 (2022-07-07)
### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.30.0`.
- Updated `azure-core-http-netty` to `1.12.3`.

## 5.1.10 (2022-06-09)
### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.29.1`.
- Updated `azure-core-http-netty` to `1.12.2`.

## 5.2.0-beta.3 (2022-05-18)
Note that this is the first version of the client library that targets the Azure Cognitive Service for Language APIs
which includes the existing text analysis and natural language processing features found in the Text Analytics client
library. In addition, the service API has changed from semantic to date-based versioning. This version of the client 
library defaults to the latest supported API version, which currently is `2022-04-01-preview`. Support for 
`v3.2-preview.2` is removed, however, all functionalities are included in the latest version.

### Features Added
- Added interfaces from `com.azure.core.client.traits` to `TextAnalyticsClientBuilder`. 
- Added support for Healthcare Entities Analysis through the `beginAnalyzeActions` API with the `AnalyzeHealthcareEntitiesAction` type. 
- Added property `fhirVersion` to `AnalyzeHealthcareEntitiesOptions` and `AnalyzeHealthcareEntitiesAction`. 
  Use the keyword to indicate the version for the `fhirBundle` contained on the `AnalyzeHealthcareEntitiesResult`.
- Added property `fhirBundle` to `AnalyzeHealthcareEntitiesResult`.

## 5.1.9 (2022-05-11)
### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.28.0`.
- Updated `azure-core-http-netty` to `1.12.0`.

## 5.1.8 (2022-04-07)
### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.27.0`.
- Updated `azure-core-http-netty` to `1.11.9`.

## 5.1.7 (2022-03-09)
### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.26.0`.
- Updated `azure-core-http-netty` to `1.11.8`.

## 5.1.6 (2022-02-09)
### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.25.0`.
- Updated `azure-core-http-netty` to `1.11.7`.

## 5.1.5 (2022-01-14)
### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.22.0` to `1.24.1`.
- Updated `azure-core-http-netty` from `1.11.2` to `1.11.6`.

## 5.1.4 (2021-11-11)
### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.21.0` to `1.22.0`.
- Updated `azure-core-http-netty` from `1.11.1` to `1.11.2`.

## 5.2.0-beta.2 (2021-11-02)
### Feature Added
- We are now targeting the service's v3.2-preview.2 API as the default instead of v3.2-preview.1.
- Multiple of the same action type is now supported with `beginAnalyzeActions` method, and the action name for each 
  action can be set now.
- Added support for `Custom Entity Recognition` actions through the `RecognizeCustomEntitiesAction` type.
  This action can be used to get a custom entity recognition for an input document or batch of documents.
- Added support for `Custom Single Classification` actions through the `SingleCategoryClassifyAction` type.
  This action can be used to get a custom classification for an input document or batch of documents.
- Added support for `Custom Multiple Classification` actions through the `MultiCategoryClassifyAction` type.
  This action can be used to get multiple custom classifications for an input document or batch of documents.

### Breaking Changes
- Renamed methods `setSentencesOrderBy()` to `setOrderBy()` and `getSentencesOrderBy()` to `getOrderBy()`, in the
  option bag, `ExtractSummaryAction`.

## 5.1.3 (2021-10-05)
### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.20.0` to `1.21.0`.
- Updated `azure-core-http-netty` from `1.11.0` to `1.11.1`.

## 5.1.2 (2021-09-09)
### Other Changes

#### Dependency Updates
- Updated `azure-core` from `1.19.0` to `1.20.0`.
- Updated `azure-core-http-netty` from `1.10.2` to `1.11.0`.

## 5.2.0-beta.1 (2021-08-11)
### Feature Added
- We are now targeting the service's v3.2-preview.1 API as the default instead of v3.1.
- Added support for Extractive Summarization actions through the `ExtractSummaryAction` type.

## 5.1.1 (2021-08-11)
### Dependency Updates
- Updated `azure-core` from `1.18.0` to `1.19.0`.
- Updated `azure-core-http-netty` from `1.10.1` to `1.10.2`.

### Bugs Fixed
- Fixed the bug to support the default value `disableServiceLogs = true`, in the option bags, `AnalyzeHealthcareEntitiesOptions`,
  `RecognizePiiEntitiesOptions` and `RecognizePiiEntitiesAction`,
- Using UTF-16 code unit as the default encoding in the `Sentiment Analysis` and `Linked Entities Recognition` actions.

## 5.1.0 (2021-07-08)
### Feature Added
- We are now targeting the service's v3.1 API as the default instead of v3.1-preview.4.
- Added a new class, `HealthcareEntityCategory` to replace the `String` type of property `category` in the `HealthcareEntity`.
- Added the new types, `ExtractKeyPhrasesAction`, `RecognizeEntitiesAction`, `RecognizePiiEntitiesAction`,
  `RecognizeLinkedEntitiesAction`, and `AnalyzeSentimentAction`.
- Added new customized `***PagedFlux`, `***PagedIterable` types, `AnalyzeActionsResultPagedFlux`,
  `AnalyzeActionsResultPagedIterable`, `AnalyzeHealthcareEntitiesPagedFlux`, and `AnalyzeHealthcareEntitiesPagedIterable`.
- `beginAnalyzeHealthcareEntities` now works with Azure Active Directory credentials.

### Breaking Changes
- Changed behavior in `beginAnalyzeActions` API where now accepts up to one action only per action type. 
  An `IllegalArgumentException` is raised if multiple actions of the same type are passed.
- Replaced
  `AnalyzeActionsResultPagedFlux` to `PagedFlux<AnalyzeActionsResult>`,
  `AnalyzeActionsResultPagedIterable` to `PagedIterable<AnalyzeActionsResult>`,
  `AnalyzeHealthcareEntitiesPagedFlux` to `PagedFlux<AnalyzeHealthcareEntitiesResultCollection>`,
  `AnalyzeHealthcareEntitiesPagedIterable` to `PagedIterable<AnalyzeHealthcareEntitiesResultCollection>`.
- Deprecated `analyzeSentimentBatch***` APIs with type `TextAnalyticsRequestOptions` option bag below. The same 
  functionalities can be done in the APIs with `AnalyzeSentimentOptions` instead:
  `AnalyzeSentimentResultCollection analyzeSentimentBatch(Iterable<String> documents, String language, TextAnalyticsRequestOptions options)`,
  `Response<AnalyzeSentimentResultCollection> analyzeSentimentBatchWithResponse(Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options, Context context)`,
  `Mono<Response<AnalyzeSentimentResultCollection>> analyzeSentimentBatchWithResponse(Iterable<TextDocumentInput> documents, TextAnalyticsRequestOptions options)`,
  `Mono<AnalyzeSentimentResultCollection> analyzeSentimentBatch(Iterable<String> documents, String language, TextAnalyticsRequestOptions options)`
- Removed `StringIndexType`. This SDK will keep using UTF-16 code unit as the default encoding.
- Removed type `ExtractKeyPhrasesOptions`, `RecognizeEntitiesOptions`, `RecognizeLinkedEntitiesOptions` and respective exposures.
- Removed the property `statistics` from `AnalyzeActionsResult` as it is not currently returned by the service even if 
  the user passes `includeStatistics` = `true`.
- Removed constructors, but to use the private setter accessor to assign the additional properties:
    `CategorizedEntity(String text, EntityCategory category, String subcategory, double confidenceScore, int offset)`,
    `LinkedEntity(String name, IterableStream<LinkedEntityMatch> matches, String languages, String dataSourceEntityId, String url, String dataSource, String bingEntitySearchApiId)`,
    `LinkedEntityMatch(String text, double confidenceScore, int offset)`
- Renamed type `PiiEntityDomainType` to `PiiEntityDomain`.
- Renamed `AnalyzeActionResult`'s property `recognizeEntitiesActionResults` to `recognizeEntitiesResults` which dropped the keyword `Action`.
  This change applied to all the other `***ActionResults` properties as well.
- Renamed property name `result` to `documentsResults` in `AnalyzeSentimentActionResult`, `ExtractKeyPhrasesActionResult`,
  `RecognizeEntitiesActionResult`, `RecognizeLinkedEntitiesActionResult`, and `RecognizePiiEntitiesActionResult`. 
- Renamed the enum values in `PiiEntityCategory` by separating words with the underscore character.
- Renamed the methods in `AnalyzeActionsOperationDetail`,
  `getActionsFailed()` to `getFailedCount()`,
  `getActionsInProgress()` to `getInProgressCount()`,
  `getActionsInTotal()` to `getTotalCount()`,
  `getActionsSucceeded()` to `getSucceededCount()`.
- `TextAnalyticsActions` now takes `***Action` types, instead of `***Options` types. Renamed The getter and setter method names
  based on the new type names. Replacing types show as follows:
  - `ExtractKeyPhrasesOption` changed to new type `ExtractKeyPhrasesAction`.
  - `RecognizeEntitiesOption` changed to new type `RecognizeEntitiesAction`.
  - `RecognizePiiEntitiesOption` changed to new type `RecognizePiiEntitiesAction`.
  - `RecognizeLinkedEntitiesOption` changed to new type `RecognizeLinkedEntitiesAction`.
  - `AnalyzeSentimentOption` changed to new type `AnalyzeSentimentAction`.
- Changed enum types `EntityCertainty` and `EntityConditionality` to `ExpandableStringEnum` types.

## 5.1.0-beta.7 (2021-05-19)
### Features Added
- Added property `disableServiceLogs` to all endpoints' options bag
- Added support for `Sentiment Analysis` as an action type for the `beginAnalyzeActions` API.
  
### Breaking Changes
- We are now targeting the service's v3.1-preview.5 API as the default instead of v3.1-preview.4.
- Removed `batch` keyword from the model names, `AnalyzeBatchActionsResult`, `AnalyzeBatchActionsOperationDetail`, 
  `AnalyzeBatchActionOptions` and the related method names, such as renamed `beginAnalyzeBatchActions` to `beginAnalyzeActions`.
- Renamed the static final String type `TEXT_ELEMENTS_V8` to `TEXT_ELEMENT_V8` in the `StringIndexType` class.

### Key Bug Fixed
- Fixed `NullPointerException` for passing value `null` to options tasks in the `TextAnalyticsActions`.

## 5.0.6 (2021-05-13)
### Dependency Updates
- Updated `azure-core` from `1.15.0` to `1.16.0`.
- Updated `azure-core-http-netty` from `1.9.1` to `1.9.2`.
- Updated `azure-core-serializer-json-jackson` from `1.2.2` to `1.2.3`.

## 5.1.0-beta.6 (2021-04-06)
### Breaking Changes
- Removed the input parameter `Context` from non-max-overload healthcare synchronous API, `beginAnalyzeHealthcareEntities()`.

## 5.0.5 (2021-04-06)
### Dependency Updates
- Update dependency version, `azure-core` to 1.15.0 and `azure-core-http-netty` to 1.9.1.

## 5.1.0-beta.5 (2021-03-10)
- We are now targeting the service's v3.1-preview.4 API as the default instead of v3.1-preview.3.

### Features Added
- Added a new property `categoriesFilter` to `RecognizePiiEntitiesOptions`. The PII entity recognition endpoint will return 
  the result with categories only match the given `categoriesFilter` list. 
- Added `normalizedText` property to `HealthcareEntity`.
- `AnalyzeHealthcareEntitiesResult` now exposes the property `entityRelations`, which is a list of `HealthcareEntityRelation`.
- Added `HealthcareEntityRelation` class which will determine all the different relations between the entities as `Roles`.
- Added `HealthcareEntityRelationRole`, which exposes `name` and `entity` of type `String` and `HealthcareEntity` respectively.
- `beginAnalyzeBatchActions` can now process recognize linked entities actions.
- `recognizePiiEntities` takes a new option, `categoriesFilter`, that specifies a list of PII categories to return.
- Added new classes, `RecognizeLinkedEntitiesActionResult`, `PiiEntityCategory`.

### Breaking Changes
- Removed `PiiEntity` constructor and `PiiEntity`'s `category` property is no longer a type of `EntityCategory` but use a new introduced type `PiiEntityCategory`.
- Replace `isNegated` by `HealthcareEntityAssertion` to `HealthcareEntity` which further exposes `EntityAssociation`, `EntityCertainity` and `EntityConditionality`.
- Renamed classes,
  `AspectSentiment` to `TargetSentiment`, `OpinionSentiment` to `AssesssmentSentiment`, `MinedOpinion` to `SentenceOpinion`.
- Renamed
  `SentenceSentiment`'s method, `getMinedOpinions()` to `getOpinions()`.
  `MinedOpinion`'s methods, `getAspect()` to `getTarget()`, `getOpinions()` to `getAssessments()`.
- Removed property, `relatedEntities` from `HealthcareEntity`.
- Removed constructors, 
  `SentenceSentiment(String text, TextSentiment sentiment, SentimentConfidenceScores confidenceScores, IterableStream<MinedOpinion> minedOpinions, int offset)`,
  `AspectSentiment(String text, TextSentiment sentiment, int offset, SentimentConfidenceScores confidenceScores)`,
  `OpinionSentiment(String text, TextSentiment sentiment, int offset, boolean isNegated, SentimentConfidenceScores confidenceScores)`

### Known Issues
- `beginAnalyzeHealthcareEntities` is currently in gated preview and can not be used with AAD credentials. 
  For more information, see [the Text Analytics for Health documentation](https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-for-health?tabs=ner#request-access-to-the-public-preview).

## 5.0.4 (2021-03-09)
### Dependency Updates
- Update dependency version, `azure-core` to 1.14.0 and `azure-core-http-netty` to 1.9.0.
  
## 5.1.0-beta.4 (2021-02-10)
### Features Added
- Added new classes, `StringIndexType`, `RecognizeEntitiesOptions`, `RecognizeLinkedEntitiesOptions`.
- A new options to control how the offset and length are calculated by the service. Added `StringIndexType` to all
  `AnalyzeSentimentOptions`, `RecognizeEntitiesOptions`, `RecognizeLinkedEntitiesOptions`, `RecognizePiiEntitiesOptions`
  and the default is `UTF16CODE_UNIT` if null value is assigned. For more information, 
  see [the Text Analytics documentation](https://docs.microsoft.com/azure/cognitive-services/text-analytics/concepts/text-offsets#offsets-in-api-version-31-preview).
- Added property `length` to `CategorizedEntity`, `SentenceSentiment`, `LinkedEntityMatch`, `AspectSentiment`, 
  `OpinionSentiment`, and `PiiEntity`.
- Added new API,
  `Mono<Response<RecognizeEntitiesResultCollection>> recognizeEntitiesBatchWithResponse(
  Iterable<TextDocumentInput> documents, RecognizeEntitiesOptions options)`,
  `Response<RecognizeEntitiesResultCollection> recognizeEntitiesBatchWithResponse(
  Iterable<TextDocumentInput> documents, RecognizeEntitiesOptions options, Context context)`,
  `Mono<Response<RecognizeLinkedEntitiesResultCollection>> recognizeLinkedEntitiesBatchWithResponse(
  Iterable<TextDocumentInput> documents, RecognizeLinkedEntitiesOptions options)`,
  `Response<RecognizeLinkedEntitiesResultCollection> recognizeLinkedEntitiesBatchWithResponse(
  Iterable<TextDocumentInput> documents, RecognizeLinkedEntitiesOptions options, Context context)`
  
### Breaking Changes
#### Analysis healthcare entities 
- The healthcare entities returned by `beginAnalyzeHealthcareEntities` are now organized as a directed graph where the 
  edges represent a certain type of healthcare relationship between the source and target entities. Edges are stored
  in the `relatedEntities` property.
- The `links` property of `HealthcareEntity` is renamed to `dataSources`, a list of objects representing medical 
  databases, where each object has `name` and `entityId` properties.
- Replace API 
  `PollerFlux<TextAnalyticsOperationResult, PagedFlux<HealthcareTaskResult>> beginAnalyzeHealthcare(Iterable<TextDocumentInput> documents, RecognizeHealthcareEntityOptions options)` to
  `PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, PagedFlux<AnalyzeHealthcareEntitiesResultCollection>> beginAnalyzeHealthcareEntities(Iterable<TextDocumentInput> documents, AnalyzeHealthcareEntitiesOptions options)`,
  `SyncPoller<TextAnalyticsOperationResult, PagedIterable<HealthcareTaskResult>> beginAnalyzeHealthcare(Iterable<TextDocumentInput> documents, RecognizeHealthcareEntityOptions options, Context context)` to
  `SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, PagedIterable<AnalyzeHealthcareEntitiesResultCollection>> beginAnalyzeHealthcareEntities(Iterable<TextDocumentInput> documents, AnalyzeHealthcareEntitiesOptions options, Context context)`
- New overload APIs,
  `PollerFlux<AnalyzeHealthcareEntitiesOperationDetail, PagedFlux<AnalyzeHealthcareEntitiesResultCollection>> beginAnalyzeHealthcareEntities(Iterable<String> documents, String language, AnalyzeHealthcareEntitiesOptions options)`,
  `SyncPoller<AnalyzeHealthcareEntitiesOperationDetail, PagedIterable<AnalyzeHealthcareEntitiesResultCollection>> beginAnalyzeHealthcareEntities(Iterable<String> documents, String language, AnalyzeHealthcareEntitiesOptions options, Context context)`
- Added `AnalyzeHealthcareEntitiesResultCollection`, `AnalyzeHealthcareEntitiesResult`, `HealthcareEntityRelationType`
- Removed `HealthcareTaskResult`, `HealthcareEntityRelation`, `HealthcareEntityCollection`, `JobMetadata`, `JobState`
- Renamed
  `HealthcareEntityLink` to `EntityDataSource`,
  `RecognizeHealthcareEntityOptions` to `AnalyzeHealthcareEntitiesOptions`,
  `RecognizeHealthcareEntitiesResult` to `AnalyzeHealthcareEntitiesResult`,
  `RecognizeHealthcareEntitiesResultCollection` to `AnalyzeHealthcareEntitiesResultCollection`
  `TextAnalyticsOperationResult` to `AnalyzeHealthcareEntitiesOperationDetail`
  
#### Analyze multiple actions
- The word "action" are used consistently in our names and documentation instead of "task".
- Replace API 
  `PollerFlux<TextAnalyticsOperationResult, PagedFlux<AnalyzeTasksResult>> beginAnalyzeTasks(Iterable<TextDocumentInput> documents, AnalyzeTasksOptions options)`to 
  `PollerFlux<AnalyzeBatchActionsOperationDetail, PagedFlux<AnalyzeBatchActionsResult>> beginAnalyzeBatchActions(Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeBatchActionsOptions options)`,
  `SyncPoller<TextAnalyticsOperationResult, PagedIterable<AnalyzeTasksResult>> beginAnalyzeTasks(Iterable<TextDocumentInput> documents, AnalyzeTasksOptions options, Context context)`to
  `SyncPoller<AnalyzeBatchActionsOperationDetail, PagedIterable<AnalyzeBatchActionsResult>> beginAnalyzeBatchActions(Iterable<TextDocumentInput> documents, TextAnalyticsActions actions, AnalyzeBatchActionsOptions options, Context context)`
- Added new overload APIs, 
  `PollerFlux<AnalyzeBatchActionsOperationDetail, PagedFlux<AnalyzeBatchActionsResult>> beginAnalyzeBatchActions(Iterable<String> documents, TextAnalyticsActions actions, String language, AnalyzeBatchActionsOptions options)`,
  `SyncPoller<AnalyzeBatchActionsOperationDetail, PagedIterable<AnalyzeBatchActionsResult>> beginAnalyzeBatchActions(Iterable<String> documents, TextAnalyticsActions actions, String language, AnalyzeBatchActionsOptions options)`
- Added `ExtractKeyPhrasesActionResult`, `RecognizeEntitiesActionResult`, `RecognizePiiEntitiesActionResult`,
  `TextAnalyticsActions`, `TextAnalyticsActionResult`
- Removed `EntitiesTask`, `KeyPhrasesTask`, `PiiTask`, `TextAnalyticsErrorInformation`
- Renamed
  `AnalyzeTasksOptions` to `AnalyzeBatchActionsOptions`,
  `AnalyzeTasksResult` to `AnalyzeBatchActionsResult`,
  `EntitiesTaskParameters` to `RecognizeEntitiesOptions`
  `KeyPhrasesTaskParameters` to `ExtractKeyPhrasesOptions`,
  `PiiTaskParameters` to `RecognizePiiEntityOptions`,
  `PiiEntityDomainType` to `PiiEntitiesDomainType`,
  `RecognizePiiEntityOptions` to `RecognizePiiEntitiesOptions`,
  `TextAnalyticsOperationResult` to `AnalyzeBatchActionsOperationDetail`

## 5.0.3 (2021-02-10)
### Dependency Updates
- Update dependency version, `azure-core` to 1.13.0 and `azure-core-http-netty` to 1.8.0.

## 5.0.2 (2021-01-14)
### Dependency Updates
- Update dependency version, `azure-core` to 1.12.0 and `azure-core-http-netty` to 1.7.1.

## 5.1.0-beta.3 (2020-11-19)
### Features Added
- Added support for healthcare recognition feature. It is represented as a long-running operation. Cancellation supported. 
- Added support for analyze tasks feature, It analyzes multiple tasks (such as, entity recognition, PII entity recognition 
and key phrases extraction) simultaneously in a list of document.
- Currently, Azure Active Directory (AAD) is not supported in the Healthcare recognition feature. For more information, see
[here](https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-for-health?tabs=ner#request-access-to-the-public-preview).
- Both new features listed above are available in `West US2`, `East US2`, `Central US`, `North Europe` and `West Europe` 
regions and in Standard tier.

## 5.0.1 (2020-11-12)
### Dependency Updates 
- Update dependency version, `azure-core` to 1.10.0 and `azure-core-http-netty` to 1.6.3. 

## 5.1.0-beta.2 (2020-10-06)
### Breaking Changes 
- Removed property `length` from `CategorizedEntity`, `SentenceSentiment`, `LinkedEntityMatch`, `AspectSentiment`,  
`OpinionSentiment`, and `PiiEntity` because the length information can be accessed from the text property itself 
using the string's length property. 
 
### Dependency Updates 
- Update dependency version, `azure-core` to 1.9.0 and `azure-core-http-netty` to 1.6.2. 
 
## 5.1.0-beta.1 (2020-09-17)
### Features Added
- Added `offset` and `length` properties for `CategorizedEntity`, `LinkedEntityMatch` and `SentenceSentiment` 
- `length` is the number of characters in the text of these models 
- `offset` is the offset of the text from the start of the document
- Updated Text Analytics SDK's default service API version to `v3.1-preview.2` from `v3.0`. 
- Added support for Personally Identifiable Information(PII) entity recognition feature. 
  To use this feature, you need to make sure you are using the service's v3.1-preview.1 API. 
- Added support for the Opinion Mining feature. To use this feature, you need to make sure you are using the  
service's v3.1-preview.1 and above API. To get this support pass `includeOpinionMining` as `true` in  
`AnalyzeSentimentOptions` when calling the sentiment analysis endpoints. 
- Add property `bingEntitySearchApiId` to the `LinkedEntity` class. This property is only available for v3.1-preview.2 
and up, and it is to be used in conjunction with the Bing Entity Search API to fetch additional relevant information 
about the returned entity. 

## 5.0.0 (2020-07-27)
- Re-release of version `1.0.1` with updated version `5.0.0`.

## 1.0.1 (2020-07-07)
- Update dependency version, `azure-core` to 1.6.0 and `azure-core-http-netty` to 1.5.3.

## 1.0.0 (2020-06-09)
- First stable release of `azure-ai-textanalytics`.

## 1.0.0-beta.5 (2020-05-27)
### Features Added
- Added Text property and `getText()` to `SentenceSentiment`.
- `Warnings` property added to each document-level response object returned from the endpoints. It is a list of `TextAnalyticsWarnings`.
- Added `CategorizedEntityCollection`, `KeyPhrasesCollection`, `LinkedEntityCollection` for having `getWarnings()` to retrieve warnings. 
- Added a new enum value `ADDRESS` to `EntityCategory`.
- Text analytics SDK update the service to version `v3.0` from `v3.0-preview.1`.

### Breaking Changes
- Removed pagination feature, which removed `TextAnalyticsPagedIterable`, `TextAnalyticsPagedFlux` and `TextAnalyticsPagedResponse`
- Removed overload methods for API that takes a list of String, only keep max-overload API that has a list of String, language or country hint, and `TextAnalyticsRequestOption`.
- Renamed `apiKey()` to `credential()` on TextAnalyticsClientBuilder.
- Removed `getGraphemeLength()` and `getGraphemeOffset()` from `CategorizedEntity`, `SentenceSentiment`, and `LinkedEntityMatch`.
- `getGraphemeCount()` in `TextDocumentStatistics` has been renamed to `getCharacterCount()`.
- `getScore()` in `DetectedLanguage` has been renamed to `getConfidenceScore()`.
- `getSubCategory()` in `CategorizedEntity` has been renamed to `getSubcategory()`.
- `getLinkedEntityMatches()` in `LinkedEntity` has been renamed to `getMatches()`.
- `getCode()` in `TextAnalyticsException` and `TextAnalyticsError` has been renamed to `getErrorCode()`.
- `getCode()` in `TextAnalyticsWarning` has been renamed to `getWarningCode()`.
- Async client returns errors, mono error or flux error but no longer throws exception. Sync client throws exceptions only.
- Deprecated `TextDocumentInput(String id, String text, String language)` constructor, but added `setLanguage()` setter since `language` is optional.
- Renamed `RecognizeCategorizedEntitiesResult` to `RecognizeEntitiesResult`.
- Renamed `DocumentResult` to `TextAnalyticsResult`.
- Removed `getServiceVersion()` from both synchronous and asynchronous clients.
- Replaced all single input asynchronous APIs, e.x., 
  - `TextAnalyticsPagedFlux<CategorizedEntity> recognizeEntities(String document)` to `Mono<CategorizedEntityCollection> recognizeEntities(String document)`.
  - `TextAnalyticsPagedFlux<LinkedEntity> recognizeLinkedEntities(String document)` to `Mono<LinkedEntityCollection> recognizeLinkedEntities(String document)`.
  - `TextAnalyticsPagedFlux<String> extractKeyPhrases(String document)` to `Mono<KeyPhrasesCollection> extractKeyPhrases(String document)`.
- Replaced all single input synchronous APIs, e.x., 
  - `TextAnalyticsPagedIterable<CategorizedEntity> recognizeEntities(String document)` to `CategorizedEntityCollection recognizeEntities(String document)`.
  - `TextAnalyticsPagedIterable<LinkedEntity> recognizeLinkedEntities(String document)` to `LinkedEntityCollection recognizeLinkedEntities(String document)`.
  - `TextAnalyticsPagedIterable<String> extractKeyPhrases(String document)` to `KeyPhrasesCollection extractKeyPhrases(String document)`.
  
## 1.0.0-beta.4 (2020-04-07)
- Throws an illegal argument exception when the given list of documents is an empty list.

### Breaking Changes
- Renamed all input parameters `text` to `document`, and `inputTexts` to `documents`.
- Removed all PII endpoints and update with related changes, such as remove related models, samples, codesnippets, docstrings, etc from this library. 
- Replaced `TextAnalyticsApiKeyCredential` with `AzureKeyCredential`.

## 1.0.0-beta.3 (2020-03-10)
### Features Added
- Introduced `TextAnalyticsPagedFlux`, `TextAnalyticsPagedIterable`, and `TextAnalyticsPagedResponse` type. Moved `modelVersion` amd `TextDocumentBatchStatistics` into `TextAnalyticsPagedResponse`. All collection APIs are return `TextAnalyticsPagedFlux` and `TextAnalyticsPagedIterable` in the asynchronous and synchronous client, respectively. So `DocumentResultCollection` is no longer required. Most of existing API surface are changes. Please check up `TextAnalyticsAsyncClient` and `TextAnalyticsClient` for more detail.
- Introduced `EntityCategory` class to support major entity categories that the service supported.
- Added `getDefaultCountryHint()`, `getDefaultLanguage()` and `getServiceVersion()` to `TextAnalyticsClient`

### Breaking Changes
- Supported `Iterable<T>` instead of `List<T>` text inputs.
- Default language and country hint can only be assigned value when building a Text Analytics client.
- Renamed `showStatistics()` to `isIncludeStatistics()` in the `TextAnalyticsRequestOptions`.
- Renamed `getErrorCodeValue()` to `getCode()` in the `TextAnalyticsException`.
- Renamed `getOffset()`, `getLength()` and `getScore()` to `getGraphemeOffset()`, `getGraphemeLength` and `getConfidenceScore()`in `CategorizedEntity`, `LinkedEntityMatch`, `PiiEntity`.
- Renamed `SentimentLabel` to `TextSentiment` class.
- Renamed `SentimentScorePerLabel` to `SentimentConfidenceScores` class.
- Renamed `getCharacterCount()` to `getGraphemeCount()` in the `TextDocumentStatistics`.
- Removed `InnerError`, `DocumentResultCollection` and `TextAnalyticsClientOptions` class.

## 1.0.0-beta.2 (2020-02-12)

### Breaking Changes

- The single text, module-level operations return an atomic type of the operation result. For example, `detectLanguage(String text)` returns a `DetectedLanguage` rather than a `DetectLanguageResult`.

  For other module-level operations,
    
  `recognizeEntities(String text)`, it no longer returns type of `Mono<RecognizeEntitiesResult>` but `PagedFlux<CategorizedEntity>` in asynchronous API and `PagedIterable<CategorizedEntity>` in synchronous API.
  
  `recognizePiiEntities(String text)`, it no longer returns type of `Mono<RecognizePiiEntitiesResult>` but `PagedFlux<PiiEntity>` in asynchronous API and `PagedIterable<PiiEntity>` in synchronous API.
  
  `recognizeLinkedEntities(String text)`, it no longer returns type of `Mono<RecognizeLinkedEntitiesResult>` but `PagedFlux<LinkedEntity>` in asynchronous API and `PagedIterable<LinkedEntity>` in synchronous API.
  
  `extractKeyPhrases(String text)`, it no longer returns type of `Mono<ExtractKeyPhraseResult>` but `PagedFlux<String>` in asynchronous API and `PagedIterable<String>` in synchronous API.
  
  `analyzeSentiment(String text)`, it no longer returns type of `Mono<AnalyzeSentimentResult>` but `Mono<DocumentSentiment>` in asynchronous API and `DocumentSentiment` in synchronous API.
  
  `recognizeEntitiesWithResponse(String text, String language)` changed to `recognizeEntities(String text, String language)` and return `PagedFlux<CategorizedEntity>` in asynchronous API and `PagedIterable<CategorizedEntity>` in synchronous API as a collection of atomic type `CategorizedEntity`.
  
  `recognizePiiEntitiesWithResponse(String text, String language)` changed to `recognizePiiEntities(String text, String language)` and return `PagedFlux<PiiEntity>` in asynchronous API and `PagedIterable<PiiEntity>` in synchronous API as a collection of atomic type `PiiEntity`.
  
  `recognizeLinkedEntitiesWithResponse(String text, String language)` changed to `recognizeLinkedEntities(String text, String language)` and return `PagedFlux<LinkedEntity>` in asynchronous API and `PagedIterable<LinkedEntity>` in synchronous API as a collection of atomic type `LinkedEntity`.
  
  `extractKeyPhrasesWithResponse(String text, String language)` changed to `extractKeyPhrases(String text, String language)` and return `PagedFlux<String>` in asynchronous API and `PagedIterable<String>` in synchronous API as a collection of atomic type `String`.
  
  `analyzeSentimentWithResponse(String text, String language)` return `Mono<Response<DocumentSentiment>>` in asynchronous API and `Response<DocumentSentiment>` in synchronous API with an atomic type `DocumentSentiment`.

- Removed `TextSentiment` class but created `DocumentSentiment` and `SentenceSentiment` instead. `DocumentSentiment` includes a list of `SentenceSentiment`.
- Added a new class model `SentimentScorePerLabel` for the scores of sentiment label.  
- Added a new parameter `TextAnalyticsRequestOptions options` to method overloads accepting a list of text inputs for allowing the users to opt for batch operation statistics.
- Passing the API key as a string is no longer supported. To use subscription key authentication a new credential class `TextAnalyticsApiKeyCredential("<api_key>")` must be passed in for the `credential` parameter.
- `detectLanguages()` is renamed to `detectLanguage()`.
- The `TextAnalyticsError` model has been simplified to an object with only attributes `code`, `message`, and `target`.
- `RecognizePiiEntitiesResult` now contains on the object a list of `PiiEntity` instead of `NamedEntity`.
- `DetectLanguageResult` no longer has `List<DetectedLanguage> detectedLanguages`. Use `getPrimaryLanguage()` to access the detected language in text.
- `AnalyzeSentimentResult` no longer takes `List<TextSentiment> sentenceSentiments` and removed `getSentenceSentiments()`.
- `NamedEntity` has been renamed to `CategorizedEntity` and its attributes `type` to `category` and `subtype` to `subcategory`.
- Renamed `SentimentClass` to `SentimentLabel`.
- `getLinkedEntities()` to `getEntities()` and variable `linkedEntities` to `entities`.
- Added suffix of `batch` to all operations' method name that takes a collection of input.

### Features Added
- Credential class `TextAnalyticsApiKeyCredential` provides an `updateCredential()` method which allows you to update the API key for long-lived clients.

### Breaking Changes
- If you try to access a result attribute on a `DocumentError` object, a `TextAnalyticsException` is raised with a custom error message that provides the document ID and error of the invalid document.

## 1.0.0-beta.1 (2020-01-09)
Version 1.0.0-beta.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

- It uses the Text Analytics service `v3.0-preview.1` API.
- New namespace/package name:
    - The namespace/package name for Azure Text Analytics client library has changed from 
    `com.microsoft.azure.cognitiveservices.language.textanalytics` to `com.azure.ai.textanalytics`
- Added support for:
  - Subscription key and AAD authentication for both synchronous and asynchronous clients.
  - Language detection.
  - Entity recognition.
  - Entity linking recognition.
  - Personally identifiable information entities recognition.
  - Key phrases extraction.
  - Analyze sentiment APIs including analysis for mixed sentiment.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-textanalytics_1.0.0-beta.1/sdk/textanalytics/azure-ai-textanalytics/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-ai-textanalytics_1.0.0-beta.1/sdk/textanalytics/azure-ai-textanalytics/src/samples) 
demonstrate the new API.
