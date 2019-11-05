
## Azure Cognitive Service - Text Analytics Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

# Samples Azure App Configuration APIs
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].
 
### Adding the package to your project

Maven dependency for Azure app configuration Client library. Add it to your project's pom file.


## How to run
These sample can be run in your IDE with default JDK.

## Examples
The following sections provide several code snippets covering some of the most common configuration service tasks, including:

- [Detect language in text][sample_hello_world]
- [Recognize entities in text][sample_entities]
- [Recognize health care entities in text][sample_health_care_entities]
- [Recognize personally identifiable information in text][sample_pii_entities]
- [Recognize linked entities in text][sample_linked_entities]
- [Recognize key phrases in text][sample_key_phrases]
- [Detect sentiment in text.][sample_sentiment]

Batch Samples:
- [Detect language for a batch of documents][sample_language_batch]
- [Recognize entities in a batch of documents][sample_entities_batch]
- [Recognize health care entities in a batch of documents][sample_health_care_entities_batch]
- [Recognize personally identifiable information in a batch of documents][sample_pii_entities_batch]
- [Recognize linked entities in a batch of documents][sample_linked_entities_batch]
- [Recognize key phrases in a batch of documents][sample_key_phrases_batch]
- [Detect sentiment in a batch of documents][sample_sentiment_batch]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Start using KeyVault Java SDK in your solutions. Our SDK documentation could be found at [SDK Documentation][ta_docs]. 

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: ../../README.md
[SDK_README_CONTRIBUTING]: ../../README.md#contributing
[SDK_README_GETTING_STARTED]: ../../README.md#getting-started
[SDK_README_TROUBLESHOOTING]: ../../README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: ../../README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#adding-the-package-to-your-product
[ta_docs]: https://docs.microsoft.com/en-us/azure/cognitive-services/text-analytics/

[sample_hello_world]: java/com/azure/cs/textanalytics/HelloWorld.java
[sample_entities]: java/com/azure/cs/textanalytics/RecognizeEntities.java
[sample_health_care_entities]: java/com/azure/cs/textanalytics/RecognizeHealthCareEntities.java
[sample_pii_entities]: java/com/azure/cs/textanalytics/RecognizePII.java
[sample_linked_entities]: java/com/azure/cs/textanalytics/RecognizeLinkedEntities.java
[sample_key_phrases]: java/com/azure/cs/textanalytics/RecognizeKeyPhrases.java
[sample_sentiment]: java/com/azure/cs/textanalytics/DetectSentiment.java

[sample_language_batch]: java/com/azure/cs/textanalytics/batch/DetectLanguageBatchDocuments.java
[sample_entities_batch]: java/com/azure/cs/textanalytics/batch/RecognizeEntitiesBatchDocuments.java
[sample_health_care_entities_batch]: java/com/azure/cs/textanalytics/batch/RecognizeHealthCareEntitiesBatchDocuments.java
[sample_pii_entities_batch]: java/com/azure/cs/textanalytics/batch/RecognizePIIBatchDocuments.java
[sample_linked_entities_batch]: java/com/azure/cs/textanalytics/batch/RecognizeLinkedEntitiesBatchDocuments.java
[sample_key_phrases_batch]: java/com/azure/cs/textanalytics/batch/RecognizeKeyPhrasesBatchDocuments.java
[sample_sentiment_batch]: java/com/azure/cs/textanalytics/batch/DetectSentimentBatchDocuments.java


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/appconfiguration/azure-data-appconfiguration/samples/README.png)
