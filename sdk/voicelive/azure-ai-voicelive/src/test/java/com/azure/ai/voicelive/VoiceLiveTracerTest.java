// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventInputAudioBufferAppend;
import com.azure.ai.voicelive.models.ClientEventResponseCancel;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.SessionUpdate;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link VoiceLiveTracer} using OpenTelemetry SDK testing utilities.
 */
class VoiceLiveTracerTest {

    private InMemorySpanExporter spanExporter;
    private SdkTracerProvider tracerProvider;
    private Tracer tracer;
    private VoiceLiveTracer voiceLiveTracer;

    @BeforeEach
    void setUp() throws Exception {
        spanExporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(spanExporter)).build();
        tracer = tracerProvider.get("azure-ai-voicelive", "1.0.0-beta.6");
        URI endpoint = new URI("wss://test.cognitiveservices.azure.com/voice-live/realtime");
        voiceLiveTracer = new VoiceLiveTracer(tracer, endpoint, "gpt-4o-realtime-preview", null);
    }

    @AfterEach
    void tearDown() {
        tracerProvider.close();
    }

    @Test
    void testConnectSpanCreated() {
        voiceLiveTracer.startConnectSpan();
        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        assertEquals(1, spans.size());
        SpanData span = spans.get(0);
        assertEquals("connect gpt-4o-realtime-preview", span.getName());
        assertEquals(SpanKind.CLIENT, span.getKind());
        assertEquals("az.ai.voicelive", span.getAttributes().get(AttributeKey.stringKey("gen_ai.system")));
        assertEquals("connect", span.getAttributes().get(AttributeKey.stringKey("gen_ai.operation.name")));
        assertEquals("Microsoft.CognitiveServices", span.getAttributes().get(AttributeKey.stringKey("az.namespace")));
        assertEquals("gpt-4o-realtime-preview",
            span.getAttributes().get(AttributeKey.stringKey("gen_ai.request.model")));
        assertEquals("test.cognitiveservices.azure.com",
            span.getAttributes().get(AttributeKey.stringKey("server.address")));
    }

    @Test
    void testSendSpanCreated() {
        voiceLiveTracer.startConnectSpan();

        ClientEventResponseCreate event = new ClientEventResponseCreate();
        voiceLiveTracer.traceSend(event, "{\"type\":\"response.create\"}");

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        assertEquals(2, spans.size());

        // Send span finishes first
        SpanData sendSpan = spans.get(0);
        assertEquals("send response.create", sendSpan.getName());
        assertEquals(SpanKind.CLIENT, sendSpan.getKind());
        assertEquals("send", sendSpan.getAttributes().get(AttributeKey.stringKey("gen_ai.operation.name")));
        assertEquals("response.create",
            sendSpan.getAttributes().get(AttributeKey.stringKey("gen_ai.voice.event_type")));

        // Send span should be a child of the connect span
        SpanData connectSpan = spans.get(1);
        assertEquals(connectSpan.getSpanContext().getTraceId(), sendSpan.getSpanContext().getTraceId());
        assertEquals(connectSpan.getSpanContext().getSpanId(), sendSpan.getParentSpanId());
    }

    @Test
    void testRecvSpanCreated() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String json = "{\"type\":\"session.created\",\"event_id\":\"evt1\","
            + "\"session\":{\"id\":\"sess_123\",\"model\":\"gpt-4o\"}}";
        SessionUpdate update = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(json));
        voiceLiveTracer.traceRecv(update, json);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        assertEquals(2, spans.size());

        SpanData recvSpan = spans.get(0);
        assertEquals("recv session.created", recvSpan.getName());
        assertEquals("recv", recvSpan.getAttributes().get(AttributeKey.stringKey("gen_ai.operation.name")));

        // Verify parent-child relationship
        SpanData connectSpan = spans.get(1);
        assertEquals(connectSpan.getSpanContext().getSpanId(), recvSpan.getParentSpanId());
    }

    @Test
    void testCloseSpanCreated() {
        voiceLiveTracer.startConnectSpan();
        voiceLiveTracer.traceClose();
        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        assertEquals(2, spans.size());

        SpanData closeSpan = spans.get(0);
        assertEquals("close", closeSpan.getName());
        assertEquals("close", closeSpan.getAttributes().get(AttributeKey.stringKey("gen_ai.operation.name")));
    }

    @Test
    void testSessionCountersOnEndConnectSpan() {
        voiceLiveTracer.startConnectSpan();

        // Simulate a response.cancel (interruption)
        ClientEventResponseCancel cancelEvent = new ClientEventResponseCancel();
        voiceLiveTracer.traceSend(cancelEvent, "{\"type\":\"response.cancel\"}");

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        // Find the connect span (last to finish)
        SpanData connectSpan = spans.get(spans.size() - 1);
        assertEquals(1L,
            connectSpan.getAttributes().get(AttributeKey.longKey("gen_ai.voice.interruption_count")).longValue());
    }

    @Test
    void testResponseCreateTracksLatency() throws Exception {
        voiceLiveTracer.startConnectSpan();

        // Send response.create
        ClientEventResponseCreate createEvent = new ClientEventResponseCreate();
        voiceLiveTracer.traceSend(createEvent, "{\"type\":\"response.create\"}");

        // Simulate small delay and receive audio delta
        Thread.sleep(10);
        String audioJson = "{\"type\":\"response.audio.delta\",\"response_id\":\"r1\","
            + "\"item_id\":\"i1\",\"output_index\":0,\"content_index\":0,\"delta\":\"AQID\"}";
        SessionUpdate audioDelta = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(audioJson));
        voiceLiveTracer.traceRecv(audioDelta, audioJson);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(spans.size() - 1);
        Long latency = connectSpan.getAttributes().get(AttributeKey.longKey("gen_ai.voice.first_token_latency_ms"));
        assertNotNull(latency);
        assertTrue(latency >= 0, "Latency should be >= 0, was: " + latency);
    }

    @Test
    void testAudioBytesTracking() throws Exception {
        voiceLiveTracer.startConnectSpan();

        // Send audio (base64 of 3 bytes: [1,2,3] = "AQID")
        ClientEventInputAudioBufferAppend appendEvent = new ClientEventInputAudioBufferAppend("AQID");
        voiceLiveTracer.traceSend(appendEvent, "{\"type\":\"input_audio_buffer.append\",\"audio\":\"AQID\"}");

        // Receive audio delta (base64 "AQIDBA==" = 4 bytes)
        String audioJson = "{\"type\":\"response.audio.delta\",\"response_id\":\"r1\","
            + "\"item_id\":\"i1\",\"output_index\":0,\"content_index\":0,\"delta\":\"AQIDBA==\"}";
        SessionUpdate audioDelta = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(audioJson));
        voiceLiveTracer.traceRecv(audioDelta, audioJson);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(spans.size() - 1);
        assertEquals(3L,
            connectSpan.getAttributes().get(AttributeKey.longKey("gen_ai.voice.audio_bytes_sent")).longValue());
        assertEquals(4L,
            connectSpan.getAttributes().get(AttributeKey.longKey("gen_ai.voice.audio_bytes_received")).longValue());
    }

    @Test
    void testTurnCountTracking() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String doneJson = "{\"type\":\"response.done\",\"event_id\":\"evt1\","
            + "\"response\":{\"id\":\"r1\",\"status\":\"completed\",\"output\":[]}}";
        SessionUpdate responseDone = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(doneJson));
        voiceLiveTracer.traceRecv(responseDone, doneJson);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(spans.size() - 1);
        assertEquals(1L, connectSpan.getAttributes().get(AttributeKey.longKey("gen_ai.voice.turn_count")).longValue());
    }

    @Test
    void testTokenUsageOnResponseDone() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String doneJson = "{\"type\":\"response.done\",\"event_id\":\"evt1\","
            + "\"response\":{\"id\":\"r1\",\"status\":\"completed\",\"output\":[],"
            + "\"usage\":{\"total_tokens\":150,\"input_tokens\":100,\"output_tokens\":50,"
            + "\"input_token_details\":{\"cached_tokens\":0,\"text_tokens\":50,\"audio_tokens\":50},"
            + "\"output_token_details\":{\"text_tokens\":25,\"audio_tokens\":25}}}}";
        SessionUpdate responseDone = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(doneJson));
        voiceLiveTracer.traceRecv(responseDone, doneJson);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        // Recv span is first to finish
        SpanData recvSpan = spans.get(0);
        assertEquals(100L, recvSpan.getAttributes().get(AttributeKey.longKey("gen_ai.usage.input_tokens")).longValue());
        assertEquals(50L, recvSpan.getAttributes().get(AttributeKey.longKey("gen_ai.usage.output_tokens")).longValue());
    }

    @Test
    void testErrorEventTracking() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String errorJson = "{\"type\":\"error\",\"event_id\":\"evt1\","
            + "\"error\":{\"type\":\"server_error\",\"code\":\"500\",\"message\":\"Internal error\"}}";
        SessionUpdate errorUpdate = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(errorJson));
        voiceLiveTracer.traceRecv(errorUpdate, errorJson);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData recvSpan = spans.get(0);

        // Verify error status is set on the span
        assertEquals(StatusCode.ERROR, recvSpan.getStatus().getStatusCode());
        assertEquals("Internal error", recvSpan.getStatus().getDescription());
        assertEquals("server_error", recvSpan.getAttributes().get(AttributeKey.stringKey("error.type")));

        // Verify error event was added
        List<EventData> events = recvSpan.getEvents();
        EventData errorEvent = events.stream()
            .filter(e -> "gen_ai.voice.error".equals(e.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected gen_ai.voice.error event"));
        assertEquals("500", errorEvent.getAttributes().get(AttributeKey.stringKey("error.code")));
        assertEquals("Internal error", errorEvent.getAttributes().get(AttributeKey.stringKey("error.message")));
    }

    @Test
    void testConnectSpanErrorStatus() {
        voiceLiveTracer.startConnectSpan();
        voiceLiveTracer.endConnectSpan(new RuntimeException("Connection lost"));

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(0);
        assertEquals(StatusCode.ERROR, connectSpan.getStatus().getStatusCode());
        assertEquals("Connection lost", connectSpan.getStatus().getDescription());
        assertEquals("java.lang.RuntimeException",
            connectSpan.getAttributes().get(AttributeKey.stringKey("error.type")));

        // Verify exception was recorded
        assertTrue(connectSpan.getEvents().stream().anyMatch(e -> "exception".equals(e.getName())));
    }

    @Test
    void testSessionIdFromSessionCreated() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String json = "{\"type\":\"session.created\",\"event_id\":\"evt1\","
            + "\"session\":{\"id\":\"sess_abc123\",\"model\":\"gpt-4o\"}}";
        SessionUpdate update = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(json));
        voiceLiveTracer.traceRecv(update, json);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(spans.size() - 1);
        assertEquals("sess_abc123", connectSpan.getAttributes().get(AttributeKey.stringKey("gen_ai.voice.session_id")));
    }

    @Test
    void testConnectSpanWithoutModel() throws Exception {
        VoiceLiveTracer tracerNoModel
            = new VoiceLiveTracer(tracer, new URI("wss://test.cognitiveservices.azure.com"), null, null);

        tracerNoModel.startConnectSpan();
        tracerNoModel.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData span = spans.get(0);
        assertEquals("connect", span.getName());
        assertFalse(span.getAttributes().asMap().containsKey(AttributeKey.stringKey("gen_ai.request.model")));
    }

    @Test
    void testParentChildSpanHierarchy() throws Exception {
        voiceLiveTracer.startConnectSpan();

        // Send
        ClientEventResponseCreate event = new ClientEventResponseCreate();
        voiceLiveTracer.traceSend(event, "{\"type\":\"response.create\"}");

        // Recv
        String json = "{\"type\":\"session.created\",\"event_id\":\"evt1\","
            + "\"session\":{\"id\":\"sess_123\",\"model\":\"gpt-4o\"}}";
        SessionUpdate update = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(json));
        voiceLiveTracer.traceRecv(update, json);

        // Close
        voiceLiveTracer.traceClose();
        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        assertEquals(4, spans.size());

        // All child spans share the same trace ID and reference the connect span as parent
        SpanData connectSpan = spans.get(spans.size() - 1);
        String traceId = connectSpan.getSpanContext().getTraceId();
        String connectSpanId = connectSpan.getSpanContext().getSpanId();

        for (int i = 0; i < spans.size() - 1; i++) {
            SpanData child = spans.get(i);
            assertEquals(traceId, child.getSpanContext().getTraceId(), "Child span should be in same trace");
            assertEquals(connectSpanId, child.getParentSpanId(), "Child span should have connect as parent");
        }
    }

    @Test
    void testBuilderWithOpenTelemetry() {
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder().endpoint("https://test.cognitiveservices.azure.com")
            .credential(new com.azure.core.credential.KeyCredential("fake"))
            .openTelemetry(io.opentelemetry.api.OpenTelemetry.noop())
            .buildAsyncClient();

        assertNotNull(client);
    }

    @Test
    void testSpanEventsContainAttributes() {
        voiceLiveTracer.startConnectSpan();

        ClientEventResponseCreate event = new ClientEventResponseCreate();
        voiceLiveTracer.traceSend(event, "{\"type\":\"response.create\"}");

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData sendSpan = spans.get(0);
        List<EventData> events = sendSpan.getEvents();
        assertFalse(events.isEmpty());

        EventData inputEvent = events.stream()
            .filter(e -> "gen_ai.input.messages".equals(e.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected gen_ai.input.messages event"));
        assertEquals("az.ai.voicelive", inputEvent.getAttributes().get(AttributeKey.stringKey("gen_ai.system")));
        assertEquals("response.create",
            inputEvent.getAttributes().get(AttributeKey.stringKey("gen_ai.voice.event_type")));
    }
}
