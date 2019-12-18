# Azure Core Text Analytics client library for Java
Text Analytics is a cloud-based service that provides advanced natural language processing over raw text, and includes four main functions

- Sentiment Analysis
- Named Entity Recognition
- Language Detection
- Key Phrase Extraction

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

[//]: # ({x-version-update-start;com.azure:azure-core-http-netty;current})
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
[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```xml
<!-- Add OkHTTP client to use with Text Analytics -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.1.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-a-client), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

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
In order to interact with the Text Analytics service, you'll need to create an instance of the [TextAnalyticsClient](#create-ta-client) class. You would need an **endpoint** and **subscription key** to instantiate a client object.

#### Get credentials
##### Types of credentials
The `subscriptionKey` parameter may be provided as the subscription key to your resource or as a token from Azure Active Directory.
See the full details regarding [authentication](https://docs.microsoft.com/azure/cognitive-services/authentication) of 
cognitive services.

1. To use a [subscription key](https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource), 
   provide the key as a string. This can be found in the Azure Portal under the "Quickstart" 
   section or by running the following Azure CLI command:

    ```az cognitiveservices account keys list --name "resource-name" --resource-group "resource-group-name"```
    
    Use the key as the credential parameter to authenticate the client:
    ```java
    TextAnalyticsClient client = new TextAnalyticsClientBuilder()
        .subscriptionKey("subscription-key")
        .endpoint("https://servicename.cognitiveservices.azure.com/")
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
    ```java
    TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .endpoint("https://servicename.cognitiveservices.azure.com/")
            .buildClient();
    ```

#### Create a Client
The Azure Text Analytics client library for Java allows you to engage with the Text Analytics service to 
analyze sentiment, recognize entities, detect language, and extract key phrases from text.
To create a client object, you will need the cognitive services or text analytics endpoint to 
your resource and a subscription key that allows you access:

```java
// Instantiate a client that will be used to call the service.
TextAnalyticsClient client = new TextAnalyticsClientBuilder()
    .subscriptionKey("subscription-key")
    .endpoint("https://servicename.cognitiveservices.azure.com/")
    .buildClient();
```

## Key concepts

The following are types of text analysis that the service offers:

1. [Sentiment Analysis](https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-sentiment-analysis)
    
    Use sentiment analysis to find out what customers think of your brand or topic by analyzing raw text for clues about positive or negative sentiment.
    Scores closer to `1` indicate positive sentiment, while scores closer to `0` indicate negative sentiment.
    Sentiment analysis returns scores and labels at a document and sentence level.

2. [Named Entity Recognition](https://docs.microsoft.com/azure/cognitive-services/text-analytics/how-tos/text-analytics-how-to-entity-linking)
    
    Use named entity recognition (NER) to identify different entities in text and categorize them into pre-defined classes, or types.
    Entity recognition in the client library provides three different methods depending on what you are interested in.
    * `recognize_entities()` can be used to identify and categorize entities in your text as people, places, organizations, date/time, quantities, percentages, currencies, and more.
    * `recognize_pii_entities()` can be used to recognize personally identifiable information such as SSNs and bank account numbers.
    * `recognize_linked_entities()` can be used to identify and disambiguate the identity of an entity found in text (For example, determining whether
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

### Text Analytics Client
``` java
TextAnalyticsClient client = new TextAnalyticsClientBuilder()
    .subscriptionKey("subscription-key")
    .endpoint("https://servicename.cognitiveservices.azure.com/")
    .buildClient();
```

``` java
TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
    .subscriptionKey("subscription-key")
    .endpoint("https://servicename.cognitiveservices.azure.com/")
    .buildAsyncClient();
```

## Examples
The following sections provide several code snippets covering some of the most common text analytics tasks, including:

### Detect language
Detect language in a batch of documents.

```java
TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
    .subscriptionKey("subscription-key")
    .endpoint("https://servicename.cognitiveservices.azure.com/")
    .buildAsyncClient();

String inputText = "Bonjour tout le monde";

for(DetectedLanguage detectedLanguage : client.detectLanguage(text, "US").getDetectedLanguages()) {
    System.out.printf("Other detected languages: %s, ISO 6391 Name: %s, Score: %s.%n",
        detectedLanguage.getName(),
        detectedLanguage.getIso6391Name(),
        detectedLanguage.getScore());
}
```

### Recognize entity
```java
TextAnalyticsClient client = new TextAnalyticsClientBuilder()
    .subscriptionKey("subscription-key")
    .endpoint("https://servicename.cognitiveservices.azure.com/")
    .buildClient();

String text = "Satya Nadella is the CEO of Microsoft";

for (NamedEntity entity : client.recognizeEntities(text).getNamedEntities()) {
    System.out.printf(
        "Recognized NamedEntity: %s, Type: %s, Subtype: %s, Score: %s.%n",
        entity.getText(),
        entity.getType(),
        entity.getSubtype(),
        entity.getOffset(),
        entity.getLength(),
        entity.getScore());
}
```

### Recognize PII(Personal Information Identification) entity
```java
TextAnalyticsClient client = new TextAnalyticsClientBuilder()
    .subscriptionKey("subscription-key")
    .endpoint("https://servicename.cognitiveservices.azure.com/")
    .buildClient();

// The text that need be analysed.
String text = "My SSN is 555-55-5555";

for (NamedEntity entity : client.recognizePiiEntities(text).getNamedEntities()) {
    System.out.printf(
        "Recognized PII Entity: %s, Type: %s, Subtype: %s, Score: %s.%n",
        entity.getText(),
        entity.getType(),
        entity.getSubtype(),
        entity.getScore()));
}
```

### Recognize linked entity
```java
TextAnalyticsClient client = new TextAnalyticsClientBuilder()
    .subscriptionKey("subscription-key")
    .endpoint("https://servicename.cognitiveservices.azure.com/")
    .buildClient();

// The text that need be analysed.
String text = "Old Faithful is a geyser at Yellowstone Park.";

for (LinkedEntity linkedEntity : client.recognizeLinkedEntities(text).getLinkedEntities()) {
    System.out.printf("Recognized Linked NamedEntity: %s, URL: %s, Data Source: %s.%n",
        linkedEntity.getName(),
        linkedEntity.getUri(),
        linkedEntity.getDataSource());
}
```

### Analyze sentiment
```java
TextAnalyticsClient client = new TextAnalyticsClientBuilder()
    .subscriptionKey("subscription-key")
    .endpoint("https://servicename.cognitiveservices.azure.com/")
    .buildClient();

String text = "The hotel was dark and unclean.";

for (TextSentiment textSentiment : client.analyzeSentiment(text).getSentenceSentiments()) {
    System.out.printf(
        "Recognized Sentence TextSentiment: %s.%n",
        textSentiment.getTextSentimentClass());
}
```

## Troubleshooting
## General
// TODO (savaity) update exceptions

## Next steps
- Samples are explained in detail [here][samples_readme].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[samples_readme]: src/samples/README.md
[source_code]: src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftextanalytics%2Fazure-ai-textanalytics%2FREADME.png)
