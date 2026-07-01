// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.openai.core.JsonValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for workflow tracing in {@link GenAiAgentTracing} and {@link GenAiResponseTracing}.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>Workflow create_agent tracing executes operations correctly</li>
 *   <li>Workflow event content is gated by content recording setting</li>
 *   <li>Workflow action event content is gated by content recording setting</li>
 *   <li>Content recording OFF does not leak sensitive data</li>
 * </ul>
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class GenAiWorkflowTracingTests {

    private static final URI TEST_ENDPOINT
        = URI.create("https://test-resource.services.ai.azure.com/api/projects/test");
    private static final String SAMPLE_WORKFLOW_YAML = "kind: workflow\ntrigger:\n  kind: OnConversationStart\n"
        + "  id: my_workflow\n  actions:\n    - kind: InvokeAzureAgent\n      id: student_agent\n";

    @BeforeEach
    void setUp() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    @AfterEach
    void tearDown() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    // =========================================================================
    // traceCreateAgent with workflow type
    // =========================================================================

    @Test
    void traceCreateAgent_workflowType_tracingDisabled_operationStillExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);

        String result = GenAiAgentTracing.traceCreateAgent("WorkflowAgent", TEST_ENDPOINT, "WorkflowAgent:1", "1",
            "workflow", null, null, null, null, SAMPLE_WORKFLOW_YAML, () -> {
                called.set(true);
                return "success";
            });

        assertTrue(called.get());
        assertEquals("success", result);
    }

    @Test
    void traceCreateAgent_workflowType_tracingEnabled_operationExecutes() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));
        AtomicBoolean called = new AtomicBoolean(false);

        String result = GenAiAgentTracing.traceCreateAgent("WorkflowAgent", TEST_ENDPOINT, "WorkflowAgent:1", "1",
            "workflow", null, null, null, null, SAMPLE_WORKFLOW_YAML, () -> {
                called.set(true);
                return "success";
            });

        assertTrue(called.get());
        assertEquals("success", result);
    }

    @Test
    void traceCreateAgent_workflowType_tracingEnabled_contentOff_noException() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        assertDoesNotThrow(() -> GenAiAgentTracing.traceCreateAgent("WorkflowAgent", TEST_ENDPOINT, "WorkflowAgent:1",
            "1", "workflow", null, null, null, null, SAMPLE_WORKFLOW_YAML, () -> "ok"));
    }

    @Test
    void traceCreateAgent_workflowType_tracingEnabled_contentOn_noException() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        assertDoesNotThrow(() -> GenAiAgentTracing.traceCreateAgent("WorkflowAgent", TEST_ENDPOINT, "WorkflowAgent:1",
            "1", "workflow", null, null, null, null, SAMPLE_WORKFLOW_YAML, () -> "ok"));
    }

    @Test
    void traceCreateAgent_workflowType_nullDefinition_noException() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        assertDoesNotThrow(() -> GenAiAgentTracing.traceCreateAgent("WorkflowAgent", TEST_ENDPOINT, "WorkflowAgent:1",
            "1", "workflow", null, null, null, null, null, () -> "ok"));
    }

    @Test
    void traceCreateAgent_workflowType_exceptionPropagates() {
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));

        assertThrows(RuntimeException.class, () -> GenAiAgentTracing.traceCreateAgent("WorkflowAgent", TEST_ENDPOINT,
            "WorkflowAgent:1", "1", "workflow", null, null, null, null, SAMPLE_WORKFLOW_YAML, () -> {
                throw new RuntimeException("API error");
            }));
    }

    @Test
    void traceCreateAgent_nonWorkflowType_withDefinition_noWorkflowEvent() {
        // If agentType is not "workflow", the workflow event should not be emitted even if definition is provided
        GenAiTracingConfiguration.enableGenAiTracing(new GenAiTracingOptions().setExperimental(true));

        assertDoesNotThrow(() -> GenAiAgentTracing.traceCreateAgent("PromptAgent", TEST_ENDPOINT, "PromptAgent:1", "1",
            "prompt", "gpt-4.1", null, null, null, SAMPLE_WORKFLOW_YAML, () -> "ok"));
    }

    // =========================================================================
    // formatWorkflowEventContent - content recording OFF
    // =========================================================================

    @Test
    void formatWorkflowEventContent_contentOff_returnsEmptyArray() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        String result = GenAiAgentTracing.formatWorkflowEventContent(SAMPLE_WORKFLOW_YAML);

        assertEquals("[]", result);
    }

    @Test
    void formatWorkflowEventContent_contentOff_noWorkflowYamlPresent() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        String result = GenAiAgentTracing.formatWorkflowEventContent(SAMPLE_WORKFLOW_YAML);

        // Verify absolutely no trace of the workflow YAML content when recording is off
        assertFalse(result.contains("OnConversationStart"));
        assertFalse(result.contains("InvokeAzureAgent"));
        assertFalse(result.contains("student_agent"));
        assertFalse(result.contains("workflow"));
        assertEquals("[]", result);
    }

    @Test
    void formatWorkflowEventContent_contentOff_nullDefinition_returnsEmptyArray() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        assertEquals("[]", GenAiAgentTracing.formatWorkflowEventContent(null));
    }

    @Test
    void formatWorkflowEventContent_contentOff_emptyDefinition_returnsEmptyArray() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        assertEquals("[]", GenAiAgentTracing.formatWorkflowEventContent(""));
    }

    // =========================================================================
    // formatWorkflowEventContent - content recording ON
    // =========================================================================

    @Test
    void formatWorkflowEventContent_contentOn_includesWorkflowYaml() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        String result = GenAiAgentTracing.formatWorkflowEventContent(SAMPLE_WORKFLOW_YAML);

        assertTrue(result.contains("\"type\":\"workflow\""));
        assertTrue(result.contains("\"content\":"));
        assertTrue(result.contains("OnConversationStart"));
        assertTrue(result.contains("InvokeAzureAgent"));
    }

    @Test
    void formatWorkflowEventContent_contentOn_nullDefinition_returnsEmptyArray() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        assertEquals("[]", GenAiAgentTracing.formatWorkflowEventContent(null));
    }

    @Test
    void formatWorkflowEventContent_contentOn_emptyDefinition_returnsEmptyArray() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        assertEquals("[]", GenAiAgentTracing.formatWorkflowEventContent(""));
    }

    @Test
    void formatWorkflowEventContent_contentOn_escapesSpecialCharacters() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        String yamlWithSpecialChars = "condition: '=!IsBlank(Find(\"[COMPLETE]\", text))'";
        String result = GenAiAgentTracing.formatWorkflowEventContent(yamlWithSpecialChars);

        // Should be properly JSON-escaped
        assertTrue(result.contains("\\\"[COMPLETE]\\\""));
        assertTrue(result.contains("content"));
    }

    // =========================================================================
    // formatWorkflowActionEventContent - content recording OFF
    // =========================================================================

    @Test
    void formatWorkflowActionEventContent_contentOff_onlyIncludesStatus() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        Map<String, JsonValue> item = new HashMap<>();
        item.put("status", JsonValue.from("completed"));
        item.put("action_id", JsonValue.from("student_agent"));
        item.put("previous_action_id", JsonValue.from("set_variable_input_task"));

        String result = GenAiResponseTracing.formatWorkflowActionEventContent(item);

        // Status SHOULD be present (always traced)
        assertTrue(result.contains("\"status\":\"completed\""));

        // action_id and previous_action_id MUST NOT be present when content recording is off
        assertFalse(result.contains("action_id"));
        assertFalse(result.contains("student_agent"));
        assertFalse(result.contains("previous_action_id"));
        assertFalse(result.contains("set_variable_input_task"));
    }

    @Test
    void formatWorkflowActionEventContent_contentOff_noSensitiveDataLeaked() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        Map<String, JsonValue> item = new HashMap<>();
        item.put("status", JsonValue.from("in_progress"));
        item.put("action_id", JsonValue.from("sensitive_action_name"));
        item.put("previous_action_id", JsonValue.from("another_sensitive_name"));

        String result = GenAiResponseTracing.formatWorkflowActionEventContent(item);

        // Verify no sensitive content leaked
        assertFalse(result.contains("sensitive_action_name"));
        assertFalse(result.contains("another_sensitive_name"));

        // Only status and structural elements should be present
        assertTrue(result.contains("\"status\":\"in_progress\""));
        assertTrue(result.contains("workflow_action"));
        assertTrue(result.contains("workflow"));
    }

    @Test
    void formatWorkflowActionEventContent_contentOff_missingStatus_emptyContent() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        Map<String, JsonValue> item = new HashMap<>();
        item.put("action_id", JsonValue.from("some_action"));

        String result = GenAiResponseTracing.formatWorkflowActionEventContent(item);

        // Should not contain any content fields
        assertFalse(result.contains("some_action"));
        assertFalse(result.contains("action_id"));
    }

    // =========================================================================
    // formatWorkflowActionEventContent - content recording ON
    // =========================================================================

    @Test
    void formatWorkflowActionEventContent_contentOn_includesAllFields() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        Map<String, JsonValue> item = new HashMap<>();
        item.put("status", JsonValue.from("completed"));
        item.put("action_id", JsonValue.from("teacher_agent"));
        item.put("previous_action_id", JsonValue.from("student_agent"));

        String result = GenAiResponseTracing.formatWorkflowActionEventContent(item);

        assertTrue(result.contains("\"status\":\"completed\""));
        assertTrue(result.contains("\"action_id\":\"teacher_agent\""));
        assertTrue(result.contains("\"previous_action_id\":\"student_agent\""));
    }

    @Test
    void formatWorkflowActionEventContent_contentOn_missingOptionalFields() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        Map<String, JsonValue> item = new HashMap<>();
        item.put("status", JsonValue.from("in_progress"));
        // No action_id or previous_action_id

        String result = GenAiResponseTracing.formatWorkflowActionEventContent(item);

        assertTrue(result.contains("\"status\":\"in_progress\""));
        assertFalse(result.contains("action_id"));
        assertFalse(result.contains("previous_action_id"));
    }

    @Test
    void formatWorkflowActionEventContent_contentOn_hasCorrectStructure() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        Map<String, JsonValue> item = new HashMap<>();
        item.put("status", JsonValue.from("completed"));
        item.put("action_id", JsonValue.from("my_action"));

        String result = GenAiResponseTracing.formatWorkflowActionEventContent(item);

        // Verify the overall structure
        assertTrue(result.startsWith("[{\"role\":\"workflow\",\"parts\":[{\"type\":\"workflow_action\",\"content\":"));
        assertTrue(result.endsWith("}]}]"));
    }
}
