// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Live tests for VoiceLive session management.
 */
public class VoiceLiveSessionTests extends VoiceLiveTestBase {

    @Test
    @LiveOnly
    public void testSessionUpdateEventIsReceived() throws InterruptedException {
        VoiceLiveAsyncClient client = createClient();

        AtomicBoolean sessionUpdatedReceived = new AtomicBoolean(false);
        AtomicReference<SessionUpdateSessionUpdated> receivedEvent = new AtomicReference<>();
        CountDownLatch eventLatch = new CountDownLatch(1);
        VoiceLiveSessionAsyncClient session = null;

        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful AI assistant for testing.")
                    .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                    .setInputAudioFormat(InputAudioFormat.PCM16);

            session = client.startSession(TEST_MODEL).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");
            Assertions.assertTrue(session.isConnected(), "Session should be connected");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.SESSION_UPDATED) {
                    sessionUpdatedReceived.set(true);
                    if (event instanceof SessionUpdateSessionUpdated) {
                        receivedEvent.set((SessionUpdateSessionUpdated) event);
                    }
                    eventLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    eventLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                eventLatch.countDown();
            });

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            boolean received = eventLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive an event within timeout");
            Assertions.assertTrue(sessionUpdatedReceived.get(), "Should receive session.updated event");
            Assertions.assertNotNull(receivedEvent.get(), "Received event should not be null");
            Assertions.assertEquals(ServerEventType.SESSION_UPDATED, receivedEvent.get().getType());
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        } finally {
            closeSession(session);
        }
    }
}
