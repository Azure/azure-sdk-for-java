// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.AzureSemanticVadTurnDetection;
import com.azure.ai.voicelive.models.AzureStandardVoice;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdateInputAudioBufferSpeechStarted;
import com.azure.ai.voicelive.models.SessionUpdateInputAudioBufferSpeechStopped;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.TurnDetection;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive audio format handling.
 */
public class VoiceLiveAudioFormatTests extends VoiceLiveTestBase {

    static Stream<Arguments> modelAndSamplingRateProvider() {
        return Stream.of(Arguments.of("gpt-4o-realtime-preview", 16000), Arguments.of("gpt-4o-realtime", 44100),
            Arguments.of("gpt-4o-realtime", 8000), Arguments.of("gpt-4o", 16000), Arguments.of("gpt-4o", 44100),
            Arguments.of("gpt-4.1", 8000), Arguments.of("phi4-mm-realtime", 16000),
            Arguments.of("phi4-mm-realtime", 44100));
    }

    static Stream<Arguments> modelAndInputAudioFormatProvider() {
        return Stream.of(Arguments.of("gpt-4o", "g711_ulaw", "azure_semantic_vad"),
            Arguments.of("gpt-4o", "g711_alaw", "azure_semantic_vad"),
            Arguments.of("gpt-4o-realtime-preview", "g711_ulaw", "azure_semantic_vad"),
            Arguments.of("gpt-4o-realtime-preview", "g711_ulaw", "server_vad"),
            Arguments.of("gpt-4o-realtime-preview", "g711_alaw", "azure_semantic_vad"),
            Arguments.of("gpt-4o-realtime-preview", "g711_alaw", "server_vad"),
            Arguments.of("phi4-mm-realtime", "g711_ulaw", "azure_semantic_vad"),
            Arguments.of("phi4-mm-realtime", "g711_alaw", "azure_semantic_vad"),
            Arguments.of("phi4-mini", "g711_ulaw", "azure_semantic_vad"),
            Arguments.of("phi4-mini", "g711_alaw", "azure_semantic_vad"));
    }

    static Stream<Arguments> modelAndOutputAudioFormatAzureVoiceProvider() {
        return Stream.of(Arguments.of("gpt-4.1", "pcm16"), Arguments.of("gpt-4.1", "pcm16_8000hz"),
            Arguments.of("gpt-4.1", "pcm16_16000hz"), Arguments.of("gpt-4.1", "pcm16_22050hz"),
            Arguments.of("gpt-4.1", "pcm16_24000hz"), Arguments.of("gpt-4.1", "pcm16_44100hz"),
            Arguments.of("gpt-4.1", "pcm16_48000hz"), Arguments.of("gpt-4.1", "g711_ulaw"),
            Arguments.of("gpt-4.1", "g711_alaw"), Arguments.of("phi4-mini", "pcm16"),
            Arguments.of("phi4-mini", "pcm16_8000hz"), Arguments.of("phi4-mini", "pcm16_16000hz"),
            Arguments.of("phi4-mini", "pcm16_22050hz"), Arguments.of("phi4-mini", "pcm16_24000hz"),
            Arguments.of("phi4-mini", "pcm16_44100hz"), Arguments.of("phi4-mini", "pcm16_48000hz"),
            Arguments.of("phi4-mini", "g711_ulaw"), Arguments.of("phi4-mini", "g711_alaw"));
    }

    static Stream<Arguments> modelAndOutputAudioFormatOpenAIVoiceProvider() {
        return Stream.of(Arguments.of("gpt-4o-realtime", "pcm16"), Arguments.of("gpt-4o-realtime", "g711_ulaw"),
            Arguments.of("gpt-4o-realtime", "g711_alaw"));
    }

    @ParameterizedTest
    @MethodSource("modelAndInputAudioFormatProvider")
    @LiveOnly
    public void testRealtimeServiceWithInputAudioFormat(String model, String audioFormat, String turnDetectionType)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        String audioFile = "g711_ulaw".equals(audioFormat) ? "largest_lake.ulaw" : "largest_lake.alaw";
        byte[] audioData = loadAudioFile(audioFile);

        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicReference<SessionUpdateSessionUpdated> sessionUpdatedEvent = new AtomicReference<>();
        CountDownLatch sessionUpdatedLatch = new CountDownLatch(1);
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            InputAudioFormat inputFormat = parseInputAudioFormat(audioFormat);

            TurnDetection turnDetection = "server_vad".equals(turnDetectionType)
                ? new ServerVadTurnDetection()
                : new AzureSemanticVadTurnDetection();

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setInputAudioFormat(inputFormat)
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AriaNeural")))
                .setInstructions("You are a helpful assistant. Please respond briefly to the user's question.")
                .setTurnDetection(turnDetection)
                .setInputAudioTranscription(getSpeechRecognitionSetting(model));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.SESSION_UPDATED) {
                    if (event instanceof SessionUpdateSessionUpdated) {
                        sessionUpdatedEvent.set((SessionUpdateSessionUpdated) event);
                    }
                    sessionUpdatedLatch.countDown();
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    sessionUpdatedLatch.countDown();
                    responseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                sessionUpdatedLatch.countDown();
                responseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            boolean sessionUpdated = sessionUpdatedLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(sessionUpdated, "Should receive session updated event");
            Assertions.assertNotNull(sessionUpdatedEvent.get(), "Session updated event should not be null");

            SessionUpdateSessionUpdated updatedEvent = sessionUpdatedEvent.get();
            if (updatedEvent.getSession() != null) {
                InputAudioFormat actualFormat = updatedEvent.getSession().getInputAudioFormat();
                Assertions.assertEquals(inputFormat, actualFormat,
                    "Expected audio format " + inputFormat + ", got " + actualFormat);

                Integer actualSamplingRate = updatedEvent.getSession().getInputAudioSamplingRate();
                if (actualSamplingRate != null) {
                    Assertions.assertEquals(8000, actualSamplingRate.intValue(),
                        "Expected sampling rate 8000 for g711 formats, got " + actualSamplingRate);
                }
            }

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes(8000, 2.0)).block(SEND_TIMEOUT);

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(audioResponseBytes.get() > MIN_AUDIO_BYTES_LARGE,
                "Output audio too short for " + audioFormat + " format: " + audioResponseBytes.get() + " bytes");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("modelAndSamplingRateProvider")
    @LiveOnly
    public void testRealtimeServiceWithInputAudioSamplingRate(String model, int samplingRate)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        String audioFile = getAudioFileForSamplingRate(samplingRate);
        byte[] audioData = loadAudioFile(audioFile);

        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicReference<SessionUpdateSessionUpdated> sessionUpdatedEvent = new AtomicReference<>();
        AtomicReference<SessionUpdateInputAudioBufferSpeechStarted> speechStartedEvent = new AtomicReference<>();
        AtomicReference<SessionUpdateInputAudioBufferSpeechStopped> speechStoppedEvent = new AtomicReference<>();
        CountDownLatch sessionUpdatedLatch = new CountDownLatch(1);
        CountDownLatch speechStartedLatch = new CountDownLatch(1);
        CountDownLatch speechStoppedLatch = new CountDownLatch(1);
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AriaNeural")))
                .setInputAudioSamplingRate(samplingRate)
                .setInputAudioTranscription(getSpeechRecognitionSetting(model))
                .setInstructions(
                    "You are a helpful assistant. Please respond briefly to the user's question about lakes.")
                .setTurnDetection(new ServerVadTurnDetection());

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.SESSION_UPDATED) {
                    if (event instanceof SessionUpdateSessionUpdated) {
                        sessionUpdatedEvent.set((SessionUpdateSessionUpdated) event);
                    }
                    sessionUpdatedLatch.countDown();
                } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
                    if (event instanceof SessionUpdateInputAudioBufferSpeechStarted) {
                        speechStartedEvent.set((SessionUpdateInputAudioBufferSpeechStarted) event);
                    }
                    speechStartedLatch.countDown();
                } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
                    if (event instanceof SessionUpdateInputAudioBufferSpeechStopped) {
                        speechStoppedEvent.set((SessionUpdateInputAudioBufferSpeechStopped) event);
                    }
                    speechStoppedLatch.countDown();
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    sessionUpdatedLatch.countDown();
                    speechStartedLatch.countDown();
                    speechStoppedLatch.countDown();
                    responseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                sessionUpdatedLatch.countDown();
                speechStartedLatch.countDown();
                speechStoppedLatch.countDown();
                responseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            boolean sessionUpdated = sessionUpdatedLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(sessionUpdated, "Should receive session updated event");
            Assertions.assertNotNull(sessionUpdatedEvent.get(), "Session updated event should not be null");
            if (sessionUpdatedEvent.get().getSession() != null) {
                Integer actualSamplingRate = sessionUpdatedEvent.get().getSession().getInputAudioSamplingRate();
                Assertions.assertEquals(samplingRate, actualSamplingRate.intValue(),
                    "Expected sampling rate " + samplingRate + ", got " + actualSamplingRate);
            }

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes(samplingRate, 2.0)).block(SEND_TIMEOUT);

            boolean speechStarted = speechStartedLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(speechStarted, "Should receive speech started event");
            Assertions.assertNotNull(speechStartedEvent.get(), "Speech started event should not be null");
            Assertions.assertEquals(0, speechStartedEvent.get().getAudioStartMs(), "Audio start ms should be 0");

            boolean speechStopped = speechStoppedLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(speechStopped, "Should receive speech stopped event");
            Assertions.assertNotNull(speechStoppedEvent.get(), "Speech stopped event should not be null");
            int audioEndMs = speechStoppedEvent.get().getAudioEndMs();
            int expectedEndMs = 1664;
            double tolerance = 0.02;
            Assertions.assertTrue(Math.abs(audioEndMs - expectedEndMs) <= expectedEndMs * tolerance,
                "Audio end ms should be approximately " + expectedEndMs + " (got " + audioEndMs + ")");

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(audioResponseBytes.get() > MIN_AUDIO_BYTES_LARGE, "Output audio too short: "
                + audioResponseBytes.get() + " bytes (expected > " + MIN_AUDIO_BYTES_LARGE + ")");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @Disabled
    @MethodSource("modelAndOutputAudioFormatAzureVoiceProvider")
    @LiveOnly
    public void testOutputFormatsWithAzureVoice(String model, String outputFormat)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("largest_lake.wav");

        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicInteger audioDoneEvents = new AtomicInteger(0);
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            OutputAudioFormat format = parseOutputAudioFormat(outputFormat);

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setOutputAudioFormat(format)
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AriaNeural")))
                .setInputAudioTranscription(getSpeechRecognitionSetting(model))
                .setInstructions("You are a helpful assistant.")
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
                    audioDoneEvents.incrementAndGet();
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
            Assertions.assertTrue(audioDoneEvents.get() >= 1, "Should receive audio done events");
            Assertions.assertTrue(audioResponseBytes.get() > MIN_AUDIO_BYTES,
                "Output audio too short: " + audioResponseBytes.get() + " bytes");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("modelAndOutputAudioFormatOpenAIVoiceProvider")
    @LiveOnly
    public void testOutputFormatsWithOpenAIVoice(String model, String outputFormat)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("largest_lake.wav");

        AtomicInteger audioResponseBytes = new AtomicInteger(0);
        AtomicInteger audioDoneEvents = new AtomicInteger(0);
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            OutputAudioFormat format = parseOutputAudioFormat(outputFormat);

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setOutputAudioFormat(format)
                .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1))
                .setInstructions("You are a helpful assistant.")
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null) {
                            audioResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DONE) {
                    audioDoneEvents.incrementAndGet();
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
            Assertions.assertTrue(audioDoneEvents.get() >= 1, "Should receive audio done events");
            Assertions.assertTrue(audioResponseBytes.get() > MIN_AUDIO_BYTES,
                "Output audio too short: " + audioResponseBytes.get() + " bytes");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }
}
