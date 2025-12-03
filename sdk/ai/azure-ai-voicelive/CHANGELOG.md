# Release History

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
