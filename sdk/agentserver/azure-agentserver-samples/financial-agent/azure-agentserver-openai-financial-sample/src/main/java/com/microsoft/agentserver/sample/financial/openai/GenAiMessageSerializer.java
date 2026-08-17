// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.sample.financial.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the JSON payloads defined by the OpenTelemetry GenAI semantic conventions
 * ({@code gen_ai.input.messages}, {@code gen_ai.output.messages},
 * {@code gen_ai.system_instructions} and {@code gen_ai.tool.definitions}) from the
 * raw values this framework-free sample already has on hand.
 * <p>
 * These are the payloads Azure AI Foundry agent-run evaluators (for example
 * <em>task adherence</em>) read to reconstruct the agent trajectory. The assistant's
 * tool-call decisions are emitted as {@code tool_call} parts inside
 * {@code gen_ai.output.messages}; tool results are emitted as
 * {@code tool_call_response} parts inside {@code gen_ai.input.messages}.
 * <p>
 * Because the underlying message representation here is simple strings rather than a
 * framework object graph, callers accumulate the running conversation as convention
 * message maps (via the {@code *Message} factory methods) and serialize the whole list
 * on each turn.
 */
final class GenAiMessageSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenAiMessageSerializer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GenAiMessageSerializer() {
    }

    /**
     * Serializes an accumulated list of convention message maps (as produced by the
     * {@code *Message} factory methods) to a JSON array string.
     *
     * @param messages the conversation messages (may be {@code null}/empty)
     * @return a JSON array string, or {@code null} if there is nothing to record
     */
    static String toJson(List<Map<String, Object>> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        return writeValue(messages);
    }

    /**
     * Builds the {@code gen_ai.system_instructions} JSON array for a single system prompt.
     *
     * @param systemPrompt the system prompt (may be {@code null}/empty)
     * @return a JSON array string, or {@code null} if there is no prompt
     */
    static String systemInstructions(String systemPrompt) {
        if (systemPrompt == null || systemPrompt.isEmpty()) {
            return null;
        }
        return writeValue(List.of(textPart(systemPrompt)));
    }

    /**
     * Creates a {@code {role, parts:[{type:text, content}]}} message.
     *
     * @param role the message role (e.g. {@code user})
     * @param text the message text
     * @return the convention message map
     */
    static Map<String, Object> textMessage(String role, String text) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("parts", List.of(textPart(text)));
        return message;
    }

    /**
     * Creates an assistant message carrying one {@code tool_call} part per requested call.
     *
     * @param toolCalls    the tool calls the model requested
     * @param finishReason the finish reason (may be {@code null})
     * @return the convention message map
     */
    static Map<String, Object> assistantToolCallsMessage(List<ToolCall> toolCalls, String finishReason) {
        List<Map<String, Object>> parts = new ArrayList<>();
        for (ToolCall call : toolCalls) {
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("type", "tool_call");
            part.put("id", call.id);
            part.put("name", call.name);
            part.put("arguments", parseJson(call.arguments));
            parts.add(part);
        }
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "assistant");
        message.put("parts", parts);
        if (finishReason != null && !finishReason.isEmpty()) {
            message.put("finish_reason", finishReason);
        }
        return message;
    }

    /**
     * Creates a {@code tool} message carrying a single {@code tool_call_response} part.
     *
     * @param callId the tool-call id this result answers
     * @param result the tool result text
     * @return the convention message map
     */
    static Map<String, Object> toolResultMessage(String callId, String result) {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("type", "tool_call_response");
        part.put("id", callId);
        part.put("response", result);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "tool");
        message.put("parts", List.of(part));
        return message;
    }

    private static Map<String, Object> textPart(String text) {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("type", "text");
        part.put("content", text);
        return part;
    }

    private static String writeValue(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to serialize GenAI telemetry content: {}", e.getMessage());
            return null;
        }
    }

    private static Object parseJson(String value) {
        if (value == null || value.isEmpty()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(value, Object.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Tool arguments are not valid JSON; recording the raw value: {}", e.getMessage());
            return value;
        }
    }

    /**
     * Minimal holder for a tool call used when rendering an assistant message.
     */
    static final class ToolCall {
        private final String id;
        private final String name;
        private final String arguments;

        ToolCall(String id, String name, String arguments) {
            this.id = id;
            this.name = name;
            this.arguments = arguments;
        }
    }
}
