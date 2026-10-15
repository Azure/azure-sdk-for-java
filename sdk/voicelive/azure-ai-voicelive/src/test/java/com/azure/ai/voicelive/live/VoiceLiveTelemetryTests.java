// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.live;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Live tests that verify VoiceLive telemetry the way a user consumes it: by registering a global OpenTelemetry SDK and
 * inspecting the spans that the public client emits during a real session.
 * <p>
 * Unlike {@code com.azure.ai.voicelive.implementation.VoiceLiveTracerTest} (which white-box tests the tracer's internal
 * span/counter logic), this test exercises the end-to-end path through {@link VoiceLiveAsyncClient} and asserts only on
 * the publicly observable OpenTelemetry output.
 */
public class VoiceLiveTelemetryTests extends VoiceLiveTestBase {

    private static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");
    private static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    private static final AttributeKey<String> GEN_AI_REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    private static final AttributeKey<String> GEN_AI_PROVIDER_NAME = AttributeKey.stringKey("gen_ai.provider.name");
    private static final AttributeKey<String> GEN_AI_VOICE_EVENT_TYPE
        = AttributeKey.stringKey("gen_ai.voice.event_type");
    private static final AttributeKey<Long> GEN_AI_VOICE_AUDIO_BYTES_SENT
        = AttributeKey.longKey("gen_ai.voice.audio_bytes_sent");
    private static final AttributeKey<Long> GEN_AI_VOICE_AUDIO_BYTES_RECEIVED
        = AttributeKey.longKey("gen_ai.voice.audio_bytes_received");
    private static final AttributeKey<Long> GEN_AI_VOICE_TURN_COUNT = AttributeKey.longKey("gen_ai.voice.turn_count");
    private static final AttributeKey<Long> GEN_AI_USAGE_INPUT_TOKENS
        = AttributeKey.longKey("gen_ai.usage.input_tokens");
    private static final AttributeKey<Long> GEN_AI_USAGE_OUTPUT_TOKENS
        = AttributeKey.longKey("gen_ai.usage.output_tokens");
    private static final AttributeKey<String> GEN_AI_RESPONSE_ID = AttributeKey.stringKey("gen_ai.response.id");

    private InMemorySpanExporter spanExporter;
    private OpenTelemetrySdk openTelemetry;

    static Stream<Arguments> apiVersionParams() {
        return Arrays.stream(API_VERSIONS).map(Arguments::of);
    }

    @BeforeEach
    void registerGlobalOpenTelemetry() {
        // A user enables tracing by registering a global OpenTelemetry SDK; the client picks it up automatically.
        GlobalOpenTelemetry.resetForTest();
        spanExporter = InMemorySpanExporter.create();
        SdkTracerProvider tracerProvider
            = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(spanExporter)).build();
        openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal();
    }

    @AfterEach
    void unregisterGlobalOpenTelemetry() {
        if (openTelemetry != null) {
            openTelemetry.close();
        }
        GlobalOpenTelemetry.resetForTest();
    }

    @ParameterizedTest
    @MethodSource("apiVersionParams")
    @LiveOnly
    public void testConnectSpanIsExportedForRealSession(String apiVersion) throws InterruptedException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        CountDownLatch eventLatch = new CountDownLatch(1);
        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;

        try {
            session = client.startSession(TEST_MODEL, null).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");
            Assertions.assertTrue(session.isConnected(), "Session should be connected");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.SESSION_UPDATED || eventType == ServerEventType.ERROR) {
                    if (eventType == ServerEventType.ERROR) {
                        handleError(event);
                    }
                    eventLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                eventLatch.countDown();
            });

            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful AI assistant for testing.")
                    .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO));
            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);

            Assertions.assertTrue(eventLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Should receive an event within timeout");
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            // Closing the session ends the connect span so it is exported.
            closeSession(session);
        }

        // The connect span is only finished on close; wait until it is actually exported.
        List<SpanData> spans = awaitConnectSpan();
        Assertions.assertFalse(spans.isEmpty(), "Expected at least one exported span from the session");

        SpanData connectSpan = spans.stream()
            .filter(s -> "connect".equals(s.getAttributes().get(GEN_AI_OPERATION_NAME)))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected a span with gen_ai.operation.name=connect"));

        Assertions.assertEquals("az.ai.voicelive", connectSpan.getAttributes().get(GEN_AI_SYSTEM),
            "Connect span should carry the VoiceLive gen_ai.system attribute");
        Assertions.assertTrue(connectSpan.hasEnded(), "Connect span should be ended (traced) after the session closes");

        // Identity attributes a consumer would filter/group telemetry on.
        Assertions.assertEquals(TEST_MODEL, connectSpan.getAttributes().get(GEN_AI_REQUEST_MODEL),
            "Connect span should record the requested model");
        Assertions.assertEquals("microsoft.foundry", connectSpan.getAttributes().get(GEN_AI_PROVIDER_NAME),
            "Connect span should record the gen_ai.provider.name");

        // Session-level counters are flushed onto the connect span on close (zero in this minimal flow, but present).
        Assertions.assertNotNull(connectSpan.getAttributes().get(GEN_AI_VOICE_AUDIO_BYTES_SENT),
            "Connect span should flush gen_ai.voice.audio_bytes_sent");
        Assertions.assertNotNull(connectSpan.getAttributes().get(GEN_AI_VOICE_AUDIO_BYTES_RECEIVED),
            "Connect span should flush gen_ai.voice.audio_bytes_received");

        // The session.update we sent should be traced as a child send span.
        boolean sessionUpdateSent = spans.stream()
            .anyMatch(s -> "send".equals(s.getAttributes().get(GEN_AI_OPERATION_NAME))
                && "session.update".equals(s.getAttributes().get(GEN_AI_VOICE_EVENT_TYPE)));
        Assertions.assertTrue(sessionUpdateSent, "Expected a send span with gen_ai.voice.event_type=session.update");

        // Closing the session should be traced as a close span.
        boolean closeTraced
            = spans.stream().anyMatch(s -> "close".equals(s.getAttributes().get(GEN_AI_OPERATION_NAME)));
        Assertions.assertTrue(closeTraced, "Expected a span with gen_ai.operation.name=close");

        // The session.updated event received over the socket should be traced as a child recv span.
        boolean sessionUpdatedTraced
            = spans.stream().anyMatch(s -> "session.updated".equals(s.getAttributes().get(GEN_AI_VOICE_EVENT_TYPE)));
        Assertions.assertTrue(sessionUpdatedTraced,
            "Expected a recv span with gen_ai.voice.event_type=session.updated");

        // The connect span is the session-lifetime parent, so it must end (and therefore be traced) last:
        // every child span (send/recv/close) ends at or before the connect span.
        long connectEnd = connectSpan.getEndEpochNanos();
        boolean connectEndsLast = spans.stream().allMatch(s -> s == connectSpan || s.getEndEpochNanos() <= connectEnd);
        Assertions.assertTrue(connectEndsLast, "Connect span should be the last traced event of the session");
    }

    static Stream<Arguments> audioParams() {
        return crossProduct(new String[] { "gpt-realtime", "gpt-4.1" }, API_VERSIONS);
    }

    /**
     * Drives a full audio turn (send audio, receive a model response) and asserts the telemetry that is only populated
     * once the model actually responds: response-level usage on the recv span and the session-level counters /
     * first-token latency flushed onto the connect span at close.
     */
    @ParameterizedTest
    @MethodSource("audioParams")
    @LiveOnly
    public void testAudioTurnTelemetry(String model, String apiVersion) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("4-1.wav");

        CountDownLatch responseDoneLatch = new CountDownLatch(1);
        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;

        try {
            session = client.startSession(model, null).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.RESPONSE_DONE) {
                    responseDoneLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseDoneLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseDoneLatch.countDown();
            });

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
                .setInputAudioFormat(InputAudioFormat.PCM16);
            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);

            Thread.sleep(SETUP_DELAY_MS);
            session.sendInputAudio(BinaryData.fromBytes(audioData)).block(SEND_TIMEOUT);

            Assertions.assertTrue(responseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Should receive response.done within timeout");
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }

        List<SpanData> spans = awaitConnectSpan();
        SpanData connectSpan = spans.stream()
            .filter(s -> "connect".equals(s.getAttributes().get(GEN_AI_OPERATION_NAME)))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected a span with gen_ai.operation.name=connect"));

        // Audio we sent should be counted and flushed onto the connect span at close.
        Long audioBytesSent = connectSpan.getAttributes().get(GEN_AI_VOICE_AUDIO_BYTES_SENT);
        Assertions.assertNotNull(audioBytesSent, "Connect span should flush gen_ai.voice.audio_bytes_sent");
        Assertions.assertTrue(audioBytesSent > 0,
            "Audio bytes sent should be greater than zero after sending input audio");

        // A completed turn should be counted.
        Long turnCount = connectSpan.getAttributes().get(GEN_AI_VOICE_TURN_COUNT);
        Assertions.assertNotNull(turnCount, "Connect span should flush gen_ai.voice.turn_count");
        Assertions.assertTrue(turnCount > 0, "Turn count should be greater than zero after a response");

        // Note: gen_ai.voice.first_token_latency_ms is only recorded for client-initiated responses (an explicit
        // response.create sets the start timestamp). This VAD-driven turn does not send one, so it is not asserted here.

        // The response.done event should produce a recv span carrying token usage and the response id.
        SpanData responseDoneSpan = spans.stream()
            .filter(s -> "response.done".equals(s.getAttributes().get(GEN_AI_VOICE_EVENT_TYPE)))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected a recv span for response.done"));
        Assertions.assertNotNull(responseDoneSpan.getAttributes().get(GEN_AI_USAGE_INPUT_TOKENS),
            "response.done span should record gen_ai.usage.input_tokens");
        Assertions.assertNotNull(responseDoneSpan.getAttributes().get(GEN_AI_USAGE_OUTPUT_TOKENS),
            "response.done span should record gen_ai.usage.output_tokens");
        Assertions.assertNotNull(responseDoneSpan.getAttributes().get(GEN_AI_RESPONSE_ID),
            "response.done span should record gen_ai.response.id");
    }

    private List<SpanData> awaitConnectSpan() throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            List<SpanData> spans = spanExporter.getFinishedSpanItems();
            boolean connectExported
                = spans.stream().anyMatch(s -> "connect".equals(s.getAttributes().get(GEN_AI_OPERATION_NAME)));
            if (connectExported) {
                return spans;
            }
            Thread.sleep(100);
        }
        return spanExporter.getFinishedSpanItems();
    }
}
