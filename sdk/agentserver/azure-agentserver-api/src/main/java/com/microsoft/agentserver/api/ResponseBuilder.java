// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.IdGenerator;
import com.openai.core.JsonMissing;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseFunctionToolCallOutputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ToolChoiceOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for constructing {@link Response} objects from output content.
 * <p>
 * Uses {@link IdGenerator} for consistent Foundry-format IDs across the codebase.
 */
public final class ResponseBuilder {

    private static final String DEFAULT_MODEL = "gpt-4o";

    private ResponseBuilder() {
        // Static utility class — do not instantiate.
    }

    /**
     * Extracts the model name from the request.
     * Returns the model specified in the request, or {@value #DEFAULT_MODEL} as a default fallback.
     *
     * @param request the create response request
     * @return the model name
     */
    public static String getModelName(AgentServerCreateResponse request) {
        try {
            ResponseCreateParams.Body body = request.responseCreateParams();
            if (body.model().isPresent() &&
                body.model().get()._json().isPresent() &&
                body.model().get()._json().get().asString().isPresent()) {
                return body.model().get()._json().get().asString().get().toString();
            }
        } catch (Exception e) {
            // Fall through to default
        }
        return DEFAULT_MODEL;
    }

    /**
     * Constructs a complete {@link Response} wrapping the given output text.
     *
     * @param createResponse     the original create request (used to extract model name)
     * @param responseOutputText the text content to include in the response
     * @return a fully-formed Response object
     */
    public static Response convertOutputToResponse(
        AgentServerCreateResponse createResponse,
        ResponseOutputText responseOutputText) {
        return convertOutputToResponse(createResponse, responseOutputText, List.of());
    }

    /**
     * Constructs a complete {@link Response} wrapping the given output text and,
     * when present, the tool calls the agent made while producing it.
     * <p>
     * Each tool call is emitted as a {@code function_call} output item followed by
     * a {@code function_call_output} item (when a result was captured), ordered
     * before the final assistant text message. Persisting the tool trajectory this
     * way lets agent-run evaluators (for example Azure AI Foundry
     * <em>task adherence</em>) see which tools were actually invoked.
     *
     * @param createResponse     the original create request (used to extract model name)
     * @param responseOutputText the text content to include in the response
     * @param toolCalls          the tool calls made while producing the response; may be empty
     * @return a fully-formed Response object
     */
    public static Response convertOutputToResponse(
        AgentServerCreateResponse createResponse,
        ResponseOutputText responseOutputText,
        List<ToolCallRecord> toolCalls) {
        // IDs use a fresh partition key here; the API layer
        // (AgentServerResponsesApi.normalizeIdsAndStamp) re-partitions all
        // child IDs to match the resolved response ID before persistence.
        IdGenerator idGen = new IdGenerator(null);
        String responseId = idGen.generateResponseId();
        String messageId = idGen.generateMessageItemId();

        ResponseOutputMessage.Content responseOutputMessage = ResponseOutputMessage.Content.ofOutputText(responseOutputText);

        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .addContent(responseOutputMessage)
            .id(messageId)
            .status(ResponseOutputMessage.Status.COMPLETED)
            .build();

        List<ResponseOutputItem> outputItems = new ArrayList<>();
        if (toolCalls != null) {
            for (ToolCallRecord call : toolCalls) {
                if (call == null || call.id() == null || call.id().isEmpty()) {
                    continue;
                }
                ResponseFunctionToolCall functionCall = ResponseFunctionToolCall.builder()
                    .id(idGen.generateFunctionCallItemId())
                    .callId(call.id())
                    .name(call.name() == null ? "" : call.name())
                    .arguments(call.arguments() == null ? "" : call.arguments())
                    .status(ResponseFunctionToolCall.Status.COMPLETED)
                    .build();
                outputItems.add(ResponseOutputItem.ofFunctionCall(functionCall));

                if (call.output() != null) {
                    ResponseFunctionToolCallOutputItem functionCallOutput =
                        ResponseFunctionToolCallOutputItem.builder()
                            .id(idGen.generateFunctionCallItemId())
                            .callId(call.id())
                            .output(call.output())
                            .status(ResponseFunctionToolCallOutputItem.Status.COMPLETED)
                            .build();
                    outputItems.add(ResponseOutputItem.ofFunctionCallOutput(functionCallOutput));
                }
            }
        }
        outputItems.add(ResponseOutputItem.ofMessage(message));

        Response.Builder builder = Response.builder()
            .id(responseId)
            .createdAt(System.currentTimeMillis() / 1000.0)
            .output(outputItems)
            .model(getModelName(createResponse))
            .parallelToolCalls(false)
            .tools(List.of())
            .status(ResponseStatus.COMPLETED)
            .toolChoice(ToolChoiceOptions.AUTO)
            .error(JsonMissing.of())
            .incompleteDetails(JsonMissing.of())
            .instructions(JsonMissing.of())
            .metadata(JsonMissing.of())
            .temperature(JsonMissing.of())
            .topP(JsonMissing.of());

        // Only echo a conversation id when the client supplied one. The platform
        // storage backend rejects responses that reference a conversation_id it
        // does not already know about ("conv_… not found").
        createResponse.responseCreateParams().conversation().ifPresent(conv -> {
            if (conv.isId()) {
                builder.conversation(Response.Conversation.builder().id(conv.asId()).build());
            }
        });

        return builder.build();
    }

    /**
     * Immutable description of a single tool call and its result, used to emit
     * {@code function_call}/{@code function_call_output} items into a stored
     * response. Framework-specific handlers translate their own tool-call
     * representation into this shape.
     */
    public record ToolCallRecord(String id, String name, String arguments, String output) {
        /**
         * Creates a tool-call record.
         *
         * @param id        the tool-call id linking the call to its output
         * @param name      the tool (function) name
         * @param arguments the raw JSON arguments string passed to the tool
         * @param output    the textual result the tool produced, or {@code null} if none was captured
         */
        public ToolCallRecord {
        }

        /**
         * @return the tool-call id linking the call to its output.
         */
        @Override
        public String id() {
            return id;
        }

        /**
         * @return the tool (function) name.
         */
        @Override
        public String name() {
            return name;
        }

        /**
         * @return the raw JSON arguments string passed to the tool.
         */
        @Override
        public String arguments() {
            return arguments;
        }

        /**
         * @return the textual result the tool produced, or {@code null} if none was captured.
         */
        @Override
        public String output() {
            return output;
        }
    }

}
