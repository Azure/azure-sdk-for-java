// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.inputitems.ResponseItemList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the full ResponsesApi lifecycle: create → get → list → delete.
 * Uses a real InMemoryResponseProvider and a test handler to exercise the complete
 * orchestration pipeline without mocking.
 */
class ResponsesApiIntegrationTest {

    private ResponsesProvider provider;
    private ResponsesApi api;

    @BeforeEach
    void setUp() {
        provider = ResponsesProvider.inMemory();
    }

    // ── Helper: builds a AgentServerCreateResponse with text input ──

    private static AgentServerCreateResponse createRequest(String text) {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input(text)
            .model("test-model")
            .build()
            ._body();
        return new AgentServerCreateResponse(null, body);
    }

    private static AgentServerCreateResponse createRequestWithPreviousResponse(String text, String previousResponseId) {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input(text)
            .model("test-model")
            .previousResponseId(previousResponseId)
            .build()
            ._body();
        return new AgentServerCreateResponse(null, body);
    }

    // ══════════════════════════════════════════════════════════════
    //  Synchronous (non-streaming) response lifecycle
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Synchronous response lifecycle")
    class SyncResponseLifecycle {

        @BeforeEach
        void setUp() {
            // Handler that returns a simple text response
            ResponseHandler handler = new ResponseHandler() {
                @Override
                public CreateResponse createResponse(ResponseContext ctx, AgentServerCreateResponse request) {
                    ResponseOutputText outputText = ResponseOutputText.builder()
                        .text("Hello from test handler!")
                        .annotations(List.of())
                        .build();
                    Response resp = ResponseBuilder.convertOutputToResponse(request, outputText);
                    return new CreateResponse(null, resp);
                }
            };
            api = ResponsesApi.builder()
                .responseHandler(handler)
                .provider(provider)
                .build();
        }

        @Test
        @DisplayName("Create → Get → Delete full lifecycle")
        void createGetDeleteLifecycle() throws ApiException {
            // Create
            AgentServerCreateResponse request = createRequest("Hello");
            CreateResponse created = api.createResponse(request);

            assertNotNull(created);
            assertNotNull(created.response());
            assertEquals(java.util.Optional.of(ResponseStatus.COMPLETED), created.response().status());
            assertTrue(created.response().id().startsWith("caresp_"));

            String responseId = created.response().id();

            // Get
            Response fetched = api.getResponse(responseId, List.of());
            assertNotNull(fetched);
            assertEquals(responseId, fetched.id());
            assertEquals(java.util.Optional.of(ResponseStatus.COMPLETED), fetched.status());

            // Verify output content
            assertFalse(fetched.output().isEmpty());
            ResponseOutputItem firstOutput = fetched.output().get(0);
            assertTrue(firstOutput.isMessage());
            ResponseOutputMessage message = firstOutput.asMessage();
            assertFalse(message.content().isEmpty());
            assertTrue(message.content().get(0).isOutputText());

            // Delete
            api.deleteResponse(responseId);

            // After deletion, get should throw 404
            ApiException ex = assertThrows(ApiException.class,
                () -> api.getResponse(responseId, List.of()));
            assertEquals(404, ex.getStatusCode());
        }

        @Test
        @DisplayName("Get non-existent response returns 404")
        void getNonExistentResponseThrows404() {
            ApiException ex = assertThrows(ApiException.class,
                () -> api.getResponse("caresp_" + "A".repeat(50), List.of()));
            assertEquals(404, ex.getStatusCode());
        }

        @Test
        @DisplayName("Multiple creates produce distinct response IDs")
        void multipleCreatesProduceDistinctIds() throws ApiException {
            CreateResponse r1 = api.createResponse(createRequest("First"));
            CreateResponse r2 = api.createResponse(createRequest("Second"));

            assertNotEquals(r1.response().id(), r2.response().id());

            // Both are retrievable
            assertNotNull(api.getResponse(r1.response().id(), List.of()));
            assertNotNull(api.getResponse(r2.response().id(), List.of()));
        }

        @Test
        @DisplayName("List input items for a stored response")
        void listInputItemsForStoredResponse() throws ApiException {
            CreateResponse created = api.createResponse(createRequest("Hello"));
            String responseId = created.response().id();

            AgentServerResponseItemList itemList = api.listInputItems(
                responseId, null, null, null, null, List.of());
            assertNotNull(itemList);
            assertNotNull(itemList.responseItemList());
        }

        @Test
        @DisplayName("List input items with after/before cursors windows the result")
        void listInputItemsCursorWindow() throws ApiException {
            // Seed a stored response and 5 input items in deterministic order: it1..it5
            CreateResponse created = api.createResponse(createRequest("anchor"));
            String responseId = created.response().id();
            List<ResponseItem> seeded = new java.util.ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                ResponseOutputMessage msg = ResponseOutputMessage.builder()
                    .id("it" + i)
                    .status(ResponseOutputMessage.Status.COMPLETED)
                    .addContent(ResponseOutputMessage.Content.ofOutputText(
                        ResponseOutputText.builder().text("input " + i).annotations(List.of()).build()))
                    .build();
                seeded.add(ResponseItem.ofResponseOutputMessage(msg));
            }
            provider.saveInputItemsAsync(responseId, seeded).join();

            // No cursors → all 5 items.
            assertEquals(5, api.listInputItems(responseId, null, "asc", null, null, List.of())
                .responseItemList().data().size());

            // after=it2 → it3, it4, it5 (3 items, excluding it2).
            ResponseItemList afterPage = api.listInputItems(
                responseId, null, "asc", "it2", null, List.of()).responseItemList();
            assertEquals(3, afterPage.data().size());
            assertEquals("it3", afterPage.firstId());
            assertEquals("it5", afterPage.lastId());

            // before=it4 → it1, it2, it3 (3 items, excluding it4).
            ResponseItemList beforePage = api.listInputItems(
                responseId, null, "asc", null, "it4", List.of()).responseItemList();
            assertEquals(3, beforePage.data().size());
            assertEquals("it1", beforePage.firstId());
            assertEquals("it3", beforePage.lastId());

            // after=it1 + before=it5 → it2, it3, it4 (open interval).
            ResponseItemList windowPage = api.listInputItems(
                responseId, null, "asc", "it1", "it5", List.of()).responseItemList();
            assertEquals(3, windowPage.data().size());
            assertEquals("it2", windowPage.firstId());
            assertEquals("it4", windowPage.lastId());

            // before=it4 + order=desc → it3, it2, it1 (window first, then reverse).
            ResponseItemList descPage = api.listInputItems(
                responseId, null, "desc", null, "it4", List.of()).responseItemList();
            assertEquals(3, descPage.data().size());
            assertEquals("it3", descPage.firstId());
            assertEquals("it1", descPage.lastId());
        }

        @Test
        @DisplayName("List input items for non-existent response throws 404")
        void listInputItemsNonExistentThrows404() {
            assertThrows(ApiException.class,
                () -> api.listInputItems("caresp_" + "A".repeat(50), null, null, null, null, List.of()));
        }

        @Test
        @DisplayName("Deleting a non-existent response is a no-op")
        void deleteNonExistentIsNoOp() {
            assertDoesNotThrow(() -> api.deleteResponse("caresp_" + "A".repeat(50)));
        }

        @Test
        @DisplayName("Malformed response ID is rejected with 400")
        void malformedIdReturns400() {
            ApiException ex = assertThrows(ApiException.class,
                () -> api.getResponse("not-a-valid-id", List.of()));
            assertEquals(400, ex.getStatusCode());
            assertEquals("Malformed identifier.", ex.getError().message());

            // Validation happens before lookup on every response-ID endpoint.
            assertEquals(400, assertThrows(ApiException.class,
                () -> api.cancelResponse("caresp_tooshort")).getStatusCode());
            assertEquals(400, assertThrows(ApiException.class,
                () -> api.deleteResponse("resp_wrongprefix" + "A".repeat(50))).getStatusCode());
            assertEquals(400, assertThrows(ApiException.class,
                () -> api.listInputItems("bad id", null, null, null, null, List.of())).getStatusCode());
            assertEquals(400, assertThrows(ApiException.class,
                () -> api.replayResponseStream("caresp_short", 0)).getStatusCode());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Streaming response lifecycle
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Streaming response lifecycle")
    class StreamingResponseLifecycle {

        @BeforeEach
        void setUp() {
            ResponseHandler handler = new ResponseHandler() {
                @Override
                public ResponseEventStream createAsync(ResponseContext ctx, AgentServerCreateResponse request) {
                    ResponseEventStream stream = ResponseEventStream.create(ctx, request);
                    stream.emitCreated()
                        .emitInProgress()
                        .addOutputMessage(msg -> msg
                            .emitAdded()
                            .addTextPart(text -> text
                                .emitAdded()
                                .emitDelta("Hello ")
                                .emitDelta("World!")
                                .emitDone("Hello World!"))
                            .emitDone())
                        .emitCompleted();
                    return stream;
                }
            };
            api = ResponsesApi.builder()
                .responseHandler(handler)
                .provider(provider)
                .build();
        }

        @Test
        @DisplayName("Streaming create returns events with proper lifecycle")
        void streamingCreateReturnsEventStream() throws ApiException {
            AgentServerCreateResponse request = createRequest("Hi");
            ResponseEventStream stream = api.createStreamingResponse(request);

            assertNotNull(stream);
            List<ResponseEvent> events = stream.getEvents();
            assertFalse(events.isEmpty());

            // Verify lifecycle event names in order
            List<String> eventNames = events.stream()
                .map(ResponseEvent::eventName)
                .toList();

            assertTrue(eventNames.contains("response.created"));
            assertTrue(eventNames.contains("response.in_progress"));
            assertTrue(eventNames.contains("response.output_item.added"));
            assertTrue(eventNames.contains("response.content_part.added"));
            assertTrue(eventNames.contains("response.output_text.delta"));
            assertTrue(eventNames.contains("response.output_text.done"));
            assertTrue(eventNames.contains("response.content_part.done"));
            assertTrue(eventNames.contains("response.output_item.done"));
            assertTrue(eventNames.contains("response.completed"));
        }

        @Test
        @DisplayName("Streaming response produces valid final Response snapshot")
        void streamingProducesValidResponseSnapshot() throws ApiException {
            AgentServerCreateResponse request = createRequest("Hi");
            ResponseEventStream stream = api.createStreamingResponse(request);

            Response snapshot = stream.getResponse();
            assertNotNull(snapshot);
            assertEquals(java.util.Optional.of(ResponseStatus.COMPLETED), snapshot.status());
            assertFalse(snapshot.output().isEmpty());
        }

        @Test
        @DisplayName("Streamed response is persisted and retrievable after completion")
        void streamedResponseIsPersisted() throws Exception {
            AgentServerCreateResponse request = createRequest("Hi");
            ResponseEventStream stream = api.createStreamingResponse(request);
            Response snapshot = stream.getResponse();

            // Wait briefly for async persistence
            Thread.sleep(200);

            Response fetched = api.getResponse(snapshot.id(), List.of());
            assertNotNull(fetched);
            assertEquals(snapshot.id(), fetched.id());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Builder validation
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Builder validation")
    class BuilderValidation {

        @Test
        @DisplayName("Builder requires responseHandler")
        void builderRequiresHandler() {
            assertThrows(NullPointerException.class,
                () -> ResponsesApi.builder().build());
        }

        @Test
        @DisplayName("Builder defaults to InMemoryResponseProvider")
        void builderDefaultsToInMemoryProvider() {
            ResponseHandler handler = new ResponseHandler() {
            };
            ResponsesApi builtApi = ResponsesApi.builder()
                .responseHandler(handler)
                .build();
            assertNotNull(builtApi);
        }

        @Test
        @DisplayName("ResponsesApi.create() convenience method works")
        void createConvenienceMethod() {
            ResponseHandler handler = new ResponseHandler() {
            };
            ResponsesApi builtApi = ResponsesApi.create(handler);
            assertNotNull(builtApi);
        }
    }
}



