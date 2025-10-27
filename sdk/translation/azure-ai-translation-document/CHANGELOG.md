# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.6 (2025-10-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.56.1` to version `1.57.0`.
- Upgraded `azure-core-http-netty` from `1.16.1` to version `1.16.2`.


## 1.0.5 (2025-09-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.16.0` to version `1.16.1`.
- Upgraded `azure-core` from `1.56.0` to version `1.56.1`.


## 1.0.4 (2025-08-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.5` to version `1.56.0`.
- Upgraded `azure-core-http-netty` from `1.15.13` to version `1.16.0`.


## 1.0.3 (2025-07-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.4` to version `1.55.5`.
- Upgraded `azure-core-http-netty` from `1.15.12` to version `1.15.13`.


## 1.0.2 (2025-06-19)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.
- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.


## 1.0.1 (2025-03-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-json` from `1.3.0` to version `1.5.0`.
- Upgraded `azure-core-http-netty` from `1.15.7` to version `1.15.11`.
- Upgraded `azure-core` from `1.54.1` to version `1.55.3`.


## 1.0.0 (2024-11-20)

### Other Changes
- Renamed `document_translate` API of SingleDocumentTranslationClient to `translate`.
- Renamed `GetTranslationsStatus` API to `ListTranslationStatuses`.
- Renamed `GetDocumentsStatus` API to `ListDocumentStatuses`.
- Encapsulated all API parameters for `ListTranslationStatuses` into a dedicated options class named `ListTranslationStatusesOptions`.
- Encapsulated all API parameters for `ListDocumentStatuses` into a dedicated options class named `ListDocumentStatusesOptions`.

## 1.0.0-beta.2 (2024-06-18)

### Other Changes
- Re-release of 1.0.0-beta.1

## 1.0.0-beta.1 (2024-06-17)

Version 1.0.0-beta.1 is preview of our efforts in creating a client library that is developer-friendly, idiomatic 
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide 
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

This package's 
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-document/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document) 
demonstrate the new API.

- Initial release. Please see the README and wiki for information on the new design.

### Features Added
- Added support for Synchronous document translation - [translate-document API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/translate-document)
- Added support for Batch Translation - [start Translation API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/start-batch-translation)
- Added support for Get Translations Status - [get translations status API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-translations-status)
- Added support for Get Translation Status - [get translation status API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-translation-status)
- Added support for Get Documents Status - [get documents status API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-documents-status)
- Added support for Get Document Status - [get document status API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-document-status)
- Added support for Cancel Translation - [cancel translation API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/cancel-translation)
- Added support for Get Supported Document Formats - [get supported document formats API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-supported-document-formats)
- Added support for Get Supported Glossary Formats - [get supported glossary formats API](https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-supported-glossary-formats)
