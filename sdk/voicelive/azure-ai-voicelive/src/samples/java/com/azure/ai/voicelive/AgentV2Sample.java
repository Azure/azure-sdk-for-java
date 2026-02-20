// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.AgentSessionConfig;
import com.azure.ai.voicelive.models.AudioEchoCancellation;
import com.azure.ai.voicelive.models.AudioNoiseReduction;
import com.azure.ai.voicelive.models.AudioNoiseReductionType;
import com.azure.ai.voicelive.models.AzureStandardVoice;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemInputAudioTranscriptionCompleted;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioTranscriptDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseTextDone;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.ServerEventWarning;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Agent V2 Sample - Demonstrates connecting to an Azure AI Foundry agent using AgentSessionConfig.
 *
 * <p>This sample demonstrates the new pattern where the agent is configured at connection time
 * using AgentSessionConfig, rather than as a tool in the session. This allows the agent to be
 * the primary responder for the voice session.</p>
 *
 * <p>Features demonstrated:</p>
 * <ul>
 *   <li>Using AgentSessionConfig to connect directly to an Azure AI Foundry agent</li>
 *   <li>Real-time audio capture and playback using javax.sound.sampled</li>
 *   <li>Sequence number based audio packet system for proper interrupt handling</li>
 *   <li>Azure Deep Noise Suppression and Echo Cancellation for audio quality</li>
 *   <li>Conversation logging to file</li>
 *   <li>Graceful shutdown handling</li>
 * </ul>
 *
 * <p>Required environment variables:</p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The Azure VoiceLive endpoint</li>
 *   <li>AGENT_NAME - The name of your Azure AI Foundry agent</li>
 *   <li>AGENT_PROJECT_NAME - The name of the Foundry project containing the agent</li>
 * </ul>
 *
 * <p>Optional environment variables:</p>
 * <ul>
 *   <li>AGENT_VERSION - The version of the agent (if not specified, uses latest)</li>
 *   <li>AGENT_VOICE - Voice to use (default: en-US-Ava:DragonHDLatestNeural)</li>
 *   <li>FOUNDRY_RESOURCE_OVERRIDE - Override for the Foundry resource URL</li>
 *   <li>AGENT_AUTH_IDENTITY_CLIENT_ID - Client ID for agent authentication</li>
 * </ul>
 */
public class AgentV2Sample {

    // Configuration from environment variables
    private static final String ENDPOINT = getRequiredEnv("AZURE_VOICELIVE_ENDPOINT");
    private static final String AGENT_NAME = getRequiredEnv("AGENT_NAME");
    private static final String AGENT_PROJECT_NAME = getRequiredEnv("AGENT_PROJECT_NAME");

    // Optional configuration
    private static final String AGENT_VERSION = System.getenv("AGENT_VERSION");
    private static final String AGENT_VOICE = getEnvOrDefault("AGENT_VOICE", "en-US-Ava:DragonHDLatestNeural");
    private static final String FOUNDRY_RESOURCE_OVERRIDE = System.getenv("FOUNDRY_RESOURCE_OVERRIDE");
    private static final String AGENT_AUTH_IDENTITY_CLIENT_ID = System.getenv("AGENT_AUTH_IDENTITY_CLIENT_ID");

    // Audio configuration - PCM16, 24kHz, mono
    private static final float SAMPLE_RATE = 24000.0f;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final int CHUNK_SIZE = 2400; // 50ms chunks at 24kHz, 16-bit mono
    private static final int AUDIO_BUFFER_SIZE_MULTIPLIER = 4; // Buffer multiplier for smoother audio

    // Turn detection configuration
    private static final double TURN_DETECTION_THRESHOLD = 0.5;
    private static final int TURN_DETECTION_PREFIX_PADDING_MS = 300;
    private static final int TURN_DETECTION_SILENCE_DURATION_MS = 500;

    // Logging
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILENAME;

    static {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        LOG_FILENAME = timestamp + "_agent_v2_conversation.log";
    }

    public static void main(String[] args) {
        System.out.println("Agent V2 Voice Assistant with Azure VoiceLive SDK");
        System.out.println("==================================================");
        System.out.println("Agent: " + AGENT_NAME);
        System.out.println("Project: " + AGENT_PROJECT_NAME);
        if (AGENT_VERSION != null && !AGENT_VERSION.isEmpty()) {
            System.out.println("Version: " + AGENT_VERSION);
        }
        System.out.println("Using AgentSessionConfig for agent configuration");
        System.out.println("==================================================");

        // Check audio system
        if (!checkAudioSystem()) {
            System.exit(1);
        }

        // Create logs directory
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
        } catch (IOException e) {
            System.err.println("Warning: Could not create logs directory: " + e.getMessage());
        }

        // Run the assistant
        try {
            runAssistant();
        } catch (Exception e) {
            System.err.println("Fatal Error: " + e.getMessage());
            // Note: In production, use a proper logger instead of printStackTrace
            if (System.getenv("DEBUG") != null) {
                e.printStackTrace();
            }
        }
    }

    private static void runAssistant() {
        // Build AgentSessionConfig
        AgentSessionConfig agentConfig = new AgentSessionConfig(AGENT_NAME, AGENT_PROJECT_NAME);

        // Add optional fields if provided
        if (AGENT_VERSION != null && !AGENT_VERSION.isEmpty()) {
            agentConfig.setAgentVersion(AGENT_VERSION);
        }
        if (FOUNDRY_RESOURCE_OVERRIDE != null && !FOUNDRY_RESOURCE_OVERRIDE.isEmpty()) {
            agentConfig.setFoundryResourceOverride(FOUNDRY_RESOURCE_OVERRIDE);
        }
        if (AGENT_AUTH_IDENTITY_CLIENT_ID != null && !AGENT_AUTH_IDENTITY_CLIENT_ID.isEmpty()) {
            agentConfig.setAuthenticationIdentityClientId(AGENT_AUTH_IDENTITY_CLIENT_ID);
        }

        System.out.println("\nUsing DefaultAzureCredential for authentication");

        // Create voice assistant
        AgentV2VoiceAssistant assistant = new AgentV2VoiceAssistant(
            ENDPOINT,
            agentConfig,
            AGENT_VOICE
        );

        // Setup shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nVoice assistant shut down. Goodbye!");
            assistant.shutdown();
        }));

        // Start the assistant
        assistant.start();
    }

    private static boolean checkAudioSystem() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);

        // Check for input (microphone)
        DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(inputInfo)) {
            System.err.println("ERROR: No audio input devices found. Please check your microphone.");
            return false;
        }

        // Check for output (speakers)
        DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(outputInfo)) {
            System.err.println("ERROR: No audio output devices found. Please check your speakers.");
            return false;
        }

        System.out.println("Audio system check passed");
        return true;
    }

    private static String getRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            System.err.println("ERROR: No " + name + " provided");
            System.err.println("Please set the " + name + " environment variable.");
            System.exit(1);
        }
        return value;
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Voice assistant using Azure AI Foundry agent with AgentSessionConfig.
     */
    static class AgentV2VoiceAssistant {
        private final String endpoint;
        private final AgentSessionConfig agentConfig;
        private final String voice;
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final CountDownLatch shutdownLatch = new CountDownLatch(1);

        private VoiceLiveSessionAsyncClient session;
        private AudioProcessor audioProcessor;

        AgentV2VoiceAssistant(String endpoint, AgentSessionConfig agentConfig, String voice) {
            this.endpoint = endpoint;
            this.agentConfig = agentConfig;
            this.voice = voice;
        }

        void start() {
            running.set(true);

            System.out.println("\nConnecting to VoiceLive API with agent " + agentConfig.getAgentName()
                + " for project " + agentConfig.getProjectName());

            // Create client with DefaultAzureCredential
            VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .serviceVersion(VoiceLiveServiceVersion.V2026_01_01_PREVIEW)
                .buildAsyncClient();

            // Connect using AgentSessionConfig
            client.startSession(agentConfig)
                .doOnSuccess(s -> {
                    this.session = s;
                    System.out.println("Connected to VoiceLive service");

                    // Initialize audio processor
                    this.audioProcessor = new AudioProcessor(s);

                    // Configure session
                    configureSession();

                    // Subscribe to events
                    subscribeToEvents();
                })
                .doOnError(e -> {
                    System.err.println("Failed to connect: " + e.getMessage());
                    running.set(false);
                    shutdownLatch.countDown();
                })
                .subscribe();

            // Wait for shutdown
            try {
                shutdownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void configureSession() {
            System.out.println("Setting up voice conversation session...");
            System.out.println("Enabling Azure Deep Noise Suppression");
            System.out.println("Enabling Echo Cancellation");

            // Create voice configuration - serialize to BinaryData
            AzureStandardVoice voiceConfig = new AzureStandardVoice(voice);
            BinaryData voiceBinaryData = BinaryData.fromObject(voiceConfig);

            // Create turn detection configuration with interrupt handling
            ServerVadTurnDetection turnDetection = new ServerVadTurnDetection()
                .setThreshold(TURN_DETECTION_THRESHOLD)
                .setPrefixPaddingMs(TURN_DETECTION_PREFIX_PADDING_MS)
                .setSilenceDurationMs(TURN_DETECTION_SILENCE_DURATION_MS)
                .setInterruptResponse(true)   // Allow user to interrupt agent response
                .setAutoTruncate(true)        // Auto-truncate response on interrupt
                .setCreateResponse(true);     // Auto-create response after speech ends

            // Create session options with full audio quality settings
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setVoice(voiceBinaryData)
                .setInputAudioFormat(InputAudioFormat.PCM16)
                .setOutputAudioFormat(OutputAudioFormat.PCM16)
                .setInputAudioSamplingRate((int) SAMPLE_RATE)  // Set explicit sampling rate
                .setTurnDetection(turnDetection)
                // Audio quality enhancements
                .setInputAudioEchoCancellation(new AudioEchoCancellation())
                .setInputAudioNoiseReduction(new AudioNoiseReduction(AudioNoiseReductionType.AZURE_DEEP_NOISE_SUPPRESSION));

            // Send session update
            ClientEventSessionUpdate sessionUpdate = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(sessionUpdate)
                .doOnSuccess(v -> System.out.println("Session configuration sent"))
                .doOnError(e -> System.err.println("Failed to configure session: " + e.getMessage()))
                .subscribe();
        }

        private void subscribeToEvents() {
            session.receiveEvents()
                .doOnNext(this::handleEvent)
                .doOnError(e -> {
                    System.err.println("Error receiving events: " + e.getMessage());
                    shutdown();
                })
                .doOnComplete(() -> {
                    System.out.println("Event stream completed");
                    shutdown();
                })
                .subscribe();
        }

        private void handleEvent(SessionUpdate event) {
            ServerEventType eventType = event.getType();

            if (eventType == ServerEventType.SESSION_UPDATED) {
                SessionUpdateSessionUpdated sessionEvent = (SessionUpdateSessionUpdated) event;
                String sessionId = sessionEvent.getSession().getId();
                String model = sessionEvent.getSession().getModel();

                System.out.println("Session ready: " + sessionId);
                writeConversationLog("SessionID: " + sessionId);
                writeConversationLog("Model: " + model);
                writeConversationLog("");

                // Start audio capture once session is ready
                audioProcessor.startCapture();
                audioProcessor.startPlayback();

                printReadyBanner();

            } else if (eventType == ServerEventType.CONVERSATION_ITEM_INPUT_AUDIO_TRANSCRIPTION_COMPLETED) {
                SessionUpdateConversationItemInputAudioTranscriptionCompleted transcriptionEvent =
                    (SessionUpdateConversationItemInputAudioTranscriptionCompleted) event;
                String transcript = transcriptionEvent.getTranscript();
                System.out.println("You said: " + transcript);
                writeConversationLog("User Input:\t" + transcript);

            } else if (eventType == ServerEventType.RESPONSE_TEXT_DONE) {
                SessionUpdateResponseTextDone textEvent = (SessionUpdateResponseTextDone) event;
                String text = textEvent.getText();
                System.out.println("Agent responded with text: " + text);
                writeConversationLog("Agent Text Response:\t" + text);

            } else if (eventType == ServerEventType.RESPONSE_AUDIO_TRANSCRIPT_DONE) {
                SessionUpdateResponseAudioTranscriptDone transcriptEvent =
                    (SessionUpdateResponseAudioTranscriptDone) event;
                String transcript = transcriptEvent.getTranscript();
                System.out.println("Agent responded with audio transcript: " + transcript);
                writeConversationLog("Agent Audio Response:\t" + transcript);

            } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
                System.out.println("Listening...");
                // Skip queued audio (interrupt handling)
                audioProcessor.skipPendingAudio();

            } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
                System.out.println("Processing...");

            } else if (eventType == ServerEventType.RESPONSE_CREATED) {
                System.out.println("Assistant response created");

            } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                SessionUpdateResponseAudioDelta audioEvent = (SessionUpdateResponseAudioDelta) event;
                byte[] audioData = audioEvent.getDelta();
                if (audioData != null && audioData.length > 0) {
                    audioProcessor.queueAudio(audioData);
                }

            } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
                System.out.println("Assistant finished speaking");

            } else if (eventType == ServerEventType.RESPONSE_DONE) {
                System.out.println("Response complete");
                System.out.println("Ready for next input...");

            } else if (eventType == ServerEventType.ERROR) {
                SessionUpdateError errorEvent = (SessionUpdateError) event;
                System.err.println("ERROR: VoiceLive error: " + errorEvent.getError().getMessage());

            } else if (eventType == ServerEventType.WARNING) {
                ServerEventWarning warningEvent = (ServerEventWarning) event;
                System.out.println("WARNING: " + warningEvent.getWarning().getMessage());

            } else if (eventType == ServerEventType.CONVERSATION_ITEM_CREATED) {
                // Conversation item created - no action needed for this sample
                System.out.println("Conversation item created");
            }
        }

        private void printReadyBanner() {
            System.out.println();
            System.out.println("============================================================");
            System.out.println("AGENT V2 VOICE ASSISTANT READY");
            System.out.println("Agent: " + agentConfig.getAgentName());
            System.out.println("Project: " + agentConfig.getProjectName());
            if (agentConfig.getAgentVersion() != null) {
                System.out.println("Version: " + agentConfig.getAgentVersion());
            }
            System.out.println("Start speaking to begin conversation");
            System.out.println("Press Ctrl+C to exit");
            System.out.println("============================================================");
            System.out.println();
        }

        void shutdown() {
            if (running.compareAndSet(true, false)) {
                System.out.println("Shutting down voice assistant...");

                if (audioProcessor != null) {
                    audioProcessor.shutdown();
                }

                if (session != null) {
                    session.closeAsync().subscribe();
                }

                shutdownLatch.countDown();
            }
        }

        private void writeConversationLog(String message) {
            try {
                Path logPath = Paths.get(LOG_DIR, LOG_FILENAME);
                try (PrintWriter writer = new PrintWriter(new FileWriter(logPath.toFile(), true))) {
                    writer.println(message);
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not write to conversation log: " + e.getMessage());
            }
        }
    }

    /**
     * Handles real-time audio capture and playback for the voice assistant.
     *
     * <p>Uses sequence number based audio packet system for proper interrupt handling.
     * Audio is captured from microphone and streamed to the service, while response
     * audio is queued and played back through speakers.</p>
     */
    static class AudioProcessor {
        private final VoiceLiveSessionAsyncClient session;
        private final AudioFormat format;
        private final BlockingQueue<AudioPacket> playbackQueue = new LinkedBlockingQueue<>();
        private final AtomicInteger nextSeqNum = new AtomicInteger(0);
        private final AtomicInteger playbackBase = new AtomicInteger(0);
        private final AtomicBoolean running = new AtomicBoolean(false);

        private TargetDataLine inputLine;
        private SourceDataLine outputLine;
        private Thread captureThread;
        private Thread playbackThread;

        AudioProcessor(VoiceLiveSessionAsyncClient session) {
            this.session = session;
            this.format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
            System.out.println("AudioProcessor initialized with 24kHz PCM16 mono audio");
        }

        void startCapture() {
            if (running.get()) {
                return;
            }
            running.set(true);

            try {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                inputLine = (TargetDataLine) AudioSystem.getLine(info);
                inputLine.open(format, CHUNK_SIZE * AUDIO_BUFFER_SIZE_MULTIPLIER);
                inputLine.start();

                captureThread = new Thread(this::captureLoop, "AudioCapture");
                captureThread.setDaemon(true);
                captureThread.start();

                System.out.println("Started audio capture");
            } catch (LineUnavailableException e) {
                System.err.println("Failed to start audio capture: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        private void captureLoop() {
            byte[] buffer = new byte[CHUNK_SIZE];

            while (running.get() && inputLine != null && inputLine.isOpen()) {
                int bytesRead = inputLine.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    byte[] audioData = (bytesRead == buffer.length)
                        ? buffer.clone()
                        : Arrays.copyOf(buffer, bytesRead);

                    // Send audio to service (sendInputAudio takes byte[])
                    session.sendInputAudio(audioData).subscribe();
                }
            }
        }

        void startPlayback() {
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                outputLine = (SourceDataLine) AudioSystem.getLine(info);
                outputLine.open(format, CHUNK_SIZE * AUDIO_BUFFER_SIZE_MULTIPLIER);
                outputLine.start();

                playbackThread = new Thread(this::playbackLoop, "AudioPlayback");
                playbackThread.setDaemon(true);
                playbackThread.start();

                System.out.println("Audio playback system ready");
            } catch (LineUnavailableException e) {
                System.err.println("Failed to initialize audio playback: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        private void playbackLoop() {
            while (running.get() && outputLine != null && outputLine.isOpen()) {
                try {
                    AudioPacket packet = playbackQueue.take();

                    if (packet.data == null) {
                        // End of stream marker
                        break;
                    }

                    // Skip if this packet is older than playback base (interrupted)
                    if (packet.seqNum < playbackBase.get()) {
                        continue;
                    }

                    // Write audio to speakers
                    outputLine.write(packet.data, 0, packet.data.length);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        void queueAudio(byte[] audioData) {
            int seqNum = nextSeqNum.getAndIncrement();
            playbackQueue.offer(new AudioPacket(seqNum, audioData));
        }

        void skipPendingAudio() {
            // Set playback base to current sequence number to skip all pending packets
            playbackBase.set(nextSeqNum.get());
            // Also drain the queue to avoid buildup
            playbackQueue.clear();
            // Flush the speaker buffer to stop playback immediately
            if (outputLine != null && outputLine.isOpen()) {
                outputLine.flush();
            }
        }

        void shutdown() {
            running.set(false);

            // Stop capture
            if (inputLine != null) {
                inputLine.stop();
                inputLine.close();
                inputLine = null;
            }
            if (captureThread != null) {
                captureThread.interrupt();
            }
            System.out.println("Stopped audio capture");

            // Stop playback
            skipPendingAudio();
            playbackQueue.offer(new AudioPacket(-1, null)); // End marker

            if (outputLine != null) {
                outputLine.stop();
                outputLine.close();
                outputLine = null;
            }
            if (playbackThread != null) {
                playbackThread.interrupt();
            }
            System.out.println("Stopped audio playback");

            System.out.println("Audio processor cleaned up");
        }

        /**
         * Represents a packet in the audio playback queue with sequence number for interrupt handling.
         */
        static class AudioPacket {
            final int seqNum;
            final byte[] data;

            AudioPacket(int seqNum, byte[] data) {
                this.seqNum = seqNum;
                this.data = data;
            }
        }
    }
}
