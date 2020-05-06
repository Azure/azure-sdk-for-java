# Azure Text Analytics client library for Java
Text Analytics is a cloud-based service that provides advanced natural language processing over raw text, 
and includes six main functions:

- Sentiment Analysis
- Language Detection
- Key Phrase Extraction
- Named Entity Recognition
- Linked Entity Recognition

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Text Analytics account][text_analytics_account] to use this package.

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-ai-textanalytics;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-textanalytics</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create a Text Analytics resource
Text Analytics supports both [multi-service and single-service access][service_access]. Create a Cognitive Services 
resource if you plan to access multiple cognitive services under a single endpoint/key. For Text Analytics access only,
create a Text Analytics resource.

You can create either resource using the 

**Option 1:** [Azure Portal][create_new_resource] 

**Option 2:** [Azure CLI][azure_cli]

Below is an example of how you can create a Text Analytics resource using the CLI:

```bash
# Create a new resource group to hold the text analytics resource -
# if using an existing resource group, skip this step
az group create --name my-resource-group --location westus2
```

```bash
# Create text analytics
az cognitiveservices account create \
    --name text-analytics-resource \
    --resource-group my-resource-group \
    --kind TextAnalytics \
    --sku F0 \
    --location westus2 \
    --yes
```
### Authenticate the client
In order to interact with the Text Analytics service, you will need to create an instance of the `TextAnalyticsClient` 
class. You will need an **endpoint** and either an **API key** or **AAD TokenCredential** to instantiate a client 
object. And they can be found in the [Azure Portal][azure_portal] under the "Quickstart" in your created
Text Analytics resource. See the full details regarding [authentication][authentication] of Cognitive Services.

#### Get credentials
The authentication credential may be provided as the API key to your resource or as a token from Azure Active Directory.


##### **Option 1**: Create TextAnalyticsClient with AzureKeyCredential
To use AzureKeyCredential authentication, provide the [key][key] as a string to the [AzureKeyCredential][azure_key_credential]. This can be found in the [Azure Portal][azure_portal] 
   under the "Quickstart" section or by running the following Azure CLI command:

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```
Use the key as the credential parameter to authenticate the client:
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L43-L46 -->
```java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
The Azure Text Analytics client library provides a way to **rotate the existing key**.

<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L73-L79 -->
```java
AzureKeyCredential credential = new AzureKeyCredential("{key}");
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .credential(credential)
    .endpoint("{endpoint}")
    .buildClient();

credential.update("{new_key}");
```
##### **Option 2**: Create TextAnalyticsClient with Azure Active Directory Credential
To use an [Azure Active Directory (AAD) token credential][aad_credential],
provide an instance of the desired credential type obtained from the [azure-identity][azure_identity] library.
Note that regional endpoints do not support AAD authentication. Create a [custom subdomain][custom_subdomain] 
name for your resource in order to use this type of authentication.

Authentication with AAD requires some initial setup:
* [Install azure-identity][install_azure_identity]
* [Register a new AAD application][register_AAD_application]
* [Grant access][grant_access] to Text Analytics by assigning the `"Cognitive Services User"` role to your service principal.

After setup, you can choose which type of [credential][credential_type] from azure.identity to use. 
As an example, [DefaultAzureCredential][default_azure_credential]
can be used to authenticate the client:

Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: 
AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

Use the returned token credential to authenticate the client:
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L63-L66 -->
```java
TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .endpoint("{endpoint}")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

## Key concepts
### Client
The Text Analytics client library provides a [TextAnalyticsClient][text_analytics_sync_client] and 
[TextAnalyticsAsyncClient][text_analytics_async_client] to do analysis on batches of documents. It provides both synchronous and
asynchronous operations to access a specific use of Text Analytics, such as language detection or key phrase extraction.

### Input
A **text input**, also called a **document**, is a single unit of document to be analyzed by the predictive models
in the Text Analytics service. Operations on Text Analytics client may take a single document or a collection
of documents to be analyzed as a batch. 
See [service limitations][service_input_limitation] for the document, including document length limits, maximum batch size,
and supported text encoding.

### Return value
An operation result, such as `AnalyzeSentimentResult`, is the result of a Text Analytics operation, containing a 
prediction or predictions about a single document. An operation's result type also may optionally include information
about the input document and how it was processed. An operation result contains a `isError` property that allows to
identify if an operation executed was successful or unsuccessful for the given document. When the operation results
an error, you can simply call `getError()` to get `TextAnalyticsError` which contains the reason why it is unsuccessful. 
If you are interested in how many characters are in your document, or the number of operation transactions that have gone
through, simply call `getStatistics()` to get the `TextDocumentStatistics` which contains both information.

### Return value collection
An operation result collection, such as `DocumentResultCollection<AnalyzeSentimentResult>`, which is the collection of 
the result of a Text Analytics analyzing sentiment operation. `DocumentResultCollection` includes the model version of
the operation and statistics of the batch documents. Since `DocumentResultCollection<T>` extends `IterableStream<T>`,
the list of item can be retrieved by streaming or iterating the list.

### Operation on multiple documents
For each supported operation, the Text Analytics client provides method overloads to take a single document, a batch 
of documents as strings, or a batch of either `TextDocumentInput` or `DetectLanguageInput` objects. The overload 
taking the `TextDocumentInput` or `DetectLanguageInput` batch allows callers to give each document a unique ID, 
indicate that the documents in the batch are written in different languages, or provide a country hint about the 
language of the document.

**Note**: It is recommended to use the batch methods when working on production environments as they allow you to send one 
request with multiple documents. This is more performant than sending a request per each document.

The following are types of text analysis that the service offers:

1. [Sentiment Analysis][sentiment_analysis]
    
    Use sentiment analysis to find out what customers think of your brand or topic by analyzing raw text for clues about positive or negative sentiment.
    The returned scores represent the model's confidence that the text is either positive, negative, or neutral. Higher values signify higher confidence.
    Sentiment analysis returns scores and labels at a document and sentence level.

2. [Named Entity Recognition][named_entity_recognition]
    
    Use named entity recognition (NER) to identify different entities in text and categorize them into pre-defined classes, or types.
    Entity recognition in the client library provides three different methods depending on what you are interested in.
    * `recognizeEntities()` can be used to identify and categorize entities in your text as people, places, organizations, date/time, quantities, percentages, currencies, and more.
    * `recognizeLinkedEntities()` can be used to identify and disambiguate the identity of an entity found in text (For example, determining whether
    "Mars" is being used as the planet or as the Roman god of war). This process uses Wikipedia as the knowledge base to which recognized entities are linked.
    
    See a full list of [Named Entity Recognition Types][named_entity_recognition_types].

3. [Language Detection][language_detection]
    
    Detect the language of the document and report a single language code for every document submitted on the request. 
    The language code is paired with a score indicating the strength of the score.
    A wide range of languages, variants, dialects, and some regional/cultural languages are supported -
    see [supported languages][supported_languages] for full details.

4. [Key Phrase Extraction][key_phrase_extraction]
    
    Extract key phrases to quickly identify the main points in text. 
    For example, for the document "The food was delicious and there were wonderful staff", the main talking points returned: "food" and "wonderful staff".

See [Language and regional support][language_regional_support] for what is currently available for each operation.

## Examples
The following sections provide several code snippets covering some of the most common text analytics tasks, including:

### Text Analytics Client
Text analytics support both synchronous and asynchronous client creation by using
`TextAnalyticsClientBuilder`,

<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L43-L46 -->
``` java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L53-L56 -->
``` java
TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

### Analyze sentiment
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L102-L106 -->
```java
String document = "The hotel was dark and unclean. I like microsoft.";
DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(document);
System.out.printf("Analyzed document sentiment: %s.%n", documentSentiment.getSentiment());
documentSentiment.getSentences().forEach(sentenceSentiment ->
    System.out.printf("Analyzed sentence sentiment: %s.%n", sentenceSentiment.getSentiment()));
```

### Detect language
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L113-L116 -->
```java
String document = "Bonjour tout le monde";
DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage(document);
System.out.printf("Detected language name: %s, ISO 6391 name: %s, score: %f.%n",
    detectedLanguage.getName(), detectedLanguage.getIso6391Name(), detectedLanguage.getScore());
```

### Recognize entity
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L123-L126 -->
```java
String document = "Satya Nadella is the CEO of Microsoft";
textAnalyticsClient.recognizeEntities(document).forEach(entity ->
    System.out.printf("Recognized entity: %s, category: %s, subCategory: %s, score: %f.%n",
        entity.getText(), entity.getCategory(), entity.getSubCategory(), entity.getConfidenceScore()));
```

### Recognize linked entity
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L133-L140 -->

```java
String document = "Old Faithful is a geyser at Yellowstone Park.";
textAnalyticsClient.recognizeLinkedEntities(document).forEach(linkedEntity -> {
    System.out.println("Linked Entities:");
    System.out.printf("Name: %s, entity ID in data source: %s, URL: %s, data source: %s.%n",
        linkedEntity.getName(), linkedEntity.getDataSourceEntityId(), linkedEntity.getUrl(), linkedEntity.getDataSource());
    linkedEntity.getLinkedEntityMatches().forEach(linkedEntityMatch ->
        System.out.printf("Text: %s, score: %f.%n", linkedEntityMatch.getText(), linkedEntityMatch.getConfidenceScore()));
});
```
### Extract key phrases
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L147-L149 -->
```java
String document = "My cat might need to see a veterinarian.";
System.out.println("Extracted phrases:");
textAnalyticsClient.extractKeyPhrases(document).forEach(keyPhrase -> System.out.printf("%s.%n", keyPhrase));
```

The above examples cover the scenario of having a single document as input.
For more examples, such as batch operation, refer to [here][samples_readme].

## Troubleshooting
### General
Text Analytics clients raise exceptions. For example, if you try to detect the languages of a batch of text with same 
document IDs, `400` error is return that indicating bad request. In the following code snippet, the error is handled 
gracefully by catching the exception and display the additional information about the error.

<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L86-L95 -->
```java
List<DetectLanguageInput> documents = Arrays.asList(
    new DetectLanguageInput("1", "This is written in English.", "us"),
    new DetectLanguageInput("1", "Este es un documento  escrito en Español.", "es")
);

try {
    textAnalyticsClient.detectLanguageBatch(documents, null, Context.NONE);
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
[azure_key_credential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureKeyCredential.java
[aad_credential]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[key]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource
[api_reference_doc]: https://aka.ms/azsdk-java-textanalytics-ref-docs
[authentication]: https://docs.microsoft.com/azure/cognitive-services/authentication
[azure_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli?tabs=windows
[azure_identity]: https://github.com/Azure/azure-sdk-for-python/tree/master/sdk/identity/azure-identity#credentials
[azure_portal]: https://ms.portal.azure.com
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[create_new_resource]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#create-a-new-azure-cognitive-services-resource
[credential_type]: https://github.com/Azure/azure-sdk-for-python/tree/master/sdk/identity/azure-identity#credentials
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[default_azure_credential]: https://github.com/Azure/azure-sdk-for-python/tree/master/sdk/identity/azure-identity#defaultazurecredential
[grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[install_azure_identity]: https://github.com/Azure/azure-sdk-for-python/tree/master/sdk/identity/azure-identity#install-the-package
[key_phrase_extraction]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-keyword-extraction
[language_detection]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-language-detection
[language_regional_support]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/language-support
[named_entity_recognition]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-entity-linking
[named_entity_recognition_types]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/named-entity-types?tabs=personal
[package]: https://mvnrepository.com/artifact/com.azure/azure-ai-textanalytics
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview
[register_AAD_application]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[samples_readme]: src/samples/README.md
[service_access]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[service_input_limitation]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview#data-limits
[sentiment_analysis]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-sentiment-analysis
[source_code]: src
[supported_language]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/language-support#language-detection
[text_analytics_account]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[text_analytics_async_client]: src/main/java/com/azure/ai/textanalytics/TextAnalyticsAsyncClient.java
[text_analytics_sync_client]: src/main/java/com/azure/ai/textanalytics/TextAnalyticsClient.java
[LogLevels]: ../../core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftextanalytics%2Fazure-ai-textanalytics%2FREADME.png)
