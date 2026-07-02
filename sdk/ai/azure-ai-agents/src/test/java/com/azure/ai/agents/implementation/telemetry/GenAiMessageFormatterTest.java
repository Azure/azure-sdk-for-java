// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link GenAiMessageFormatter}, focused on the content-recording privacy gate and the corrected
 * tool-call output formatting.
 */
public final class GenAiMessageFormatterTest {

    @Test
    public void jsonEscapeHandlesNullAndSpecialCharacters() {
        assertEquals("null", GenAiMessageFormatter.jsonEscape(null));
        assertEquals("\"a\\\"b\\\\c\\nd\"", GenAiMessageFormatter.jsonEscape("a\"b\\c\nd"));
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

    @Test
    public void workflowEventContentRespectsContentGate() {
        assertTrue(GenAiMessageFormatter.formatWorkflowEventContent(true, "wf").contains("\"content\":\"wf\""));
        assertEquals("[]", GenAiMessageFormatter.formatWorkflowEventContent(false, "wf"));
        assertEquals("[]", GenAiMessageFormatter.formatWorkflowEventContent(true, null));
    }
}
