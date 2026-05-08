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
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.time.Duration;
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
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The VoiceLive service endpoint URL</li>
 *   <li>AZURE_VOICELIVE_API_KEY - (Optional) The API key, if not using DefaultAzureCredential</li>
 * </ul>
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

        // Create the VoiceLive client using DefaultAzureCredential (recommended).
        // To use an API key instead:
        //   .credential(new KeyCredential(System.getenv("AZURE_VOICELIVE_API_KEY")))
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

        final AtomicBoolean isCapturing = new AtomicBoolean(false);
        final AtomicReference<TargetDataLine> microphoneRef = new AtomicReference<>();
        final AtomicReference<Thread> captureThreadRef = new AtomicReference<>();

        // Latch keeps main alive until the event stream completes (or an error occurs).
        final CountDownLatch completionLatch = new CountDownLatch(1);

        // Start session
        client.startSession("gpt-realtime")
            // Configure the session.
            .flatMap(session -> {
                System.out.println("✓ Session started");
                // Install Ctrl+C handler that stops capture, closes the session, and releases the latch.
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("\n👋 Shutting down...");
                    stopMicrophone(isCapturing, microphoneRef, captureThreadRef);
                    try {
                        session.closeAsync().block(Duration.ofSeconds(5));
                    } catch (Exception e) {
                        // Suppress errors during forced JVM shutdown
                    }
                    completionLatch.countDown();
                }));
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                return session.sendEvent(updateEvent).thenReturn(session);
            })
            // Start microphone capture once the session is configured.
            .flatMap(session -> {
                startMicrophone(session, isCapturing, microphoneRef, captureThreadRef);
                return Mono.just(session);
            })
            // Subscribe to the server event stream.
            .flatMapMany(session -> session.receiveEvents())
            .subscribe(
                event -> handleEvent(event, isCapturing),
                error -> {
                    System.err.println("Error: " + error.getMessage());
                    completionLatch.countDown();
                },
                completionLatch::countDown
            );

        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stopMicrophone(isCapturing, microphoneRef, captureThreadRef);
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
     * Start capturing audio from microphone.
     *
     * @param session The VoiceLive session
     * @param isCapturing Flag to control capture loop
     * @param microphoneRef Reference to store the microphone line
     * @param captureThreadRef Reference to store the capture thread
     */
    private static void startMicrophone(VoiceLiveSessionAsyncClient session, AtomicBoolean isCapturing,
        AtomicReference<TargetDataLine> microphoneRef, AtomicReference<Thread> captureThreadRef) {
        try {
            AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                SAMPLE_RATE,
                SAMPLE_SIZE_BITS,
                CHANNELS,
                CHANNELS * SAMPLE_SIZE_BITS / 8,
                SAMPLE_RATE,
                false
            );

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format, CHUNK_SIZE * 4);
            microphone.start();

            microphoneRef.set(microphone);
            isCapturing.set(true);

            System.out.println("🎤 Microphone started - speak now");
            System.out.println("Press Ctrl+C to stop");

            // Start capture thread
            Thread captureThread = new Thread(() -> {
                byte[] buffer = new byte[CHUNK_SIZE * 2]; // 16-bit samples

                while (isCapturing.get()) {
                    try {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            // Send audio to VoiceLive service. sendInputAudio returns a cold
                            // Mono - it must be subscribed for the audio to actually be sent.
                            byte[] audioChunk = Arrays.copyOf(buffer, bytesRead);
                            session.sendInputAudio(BinaryData.fromBytes(audioChunk))
                                .subscribe(
                                    v -> { },
                                    error -> System.err.println("Error sending audio: " + error.getMessage())
                                );
                        }
                    } catch (Exception e) {
                        if (isCapturing.get()) {
                            System.err.println("Error capturing audio: " + e.getMessage());
                        }
                        break;
                    }
                }
            }, "MicrophoneCapture");
            captureThread.setDaemon(true);
            captureThreadRef.set(captureThread);
            captureThread.start();

        } catch (LineUnavailableException e) {
            System.err.println("Failed to start microphone: " + e.getMessage());
        }
    }

    /**
     * Stop microphone capture.
     *
     * @param isCapturing Flag to control capture loop
     * @param microphoneRef Reference to the microphone line to close
     * @param captureThreadRef Reference to the capture thread
     */
    private static void stopMicrophone(AtomicBoolean isCapturing, AtomicReference<TargetDataLine> microphoneRef,
        AtomicReference<Thread> captureThreadRef) {
        isCapturing.set(false);

        Thread captureThread = captureThreadRef.getAndSet(null);
        if (captureThread != null) {
            captureThread.interrupt();
            try {
                captureThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        TargetDataLine microphone = microphoneRef.getAndSet(null);
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
        System.out.println("🎤 Microphone stopped");
    }

    /**
     * Handle incoming server events.
     *
     * @param event The server event
     * @param isCapturing Flag indicating if capture is active
     */
    private static void handleEvent(SessionUpdate event, AtomicBoolean isCapturing) {
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
}
