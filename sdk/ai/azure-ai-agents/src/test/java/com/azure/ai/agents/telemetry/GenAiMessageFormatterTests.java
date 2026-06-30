// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link GenAiMessageFormatter} verifying content privacy gating.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class GenAiMessageFormatterTests {

    @BeforeEach
    void setUp() {
        // Start with tracing enabled, content recording OFF
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setContentRecording(false));
    }

    @AfterEach
    void tearDown() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    // --- Content recording OFF tests ---

    @Test
    void userTextInput_contentOff_noContent() {
        String result = GenAiMessageFormatter.formatUserTextInput("What is the capital of France?");
        assertEquals("[{\"role\":\"user\",\"parts\":[{\"type\":\"text\"}]}]", result);
    }

    @Test
    void toolResponseInput_contentOff_noContent() {
        String result = GenAiMessageFormatter.formatToolResponseInput("call_123", "Paris is the capital");
        assertEquals("[{\"role\":\"tool\",\"parts\":[{\"type\":\"tool_call_response\",\"id\":\"call_123\"}]}]", result);
    }

    @Test
    void textOutput_contentOff_noContent() {
        String result = GenAiMessageFormatter.formatTextOutput("The capital of France is Paris.", "completed");
        assertEquals("[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\"}],\"finish_reason\":\"completed\"}]",
            result);
    }

    // --- Content recording ON tests ---

    @Test
    void userTextInput_contentOn_hasContent() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setContentRecording(true));

        String result = GenAiMessageFormatter.formatUserTextInput("What is the capital of France?");
        assertEquals(
            "[{\"role\":\"user\",\"parts\":[{\"type\":\"text\",\"content\":\"What is the capital of France?\"}]}]",
            result);
    }

    @Test
    void toolResponseInput_contentOn_hasContent() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setContentRecording(true));

        String result = GenAiMessageFormatter.formatToolResponseInput("call_123", "22 degrees");
        assertEquals(
            "[{\"role\":\"tool\",\"parts\":[{\"type\":\"tool_call_response\",\"id\":\"call_123\",\"content\":\"22 degrees\"}]}]",
            result);
    }

    @Test
    void textOutput_contentOn_hasContent() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setContentRecording(true));

        String result = GenAiMessageFormatter.formatTextOutput("Paris.", "completed");
        assertEquals(
            "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\",\"content\":\"Paris.\"}],\"finish_reason\":\"completed\"}]",
            result);
    }

    // --- Special characters ---

    @Test
    void userTextInput_contentOn_escapesQuotes() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setContentRecording(true));

        String result = GenAiMessageFormatter.formatUserTextInput("Say \"hello\"");
        assertEquals("[{\"role\":\"user\",\"parts\":[{\"type\":\"text\",\"content\":\"Say \\\"hello\\\"\"}]}]", result);
    }

    @Test
    void userTextInput_contentOn_escapesNewlines() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setContentRecording(true));

        String result = GenAiMessageFormatter.formatUserTextInput("Line1\nLine2");
        assertEquals("[{\"role\":\"user\",\"parts\":[{\"type\":\"text\",\"content\":\"Line1\\nLine2\"}]}]", result);
    }

    // --- Null safety ---

    @Test
    void textOutput_nullFinishReason() {
        String result = GenAiMessageFormatter.formatTextOutput("text", null);
        assertEquals("[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\"}]}]", result);
    }

    @Test
    void formatRaw_passthrough() {
        String json = "[{\"role\":\"user\"}]";
        assertEquals(json, GenAiMessageFormatter.formatRaw(json));
    }
}
