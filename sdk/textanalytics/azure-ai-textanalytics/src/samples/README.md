---
page_type: sample
languages:
  - java
products:
    - azure
    - azure-cognitive-services
    - language-service
urlFragment: textanalytics-java-samples
---

# Azure Cognitive Services for Language client library samples for Java

Azure Language service samples are a set of self-contained Java programs that demonstrate interacting with
Azure Language service using the client library. Each sample focuses on a specific scenario and can be executed independently. 

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
- [Recognize Personally Identifiable Information in a document][sample_pii_entities]
- [Recognize Personally Identifiable Information in a document with asynchronous client][async_sample_pii_entities]
- [Recognize linked entities in a document][sample_linked_entities]
- [Recognize linked entities in a document with asynchronous client][async_sample_linked_entities]
- [Extract key phrases in a document][sample_key_phrases]
- [Extract key phrases in a document with asynchronous client][async_sample_key_phrases]
- [Analyze sentiment in a document][sample_sentiment]
- [Analyze sentiment in a document with asynchronous client][async_sample_sentiment]
- [Analyze sentiment with opinion mining in a document][sample_sentiment_opinion_mining]
- [Analyze sentiment with opinion mining in a document with asynchronous client][async_sample_sentiment_opinion_mining]
- [Rotate key credential][sample_rotate_key]
- [Rotate key credential with asynchronous client][async_sample_rotate_key]
- [Model Version][sample_model_version]
- [Model Version with asynchronous client][async_sample_model_version]

Batch Samples:
- [Detect language for a batch of documents][sample_detect_language_batch]
- [Detect language for a batch of documents(Convenience)][sample_detect_language_batch_convenience]
- [Detect language for a batch of documents with asynchronous client][async_sample_detect_language_batch]
- [Detect language for a batch of documents with asynchronous client(Convenience)][async_sample_detect_language_batch_convenience]
- [Recognize entities in a batch of documents][sample_entities_batch]
- [Recognize entities in a batch of documents(Convenience)][sample_entities_batch_convenience]
- [Recognize entities in a batch of documents with asynchronous client][async_sample_entities_batch]
- [Recognize entities in a batch of documents with asynchronous client(Convenience)][async_sample_entities_batch_convenience]
- [Recognize Personally Identifiable Information in a batch of documents][sample_pii_entities_batch]
- [Recognize Personally Identifiable Information in a batch of documents(Convenience)][sample_pii_entities_batch_convenience]
- [Recognize Personally Identifiable Information in a batch of documents with asynchronous client][async_sample_pii_entities_batch]
- [Recognize Personally Identifiable Information in a batch of documents with asynchronous client(Convenience)][async_sample_pii_entities_batch_convenience]
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
- [Analyze healthcare entities in a batch of documents][sample_healthcare_batch]
- [Analyze healthcare entities in a batch of documents with asynchronous client][async_sample_healthcare_batch]
- [Cancel analyze healthcare entities][sample_cancel_healthcare_task]
- [Cancel analyze healthcare entities with asynchronous client][async_sample_cancel_healthcare_task]
- [Abstractive text summarization][sample_abstractive_text_summarization]
- [Abstractive text summarization with asynchronous client][async_sample_abstractive_text_summarization]
- [Extractive text summarization][sample_extractive_text_summarization]
- [Extractive text summarization with asynchronous client][async_sample_extractive_text_summarization]
- [Execute custom entities recognition][sample_execute_custom_entities]
- [Execute custom entities recognition with asynchronous client][async_sample_execute_custom_entities]
- [Execute single-label classification][sample_execute_single_classification]
- [Execute single-label classification with asynchronous client][async_sample_execute_single_classification]
- [Execute multi-label classification][sample_execute_multi_classification]
- [Execute multi-label classification with asynchronous client][async_sample_execute_multi_classification]
- [Execute multiple actions][sample_execute_multiple_actions]
- [Execute multiple actions with asynchronous client][async_sample_execute_multiple_actions]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
See [Next steps][SDK_README_NEXT_STEPS]. 

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/README.md#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/README.md#key-concepts
[SDK_README_DEPENDENCY]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/README.md#adding-the-package-to-your-product
[SDK_README_NEXT_STEPS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/README.md#next-steps

[async_sample_detect_language]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/DetectLanguageAsync.java
[async_sample_detect_language_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/DetectLanguageBatchDocumentsAsync.java
[async_sample_detect_language_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/DetectLanguageBatchStringDocumentsAsync.java
[async_sample_entities]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/RecognizeEntitiesAsync.java
[async_sample_entities_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchDocumentsAsync.java
[async_sample_entities_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchStringDocumentsAsync.java
[async_sample_pii_entities]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/RecognizePiiEntitiesAsync.java
[async_sample_pii_entities_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizePiiEntitiesBatchDocumentsAsync.java
[async_sample_pii_entities_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchStringDocumentsAsync.java
[async_sample_linked_entities]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/RecognizeLinkedEntitiesAsync.java
[async_sample_linked_entities_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchDocumentsAsync.java
[async_sample_linked_entities_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchStringDocumentsAsync.java
[async_sample_key_phrases]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/ExtractKeyPhrasesAsync.java
[async_sample_key_phrases_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchDocumentsAsync.java
[async_sample_key_phrases_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchStringDocumentsAsync.java
[async_sample_rotate_key]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/RotateAzureKeyCredentialAsync.java
[async_sample_sentiment]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/AnalyzeSentimentAsync.java
[async_sample_sentiment_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchDocumentsAsync.java
[async_sample_sentiment_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchStringDocumentsAsync.java
[async_sample_sentiment_opinion_mining]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/AnalyzeSentimentWithOpinionMiningAsync.java
[async_sample_healthcare_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AnalyzeHealthcareEntitiesAsync.java
[async_sample_cancel_healthcare_task]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/CancelAnalyzeHealthcareEntitiesAsync.java
[async_sample_execute_multiple_actions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AnalyzeActionsAsync.java
[async_sample_execute_custom_entities]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/RecognizeCustomEntitiesAsync.java
[async_sample_execute_single_classification]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/SingleLabelClassifyDocumentAsync.java
[async_sample_execute_multi_classification]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/MultiLabelClassifyDocumentAsync.java
[async_sample_model_version]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/ModelVersionAsync.java
[async_sample_extractive_text_summarization]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/ExtractiveSummarizationAsync.java
[async_sample_abstractive_text_summarization]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AbstractiveSummarizationAsync.java

[sample_detect_language]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/DetectLanguage.java
[sample_detect_language_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/DetectLanguageBatchDocuments.java
[sample_detect_language_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/DetectLanguageBatchStringDocuments.java
[sample_entities]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/RecognizeEntities.java
[sample_entities_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchDocuments.java
[sample_entities_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchStringDocuments.java
[sample_pii_entities]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/RecognizePiiEntities.java
[sample_pii_entities_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizePiiEntitiesBatchDocuments.java
[sample_pii_entities_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizePiiEntitiesBatchStringDocuments.java
[sample_linked_entities]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/RecognizeLinkedEntities.java
[sample_linked_entities_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchDocuments.java
[sample_linked_entities_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchStringDocuments.java
[sample_key_phrases]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/ExtractKeyPhrases.java
[sample_key_phrases_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchDocuments.java
[sample_key_phrases_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchStringDocuments.java
[sample_rotate_key]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/RotateAzureKeyCredential.java
[sample_sentiment]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/AnalyzeSentiment.java
[sample_sentiment_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchDocuments.java
[sample_sentiment_batch_convenience]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchStringDocuments.java
[sample_sentiment_opinion_mining]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/AnalyzeSentimentWithOpinionMining.java
[sample_healthcare_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AnalyzeHealthcareEntities.java
[sample_cancel_healthcare_task]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/CancelAnalyzeHealthcareEntities.java
[sample_execute_multiple_actions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AnalyzeActions.java
[sample_execute_custom_entities]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/RecognizeCustomEntities.java
[sample_execute_single_classification]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/SingleLabelClassifyDocument.java
[sample_execute_multi_classification]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/MultiLabelClassifyDocument.java
[sample_model_version]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/ModelVersion.java
[sample_abstractive_text_summarization]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AbstractiveSummarization.java
[sample_extractive_text_summarization]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/ExtractiveSummarization.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftextanalytics%2Fazure-ai-textanalytics%2FREADME.png)
