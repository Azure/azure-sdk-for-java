// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests that exercise the full pipeline:
 * Request deserialization → Handler → ResponsesApi orchestration → Storage → Retrieval
 * <p>
 * These tests simulate realistic agent scenarios with multiple turns,
 * streaming handlers, error handling, and concurrent request processing.
 */
class EndToEndIntegrationTest {

    private ResponsesProvider provider;

    @BeforeEach
    void setUp() {
        provider = ResponsesProvider.inMemory();
    }

    private static AgentServerCreateResponse createRequest(String text) {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input(text)
            .model("test-model")
            .build()
            ._body();
        return new AgentServerCreateResponse(null, body);
    }

    private static AgentServerCreateResponse createRequestWithPrevious(String text, String previousResponseId) {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input(text)
            .model("test-model")
            .previousResponseId(previousResponseId)
            .build()
            ._body();
        return new AgentServerCreateResponse(null, body);
    }

    // ══════════════════════════════════════════════════════════════
    //  Multi-turn conversation simulation
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Multi-turn conversation E2E")
    class MultiTurnConversation {

        private ResponsesApi api;

        @BeforeEach
        void setUp() {
            // Echo handler: returns the input text as the response
            ResponseHandler echoHandler = new ResponseHandler() {
                @Override
                public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse request) {
                    String inputText = request.inputText();
                    if (inputText.isEmpty()) inputText = "Echo: (structured input)";
                    ResponseOutputText outputText = ResponseOutputText.builder()
                        .text("Echo: " + inputText)
                        .annotations(List.of())
                        .build();
                    Response resp = ResponseBuilder.convertOutputToResponse(request, outputText);
                    return new CreateResponse(null, resp);
                }
            };
            api = ResponsesApi.builder()
                .responseHandler(echoHandler)
                .provider(provider)
                .build();
        }

        @Test
        @DisplayName("Three-turn conversation: create, follow-up, follow-up")
        void threeTurnConversation() throws ApiException {
            // Turn 1
            CreateResponse turn1 = api.createResponse(createRequest("Hello"));
            assertNotNull(turn1.response());
            String turn1Id = turn1.response().id();

            // Turn 2: references turn 1
            CreateResponse turn2 = api.createResponse(
                createRequestWithPrevious("How are you?", turn1Id));
            String turn2Id = turn2.response().id();

            // Turn 3: references turn 2
            CreateResponse turn3 = api.createResponse(
                createRequestWithPrevious("What about tomorrow?", turn2Id));
            String turn3Id = turn3.response().id();

            // All three should be retrievable
            assertNotNull(api.getResponse(turn1Id, List.of()));
            assertNotNull(api.getResponse(turn2Id, List.of()));
            assertNotNull(api.getResponse(turn3Id, List.of()));

            // All IDs should be distinct
            assertNotEquals(turn1Id, turn2Id);
            assertNotEquals(turn2Id, turn3Id);
        }

        @Test
        @DisplayName("Delete middle of conversation chain, endpoints remain accessible")
        void deleteMiddleOfChain() throws ApiException {
            CreateResponse turn1 = api.createResponse(createRequest("First"));
            CreateResponse turn2 = api.createResponse(
                createRequestWithPrevious("Second", turn1.response().id()));
            CreateResponse turn3 = api.createResponse(
                createRequestWithPrevious("Third", turn2.response().id()));

            // Delete the middle response
            api.deleteResponse(turn2.response().id());

            // First and third should still be accessible
            assertNotNull(api.getResponse(turn1.response().id(), List.of()));
            assertNotNull(api.getResponse(turn3.response().id(), List.of()));

            // Middle should be gone
            assertThrows(ApiException.class,
                () -> api.getResponse(turn2.response().id(), List.of()));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Streaming handler E2E
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Streaming handler E2E")
    class StreamingHandlerE2E {

        @Test
        @DisplayName("Streaming handler that emits multiple chunks assembles correct final response")
        void streamingMultiChunkResponse() throws ApiException {
            ResponseHandler chunkingHandler = new ResponseHandler() {
                @Override
                public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse request) {
                    ResponseEventStream stream = ResponseEventStream.create(ctx, request);
                    stream.emitCreated()
                        .emitInProgress()
                        .addOutputMessage(msg -> msg
                            .emitAdded()
                            .addTextPart(text -> {
                                text.emitAdded();
                                // Simulate chunked response
                                String[] chunks = {"The ", "answer ", "is ", "42."};
                                for (String chunk : chunks) {
                                    text.emitDelta(chunk);
                                }
                                text.emitDone("The answer is 42.");
                            })
                            .emitDone())
                        .emitCompleted();
                    return stream;
                }
            };

            ResponsesApi api = ResponsesApi.builder()
                .responseHandler(chunkingHandler)
                .provider(provider)
                .build();

            ResponseEventStream stream = api.createStreamingResponse(createRequest("What is the answer?"));

            // Verify all delta events
            List<String> eventNames = stream.getEvents().stream()
                .map(ResponseEvent::eventName)
                .toList();

            long deltaCount = eventNames.stream()
                .filter("response.output_text.delta"::equals)
                .count();
            assertEquals(4, deltaCount, "Should have 4 text delta events");

            // Final snapshot should have the complete text
            Response snapshot = stream.getResponse();
            assertEquals(java.util.Optional.of(ResponseStatus.COMPLETED), snapshot.status());
            assertEquals(1, snapshot.output().size());
            assertTrue(snapshot.output().get(0).isMessage());
        }

        @Test
        @DisplayName("Streaming handler with function call + message output")
        void streamingFunctionCallAndMessage() throws ApiException {
            ResponseHandler handler = new ResponseHandler() {
                @Override
                public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse request) {
                    ResponseEventStream stream = ResponseEventStream.create(ctx, request);
                    stream.emitCreated()
                        .emitInProgress()
                        // First: function call
                        .addOutputFunctionCall(func -> func
                            .emitAdded("get_time", "call_time_1")
                            .emitArgumentsDelta("{}")
                            .emitArgumentsDone("get_time", "{}")
                            .emitDone())
                        // Second: message with the result
                        .addOutputMessage(msg -> msg
                            .outputItemMessage("The current time is 3:00 PM."))
                        .emitCompleted();
                    return stream;
                }
            };

            ResponsesApi api = ResponsesApi.builder()
                .responseHandler(handler)
                .provider(provider)
                .build();

            ResponseEventStream stream = api.createStreamingResponse(createRequest("What time is it?"));
            Response snapshot = stream.getResponse();

            assertEquals(2, snapshot.output().size());
            assertTrue(snapshot.output().get(0).isFunctionCall());
            assertTrue(snapshot.output().get(1).isMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Error handling E2E
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Error handling E2E")
    class ErrorHandlingE2E {

        @Test
        @DisplayName("Handler that throws exception propagates through API")
        void handlerExceptionPropagates() {
            ResponseHandler failingHandler = new ResponseHandler() {
                @Override
                public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse request) {
                    throw new RuntimeException("Simulated handler failure");
                }
            };

            ResponsesApi api = ResponsesApi.builder()
                .responseHandler(failingHandler)
                .provider(provider)
                .build();

            assertThrows(RuntimeException.class,
                () -> api.createResponse(createRequest("This will fail")));
        }

        @Test
        @DisplayName("Streaming handler that emits failure produces failed response")
        void streamingFailure() throws ApiException {
            ResponseHandler failHandler = new ResponseHandler() {
                @Override
                public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse request) {
                    ResponseEventStream stream = ResponseEventStream.create(ctx, request);
                    stream.emitCreated()
                        .emitInProgress()
                        .emitFailed(com.openai.models.responses.ResponseError.Code.SERVER_ERROR,
                            "Something went wrong");
                    return stream;
                }
            };

            ResponsesApi api = ResponsesApi.builder()
                .responseHandler(failHandler)
                .provider(provider)
                .build();

            ResponseEventStream stream = api.createStreamingResponse(createRequest("Fail please"));
            Response snapshot = stream.getResponse();

            assertEquals(java.util.Optional.of(ResponseStatus.FAILED), snapshot.status());
            assertTrue(snapshot.error().isPresent());
            assertEquals("Something went wrong", snapshot.error().get().message());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Concurrent request processing
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Concurrent request processing")
    class ConcurrentRequests {

        @Test
        @DisplayName("Multiple concurrent sync requests all succeed")
        void concurrentSyncRequests() throws Exception {
            ResponseHandler echoHandler = new ResponseHandler() {
                @Override
                public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse request) {
                    ResponseOutputText outputText = ResponseOutputText.builder()
                        .text("Echo: " + request.inputText())
                        .annotations(List.of())
                        .build();
                    Response resp = ResponseBuilder.convertOutputToResponse(request, outputText);
                    return new CreateResponse(null, resp);
                }
            };

            ResponsesApi api = ResponsesApi.builder()
                .responseHandler(echoHandler)
                .provider(provider)
                .build();

            int numRequests = 20;
            ExecutorService executor = Executors.newFixedThreadPool(4);
            ConcurrentHashMap<String, String> responseIds = new ConcurrentHashMap<>();
            CountDownLatch latch = new CountDownLatch(numRequests);
            CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<>();

            for (int i = 0; i < numRequests; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        CreateResponse resp = api.createResponse(
                            createRequest("Request " + idx));
                        responseIds.put(resp.response().id(), "Request " + idx);
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS));
            assertTrue(errors.isEmpty(), "Got errors: " + errors);
            assertEquals(numRequests, responseIds.size(),
                "All requests should produce unique response IDs");

            executor.shutdown();
        }

        @Test
        @DisplayName("Concurrent streaming requests all complete independently")
        void concurrentStreamingRequests() throws Exception {
            ResponseHandler handler = new ResponseHandler() {
                @Override
                public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse request) {
                    ResponseEventStream stream = ResponseEventStream.create(ctx, request);
                    stream.emitCreated()
                        .emitInProgress()
                        .addOutputMessage(msg -> msg.outputItemMessage("Response for: " + request.inputText()))
                        .emitCompleted();
                    return stream;
                }
            };

            ResponsesApi api = ResponsesApi.builder()
                .responseHandler(handler)
                .provider(provider)
                .build();

            int numRequests = 10;
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CopyOnWriteArrayList<Response> responses = new CopyOnWriteArrayList<>();
            CountDownLatch latch = new CountDownLatch(numRequests);

            for (int i = 0; i < numRequests; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        ResponseEventStream stream = api.createStreamingResponse(
                            createRequest("Stream " + idx));
                        responses.add(stream.getResponse());
                    } catch (ApiException e) {
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS));
            assertEquals(numRequests, responses.size());

            // All should be completed
            for (Response r : responses) {
                assertEquals(java.util.Optional.of(ResponseStatus.COMPLETED), r.status());
            }

            // All IDs should be unique
            long uniqueIds = responses.stream().map(Response::id).distinct().count();
            assertEquals(numRequests, uniqueIds);

            executor.shutdown();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  ResponseBuilder utility
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ResponseBuilder utility")
    class ResponseBuilderTests {

        @Test
        @DisplayName("convertOutputToResponse builds a complete response with all required fields")
        void convertOutputToResponseComplete() {
            AgentServerCreateResponse request = createRequest("Hello");
            ResponseOutputText text = ResponseOutputText.builder()
                .text("World")
                .annotations(List.of())
                .build();

            Response resp = ResponseBuilder.convertOutputToResponse(request, text);
            assertNotNull(resp);
            assertTrue(resp.id().startsWith("caresp_"));
            assertEquals(java.util.Optional.of(ResponseStatus.COMPLETED), resp.status());
            assertEquals(1, resp.output().size());
            assertTrue(resp.output().get(0).isMessage());
            // No conversation id should be invented when the client didn't supply one
            // (Foundry storage rejects responses that reference an unknown conversation).
            assertFalse(resp.conversation().isPresent(),
                "Conversation should be absent when the request did not specify one");
        }

        @Test
        @DisplayName("getModelName extracts model from request or uses default")
        void getModelNameExtractsOrDefaults() {
            AgentServerCreateResponse request = createRequest("Hello");
            String model = ResponseBuilder.getModelName(request);
            // Should be either "test-model" or the default "gpt-4o"
            assertNotNull(model);
            assertFalse(model.isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  HealthApi defaults
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("HealthApi defaults")
    class HealthApiTests {

        @Test
        @DisplayName("Default HealthApi returns true for readiness and liveness")
        void defaultHealthChecks() {
            HealthApi health = new HealthApi() {
            };
            assertTrue(health.isReady());
            assertTrue(health.isAlive());
        }

        @Test
        @DisplayName("Custom HealthApi can override readiness")
        void customHealthCheck() {
            HealthApi health = new HealthApi() {
                @Override
                public boolean isReady() {
                    return false;
                }
            };
            assertFalse(health.isReady());
            assertTrue(health.isAlive()); // Default still true
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  ApiException
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ApiException")
    class ApiExceptionTests {

        @Test
        @DisplayName("ApiException carries status code and structured error body")
        void exceptionCarriesInfo() {
            ApiException ex = new ApiException(404, ApiError.invalidRequest("Not found"));
            assertEquals(404, ex.getStatusCode());
            assertEquals("Not found", ex.getError().message());
            assertEquals("invalid_request_error", ex.getError().type());
            assertEquals("invalid_request_error", ex.getError().code());
            assertTrue(ex.getMessage().contains("404"));
        }

        @Test
        @DisplayName("ApiException with serverError builds a 500-style body")
        void serverErrorBody() {
            ApiException ex = new ApiException(500, ApiError.serverError("boom"));
            assertEquals(500, ex.getStatusCode());
            assertEquals("server_error", ex.getError().type());
            assertEquals("server_error", ex.getError().code());
            assertEquals("boom", ex.getError().message());
        }
    }
}

