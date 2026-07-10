// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.live;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventResponseCancel;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputTextContentPart;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.MessageContentPart;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionResponseStatus;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseTextDelta;
import com.azure.ai.voicelive.models.UserMessageItem;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive response lifecycle: response creation from text input
 * and response cancellation.
 */
public class VoiceLiveResponseTests extends VoiceLiveTestBase {

    static Stream<Arguments> apiVersionParams() {
        return Arrays.stream(API_VERSIONS).map(Arguments::of);
    }

    static Stream<Arguments> realtimeModelParams() {
        return crossProduct(new String[] { MODEL_GPT_REALTIME }, API_VERSIONS);
    }

    // Drives the conversation purely via text input (no audio): create a user
    // message item with text content, request a text-only response, and assert
    // the server streams response.text events back.
    @ParameterizedTest
    @MethodSource("apiVersionParams")
    @LiveOnly
    public void testInputTextProducesTextResponse(String apiVersion) throws InterruptedException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        AtomicBoolean textDeltaReceived = new AtomicBoolean(false);
        StringBuilder responseText = new StringBuilder();
        CountDownLatch responseDoneLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful assistant. Answer concisely.")
                    .setModalities(Collections.singletonList(InteractionModality.TEXT));

            session = client.startSession(TEST_MODEL, null).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.RESPONSE_TEXT_DELTA) {
                    textDeltaReceived.set(true);
                    if (event instanceof SessionUpdateResponseTextDelta) {
                        String delta = ((SessionUpdateResponseTextDelta) event).getDelta();
                        if (delta != null) {
                            responseText.append(delta);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_DONE) {
                    responseDoneLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseDoneLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseDoneLatch.countDown();
            });

            waitForSetup();

            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);

            // Add a user text message to the conversation history.
            MessageContentPart textPart = new InputTextContentPart("What is the capital of France?");
            UserMessageItem userMessage = new UserMessageItem(Collections.singletonList(textPart));
            session.sendEvent(new ClientEventConversationItemCreate().setItem(userMessage)).block(SEND_TIMEOUT);

            // Ask the model to respond.
            session.sendEvent(new ClientEventResponseCreate()).block(SEND_TIMEOUT);

            boolean done = responseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(done, "Should receive response done event");
            Assertions.assertTrue(textDeltaReceived.get(), "Should receive response text delta events");
            Assertions.assertFalse(responseText.toString().trim().isEmpty(), "Response text should not be empty");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    // Start an audio response, then immediately cancel it. The response should
    // terminate with a cancelled (or otherwise non-completed) status.
    @ParameterizedTest
    @MethodSource("realtimeModelParams")
    @LiveOnly
    public void testResponseCancel(String model, String apiVersion) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicReference<SessionUpdateResponseDone> responseDone = new AtomicReference<>();
        CountDownLatch firstAudioLatch = new CountDownLatch(1);
        CountDownLatch responseDoneLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setInstructions("You are a helpful assistant. Speak a long, detailed answer.")
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO));

            session = client.startSession(model, null).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    firstAudioLatch.countDown();
                } else if (eventType == ServerEventType.RESPONSE_DONE) {
                    if (event instanceof SessionUpdateResponseDone) {
                        responseDone.set((SessionUpdateResponseDone) event);
                    }
                    responseDoneLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseDoneLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseDoneLatch.countDown();
            });

            waitForSetup();

            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);
            waitForSetup();

            session.sendInputAudio(BinaryData.fromBytes(audioData)).block(SEND_TIMEOUT);
            session.sendEvent(new ClientEventResponseCreate()).block(SEND_TIMEOUT);

            // Wait until audio output begins, then cancel the in-progress response
            // from the main thread (blocking inside the reactor callback is illegal).
            boolean audioStarted = firstAudioLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(audioStarted, "Should start receiving audio before cancelling");
            session.sendEvent(new ClientEventResponseCancel()).block(SEND_TIMEOUT);

            boolean done = responseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(done, "Should receive response done event");
            Assertions.assertNotNull(responseDone.get(), "Response done event should not be null");
            Assertions.assertNotNull(responseDone.get().getResponse(), "Response should not be null");
            SessionResponseStatus status = responseDone.get().getResponse().getStatus();
            Assertions.assertNotEquals(SessionResponseStatus.COMPLETED, status,
                "Cancelled response should not have completed status, got: " + status);
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }
}
