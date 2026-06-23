# Release History

## 2.2.0-beta.1 (Unreleased)

### Features Added

- Added protocol-style `listOptimizationCandidates(String, com.azure.core.http.rest.RequestOptions)` overloads on `AgentsClient` and `AgentsAsyncClient` for listing raw optimization candidate pages as `BinaryData`.

### Breaking Changes

- Preview operation groups now use beta-prefixed clients built through `AgentsClientBuilder.beta()`: `MemoryStoresClient` / `MemoryStoresAsyncClient` renamed to `BetaMemoryStoresClient` / `BetaMemoryStoresAsyncClient`, `ToolboxesClient` / `ToolboxesAsyncClient` renamed to `BetaToolboxesClient` / `BetaToolboxesAsyncClient`, and preview agent/session operations moved to `BetaAgentsClient` / `BetaAgentsAsyncClient`. `AgentSessionFilesClient` / `AgentSessionFilesAsyncClient` were removed; use the session-file methods on `BetaAgentsClient` / `BetaAgentsAsyncClient` instead.
- `listOptimizationCandidates` on `AgentsClient` and `AgentsAsyncClient` now returns paged optimization candidates (`PagedIterable<OptimizationCandidate>` / `PagedFlux<OptimizationCandidate>`) instead of `OptimizationCandidatePagedResult` / `Mono<OptimizationCandidatePagedResult>`. The `OptimizationCandidatePagedResult` model was removed. The protocol methods where adjusted accordingly.

### Bugs Fixed

- Fixed the agent-scoped OpenAI client returned by `AgentsClientBuilder.buildAgentScopedOpenAIClient` and `buildAgentScopedOpenAIAsyncClient` so requests to a hosted-agent endpoint target the correct URL. Previously the request path was duplicated (`.../protocols/openai/openai/responses`) and used an unsupported default `api-version`, causing `400` errors when invoking the OpenAI Responses API or streaming session logs through an agent endpoint. The client now uses the unified Azure URL path mode and sends `api-version=v1`.
- Fixed OpenAI and Responses clients built from `AgentsClientBuilder` to honor a custom `HttpPipeline` supplied through `pipeline(...)`, preserving custom policies while still adding required preview feature headers for applicable preview clients.

### Other Changes

## 2.1.0 (2026-06-01)

### Features Added

- Added protocol-style methods on `ResponsesClient` and `ResponsesAsyncClient` that accept a raw JSON request body (`BinaryData`) and a `com.openai.core.RequestOptions`, and return the openai-java raw HTTP response. These mirror the existing `createAzureResponse` and `createStreamingAzureResponse` typed surface: `createResponseWithResponse` (returns `HttpResponseFor<Response>`) and `createResponseStreamWithResponse` (returns `HttpResponseFor<StreamResponse<ResponseStreamEvent>>`). They delegate to the underlying openai-java `ResponseService.withRawResponse()` surface and continue to flow through the Azure HTTP pipeline.
- Added preview support for external agents via `ExternalAgentDefinition`, `AgentKind.EXTERNAL`, and `AgentDefinitionOptInKeys.EXTERNAL_AGENTS_V1_PREVIEW`.
- Added preview code-based hosted agent operations on `AgentsClient` and `AgentsAsyncClient`, including `createAgentVersionFromCode`, `updateAgentFromCode`, and `downloadAgentCode`, plus related code package models such as `CreateAgentVersionFromCodeContent`, `CodeFileDetails`, and `CodeDependencyResolution`. `CodeConfiguration` now exposes the service-computed code package hash via `getContentSha256()`.
- Added preview agent optimization job and candidate management operations on `AgentsClient` and `AgentsAsyncClient`, including creating, listing, retrieving, canceling, and deleting optimization jobs, listing and inspecting candidates, downloading candidate files, and promoting candidates.
- Added `stopSession` and `stopSessionWithResponse` to stop hosted-agent sessions.
- Added `force` query parameter support for hosted-agent `deleteAgentWithResponse` and `deleteAgentVersionWithResponse` requests through `RequestOptions`, allowing active sessions to be cascade-deleted.
- Added individual memory item operations to `MemoryStoresClient` and `MemoryStoresAsyncClient`: `createMemory`, `updateMemory`, `listMemories`, `getMemory`, and `deleteMemory`, with new `ListMemoriesOptions`, `DeleteMemoryResponse`, and `MemoryItemKind.PROCEDURAL` support.
- Added new preview tools `FabricIqPreviewTool` and `ToolboxSearchPreviewTool`, plus related tool call/output models for Azure tools.
- Added optional per-tool configuration via `ToolConfig` and `toolConfigs` accessors on supported tool classes.
- Added `getComparisonFilter()` and `getCompoundFilter()` convenience getters on `FileSearchTool` for retrieving OpenAI filter types.
- Added new feature-flag values, including `AgentDefinitionOptInKeys.CODE_AGENTS_V1_PREVIEW`, `AgentDefinitionOptInKeys.EXTERNAL_AGENTS_V1_PREVIEW`, and `FoundryFeaturesOptInKeys.AGENTS_OPTIMIZATION_V1_PREVIEW`.
- Added hosted-agent, Fabric IQ, Toolbox Search, and async toolbox samples.

### Breaking Changes

- `AgentEndpoint` renamed to `AgentEndpointConfig`.
- Session file listing methods on `AgentSessionFilesClient` and `AgentSessionFilesAsyncClient` were renamed from `getSessionFiles` to `listSessionFiles` and now return paged `SessionDirectoryEntry` results. `SessionDirectoryListResponse` was removed.
- Hosted-agent session methods no longer take a required `isolationKey` argument. Use overloads that accept the optional `userIsolationKey` value, or set the `x-ms-user-isolation-key` header through `RequestOptions`.
- `AgentDefinitionOptInKeys.CONTAINER_AGENTS_V1_PREVIEW` was removed. Use the applicable hosted-agent, code-agent, agent-endpoint, workflow-agent, or external-agent opt-in key instead.
- `HostedAgentDefinition` no longer exposes top-level `image` or `containerProtocolVersions` accessors. Use `ContainerConfiguration` for container images and `protocolVersions` for ingress protocol configuration.
- `CodeConfiguration` constructor now requires `CodeDependencyResolution` in addition to runtime and entry point.
- `WorkIqPreviewTool` now takes the Work IQ project connection ID directly. `WorkIQPreviewToolParameters` was removed.

### Other Changes

- Enabled `ResponsesTests` and `ResponsesAsyncTests` (previously `@Disabled`) with create/retrieve/delete/input-items and background-cancel coverage for the typed (`ResponseService` / `ResponseServiceAsync`) surface, plus coverage for the new protocol-method surface. Recordings published to `Azure/azure-sdk-assets` and referenced from `assets.json`.
- Re-enabled `SessionLogSyncTest` and `SessionLogAsyncTest`; both tests are recordable via `@RecordWithoutRequestBody` and run live against the configured Foundry project.
- Regenerated client from the updated TypeSpec specification.

## 2.1.0-beta.1 (2026-05-12)

### Features Added

- Added new `ToolboxesClient` and `ToolboxesAsyncClient` sub-clients (preview, opt-in via `FoundryFeaturesOptInKeys.TOOLBOXES_V1_PREVIEW`) for managing toolboxes and toolbox versions, with operations including `createToolboxVersion`, `getToolbox`, `getToolboxVersion`, `listToolboxes`, `listToolboxVersions`, `updateToolbox`, `deleteToolbox`, and `deleteToolboxVersion`. New `buildToolboxesClient()` and `buildToolboxesAsyncClient()` methods on `AgentsClientBuilder`.
- Added new `AgentSessionFilesClient` and `AgentSessionFilesAsyncClient` sub-clients for working with files in an agent session, with `uploadSessionFile`, `downloadSessionFile`, `getSessionFiles`, and `deleteSessionFile`. New `buildAgentSessionFilesClient()` and `buildAgentSessionFilesAsyncClient()` methods on `AgentsClientBuilder`.
- Added `buildAgentScopedOpenAIClient(String agentName)` and `buildAgentScopedOpenAIAsyncClient(String agentName)` to `AgentsClientBuilder` for constructing OpenAI clients targeting a specific agent's endpoint (base URL `{endpoint}/agents/{agentName}/endpoint/protocols/openai`). The default `buildOpenAIClient()` / `buildOpenAIAsyncClient()` continue to target `{endpoint}/openai/v1`.
- Added agent-session operations to `AgentsClient` and `AgentsAsyncClient`: `createSession`, `getSession`, `deleteSession`, `listSessions`, and `getSessionLogStreamWithResponse`. Added typed session log streaming convenience methods: `AgentsClient.getSessionLogStream(...)`, and `AgentsAsyncClient.getSessionLogStream(...)`, returning `SessionLogEvent`. New related models: `AgentSessionResource`, `AgentSessionStatus`, `SessionDirectoryEntry`, `SessionDirectoryListResponse`, `SessionFileWriteResult`, `SessionLogEvent`, `SessionLogEventType`, `IsolationKeySource` (with `Kind`), `EntraIsolationKeySource`, and `HeaderIsolationKeySource`.
- Added `updateAgentDetails(String, UpdateAgentDetailsPatchRequest, ...)` and `updateAgentDetailsWithResponse` on `AgentsClient`/`AgentsAsyncClient` for patching agent details, plus new `UpdateAgentDetailsPatchRequest` model.
- Added new agent-endpoint and identity model types for hosted agents: `AgentEndpoint`, `AgentEndpointProtocol`, `AgentEndpointAuthorizationScheme` (with `Type`), `EntraAuthorizationScheme`, `BotServiceAuthorizationScheme`, `BotServiceRbacAuthorizationScheme`, `AgentIdentity`, `AgentBlueprintReference` (with `Type`), `ManagedAgentIdentityBlueprintReference`, `AgentCard`, and `AgentCardSkill`. `AgentDetails` now exposes `getAgentEndpoint`, `getInstanceIdentity`, `getBlueprint`, `getBlueprintReference`, and `getAgentCard`. `AgentVersionDetails` now exposes `getInstanceIdentity`, `getBlueprint`, `getBlueprintReference`, and `getAgentGuid`.
- Added agent-versioning model types: `VersionIndicator` (with `Type`), `VersionRefIndicator`, `VersionSelector` (with `Type`), `VersionSelectionRule`, `FixedRatioVersionSelectionRule`, and `CreateAgentVersionInput`.
- `HostedAgentDefinition` now supports both container-based and code-based deployments: added `ContainerConfiguration` and `CodeConfiguration` model types, with new `getContainerConfiguration`/`setContainerConfiguration`, `getCodeConfiguration`/`setCodeConfiguration`, `getProtocolVersions`/`setProtocolVersions`, and `setContainerProtocolVersions` accessors. Container vs. code configuration is mutually exclusive (validated server-side).
- Added new preview tool `WorkIqPreviewTool` (and parameters `WorkIQPreviewToolParameters`) with discriminator value `work_iq_preview`. Added `ToolType.WORK_IQ_PREVIEW`.
- Added optional `name` and `description` properties (with getters and setters) to `CodeInterpreterTool`, `CaptureStructuredOutputsTool`, `FileSearchTool`, `ImageGenTool`, `WebSearchTool`, and `WorkIqPreviewTool` for user-defined tool labels.
- Added new feature-flag values to `FoundryFeaturesOptInKeys`: `TOOLBOXES_V1_PREVIEW` (`Toolboxes=V1Preview`) and `SKILLS_V1_PREVIEW` (`Skills=V1Preview`).
- Added new feature-flag values to `AgentDefinitionOptInKeys`: `CONTAINER_AGENTS_V1_PREVIEW` (`ContainerAgents=V1Preview`) and `AGENT_ENDPOINT_V1_PREVIEW` (`AgentEndpoints=V1Preview`).
- Added new toolbox samples under `com.azure.ai.agents.toolboxes`: `CreateToolboxVersion`, `GetToolbox`, `GetToolboxVersion`, `ListToolboxes`, `ListToolboxVersions`, `UpdateToolbox`, `DeleteToolbox`, and `DeleteToolboxVersion`.

### Breaking Changes

- `HostedAgentDefinition`'s canonical (`@Generated`) constructor changed from `HostedAgentDefinition(List<ProtocolVersionRecord> containerProtocolVersions, String cpu, String memory)` to `HostedAgentDefinition(String cpu, String memory)`; `containerProtocolVersions` is now a mutable property set via `setContainerProtocolVersions(...)`. The previous 3-argument constructor is retained for source compatibility but is no longer the recommended entry point.

### Other Changes

- Regenerated client from the updated TypeSpec specification.
- Added README examples for synchronous and asynchronous hosted agent session log streaming.

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
