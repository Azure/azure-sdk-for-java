// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.core.JsonMissing;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ToolChoiceOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ResponseContext: input item resolution, history fetching,
 * and caching behavior with a real InMemoryResponseProvider.
 */
class ResponseContextIntegrationTest {

    private ResponsesProvider provider;

    @BeforeEach
    void setUp() {
        provider = ResponsesProvider.inMemory();
    }

    private static Response buildResponse(String id, String convId, String prevId, String text) {
        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .id("msg_" + id.substring(5))
            .status(ResponseOutputMessage.Status.COMPLETED)
            .addContent(ResponseOutputMessage.Content.ofOutputText(
                ResponseOutputText.builder().text(text).annotations(List.of()).build()))
            .build();

        Response.Builder b = Response.builder()
            .id(id)
            .createdAt(System.currentTimeMillis() / 1000.0)
            .addOutput(ResponseOutputItem.ofMessage(message))
            .model("test")
            .parallelToolCalls(false)
            .tools(List.of())
            .status(ResponseStatus.COMPLETED)
            .error(JsonMissing.of())
            .incompleteDetails(JsonMissing.of())
            .instructions(JsonMissing.of())
            .metadata(JsonMissing.of())
            .temperature(JsonMissing.of())
            .topP(JsonMissing.of())
            .toolChoice(ToolChoiceOptions.AUTO);
        if (convId != null) b.conversation(Response.Conversation.builder().id(convId).build());
        if (prevId != null) b.previousResponseId(prevId);
        return b.build();
    }

    @Nested
    @DisplayName("Input item resolution")
    class InputItemResolution {

        @Test
        @DisplayName("Text input returns empty input items list")
        void textInputReturnsEmpty() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("Just a string")
                .model("test")
                .build()
                ._body();

            ResponseContext ctx = ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(provider)
                .request(body)
                .build();

            List<ResponseOutputItem> items = ctx.getInputItemsAsync().join();
            assertTrue(items.isEmpty());
        }

        @Test
        @DisplayName("Structured input items are converted to output items")
        void structuredInputConverted() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input(ResponseCreateParams.Input.ofResponse(List.of(
                    com.openai.models.responses.ResponseInputItem.ofEasyInputMessage(
                        com.openai.models.responses.EasyInputMessage.builder()
                            .role(com.openai.models.responses.EasyInputMessage.Role.USER)
                            .content("What is 2+2?")
                            .build()
                    )
                )))
                .model("test")
                .build()
                ._body();

            ResponseContext ctx = ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(provider)
                .request(body)
                .build();

            List<ResponseOutputItem> items = ctx.getInputItemsAsync().join();
            assertFalse(items.isEmpty());
            assertTrue(items.get(0).isMessage());
        }

        @Test
        @DisplayName("Input items are cached after first resolution")
        void inputItemsCached() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("cached test")
                .model("test")
                .build()
                ._body();

            ResponseContext ctx = ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(provider)
                .request(body)
                .build();

            CompletableFuture<List<ResponseOutputItem>> first = ctx.getInputItemsAsync();
            CompletableFuture<List<ResponseOutputItem>> second = ctx.getInputItemsAsync();

            // Should return the same future instance (cached)
            assertSame(first, second);
        }
    }

    @Nested
    @DisplayName("History resolution")
    class HistoryResolution {

        @Test
        @DisplayName("No previous response or conversation returns empty history")
        void noHistoryContext() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("no history")
                .model("test")
                .build()
                ._body();

            ResponseContext ctx = ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(provider)
                .request(body)
                .build();

            List<ResponseItem> history = ctx.getHistoryAsync().join();
            assertTrue(history.isEmpty());
        }

        @Test
        @DisplayName("History from previous response chain")
        void historyFromPreviousResponse() {
            // Save a previous response
            Response prev = buildResponse("resp_prev_001", "conv_hist", null, "Previous message");
            provider.saveResponseAsync("resp_prev_001", prev, null, "conv_hist").join();

            // Build context that references the previous response
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("follow up")
                .model("test")
                .previousResponseId("resp_prev_001")
                .build()
                ._body();

            ResponseContext ctx = ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(provider)
                .request(body)
                .build();

            List<ResponseItem> history = ctx.getHistoryAsync().join();
            assertFalse(history.isEmpty(), "Should have history from previous response");
        }

        @Test
        @DisplayName("History is cached after first resolution")
        void historyCached() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("cached history")
                .model("test")
                .build()
                ._body();

            ResponseContext ctx = ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(provider)
                .request(body)
                .build();

            CompletableFuture<List<ResponseItem>> first = ctx.getHistoryAsync();
            CompletableFuture<List<ResponseItem>> second = ctx.getHistoryAsync();
            assertSame(first, second);
        }
    }

    @Nested
    @DisplayName("Context builder validation")
    class BuilderValidation {

        @Test
        @DisplayName("Builder requires responseId")
        void requiresResponseId() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("test").model("test").build()._body();

            assertThrows(NullPointerException.class, () ->
                ResponseContext.builder()
                    .provider(provider)
                    .request(body)
                    .build());
        }

        @Test
        @DisplayName("Builder requires provider")
        void requiresProvider() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("test").model("test").build()._body();

            assertThrows(NullPointerException.class, () ->
                ResponseContext.builder()
                    .responseId("resp_test")
                    .request(body)
                    .build());
        }

        @Test
        @DisplayName("Builder requires request")
        void requiresRequest() {
            assertThrows(NullPointerException.class, () ->
                ResponseContext.builder()
                    .responseId("resp_test")
                    .provider(provider)
                    .build());
        }

        @Test
        @DisplayName("Shutdown flag is initially false")
        void shutdownInitiallyFalse() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("test").model("test").build()._body();

            AgentServerResponseContext ctx = (AgentServerResponseContext) ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(provider)
                .request(body)
                .build();

            assertFalse(ctx.isShutdownRequested());
            ctx.setShutdownRequested(true);
            assertTrue(ctx.isShutdownRequested());
        }

        @Test
        @DisplayName("rawBody is null when not provided")
        void rawBodyNullByDefault() {
            ResponseCreateParams.Body body = ResponseCreateParams.builder()
                .input("test").model("test").build()._body();

            ResponseContext ctx = ResponseContext.builder()
                .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
                .provider(provider)
                .request(body)
                .build();

            assertNull(ctx.getRawBody());
        }
    }
}




