# Release History

## 1.0.0-beta.12 (Unreleased)

### Features Added

- Added support for service API version, `2024-08-01-preview`.
- Added support for MongoDB chat extension. New classes `MongoDBChatExtensionConfiguration`, `MongoDBChatExtensionParameters`, and `MongoDBChatExtensionParametersFieldsMapping` are added to support MongoDB chat extension.
- Added `roleInformation` property in `AzureCosmosDBChatExtensionParameters` class to support role information.
- Added `rerank_score` property in `AzureChatExtensionDataSourceResponseCitation` class to support re-rank score.
- Added `refusal` property in `ChatChoiceLogProbabilityInfo`, `ChatMessageContentItem`, `ChatResponseMessage` classes and a new type of content item class `ChatMessageRefusalContentItem` to support refusal.
- Added `json_schema` property in `ChatCompletionsResponseFormat` class to support JSON schema.
- Added `username_and_password` in `OnYourDataAuthenticationOptions` class and an input option class`OnYourDataUsernameAndPasswordAuthenticationOptions`  to support username and password authentication.
- Added `intergrated` property in `OnYourDataVectorizationSource` class and `OnYourDataVectorizationSourceType` to support integrated vectorization source.

### Breaking Changes

- The `content` type in `ChatRequestAssistantsMessage`, `ChatRequestSystemMessage`, `ChatRequestToolMessage` classes is now of type `BinaryData` instead of `String`.
- Removed `azure_ml_index` from `AzureChatExtensionConfiguration` class, and its response models `AzureMachineLearningIndexConfiguration` and `AzureMachineLearningIndexChatExtensionParameters`.
- Removed `role_information` from `AzureSearchChatExtensionParameters`, `ElasticsearchChatExtensionParameters` and `PineconeChatExtensionParameters` classes.

### Bugs Fixed

### Other Changes

## 1.0.0-beta.11 (2024-08-29)

### Features Added

- Added a new overload `getImageGenerationsWithResponse` that takes `RequestOptions` to provide the flexibility to
  modify the HTTP request.
- Added the capability to handle the float[] `embedding` type when serializing to `toJson()` and deserializing from `fromJson()` in the `EmbeddingItem` class. ([#41159](https://github.com/Azure/azure-sdk-for-java/issues/41159))
- A new `required` keyword is added to `tool_choice` in the request options, which specifies that at least one tool must be called. This adds to the existing `auto` (default), `none`, and ability to specify a specific (function) tool by name
- New operation paths: `/batches`, `/batches/{batch-id}` and `/batches/{batch-id}/cancel`
- New request model: `BatchCreateRequest`
- New response models: `Batch`, `BatchErrors`, etc.
- Added `/files` operation from the Azure OpenAI Assistants SDK
- New `batch` and `batch_output` files purposes
- In `ChatCompletionsOptions` the `setToolChoice` method uses `ChatCompletionsToolSelection` to pass either a preset enum or a named tool.

### Breaking Changes

- Removed `finish_details` field from `ChatChoice` class since service API version, `2024-07-01-preview`, does not return this field. 

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to version `1.51.0`.
- Upgraded `azure-core-http-netty` to version `1.15.3`.


## 1.0.0-beta.10 (2024-07-02)

### Bugs Fixed

- Fixed a bug during the serialization and deserialization of the `content` property in the `ChatRequestUserMessage`. ([#40687](https://github.com/Azure/azure-sdk-for-java/pull/40687))


## 1.0.0-beta.9 (2024-06-06)

### Features Added

- Added support for service API versions, `2024-04-01-preview` and `2024-05-01-preview`. 
- Note that `AOAI` refers to Azure OpenAI and `OAI` refers to OpenAI.
- Added timestamp granularity to Whisper transcription; this is an array of enumerated string values
  (word and/or segment) that controls which, if any, timestamp information is emitted to transcription results.
  - `AudioTranscriptionTimestampGranularity` enum to represent the timestamp granularity options for Whisper transcription.
  - `AudioTranscriptionWord` class to represent the word timestamp information in the transcription results.
- Added two new audio formats, `wav` and `pcm`, to the `SpeechGenerationResponseFormat` enum.

*AOAI ONLY*

- Added a new RAI content filter schema type, `ContentFilterDetailedResults`, that features:
    - The boolean `filtered` property from `ContentFilterResult`.
    - An array named `details` of the existing `ContentFilterBlocklistIdResult` type, each of which has:
      - The base boolean `filtered`
      - A string `id`
- Added a new property `indirectAttack` in `ContentFilterResultDetailsForPrompt` class to represent the indirect attack results.
- Added a new property `custom_blocklists` in `ImageGenerationPromptFilterResults` class to represent the prompt filter results.

**On Your Data**

- New string enum type used in options: `OnYourDataContextProperty`: "citations" | "intent" | "allRetrievedDocuments"
  - This is used in arrays like a bitmasked flag; "give me citations and documents" == `[ "citations", "allRetrievedDocuments" ]`
  - It's not dissimilar to how transcription uses `timestamp_granularities[]`
- New model type used in response extensions: `retrievedDocument`
  - Inherits from existing `citation`
  - Required properties: `content` (string, inherited), `search_queries` (array of strings), `data_source_index` (int32), `original_search_score` (double)
  - Optional properties: `title`, `url`, `filepath`, `chunk_id` (all strings inherited from `citation`); `re_rank_score` (double)
- New options fields for chat extension parameters (request options):
  - `max_search_queries` (optional int32)
  - `allow_partial_result` (optional boolean)
  - `include_contexts` (optional array of the above `OnYourDataContextProperty` enum (effective flag selection))
  - Affected `*parameters` types:
    - `AzureSearchChatExtensionParameters`
    - `AzureMachineLearningIndexChatExtensionParameters`
    - `AzureCosmosDBChatExtensionParameters`
    - `ElasticsearchChatExtensionParameters`
    - `PineconeChatExtensionParameters`
- Vectorization source types have a new `dimensions` property (optional int32)
    - Affected: `OnYourDataEndpointVectorizationSource`, `OnYourDataDeploymentNameVectorizationSource`
- `AzureSearchChatExtensionParameters` now supports `OnYourDataAccessTokenAuthenticationOptions` in its named `authentication` field
- `OnYourDataEndpointVectorizationSource` now supports `OnYourDataAccessTokenAuthenticationOptions` for its named `authentication` field.
- Added new class `OnYourDataVectorSearchAuthenticationType`, `OnYourDataVectorSearchAuthenticationOptions`,
  `OnYourDataVectorSearchApiKeyAuthenticationOptions`, `OnYourDataVectorSearchAccessTokenAuthenticationOptions` for the
  vector search authentication options.
- The response extension type `AzureChatExtensionsMessageContext` has a new `all_retrieved_documents` field, which is an optional array of the new `retrievedDocument` type defined earlier.

### Breaking Changes

- Replaced Jackson Databind annotations with `azure-json` functionality for OpenAI service models.
- [AOAI] Added a new class `ContentFilterDetailedResults` to represent detailed content filter results, which replaces the
  `customBlocklists` response property type, `List<ContentFilterBlocklistIdResult>` in 
  `ContentFilterResultDetailsForPrompt` and `ContentFilterResultsForChoice` class.
- [AOAI] Replaced `OnYourDataAuthenticationOptions` with `OnYourDataVectorSearchAuthenticationOptions` in the `OnYourDataEndpointVectorizationSource` class.
  Currently, `OnYourDataEndpointVectorizationSource` only supports `OnYourDataApiKeyAuthenticationOptions` and `OnYourDataAccessTokenAuthenticationOptions` as authentication options.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to version `1.49.1`.
- Upgraded `azure-core-http-netty` to version `1.15.1`.


## 1.0.0-beta.8 (2024-04-09)

### Features Added

- Added support for service API version, `2024-03-01-preview`.
- Added a new property to `EmbeddingOptions`:
  - `dimensions`, which is only supported in models `text-embedding-3-*` and above.
- Added a new method to get base64 encoded string in `EmbeddingItem` class:
  - `getEmbeddingAsString` method returns the embedding as a base64 encoded string.
- Added a new overload `getChatCompletionsStreamWithResponse` that takes `RequestOptions` to provide the flexibility to
  modify the HTTP request.

### Breaking Changes

- Replace return type `List<Double>` with `List<Float>` of `getEmbedding` method in `EmbeddingItem` class.

### Bugs Fixed

- A bugs fixed in Azure Core SDK that solves where text/event-stream content type wasn't being handled correctly.
  Replaced content type exact match equal by 'startwith'. ([#39204](https://github.com/Azure/azure-sdk-for-java/pull/39204))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 1.0.0-beta.7 (2024-03-04)

### Features Added

- Text-to-speech using OpenAI TTS models is now supported. See [OpenAI's API reference](https://platform.openai.com/docs/api-reference/audio/createSpeech) 
  or the [Azure OpenAI quickstart](https://learn.microsoft.com/azure/ai-services/openai/text-to-speech-quickstart)
  for detailed overview and background information. The new method `generateSpeechFromText` exposes this capability on 
  `OpenAIClient` and `OpenAIAsyncClient`. Text-to-speech converts text into lifelike spoken audio in a chosen voice, together with other optional
  configurations. This method works for both Azure OpenAI and non-Azure `api.openai.com` client configurations.
- Added two new authentication options, `OnYourDataEncodedApiKeyAuthenticationOptions` and `OnYourDataAccessTokenAuthenticationOptions`
  to support the new authentication mechanism for "On Your Data" feature.

### Breaking Changes

- Introduced a new type `AzureChatExtensionDataSourceResponseCitation` for a more structured representation of citation data.
- Correspondingly, updated `AzureChatExtensionsMessageContext`:
  - Replaced `messages` with `citations` of type `AzureChatExtensionDataSourceResponseCitation`.
  - Added `intent` as a string type.
- Renamed "AzureCognitiveSearch" to "AzureSearch":
  - `AzureCognitiveSearchChatExtensionConfiguration` is now `AzureSearchChatExtensionConfiguration`.
  - `AzureCognitiveSearchIndexFieldMappingOptions` is now `AzureSearchIndexFieldMappingOptions`.
  - `AzureCognitiveSearchQueryType` is now `AzureSearchQueryType`.
- Replaced `String` property `name` by `ChatCompletionsFunctionToolSelection` property `function` in `ChatCompletionsNamedFunctionToolSelection`
- Made `embeddingDependency` as a required parameter in `AzureCosmosDBChatExtensionParameters` and `PineconeChatExtensionParameters` class, and removed setter method.
- Removed `vectorFields` and `imageVectorFields` from `PineconeFieldMappingOptions` class, and made `contentField` as required parameter.
- Removed `getAudioTranscriptionAsPlainTextWithResponse` and `getAudioTranslationAsPlainTextWithResponse` methods from `OpenAIClient` and `OpenAIAsyncClient` classes.
- Made `ImageGeneration` constructor as private.
- Made `ImageGenerationData` constructor as private and removed setter methods.

### Bugs Fixed

- Fixed `ChatRequestUserMessage` deserialization issue. [#38183](https://github.com/Azure/azure-sdk-for-java/issues/38183)

### Other Changes

- Dropped service API version support for `2023-08-01-preview`, `2023-09-01-preview` and `2023-12-01-preview`.
- Made the `getContent` a public method in `ChatRequestUserMessage` class. ([#38805](https://github.com/Azure/azure-sdk-for-java/pull/38805))
- Added a new property `logprobs` in `ChatChoice` class to support log probabilities for this chat choice.
- Added new properties `logprobs` and `topLogprobs` in `ChatCompletionsOptions` class to support log probabilities for chat completions.
- Added a new property `inputType` in `EmbeddingsOptions` class to support embeddings for different input types 
  when using Azure OpenAI, specifies the input type to use for embedding search.
- Added more properties to `AzureCosmosDBFieldMappingOptions` class to support more field mapping options, including
  `titleField`, `urlField`, `filepathField`, `contentFields`, and `contentFieldsSeparator`. Made `contentField` as required parameter.
- Added new properties `ImageGenerationContentFilterResults contentFilterResults` and `ImageGenerationPromptFilterResults promptFilterResults`
  in `ImageGenerationData` class to support filtering results.
- Added new property `suffix` in `CompletionsOptions` class to support suffix for completions.

## 1.0.0-beta.6 (2023-12-11)

### Features Added

- `-1106` model feature support for `gpt-35-turbo` and `gpt-4-turbo`, including use of `seed`, `system_fingerprint`,
  parallel function calling via tools, "JSON mode" for guaranteed function outputs, and more
- `dall-e-3` image generation capabilities via `getImageGenerations`, featuring higher model quality, automatic prompt
  revisions by `gpt-4`, and customizable quality/style settings
- Greatly expanded "On Your Data" capabilities in Azure OpenAI, including many new data source options and authentication
  mechanisms
- Early support for `gpt-4-vision-preview`, which allows the hybrid use of text and images as input to enable scenarios
  like "describe this image for me"
- Support for Azure enhancements to `gpt-4-vision-preview` results that include grounding and OCR features

### Breaking Changes

- Removed methods `getAudioTranscriptionAsResponseObject` and `getAudioTranscriptionAsResponseObjectWithResponse` from `OpenAIClient` and `OpenAIAsyncClient` classes.
Use `getAudioTranscription` or `getAudioTranscriptionWithResponse` convenience methods from respective classes.
- Removed methods `getAudioTranslationAsResponseObject` and `getAudioTranslationAsResponseObjectWithResponse` from `OpenAIClient` and `OpenAIAsyncClient` classes.
Use `getAudioTranslation` or `getAudioTranslationWithResponse` convenience methods from respective classes.

**`ChatMessage` changes:**

- The singular `ChatMessage` type has been replaced by `ChatRequestMessage` and `ChatResponseMessage`, the former of
  which is an abstract, polymorphic type with concrete derivations like `ChatRequestSystemMessage` and
  `ChatRequestUserMessage`. This requires conversion from old `ChatMessage` into the new types. While this is
  usually a straightforward string replacement, converting a response message into a request message (e.g. when
  propagating an assistant response to continue the conversation) will require creating a new instance of the
  appropriate request message with the response message's data. See the examples for details.

**Dall-e-3:**

- Azure OpenAI now uses `dall-e-3` model deployments for its image generation API and such a valid deployment must
  be provided into the options for the `getImageGenerations()` method to receive results.

### Other Changes

- Audio transcription and translation (via `getAudioTranscription()` and `getAudioTranslation()` now allow specification  
  of an optional `fileName` in addition to the binary audio data. This is used purely as an identifier and does not 
  functionally alter the transcription/translation behavior in any way.

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
