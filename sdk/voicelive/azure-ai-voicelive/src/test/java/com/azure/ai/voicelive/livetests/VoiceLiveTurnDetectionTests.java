// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AzureSemanticEouDetection;
import com.azure.ai.voicelive.models.AzureSemanticEouDetectionEn;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetection;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetectionMultilingual;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.TurnDetection;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Assertions;
import reactor.core.Disposable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive turn detection features.
 * Translated from Python turn detection tests.
 */
public class VoiceLiveTurnDetectionTests extends VoiceLiveTestBase {

    // ===== test_realtime_service_with_turn_detection_long_tts_vad_duration =====
    // Python: models=[gpt-4o-realtime-preview, gpt-4o], api_versions=[2025-10-01, 2026-01-01-preview]
    // turn_detection: {"type": "azure_semantic_vad", "speech_duration_assistant_speaking_ms": 800}
    // Note: speechDurationAssistantSpeakingMs not available in Java SDK;
    // using speechDurationMs(800) as the closest available parameter.

    static Stream<Arguments> longTtsVadDurationParams() {
        return crossProduct(new String[] { MODEL_GPT_4O_REALTIME_PREVIEW, MODEL_GPT_4O },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("longTtsVadDurationParams")
    @LiveOnly
    public void testRealtimeServiceWithTurnDetectionLongTtsVadDuration(String model, String apiVersion)
        throws InterruptedException, IOException {
        // Python uses @pytest.mark.flaky(reruns=3, reruns_delay=2)
        int maxAttempts = 3;
        AssertionError lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                doTestLongTtsVadDuration(model, apiVersion);
                return; // passed
            } catch (AssertionError e) {
                lastFailure = e;
                System.out.println("testRealtimeServiceWithTurnDetectionLongTtsVadDuration attempt " + attempt + "/"
                    + maxAttempts + " failed: " + e.getMessage());
                if (attempt < maxAttempts) {
                    Thread.sleep(2000); // reruns_delay=2
                }
            }
        }
        throw lastFailure;
    }

    private void doTestLongTtsVadDuration(String model, String apiVersion) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("4-1.wav");

        // Python: turn_detection = {"type": "azure_semantic_vad", "speech_duration_assistant_speaking_ms": 800}
        // Java SDK doesn't expose speechDurationAssistantSpeakingMs; using speechDurationMs as closest match.
        AzureSemanticVadTurnDetection turnDetection = new AzureSemanticVadTurnDetection().setSpeechDurationMs(800);

        AtomicReference<SessionUpdateResponseAudioDelta> audioDeltaRef = new AtomicReference<>();
        CountDownLatch audioDeltaLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            session = client.startSession(model).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        audioDeltaRef.compareAndSet(null, (SessionUpdateResponseAudioDelta) event);
                        audioDeltaLatch.countDown();
                    }
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    audioDeltaLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                audioDeltaLatch.countDown();
            });

            waitForSetup();

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setTurnDetection(turnDetection);
            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);
            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);

            // Python: _wait_for_event(conn, {RESPONSE_AUDIO_DELTA}, 30)
            boolean received = audioDeltaLatch.await(30, TimeUnit.SECONDS);
            Assertions.assertTrue(received, "Should receive RESPONSE_AUDIO_DELTA within 30 seconds");

            SessionUpdateResponseAudioDelta audioDelta = audioDeltaRef.get();
            Assertions.assertNotNull(audioDelta, "Audio delta event should not be null");
            Assertions.assertNotNull(audioDelta.getDelta(), "Audio delta data should not be null");
            Assertions.assertTrue(audioDelta.getDelta().length > 0, "Audio delta data should not be empty");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    // ===== test_realtime_service_with_turn_detection_multilingual =====
    // Python: models × semanticVadParams, api_versions=[2025-10-01, 2026-01-01-preview]
    // Uses AzureSemanticVadMultilingual(**semantic_vad_params)

    static Stream<Arguments> multilingualParams() {
        return withApiVersions(Stream.of(
            Arguments.of("gpt-4o-realtime-preview, default", MODEL_GPT_4O_REALTIME_PREVIEW,
                new AzureSemanticVadTurnDetectionMultilingual()),
            Arguments.of("gpt-4o, default", MODEL_GPT_4O, new AzureSemanticVadTurnDetectionMultilingual()),
            Arguments.of("gpt-4o, speechDuration200", MODEL_GPT_4O,
                new AzureSemanticVadTurnDetectionMultilingual().setSpeechDurationMs(200)),
            Arguments.of("gpt-4o, languages [en, es]", MODEL_GPT_4O,
                new AzureSemanticVadTurnDetectionMultilingual().setLanguages(Arrays.asList("en", "es")))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multilingualParams")
    @LiveOnly
    public void testRealtimeServiceWithTurnDetectionMultilingual(String description, String model,
        TurnDetection turnDetection, String apiVersion) throws InterruptedException, IOException {
        // Python uses @pytest.mark.flaky(reruns=3, reruns_delay=2)
        int maxAttempts = 3;
        AssertionError lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                doTestMultilingual(description, model, turnDetection, apiVersion);
                return; // passed
            } catch (AssertionError e) {
                lastFailure = e;
                System.out.println("testRealtimeServiceWithTurnDetectionMultilingual attempt " + attempt + "/"
                    + maxAttempts + " failed (" + description + "): " + e.getMessage());
                if (attempt < maxAttempts) {
                    Thread.sleep(2000); // reruns_delay=2
                }
            }
        }
        throw lastFailure;
    }

    private void doTestMultilingual(String description, String model, TurnDetection turnDetection, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger speechStartedCount = new AtomicInteger(0);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            session = client.startSession(model).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                if (!collectingEvents.get()) {
                    return;
                }
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
                    speechStartedCount.incrementAndGet();
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

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setTurnDetection(turnDetection);
            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);
            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            // Python uses _collect_event which collects events over a timeout period
            Thread.sleep(EVENT_TIMEOUT_SECONDS * 1000);
            collectingEvents.set(false);

            Assertions.assertEquals(2, speechStartedCount.get(),
                description + ": Should have exactly 2 speech segments, got " + speechStartedCount.get());
            Assertions.assertTrue(audioResponseBytes.get() > 0,
                description + ": Should receive audio response bytes, got " + audioResponseBytes.get());
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    // ===== test_realtime_service_with_eou =====
    // Python: model=gpt-4o, turn_detection_cls × end_of_detection combinations
    // 6 combos: ServerVad/AzureSemanticVad/AzureSemanticVadMultilingual × AzureSemanticDetection/AzureSemanticDetectionEn
    // api_versions=[2025-10-01, 2026-01-01-preview]

    static Stream<Arguments> eouParams() {
        return withApiVersions(Stream.of(
            // ServerVad + EoU combinations
            Arguments.of("ServerVad + AzureSemanticDetection",
                new ServerVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetection().setTimeoutMs(2000))),
            Arguments.of("ServerVad + AzureSemanticDetectionEn",
                new ServerVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionEn().setTimeoutMs(2000))),
            // AzureSemanticVad + EoU combinations
            Arguments.of("AzureSemanticVad + AzureSemanticDetection",
                new AzureSemanticVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetection().setTimeoutMs(2000))),
            Arguments.of("AzureSemanticVad + AzureSemanticDetectionEn",
                new AzureSemanticVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionEn().setTimeoutMs(2000))),
            // AzureSemanticVadMultilingual + EoU combinations
            Arguments.of("AzureSemanticVadMultilingual + AzureSemanticDetection",
                new AzureSemanticVadTurnDetectionMultilingual()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetection().setTimeoutMs(2000))),
            Arguments.of("AzureSemanticVadMultilingual + AzureSemanticDetectionEn",
                new AzureSemanticVadTurnDetectionMultilingual()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionEn().setTimeoutMs(2000)))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("eouParams")
    @LiveOnly
    public void testRealtimeServiceWithEou(String description, TurnDetection turnDetection, String apiVersion)
        throws InterruptedException, IOException {
        // Python uses @pytest.mark.flaky(reruns=3, reruns_delay=2)
        int maxAttempts = 3;
        AssertionError lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                doTestEou(description, turnDetection, apiVersion);
                return; // passed
            } catch (AssertionError e) {
                lastFailure = e;
                System.out.println("testRealtimeServiceWithEou attempt " + attempt + "/" + maxAttempts + " failed ("
                    + description + "): " + e.getMessage());
                if (attempt < maxAttempts) {
                    Thread.sleep(2000); // reruns_delay=2
                }
            }
        }
        throw lastFailure;
    }

    private void doTestEou(String description, TurnDetection turnDetection, String apiVersion)
        throws InterruptedException, IOException {
        // Python: model is always gpt-4o for this test
        String model = MODEL_GPT_4O;
        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger responseDoneCount = new AtomicInteger(0);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            session = client.startSession(model).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                if (!collectingEvents.get()) {
                    return;
                }
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.RESPONSE_DONE) {
                    responseDoneCount.incrementAndGet();
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

            // Python: session = RequestSession(turn_detection=turn_detection,
            //     input_audio_transcription=_get_speech_recognition_setting(model=model))
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setTurnDetection(turnDetection)
                .setInputAudioTranscription(getSpeechRecognitionSetting(model));
            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);
            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            // Python: _get_trailing_silence_bytes(duration_s=0.5)
            session.sendInputAudio(getTrailingSilenceBytes(DEFAULT_SAMPLE_RATE, 0.5)).block(SEND_TIMEOUT);

            // Python: _collect_event(conn, event_type=ServerEventType.RESPONSE_DONE)
            Thread.sleep(EVENT_TIMEOUT_SECONDS * 1000);
            collectingEvents.set(false);

            Assertions.assertTrue(responseDoneCount.get() > 0,
                description + ": Should have at least 1 RESPONSE_DONE event, got " + responseDoneCount.get());
            Assertions.assertTrue(audioResponseBytes.get() > 0,
                description + ": Should receive audio response bytes, got " + audioResponseBytes.get());
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }
}
