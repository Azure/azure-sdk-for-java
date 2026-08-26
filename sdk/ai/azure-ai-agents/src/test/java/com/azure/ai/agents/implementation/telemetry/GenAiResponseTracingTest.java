// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.ai.agents.implementation.http.OpenAITracingContextBridge;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.openai.core.JsonValue;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.ResponseCreateParams;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for response tracing lifecycle behavior that does not require a live service.
 */
public final class GenAiResponseTracingTest {

    private static final String CONVERSATION_ID = "conversation-1";
    private static final AttributeKey<String> GEN_AI_CONVERSATION_ID = AttributeKey.stringKey("gen_ai.conversation.id");

    private TestSpanProcessor spanProcessor;
    private GenAiResponseTracing responseTracing;

    @BeforeEach
    public void setup() {
        spanProcessor = new TestSpanProcessor();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build())
            .build();
        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, "Microsoft.CognitiveServices",
                new OpenTelemetryTracingOptions().setOpenTelemetry(openTelemetry));
        Meter meter = MeterProvider.getDefaultProvider().createMeter("test", null, null);
        responseTracing = new GenAiResponseTracing(new GenAiInstrumentation("https://contoso.services.ai.azure.com",
            new ConfigurationBuilder().build(), tracer, meter), new OpenAITracingContextBridge());
    }

    @Test
    public void tracesConversationAroundSyncOperation() {
        Conversation conversation = conversation();

        Conversation result = responseTracing.traceCreateConversation(() -> conversation);

        assertSame(conversation, result);
        assertEquals(CONVERSATION_ID, getConversationSpan().getAttributes().get(GEN_AI_CONVERSATION_ID));
    }

    @Test
    public void tracesConversationAroundAsyncOperation() {
        Conversation conversation = conversation();

        Conversation result = responseTracing.traceCreateConversationAsync(params -> {
            assertTrue(io.opentelemetry.api.trace.Span.current().getSpanContext().isValid());
            assertTrue(params._additionalHeaders().names().contains(OpenAITracingContextBridge.TRACE_CONTEXT_HEADER));
            return Mono.just(conversation);
        }).block();

        assertSame(conversation, result);
        assertEquals(CONVERSATION_ID, getConversationSpan().getAttributes().get(GEN_AI_CONVERSATION_ID));
    }

    @Test
    public void syncConversationErrorPropagatesAndEndsSpan() {
        RuntimeException failure = new RuntimeException("boom");

        RuntimeException thrown
            = assertThrows(RuntimeException.class, () -> responseTracing.traceCreateConversation(() -> {
                throw failure;
            }));

        assertSame(failure, thrown);
        assertEquals(StatusCode.ERROR, getConversationSpan().toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void asyncConversationCancellationEndsSpan() {
        Disposable subscription = responseTracing.traceCreateConversationAsync(params -> Mono.never()).subscribe();

        subscription.dispose();

        assertNotNull(getConversationSpan());
    }

    @Test
    public void startsAsyncStreamingOperationWithCurrentSpan() {
        Disposable subscription = responseTracing.traceStreamingResponseAsync(new AzureCreateResponseOptions(),
            ResponseCreateParams.builder().model("gpt-4o").input("hello").build(), tracedParams -> {
                assertTrue(io.opentelemetry.api.trace.Span.current().getSpanContext().isValid());
                assertTrue(tracedParams._additionalHeaders()
                    .names()
                    .contains(OpenAITracingContextBridge.TRACE_CONTEXT_HEADER));
                return reactor.core.publisher.Flux.never();
            }).subscribe();

        subscription.dispose();

        assertNotNull(spanProcessor.getEndedSpans()
            .stream()
            .filter(candidate -> "chat gpt-4o".equals(candidate.getName()))
            .findFirst()
            .orElse(null));
    }

    private ReadableSpan getConversationSpan() {
        ReadableSpan span = spanProcessor.getEndedSpans()
            .stream()
            .filter(candidate -> "create_conversation".equals(candidate.getName()))
            .findFirst()
            .orElse(null);
        assertNotNull(span, "create_conversation span not found.");
        return span;
    }

    private static Conversation conversation() {
        return Conversation.builder()
            .id(CONVERSATION_ID)
            .createdAt(0L)
            .metadata(JsonValue.from(Collections.emptyMap()))
            .object_(JsonValue.from("conversation"))
            .build();
    }

    private static final class TestSpanProcessor implements SpanProcessor {
        private final ConcurrentLinkedDeque<ReadableSpan> spans = new ConcurrentLinkedDeque<>();

        List<ReadableSpan> getEndedSpans() {
            return new ArrayList<>(spans);
        }

        @Override
        public void onStart(io.opentelemetry.context.Context parentContext, ReadWriteSpan span) {
        }

        @Override
        public boolean isStartRequired() {
            return false;
        }

        @Override
        public void onEnd(ReadableSpan span) {
            spans.add(span);
        }

        @Override
        public boolean isEndRequired() {
            return true;
        }
    }
}
