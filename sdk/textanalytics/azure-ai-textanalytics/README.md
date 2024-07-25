# Azure Text Analytics client library for Java
The Azure Cognitive Service for Language is a cloud-based service that provides Natural Language Processing (NLP) 
features for understanding and analyzing text, and includes the following main features:

- Sentiment Analysis
- Entity Recognition (Named, Linked, and Personally Identifiable Information (PII) entities)
- Language Detection
- Key Phrase Extraction
- Multiple Actions Analysis Per Document
- Healthcare Entities Analysis
- Abstractive Text Summarization
- Extractive Text Summarization
- Custom Named Entity Recognition
- Custom Text Classification

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Language service account][language_service_account] to use this package.

### Include the Package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on GA version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-textanalytics</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-ai-textanalytics;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-textanalytics</artifactId>
    <version>5.4.7</version>
</dependency>
```
[//]: # ({x-version-update-end})
**Note:** This version of the client library defaults to the `2023-04-01` version of the service.
It is a newer version than `3_0`, `3_1` and `2022-05-01`.

This table shows the relationship between SDK services and supported API versions of the service:

|SDK version|Supported API version of service
|-|-
|5.3.x | 3.0, 3.1, 2022-05-01, 2023-04-01 (default)
|5.2.x | 3.0, 3.1, 2022-05-01
|5.1.x | 3.0, 3.1 
|5.0.x | 3.0

#### Create a Cognitive Services or Language Service resource
The Language service supports both [multi-service and single-service access][service_access]. Create a Cognitive Services 
resource if you plan to access multiple cognitive services under a single endpoint/key. For Language service access only,
create a Language service resource.

You can create the resource using the [Azure Portal][create_new_resource_in_azure_portal] or 
[Azure CLI][azure_cli_doc] following the steps in [this document][create_new_resource_in_azure_cli].

### Authenticate the client
In order to interact with the Language service, you will need to create an instance of the Text Analytics client,
both the asynchronous and synchronous clients can be created by using `TextAnalyticsClientBuilder` invoking `buildClient()`
creates a synchronous client while `buildAsyncClient()` creates its asynchronous counterpart.

You will need an **endpoint** and either a **key** or **AAD TokenCredential** to instantiate a client object. 

#### Looking up the endpoint
You can find the **endpoint** for your Language service resource in the [Azure Portal][azure_portal] under the
"Keys and Endpoint", or [Azure CLI][azure_cli_endpoint].
```bash
# Get the endpoint for the Language service resource
az cognitiveservices account show --name "resource-name" --resource-group "resource-group-name" --query "endpoint"
```

#### Create a Text Analytics client with key credential
Once you have the value for the [key][key], provide it as a string to the [AzureKeyCredential][azure_key_credential].
This can be found in the [Azure Portal][azure_portal] under the "Keys and Endpoint" section in your created Language 
service resource or by running the following Azure CLI command:

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

Use the key as the credential parameter to authenticate the client:
```java readme-sample-createTextAnalyticsClientWithKeyCredential
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

The Azure Text Analytics client library provides a way to **rotate the existing key**.
```java readme-sample-rotatingAzureKeyCredential
AzureKeyCredential credential = new AzureKeyCredential("{key}");
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .credential(credential)
    .endpoint("{endpoint}")
    .buildClient();

credential.update("{new_key}");
```

#### Create a Text Analytics client with Azure Active Directory credential
Azure SDK for Java supports an Azure Identity package, making it easy to get credentials from Microsoft identity
platform. 

Authentication with AAD requires some initial setup:
* Add the Azure Identity package

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.1</version>
</dependency>
```
[//]: # ({x-version-update-end})
* [Register a new Azure Active Directory application][register_AAD_application]
* [Grant access][grant_access] to Language service by assigning the `"Cognitive Services User"` role to your service principal.

After setup, you can choose which type of [credential][azure_identity_credential_type] from azure.identity to use. 
As an example, [DefaultAzureCredential][wiki_identity] can be used to authenticate the client:
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: 
AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET. 

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with Language service, please
refer to [the associated documentation][aad_authorization].

```java readme-sample-createTextAnalyticsAsyncClientWithAAD
TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
    .endpoint("{endpoint}")
    .credential(defaultCredential)
    .buildAsyncClient();
```

## Key concepts
### Text Analytics client
The Text Analytics client library provides a [TextAnalyticsClient][text_analytics_sync_client] and 
[TextAnalyticsAsyncClient][text_analytics_async_client] to do analysis on batches of documents. It provides both synchronous and
asynchronous operations to access a specific use of Language service, such as language detection or key phrase extraction.

### Input
A **text input**, also called a **document**, is a single unit of document to be analyzed by the predictive models
in the Language service. Operations on a Text Analytics client may take a single document or a collection
of documents to be analyzed as a batch. 
See [service limitations][service_input_limitation] for the document, including document length limits, maximum batch size,
and supported text encoding.

### Operation on multiple documents
For each supported operation, the Text Analytics client provides method overloads to take a single document, a batch 
of documents as strings, or a batch of either `TextDocumentInput` or `DetectLanguageInput` objects. The overload 
taking the `TextDocumentInput` or `DetectLanguageInput` batch allows callers to give each document a unique ID, 
indicate that the documents in the batch are written in different languages, or provide a country hint about the 
language of the document.

### Return value
An operation result, such as `AnalyzeSentimentResult`, is the result of a Language service operation, containing a 
prediction or predictions about a single document and a list of warnings inside of it. An operation's result type also 
may optionally include information about the input document and how it was processed. An operation result contains a 
`isError` property that allows to identify if an operation executed was successful or unsuccessful for the given
document. When the operation results an error, you can simply call `getError()` to get `TextAnalyticsError` which 
contains the reason why it is unsuccessful. If you are interested in how many characters are in your document, 
or the number of operation transactions that have gone through, simply call `getStatistics()` to get the
`TextDocumentStatistics` which contains both information. 

### Return value collection
An operation result collection, such as `AnalyzeSentimentResultCollection`, which is the collection of 
the result of analyzing sentiment operation. It also includes the model version of the operation and statistics of 
the batch documents. 

**Note**: It is recommended to use the batch methods when working on production environments as they allow you to send one 
request with multiple documents. This is more performant than sending a request per each document.

## Examples
The following sections provide several code snippets covering some of the most common Language service tasks, including:

* [Analyze Sentiment](#analyze-sentiment "Analyze sentiment")
* [Detect Language](#detect-language "Detect language")
* [Extract Key Phrases](#extract-key-phrases "Extract key phrases")
* [Recognize Named Entities](#recognize-named-entities "Recognize named entities")
* [Recognize Personally Identifiable Information Entities](#recognize-personally-identifiable-information-entities "Recognize Personally Identifiable Information entities")
* [Recognize Linked Entities](#recognize-linked-entities "Recognize linked entities")
* [Analyze Healthcare Entities](#analyze-healthcare-entities "Analyze healthcare entities")
* [Analyze Multiple Actions](#analyze-multiple-actions "Analyze multiple actions")
* [Custom Entities Recognition](#custom-entities-recognition "Custom entities recognition")
* [Custom Text Classification](#custom-text-classification "Custom text classification")
* [Abstractive Text Summarization][abstractive_summary_action_sample]
* [Extractive Text Summarization][extractive_summary_action_sample]

### Text Analytics Client
Language service supports both synchronous and asynchronous client creation by using
`TextAnalyticsClientBuilder`,

``` java readme-sample-createTextAnalyticsClientWithKeyCredential
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

or

``` java readme-sample-createTextAnalyticsAsyncClientWithKeyCredential
TextAnalyticsAsyncClient textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

### Analyze sentiment
Run a predictive model to identify the positive, negative, neutral or mixed sentiment contained in the
provided document or batch of documents.
``` java readme-sample-analyzeSentiment
String document = "The hotel was dark and unclean. I like microsoft.";
DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(document);
System.out.printf("Analyzed document sentiment: %s.%n", documentSentiment.getSentiment());
documentSentiment.getSentences().forEach(sentenceSentiment ->
    System.out.printf("Analyzed sentence sentiment: %s.%n", sentenceSentiment.getSentiment()));
```

For samples on using the production recommended option `AnalyzeSentimentBatch` see [here][analyze_sentiment_sample].

To get more granular information about the opinions related to aspects of a product/service, also knows as Aspect-based
Sentiment Analysis in Natural Language Processing (NLP), see sample on sentiment analysis with opinion mining see 
[here][analyze_sentiment_with_opinion_mining_sample].

Please refer to the service documentation for a conceptual discussion of [sentiment analysis][sentiment_analysis].

### Detect language
Run a predictive model to determine the language that the provided document or batch of documents are written in.

```java readme-sample-detectLanguages
String document = "Bonjour tout le monde";
DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage(document);
System.out.printf("Detected language name: %s, ISO 6391 name: %s, confidence score: %f.%n",
    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getConfidenceScore());
```
For samples on using the production recommended option `DetectLanguageBatch` see [here][detect_language_sample].
Please refer to the service documentation for a conceptual discussion of [language detection][language_detection].

### Extract key phrases
Run a model to identify a collection of significant phrases found in the provided document or batch of documents.

```java readme-sample-extractKeyPhrases
String document = "My cat might need to see a veterinarian.";
System.out.println("Extracted phrases:");
textAnalyticsClient.extractKeyPhrases(document).forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
```
For samples on using the production recommended option `ExtractKeyPhrasesBatch` see [here][extract_key_phrases_sample].
Please refer to the service documentation for a conceptual discussion of [key phrase extraction][key_phrase_extraction].

### Recognize named entities
Run a predictive model to identify a collection of named entities in the provided document or batch of documents and 
categorize those entities into categories such as person, location, or organization.  For more information on available
categories, see [Named Entity Categories][named_entities_categories].

```java readme-sample-recognizeEntity
String document = "Satya Nadella is the CEO of Microsoft";
textAnalyticsClient.recognizeEntities(document).forEach(entity ->
    System.out.printf("Recognized entity: %s, category: %s, subcategory: %s, confidence score: %f.%n",
        entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
```
For samples on using the production recommended option `RecognizeEntitiesBatch` see [here][recognize_entities_sample].
Please refer to the service documentation for a conceptual discussion of [named entity recognition][named_entity_recognition].

### Recognize Personally Identifiable Information entities
Run a predictive model to identify a collection of Personally Identifiable Information(PII) entities in the provided 
document. It recognizes and categorizes PII entities in its input text, such as
Social Security Numbers, bank account information, credit card numbers, and more. This endpoint is only supported for
API versions v3.1-preview.1 and above.

```java readme-sample-recognizePiiEntity
String document = "My SSN is 859-98-0987";
PiiEntityCollection piiEntityCollection = textAnalyticsClient.recognizePiiEntities(document);
System.out.printf("Redacted Text: %s%n", piiEntityCollection.getRedactedText());
piiEntityCollection.forEach(entity -> System.out.printf(
    "Recognized Personally Identifiable Information entity: %s, entity category: %s, entity subcategory: %s,"
        + " confidence score: %f.%n",
    entity.getText(), entity.getCategory(), entity.getSubcategory(), entity.getConfidenceScore()));
```

For samples on using the production recommended option `RecognizePiiEntitiesBatch` see [here][recognize_pii_entities_sample].
Please refer to the service documentation for [supported PII entity types][pii_entity_recognition].

### Recognize linked entities
Run a predictive model to identify a collection of entities found in the provided document or batch of documents, 
and include information linking the entities to their corresponding entries in a well-known knowledge base.

```java readme-sample-recognizeLinkedEntity
String document = "Old Faithful is a geyser at Yellowstone Park.";
textAnalyticsClient.recognizeLinkedEntities(document).forEach(linkedEntity -> {
    System.out.println("Linked Entities:");
    System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
        linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(), linkedEntity.getDataSource());
    linkedEntity.getMatches().forEach(match ->
        System.out.printf("Text: %s, confidence score: %f.%n", match.getText(), match.getConfidenceScore()));
});
```
For samples on using the production recommended option `RecognizeLinkedEntitiesBatch` see [here][recognize_linked_entities_sample].
Please refer to the service documentation for a conceptual discussion of [entity linking][entity_linking].

### Analyze healthcare entities
Text Analytics for health is a containerized service that extracts and labels relevant medical information from 
unstructured texts such as doctor's notes, discharge summaries, clinical documents, and electronic health records.

- [Healthcare entities recognition][recognize_healthcare_entities_sample]

For more information see [How to: Use Text Analytics for health][healthcare].

### Custom entities recognition
Custom NER is one of the custom features offered by Azure Cognitive Service for Language. It is a cloud-based API 
service that applies machine-learning intelligence to enable you to build custom models for custom named entity
recognition tasks.

- [Custom entities recognition][recognize_custom_entities_sample]

For more information see [How to use: Custom Entities Recognition][custom_entities_recognition_overview].

### Custom text classification
Custom text classification is one of the custom features offered by Azure Cognitive Service for Language. It is a 
cloud-based API service that applies machine-learning intelligence to enable you to build custom models for text 
classification tasks.

- [Single label classification][single_label_classification_sample]

- [Multi label classification][multi_label_classification_sample]

For more information see [How to use: Custom Text Classification][custom_text_classification_overview].

### Analyze multiple actions
The `Analyze` functionality allows choosing which of the supported Language service features to execute in the same
set of documents. Currently, the supported features are:

- Named Entities Recognition
- PII Entities Recognition
- Linked Entity Recognition
- Key Phrase Extraction
- Sentiment Analysis
- Healthcare Analysis
- Custom Entity Recognition (API version 2022-05-01 and newer)
- Custom Single-Label Classification (API version 2022-05-01 and newer)
- Custom Multi-Label Classification (API version 2022-05-01 and newer)
- Abstractive Text Summarization (API version 2023-04-01 and newer)
- Extractive Text Summarization (API version 2023-04-01 and newer)


Sample: [Multiple action analysis][multiple_actions_analysis_sample]

For more examples, such as asynchronous samples, refer to [here][samples_readme].

## Troubleshooting
### General
Text Analytics clients raise exceptions. For example, if you try to detect the languages of a batch of text with same 
document IDs, `400` error is return that indicating bad request. In the following code snippet, the error is handled 
gracefully by catching the exception and display the additional information about the error.

```java readme-sample-handlingException
List<DetectLanguageInput> documents = Arrays.asList(
    new DetectLanguageInput("1", "This is written in English.", "us"),
    new DetectLanguageInput("1", "Este es un documento  escrito en Español.", "es")
);

try {
    textAnalyticsClient.detectLanguageBatchWithResponse(documents, null, Context.NONE);
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
}
```

### Enable client logging
You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][LogLevels].

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure 
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL 
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides 
better performance compared to the default SSL implementation within the JDK. For more information, including how to 
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
- Samples are explained in detail [here][samples_readme].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[aad_authorization]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[aad_credential]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[api_reference_doc]: https://aka.ms/azsdk-java-textanalytics-ref-docs
[authentication]: https://docs.microsoft.com/azure/cognitive-services/authentication
[azure_cli_doc]: https://learn.microsoft.com/cli/azure/
[azure_cli_endpoint]: https://docs.microsoft.com/cli/azure/cognitiveservices/account?view=azure-cli-latest#az-cognitiveservices-account-show
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_key_credential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureKeyCredential.java
[azure_portal]: https://ms.portal.azure.com
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[create_new_resource_in_azure_portal]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#create-a-new-azure-cognitive-services-resource
[create_new_resource_in_azure_cli]: https://learn.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli?tabs=windows
[custom_entities_recognition_overview]: https://docs.microsoft.com/azure/cognitive-services/language-service/custom-named-entity-recognition/overview
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[custom_text_classification_overview]: https://docs.microsoft.com/azure/cognitive-services/language-service/custom-text-classification/overview
[grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[healthcare]: https://docs.microsoft.com/azure/cognitive-services/language-service/text-analytics-for-health/overview?tabs=ner
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[key]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource
[key_phrase_extraction]: https://docs.microsoft.com/azure/cognitive-services/language-service/key-phrase-extraction/overview
[language_detection]: https://docs.microsoft.com/azure/cognitive-services/language-service/language-detection/overview
[language_regional_support]: https://docs.microsoft.com/azure/cognitive-services/language-service/language-detection/language-support
[named_entity_recognition]: https://docs.microsoft.com/azure/cognitive-services/language-service/named-entity-recognition/overview
[named_entities_categories]: https://docs.microsoft.com/azure/cognitive-services/language-service/named-entity-recognition/concepts/named-entity-categories
[entity_linking]: https://docs.microsoft.com/azure/cognitive-services/language-service/entity-linking/overview
[pii_entity_recognition]: https://docs.microsoft.com/azure/cognitive-services/language-service/personally-identifiable-information/overview
[package]: https://central.sonatype.com/artifact/com.azure/azure-ai-textanalytics
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/language-service/overview
[register_AAD_application]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[service_access]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[service_input_limitation]: https://docs.microsoft.com/azure/cognitive-services/language-service/overview#data-limits
[sentiment_analysis]: https://docs.microsoft.com/azure/cognitive-services/language-service/sentiment-opinion-mining/overview
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/textanalytics/azure-ai-textanalytics/src
[language_service_account]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[text_analytics_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/main/java/com/azure/ai/textanalytics/TextAnalyticsAsyncClient.java
[text_analytics_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/main/java/com/azure/ai/textanalytics/TextAnalyticsClient.java
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication
[LogLevels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java

[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/README.md
[abstractive_summary_action_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AbstractiveSummarization.java
[analyze_sentiment_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/AnalyzeSentimentBatchDocuments.java
[analyze_sentiment_with_opinion_mining_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/AnalyzeSentimentWithOpinionMining.java
[detect_language_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/DetectLanguageBatchDocuments.java
[extract_key_phrases_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/ExtractKeyPhrasesBatchDocuments.java
[extractive_summary_action_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/ExtractiveSummarization.java
[multi_label_classification_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/MultiLabelClassifyDocument.java
[multiple_actions_analysis_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AnalyzeActions.java
[recognize_entities_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeEntitiesBatchDocuments.java
[recognize_pii_entities_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizePiiEntitiesBatchDocuments.java
[recognize_linked_entities_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/batch/RecognizeLinkedEntitiesBatchDocuments.java
[recognize_healthcare_entities_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/AnalyzeHealthcareEntities.java
[recognize_custom_entities_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/RecognizeCustomEntities.java
[single_label_classification_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/textanalytics/azure-ai-textanalytics/src/samples/java/com/azure/ai/textanalytics/lro/SingleLabelClassifyDocument.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftextanalytics%2Fazure-ai-textanalytics%2FREADME.png)
