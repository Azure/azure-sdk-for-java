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
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
     * <p>
     * Handles both request shapes accepted by the Responses API:
     * <ul>
     *   <li>A simple text input ({@code "input": "hello"}) — returned directly.</li>
     *   <li>A structured input array of message items (the shape sent by the Foundry
     *       chat playground and multi-turn clients) — the text of the most recent
     *       {@code user} message is returned. If no {@code user} message is found,
     *       the text of the last message containing any text is used.</li>
     * </ul>
     * Returns an empty string if no text input is present.
     *
     * @return the input text, or empty string
     */
    public String inputText() {
        if (responseCreateParams == null) {
            return "";
        }

        Optional<ResponseCreateParams.Input> inputOpt = responseCreateParams.input();
        if (inputOpt.isEmpty()) {
            return "";
        }

        ResponseCreateParams.Input input = inputOpt.get();
        if (input.isText()) {
            return input.asText();
        }

        if (input.isResponse()) {
            return extractTextFromItems(input.asResponse());
        }

        return "";
    }

    /**
     * Walks the input items and returns the text of the most recent {@code user}
     * message, falling back to the last message that contains any text.
     */
    private static String extractTextFromItems(List<ResponseInputItem> items) {
        String lastUserText = "";
        String lastAnyText = "";

        for (ResponseInputItem item : items) {
            String text;
            boolean isUser;

            if (item.isEasyInputMessage()) {
                EasyInputMessage message = item.asEasyInputMessage();
                text = textFromEasyContent(message.content());
                isUser = message.role().equals(EasyInputMessage.Role.USER);
            } else if (item.isMessage()) {
                ResponseInputItem.Message message = item.asMessage();
                text = textFromContentList(message.content());
                isUser = message.role().equals(ResponseInputItem.Message.Role.USER);
            } else {
                continue;
            }

            if (!text.isEmpty()) {
                lastAnyText = text;
                if (isUser) {
                    lastUserText = text;
                }
            }
        }

        return !lastUserText.isEmpty() ? lastUserText : lastAnyText;
    }

    private static String textFromEasyContent(EasyInputMessage.Content content) {
        if (content.isTextInput()) {
            return content.asTextInput();
        }
        if (content.isResponseInputMessageContentList()) {
            return textFromContentList(content.asResponseInputMessageContentList());
        }
        return "";
    }

    private static String textFromContentList(List<ResponseInputContent> contents) {
        StringBuilder sb = new StringBuilder();
        for (ResponseInputContent content : contents) {
            if (content.isInputText()) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(content.asInputText().text());
            }
        }
        return sb.toString();
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
