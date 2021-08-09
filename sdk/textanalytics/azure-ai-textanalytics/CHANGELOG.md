# Release History
## 5.1.1 (2021-08-10)
### Bugs Fixed
- Fixed the bug to support the default value `disableServiceLogs = true`, in the option bags, `AnalyzeHealthcareEntitiesOptions`, 
  `RecognizePiiEntitiesOptions` and `RecognizePiiEntitiesAction`, 
- Using UTF-16 code unit as the default encoding in the `Sentimant Analysis` and `Linked Entities Recognition` actions. 

## 5.1.0 (2021-07-08)
#### Feature Added
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
