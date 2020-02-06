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

- [Detect language in text][sample_detect_language]
- [Detect language in text with asynchronous client][async_sample_detect_language]
- [Recognize entities in text][sample_entities]
- [Recognize entities in text with asynchronous client][async_sample_entities]
- [Recognize personally identifiable information in text][sample_pii_entities]
- [Recognize personally identifiable information in text with asynchronous client][async_sample_pii_entities]
- [Recognize linked entities in text][sample_linked_entities]
- [Recognize linked entities in text with asynchronous client][async_sample_linked_entities]
- [Extract key phrases in text][sample_key_phrases]
- [Extract key phrases in text with asynchronous client][async_sample_key_phrases]
- [Analyze sentiment in text][sample_sentiment]
- [Analyze sentiment in text with asynchronous client][async_sample_sentiment]
- [Rotate API key][sample_rotate_key]
- [Rotate API key with asynchronous client][async_sample_rotate_key]

Batch Samples:
- [Detect language for a batch of documents][sample_detect_language_batch]
- [Detect language for a batch of documents with asynchronous client][async_sample_detect_language_batch]
- [Recognize entities in a batch of documents][sample_entities_batch]
- [Recognize entities in a batch of documents with asynchronous client][async_sample_entities_batch]
- [Recognize personally identifiable information in a batch of documents][sample_pii_entities_batch]
- [Recognize personally identifiable information in a batch of documents with asynchronous client][async_sample_pii_entities_batch]
- [Recognize linked entities in a batch of documents][sample_linked_entities_batch]
- [Recognize linked entities in a batch of documents with asynchronous client][async_sample_linked_entities_batch]
- [Extract key phrases in a batch of documents][sample_key_phrases_batch]
- [Extract key phrases in a batch of documents with asynchronous client][async_sample_key_phrases_batch]
- [Analyze sentiment in a batch of documents][sample_sentiment_batch]
- [Analyze sentiment in a batch of documents with asynchronous client][async_sample_sentiment_batch]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
See [Next steps][SDK_README_NEXT_STEPS]. 

## Contributing
If you would like to become an active contributor to this project please refer to our [Contribution
Guidelines](../../CONTRIBUTING.md) for more information.

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
[async_sample_entities]: java/com/azure/ai/textanalytics/RecognizeEntitiesAsync.java
[async_sample_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchDocumentsAsync.java
[async_sample_linked_entities]: java/com/azure/ai/textanalytics/RecognizeLinkedEntitiesAsync.java
[async_sample_linked_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchDocumentsAsync.java
[async_sample_key_phrases]: java/com/azure/ai/textanalytics/ExtractKeyPhrasesAsync.java
[async_sample_key_phrases_batch]: java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchDocumentsAsync.java
[async_sample_pii_entities]: java/com/azure/ai/textanalytics/RecognizePiiAsync.java
[async_sample_pii_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizePiiBatchDocumentsAsync.java
[async_sample_rotate_key]: java/com/azure/ai/textanalytics/RotateApiKeyAsync.java
[async_sample_sentiment]: java/com/azure/ai/textanalytics/AnalyzeSentimentAsync.java
[async_sample_sentiment_batch]: java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchDocumentsAsync.java

[sample_detect_language]: java/com/azure/ai/textanalytics/DetectLanguage.java
[sample_detect_language_batch]: java/com/azure/ai/textanalytics/batch/DetectLanguageBatchDocuments.java
[sample_entities]: java/com/azure/ai/textanalytics/RecognizeEntities.java
[sample_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchDocuments.java
[sample_linked_entities]: java/com/azure/ai/textanalytics/RecognizeLinkedEntities.java
[sample_linked_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchDocuments.java
[sample_key_phrases]: java/com/azure/ai/textanalytics/ExtractKeyPhrases.java
[sample_key_phrases_batch]: java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchDocuments.java
[sample_pii_entities]: java/com/azure/ai/textanalytics/RecognizePii.java
[sample_pii_entities_batch]: java/com/azure/ai/textanalytics/batch/RecognizePiiBatchDocuments.java
[sample_rotate_key]: java/com/azure/ai/textanalytics/RotateApiKey.java
[sample_sentiment]: java/com/azure/ai/textanalytics/AnalyzeSentiment.java
[sample_sentiment_batch]: java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchDocuments.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftextanalytics%2Fazure-ai-textanalytics%2FREADME.png)
