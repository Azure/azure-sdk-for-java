// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AzureSemanticEouDetection;
import com.azure.ai.voicelive.models.AzureSemanticEouDetectionEn;
import com.azure.ai.voicelive.models.AzureSemanticEouDetectionMultilingual;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetection;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetectionMultilingual;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.EouDetection;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.TurnDetection;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive end-of-utterance (EoU) detection features.
 */
public class VoiceLiveEouTests extends VoiceLiveTestBase {

    static Stream<Arguments> eouCombinations() {
        return withApiVersions(Stream.of(
            // ServerVad + EoU combinations
            Arguments.of("ServerVad + SemanticDetection",
                new ServerVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetection().setTimeoutMs(2000))),
            Arguments.of("ServerVad + SemanticDetectionEn",
                new ServerVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionEn().setTimeoutMs(2000))),
            Arguments.of("ServerVad + SemanticDetectionMultilingual",
                new ServerVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionMultilingual().setTimeoutMs(2000))),

            // AzureSemanticVad + EoU combinations
            Arguments.of("AzureSemanticVad + SemanticDetection",
                new AzureSemanticVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetection().setTimeoutMs(2000))),
            Arguments.of("AzureSemanticVad + SemanticDetectionEn",
                new AzureSemanticVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionEn().setTimeoutMs(2000))),
            Arguments.of("AzureSemanticVad + SemanticDetectionMultilingual",
                new AzureSemanticVadTurnDetection()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionMultilingual().setTimeoutMs(2000))),

            // AzureSemanticVadMultilingual + EoU combinations
            Arguments.of("AzureSemanticVadMultilingual + SemanticDetection",
                new AzureSemanticVadTurnDetectionMultilingual()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetection().setTimeoutMs(2000))),
            Arguments.of("AzureSemanticVadMultilingual + SemanticDetectionEn",
                new AzureSemanticVadTurnDetectionMultilingual()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionEn().setTimeoutMs(2000))),
            Arguments.of("AzureSemanticVadMultilingual + SemanticDetectionMultilingual",
                new AzureSemanticVadTurnDetectionMultilingual()
                    .setEndOfUtteranceDetection(new AzureSemanticEouDetectionMultilingual().setTimeoutMs(2000)))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("eouCombinations")
    @LiveOnly
    public void testRealtimeServiceWithEou(String description, TurnDetection turnDetection, String apiVersion)
        throws InterruptedException, IOException {
        String model = "gpt-4o";
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicInteger speechStartedEvents = new AtomicInteger(0);
        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicBoolean collectingEvents = new AtomicBoolean(true);

        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setTurnDetection(turnDetection);

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

            Assertions.assertTrue(speechStartedEvents.get() > 0,
                description + ": Should have at least 1 speech started event, got " + speechStartedEvents.get());
            Assertions.assertTrue(audioResponseBytes.get() > 0,
                description + ": Should receive audio response bytes, got " + audioResponseBytes.get());

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception (" + description + "): " + e.getMessage());
        }
    }
}
