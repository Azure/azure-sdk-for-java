# Release History

## 1.0.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

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
