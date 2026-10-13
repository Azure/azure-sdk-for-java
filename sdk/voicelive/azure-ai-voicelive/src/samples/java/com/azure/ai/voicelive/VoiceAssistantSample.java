// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.AudioEchoCancellation;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.AudioNoiseReduction;
import com.azure.ai.voicelive.models.AudioNoiseReductionType;
import com.azure.ai.voicelive.models.AzureStandardVoice;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionResponse;
import com.azure.ai.voicelive.models.SessionResponseStatus;
import com.azure.ai.voicelive.models.SessionServerEvent;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
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

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Complete voice assistant sample demonstrating full-featured real-time voice conversation.
 *
 * <p><strong>NOTE:</strong> This is a comprehensive sample showing all features together.
 * For easier understanding, see these focused samples:</p>
 * <ul>
 *   <li>{@link BasicVoiceConversationSample} - Minimal setup and session management</li>
 *   <li>{@link MicrophoneInputSample} - Audio capture from microphone</li>
 *   <li>{@link AudioPlaybackSample} - Audio playback to speakers</li>
 *   <li>{@link AuthenticationMethodsSample} - Different authentication methods</li>
 * </ul>
 *
 * <p>Use this sample when you want the closest thing to an end-to-end assistant experience in this
 * package. It combines session configuration, microphone capture, speaker playback, and interruption
 * handling in one place.</p>
 *
 * <p>When you run it, the sample opens a realtime session, sends the session configuration, waits
 * for the service to report the session as ready, and then starts full-duplex microphone capture
 * and speaker playback.</p>
 *
 * <p>This sample demonstrates:</p>
 * <ul>
 *   <li>Real-time microphone audio capture</li>
 *   <li>Streaming audio to VoiceLive service</li>
 *   <li>Receiving and playing audio responses through speakers</li>
 *   <li>Voice Activity Detection (VAD) with interruption handling</li>
 *   <li>Multi-threaded audio processing</li>
 *   <li>Audio transcription with Whisper</li>
 *   <li>Noise reduction and echo cancellation</li>
 * </ul>
 *
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The VoiceLive service endpoint URL</li>
 * </ul>
 *
 * <p><strong>Audio Requirements:</strong></p>
 * The sample requires a working microphone and speakers/headphones.
 * Audio format is 24kHz, 16-bit PCM, mono as required by the VoiceLive service.
 *
 * <p>This sample uses {@link DefaultAzureCredentialBuilder} (Entra ID, recommended). For an example
 * of API key authentication, see {@link AuthenticationMethodsSample}.</p>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.VoiceAssistantSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class VoiceAssistantSample {

    // Service configuration constants
    private static final String DEFAULT_MODEL = "gpt-realtime";

    // Environment variable names
    private static final String ENV_ENDPOINT = "AZURE_VOICELIVE_ENDPOINT";

    // Audio format constants (VoiceLive requirements)
    private static final int SAMPLE_RATE = 24000;          // 24kHz as required by VoiceLive
    private static final int CHANNELS = 1;                 // Mono
    private static final int SAMPLE_SIZE_BITS = 16;        // 16-bit PCM
    private static final int CHUNK_SIZE = 1200;            // 50ms chunks (24000 * 0.05)
    private static final int AUDIO_BUFFER_SIZE_MULTIPLIER = 4;

    // Private constructor to prevent instantiation
    private VoiceAssistantSample() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Audio packet for playback queue management.
     * Uses sequence numbers to support interruption handling.
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
     * Handles real-time audio capture from microphone and playback to speakers.
     *
     * <p>This class manages two separate threads:</p>
     * <ul>
     *   <li>Capture thread: Continuously reads audio from microphone and sends to VoiceLive service</li>
     *   <li>Playback thread: Receives audio responses and plays them through speakers</li>
     * </ul>
     *
     * <p>Supports interruption handling where user speech can cancel ongoing assistant responses.</p>
     */
    static final class AudioProcessor {
        private final VoiceLiveSessionAsyncClient session;
        private final AudioFormat audioFormat;

        // Audio capture components
        // volatile: shared between the reactor event thread (startCapture) and the audio capture worker thread
        private volatile TargetDataLine microphone;
        private volatile Thread captureThread;
        private final AtomicBoolean isCapturing = new AtomicBoolean(false);

        // Audio playback components
        // volatile: shared between the reactor event thread (startPlayback) and the audio playback worker thread
        private volatile SourceDataLine speaker;
        private volatile Thread playbackThread;
        private final CountDownLatch playbackCompleted = new CountDownLatch(1);
        private final AtomicReference<Throwable> playbackFailure = new AtomicReference<>();
        private final CompletableFuture<Void> playbackTermination = new CompletableFuture<>();
        private final Object playbackControlLock = new Object();
        private final BlockingQueue<AudioPlaybackPacket> playbackQueue = new LinkedBlockingQueue<>(1000);
        private final AtomicBoolean isPlaying = new AtomicBoolean(false);
        private final AtomicInteger nextSequenceNumber = new AtomicInteger(0);
        private final AtomicInteger playbackBase = new AtomicInteger(0);
        private final VoiceAssistantPlaybackDiagnostics playbackDiagnostics
            = new VoiceAssistantPlaybackDiagnostics();

        AudioProcessor(VoiceLiveSessionAsyncClient session) {
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
                microphone.open(audioFormat, CHUNK_SIZE * AUDIO_BUFFER_SIZE_MULTIPLIER);
                microphone.start();

                isCapturing.set(true);

                // Start capture thread
                captureThread = new Thread(this::captureAudioLoop, "VoiceLive-AudioCapture");
                captureThread.setDaemon(true);
                captureThread.start();

                System.out.println("🎤 Microphone capture started");

            } catch (LineUnavailableException e) {
                System.err.println("❌ Failed to start microphone: " + e.getMessage());
                throw new RuntimeException("Failed to initialize microphone", e);
            }
        }

        /**
         * Starts audio playback system.
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
                speaker.open(audioFormat, CHUNK_SIZE * AUDIO_BUFFER_SIZE_MULTIPLIER);
                speaker.start();

                isPlaying.set(true);

                // Start playback thread
                playbackThread = new Thread(this::playbackAudioLoop, "VoiceLive-AudioPlayback");
                playbackThread.setDaemon(true);
                playbackThread.start();

                System.out.println("🔊 Audio playback started");

            } catch (LineUnavailableException e) {
                System.err.println("❌ Failed to start speaker: " + e.getMessage());
                throw new RuntimeException("Failed to initialize speaker", e);
            }
        }

        /**
         * Audio capture loop - runs in separate thread
         */
        private void captureAudioLoop() {
            byte[] buffer = new byte[CHUNK_SIZE * 2]; // 16-bit samples
            System.out.println("🎤 Audio capture loop started");

            while (isCapturing.get() && microphone != null) {
                try {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        // Send audio to VoiceLive service
                        byte[] audioChunk = Arrays.copyOf(buffer, bytesRead);

                        // Block on this capture thread so sends are serialized; fire-and-forget
                        // subscribes can flood the WebSocket send sink and trigger FAIL_OVERFLOW.
                        try {
                            session.sendInputAudio(BinaryData.fromBytes(audioChunk)).block();
                        } catch (Exception sendError) {
                            String msg = sendError.getMessage();
                            if (isCapturing.get() && (msg == null || !msg.contains("cancelled"))) {
                                System.err.println("❌ Error sending audio: " + msg);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (isCapturing.get()) {
                        System.err.println("❌ Error in audio capture: " + e.getMessage());
                    }
                    break;
                }
            }
            System.out.println("🎤 Audio capture loop ended");
        }

        /**
         * Audio playback loop - runs in separate thread
         */
        private void playbackAudioLoop() {
            try {
                while (true) {
                    AudioPlaybackPacket packet = playbackQueue.take(); // Blocking wait

                    if (packet.audioData == null) {
                        synchronized (playbackControlLock) {
                            if (isPlaying.get() && speaker != null && speaker.isOpen()) {
                                speaker.drain();
                            }
                        }
                        break;
                    }

                    synchronized (playbackControlLock) {
                        // Check and write under the same lock so interruption cannot flush between them.
                        int currentBase = playbackBase.get();
                        if (packet.sequenceNumber < currentBase) {
                            playbackDiagnostics.recordSkippedPacket();
                            continue;
                        }

                        if (speaker != null && speaker.isOpen()) {
                            speaker.write(packet.audioData, 0, packet.audioData.length);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (isPlaying.get()) {
                    playbackFailure.compareAndSet(null, e);
                }
            } catch (Exception e) {
                playbackFailure.compareAndSet(null, e);
                System.err.println("❌ Error in audio playback: " + e.getMessage());
            } finally {
                Throwable failure = playbackFailure.get();
                if (failure == null) {
                    playbackTermination.complete(null);
                } else {
                    playbackTermination.completeExceptionally(failure);
                }
                playbackCompleted.countDown();
            }
        }

        Mono<Void> playbackFailure() {
            return Mono.fromFuture(playbackTermination).then(Mono.<Void>never());
        }

        /**
         * Queue audio data for playback
         */
        void queueAudio(byte[] audioData) {
            if (audioData == null || audioData.length == 0) {
                return;
            }

            int seqNum = nextSequenceNumber.getAndIncrement();
            // offer() returns false if the bounded queue is full; count drops without logging every delta.
            boolean accepted = playbackQueue.offer(new AudioPlaybackPacket(seqNum, audioData));
            playbackDiagnostics.recordAudioChunk(audioData.length, accepted, playbackQueue.size());
        }

        /**
         * Skip pending audio (for interruption handling)
         */
        void skipPendingAudio() {
            int cutoff;
            int removed = 0;
            int queueDepth;
            synchronized (playbackControlLock) {
                cutoff = nextSequenceNumber.get();
                playbackBase.set(cutoff);

                AudioPlaybackPacket packet;
                while ((packet = playbackQueue.poll()) != null) {
                    if (packet.audioData != null) {
                        removed++;
                    }
                }
                queueDepth = playbackQueue.size();

                // Flush after advancing the cutoff and clearing queued audio.
                if (speaker != null && speaker.isOpen()) {
                    speaker.flush();
                }
            }
            playbackDiagnostics.recordSkip(removed);

            System.out.println("Playback interruption: skipCount=" + playbackDiagnostics.getSkipOperationCount()
                + ", removed=" + removed
                + ", cutoff=" + cutoff
                + ", totalSkipped=" + playbackDiagnostics.getSkippedPacketCount()
                + ", queueCurrent=" + queueDepth
                + ", queueHighWaterApprox=" + playbackDiagnostics.getHighWaterQueueDepth());
        }

        void printAudioSummary(SessionUpdateResponseAudioDone audioDone) {
            System.out.println("Audio response complete: responseId=" + audioDone.getResponseId()
                + ", itemId=" + audioDone.getItemId()
                + ", outputIndex=" + audioDone.getOutputIndex()
                + ", contentIndex=" + audioDone.getContentIndex()
                + ", chunks=" + playbackDiagnostics.getAudioChunkCount()
                + ", bytes=" + playbackDiagnostics.getAudioByteCount()
                + ", dropped=" + playbackDiagnostics.getDroppedPacketCount()
                + ", skipped=" + playbackDiagnostics.getSkippedPacketCount()
                + ", queueCurrent=" + playbackQueue.size()
                + ", queueHighWaterApprox=" + playbackDiagnostics.getHighWaterQueueDepth());
        }

        void printResponseSummary(String responseId, SessionResponseStatus status) {
            System.out.println("✅ Response complete: responseId=" + responseId
                + ", status=" + status
                + ", chunks=" + playbackDiagnostics.getAudioChunkCount()
                + ", bytes=" + playbackDiagnostics.getAudioByteCount()
                + ", dropped=" + playbackDiagnostics.getDroppedPacketCount()
                + ", skipped=" + playbackDiagnostics.getSkippedPacketCount()
                + ", skipCalls=" + playbackDiagnostics.getSkipOperationCount()
                + ", queueCurrent=" + playbackQueue.size()
                + ", queueHighWaterApprox=" + playbackDiagnostics.getHighWaterQueueDepth());
        }

        /**
         * Stop capture and drain queued playback after a normal receive-stream completion.
         */
        void shutdownGracefully() {
            stopCapture();
            if (isPlaying.get() && playbackThread != null) {
                try {
                    if (!playbackQueue.offer(new AudioPlaybackPacket(-1, null), 5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out enqueueing playback drain marker");
                    }
                    if (!playbackCompleted.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out waiting for queued audio to drain");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while draining queued audio", e);
                }
                Throwable failure = playbackFailure.get();
                if (failure != null) {
                    throw new IllegalStateException("Audio playback failed", failure);
                }
            }
            isPlaying.set(false);
            closeSpeaker(false);
        }

        /**
         * Abort capture and playback without waiting for queued audio.
         */
        void shutdown() {
            stopCapture();
            isPlaying.set(false);
            playbackQueue.clear();
            playbackQueue.offer(new AudioPlaybackPacket(-1, null));
            Thread currentPlaybackThread = playbackThread;
            if (currentPlaybackThread != null) {
                currentPlaybackThread.interrupt();
            }
            closeSpeaker(true);
        }

        private void stopCapture() {
            isCapturing.set(false);
            if (microphone != null) {
                microphone.stop();
                microphone.close();
                microphone = null;
            }
            Thread currentCaptureThread = captureThread;
            if (currentCaptureThread != null) {
                currentCaptureThread.interrupt();
            }
            System.out.println("🎤 Microphone capture stopped");
        }

        private void closeSpeaker(boolean flush) {
            synchronized (playbackControlLock) {
                if (speaker != null) {
                    if (flush && speaker.isOpen()) {
                        speaker.flush();
                    }
                    speaker.stop();
                    speaker.close();
                    speaker = null;
                }
            }
            System.out.println("🔊 Audio playback stopped");
        }
    }

    /**
     * Main method to run the voice assistant sample.
     *
     * <p>Authenticates using {@link DefaultAzureCredentialBuilder} (Entra ID). For an example of
     * API key authentication, see {@link AuthenticationMethodsSample}.</p>
     *
     * @param args Unused command line arguments.
     */
    public static void main(String[] args) {
        // Validate environment variables
        String endpoint = System.getenv(ENV_ENDPOINT);

        if (endpoint == null) {
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
            System.out.println("🔑 Using DefaultAzureCredential authentication");
            System.out.println("   Make sure you have run 'az login' before running this sample");
            TokenCredential credential = new DefaultAzureCredentialBuilder().build();
            runVoiceAssistant(endpoint, credential);
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
     * Prints usage instructions for setting up environment variables.
     */
    private static void printUsage() {
        System.err.println("\nRequired Environment Variables:");
        System.err.println("  " + ENV_ENDPOINT + "=<your-voicelive-endpoint>");
    }

    /**
     * Run the voice assistant with Azure AD authentication.
     *
     * @param endpoint The VoiceLive service endpoint
     * @param credential The token credential
     */
    private static void runVoiceAssistant(String endpoint, TokenCredential credential) {
        System.out.println("🔧 Initializing VoiceLive client:");
        System.out.println("   Endpoint: " + endpoint);

        // Create the VoiceLive client
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        runVoiceAssistantWithClient(client);
    }

    /**
     * Run the voice assistant with the configured client.
     *
     * @param client The VoiceLive async client
     */
    private static void runVoiceAssistantWithClient(VoiceLiveAsyncClient client) {
        System.out.println("✓ VoiceLive client created");

        AtomicReference<AudioProcessor> audioProcessorRef = new AtomicReference<>();
        AtomicReference<VoiceLiveSessionAsyncClient> sessionRef = new AtomicReference<>();
        Thread shutdownHook = new Thread(() -> {
            shutdownAudio(audioProcessorRef);
            closeSession(sessionRef);
        }, "VoiceLive-Shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        boolean streamCompleted = false;
        try {
            Mono.usingWhen(
                client.startSession(DEFAULT_MODEL, null).doOnNext(session -> {
                    System.out.println("✓ Session started successfully");
                    sessionRef.set(session);
                    audioProcessorRef.set(new AudioProcessor(session));
                }),
                session -> {
                    AudioProcessor audioProcessor = audioProcessorRef.get();
                    Mono<Void> eventStream = configureSession(session)
                        .thenMany(session.receiveEvents())
                        .doOnNext(event -> handleServerEvent(event, audioProcessor))
                        .then();
                    return Mono.firstWithSignal(eventStream, audioProcessor.playbackFailure());
                },
                session -> closeSessionAsync(session, sessionRef))
                .block();
            shutdownAudioGracefully(audioProcessorRef);
            streamCompleted = true;
            System.out.println("✓ Event stream completed");
        } finally {
            if (!streamCompleted) {
                shutdownAudio(audioProcessorRef);
            }
            closeSession(sessionRef);
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {
                // JVM shutdown is already in progress and the hook is running.
            }
        }
    }

    /**
     * Send the session configuration for voice conversation.
     */
    private static Mono<Void> configureSession(VoiceLiveSessionAsyncClient session) {
        System.out.println("📤 Sending session.update configuration...");
        return session.sendEvent(new ClientEventSessionUpdate(createVoiceSessionOptions())).then();
    }

    /**
     * Stop capture and drain playback after a normal stream completion.
     */
    private static void shutdownAudioGracefully(AtomicReference<AudioProcessor> audioProcessorRef) {
        AudioProcessor audioProcessor = audioProcessorRef.get();
        if (audioProcessor != null) {
            audioProcessor.shutdownGracefully();
            audioProcessorRef.compareAndSet(audioProcessor, null);
        }
    }

    /**
     * Abort the audio processor.
     */
    private static void shutdownAudio(AtomicReference<AudioProcessor> audioProcessorRef) {
        AudioProcessor audioProcessor = audioProcessorRef.getAndSet(null);
        if (audioProcessor != null) {
            audioProcessor.shutdown();
        }
    }

    private static Mono<Void> closeSessionAsync(VoiceLiveSessionAsyncClient session,
        AtomicReference<VoiceLiveSessionAsyncClient> sessionRef) {
        return session.closeAsync()
            .timeout(Duration.ofSeconds(5))
            .doOnSuccess(ignored -> sessionRef.compareAndSet(session, null));
    }

    private static void closeSession(AtomicReference<VoiceLiveSessionAsyncClient> sessionRef) {
        VoiceLiveSessionAsyncClient session = sessionRef.getAndSet(null);
        if (session != null) {
            try {
                session.closeAsync().block(Duration.ofSeconds(5));
            } catch (Exception error) {
                System.err.println("❌ Error closing session: " + error.getMessage());
            }
        }
    }

    /**
     * Create session configuration for voice conversation
     */
    private static VoiceLiveSessionOptions createVoiceSessionOptions() {
        System.out.println("🔧 Creating session configuration:");

        // Create server VAD configuration similar to Python sample
        ServerVadTurnDetection turnDetection = new ServerVadTurnDetection()
            .setThreshold(0.5)
            .setPrefixPaddingMs(300)
            .setSilenceDurationMs(500)
            .setInterruptResponse(true)
            .setAutoTruncate(true)
            .setCreateResponse(true);

        // Create audio input transcription configuration
        AudioInputTranscriptionOptions transcriptionOptions = new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1).setLanguage("en");

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful AI voice assistant. Respond naturally and conversationally. Keep your responses concise but engaging. Speak as if having a real conversation.")
            // Voice: OpenAIVoice (OpenAIVoiceName enum) or AzureStandardVoice/AzureCustomVoice/AzurePersonalVoice
            .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AvaNeural")))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE)
            .setInputAudioNoiseReduction(new AudioNoiseReduction(AudioNoiseReductionType.NEAR_FIELD))
            .setInputAudioEchoCancellation(new AudioEchoCancellation())
            .setInputAudioTranscription(transcriptionOptions)
            .setTurnDetection(turnDetection);


        System.out.println("✓ Session configuration created");
        return options;
    }

    /**
     * Handle a single server event. Exceptions propagate through the receive stream.
     */
    static void handleServerEvent(SessionServerEvent event, AudioProcessor audioProcessor) {
        ServerEventType eventType = event.getType();

        if (eventType == ServerEventType.SESSION_CREATED) {
            System.out.println("✓ Session created - initializing...");
        } else if (event instanceof SessionUpdateSessionUpdated) {
            System.out.println("✓ Session updated - starting audio");

            // Print the full JSON representation
            SessionUpdateSessionUpdated sessionUpdated = (SessionUpdateSessionUpdated) event;
            System.out.println("📄 Session Updated Event (Full JSON):");
            System.out.println(BinaryData.fromObject(sessionUpdated).toString());

            audioProcessor.startPlayback();
            audioProcessor.startCapture();

            System.out.println("🎤 VOICE ASSISTANT READY");
            System.out.println("Start speaking to begin conversation");
            System.out.println("Press Ctrl+C to exit");
        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
            System.out.println("🎤 Speech detected");
            // Server handles interruption automatically with interruptResponse=true.
            // Preserve immediate queue clearing so pending assistant audio is not played.
            audioProcessor.skipPendingAudio();
        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
            System.out.println("🤔 Speech ended - processing...");
        } else if (event instanceof SessionUpdateResponseAudioDelta) {
            audioProcessor.queueAudio(((SessionUpdateResponseAudioDelta) event).getDelta());
        } else if (event instanceof SessionUpdateResponseAudioDone) {
            audioProcessor.printAudioSummary((SessionUpdateResponseAudioDone) event);
            System.out.println("🎤 Ready for next input...");
        } else if (event instanceof SessionUpdateResponseDone) {
            SessionResponse response = ((SessionUpdateResponseDone) event).getResponse();
            String responseId = response == null ? null : response.getId();
            SessionResponseStatus status = response == null ? null : response.getStatus();
            if (audioProcessor == null) {
                System.out.println("✅ Response complete: responseId=" + responseId + ", status=" + status);
            } else {
                audioProcessor.printResponseSummary(responseId, status);
            }
        } else if (event instanceof SessionUpdateError) {
            SessionUpdateError errorEvent = (SessionUpdateError) event;
            String message = errorEvent.getError() == null
                ? "Unknown VoiceLive error"
                : errorEvent.getError().getMessage();
            throw new IllegalStateException("VoiceLive error: " + message);
        }
    }
}

/**
 * Hardware-independent aggregate playback diagnostics used by {@link VoiceAssistantSample}.
 */
final class VoiceAssistantPlaybackDiagnostics {
    private final AtomicLong audioChunkCount = new AtomicLong(0);
    private final AtomicLong audioByteCount = new AtomicLong(0);
    private final AtomicLong droppedPacketCount = new AtomicLong(0);
    private final AtomicLong skippedPacketCount = new AtomicLong(0);
    private final AtomicLong skipOperationCount = new AtomicLong(0);
    private final AtomicInteger highWaterQueueDepth = new AtomicInteger(0);

    void recordAudioChunk(int byteCount, boolean accepted, int queueDepth) {
        audioChunkCount.incrementAndGet();
        audioByteCount.addAndGet(byteCount);
        if (!accepted) {
            droppedPacketCount.incrementAndGet();
        }
        recordQueueDepth(queueDepth);
    }

    void recordQueueDepth(int queueDepth) {
        int normalizedQueueDepth = Math.max(0, queueDepth);
        highWaterQueueDepth.updateAndGet(current -> Math.max(current, normalizedQueueDepth));
    }

    void recordSkippedPacket() {
        skippedPacketCount.incrementAndGet();
    }

    void recordSkip(int removed) {
        skippedPacketCount.addAndGet(removed);
        skipOperationCount.incrementAndGet();
    }

    long getAudioChunkCount() {
        return audioChunkCount.get();
    }

    long getAudioByteCount() {
        return audioByteCount.get();
    }

    long getDroppedPacketCount() {
        return droppedPacketCount.get();
    }

    long getSkippedPacketCount() {
        return skippedPacketCount.get();
    }

    long getSkipOperationCount() {
        return skipOperationCount.get();
    }

    int getHighWaterQueueDepth() {
        return highWaterQueueDepth.get();
    }
}
