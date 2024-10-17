# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2024-10-03)

### Features Added

- getModelInfo() API added for ChatCompletionsClient and EmbeddingsClient, along with sample
- ChatRequestMessage.fromContentItems() static constructor added for image chat scenarios
- ChatMessageImageContentItem(Path, string) constructor for image file chat scenario
- getChoice() API added to ChatCompletions and StreamingChatMessageResponseUpdate

### Bugs Fixed

- #42036: illegal char exception for newline in prompt
- Fixed key authentication issue with non-Azure OpenAI models

### Other Changes

- samples for image chat, function calling, and model info scenarios added
- tests for image chat and function calling

## 1.0.0-beta.1 (2024-09-19)

- Azure AI Inference client library for Java. This package contains Microsoft Azure AI Inference client library.

### Features Added

- Initial release of the Azure AI Inference client library for Java. 
