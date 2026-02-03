// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetection;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetectionMultilingual;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Live tests for VoiceLive turn detection and VAD features.
 */
public class VoiceLiveTurnDetectionTests extends VoiceLiveTestBase {

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime-preview", "gpt-4.1" })
    @LiveOnly
    public void testRealtimeServiceWithoutTurnDetection(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicBoolean speechStartedReceived = new AtomicBoolean(false);
        AtomicBoolean speechStoppedReceived = new AtomicBoolean(false);
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setInputAudioFormat(InputAudioFormat.PCM16);

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
                    speechStartedReceived.set(true);
                } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
                    speechStoppedReceived.set(true);
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(speechStartedReceived.get(), "Should receive speech started event");
            Assertions.assertTrue(speechStoppedReceived.get(), "Should receive speech stopped event");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime-preview", "gpt-4.1" })
    @LiveOnly
    public void testRealtimeServiceWithSemanticVadMultilingual(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicInteger speechStoppedEvents = new AtomicInteger(0);
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setInputAudioFormat(InputAudioFormat.PCM16)
                .setTurnDetection(new AzureSemanticVadTurnDetection());

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
                    speechStartedEvents.incrementAndGet();
                } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
                    speechStoppedEvents.incrementAndGet();
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(speechStartedEvents.get() >= 1, "Should have at least 1 speech started event");
            Assertions.assertTrue(speechStoppedEvents.get() >= 1, "Should have at least 1 speech stopped event");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime-preview", "gpt-4.1" })
    @LiveOnly
    public void testRealtimeServiceWithFillerWordRemoval(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicBoolean audioResponseReceived = new AtomicBoolean(false);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setInputAudioFormat(InputAudioFormat.PCM16)
                .setTurnDetection(new AzureSemanticVadTurnDetection().setRemoveFillerWords(true));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    audioResponseReceived.set(true);
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(audioResponseReceived.get(), "Should receive audio response delta events");
            Assertions.assertTrue(audioResponseBytes.get() > 0, "Should receive audio data");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime-preview", "gpt-4o-realtime" })
    @LiveOnly
    public void testRealtimeServiceWithAzureSemanticVadMultilingual(String model)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setTurnDetection(new AzureSemanticVadTurnDetectionMultilingual());

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
                    speechStartedEvents.incrementAndGet();
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(audioResponseBytes.get() > 0, "Should receive audio bytes");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    @LiveOnly
    public void testRealtimeServiceWithFillerWordRemovalSpecial() throws InterruptedException, IOException {
        String model = "gpt-4o-realtime-preview";
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("filler_word_24kHz.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        try {
            AzureSemanticVadTurnDetection turnDetection
                = new AzureSemanticVadTurnDetection().setRemoveFillerWords(true);

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setTurnDetection(turnDetection);

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                if (!collectingEvents.get()) {
                    return;
                }
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
                    speechStartedEvents.incrementAndGet();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);

            Thread.sleep(10000);
            collectingEvents.set(false);

            Assertions.assertEquals(0, speechStartedEvents.get(),
                "Should have no speech started events when filler words are removed");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }
}
