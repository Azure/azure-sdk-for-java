// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GenAiObservabilityTest {

    private InMemorySpanExporter exporter;
    private OpenTelemetrySdk openTelemetry;
    private SdkTracerProvider tracerProvider;

    @BeforeEach
    void setUp() {
        exporter = InMemorySpanExporter.create();
        tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();
        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();
        GenAiObservability.setTracerForTest(openTelemetry.getTracer("gen-ai-test"));
    }

    @AfterEach
    void tearDown() {
        GenAiObservability.setTracerForTest(null);
        openTelemetry.close();
        exporter.close();
    }

    @Test
    void chatSpanUsesGenAiConventionsAndCurrentParent() {
        Span parent = tracerProvider.get("parent-test").spanBuilder("invoke_agent").startSpan();
        Span chat;
        try (Scope ignored = parent.makeCurrent()) {
            chat = GenAiObservability.startChatSpan("gpt-5.4");
            GenAiObservability.recordChatResponse(chat, "chatcmpl-1", "gpt-5.4-2026-01-01",
                12L, 7L, List.of("stop"));
        } finally {
            parent.end();
        }

        SpanData data = span("chat gpt-5.4");
        assertEquals(parent.getSpanContext().getSpanId(), data.getParentSpanId());
        assertEquals("chat", data.getAttributes().get(AttributeKey.stringKey("gen_ai.operation.name")));
        assertEquals("azure.ai.openai",
            data.getAttributes().get(AttributeKey.stringKey("gen_ai.provider.name")));
        assertEquals("chatcmpl-1",
            data.getAttributes().get(AttributeKey.stringKey("gen_ai.response.id")));
        assertEquals(12L, data.getAttributes().get(AttributeKey.longKey("gen_ai.usage.input_tokens")));
        assertEquals(List.of("stop"),
            data.getAttributes().get(AttributeKey.stringArrayKey("gen_ai.response.finish_reasons")));
    }

    @Test
    void messageContentRequiresExplicitOptIn() {
        Span disabled = GenAiObservability.startChatSpan("model");
        GenAiObservability.setChatMessages(disabled, "[input]", "[output]", "[system]", "[tools]", false);
        GenAiObservability.recordChatResponse(disabled, null, null, null, null, List.of());

        Span enabled = GenAiObservability.startChatSpan("model");
        GenAiObservability.setChatMessages(enabled, "[input]", "[output]", "[system]", "[tools]", true);
        GenAiObservability.recordChatResponse(enabled, null, null, null, null, List.of());

        List<SpanData> spans = exporter.getFinishedSpanItems().stream()
            .filter(span -> span.getName().equals("chat model"))
            .toList();
        assertNull(spans.get(0).getAttributes().get(AttributeKey.stringKey("gen_ai.input.messages")));
        assertEquals("[input]",
            spans.get(1).getAttributes().get(AttributeKey.stringKey("gen_ai.input.messages")));
    }

    @Test
    void toolSpanCoversExecutionAndRecordsFailures() {
        Span parent = tracerProvider.get("parent-test").spanBuilder("invoke_agent").startSpan();
        IllegalArgumentException failure = new IllegalArgumentException("invalid amount");
        Span tool;
        try (Scope ignored = parent.makeCurrent()) {
            tool = GenAiObservability.startExecuteToolSpan("withdraw", "call-1", "{\"amount\":-1}", true);
            GenAiObservability.recordToolResult(tool, null, failure, true);
        } finally {
            parent.end();
        }

        SpanData data = span("execute_tool withdraw");
        assertEquals(parent.getSpanContext().getSpanId(), data.getParentSpanId());
        assertEquals(StatusCode.ERROR, data.getStatus().getStatusCode());
        assertEquals(1, data.getEvents().size());
        assertEquals("{\"amount\":-1}",
            data.getAttributes().get(AttributeKey.stringKey("gen_ai.tool.call.arguments")));
        assertNull(data.getAttributes().get(AttributeKey.stringKey("gen_ai.tool.call.result")));
    }

    @Test
    void toolContentRequiresExplicitOptIn() {
        Span disabled = GenAiObservability.startExecuteToolSpan(
            "get_balance", "call-1", "{\"user\":\"Alice\"}", false);
        GenAiObservability.recordToolResult(disabled, "1000.0", null, false);

        Span enabled = GenAiObservability.startExecuteToolSpan(
            "get_balance", "call-2", "{\"user\":\"Alice\"}", true);
        GenAiObservability.recordToolResult(enabled, "1000.0", null, true);

        List<SpanData> spans = exporter.getFinishedSpanItems().stream()
            .filter(span -> span.getName().equals("execute_tool get_balance"))
            .toList();
        assertNull(spans.get(0).getAttributes().get(
            AttributeKey.stringKey("gen_ai.tool.call.arguments")));
        assertNull(spans.get(0).getAttributes().get(
            AttributeKey.stringKey("gen_ai.tool.call.result")));
        assertEquals("{\"user\":\"Alice\"}", spans.get(1).getAttributes().get(
            AttributeKey.stringKey("gen_ai.tool.call.arguments")));
        assertEquals("1000.0", spans.get(1).getAttributes().get(
            AttributeKey.stringKey("gen_ai.tool.call.result")));
    }

    private SpanData span(String name) {
        return exporter.getFinishedSpanItems().stream()
            .filter(span -> span.getName().equals(name))
            .findFirst()
            .orElseThrow();
    }
}
