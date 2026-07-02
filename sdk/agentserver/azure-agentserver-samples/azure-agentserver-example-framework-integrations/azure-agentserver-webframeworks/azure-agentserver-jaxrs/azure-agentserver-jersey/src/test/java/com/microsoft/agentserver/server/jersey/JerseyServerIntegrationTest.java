// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.jersey;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.CreateResponse;
import com.microsoft.agentserver.api.ResponseBuilder;
import com.microsoft.agentserver.api.ResponseContext;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseHandler;
import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.api.ResponsesProvider;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseOutputText;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for the Jersey HTTP server adapter.
 * Boots a real Grizzly HTTP server with the full Jersey stack and makes real HTTP requests
 * using {@link HttpURLConnection} (which properly handles Content-Length for POST bodies).
 * <p>
 * SSE streaming tests are intentionally excluded — the streaming pipeline is fully
 * covered by the core API module tests.
 */
@Timeout(30)
class JerseyServerIntegrationTest {

    private static final String BASE_URI = "http://localhost:19876";
    private static final ObjectMapper MAPPER = ObjectMapperFactory.getObjectMapper();
    /**
     * A well-formed but non-existent response ID (caresp_ + 50 chars) for 404 tests.
     */
    private static final String MISSING_ID = "caresp_" + "A".repeat(50);
    private static HttpServer server;

    /**
     * Simple response wrapper for test assertions.
     */
    record TestResponse(int statusCode, String body, Map<String, List<String>> headers) {
    }

    @BeforeAll
    static void startServer() {
        ResponseHandler echoHandler = new ResponseHandler() {
            @Override
            public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse request) {
                String inputText = request.inputText();
                if (inputText.isEmpty()) inputText = "(no input)";
                ResponseOutputText outputText = ResponseOutputText.builder()
                    .text("Echo: " + inputText)
                    .annotations(List.of())
                    .build();
                Response resp = ResponseBuilder.convertOutputToResponse(request, outputText);
                return new CreateResponse(null, resp);
            }

            @Override
            public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse request) {
                ResponseEventStream stream = ResponseEventStream.create(ctx, request);
                stream.emitCreated()
                    .emitInProgress()
                    .addOutputMessage(msg -> msg
                        .outputItemMessage("Streaming: " + request.inputText()))
                    .emitCompleted();
                return stream;
            }
        };

        ResponsesApi api = ResponsesApi.builder()
            .responseHandler(echoHandler)
            .provider(ResponsesProvider.inMemory())
            .build();

        server = JerseyAgentServerAdaptorService.buildAgent(BASE_URI, api);
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    // ── HTTP helpers using HttpURLConnection ─────────────────────

    private TestResponse post(String path, String jsonBody) throws Exception {
        return post(path, jsonBody, Map.of());
    }

    private TestResponse post(String path, String jsonBody, Map<String, String> extraHeaders) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URI + path).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        extraHeaders.forEach(conn::setRequestProperty);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
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

    private TestResponse postNoBody(String path) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(BASE_URI + path).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setFixedLengthStreamingMode(0);
        conn.setDoOutput(true);
        try (var out = conn.getOutputStream()) {
            out.flush();
        }
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
    //  Non-streaming response creation
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /responses (non-streaming)")
    class CreateResponseEndpoint {

        @Test
        @DisplayName("Returns 200 with valid response JSON")
        void createResponseReturns200() throws Exception {
            TestResponse response = post("/responses",
                "{\"input\": \"Hello, agent!\", \"model\": \"test-model\"}");

            assertEquals(200, response.statusCode());
            JsonNode json = MAPPER.readTree(response.body());
            assertTrue(json.has("id"));
            assertTrue(json.get("id").asText().startsWith("caresp_"));
            assertTrue(json.has("output"));
            assertTrue(json.has("status"));
        }

        @Test
        @DisplayName("Response output is a non-empty array")
        void responseContainsOutput() throws Exception {
            TestResponse response = post("/responses",
                "{\"input\": \"Test message\", \"model\": \"gpt-4o\"}");
            assertEquals(200, response.statusCode());

            JsonNode json = MAPPER.readTree(response.body());
            JsonNode output = json.get("output");
            assertNotNull(output);
            assertTrue(output.isArray());
            assertFalse(output.isEmpty());
        }

        @Test
        @DisplayName("Response includes model field")
        void responseIncludesModel() throws Exception {
            TestResponse response = post("/responses",
                "{\"input\": \"Test\", \"model\": \"gpt-4o\"}");
            assertEquals(200, response.statusCode());

            JsonNode json = MAPPER.readTree(response.body());
            assertTrue(json.has("model"));
        }

        @Test
        @DisplayName("x-agent-response-id header overrides the generated id")
        void responseIdHeaderOverride() throws Exception {
            String custom = "caresp_" + "Z".repeat(50);
            TestResponse response = post("/responses",
                "{\"input\": \"override me\", \"model\": \"test-model\"}",
                Map.of("x-agent-response-id", custom));
            assertEquals(200, response.statusCode());

            JsonNode json = MAPPER.readTree(response.body());
            assertEquals(custom, json.get("id").asText(),
                "the returned response.id must match the x-agent-response-id header");

            // The override survives round-trip through storage.
            TestResponse getResp = get("/responses/" + custom);
            assertEquals(200, getResp.statusCode());
            assertEquals(custom, MAPPER.readTree(getResp.body()).get("id").asText());
        }

        @Test
        @DisplayName("x-agent-session-id response header is echoed; GET on the same id echoes too")
        void sessionIdResponseHeaderEcho() throws Exception {
            // Provide a client-supplied agent_session_id via the request body
            // (Tier 1 of the resolver chain) so the value is deterministic.
            TestResponse createResp = post("/responses",
                "{\"input\": \"sid please\", \"model\": \"test-model\", "
                    + "\"agent_session_id\": \"client-session-42\"}");
            assertEquals(200, createResp.statusCode());

            List<String> created = createResp.headers().get("x-agent-session-id");
            assertNotNull(created, "x-agent-session-id header must be present on create");
            assertTrue(created.contains("client-session-42"),
                "create response must echo the resolved session ID, got: " + created);

            // GET on the stored response must also echo the same header.
            String id = MAPPER.readTree(createResp.body()).get("id").asText();
            TestResponse getResp = get("/responses/" + id);
            assertEquals(200, getResp.statusCode());
            List<String> echoed = getResp.headers().get("x-agent-session-id");
            assertNotNull(echoed, "GET response must echo x-agent-session-id");
            assertTrue(echoed.contains("client-session-42"),
                "GET must echo the stored session ID, got: " + echoed);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  GET / DELETE response endpoints
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET / DELETE /responses/{id}")
    class GetDeleteEndpoints {

        @Test
        @DisplayName("Create then GET retrieves the stored response")
        void createThenGet() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"Store me\", \"model\": \"test-model\"}");
            assertEquals(200, createResp.statusCode());

            String responseId = MAPPER.readTree(createResp.body()).get("id").asText();

            TestResponse getResp = get("/responses/" + responseId);
            assertEquals(200, getResp.statusCode());
            assertEquals(responseId, MAPPER.readTree(getResp.body()).get("id").asText());
        }

        @Test
        @DisplayName("GET non-existent response returns 404")
        void getNonExistentReturns404() throws Exception {
            TestResponse response = get("/responses/" + MISSING_ID);
            assertEquals(404, response.statusCode());
        }

        @Test
        @DisplayName("DELETE response returns 200 with deleted body")
        void deleteReturns200() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"Delete me\", \"model\": \"test-model\"}");
            String responseId = MAPPER.readTree(createResp.body()).get("id").asText();

            TestResponse deleteResp = delete("/responses/" + responseId);
            assertEquals(200, deleteResp.statusCode());
            JsonNode deleted = MAPPER.readTree(deleteResp.body());
            assertEquals(responseId, deleted.get("id").asText());
            assertEquals("response", deleted.get("object").asText());
            assertTrue(deleted.get("deleted").asBoolean());

            TestResponse getResp = get("/responses/" + responseId);
            assertEquals(404, getResp.statusCode());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Input items endpoint
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /responses/{id}/input_items")
    class InputItemsEndpoint {

        @Test
        @DisplayName("List input items for a stored response")
        void listInputItems() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"Items test\", \"model\": \"test-model\"}");
            String responseId = MAPPER.readTree(createResp.body()).get("id").asText();

            TestResponse itemsResp = get("/responses/" + responseId + "/input_items");
            assertEquals(200, itemsResp.statusCode());
            assertTrue(MAPPER.readTree(itemsResp.body()).has("data"));
        }

        @Test
        @DisplayName("List input items for non-existent response returns 404")
        void listItemsNonExistentReturns404() throws Exception {
            TestResponse response = get("/responses/" + MISSING_ID + "/input_items");
            assertEquals(404, response.statusCode());
        }

        @Test
        @DisplayName("Malformed response ID returns 400")
        void malformedIdReturns400() throws Exception {
            // Validated before lookup on GET, DELETE, cancel, input_items, and SSE replay.
            assertEquals(400, get("/responses/not-a-valid-id").statusCode());
            assertEquals(400, delete("/responses/caresp_tooshort").statusCode());
            assertEquals(400, postNoBody("/responses/bad-id/cancel").statusCode());
            assertEquals(400, get("/responses/resp_wrongprefix/input_items").statusCode());
            assertEquals(400, get("/responses/caresp_short?stream=true").statusCode());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Cancel endpoint
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /responses/{id}/cancel")
    class CancelEndpoint {

        @Test
        @DisplayName("Cancel non-existent response returns 404")
        void cancelNonExistentReturns404() throws Exception {
            assertEquals(404, postNoBody("/responses/" + MISSING_ID + "/cancel").statusCode());
        }

        @Test
        @DisplayName("Cancel a synchronous (non-background) response returns 400")
        void cancelSynchronousReturns400() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"sync\", \"model\": \"test-model\"}");
            String responseId = MAPPER.readTree(createResp.body()).get("id").asText();

            TestResponse cancelResp = postNoBody("/responses/" + responseId + "/cancel");
            assertEquals(400, cancelResp.statusCode());
            assertTrue(cancelResp.body().contains("Cannot cancel a synchronous response."),
                "Body should explain synchronous cancel rejection. Was: " + cancelResp.body());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  SSE replay via GET
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /responses/{id}?stream=true (SSE replay)")
    class ReplayEndpoint {

        private String createBackgroundStream() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"replay me\", \"model\": \"test-model\", \"background\": true, \"stream\": true}");
            assertEquals(200, createResp.statusCode());
            // The streaming SSE body carries the response object inside its events.
            // Recover the id from a subsequent GET is unnecessary — parse from the created event.
            JsonNode createdEvent = null;
            for (String block : createResp.body().split("\n\n")) {
                if (block.contains("response.created")) {
                    for (String line : block.split("\n")) {
                        if (line.startsWith("data:")) {
                            createdEvent = MAPPER.readTree(line.substring("data:".length()).trim());
                        }
                    }
                }
            }
            assertNotNull(createdEvent, "stream should contain a response.created event");
            return createdEvent.get("response").get("id").asText();
        }

        @Test
        @DisplayName("Replays stored events for a background streaming response; honours starting_after")
        void replaysBackgroundStream() throws Exception {
            String id = createBackgroundStream();

            // Full replay.
            TestResponse full = get("/responses/" + id + "?stream=true");
            assertEquals(200, full.statusCode());
            String contentType = String.join(",", full.headers().getOrDefault("Content-Type", List.of()));
            assertTrue(contentType.contains("text/event-stream"), "Content-Type was: " + contentType);
            assertTrue(full.body().contains("event: response.created"), "full replay body:\n" + full.body());
            assertTrue(full.body().contains("event: response.completed"));
            assertFalse(full.body().contains("[DONE]"), "replay must not contain [DONE]");

            // starting_after=0 skips the response.created event (sequence_number 0).
            TestResponse after0 = get("/responses/" + id + "?stream=true&starting_after=0");
            assertEquals(200, after0.statusCode());
            assertFalse(after0.body().contains("event: response.created"),
                "starting_after=0 should skip seq 0. Body:\n" + after0.body());
            assertTrue(after0.body().contains("event: response.completed"));
        }

        @Test
        @DisplayName("C4: background+stream response is retrievable as background via JSON GET")
        void c4ResponseIsRetrievableAsBackground() throws Exception {
            String id = createBackgroundStream();

            // After the SSE stream returned, the response must be retrievable via JSON GET,
            // with background=true persisted (so cancel/replay invariants hold).
            TestResponse getResp = get("/responses/" + id);
            assertEquals(200, getResp.statusCode());
            JsonNode node = MAPPER.readTree(getResp.body());
            assertEquals(id, node.get("id").asText());
            assertTrue(node.has("background") && node.get("background").asBoolean(),
                "C4 response must carry background=true. Body: " + getResp.body());
            // Status reached a terminal — completed for our echo handler.
            assertEquals("completed", node.get("status").asText());
        }

        @Test
        @DisplayName("Replay on a non-background streaming response → 400")
        void replayNonBackgroundReturns400() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"sync stream\", \"model\": \"test-model\", \"stream\": true}");
            assertEquals(200, createResp.statusCode());
            // Recover the id from the created event.
            String id = null;
            for (String block : createResp.body().split("\n\n")) {
                if (block.contains("response.created")) {
                    for (String line : block.split("\n")) {
                        if (line.startsWith("data:")) {
                            id = MAPPER.readTree(line.substring("data:".length()).trim())
                                .get("response").get("id").asText();
                        }
                    }
                }
            }
            assertNotNull(id);

            TestResponse replay = get("/responses/" + id + "?stream=true");
            assertEquals(400, replay.statusCode());
            assertTrue(replay.body().contains("background=true"), "Body: " + replay.body());
        }

        @Test
        @DisplayName("Replay on a background non-streaming response → 400")
        void replayNonStreamReturns400() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"bg only\", \"model\": \"test-model\", \"background\": true}");
            assertEquals(200, createResp.statusCode());
            String id = MAPPER.readTree(createResp.body()).get("id").asText();

            TestResponse replay = get("/responses/" + id + "?stream=true");
            assertEquals(400, replay.statusCode());
            assertTrue(replay.body().contains("stream=true"), "Body: " + replay.body());
        }

        @Test
        @DisplayName("Replay on a non-existent response → 404")
        void replayNotFoundReturns404() throws Exception {
            assertEquals(404, get("/responses/" + MISSING_ID + "?stream=true").statusCode());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Background mode
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /responses (background)")
    class BackgroundEndpoint {

        @Test
        @DisplayName("background=true returns 200 with in_progress and becomes retrievable/completed")
        void backgroundReturnsInProgress() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"bg\", \"model\": \"test-model\", \"background\": true}");
            assertEquals(200, createResp.statusCode());

            JsonNode created = MAPPER.readTree(createResp.body());
            String id = created.get("id").asText();
            assertEquals("in_progress", created.get("status").asText());

            // The background response is immediately retrievable and eventually completes.
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
            String status = null;
            while (System.nanoTime() < deadline) {
                TestResponse getResp = get("/responses/" + id);
                assertEquals(200, getResp.statusCode());
                status = MAPPER.readTree(getResp.body()).get("status").asText();
                if ("completed".equals(status)) {
                    break;
                }
                Thread.sleep(20);
            }
            assertEquals("completed", status, "background response should reach completed");
        }

        @Test
        @DisplayName("background=true + store=false returns 400")
        void backgroundStoreFalseReturns400() throws Exception {
            TestResponse resp = post("/responses",
                "{\"input\": \"bg\", \"model\": \"test-model\", \"background\": true, \"store\": false}");
            assertEquals(400, resp.statusCode());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  SSE streaming wire-format contract
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /responses (streaming SSE)")
    class StreamingEndpoint {

        @Test
        @DisplayName("Streaming returns text/event-stream with terminal event and no [DONE] sentinel")
        void streamingWireFormat() throws Exception {
            TestResponse response = post("/responses",
                "{\"input\": \"stream please\", \"model\": \"test-model\", \"stream\": true}");

            assertEquals(200, response.statusCode());

            // SSE Content-Type must declare charset=utf-8.
            String contentType = String.join(",", response.headers()
                .getOrDefault("Content-Type", List.of()));
            assertTrue(contentType.contains("text/event-stream"),
                "Content-Type should be text/event-stream, was: " + contentType);

            String body = response.body();
            // exactly one terminal event ends the stream; no [DONE] sentinel.
            assertTrue(body.contains("event: response.completed")
                    || body.contains("event:response.completed"),
                "Stream should contain a terminal response.completed event. Body:\n" + body);
            assertFalse(body.contains("[DONE]"),
                "Stream must NOT contain a [DONE] sentinel. Body:\n" + body);

            // each event is `event: {type}\ndata: {json}\n\n` — no `id:` line.
            for (String line : body.split("\n")) {
                assertFalse(line.startsWith("id:") || line.startsWith("id :"),
                    "SSE stream must NOT emit an `id:` line. Offending line: " + line);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Health endpoints
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Health probe endpoints")
    class HealthEndpoints {

        @Test
        @DisplayName("GET /readiness returns 200")
        void readinessReturns200() throws Exception {
            assertEquals(200, get("/readiness").statusCode());
        }

        @Test
        @DisplayName("GET /liveness returns 200")
        void livenessReturns200() throws Exception {
            assertEquals(200, get("/liveness").statusCode());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CORS headers
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("CORS filter")
    class CorsHeaders {

        @Test
        @DisplayName("Responses include CORS Allow-Origin header")
        void corsHeadersPresent() throws Exception {
            TestResponse response = get("/readiness");
            assertEquals(200, response.statusCode());
            List<String> origin = response.headers().get("Access-Control-Allow-Origin");
            assertNotNull(origin, "Should have Access-Control-Allow-Origin header");
            assertTrue(origin.contains("*"));
        }

        @Test
        @DisplayName("CORS allows standard methods")
        void corsAllowsMethods() throws Exception {
            TestResponse response = get("/readiness");
            List<String> methods = response.headers().get("Access-Control-Allow-Methods");
            assertNotNull(methods);
            String methodsStr = String.join(",", methods);
            assertTrue(methodsStr.contains("GET"));
            assertTrue(methodsStr.contains("POST"));
            assertTrue(methodsStr.contains("DELETE"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Exception handling
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Exception handling")
    class ExceptionHandling {

        @Test
        @DisplayName("404 for unknown path")
        void unknownPathReturns404() throws Exception {
            assertEquals(404, get("/nonexistent/path").statusCode());
        }

        // Error envelope contract:
        // every HTTP error returns `{ "error": { "message", "type", "code", "param"?,... } }`.

        @Test
        @DisplayName("404 envelope: type=invalid_request_error, code=invalid_request_error, message includes ID")
        void notFoundUsesStructuredEnvelope() throws Exception {
            TestResponse resp = get("/responses/" + MISSING_ID);
            assertEquals(404, resp.statusCode());
            JsonNode error = MAPPER.readTree(resp.body()).get("error");
            assertNotNull(error, "body must wrap an `error` object");
            assertEquals("invalid_request_error", error.get("type").asText());
            assertEquals("invalid_request_error", error.get("code").asText());
            assertTrue(error.get("message").asText().contains(MISSING_ID),
                "message should include the response ID, was: " + error.get("message").asText());
        }

        @Test
        @DisplayName("Malformed path ID envelope: code=invalid_parameters, param=responseId{<value>}")
        void malformedIdUsesStructuredEnvelope() throws Exception {
            TestResponse resp = get("/responses/not-a-valid-id");
            assertEquals(400, resp.statusCode());
            JsonNode error = MAPPER.readTree(resp.body()).get("error");
            assertNotNull(error);
            assertEquals("invalid_request_error", error.get("type").asText());
            assertEquals("invalid_parameters", error.get("code").asText());
            assertEquals("responseId{not-a-valid-id}", error.get("param").asText());
            assertEquals("Malformed identifier.", error.get("message").asText());
        }

        @Test
        @DisplayName("envelope: code=unsupported_parameter, param=background")
        void backgroundStoreFalseUsesStructuredEnvelope() throws Exception {
            TestResponse resp = post("/responses",
                "{\"input\": \"x\", \"model\": \"test\", \"background\": true, \"store\": false}");
            assertEquals(400, resp.statusCode());
            JsonNode error = MAPPER.readTree(resp.body()).get("error");
            assertNotNull(error);
            assertEquals("invalid_request_error", error.get("type").asText());
            assertEquals("unsupported_parameter", error.get("code").asText());
            assertEquals("background", error.get("param").asText());
        }

        @Test
        @DisplayName("Cancel-on-synchronous envelope: code=invalid_request_error, no param")
        void cancelSyncUsesStructuredEnvelope() throws Exception {
            TestResponse createResp = post("/responses",
                "{\"input\": \"sync\", \"model\": \"test-model\"}");
            String id = MAPPER.readTree(createResp.body()).get("id").asText();

            TestResponse cancelResp = postNoBody("/responses/" + id + "/cancel");
            assertEquals(400, cancelResp.statusCode());
            JsonNode error = MAPPER.readTree(cancelResp.body()).get("error");
            assertNotNull(error);
            assertEquals("invalid_request_error", error.get("type").asText());
            assertEquals("invalid_request_error", error.get("code").asText());
            assertEquals("Cannot cancel a synchronous response.", error.get("message").asText());
            assertFalse(error.has("param"), "no param expected on cancel rejections");
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Full lifecycle E2E
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Full lifecycle E2E")
    class FullLifecycle {

        @Test
        @DisplayName("Create → Get → List items → Delete → Verify gone")
        void fullCrudLifecycle() throws Exception {
            // 1. Create
            TestResponse createResp = post("/responses",
                "{\"input\": \"Full lifecycle test\", \"model\": \"test-model\"}");
            assertEquals(200, createResp.statusCode());
            String responseId = MAPPER.readTree(createResp.body()).get("id").asText();

            // 2. Get
            TestResponse getResp = get("/responses/" + responseId);
            assertEquals(200, getResp.statusCode());
            assertEquals(responseId, MAPPER.readTree(getResp.body()).get("id").asText());

            // 3. List input items
            assertEquals(200, get("/responses/" + responseId + "/input_items").statusCode());

            // 4. Delete
            assertEquals(200, delete("/responses/" + responseId).statusCode());

            // 5. Verify gone
            assertEquals(404, get("/responses/" + responseId).statusCode());
        }

        @Test
        @DisplayName("Multiple responses are independently stored")
        void multipleIndependentResponses() throws Exception {
            TestResponse resp1 = post("/responses",
                "{\"input\": \"First\", \"model\": \"test\"}");
            TestResponse resp2 = post("/responses",
                "{\"input\": \"Second\", \"model\": \"test\"}");

            String id1 = MAPPER.readTree(resp1.body()).get("id").asText();
            String id2 = MAPPER.readTree(resp2.body()).get("id").asText();
            assertNotEquals(id1, id2);

            // Both retrievable
            assertEquals(200, get("/responses/" + id1).statusCode());
            assertEquals(200, get("/responses/" + id2).statusCode());

            // Delete one doesn't affect the other
            delete("/responses/" + id1);
            assertEquals(404, get("/responses/" + id1).statusCode());
            assertEquals(200, get("/responses/" + id2).statusCode());
        }
    }
}

