// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AudioEchoCancellation;
import com.azure.ai.voicelive.models.AudioNoiseReduction;
import com.azure.ai.voicelive.models.AudioNoiseReductionType;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.Disposable;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive audio processing.
 */
public class VoiceLiveAudioTests extends VoiceLiveTestBase {

    static Stream<Arguments> audioParams() {
        return crossProduct(new String[] { "gpt-4o-realtime-preview", "gpt-4.1" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("audioParams")
    @LiveOnly
    public void testRealtimeServiceWithAudio(String model, String apiVersion) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicBoolean audioResponseReceived = new AtomicBoolean(false);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        CountDownLatch audioResponseLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setInputAudioFormat(InputAudioFormat.PCM16);

            session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    audioResponseReceived.set(true);
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                    audioResponseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    audioResponseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                audioResponseLatch.countDown();
            });

            Thread.sleep(SETUP_DELAY_MS);

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            Thread.sleep(SETUP_DELAY_MS);

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);

            boolean received = audioResponseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(audioResponseReceived.get(), "Should receive audio response delta events");
            Assertions.assertTrue(audioResponseBytes.get() > 0,
                "Should receive audio data (got " + audioResponseBytes.get() + " bytes)");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    static Stream<Arguments> audioEnhancementsParams() {
        return crossProduct(new String[] { "gpt-4o-realtime-preview", "gpt-4.1" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("audioEnhancementsParams")
    @LiveOnly
    public void testRealtimeServiceWithAudioEnhancements(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setInputAudioNoiseReduction(
                    new AudioNoiseReduction(AudioNoiseReductionType.AZURE_DEEP_NOISE_SUPPRESSION))
                .setInputAudioEchoCancellation(new AudioEchoCancellation());

            session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                if (!collectingEvents.get()) {
                    return;
                }
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

            // Wait for events to be collected
            Thread.sleep(EVENT_TIMEOUT_SECONDS * 1000);
            collectingEvents.set(false);

            Assertions.assertEquals(2, speechStartedEvents.get(),
                "Should detect exactly 2 speech segments with audio enhancements");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    static Stream<Arguments> echoCancellationParams() {
        return crossProduct(new String[] { "gpt-4o-realtime-preview", "gpt-4.1" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("echoCancellationParams")
    @LiveOnly
    public void testRealtimeServiceWithEchoCancellation(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        CountDownLatch responseLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInputAudioTranscription(getSpeechRecognitionSetting(model))
                    .setInputAudioEchoCancellation(new AudioEchoCancellation());

            session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
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
            Assertions.assertTrue(speechStartedEvents.get() > 1,
                "Expected more than 1 speech segment, got " + speechStartedEvents.get());
            Assertions.assertTrue(audioResponseBytes.get() > 0, "Audio bytes should be greater than 0");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }
}
