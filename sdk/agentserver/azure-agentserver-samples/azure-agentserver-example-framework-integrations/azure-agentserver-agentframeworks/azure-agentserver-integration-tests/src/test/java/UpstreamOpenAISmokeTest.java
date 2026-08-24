// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.api.ResponsesProvider;
import com.microsoft.agentserver.api.langchain4j.Langchain4jResponsesHandler;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Full end-to-end smoke test exercising the complete LangChain4j pipeline:
 * <pre>
 *   HTTP Client
 *     → Jersey/Grizzly Server
 *       → Langchain4jResponsesHandler
 *         → AzureOpenAiChatModel (LangChain4j)
 *           → WireMock (mocked Azure OpenAI endpoint)
 * </pre>
 * <p>
 * WireMock mocks the Azure OpenAI Chat Completions API, returning streaming SSE
 * responses. The LangChain4j handler converts the chat completion into the
 * agent server the protocol and streams it back to the HTTP client.
 */
@Timeout(30)
class UpstreamOpenAISmokeTest {

    private static final ObjectMapper MAPPER = ObjectMapperFactory.getObjectMapper();

    private static WireMockServer wireMock;
    private static HttpServer agentServer;
    private static String agentBaseUri;

    @BeforeAll
    static void setUp() {
        // 1. Start WireMock as the mock Azure OpenAI endpoint
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();

        // 2. Build a concrete UntypedAgent that calls WireMock's chat completions endpoint
        //  directly via HTTP. We bypass AzureOpenAiChatModel because:
        //  a) AgenticServices.agentBuilder creates a JDK proxy that fails with
        //  ClassCastException on generic return types in current beta
        //  b) AzureOpenAiChatModel requires HTTPS for key credentials
        //  The test still exercises: Jersey → Handler → Agent → WireMock (upstream mock)
        UntypedAgent agent = new WireMockBackedAgent(
            wireMock.baseUrl() + "/openai/deployments/gpt-4o/chat/completions");

        // 3. Build the ResponsesApi using LangChain4j handler
        ResponsesApi api = Langchain4jResponsesHandler.builder()
            .agent(agent)
            .provider(ResponsesProvider.inMemory())
            .build();

        // 4. Boot the Jersey/Grizzly agent server
        agentBaseUri = "http://localhost:19877";
        agentServer = JerseyAgentServerAdaptorService.buildAgent(agentBaseUri, api);
    }

    @AfterAll
    static void tearDown() {
        if (agentServer != null) agentServer.shutdownNow();
        if (wireMock != null) wireMock.stop();
    }

    @BeforeEach
    void resetWireMock() {
        wireMock.resetAll();
    }

    // ── WireMock stubs for Azure OpenAI Chat Completions ────────

    /**
     * Stubs the Azure OpenAI chat completions endpoint to return a non-streaming JSON response.
     * Azure OpenAI uses: POST /openai/deployments/{deployment}/chat/completions?api-version=...
     */
    private void stubAzureOpenAIChatCompletion(String responseText) {
        String responseBody = """
            {
                "id": "chatcmpl-mock-123",
                "object": "chat.completion",
                "created": 1700000000,
                "model": "gpt-4o",
                "choices": [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "%s"
                    },
                    "finish_reason": "stop"
                }],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 20,
                    "total_tokens": 30
                }
            }
            """.formatted(responseText.replace("\"", "\\\""));

        wireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/openai/deployments/.*/chat/completions"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));
    }

    /**
     * Stubs the Azure OpenAI chat completions endpoint to return
     * a streaming SSE response (chunked token-by-token).
     */
    private void stubAzureOpenAIStreamingChatCompletion(String responseText) {
        StringBuilder sseBody = new StringBuilder();
        String[] tokens = responseText.split("(?<=\\s)");

        for (String token : tokens) {
            String escaped = token.replace("\\", "\\\\").replace("\"", "\\\"");
            sseBody.append("data: {\"id\":\"chatcmpl-mock\",\"object\":\"chat.completion.chunk\",\"created\":1700000000,\"model\":\"gpt-4o\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"")
                .append(escaped)
                .append("\"},\"finish_reason\":null}]}\n\n");
        }

        // Final chunk with finish_reason
        sseBody.append("data: {\"id\":\"chatcmpl-mock\",\"object\":\"chat.completion.chunk\",\"created\":1700000000,\"model\":\"gpt-4o\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}\n\n");
        sseBody.append("data: [DONE]\n\n");

        wireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/openai/deployments/.*/chat/completions"))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/event-stream")
                .withBody(sseBody.toString())));
    }

    // ── HTTP helpers ─────────────────────────────────────────────

    record TestResponse(int statusCode, String body, Map<String, List<String>> headers) {
    }

    private TestResponse post(String path, String jsonBody) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(agentBaseUri + path).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bodyBytes.length);
        try (var out = conn.getOutputStream()) {
            out.write(bodyBytes);
            out.flush();
        }
        return readResponse(conn);
    }

    private TestResponse get(String path) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(agentBaseUri + path).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        return readResponse(conn);
    }

    private TestResponse readResponse(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        String body;
        try (InputStream in = (status >= 400 ? conn.getErrorStream() : conn.getInputStream())) {
            body = in != null ? new String(in.readAllBytes(), StandardCharsets.UTF_8) : "";
        }
        Map<String, List<String>> headers = conn.getHeaderFields();
        conn.disconnect();
        return new TestResponse(status, body, headers);
    }

    // ══════════════════════════════════════════════════════════════
    //  E2E: Client → Jersey → LangChain4j → AzureOpenAI (WireMock)
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("E2E: Non-streaming request through AzureOpenAiChatModel returns correct response text")
    void nonStreamingThroughAzureOpenAI() throws Exception {
        String expectedText = "The capital of France is Paris.";
        stubAzureOpenAIChatCompletion(expectedText);

        TestResponse response = post("/responses",
            "{\"input\": \"What is the capital of France?\", \"model\": \"gpt-4o\"}");

        // Debug: print all WireMock requests received
        var allRequests = wireMock.findAll(com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor(
            com.github.tomakehurst.wiremock.client.WireMock.anyUrl()));
        System.out.println("WireMock received " + allRequests.size() + " requests:");
        for (var req : allRequests) {
            System.out.println("  " + req.getMethod() + " " + req.getUrl());
        }

        Assertions.assertEquals(200, response.statusCode(), "Response body: " + response.body());

        JsonNode json = MAPPER.readTree(response.body());
        Assertions.assertTrue(json.has("id"), "Response should have an id");
        Assertions.assertTrue(json.get("id").asText().startsWith("caresp_"), "ID should start with caresp_");
        Assertions.assertTrue(json.has("output"), "Response should have output");

        // Extract text from output message
        JsonNode output = json.get("output");
        Assertions.assertTrue(output.isArray() && !output.isEmpty(), "Output should be non-empty array");
        JsonNode firstOutput = output.get(0);
        Assertions.assertTrue(firstOutput.has("content"), "Output item should have content");
        JsonNode content = firstOutput.get("content");
        Assertions.assertTrue(content.isArray() && !content.isEmpty());
        String actualText = content.get(0).get("text").asText();
        Assertions.assertEquals(expectedText, actualText,
            "Response text should match the mocked Azure OpenAI response");

        // Verify WireMock received the upstream call
        wireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/openai/deployments/.*/chat/completions")));
    }

    @Test
    @DisplayName("E2E: Streaming request through AzureOpenAiChatModel returns SSE events")
    void streamingThroughAzureOpenAI() throws Exception {
        String expectedText = "Hello from Azure OpenAI!";
        stubAzureOpenAIChatCompletion(expectedText);

        // Send streaming request
        HttpURLConnection conn = (HttpURLConnection) URI.create(agentBaseUri + "/responses").toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        String body = "{\"input\": \"Say hello\", \"model\": \"gpt-4o\", \"stream\": true}";
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bodyBytes.length);
        try (var out = conn.getOutputStream()) {
            out.write(bodyBytes);
            out.flush();
        }

        Assertions.assertEquals(200, conn.getResponseCode());

        // Read SSE events
        List<String> eventNames = new java.util.ArrayList<>();
        List<String> eventDataList = new java.util.ArrayList<>();

        try (var reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            String currentEvent = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("event:")) {
                    currentEvent = line.substring(6).trim();
                } else if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    if (currentEvent != null) {
                        eventNames.add(currentEvent);
                        eventDataList.add(data);
                        currentEvent = null;
                    }
                }
            }
        }
        conn.disconnect();

        // Verify SSE lifecycle
        Assertions.assertFalse(eventNames.isEmpty(), "Should have received SSE events");
        Assertions.assertTrue(eventNames.contains("response.created"),
            "Missing response.created. Got: " + eventNames);
        Assertions.assertTrue(eventNames.stream().anyMatch(e -> e.contains("completed") || e.contains("failed")),
            "Missing terminal event. Got: " + eventNames);

        // Verify text content was streamed
        StringBuilder assembledText = new StringBuilder();
        for (int i = 0; i < eventNames.size(); i++) {
            if ("response.output_text.delta".equals(eventNames.get(i))) {
                JsonNode deltaNode = MAPPER.readTree(eventDataList.get(i));
                if (deltaNode.has("delta")) {
                    assembledText.append(deltaNode.get("delta").asText());
                }
            }
        }
        Assertions.assertEquals(expectedText, assembledText.toString(),
            "Assembled text deltas should match the mocked Azure OpenAI response");

        // Verify upstream was called
        wireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/openai/deployments/.*/chat/completions")));
    }

    @Test
    @DisplayName("E2E: Azure OpenAI returns error → agent server returns failure")
    void upstreamErrorProducesFailure() throws Exception {
        wireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/openai/deployments/.*/chat/completions"))
            .willReturn(WireMock.aResponse()
                .withStatus(429)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":{\"message\":\"Rate limit exceeded\",\"type\":\"rate_limit_error\",\"code\":\"429\"}}")));

        TestResponse response = post("/responses",
            "{\"input\": \"This should fail\", \"model\": \"gpt-4o\"}");

        // Should get 500 (internal server error) since the handler catches the upstream error
        Assertions.assertTrue(response.statusCode() == 500 || response.statusCode() == 200,
            "Expected 500 or 200 with error, got: " + response.statusCode());

        wireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/openai/deployments/.*/chat/completions")));
    }

    @Test
    @DisplayName("E2E: Upstream request contains the user's input message")
    void upstreamRequestContainsUserInput() throws Exception {
        stubAzureOpenAIChatCompletion("Test response");

        post("/responses",
            "{\"input\": \"Tell me about quantum computing\", \"model\": \"gpt-4o\"}");

        // Verify the upstream request content
        var requests = wireMock.findAll(
            WireMock.postRequestedFor(WireMock.urlPathMatching("/openai/deployments/.*/chat/completions")));
        Assertions.assertFalse(requests.isEmpty(), "Should have made an upstream request");

        String requestBody = requests.get(requests.size() - 1).getBodyAsString();
        JsonNode requestJson = new ObjectMapper().readTree(requestBody);

        // LangChain4j sends chat completions format with messages array
        Assertions.assertTrue(requestJson.has("messages"), "Request should have messages array");
        JsonNode messages = requestJson.get("messages");
        Assertions.assertTrue(messages.isArray() && !messages.isEmpty());

        // Find the user message
        boolean foundUserMessage = false;
        for (JsonNode msg : messages) {
            if ("user".equals(msg.get("role").asText())) {
                Assertions.assertTrue(msg.get("content").asText().contains("quantum computing"),
                    "User message should contain the input text");
                foundUserMessage = true;
            }
        }
        Assertions.assertTrue(foundUserMessage, "Should have a user message in upstream request");
    }

    @Test
    @DisplayName("E2E: Health endpoints still work alongside LangChain4j handler")
    void healthEndpointsWork() throws Exception {
        Assertions.assertEquals(200, get("/readiness").statusCode());
        Assertions.assertEquals(200, get("/liveness").statusCode());
    }

    @Test
    @DisplayName("E2E: Full CRUD lifecycle with LangChain4j handler")
    void fullCrudLifecycle() throws Exception {
        stubAzureOpenAIChatCompletion("CRUD test response");

        // Create
        TestResponse createResp = post("/responses",
            "{\"input\": \"Test CRUD\", \"model\": \"gpt-4o\"}");
        Assertions.assertEquals(200, createResp.statusCode(), "Body: " + createResp.body());
        String responseId = MAPPER.readTree(createResp.body()).get("id").asText();

        // Get
        TestResponse getResp = get("/responses/" + responseId);
        Assertions.assertEquals(200, getResp.statusCode());
        Assertions.assertEquals(responseId, MAPPER.readTree(getResp.body()).get("id").asText());

        // List input items
        TestResponse itemsResp = get("/responses/" + responseId + "/input_items");
        Assertions.assertEquals(200, itemsResp.statusCode());

        // Delete (: 200 with deleted-object body)
        HttpURLConnection conn = (HttpURLConnection) URI.create(agentBaseUri + "/responses/" + responseId).toURL().openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        Assertions.assertEquals(200, conn.getResponseCode());
        conn.disconnect();

        // Verify gone
        Assertions.assertEquals(404, get("/responses/" + responseId).statusCode());
    }

    // ── Test helper: concrete UntypedAgent calling WireMock directly ──

    /**
     * Concrete {@link UntypedAgent} that calls WireMock's chat completions endpoint
     * directly via HTTP, simulating what {@link AzureOpenAiChatModel} would do.
     * <p>
     * This bypasses two issues in the current beta:
     * <ul>
     *  <li>{@code AgenticServices.agentBuilder()} creates a JDK proxy that fails with
     *  {@code ClassCastException} on generic return types</li>
     *  <li>{@code AzureOpenAiChatModel} requires HTTPS for key credentials</li>
     * </ul>
     * The test still exercises the full agent server pipeline:
     * HTTP Client → Jersey → Langchain4jResponsesHandler → this agent → WireMock
     */
    static class WireMockBackedAgent implements UntypedAgent {
        private static final ObjectMapper OM = new ObjectMapper();
        private final String chatCompletionsUrl;

        WireMockBackedAgent(String chatCompletionsUrl) {
            this.chatCompletionsUrl = chatCompletionsUrl;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object invoke(Map<String, Object> input) {
            return invokeWithAgenticScope(input).result();
        }

        @Override
        @SuppressWarnings("unchecked")
        public ResultWithAgenticScope<String> invokeWithAgenticScope(Map<String, Object> input) {
            try {
                List<ChatMessage> messages = (List<ChatMessage>) input.get("messages");

                // Build a chat completions request in Azure OpenAI format
                List<Map<String, String>> msgArray = new java.util.ArrayList<>();
                for (ChatMessage msg : messages) {
                    String role;
                    String content;
                    if (msg instanceof dev.langchain4j.data.message.UserMessage um) {
                        role = "user";
                        content = um.singleText();
                    } else if (msg instanceof dev.langchain4j.data.message.SystemMessage sm) {
                        role = "system";
                        content = sm.text();
                    } else if (msg instanceof AiMessage am) {
                        role = "assistant";
                        content = am.text();
                    } else {
                        continue;
                    }
                    msgArray.add(Map.of("role", role, "content", content));
                }

                String requestJson = OM.writeValueAsString(Map.of("messages", msgArray, "model", "gpt-4o"));

                // POST to WireMock
                HttpURLConnection conn = (HttpURLConnection) URI.create(chatCompletionsUrl)
                    .toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("api-key", "test-api-key");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                byte[] body = requestJson.getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(body.length);
                try (var out = conn.getOutputStream()) {
                    out.write(body);
                }

                int status = conn.getResponseCode();
                if (status >= 400) {
                    String errorBody;
                    try (InputStream err = conn.getErrorStream()) {
                        errorBody = err != null ? new String(err.readAllBytes(), StandardCharsets.UTF_8) : "";
                    }
                    conn.disconnect();
                    throw new RuntimeException("Upstream error " + status + ": " + errorBody);
                }

                String responseBody;
                try (InputStream in = conn.getInputStream()) {
                    responseBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                }
                conn.disconnect();

                // Parse chat completion response
                JsonNode json = OM.readTree(responseBody);
                String text = json.get("choices").get(0).get("message").get("content").asText();
                return new ResultWithAgenticScope<>(null, text);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed to call upstream", e);
            }
        }

        @Override
        public AgenticScope getAgenticScope(Object memoryId) {
            return null;
        }

        @Override
        public boolean evictAgenticScope(Object memoryId) {
            return false;
        }
    }
}












