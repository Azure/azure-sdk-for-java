# Azure Text Analytics client library for Java
Text Analytics is a cloud-based service that provides advanced natural language processing over raw text, 
and includes six main functions:

- Language Detection
- Sentiment Analysis
- Key Phrase Extraction
- Named Entity Recognition
- Recognition of Personally Identifiable Information 
- Linked Entity Recognition

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Text Analytics account](https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows) to use this package.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-textanalytics;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-textanalytics</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
Text Analytics to use Netty HTTP client. 

[//]: # ({x-version-update-start;com.azure:azure-core-http-netty;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-netty</artifactId>
    <version>1.1.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Alternate HTTP Client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-ai-textanalytics;current})
```xml
<!-- Add Text Analytics dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-textanalytics</artifactId>
    <version>1.0.0-beta.1</version>
    <exclusions>
      <exclusion>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-http-netty</artifactId>
      </exclusion>
    </exclusions>
</dependency>
```
[//]: # ({x-version-update-end})

[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;dependency})
```xml
<!-- Add OkHTTP client to use with Text Analytics -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-a-client), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L159-L162 -->
```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

### Create a Text Analytics resource
Text Analytics supports both [multi-service and single-service access](https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows). Create a Cognitive Services resource if you plan
to access multiple cognitive services under a single endpoint/key. For Text Analytics access only, create a Text Analytics resource.

You can create either resource using the
[Azure Portal](https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#create-a-new-azure-cognitive-services-resource)
or [Azure CLI](https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli?tabs=windows).
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
In order to interact with the Text Analytics service, you'll need to create an instance of the [TextAnalyticsClient](#create-a-client) class. You would need an **endpoint** and **subscription key** to instantiate a client object.

#### Get credentials
##### Types of credentials
The `subscriptionKey` parameter may be provided as the subscription key to your resource or as a token from Azure Active Directory.
See the full details regarding [authentication](https://docs.microsoft.com/azure/cognitive-services/authentication) of 
cognitive services.

1. To use a [subscription key](https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource), 
   provide the key as a string. This can be found in the Azure Portal under the "Quickstart" 
   section or by running the following Azure CLI command:

    ```bash
    az cognitiveservices account keys list --name "resource-name" --resource-group "resource-group-name"
    ```
    
    Use the key as the credential parameter to authenticate the client:
    <!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L29-L32 -->
    ```java
    TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
        .subscriptionKey(SUBSCRIPTION_KEY)
        .endpoint(ENDPOINT)
        .buildClient();
    ```

2. To use an [Azure Active Directory (AAD) token credential](https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory),
   provide an instance of the desired credential type obtained from the
   [azure-identity](https://github.com/Azure/azure-sdk-for-python/tree/master/sdk/identity/azure-identity#credentials) library.
   Note that regional endpoints do not support AAD authentication. Create a [custom subdomain](https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain) 
   name for your resource in order to use this type of authentication.

   Authentication with AAD requires some initial setup:
   * [Install azure-identity](https://github.com/Azure/azure-sdk-for-python/tree/master/sdk/identity/azure-identity#install-the-package)
   * [Register a new AAD application](https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal)
   * [Grant access](https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal) to Text Analytics by assigning the `"Cognitive Services User"` role to your service principal.
   
   After setup, you can choose which type of [credential](https://github.com/Azure/azure-sdk-for-python/tree/master/sdk/identity/azure-identity#credentials) from azure.identity to use. 
   As an example, [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-python/tree/master/sdk/identity/azure-identity#defaultazurecredential)
   can be used to authenticate the client:

   Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: 
   AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET

   Use the returned token credential to authenticate the client:
   <!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L49-L52 -->
    ```java
    TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
        .endpoint(ENDPOINT)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();
    ```

#### Create a Client
The Azure Text Analytics client library for Java allows you to engage with the Text Analytics service to 
analyze sentiment, recognize entities, detect language, and extract key phrases from text.
To create a client object, you will need the cognitive services or text analytics endpoint to 
your resource and a subscription key that allows you access:

<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L29-L32 -->
```java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .subscriptionKey(SUBSCRIPTION_KEY)
    .endpoint(ENDPOINT)
    .buildClient();
```

## Key concepts

### Text Input
A text input, sometimes called a `document`, is a single unit of input to be analyzed by the predictive models
in the Text Analytics service. Operations on Text Analytics client may take a single text input or a collection
of inputs to be analyzed as a batch.

### Operation Result
An operation result, such as `AnalyzeSentimentResult`, is the result of a Text Analytics operation, containing a 
prediction or predictions about a single text input. An operation's result type also may optionally include information
about the input document and how it was processed.

### Operation Result Collection
An operation result collection, such as `DocumentResultCollection<AnalyzeSentimentResult>`, which is the collection of 
the result of a Text Analytics analyzing sentiment operation. `DocumentResultCollection` includes the model version of
the operation and statistics of the batch documents. Since `DocumentResultCollection<T>` extends `IterableStream<T>`,
the list of item can be retrieved by streaming or iterating the list.

### Operation Overloads
For each supported operation, the Text Analytics client provides method overloads to take a single text input, a batch 
of text inputs as strings, or a batch of either `TextDocumentInput` or `DetectLanguageInput` objects. The overload 
taking the `TextDocumentInput` or `DetectLanguageInput` batch allows callers to give each document a unique ID, or 
indicate that the documents in the batch are written in different languages.

The following are types of text analysis that the service offers:

1. [Sentiment Analysis](https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-sentiment-analysis)
    
    Use sentiment analysis to find out what customers think of your brand or topic by analyzing raw text for clues about positive or negative sentiment.
    Scores closer to `1` indicate positive sentiment, while scores closer to `0` indicate negative sentiment.
    Sentiment analysis returns scores and labels at a document and sentence level.

2. [Named Entity Recognition](https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-entity-linking)
    
    Use named entity recognition (NER) to identify different entities in text and categorize them into pre-defined classes, or types.
    Entity recognition in the client library provides three different methods depending on what you are interested in.
    * `recognizeEntities()` can be used to identify and categorize entities in your text as people, places, organizations, date/time, quantities, percentages, currencies, and more.
    * `recognizePiiEntities()` can be used to recognize personally identifiable information such as SSNs and bank account numbers.
    * `recognizeLinkedEntities()` can be used to identify and disambiguate the identity of an entity found in text (For example, determining whether
    "Mars" is being used as the planet or as the Roman god of war). This process uses Wikipedia as the knowledge base to which recognized entities are linked.
    
    See a full list of [Named Entity Recognition Types](https://docs.microsoft.com/azure/cognitive-services/text-analytics/named-entity-types?tabs=personal).

3. [Language Detection](https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-language-detection)
    
    Detect the language of the input text and report a single language code for every document submitted on the request. 
    The language code is paired with a score indicating the strength of the score.
    A wide range of languages, variants, dialects, and some regional/cultural languages are supported -
    see [supported languages](https://docs.microsoft.com/azure/cognitive-services/text-analytics/language-support#language-detection) for full details.

4. [Key Phrase Extraction](https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-keyword-extraction)
    
    Extract key phrases to quickly identify the main points in text. 
    For example, for the input text "The food was delicious and there were wonderful staff", the main talking points returned: "food" and "wonderful staff".

See [Language and regional support](https://docs.microsoft.com/azure/cognitive-services/text-analytics/language-support) for what is currently available for each operation.

## Examples
The following sections provide several code snippets covering some of the most common text analytics tasks, including:

### Text Analytics Client
Text analytics support both synchronous and asynchronous client creation by using
`TextAnalyticsClientBuilder`,

<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L29-L32 -->
``` java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .subscriptionKey(SUBSCRIPTION_KEY)
    .endpoint(ENDPOINT)
    .buildClient();
```
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L39-L42 -->
``` java
TextAnalyticsAsyncClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .subscriptionKey(SUBSCRIPTION_KEY)
    .endpoint(ENDPOINT)
    .buildAsyncClient();
```

### Detect language
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L59-L71 -->
```java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .subscriptionKey(SUBSCRIPTION_KEY)
    .endpoint(ENDPOINT)
    .buildClient();

String inputText = "Bonjour tout le monde";

for (DetectedLanguage detectedLanguage : textAnalyticsClient.detectLanguage(inputText).getDetectedLanguages()) {
    System.out.printf("Detected languages name: %s, ISO 6391 Name: %s, Score: %s.%n",
        detectedLanguage.getName(),
        detectedLanguage.getIso6391Name(),
        detectedLanguage.getScore());
}
```

### Recognize entity
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L78-L92 -->
```java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .subscriptionKey(SUBSCRIPTION_KEY)
    .endpoint(ENDPOINT)
    .buildClient();

String text = "Satya Nadella is the CEO of Microsoft";

for (NamedEntity entity : textAnalyticsClient.recognizeEntities(text).getNamedEntities()) {
    System.out.printf(
        "Recognized Named Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
        entity.getText(),
        entity.getType(),
        entity.getSubtype(),
        entity.getScore());
}
```

### Recognize PII(Personally Identifiable Information) entity
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L99-L114 -->
```java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .subscriptionKey(SUBSCRIPTION_KEY)
    .endpoint(ENDPOINT)
    .buildClient();

// The text that need be analysed.
String text = "My SSN is 555-55-5555";

for (NamedEntity entity : textAnalyticsClient.recognizePiiEntities(text).getNamedEntities()) {
    System.out.printf(
        "Recognized PII Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
        entity.getText(),
        entity.getType(),
        entity.getSubtype(),
        entity.getScore());
}
```

### Recognize linked entity
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L121-L134 -->

```java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .subscriptionKey(SUBSCRIPTION_KEY)
    .endpoint(ENDPOINT)
    .buildClient();

// The text that need be analysed.
String text = "Old Faithful is a geyser at Yellowstone Park.";

for (LinkedEntity linkedEntity : textAnalyticsClient.recognizeLinkedEntities(text).getLinkedEntities()) {
    System.out.printf("Recognized Linked Entity: %s, Url: %s, Data Source: %s.%n",
        linkedEntity.getName(),
        linkedEntity.getUrl(),
        linkedEntity.getDataSource());
}
```

### Analyze sentiment
<!-- embedme ./src/samples/java/com/azure/ai/textanalytics/ReadmeSamples.java#L141-L152 -->

```java
TextAnalyticsClient textAnalyticsClient = new TextAnalyticsClientBuilder()
    .subscriptionKey(SUBSCRIPTION_KEY)
    .endpoint(ENDPOINT)
    .buildClient();

String text = "The hotel was dark and unclean.";

for (TextSentiment textSentiment : textAnalyticsClient.analyzeSentiment(text).getSentenceSentiments()) {
    System.out.printf(
        "Analyzed Sentence Sentiment class: %s.%n",
        textSentiment.getTextSentimentClass());
}
```

## Troubleshooting
## General

## Next steps
- Samples are explained in detail [here][samples_readme].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[azure_subscription]: https://azure.microsoft.com/free
[api_reference_doc]: https://azure.github.io/azure-sdk-for-java/textanalytics.html
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[package]: https://mvnrepository.com/artifact/com.azure/azure-ai-textanalytics
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/text-analytics/overview
[samples_readme]: src/samples/README.md
[source_code]: src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftextanalytics%2Fazure-ai-textanalytics%2FREADME.png)
