// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GenAiMessageFormatter}, focused on the content-recording privacy gate and the corrected
 * tool-call output formatting.
 */
public final class GenAiMessageFormatterTest {

    @Test
    public void serializationRoundTripsSpecialCharacters() throws IOException {
        String content = "quote=\" slash=\\ newline=\n control=\u0001";

        Map<String, Object> message = firstMessage(GenAiMessageFormatter.formatUserTextInput(true, content));
        Map<String, Object> part = firstPart(message);

        assertEquals(content, part.get("content"));
    }

    @Test
    public void userTextInputRespectsContentGate() {
        String on = GenAiMessageFormatter.formatUserTextInput(true, "hello");
        assertTrue(on.contains("\"content\":\"hello\""));

        String off = GenAiMessageFormatter.formatUserTextInput(false, "hello");
        assertFalse(off.contains("hello"));
        assertTrue(off.contains("\"type\":\"text\""));
    }

    @Test
    public void systemInstructionsRespectContentGate() {
        assertTrue(GenAiMessageFormatter.formatSystemInstructions(true, "be nice").contains("\"content\":\"be nice\""));
        assertEquals("[{\"type\":\"text\"}]", GenAiMessageFormatter.formatSystemInstructions(false, "be nice"));
    }

    @Test
    public void toolResponseInputRespectsContentGate() {
        String on = GenAiMessageFormatter.formatToolResponseInput(true, "call-1", "result-text");
        assertTrue(on.contains("\"result\":\"result-text\""));

        String off = GenAiMessageFormatter.formatToolResponseInput(false, "call-1", "result-text");
        assertFalse(off.contains("result-text"));
        assertTrue(off.contains("\"id\":\"call-1\""));
    }

    @Test
    public void toolResponseSerializesStructuredJsonResult() throws IOException {
        Map<String, Object> message
            = firstMessage(GenAiMessageFormatter.formatToolResponseInput(true, "call-1", "{\"value\":[1,true]}"));
        Object result = firstPart(message).get("result");

        assertTrue(result instanceof Map);
        assertEquals(1, ((List<?>) ((Map<?, ?>) result).get("value")).get(0));
    }

    @Test
    public void toolResponsePreservesMalformedJsonAsString() throws IOException {
        String malformedResult = "{\"unterminated\":}";
        Map<String, Object> message
            = firstMessage(GenAiMessageFormatter.formatToolResponseInput(true, "call-1", malformedResult));

        assertEquals(malformedResult, firstPart(message).get("result"));
    }

    @Test
    public void toolResponsePreservesJsonWithTrailingContentAsString() throws IOException {
        String malformedResult = "{\"first\":1}{\"second\":2}";
        Map<String, Object> message
            = firstMessage(GenAiMessageFormatter.formatToolResponseInput(true, "call-1", malformedResult));

        assertEquals(malformedResult, firstPart(message).get("result"));
    }

    @Test
    public void toolResponsePreservesLargeJsonNumbers() throws IOException {
        String result = "{\"integer\":9223372036854775808,\"decimal\":1e400}";
        Map<String, Object> message
            = firstMessage(GenAiMessageFormatter.formatToolResponseInput(true, "call-1", result));
        Map<?, ?> structuredResult = (Map<?, ?>) firstPart(message).get("result");

        assertTrue(structuredResult.get("integer") instanceof Number);
        assertTrue(structuredResult.get("decimal") instanceof Number);
    }

    @Test
    public void deeplyNestedToolResponseFallsBackToString() throws IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            result.append('[');
        }
        for (int i = 0; i < 1001; i++) {
            result.append(']');
        }

        Map<String, Object> message
            = firstMessage(GenAiMessageFormatter.formatToolResponseInput(true, "call-1", result.toString()));

        assertEquals(result.toString(), firstPart(message).get("result"));
    }

    @Test
    public void toolCallOutputAlwaysIncludesTypeAndGatesContent() {
        // Corrected behaviour: the nested content object (with the tool type) is always emitted, even when the
        // extra content argument is null; the extra content value is only included when content recording is on.
        String withContent = GenAiMessageFormatter.formatToolCallOutput(true, "call-1", "function_call", "extra");
        assertTrue(withContent.contains("\"type\":\"function_call\""));
        assertTrue(withContent.contains("\"content\":\"extra\""));

        String gatedOff = GenAiMessageFormatter.formatToolCallOutput(false, "call-1", "function_call", "extra");
        assertTrue(gatedOff.contains("\"type\":\"function_call\""));
        assertFalse(gatedOff.contains("\"content\":\"extra\""));

        String nullContent = GenAiMessageFormatter.formatToolCallOutput(true, "call-1", "code_interpreter_call", null);
        assertTrue(nullContent.contains("\"type\":\"code_interpreter_call\""));
        assertTrue(nullContent.contains("\"id\":\"call-1\""));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstMessage(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return (Map<String, Object>) ((List<?>) reader.readUntyped()).get(0);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstPart(Map<String, Object> message) {
        return (Map<String, Object>) ((List<?>) message.get("parts")).get(0);
    }
}
