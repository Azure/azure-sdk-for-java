// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.core.util.logging.ClientLogger;
import com.openai.core.JsonValue;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseComputerToolCall;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFileSearchToolCall;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseFunctionWebSearch;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.ResponseUsage;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.azure.ai.agents.telemetry.GenAiConstants.GEN_AI_EVENT_CONTENT;
import static com.azure.ai.agents.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME;
import static com.azure.ai.agents.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME_VALUE;
import static com.azure.ai.agents.telemetry.GenAiConstants.GEN_AI_WORKFLOW_ACTION;
import static com.azure.ai.agents.telemetry.GenAiConstants.OPERATION_CHAT;
import static com.azure.ai.agents.telemetry.GenAiConstants.OPERATION_CREATE_CONVERSATION;
import static com.azure.ai.agents.telemetry.GenAiConstants.OPERATION_INVOKE_AGENT;

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
     * @param instructions the system instructions (may be null).
     * @param conversationId the conversation ID (may be null).
     * @param operation the supplier that performs the actual API call.
     * @return the response from the operation.
     * @throws RuntimeException if the operation throws a checked exception.
     */
    @SuppressWarnings("try")
    public static Response traceResponse(String operationName, String nameForSpan, String agentName, URI endpoint,
        String inputMessages, String instructions, String conversationId, Supplier<Response> operation) {
        GenAiTracingScope scope = startOperationScope(operationName, nameForSpan, endpoint);
        if (scope == null) {
            return operation.get();
        }

        boolean isInvokeAgent = OPERATION_INVOKE_AGENT.equals(operationName);

        try {
            if (agentName != null) {
                scope.setAgentAttributes(null, agentName, null, null);
            }
            scope.setInputMessages(inputMessages);
            if (!isInvokeAgent && instructions != null && GenAiTracingConfiguration.isContentRecordingEnabled()) {
                scope.setSystemInstructions(instructions);
            }
            if (conversationId != null) {
                scope.setConversationId(conversationId);
            }

            Response response;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                response = operation.get();
            }

            recordResponseAttributes(scope, response, isInvokeAgent);
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
     * @param instructions the system instructions (may be null).
     * @param conversationId the conversation ID (may be null).
     * @param operation the supplier that starts the streaming operation.
     * @return a traced iterable that wraps the stream and records attributes on completion.
     * @throws RuntimeException if the operation throws a checked exception.
     */
    @SuppressWarnings("try")
    public static TracedStreamIterable traceStreamingResponse(String operationName, String nameForSpan,
        String agentName, URI endpoint, String inputMessages, String instructions, String conversationId,
        Supplier<Iterable<ResponseStreamEvent>> operation) {
        GenAiTracingScope scope = startOperationScope(operationName, nameForSpan, endpoint);
        if (scope == null) {
            return new TracedStreamIterable(operation.get(), null);
        }

        boolean isInvokeAgent = OPERATION_INVOKE_AGENT.equals(operationName);

        if (agentName != null) {
            scope.setAgentAttributes(null, agentName, null, null);
        }
        scope.setInputMessages(inputMessages);
        if (!isInvokeAgent && instructions != null && GenAiTracingConfiguration.isContentRecordingEnabled()) {
            scope.setSystemInstructions(instructions);
        }
        if (conversationId != null) {
            scope.setConversationId(conversationId);
        }

        try {
            Iterable<ResponseStreamEvent> stream;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                stream = operation.get();
            }
            return new TracedStreamIterable(stream, scope, isInvokeAgent);
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
            case OPERATION_CHAT:
                return GenAiTracingScope.startChat(nameForSpan, endpoint);

            case OPERATION_INVOKE_AGENT:
                return GenAiTracingScope.startInvokeAgent(nameForSpan, endpoint);

            case OPERATION_CREATE_CONVERSATION:
                return GenAiTracingScope.startCreateConversation(endpoint);

            default:
                return GenAiTracingScope.startChat(nameForSpan, endpoint);
        }
    }

    /**
     * Traces a non-streaming response operation, extracting tracing parameters from the request objects.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the built request parameters.
     * @param endpoint the service endpoint.
     * @param operation the supplier that performs the actual API call.
     * @return the response from the operation.
     */
    public static Response traceResponse(AzureCreateResponseOptions createResponse, ResponseCreateParams builtParams,
        URI endpoint, Supplier<Response> operation) {
        String model = builtParams.model().isPresent() ? extractModelString(builtParams.model().get()) : null;
        AgentReference agentRef = createResponse.getAgentReference();
        String agentName = agentRef != null ? agentRef.getName() : null;
        String operationName = agentName != null ? OPERATION_INVOKE_AGENT : OPERATION_CHAT;
        String nameForSpan = agentName != null ? agentName : model;
        String inputMessages = extractInputMessages(builtParams);
        String instructions = builtParams.instructions().orElse("");
        String conversationId = builtParams.conversation().isPresent() ? builtParams.conversation().get().asId() : null;

        return traceResponse(operationName, nameForSpan, agentName, endpoint, inputMessages, instructions,
            conversationId, operation);
    }

    /**
     * Traces a streaming response operation, extracting tracing parameters from the request objects.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the built request parameters.
     * @param endpoint the service endpoint.
     * @param operation the supplier that starts the streaming operation.
     * @return a traced iterable that wraps the stream and records attributes on completion.
     */
    public static TracedStreamIterable traceStreamingResponse(AzureCreateResponseOptions createResponse,
        ResponseCreateParams builtParams, URI endpoint, Supplier<Iterable<ResponseStreamEvent>> operation) {
        String model = builtParams.model().isPresent() ? extractModelString(builtParams.model().get()) : null;
        AgentReference agentRef = createResponse.getAgentReference();
        String agentName = agentRef != null ? agentRef.getName() : null;
        String operationName = agentName != null ? OPERATION_INVOKE_AGENT : OPERATION_CHAT;
        String nameForSpan = agentName != null ? agentName : model;
        String inputMessages = extractInputMessages(builtParams);
        String instructions = builtParams.instructions().orElse("");
        String conversationId = builtParams.conversation().isPresent() ? builtParams.conversation().get().asId() : null;

        return traceStreamingResponse(operationName, nameForSpan, agentName, endpoint, inputMessages, instructions,
            conversationId, operation);
    }

    /**
     * Extracts and formats input messages from ResponseCreateParams for tracing.
     *
     * @param builtParams the built request parameters.
     * @return formatted input messages JSON string, or null if input cannot be extracted.
     */
    static String extractInputMessages(ResponseCreateParams builtParams) {
        if (!builtParams.input().isPresent()) {
            return null;
        }

        ResponseCreateParams.Input input = builtParams.input().get();
        if (input.isText()) {
            return GenAiMessageFormatter.formatUserTextInput(input.asText());
        }

        if (input.isResponse()) {
            List<ResponseInputItem> items = input.asResponse();
            for (int i = items.size() - 1; i >= 0; i--) {
                ResponseInputItem item = items.get(i);
                if (item.easyInputMessage().isPresent()) {
                    EasyInputMessage msg = item.easyInputMessage().get();
                    if (EasyInputMessage.Role.USER.equals(msg.role()) && msg.content().isTextInput()) {
                        return GenAiMessageFormatter.formatUserTextInput(msg.content().asTextInput());
                    }
                }
                if (item.functionCallOutput().isPresent()) {
                    ResponseInputItem.FunctionCallOutput fco = item.functionCallOutput().get();
                    String output = fco.output().isString() ? fco.output().asString() : null;
                    return GenAiMessageFormatter.formatToolResponseInput(fco.callId(), output);
                }
            }
        }

        return null;
    }

    private static void recordResponseAttributes(GenAiTracingScope scope, Response response, boolean isInvokeAgent) {
        if (response == null) {
            return;
        }

        String responseId = response.id();
        String responseModel = extractModelString(response.model());
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

        // Set request model only for chat operations (not invoke_agent)
        if (responseModel != null && !isInvokeAgent) {
            scope.setRequestModelAttributes(responseModel, null, null);
        }

        // Emit workflow action events for workflow_action output items
        emitWorkflowActionEvents(scope, response);
    }

    static String formatOutputFromResponse(Response response) {
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
                sb.append(formatOutputMessage(item.asMessage()));
            } else if (item.isFunctionCall()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatFunctionCall(item.asFunctionCall()));
            } else if (item.isFileSearchCall()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatFileSearchCall(item.asFileSearchCall()));
            } else if (item.isWebSearchCall()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatWebSearchCall(item.asWebSearchCall()));
            } else if (item.isCodeInterpreterCall()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatCodeInterpreterCall(item.asCodeInterpreterCall()));
            } else if (item.isComputerCall()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatComputerCall(item.asComputerCall()));
            } else if (item.isMcpCall()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatMcpCall(item.asMcpCall()));
            } else if (item.isImageGenerationCall()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatImageGenerationCall(item.asImageGenerationCall()));
            } else if (item.isMcpApprovalRequest()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatMcpApprovalRequest(item.asMcpApprovalRequest()));
            } else if (item.isMcpListTools()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(formatMcpListTools(item.asMcpListTools()));
            }
        }

        if (first) {
            return "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\"}],\"finish_reason\":\"completed\"}]";
        }

        sb.append("]");
        return sb.toString();
    }

    static String formatFunctionCall(ResponseFunctionToolCall funcCall) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"id\":");
        sb.append(jsonEscape(funcCall.callId()));
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            sb.append(",\"name\":").append(jsonEscape(funcCall.name()));
            sb.append(",\"arguments\":").append(jsonEscape(funcCall.arguments()));
        }
        sb.append("}]}");
        return sb.toString();
    }

    static String formatFileSearchCall(ResponseFileSearchToolCall fileSearch) {
        StringBuilder sb = new StringBuilder(
            "{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"content\":{\"type\":\"file_search_call\",\"id\":");
        sb.append(jsonEscape(fileSearch.id()));
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            List<String> queries = fileSearch.queries();
            if (queries != null && !queries.isEmpty()) {
                sb.append(",\"queries\":[");
                for (int i = 0; i < queries.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(jsonEscape(queries.get(i)));
                }
                sb.append("]");
            }
            Optional<List<ResponseFileSearchToolCall.Result>> results = fileSearch.results();
            if (results.isPresent() && !results.get().isEmpty()) {
                sb.append(",\"results\":[");
                boolean firstResult = true;
                for (ResponseFileSearchToolCall.Result result : results.get()) {
                    if (!firstResult) {
                        sb.append(",");
                    }
                    firstResult = false;
                    sb.append("{");
                    boolean hasField = false;
                    if (result.fileId().isPresent()) {
                        sb.append("\"file_id\":").append(jsonEscape(result.fileId().get()));
                        hasField = true;
                    }
                    if (result.filename().isPresent()) {
                        if (hasField) {
                            sb.append(",");
                        }
                        sb.append("\"filename\":").append(jsonEscape(result.filename().get()));
                        hasField = true;
                    }
                    if (result.score().isPresent()) {
                        if (hasField) {
                            sb.append(",");
                        }
                        sb.append("\"score\":").append(result.score().get());
                    }
                    sb.append("}");
                }
                sb.append("]");
            }
        }
        sb.append("}}]}");
        return sb.toString();
    }

    static String formatWebSearchCall(ResponseFunctionWebSearch webSearch) {
        StringBuilder sb = new StringBuilder(
            "{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"content\":{\"type\":\"web_search_call\",\"id\":");
        sb.append(jsonEscape(webSearch.id()));
        sb.append("}}]}");
        return sb.toString();
    }

    static String formatCodeInterpreterCall(ResponseCodeInterpreterToolCall codeInterpreter) {
        StringBuilder sb = new StringBuilder(
            "{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"content\":{\"type\":\"code_interpreter_call\",\"id\":");
        sb.append(jsonEscape(codeInterpreter.id()));
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            Optional<String> code = codeInterpreter.code();
            if (code.isPresent()) {
                sb.append(",\"code\":").append(jsonEscape(code.get()));
            }
        }
        sb.append("}}]}");
        return sb.toString();
    }

    static String formatComputerCall(ResponseComputerToolCall computerCall) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"computer_call\",\"id\":");
        sb.append(jsonEscape(computerCall.callId()));
        sb.append("}]}");
        return sb.toString();
    }

    static String formatMcpCall(ResponseOutputItem.McpCall mcpCall) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"mcp_call\",\"id\":");
        sb.append(jsonEscape(mcpCall.id()));
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            sb.append(",\"name\":").append(jsonEscape(mcpCall.name()));
            sb.append(",\"server_label\":").append(jsonEscape(mcpCall.serverLabel()));
            sb.append(",\"arguments\":").append(jsonEscape(mcpCall.arguments()));
        }
        sb.append("}]}");
        return sb.toString();
    }

    static String formatImageGenerationCall(ResponseOutputItem.ImageGenerationCall imageGen) {
        StringBuilder sb
            = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"image_generation_call\",\"id\":");
        sb.append(jsonEscape(imageGen.id()));
        sb.append("}]}");
        return sb.toString();
    }

    static String formatMcpApprovalRequest(ResponseOutputItem.McpApprovalRequest approval) {
        StringBuilder sb
            = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"mcp_approval_request\",\"id\":");
        sb.append(jsonEscape(approval.id()));
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            sb.append(",\"name\":").append(jsonEscape(approval.name()));
            sb.append(",\"server_label\":").append(jsonEscape(approval.serverLabel()));
            sb.append(",\"arguments\":").append(jsonEscape(approval.arguments()));
        }
        sb.append("}]}");
        return sb.toString();
    }

    static String formatMcpListTools(ResponseOutputItem.McpListTools mcpListTools) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"mcp_list_tools\",\"id\":");
        sb.append(jsonEscape(mcpListTools.id()));
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            sb.append(",\"server_label\":").append(jsonEscape(mcpListTools.serverLabel()));
        }
        sb.append("}]}");
        return sb.toString();
    }

    static String formatOutputMessage(ResponseOutputMessage message) {
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

    /**
     * Extracts the model name string from a ResponsesModel union type.
     *
     * @param model the model union type.
     * @return the model name string, or null if the model is null or unrecognized.
     */
    public static String extractModelString(com.openai.models.ResponsesModel model) {
        if (model == null) {
            return null;
        }
        if (model.isString()) {
            return model.asString();
        }
        if (model.isChat()) {
            return model.asChat().asString();
        }
        if (model.isOnly()) {
            return model.asOnly().asString();
        }
        return model.toString();
    }

    /**
     * Emits gen_ai.workflow.action events for any workflow_action output items in the response.
     * Package-private to allow use from {@link TracedStreamIterable}.
     */
    static void emitWorkflowActionEventsIfPresent(GenAiTracingScope scope, Response response) {
        emitWorkflowActionEvents(scope, response);
    }

    /**
     * Emits gen_ai.workflow.action events for any workflow_action output items in the response.
     */
    @SuppressWarnings("unchecked")
    private static void emitWorkflowActionEvents(GenAiTracingScope scope, Response response) {
        if (response.output() == null || response.output().isEmpty()) {
            return;
        }

        for (ResponseOutputItem item : response.output()) {
            // workflow_action is not explicitly modeled — detect via raw JSON
            Optional<JsonValue> rawJson = item._json();
            if (!rawJson.isPresent()) {
                continue;
            }

            Optional<Map<String, JsonValue>> objOpt = rawJson.get().asObject();
            if (!objOpt.isPresent()) {
                continue;
            }

            Map<String, JsonValue> obj = objOpt.get();
            JsonValue typeVal = obj.get("type");
            if (typeVal == null) {
                continue;
            }

            Optional<String> typeStr = typeVal.asString();
            if (!typeStr.isPresent() || !"workflow_action".equals(typeStr.get())) {
                continue;
            }

            emitSingleWorkflowActionEvent(scope, obj);
        }
    }

    /**
     * Emits a single gen_ai.workflow.action event from a workflow_action output item.
     */
    @SuppressWarnings("unchecked")
    private static void emitSingleWorkflowActionEvent(GenAiTracingScope scope, Map<String, JsonValue> item) {
        String contentArray = formatWorkflowActionEventContent(item);

        Map<String, Object> eventAttributes = new HashMap<>();
        eventAttributes.put(GEN_AI_PROVIDER_NAME, GEN_AI_PROVIDER_NAME_VALUE);
        eventAttributes.put(GEN_AI_EVENT_CONTENT, contentArray);
        scope.addEvent(GEN_AI_WORKFLOW_ACTION, eventAttributes);
    }

    /**
     * Formats the content array for a gen_ai.workflow.action event.
     * Package-private for testing.
     */
    @SuppressWarnings("unchecked")
    static String formatWorkflowActionEventContent(Map<String, JsonValue> item) {
        StringBuilder workflowDetails = new StringBuilder("{");
        boolean hasField = false;

        // Always include status
        JsonValue statusVal = item.get("status");
        if (statusVal != null) {
            Optional<String> status = statusVal.asString();
            if (status.isPresent()) {
                workflowDetails.append("\"status\":").append(jsonEscape(status.get()));
                hasField = true;
            }
        }

        // Include action_id and previous_action_id only when content recording is enabled
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            JsonValue actionIdVal = item.get("action_id");
            if (actionIdVal != null) {
                Optional<String> actionId = actionIdVal.asString();
                if (actionId.isPresent()) {
                    if (hasField) {
                        workflowDetails.append(",");
                    }
                    workflowDetails.append("\"action_id\":").append(jsonEscape(actionId.get()));
                    hasField = true;
                }
            }

            JsonValue prevActionIdVal = item.get("previous_action_id");
            if (prevActionIdVal != null) {
                Optional<String> prevActionId = prevActionIdVal.asString();
                if (prevActionId.isPresent()) {
                    if (hasField) {
                        workflowDetails.append(",");
                    }
                    workflowDetails.append("\"previous_action_id\":").append(jsonEscape(prevActionId.get()));
                    hasField = true;
                }
            }
        }

        workflowDetails.append("}");

        return "[{\"role\":\"workflow\",\"parts\":[{\"type\":\"workflow_action\",\"content\":"
            + workflowDetails.toString() + "}]}]";
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

    /**
     * Traces a create_conversation operation.
     *
     * @param endpoint the service endpoint.
     * @param conversationId the conversation ID returned by the service.
     */
    public static void traceCreateConversation(URI endpoint, String conversationId) {
        GenAiTracingScope scope = GenAiTracingScope.startCreateConversation(endpoint);
        if (scope == null) {
            return;
        }
        try {
            if (conversationId != null) {
                scope.setConversationId(conversationId);
            }
        } finally {
            scope.close();
        }
    }
}
