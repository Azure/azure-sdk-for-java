// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Formats messages for span attributes, respecting the content-recording privacy gate.
 *
 * <p>When content recording is OFF, messages include only structural information (roles and types)
 * without any user content. When ON, full message text is included. The {@code captureContent} flag is
 * passed in from the per-client {@link GenAiInstrumentation} rather than read from global state.</p>
 */
final class GenAiMessageFormatter {
    private static final ClientLogger LOGGER = new ClientLogger(GenAiMessageFormatter.class);

    private GenAiMessageFormatter() {
        // utility class
    }

    /**
     * Formats the {@code gen_ai.system_instructions} attribute value (content-gated).
     */
    static String formatSystemInstructions(boolean captureContent, String instructions) {
        Map<String, Object> textPart = jsonObject("type", "text");
        if (captureContent && instructions != null && !instructions.isEmpty()) {
            textPart.put("content", instructions);
        }
        return toJson(jsonArray(textPart));
    }

    /**
     * Formats a user text input message for the {@code gen_ai.input.messages} attribute.
     */
    static String formatUserTextInput(boolean captureContent, String text) {
        Map<String, Object> textPart = jsonObject("type", "text");
        if (captureContent) {
            textPart.put("content", text);
        }
        return toJson(jsonArray(jsonObject("role", "user", "parts", jsonArray(textPart))));
    }

    /**
     * Formats a tool response input message for the {@code gen_ai.input.messages} attribute.
     */
    static String formatToolResponseInput(boolean captureContent, String toolCallId, String content) {
        Map<String, Object> toolPart = jsonObject("type", "tool_call_response", "id", toolCallId);
        if (captureContent) {
            toolPart.put("result", parseResultValue(content));
        }
        return toJson(jsonArray(jsonObject("role", "tool", "parts", jsonArray(toolPart))));
    }

    /**
     * Formats a text output message for the {@code gen_ai.output.messages} attribute.
     */
    static String formatTextOutput(boolean captureContent, String text, String finishReason) {
        Map<String, Object> textPart = jsonObject("type", "text");
        if (captureContent) {
            textPart.put("content", text);
        }
        Map<String, Object> message = jsonObject("role", "assistant", "parts", jsonArray(textPart));
        if (finishReason != null) {
            message.put("finish_reason", finishReason);
        }
        return toJson(jsonArray(message));
    }

    /**
     * Formats a tool call output message for the {@code gen_ai.output.messages} attribute. The nested content object
     * (tool type and, when content recording is enabled, the extra content) is always emitted since the tool type is
     * always known.
     */
    static String formatToolCallOutput(boolean captureContent, String toolCallId, String toolType, String content) {
        Map<String, Object> nestedContent = jsonObject("type", toolType);
        Map<String, Object> toolPart = jsonObject("type", "tool_call");
        if (toolCallId != null) {
            toolPart.put("id", toolCallId);
            nestedContent.put("id", toolCallId);
        }
        if (captureContent && content != null) {
            nestedContent.put("content", content);
        }
        toolPart.put("content", nestedContent);
        return toJson(jsonArray(jsonObject("role", "assistant", "parts", jsonArray(toolPart))));
    }

    static Map<String, Object> jsonObject(Object... fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < fields.length; i += 2) {
            result.put((String) fields[i], fields[i + 1]);
        }
        return result;
    }

    static List<Object> jsonArray(Object... values) {
        return Arrays.asList(values);
    }

    static String toJson(Object value) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(stream)) {
            writeValue(writer, value);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException("Failed to serialize GenAI telemetry.", e));
        }
        return new String(stream.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void writeValue(JsonWriter writer, Object value) throws IOException {
        if (value instanceof Number) {
            writer.writeNumber((Number) value);
        } else if (value instanceof Map<?, ?>) {
            writer.writeStartObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                writer.writeFieldName(String.valueOf(entry.getKey()));
                writeValue(writer, entry.getValue());
            }
            writer.writeEndObject();
        } else if (value instanceof Iterable<?>) {
            writer.writeStartArray();
            for (Object element : (Iterable<?>) value) {
                writeValue(writer, element);
            }
            writer.writeEndArray();
        } else if (value instanceof JsonSerializable<?>) {
            ((JsonSerializable<?>) value).toJson(writer);
        } else {
            writer.writeUntyped(value);
        }
    }

    private static Object parseResultValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            try (JsonReader reader = JsonProviders.createReader(trimmed)) {
                Object result = reader.readUntyped();
                JsonToken trailingToken = reader.nextToken();
                if (trailingToken == null || trailingToken == JsonToken.END_DOCUMENT) {
                    return result;
                }
            } catch (IOException | IllegalStateException ignored) {
                // Preserve malformed or non-JSON tool output as a string.
            }
        }
        return value;
    }
}
