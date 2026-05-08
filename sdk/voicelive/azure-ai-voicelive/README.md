# Azure VoiceLive client library for Java

The Azure VoiceLive client library for Java enables real-time, bidirectional voice conversations with AI assistants. Built on WebSocket technology, it provides low-latency audio streaming with support for voice activity detection, interruption handling, and flexible authentication.

Use the Azure VoiceLive client library for Java to:

* Create real-time voice conversations with AI assistants
* Stream audio input from microphone with automatic voice activity detection
* Receive and play audio responses with interruption support
* Handle conversational flow with turn detection and session management
* Authenticate using API keys or Azure AD (token credentials)

[Source code][source_code] | [API reference documentation][docs] | [Product documentation][product_documentation] | [Samples][samples_folder]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- Azure VoiceLive resource with endpoint and API key

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-voicelive;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-voicelive</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

To interact with the Azure VoiceLive service, you'll need to create an instance of the [VoiceLiveAsyncClient][voicelive_client_async] using [VoiceLiveClientBuilder][voicelive_client_builder]. The client supports two authentication methods:

#### Authenticate with API Key

Get your Azure VoiceLive API key from the Azure Portal:

```java com.azure.ai.voicelive.authentication.apikey
VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
    .endpoint("https://your-resource.openai.azure.com/")
    .credential(new AzureKeyCredential("your-api-key"))
    .buildAsyncClient();
```

#### Authenticate with Azure AD (Token Credential)

Azure SDK for Java supports Azure Identity, making it easy to use Microsoft identity platform for authentication.

First, add the Azure Identity package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.18.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

Then create a client with DefaultAzureCredential:

```java com.azure.ai.voicelive.authentication.defaultcredential
TokenCredential credential = new DefaultAzureCredentialBuilder().build();
VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
    .endpoint("https://your-resource.openai.azure.com/")
    .credential(credential)
    .buildAsyncClient();
```

For development and testing, you can use Azure CLI credentials:

```java com.azure.ai.voicelive.authentication.azurecli
TokenCredential credential = new AzureCliCredentialBuilder().build();
VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
    .endpoint("https://your-resource.openai.azure.com/")
    .credential(credential)
    .buildAsyncClient();
```

## Key concepts

### VoiceLiveAsyncClient

The main entry point for interacting with the Azure VoiceLive service. Use the `VoiceLiveClientBuilder` to construct a client instance. The client provides methods to start sessions and manage real-time voice conversations.

### VoiceLiveSessionAsyncClient

Represents an active WebSocket connection for bidirectional streaming communication. This async client supports:
- Sending audio input streams via `sendInputAudio()`
- Sending command events via `sendEvent()`
- Receiving server events as a reactive stream (Flux) via `receiveEvents()`
- Graceful shutdown with `close()` or `closeAsync()`

### VoiceLiveSessionOptions

Configuration options for customizing session behavior:
- **Model selection**: Specify the AI model (e.g., "gpt-4o-realtime-preview")
- **Voice settings**: Choose from OpenAI voices (Alloy, Ash, Ballad, Coral, Echo, Sage, Shimmer, Verse) or Azure voices
- **Modalities**: Configure text and/or audio interaction modes
- **Turn detection**: Server-side voice activity detection with configurable thresholds
- **Audio formats**: PCM16 input/output with configurable sample rates
- **Audio enhancements**: Noise reduction and echo cancellation
- **Transcription**: Optional input audio transcription using Whisper models

### Audio Requirements

The VoiceLive service uses specific audio formats:
- **Sample Rate**: 24kHz (24000 Hz)
- **Bit Depth**: 16-bit PCM
- **Channels**: Mono (1 channel)
- **Format**: Signed PCM, little-endian

## Examples

The following sections provide code snippets for common scenarios:

* [Simple voice assistant](#simple-voice-assistant)
* [Configure session options](#configure-session-options)
* [Send audio input](#send-audio-input)
* [Handle event types](#handle-event-types)
* [Voice configuration](#voice-configuration)
* [Function calling](#function-calling)
* [MCP tool integration](#mcp-tool-integration)
* [Azure AI Foundry agent session](#azure-ai-foundry-agent-session)
* [Telemetry and tracing](#telemetry-and-tracing)
* [Complete voice assistant with microphone](#complete-voice-assistant-with-microphone)

### Focused Sample Files

For easier learning, explore these focused samples in order:

1. **BasicVoiceConversationSample.java** - Start here to learn the basics
   - Minimal setup and session management
   - Client creation and configuration
   - Basic event handling

2. **AuthenticationMethodsSample.java** - Learn authentication options
   - API Key authentication (default)
   - Token Credential authentication with DefaultAzureCredential

3. **MicrophoneInputSample.java** - Add audio input capability
   - Real-time microphone audio capture
   - Audio format configuration (24kHz, 16-bit PCM, mono)
   - Streaming audio to the service
   - Speech detection events

4. **AudioPlaybackSample.java** - Add audio output capability
   - Receiving audio responses from service
   - Audio playback to speakers
   - Response completion tracking

5. **VoiceAssistantSample.java** - Complete production-ready implementation
   - Full bidirectional audio streaming
   - Voice Activity Detection (VAD) with interruption handling
   - Audio transcription with Whisper
   - Noise reduction and echo cancellation
   - Multi-threaded audio processing

6. **FunctionCallingSample.java** - Voice assistant with custom function tools
   - Define function tools with parameters
   - Register functions with the VoiceLive session
   - Handle function call requests from the AI model
   - Execute functions locally and return results
   - Continue conversation with function results

7. **telemetry/GlobalTracingSample.java** - Automatic tracing via GlobalOpenTelemetry
   - Zero builder configuration — uses `buildAndRegisterGlobal()`
   - Also works with OTel Java agent (`-javaagent:opentelemetry-javaagent.jar`)

8. **MCPSample.java** - Model Context Protocol (MCP) tool integration
   - Configure MCP servers for external tool access
   - Handle MCP call events and tool execution
   - Handle MCP approval requests for tool calls
   - Process MCP call results and continue conversations

9. **AgentV2Sample.java** - Azure AI Foundry agent session
   - Connect directly to an Azure AI Foundry agent via AgentSessionConfig
   - Real-time audio capture and playback
   - Sequence number based audio for interrupt handling
   - Azure noise suppression and echo cancellation
   - Conversation logging to file

> **Note:** To run audio samples (AudioPlaybackSample, MicrophoneInputSample, VoiceAssistantSample, FunctionCallingSample):
> ```bash
> mvn exec:java -Dexec.mainClass=com.azure.ai.voicelive.FunctionCallingSample -Dexec.classpathScope=test
> ```
> These samples use `javax.sound.sampled` for audio I/O.

### Simple voice assistant

Create a basic voice assistant session:

```java com.azure.ai.voicelive.simple.session
// Start session with default options
client.startSession("gpt-realtime")
    .flatMap(session -> {
        System.out.println("Session started");

        // Listen for events.
        return session.receiveEvents()
            .doOnNext(event -> System.out.println("Event: " + event.getType()))
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .then();
    })
    .block();
```

### Configure session options

Customize the session with specific options:

```java com.azure.ai.voicelive.configure.sessionoptions
// Configure server-side voice activity detection
ServerVadTurnDetection turnDetection = new ServerVadTurnDetection()
    .setThreshold(0.5)                    // Sensitivity threshold (0.0-1.0)
    .setPrefixPaddingMs(300)              // Audio before speech detection
    .setSilenceDurationMs(500)            // Silence to end turn
    .setInterruptResponse(true)           // Allow user interruptions
    .setAutoTruncate(true)                // Auto-truncate on interruption
    .setCreateResponse(true);             // Auto-create response after turn

// Configure input audio transcription
AudioInputTranscriptionOptions transcription = new AudioInputTranscriptionOptions(
    AudioInputTranscriptionOptionsModel.WHISPER_1);

// Create session options
VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
    .setInstructions("You are a helpful AI voice assistant. Respond naturally and conversationally.")
    .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
    .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
    .setInputAudioFormat(InputAudioFormat.PCM16)
    .setOutputAudioFormat(OutputAudioFormat.PCM16)
    .setInputAudioSamplingRate(24000)
    .setInputAudioNoiseReduction(new AudioNoiseReduction(AudioNoiseReductionType.NEAR_FIELD))
    .setInputAudioEchoCancellation(new AudioEchoCancellation())
    .setInputAudioTranscription(transcription)
    .setTurnDetection(turnDetection);

// Start session with options
client.startSession("gpt-realtime")
    .flatMap(session -> {
        // Send session configuration
        ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(options);
        return session.sendEvent(updateEvent).then(Mono.just(session));
    })
    .subscribe(
        session -> System.out.println("Session configured"),
        error -> System.err.println("Error: " + error.getMessage())
    );
```

### Send audio input

Stream audio data to the service:

```java com.azure.ai.voicelive.send.audioinput
// Send audio chunk
byte[] audioData = readAudioChunk(); // Your audio data in PCM16 format
session.sendInputAudio(BinaryData.fromBytes(audioData))
    .subscribe();

// Send audio from file
try {
    Path audioFile = Paths.get("audio.wav");
    byte[] fileData = Files.readAllBytes(audioFile);
    session.sendInputAudio(BinaryData.fromBytes(fileData))
        .subscribe();
} catch (IOException e) {
    System.err.println("Error reading audio file: " + e.getMessage());
}
```

### Handle event types

Process different event types for complete conversation flow:

```java com.azure.ai.voicelive.handle.eventtypes
session.receiveEvents()
    .subscribe(event -> {
        ServerEventType eventType = event.getType();

        if (ServerEventType.SESSION_CREATED.equals(eventType)) {
            System.out.println("✓ Session created - ready to start");
        } else if (ServerEventType.SESSION_UPDATED.equals(eventType)) {
            System.out.println("✓ Session configured - starting conversation");
            if (event instanceof SessionUpdateSessionUpdated) {
                SessionUpdateSessionUpdated updated = (SessionUpdateSessionUpdated) event;
                // Access session configuration details
                String json = BinaryData.fromObject(updated).toString();
                System.out.println("Config: " + json);
            }
        } else if (ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED.equals(eventType)) {
            System.out.println("🎤 User started speaking");
        } else if (ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED.equals(eventType)) {
            System.out.println("🤔 User stopped speaking - processing...");
        } else if (ServerEventType.RESPONSE_AUDIO_DELTA.equals(eventType)) {
            // Play audio response
            if (event instanceof SessionUpdateResponseAudioDelta) {
                SessionUpdateResponseAudioDelta audioEvent =
                    (SessionUpdateResponseAudioDelta) event;
                playAudioChunk(audioEvent.getDelta());
            }
        } else if (ServerEventType.RESPONSE_AUDIO_DONE.equals(eventType)) {
            System.out.println("🔊 Assistant finished speaking");
        } else if (ServerEventType.RESPONSE_DONE.equals(eventType)) {
            System.out.println("✅ Response complete - ready for next input");
        } else if (ServerEventType.ERROR.equals(eventType)) {
            if (event instanceof SessionUpdateError) {
                SessionUpdateError errorEvent = (SessionUpdateError) event;
                System.err.println("❌ Error: "
                    + errorEvent.getError().getMessage());
            }
        }
    });
```

### Voice configuration

The SDK supports multiple voice providers:

#### OpenAI Voices

```java com.azure.ai.voicelive.voice.openai
// Use OpenAIVoiceName enum for available voices (ALLOY, ASH, BALLAD, CORAL, ECHO, SAGE, SHIMMER, VERSE)
VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
    .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)));
```

#### Azure Voices

Azure voices include `AzureStandardVoice`, `AzureCustomVoice`, and `AzurePersonalVoice` (all extend `AzureVoice`):

```java com.azure.ai.voicelive.voice.azure
// Azure Standard Voice - use any Azure TTS voice name
// See: https://learn.microsoft.com/azure/ai-services/speech-service/language-support?tabs=tts
VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
    .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-JennyNeural")));

// Azure Custom Voice - requires custom voice name and endpoint ID
VoiceLiveSessionOptions options2 = new VoiceLiveSessionOptions()
    .setVoice(BinaryData.fromObject(new AzureCustomVoice("myCustomVoice", "myEndpointId")));

// Azure Personal Voice - requires speaker profile ID and model
// Models: DRAGON_LATEST_NEURAL, PHOENIX_LATEST_NEURAL, PHOENIX_V2NEURAL
VoiceLiveSessionOptions options3 = new VoiceLiveSessionOptions()
    .setVoice(BinaryData.fromObject(
        new AzurePersonalVoice("speakerProfileId", PersonalVoiceModels.PHOENIX_LATEST_NEURAL)));
```

### Function calling

Enable your voice assistant to call custom functions during conversations. This allows the AI to request information or perform actions by executing your code:

```java com.azure.ai.voicelive.functioncalling
// 1. Define function tool with parameters
VoiceLiveFunctionDefinition getWeatherFunction = new VoiceLiveFunctionDefinition("get_current_weather")
    .setDescription("Get the current weather in a given location")
    .setParameters(BinaryData.fromObject(parametersSchema)); // JSON schema

// 2. Configure session with tools
VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
    .setTools(Arrays.asList(getWeatherFunction))
    .setInstructions("You have access to weather information. Use get_current_weather when asked about weather.");

// 3. Handle function call events
client.startSession("gpt-realtime")
    .flatMap(session -> {
        return session.receiveEvents()
            .doOnNext(event -> {
                if (event instanceof SessionUpdateConversationItemCreated) {
                    SessionUpdateConversationItemCreated itemCreated = (SessionUpdateConversationItemCreated) event;
                    if (itemCreated.getItem().getType() == ItemType.FUNCTION_CALL) {
                        ResponseFunctionCallItem functionCall = (ResponseFunctionCallItem) itemCreated.getItem();

                        // Wait for arguments
                        String callId = functionCall.getCallId();
                        String arguments = waitForArguments(session, callId); // Helper method

                        // Execute function
                        try {
                            Map<String, Object> result = getCurrentWeather(arguments);
                            String resultJson = new ObjectMapper().writeValueAsString(result);

                            // Return result
                            FunctionCallOutputItem output = new FunctionCallOutputItem(callId, resultJson);
                            ClientEventConversationItemCreate createItem = new ClientEventConversationItemCreate()
                                .setItem(output)
                                .setPreviousItemId(functionCall.getId());

                            // Chain the two sends sequentially.
                            session.sendEvent(createItem)
                                .then(session.sendEvent(new ClientEventResponseCreate()))
                                .subscribe();
                        } catch (Exception e) {
                            System.err.println("Error executing function: " + e.getMessage());
                        }
                    }
                }
            })
            .then();
    })
    .block();
```

**Key points:**
* Define function tools with JSON schemas describing parameters
* The AI decides when to call functions based on conversation context
* Your code executes the function and returns results
* Results are sent back to continue the conversation
* See `FunctionCallingSample.java` for a complete working example

### MCP tool integration

Use [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) servers to give the AI access to external tools during a voice session. The service calls the MCP server directly — your code only needs to handle approval requests when required:

```java com.azure.ai.voicelive.mcp
// Configure MCP servers as tools
MCPServer mcpServer = new MCPServer("deepwiki", "https://mcp.deepwiki.com/mcp")
    .setRequireApproval(BinaryData.fromObject(MCPApprovalType.ALWAYS));

VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
    .setTools(Arrays.asList(mcpServer))
    .setInstructions("You have access to external tools via MCP. Use them when asked.");

// Handle MCP approval requests in your event loop
session.receiveEvents()
    .doOnNext(event -> {
        if (event instanceof SessionUpdateResponseOutputItemDone) {
            SessionUpdateResponseOutputItemDone itemDone = (SessionUpdateResponseOutputItemDone) event;
            SessionResponseItem item = itemDone.getItem();

            if (item instanceof ResponseMCPApprovalRequestItem) {
                // Approve the tool call
                ResponseMCPApprovalRequestItem approvalRequest = (ResponseMCPApprovalRequestItem) item;
                MCPApprovalResponseRequestItem approval = new MCPApprovalResponseRequestItem(
                    approvalRequest.getId(), true);
                ClientEventConversationItemCreate createItem = new ClientEventConversationItemCreate()
                    .setItem(approval);
                // Chain the two sends sequentially.
                session.sendEvent(createItem)
                    .then(session.sendEvent(new ClientEventResponseCreate()))
                    .subscribe();
            }
        }
    })
    .subscribe();
```

> See `MCPSample.java` for a complete working example with MCP server configuration.

### Azure AI Foundry agent session

Connect directly to an Azure AI Foundry agent using `AgentSessionConfig`. The agent becomes the primary responder for the voice session:

```java com.azure.ai.voicelive.agentsession
// Configure agent connection
AgentSessionConfig agentConfig = new AgentSessionConfig("my-agent", "my-project")
    .setAgentVersion("1.0");

// Start session with agent config (uses DefaultAzureCredential)
VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();

client.startSession(agentConfig)
    .flatMap(session -> {
        return session.receiveEvents()
            .doOnNext(event -> handleEvent(event))
            .then();
    })
    .block();
```

> See `AgentV2Sample.java` for a full implementation with audio capture, playback, and conversation logging.

### Telemetry and tracing

The SDK has built-in [OpenTelemetry](https://opentelemetry.io/) tracing that emits spans for every WebSocket operation. When no OpenTelemetry SDK is present, all tracing calls are automatically no-op with zero performance impact.

#### Automatic tracing (recommended)

The SDK defaults to `GlobalOpenTelemetry.getOrNoop()` —
tracing is automatically active when a global OpenTelemetry instance exists (e.g., via the
[OpenTelemetry Java agent](https://opentelemetry.io/docs/languages/java/automatic/) or
`OpenTelemetrySdk.builder().buildAndRegisterGlobal()`), and is a zero-cost no-op otherwise:

```java com.azure.ai.voicelive.tracing.automatic
// No special configuration needed — tracing is picked up from GlobalOpenTelemetry
VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(apiKey))
    .buildAsyncClient();
```

#### Content recording

By default, message payloads are not recorded in spans for privacy. Enable content recording via environment variable:

```bash
# Enable content recording (no code changes needed):
OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT=true
# (legacy fallback) AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED=true
```

#### Span structure

When tracing is active, the following span hierarchy is emitted for each voice session:

```
connect gpt-4o-realtime-preview        ← session lifetime span
├── send session.update                 ← one span per sent event
├── send input_audio_buffer.append
├── send response.create
├── recv session.created                ← one span per received event
├── recv session.updated
├── recv response.audio.delta
├── recv response.done                  ← includes token usage attributes
├── recv rate_limits.updated            ← rate limit info
└── close
```

**Common attributes** (on all spans): `gen_ai.system`, `gen_ai.operation.name`, `gen_ai.provider.name`, `gen_ai.request.model`, `az.namespace`, `server.address`, `server.port`

**Session-level attributes** (on the connect span, flushed at session close):
- `gen_ai.voice.session_id` — Voice session ID
- `gen_ai.voice.input_audio_format` / `gen_ai.voice.output_audio_format` — Audio formats (e.g., `pcm16`)
- `gen_ai.voice.input_sample_rate` — Input audio sampling rate (Hz)
- `gen_ai.voice.turn_count` — Completed response turns
- `gen_ai.voice.interruption_count` — User interruptions
- `gen_ai.voice.audio_bytes_sent` / `gen_ai.voice.audio_bytes_received` — Audio payload bytes
- `gen_ai.voice.first_token_latency_ms` — Time to first audio response
- `gen_ai.conversation.id` — Conversation ID
- `gen_ai.response.id` / `gen_ai.response.finish_reasons` — Last response metadata
- `gen_ai.system_instructions` / `gen_ai.request.temperature` / `gen_ai.request.max_output_tokens` / `gen_ai.request.tools` — Session config from `session.update`
- `gen_ai.agent.name` / `gen_ai.agent.id` / `gen_ai.agent.version` / `gen_ai.agent.project_name` / `gen_ai.agent.thread_id` — Agent metadata (when using `AgentSessionConfig`)

> See `telemetry/GlobalTracingSample.java` for a complete tracing example.
>
> **Run the telemetry sample** to see tracing in action:
> ```bash
> mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.telemetry.GlobalTracingSample" -Dexec.classpathScope=test
> ```
>
> Sample output:
> ```
> 'send session.update' : {gen_ai.operation.name=send, gen_ai.voice.event_type=session.update, ...}
> 'recv session.created' : {gen_ai.operation.name=recv, gen_ai.voice.event_type=session.created, ...}
> 'recv response.done'   : {gen_ai.usage.input_tokens=100, gen_ai.usage.output_tokens=50, ...}
> 'close'                : {gen_ai.operation.name=close, ...}
> 'connect gpt-realtime' : {gen_ai.voice.session_id=..., gen_ai.voice.turn_count=1, ...}
> ```

### Complete voice assistant with microphone

A full example demonstrating real-time microphone input and audio playback:

<!-- BEGIN: com.azure.ai.voicelive.readme -->
```java
String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
String apiKey = System.getenv("AZURE_VOICELIVE_API_KEY");

// Create the VoiceLive client
VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(apiKey))
    .buildAsyncClient();

// Configure session options for voice conversation
ServerVadTurnDetection turnDetection = new ServerVadTurnDetection()
    .setThreshold(0.5)
    .setPrefixPaddingMs(300)
    .setSilenceDurationMs(500)
    .setInterruptResponse(true)
    .setAutoTruncate(true)
    .setCreateResponse(true);

AudioInputTranscriptionOptions transcriptionOptions = new AudioInputTranscriptionOptions(
    AudioInputTranscriptionOptionsModel.WHISPER_1);

VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
    .setInstructions("You are a helpful AI voice assistant.")
    .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
    .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
    .setInputAudioFormat(InputAudioFormat.PCM16)
    .setOutputAudioFormat(OutputAudioFormat.PCM16)
    .setInputAudioSamplingRate(24000)
    .setInputAudioNoiseReduction(new AudioNoiseReduction(AudioNoiseReductionType.NEAR_FIELD))
    .setInputAudioEchoCancellation(new AudioEchoCancellation())
    .setInputAudioTranscription(transcriptionOptions)
    .setTurnDetection(turnDetection);

// Start session and handle events
client.startSession("gpt-4o-realtime-preview")
    .flatMap(session -> {
        // Subscribe to receive server events
        session.receiveEvents()
            .subscribe(
                event -> handleEvent(event),
                error -> System.err.println("Error: " + error.getMessage())
            );

        // Send session configuration
        ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
        return session.sendEvent(updateEvent).then(Mono.just(session));
    })
    .block();
```
<!-- END: com.azure.ai.voicelive.readme -->

For complete, runnable implementations, see the [Focused Sample Files](#focused-sample-files) section above.

## Troubleshooting

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can be found here: [log levels][log_levels].

### Common issues

#### Audio system not available

Ensure your system has a working microphone and speakers/headphones. The VoiceLive service requires:
- **Microphone**: For capturing audio input (24kHz, 16-bit PCM, mono)
- **Speakers**: For playing audio responses (24kHz, 16-bit PCM, mono)

#### WebSocket connection failures

If you encounter connection issues:
- Verify your endpoint URL is correct
- Check that your API key or token credential is valid
- Ensure your network allows WebSocket connections
- Confirm your Azure VoiceLive resource is properly provisioned

#### Authentication errors

For API key authentication:
- Verify the `AZURE_VOICELIVE_API_KEY` environment variable is set correctly
- Ensure the API key matches your Azure VoiceLive resource

For token credential authentication:
- Run `az login` before using Azure CLI credentials
- Verify the credential has appropriate permissions for the VoiceLive resource
- Check that the Azure Identity library is properly configured

### Default HTTP Client

All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. For more information on HTTP client configuration, see the [HTTP clients wiki][http_clients_wiki].

### Default SSL library

All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

### Sample files

All sample files are located in the `src/samples/java/com/azure/ai/voicelive/` directory. See the [Focused Sample Files](#focused-sample-files) section for detailed descriptions and running instructions.

### Additional documentation

- [Azure VoiceLive product documentation][product_documentation]
- [Azure SDK for Java][azure_sdk_java]

## Contributing

For details on contributing to this repository, see the [contributing guide][contributing].

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
