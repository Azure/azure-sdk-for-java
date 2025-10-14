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

# Azure OpenAI Realtime Audio client library samples for Java

Azure OpenAI Service samples are a set of self-contained Java programs that demonstrate interacting with Azure OpenAI
service using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
The following sections provide several code snippets covering some of the most common OpenAI service tasks, including:

Synchronous:
- [Audio prompt and audio response with transcript][audio_collection_sync]

Asynchronous:
- [Audio prompt and audio response with transcript][audio_collection_async]
- [Audio prompt and tool call definition for text response][tool_call]

Cookbook:
- [Low level event handling (Azure)][low_level_client_azure]
- [Low level event handling (non-Azure)][low_level_client_non_azure]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
See [Next steps][SDK_README_NEXT_STEPS].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/README.md#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/README.md#key-concepts
[SDK_README_DEPENDENCY]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/README.md#adding-the-package-to-your-product
[SDK_README_NEXT_STEPS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/README.md#next-steps

[audio_collection_async]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/src/samples/java/com/azure/ai/openai/realtime/AudioCollection.java
[audio_collection_sync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/src/samples/java/com/azure/ai/openai/realtime/AudioCollectionSync.java
[low_level_client_azure]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/src/samples/java/com/azure/ai/openai/realtime/LowLevelClient.java
[low_level_client_non_azure]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/src/samples/java/com/azure/ai/openai/realtime/NonAzureLowLevelClient.java
[tool_call]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-realtime/src/samples/java/com/azure/ai/openai/realtime/ToolCall.java

