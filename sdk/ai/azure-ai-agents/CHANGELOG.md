# Release History

## 2.0.1 (2026-04-16)

### Bugs Fixed

- Fixed streaming APIs to properly stream response data instead of eagerly buffering the entire response body in memory, and moved async completions off I/O threads to prevent blocking.

## 2.0.0 (2026-03-27)

### Features Added

- Added `beginUpdateMemories(String name, String scope)` required-params-only overload to `MemoryStoresClient` and `MemoryStoresAsyncClient`, for updating a memory store without specifying optional conversation items, previous update ID, or delay.

### Breaking Changes

- The following types changed from standard Java `enum` to `ExpandableStringEnum`-based classes, allowing unknown values to be handled without throwing exceptions. The `values()` method now returns a `Collection` instead of an array, and instances should be compared using `.equals()` rather than `==`:
  - `ComputerEnvironment`
  - `ContainerMemoryLimit`
  - `GrammarSyntax`
  - `ImageGenActionEnum`
  - `ImageGenToolBackground`
  - `ImageGenToolModeration`
  - `ImageGenToolOutputFormat`
  - `ImageGenToolQuality`
  - `ImageGenToolSize`
  - `InputFidelity`
  - `McpToolConnectorId`
  - `MemoryStoreUpdateStatus`
  - `RankerVersionType`
  - `SearchContextSize`
  - `WebSearchToolSearchContextSize`
- Renamed `getObject()` to `getObjectType()` in `AgentDetails`, `AgentVersionDetails`, and `MemoryStoreDetails`. The underlying field was renamed from `object` to `objectType`.
- Renamed `MCPToolConnectorId` enum to `McpToolConnectorId` for consistent casing. The `McpTool` methods `getConnectorType()` and `setConnectorType()` now use `McpToolConnectorId` instead of `MCPToolConnectorId`.
- `getContainerAsAutoCodeInterpreterToolParam()` on `CodeInterpreterTool` renamed to `getContainerAsAutoCodeInterpreterToolParameter()`, and `setContainer(AutoCodeInterpreterToolParam)` now accepts `AutoCodeInterpreterToolParameter` instead.
- Renamed remaining `*Param` model classes to `*Parameter` for naming consistency:
  - `AutoCodeInterpreterToolParam` → `AutoCodeInterpreterToolParameter`
  - `ContainerAutoParam` → `ContainerAutoParameter`
  - `ContainerNetworkPolicyParam` → `ContainerNetworkPolicyParameter`
  - `ContainerNetworkPolicyAllowlistParam` → `ContainerNetworkPolicyAllowlistParameter`
  - `ContainerNetworkPolicyDisabledParam` → `ContainerNetworkPolicyDisabledParameter`
  - `ContainerNetworkPolicyDomainSecretParam` → `ContainerNetworkPolicyDomainSecretParameter`
  - `CustomTextFormatParam` → `CustomTextFormatParameter`
  - `FunctionShellToolParamEnvironmentContainerReferenceParam` → `FunctionShellToolParameterEnvironmentContainerReferenceParameter`
  - `FunctionShellToolParamEnvironmentLocalEnvironmentParam` → `FunctionShellToolParameterEnvironmentLocalEnvironmentParameter`
  - `InlineSkillParam` → `InlineSkillParameter`
  - `InlineSkillSourceParam` → `InlineSkillSourceParameter`
  - `LocalSkillParam` → `LocalSkillParameter`
  - `SkillReferenceParam` → `SkillReferenceParameter`
- `deleteAgentWithResponse` on `AgentsClient` now returns `Response<Void>` instead of `Response<BinaryData>`. The corresponding async method on `AgentsAsyncClient` now returns `Mono<Response<Void>>` instead of `Mono<Response<BinaryData>>`.
- `deleteAgentVersionWithResponse` on `AgentsClient` now returns `Response<Void>` instead of `Response<BinaryData>`. The corresponding async method on `AgentsAsyncClient` now returns `Mono<Response<Void>>` instead of `Mono<Response<BinaryData>>`.
- `deleteMemoryStoreWithResponse` on `MemoryStoresClient` now returns `Response<Void>` instead of `Response<BinaryData>`. The corresponding async method on `MemoryStoresAsyncClient` now returns `Mono<Response<Void>>` instead of `Mono<Response<BinaryData>>`.
- `deleteScopeWithResponse` on `MemoryStoresClient` now returns `Response<Void>` instead of `Response<BinaryData>`. The corresponding async method on `MemoryStoresAsyncClient` now returns `Mono<Response<Void>>` instead of `Mono<Response<BinaryData>>`.
- `deleteMemoryStore(String)` on `MemoryStoresClient` now returns `void` instead of `DeleteMemoryStoreResult`. The corresponding async method on `MemoryStoresAsyncClient` now returns `Mono<Void>` instead of `Mono<DeleteMemoryStoreResult>`.
- `deleteScope(String, String)` on `MemoryStoresClient` now returns `void` instead of `MemoryStoreDeleteScopeResponse`. The corresponding async method on `MemoryStoresAsyncClient` now returns `Mono<Void>` instead of `Mono<MemoryStoreDeleteScopeResponse>`.
- `DeleteMemoryStoreResult` and `MemoryStoreDeleteScopeResponse` removed from `com.azure.ai.agents.models` and are no longer part of the public API.
- `ResponsesUtils` class has been removed. Use `ResponsesClient.getAzureFields(Response)` instead of `ResponsesUtils.getAzureFields(Response)` to extract Azure-specific fields from a response.

### Other Changes

- Regenerated client from updated TypeSpec specification.

## 2.0.0-beta.3 (2026-03-19)

### Features Added

- Added `readSpecFromFile(Path)` static convenience method to `OpenApiFunctionDefinition` for loading OpenAPI specification JSON files as the `Map<String, BinaryData>` required by the constructor, eliminating the need for manual `JsonReader`/`BinaryData` wiring.
- Added new `OpenApiSync`/`OpenApiAsync` samples demonstrating end-to-end OpenAPI tool integration: loading a spec file, creating an agent with an `OpenApiTool`, and invoking an external API via conversation.
- Added new tool samples for parity with the Python SDK: `AzureFunctionSync`/`AzureFunctionAsync`, `BingCustomSearchSync`/`BingCustomSearchAsync`, `MemorySearchSync`/`MemorySearchAsync`, `McpWithConnectionSync`/`McpWithConnectionAsync`, and `OpenApiWithConnectionSync`/`OpenApiWithConnectionAsync`.
- Added type-safe accessors on `CodeInterpreterTool` for the `container` property: `setContainer(String)`, `setContainer(AutoCodeInterpreterToolParam)`, `getContainerAsString()`, and `getContainerAsAutoCodeInterpreterToolParam()`.
- Added type-safe accessors on `McpTool` for the `allowedTools` property: `setAllowedTools(List<String>)`, `setAllowedTools(McpToolFilter)`, `getAllowedToolsAsStringList()`, and `getAllowedToolsAsMcpToolFilter()`.
- Added type-safe accessors on `McpTool` for the `requireApproval` property: `setRequireApproval(String)`, `setRequireApproval(McpToolRequireApproval)`, `getRequireApprovalAsString()`, and `getRequireApprovalAsMcpToolRequireApproval()`.
- Added `setComparisonFilter(ComparisonFilter)` and `setCompoundFilter(CompoundFilter)` convenience methods on `FileSearchTool`, accepting the openai-java filter types directly.
- Added `listAgentConversations` operation to `AgentsClient` and `AgentsAsyncClient` to list conversations attached to a specific agent filtering by `agentName` and `agentId`.
- Added streaming response methods to `ResponsesClient` and `ResponsesAsyncClient`:
  - `createStreamingWithAgent` and `createStreamingWithAgentConversation` on `ResponsesClient` return `IterableStream<ResponseStreamEvent>` for synchronous streaming.
  - `createStreamingWithAgent` and `createStreamingWithAgentConversation` on `ResponsesAsyncClient` return `Flux<ResponseStreamEvent>` for asynchronous streaming.
- Added `StreamingUtils` implementation helper that bridges OpenAI `StreamResponse` to `IterableStream` and `AsyncStreamResponse` to `Flux`.
- Added streaming samples: `SimpleStreamingSync`/`SimpleStreamingAsync`, `FunctionCallStreamingSync`/`FunctionCallStreamingAsync`, and `CodeInterpreterStreamingSync`/`CodeInterpreterStreamingAsync`.
- Added structured input convenience methods to `ResponsesClient` and `ResponsesAsyncClient` for creating responses with agent-defined template parameters:
  - `createWithAgentStructuredInput` accepts a `Map<String, Object>` of runtime values that are substituted into the agent's prompt template.
  - `createStreamingWithAgentStructuredInput` provides the streaming equivalent, returning `IterableStream<ResponseStreamEvent>` (sync) or `Flux<ResponseStreamEvent>` (async).
- Added `CreateResponseWithStructuredInput` sample demonstrating how to define structured inputs on an agent and pass runtime values when creating a response.

### Breaking Changes

- Removed deprecated convenience methods from `ResponsesClient` and `ResponsesAsyncClient`: `createWithAgent`, `createWithAgentConversation`, `createStreamingWithAgent`, `createStreamingWithAgentConversation`, `createWithAgentStructuredInput`, and `createStreamingWithAgentStructuredInput`. Use `createAzureResponse` and `createStreamingAzureResponse` with `AzureCreateResponseOptions` instead.
- `deleteAgent(String)` on `AgentsClient` now returns `void` instead of `DeleteAgentResponse`. The corresponding async method on `AgentsAsyncClient` now returns `Mono<Void>` instead of `Mono<DeleteAgentResponse>`.
- `deleteAgentVersion(String, String)` on `AgentsClient` now returns `void` instead of `DeleteAgentVersionResponse`. The corresponding async method on `AgentsAsyncClient` now returns `Mono<Void>` instead of `Mono<DeleteAgentVersionResponse>`.
- `DeleteAgentResponse` removed from `com.azure.ai.agents.models` and is no longer part of the public API.
- `DeleteAgentVersionResponse` removed from `com.azure.ai.agents.models` and is no longer part of the public API.
- The `updateDelay` parameter on `MemoryStoresClient.beginUpdateMemories` was renamed to `updateDelayInSeconds`.
- `AgentDefinitionOptInKeys` and `FoundryFeaturesOptInKeys` changed from `ExpandableStringEnum`-based classes to standard Java `enum` types. The `values()` method now returns an array instead of a `Collection`, and the deprecated no-arg constructor is removed.
- The `timezone` property in `ApproximateLocation` and `WebSearchApproximateLocation` changed from `String` to `java.util.TimeZone`.
- The `container` property on `CodeInterpreterTool` no longer exposes `BinaryData` getter/setter publicly. Use the new typed accessors instead (e.g., `setContainer("container-id")` or `setContainer(new AutoCodeInterpreterToolParam())`).
- The `allowedTools` and `requireApproval` properties on `McpTool` no longer expose `BinaryData` getter/setter publicly. Use the new typed accessors instead (e.g., `setRequireApproval("always")` or `setAllowedTools(List.of("tool_a", "tool_b"))`).
- The `filters` property on `FileSearchTool` no longer exposes `BinaryData` getter/setter publicly.
- The `reasoning` property on `PromptAgentDefinition` now uses `com.openai.models.Reasoning` from the openai-java library instead of the previously generated `Reasoning` class. Use `Reasoning.builder().effort(ReasoningEffort.HIGH).build()` to construct values.
- Removed `ComparisonFilter`, `ComparisonFilterType`, `CompoundFilter`, `CompoundFilterType`, `Reasoning`, `ReasoningEffort`, `ReasoningSummary`, and `ReasoningGenerateSummary` from `com.azure.ai.agents.models`. Use the equivalent types from `com.openai.models` instead (e.g., `com.openai.models.ComparisonFilter`, `com.openai.models.Reasoning`).

### Other Changes

- Updated all samples and tests to use the new `createAzureResponse` and `createStreamingAzureResponse` API.
- Added `ToolsTests` and `ToolsAsyncTests` with recorded end-to-end test coverage for OpenAPI, Code Interpreter, Function Call, Web Search, MCP, and File Search tools.
- Added `StreamingTests` and `StreamingAsyncTests` with recorded test coverage for streaming responses (simple prompt, function calling, and Code Interpreter scenarios).
- Added structured input test coverage to `AgentsTests`, `AgentsAsyncTests`, `StreamingTests`, and `StreamingAsyncTests`.

## 2.0.0-beta.2 (2026-03-04)

### Features Added

- Added `buildOpenAIClient()` and `buildOpenAIAsyncClient()` methods to `AgentsClientBuilder` for simplified creation of OpenAI clients with default Azure setup
- Added new agent tool samples: `CodeInterpreterAgent`, `FileSearchAgent`, `FunctionCallAgent`, `McpAgent`, and `WebSearchAgent` (sync and async variants)
- Added `action` property to `ImageGenTool` with new `ImageGenActionEnum` (values: `GENERATE`, `EDIT`, `AUTO`).
- Added `GPT_IMAGE_1_5` to `ImageGenToolModel`.
- Added container skill types: `ContainerSkill`, `ContainerSkillType`, `ContainerAutoParam`, `ContainerNetworkPolicyParam`, and related network policy types (`ContainerNetworkPolicyAllowlistParam`, `ContainerNetworkPolicyDisabledParam`, `ContainerNetworkPolicyDomainSecretParam`, `ContainerNetworkPolicyParamType`).
- Added environment configuration for `FunctionShellToolParameter` and `InputItemFunctionShellCallItemParam` via new `FunctionShellToolParamEnvironment`, `FunctionShellCallItemParamEnvironment`, and related container/local environment parameter types. `InputItemFunctionShellCallItemParam`, `FunctionShellCallItemParamEnvironment`, and related types moved to `implementation/models` (internal).
- Added `MessageContent` and `MessageContentType` model types; subsequently moved to `implementation/models` (internal).
- Added skill parameter types: `InlineSkillParam`, `InlineSkillSourceParam`, `LocalSkillParam`, `SkillReferenceParam`.

### Breaking Changes

- Removed `ContainerAppAgentDefinition` class and `AgentKind.CONTAINER_APP` enum value. The `container_app` agent kind is no longer supported.
- Removed `CONTAINER_AGENTS_V1_PREVIEW` from `AgentDefinitionOptInKeys` and `FoundryFeaturesOptInKeys`. The `ContainerAgents=V1Preview` feature flag is no longer valid.
- Renamed computer action classes to use `Param` suffix and moved to `implementation/models` (internal):
  - `Drag` → `DragParam`
  - `DragPoint` → `CoordParam`
  - `Move` → `MoveParam`
  - `Screenshot` → `ScreenshotParam`
  - `Scroll` → `ScrollParam`
  - `Type` → `TypeParam`
  - `Wait` → `WaitParam`
- `CodeInterpreterContainerAuto` renamed to `AutoCodeInterpreterToolParam`.
- `Summary` renamed to `SummaryTextContent` and moved to `implementation/models` (internal).
- Moved ~100 model classes from `com.azure.ai.agents.models` to `com.azure.ai.agents.implementation.models`, removing them from the public API surface. This includes `InputItem` and all subtypes, `Annotation`, output content types, and related types.
- Removed public methods from `MemoryStoresClient`/`MemoryStoresAsyncClient`: `searchMemoriesWithResponse` and `beginUpdateMemories` (protocol methods accepting `BinaryData`), `searchMemories(name, scope)` (minimal convenience overload), and `searchMemories`/`beginUpdateMemories` overloads accepting `List<ResponseInputItem>`.
- Renamed model classes for naming consistency:
  - `AgentObjectVersions` renamed to `AgentDetailsVersions`
  - `OpenAIError` renamed to `ApiError`
  - `AzureFunctionDefinitionFunction` renamed to `AzureFunctionDefinitionDetails`
- Renamed tool classes from `*Param` suffix to `*Parameter`:
  - `ApplyPatchToolParam` renamed to `ApplyPatchToolParameter`
  - `CustomGrammarFormatParam` renamed to `CustomGrammarFormatParameter`
  - `CustomToolParam` renamed to `CustomToolParameter`
  - `FunctionShellToolParam` renamed to `FunctionShellToolParameter`
  - `LocalShellToolParam` renamed to `LocalShellToolParameter`
- `OpenApiFunctionDefinition`: `getDefaultParams()` and `setDefaultParams()` renamed to `getDefaultParameters()` and `setDefaultParameters()`

### Bugs Fixed

- Fixed Memory Stores long-running operations (e.g. `beginUpdateMemories`) failing because the required `Foundry-Features` header was not included in poll requests, and custom LRO terminal states (`"completed"`, `"superseded"`) were not mapped to standard `LongRunningOperationStatus` values, causing pollers to hang indefinitely.
- Fixed request parameter name from `"agent"` to `"agent_reference"` in `ResponsesClient` and `ResponsesAsyncClient` methods `createWithAgent` and `createWithAgentConversation`

### Other Changes

- Enabled and stabilised `MemoryStoresTests` and `MemoryStoresAsyncTests` (previously `@Disabled`), with timeout guards to prevent hanging.

## 2.0.0-beta.1 (2026-02-25)

### Features Added

- New `MemorySearchAgent` sample was added demonstrating memory search functionality
- Tests for `MemoryStoresClient` and `MemoryStoresAsyncClient`
- Various documentation updates
- Using unified `HttpClient` setup for Azure specifics and `openai` client library wrapping methods
- Added new `ComputerUse` samples demonstrating Computer Use tool integration (sync and async)
- Added new tool models: `ApplyPatchToolParam`, `CustomToolParam`, `FunctionShellToolParam`, `McpTool`, `WebSearchTool`, and related types
- Added `HybridSearchOptions` for file search configuration
- Added new input item types for tool call outputs (e.g., `InputItemFunctionCallOutputItemParam`, `InputItemComputerCallOutputItemParam`, `InputItemApplyPatchToolCallItemParam`)
- Added status enums for output items (e.g., `OutputItemCodeInterpreterToolCallStatus`, `OutputItemFunctionToolCallStatus`, `OutputItemWebSearchToolCallStatus`)

### Breaking Changes

- `MemoryStoreObject` was renamed to `MemoryStoreDetails`
- Service version changed from date-based versions (`V2025_05_01`, `V2025_05_15_PREVIEW`, `V2025_11_15_PREVIEW`) to `V1`
- `ListAgentsRequestOrder` was renamed to `PageOrder`
- Widespread model renaming to align with the latest API spec. Key patterns include:
  - Tool classes renamed to use `Tool` or `PreviewTool` suffix (e.g., `AzureAISearchAgentTool` → `AzureAISearchTool`, `BingGroundingAgentTool` → `BingGroundingTool`, `SharepointAgentTool` → `SharepointPreviewTool`, `MemorySearchTool` → `MemorySearchPreviewTool`)
  - Computer action classes simplified (e.g., `ComputerActionClick` → `ClickParam`, `ComputerActionScroll` → `Scroll`, `ComputerActionScreenshot` → `Screenshot`, `ComputerActionKeyPress` → `KeyPressAction`)
  - Item content classes renamed (e.g., `ItemContentInputText` → `InputContentInputTextContent`, `ItemContentOutputText` → `OutputMessageContentOutputTextContent`, `ItemContentRefusal` → `OutputMessageContentRefusalContent`)
  - Tool call item param classes renamed to `InputItem*` pattern (e.g., `FunctionToolCallItemParam` → `InputItemFunctionToolCall`, `ComputerToolCallItemParam` → `InputItemComputerToolCall`)
  - Annotation classes renamed (e.g., `AnnotationFileCitation` → `FileCitationBody`, `AnnotationUrlCitation` → `UrlCitationBody`, `AnnotationFilePath` → `FilePath`)
  - `Error` renamed to `OpenAIError`
  - `DeleteMemoryStoreResponse` renamed to `DeleteMemoryStoreResult`
  - `ImageGenToolSize` enum values renamed (e.g., `SIZE_1024X1024` → `RESOLUTION_1024_X_1024`)
- Several models were removed: `AgentId`, `ImageBasedHostedAgentDefinition`, `MCPTool`, `MCPToolAllowedTools`, `MCPToolRequireApprovalAlways`, `MCPToolRequireApprovalNever`, `ResponsesMessageItemParam` and its role-specific subclasses
- `ItemReferenceItemParam` and `ResponsesMessageRole` moved to internal implementation package

### Bugs Fixed

- Fixed base URL construction in `AgentsClientBuilder` to append `/openai/v1` directly, removing dependency on `AzureOpenAIServiceVersion` and `AzureUrlPathMode` for URL path resolution

### Other Changes

- Updated version of `openai` client library to `4.14.0`
- Regenerated client from the latest AI Foundry API spec
- `openai-java-client-okhttp` and `openai-java-core` modules are now `transitive` dependencies in `module-info.java`

## 1.0.0-beta.1 (2025-11-12)

### Features Added

- New Azure Agents client library for Java. This package contains Microsoft Azure Agents client library.
