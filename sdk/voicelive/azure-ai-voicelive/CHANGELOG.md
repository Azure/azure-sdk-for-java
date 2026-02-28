# Release History

## 1.0.0-beta.6 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

- Changed default service API version from `2025-10-01` to `2026-01-01-preview`

## 1.0.0-beta.5 (2026-02-13)

### Features Added

- Added `AgentSessionConfig` class for configuring Azure AI Foundry agent sessions:
  - Constructor takes required `agentName` and `projectName` parameters
  - Fluent setters for optional parameters: `setAgentVersion()`, `setConversationId()`, `setAuthenticationIdentityClientId()`, `setFoundryResourceOverride()`
  - `toQueryParameters()` method for converting configuration to WebSocket query parameters
- Added new `startSession(AgentSessionConfig)` overload to `VoiceLiveAsyncClient` for connecting directly to Azure AI Foundry agents
- Added `startSession(AgentSessionConfig, VoiceLiveRequestOptions)` overload for agent sessions with custom request options
- Added `Scene` class for configuring avatar's zoom level, position, rotation and movement amplitude in the video frame
- Added `scene` property to `AvatarConfiguration` for avatar scene configuration
- Added `outputAuditAudio` property to `AvatarConfiguration` to enable audit audio forwarding via WebSocket for review/debugging purposes
- Added `ServerEventWarning` and `ServerEventWarningDetails` classes for non-interrupting warning events
- Added `ServerEventType.WARNING` enum value
- Added interim response configuration for handling latency and tool calls (replaces filler response):
  - `InterimResponseConfigBase` base class for interim response configurations
  - `StaticInterimResponseConfig` for static/random text interim responses
  - `LlmInterimResponseConfig` for LLM-generated context-aware interim responses
  - `InterimResponseConfigType` enum (static_interim_response, llm_interim_response)
  - `InterimResponseTrigger` enum for trigger conditions (latency, tool)
  - Added `interimResponse` property to `VoiceLiveSessionOptions` and `VoiceLiveSessionResponse`

### Breaking Changes

- Changed token authentication scope from `https://cognitiveservices.azure.com/.default` to `https://ai.azure.com/.default`
- Removed `FoundryAgentTool` class - use `AgentSessionConfig` with `startSession(AgentSessionConfig)` for direct agent connections instead
- Removed `FoundryAgentContextType` enum
- Removed `ResponseFoundryAgentCallItem` class
- Removed Foundry agent call lifecycle server events: `ServerEventResponseFoundryAgentCallArgumentsDelta`, `ServerEventResponseFoundryAgentCallArgumentsDone`, `ServerEventResponseFoundryAgentCallInProgress`, `ServerEventResponseFoundryAgentCallCompleted`, `ServerEventResponseFoundryAgentCallFailed`
- Removed `ItemType.FOUNDRY_AGENT_CALL` enum value
- Removed `ToolType.FOUNDRY_AGENT` enum value
- Removed `ServerEventType.MCP_APPROVAL_REQUEST` and `ServerEventType.MCP_APPROVAL_RESPONSE` enum values
- Renamed filler response API to interim response:
  - `FillerResponseConfigBase` → `InterimResponseConfigBase`
  - `BasicFillerResponseConfig` → `StaticInterimResponseConfig`
  - `LlmFillerResponseConfig` → `LlmInterimResponseConfig`
  - `FillerResponseConfigType` → `InterimResponseConfigType`
  - `FillerTrigger` → `InterimResponseTrigger`
  - `VoiceLiveSessionOptions.getFillerResponse()`/`setFillerResponse()` → `getInterimResponse()`/`setInterimResponse()`
  - Type values changed: `static_filler` → `static_interim_response`, `llm_filler` → `llm_interim_response`

## 1.0.0-beta.4 (2026-02-09)

### Features Added

- Added `VoiceLiveRequestOptions` class for per-request customization:
  - Supports custom query parameters via `addCustomQueryParameter(String key, String value)` method
  - Supports custom headers via `addCustomHeader(String name, String value)` and `setCustomHeaders(HttpHeaders)` methods
  - Custom parameters and headers can be passed to session creation methods
- Enhanced session creation with new overloads:
  - Added `startSession(String model, VoiceLiveRequestOptions requestOptions)` for model with custom options
  - Added `startSession(VoiceLiveRequestOptions requestOptions)` for custom options without explicit model parameter
  - Original `startSession(String model)` and `startSession()` methods preserved for backward compatibility
- Added Foundry Agent tool support:
  - `FoundryAgentTool` for integrating Foundry agents as tools in VoiceLive sessions
  - `FoundryAgentContextType` enum for configuring agent context (no_context, agent_context)
  - `ResponseFoundryAgentCallItem` for tracking Foundry agent call responses
  - Foundry agent call lifecycle events: `ServerEventResponseFoundryAgentCallArgumentsDelta`, `ServerEventResponseFoundryAgentCallArgumentsDone`, `ServerEventResponseFoundryAgentCallInProgress`, `ServerEventResponseFoundryAgentCallCompleted`, `ServerEventResponseFoundryAgentCallFailed`
  - `ItemType.FOUNDRY_AGENT_CALL` and `ToolType.FOUNDRY_AGENT` discriminator values
- Added filler response configuration for handling latency and tool calls (renamed to interim response in 1.0.0-beta.5):
  - `FillerResponseConfigBase` base class for filler response configurations
  - `BasicFillerResponseConfig` for static/random text filler responses
  - `LlmFillerResponseConfig` for LLM-generated context-aware filler responses
  - `FillerResponseConfigType` enum (static_filler, llm_filler)
  - `FillerTrigger` enum for trigger conditions (latency, tool)
  - Added `fillerResponse` property to `VoiceLiveSessionOptions` and `VoiceLiveSessionResponse`
- Added reasoning effort configuration for reasoning models:
  - `ReasoningEffort` enum with levels: none, minimal, low, medium, high, xhigh
  - Added `reasoningEffort` property to `VoiceLiveSessionOptions`, `VoiceLiveSessionResponse`, and `ResponseCreateParams`
- Added metadata support:
  - Added `metadata` property to `ResponseCreateParams` and `SessionResponse` for attaching key-value pairs
- Added custom text normalization URL support for Azure voices:
  - Added `customTextNormalizationUrl` property to `AzureCustomVoice`, `AzurePersonalVoice`, and `AzureStandardVoice`

### Bugs Fixed

- Fixed `OutputAudioFormat` enum values from dash-separated to underscore-separated:
  - `pcm16-8000hz` → `pcm16_8000hz`
  - `pcm16-16000hz` → `pcm16_16000hz`

## 1.0.0-beta.3 (2025-12-03)

### Features Added

- Added image input support for multimodal conversations:
  - `RequestImageContentPart` for including images in conversation messages with URL references
  - `RequestImageContentPartDetail` enum for controlling image detail level (auto, low, high)
  - `ContentPartType.INPUT_IMAGE` discriminator for image content parts
- Added avatar configuration enhancements:
  - `AvatarConfiguration` class for configuring avatar streaming and behavior with ICE servers, character selection, style, and video parameters
  - `AvatarConfigTypes` enum for video and photo avatar types
  - `AvatarOutputProtocol` enum supporting WebRTC and WebSocket protocols
  - `PhotoAvatarBaseModes` enum with VASA-1 model support
- Added token usage tracking improvements:
  - `CachedTokenDetails` for tracking cached text, audio, and image tokens
  - Enhanced `InputTokenDetails` with image token tracking and cached token details
- Added MCP call lifecycle events:
  - `ServerEventResponseMcpCallInProgress` for tracking ongoing MCP calls
  - `ServerEventResponseMcpCallCompleted` for successful MCP call completion
  - `ServerEventResponseMcpCallFailed` for failed MCP calls
- Added two new OpenAI voices: `OpenAIVoiceName.MARIN` and `OpenAIVoiceName.CEDAR`
- Enhanced `AzurePersonalVoice` with additional customization options:
  - Custom lexicon URL support for pronunciation customization
  - Locale preferences with `preferLocales` for multilingual scenarios
  - Voice style, pitch, rate, and volume controls for fine-tuned voice characteristics

## 1.0.0-beta.2 (2025-11-14)

### Features Added

- Added Model Context Protocol (MCP) support for tool integration:
  - `MCPServer` class for defining MCP server configurations with server label, URL, authorization, headers, and tool restrictions
  - `MCPTool` class representing MCP tool definitions with name, description, input schema, and annotations
  - `ResponseMCPListToolItem` for listing available tools on an MCP server
  - `ResponseMCPCallItem` for MCP tool call responses with arguments, output, and error handling
  - `ResponseMCPApprovalRequestItem` and `ResponseMCPApprovalResponseItem` for tool call approval workflow
  - Server events: `ServerEventMcpListToolsInProgress`, `ServerEventMcpListToolsCompleted`, `ServerEventMcpListToolsFailed`
  - Server events: `ServerEventResponseMcpCallArgumentsDelta`, `ServerEventResponseMcpCallArgumentsDone`
  - New `ToolType.MCP` for MCP-based tool definitions
  - New `ServerEventType` constants for MCP-related events

### Other Changes

#### Dependency Updates

- Dependency versions remain unchanged from `1.0.0-beta.1`.

## 1.0.0-beta.1 (2025-11-10)

- Initial release of Azure VoiceLive client library for Java. This library enables real-time, bidirectional voice conversations with AI assistants using WebSocket-based streaming communication.

### Features Added

- `VoiceLiveAsyncClient` for managing real-time voice communication sessions with Azure VoiceLive service
- `VoiceLiveSessionAsyncClient` for WebSocket-based bidirectional streaming of audio and events
- `VoiceLiveClientBuilder` with support for both API Key and Azure AD token authentication
- Real-time audio input streaming with support for PCM16 format at 24kHz sample rate
- Audio output streaming with automatic delta decoding
- Server-side Voice Activity Detection (VAD) with configurable thresholds and turn detection
- Audio enhancements including noise reduction and echo cancellation
- Input audio transcription support using Whisper models
- Conversation management with support for adding, deleting, and truncating conversation items
- Response generation control with support for interruption and cancellation
- Configurable session options including voice selection, modalities, and audio formats
- Support for OpenAI voices (Alloy, Ash, Ballad, Coral, Echo, Sage, Shimmer, Verse)
- Support for Azure voices including AzureStandardVoice, AzureCustomVoice, and AzurePersonalVoice
- Audio turn management for multi-turn conversations
- Avatar connection support for video-enabled scenarios
- Comprehensive sample applications demonstrating microphone input, audio playback, and complete voice assistant implementations
