// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseFileSearchToolCall;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseFunctionWebSearch;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for tool call output formatting in {@link GenAiResponseTracing}.
 *
 * <p>Verifies that content recording gating works correctly for each tool call type:
 * <ul>
 *   <li>Content OFF: Only structural data (ids, types) is included. No arguments, code, queries, etc.</li>
 *   <li>Content ON: All relevant content is included.</li>
 * </ul>
 *
 * <p>This is critical for privacy compliance — content recording OFF must never leak sensitive data.</p>
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class GenAiToolCallFormattingTests {

    @BeforeEach
    void setUp() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    @AfterEach
    void tearDown() {
        GenAiTracingConfiguration.disableGenAiTracing();
    }

    // =========================================================================
    // Function Call - Content OFF
    // =========================================================================

    @Test
    void formatFunctionCall_contentOff_noArguments() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        ResponseFunctionToolCall funcCall = ResponseFunctionToolCall.builder()
            .callId("call_abc123")
            .name("getCurrentWeather")
            .arguments("{\"location\":\"Seattle, WA\"}")
            .id("fc_001")
            .status(ResponseFunctionToolCall.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatFunctionCall(funcCall);

        // Should include id (structural)
        assertTrue(result.contains("\"id\":\"call_abc123\""));
        assertTrue(result.contains("\"type\":\"tool_call\""));

        // MUST NOT include name or arguments when content recording is OFF
        assertFalse(result.contains("name"));
        assertFalse(result.contains("getCurrentWeather"));
        assertFalse(result.contains("arguments"));
        assertFalse(result.contains("Seattle"));
        assertFalse(result.contains("location"));
    }

    // =========================================================================
    // Function Call - Content ON
    // =========================================================================

    @Test
    void formatFunctionCall_contentOn_includesArguments() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseFunctionToolCall funcCall = ResponseFunctionToolCall.builder()
            .callId("call_abc123")
            .name("getCurrentWeather")
            .arguments("{\"location\":\"Seattle, WA\"}")
            .id("fc_001")
            .status(ResponseFunctionToolCall.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatFunctionCall(funcCall);

        assertTrue(result.contains("\"id\":\"call_abc123\""));
        assertTrue(result.contains("\"type\":\"tool_call\""));
        assertTrue(result.contains("\"name\":\"getCurrentWeather\""));
        assertTrue(result.contains("\"arguments\":"));
        assertTrue(result.contains("Seattle"));
    }

    // =========================================================================
    // File Search Call - Content OFF
    // =========================================================================

    @Test
    void formatFileSearchCall_contentOff_noQueriesOrResults() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        ResponseFileSearchToolCall fileSearch = ResponseFileSearchToolCall.builder()
            .id("fs_001")
            .queries(Arrays.asList("What is Azure?", "Cloud pricing"))
            .results(Arrays.asList(ResponseFileSearchToolCall.Result.builder()
                .fileId("file_123")
                .filename("azure_docs.pdf")
                .score(0.95f)
                .build()))
            .status(ResponseFileSearchToolCall.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatFileSearchCall(fileSearch);

        // Should include id (structural)
        assertTrue(result.contains("\"id\":\"fs_001\""));
        assertTrue(result.contains("\"type\":\"file_search_call\""));

        // MUST NOT include queries or results when content recording is OFF
        assertFalse(result.contains("queries"));
        assertFalse(result.contains("What is Azure"));
        assertFalse(result.contains("Cloud pricing"));
        assertFalse(result.contains("results"));
        assertFalse(result.contains("file_123"));
        assertFalse(result.contains("azure_docs.pdf"));
        assertFalse(result.contains("0.95"));
    }

    // =========================================================================
    // File Search Call - Content ON
    // =========================================================================

    @Test
    void formatFileSearchCall_contentOn_includesQueriesAndResults() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseFileSearchToolCall fileSearch = ResponseFileSearchToolCall.builder()
            .id("fs_001")
            .queries(Arrays.asList("What is Azure?", "Cloud pricing"))
            .results(Arrays.asList(ResponseFileSearchToolCall.Result.builder()
                .fileId("file_123")
                .filename("azure_docs.pdf")
                .score(0.95f)
                .build()))
            .status(ResponseFileSearchToolCall.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatFileSearchCall(fileSearch);

        assertTrue(result.contains("\"id\":\"fs_001\""));
        assertTrue(result.contains("\"queries\":["));
        assertTrue(result.contains("What is Azure?"));
        assertTrue(result.contains("Cloud pricing"));
        assertTrue(result.contains("\"results\":["));
        assertTrue(result.contains("file_123"));
        assertTrue(result.contains("azure_docs.pdf"));
        assertTrue(result.contains("0.95"));
    }

    // =========================================================================
    // Web Search Call - Content OFF (no content-gated fields)
    // =========================================================================

    @Test
    void formatWebSearchCall_contentOff_includesId() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        ResponseFunctionWebSearch webSearch = ResponseFunctionWebSearch.builder()
            .id("ws_001")
            .searchAction("Azure services")
            .status(ResponseFunctionWebSearch.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatWebSearchCall(webSearch);

        assertTrue(result.contains("\"id\":\"ws_001\""));
        assertTrue(result.contains("\"type\":\"web_search_call\""));
    }

    @Test
    void formatWebSearchCall_contentOn_includesId() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseFunctionWebSearch webSearch = ResponseFunctionWebSearch.builder()
            .id("ws_001")
            .searchAction("Azure services")
            .status(ResponseFunctionWebSearch.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatWebSearchCall(webSearch);

        assertTrue(result.contains("\"id\":\"ws_001\""));
        assertTrue(result.contains("\"type\":\"web_search_call\""));
    }

    // =========================================================================
    // Code Interpreter Call - Content OFF
    // =========================================================================

    @Test
    void formatCodeInterpreterCall_contentOff_noCode() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        ResponseCodeInterpreterToolCall codeInterpreter = ResponseCodeInterpreterToolCall.builder()
            .id("ci_001")
            .code("import os\nprint(os.environ['SECRET_KEY'])")
            .containerId("container_xyz")
            .outputs(Collections.emptyList())
            .status(ResponseCodeInterpreterToolCall.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatCodeInterpreterCall(codeInterpreter);

        // Should include id (structural)
        assertTrue(result.contains("\"id\":\"ci_001\""));
        assertTrue(result.contains("\"type\":\"code_interpreter_call\""));

        // MUST NOT include code content when content recording is OFF (code can contain secrets)
        assertFalse(result.contains("\"code\":"));
        assertFalse(result.contains("import os"));
        assertFalse(result.contains("SECRET_KEY"));
        assertFalse(result.contains("print"));
    }

    // =========================================================================
    // Code Interpreter Call - Content ON
    // =========================================================================

    @Test
    void formatCodeInterpreterCall_contentOn_includesCode() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseCodeInterpreterToolCall codeInterpreter = ResponseCodeInterpreterToolCall.builder()
            .id("ci_001")
            .code("print('Hello, World!')")
            .containerId("container_xyz")
            .outputs(Collections.emptyList())
            .status(ResponseCodeInterpreterToolCall.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatCodeInterpreterCall(codeInterpreter);

        assertTrue(result.contains("\"id\":\"ci_001\""));
        assertTrue(result.contains("\"code\":"));
        assertTrue(result.contains("Hello, World!"));
    }

    // =========================================================================
    // MCP Call - Content OFF
    // =========================================================================

    @Test
    void formatMcpCall_contentOff_noNameOrArgumentsOrServerLabel() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        ResponseOutputItem.McpCall mcpCall = ResponseOutputItem.McpCall.builder()
            .id("mcp_001")
            .name("get_readme")
            .serverLabel("api-specs")
            .arguments("{\"path\":\"README.md\"}")
            .build();

        String result = GenAiResponseTracing.formatMcpCall(mcpCall);

        // Should include id (structural)
        assertTrue(result.contains("\"id\":\"mcp_001\""));
        assertTrue(result.contains("\"type\":\"mcp_call\""));

        // MUST NOT include name, server_label, or arguments when content recording is OFF
        assertFalse(result.contains("\"name\""));
        assertFalse(result.contains("get_readme"));
        assertFalse(result.contains("server_label"));
        assertFalse(result.contains("api-specs"));
        assertFalse(result.contains("arguments"));
        assertFalse(result.contains("README.md"));
    }

    // =========================================================================
    // MCP Call - Content ON
    // =========================================================================

    @Test
    void formatMcpCall_contentOn_includesAllFields() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseOutputItem.McpCall mcpCall = ResponseOutputItem.McpCall.builder()
            .id("mcp_001")
            .name("get_readme")
            .serverLabel("api-specs")
            .arguments("{\"path\":\"README.md\"}")
            .build();

        String result = GenAiResponseTracing.formatMcpCall(mcpCall);

        assertTrue(result.contains("\"id\":\"mcp_001\""));
        assertTrue(result.contains("\"name\":\"get_readme\""));
        assertTrue(result.contains("\"server_label\":\"api-specs\""));
        assertTrue(result.contains("\"arguments\":"));
        assertTrue(result.contains("README.md"));
    }

    // =========================================================================
    // Image Generation Call - Content OFF (no content-gated fields)
    // =========================================================================

    @Test
    void formatImageGenerationCall_contentOff_includesId() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        ResponseOutputItem.ImageGenerationCall imageGen = ResponseOutputItem.ImageGenerationCall.builder()
            .id("ig_001")
            .result("base64encodedimagedata...")
            .status(ResponseOutputItem.ImageGenerationCall.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatImageGenerationCall(imageGen);

        // Should include id
        assertTrue(result.contains("\"id\":\"ig_001\""));
        assertTrue(result.contains("\"type\":\"image_generation_call\""));

        // Image data should NOT be present regardless of content recording
        assertFalse(result.contains("base64encodedimagedata"));
    }

    @Test
    void formatImageGenerationCall_contentOn_includesIdOnly() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseOutputItem.ImageGenerationCall imageGen = ResponseOutputItem.ImageGenerationCall.builder()
            .id("ig_001")
            .result("base64encodedimagedata...")
            .status(ResponseOutputItem.ImageGenerationCall.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatImageGenerationCall(imageGen);

        // Image generation never includes the image data in traces (too large)
        assertTrue(result.contains("\"id\":\"ig_001\""));
        assertFalse(result.contains("base64encodedimagedata"));
    }

    // =========================================================================
    // MCP Approval Request - Content OFF
    // =========================================================================

    @Test
    void formatMcpApprovalRequest_contentOff_noNameOrArgumentsOrServerLabel() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        ResponseOutputItem.McpApprovalRequest approval = ResponseOutputItem.McpApprovalRequest.builder()
            .id("apr_001")
            .name("execute_command")
            .serverLabel("my-server")
            .arguments("{\"command\":\"rm -rf /\"}")
            .build();

        String result = GenAiResponseTracing.formatMcpApprovalRequest(approval);

        // Should include id (structural)
        assertTrue(result.contains("\"id\":\"apr_001\""));
        assertTrue(result.contains("\"type\":\"mcp_approval_request\""));

        // MUST NOT include name, server_label, or arguments (sensitive!) when OFF
        assertFalse(result.contains("\"name\""));
        assertFalse(result.contains("execute_command"));
        assertFalse(result.contains("server_label"));
        assertFalse(result.contains("my-server"));
        assertFalse(result.contains("arguments"));
        assertFalse(result.contains("rm -rf"));
    }

    // =========================================================================
    // MCP Approval Request - Content ON
    // =========================================================================

    @Test
    void formatMcpApprovalRequest_contentOn_includesAllFields() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseOutputItem.McpApprovalRequest approval = ResponseOutputItem.McpApprovalRequest.builder()
            .id("apr_001")
            .name("execute_command")
            .serverLabel("my-server")
            .arguments("{\"command\":\"ls\"}")
            .build();

        String result = GenAiResponseTracing.formatMcpApprovalRequest(approval);

        assertTrue(result.contains("\"id\":\"apr_001\""));
        assertTrue(result.contains("\"name\":\"execute_command\""));
        assertTrue(result.contains("\"server_label\":\"my-server\""));
        assertTrue(result.contains("\"arguments\":"));
    }

    // =========================================================================
    // Output Message - Content OFF
    // =========================================================================

    @Test
    void formatOutputMessage_contentOff_noTextContent() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .id("msg_001")
            .content(Collections.singletonList(ResponseOutputMessage.Content.ofOutputText(ResponseOutputText.builder()
                .text("The secret password is hunter2")
                .annotations(Collections.emptyList())
                .build())))
            .status(ResponseOutputMessage.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatOutputMessage(message);

        // Should include type (structural)
        assertTrue(result.contains("\"type\":\"text\""));
        assertTrue(result.contains("\"role\":\"assistant\""));

        // MUST NOT include the actual text content when recording is OFF
        assertFalse(result.contains("secret password"));
        assertFalse(result.contains("hunter2"));
        assertFalse(result.contains("\"content\""));
    }

    // =========================================================================
    // Output Message - Content ON
    // =========================================================================

    @Test
    void formatOutputMessage_contentOn_includesTextContent() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .id("msg_001")
            .content(Collections.singletonList(ResponseOutputMessage.Content.ofOutputText(ResponseOutputText.builder()
                .text("The capital of France is Paris.")
                .annotations(Collections.emptyList())
                .build())))
            .status(ResponseOutputMessage.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatOutputMessage(message);

        assertTrue(result.contains("\"type\":\"text\""));
        assertTrue(result.contains("\"content\":\"The capital of France is Paris.\""));
    }

    // =========================================================================
    // Output Message - Content ON with special characters
    // =========================================================================

    @Test
    void formatOutputMessage_contentOn_escapesSpecialCharacters() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(true));

        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .id("msg_002")
            .content(Collections.singletonList(ResponseOutputMessage.Content.ofOutputText(ResponseOutputText.builder()
                .text("He said \"hello\"\nNew line")
                .annotations(Collections.emptyList())
                .build())))
            .status(ResponseOutputMessage.Status.COMPLETED)
            .build();

        String result = GenAiResponseTracing.formatOutputMessage(message);

        // Should be properly JSON-escaped
        assertTrue(result.contains("\\\"hello\\\""));
        assertTrue(result.contains("\\n"));
    }

    // =========================================================================
    // Comprehensive content OFF verification
    // =========================================================================

    @Test
    void allFormats_contentOff_neverContainSensitiveKeywords() {
        GenAiTracingConfiguration
            .enableGenAiTracing(new GenAiTracingOptions().setExperimental(true).setContentRecording(false));

        // Function call
        String funcResult = GenAiResponseTracing.formatFunctionCall(ResponseFunctionToolCall.builder()
            .callId("call_1")
            .name("get_secret")
            .arguments("{\"password\":\"super_secret_123\"}")
            .id("fc_1")
            .status(ResponseFunctionToolCall.Status.COMPLETED)
            .build());
        assertFalse(funcResult.contains("super_secret_123"), "Function call leaked arguments");
        assertFalse(funcResult.contains("password"), "Function call leaked argument key");
        assertFalse(funcResult.contains("get_secret"), "Function call leaked function name");

        // Code interpreter
        String codeResult = GenAiResponseTracing.formatCodeInterpreterCall(ResponseCodeInterpreterToolCall.builder()
            .id("ci_1")
            .code("api_key = 'sk-SENSITIVE_API_KEY_VALUE'")
            .containerId("c1")
            .outputs(Collections.emptyList())
            .status(ResponseCodeInterpreterToolCall.Status.COMPLETED)
            .build());
        assertFalse(codeResult.contains("SENSITIVE_API_KEY_VALUE"), "Code interpreter leaked code");
        assertFalse(codeResult.contains("api_key"), "Code interpreter leaked variable name");

        // MCP call
        String mcpResult = GenAiResponseTracing.formatMcpCall(ResponseOutputItem.McpCall.builder()
            .id("mcp_1")
            .name("database_query")
            .serverLabel("production-db")
            .arguments("{\"query\":\"SELECT * FROM users WHERE password='admin'\"}")
            .build());
        assertFalse(mcpResult.contains("database_query"), "MCP call leaked tool name");
        assertFalse(mcpResult.contains("production-db"), "MCP call leaked server label");
        assertFalse(mcpResult.contains("SELECT"), "MCP call leaked query");
        assertFalse(mcpResult.contains("admin"), "MCP call leaked sensitive data");

        // MCP approval
        String approvalResult
            = GenAiResponseTracing.formatMcpApprovalRequest(ResponseOutputItem.McpApprovalRequest.builder()
                .id("apr_1")
                .name("delete_all_data")
                .serverLabel("critical-server")
                .arguments("{\"confirm\":true}")
                .build());
        assertFalse(approvalResult.contains("delete_all_data"), "MCP approval leaked name");
        assertFalse(approvalResult.contains("critical-server"), "MCP approval leaked server");
        assertFalse(approvalResult.contains("confirm"), "MCP approval leaked arguments");

        // Output message
        String msgResult = GenAiResponseTracing.formatOutputMessage(ResponseOutputMessage.builder()
            .id("msg_1")
            .content(Collections.singletonList(ResponseOutputMessage.Content.ofOutputText(ResponseOutputText.builder()
                .text("Your credit card number is 4111-1111-1111-1111")
                .annotations(Collections.emptyList())
                .build())))
            .status(ResponseOutputMessage.Status.COMPLETED)
            .build());
        assertFalse(msgResult.contains("4111"), "Output message leaked credit card");
        assertFalse(msgResult.contains("credit card"), "Output message leaked content");
    }
}
