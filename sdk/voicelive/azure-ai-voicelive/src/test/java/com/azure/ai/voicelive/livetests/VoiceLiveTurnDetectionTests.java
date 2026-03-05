// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetection;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetectionMultilingual;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferCommit;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive turn detection and VAD features.
 */
public class VoiceLiveTurnDetectionTests extends VoiceLiveTestBase {

    static Stream<Arguments> withoutTurnDetectionParams() {
        return crossProduct(new String[] { "gpt-4o-realtime-preview", "gpt-4.1" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("withoutTurnDetectionParams")
    @LiveOnly
    public void testRealtimeServiceWithoutTurnDetection(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

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

    static Stream<Arguments> semanticVadParams() {
        return crossProduct(new String[] { "gpt-4o-realtime-preview", "gpt-4.1" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("semanticVadParams")
    @LiveOnly
    public void testRealtimeServiceWithSemanticVadMultilingual(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

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

    static Stream<Arguments> fillerWordRemovalParams() {
        return crossProduct(new String[] { "gpt-4o-realtime-preview" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("fillerWordRemovalParams")
    @LiveOnly
    public void testRealtimeServiceWithFillerWordRemoval(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("filler_word_24kHz.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setTurnDetection(new AzureSemanticVadTurnDetection().setRemoveFillerWords(true));

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

    static Stream<Arguments> azureSemanticVadMultilingualParams() {
        return crossProduct(new String[] { "gpt-4o-realtime-preview", "gpt-4o-realtime" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("azureSemanticVadMultilingualParams")
    @LiveOnly
    public void testRealtimeServiceWithAzureSemanticVadMultilingual(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setTurnDetection(new AzureSemanticVadTurnDetectionMultilingual());

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
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
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            Thread.sleep(EVENT_TIMEOUT_SECONDS * 1000);
            collectingEvents.set(false);

            Assertions.assertEquals(2, speechStartedEvents.get(),
                "Should detect exactly 2 speech segments with multilingual VAD");
            Assertions.assertTrue(audioResponseBytes.get() > 0, "Should receive audio bytes");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    static Stream<Arguments> fillerWordMultilingualParams() {
        return crossProduct(new String[] { "gpt-4o-realtime-preview" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("fillerWordMultilingualParams")
    @LiveOnly
    public void testRealtimeServiceWithFillerWordRemovalMultilingual(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("filler_word_24kHz.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        try {
            AzureSemanticVadTurnDetectionMultilingual turnDetection
                = new AzureSemanticVadTurnDetectionMultilingual().setRemoveFillerWords(true);

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setTurnDetection(turnDetection)
                .setInputAudioTranscription(getSpeechRecognitionSetting(model));

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
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            Thread.sleep(10000);
            collectingEvents.set(false);

            Assertions.assertEquals(0, speechStartedEvents.get(),
                "Should have no speech started events when filler words are removed with multilingual VAD");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    static Stream<Arguments> withoutTurnDetectionExplicitParams() {
        return crossProduct(new String[] { "gpt-4o-realtime", "gpt-4o", "phi4-mm-realtime", "phi4-mini" },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("withoutTurnDetectionExplicitParams")
    @LiveOnly
    public void testRealtimeServiceWithoutTurnDetectionExplicit(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("ask_weather.mp3");

        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicInteger transcriptionEvents = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInputAudioTranscription(getSpeechRecognitionSetting(model));

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
                } else if (eventType == ServerEventType.CONVERSATION_ITEM_INPUT_AUDIO_TRANSCRIPTION_COMPLETED) {
                    transcriptionEvents.incrementAndGet();
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

            // Send audio without turn detection - should not trigger automatic response
            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            Thread.sleep(5000);

            Assertions.assertEquals(0, audioResponseBytes.get(),
                "Should not receive audio response without turn detection before commit");
            Assertions.assertEquals(0, transcriptionEvents.get(),
                "Should not receive transcription without turn detection before commit");

            // Commit audio buffer - still should not trigger response
            session.sendEvent(new ClientEventInputAudioBufferCommit()).block(SEND_TIMEOUT);
            Thread.sleep(5000);

            Assertions.assertEquals(0, audioResponseBytes.get(),
                "Should not receive audio response after commit without response.create");

            // Explicitly create a response - now should get audio and transcript
            session.sendEvent(new ClientEventResponseCreate()).block(SEND_TIMEOUT);
            Thread.sleep(EVENT_TIMEOUT_SECONDS * 1000);
            collectingEvents.set(false);

            Assertions.assertTrue(audioResponseBytes.get() > 0,
                "Should receive audio response after response.create, got " + audioResponseBytes.get() + " bytes");
            Assertions.assertTrue(transcriptionEvents.get() > 0,
                "Should receive transcription after response.create, got " + transcriptionEvents.get() + " events");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }
}
