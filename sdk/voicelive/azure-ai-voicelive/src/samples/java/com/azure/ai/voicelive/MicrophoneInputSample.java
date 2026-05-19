// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ResponseTextContentPart;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionResponseMessageItem;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample demonstrating how to capture audio from microphone and send it to VoiceLive service.
 *
 * <p>Use this sample when you want to validate microphone capture and upstream audio streaming
 * without the extra moving parts of local speaker playback or function/tool integration.</p>
 *
 * <p>When you run it, the sample opens a realtime session, starts reading PCM audio from your
 * default microphone, streams that audio to the service, and prints speech / response events so
 * you can confirm the service is receiving your input.</p>
 *
 * <p>This sample shows how to:</p>
 * <ul>
 *   <li>Initialize microphone for audio capture</li>
 *   <li>Read audio data from microphone in real-time</li>
 *   <li>Send captured audio to VoiceLive service</li>
 *   <li>Handle audio format requirements (24kHz, 16-bit PCM, mono)</li>
 *   <li>Track speech detection and response completion</li>
 * </ul>
 *
 * <p><strong>Related Samples:</strong></p>
 * <ul>
 *   <li>{@link BasicVoiceConversationSample} - Learn the basics first</li>
 *   <li>{@link AuthenticationMethodsSample} - Understand authentication options</li>
 *   <li>{@link AudioPlaybackSample} - Add audio output (next step after input)</li>
 *   <li>{@link VoiceAssistantSample} - Complete voice assistant combining input and output</li>
 * </ul>
 *
 * <p><strong>Environment Variables:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - (Required) The VoiceLive service endpoint URL</li>
 * </ul>
 *
 * <p>This sample uses {@link DefaultAzureCredentialBuilder} (Entra ID, recommended). For an example
 * of API key authentication, see {@link AuthenticationMethodsSample}.</p>
 *
 * <p><strong>Audio Requirements:</strong></p>
 * Requires a working microphone. Audio format is 24kHz, 16-bit PCM, mono.
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.MicrophoneInputSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class MicrophoneInputSample {

    // Audio format constants required by VoiceLive
    private static final int SAMPLE_RATE = 24000;     // 24kHz
    private static final int CHANNELS = 1;            // Mono
    private static final int SAMPLE_SIZE_BITS = 16;   // 16-bit PCM
    private static final int CHUNK_SIZE = 1200;       // 50ms chunks

    /**
     * Main method to run the microphone input sample.
     *
     * @param args Unused command line arguments
     */
    public static void main(String[] args) {
        // Get endpoint from environment variable
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");

        if (endpoint == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT environment variable");
            return;
        }

        // Check if microphone is available
        if (!checkMicrophoneAvailable()) {
            System.err.println("No compatible microphone found");
            return;
        }

        // Create the VoiceLive client using DefaultAzureCredential (Entra ID).
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        System.out.println("Starting microphone input sample...");

        // Configure session options for audio input
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful AI assistant that responds to voice input.")
            // Voice: OpenAIVoice (use OpenAIVoiceName enum) or AzureStandardVoice/AzureCustomVoice/AzurePersonalVoice
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE);

        final AtomicReference<AudioProcessor> audioProcessorRef = new AtomicReference<>();

        // Latch keeps main alive until the event stream completes (or an error occurs).
        final CountDownLatch completionLatch = new CountDownLatch(1);

        // Start session. Session lifetime is local to this reactive chain; the session
        // instance is captured only inside the flatMapMany lambda.
        client.startSession("gpt-realtime")
            .flatMapMany(session -> {
                System.out.println("✓ Session started");
                AudioProcessor audioProcessor = new AudioProcessor(session);
                audioProcessorRef.set(audioProcessor);
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                return session.sendEvent(updateEvent)
                    .then(Mono.fromRunnable(audioProcessor::startCapture))
                    .thenReturn(session)
                    .flatMapMany(VoiceLiveSessionAsyncClient::receiveEvents);
            })
            .subscribe(
                event -> handleEvent(event),
                error -> {
                    System.err.println("Error: " + error.getMessage());
                    shutdownAudio(audioProcessorRef);
                    completionLatch.countDown();
                },
                () -> {
                    shutdownAudio(audioProcessorRef);
                    completionLatch.countDown();
                }
            );

        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stop microphone capture. Safe to call from both the onError and onComplete handlers
     * (idempotent via {@link AtomicReference#getAndSet(Object)}).
     */
    private static void shutdownAudio(AtomicReference<AudioProcessor> audioProcessorRef) {
        AudioProcessor audioProcessor = audioProcessorRef.getAndSet(null);
        if (audioProcessor != null) {
            audioProcessor.shutdown();
        }
    }

    /**
     * Check if a compatible microphone is available.
     *
     * @return true if microphone is available, false otherwise
     */
    private static boolean checkMicrophoneAvailable() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Handle incoming server events.
     *
     * @param event The server event
     */
    private static void handleEvent(SessionUpdate event) {
        ServerEventType eventType = event.getType();

        if (eventType == ServerEventType.SESSION_CREATED) {
            System.out.println("✓ Session created");
        } else if (eventType == ServerEventType.SESSION_UPDATED) {
            System.out.println("✓ Session updated - ready for audio input");
        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
            System.out.println("🎤 Speech detected");
        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
            System.out.println("🤔 Speech ended - processing...");
        } else if (eventType == ServerEventType.RESPONSE_DONE) {
            SessionUpdateResponseDone responseDone = (SessionUpdateResponseDone) event;
            String status = responseDone.getResponse().getStatus().toString();

            // Try to get text output if available
            String outputText = responseDone.getResponse().getOutput().stream()
                .filter(item -> item instanceof SessionResponseMessageItem)
                .map(item -> (SessionResponseMessageItem) item)
                .flatMap(messageItem -> messageItem.getContent().stream())
                .filter(content -> content instanceof ResponseTextContentPart)
                .map(content -> ((ResponseTextContentPart) content).getText())
                .findFirst()
                .orElse(null);

            if (outputText != null && !outputText.isEmpty()) {
                System.out.println("✓ Response complete (status: " + status + ")");
                System.out.println("  Text: \"" + outputText + "\"");
            } else {
                System.out.println("✓ Response complete (status: " + status + ") - audio only response");
            }
        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("❌ Error occurred in session");
        }
    }

    // Private constructor to prevent instantiation
    private MicrophoneInputSample() {
    }

    /**
     * Handles real-time microphone capture for the sample. Mirrors the {@code AudioProcessor}
     * pattern used by the other streaming samples (e.g. {@link VoiceAssistantSample}), but
     * captures only — this sample doesn't play any audio back through speakers.
     */
    private static class AudioProcessor {
        private final VoiceLiveSessionAsyncClient session;
        private final AudioFormat audioFormat;

        // Audio capture components
        // volatile: written by reactor thread (startCapture), read/closed by cleanup thread
        private volatile TargetDataLine microphone;
        private final AtomicBoolean isCapturing = new AtomicBoolean(false);

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
                microphone.open(audioFormat, CHUNK_SIZE * 4);
                microphone.start();

                isCapturing.set(true);

                // Start capture thread
                Thread captureThread = new Thread(this::captureAudioLoop, "VoiceLive-AudioCapture");
                captureThread.setDaemon(true);
                captureThread.start();

                System.out.println("🎤 Microphone capture started - speak now");
                System.out.println("Press Ctrl+C to stop");

            } catch (LineUnavailableException e) {
                System.err.println("❌ Failed to start microphone: " + e.getMessage());
                throw new RuntimeException("Failed to initialize microphone", e);
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
                        // Send audio to VoiceLive service. sendInputAudio returns a cold
                        // Mono - it must be subscribed for the audio to actually be sent.
                        byte[] audioChunk = Arrays.copyOf(buffer, bytesRead);
                        session.sendInputAudio(BinaryData.fromBytes(audioChunk))
                            .subscribe(
                                noValueEmitted -> { /* sendInputAudio returns Mono<Void>; no onNext values are ever emitted */ },
                                error -> {
                                    if (!error.getMessage().contains("cancelled")) {
                                        System.err.println("❌ Error sending audio: " + error.getMessage());
                                    }
                                }
                            );
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
         * Stop capture
         */
        void shutdown() {
            isCapturing.set(false);
            if (microphone != null) {
                microphone.stop();
                microphone.close();
                microphone = null;
            }
            System.out.println("🎤 Microphone capture stopped");
        }
    }
}
