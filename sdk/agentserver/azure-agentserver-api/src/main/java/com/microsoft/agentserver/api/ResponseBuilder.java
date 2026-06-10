// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.IdGenerator;
import com.openai.core.JsonMissing;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ToolChoiceOptions;

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

        Response.Builder builder = Response.builder()
            .id(responseId)
            .createdAt(System.currentTimeMillis() / 1000.0)
            .addOutput(ResponseOutputItem.ofMessage(message))
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

}
