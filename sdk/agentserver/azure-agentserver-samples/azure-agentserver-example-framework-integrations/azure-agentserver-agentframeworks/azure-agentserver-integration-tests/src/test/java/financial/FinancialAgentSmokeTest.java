// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package financial;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.agentserver.api.ResponsesProvider;
import com.microsoft.agentserver.api.langchain4j.Langchain4jResponsesHandler;
import com.microsoft.agentserver.api.langchain4j.SupervisorAgentWithMemory;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import com.microsoft.agentserver.sample.financial.AccountingAgent;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * End-to-end smoke test for the <b>financial supervisor agent</b> sample,
 * exercising the full agent server pipeline with a Mockito-mocked
 * {@link ChatModel}:
 * <pre>
 *   HTTP Client
 *     → Jersey/Grizzly Server
 *       → Langchain4jResponsesHandler (supervisor path)
 *         → SupervisorAgent (LangChain4j agentic)
 *           → Planner (→ mocked ChatModel returning AgentInvocation JSON)
 *           → Sub-agents (withdraw$0, credit$1, exchange$2, getBalance$3)
 *             → Mocked ChatModel (returning text or tool_calls)
 *           → BankTool / ExchangeTool (real local execution)
 * </pre>
 * <p>
 * The supervisor planner expects JSON responses in iterative steps:
 * <ol>
 *  <li>Step 1: route to a sub-agent → {@code {"agentName": "getBalance$3", "arguments": {"user": "Mario"}}}</li>
 *  <li>Step 2: after sub-agent result, return "done" → {@code {"agentName": "done", "arguments": {"response": "..."}}}</li>
 * </ol>
 * Sub-agents use the same ChatModel for tool calls and text responses.
 * <p>
 * Available sub-agent names (registered by AgenticServices):
 * <ul>
 *  <li>{@code withdraw$0} — A banker that withdraws USD from an account [user: String, amount: Double]</li>
 *  <li>{@code credit$1} — A banker that credits USD to an account [user: String, amount: Double]</li>
 *  <li>{@code exchange$2} — Currency exchange [originalCurrency: String, amount: Double, targetCurrency: String]</li>
 *  <li>{@code getBalance$3} — Account info [user: String]</li>
 * </ul>
 */
@Timeout(30)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinancialAgentSmokeTest {

    private static final ObjectMapper MAPPER = ObjectMapperFactory.getObjectMapper();
    private static final String BASE_URI = "http://localhost:19878";

    private static HttpServer agentServer;
    private static ChatModel mockModel;

    @BeforeAll
    static void setUp() {
        mockModel = mock(ChatModel.class);

        // Default stub so the supervisor can be built without errors
        when(mockModel.chat(any(ChatRequest.class)))
            .thenReturn(textResponse("{\"agentName\": \"done\", \"arguments\": {\"response\": \"default\"}}"));

        SupervisorAgentWithMemory agent = AccountingAgent.build(mockModel);

        agentServer = JerseyAgentServerAdaptorService.buildAgent(
            BASE_URI,
            Langchain4jResponsesHandler.builder()
                .supervisorAgent(agent)
                .provider(ResponsesProvider.inMemory())
                .build());
    }

    @AfterAll
    static void tearDown() {
        if (agentServer != null) agentServer.shutdownNow();
    }

    // ── Mock helpers ─────────────────────────────────────────────

    /**
     * Detects whether the current call is a planner call by checking for the
     * planner's characteristic system message.
     */
    private static boolean isPlannerCall(ChatRequest request) {
        String systemText = extractSystemMessage(request);
        return systemText != null && systemText.contains("planner expert");
    }

    /**
     * Detects whether the current call is a scorer call. The scorer has no system
     * message and typically a single user message asking for scoring.
     */
    private static boolean isScorerCall(ChatRequest request) {
        String systemText = extractSystemMessage(request);
        if (systemText != null && (systemText.contains("score") || systemText.contains("rating"))) {
            return true;
        }
        // Scorer call has no system message, few messages, and no tool specs
        if (systemText == null
            && (request.toolSpecifications() == null || request.toolSpecifications().isEmpty())) {
            return true;
        }
        return false;
    }

    /**
     * Configures the mock for a simple flow: route to a sub-agent, the sub-agent
     * returns text directly (no tool calls), then planner returns "done".
     * <p>
     * Flow:
     * Planner call 1 → route to targetAgent with args
     * Sub-agent call → return text
     * Planner call 2 → return "done" with responseText
     */
    private static void stubSimpleRouting(String targetAgent, String agentArgs,
                                          String subAgentResponse, String finalResponse) {
        AtomicInteger plannerCalls = new AtomicInteger(0);

        when(mockModel.chat(any(ChatRequest.class))).thenAnswer(invocation -> {
            ChatRequest request = invocation.getArgument(0);

            if (isPlannerCall(request)) {
                int call = plannerCalls.incrementAndGet();
                if (call == 1) {
                    // First planner call: route to the target agent
                    return textResponse("""
                        {"agentName": "%s", "arguments": %s}
                        """.formatted(targetAgent, agentArgs));
                } else {
                    // Subsequent planner call: we're done
                    return textResponse("""
                        {"agentName": "done", "arguments": {"response": "%s"}}
                        """.formatted(escapeJson(finalResponse)));
                }
            }

            if (isScorerCall(request)) {
                return textResponse("{\"score1\": 0.9, \"score2\": 0.1}");
            }

            // Sub-agent call: return text
            return textResponse(subAgentResponse);
        });
    }

    /**
     * Configures the mock for a tool-calling flow: route to a sub-agent, the
     * sub-agent issues a tool call, tool executes locally, sub-agent returns
     * text after tool result, then planner returns "done".
     * <p>
     * Flow:
     * Planner call 1 → route to targetAgent with args
     * Sub-agent call 1 (has tool specs) → return tool_call
     * [BankTool/ExchangeTool executes locally]
     * Sub-agent call 2 (has tool result message) → return text
     * Planner call 2 → return "done" with responseText
     */
    private static void stubToolCallingFlow(String targetAgent, String agentArgs,
                                            String toolName, String toolArgs,
                                            String subAgentResponse, String finalResponse) {
        AtomicInteger plannerCalls = new AtomicInteger(0);
        AtomicInteger toolCallCount = new AtomicInteger(0);

        when(mockModel.chat(any(ChatRequest.class))).thenAnswer(invocation -> {
            ChatRequest request = invocation.getArgument(0);
            List<? extends ChatMessage> messages = request.messages();

            if (isPlannerCall(request)) {
                int call = plannerCalls.incrementAndGet();
                if (call == 1) {
                    return textResponse("""
                        {"agentName": "%s", "arguments": %s}
                        """.formatted(targetAgent, agentArgs));
                } else {
                    return textResponse("""
                        {"agentName": "done", "arguments": {"response": "%s"}}
                        """.formatted(escapeJson(finalResponse)));
                }
            }

            if (isScorerCall(request)) {
                return textResponse("{\"score1\": 0.9, \"score2\": 0.1}");
            }

            // Sub-agent: check if tool result is present (second call)
            boolean hasToolResult = messages.stream()
                .anyMatch(m -> m instanceof ToolExecutionResultMessage);
            if (hasToolResult) {
                return textResponse(subAgentResponse);
            }

            // Sub-agent: if tools are available, issue a tool call (first call)
            if (request.toolSpecifications() != null && !request.toolSpecifications().isEmpty()) {
                toolCallCount.incrementAndGet();
                return toolCallResponse(toolName, toolArgs);
            }

            // Fallback
            return textResponse(subAgentResponse);
        });
    }

    private static String extractSystemMessage(ChatRequest request) {
        for (ChatMessage msg : request.messages()) {
            if (msg instanceof SystemMessage sm) {
                return sm.text().toLowerCase();
            }
        }
        return null;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static ChatResponse textResponse(String text) {
        return ChatResponse.builder()
            .aiMessage(AiMessage.from(text))
            .tokenUsage(new TokenUsage(10, 20, 30))
            .finishReason(FinishReason.STOP)
            .build();
    }

    private static ChatResponse toolCallResponse(String toolName, String argsJson) {
        ToolExecutionRequest toolCall = ToolExecutionRequest.builder()
            .id("call_" + toolName)
            .name(toolName)
            .arguments(argsJson)
            .build();
        return ChatResponse.builder()
            .aiMessage(AiMessage.from(List.of(toolCall)))
            .tokenUsage(new TokenUsage(10, 5, 15))
            .finishReason(FinishReason.TOOL_EXECUTION)
            .build();
    }

    // ── HTTP helpers ─────────────────────────────────────────────

    record TestResponse(int statusCode, String body, Map<String, List<String>> headers) {
    }

    private TestResponse post(String path, String jsonBody) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URI + path).toURL().openConnection();
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
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URI + path).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        return readResponse(conn);
    }

    private TestResponse delete(String path) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URI + path).toURL().openConnection();
        conn.setRequestMethod("DELETE");
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
    //  Tests
    // ══════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Non-streaming: simple text response through getBalance agent")
    void nonStreamingSimpleTextResponse() throws Exception {
        stubSimpleRouting("getBalance$3", "{\"user\": \"Mario\"}",
            "1000.0",
            "Mario's balance is $1000.00");

        TestResponse response = post("/responses",
            """
                {"input": "What is Mario's balance?", "model": "gpt-4o"}
                """);

        Assertions.assertEquals(200, response.statusCode(), "Response body: " + response.body());

        JsonNode json = MAPPER.readTree(response.body());
        Assertions.assertTrue(json.has("id"), "Response should have an id");
        Assertions.assertTrue(json.get("id").asText().startsWith("caresp_"), "ID should start with caresp_");
        Assertions.assertTrue(json.has("output"), "Response should have output");

        JsonNode output = json.get("output");
        Assertions.assertTrue(output.isArray() && !output.isEmpty(), "Output should be non-empty array");
        JsonNode content = output.get(0).get("content");
        Assertions.assertTrue(content.isArray() && !content.isEmpty(), "Content should be non-empty array");

        String actualText = content.get(0).get("text").asText();
        Assertions.assertFalse(actualText.isBlank(), "Response text should not be blank");
    }

    @Test
    @Order(2)
    @DisplayName("Non-streaming: tool call (getBalance) → tool execution → text response")
    void nonStreamingWithToolExecution() throws Exception {
        stubToolCallingFlow("getBalance$3", "{\"user\": \"Mario\"}",
            "getBalance", "{\"user\": \"Mario\"}",
            "1000.0",
            "I checked Mario's account. His balance is 1000.0 USD.");

        TestResponse response = post("/responses",
            """
                {"input": "What is Mario's account balance?", "model": "gpt-4o"}
                """);

        Assertions.assertEquals(200, response.statusCode(), "Response body: " + response.body());

        JsonNode json = MAPPER.readTree(response.body());
        JsonNode output = json.get("output");
        Assertions.assertTrue(output.isArray() && !output.isEmpty(), "Output should be non-empty array");

        String text = output.get(0).get("content").get(0).get("text").asText();
        Assertions.assertFalse(text.isBlank(), "Response text should not be blank");
    }

    @Test
    @Order(3)
    @DisplayName("Streaming: returns SSE events with expected lifecycle")
    void streamingReturnsSSEEvents() throws Exception {
        stubSimpleRouting("getBalance$3", "{\"user\": \"Mario\"}",
            "1000.0",
            "Hello from the financial agent!");

        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URI + "/responses").toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        String body = """
            {"input": "Say hello", "model": "gpt-4o", "stream": true}
            """;
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bodyBytes.length);
        try (var out = conn.getOutputStream()) {
            out.write(bodyBytes);
            out.flush();
        }

        Assertions.assertEquals(200, conn.getResponseCode());

        List<String> eventNames = new ArrayList<>();
        List<String> eventDataList = new ArrayList<>();

        try (var reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
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

        Assertions.assertFalse(eventNames.isEmpty(), "Should have received SSE events");
        Assertions.assertTrue(eventNames.contains("response.created"),
            "Missing response.created. Got: " + eventNames);
        Assertions.assertTrue(
            eventNames.stream().anyMatch(e -> e.contains("completed") || e.contains("failed")),
            "Missing terminal event. Got: " + eventNames);
    }

    @Test
    @Order(4)
    @DisplayName("Streaming with tool call: getBalance tool executed and result streamed")
    void streamingWithToolExecution() throws Exception {
        stubToolCallingFlow("getBalance$3", "{\"user\": \"Georgio\"}",
            "getBalance", "{\"user\": \"Georgio\"}",
            "1000.0",
            "Georgio's balance is 1000.0 USD");

        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URI + "/responses").toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "text/event-stream");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        String body = """
            {"input": "What is Georgio's balance?", "model": "gpt-4o", "stream": true}
            """;
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bodyBytes.length);
        try (var out = conn.getOutputStream()) {
            out.write(bodyBytes);
            out.flush();
        }

        Assertions.assertEquals(200, conn.getResponseCode());

        List<String> eventNames = new ArrayList<>();
        try (var reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            String currentEvent = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("event:")) {
                    currentEvent = line.substring(6).trim();
                } else if (line.startsWith("data:")) {
                    if (currentEvent != null) {
                        eventNames.add(currentEvent);
                        currentEvent = null;
                    }
                }
            }
        }
        conn.disconnect();

        Assertions.assertFalse(eventNames.isEmpty(), "Should have received SSE events");
        Assertions.assertTrue(
            eventNames.stream().anyMatch(e -> e.contains("completed") || e.contains("failed")),
            "Missing terminal event. Got: " + eventNames);
    }

    @Test
    @Order(5)
    @DisplayName("CRUD lifecycle: create → get → list input items → delete → 404")
    void fullCrudLifecycle() throws Exception {
        stubSimpleRouting("getBalance$3", "{\"user\": \"Mario\"}",
            "1000.0",
            "CRUD test complete");

        // Create
        TestResponse createResp = post("/responses",
            """
                {"input": "Test CRUD", "model": "gpt-4o"}
                """);
        Assertions.assertEquals(200, createResp.statusCode(), "Body: " + createResp.body());
        String responseId = MAPPER.readTree(createResp.body()).get("id").asText();
        Assertions.assertTrue(responseId.startsWith("caresp_"), "ID should start with caresp_");

        // Get
        TestResponse getResp = get("/responses/" + responseId);
        Assertions.assertEquals(200, getResp.statusCode());
        Assertions.assertEquals(responseId, MAPPER.readTree(getResp.body()).get("id").asText());

        // List input items
        TestResponse itemsResp = get("/responses/" + responseId + "/input_items");
        Assertions.assertEquals(200, itemsResp.statusCode());

        // Delete (: 200 with deleted-object body)
        TestResponse deleteResp = delete("/responses/" + responseId);
        Assertions.assertEquals(200, deleteResp.statusCode());
        Assertions.assertTrue(MAPPER.readTree(deleteResp.body()).get("deleted").asBoolean());

        // Verify gone
        Assertions.assertEquals(404, get("/responses/" + responseId).statusCode());
    }

    @Test
    @Order(6)
    @DisplayName("Health endpoints work alongside supervisor handler")
    void healthEndpoints() throws Exception {
        Assertions.assertEquals(200, get("/readiness").statusCode());
        Assertions.assertEquals(200, get("/liveness").statusCode());
    }

    @Test
    @Order(7)
    @DisplayName("Instructions field is forwarded as system message to the model")
    void instructionsAreForwarded() throws Exception {
        stubSimpleRouting("getBalance$3", "{\"user\": \"Mario\"}",
            "1000.0",
            "Responded with custom instructions");

        TestResponse response = post("/responses",
            """
                {
                    "input": "Hello",
                    "model": "gpt-4o",
                    "instructions": "You are a helpful financial assistant. Always respond in USD."
                }
                """);

        Assertions.assertEquals(200, response.statusCode(), "Response body: " + response.body());
        JsonNode json = MAPPER.readTree(response.body());
        Assertions.assertTrue(json.has("output"), "Response should have output");
    }

    @Test
    @Order(8)
    @DisplayName("Credit tool: supervisor routes to credit agent which executes credit tool")
    void creditToolExecution() throws Exception {
        stubToolCallingFlow("credit$1", "{\"user\": \"Mario\", \"amount\": 500.0}",
            "credit", "{\"user\": \"Mario\", \"amount\": 500.0}",
            "Successfully credited 500.0 to Mario. New balance: 1500.0 USD",
            "Credited 500 USD to Mario. New balance is 1500.0 USD.");

        TestResponse response = post("/responses",
            """
                {"input": "Credit 500 dollars to Mario's account", "model": "gpt-4o"}
                """);

        Assertions.assertEquals(200, response.statusCode(), "Response body: " + response.body());

        JsonNode json = MAPPER.readTree(response.body());
        String text = json.get("output").get(0).get("content").get(0).get("text").asText();
        Assertions.assertFalse(text.isBlank(), "Response should have text content");
    }

    @Test
    @Order(9)
    @DisplayName("Exchange tool: supervisor routes to exchange agent which executes exchange tool")
    void exchangeToolExecution() throws Exception {
        stubToolCallingFlow("exchange$2",
            "{\"originalCurrency\": \"USD\", \"amount\": 100.0, \"targetCurrency\": \"EUR\"}",
            "exchange",
            "{\"originalCurrency\": \"USD\", \"amount\": 100.0, \"targetCurrency\": \"EUR\"}",
            "90.0",
            "Converted 100 USD to 90.0 EUR.");

        TestResponse response = post("/responses",
            """
                {"input": "Convert 100 USD to EUR", "model": "gpt-4o"}
                """);

        Assertions.assertEquals(200, response.statusCode(), "Response body: " + response.body());
    }

    @Test
    @Order(10)
    @DisplayName("Malformed JSON returns error status")
    void malformedJsonReturnsError() throws Exception {
        TestResponse response = post("/responses", "not valid json");

        Assertions.assertTrue(response.statusCode() >= 400,
            "Expected error status for malformed JSON, got: " + response.statusCode());
    }
}
