# Release History
## 5.1.0-beta.1 (Unreleased)
- Added `offset` and `length` properties for `CategorizedEntity`, `LinkedEntityMatch` and `SentenceSentiment`
  - `length` is the number of characters in the text of these models
  - `offset` is the offset of the text from the start of the document
  
**New features**
- Updated Text Analytics SDK's default service API version to `v3.1-preview.1` from `v3.0`.
- Added support for Personally Identifiable Information(PII) entity recognition feature.
  To use this feature, you need to make sure you are using the service's v3.1-preview.1 API.
- Added support for the Opinion Mining feature. To use this feature, you need to make sure you are using the 
service's v3.1-preview.1 and above API. To get this support pass `includeOpinionMining` as `true` in 
`AnalyzeSentimentOptions` when calling the sentiment analysis endpoints.

## 5.0.0 (2020-07-27)
- Re-release of version `1.0.1` with updated version `5.0.0`.

## 1.0.1 (2020-07-07)
- Update dependency version, `azure-core` to 1.6.0 and `azure-core-http-netty` to 1.5.3.

## 1.0.0 (2020-06-09)
- First stable release of `azure-ai-textanalytics`.

## 1.0.0-beta.5 (2020-05-27)
**New features**
- Added Text property and `getText()` to `SentenceSentiment`.
- `Warnings` property added to each document-level response object returned from the endpoints. It is a list of `TextAnalyticsWarnings`.
- Added `CategorizedEntityCollection`, `KeyPhrasesCollection`, `LinkedEntityCollection` for having `getWarnings()` to retrieve warnings. 
- Added a new enum value `ADDRESS` to `EntityCategory`.
- Text analytics SDK update the service to version `v3.0` from `v3.0-preview.1`.

**Breaking changes**
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

**Breaking changes**
- Renamed all input parameters `text` to `document`, and `inputTexts` to `documents`.
- Removed all PII endpoints and update with related changes, such as remove related models, samples, codesnippets, docstrings, etc from this library. 
- Replaced `TextAnalyticsApiKeyCredential` with `AzureKeyCredential`.

## 1.0.0-beta.3 (2020-03-10)
**New features**
- Introduced `TextAnalyticsPagedFlux`, `TextAnalyticsPagedIterable`, and `TextAnalyticsPagedResponse` type. Moved `modelVersion` amd `TextDocumentBatchStatistics` into `TextAnalyticsPagedResponse`. All collection APIs are return `TextAnalyticsPagedFlux` and `TextAnalyticsPagedIterable` in the asynchronous and synchronous client, respectively. So `DocumentResultCollection` is no longer required. Most of existing API surface are changes. Please check up `TextAnalyticsAsyncClient` and `TextAnalyticsClient` for more detail.
- Introduced `EntityCategory` class to support major entity categories that the service supported.
- Added `getDefaultCountryHint()`, `getDefaultLanguage()` and `getServiceVersion()` to `TextAnalyticsClient`

**Breaking changes**
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

**Breaking changes**

- The single text, module-level operations return an atomic type of the operation result. For example, `detectLanguage(String text)` returns a `DetectedLanguage` rather than a `DetectLanguageResult`.

  For other module-level operations, :
    
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
 
**New features**

- Credential class `TextAnalyticsApiKeyCredential` provides an `updateCredential()` method which allows you to update the API key for long-lived clients.

**Fixes and improvements**

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
