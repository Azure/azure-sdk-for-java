// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.ai.agents.models.RealtimeInputAudioBufferAppendEvent;
import com.azure.ai.agents.models.RealtimeResponseCreateEvent;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentTracerTests {
    private InMemorySpanExporter exporter;
    private SdkTracerProvider tracerProvider;
    private OpenTelemetrySdk openTelemetry;
    private VoiceAgentTracer tracer;

    @BeforeEach
    public void setup() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        tracer = new VoiceAgentTracer(openTelemetry.getTracer("test"), URI.create("wss://example.test/session"),
            "weather-agent", false);
    }

    @AfterEach
    public void cleanup() {
        tracerProvider.close();
        exporter.close();
    }

    @Test
    public void sessionSpanParentsEventsAndFlushesCounters() throws Exception {
        tracer.startConnectSpan();
        tracer.traceSend(new RealtimeResponseCreateEvent(), "{\"type\":\"response.create\"}");
        tracer.traceSend(new RealtimeInputAudioBufferAppendEvent("AQID"),
            "{\"type\":\"input_audio_buffer.append\",\"audio\":\"AQID\"}");
        tracer.traceReceive(
            event("{\"type\":\"session.created\",\"event_id\":\"e1\","
                + "\"conversation_id\":\"conversation-1\",\"session\":{\"id\":\"session-1\","
                + "\"model\":\"gpt-realtime\"}}"),
            "{\"type\":\"session.created\",\"event_id\":\"e1\",\"conversation_id\":\"conversation-1\","
                + "\"session\":{\"id\":\"session-1\",\"model\":\"gpt-realtime\"}}");
        tracer.traceReceive(event("{\"type\":\"response.output_audio.delta\",\"delta\":\"AQIDBA==\"}"),
            "{\"type\":\"response.output_audio.delta\",\"delta\":\"AQIDBA==\"}");
        tracer.traceReceive(event("{\"type\":\"response.done\",\"response\":{\"output\":[]}}"),
            "{\"type\":\"response.done\",\"response\":{\"output\":[]}}");
        tracer.traceClose();
        tracer.traceClose();
        tracer.endConnectSpan(null);

        List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(6, spans.size());
        assertFalse(spans.stream().anyMatch(span -> "recv response.output_audio.delta".equals(span.getName())));
        SpanData connect = spans.get(spans.size() - 1);
        assertEquals("connect", connect.getName());
        assertEquals(SpanKind.CLIENT, connect.getKind());
        assertEquals("az.ai.agents", connect.getAttributes().get(VoiceAgentTracer.GEN_AI_SYSTEM));
        assertEquals("Microsoft.CognitiveServices", connect.getAttributes().get(VoiceAgentTracer.AZ_NAMESPACE));
        assertEquals(443L, connect.getAttributes().get(VoiceAgentTracer.SERVER_PORT));
        assertEquals("weather-agent", connect.getAttributes().get(VoiceAgentTracer.GEN_AI_AGENT_NAME));
        assertEquals("session-1", connect.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_SESSION_ID));
        assertEquals("gpt-realtime", connect.getAttributes().get(VoiceAgentTracer.GEN_AI_REQUEST_MODEL));
        assertEquals("conversation-1", connect.getAttributes().get(VoiceAgentTracer.GEN_AI_CONVERSATION_ID));
        assertEquals(3L, connect.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_AUDIO_BYTES_SENT));
        assertEquals(4L, connect.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_AUDIO_BYTES_RECEIVED));
        assertEquals(1L, connect.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_TURN_COUNT));

        for (int index = 0; index < spans.size() - 1; index++) {
            SpanData child = spans.get(index);
            assertEquals(SpanKind.CLIENT, child.getKind());
            assertEquals(connect.getSpanContext().getTraceId(), child.getSpanContext().getTraceId());
            assertEquals(connect.getSpanContext().getSpanId(), child.getParentSpanId());
            assertTrue(child.getEvents().isEmpty(), "Payload events must be suppressed by default.");
        }
    }

    @Test
    public void rawEventsUsePayloadType() {
        tracer.startConnectSpan();
        tracer.traceSendRaw("{\"type\":\"custom.event\",\"value\":1}");
        tracer.traceReceiveRaw("{\"type\":\"future.event\",\"value\":2}");
        tracer.traceReceiveRaw("{\"type\":\"rate_limits.updated\",\"rate_limits\":[{\"name\":\"tokens\"}]}");
        tracer.endConnectSpan(null);

        SpanData send = exporter.getFinishedSpanItems().get(0);
        assertEquals("send custom.event", send.getName());
        assertEquals("custom.event", send.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_EVENT_TYPE));
        assertTrue(send.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_MESSAGE_SIZE) > 0);

        SpanData receive = exporter.getFinishedSpanItems().get(1);
        assertEquals("recv future.event", receive.getName());
        assertEquals("future.event", receive.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_EVENT_TYPE));
        assertTrue(receive.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_MESSAGE_SIZE) > 0);

        SpanData rateLimits = exporter.getFinishedSpanItems().get(2);
        assertTrue(rateLimits.getEvents()
            .stream()
            .anyMatch(event -> VoiceAgentTracer.RATE_LIMITS_EVENT.equals(event.getName())
                && event.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_RATE_LIMITS).contains("tokens")));
    }

    @Test
    public void highVolumeRawReceiveDeltasDoNotCreateSpans() {
        tracer.startConnectSpan();
        tracer.traceReceiveRaw("{\"type\":\"response.output_text.delta\",\"delta\":\"hello\"}");
        tracer.traceReceiveRaw("{\"type\":\"response.output_audio_transcript.delta\",\"delta\":\"hello\"}");
        tracer.traceReceiveRaw("{\"type\":\"conversation.item.input_audio_transcription.delta\",\"delta\":\"hello\"}");
        tracer.traceReceiveRaw("{\"type\":\"response.output_audio.delta\",\"delta\":\"AQID\"}");
        tracer.endConnectSpan(null);

        List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(1, spans.size());
        assertEquals("connect", spans.get(0).getName());
    }

    @Test
    public void responseTelemetryTracksUsageAndLatestTurnLatency() throws Exception {
        AtomicLong nanoTime = new AtomicLong(1_000_000);
        VoiceAgentTracer timedTracer = new VoiceAgentTracer(openTelemetry.getTracer("test"),
            URI.create("wss://example.test/session"), "weather-agent", false, nanoTime::get);
        timedTracer.startConnectSpan();

        timedTracer.traceSend(new RealtimeResponseCreateEvent(), "{\"type\":\"response.create\"}");
        nanoTime.set(26_000_000);
        timedTracer.traceReceiveRaw("{\"type\":\"response.output_text.delta\",\"delta\":\"hello\"}");
        timedTracer.traceReceive(event(
            "{\"type\":\"response.done\",\"response\":{" + "\"id\":\"response-1\",\"status\":\"completed\",\"usage\":{"
                + "\"input_tokens\":12,\"output_tokens\":7},\"output\":[]}}"),
            "{\"type\":\"response.done\"}");

        nanoTime.set(100_000_000);
        timedTracer.traceSend(new RealtimeResponseCreateEvent(), "{\"type\":\"response.create\"}");
        nanoTime.set(142_000_000);
        timedTracer.traceReceiveRaw("{\"type\":\"response.output_audio.delta\",\"delta\":\"AQID\"}");
        timedTracer.traceReceive(
            event("{\"type\":\"response.done\",\"response\":{"
                + "\"id\":\"response-2\",\"status\":\"failed\",\"status_details\":{"
                + "\"type\":\"failed\",\"reason\":\"content_filter\"},\"output\":[]}}"),
            "{\"type\":\"response.done\"}");
        timedTracer.endConnectSpan(null);

        List<SpanData> spans = exporter.getFinishedSpanItems();
        SpanData firstDone = spans.stream()
            .filter(span -> "recv response.done".equals(span.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected a response.done span."));
        assertEquals("response-1", firstDone.getAttributes().get(VoiceAgentTracer.GEN_AI_RESPONSE_ID));
        assertEquals(12L, firstDone.getAttributes().get(VoiceAgentTracer.GEN_AI_USAGE_INPUT_TOKENS));
        assertEquals(7L, firstDone.getAttributes().get(VoiceAgentTracer.GEN_AI_USAGE_OUTPUT_TOKENS));

        SpanData connect = spans.get(spans.size() - 1);
        assertEquals(2L, connect.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_TURN_COUNT));
        assertEquals(42D, connect.getAttributes().get(VoiceAgentTracer.GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS));
        assertEquals("response-2", connect.getAttributes().get(VoiceAgentTracer.GEN_AI_RESPONSE_ID));
        assertEquals(Collections.singletonList("content_filter"),
            connect.getAttributes().get(VoiceAgentTracer.GEN_AI_RESPONSE_FINISH_REASONS));
    }

    @Test
    public void connectionFailureIsRecordedOnce() {
        tracer.startConnectSpan();
        RuntimeException failure = new RuntimeException("connection lost");
        tracer.endConnectSpan(failure);
        tracer.endConnectSpan(new RuntimeException("duplicate"));

        List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(1, spans.size());
        SpanData connect = spans.get(0);
        assertEquals(StatusCode.ERROR, connect.getStatus().getStatusCode());
        assertEquals(RuntimeException.class.getName(), connect.getAttributes().get(VoiceAgentTracer.ERROR_TYPE));
        assertFalse(connect.getEvents().isEmpty());
        assertNotNull(connect.getEndEpochNanos());
    }

    @Test
    public void serviceErrorRecordsStatusAndDetails() throws Exception {
        tracer.startConnectSpan();
        tracer.traceReceive(event("{\"type\":\"error\",\"event_id\":\"e1\",\"error\":{"
            + "\"type\":\"invalid_request_error\",\"code\":\"invalid_value\","
            + "\"message\":\"Invalid request\",\"param\":\"audio\"}}"), "{\"type\":\"error\"}");
        tracer.endConnectSpan(null);

        SpanData receive = exporter.getFinishedSpanItems().get(0);
        assertEquals(StatusCode.ERROR, receive.getStatus().getStatusCode());
        assertEquals("", receive.getStatus().getDescription());
        assertEquals("invalid_request_error", receive.getAttributes().get(VoiceAgentTracer.ERROR_TYPE));
        assertEquals("invalid_value", receive.getAttributes().get(VoiceAgentTracer.ERROR_CODE));
        assertNull(receive.getAttributes().get(VoiceAgentTracer.ERROR_MESSAGE));
        assertTrue(
            receive.getEvents().stream().anyMatch(event -> VoiceAgentTracer.ERROR_EVENT.equals(event.getName())));
    }

    @Test
    public void serviceErrorIncludesMessageWhenContentRecordingIsEnabled() throws Exception {
        VoiceAgentTracer contentTracer = new VoiceAgentTracer(openTelemetry.getTracer("test"),
            URI.create("wss://example.test/session"), "weather-agent", true);
        contentTracer.startConnectSpan();
        contentTracer.traceReceive(event("{\"type\":\"error\",\"event_id\":\"e1\",\"error\":{"
            + "\"type\":\"invalid_request_error\",\"message\":\"Sensitive detail\"}}"), "{\"type\":\"error\"}");
        contentTracer.endConnectSpan(null);

        SpanData receive = exporter.getFinishedSpanItems().get(0);
        assertEquals("Sensitive detail", receive.getStatus().getDescription());
        assertEquals("Sensitive detail", receive.getAttributes().get(VoiceAgentTracer.ERROR_MESSAGE));
    }

    @Test
    public void wsEndpointUsesDefaultPort() {
        VoiceAgentTracer wsTracer = new VoiceAgentTracer(openTelemetry.getTracer("test"),
            URI.create("ws://example.test/session"), "weather-agent", false);
        wsTracer.startConnectSpan();
        wsTracer.endConnectSpan(null);

        SpanData connect = exporter.getFinishedSpanItems().get(0);
        assertEquals(80L, connect.getAttributes().get(VoiceAgentTracer.SERVER_PORT));
    }

    @Test
    public void standardContentRecordingSettingTakesPrecedence() {
        Configuration standardEnabled
            = new ConfigurationBuilder().putProperty("otel.instrumentation.genai.capture.message.content", "true")
                .build();
        Configuration legacyEnabled
            = new ConfigurationBuilder().putProperty("azure.tracing.gen_ai.content_recording_enabled", "true").build();
        Configuration standardDisabled
            = new ConfigurationBuilder().putProperty("otel.instrumentation.genai.capture.message.content", "false")
                .putProperty("azure.tracing.gen_ai.content_recording_enabled", "true")
                .build();

        assertTrue(VoiceAgentTracer.isContentRecordingEnabled(standardEnabled));
        assertTrue(VoiceAgentTracer.isContentRecordingEnabled(legacyEnabled));
        assertFalse(VoiceAgentTracer.isContentRecordingEnabled(standardDisabled));
        assertFalse(VoiceAgentTracer.isContentRecordingEnabled(Configuration.NONE));
    }

    private static RealtimeServerEvent event(String json) throws Exception {
        return VoiceAgentWebSocketUtils.deserializeEvent(json);
    }
}
