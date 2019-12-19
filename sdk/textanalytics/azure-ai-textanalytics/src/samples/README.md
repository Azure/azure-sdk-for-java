
## Azure Text Analytics client library for Java Samples

Azure Text Analytics samples are a set of self-contained Java programs that demonstrate interacting with Azure Text Analytics service
using the client library. Each sample focuses on a specific scenario and can be executed independently. 

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

# Samples Azure Text Analytics APIs
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
The following sections provide several code snippets covering some of the most common configuration service tasks, including:

- [Detect language in text][sample_hello_world]
- [Recognize entities in text][sample_entities]
- [Recognize personally identifiable information in text][sample_pii_entities]
- [Recognize linked entities in text][sample_linked_entities]
- [Extract key phrases in text][sample_key_phrases]
- [Analyze sentiment in text.][sample_sentiment]

Batch Samples:
- [Detect language for a batch of documents][sample_language_batch]
- [Recognize entities in a batch of documents][sample_entities_batch]
- [Recognize personally identifiable information in a batch of documents][sample_pii_entities_batch]
- [Recognize linked entities in a batch of documents][sample_linked_entities_batch]
- [Extract key phrases in a batch of documents][sample_key_phrases_batch]
- [Analyze sentiment in a batch of documents][sample_sentiment_batch]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
See [Next steps][ta_docs]. 

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

[sample_hello_world]: java/com/azure/cs/textanalytics/HelloWorld.java
[sample_entities]: java/com/azure/cs/textanalytics/RecognizeEntities.java
[sample_pii_entities]: java/com/azure/cs/textanalytics/RecognizePII.java
[sample_linked_entities]: java/com/azure/cs/textanalytics/RecognizeLinkedEntities.java
[sample_key_phrases]: java/com/azure/cs/textanalytics/RecognizeKeyPhrases.java
[sample_sentiment]: java/com/azure/cs/textanalytics/DetectSentiment.java

[sample_language_batch]: java/com/azure/cs/textanalytics/batch/DetectLanguageBatchDocuments.java
[sample_entities_batch]: java/com/azure/cs/textanalytics/batch/RecognizeEntitiesBatchDocuments.java
[sample_pii_entities_batch]: java/com/azure/cs/textanalytics/batch/RecognizePiiBatchDocuments.java
[sample_linked_entities_batch]: java/com/azure/cs/textanalytics/batch/RecognizeLinkedEntitiesBatchDocuments.java
[sample_key_phrases_batch]: java/com/azure/cs/textanalytics/batch/RecognizeKeyPhrasesBatchDocuments.java
[sample_sentiment_batch]: java/com/azure/cs/textanalytics/batch/DetectSentimentBatchDocuments.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftextanalytics%2Fazure-ai-textanalytics%2FREADME.png)
