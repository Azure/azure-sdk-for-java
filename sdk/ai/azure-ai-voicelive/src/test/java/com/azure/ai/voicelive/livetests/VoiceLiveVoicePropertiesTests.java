// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AnimationOptions;
import com.azure.ai.voicelive.models.AnimationOutputType;
import com.azure.ai.voicelive.models.AudioTimestampType;
import com.azure.ai.voicelive.models.AzureStandardVoice;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Live tests for VoiceLive voice properties and animation features.
 */
public class VoiceLiveVoicePropertiesTests extends VoiceLiveTestBase {

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime", "gpt-4.1", "phi4-mm-realtime" })
    @LiveOnly
    public void testRealtimeServiceWithVoiceProperties(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("largest_lake.wav");

        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicInteger contentPartAddedEvents = new AtomicInteger(0);
        java.util.concurrent.CountDownLatch responseLatch = new java.util.concurrent.CountDownLatch(1);

        try {
            AzureStandardVoice voice = new AzureStandardVoice("en-us-emma:DragonHDLatestNeural").setTemperature(0.7)
                .setRate("1.2")
                .setPreferLocales(Arrays.asList("en-IN"));

            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setVoice(BinaryData.fromObject(voice))
                    .setInputAudioTranscription(getSpeechRecognitionSetting(model));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_CONTENT_PART_ADDED) {
                    contentPartAddedEvents.incrementAndGet();
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
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

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(contentPartAddedEvents.get() >= 1, "Should receive content part added events");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime-preview", "gpt-4.1" })
    @LiveOnly
    public void testRealtimeServiceWithAudioTimestampAndViseme(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicInteger timestampEvents = new AtomicInteger(0);
        AtomicInteger visemeEvents = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-NancyNeural")))
                .setAnimation(new AnimationOptions().setOutputs(Arrays.asList(AnimationOutputType.VISEME_ID)))
                .setOutputAudioTimestampTypes(Arrays.asList(AudioTimestampType.WORD));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                if (!collectingEvents.get()) {
                    return;
                }
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_TIMESTAMP_DELTA) {
                    timestampEvents.incrementAndGet();
                } else if (eventType == ServerEventType.RESPONSE_ANIMATION_VISEME_DELTA) {
                    visemeEvents.incrementAndGet();
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

            Assertions.assertTrue(audioResponseBytes.get() > 0, "Should receive audio bytes");
            Assertions.assertTrue(timestampEvents.get() > 0, "Should receive audio timestamp events");
            Assertions.assertTrue(visemeEvents.get() > 0, "Should receive viseme events");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }
}
