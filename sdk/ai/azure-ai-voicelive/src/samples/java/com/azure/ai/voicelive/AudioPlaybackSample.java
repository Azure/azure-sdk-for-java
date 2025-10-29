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
import com.azure.core.credential.AzureKeyCredential;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample demonstrating how to receive and play audio responses from VoiceLive service.
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
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The VoiceLive service endpoint URL</li>
 *   <li>AZURE_VOICELIVE_API_KEY - The API key for authentication</li>
 * </ul>
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

    /**
     * Main method to run the audio playback sample.
     *
     * @param args Unused command line arguments
     */
    public static void main(String[] args) {
        // Get credentials from environment variables
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
        String apiKey = System.getenv("AZURE_VOICELIVE_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT and AZURE_VOICELIVE_API_KEY environment variables");
            return;
        }

        // Check if speaker is available
        if (!checkSpeakerAvailable()) {
            System.err.println("No compatible speaker found");
            return;
        }

        // Create the VoiceLive client
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        System.out.println("Starting audio playback sample...");

        // Configure session options
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful assistant. Respond to user messages with clear, friendly audio.")
            .setVoice(new OpenAIVoice(OpenAIVoiceName.ALLOY))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE);

        // Audio playback components
        final BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>();
        final AtomicBoolean isPlaying = new AtomicBoolean(false);
        final SourceDataLine[] speakerRef = new SourceDataLine[1];

        // Start session
        client.startSession("gpt-4o-realtime-preview")
            .flatMap(session -> {
                System.out.println("✓ Session started");

                // Subscribe to receive events
                session.receiveEvents()
                    .subscribe(
                        event -> handleEvent(event, audioQueue),
                        error -> System.err.println("Error: " + error.getMessage())
                    );

                // Send session configuration
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                return session.sendEvent(updateEvent)
                    .doOnSuccess(v -> {
                        System.out.println("✓ Session configured");
                        // Start audio playback system
                        startPlayback(audioQueue, isPlaying, speakerRef);
                    })
                    .then(Mono.delay(Duration.ofMillis(500))) // Wait for session to be fully ready
                    .flatMap(v -> {
                        // Send a user message to trigger an audio response
                        System.out.println("📤 Sending text message to trigger audio response...");
                        InputTextContentPart textContent = new InputTextContentPart(
                            "Please say 'Hello! This is a test of the audio playback system.' in a friendly voice.");
                        UserMessageItem messageItem = new UserMessageItem(Collections.singletonList(textContent));
                        ClientEventConversationItemCreate createEvent = new ClientEventConversationItemCreate()
                            .setItem(messageItem);

                        return session.sendEvent(createEvent);
                    })
                    .then(Mono.delay(Duration.ofMillis(100)))
                    .flatMap(v -> {
                        // Trigger response generation
                        System.out.println("🎯 Triggering response generation...");
                        ClientEventResponseCreate responseEvent = new ClientEventResponseCreate();
                        return session.sendEvent(responseEvent);
                    })
                    .then(Mono.delay(Duration.ofSeconds(10))) // Wait for audio response
                    .doFinally(signal -> System.out.println("\n✓ Sample completed - audio playback demonstrated"));
            })
            .doFinally(signalType -> {
                // Cleanup
                stopPlayback(audioQueue, isPlaying, speakerRef[0]);
            })
            .block(); // Block for demo purposes
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
     */
    private static void startPlayback(BlockingQueue<byte[]> audioQueue, AtomicBoolean isPlaying, SourceDataLine[] speakerRef) {
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

            speakerRef[0] = speaker;
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
     * @param speaker The speaker line to close
     */
    private static void stopPlayback(BlockingQueue<byte[]> audioQueue, AtomicBoolean isPlaying, SourceDataLine speaker) {
        isPlaying.set(false);
        audioQueue.offer(new byte[0]); // Shutdown signal
        if (speaker != null) {
            speaker.stop();
            speaker.close();
        }
        System.out.println("🔊 Audio playback stopped");
    }

    /**
     * Handle incoming server events.
     *
     * @param event The server event
     * @param audioQueue Queue to receive audio data
     */
    private static void handleEvent(SessionUpdate event, BlockingQueue<byte[]> audioQueue) {
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
                    audioQueue.offer(audioData);
                    System.out.println("🔊 Received audio chunk: " + audioData.length + " bytes");
                }
            }
        } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
            System.out.println("✓ Audio response complete");
        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("❌ Error occurred in session " + ((SessionUpdateError) event).getError().getMessage());
        }
    }

    // Private constructor to prevent instantiation
    private AudioPlaybackSample() {
    }
}
