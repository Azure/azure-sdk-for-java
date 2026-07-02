// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AgentsClientTracer}. These exercise the tracer against a real in-memory OpenTelemetry
 * SDK (no live service), asserting span name/attributes, content gating, and error propagation.
 */
public final class AgentsClientTracerTest {

    private static final String ENDPOINT_HOST = "contoso.services.ai.azure.com";
    private static final String ENDPOINT = "https://" + ENDPOINT_HOST;
    private static final String AGENTS_GEN_AI_SYSTEM_NAME = "az.ai.agents";
    private static final String AZ_NAMESPACE_NAME = "Microsoft.CognitiveServices";
    private static final String AGENT_NAME = "weather-agent";
    private static final String SPAN_NAME = "create_agent " + AGENT_NAME;
    private static final String AGENT_ID = "agent-123";
    private static final String AGENT_DESCRIPTION = "A helpful weather agent";

    private static final AttributeKey<String> AZ_NAMESPACE = AttributeKey.stringKey("az.namespace");
    private static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");
    private static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    private static final AttributeKey<String> GEN_AI_AGENT_NAME = AttributeKey.stringKey("gen_ai.agent.name");
    private static final AttributeKey<String> GEN_AI_AGENT_ID = AttributeKey.stringKey("gen_ai.agent.id");
    private static final AttributeKey<String> GEN_AI_AGENT_DESCRIPTION
        = AttributeKey.stringKey("gen_ai.agent.description");
    private static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");

    private TestSpanProcessor spanProcessor;
    private Tracer tracer;

    @BeforeEach
    public void setup() {
        spanProcessor = new TestSpanProcessor();
        final OpenTelemetryTracingOptions tracingOptions
            = new OpenTelemetryTracingOptions().setOpenTelemetry(OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build())
                .build());
        tracer = TracerProvider.getDefaultProvider().createTracer("test", null, AZ_NAMESPACE_NAME, tracingOptions);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldTraceSyncCreateAgentVersion(boolean captureContent) {
        final AgentsClientTracer agentsTracer = new AgentsClientTracer(ENDPOINT, configuration(captureContent), tracer);
        final AtomicBoolean invoked = new AtomicBoolean(false);
        final AgentVersionDetails response = agentVersionResponse();

        final AgentVersionDetails result
            = agentsTracer.traceCreateAgentVersion(AGENT_NAME, null, (request, options) -> {
                invoked.set(true);
                return response;
            }, BinaryData.fromString("{}"), new RequestOptions());

        assertTrue(invoked.get());
        assertSame(response, result);
        assertSpanAttributes(getSpan().getAttributes(), captureContent);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldTraceAsyncCreateAgentVersion(boolean captureContent) {
        final AgentsClientTracer agentsTracer = new AgentsClientTracer(ENDPOINT, configuration(captureContent), tracer);
        final AgentVersionDetails response = agentVersionResponse();

        final AgentVersionDetails result
            = agentsTracer
                .traceCreateAgentVersionAsync(AGENT_NAME, null, (request, options) -> Mono.just(response),
                    BinaryData.fromString("{}"), new RequestOptions())
                .block();

        assertSame(response, result);
        assertSpanAttributes(getSpan().getAttributes(), captureContent);
    }

    @Test
    public void syncOperationThrowsPropagatesAndEndsSpan() {
        final AgentsClientTracer agentsTracer = new AgentsClientTracer(ENDPOINT, configuration(false), tracer);
        final RuntimeException failure = new RuntimeException("boom");

        final RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> agentsTracer.traceCreateAgentVersion(AGENT_NAME, null, (request, options) -> {
                throw failure;
            }, BinaryData.fromString("{}"), new RequestOptions()));

        assertSame(failure, thrown);
        assertEquals(StatusCode.ERROR, getSpan().toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void asyncOperationErrorPropagatesAndEndsSpan() {
        final AgentsClientTracer agentsTracer = new AgentsClientTracer(ENDPOINT, configuration(false), tracer);
        final RuntimeException failure = new RuntimeException("boom");

        final RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> agentsTracer
                .traceCreateAgentVersionAsync(AGENT_NAME, null, (request, options) -> Mono.error(failure),
                    BinaryData.fromString("{}"), new RequestOptions())
                .block());

        assertSame(failure, thrown);
        assertEquals(StatusCode.ERROR, getSpan().toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void tracingDisabledOperationStillRuns() {
        final Tracer disabledTracer
            = TracerProvider.getDefaultProvider().createTracer("test", null, AZ_NAMESPACE_NAME, null);
        final AgentsClientTracer agentsTracer = new AgentsClientTracer(ENDPOINT, configuration(false), disabledTracer);
        final AtomicBoolean invoked = new AtomicBoolean(false);
        final AgentVersionDetails response = agentVersionResponse();

        final AgentVersionDetails result
            = agentsTracer.traceCreateAgentVersion(AGENT_NAME, null, (request, options) -> {
                invoked.set(true);
                return response;
            }, BinaryData.fromString("{}"), new RequestOptions());

        assertTrue(invoked.get());
        assertSame(response, result);
        assertTrue(spanProcessor.getEndedSpans().isEmpty(), "No spans should be emitted when tracing is disabled.");
    }

    private void assertSpanAttributes(Attributes attributes, boolean captureContent) {
        assertEquals(AZ_NAMESPACE_NAME, attributes.get(AZ_NAMESPACE));
        assertEquals("create_agent", attributes.get(GEN_AI_OPERATION_NAME));
        assertEquals(AGENTS_GEN_AI_SYSTEM_NAME, attributes.get(GEN_AI_SYSTEM));
        assertEquals(AGENT_NAME, attributes.get(GEN_AI_AGENT_NAME));
        assertEquals(ENDPOINT_HOST, attributes.get(SERVER_ADDRESS));
        assertEquals(AGENT_ID, attributes.get(GEN_AI_AGENT_ID));
        if (captureContent) {
            assertEquals(AGENT_DESCRIPTION, attributes.get(GEN_AI_AGENT_DESCRIPTION));
        } else {
            assertNull(attributes.get(GEN_AI_AGENT_DESCRIPTION));
        }
    }

    private ReadableSpan getSpan() {
        final List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertFalse(spans.isEmpty(), "Expected at least one ended span.");
        final ReadableSpan span = spans.stream().filter(s -> SPAN_NAME.equals(s.getName())).findFirst().orElse(null);
        assertNotNull(span, "create_agent span not found.");
        return span;
    }

    private static AgentVersionDetails agentVersionResponse() {
        final String json = "{\"id\":\"" + AGENT_ID + "\",\"name\":\"" + AGENT_NAME + "\",\"version\":\"1\","
            + "\"description\":\"" + AGENT_DESCRIPTION + "\"}";
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return AgentVersionDetails.fromJson(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Configuration configuration(boolean captureContent) {
        if (captureContent) {
            return new ConfigurationBuilder().putProperty("azure.tracing.gen_ai.content_recording_enabled", "true")
                .build();
        }
        return new ConfigurationBuilder().build();
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
