// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Live tests for VoiceLive conversation item operations (retrieve, truncate).
 */
public class VoiceLiveConversationTests extends VoiceLiveTestBase {

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime" })
    @LiveOnly
    public void testRealtimeServiceRetrieveItem(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("largest_lake.wav");

        AtomicBoolean outputItemReceived = new AtomicBoolean(false);
        AtomicBoolean itemRetrieved = new AtomicBoolean(false);
        CountDownLatch outputItemLatch = new CountDownLatch(1);
        CountDownLatch retrieveLatch = new CountDownLatch(1);

        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful assistant.")
                    .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_OUTPUT_ITEM_DONE) {
                    outputItemReceived.set(true);
                    outputItemLatch.countDown();
                } else if (eventType == ServerEventType.CONVERSATION_ITEM_RETRIEVED) {
                    itemRetrieved.set(true);
                    retrieveLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    outputItemLatch.countDown();
                    retrieveLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                outputItemLatch.countDown();
                retrieveLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean outputReceived = outputItemLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(outputReceived, "Should receive output item");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime" })
    @LiveOnly
    public void testRealtimeServiceTruncateItem(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("largest_lake.wav");

        AtomicBoolean outputItemReceived = new AtomicBoolean(false);
        AtomicBoolean itemTruncated = new AtomicBoolean(false);
        CountDownLatch outputItemLatch = new CountDownLatch(1);
        CountDownLatch truncateLatch = new CountDownLatch(1);

        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful assistant.");

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_OUTPUT_ITEM_DONE) {
                    outputItemReceived.set(true);
                    outputItemLatch.countDown();
                } else if (eventType == ServerEventType.CONVERSATION_ITEM_TRUNCATED) {
                    itemTruncated.set(true);
                    truncateLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    outputItemLatch.countDown();
                    truncateLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                outputItemLatch.countDown();
                truncateLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);

            boolean outputReceived = outputItemLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(outputReceived, "Should receive output item");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }
}
