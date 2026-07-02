// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.Meter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.DEFAULT_HTTPS_PORT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.ERROR_TYPE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_HOSTED_CPU;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_HOSTED_IMAGE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_HOSTED_MEMORY;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_HOSTED_PROTOCOL;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_HOSTED_PROTOCOL_VERSION;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_ID;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_NAME;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_TYPE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_VERSION;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_CONVERSATION_ID;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_INPUT_MESSAGES;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_OPERATION_NAME;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_OUTPUT_MESSAGES;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME_VALUE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_REQUEST_MODEL;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_REQUEST_TEMPERATURE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_REQUEST_TOP_P;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_RESPONSE_FINISH_REASONS;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_RESPONSE_ID;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_RESPONSE_MODEL;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_SYSTEM_INSTRUCTIONS;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_TOKEN_TYPE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_USAGE_INPUT_TOKENS;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_USAGE_OUTPUT_TOKENS;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.SERVER_ADDRESS;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.SERVER_PORT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.TOKEN_TYPE_INPUT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.TOKEN_TYPE_OUTPUT;

/**
 * Manages the lifecycle of a single GenAI tracing span, including attribute setting, metrics recording, and content
 * privacy gating. Created by {@link GenAiInstrumentation}; all telemetry primitives (tracer, meter, histograms,
 * content flag) come from that per-client instance rather than global state.
 */
final class GenAiTracingScope implements AutoCloseable {

    private final GenAiInstrumentation instrumentation;
    private final Context spanContext;
    private final String operationName;
    private final String serverAddress;
    private final int serverPort;
    private final Instant startTime;
    private final AtomicInteger ended = new AtomicInteger(0);

    // Deferred attributes for metrics.
    private String responseModel;
    private String errorType;
    private Throwable error;
    private Long inputTokens;
    private Long outputTokens;

    GenAiTracingScope(GenAiInstrumentation instrumentation, Context spanContext, String operationName,
        String serverAddress, int serverPort) {
        this.instrumentation = instrumentation;
        this.spanContext = spanContext;
        this.operationName = operationName;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.startTime = Instant.now();
    }

    void setAgentAttributes(String agentId, String agentName, String agentVersion, String agentType) {
        setAttributeIfNotEmpty(GEN_AI_AGENT_ID, agentId);
        setAttributeIfNotEmpty(GEN_AI_AGENT_NAME, agentName);
        setAttributeIfNotEmpty(GEN_AI_AGENT_VERSION, agentVersion);
        setAttributeIfNotEmpty(GEN_AI_AGENT_TYPE, agentType);
    }

    void setAgentIdAndVersion(String agentId, String agentVersion) {
        setAttributeIfNotEmpty(GEN_AI_AGENT_ID, agentId);
        setAttributeIfNotEmpty(GEN_AI_AGENT_VERSION, agentVersion);
    }

    void setHostedAgentAttributes(String cpu, String memory, String image, String protocol, String protocolVersion) {
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_CPU, cpu);
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_MEMORY, memory);
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_IMAGE, image);
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_PROTOCOL, protocol);
        setAttributeIfNotEmpty(GEN_AI_AGENT_HOSTED_PROTOCOL_VERSION, protocolVersion);
    }

    void setRequestModelAttributes(String model, Double temperature, Double topP) {
        setAttributeIfNotEmpty(GEN_AI_REQUEST_MODEL, model);
        if (temperature != null) {
            instrumentation.tracer().setAttribute(GEN_AI_REQUEST_TEMPERATURE, String.valueOf(temperature), spanContext);
        }
        if (topP != null) {
            instrumentation.tracer().setAttribute(GEN_AI_REQUEST_TOP_P, String.valueOf(topP), spanContext);
        }
    }

    void setSystemInstructions(String instructions) {
        if (instructions == null) {
            return;
        }
        String value
            = GenAiMessageFormatter.formatSystemInstructions(instrumentation.isContentRecordingEnabled(), instructions);
        instrumentation.tracer().setAttribute(GEN_AI_SYSTEM_INSTRUCTIONS, value, spanContext);
    }

    void setInputMessages(String messages) {
        if (messages != null) {
            instrumentation.tracer().setAttribute(GEN_AI_INPUT_MESSAGES, messages, spanContext);
        }
    }

    void setOutputMessages(String messages) {
        if (messages != null) {
            instrumentation.tracer().setAttribute(GEN_AI_OUTPUT_MESSAGES, messages, spanContext);
        }
    }

    void setResponseAttributes(String responseId, String model, Long inputTokenCount, Long outputTokenCount,
        String finishReasons) {
        setAttributeIfNotEmpty(GEN_AI_RESPONSE_ID, responseId);
        setAttributeIfNotEmpty(GEN_AI_RESPONSE_MODEL, model);
        if (inputTokenCount != null) {
            instrumentation.tracer().setAttribute(GEN_AI_USAGE_INPUT_TOKENS, inputTokenCount, spanContext);
        }
        if (outputTokenCount != null) {
            instrumentation.tracer().setAttribute(GEN_AI_USAGE_OUTPUT_TOKENS, outputTokenCount, spanContext);
        }
        setAttributeIfNotEmpty(GEN_AI_RESPONSE_FINISH_REASONS, finishReasons);

        this.responseModel = model;
        this.inputTokens = inputTokenCount;
        this.outputTokens = outputTokenCount;
    }

    void setConversationId(String conversationId) {
        setAttributeIfNotEmpty(GEN_AI_CONVERSATION_ID, conversationId);
    }

    void addEvent(String eventName, Map<String, Object> attributes) {
        instrumentation.tracer().addEvent(eventName, attributes, OffsetDateTime.now(), spanContext);
    }

    void recordError(Throwable error) {
        if (error != null) {
            this.error = error;
            this.errorType = error.getClass().getName();
            instrumentation.tracer().setAttribute(ERROR_TYPE, this.errorType, spanContext);
        }
    }

    Context getSpanContext() {
        return spanContext;
    }

    boolean isContentRecordingEnabled() {
        return instrumentation.isContentRecordingEnabled();
    }

    AutoCloseable makeSpanCurrent() {
        return instrumentation.tracer().makeSpanCurrent(spanContext);
    }

    @Override
    public void close() {
        if (ended.compareAndSet(0, 1)) {
            recordMetrics();
            instrumentation.tracer().end(errorType, error, spanContext);
        }
    }

    private void recordMetrics() {
        double durationSeconds = (Instant.now().toEpochMilli() - startTime.toEpochMilli()) / 1000.0;

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

        final Meter meter = instrumentation.meter();
        final DoubleHistogram durationHistogram = instrumentation.durationHistogram();
        if (durationHistogram.isEnabled()) {
            TelemetryAttributes durationAttrs = meter.createAttributes(baseAttributes);
            durationHistogram.record(durationSeconds, durationAttrs, spanContext);
        }

        final DoubleHistogram tokenUsageHistogram = instrumentation.tokenUsageHistogram();
        if (tokenUsageHistogram.isEnabled()) {
            if (inputTokens != null && inputTokens > 0) {
                Map<String, Object> inputAttrs = new HashMap<>(baseAttributes);
                inputAttrs.put(GEN_AI_TOKEN_TYPE, TOKEN_TYPE_INPUT);
                tokenUsageHistogram.record(inputTokens.doubleValue(), meter.createAttributes(inputAttrs), spanContext);
            }
            if (outputTokens != null && outputTokens > 0) {
                Map<String, Object> outputAttrs = new HashMap<>(baseAttributes);
                outputAttrs.put(GEN_AI_TOKEN_TYPE, TOKEN_TYPE_OUTPUT);
                tokenUsageHistogram.record(outputTokens.doubleValue(), meter.createAttributes(outputAttrs),
                    spanContext);
            }
        }
    }

    private void setAttributeIfNotEmpty(String key, String value) {
        if (value != null && !value.isEmpty()) {
            instrumentation.tracer().setAttribute(key, value, spanContext);
        }
    }
}
