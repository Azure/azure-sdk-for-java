---
topic: sample
languages:
  - java
products:
  - azure
  - azure-cognitive-service
---

# Azure Text Analytics client library for Java Samples

Azure Text Analytics samples are a set of self-contained Java programs that demonstrate interacting with Azure Text Analytics service
using the client library. Each sample focuses on a specific scenario and can be executed independently. 

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
The following sections provide several code snippets covering some of the most common configuration service tasks, including:

- [Detect language in a document][sample_detect_language]
- [Detect language in a document with asynchronous client][async_sample_detect_language]
- [Recognize entities in a document][sample_entities]
- [Recognize entities in a document with asynchronous client][async_sample_entities]
- [Recognize linked entities in a document][sample_linked_entities]
- [Recognize linked entities in a document with asynchronous client][async_sample_linked_entities]
- [Extract key phrases in a document][sample_key_phrases]
- [Extract key phrases in a document with asynchronous client][async_sample_key_phrases]
- [Analyze sentiment in a document][sample_sentiment]
- [Analyze sentiment in a document with asynchronous client][async_sample_sentiment]
- [Rotate API key][sample_rotate_key]
- [Rotate API key with asynchronous client][async_sample_rotate_key]

Batch Samples:
- [Detect language for a batch of documents][sample_detect_language_batch]
- [Detect language for a batch of documents(Convenience)][sample_detect_language_batch_convenience]
- [Detect language for a batch of documents with asynchronous client][async_sample_detect_language_batch]
- [Detect language for a batch of documents with asynchronous client(Convenience)][async_sample_detect_language_batch_convenience]
- [Recognize entities in a batch of documents][sample_entities_batch]
- [Recognize entities in a batch of documents(Convenience)][sample_entities_batch_convenience]
- [Recognize entities in a batch of documents with asynchronous client][async_sample_entities_batch]
- [Recognize entities in a batch of documents with asynchronous client(Convenience)][async_sample_entities_batch_convenience]
- [Recognize linked entities in a batch of documents][sample_linked_entities_batch]
- [Recognize linked entities in a batch of documents(Convenience)][sample_linked_entities_batch_convenience]
- [Recognize linked entities in a batch of documents with asynchronous client][async_sample_linked_entities_batch]
- [Recognize linked entities in a batch of documents with asynchronous client(Convenience)][async_sample_linked_entities_batch_convenience]
- [Extract key phrases in a batch of documents][sample_key_phrases_batch]
- [Extract key phrases in a batch of documents(Convenience)][sample_key_phrases_batch_convenience]
- [Extract key phrases in a batch of documents with asynchronous client][async_sample_key_phrases_batch]
- [Extract key phrases in a batch of documents with asynchronous client(Convenience)][async_sample_key_phrases_batch_convenience]
- [Analyze sentiment in a batch of documents][sample_sentiment_batch]
- [Analyze sentiment in a batch of documents(Convenience)][sample_sentiment_batch_convenience]
- [Analyze sentiment in a batch of documents with asynchronous client][async_sample_sentiment_batch]
- [Analyze sentiment in a batch of documents with asynchronous client(Convenience)][async_sample_sentiment_batch_convenience]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
See [Next steps][SDK_README_NEXT_STEPS]. 

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: ../../README.md
[SDK_README_CONTRIBUTING]: ../../README.md#contributing
[SDK_README_GETTING_STARTED]: ../../README.md#getting-started
[SDK_README_TROUBLESHOOTING]: ../../README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: ../../README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#adding-the-package-to-your-product
[SDK_README_NEXT_STEPS]: ../../README.md#next-steps

[async_sample_detect_language]: java/com/azure/ai/textanalytics/DetectLanguageAsync.java
[async_sample_detect_language_batch]: java/com/azure/ai/textanalytics/batch/DetectLanguageBatchDocumentsAsync.java
[async_sample_detect_language_batch_convenience]: java/com/azure/ai/textanalytics/batch/DetectLanguageBatchStringDocumentsAsync.java
[async_sample_entities]: java/com/azure/ai/textanalytics/RecognizeEntitiesAsync.java
[async_sample_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchDocumentsAsync.java
[async_sample_entities_batch_convenience]: java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchStringDocumentsAsync.java
[async_sample_linked_entities]: java/com/azure/ai/textanalytics/RecognizeLinkedEntitiesAsync.java
[async_sample_linked_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchDocumentsAsync.java
[async_sample_linked_entities_batch_convenience]: java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchStringDocumentsAsync.java
[async_sample_key_phrases]: java/com/azure/ai/textanalytics/ExtractKeyPhrasesAsync.java
[async_sample_key_phrases_batch]: java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchDocumentsAsync.java
[async_sample_key_phrases_batch_convenience]: java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchStringDocumentsAsync.java
[async_sample_rotate_key]: java/com/azure/ai/textanalytics/RotateApiKeyAsync.java
[async_sample_sentiment]: java/com/azure/ai/textanalytics/AnalyzeSentimentAsync.java
[async_sample_sentiment_batch]: java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchDocumentsAsync.java
[async_sample_sentiment_batch_convenience]: java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchStringDocumentsAsync.java

[sample_detect_language]: java/com/azure/ai/textanalytics/DetectLanguage.java
[sample_detect_language_batch]: java/com/azure/ai/textanalytics/batch/DetectLanguageBatchDocuments.java
[sample_detect_language_batch_convenience]: java/com/azure/ai/textanalytics/batch/DetectLanguageBatchStringDocuments.java
[sample_entities]: java/com/azure/ai/textanalytics/RecognizeEntities.java
[sample_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchDocuments.java
[sample_entities_batch_convenience]: java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchStringDocuments.java
[sample_linked_entities]: java/com/azure/ai/textanalytics/RecognizeLinkedEntities.java
[sample_linked_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchDocuments.java
[sample_linked_entities_batch_convenience]: java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchStringDocuments.java
[sample_key_phrases]: java/com/azure/ai/textanalytics/ExtractKeyPhrases.java
[sample_key_phrases_batch]: java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchDocuments.java
[sample_key_phrases_batch_convenience]: java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchStringDocuments.java
[sample_rotate_key]: java/com/azure/ai/textanalytics/RotateApiKey.java
[sample_sentiment]: java/com/azure/ai/textanalytics/AnalyzeSentiment.java
[sample_sentiment_batch]: java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchDocuments.java
[sample_sentiment_batch_convenience]: java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchStringDocuments.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftextanalytics%2Fazure-ai-textanalytics%2FREADME.png)
