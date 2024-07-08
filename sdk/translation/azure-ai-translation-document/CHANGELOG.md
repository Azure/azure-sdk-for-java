# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
