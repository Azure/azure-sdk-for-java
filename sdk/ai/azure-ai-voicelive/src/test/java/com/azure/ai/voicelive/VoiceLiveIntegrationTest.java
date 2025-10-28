// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for VoiceLive SDK.
 * These tests require actual Azure VoiceLive service credentials.
 */
class VoiceLiveIntegrationCorrectTest extends TestBase {

    private VoiceLiveAsyncClient client;
    private String endpoint;
    private String apiKey;

    @BeforeEach
    void setUp() {
        // Get credentials from environment variables or test configuration
        endpoint = System.getenv("VOICE_LIVE_ENDPOINT");
        apiKey = System.getenv("VOICE_LIVE_API_KEY");

        if (getTestMode() == TestMode.PLAYBACK) {
            // Use test values for playback mode
            endpoint = "https://test.cognitiveservices.azure.com";
            apiKey = "test-api-key";
        }

        if (endpoint != null && apiKey != null) {
            client = new VoiceLiveClientBuilder().endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey))
                .buildAsyncClient();
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "VOICE_LIVE_ENDPOINT", matches = ".*")
    void testCreateSession() {
        assumeTrue(client != null, "VoiceLive credentials not provided");

        // Arrange
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInstructions("You are a test assistant. Respond with 'Hello from VoiceLive SDK test!'")
            .setVoice(new OpenAIVoice(OpenAIVoiceName.ALLOY))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO));

        // Act & Assert
        StepVerifier.create(client.startSession(options.getModel())).assertNext(session -> {
            assertNotNull(session);
            // Additional session validation can be added here
        }).verifyComplete();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "VOICE_LIVE_ENDPOINT", matches = ".*")
    void testSessionWithServerVAD() {
        assumeTrue(client != null, "VoiceLive credentials not provided");

        // Arrange
        ServerVadTurnDetection turnDetection = new ServerVadTurnDetection();
        turnDetection.setThreshold(0.5);
        turnDetection.setPrefixPaddingMs(300);
        turnDetection.setSilenceDurationMs(500);

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInstructions("You are a test assistant.")
            .setVoice(new OpenAIVoice(OpenAIVoiceName.ECHO))
            .setTurnDetection(turnDetection)
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO));

        // Act & Assert
        StepVerifier.create(client.startSession(options.getModel())).assertNext(session -> {
            assertNotNull(session);
            // Test that we can receive events
            AtomicBoolean receivedEvent = new AtomicBoolean(false);
            session.receiveEvents()
                .take(1) // Take only the first event to avoid infinite stream
                .subscribe(event -> {
                    assertNotNull(event);
                    receivedEvent.set(true);
                });

            // Give it a moment to receive events
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // In a real test, we'd verify specific events were received
        }).verifyComplete();
    }

    @Test
    void testInvalidCredentials() {
        // Arrange
        VoiceLiveAsyncClient invalidClient
            = new VoiceLiveClientBuilder().endpoint("https://invalid.cognitiveservices.azure.com")
                .credential(new AzureKeyCredential("invalid-key"))
                .buildAsyncClient();

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview");

        // Act & Assert
        if (getTestMode() == TestMode.LIVE) {
            StepVerifier.create(invalidClient.startSession(options.getModel()))
                .expectErrorMatches(throwable -> throwable.getMessage().contains("401")
                    || throwable.getMessage().contains("Unauthorized")
                    || throwable.getMessage().contains("Invalid"))
                .verify(Duration.ofSeconds(30));
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "VOICE_LIVE_ENDPOINT", matches = ".*")
    void testSessionOptionsValidation() {
        assumeTrue(client != null, "VoiceLive credentials not provided");

        // Test various configuration options
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInstructions("Test instructions")
            .setVoice(new OpenAIVoice(OpenAIVoiceName.SHIMMER))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(24000)
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO));

        // Act & Assert
        StepVerifier.create(client.startSession(options.getModel())).assertNext(session -> {
            assertNotNull(session);
        }).verifyComplete();
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "VOICE_LIVE_ENDPOINT", matches = ".*")
    void testEventHandling() {
        assumeTrue(client != null, "VoiceLive credentials not provided");

        // Arrange
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInstructions("You are a test assistant. Say hello when the session starts.")
            .setVoice(new OpenAIVoice(OpenAIVoiceName.ALLOY))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO));

        AtomicReference<String> firstEventType = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client.startSession(options.getModel())).assertNext(session -> {
            assertNotNull(session);

            // Subscribe to events and capture the first one
            session.receiveEvents().take(1).subscribe(event -> {
                String eventString = event.toString();
                if (eventString.contains("\"type\":")) {
                    // Extract event type for verification
                    firstEventType.set(eventString);
                }
            });
        }).verifyComplete();

        // Give time for event processing
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify we received some event (in live mode)
        if (getTestMode() == TestMode.LIVE) {
            assertNotNull(firstEventType.get(), "Should have received at least one event");
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "VOICE_LIVE_ENDPOINT", matches = ".*")
    void testWithAzureVoice() {
        assumeTrue(client != null, "VoiceLive credentials not provided");

        // Arrange - Test with Azure voice instead of OpenAI
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview")
            .setInstructions("You are a test assistant using Azure voice.")
            .setVoice(new AzureStandardVoice("en-US-JennyNeural"))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO));

        // Act & Assert
        StepVerifier.create(client.startSession(options.getModel())).assertNext(session -> {
            assertNotNull(session);
        }).verifyComplete();
    }

    // Helper method to check test mode
    private void assumeTrue(boolean condition, String message) {
        if (getTestMode() == TestMode.LIVE) {
            assertTrue(condition, message);
        }
        // In PLAYBACK mode, we assume the condition is met
    }
}
