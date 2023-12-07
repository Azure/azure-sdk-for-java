# Release History

## 1.0.0-beta.6 (Unreleased)

### Features Added

#### gpt-4-vision-preview

With the new `gpt-4-vision-preview` model, chat completions can now include user input of images. This is achieved by providing a collection of `ChatMessageContentItem ` instances to as the `content` of a `ChatRequestUserMessage`. For more information on `gpt-4-vision-preview` capabilities and current limitations, see [OpenAI's Vision guide](https://platform.openai.com/docs/guides/vision).

#### Chat tools and parallel function calling

The latest gpt-35-turbo and gpt-4 `1106` models support `tools`, which replace `functions` and allow the model to request resolution of multiple function calls in a single round trip. As chat completions only support function tools, the syntax and usage pattern are very similar to functions: instantiate a `ChatCompletionsFunctionToolDefinition` with the name, description, and argument details for the capability to advertise to the model, make a request, and then respond to the `ToolCall` `CompletionsFinishReason` by appending `ChatRequestToolMessage` instances to the `ChatRequestMessage` collection for the next request of the conversation.

#### Azure OpenAI On Your Data: new data sources and authentication mechanisms

Azure OpenAI On Your Data previously supported Azure Cognitive Search data sources via authentication with the administrator key for the ACS resource. This support is greatly expanded in this version:

| Data Source | Authentication Types |
|---|---|
| Azure Cognitive Search | API Key, System Managed Identity, User Managed Identity |
| Cosmos DB (MongoDB vCore) | Connection String |
| Pinecone | API Key |
| Elasticsearch | Key and Key ID |
| Azure Machine Learning Index | System Managed Identity, User Managed Identity, Access Token |
| Microsoft Search | Access Token |

To use these new sources, provide an instance of the appropriate derived type of `OnYourDataVectorizationSource` for the desired data source and use a supported `OnYourDataAuthenticationOptions` instance for its `Authentication`.

#### dall-e-3 image generation

Azure OpenAI now supports image generation with the improved `dall-e-3` models and this library supports the corresponding new features for quality level selection, prompt adjustment, and style. Use `getImageGenerations()` and note that Azure OpenAI will *only* support `dall-e-3` models moving forward.

#### Expanded responsible AI content filter annotations

Azure OpenAI content filter annotations now include substantial additional information including detection of potential protected material, separate profanity detection, custom blocklist support, and jailbreak analysis.

### Breaking Changes

- Removed methods `getAudioTranscriptionAsResponseObject` and `getAudioTranscriptionAsResponseObjectWithResponse` from `OpenAIClient` and `OpenAIAsyncClient` classes.
Use `getAudioTranscription` or `getAudioTranscriptionWithResponse` convenience methods from respective classes.
- Removed methods `getAudioTranslationAsResponseObject` and `getAudioTranslationAsResponseObjectWithResponse` from `OpenAIClient` and `OpenAIAsyncClient` classes.
Use `getAudioTranslation` or `getAudioTranslationWithResponse` convenience methods from respective classes.

#### `ChatRequestMessage` and `ChatResponseMessage` separation

With all of the above expanded capabilities, a few breaking changes manifest in the chat completions API:

- `ChatMessage` is replaced by a number of new, role- and usage-specific types:
  - `ChatRequestMessage` is the abstract base type for messages provided as options/input, with derived types:
    - `ChatRequestSystemMessage`, for assistant instructions and constraints
    - `ChatRequestUserMessage`, which accepts either conventional string `content` or the new `ChatMessageContentItem ` used for `gpt-4-vision`
    - `ChatRequestToolMessage`, used to respond to `ToolCall` finish reasons when providing new `ChatCompletionsToolDefinition` on a request
    - `ChatRequestAssistantMessage`, used to capture conversation history for prior responses or few-shot examples from the assistant
  - `ChatResponseMessage` is the type used for chat messages received as part of `ChatChoice` instances in `ChatCompletions`

**To migrate:**

- Replace `new ChatMessage(ChatRole.System, "my content string")` with `new ChatRequestSystemMessage("my content string")`, applying similar patterns for other request messages
- Handle `ChatResponseMessage` instances in `ChatCompletions` the same way response `ChatMessage` instances were previously
- When adding past assistant messages (received as `ChatResponseMessage`) to conversation history, construct a new `ChatRequestAssistantMessage` with the appropriate content and, if applicable, tool calls

### Bugs Fixed

### Other Changes

## 1.0.0-beta.5 (2023-09-22)

### Features Added

- Added support for `Whisper` endpoints.
- Translation and Transcription of audio files is available.
- The above features are available both in Azure and non-Azure OpenAI.
- Added more convenience methods, which are wrappers around the existing`get{ChatCompletions|Completions|Embeddings}WithResponse` 
  methods with concrete data types instead of using `BinaryData` as the return data type. For example, a new method 
  introduced is
  - Async: `Mono<Response<ChatCompletions>> getChatCompletionsWithResponse(String deploymentOrModelName, ChatCompletionsOptions chatCompletionsOptions, RequestOptions requestOptions)`
  - Sync: `Response<ChatCompletions> getChatCompletionsWithResponse(String deploymentOrModelName, ChatCompletionsOptions chatCompletionsOptions, RequestOptions requestOptions)`
  
  Same methods are added for `Completions` and `Embeddings` endpoints as well.

### Breaking Changes

- Replaced usage of class `AzureKeyCredential` by `KeyCredential`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.

## 1.0.0-beta.4 (2023-08-28)

### Features Added

- Support for BYOD (Bring Your Own Data) was added. [Related link](https://learn.microsoft.com/azure/ai-services/openai/use-your-data-quickstart)

### Breaking Changes

- Replaced usage of class `NonAzureOpenAIKeyCredential` by Azure Core class `KeyCredential`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.

## 1.0.0-beta.3 (2023-07-19)

### Features Added

- Added methods and models to support DALL-E
- Added methods and models to support Functions
- Added models supporting ResponsibleAI annotations

### Bugs Fixed

- Fixed garbled characters issue in the returned data of `getChatCompletionsStream`.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.41.0`.
- Updated `azure-core-http-netty` to `1.13.5`.

## 1.0.0-beta.2 (2023-06-13)

### Breaking Changes

- Removed class `ChatMessageDelta` and replaced usage of it by `ChatMessage`. 

### Bugs Fixed
- [PR#35336](https://github.com/Azure/azure-sdk-for-java/commit/bf4fdac9cea3c18362029df4589bc78b834a4348) fixed
  `com.fasterxml.jackson.databind.exc.MismatchedInputException: Missing required creator property 'usage' (index 3)`.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.40.0`.
- Updated `azure-core-http-netty` to `1.13.4`.

## 1.0.0-beta.1 (2023-05-22)

- Azure OpenAI client library for Java. This package contains Microsoft Azure OpenAI client library. Initial generation from [spec](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/cognitiveservices/OpenAI.Inference)
- Support for Non-Azure OpenAI endpoints was introduced, by passing `NonAzureOpenAIKeyCredential` to the `OpenAIClientBuilder`
- Added Streaming support to `Completions` and `ChatCompletions` endpoints
