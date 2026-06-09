// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import java.util.List;

/**
 * Formats messages for span attributes, respecting the content recording privacy gate.
 *
 * <p>When content recording is OFF, messages include only structural information
 * (roles and types) without any user content. When ON, full message text is included.</p>
 */
public final class GenAiMessageFormatter {

    private GenAiMessageFormatter() {
        // utility class
    }

    /**
     * Formats a user text input message for the gen_ai.input.messages attribute.
     *
     * @param text the user's input text.
     * @return the JSON-formatted message string.
     */
    public static String formatUserTextInput(String text) {
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            return "[{\"role\":\"user\",\"parts\":[{\"type\":\"text\",\"content\":" + jsonEscape(text) + "}]}]";
        } else {
            return "[{\"role\":\"user\",\"parts\":[{\"type\":\"text\"}]}]";
        }
    }

    /**
     * Formats a tool response input message for the gen_ai.input.messages attribute.
     *
     * @param toolCallId the tool call ID.
     * @param content the tool response content (gated).
     * @return the JSON-formatted message string.
     */
    public static String formatToolResponseInput(String toolCallId, String content) {
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            return "[{\"role\":\"tool\",\"parts\":[{\"type\":\"tool_call_response\",\"id\":" + jsonEscape(toolCallId)
                + ",\"content\":" + jsonEscape(content) + "}]}]";
        } else {
            return "[{\"role\":\"tool\",\"parts\":[{\"type\":\"tool_call_response\",\"id\":" + jsonEscape(toolCallId)
                + "}]}]";
        }
    }

    /**
     * Formats a text output message for the gen_ai.output.messages attribute.
     *
     * @param text the assistant's response text.
     * @param finishReason the completion finish reason.
     * @return the JSON-formatted message string.
     */
    public static String formatTextOutput(String text, String finishReason) {
        String finishPart = finishReason != null ? ",\"finish_reason\":" + jsonEscape(finishReason) : "";
        if (GenAiTracingConfiguration.isContentRecordingEnabled()) {
            return "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\",\"content\":" + jsonEscape(text) + "}]"
                + finishPart + "}]";
        } else {
            return "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\"}]" + finishPart + "}]";
        }
    }

    /**
     * Formats a tool call output message for the gen_ai.output.messages attribute.
     *
     * @param toolCallId the tool call ID.
     * @param toolType the type of tool call (e.g., "function_call", "code_interpreter_call").
     * @param content optional additional content (gated); may be null.
     * @return the JSON-formatted message string.
     */
    public static String formatToolCallOutput(String toolCallId, String toolType, String content) {
        StringBuilder sb = new StringBuilder("[{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\"");
        if (toolCallId != null) {
            sb.append(",\"id\":").append(jsonEscape(toolCallId));
        }
        if (GenAiTracingConfiguration.isContentRecordingEnabled() && content != null) {
            sb.append(",\"content\":{\"type\":").append(jsonEscape(toolType));
            sb.append(",\"id\":").append(jsonEscape(toolCallId));
            sb.append("}");
        } else if (content != null) {
            // No content, but include the type info for code interpreter etc.
            sb.append(",\"content\":{\"type\":").append(jsonEscape(toolType));
            sb.append(",\"id\":").append(jsonEscape(toolCallId));
            sb.append("}");
        }
        sb.append("}]}]");
        return sb.toString();
    }

    /**
     * Formats a multi-part output message that includes both tool calls and text.
     *
     * @param parts list of pre-formatted part JSON strings.
     * @param finishReason the finish reason (may be null).
     * @return the combined JSON-formatted message string.
     */
    public static String formatMultiPartOutput(List<String> parts, String finishReason) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(parts.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Formats a raw pre-built messages JSON string directly.
     * Used when the caller has already composed the message format.
     *
     * @param messagesJson the pre-formatted JSON string.
     * @return the input string unchanged.
     */
    public static String formatRaw(String messagesJson) {
        return messagesJson;
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
