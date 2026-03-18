// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.FileSearchTool;
import com.azure.ai.agents.models.FunctionTool;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.OpenApiAnonymousAuthDetails;
import com.azure.ai.agents.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.models.OpenApiTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WebSearchPreviewTool;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.vectorstores.VectorStore;
import com.openai.models.vectorstores.VectorStoreCreateParams;
import com.openai.services.blocking.ConversationService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class ToolsTests extends ClientTestBase {

    // -----------------------------------------------------------------------
    // OpenAPI Tool
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void openApiToolEndToEnd(HttpClient httpClient, AgentsServiceVersion serviceVersion) throws IOException {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);
        ConversationService conversationService = getConversationsSyncClient(httpClient, serviceVersion);

        Map<String, BinaryData> spec
            = OpenApiFunctionDefinition.readSpecFromFile(TestUtils.getTestResourcePath("assets/httpbin_openapi.json"));

        OpenApiFunctionDefinition toolDefinition
            = new OpenApiFunctionDefinition("httpbin_get", spec, new OpenApiAnonymousAuthDetails())
                .setDescription("Get request metadata from an OpenAPI endpoint.");

        PromptAgentDefinition agentDefinition
            = new PromptAgentDefinition("gpt-4o").setInstructions("Use the OpenAPI tool for HTTP request metadata.")
                .setTools(Arrays.asList(new OpenApiTool(toolDefinition)));

        AgentVersionDetails agent = agentsClient.createAgentVersion("openapi-tool-test-agent-java", agentDefinition);
        assertNotNull(agent);
        assertNotNull(agent.getId());

        try {
            Conversation conversation = conversationService.create();
            assertNotNull(conversation);

            conversationService.items()
                .create(ItemCreateParams.builder()
                    .conversationId(conversation.id())
                    .addItem(EasyInputMessage.builder()
                        .role(EasyInputMessage.Role.USER)
                        .content("Use the OpenAPI tool and summarize the returned URL and origin in one sentence.")
                        .build())
                    .build());

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id(),
                ResponseCreateParams.builder().maxOutputTokens(300L));

            assertNotNull(response);
            assertTrue(response.id().startsWith("resp"));
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());
            assertFalse(response.output().isEmpty());
            assertTrue(response.output().stream().anyMatch(item -> item.isMessage()));
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // -----------------------------------------------------------------------
    // Code Interpreter Tool
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void codeInterpreterToolEndToEnd(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        CodeInterpreterTool tool = new CodeInterpreterTool();

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition("gpt-4o")
            .setInstructions("You are a helpful assistant that can execute Python code to solve problems.")
            .setTools(Collections.singletonList(tool));

        AgentVersionDetails agent
            = agentsClient.createAgentVersion("code-interpreter-test-agent-java", agentDefinition);
        assertNotNull(agent);
        assertNotNull(agent.getId());

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(agentReference,
                ResponseCreateParams.builder().input("Calculate the first 10 prime numbers and show the Python code."));

            assertNotNull(response);
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());
            assertFalse(response.output().isEmpty());
            // Should contain at least one code interpreter call
            assertTrue(response.output().stream().anyMatch(item -> item.isCodeInterpreterCall()));
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // -----------------------------------------------------------------------
    // Function Calling Tool
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void functionCallToolEndToEnd(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        Map<String, Object> locationProp = new LinkedHashMap<String, Object>();
        locationProp.put("type", "string");
        locationProp.put("description", "The city and state, e.g. Seattle, WA");

        Map<String, Object> unitProp = new LinkedHashMap<String, Object>();
        unitProp.put("type", "string");
        unitProp.put("enum", Arrays.asList("celsius", "fahrenheit"));

        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("location", locationProp);
        properties.put("unit", unitProp);

        Map<String, BinaryData> parameters = new HashMap<String, BinaryData>();
        parameters.put("type", BinaryData.fromObject("object"));
        parameters.put("properties", BinaryData.fromObject(properties));
        parameters.put("required", BinaryData.fromObject(Arrays.asList("location", "unit")));
        parameters.put("additionalProperties", BinaryData.fromObject(false));

        FunctionTool tool = new FunctionTool("get_weather", parameters, true)
            .setDescription("Get the current weather in a given location");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition("gpt-4o")
            .setInstructions("You are a helpful assistant. When asked about weather, use the get_weather function.")
            .setTools(Collections.singletonList(tool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("function-call-test-agent-java", agentDefinition);
        assertNotNull(agent);

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(agentReference,
                ResponseCreateParams.builder().input("What's the weather like in Seattle?"));

            assertNotNull(response);
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());
            assertFalse(response.output().isEmpty());
            // Should contain a function call
            assertTrue(response.output().stream().anyMatch(item -> item.isFunctionCall()));

            // Validate function call details
            ResponseOutputItem functionCallItem
                = response.output().stream().filter(item -> item.isFunctionCall()).findFirst().get();

            assertEquals("get_weather", functionCallItem.asFunctionCall().name());
            assertNotNull(functionCallItem.asFunctionCall().arguments());
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // -----------------------------------------------------------------------
    // Web Search Tool
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void webSearchToolEndToEnd(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        WebSearchPreviewTool tool = new WebSearchPreviewTool();

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition("gpt-4o")
            .setInstructions("You are a helpful assistant that can search the web.")
            .setTools(Collections.singletonList(tool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("web-search-test-agent-java", agentDefinition);
        assertNotNull(agent);

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(agentReference,
                ResponseCreateParams.builder().input("What are the latest trends in renewable energy?"));

            assertNotNull(response);
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());
            assertFalse(response.output().isEmpty());
            assertTrue(response.output().stream().anyMatch(item -> item.isMessage()));
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // -----------------------------------------------------------------------
    // MCP Tool
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void mcpToolEndToEnd(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);

        McpTool tool = new McpTool("api-specs").setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
            .setRequireApproval("always");

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition("gpt-4o")
            .setInstructions("You are a helpful agent that can use MCP tools to assist users.")
            .setTools(Collections.singletonList(tool));

        AgentVersionDetails agent = agentsClient.createAgentVersion("mcp-test-agent-java", agentDefinition);
        assertNotNull(agent);

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(agentReference,
                ResponseCreateParams.builder().input("Please summarize the Azure REST API specifications Readme"));

            assertNotNull(response);
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());

            // Process MCP approval requests
            List<ResponseInputItem> approvals = new ArrayList<ResponseInputItem>();
            for (ResponseOutputItem item : response.output()) {
                if (item.isMcpApprovalRequest()) {
                    approvals
                        .add(ResponseInputItem.ofMcpApprovalResponse(ResponseInputItem.McpApprovalResponse.builder()
                            .approvalRequestId(item.asMcpApprovalRequest().id())
                            .approve(true)
                            .build()));
                }
            }

            assertFalse(approvals.isEmpty(), "Expected at least one MCP approval request");

            // Send approvals and get the final response
            Response finalResponse = responsesClient.createWithAgent(agentReference,
                ResponseCreateParams.builder().inputOfResponse(approvals).previousResponseId(response.id()));

            assertNotNull(finalResponse);
            assertTrue(finalResponse.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, finalResponse.status().get());
            assertFalse(finalResponse.output().isEmpty());
            assertTrue(finalResponse.output().stream().anyMatch(item -> item.isMessage()));
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    // -----------------------------------------------------------------------
    // File Search Tool
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void fileSearchToolEndToEnd(HttpClient httpClient, AgentsServiceVersion serviceVersion) throws Exception {
        AgentsClient agentsClient = getAgentsSyncClient(httpClient, serviceVersion);
        ResponsesClient responsesClient = getResponsesSyncClient(httpClient, serviceVersion);
        ConversationService conversationService = getConversationsSyncClient(httpClient, serviceVersion);

        AgentsClientBuilder openAIBuilder = getClientBuilder(httpClient, serviceVersion);
        com.openai.client.OpenAIClient openAIClient = openAIBuilder.buildOpenAIClient();

        // Create a sample document and upload it
        String sampleContent = "The Solar System consists of the Sun and the celestial objects bound to it by gravity. "
            + "The eight planets in order from the Sun are: Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, and Neptune. "
            + "Jupiter is the largest planet, while Mercury is the smallest.";

        Path tempFile = Files.createTempFile("test_document", ".txt");
        Files.write(tempFile, sampleContent.getBytes(StandardCharsets.UTF_8));

        FileObject uploadedFile = null;
        VectorStore vectorStore = null;
        AgentVersionDetails agent = null;

        try {
            uploadedFile = openAIClient.files()
                .create(FileCreateParams.builder().file(tempFile).purpose(FilePurpose.ASSISTANTS).build());
            assertNotNull(uploadedFile);
            assertNotNull(uploadedFile.id());

            vectorStore = openAIClient.vectorStores()
                .create(VectorStoreCreateParams.builder()
                    .name("TestVectorStore")
                    .fileIds(Collections.singletonList(uploadedFile.id()))
                    .build());
            assertNotNull(vectorStore);

            // Wait for vector store to process
            sleep(5000);

            FileSearchTool tool = new FileSearchTool(Collections.singletonList(vectorStore.id()));

            PromptAgentDefinition agentDefinition = new PromptAgentDefinition("gpt-4o")
                .setInstructions("You are a helpful assistant that searches uploaded files to answer questions.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("file-search-test-agent-java", agentDefinition);
            assertNotNull(agent);

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            Conversation conversation = conversationService.create();
            assertNotNull(conversation);

            Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id(),
                ResponseCreateParams.builder().input("What is the largest planet in the Solar System?"));

            assertNotNull(response);
            assertTrue(response.status().isPresent());
            assertEquals(ResponseStatus.COMPLETED, response.status().get());
            assertFalse(response.output().isEmpty());
            // Should contain file search call and a message
            assertTrue(response.output().stream().anyMatch(item -> item.isFileSearchCall()));
            assertTrue(response.output().stream().anyMatch(item -> item.isMessage()));
        } finally {
            Files.deleteIfExists(tempFile);
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }
            if (vectorStore != null) {
                openAIClient.vectorStores().delete(vectorStore.id());
            }
            if (uploadedFile != null) {
                openAIClient.files().delete(uploadedFile.id());
            }
        }
    }
}
