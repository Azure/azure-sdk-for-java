// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.MaxOutputTokens;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.AzureKeyCredential;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Sample demonstrating basic usage of the VoiceLive SDK for real-time voice communication.
 *
 * <p>This sample shows how to:</p>
 * <ul>
 *   <li>Create a VoiceLive client</li>
 *   <li>Configure session options with type-safe classes</li>
 *   <li>Start a session and receive events</li>
 *   <li>Send audio data and client events</li>
 * </ul>
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
            System.err.println("Example:");
            System.err.println("  export VOICE_LIVE_ENDPOINT=wss://your-resource.cognitiveservices.azure.com/");
            System.err.println("  export VOICE_LIVE_API_KEY=your-api-key");
            return;
        }

        // Create the VoiceLive client
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        // Configure session options with type-safe classes
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setModel("gpt-4o-realtime-preview")
            .setInstructions("You are a helpful AI assistant. Respond concisely and clearly.")
            .setVoice(new OpenAIVoice(OpenAIVoiceName.ALLOY))  // Type-safe voice configuration
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setMaxResponseOutputTokens(MaxOutputTokens.of(1000));  // Type-safe token limit

        // Start a session
        client.startSession(sessionOptions)
            .flatMap(session -> {
                System.out.println("✓ Session started successfully");

                // Subscribe to receive server events
                session.receiveEvents()
                    .subscribe(
                        sessionUpdate -> handleServerEvent(sessionUpdate),
                        error -> System.err.println("❌ Error receiving updates: " + error.getMessage()),
                        () -> System.out.println("✓ Receive stream completed")
                    );

                // Example: Send a client event to create a response
                // In a real scenario, you would send actual audio data first
                return sendResponseRequest(session)
                    .then(Mono.just(session));
            })
            .doOnError(error -> System.err.println("❌ Error: " + error.getMessage()))
            .block(); // Block for demo purposes; in production use reactive patterns

        // Keep the program running to receive events
        try {
            System.out.println("⏳ Waiting for events (30 seconds)...");
            Thread.sleep(30000); // Wait for 30 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("✓ Sample completed");
    }

    /**
     * Handles incoming server events using type-safe SessionUpdate objects.
     *
     * @param sessionUpdate The session update event from the server.
     */
    private static void handleServerEvent(SessionUpdate sessionUpdate) {
        try {
            // SessionUpdate is a discriminated union - check the type
            System.out.println("📨 Received event: " + sessionUpdate.getClass().getSimpleName());

            // In a real implementation, you would use pattern matching or instanceof checks
            // to handle specific event types and extract their data

            // Example: Print the event for demonstration
            System.out.println("   Event details: " + sessionUpdate.toString());

        } catch (Exception e) {
            System.err.println("❌ Error handling event: " + e.getMessage());
        }
    }

    /**
     * Sends a response creation request to trigger the AI model to respond.
     *
     * @param session The VoiceLive session.
     * @return A Mono that completes when the event is sent.
     */
    private static Mono<Void> sendResponseRequest(VoiceLiveSession session) {
        System.out.println("📤 Sending response creation request...");

        // Create a response creation event
        ClientEventResponseCreate responseCreate = new ClientEventResponseCreate();

        // You can configure the response parameters here if needed
        // For example:
        // responseCreate.setResponse(new ResponseCreateParams()
        //     .setMaxOutputTokens(MaxOutputTokens.of(500))
        //     .setInstructions("Be very concise"));

        return session.sendEvent(responseCreate)
            .doOnSuccess(v -> System.out.println("✓ Response request sent"))
            .doOnError(error -> System.err.println("❌ Error sending request: " + error.getMessage()));
    }

    /**
     * Example method to send actual audio from a file.
     * The audio should be in PCM16 format at 24kHz sample rate.
     *
     * @param session The VoiceLive session.
     * @param audioFilePath Path to the audio file (PCM16, 24kHz).
     * @return A Mono that completes when audio is sent.
     */
    private static Mono<Void> sendAudioFromFile(VoiceLiveSession session, String audioFilePath) {
        try {
            System.out.println("🎤 Sending audio from file: " + audioFilePath);
            InputStream audioStream = new FileInputStream(audioFilePath);

            return session.sendAudio(audioStream)
                .doOnSuccess(v -> System.out.println("✓ Audio sent successfully"))
                .doOnError(error -> System.err.println("❌ Error sending audio: " + error.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Error reading audio file: " + e.getMessage());
            return Mono.error(e);
        }
    }

    /**
     * Example method demonstrating how to send different types of client events.
     *
     * @param session The VoiceLive session.
     */
    private static void sendVariousClientEvents(VoiceLiveSession session) {
        // Example 1: Create a response with specific parameters
        ClientEventResponseCreate createResponse = new ClientEventResponseCreate();
        session.sendEvent(createResponse).subscribe();

        // Example 2: Send other client event types
        // You can create and send various ClientEvent subtypes:
        // - ClientEventInputAudioBufferAppend
        // - ClientEventInputAudioBufferClear
        // - ClientEventInputAudioBufferCommit
        // - ClientEventResponseCancel
        // - ClientEventSessionUpdate
        // etc.
    }

    /**
     * Example method showing type-safe usage of updated models.
     */
    private static void demonstrateTypeSafeModels() {
        // Type-safe voice configuration
        OpenAIVoice openAIVoice = new OpenAIVoice(OpenAIVoiceName.ECHO);

        // Type-safe token limits
        MaxOutputTokens limited = MaxOutputTokens.of(2048);
        MaxOutputTokens unlimited = MaxOutputTokens.infinite();

        // Type-safe session options
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
            .setModel("gpt-4o-realtime-preview")
            .setVoice(openAIVoice)
            .setMaxResponseOutputTokens(limited)
            .setTemperature(0.8);

        System.out.println("Configuration created with type-safe models");
    }
}
