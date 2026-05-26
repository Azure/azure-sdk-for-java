# Release History

## 2.0.0-beta.2 (2026-04-07)

### Breaking Changes

#### `models.DictionaryExampleTextItem` was removed

#### `models.SourceText` was removed

#### `models.TranslateOptions` was removed

#### `models.DictionaryExample` was removed

#### `models.InputTextItem` was removed

#### `models.BackTranslation` was removed

#### `models.SourceDictionaryLanguage` was removed

#### `models.BreakSentenceItem` was removed

#### `models.SentenceBoundaries` was removed

#### `models.DictionaryTranslation` was removed

#### `models.DictionaryExampleItem` was removed

#### `models.TranslatedTextAlignment` was removed

#### `models.TargetDictionaryLanguage` was removed

#### `models.DictionaryLookupItem` was removed

#### `TextTranslationAsyncClient` was modified

* `findSentenceBoundaries(java.lang.String,java.lang.String,java.lang.String)` was removed
* `lookupDictionaryExamples(java.lang.String,java.lang.String,java.util.List)` was removed
* `findSentenceBoundaries(java.util.List,java.lang.String,java.lang.String)` was removed
* `findSentenceBoundaries(java.util.List)` was removed
* `lookupDictionaryExamplesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was removed
* `translateWithResponse(java.util.List,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was removed
* `findSentenceBoundariesWithResponse(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was removed
* `lookupDictionaryEntriesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was removed
* `lookupDictionaryEntries(java.lang.String,java.lang.String,java.lang.String)` was removed
* `translate(java.lang.String,models.TranslateOptions)` was removed
* `lookupDictionaryEntries(java.lang.String,java.lang.String,java.util.List)` was removed
* `findSentenceBoundaries(java.lang.String)` was removed
* `translate(java.lang.String,java.util.List)` was removed
* `translate(java.util.List,models.TranslateOptions)` was removed

#### `TextTranslationClient` was modified

* `findSentenceBoundaries(java.util.List,java.lang.String,java.lang.String)` was removed
* `lookupDictionaryEntriesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was removed
* `findSentenceBoundariesWithResponse(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was removed
* `lookupDictionaryEntries(java.lang.String,java.lang.String,java.lang.String)` was removed
* `lookupDictionaryEntries(java.lang.String,java.lang.String,java.util.List)` was removed
* `lookupDictionaryExamples(java.lang.String,java.lang.String,java.util.List)` was removed
* `translate(java.lang.String,models.TranslateOptions)` was removed
* `findSentenceBoundaries(java.lang.String)` was removed
* `findSentenceBoundaries(java.util.List)` was removed
* `lookupDictionaryExamplesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was removed
* `translateWithResponse(java.util.List,com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was removed
* `findSentenceBoundaries(java.lang.String,java.lang.String,java.lang.String)` was removed
* `translate(java.lang.String,java.util.List)` was removed
* `translate(java.util.List,models.TranslateOptions)` was removed

#### `models.TranslationText` was modified

* `getTargetLanguage()` was removed
* `getAlignment()` was removed
* `getSentenceBoundaries()` was removed
* `getTransliteration()` was removed

#### `models.DetectedLanguage` was modified

* `getConfidence()` was removed

#### `models.GetSupportedLanguagesResult` was modified

* `getDictionary()` was removed

#### `models.TransliterableScript` was modified

* `getTargetLanguageScripts()` was removed

#### `models.LanguageScope` was modified

* `DICTIONARY` was removed

#### `models.TranslatedTextItem` was modified

* `getSourceText()` was removed

#### `TextTranslationServiceVersion` was modified

* `V3_0` was removed

### Features Added

* `models.ReferenceTextPair` was added

* `models.TranslateInputItem` was added

* `models.TranslationTarget` was added

#### `TextTranslationAsyncClient` was modified

* `translateWithResponse(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added
* `translate(java.util.List)` was added

#### `TextTranslationClient` was modified

* `translate(java.util.List)` was added
* `translateWithResponse(com.azure.core.util.BinaryData,com.azure.core.http.rest.RequestOptions)` was added

#### `models.TranslationText` was modified

* `getResponseTokens()` was added
* `getInstructionTokens()` was added
* `getLanguage()` was added
* `getSourceCharacters()` was added
* `getSourceTokens()` was added
* `getTargetTokens()` was added

#### `models.DetectedLanguage` was modified

* `getScore()` was added

#### `models.GetSupportedLanguagesResult` was modified

* `getModels()` was added

#### `models.TransliterableScript` was modified

* `getToScripts()` was added

#### `models.LanguageScope` was modified

* `MODELS` was added

#### `TextTranslationServiceVersion` was modified

* `V2026_06_06` was added

#### `models.TranslationLanguage` was modified

* `getModels()` was added

## 1.1.9 (2026-05-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.57.1` to version `1.58.0`.
- Upgraded `azure-core-http-netty` from `1.16.3` to version `1.16.4`.

## 1.1.8 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.16.2` to version `1.16.3`.
- Upgraded `azure-core` from `1.57.0` to version `1.57.1`.

## 2.0.0-beta.1 (2026-01-08)

### Features Added

- Added support for the Azure AI Translator API 2025-10-01-preview, including translations using LLM models, adaptive custom translation, tone variant translations, and gender-specific translations.
- Added `TranslationTarget` class for configuring translation options.

### Breaking Changes

- Added `Models` property to `GetSupportedLanguagesResult` to include the list of LLM models available for translations.
- Changed the name of `TargetLanguage` property to `Language` in `TranslationText`.
- Changed the name of `Confidence` property to `Score` in `DetectedLanguage`.
- Removed `SourceText` and `Transliteration` properties in translation responses.
- Dictionary, sentence boundaries and text alignments features have been deprecated and relevant classes and properties have been removed.

## 1.1.7 (2025-10-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.56.1` to version `1.57.0`.
- Upgraded `azure-core-http-netty` from `1.16.1` to version `1.16.2`.

## 1.1.6 (2025-09-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.16.0` to version `1.16.1`.
- Upgraded `azure-core` from `1.56.0` to version `1.56.1`.

## 1.1.5 (2025-08-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.5` to version `1.56.0`.
- Upgraded `azure-core-http-netty` from `1.15.13` to version `1.16.0`.

## 1.1.4 (2025-07-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.4` to version `1.55.5`.
- Upgraded `azure-core-http-netty` from `1.15.12` to version `1.15.13`.

## 1.1.3 (2025-06-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.
- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.

## 1.1.2 (2025-03-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.2` to version `1.55.3`.
- Upgraded `azure-core-http-netty` from `1.15.10` to version `1.15.11`.

## 1.1.1 (2025-02-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.7` to version `1.15.10`.
- Upgraded `azure-core` from `1.54.1` to version `1.55.2`.

## 1.1.0 (2024-11-15)

### Other Changes

- Removed `JsonCreator` and `JsonProperty` annotation and replaced them with new methods `toJson` and `fromJson` using stream-style serialization.

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to version `1.54.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.7`.

## 1.0.0 (2024-05-21)

### Features Added

- Added support for AAD authentication for Text Translation endpoints.
- Ability to translate strings directly without a need to create instances of objects.
- Options pattern used for methods with too many parameters.

### Breaking Changes

- Method `getSentLen` renamed to `getSentencesLengths`.
- `String` can be used for translate, transliterate, find sentence boundaries methods instead of `InputTextItem`.
- `GetLanguages*` classes and definitions were renamed to `GetSupportedLanguages*`.
- `getProj` method renamed to `getProjections`.
- `Translation` class renamed to `TranslationText`.
- `getScore` method renamed to `getConfidence`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.15.0`.

## 1.0.0-beta.1 (2023-04-18)

- Azure Text Translation client library for Java. This package contains Microsoft Azure Text Translation client library.

