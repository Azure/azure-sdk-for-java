// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkflowAgentDefinition;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
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
import io.opentelemetry.sdk.trace.data.EventData;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GenAiAgentTracing} against a real in-memory OpenTelemetry SDK (no live service).
 */
public final class GenAiAgentTracingTest {

    private static final String ENDPOINT_HOST = "contoso.services.ai.azure.com";
    private static final String ENDPOINT = "https://" + ENDPOINT_HOST;
    private static final String AZ_NAMESPACE_NAME = "Microsoft.CognitiveServices";
    private static final String AGENT_NAME = "weather-agent";

    private static final AttributeKey<String> AZ_NAMESPACE = AttributeKey.stringKey("az.namespace");
    private static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");
    private static final AttributeKey<String> GEN_AI_PROVIDER_NAME = AttributeKey.stringKey("gen_ai.provider.name");
    private static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    private static final AttributeKey<String> GEN_AI_AGENT_NAME = AttributeKey.stringKey("gen_ai.agent.name");
    private static final AttributeKey<String> GEN_AI_AGENT_ID = AttributeKey.stringKey("gen_ai.agent.id");
    private static final AttributeKey<String> GEN_AI_AGENT_VERSION = AttributeKey.stringKey("gen_ai.agent.version");
    private static final AttributeKey<String> GEN_AI_AGENT_TYPE = AttributeKey.stringKey("gen_ai.agent.type");
    private static final AttributeKey<String> GEN_AI_REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    private static final AttributeKey<String> GEN_AI_SYSTEM_INSTRUCTIONS
        = AttributeKey.stringKey("gen_ai.system_instructions");
    private static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");

    private TestSpanProcessor spanProcessor;
    private Tracer tracer;
    private Meter meter;

    @BeforeEach
    public void setup() {
        spanProcessor = new TestSpanProcessor();
        final OpenTelemetryTracingOptions tracingOptions
            = new OpenTelemetryTracingOptions().setOpenTelemetry(OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build())
                .build());
        tracer = TracerProvider.getDefaultProvider().createTracer("test", null, AZ_NAMESPACE_NAME, tracingOptions);
        meter = MeterProvider.getDefaultProvider().createMeter("test", null, null);
    }

    private GenAiAgentTracing agentTracing(boolean captureContent) {
        return new GenAiAgentTracing(new GenAiInstrumentation(ENDPOINT, configuration(captureContent), tracer, meter));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void tracesPromptAgentCreation(boolean captureContent) {
        PromptAgentDefinition definition
            = new PromptAgentDefinition("gpt-4o").setTemperature(0.5).setTopP(0.9).setInstructions("Be helpful");
        AgentVersionDetails response = agentVersionResponse();

        AgentVersionDetails result = agentTracing(captureContent).traceCreateAgentVersion(AGENT_NAME, definition,
            (request, options) -> response, BinaryData.fromString("{}"), new RequestOptions());

        assertSame(response, result);
        ReadableSpan span = getSpan();
        Attributes attrs = span.getAttributes();
        assertEquals(AZ_NAMESPACE_NAME, attrs.get(AZ_NAMESPACE));
        assertEquals("create_agent", attrs.get(GEN_AI_OPERATION_NAME));
        assertEquals("az.ai.agents", attrs.get(GEN_AI_SYSTEM));
        assertEquals("microsoft.foundry", attrs.get(GEN_AI_PROVIDER_NAME));
        assertEquals(AGENT_NAME, attrs.get(GEN_AI_AGENT_NAME));
        assertEquals("prompt", attrs.get(GEN_AI_AGENT_TYPE));
        assertEquals("gpt-4o", attrs.get(GEN_AI_REQUEST_MODEL));
        assertEquals(ENDPOINT_HOST, attrs.get(SERVER_ADDRESS));
        // Enriched from the response: agent id is "name:version".
        assertEquals(AGENT_NAME + ":1", attrs.get(GEN_AI_AGENT_ID));
        assertEquals("1", attrs.get(GEN_AI_AGENT_VERSION));
        // System instructions attribute is always present but content is gated.
        String instructions = attrs.get(GEN_AI_SYSTEM_INSTRUCTIONS);
        assertNotNull(instructions);
        assertEquals(captureContent, instructions.contains("Be helpful"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void tracesWorkflowAgentCreationWithEvent(boolean captureContent) {
        WorkflowAgentDefinition definition = new WorkflowAgentDefinition().setWorkflow("workflow-definition-yaml");
        AgentVersionDetails response = agentVersionResponse();

        agentTracing(captureContent).traceCreateAgentVersion(AGENT_NAME, definition, (request, options) -> response,
            BinaryData.fromString("{}"), new RequestOptions());

        ReadableSpan span = getSpan();
        assertEquals("workflow", span.getAttributes().get(GEN_AI_AGENT_TYPE));

        List<EventData> events = span.toSpanData().getEvents();
        EventData workflowEvent
            = events.stream().filter(e -> "gen_ai.agent.workflow".equals(e.getName())).findFirst().orElse(null);
        assertNotNull(workflowEvent, "expected a gen_ai.agent.workflow event");
        String content = workflowEvent.getAttributes().get(AttributeKey.stringKey("gen_ai.event.content"));
        assertNotNull(content);
        assertEquals(captureContent, content.contains("workflow-definition-yaml"));
    }

    @Test
    public void tracesAsyncPromptAgentCreation() {
        PromptAgentDefinition definition = new PromptAgentDefinition("gpt-4o");
        AgentVersionDetails response = agentVersionResponse();

        AgentVersionDetails result = agentTracing(false).traceCreateAgentVersionAsync(AGENT_NAME, definition,
            (request, options) -> Mono.just(response), BinaryData.fromString("{}"), new RequestOptions()).block();

        assertSame(response, result);
        Attributes attrs = getSpan().getAttributes();
        assertEquals("create_agent", attrs.get(GEN_AI_OPERATION_NAME));
        assertEquals(AGENT_NAME, attrs.get(GEN_AI_AGENT_NAME));
        assertEquals(AGENT_NAME + ":1", attrs.get(GEN_AI_AGENT_ID));
    }

    @Test
    public void syncOperationThrowsPropagatesAndEndsSpan() {
        RuntimeException failure = new RuntimeException("boom");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> agentTracing(false)
            .traceCreateAgentVersion(AGENT_NAME, new PromptAgentDefinition("gpt-4o"), (request, options) -> {
                throw failure;
            }, BinaryData.fromString("{}"), new RequestOptions()));

        assertSame(failure, thrown);
        assertEquals(StatusCode.ERROR, getSpan().toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void asyncOperationErrorPropagatesAndEndsSpan() {
        RuntimeException failure = new RuntimeException("boom");
        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> agentTracing(false)
                .traceCreateAgentVersionAsync(AGENT_NAME, new PromptAgentDefinition("gpt-4o"),
                    (request, options) -> Mono.error(failure), BinaryData.fromString("{}"), new RequestOptions())
                .block());

        assertSame(failure, thrown);
        assertEquals(StatusCode.ERROR, getSpan().toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void tracingDisabledOperationStillRuns() {
        Tracer disabled = TracerProvider.getDefaultProvider().createTracer("test", null, AZ_NAMESPACE_NAME, null);
        GenAiAgentTracing tracing
            = new GenAiAgentTracing(new GenAiInstrumentation(ENDPOINT, configuration(false), disabled, meter));
        AtomicBoolean invoked = new AtomicBoolean(false);
        AgentVersionDetails response = agentVersionResponse();

        AgentVersionDetails result
            = tracing.traceCreateAgentVersion(AGENT_NAME, new PromptAgentDefinition("gpt-4o"), (request, options) -> {
                invoked.set(true);
                return response;
            }, BinaryData.fromString("{}"), new RequestOptions());

        assertTrue(invoked.get());
        assertSame(response, result);
        assertTrue(spanProcessor.getEndedSpans().isEmpty());
    }

    private ReadableSpan getSpan() {
        List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        assertFalse(spans.isEmpty(), "Expected at least one ended span.");
        ReadableSpan span
            = spans.stream().filter(s -> ("create_agent " + AGENT_NAME).equals(s.getName())).findFirst().orElse(null);
        assertNotNull(span, "create_agent span not found.");
        return span;
    }

    private static AgentVersionDetails agentVersionResponse() {
        String json = "{\"name\":\"" + AGENT_NAME + "\",\"version\":\"1\"}";
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
