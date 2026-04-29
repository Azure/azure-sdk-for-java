// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.implementation;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveClientBuilder;
import com.azure.ai.voicelive.models.AgentSessionConfig;
import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventInputAudioBufferAppend;
import com.azure.ai.voicelive.models.ClientEventResponseCancel;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.FunctionCallOutputItem;
import com.azure.ai.voicelive.models.SessionUpdate;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
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
    private SdkMeterProvider meterProvider;
    private Tracer tracer;
    private VoiceLiveTracer voiceLiveTracer;

    @BeforeEach
    void setUp() throws Exception {
        spanExporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(spanExporter)).build();
        meterProvider = SdkMeterProvider.builder().build();
        tracer = tracerProvider.get("azure-ai-voicelive", "1.0.0-beta.6");
        Meter meter = meterProvider.get("azure-ai-voicelive");
        URI endpoint = new URI("wss://test.cognitiveservices.azure.com/voice-live/realtime");
        voiceLiveTracer = new VoiceLiveTracer(tracer, meter, endpoint, "gpt-4o-realtime-preview", null);
    }

    @AfterEach
    void tearDown() {
        tracerProvider.close();
        meterProvider.close();
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
        assertEquals(VoiceLiveTracer.GEN_AI_SYSTEM_VALUE, span.getAttributes().get(VoiceLiveTracer.GEN_AI_SYSTEM));
        assertEquals(VoiceLiveTracer.OPERATION_CONNECT,
            span.getAttributes().get(VoiceLiveTracer.GEN_AI_OPERATION_NAME));
        assertEquals(VoiceLiveTracer.AZ_NAMESPACE_VALUE, span.getAttributes().get(VoiceLiveTracer.AZ_NAMESPACE));
        assertEquals(VoiceLiveTracer.GEN_AI_PROVIDER_NAME_VALUE,
            span.getAttributes().get(VoiceLiveTracer.GEN_AI_PROVIDER_NAME));
        assertEquals("gpt-4o-realtime-preview", span.getAttributes().get(VoiceLiveTracer.GEN_AI_REQUEST_MODEL));
        assertEquals("test.cognitiveservices.azure.com", span.getAttributes().get(VoiceLiveTracer.SERVER_ADDRESS));
        assertEquals(443L, span.getAttributes().get(VoiceLiveTracer.SERVER_PORT).longValue());
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
        assertEquals(VoiceLiveTracer.OPERATION_SEND,
            sendSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_OPERATION_NAME));
        assertEquals("response.create", sendSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_EVENT_TYPE));

        // Send span should be a child of the connect span
        SpanData connectSpan = spans.get(1);
        assertEquals(connectSpan.getSpanContext().getTraceId(), sendSpan.getSpanContext().getTraceId());
        assertEquals(connectSpan.getSpanContext().getSpanId(), sendSpan.getParentSpanId());
    }

    @Test
    void testRecvSpanCreated() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String json = "{\"type\":\"session.created\",\"event_id\":\"event1\","
            + "\"session\":{\"id\":\"session123\",\"model\":\"gpt-4o\"}}";
        SessionUpdate update = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(json));
        voiceLiveTracer.traceRecv(update, json);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        assertEquals(2, spans.size());

        SpanData recvSpan = spans.get(0);
        assertEquals("recv session.created", recvSpan.getName());
        assertEquals(VoiceLiveTracer.OPERATION_RECV,
            recvSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_OPERATION_NAME));

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
        assertEquals(VoiceLiveTracer.OPERATION_CLOSE,
            closeSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_OPERATION_NAME));
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
        assertEquals(1L, connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_INTERRUPTION_COUNT).longValue());
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
        Double latency = connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_FIRST_TOKEN_LATENCY_MS);
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
        assertEquals(3L, connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_AUDIO_BYTES_SENT).longValue());
        assertEquals(4L,
            connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_AUDIO_BYTES_RECEIVED).longValue());
    }

    @Test
    void testTurnCountTracking() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String doneJson = "{\"type\":\"response.done\",\"event_id\":\"event1\","
            + "\"response\":{\"id\":\"response1\",\"status\":\"completed\",\"output\":[]}}";
        SessionUpdate responseDone = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(doneJson));
        voiceLiveTracer.traceRecv(responseDone, doneJson);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(spans.size() - 1);
        assertEquals(1L, connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_TURN_COUNT).longValue());
    }

    @Test
    void testTokenUsageOnResponseDone() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String doneJson = "{\"type\":\"response.done\",\"event_id\":\"event1\","
            + "\"response\":{\"id\":\"response1\",\"status\":\"completed\",\"output\":[],"
            + "\"usage\":{\"total_tokens\":150,\"input_tokens\":100,\"output_tokens\":50,"
            + "\"input_token_details\":{\"cached_tokens\":0,\"text_tokens\":50,\"audio_tokens\":50},"
            + "\"output_token_details\":{\"text_tokens\":25,\"audio_tokens\":25}}}}";
        SessionUpdate responseDone = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(doneJson));
        voiceLiveTracer.traceRecv(responseDone, doneJson);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        // Recv span is first to finish
        SpanData recvSpan = spans.get(0);
        assertEquals(100L, recvSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_USAGE_INPUT_TOKENS).longValue());
        assertEquals(50L, recvSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_USAGE_OUTPUT_TOKENS).longValue());
    }

    @Test
    void testErrorEventTracking() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String errorJson = "{\"type\":\"error\",\"event_id\":\"event1\","
            + "\"error\":{\"type\":\"server_error\",\"code\":\"500\",\"message\":\"Internal error\"}}";
        SessionUpdate errorUpdate = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(errorJson));
        voiceLiveTracer.traceRecv(errorUpdate, errorJson);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData recvSpan = spans.get(0);

        assertEquals(StatusCode.UNSET, recvSpan.getStatus().getStatusCode());

        List<EventData> events = recvSpan.getEvents();
        EventData errorEvent = events.stream()
            .filter(e -> VoiceLiveTracer.GEN_AI_VOICE_ERROR.equals(e.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected gen_ai.voice.error event"));
        assertEquals("500", errorEvent.getAttributes().get(VoiceLiveTracer.ERROR_CODE));
        assertEquals("Internal error", errorEvent.getAttributes().get(VoiceLiveTracer.ERROR_MESSAGE));
    }

    @Test
    void testConnectSpanErrorStatus() {
        voiceLiveTracer.startConnectSpan();
        voiceLiveTracer.endConnectSpan(new RuntimeException("Connection lost"));

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(0);
        assertEquals(StatusCode.ERROR, connectSpan.getStatus().getStatusCode());
        assertEquals("Connection lost", connectSpan.getStatus().getDescription());
        assertEquals("java.lang.RuntimeException", connectSpan.getAttributes().get(VoiceLiveTracer.ERROR_TYPE));

        // Verify exception was recorded
        assertTrue(connectSpan.getEvents().stream().anyMatch(e -> "exception".equals(e.getName())));
    }

    @Test
    void testSessionIdFromSessionCreated() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String json = "{\"type\":\"session.created\",\"event_id\":\"event1\","
            + "\"session\":{\"id\":\"session456\",\"model\":\"gpt-4o\"}}";
        SessionUpdate update = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(json));
        voiceLiveTracer.traceRecv(update, json);

        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(spans.size() - 1);
        assertEquals("session456", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_SESSION_ID));
    }

    @Test
    void testConnectSpanWithoutModel() throws Exception {
        VoiceLiveTracer tracerNoModel = new VoiceLiveTracer(tracer, meterProvider.get("test"),
            new URI("wss://test.cognitiveservices.azure.com"), null, null);

        tracerNoModel.startConnectSpan();
        tracerNoModel.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData span = spans.get(0);
        assertEquals(VoiceLiveTracer.OPERATION_CONNECT, span.getName());
        assertFalse(span.getAttributes().asMap().containsKey(VoiceLiveTracer.GEN_AI_REQUEST_MODEL));
    }

    @Test
    void testParentChildSpanHierarchy() throws Exception {
        voiceLiveTracer.startConnectSpan();

        // Send
        ClientEventResponseCreate event = new ClientEventResponseCreate();
        voiceLiveTracer.traceSend(event, "{\"type\":\"response.create\"}");

        // Recv
        String json = "{\"type\":\"session.created\",\"event_id\":\"event1\","
            + "\"session\":{\"id\":\"session123\",\"model\":\"gpt-4o\"}}";
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
    void testBuilderWithGlobalOpenTelemetry() {
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder().endpoint("https://test.cognitiveservices.azure.com")
            .credential(new com.azure.core.credential.KeyCredential("fake"))
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
            .filter(e -> VoiceLiveTracer.GEN_AI_INPUT_MESSAGES.equals(e.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected gen_ai.input.messages event"));
        assertEquals(VoiceLiveTracer.GEN_AI_SYSTEM_VALUE,
            inputEvent.getAttributes().get(VoiceLiveTracer.GEN_AI_SYSTEM));
        assertEquals("response.create", inputEvent.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_EVENT_TYPE));
    }

    @Test
    void testResponseDoneTracksConversationAndFinishReason() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String doneJson = "{\"type\":\"response.done\",\"event_id\":\"event1\","
            + "\"response\":{\"id\":\"response1\",\"conversation_id\":\"conversation1\","
            + "\"status\":\"completed\",\"output\":[]}}";
        SessionUpdate responseDone = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(doneJson));
        voiceLiveTracer.traceRecv(responseDone, doneJson);
        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData recvSpan = spans.get(0);
        SpanData connectSpan = spans.get(1);

        assertEquals("response1", recvSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_RESPONSE_ID));
        assertEquals("conversation1", recvSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_CONVERSATION_ID));
        assertEquals("[\"completed\"]", recvSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_RESPONSE_FINISH_REASONS));
        assertEquals("conversation1", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_CONVERSATION_ID));
        // Verify accumulated lastResponseId and lastFinishReasons are flushed to the connect span
        assertEquals("response1", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_RESPONSE_ID));
        assertEquals("[\"completed\"]",
            connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_RESPONSE_FINISH_REASONS));
    }

    @Test
    void testFunctionCallOutputSendTracksIds() {
        voiceLiveTracer.startConnectSpan();

        ClientEventConversationItemCreate event
            = new ClientEventConversationItemCreate().setPreviousItemId("previousItem1")
                .setItem(new FunctionCallOutputItem("call123", "{\"ok\":true}"));
        voiceLiveTracer.traceSend(event,
            "{\"type\":\"conversation.item.create\",\"previous_item_id\":\"previousItem1\","
                + "\"item\":{\"type\":\"function_call_output\",\"call_id\":\"call123\","
                + "\"output\":\"{\\\"ok\\\":true}\"}}");

        voiceLiveTracer.endConnectSpan(null);

        SpanData sendSpan = spanExporter.getFinishedSpanItems().get(0);
        assertEquals("call123", sendSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_CALL_ID));
        assertEquals("previousItem1", sendSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_PREVIOUS_ITEM_ID));
    }

    @Test
    void testSessionUpdateTracksConnectSpanConfig() {
        voiceLiveTracer.startConnectSpan();

        String updateJson = "{\"type\":\"session.update\",\"session\":{" + "\"instructions\":\"You are concise.\","
            + "\"temperature\":0.2," + "\"max_response_output_tokens\":256," + "\"input_audio_sampling_rate\":24000,"
            + "\"input_audio_format\":\"pcm16\"," + "\"output_audio_format\":\"pcm16\","
            + "\"tools\":[{\"type\":\"function\",\"name\":\"get_weather\"}]}}";
        voiceLiveTracer.traceSend(new ClientEventResponseCreate(), updateJson);
        voiceLiveTracer.endConnectSpan(null);

        SpanData connectSpan = spanExporter.getFinishedSpanItems().get(1);
        assertEquals("You are concise.", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_SYSTEM_INSTRUCTIONS));
        assertEquals("0.2", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_REQUEST_TEMPERATURE));
        assertEquals("256", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_REQUEST_MAX_OUTPUT_TOKENS));
        assertEquals("pcm16", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_INPUT_AUDIO_FORMAT));
        assertEquals(24000L,
            connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_INPUT_SAMPLE_RATE).longValue());
        assertTrue(connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_REQUEST_TOOLS).contains("get_weather"));
    }

    @Test
    void testSessionCreatedTracksAgentAttributes() throws Exception {
        voiceLiveTracer.startConnectSpan(new AgentSessionConfig("agent-name", "project-name").setAgentVersion("1.2.3")
            .setConversationId("clientConversation1"));

        String json = "{\"type\":\"session.created\",\"event_id\":\"event1\",\"session\":{"
            + "\"id\":\"session123\",\"input_audio_sampling_rate\":24000,"
            + "\"agent\":{\"agent_id\":\"agent123\",\"thread_id\":\"thread456\"}}}";
        SessionUpdate update = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(json));
        voiceLiveTracer.traceRecv(update, json);
        voiceLiveTracer.endConnectSpan(null);

        SpanData connectSpan = spanExporter.getFinishedSpanItems().get(1);
        assertEquals("agent-name", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_AGENT_NAME));
        assertEquals("1.2.3", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_AGENT_VERSION));
        assertEquals("project-name", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_AGENT_PROJECT_NAME));
        assertEquals("agent123", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_AGENT_ID));
        assertEquals("thread456", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_AGENT_THREAD_ID));
        assertEquals("clientConversation1", connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_CONVERSATION_ID));
    }

    @Test
    void testTraceRecvRawRateLimitsUpdated() {
        voiceLiveTracer.startConnectSpan();

        String rawJson
            = "{\"type\":\"rate_limits.updated\",\"rate_limits\":[{\"name\":\"requests\",\"remaining\":10}]}";
        voiceLiveTracer.traceRecvRaw(rawJson);
        voiceLiveTracer.endConnectSpan(null);

        SpanData recvSpan = spanExporter.getFinishedSpanItems().get(0);
        EventData rateLimitEvent = recvSpan.getEvents()
            .stream()
            .filter(e -> VoiceLiveTracer.GEN_AI_VOICE_RATE_LIMITS_UPDATED.equals(e.getName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected gen_ai.voice.rate_limits.updated event"));
        assertTrue(rateLimitEvent.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_RATE_LIMITS).contains("requests"));
    }

    @Test
    void testSessionCreatedTracksInputAudioSamplingRate() throws Exception {
        voiceLiveTracer.startConnectSpan();

        String json = "{\"type\":\"session.created\",\"event_id\":\"event1\","
            + "\"session\":{\"id\":\"session789\",\"model\":\"gpt-4o\"," + "\"input_audio_sampling_rate\":24000}}";
        SessionUpdate update = SessionUpdate.fromJson(com.azure.json.JsonProviders.createReader(json));
        voiceLiveTracer.traceRecv(update, json);
        voiceLiveTracer.endConnectSpan(null);

        List<SpanData> spans = spanExporter.getFinishedSpanItems();
        SpanData connectSpan = spans.get(spans.size() - 1);
        assertEquals(24000L,
            connectSpan.getAttributes().get(VoiceLiveTracer.GEN_AI_VOICE_INPUT_SAMPLE_RATE).longValue());
    }
}
