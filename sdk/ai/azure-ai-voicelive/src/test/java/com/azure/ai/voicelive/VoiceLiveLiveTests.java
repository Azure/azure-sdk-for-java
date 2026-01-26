// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.KeyCredential;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Live tests for VoiceLive client.
 *
 * <p>These tests require the following environment variables to be set:</p>
 * <ul>
 *   <li>AI_SERVICES_ENDPOINT - The VoiceLive service endpoint URL</li>
 *   <li>AI_SERVICES_KEY - The API key for authentication</li>
 * </ul>
 */
public class VoiceLiveLiveTests extends TestProxyTestBase {

    /**
     * Tests that a session update event is received after sending a session.update client event.
     *
     * <p>This test verifies the basic session lifecycle:</p>
     * <ol>
     *   <li>Connect to the VoiceLive service</li>
     *   <li>Start a session</li>
     *   <li>Send a session.update event with configuration</li>
     *   <li>Verify that a session.updated event is received from the server</li>
     * </ol>
     */
    @Test
    @LiveOnly
    public void testSessionUpdateEventIsReceived() throws InterruptedException {
        // Get credentials from environment variables
        String endpoint = Configuration.getGlobalConfiguration().get("AI_SERVICES_ENDPOINT");
        String apiKey = Configuration.getGlobalConfiguration().get("AI_SERVICES_KEY");

        Assertions.assertNotNull(endpoint, "AI_SERVICES_ENDPOINT environment variable must be set");
        Assertions.assertNotNull(apiKey, "AI_SERVICES_KEY environment variable must be set");

        // Create the VoiceLive client
        VoiceLiveAsyncClient client
            = new VoiceLiveClientBuilder().endpoint(endpoint).credential(new KeyCredential(apiKey)).buildAsyncClient();

        // Track whether we received the session.updated event
        AtomicBoolean sessionUpdatedReceived = new AtomicBoolean(false);
        AtomicReference<SessionUpdateSessionUpdated> receivedEvent = new AtomicReference<>();
        CountDownLatch eventLatch = new CountDownLatch(1);

        try {
            // Configure session options
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful AI assistant for testing.")
                    .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                    .setInputAudioFormat(InputAudioFormat.PCM16);

            // Start session and subscribe to events
            VoiceLiveSessionAsyncClient session
                = client.startSession("gpt-4o-realtime-preview").block(Duration.ofSeconds(30));

            Assertions.assertNotNull(session, "Session should be created successfully");
            Assertions.assertTrue(session.isConnected(), "Session should be connected");

            // Subscribe to receive events
            session.receiveEvents()
                .subscribe(event -> handleEvent(event, sessionUpdatedReceived, receivedEvent, eventLatch), error -> {
                    System.err.println("Error receiving events: " + error.getMessage());
                    eventLatch.countDown();
                });

            // Send session configuration update
            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(Duration.ofSeconds(10));

            // Wait for the session.updated event (with timeout)
            boolean received = eventLatch.await(30, TimeUnit.SECONDS);

            // Verify we received the session.updated event
            Assertions.assertTrue(received, "Should receive an event within timeout");
            Assertions.assertTrue(sessionUpdatedReceived.get(), "Should receive session.updated event");
            Assertions.assertNotNull(receivedEvent.get(), "Received event should not be null");
            Assertions.assertEquals(ServerEventType.SESSION_UPDATED, receivedEvent.get().getType(),
                "Event type should be SESSION_UPDATED");

            // Close the session
            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    /**
     * Handle incoming server events.
     *
     * @param event The server event
     * @param sessionUpdatedReceived Flag to track if session.updated was received
     * @param receivedEvent Reference to store the received event
     * @param latch Countdown latch to signal event receipt
     */
    private void handleEvent(SessionUpdate event, AtomicBoolean sessionUpdatedReceived,
        AtomicReference<SessionUpdateSessionUpdated> receivedEvent, CountDownLatch latch) {
        ServerEventType eventType = event.getType();
        System.out.println("Received event: " + eventType);

        if (eventType == ServerEventType.SESSION_UPDATED) {
            System.out.println("  → Session configuration updated");
            sessionUpdatedReceived.set(true);
            if (event instanceof SessionUpdateSessionUpdated) {
                receivedEvent.set((SessionUpdateSessionUpdated) event);
            }
            latch.countDown();
        } else if (eventType == ServerEventType.SESSION_CREATED) {
            System.out.println("  → Session created");
        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("  → Error occurred in session");
            latch.countDown();
        }
    }
}
