// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AISearchIndexResource;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureAISearchQueryType;
import com.azure.ai.agents.models.AzureAISearchTool;
import com.azure.ai.agents.models.AzureAISearchToolResource;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.BingGroundingSearchConfiguration;
import com.azure.ai.agents.models.BingGroundingSearchToolParameters;
import com.azure.ai.agents.models.BingGroundingTool;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.ImageGenTool;
import com.azure.ai.agents.models.ImageGenToolModel;
import com.azure.ai.agents.models.ImageGenToolQuality;
import com.azure.ai.agents.models.ImageGenToolSize;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.MemorySearchPreviewTool;
import com.azure.ai.agents.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.models.OpenApiProjectConnectionAuthDetails;
import com.azure.ai.agents.models.OpenApiProjectConnectionSecurityScheme;
import com.azure.ai.agents.models.OpenApiTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.Tool;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.openai.client.OpenAIClient;
import com.openai.core.http.HttpResponse;
import com.openai.models.containers.files.content.ContentRetrieveParams;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputText;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PortableToolBehaviorMockTests {
    private static final String ENDPOINT = "https://localhost:8080/api/projects/project";
    private static final String AGENT_NAME = "portable-tool-agent";

    @Test
    public void codeInterpreterGeneratedFileCitationCanBeDownloaded() throws IOException {
        byte[] png = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x01 };
        String citation = "{\"type\":\"container_file_citation\",\"container_id\":\"container-1\","
            + "\"file_id\":\"file-1\",\"filename\":\"chart.png\",\"start_index\":0,\"end_index\":9}";
        String responseJson = OpenAIResponseFixtures.response("resp-file",
            OpenAIResponseFixtures.message("msg-file", "chart.png", citation));
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueueJson(200, responseJson)
            .enqueue(200, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "image/png"), png);
        AgentsClientBuilder builder = createBuilder(httpClient);

        Response response = builder.buildResponsesClient()
            .createAzureResponse(agentOptions(), ResponseCreateParams.builder().input("Create a chart."));
        ResponseOutputText.Annotation.ContainerFileCitation parsedCitation = response.output()
            .get(0)
            .asMessage()
            .content()
            .get(0)
            .asOutputText()
            .annotations()
            .get(0)
            .asContainerFileCitation();
        assertEquals("container-1", parsedCitation.containerId());
        assertEquals("file-1", parsedCitation.fileId());
        assertEquals("chart.png", parsedCitation.filename());

        OpenAIClient openAIClient = builder.buildOpenAIClient();
        ContentRetrieveParams params = ContentRetrieveParams.builder()
            .containerId(parsedCitation.containerId())
            .fileId(parsedCitation.fileId())
            .build();
        byte[] downloaded;
        try (HttpResponse content = openAIClient.containers().files().content().retrieve(params)) {
            downloaded = readBytes(content.body());
        }

        assertArrayEquals(png, downloaded);
        HttpRequest download = httpClient.getRequest(1);
        assertEquals(HttpMethod.GET, download.getHttpMethod());
        assertTrue(download.getUrl().getPath().endsWith("/containers/container-1/files/file-1/content"));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void multipleFunctionCallsAreReturnedWithMatchingOutputs() {
        String calls = OpenAIResponseFixtures.functionCall("call-item-1", "call-1", "get_weather",
            "{\"city\":\"New York\"}") + ","
            + OpenAIResponseFixtures.functionCall("call-item-2", "call-2", "get_forecast", "{\"city\":\"New York\"}");
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, OpenAIResponseFixtures.response("resp-calls", calls))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-final",
                    OpenAIResponseFixtures.message("msg-final", "Weather and forecast received.")));
        ResponsesClient responsesClient = createBuilder(httpClient).buildResponsesClient();

        Response initial
            = responsesClient.createAzureResponse(agentOptions(), ResponseCreateParams.builder().input("Weather?"));
        List<ResponseInputItem> outputs = new ArrayList<>();
        for (ResponseOutputItem item : initial.output()) {
            assertTrue(item.isFunctionCall());
            outputs.add(ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
                .callId(item.asFunctionCall().callId())
                .output("{\"ok\":true}")
                .build()));
        }
        Response completed = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder().inputOfResponse(outputs).previousResponseId(initial.id()));

        assertEquals(2, outputs.size());
        assertEquals("Weather and forecast received.", responseText(completed));
        String continuation = body(httpClient.getRequest(1));
        assertTrue(continuation.contains("\"previous_response_id\":\"resp-calls\""));
        assertTrue(continuation.contains("\"call_id\":\"call-1\""));
        assertTrue(continuation.contains("\"call_id\":\"call-2\""));
        assertTrue(continuation.contains("\"type\":\"function_call_output\""));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void asyncFunctionContinuationPreservesPreviousResponse() {
        String call = OpenAIResponseFixtures.functionCall("call-item", "call-async", "get_temperature",
            "{\"city\":\"Boston\"}");
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, OpenAIResponseFixtures.response("resp-async-call", call))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-async-final",
                    OpenAIResponseFixtures.message("msg-async", "72 degrees.")));
        ResponsesAsyncClient responsesClient = createBuilder(httpClient).buildResponsesAsyncClient();

        Mono<Response> operation
            = responsesClient.createAzureResponse(agentOptions(), ResponseCreateParams.builder().input("Temperature?"))
                .flatMap(initial -> {
                    ResponseInputItem output
                        = ResponseInputItem.ofFunctionCallOutput(ResponseInputItem.FunctionCallOutput.builder()
                            .callId(initial.output().get(0).asFunctionCall().callId())
                            .output("{\"temperature\":72}")
                            .build());
                    return responsesClient.createAzureResponse(agentOptions(),
                        ResponseCreateParams.builder()
                            .inputOfResponse(Collections.singletonList(output))
                            .previousResponseId(initial.id()));
                });

        StepVerifier.create(operation)
            .assertNext(response -> assertEquals("72 degrees.", responseText(response)))
            .verifyComplete();
        assertTrue(body(httpClient.getRequest(1)).contains("\"previous_response_id\":\"resp-async-call\""));
        assertTrue(body(httpClient.getRequest(1)).contains("\"call_id\":\"call-async\""));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void imageGenerationToolAndResultUseExpectedWireShape() {
        byte[] png = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 };
        String base64 = Base64.getEncoder().encodeToString(png);
        String imageOutput = "{\"id\":\"image-1\",\"type\":\"image_generation_call\","
            + "\"status\":\"completed\",\"result\":\"" + base64 + "\"}";
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, OpenAIResponseFixtures.promptAgentVersion(AGENT_NAME, "1"))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-image", imageOutput));
        AgentsClientBuilder builder = createBuilder(httpClient);
        ImageGenTool imageTool = new ImageGenTool().setModel(ImageGenToolModel.GPT_IMAGE_1)
            .setQuality(ImageGenToolQuality.LOW)
            .setSize(ImageGenToolSize.fromString("1024x1024"));

        builder.buildAgentsClient()
            .createAgentVersion(AGENT_NAME, new CreateAgentVersionInput(
                new PromptAgentDefinition("gpt-4o").setTools(Collections.singletonList(imageTool))));
        Response response = builder.buildResponsesClient()
            .createAzureResponse(agentOptions(), ResponseCreateParams.builder().input("Generate an image."));

        String request = body(httpClient.getRequest(0));
        assertTrue(request.contains("\"type\":\"image_generation\""));
        assertTrue(request.contains("\"model\":\"gpt-image-1\""));
        assertTrue(request.contains("\"quality\":\"low\""));
        assertTrue(response.output().get(0).isImageGenerationCall());
        assertArrayEquals(png,
            Base64.getDecoder().decode(response.output().get(0).asImageGenerationCall().result().get()));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void asyncImageGenerationResultDecodesKnownBytes() {
        byte[] png = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 };
        String base64 = Base64.getEncoder().encodeToString(png);
        String imageOutput = "{\"id\":\"image-async\",\"type\":\"image_generation_call\","
            + "\"status\":\"completed\",\"result\":\"" + base64 + "\"}";
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueueJson(200,
            OpenAIResponseFixtures.response("resp-image-async", imageOutput));
        ResponsesAsyncClient responsesClient = createBuilder(httpClient).buildResponsesAsyncClient();

        StepVerifier.create(responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder().input("Generate an image."))).assertNext(response -> {
                assertTrue(response.output().get(0).isImageGenerationCall());
                assertArrayEquals(png,
                    Base64.getDecoder().decode(response.output().get(0).asImageGenerationCall().result().get()));
            }).verifyComplete();
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void mcpProjectConnectionApprovalContinuesResponse() {
        String approvalRequest = "{\"id\":\"approval-1\",\"type\":\"mcp_approval_request\","
            + "\"arguments\":\"{}\",\"name\":\"get_profile\",\"server_label\":\"github\"}";
        DeterministicHttpClient httpClient
            = new DeterministicHttpClient().enqueueJson(200, OpenAIResponseFixtures.promptAgentVersion(AGENT_NAME, "1"))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-approval", approvalRequest))
                .enqueueJson(200, OpenAIResponseFixtures.response("resp-approved",
                    OpenAIResponseFixtures.message("msg-approved", "Profile retrieved.")));
        AgentsClientBuilder builder = createBuilder(httpClient);
        McpTool mcpTool = new McpTool("github").setServerUrl("https://example.test/mcp")
            .setProjectConnectionId("mcp-connection")
            .setRequireApproval("always");

        builder.buildAgentsClient().createAgentVersion(AGENT_NAME, definition(mcpTool));
        ResponsesClient responsesClient = builder.buildResponsesClient();
        Response initial = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder().conversation("conversation-mcp").input("Get my profile."));
        ResponseOutputItem.McpApprovalRequest request = initial.output().get(0).asMcpApprovalRequest();
        ResponseInputItem approval = ResponseInputItem.ofMcpApprovalResponse(
            ResponseInputItem.McpApprovalResponse.builder().approvalRequestId(request.id()).approve(true).build());
        Response completed = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder()
                .conversation("conversation-mcp")
                .inputOfResponse(Collections.singletonList(approval))
                .previousResponseId(initial.id()));

        assertEquals("github", request.serverLabel());
        assertEquals("get_profile", request.name());
        assertEquals("Profile retrieved.", responseText(completed));
        assertTrue(body(httpClient.getRequest(0)).contains("\"project_connection_id\":\"mcp-connection\""));
        String continuation = body(httpClient.getRequest(2));
        assertTrue(continuation.contains("\"type\":\"mcp_approval_response\""));
        assertTrue(continuation.contains("\"approval_request_id\":\"approval-1\""));
        assertTrue(continuation.contains("\"approve\":true"));
        assertTrue(continuation.contains("\"previous_response_id\":\"resp-approval\""));
        assertTrue(continuation.contains("\"conversation\":\"conversation-mcp\""));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void fileSearchMultiTurnPreservesConversationAndPreviousResponse() {
        String fileSearch = "{\"id\":\"file-search-1\",\"type\":\"file_search_call\","
            + "\"queries\":[\"product documentation\"],\"status\":\"completed\",\"results\":[]}";
        DeterministicHttpClient httpClient = new DeterministicHttpClient()
            .enqueueJson(200,
                OpenAIResponseFixtures.response("resp-file-search",
                    fileSearch + "," + OpenAIResponseFixtures.message("msg-search-1", "First result.")))
            .enqueueJson(200, OpenAIResponseFixtures.response("resp-file-search-follow-up",
                OpenAIResponseFixtures.message("msg-search-2", "Follow-up result.")));
        ResponsesClient responsesClient = createBuilder(httpClient).buildResponsesClient();

        Response first = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder()
                .conversation("conversation-file-search")
                .input("Search the product documentation."));
        assertTrue(first.output().get(0).isFileSearchCall());
        assertEquals("product documentation", first.output().get(0).asFileSearchCall().queries().get(0));
        Response second = responsesClient.createAzureResponse(agentOptions(),
            ResponseCreateParams.builder()
                .conversation("conversation-file-search")
                .input("Tell me more.")
                .previousResponseId(first.id()));

        assertEquals("Follow-up result.", responseText(second));
        String continuation = body(httpClient.getRequest(1));
        assertTrue(continuation.contains("\"conversation\":\"conversation-file-search\""));
        assertTrue(continuation.contains("\"previous_response_id\":\"resp-file-search\""));
        assertTrue(continuation.contains("Tell me more."));
        httpClient.assertResponsesConsumed();
    }

    @Test
    public void connectionBackedToolsSerializeTheirConfiguration() {
        DeterministicHttpClient httpClient = new DeterministicHttpClient();
        for (int i = 1; i <= 5; i++) {
            httpClient.enqueueJson(200, OpenAIResponseFixtures.promptAgentVersion(AGENT_NAME, Integer.toString(i)));
        }
        AgentsClient client = createBuilder(httpClient).allowPreview(true).buildAgentsClient();

        client.createAgentVersion(AGENT_NAME,
            definition(new McpTool("github").setServerUrl("https://example.test/mcp")
                .setProjectConnectionId("mcp-connection")
                .setRequireApproval("always")));
        client.createAgentVersion(AGENT_NAME, definition(openApiTool()));
        client.createAgentVersion(AGENT_NAME,
            definition(new AzureAISearchTool(new AzureAISearchToolResource(
                Collections.singletonList(new AISearchIndexResource().setProjectConnectionId("search-connection")
                    .setIndexName("products")
                    .setQueryType(AzureAISearchQueryType.SEMANTIC)
                    .setTopK(5))))));
        client.createAgentVersion(AGENT_NAME,
            definition(new BingGroundingTool(new BingGroundingSearchToolParameters(Collections.singletonList(
                new BingGroundingSearchConfiguration("bing-connection").setMarket("en-US").setCount(3L))))));
        client.createAgentVersion(AGENT_NAME,
            definition(new MemorySearchPreviewTool("memory-store", "user-123").setUpdateDelaySeconds(1)));

        String mcp = body(httpClient.getRequest(0));
        assertTrue(mcp.contains("\"project_connection_id\":\"mcp-connection\""));
        assertTrue(mcp.contains("\"require_approval\":\"always\""));
        String openApi = body(httpClient.getRequest(1));
        assertTrue(openApi.contains("\"type\":\"project_connection\""));
        assertTrue(openApi.contains("\"project_connection_id\":\"openapi-connection\""));
        String search = body(httpClient.getRequest(2));
        assertTrue(search.contains("\"type\":\"azure_ai_search\""));
        assertTrue(search.contains("\"index_name\":\"products\""));
        assertTrue(search.contains("\"query_type\":\"semantic\""));
        String bing = body(httpClient.getRequest(3));
        assertTrue(bing.contains("\"type\":\"bing_grounding\""));
        assertTrue(bing.contains("\"project_connection_id\":\"bing-connection\""));
        String memory = body(httpClient.getRequest(4));
        assertTrue(memory.contains("\"type\":\"memory_search_preview\""));
        assertTrue(memory.contains("\"memory_store_name\":\"memory-store\""));
        assertTrue(memory.contains("\"scope\":\"user-123\""));
        assertTrue(memory.contains("\"update_delay\":1"));
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

    private static CreateAgentVersionInput definition(Tool tool) {
        return new CreateAgentVersionInput(
            new PromptAgentDefinition("gpt-4o").setTools(Collections.singletonList(tool)));
    }

    private static OpenApiTool openApiTool() {
        Map<String, BinaryData> spec = new LinkedHashMap<>();
        spec.put("openapi", BinaryData.fromObject("3.0.0"));
        spec.put("info", BinaryData.fromObject(Collections.singletonMap("title", "Test API")));
        spec.put("paths", BinaryData.fromObject(Collections.emptyMap()));
        return new OpenApiTool(new OpenApiFunctionDefinition("test_api", spec,
            new OpenApiProjectConnectionAuthDetails(new OpenApiProjectConnectionSecurityScheme("openapi-connection"))));
    }

    private static String responseText(Response response) {
        return response.output().get(0).asMessage().content().get(0).asOutputText().text();
    }

    private static String body(HttpRequest request) {
        return request.getBodyAsBinaryData().toString();
    }

    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
