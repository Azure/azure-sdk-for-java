# Release History

## 1.0.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.5 (2025-05-15)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.2` to `1.55.3`
- Upgraded `azure-core-http-netty` from `1.15.10` to `1.15.11`

## 1.0.0-beta.4 (2025-03-14)

### Bugs Fixed

- #44517: Use ModelServiceVersion (api-version) of 2024-05-01-preview as default 

## 1.0.0-beta.3 (2025-02-20)

### Breaking Changes
- Change ChatCompletionsResponseFormatJSON class name to ChatCompletionsResponseFormatJsonObject.

### Features Added
- Add samples and tests for image embeddings.
- Add ChatCompletionsResponseFormatJsonSchema class for structured output in completions response.
- Add sample and test for Chat Completions with structured output
- Add "developer" chat role and ChatRequestDeveloperMessage to support new GPT models

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
