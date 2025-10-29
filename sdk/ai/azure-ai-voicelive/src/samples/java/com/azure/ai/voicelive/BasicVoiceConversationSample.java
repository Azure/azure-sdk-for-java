// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.AzureKeyCredential;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Basic voice conversation sample demonstrating minimal setup for VoiceLive service.
 *
 * <p><strong>Start here if you're new to VoiceLive!</strong> This is the simplest sample showing core concepts.</p>
 *
 * <p>This sample shows the simplest way to:</p>
 * <ul>
 *   <li>Create a VoiceLive client</li>
 *   <li>Start a session with basic configuration</li>
 *   <li>Subscribe to receive events</li>
 * </ul>
 *
 * <p><strong>Next Steps - Learn More:</strong></p>
 * <ul>
 *   <li>{@link AuthenticationMethodsSample} - Explore authentication options (API Key vs Token Credential)</li>
 *   <li>{@link MicrophoneInputSample} - Add real-time microphone audio input</li>
 *   <li>{@link AudioPlaybackSample} - Add audio response playback</li>
 *   <li>{@link VoiceAssistantSample} - See a complete production-ready voice assistant</li>
 * </ul>
 *
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The VoiceLive service endpoint URL</li>
 *   <li>AZURE_VOICELIVE_API_KEY - The API key for authentication</li>
 * </ul>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.BasicVoiceConversationSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class BasicVoiceConversationSample {

    /**
     * Main method to run the basic voice conversation sample.
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

        // Create the VoiceLive client
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        System.out.println("Starting basic voice conversation...");

        // Configure basic session options
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful AI assistant.")
            .setVoice(new OpenAIVoice(OpenAIVoiceName.ALLOY))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(24000);

        // Start session and subscribe to events
        client.startSession("gpt-4o-realtime-preview")
            .flatMap(session -> {
                System.out.println("✓ Session started");

                // Subscribe to receive events
                session.receiveEvents()
                    .subscribe(
                        event -> handleEvent(event),
                        error -> System.err.println("Error: " + error.getMessage()),
                        () -> System.out.println("Event stream completed")
                    );

                // Send session configuration
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                return session.sendEvent(updateEvent)
                    .doOnSuccess(v -> System.out.println("✓ Session configured"))
                    .then(Mono.never()); // Keep session alive
            })
            .block(); // Block for demo purposes
    }

    /**
     * Handle incoming server events.
     *
     * @param event The server event
     */
    private static void handleEvent(SessionUpdate event) {
        ServerEventType eventType = event.getType();
        System.out.println("Received event: " + eventType);

        if (eventType == ServerEventType.SESSION_CREATED) {
            System.out.println("  → Session created and ready");
        } else if (eventType == ServerEventType.SESSION_UPDATED) {
            System.out.println("  → Session configuration updated");
        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("  → Error occurred in session");
        }
    }

    // Private constructor to prevent instantiation
    private BasicVoiceConversationSample() {
    }
}
