# Azure Model client library for Java

Azure Model client library for Java.

This package contains Microsoft Azure Model client library.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-inference;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-inference</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

In order to interact with the Azure AI Inference Service you'll need to create an instance of client class,
[ChatCompletionsAsyncClient][chat_completions_client_async] or [ChatCompletionsClient][chat_completions_client_sync] by using
[ChatCompletionsClientBuilder][chat_completions_client_builder]. To configure a client for use with
Azure Inference, provide a valid endpoint URI to an Azure Model resource along with a corresponding key credential,
token credential, or [Azure Identity][azure_identity] credential that's authorized to use the Azure Model resource.

#### Create a Chat Completions client with key credential
Get Azure Model `key` credential from the Azure Portal.

```java readme-sample-createSyncClientKeyCredential
ChatCompletionsClient client = new ChatCompletionsClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
or
```java readme-sample-createAsyncClientKeyCredential
ChatCompletionsAsyncClient client = new ChatCompletionsClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

#### Create a client with Azure Active Directory credential
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

After setup, you can choose which type of [credential][azure_identity_credential_type] from azure.identity to use.
As an example, [DefaultAzureCredential][wiki_identity] can be used to authenticate the client:
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables:
`AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_CLIENT_SECRET`.

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with OpenAI service, please
refer to [the associated documentation][aad_authorization].

```java readme-sample-createChatCompletionsClientWithAAD
TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
ChatCompletionsClient client = new ChatCompletionsClientBuilder()
    .credential(defaultCredential)
    .endpoint("{endpoint}")
    .buildClient();
```

## Key concepts

## Examples
The following sections provide several code snippets covering some of the most common OpenAI service tasks, including:

* [Chat completions sample](#chat-completions "Chat completions")
* [Streaming chat completions sample](#streaming-chat-completions "Streaming chat completions")
<!--
* [Embeddings sample](#text-embeddings "Text Embeddings")
-->

## Examples

### Chat completions

```java readme-sample-getChatCompletions
List<ChatRequestMessage> chatMessages = new ArrayList<>();
chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

ChatCompletions chatCompletions = client.complete(new ChatCompletionsOptions(chatMessages));

System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreated());
for (ChatChoice choice : chatCompletions.getChoices()) {
    ChatResponseMessage message = choice.getMessage();
    System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
    System.out.println("Message:");
    System.out.println(message.getContent());
}
```
For a complete sample example, see sample [Chat Completions][sample_get_chat_completions].

Please refer to the service documentation for a conceptual discussion of [text completion][microsoft_docs_openai_completion].

### Streaming chat completions

```java readme-sample-getChatCompletionsStream
List<ChatRequestMessage> chatMessages = new ArrayList<>();
chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
chatMessages.add(new ChatRequestUserMessage("What's the best way to train a parrot?"));

client.completeStreaming(new ChatCompletionsOptions(chatMessages))
    .forEach(chatCompletions -> {
        if (CoreUtils.isNullOrEmpty(chatCompletions.getChoices())) {
            return;
        }
        StreamingChatResponseMessageUpdate delta = chatCompletions.getChoices().get(0).getDelta();
        if (delta.getRole() != null) {
            System.out.println("Role = " + delta.getRole());
        }
        if (delta.getContent() != null) {
            String content = delta.getContent();
            System.out.print(content);
        }
    });
```

To compute tokens in streaming chat completions, see sample [Streaming Chat Completions][sample_get_chat_completions_streaming].

<!--
### Text embeddings

```java readme-sample-getEmbedding
```
For a complete sample example, see sample [Embedding][sample_get_embedding].

Please refer to the service documentation for a conceptual discussion of [openAI embedding][microsoft_docs_openai_embedding].
-->

### Service API versions

The client library targets the latest service API version by default.
The service client builder accepts an optional service API version parameter to specify which API version to communicate.

#### Select a service API version

You have the flexibility to explicitly select a supported service API version when initializing a service client via the service client builder.
This ensures that the client can communicate with services using the specified API version.

When selecting an API version, it is important to verify that there are no breaking changes compared to the latest API version.
If there are significant differences, API calls may fail due to incompatibility.

Always ensure that the chosen API version is fully supported and operational for your specific use case and that it aligns with the service's versioning policy.

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

For more details, see [TROUBLESHOOTING][troubleshooting] guideline.

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[aad_authorization]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[sample_get_chat_completions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsSample.java
[sample_get_chat_completions_streaming]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsStreamSample.java
[microsoft_docs_openai_completion]: https://learn.microsoft.com/azure/cognitive-services/openai/how-to/completions
[microsoft_docs_openai_embedding]: https://learn.microsoft.com/azure/cognitive-services/openai/concepts/understand-embeddings
[chat_completions_client_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIAsyncClient.java
[chat_completions_client_builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIClientBuilder.java
[chat_completions_client_sync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIClient.java
[logLevels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[troubleshooting]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/TROUBLESHOOTING.md
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fai%2Fazure-ai-inference%2FREADME.png)
