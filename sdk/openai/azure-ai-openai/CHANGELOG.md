# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
