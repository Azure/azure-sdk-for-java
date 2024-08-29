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
- [Legacy Completions][sample_get_completions]
- [Chat Completions][sample_get_chat_completions]
- [Embeddings][sample_get_embedding]
- [Image Generation][sample_image_generation]
- [Audio Transcription][sample_audio_transcription]
- [Audio Translation][sample_audio_translation]
- [Chat with Images][sample_chat_with_images]
- [Text to Speech][sample_text_to_speech]
- [Tool Calls][sample_tool_calls]

Asynchronous:
- [Legacy Completions][async_sample_get_completions]
- [Chat Completions][async_sample_get_chat_completions]
- [Embeddings][async_sample_get_embedding]
- [Image Generation][async_sample_image_generation]
- [Audio Transcription][async_sample_audio_transcription]
- [Audio Translation][async_sample_audio_translation]
- [Chat with Images][async_sample_chat_with_images]
- [Text to Speech][async_sample_text_to_speech]
- [Tool Calls][async_sample_tool_calls]

Cookbook:
- [Batch operations][cookbook_batch_operations]
- [Chat bot][cookbook_chat_bot]
- [Chat bot With Key][cookbook_chat_bot_with_key]
- [Chat with Bring Your Own Data][cookbook_chat_with_bring_your_own_data]
- [File operations][cookbook_file_operations]
- [Function Call][cookbook_chat_with_function_call]
- [OpenAIProxy][cookbook_openai_proxy]
- [Streaming Chat][cookbook_streaming_chat]
- [Summarize Text][cookbook_summarize_text]
- [Streaming Tool Call][cookbook_streaming_tool_call]

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
[async_sample_audio_transcription]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/AudioTranscriptionAsyncSample.java
[async_sample_audio_translation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/AudioTranslationAsyncSample.java
[async_sample_chat_with_images]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsVisionAsyncSample.java
[async_sample_text_to_speech]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/TextToSpeechAsyncSample.java
[async_sample_tool_calls]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsToolCallAsyncSample.java

[sample_get_completions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetCompletionsSample.java
[sample_get_chat_completions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsSample.java
[sample_get_embedding]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetEmbeddingsSample.java
[sample_image_generation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetImagesSample.java
[sample_audio_transcription]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/AudioTranscriptionSample.java
[sample_audio_translation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/AudioTranslationSample.java
[sample_chat_with_images]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsVisionSample.java
[sample_text_to_speech]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/TextToSpeechSample.java
[sample_tool_calls]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/usage/GetChatCompletionsToolCallSample.java

[cookbook_batch_operations]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/BatchOperationsSample.java
[cookbook_chat_bot]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/ChatbotSample.java
[cookbook_chat_bot_with_key]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/ChatbotWithKeySample.java
[cookbook_chat_with_bring_your_own_data]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/ChatCompletionsWithYourData.java
[cookbook_chat_with_function_call]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/ChatCompletionsFunctionCall.java
[cookbook_file_operations]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/FileOperationsSample.java
[cookbook_openai_proxy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/OpenAIProxySample.java
[cookbook_streaming_chat]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/StreamingChatSample.java
[cookbook_summarize_text]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/SummarizeTextSample.java
[cookbook_streaming_tool_call]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/java/com/azure/ai/openai/StreamingToolCall.java
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fopenai%2Fazure-ai-openai%2FREADME.png)
