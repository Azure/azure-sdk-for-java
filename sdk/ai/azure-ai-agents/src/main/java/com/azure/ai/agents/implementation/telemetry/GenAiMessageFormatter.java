// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

/**
 * Formats messages for span attributes, respecting the content-recording privacy gate.
 *
 * <p>When content recording is OFF, messages include only structural information (roles and types)
 * without any user content. When ON, full message text is included. The {@code captureContent} flag is
 * passed in from the per-client {@link GenAiInstrumentation} rather than read from global state.</p>
 */
final class GenAiMessageFormatter {

    private GenAiMessageFormatter() {
        // utility class
    }

    /**
     * Formats the {@code gen_ai.system_instructions} attribute value (content-gated).
     */
    static String formatSystemInstructions(boolean captureContent, String instructions) {
        if (captureContent && instructions != null && !instructions.isEmpty()) {
            return "[{\"type\":\"text\",\"content\":" + jsonEscape(instructions) + "}]";
        }
        return "[{\"type\":\"text\"}]";
    }

    /**
     * Formats a user text input message for the {@code gen_ai.input.messages} attribute.
     */
    static String formatUserTextInput(boolean captureContent, String text) {
        if (captureContent) {
            return "[{\"role\":\"user\",\"parts\":[{\"type\":\"text\",\"content\":" + jsonEscape(text) + "}]}]";
        }
        return "[{\"role\":\"user\",\"parts\":[{\"type\":\"text\"}]}]";
    }

    /**
     * Formats a tool response input message for the {@code gen_ai.input.messages} attribute.
     */
    static String formatToolResponseInput(boolean captureContent, String toolCallId, String content) {
        if (captureContent) {
            return "[{\"role\":\"tool\",\"parts\":[{\"type\":\"tool_call_response\",\"id\":" + jsonEscape(toolCallId)
                + ",\"result\":" + formatResultValue(content) + "}]}]";
        }
        return "[{\"role\":\"tool\",\"parts\":[{\"type\":\"tool_call_response\",\"id\":" + jsonEscape(toolCallId)
            + "}]}]";
    }

    /**
     * Formats a text output message for the {@code gen_ai.output.messages} attribute.
     */
    static String formatTextOutput(boolean captureContent, String text, String finishReason) {
        String finishPart = finishReason != null ? ",\"finish_reason\":" + jsonEscape(finishReason) : "";
        if (captureContent) {
            return "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\",\"content\":" + jsonEscape(text) + "}]"
                + finishPart + "}]";
        }
        return "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\"}]" + finishPart + "}]";
    }

    /**
     * Formats a tool call output message for the {@code gen_ai.output.messages} attribute. The nested content object
     * (tool type and, when content recording is enabled, the extra content) is always emitted since the tool type is
     * always known.
     */
    static String formatToolCallOutput(boolean captureContent, String toolCallId, String toolType, String content) {
        StringBuilder sb = new StringBuilder("[{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\"");
        if (toolCallId != null) {
            sb.append(",\"id\":").append(jsonEscape(toolCallId));
        }
        sb.append(",\"content\":{\"type\":").append(jsonEscape(toolType));
        if (toolCallId != null) {
            sb.append(",\"id\":").append(jsonEscape(toolCallId));
        }
        if (captureContent && content != null) {
            sb.append(",\"content\":").append(jsonEscape(content));
        }
        sb.append("}}]}]");
        return sb.toString();
    }

    /**
     * Formats the content array for a {@code gen_ai.agent.workflow} event (content-gated).
     */
    static String formatWorkflowEventContent(boolean captureContent, String workflowDefinition) {
        if (captureContent && workflowDefinition != null && !workflowDefinition.isEmpty()) {
            return "[{\"type\":\"workflow\",\"content\":" + jsonEscape(workflowDefinition) + "}]";
        }
        return "[]";
    }

    /**
     * Formats a tool result value. If the value looks like a JSON object or array, emit it raw (unescaped).
     * Otherwise, emit it as a JSON string.
     */
    private static String formatResultValue(String value) {
        if (value == null) {
            return "null";
        }
        String trimmed = value.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return trimmed;
        }
        return jsonEscape(value);
    }

    /**
     * Escapes a string as a JSON string literal (including surrounding quotes). Returns {@code "null"} for a
     * {@code null} input.
     */
    static String jsonEscape(String text) {
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
