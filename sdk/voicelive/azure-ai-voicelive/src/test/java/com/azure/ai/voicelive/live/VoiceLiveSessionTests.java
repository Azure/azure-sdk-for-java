// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.live;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.InterimResponseTrigger;
import com.azure.ai.voicelive.models.MaxOutputTokens;
import com.azure.ai.voicelive.models.ReasoningEffort;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.StaticInterimResponseConfig;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import reactor.core.Disposable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive session management.
 */
public class VoiceLiveSessionTests extends VoiceLiveTestBase {

    static Stream<Arguments> apiVersionParams() {
        return Arrays.stream(API_VERSIONS).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("apiVersionParams")
    @LiveOnly
    public void testSessionUpdateEventIsReceived(String apiVersion) throws InterruptedException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        AtomicBoolean sessionUpdatedReceived = new AtomicBoolean(false);
        AtomicReference<SessionUpdateSessionUpdated> receivedEvent = new AtomicReference<>();
        CountDownLatch eventLatch = new CountDownLatch(1);
        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;

        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful AI assistant for testing.")
                    .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                    .setInputAudioFormat(InputAudioFormat.PCM16);

            session = client.startSession(TEST_MODEL, null).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");
            Assertions.assertTrue(session.isConnected(), "Session should be connected");

            subscription = session.receiveEvents().subscribe(event -> {
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
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    @ParameterizedTest
    @MethodSource("apiVersionParams")
    @LiveOnly
    public void testReasoningEffortIsEchoedInSessionUpdated(String apiVersion) throws InterruptedException {
        VoiceLiveAsyncClient client = createClient(apiVersion);
        AtomicReference<SessionUpdateSessionUpdated> receivedEvent = new AtomicReference<>();
        CountDownLatch eventLatch = new CountDownLatch(1);
        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            session = client.startSession(TEST_MODEL, null).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                if (event.getType() == ServerEventType.SESSION_UPDATED
                    && event instanceof SessionUpdateSessionUpdated) {
                    receivedEvent.set((SessionUpdateSessionUpdated) event);
                    eventLatch.countDown();
                } else if (event.getType() == ServerEventType.ERROR) {
                    handleError(event);
                    eventLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                eventLatch.countDown();
            });

            VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setReasoningEffort(ReasoningEffort.LOW);
            session.sendEvent(new ClientEventSessionUpdate(options)).block(SEND_TIMEOUT);

            Assertions.assertTrue(eventLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Should receive session.updated within timeout");
            SessionUpdateSessionUpdated updated = receivedEvent.get();
            Assertions.assertNotNull(updated, "Should receive a session.updated event");
            Assertions.assertNotNull(updated.getSession(), "Session response should not be null");
            Assertions.assertEquals(ReasoningEffort.LOW, updated.getSession().getReasoningEffort(),
                "Server should echo reasoning_effort=low");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    @ParameterizedTest
    @MethodSource("apiVersionParams")
    @LiveOnly
    public void testInterimResponseIsEchoedInSessionUpdated(String apiVersion) throws InterruptedException {
        VoiceLiveAsyncClient client = createClient(apiVersion);
        AtomicReference<SessionUpdateSessionUpdated> receivedEvent = new AtomicReference<>();
        CountDownLatch eventLatch = new CountDownLatch(1);
        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            session = client.startSession(TEST_MODEL, null).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                if (event.getType() == ServerEventType.SESSION_UPDATED
                    && event instanceof SessionUpdateSessionUpdated) {
                    receivedEvent.set((SessionUpdateSessionUpdated) event);
                    eventLatch.countDown();
                } else if (event.getType() == ServerEventType.ERROR) {
                    handleError(event);
                    eventLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                eventLatch.countDown();
            });

            StaticInterimResponseConfig interim
                = new StaticInterimResponseConfig().setTexts(Arrays.asList("One moment please..."))
                    .setTriggers(Arrays.asList(InterimResponseTrigger.LATENCY))
                    .setLatencyThresholdMs(2000);
            VoiceLiveSessionOptions options
                = new VoiceLiveSessionOptions().setInterimResponse(BinaryData.fromObject(interim));
            session.sendEvent(new ClientEventSessionUpdate(options)).block(SEND_TIMEOUT);

            Assertions.assertTrue(eventLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Should receive session.updated within timeout");
            SessionUpdateSessionUpdated updated = receivedEvent.get();
            Assertions.assertNotNull(updated, "Should receive a session.updated event");
            Assertions.assertNotNull(updated.getSession(), "Session response should not be null");
            Assertions.assertNotNull(updated.getSession().getInterimResponse(),
                "Server should echo interim_response config");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    // Set max_response_output_tokens in a session update and assert the server
    // echoes it back in the session.updated event.
    @ParameterizedTest
    @MethodSource("apiVersionParams")
    @LiveOnly
    public void testMaxOutputTokensIsEchoedInSessionUpdated(String apiVersion) throws InterruptedException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        AtomicReference<SessionUpdateSessionUpdated> receivedEvent = new AtomicReference<>();
        CountDownLatch eventLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            session = client.startSession(TEST_MODEL, null).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                if (event.getType() == ServerEventType.SESSION_UPDATED
                    && event instanceof SessionUpdateSessionUpdated) {
                    receivedEvent.set((SessionUpdateSessionUpdated) event);
                    eventLatch.countDown();
                } else if (event.getType() == ServerEventType.ERROR) {
                    handleError(event);
                    eventLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                eventLatch.countDown();
            });

            VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
                .setMaxResponseOutputTokens(BinaryData.fromObject(MaxOutputTokens.of(100)));
            session.sendEvent(new ClientEventSessionUpdate(options)).block(SEND_TIMEOUT);

            Assertions.assertTrue(eventLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Should receive session.updated within timeout");
            SessionUpdateSessionUpdated updated = receivedEvent.get();
            Assertions.assertNotNull(updated, "Should receive a session.updated event");
            Assertions.assertNotNull(updated.getSession(), "Session response should not be null");
            Assertions.assertNotNull(updated.getSession().getMaxResponseOutputTokens(),
                "Server should echo max_response_output_tokens");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }
}
