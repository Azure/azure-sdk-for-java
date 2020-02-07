# Release History

## 1.0.0-beta.2 (2020-02-11)

**Breaking changes**

- The single text, module-level operations return an atomic type of the operation result. For example, `detectLanguage(String text)` returns a `DetectedLanguage` rather than a `DetectLanguageResult`.

  For other module-level operations:
    
  `recognizeEntities(String text)`, it no longer returns type of `Mono<RecognizeEntitiesResult>` but `PagedFlux<CategorizedEntity>`.
  
  `recognizePiiEntities(String text)`, it no longer returns type of `Mono<RecognizePiiEntitiesResult>` but `PagedFlux<PiiEntity>`.
  
  `recognizeLinkedEntities(String text)`, it no longer returns type of `Mono<RecognizeLinkedEntitiesResult>` but `PagedFlux<LinkedEntity>`.
  
  `extractKeyPhrases(String text)`, it no longer returns type of `Mono<ExtractKeyPhraseResult>` but `PagedFlux<String>`.
  
  `analyzeSentiment(String text)`, it no longer returns type of `Mono<AnalyzeSentimentResult>` but `Mono<DocumentSentiment>`.
  
  `recognizeEntitiesWithResponse(String text, String language)` changed to `recognizeEntities(String text, String language)` and return `PagedFlux<CategorizedEntity>` as a collection of atomic type `CategorizedEntity`.
  
  `recognizePiiEntitiesWithResponse(String text, String language)` changed to `recognizePiiEntities(String text, String language)` and return `PagedFlux<PiiEntity>` as a collection of atomic type `PiiEntity`.
  
  `recognizeLinkedEntitiesWithResponse(String text, String language)` changed to `recognizeLinkedEntities(String text, String language)` and return `PagedFlux<LinkedEntity>` as a collection of atomic type `LinkedEntity`.
  
  `extractKeyPhrasesWithResponse(String text, String language)` changed to `extractKeyPhrases(String text, String language)` and return `PagedFlux<String>` as a collection of atomic type `String`.
  
  `analyzeSentimentWithResponse(String text, String language)` return `Mono<Response<DocumentSentiment>>` with an atomic type `DocumentSentiment`.

- Removed `TextSentiment` class but created `DocumentSentiment` and `SentenceSentiment` instead. `DocumentSentiment` includes a list of `SentenceSentiment`.
- Renamed `SentimentClass` to `SentimentLabel`.
- Added a new class model `SentimentScorePerLabel` for the scores of sentiment label.  
- Added a new parameter `TextAnalyticsRequestOptions options` to method overloads accepting a list of text inputs for allowing the users to opt for batch operation statistics.
- Passing the API key as a string is no longer supported. To use subscription key authentication a new credential class `TextAnalyticsApiKeyCredential("<api_key>")` must be passed in for the `credential` parameter.
  Rotating API Key is supported by using method `updateCredential()` to update the existing API key in `TextAnalyticsApiKeyCredential`.
- `detectLanguages()` is renamed to `detectLanguage()`.
- The `TextAnalyticsError` model has been simplified to an object with only attributes `code`, `message`, and `target`.
- `NamedEntity` has been renamed to `CategorizedEntity` and its attributes `type` to `category` and `subtype` to `subcategory`.
- `RecognizePiiEntitiesResult` now contains on the object a list of `PiiEntity` instead of `NamedEntity`.
- `DetectLanguageResult` no longer has `List<DetectedLanguage> detectedLanguages`. Use `getPrimaryLanguage()` to access the detected language in text.
- `AnalyzeSentimentResult` no longer takes `List<TextSentiment> sentenceSentiments` and removed `getSentenceSentiments()` 

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
