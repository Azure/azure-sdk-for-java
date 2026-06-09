// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.azure.core.util.logging.ClientLogger;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.ResponseUsage;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provides tracing integration for GenAI response operations.
 *
 * <p>This class wraps response create calls with OpenTelemetry spans, recording
 * attributes, metrics, and respecting content privacy settings.</p>
 *
 * <p>Usage pattern:</p>
 * <pre>{@code
 * Response response = GenAiResponseTracing.traceResponse(
 *     "chat", modelName, null, endpoint, inputMessages,
 *     () -> responsesClient.createResponse(params));
 * }</pre>
 */
public final class GenAiResponseTracing {

    private static final ClientLogger LOGGER = new ClientLogger(GenAiResponseTracing.class);

    private GenAiResponseTracing() {
        // utility class
    }

    /**
     * Traces a non-streaming response operation.
     *
     * @param operationName the operation name ("chat" or "invoke_agent").
     * @param nameForSpan the model or agent name for the span name.
     * @param agentName the agent name (null for chat operations).
     * @param endpoint the service endpoint.
     * @param inputMessages the formatted input messages (content-gated).
     * @param operation the supplier that performs the actual API call.
     * @return the response from the operation.
     */
    public static Response traceResponse(String operationName, String nameForSpan, String agentName, URI endpoint,
        String inputMessages, Supplier<Response> operation) {
        GenAiTracingScope scope = startOperationScope(operationName, nameForSpan, endpoint);
        if (scope == null) {
            return operation.get();
        }

        try {
            if (agentName != null) {
                scope.setAgentAttributes(null, agentName, null, null);
            }
            scope.setInputMessages(inputMessages);

            Response response = operation.get();

            recordResponseAttributes(scope, response);
            return response;
        } catch (Throwable ex) {
            scope.recordError(ex);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        } finally {
            scope.close();
        }
    }

    /**
     * Traces a streaming response operation. The span remains open until the stream
     * is fully consumed.
     *
     * @param operationName the operation name ("chat" or "invoke_agent").
     * @param nameForSpan the model or agent name for the span name.
     * @param agentName the agent name (null for chat operations).
     * @param endpoint the service endpoint.
     * @param inputMessages the formatted input messages (content-gated).
     * @param operation the supplier that starts the streaming operation.
     * @return a traced iterable that wraps the stream and records attributes on completion.
     */
    public static TracedStreamIterable traceStreamingResponse(String operationName, String nameForSpan,
        String agentName, URI endpoint, String inputMessages, Supplier<Iterable<ResponseStreamEvent>> operation) {
        GenAiTracingScope scope = startOperationScope(operationName, nameForSpan, endpoint);
        if (scope == null) {
            return new TracedStreamIterable(operation.get(), null);
        }

        if (agentName != null) {
            scope.setAgentAttributes(null, agentName, null, null);
        }
        scope.setInputMessages(inputMessages);

        try {
            Iterable<ResponseStreamEvent> stream = operation.get();
            return new TracedStreamIterable(stream, scope);
        } catch (Throwable ex) {
            scope.recordError(ex);
            scope.close();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }

    private static GenAiTracingScope startOperationScope(String operationName, String nameForSpan, URI endpoint) {
        switch (operationName) {
            case GenAiConstants.OPERATION_CHAT:
                return GenAiTracingScope.startChat(nameForSpan, endpoint);

            case GenAiConstants.OPERATION_INVOKE_AGENT:
                return GenAiTracingScope.startInvokeAgent(nameForSpan, endpoint);

            case GenAiConstants.OPERATION_CREATE_CONVERSATION:
                return GenAiTracingScope.startCreateConversation(endpoint);

            default:
                return GenAiTracingScope.startChat(nameForSpan, endpoint);
        }
    }

    private static void recordResponseAttributes(GenAiTracingScope scope, Response response) {
        if (response == null) {
            return;
        }

        String responseId = response.id();
        String responseModel = response.model() != null ? response.model().toString() : null;
        Long inputTokens = null;
        Long outputTokens = null;

        Optional<ResponseUsage> usageOpt = response.usage();
        if (usageOpt.isPresent()) {
            ResponseUsage usage = usageOpt.get();
            inputTokens = usage.inputTokens();
            outputTokens = usage.outputTokens();
        }

        // Extract output messages and finish reason
        String outputMessages = formatOutputFromResponse(response);

        scope.setResponseAttributes(responseId, responseModel, inputTokens, outputTokens, null);
        scope.setOutputMessages(outputMessages);

        // Set request model if available
        if (responseModel != null) {
            scope.setRequestModelAttributes(responseModel, null, null);
        }
    }

    private static String formatOutputFromResponse(Response response) {
        if (response.output() == null || response.output().isEmpty()) {
            return null;
        }

        List<ResponseOutputItem> outputs = response.output();
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (ResponseOutputItem item : outputs) {
            if (item.isMessage()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                ResponseOutputMessage message = item.asMessage();
                sb.append(formatOutputMessage(message));
            } else if (item.isFunctionCall()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append("{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"id\":");
                sb.append(jsonEscape(item.asFunctionCall().callId()));
                sb.append("}]}");
            }
        }

        if (first) {
            // No recognized items
            return "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\"}],\"finish_reason\":\"completed\"}]";
        }

        sb.append("]");
        return sb.toString();
    }

    private static String formatOutputMessage(ResponseOutputMessage message) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[");
        boolean firstPart = true;

        if (message.content() != null) {
            for (ResponseOutputMessage.Content contentPart : message.content()) {
                if (!firstPart) {
                    sb.append(",");
                }
                firstPart = false;
                if (contentPart.isOutputText()) {
                    ResponseOutputText textPart = contentPart.asOutputText();
                    if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
                        sb.append("{\"type\":\"text\",\"content\":").append(jsonEscape(textPart.text())).append("}");
                    } else {
                        sb.append("{\"type\":\"text\"}");
                    }
                } else {
                    sb.append("{\"type\":\"text\"}");
                }
            }
        }

        sb.append("],\"finish_reason\":\"completed\"}");
        return sb.toString();
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
