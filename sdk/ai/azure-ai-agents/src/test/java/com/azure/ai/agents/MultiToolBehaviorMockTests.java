// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AutoCodeInterpreterToolParameter;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.FileSearchTool;
import com.azure.ai.agents.models.FunctionTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.Tool;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiToolBehaviorMockTests {
    private static final String ENDPOINT = "https://localhost:8080/api/projects/project";
    private static final String AGENT_NAME = "multitool-agent";

    @Test
    public void codeInterpreterAndFunctionPreserveOrderAndContinuation() {
        String outputs = codeInterpreterCall("code-1") + ","
            + OpenAIResponseFixtures.functionCall("function-1", "save-call", "save_result", "{\"result\":\"83521\"}");
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, OpenAIResponseFixtures.promptAgentVersion(AGENT_NAME, "1"))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-code-function", outputs))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-code-function-final",
                    OpenAIResponseFixtures.message("message-final", "Result saved.")));
        AgentsClientBuilder builder = createBuilder(httpClient);

        builder.buildAgentsClient()
            .createAgentVersion(AGENT_NAME,
                definition(new CodeInterpreterTool().setContainer(new AutoCodeInterpreterToolParameter()),
                    functionTool("save_result")));
        ResponsesClient responsesClient = builder.buildResponsesClient();
        Response initial = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder().input("Calculate and save."));
        ResponseInputItem functionOutput
            = ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
                .callId(initial.output().get(1).asFunctionCall().callId())
                .output("{\"saved\":true}")
                .build());
        Response completed = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder()
                .inputOfResponse(Collections.singletonList(functionOutput))
                .previousResponseId(initial.id()));

        assertToolOrder(httpClient.getRequest(0), "code_interpreter", "function");
        assertTrue(initial.output().get(0).isCodeInterpreterCall());
        assertEquals("container-1", initial.output().get(0).asCodeInterpreterCall().containerId());
        assertEquals("save_result", initial.output().get(1).asFunctionCall().name());
        assertEquals("Result saved.", responseText(completed));
        String continuation = body(httpClient.getRequest(2));
        assertTrue(continuation.contains("\"previous_response_id\":\"resp-code-function\""));
        assertTrue(continuation.contains("\"call_id\":\"save-call\""));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void fileSearchAndCodeInterpreterParseBothToolCalls() {
        String outputs = fileSearchCall("search-1") + "," + codeInterpreterCall("code-1") + ","
            + OpenAIResponseFixtures.message("message-analysis", "Analysis complete.");
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, OpenAIResponseFixtures.promptAgentVersion(AGENT_NAME, "1"))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-search-code", outputs));
        AgentsClientBuilder builder = createBuilder(httpClient);

        builder.buildAgentsClient()
            .createAgentVersion(AGENT_NAME, definition(new FileSearchTool(Collections.singletonList("vector-store-1")),
                new CodeInterpreterTool().setContainer(new AutoCodeInterpreterToolParameter())));
        Response response = builder.buildResponsesClient()
            .createAzureResponse(agentOptions(), ResponseCreateParams.builder().input("Find and analyze."));

        assertToolOrder(httpClient.getRequest(0), "file_search", "code_interpreter");
        assertTrue(body(httpClient.getRequest(0)).contains("\"vector_store_ids\":[\"vector-store-1\"]"));
        assertTrue(response.output().get(0).isFileSearchCall());
        assertEquals("sales data", response.output().get(0).asFileSearchCall().queries().get(0));
        assertTrue(response.output().get(1).isCodeInterpreterCall());
        assertEquals("Analysis complete.", responseText(response, 2));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void fileSearchAndFunctionShareConversationState() {
        String firstOutputs
            = fileSearchCall("search-1") + "," + OpenAIResponseFixtures.message("message-search", "Q1 revenue found.");
        String functionCall = OpenAIResponseFixtures.functionCall("function-save", "save-report-call", "save_report",
            "{\"title\":\"Q1 report\",\"summary\":\"Revenue summary\"}");
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, OpenAIResponseFixtures.promptAgentVersion(AGENT_NAME, "1"))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-search", firstOutputs))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-save", functionCall))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-saved",
                    OpenAIResponseFixtures.message("message-saved", "Report saved.")));
        AgentsClientBuilder builder = createBuilder(httpClient);

        builder.buildAgentsClient()
            .createAgentVersion(AGENT_NAME, definition(new FileSearchTool(Collections.singletonList("vector-store-1")),
                functionTool("save_report")));
        ResponsesClient responsesClient = builder.buildResponsesClient();
        Response search = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder().conversation("conversation-1").input("Find Q1 revenue."));
        Response save = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder().conversation("conversation-1").input("Save the report."));
        ResponseInputItem output = ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
            .callId(save.output().get(0).asFunctionCall().callId())
            .output("{\"status\":\"saved\"}")
            .build());
        Response completed = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder()
                .conversation("conversation-1")
                .inputOfResponse(Collections.singletonList(output))
                .previousResponseId(save.id()));

        assertToolOrder(httpClient.getRequest(0), "file_search", "function");
        assertTrue(search.output().get(0).isFileSearchCall());
        assertEquals("save_report", save.output().get(0).asFunctionCall().name());
        assertEquals("Report saved.", responseText(completed));
        assertTrue(body(httpClient.getRequest(1)).contains("\"conversation\":\"conversation-1\""));
        assertTrue(body(httpClient.getRequest(2)).contains("\"conversation\":\"conversation-1\""));
        String continuation = body(httpClient.getRequest(3));
        assertTrue(continuation.contains("\"conversation\":\"conversation-1\""));
        assertTrue(continuation.contains("\"previous_response_id\":\"resp-save\""));
        assertTrue(continuation.contains("\"call_id\":\"save-report-call\""));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void allThreeToolsRoundTripInOneResponse() {
        String outputs = fileSearchCall("search-1") + "," + codeInterpreterCall("code-1") + "," + OpenAIResponseFixtures
            .functionCall("function-save", "analysis-call", "save_analysis", "{\"total\":520,\"average\":52}");
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, OpenAIResponseFixtures.promptAgentVersion(AGENT_NAME, "1"))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-all-tools", outputs))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-all-tools-final",
                    OpenAIResponseFixtures.message("message-final", "Analysis saved.")));
        AgentsClientBuilder builder = createBuilder(httpClient);

        builder.buildAgentsClient()
            .createAgentVersion(AGENT_NAME,
                definition(new FileSearchTool(Collections.singletonList("vector-store-1")),
                    new CodeInterpreterTool().setContainer(new AutoCodeInterpreterToolParameter()),
                    functionTool("save_analysis")));
        ResponsesClient responsesClient = builder.buildResponsesClient();
        Response initial = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder().input("Find, analyze, and save."));
        ResponseInputItem output = ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
            .callId(initial.output().get(2).asFunctionCall().callId())
            .output("{\"saved\":true}")
            .build());
        Response completed = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder()
                .inputOfResponse(Collections.singletonList(output))
                .previousResponseId(initial.id()));

        assertToolOrder(httpClient.getRequest(0), "file_search", "code_interpreter", "function");
        String agentBody = body(httpClient.getRequest(0));
        assertTrue(agentBody.contains("\"strict\":true"));
        assertTrue(agentBody.contains("\"additionalProperties\":false"));
        assertTrue(initial.output().get(0).isFileSearchCall());
        assertTrue(initial.output().get(1).isCodeInterpreterCall());
        assertTrue(initial.output().get(2).isFunctionCall());
        assertEquals("Analysis saved.", responseText(completed));
        assertTrue(body(httpClient.getRequest(2)).contains("\"previous_response_id\":\"resp-all-tools\""));
        httpClient.assertResponsesConsumed();
    }

    private static AgentsClientBuilder createBuilder(DeterministicHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    private static AzureCreateResponseOptions agentOptions() {
        return new AzureCreateResponseOptions().setAgentReference(new AgentReference(AGENT_NAME).setVersion("1"));
    }

    private static CreateAgentVersionInput definition(Tool... tools) {
        return new CreateAgentVersionInput(new PromptAgentDefinition("gpt-4o").setTools(Arrays.asList(tools)));
    }

    private static FunctionTool functionTool(String name) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("value", Collections.singletonMap("type", "string"));
        Map<String, BinaryData> parameters = new LinkedHashMap<>();
        parameters.put("type", BinaryData.fromObject("object"));
        parameters.put("properties", BinaryData.fromObject(properties));
        parameters.put("required", BinaryData.fromObject(Collections.singletonList("value")));
        parameters.put("additionalProperties", BinaryData.fromObject(false));
        return new FunctionTool(name, parameters, true);
    }

    private static String fileSearchCall(String id) {
        return "{\"id\":\"" + id + "\",\"type\":\"file_search_call\","
            + "\"queries\":[\"sales data\"],\"status\":\"completed\",\"results\":[]}";
    }

    private static String codeInterpreterCall(String id) {
        return "{\"id\":\"" + id + "\",\"type\":\"code_interpreter_call\","
            + "\"container_id\":\"container-1\",\"code\":\"print(52)\"," + "\"outputs\":[],\"status\":\"completed\"}";
    }

    private static void assertToolOrder(HttpRequest request, String... toolTypes) {
        String requestBody = body(request);
        int previous = -1;
        for (String toolType : toolTypes) {
            int current = requestBody.indexOf("\"type\":\"" + toolType + "\"");
            assertTrue(current > previous, "Expected tool order to contain " + toolType);
            previous = current;
        }
    }

    private static String responseText(Response response) {
        return responseText(response, 0);
    }

    private static String responseText(Response response, int itemIndex) {
        return response.output().get(itemIndex).asMessage().content().get(0).asOutputText().text();
    }

    private static String body(HttpRequest request) {
        return request.getBodyAsBinaryData().toString();
    }
}
