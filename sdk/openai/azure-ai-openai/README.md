# Azure OpenAI client library for Java

Azure OpenAI is a managed service that allows developers to deploy, tune, and generate content from OpenAI models on 
Azure resources.

The Azure OpenAI client library for Java is an adaptation of OpenAI's REST APIs that provides an idiomatic interface 
and rich integration with the rest of the Azure SDK ecosystem.

Use the client library for Azure OpenAI to:

* [Create a completion for text][microsoft_docs_openai_completion]
* [Create a text embedding for comparisons][microsoft_docs_openai_embedding]

[Source code][source_code] | [API reference documentation][docs] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-openai;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-openai</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

In order to interact with the Azure OpenAI service you'll need to create an instance of client class,
[OpenAIAsyncClient][openai_client_async] or [OpenAIClient][openai_client_sync] by using 
[OpenAIClientBuilder][openai_client_builder]. To configure a client for use with 
Azure OpenAI, provide a valid endpoint URI to an Azure OpenAI resource along with a corresponding key credential,
token credential, or [Azure Identity][azure_identity] credential that's authorized to use the Azure OpenAI resource. 

#### Create a Azure OpenAI client with key credential
Get Azure OpenAI `key` credential from the Azure Portal.

```java readme-sample-createSyncClientKeyCredential
OpenAIClient client = new OpenAIClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
or
```java readme-sample-createAsyncClientKeyCredential
OpenAIAsyncClient client = new OpenAIClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

#### Create an Azure OpenAI client with Azure Active Directory credential
Azure SDK for Java supports an Azure Identity package, making it easy to get credentials from Microsoft identity
platform.

Authentication with AAD requires some initial setup:
* Add the Azure Identity package

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.8.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

After setup, you can choose which type of [credential][azure_identity_credential_type] from azure.identity to use.
As an example, [DefaultAzureCredential][wiki_identity] can be used to authenticate the client:
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables:
`AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_CLIENT_SECRET`.

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with OpenAI service, please
refer to [the associated documentation][aad_authorization].

```java readme-sample-createOpenAIClientWithAAD
TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
OpenAIClient client = new OpenAIClientBuilder()
    .credential(defaultCredential)
    .endpoint("{endpoint}")
    .buildClient();
```

#### Create a client with proxy options
Create an OpenAI client with proxy options.
```java readme-sample-createOpenAIClientWithProxyOption
// Proxy options
final String hostname = "{your-host-name}";
final int port = 447; // your port number

ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
    new InetSocketAddress(hostname, port));
HttpClient httpClient = new NettyAsyncHttpClientBuilder()
    .proxy(proxyOptions)
    .build();

OpenAIClient client = new OpenAIClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

## Key concepts

## Examples
The following sections provide several code snippets covering some of the most common OpenAI service tasks, including:

* [Create text completions](#text-completions "Text completions")
* [Create embeddings](#text-embeddings "Text Embeddings")

### Text completions

``` java readme-sample-getCompletions
List<String> prompt = new ArrayList<>();
prompt.add("Say this is a test");

Completions completions = client.getCompletions("{deploymentOrModelId}", new CompletionsOptions(prompt));

System.out.printf("Model ID=%s is created at %d.%n", completions.getId(), completions.getCreated());
for (Choice choice : completions.getChoices()) {
    System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
}
```

``` java readme-sample-getChatCompletions
List<ChatMessage> chatMessages = new ArrayList<>();
chatMessages.add(new ChatMessage(ChatRole.SYSTEM).setContent("You are a helpful assistant. You will talk like a pirate."));
chatMessages.add(new ChatMessage(ChatRole.USER).setContent("Can you help me?"));
chatMessages.add(new ChatMessage(ChatRole.ASSISTANT).setContent("Of course, me hearty! What can I do for ye?"));
chatMessages.add(new ChatMessage(ChatRole.USER).setContent("What's the best way to train a parrot?"));

ChatCompletions chatCompletions = client.getChatCompletions("{deploymentOrModelId}",
    new ChatCompletionsOptions(chatMessages));

System.out.printf("Model ID=%s is created at %d.%n", chatCompletions.getId(), chatCompletions.getCreated());
for (ChatChoice choice : chatCompletions.getChoices()) {
    ChatMessage message = choice.getMessage();
    System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
    System.out.println("Message:");
    System.out.println(message.getContent());
}
```
Please refer to the service documentation for a conceptual discussion of [text completion][microsoft_docs_openai_completion].

### Text embeddings

```java readme-sample-getEmbedding
EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(
    new StringInputModel("Your text string goes here"));

Embeddings embeddings = client.getEmbeddings("{deploymentOrModelId}", embeddingsOptions);

for (EmbeddingItem item : embeddings.getData()) {
    System.out.printf("Index: %d.%n", item.getIndex());
    for (Double embedding : item.getEmbedding()) {
        System.out.printf("%f;", embedding);
    }
}
```
Please refer to the service documentation for a conceptual discussion of [openAI embedding][microsoft_docs_openai_embedding].

## Troubleshooting
### Enable client logging
You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][logLevels].

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

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[aad_authorization]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_subscription]: https://azure.microsoft.com/free/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[logLevels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[microsoft_docs_openai_completion]: https://learn.microsoft.com/azure/cognitive-services/openai/how-to/completions
[microsoft_docs_openai_embedding]: https://learn.microsoft.com/azure/cognitive-services/openai/concepts/understand-embeddings
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[product_documentation]: https://azure.microsoft.com/services/
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/feature/open-ai/sdk/openai/azure-ai-openai/src
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/feature/open-ai/sdk/openai/azure-ai-openai/src/samples
[openai_client_async]: https://github.com/Azure/azure-sdk-for-java/blob/feature/open-ai/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIAsyncClient.java
[openai_client_builder]: https://github.com/Azure/azure-sdk-for-java/blob/feature/open-ai/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIClientBuilder.java
[openai_client_sync]: https://github.com/Azure/azure-sdk-for-java/blob/feature/open-ai/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIClient.java
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

