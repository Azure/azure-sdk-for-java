// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.live;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemInputAudioTranscriptionCompleted;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.util.BinaryData;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive transcription features.
 */
public class VoiceLiveTranscriptionTests extends VoiceLiveTestBase {

    static Stream<Arguments> whisperTranscriptionParams() {
        // gpt-realtime uses WHISPER_1, which the 2026-04-10 service does not emit
        // transcription.completed events for; restrict it to the GA version. gpt-4.1
        // uses AZURE_SPEECH, which works on both API versions.
        return Stream.concat(Stream.of(Arguments.of("gpt-realtime", API_VERSIONS[0])),
            Arrays.stream(API_VERSIONS).map(v -> Arguments.of("gpt-4.1", v)));
    }

    @ParameterizedTest
    @MethodSource("whisperTranscriptionParams")
    @LiveOnly
    public void testInputAudioTranscriptionWithWhisper(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("4-1.wav");

        AtomicBoolean transcriptionReceived = new AtomicBoolean(false);
        AtomicReference<String> transcriptText = new AtomicReference<>("");
        CountDownLatch transcriptionLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setInputAudioFormat(InputAudioFormat.PCM16)
                .setInputAudioTranscription(getSpeechRecognitionSetting(model));

            session = client.startSession(model, null).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.CONVERSATION_ITEM_INPUT_AUDIO_TRANSCRIPTION_COMPLETED) {
                    transcriptionReceived.set(true);
                    if (event instanceof SessionUpdateConversationItemInputAudioTranscriptionCompleted) {
                        SessionUpdateConversationItemInputAudioTranscriptionCompleted transcriptionEvent
                            = (SessionUpdateConversationItemInputAudioTranscriptionCompleted) event;
                        if (transcriptionEvent.getTranscript() != null) {
                            transcriptText.set(transcriptionEvent.getTranscript());
                        }
                    }
                    transcriptionLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    transcriptionLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                transcriptionLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(BinaryData.fromBytes(audioData)).block(SEND_TIMEOUT);
            session.sendInputAudio(BinaryData.fromBytes(getTrailingSilenceBytes())).block(SEND_TIMEOUT);

            boolean received = transcriptionLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive transcription within timeout");
            Assertions.assertTrue(transcriptionReceived.get(), "Should receive transcription completed event");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    static Stream<Arguments> gpt4oTranscribeParams() {
        // gpt-4o-transcribe and gpt-4o-mini-transcribe do not emit transcription.completed
        // events on the 2026-04-10 service; restrict to the GA version.
        return crossProduct(new String[] { "gpt-4o-transcribe", "gpt-4o-mini-transcribe" },
            new String[] { API_VERSIONS[0] });
    }

    @ParameterizedTest
    @MethodSource("gpt4oTranscribeParams")
    @LiveOnly
    public void testInputAudioTranscriptionWithGpt4oTranscribe(String transcriptionModel, String apiVersion)
        throws InterruptedException, IOException {
        String model = "gpt-realtime";
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("largest_lake.wav");

        AtomicBoolean transcriptionReceived = new AtomicBoolean(false);
        AtomicReference<String> transcriptText = new AtomicReference<>("");
        CountDownLatch transcriptionLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            AudioInputTranscriptionOptionsModel transcriptionOptionsModel
                = AudioInputTranscriptionOptionsModel.fromString(transcriptionModel);

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setInputAudioTranscription(
                new AudioInputTranscriptionOptions(transcriptionOptionsModel).setLanguage("en-US"));

            session = client.startSession(model, null).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.CONVERSATION_ITEM_INPUT_AUDIO_TRANSCRIPTION_COMPLETED) {
                    transcriptionReceived.set(true);
                    if (event instanceof SessionUpdateConversationItemInputAudioTranscriptionCompleted) {
                        SessionUpdateConversationItemInputAudioTranscriptionCompleted transcriptionEvent
                            = (SessionUpdateConversationItemInputAudioTranscriptionCompleted) event;
                        if (transcriptionEvent.getTranscript() != null) {
                            transcriptText.set(transcriptionEvent.getTranscript());
                        }
                    }
                    transcriptionLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    transcriptionLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                transcriptionLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(BinaryData.fromBytes(audioData)).block(SEND_TIMEOUT);
            session.sendInputAudio(BinaryData.fromBytes(getTrailingSilenceBytes())).block(SEND_TIMEOUT);

            boolean received = transcriptionLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive transcription within timeout");
            Assertions.assertTrue(transcriptionReceived.get(), "Should receive transcription completed event");
            Assertions.assertFalse(transcriptText.get().isEmpty(),
                "Transcript should not be empty for model: " + transcriptionModel);
            Assertions.assertTrue(
                transcriptText.get().toLowerCase().contains("largest")
                    || transcriptText.get().toLowerCase().contains("lake"),
                "Transcript should contain 'largest' or 'lake', got: " + transcriptText.get());
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }
}
