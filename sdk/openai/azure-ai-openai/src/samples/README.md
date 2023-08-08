---
page_type: sample
languages:
  - java
products: 
  - azure 
  - azure-cognitive-services
  - azure-openai
urlFragment: ai-openai-java-samples
---

# Azure OpenAI client library samples for Java

Azure OpenAI Service samples are a set of self-contained Java programs that demonstrate interacting with Azure OpenAI 
service using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
The following sections provide several code snippets covering some of the most common OpenAI service tasks, including:

Synchronous:
- [Text Completions][sample_get_completions]
- [Chat Completions][sample_get_chat_completions]
- [Embeddings][sample_get_embedding]
- [Image Generation][sample_image_generation]

Asynchronous:
- [Text Completions][async_sample_get_completions]
- [Chat Completions][async_sample_get_chat_completions]
- [Embeddings][async_sample_get_embedding]
- [Image Generation][async_sample_image_generation]

Cookbook:
- [Chat bot][cookbook_chat_bot]
- [Chat bot With Key][cookbook_chat_bot_with_key]
- [Function Call][cookbook_function_call]
- [OpenAIProxy][cookbook_openai_proxy]
- [Streaming Chat][cookbook_streaming_chat]
- [Summarize Text][cookbook_summarize_text]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
See [Next steps][SDK_README_NEXT_STEPS].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md#key-concepts
[SDK_README_DEPENDENCY]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md#adding-the-package-to-your-product
[SDK_README_NEXT_STEPS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md#next-steps

[async_sample_get_completions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetCompletionsAsyncSample.java
[async_sample_get_chat_completions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsAsyncSample.java
[async_sample_get_embedding]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetEmbeddingsAsyncSample.java
[async_sample_image_generation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetImagesAsyncSample.java

[sample_get_completions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetCompletionsSample.java
[sample_get_chat_completions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsSample.java
[sample_get_embedding]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetEmbeddingsSample.java
[sample_image_generation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetImagesSample.java

[cookbook_chat_bot]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/ChatbotSample.java
[cookbook_chat_bot_with_key]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/ChatbotWithKeySample.java
[cookbook_function_call]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/FunctionCallSample.java
[cookbook_openai_proxy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/OpenAIProxySample.java
[cookbook_streaming_chat]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/StreamingChatSample.java
[cookbook_summarize_text]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/SummarizeTextSample.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fopenai%2Fazure-ai-openai%2FREADME.png)
