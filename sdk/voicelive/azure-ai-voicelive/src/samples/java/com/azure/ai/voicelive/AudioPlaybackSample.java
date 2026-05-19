// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InputTextContentPart;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.UserMessageItem;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample demonstrating how to receive and play audio responses from VoiceLive service.
 *
 * <p>Use this sample when you want to understand downstream audio playback only. It is a good next
 * step after the basic sample because it avoids microphone capture and focuses on speaker output.</p>
 *
 * <p>When you run it, the sample sends a fixed text prompt, asks the model to generate an audio
 * response, and plays the returned PCM audio through your default speaker or headphones.</p>
 *
 * <p>This sample shows how to:</p>
 * <ul>
 *   <li>Send a text message to trigger an audio response</li>
 *   <li>Subscribe to audio response events</li>
 *   <li>Receive audio data in chunks</li>
 *   <li>Play audio through speakers in real-time</li>
 *   <li>Handle audio playback threading</li>
 *   <li>Manage audio queue for smooth playback</li>
 * </ul>
 *
 * <p><strong>Related Samples:</strong></p>
 * <ul>
 *   <li>{@link BasicVoiceConversationSample} - Learn the basics first</li>
 *   <li>{@link AuthenticationMethodsSample} - Understand authentication options</li>
 *   <li>{@link MicrophoneInputSample} - Learn audio input (complement to this sample)</li>
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
 * Requires working speakers or headphones. Audio format is 24kHz, 16-bit PCM, mono.
 *
 * <p><strong>Note:</strong> This sample sends a text message to trigger an audio response.
 * You should hear the assistant speak through your speakers/headphones.</p>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.AudioPlaybackSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class AudioPlaybackSample {

    // Audio format constants required by VoiceLive
    private static final int SAMPLE_RATE = 24000;     // 24kHz
    private static final int CHANNELS = 1;            // Mono
    private static final int SAMPLE_SIZE_BITS = 16;   // 16-bit PCM
    private static final int CHUNK_SIZE = 1200;       // 50ms chunks
    private static final long COMPLETION_TIMEOUT_SECONDS = 60;

    /**
     * Main method to run the audio playback sample.
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

        // Check if speaker is available
        if (!checkSpeakerAvailable()) {
            System.err.println("No compatible speaker found");
            return;
        }

        // Create the VoiceLive client using DefaultAzureCredential (Entra ID).
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        System.out.println("Starting audio playback sample...");

        // Configure session options
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful assistant. Respond to user messages with clear, friendly audio.")
            // Voice options:
            // - OpenAI: new OpenAIVoice(OpenAIVoiceName.ALLOY) - use OpenAIVoiceName enum
            // - Azure: AzureStandardVoice, AzureCustomVoice, AzurePersonalVoice (all extend AzureVoice)
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE);

        // Audio playback components
        final BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>(1000);
        final AtomicBoolean isPlaying = new AtomicBoolean(false);
        final AtomicReference<SourceDataLine> speakerRef = new AtomicReference<>();
        final AtomicReference<Thread> playbackThreadRef = new AtomicReference<>();

        // Latch keeps main alive until the response completes (or an error occurs).
        final CountDownLatch completionLatch = new CountDownLatch(1);

        // Open a WebSocket session against the realtime model.
        client.startSession("gpt-realtime")
            // Configure the session (voice, modalities, audio formats, instructions).
            .flatMap(session -> {
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                return session.sendEvent(updateEvent).thenReturn(session);
            })
            // Open the speaker line and start the playback worker thread before
            // any audio deltas arrive, so chunks can be played as soon as they stream in.
            .flatMap(session -> {
                startPlayback(audioQueue, isPlaying, speakerRef, playbackThreadRef);
                return Mono.just(session);
            })
            // Send a user message that prompts the model to produce a spoken reply.
            .flatMap(session -> {
                InputTextContentPart textContent = new InputTextContentPart(
                    "Please say 'Hello! This is a test of the audio playback system.' in a friendly voice.");
                UserMessageItem messageItem = new UserMessageItem(Collections.singletonList(textContent));
                ClientEventConversationItemCreate createEvent = new ClientEventConversationItemCreate()
                    .setItem(messageItem);
                return session.sendEvent(createEvent).thenReturn(session);
            })
            // Ask the model to start generating a response for the queued message.
            .flatMap(session -> {
                ClientEventResponseCreate responseEvent = new ClientEventResponseCreate();
                return session.sendEvent(responseEvent).thenReturn(session);
            })
            // Subscribe to the server event stream (session.created, audio deltas, etc.).
            .flatMapMany(session -> session.receiveEvents())
            .subscribe(
                // onNext: route each server event (audio chunks go to the playback queue).
                event -> handleEvent(event, audioQueue, completionLatch),
                // onError: log and release main so it can clean up and exit.
                error -> {
                    System.err.println("Error: " + error.getMessage());
                    completionLatch.countDown();
                },
                // onComplete: stream ended cleanly; release main.
                completionLatch::countDown
            );

        try {
            if (!completionLatch.await(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Timed out waiting for audio response to complete.");
            } else {
                System.out.println("\n✓ Sample completed - audio playback demonstrated");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stopPlayback(audioQueue, isPlaying, speakerRef, playbackThreadRef);
        }
    }

    /**
     * Check if a compatible speaker is available.
     *
     * @return true if speaker is available, false otherwise
     */
    private static boolean checkSpeakerAvailable() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Start audio playback system.
     *
     * @param audioQueue Queue containing audio data to play
     * @param isPlaying Flag to control playback loop
     * @param speakerRef Reference to store the speaker line
     * @param playbackThreadRef Reference to store the playback thread
     */
    private static void startPlayback(BlockingQueue<byte[]> audioQueue, AtomicBoolean isPlaying,
        AtomicReference<SourceDataLine> speakerRef, AtomicReference<Thread> playbackThreadRef) {
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

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format, CHUNK_SIZE * 4);
            speaker.start();

            speakerRef.set(speaker);
            isPlaying.set(true);

            System.out.println("🔊 Audio playback started");

            // Start playback thread
            Thread playbackThread = new Thread(() -> {
                while (isPlaying.get()) {
                    try {
                        byte[] audioData = audioQueue.take(); // Blocking wait

                        if (audioData.length == 0) {
                            // Shutdown signal
                            break;
                        }

                        // Play the audio
                        if (speaker.isOpen()) {
                            speaker.write(audioData, 0, audioData.length);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        System.err.println("Error in audio playback: " + e.getMessage());
                    }
                }
            }, "AudioPlayback");
            playbackThread.setDaemon(true);
            playbackThreadRef.set(playbackThread);
            playbackThread.start();

        } catch (LineUnavailableException e) {
            System.err.println("Failed to start speaker: " + e.getMessage());
        }
    }

    /**
     * Stop audio playback.
     *
     * @param audioQueue Queue containing audio data
     * @param isPlaying Flag to control playback loop
     * @param speakerRef Reference to the speaker line to close
     * @param playbackThreadRef Reference to the playback thread
     */
    private static void stopPlayback(BlockingQueue<byte[]> audioQueue, AtomicBoolean isPlaying,
        AtomicReference<SourceDataLine> speakerRef, AtomicReference<Thread> playbackThreadRef) {
        isPlaying.set(false);
        audioQueue.offer(new byte[0]); // Shutdown signal

        Thread playbackThread = playbackThreadRef.getAndSet(null);
        if (playbackThread != null) {
            playbackThread.interrupt();
            try {
                playbackThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        SourceDataLine speaker = speakerRef.getAndSet(null);
        if (speaker != null) {
            speaker.stop();
            speaker.close();
        }
        System.out.println("🔊 Audio playback stopped");
    }

    /**
     * Handle incoming server events. Queues audio chunks for playback and signals completion
     * when the response is finished or an error is reported.
     *
     * @param event The server event
     * @param audioQueue Queue to receive audio data
     * @param completionLatch Latch to release when the response is complete or fails
     */
    private static void handleEvent(SessionUpdate event, BlockingQueue<byte[]> audioQueue,
        CountDownLatch completionLatch) {
        ServerEventType eventType = event.getType();

        if (eventType == ServerEventType.SESSION_CREATED) {
            System.out.println("✓ Session created");
        } else if (eventType == ServerEventType.SESSION_UPDATED) {
            System.out.println("✓ Session updated - ready to receive audio");
        } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
            // Receive audio response and queue for playback
            if (event instanceof SessionUpdateResponseAudioDelta) {
                SessionUpdateResponseAudioDelta audioEvent = (SessionUpdateResponseAudioDelta) event;
                byte[] audioData = audioEvent.getDelta();
                if (audioData != null && audioData.length > 0) {
                    if (!audioQueue.offer(audioData)) {
                        System.err.println("Warning: audio queue full, dropping chunk of " + audioData.length + " bytes");
                    } else {
                        System.out.println("🔊 Received audio chunk: " + audioData.length + " bytes");
                    }
                }
            }
        } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
            System.out.println("✓ Audio response complete");
        } else if (eventType == ServerEventType.RESPONSE_DONE) {
            System.out.println("✓ Response complete");
            completionLatch.countDown();
        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("❌ Error occurred in session "
                + ((SessionUpdateError) event).getError().getMessage());
            completionLatch.countDown();
        }
    }

    // Private constructor to prevent instantiation
    private AudioPlaybackSample() {
    }
}
