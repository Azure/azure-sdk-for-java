// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.ai.agents.telemetry.GenAiConstants.*;

/**
 * Manages the lifecycle of a GenAI tracing span, including attribute setting,
 * metrics recording, and content privacy gating.
 *
 * <p>Follows the try-with-resources / AutoCloseable pattern (similar to C#'s IDisposable).
 * A scope is created at the start of an operation, collects attributes during execution,
 * and records metrics/ends the span on close.</p>
 *
 * <p>If tracing is disabled or no listeners are active, factory methods return {@code null},
 * and all public instance methods are safe to call on null (callers should use null-check pattern).</p>
 */
public final class GenAiTracingScope implements AutoCloseable {

    private static final ClientLogger LOGGER = new ClientLogger(GenAiTracingScope.class);

    static final String CLIENT_NAME = "Azure.AI.Agents";
    private static final String CLIENT_VERSION = "2.1.0-beta.2";

    private static volatile Tracer lazyTracer;
    private static final DoubleHistogram DURATION_HISTOGRAM;
    private static final DoubleHistogram TOKEN_USAGE_HISTOGRAM;
    private static final Meter METER;

    static {
        METER = MeterProvider.getDefaultProvider().createMeter(CLIENT_NAME, CLIENT_VERSION, null);

        DURATION_HISTOGRAM = METER.createDoubleHistogram(METRIC_OPERATION_DURATION, "Duration of GenAI operations",
            METRIC_UNIT_SECONDS);

        TOKEN_USAGE_HISTOGRAM = METER.createDoubleHistogram(METRIC_TOKEN_USAGE,
            "Number of input and output tokens used", METRIC_UNIT_TOKENS);
    }

    /**
     * Lazily initializes the tracer to ensure GlobalOpenTelemetry is registered before we resolve.
     */
    private static Tracer getTracer() {
        Tracer local = lazyTracer;
        if (local == null) {
            synchronized (GenAiTracingScope.class) {
                local = lazyTracer;
                if (local == null) {
                    local = TracerProvider.getDefaultProvider()
                        .createTracer(CLIENT_NAME, CLIENT_VERSION, AZ_NAMESPACE_VALUE, null);
                    lazyTracer = local;
                }
            }
        }
        return local;
    }

    private final Context spanContext;
    private final String operationName;
    private final String serverAddress;
    private final int serverPort;
    private final Instant startTime;
    private final AtomicInteger ended = new AtomicInteger(0);

    // Deferred attributes for metrics
    private String responseModel;
    private String errorType;
    private Long inputTokens;
    private Long outputTokens;

    private GenAiTracingScope(Context spanContext, String operationName, String serverAddress, int serverPort) {
        this.spanContext = spanContext;
        this.operationName = operationName;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.startTime = Instant.now();
    }

    // --- Factory methods ---

    /**
     * Starts a tracing scope for a create_agent operation.
     *
     * @param agentName the agent name (used in span name).
     * @param endpoint the service endpoint URI.
     * @return a new scope, or {@code null} if tracing is disabled.
     */
    public static GenAiTracingScope startCreateAgent(String agentName, URI endpoint) {
        return startScope(OPERATION_CREATE_AGENT, agentName, endpoint);
    }

    /**
     * Starts a tracing scope for an invoke_agent operation.
     *
     * @param agentName the agent name (used in span name).
     * @param endpoint the service endpoint URI.
     * @return a new scope, or {@code null} if tracing is disabled.
     */
    public static GenAiTracingScope startInvokeAgent(String agentName, URI endpoint) {
        return startScope(OPERATION_INVOKE_AGENT, agentName, endpoint);
    }

    /**
     * Starts a tracing scope for a chat (direct model response) operation.
     *
     * @param modelName the model name (used in span name).
     * @param endpoint the service endpoint URI.
     * @return a new scope, or {@code null} if tracing is disabled.
     */
    public static GenAiTracingScope startChat(String modelName, URI endpoint) {
        return startScope(OPERATION_CHAT, modelName, endpoint);
    }

    /**
     * Starts a tracing scope for a create_conversation operation.
     *
     * @param endpoint the service endpoint URI.
     * @return a new scope, or {@code null} if tracing is disabled.
     */
    public static GenAiTracingScope startCreateConversation(URI endpoint) {
        return startScope(OPERATION_CREATE_CONVERSATION, null, endpoint);
    }

    private static GenAiTracingScope startScope(String operationName, String spanNameSuffix, URI endpoint) {
        if (!GenAiTracingConfiguration.isTracingEnabled()) {
            return null;
        }
        if (!getTracer().isEnabled() && !DURATION_HISTOGRAM.isEnabled()) {
            return null;
        }

        String spanName = spanNameSuffix != null ? operationName + " " + spanNameSuffix : operationName;
        String serverAddress = endpoint != null ? endpoint.getHost() : "unknown";
        int serverPort = endpoint != null ? endpoint.getPort() : -1;
        if (serverPort <= 0) {
            serverPort = DEFAULT_HTTPS_PORT;
        }

        StartSpanOptions options
            = new StartSpanOptions(SpanKind.CLIENT).setAttribute(GEN_AI_OPERATION_NAME, operationName)
                .setAttribute(AZ_NAMESPACE, AZ_NAMESPACE_VALUE)
                .setAttribute(GEN_AI_PROVIDER_NAME, GEN_AI_PROVIDER_NAME_VALUE)
                .setAttribute(SERVER_ADDRESS, serverAddress);

        if (serverPort != DEFAULT_HTTPS_PORT) {
            options.setAttribute(SERVER_PORT, (long) serverPort);
        }

        Context spanContext = getTracer().start(spanName, options, Context.NONE);
        return new GenAiTracingScope(spanContext, operationName, serverAddress, serverPort);
    }

    // --- Attribute setters ---

    /**
     * Sets agent-related attributes on the span.
     *
     * @param agentId the agent ID (e.g., "name:version").
     * @param agentName the agent name.
     * @param agentVersion the agent version string.
     * @param agentType the agent type (prompt, hosted, workflow).
     */
    public void setAgentAttributes(String agentId, String agentName, String agentVersion, String agentType) {
        setAttributeIfNotEmpty(GEN_AI_AGENT_ID, agentId);
        setAttributeIfNotEmpty(GEN_AI_AGENT_NAME, agentName);
        setAttributeIfNotEmpty(GEN_AI_AGENT_VERSION, agentVersion);
        setAttributeIfNotEmpty(GEN_AI_AGENT_TYPE, agentType);
    }

    /**
     * Sets the agent ID and version attributes from the response (after creation).
     *
     * @param agentId the agent ID (e.g., "name:version").
     * @param agentVersion the agent version string.
     */
    public void setAgentIdAndVersion(String agentId, String agentVersion) {
        setAttributeIfNotEmpty(GEN_AI_AGENT_ID, agentId);
        setAttributeIfNotEmpty(GEN_AI_AGENT_VERSION, agentVersion);
    }

    /**
     * Sets hosted agent-specific attributes.
     *
     * @param cpu CPU allocation (e.g., "0.5").
     * @param memory memory allocation (e.g., "1Gi").
     * @param image container image URI.
     * @param protocol protocol name (e.g., "responses").
     * @param protocolVersion protocol version.
     */
    public void setHostedAgentAttributes(String cpu, String memory, String image, String protocol,
        String protocolVersion) {
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_CPU, cpu);
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_MEMORY, memory);
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_IMAGE, image);
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_PROTOCOL, protocol);
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_PROTOCOL_VERSION, protocolVersion);
    }

    /**
     * Sets request model parameters.
     *
     * @param model the model name.
     * @param temperature temperature parameter (may be null).
     * @param topP top_p parameter (may be null).
     */
    public void setRequestModelAttributes(String model, Double temperature, Double topP) {
        setAttributeIfNotEmpty(GEN_AI_REQUEST_MODEL, model);
        if (temperature != null) {
            getTracer().setAttribute(GEN_AI_REQUEST_TEMPERATURE, String.valueOf(temperature), spanContext);
        }
        if (topP != null) {
            getTracer().setAttribute(GEN_AI_REQUEST_TOP_P, String.valueOf(topP), spanContext);
        }
    }

    /**
     * Sets system instructions attribute (content-gated).
     *
     * @param instructions the system instruction text.
     */
    public void setSystemInstructions(String instructions) {
        if (instructions == null) {
            return;
        }
        String value;
        if (GenAiTracingConfiguration.isContentRecordingEnabled() && !instructions.isEmpty()) {
            value = "[{\"type\":\"text\",\"content\":" + jsonEscape(instructions) + "}]";
        } else {
            value = "[{\"type\":\"text\"}]";
        }
        getTracer().setAttribute(GEN_AI_SYSTEM_INSTRUCTIONS, value, spanContext);
    }

    /**
     * Sets input messages attribute (content-gated).
     *
     * @param messages the formatted input messages JSON string.
     */
    public void setInputMessages(String messages) {
        if (messages != null) {
            getTracer().setAttribute(GEN_AI_INPUT_MESSAGES, messages, spanContext);
        }
    }

    /**
     * Sets output messages attribute (content-gated).
     *
     * @param messages the formatted output messages JSON string.
     */
    public void setOutputMessages(String messages) {
        if (messages != null) {
            getTracer().setAttribute(GEN_AI_OUTPUT_MESSAGES, messages, spanContext);
        }
    }

    /**
     * Sets response-related attributes after a successful response.
     *
     * @param responseId the response ID.
     * @param model the response model name.
     * @param inputTokenCount input token count (may be null).
     * @param outputTokenCount output token count (may be null).
     * @param finishReasons the finish reason(s).
     */
    public void setResponseAttributes(String responseId, String model, Long inputTokenCount, Long outputTokenCount,
        String finishReasons) {
        setAttributeIfNotEmpty(GEN_AI_RESPONSE_ID, responseId);
        setAttributeIfNotEmpty(GEN_AI_RESPONSE_MODEL, model);
        if (inputTokenCount != null) {
            getTracer().setAttribute(GEN_AI_USAGE_INPUT_TOKENS, inputTokenCount, spanContext);
        }
        if (outputTokenCount != null) {
            getTracer().setAttribute(GEN_AI_USAGE_OUTPUT_TOKENS, outputTokenCount, spanContext);
        }
        setAttributeIfNotEmpty(GEN_AI_RESPONSE_FINISH_REASONS, finishReasons);

        // Save for metrics recording
        this.responseModel = model;
        this.inputTokens = inputTokenCount;
        this.outputTokens = outputTokenCount;
    }

    /**
     * Sets the conversation ID attribute.
     *
     * @param conversationId the conversation ID.
     */
    public void setConversationId(String conversationId) {
        setAttributeIfNotEmpty(GEN_AI_CONVERSATION_ID, conversationId);
    }

    /**
     * Adds a span event with the given name and attributes.
     *
     * @param eventName the event name (e.g., "gen_ai.agent.workflow").
     * @param attributes the event attributes map.
     */
    public void addEvent(String eventName, Map<String, Object> attributes) {
        getTracer().addEvent(eventName, attributes, OffsetDateTime.now(), spanContext);
    }

    /**
     * Records an error on the span.
     *
     * @param error the exception that occurred.
     */
    public void recordError(Throwable error) {
        if (error != null) {
            this.errorType = error.getClass().getName();
            getTracer().setAttribute(ERROR_TYPE, this.errorType, spanContext);
        }
    }

    /**
     * Gets the span context for passing to downstream calls.
     *
     * @return the context containing the active span.
     */
    public Context getSpanContext() {
        return spanContext;
    }

    /**
     * Makes this span the current active span (for trace context propagation).
     *
     * @return an AutoCloseable scope that restores the previous context when closed.
     */
    public AutoCloseable makeSpanCurrent() {
        return getTracer().makeSpanCurrent(spanContext);
    }

    /**
     * Ends the span and records metrics. Idempotent — safe to call multiple times.
     */
    @Override
    public void close() {
        if (ended.compareAndSet(0, 1)) {
            recordMetrics();
            getTracer().end(errorType, errorType != null ? null : null, spanContext);
        }
    }

    // --- Private helpers ---

    private void recordMetrics() {
        double durationSeconds = (System.nanoTime() - startTime.toEpochMilli() * 1_000_000L) / 1_000_000_000.0;
        // Use wall-clock for more accurate duration
        durationSeconds = (Instant.now().toEpochMilli() - startTime.toEpochMilli()) / 1000.0;

        Map<String, Object> baseAttributes = new HashMap<>();
        baseAttributes.put(GEN_AI_OPERATION_NAME, operationName);
        baseAttributes.put(GEN_AI_PROVIDER_NAME, GEN_AI_PROVIDER_NAME_VALUE);
        baseAttributes.put(SERVER_ADDRESS, serverAddress);
        if (serverPort != DEFAULT_HTTPS_PORT) {
            baseAttributes.put(SERVER_PORT, (long) serverPort);
        }
        if (responseModel != null) {
            baseAttributes.put(GEN_AI_RESPONSE_MODEL, responseModel);
        }
        if (errorType != null) {
            baseAttributes.put(ERROR_TYPE, errorType);
        }

        // Record operation duration
        if (DURATION_HISTOGRAM.isEnabled()) {
            TelemetryAttributes durationAttrs = METER.createAttributes(baseAttributes);
            DURATION_HISTOGRAM.record(durationSeconds, durationAttrs, spanContext);
        }

        // Record token usage
        if (TOKEN_USAGE_HISTOGRAM.isEnabled()) {
            if (inputTokens != null && inputTokens > 0) {
                Map<String, Object> inputAttrs = new HashMap<>(baseAttributes);
                inputAttrs.put(GEN_AI_TOKEN_TYPE, TOKEN_TYPE_INPUT);
                TelemetryAttributes attrs = METER.createAttributes(inputAttrs);
                TOKEN_USAGE_HISTOGRAM.record(inputTokens.doubleValue(), attrs, spanContext);
            }
            if (outputTokens != null && outputTokens > 0) {
                Map<String, Object> outputAttrs = new HashMap<>(baseAttributes);
                outputAttrs.put(GEN_AI_TOKEN_TYPE, TOKEN_TYPE_OUTPUT);
                TelemetryAttributes attrs = METER.createAttributes(outputAttrs);
                TOKEN_USAGE_HISTOGRAM.record(outputTokens.doubleValue(), attrs, spanContext);
            }
        }
    }

    private void setAttributeIfNotEmpty(String key, String value) {
        if (value != null && !value.isEmpty()) {
            getTracer().setAttribute(key, value, spanContext);
        }
    }

    private static String jsonEscape(String text) {
        if (text == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;

                case '\\':
                    sb.append("\\\\");
                    break;

                case '\n':
                    sb.append("\\n");
                    break;

                case '\r':
                    sb.append("\\r");
                    break;

                case '\t':
                    sb.append("\\t");
                    break;

                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
