// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import java.util.List;

/**
 * OpenTelemetry instrumentation helpers for GenAI model and tool operations.
 * <p>
 * Sensitive prompts, model outputs, tool arguments, and tool results are recorded
 * only when {@code OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT=true} or
 * {@code -Dotel.instrumentation.genai.capture-message-content=true}.
 */
public final class GenAiObservability {

    private static final String CAPTURE_MESSAGE_CONTENT
        = "OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT";
    private static final String CAPTURE_MESSAGE_CONTENT_PROPERTY
        = "otel.instrumentation.genai.capture-message-content";
    private static final AttributeKey<List<String>> FINISH_REASONS
        = AttributeKey.stringArrayKey("gen_ai.response.finish_reasons");
    private static volatile Tracer testTracer;

    private GenAiObservability() {
    }

    /**
     * Starts a GenAI {@code chat} client span for a model invocation.
     *
     * @param requestModel the requested model or deployment name
     * @return the started span; the caller must end it
     */
    public static Span startChatSpan(String requestModel) {
        String spanName = hasText(requestModel) ? "chat " + requestModel : "chat";
        var builder = getTracer()
            .spanBuilder(spanName)
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute("gen_ai.operation.name", "chat")
            .setAttribute("gen_ai.provider.name", "azure.ai.openai");
        if (hasText(requestModel)) {
            builder.setAttribute("gen_ai.request.model", requestModel);
        }
        return builder.startSpan();
    }

    /**
     * Records a successful chat response and ends its span.
     *
     * @param span           the chat span
     * @param responseId     the provider response identifier
     * @param responseModel  the provider response model
     * @param inputTokens    input token count
     * @param outputTokens   output token count
     * @param finishReasons  completion finish reasons
     */
    public static void recordChatResponse(Span span, String responseId, String responseModel,
                                          Long inputTokens, Long outputTokens, List<String> finishReasons) {
        if (span == null) {
            return;
        }
        if (hasText(responseId)) {
            span.setAttribute("gen_ai.response.id", responseId);
        }
        if (hasText(responseModel)) {
            span.setAttribute("gen_ai.response.model", responseModel);
        }
        if (inputTokens != null) {
            span.setAttribute("gen_ai.usage.input_tokens", inputTokens);
        }
        if (outputTokens != null) {
            span.setAttribute("gen_ai.usage.output_tokens", outputTokens);
        }
        if (finishReasons != null && !finishReasons.isEmpty()) {
            span.setAttribute(FINISH_REASONS, finishReasons);
        }
        span.end();
    }

    /**
     * Records a chat failure and ends its span.
     *
     * @param span  the chat span
     * @param error the failure
     */
    public static void recordChatError(Span span, Throwable error) {
        if (span == null) {
            return;
        }
        recordError(span, error);
        span.end();
    }

    /**
     * Records opt-in GenAI message and tool-definition content on a chat span.
     *
     * @param span                   the chat span
     * @param inputMessagesJson      serialized input messages
     * @param outputMessagesJson     serialized output messages
     * @param systemInstructionsJson serialized system instructions
     * @param toolDefinitionsJson    serialized tool definitions
     */
    public static void setChatMessages(Span span, String inputMessagesJson, String outputMessagesJson,
                                       String systemInstructionsJson, String toolDefinitionsJson) {
        setChatMessages(span, inputMessagesJson, outputMessagesJson, systemInstructionsJson,
            toolDefinitionsJson, isMessageContentCaptureEnabled());
    }

    /**
     * Returns whether sensitive GenAI message and tool content capture is explicitly enabled.
     *
     * @return {@code true} only when the environment variable or system property is {@code true}
     */
    public static boolean isMessageContentCaptureEnabled() {
        String value = System.getenv(CAPTURE_MESSAGE_CONTENT);
        if (value == null) {
            value = System.getProperty(CAPTURE_MESSAGE_CONTENT_PROPERTY);
        }
        return Boolean.parseBoolean(value);
    }

    static void setChatMessages(Span span, String inputMessagesJson, String outputMessagesJson,
                                String systemInstructionsJson, String toolDefinitionsJson, boolean capture) {
        if (span == null || !capture) {
            return;
        }
        setIfPresent(span, "gen_ai.input.messages", inputMessagesJson);
        setIfPresent(span, "gen_ai.output.messages", outputMessagesJson);
        setIfPresent(span, "gen_ai.system_instructions", systemInstructionsJson);
        setIfPresent(span, "gen_ai.tool.definitions", toolDefinitionsJson);
    }

    /**
     * Starts an {@code execute_tool} span immediately before tool execution.
     *
     * @param toolName  the tool name
     * @param callId    the model-assigned tool-call identifier
     * @param arguments serialized JSON arguments
     * @return the started span; complete it with {@link #recordToolResult}
     */
    public static Span startExecuteToolSpan(String toolName, String callId, String arguments) {
        return startExecuteToolSpan(toolName, callId, arguments, isMessageContentCaptureEnabled());
    }

    static Span startExecuteToolSpan(String toolName, String callId, String arguments, boolean capture) {
        String name = hasText(toolName) ? toolName : "unknown";
        var builder = getTracer()
            .spanBuilder("execute_tool " + name)
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("gen_ai.operation.name", "execute_tool")
            .setAttribute("gen_ai.tool.name", name)
            .setAttribute("gen_ai.tool.type", "function");
        if (hasText(callId)) {
            builder.setAttribute("gen_ai.tool.call.id", callId);
        }
        if (capture && hasText(arguments)) {
            builder.setAttribute("gen_ai.tool.call.arguments", arguments);
        }
        return builder.startSpan();
    }

    /**
     * Records a tool result or failure and ends the tool span.
     *
     * @param span   the tool span
     * @param result the successful tool result
     * @param error  the tool failure, or {@code null} on success
     */
    public static void recordToolResult(Span span, String result, Throwable error) {
        recordToolResult(span, result, error, isMessageContentCaptureEnabled());
    }

    static void recordToolResult(Span span, String result, Throwable error, boolean capture) {
        if (span == null) {
            return;
        }
        if (error != null) {
            recordError(span, error);
        } else if (capture && hasText(result)) {
            span.setAttribute("gen_ai.tool.call.result", result);
        }
        span.end();
    }

    static void setTracerForTest(Tracer tracer) {
        testTracer = tracer;
    }

    private static Tracer getTracer() {
        Tracer tracer = testTracer;
        return tracer != null ? tracer : Observability.getResponsesTracer();
    }

    private static void recordError(Span span, Throwable error) {
        if (error == null) {
            span.setStatus(StatusCode.ERROR);
            return;
        }
        span.recordException(error);
        span.setAttribute("error.type", error.getClass().getName());
        span.setStatus(StatusCode.ERROR, error.getMessage() != null ? error.getMessage() : "");
    }

    private static void setIfPresent(Span span, String key, String value) {
        if (hasText(value)) {
            span.setAttribute(key, value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isEmpty();
    }
}
