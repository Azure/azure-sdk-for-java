// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Represents an incoming create-response request in the agent server protocol.
 * <p>
 * Wraps the standard OpenAI {@link ResponseCreateParams.Body} together with an
 * optional {@link AgentReference} identifying the target agent. Uses a custom
 * Jackson deserializer to handle the Foundry platform's request format, including
 * fallback handling for input items missing the {@code "type"} discriminator.
 */
@JsonDeserialize(using = AgentServerCreateResponse.AgentServerResponseCreateDeserializer.class)
public record AgentServerCreateResponse(AgentReference agent, ResponseCreateParams.Body responseCreateParams) {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentServerCreateResponse.class);

    /**
     * Extracts the input text from the request.
     * If the input is a simple text string, returns it directly.
     * Returns an empty string if no text input is present.
     *
     * @return the input text, or empty string
     */
    public String inputText() {
        if (responseCreateParams == null) {
            return "";
        }
        return responseCreateParams.input()
            .filter(ResponseCreateParams.Input::isText)
            .map(ResponseCreateParams.Input::asText)
            .orElse("");
    }

    public static class AgentServerResponseCreateDeserializer extends JsonDeserializer<AgentServerCreateResponse> {

        @Override
        public AgentServerCreateResponse deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

            try {
                JsonNode node = p.getCodec().readTree(p);

                AgentReference agent = null;
                // "agent_reference" is the canonical Foundry field name and takes
                // precedence over the legacy "agent" alias when both are present.
                if (node.has("agent_reference")) {
                    agent = p.getCodec().treeToValue(node.get("agent_reference"), AgentReference.class);
                } else if (node.has("agent")) {
                    agent = p.getCodec().treeToValue(node.get("agent"), AgentReference.class);
                }
                // First pass: deserialize as-is
                ResponseCreateParams.Body createResponse = p.getCodec().treeToValue(node, ResponseCreateParams.Body.class);

                createResponse = fixMessageParsingOnMissingTypes(p, createResponse, node);

                return new AgentServerCreateResponse(agent, createResponse);
            } catch (Exception e) {
                LOGGER.debug("Failed to deserialize AgentServerCreateResponse", e);
                throw e;
            }
        }

        private ResponseCreateParams.Body fixMessageParsingOnMissingTypes(JsonParser p, ResponseCreateParams.Body createResponse, JsonNode node) throws JsonProcessingException {
            // Detect input items that fell back to raw JSON (missing "type" discriminator)
            // and reparse with "type": "message" injected so they become EasyInputMessages.
            if (createResponse.input().isPresent()
                && !createResponse.input().get().isText()) {
                List<ResponseInputItem> items = createResponse.input().get().asResponse();
                boolean needsReparse = false;
                for (ResponseInputItem item : items) {
                    if (!item.isEasyInputMessage() && !item.isMessage()
                        && !item.isFunctionCall() && !item.isFunctionCallOutput()
                        && !item.isComputerCall() && !item.isComputerCallOutput()
                        && !item.isItemReference() && !item.isCodeInterpreterCall()
                        && item._json().isPresent()) {
                        needsReparse = true;
                        break;
                    }
                }

                if (needsReparse && node.has("input") && node.get("input").isArray()) {
                    ArrayNode inputArray = (ArrayNode) node.get("input");
                    boolean modified = false;
                    for (int i = 0; i < inputArray.size(); i++) {
                        JsonNode element = inputArray.get(i);
                        if (element.isObject() && !element.has("type")
                            && element.has("role") && element.has("content")) {
                            ((ObjectNode) element).put("type", "message");
                            modified = true;
                        }
                    }
                    if (modified) {
                        LOGGER.debug("Injected 'type: message' into input items missing type discriminator");
                        createResponse = p.getCodec().treeToValue(node, ResponseCreateParams.Body.class);
                    }
                }
            }
            return createResponse;
        }
    }
}
