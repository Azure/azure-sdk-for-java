// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Sample demonstrating basic usage of the VoiceLive SDK for real-time voice communication.
 */
public class BasicVoiceLiveSample {

    /**
     * Main method to run the sample.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // NOTE: Replace with your actual endpoint and API key
        String endpoint = System.getenv("VOICE_LIVE_ENDPOINT");
        String apiKey = System.getenv("VOICE_LIVE_API_KEY");

        if (endpoint == null || apiKey == null) {
            System.err.println("Please set VOICE_LIVE_ENDPOINT and VOICE_LIVE_API_KEY environment variables");
            return;
        }

        // Create the VoiceLive client
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();

        // Configure session options with OpenAI voice
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setModel("gpt-4o-realtime-preview")
            .setInstructions("You are a helpful AI assistant. Respond concisely and clearly.")
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO));

        // Start a session
        client.startSession(sessionOptions)
            .flatMap(session -> {
                System.out.println("Session started successfully");

                // Subscribe to receive updates
                session.receiveUpdates()
                    .subscribe(
                        binaryData -> handleServerEvent(binaryData),
                        error -> System.err.println("Error receiving updates: " + error.getMessage()),
                        () -> System.out.println("Receive stream completed")
                    );

                // Example: Send a text message via input audio buffer
                // In a real scenario, you would send actual audio data
                return sendTextAsAudio(session, "Hello, how are you today?")
                    .then(Mono.just(session));
            })
            .doOnError(error -> System.err.println("Error: " + error.getMessage()))
            .block(); // Block for demo purposes; in production use reactive patterns

        // Keep the program running to receive events
        try {
            Thread.sleep(30000); // Wait for 30 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Handles incoming server events.
     *
     * @param binaryData The binary data from the server.
     */
    private static void handleServerEvent(BinaryData binaryData) {
        try {
            // In a real implementation, you would deserialize to specific event types
            String eventJson = binaryData.toString();
            System.out.println("Received event: " + eventJson);

            // Example of handling specific event types
            // Note: You would need proper JSON deserialization here
            if (eventJson.contains("\"type\":\"session.created\"")) {
                System.out.println("✓ Session created");
            } else if (eventJson.contains("\"type\":\"response.text.delta\"")) {
                System.out.println("📝 Received text delta");
            } else if (eventJson.contains("\"type\":\"response.audio.delta\"")) {
                System.out.println("🔊 Received audio delta");
            }
        } catch (Exception e) {
            System.err.println("Error handling event: " + e.getMessage());
        }
    }

    /**
     * Example method to send text as if it were audio input.
     * In a real scenario, you would read actual audio data from a file or microphone.
     *
     * @param session The VoiceLive session.
     * @param text The text to send (simulated).
     * @return A Mono that completes when sending is done.
     */
    private static Mono<Void> sendTextAsAudio(VoiceLiveSession session, String text) {
        System.out.println("Sending message: " + text);

        // In a real implementation, you would:
        // 1. Read audio data from a microphone or file
        // 2. Send it in chunks using session.sendInputAudio()
        // 3. Commit the audio buffer

        // For this example, we'll just trigger a response
        ClientEventResponseCreate responseCreate = new ClientEventResponseCreate();
        return session.sendCommand(responseCreate);
    }

    /**
     * Example method to send actual audio from a file.
     *
     * @param session The VoiceLive session.
     * @param audioFilePath Path to the audio file.
     * @return A Mono that completes when audio is sent.
     */
    private static Mono<Void> sendAudioFromFile(VoiceLiveSession session, String audioFilePath) {
        try {
            InputStream audioStream = new FileInputStream(audioFilePath);
            return session.sendInputAudio(audioStream)
                .doOnSuccess(v -> System.out.println("Audio sent successfully"))
                .doOnError(error -> System.err.println("Error sending audio: " + error.getMessage()));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /**
     * Example method to handle different types of server events.
     *
     * @param session The VoiceLive session.
     */
    private static void subscribeToEvents(VoiceLiveSession session) {
        session.receiveUpdates()
            .subscribe(
                binaryData -> {
                    // Handle different event types
                    String json = binaryData.toString();

                    if (json.contains("\"type\":\"session.created\"")) {
                        System.out.println("Session initialized");
                    } else if (json.contains("\"type\":\"response.text.delta\"")) {
                        // Extract and print text delta
                        System.out.println("Text response received");
                    } else if (json.contains("\"type\":\"response.audio.delta\"")) {
                        // Process audio delta
                        System.out.println("Audio response received");
                    } else if (json.contains("\"type\":\"error\"")) {
                        System.err.println("Error event received: " + json);
                    }
                },
                error -> System.err.println("Stream error: " + error),
                () -> System.out.println("Stream completed")
            );
    }
}
