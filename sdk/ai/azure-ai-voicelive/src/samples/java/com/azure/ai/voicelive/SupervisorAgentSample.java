// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.AudioEchoCancellation;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.AzureStandardVoice;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.FoundryAgentTool;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ItemType;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ResponseFoundryAgentCallItem;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerEventResponseFoundryAgentCallArgumentsDone;
import com.azure.ai.voicelive.models.ServerEventResponseFoundryAgentCallCompleted;
import com.azure.ai.voicelive.models.ServerEventResponseFoundryAgentCallFailed;
import com.azure.ai.voicelive.models.ServerEventResponseFoundryAgentCallInProgress;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemCreated;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseOutputItemDone;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.ai.voicelive.models.VoiceLiveToolDefinition;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Supervisor Agent sample demonstrating how to use Azure Foundry Agent tools with VoiceLive.
 *
 * <p>The Supervisor pattern is a common multi-agent architecture where a central AI agent acts
 * as a coordinator, managing and delegating tasks to specialized supervisor agents.</p>
 *
 * <p>This sample demonstrates:</p>
 * <ul>
 *   <li>Configuring VoiceLive with Foundry Agent tools</li>
 *   <li>Using a supervisor agent powered by Azure Foundry</li>
 *   <li>Handling Foundry Agent call lifecycle events</li>
 *   <li>Real-time microphone audio capture and playback</li>
 *   <li>Voice Activity Detection (VAD) with interruption handling</li>
 * </ul>
 *
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The VoiceLive service endpoint URL</li>
 *   <li>SUPERVISOR_AGENT_NAME - The name of the supervisor agent in Azure Foundry</li>
 *   <li>SUPERVISOR_AGENT_PROJECT_NAME - The name of the Foundry project containing the agent</li>
 * </ul>
 *
 * <p><strong>Optional Environment Variables:</strong></p>
 * <ul>
 *   <li>SUPERVISOR_AGENT_VERSION - The version of the supervisor agent (optional)</li>
 *   <li>CHAT_AGENT_MODEL - The model to use (default: gpt-4o-realtime-preview)</li>
 *   <li>CHAT_AGENT_VOICE - The voice to use (default: alloy)</li>
 * </ul>
 *
 * <p><strong>Authentication:</strong></p>
 * <p>This sample uses Azure Active Directory (AAD) authentication via DefaultAzureCredential.
 * Make sure you are logged in via Azure CLI ({@code az login}) or have appropriate
 * environment variables set for service principal authentication.</p>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.SupervisorAgentSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class SupervisorAgentSample {

    // Environment variable names
    private static final String ENV_ENDPOINT = "AZURE_VOICELIVE_ENDPOINT";
    private static final String ENV_SUPERVISOR_AGENT_NAME = "SUPERVISOR_AGENT_NAME";
    private static final String ENV_SUPERVISOR_AGENT_VERSION = "SUPERVISOR_AGENT_VERSION";
    private static final String ENV_SUPERVISOR_AGENT_PROJECT_NAME = "SUPERVISOR_AGENT_PROJECT_NAME";
    private static final String ENV_CHAT_AGENT_MODEL = "CHAT_AGENT_MODEL";
    private static final String ENV_CHAT_AGENT_VOICE = "CHAT_AGENT_VOICE";

    // Audio format constants (VoiceLive requirements)
    private static final int SAMPLE_RATE = 24000;
    private static final int CHANNELS = 1;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHUNK_SIZE = 1200;

    // Private constructor to prevent instantiation
    private SupervisorAgentSample() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Main entry point for the supervisor agent sample.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Load configuration from environment variables
        String endpoint = getRequiredEnv(ENV_ENDPOINT);
        String supervisorAgentName = getRequiredEnv(ENV_SUPERVISOR_AGENT_NAME);
        String supervisorAgentProjectName = getRequiredEnv(ENV_SUPERVISOR_AGENT_PROJECT_NAME);

        String supervisorAgentVersion = System.getenv(ENV_SUPERVISOR_AGENT_VERSION);
        String chatAgentModel = getEnvOrDefault(ENV_CHAT_AGENT_MODEL, "gpt-realtime");
        String chatAgentVoice = getEnvOrDefault(ENV_CHAT_AGENT_VOICE, "en-US-AvaMultilingualNeural");

        String separator = new String(new char[70]).replace("\0", "=");
        System.out.println(separator);
        System.out.println("Voice Assistant with Supervisor Agent - Azure VoiceLive SDK");
        System.out.println(separator);
        System.out.println("Supervisor Agent: " + supervisorAgentName);
        System.out.println("Project: " + supervisorAgentProjectName);
        if (supervisorAgentVersion != null) {
            System.out.println("Version: " + supervisorAgentVersion);
        }
        System.out.println("Model: " + chatAgentModel);
        System.out.println("Voice: " + chatAgentVoice);
        System.out.println(separator);

        // Create client with AAD authentication using DefaultAzureCredential
        System.out.println("Using Azure Active Directory (AAD) authentication...");
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .serviceVersion(VoiceLiveServiceVersion.V2026_01_01_PREVIEW)
            .buildAsyncClient();

        try {
            runSupervisorAgentSession(
                client,
                chatAgentModel,
                chatAgentVoice,
                supervisorAgentName,
                supervisorAgentProjectName,
                supervisorAgentVersion
            );
        } catch (Exception e) {
            System.err.println("[ERROR] Error running supervisor agent sample: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            System.exit(1);
        }
    }

    /**
     * Get a required environment variable or exit with an error message.
     */
    private static String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            System.err.println("[ERROR] No " + name + " provided");
            System.err.println("Please set the " + name + " environment variable.");
            System.exit(1);
        }
        return value;
    }

    /**
     * Get an environment variable with a default value.
     */
    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Runs the supervisor agent session with audio input/output.
     */
    private static void runSupervisorAgentSession(
        VoiceLiveAsyncClient client,
        String model,
        String voice,
        String supervisorAgentName,
        String supervisorAgentProjectName,
        String supervisorAgentVersion
    ) throws Exception {

        System.out.println("Connecting to VoiceLive service...");

        AtomicBoolean running = new AtomicBoolean(true);

        // Start session
        client.startSession(model)
            .flatMap(session -> {
                System.out.println("[OK] Session started successfully");

                // Create audio processor
                AudioProcessor audioProcessor = new AudioProcessor(session);

                // Subscribe to receive server events
                session.receiveEvents()
                    .subscribe(
                        event -> handleServerEvent(event, audioProcessor),
                        error -> {
                            System.err.println("[ERROR] Error processing events: " + error.getMessage());
                            running.set(false);
                        },
                        () -> System.out.println("[OK] Event stream completed")
                    );

                // Create session configuration with supervisor agent
                ClientEventSessionUpdate sessionConfig = createSessionConfigWithSupervisorAgent(
                    voice,
                    supervisorAgentName,
                    supervisorAgentProjectName,
                    supervisorAgentVersion
                );

                // Send session configuration
                System.out.println("Sending session configuration with Foundry Agent tool...");
                session.sendEvent(sessionConfig)
                    .doOnSuccess(v -> System.out.println("[OK] Session configured with supervisor agent"))
                    .doOnError(error -> System.err.println("[ERROR] Failed to send session.update: " + error.getMessage()))
                    .subscribe();

                // Start audio playback
                audioProcessor.startPlayback();

                String separator = new String(new char[70]).replace("\0", "=");
                System.out.println("\n" + separator);
                System.out.println("VOICE ASSISTANT WITH SUPERVISOR AGENT READY");
                System.out.println("Try saying:");
                System.out.println("  - 'What products do you have?'");
                System.out.println("  - 'What colors are available?'");
                System.out.println("  - For greetings like 'hello', the assistant responds directly");
                System.out.println("  - For other questions, the supervisor agent is invoked");
                System.out.println("Press Ctrl+C to exit");
                System.out.println(separator + "\n");

                // Add shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("\nShutting down supervisor agent...");
                    running.set(false);
                    audioProcessor.cleanup();
                }));

                // Keep the reactive chain alive
                return Mono.never();
            })
            .doOnError(error -> System.err.println("[ERROR] Error: " + error.getMessage()))
            .block();
    }

    /**
     * Create session configuration with supervisor agent.
     *
     * @param voiceName the voice name to use for the assistant
     * @param supervisorAgentName the name of the supervisor agent
     * @param supervisorAgentProjectName the Foundry project name
     * @param supervisorAgentVersion the agent version (optional, can be null)
     * @return the session update configuration
     */
    private static ClientEventSessionUpdate createSessionConfigWithSupervisorAgent(
        String voiceName,
        String supervisorAgentName,
        String supervisorAgentProjectName,
        String supervisorAgentVersion
    ) {
        // Create supervisor agent description
        String supervisorAgentDescription = "You are a supervisor agent that determines the next response "
            + "whenever the agent faces a non-trivial decision";

        // Create chat agent instructions
        String chatAgentInstructions = String.format(
            "You are a helpful agent. Your task is to maintain a natural conversation flow with the user. "
            + "By default, you must always use the %s tool to get your next response, except when handling "
            + "greetings (e.g., 'hello', 'hi there') or engaging in basic chitchat (e.g., 'how are you?', "
            + "'thank you'). "
            + "Before calling %s, you MUST ALWAYS say something to the user (e.g., 'Let me look into that.', "
            + "'Just a second.'). Never call %s without first saying something to the user.",
            supervisorAgentName, supervisorAgentName, supervisorAgentName);

        // Create Foundry agent tool
        FoundryAgentTool foundryAgentTool = new FoundryAgentTool(supervisorAgentName, supervisorAgentProjectName)
            .setDescription(supervisorAgentDescription);

        // Set agent version if provided
        if (supervisorAgentVersion != null && !supervisorAgentVersion.isEmpty()) {
            foundryAgentTool.setAgentVersion(supervisorAgentVersion);
        }

        List<VoiceLiveToolDefinition> foundryAgentTools = Arrays.asList(foundryAgentTool);

        // Create session options with Azure Standard Voice (TTS)
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions(chatAgentInstructions)
            .setVoice(BinaryData.fromObject(new AzureStandardVoice(voiceName)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE)
            .setInputAudioEchoCancellation(new AudioEchoCancellation())
            .setTurnDetection(new ServerVadTurnDetection()
                .setThreshold(0.5)
                .setPrefixPaddingMs(300)
                .setSilenceDurationMs(500)
                .setInterruptResponse(true)
                .setAutoTruncate(true)
                .setCreateResponse(true))
            .setTools(foundryAgentTools)
            .setToolChoice(BinaryData.fromString("auto"))  // Let the model decide when to call the agent
            .setInputAudioTranscription(
                new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1)
            );

        return new ClientEventSessionUpdate(sessionOptions);
    }

    /**
     * Handle server events from VoiceLive.
     */
    private static void handleServerEvent(SessionUpdate event, AudioProcessor audioProcessor) {
        ServerEventType eventType = event.getType();

        // Session updated - session is ready
        if (event instanceof SessionUpdateSessionUpdated) {
            SessionUpdateSessionUpdated sessionUpdated = (SessionUpdateSessionUpdated) event;
            System.out.println("[OK] Session ready: " + sessionUpdated.getSession().getId());
            audioProcessor.startCapture();
            System.out.println("[MIC] Start speaking...");

        // User started speaking - stop playback (interruption handling)
        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
            System.out.println("[MIC] Listening...");
            audioProcessor.skipPendingAudio();

        // User stopped speaking - processing
        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
            System.out.println("[...] Processing...");

        // Audio response delta - play audio
        } else if (event instanceof SessionUpdateResponseAudioDelta) {
            SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
            byte[] audioData = audioDelta.getDelta();
            if (audioData != null && audioData.length > 0) {
                audioProcessor.queueAudio(audioData);
            }

        // Response audio done - assistant finished speaking
        } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
            System.out.println("[BOT] Assistant finished speaking");
            System.out.println("[MIC] Ready for next input...");

        // Conversation item created - check for Foundry agent call
        } else if (event instanceof SessionUpdateConversationItemCreated) {
            SessionUpdateConversationItemCreated itemCreated = (SessionUpdateConversationItemCreated) event;
            if (itemCreated.getItem() != null
                && itemCreated.getItem().getType() == ItemType.FOUNDRY_AGENT_CALL) {
                ResponseFoundryAgentCallItem agentCallItem = (ResponseFoundryAgentCallItem) itemCreated.getItem();
                System.out.println("[TOOL] Foundry Agent Call initiated: " + agentCallItem.getName());
            }

        // Foundry agent call arguments done
        } else if (event instanceof ServerEventResponseFoundryAgentCallArgumentsDone) {
            ServerEventResponseFoundryAgentCallArgumentsDone argsDone
                = (ServerEventResponseFoundryAgentCallArgumentsDone) event;
            System.out.println("[TOOL] Foundry Agent Call arguments: " + argsDone.getArguments());

        // Foundry agent call in progress
        } else if (event instanceof ServerEventResponseFoundryAgentCallInProgress) {
            ServerEventResponseFoundryAgentCallInProgress inProgress
                = (ServerEventResponseFoundryAgentCallInProgress) event;
            if (inProgress.getAgentResponseId() != null) {
                System.out.println("[TOOL] Foundry Agent Call in progress with response ID: "
                    + inProgress.getAgentResponseId());
            }

        // Foundry agent call completed
        } else if (event instanceof ServerEventResponseFoundryAgentCallCompleted) {
            System.out.println("[OK] Foundry Agent Call completed");

        // Foundry agent call failed
        } else if (event instanceof ServerEventResponseFoundryAgentCallFailed) {
            System.err.println("[ERROR] Foundry Agent Call failed");

        // Response output item done - check for agent call output
        } else if (event instanceof SessionUpdateResponseOutputItemDone) {
            SessionUpdateResponseOutputItemDone outputDone = (SessionUpdateResponseOutputItemDone) event;
            if (outputDone.getItem() instanceof ResponseFoundryAgentCallItem) {
                ResponseFoundryAgentCallItem agentCallItem
                    = (ResponseFoundryAgentCallItem) outputDone.getItem();
                System.out.println("[TOOL] Foundry Agent Call output: " + agentCallItem.getOutput());
            }

        // Error event
        } else if (event instanceof SessionUpdateError) {
            SessionUpdateError errorEvent = (SessionUpdateError) event;
            System.err.println("[ERROR] VoiceLive error: " + errorEvent.getError().getCode()
                + " - " + errorEvent.getError().getMessage());
        }
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

        /**
         * Start capturing audio from microphone.
         */
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
                Thread captureThread = new Thread(() -> {
                    byte[] buffer = new byte[CHUNK_SIZE];
                    while (isCapturing.get()) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            byte[] audioData = Arrays.copyOf(buffer, bytesRead);
                            session.sendInputAudio(BinaryData.fromBytes(audioData)).subscribe();
                        }
                    }
                }, "AudioCapture");
                captureThread.setDaemon(true);
                captureThread.start();

                System.out.println("[OK] Audio capture started");
            } catch (LineUnavailableException e) {
                System.err.println("[ERROR] Failed to start audio capture: " + e.getMessage());
                isCapturing.set(false);
            }
        }

        /**
         * Start audio playback system.
         */
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
                Thread playbackThread = new Thread(() -> {
                    while (isPlaying.get()) {
                        try {
                            byte[] audioData = playbackQueue.poll(10, java.util.concurrent.TimeUnit.MILLISECONDS);
                            if (audioData != null) {
                                speaker.write(audioData, 0, audioData.length);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }, "AudioPlayback");
                playbackThread.setDaemon(true);
                playbackThread.start();

                System.out.println("[OK] Audio playback started");
            } catch (LineUnavailableException e) {
                System.err.println("[ERROR] Failed to start audio playback: " + e.getMessage());
                isPlaying.set(false);
            }
        }

        /**
         * Skip pending audio (for interruption handling).
         */
        void skipPendingAudio() {
            playbackQueue.clear();
        }

        /**
         * Queue audio data for playback.
         */
        void queueAudio(byte[] audioData) {
            if (isPlaying.get()) {
                playbackQueue.offer(audioData);
            }
        }

        /**
         * Clean up audio resources.
         */
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
            System.out.println("[OK] Audio processor cleaned up");
        }
    }
}
