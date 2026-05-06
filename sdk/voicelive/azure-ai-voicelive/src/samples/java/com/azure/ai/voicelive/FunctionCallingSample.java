// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.AudioEchoCancellation;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.AudioNoiseReduction;
import com.azure.ai.voicelive.models.AudioNoiseReductionType;
import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.FunctionCallOutputItem;
import com.azure.ai.voicelive.models.VoiceLiveFunctionDefinition;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ItemType;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ResponseFunctionCallItem;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemCreated;
import com.azure.ai.voicelive.models.SessionUpdateResponseFunctionCallArgumentsDone;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.ai.voicelive.models.VoiceLiveToolDefinition;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Function calling sample demonstrating how to use VoiceLive with custom function tools.
 *
 * <p>This sample shows how to:</p>
 * <ul>
 *   <li>Define function tools with parameters</li>
 *   <li>Register functions with the VoiceLive session</li>
 *   <li>Handle function call requests from the AI model</li>
 *   <li>Return function results back to the conversation</li>
 *   <li>Process the AI's response after function execution</li>
 * </ul>
 *
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The VoiceLive service endpoint URL</li>
 *   <li>AZURE_VOICELIVE_API_KEY - The API key for authentication</li>
 * </ul>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.FunctionCallingSample" -Dexec.classpathScope=test
 * }</pre>
 *
 * <p><strong>Try asking:</strong></p>
 * <ul>
 *   <li>"What's the current time?"</li>
 *   <li>"What's the weather in Seattle?"</li>
 *   <li>"What time is it in UTC?"</li>
 * </ul>
 */
public final class FunctionCallingSample {

    // Service configuration
    private static final String DEFAULT_MODEL = "gpt-realtime";
    private static final String ENV_ENDPOINT = "AZURE_VOICELIVE_ENDPOINT";
    private static final String ENV_API_KEY = "AZURE_VOICELIVE_API_KEY";

    // Audio format constants
    private static final int SAMPLE_RATE = 24000;
    private static final int CHANNELS = 1;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHUNK_SIZE = 1200;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Private constructor to prevent instantiation
    private FunctionCallingSample() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Main entry point for the function calling sample.
     */
    public static void main(String[] args) {
        // Load configuration
        String endpoint = System.getenv(ENV_ENDPOINT);
        String apiKey = System.getenv(ENV_API_KEY);

        if (endpoint == null || endpoint.isEmpty()) {
            System.err.println("Error: AZURE_VOICELIVE_ENDPOINT environment variable is not set.");
            System.exit(1);
        }

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: AZURE_VOICELIVE_API_KEY environment variable is not set.");
            System.exit(1);
        }

        String separator = new String(new char[70]).replace("\0", "=");
        System.out.println(separator);
        System.out.println("🎤️ Voice Assistant with Function Calling - Azure VoiceLive SDK");
        System.out.println(separator);

        try {
            // Create client
            KeyCredential credential = new KeyCredential(apiKey);
            VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

            runFunctionCallingSession(client);
        } catch (Exception e) {
            System.err.println("Error running function calling sample: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Runs the function calling session with audio input/output.
     */
    private static void runFunctionCallingSession(VoiceLiveAsyncClient client) throws Exception {
        System.out.println("Connecting to VoiceLive service...");

        AtomicReference<AudioProcessor> audioProcessorRef = new AtomicReference<>();
        AtomicReference<VoiceLiveSessionAsyncClient> sessionRef = new AtomicReference<>();
        AtomicBoolean running = new AtomicBoolean(true);

        // Track pending function calls: callId -> (functionName, previousItemId)
        Map<String, String[]> pendingFunctionCalls = new ConcurrentHashMap<>();

        // Start session
        client.startSession(DEFAULT_MODEL)
            .flatMap(session -> {
                sessionRef.set(session);
                System.out.println("✓ Session started successfully");

                // Create audio processor
                AudioProcessor audioProcessor = new AudioProcessor(session);
                audioProcessorRef.set(audioProcessor);

                // Send session configuration with function tools, then listen for events.
                System.out.println("📤 Sending session configuration with function tools...");
                ClientEventSessionUpdate sessionConfig = createSessionConfigWithFunctions();
                return session.sendEvent(sessionConfig)
                    .doOnSuccess(v -> {
                        System.out.println("✓ Session configured with function tools");

                        // Start audio playback
                        audioProcessor.startPlayback();

                        String separator = new String(new char[70]).replace("\0", "=");
                        System.out.println("\n" + separator);
                        System.out.println("🎤 VOICE ASSISTANT WITH FUNCTION CALLING READY");
                        System.out.println("Try saying:");
                        System.out.println("  • 'What's the current time?'");
                        System.out.println("  • 'What's the weather in Seattle?'");
                        System.out.println("  • 'What time is it in UTC?'");
                        System.out.println("Press Ctrl+C to exit");
                        System.out.println(separator + "\n");

                        // Add shutdown hook
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                            System.out.println("\n👋 Shutting down voice assistant...");
                            running.set(false);
                            audioProcessor.cleanup();
                            try {
                                session.closeAsync().block(Duration.ofSeconds(5));
                            } catch (Exception e) {
                                // Suppress errors during forced JVM shutdown
                            }
                        }));
                    })
                    .doOnError(error -> System.err.println("❌ Failed to send session.update: " + error.getMessage()))
                    .thenMany(session.receiveEvents()
                        .doOnNext(event -> handleServerEvent(session, event, audioProcessor, pendingFunctionCalls))
                        .doOnError(error -> {
                            System.err.println("Error processing events: " + error.getMessage());
                            running.set(false);
                        })
                        .doOnComplete(() -> System.out.println("✓ Event stream completed")))
                    .then(); // receiveEvents() never completes, so this keeps session alive
            })
            .doOnError(error -> System.err.println("❌ Error: " + error.getMessage()))
            .doFinally(signalType -> {
                AudioProcessor audioProcessor = audioProcessorRef.get();
                if (audioProcessor != null) {
                    audioProcessor.cleanup();
                }
                VoiceLiveSessionAsyncClient s = sessionRef.get();
                if (s != null) {
                    try {
                        s.close();
                    } catch (Exception e) {
                        // Suppress errors during cleanup
                    }
                }
            })
            .block();
    }

    /**
     * Create session configuration with function tools.
     */
    private static ClientEventSessionUpdate createSessionConfigWithFunctions() {
        // Define function tools
        List<VoiceLiveToolDefinition> functionTools = Arrays.asList(
            createGetCurrentTimeFunction(),
            createGetCurrentWeatherFunction()
        );

        // Create session options
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions(
                "You are a helpful voice assistant with access to function tools. "
                + "When asked about the time, use the get_current_time function. "
                + "When asked about weather, use the get_current_weather function. "
                + "Acknowledge when you're calling a function and present the results naturally in your response."
            )
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE)
            .setInputAudioEchoCancellation(new AudioEchoCancellation())
            .setInputAudioNoiseReduction(new AudioNoiseReduction(AudioNoiseReductionType.NEAR_FIELD))
            .setTurnDetection(new ServerVadTurnDetection()
                .setThreshold(0.5)
                .setPrefixPaddingMs(300)
                .setSilenceDurationMs(500)
                .setInterruptResponse(true)
                .setAutoTruncate(true)
                .setCreateResponse(true))
            .setTools(functionTools)
            .setInputAudioTranscription(
                new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1)
            );

        return new ClientEventSessionUpdate(sessionOptions);
    }

    /**
     * Create the get_current_time function tool.
     */
    private static VoiceLiveFunctionDefinition createGetCurrentTimeFunction() {
        ObjectNode parametersSchema = OBJECT_MAPPER.createObjectNode();
        parametersSchema.put("type", "object");

        ObjectNode properties = parametersSchema.putObject("properties");
        ObjectNode timezoneProperty = properties.putObject("timezone");
        timezoneProperty.put("type", "string");
        timezoneProperty.put("description", "The timezone to get the current time for, e.g., 'UTC', 'local'");

        return new VoiceLiveFunctionDefinition("get_current_time")
            .setDescription("Get the current time")
            .setParameters(BinaryData.fromObject(parametersSchema));
    }

    /**
     * Create the get_current_weather function tool.
     */
    private static VoiceLiveFunctionDefinition createGetCurrentWeatherFunction() {
        ObjectNode parametersSchema = OBJECT_MAPPER.createObjectNode();
        parametersSchema.put("type", "object");

        ObjectNode properties = parametersSchema.putObject("properties");

        ObjectNode locationProperty = properties.putObject("location");
        locationProperty.put("type", "string");
        locationProperty.put("description", "The city and state, e.g., 'San Francisco, CA'");

        ObjectNode unitProperty = properties.putObject("unit");
        unitProperty.put("type", "string");
        unitProperty.set("enum", OBJECT_MAPPER.createArrayNode().add("celsius").add("fahrenheit"));
        unitProperty.put("description", "The unit of temperature to use (celsius or fahrenheit)");

        parametersSchema.set("required", OBJECT_MAPPER.createArrayNode().add("location"));

        return new VoiceLiveFunctionDefinition("get_current_weather")
            .setDescription("Get the current weather in a given location")
            .setParameters(BinaryData.fromObject(parametersSchema));
    }

    /**
     * Handle server events in a single subscription.
     */
    private static void handleServerEvent(
        VoiceLiveSessionAsyncClient session,
        SessionUpdate event,
        AudioProcessor audioProcessor,
        Map<String, String[]> pendingFunctionCalls
    ) {
        ServerEventType eventType = event.getType();

        if (event instanceof SessionUpdateSessionUpdated) {
            System.out.println("✅ Session ready");
            audioProcessor.startCapture();
            System.out.println("🎤 Start speaking. Say 'What's the time?' or 'What's the weather in Seattle?'");

        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
            System.out.println("🎤 Listening...");
            audioProcessor.skipPendingAudio();

        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
            System.out.println("🤔 Processing...");

        } else if (event instanceof SessionUpdateResponseAudioDelta) {
            SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
            byte[] audioData = audioDelta.getDelta();
            if (audioData != null && audioData.length > 0) {
                audioProcessor.queueAudio(audioData);
            }

        } else if (event instanceof SessionUpdateConversationItemCreated) {
            // A function call item was created — remember it so we can execute when arguments arrive.
            SessionUpdateConversationItemCreated itemCreated = (SessionUpdateConversationItemCreated) event;
            if (itemCreated.getItem() != null && itemCreated.getItem().getType() == ItemType.FUNCTION_CALL) {
                ResponseFunctionCallItem fc = (ResponseFunctionCallItem) itemCreated.getItem();
                System.out.println("🔧 Function call started: " + fc.getName());
                pendingFunctionCalls.put(fc.getCallId(), new String[]{fc.getName(), fc.getId()});
            }

        } else if (event instanceof SessionUpdateResponseFunctionCallArgumentsDone) {
            // Arguments are complete — execute the function and send the result.
            SessionUpdateResponseFunctionCallArgumentsDone argsDone
                = (SessionUpdateResponseFunctionCallArgumentsDone) event;
            String callId = argsDone.getCallId();
            String[] meta = pendingFunctionCalls.remove(callId);
            if (meta == null) {
                return;
            }
            String functionName = meta[0];
            String previousItemId = meta[1];
            String arguments = argsDone.getArguments();

            System.out.println("📋 Function arguments: " + arguments);
            Map<String, Object> result = executeFunction(functionName, arguments);
            System.out.println("✅ Function result: " + result);

            String resultJson;
            try {
                resultJson = OBJECT_MAPPER.writeValueAsString(result);
            } catch (Exception e) {
                resultJson = "{\"error\": \"Failed to serialize result\"}";
            }

            FunctionCallOutputItem output = new FunctionCallOutputItem(callId, resultJson);
            ClientEventConversationItemCreate createItem = new ClientEventConversationItemCreate()
                .setItem(output)
                .setPreviousItemId(previousItemId);

            session.sendEvent(createItem)
                .then(session.sendEvent(new ClientEventResponseCreate()))
                .doOnSuccess(v -> System.out.println("🤖 Function result sent"))
                .subscribe();
        }
    }

    /**
     * Execute the requested function.
     */
    private static Map<String, Object> executeFunction(String functionName, String argumentsJson) {
        try {
            JsonNode arguments = OBJECT_MAPPER.readTree(argumentsJson);

            switch (functionName) {
                case "get_current_time":
                    return getCurrentTime(arguments);
                case "get_current_weather":
                    return getCurrentWeather(arguments);
                default:
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Unknown function: " + functionName);
                    return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to parse arguments: " + e.getMessage());
            return error;
        }
    }

    /**
     * Get current time function implementation.
     */
    private static Map<String, Object> getCurrentTime(JsonNode arguments) {
        String timezone = arguments.has("timezone") ? arguments.get("timezone").asText() : "local";

        LocalDateTime now;
        String timezoneName;

        if ("UTC".equalsIgnoreCase(timezone)) {
            now = LocalDateTime.now(ZoneId.of("UTC"));
            timezoneName = "UTC";
        } else {
            now = LocalDateTime.now();
            timezoneName = "local";
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

        Map<String, Object> result = new HashMap<>();
        result.put("time", now.format(timeFormatter));
        result.put("date", now.format(dateFormatter));
        result.put("timezone", timezoneName);

        return result;
    }

    /**
     * Get current weather function implementation.
     */
    private static Map<String, Object> getCurrentWeather(JsonNode arguments) {
        if (!arguments.has("location")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Location parameter is required");
            return error;
        }

        String location = arguments.get("location").asText();
        String unit = arguments.has("unit") ? arguments.get("unit").asText() : "celsius";

        // Simulated weather data
        Map<String, Object> result = new HashMap<>();
        result.put("location", location);
        result.put("temperature", unit.equals("celsius") ? 22 : 72);
        result.put("unit", unit);
        result.put("forecast", "Partly cloudy");
        result.put("humidity", 65);
        result.put("wind_speed", 15);

        return result;
    }

    /**
     * Manages audio capture and playback.
     */
    private static class AudioProcessor {
        private final VoiceLiveSessionAsyncClient session;
        private final AudioFormat audioFormat;

        private TargetDataLine microphone;
        private final AtomicBoolean isCapturing = new AtomicBoolean(false);

        private SourceDataLine speaker;
        private final BlockingQueue<byte[]> playbackQueue = new LinkedBlockingQueue<>();
        private final AtomicBoolean isPlaying = new AtomicBoolean(false);

        AudioProcessor(VoiceLiveSessionAsyncClient session) {
            this.session = session;
            this.audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
        }

        void startCapture() {
            if (isCapturing.get()) {
                return;
            }

            try {
                DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
                microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
                microphone.open(audioFormat, CHUNK_SIZE * 4);
                microphone.start();
                isCapturing.set(true);

                // Start capture thread
                new Thread(() -> {
                    byte[] buffer = new byte[CHUNK_SIZE];
                    while (isCapturing.get()) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            byte[] audioData = Arrays.copyOf(buffer, bytesRead);
                            session.sendInputAudio(BinaryData.fromBytes(audioData)).subscribe();
                        }
                    }
                }, "AudioCapture").start();

                System.out.println("✅ Audio capture started");
            } catch (LineUnavailableException e) {
                System.err.println("Failed to start audio capture: " + e.getMessage());
                isCapturing.set(false);
            }
        }

        void startPlayback() {
            if (isPlaying.get()) {
                return;
            }

            try {
                DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
                speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
                speaker.open(audioFormat, CHUNK_SIZE * 4);
                speaker.start();
                isPlaying.set(true);

                // Start playback thread
                new Thread(() -> {
                    while (isPlaying.get()) {
                        try {
                            byte[] audioData = playbackQueue.poll();
                            if (audioData != null) {
                                speaker.write(audioData, 0, audioData.length);
                            } else {
                                Thread.sleep(10);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }, "AudioPlayback").start();

                System.out.println("✅ Audio playback started");
            } catch (LineUnavailableException e) {
                System.err.println("Failed to start audio playback: " + e.getMessage());
                isPlaying.set(false);
            }
        }

        void skipPendingAudio() {
            playbackQueue.clear();
        }

        void queueAudio(byte[] audioData) {
            if (isPlaying.get()) {
                playbackQueue.offer(audioData);
            }
        }

        void cleanup() {
            isCapturing.set(false);
            isPlaying.set(false);

            if (microphone != null) {
                microphone.stop();
                microphone.close();
            }

            if (speaker != null) {
                speaker.drain();
                speaker.stop();
                speaker.close();
            }

            playbackQueue.clear();
        }
    }
}
