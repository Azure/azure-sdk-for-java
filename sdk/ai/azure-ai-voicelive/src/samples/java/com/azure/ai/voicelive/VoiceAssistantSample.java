// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Enhanced Voice Assistant sample demonstrating real-time microphone input and audio playback.
 *
 * This sample shows how to:
 * - Capture audio from microphone in real-time
 * - Send captured audio to VoiceLive service
 * - Receive and play audio responses from the service
 * - Handle conversation flow with proper interruption
 * - Manage audio streaming with proper threading
 */
public class VoiceAssistantSample {

    // Constants
    private static final String DEFAULT_API_VERSION = "2025-10-01";
    private static final String DEFAULT_MODEL = "gpt-4o-realtime-preview";
    private static final int SAMPLE_RATE = 24000;  // 24kHz as required by VoiceLive
    private static final int CHANNELS = 1;         // Mono
    private static final int SAMPLE_SIZE_BITS = 16; // 16-bit PCM
    private static final int CHUNK_SIZE = 1200;    // 50ms chunks (24000 * 0.05)

    /**
     * Audio packet for playback queue management
     */
    private static class AudioPlaybackPacket {
        final int sequenceNumber;
        final byte[] audioData;

        AudioPlaybackPacket(int sequenceNumber, byte[] audioData) {
            this.sequenceNumber = sequenceNumber;
            this.audioData = audioData;
        }
    }

    /**
     * Handles real-time audio capture and playback for the voice assistant.
     */
    private static class AudioProcessor {
        private final VoiceLiveSession session;
        private final AudioFormat audioFormat;

        // Audio capture components
        private TargetDataLine microphone;
        private final AtomicBoolean isCapturing = new AtomicBoolean(false);

        // Audio playback components
        private SourceDataLine speaker;
        private final BlockingQueue<AudioPlaybackPacket> playbackQueue = new LinkedBlockingQueue<>();
        private final AtomicBoolean isPlaying = new AtomicBoolean(false);
        private final AtomicInteger nextSequenceNumber = new AtomicInteger(0);
        private final AtomicInteger playbackBase = new AtomicInteger(0);

        // Session management for cancellation
        private final AtomicReference<String> currentResponseId = new AtomicReference<>();

        AudioProcessor(VoiceLiveSession session) {
            this.session = session;
            this.audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                SAMPLE_RATE,
                SAMPLE_SIZE_BITS,
                CHANNELS,
                CHANNELS * SAMPLE_SIZE_BITS / 8, // frameSize
                SAMPLE_RATE,
                false // bigEndian
            );

            System.out.println("✓ AudioProcessor initialized (24kHz PCM16 mono)");
        }

        /**
         * Start capturing audio from microphone
         */
        void startCapture() {
            if (isCapturing.get()) {
                return;
            }

            try {
                DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

                if (!AudioSystem.isLineSupported(micInfo)) {
                    throw new UnsupportedOperationException("Microphone not supported with required format");
                }

                microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
                microphone.open(audioFormat, CHUNK_SIZE * 4); // Buffer size
                microphone.start();

                isCapturing.set(true);

                // Start capture thread
                Thread captureThread = new Thread(this::captureAudioLoop, "AudioCapture");
                captureThread.setDaemon(true);
                captureThread.start();

                System.out.println("🎤 Microphone capture started");

            } catch (LineUnavailableException e) {
                System.err.println("❌ Failed to start microphone: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        /**
         * Start audio playback system
         */
        void startPlayback() {
            if (isPlaying.get()) {
                return;
            }

            try {
                DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

                if (!AudioSystem.isLineSupported(speakerInfo)) {
                    throw new UnsupportedOperationException("Speaker not supported with required format");
                }

                speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
                speaker.open(audioFormat, CHUNK_SIZE * 4); // Buffer size
                speaker.start();

                isPlaying.set(true);

                // Start playback thread
                Thread playbackThread = new Thread(this::playbackAudioLoop, "AudioPlayback");
                playbackThread.setDaemon(true);
                playbackThread.start();

                System.out.println("🔊 Audio playback started");

            } catch (LineUnavailableException e) {
                System.err.println("❌ Failed to start speaker: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        /**
         * Audio capture loop - runs in separate thread
         */
        private void captureAudioLoop() {
            byte[] buffer = new byte[CHUNK_SIZE * 2]; // 16-bit samples

            while (isCapturing.get() && microphone != null) {
                try {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        // Send audio to VoiceLive service
                        byte[] audioChunk = Arrays.copyOf(buffer, bytesRead);

                        // Send audio asynchronously using the session's audio buffer append
                        session.sendInputAudio(BinaryData.fromBytes(audioChunk))
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe(
                                v -> {}, // onNext
                                error -> System.err.println("❌ Error sending audio: " + error.getMessage()) // onError
                            );
                    }
                } catch (Exception e) {
                    if (isCapturing.get()) {
                        System.err.println("❌ Error in audio capture: " + e.getMessage());
                    }
                    break;
                }
            }
        }

        /**
         * Audio playback loop - runs in separate thread
         */
        private void playbackAudioLoop() {
            while (isPlaying.get()) {
                try {
                    AudioPlaybackPacket packet = playbackQueue.take(); // Blocking wait

                    if (packet.audioData == null) {
                        // Shutdown signal
                        break;
                    }

                    // Check if packet should be skipped
                    if (packet.sequenceNumber < playbackBase.get()) {
                        continue;
                    }

                    // Play the audio
                    if (speaker != null && speaker.isOpen()) {
                        speaker.write(packet.audioData, 0, packet.audioData.length);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("❌ Error in audio playback: " + e.getMessage());
                }
            }
        }

        /**
         * Queue audio data for playback
         */
        void queueAudio(byte[] audioData) {
            if (audioData != null && audioData.length > 0) {
                int seqNum = nextSequenceNumber.getAndIncrement();
                playbackQueue.offer(new AudioPlaybackPacket(seqNum, audioData));
            }
        }

        /**
         * Skip pending audio (for interruption handling)
         */
        void skipPendingAudio() {
            playbackBase.set(nextSequenceNumber.get());
            playbackQueue.clear();
        }

        /**
         * Cancel current response and skip audio
         */
        void cancelCurrentResponse() {
            skipPendingAudio();
            // Try to cancel any ongoing response
            String responseId = currentResponseId.get();
            if (responseId != null) {
                session.startResponse() // This will effectively cancel previous response
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                        v -> {}, // onNext
                        error -> {} // onError - ignore cancellation errors
                    );
            }
        }

        /**
         * Set current response ID for tracking
         */
        void setCurrentResponseId(String responseId) {
            currentResponseId.set(responseId);
        }

        /**
         * Stop capture and playback
         */
        void shutdown() {
            // Stop capture
            isCapturing.set(false);
            if (microphone != null) {
                microphone.stop();
                microphone.close();
                microphone = null;
            }
            System.out.println("🎤 Microphone capture stopped");

            // Stop playback
            isPlaying.set(false);
            playbackQueue.offer(new AudioPlaybackPacket(-1, null)); // Shutdown signal
            if (speaker != null) {
                speaker.stop();
                speaker.close();
                speaker = null;
            }
            System.out.println("🔊 Audio playback stopped");
        }
    }

    /**
     * Main method to run the voice assistant sample.
     */
    public static void main(String[] args) {
        // Validate environment variables
        String endpoint = System.getenv("VOICELIVE_OPENAI_ENDPOINT");
        String apiKey = System.getenv("VOICELIVE_OPENAI_API_KEY");

        if (endpoint == null || apiKey == null) {
            printUsage();
            return;
        }

        // Check audio system availability
        if (!checkAudioSystem()) {
            System.err.println("❌ Audio system check failed. Please ensure microphone and speakers are available.");
            return;
        }

        System.out.println("🎙️ Starting Voice Assistant...");

        try {
            runVoiceAssistant(endpoint, apiKey);
            System.out.println("✓ Voice Assistant completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Voice Assistant failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if audio system is available
     */
    private static boolean checkAudioSystem() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);

            // Check microphone
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(micInfo)) {
                System.err.println("❌ No compatible microphone found");
                return false;
            }

            // Check speaker
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(speakerInfo)) {
                System.err.println("❌ No compatible speaker found");
                return false;
            }

            System.out.println("✓ Audio system check passed");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Audio system check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Print usage instructions
     */
    private static void printUsage() {
        System.err.println("Please set VOICELIVE_OPENAI_ENDPOINT and VOICELIVE_OPENAI_API_KEY environment variables");
        System.err.println("\nExample:");
        System.err.println("  export VOICELIVE_OPENAI_ENDPOINT=https://your-resource.cognitiveservices.azure.com/");
        System.err.println("  export VOICELIVE_OPENAI_API_KEY=your-api-key");
    }

    /**
     * Run the voice assistant
     */
    private static void runVoiceAssistant(String endpoint, String apiKey) {
        System.out.println("🔧 Initializing VoiceLive client:");
        System.out.println("   Endpoint: " + endpoint);
        System.out.println("   API Key: " + (apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "***"));

        // Create the VoiceLive client
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .apiVersion(DEFAULT_API_VERSION)
            .buildAsyncClient();

        System.out.println("✓ VoiceLive client created");

        // Configure session options for voice conversation
        VoiceLiveSessionOptions sessionOptions = createVoiceSessionOptions();

        AtomicReference<AudioProcessor> audioProcessorRef = new AtomicReference<>();
        AtomicBoolean sessionUpdateSent = new AtomicBoolean(false);

        // Execute the reactive workflow - start with just the model
        client.startSession(DEFAULT_MODEL)
            .flatMap(session -> {
                System.out.println("✓ Session started successfully");

                // Create audio processor
                AudioProcessor audioProcessor = new AudioProcessor(session);
                audioProcessorRef.set(audioProcessor);

                System.out.println("📤 Sending session.update configuration...");
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                session.sendEvent(updateEvent)
                                .doOnSuccess(v -> System.out.println("✓ Session configuration sent"))
                                .doOnError(error -> System.err.println("❌ Failed to send session.update: " + error.getMessage()))
                                .subscribe();

                // Subscribe to receive server events asynchronously
                session.receiveEvents()
                    .doOnSubscribe(subscription -> System.out.println("🔗 Subscribed to event stream"))
                    .doOnNext(event -> {
                        System.out.println("📨 Received event: " + event.getType());
                    })
                    .doOnComplete(() -> System.out.println("⚠️ Event stream completed (this might indicate a connection issue)"))
                    .doOnError(error -> System.out.println("❌ Event stream error: " + error.getMessage()))
                    .subscribe(
                        event -> handleServerEvent(event, audioProcessor),
                        error -> System.err.println("❌ Error receiving events: " + error.getMessage()),
                        () -> System.out.println("✓ Event stream completed")
                    );

                // Start audio systems
                audioProcessor.startPlayback();

                System.out.println("🎤 VOICE ASSISTANT READY");
                System.out.println("Start speaking to begin conversation");
                System.out.println("Press Ctrl+C to exit");

                // Don't send audio buffer immediately - wait for session.created from server first
                // The server needs to establish the session before accepting audio

                // Keep the session open - return a Mono that never completes
                // This allows the event stream to continue running until interrupted
                return Mono.never();
            })
            .doOnError(error -> System.err.println("❌ Error: " + error.getMessage()))
            .doFinally(signalType -> {
                // Cleanup audio processor
                AudioProcessor audioProcessor = audioProcessorRef.get();
                if (audioProcessor != null) {
                    audioProcessor.shutdown();
                }
            })
            .block(); // Block only for demo purposes; use reactive patterns in production
    }

    /**
     * Create session configuration for voice conversation
     */
    private static VoiceLiveSessionOptions createVoiceSessionOptions() {
        System.out.println("🔧 Creating session configuration:");
        System.out.println("   Model: " + DEFAULT_MODEL);
        System.out.println("   API Version: " + DEFAULT_API_VERSION);

        // Create server VAD configuration similar to Python sample
        ServerVadTurnDetection turnDetection = new ServerVadTurnDetection()
            .setThreshold(0.5)
            .setPrefixPaddingMs(300)
            .setSilenceDurationMs(500)
            .setInterruptResponse(true)
            .setAutoTruncate(true)
            .setCreateResponse(true);

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful AI voice assistant. Respond naturally and conversationally. Keep your responses concise but engaging. Speak as if having a real conversation.")
            .setVoice(new OpenAIVoice(OpenAIVoiceName.ALLOY))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE)
            .setInputAudioNoiseReduction(new AudioNoiseReduction(AudioNoiseReductionType.AZURE_DEEP_NOISE_SUPPRESSION))
            .setInputAudioEchoCancellation(new AudioEchoCancellation())
            .setTurnDetection(turnDetection);


        System.out.println("✓ Session configuration created");
        return options;
    }

    /**
     * Handle incoming server events
     */
    private static void handleServerEvent(SessionUpdate event, AudioProcessor audioProcessor) {
        ServerEventType eventType = event.getType();
        System.out.printf("📨 Event: %s%n", eventType);

        try {
            if (eventType == ServerEventType.SESSION_CREATED) {
                System.out.println("✓ Session created - initializing...");
            } else if (eventType == ServerEventType.SESSION_UPDATED) {
                System.out.println("✓ Session ready - starting microphone");
                audioProcessor.startCapture();

            } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
                System.out.println("🎤 Listening...");
                audioProcessor.cancelCurrentResponse();

            } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
                System.out.println("🤔 Processing...");

            } else if (eventType == ServerEventType.RESPONSE_CREATED) {
                System.out.println("🤖 Assistant responding...");
                if (event instanceof SessionUpdateResponseCreated) {
                    SessionUpdateResponseCreated responseEvent = (SessionUpdateResponseCreated) event;
                    audioProcessor.setCurrentResponseId(responseEvent.getResponse().getId());
                }

            } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                // Handle audio response - extract and queue for playback
                if (event instanceof SessionUpdateResponseAudioDelta) {
                    SessionUpdateResponseAudioDelta audioEvent = (SessionUpdateResponseAudioDelta) event;
                    byte[] audioData = audioEvent.getDelta();
                    if (audioData != null && audioData.length > 0) {
                        audioProcessor.queueAudio(audioData);
                        System.out.println("🔊 Queueing audio response (" + audioData.length + " bytes)");
                    }
                }

            } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
                System.out.println("✓ Assistant finished speaking");
                System.out.println("🎤 Ready for next input...");

            } else if (eventType == ServerEventType.RESPONSE_DONE) {
                System.out.println("✅ Response complete");

            } else if (eventType == ServerEventType.ERROR) {
                if (event instanceof SessionUpdateError) {
                    SessionUpdateError errorEvent = (SessionUpdateError) event;
                    System.out.println("❌ VoiceLive error: " + errorEvent.getError().getMessage());
                } else {
                    System.out.println("❌ VoiceLive error occurred");
                }

            } else if (eventType == ServerEventType.CONVERSATION_ITEM_CREATED) {
                System.out.println("📝 Conversation item created");

            } else if (eventType == ServerEventType.RESPONSE_CONTENT_PART_DONE) {
                System.out.println("📝 Conversation content part done");

            } else if (eventType == ServerEventType.RESPONSE_OUTPUT_ITEM_DONE) {
                System.out.println("🔊 Response output item done");

            } else if (eventType == ServerEventType.RESPONSE_AUDIO_TRANSCRIPT_DELTA) {
                System.out.println("🔊 Response audio transcript delta");

            } else {
                System.out.printf("📋 Unhandled event: %s%n", eventType);
            }

        } catch (Exception e) {
            System.err.println("❌ Error handling event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================================================
    // Utility Methods
    // ========================================================================

    /**
     * Create demo audio data (for testing purposes)
     */
    private static byte[] createDemoAudio(int durationMs) {
        int samples = (SAMPLE_RATE * durationMs) / 1000;
        byte[] audio = new byte[samples * 2]; // 16-bit samples

        // Generate a simple sine wave for testing
        for (int i = 0; i < samples; i++) {
            double sample = Math.sin(2.0 * Math.PI * 440.0 * i / SAMPLE_RATE); // 440Hz tone
            short value = (short) (sample * Short.MAX_VALUE * 0.1); // Low volume
            audio[i * 2] = (byte) (value & 0xFF);
            audio[i * 2 + 1] = (byte) ((value >> 8) & 0xFF);
        }

        return audio;
    }

    /**
     * List available audio devices (for debugging)
     */
    @SuppressWarnings("unused")
    private static void listAudioDevices() {
        System.out.println("\n🎧 Available Audio Devices:");

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixers) {
            System.out.println("Mixer: " + mixerInfo.getName());

            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            // Input lines
            Line.Info[] sourceLines = mixer.getSourceLineInfo();
            for (Line.Info lineInfo : sourceLines) {
                System.out.println("  Input: " + lineInfo);
            }

            // Output lines
            Line.Info[] targetLines = mixer.getTargetLineInfo();
            for (Line.Info lineInfo : targetLines) {
                System.out.println("  Output: " + lineInfo);
            }
        }
        System.out.println();
    }
}
