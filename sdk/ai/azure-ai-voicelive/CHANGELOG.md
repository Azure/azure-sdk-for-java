# Release History

## 1.0.0-beta.1 (2025-11-07)

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
