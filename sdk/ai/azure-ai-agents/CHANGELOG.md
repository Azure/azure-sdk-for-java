# Release History

## 2.0.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
