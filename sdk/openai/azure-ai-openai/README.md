# Azure OpenAI client library for Java

Azure OpenAI is a managed service that allows developers to deploy, tune, and generate content from OpenAI models on 
Azure resources.

The Azure OpenAI client library for Java is an adaptation of OpenAI's REST APIs that provides an idiomatic interface 
and rich integration with the rest of the Azure SDK ecosystem.

Use the client library for Azure OpenAI to:

* [Create a completion for text][microsoft_docs_openai_completion]
* [Create a text embedding for comparisons][microsoft_docs_openai_embedding]

For concrete examples you can have a look at the following links. Some of the more common scenarios are covered: 

* [Text completions sample](#text-completions "Text completions")
* [Streaming text completions sample](#streaming-text-completions "Streaming text completions")
* [Chat completions sample](#chat-completions "Chat completions")
* [Streaming chat completions sample](#streaming-chat-completions "Streaming chat completions")
* [Embeddings sample](#text-embeddings "Text Embeddings")
* [Image Generation sample](#image-generation "Image Generation")
* [Audio Transcription sample](#audio-transcription "Audio Transcription")
* [Audio Translation sample](#audio-translation "Audio Translation")

If you want to see the full code for these snippets check out our [samples folder][samples_folder].

[Source code][source_code] | [API reference documentation][docs] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Azure OpenAI access][azure_openai_access]
- [Quickstart: Get started generating text using Azure OpenAI Service][quickstart]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-openai;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-openai</artifactId>
    <version>1.0.0-beta.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

In order to interact with the Azure OpenAI Service you'll need to create an instance of client class,
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

### Support for non-Azure OpenAI

The SDK also supports operating against the public non-Azure OpenAI. The response models remain the same, only the setup of the `OpenAIClient` is slightly different. First, get Non-Azure OpenAI API key from [Open AI authentication API keys][non_azure_openai_authentication]. Then setup your `OpenAIClient` as follows: 


```java readme-sample-createNonAzureOpenAISyncClientApiKey
OpenAIClient client = new OpenAIClientBuilder()
    .credential(new KeyCredential("{openai-secret-key}"))
    .buildClient();
```
or 

```java readme-sample-createNonAzureOpenAIAsyncClientApiKey
OpenAIAsyncClient client = new OpenAIClientBuilder()
    .credential(new KeyCredential("{openai-secret-key}"))
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
    <version>1.10.1</version>
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

ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(hostname, port))
    .setCredentials("{username}", "{password}");

OpenAIClient client = new OpenAIClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .clientOptions(new HttpClientOptions().setProxyOptions(proxyOptions))
    .buildClient();
```

## Key concepts

## Examples
The following sections provide several code snippets covering some of the most common OpenAI service tasks, including:

* [Text completions sample](#text-completions "Text completions")
* [Streaming text completions sample](#streaming-text-completions "Streaming text completions")
* [Chat completions sample](#chat-completions "Chat completions")
* [Streaming chat completions sample](#streaming-chat-completions "Streaming chat completions")
* [Embeddings sample](#text-embeddings "Text Embeddings")
* [Image Generation sample](#image-generation "Image Generation")
* [Audio Transcription sample](#audio-transcription "Audio Transcription")
* [Audio Translation sample](#audio-translation "Audio Translation")

### Text completions

``` java readme-sample-getCompletions
List<String> prompt = new ArrayList<>();
prompt.add("Say this is a test");

Completions completions = client.getCompletions("{deploymentOrModelId}", new CompletionsOptions(prompt));

System.out.printf("Model ID=%s is created at %s.%n", completions.getId(), completions.getCreatedAt());
for (Choice choice : completions.getChoices()) {
    System.out.printf("Index: %d, Text: %s.%n", choice.getIndex(), choice.getText());
}
```

For a complete sample example, see sample [Text Completions][sample_get_completions].

### Streaming text completions

```java readme-sample-getCompletionsStream
List<String> prompt = new ArrayList<>();
prompt.add("How to bake a cake?");

IterableStream<Completions> completionsStream = client
    .getCompletionsStream("{deploymentOrModelId}", new CompletionsOptions(prompt));

completionsStream
    .stream()
    // Remove .skip(1) when using Non-Azure OpenAI API
    // Note: the first chat completions can be ignored when using Azure OpenAI service which is a known service bug.
    // TODO: remove .skip(1) when service fix the issue.
    .skip(1)
    .forEach(completions -> System.out.print(completions.getChoices().get(0).getText()));
```

For a complete sample example, see sample [Streaming Text Completions][sample_get_completions_streaming].

### Chat completions

``` java readme-sample-getChatCompletions
List<ChatMessage> chatMessages = new ArrayList<>();
chatMessages.add(new ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. You will talk like a pirate."));
chatMessages.add(new ChatMessage(ChatRole.USER, "Can you help me?"));
chatMessages.add(new ChatMessage(ChatRole.ASSISTANT, "Of course, me hearty! What can I do for ye?"));
chatMessages.add(new ChatMessage(ChatRole.USER, "What's the best way to train a parrot?"));

ChatCompletions chatCompletions = client.getChatCompletions("{deploymentOrModelId}",
    new ChatCompletionsOptions(chatMessages));

System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
for (ChatChoice choice : chatCompletions.getChoices()) {
    ChatMessage message = choice.getMessage();
    System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
    System.out.println("Message:");
    System.out.println(message.getContent());
}
```
For a complete sample example, see sample [Chat Completions][sample_get_chat_completions].

For `function call` sample, see [function call][sample_chat_completion_function_call].

For `Bring Your Own Data` sample, see [Bring Your Own Data][sample_chat_completion_function_call].

Please refer to the service documentation for a conceptual discussion of [text completion][microsoft_docs_openai_completion].

### Streaming chat completions

```java readme-sample-getChatCompletionsStream
List<ChatMessage> chatMessages = new ArrayList<>();
chatMessages.add(new ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. You will talk like a pirate."));
chatMessages.add(new ChatMessage(ChatRole.USER, "Can you help me?"));
chatMessages.add(new ChatMessage(ChatRole.ASSISTANT, "Of course, me hearty! What can I do for ye?"));
chatMessages.add(new ChatMessage(ChatRole.USER, "What's the best way to train a parrot?"));

IterableStream<ChatCompletions> chatCompletionsStream = client.getChatCompletionsStream("{deploymentOrModelId}",
    new ChatCompletionsOptions(chatMessages));

chatCompletionsStream
    .stream()
    // Remove .skip(1) when using Non-Azure OpenAI API
    // Note: the first chat completions can be ignored when using Azure OpenAI service which is a known service bug.
    // TODO: remove .skip(1) when service fix the issue.
    .skip(1)
    .forEach(chatCompletions -> {
        ChatMessage delta = chatCompletions.getChoices().get(0).getDelta();
        if (delta.getRole() != null) {
            System.out.println("Role = " + delta.getRole());
        }
        if (delta.getContent() != null) {
            System.out.print(delta.getContent());
        }
    });
```
For a complete sample example, see sample [Streaming Chat Completions][sample_get_chat_completions_streaming].

### Text embeddings

```java readme-sample-getEmbedding
EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(
    Arrays.asList("Your text string goes here"));

Embeddings embeddings = client.getEmbeddings("{deploymentOrModelId}", embeddingsOptions);

for (EmbeddingItem item : embeddings.getData()) {
    System.out.printf("Index: %d.%n", item.getPromptIndex());
    for (Double embedding : item.getEmbedding()) {
        System.out.printf("%f;", embedding);
    }
}
```
For a complete sample example, see sample [Embedding][sample_get_embedding].

Please refer to the service documentation for a conceptual discussion of [openAI embedding][microsoft_docs_openai_embedding].

### Image Generation

```java readme-sample-imageGeneration
ImageGenerationOptions imageGenerationOptions = new ImageGenerationOptions(
    "A drawing of the Seattle skyline in the style of Van Gogh");
ImageResponse images = client.getImages(imageGenerationOptions);

for (ImageLocation imageLocation : images.getData()) {
    ResponseError error = imageLocation.getError();
    if (error != null) {
        System.out.printf("Image generation operation failed. Error code: %s, error message: %s.%n",
            error.getCode(), error.getMessage());
    } else {
        System.out.printf(
            "Image location URL that provides temporary access to download the generated image is %s.%n",
            imageLocation.getUrl());
    }
}
```

For a complete sample example, see sample [Image Generation][sample_image_generation].

### Audio Transcription
The OpenAI service starts supporting `audio transcription` with the introduction of `Whisper` models. 
The following code snippet shows how to use the service to transcribe audio.

```java readme-sample-audioTranscription
String fileName = "{your-file-name}";
Path filePath = Paths.get("{your-file-path}" + fileName);

byte[] file = BinaryData.fromFile(filePath).toBytes();
AudioTranscriptionOptions transcriptionOptions = new AudioTranscriptionOptions(file)
    .setResponseFormat(AudioTranscriptionFormat.JSON);

AudioTranscription transcription = client.getAudioTranscription("{deploymentOrModelId}", fileName, transcriptionOptions);

System.out.println("Transcription: " + transcription.getText());
```
For a complete sample example, see sample [Audio Transcription][sample_audio_transcription].
Please refer to the service documentation for a conceptual discussion of [Whisper][microsoft_docs_whisper_model].

### Audio Translation
The OpenAI service starts supporting `audio translation` with the introduction of `Whisper` models.
The following code snippet shows how to use the service to translate audio.

```java readme-sample-audioTranslation
String fileName = "{your-file-name}";
Path filePath = Paths.get("{your-file-path}" + fileName);

byte[] file = BinaryData.fromFile(filePath).toBytes();
AudioTranslationOptions translationOptions = new AudioTranslationOptions(file)
    .setResponseFormat(AudioTranslationFormat.JSON);

AudioTranslation translation = client.getAudioTranslation("{deploymentOrModelId}", fileName, translationOptions);

System.out.println("Translation: " + translation.getText());
```
For a complete sample example, see sample [Audio Translation][sample_audio_translation].
Please refer to the service documentation for a conceptual discussion of [Whisper][microsoft_docs_whisper_model].

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
[azure_openai_access]: https://learn.microsoft.com/azure/cognitive-services/openai/overview#how-do-i-get-access-to-azure-openai
[azure_subscription]: https://azure.microsoft.com/free/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[logLevels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[microsoft_docs_openai_completion]: https://learn.microsoft.com/azure/cognitive-services/openai/how-to/completions
[microsoft_docs_openai_embedding]: https://learn.microsoft.com/azure/cognitive-services/openai/concepts/understand-embeddings
[microsoft_docs_whisper_model]: https://learn.microsoft.com/azure/ai-services/openai/whisper-quickstart?tabs=command-line
[non_azure_openai_authentication]: https://platform.openai.com/docs/api-reference/authentication
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[product_documentation]: https://azure.microsoft.com/services/
[quickstart]: https://learn.microsoft.com/azure/cognitive-services/openai/quickstart
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/openai/azure-ai-openai/src
[samples_folder]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/openai/azure-ai-openai/src/samples
[sample_chat_completion_function_call]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/FunctionCallSample.java
[sample_chat_completion_BYOD]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/ChatCompletionsWithYourData.java
[sample_get_chat_completions]: https://Dgithub.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsSample.java
[sample_get_chat_completions_streaming]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsStreamSample.java
[sample_get_completions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetCompletionsSample.java
[sample_get_completions_streaming]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetCompletionsStreamSample.java
[sample_get_embedding]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetEmbeddingsSample.java
[sample_image_generation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetImagesSample.java
[sample_audio_transcription]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/AudioTranscriptionSample.java
[sample_audio_translation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/AudioTranslationSample.java
[openai_client_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIAsyncClient.java
[openai_client_builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIClientBuilder.java
[openai_client_sync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIClient.java
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

