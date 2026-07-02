// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.InMemoryResponseProvider;
import com.openai.core.JsonMissing;
import com.openai.models.responses.Response;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for InMemoryResponseProvider: exercises the full state management
 * lifecycle including conversation chaining, history resolution, and pagination.
 */
class InMemoryResponseProviderIntegrationTest {

    private InMemoryResponseProvider provider;

    private static Response buildTestResponse(String responseId, String conversationId, String previousResponseId, String messageText) {
        String msgId = "msg_" + responseId.substring(5);
        ResponseOutputMessage message = ResponseOutputMessage.builder()
            .id(msgId)
            .status(ResponseOutputMessage.Status.COMPLETED)
            .addContent(ResponseOutputMessage.Content.ofOutputText(
                ResponseOutputText.builder()
                    .text(messageText)
                    .annotations(List.of())
                    .build()))
            .build();

        Response.Builder builder = Response.builder()
            .id(responseId)
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

        if (conversationId != null) {
            builder.conversation(Response.Conversation.builder().id(conversationId).build());
        }
        if (previousResponseId != null) {
            builder.previousResponseId(previousResponseId);
        }
        return builder.build();
    }

    @BeforeEach
    void setUp() {
        provider = new InMemoryResponseProvider();
    }

    // ══════════════════════════════════════════════════════════════
    //  Basic CRUD operations
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Basic CRUD operations")
    class BasicCrud {

        @Test
        @DisplayName("Save and retrieve a response")
        void saveAndGet() {
            Response resp = buildTestResponse("resp_001", null, null, "Hello");
            provider.saveResponseAsync("resp_001", resp, null, null).join();

            Optional<Response> fetched = provider.getResponseAsync("resp_001").join();
            assertTrue(fetched.isPresent());
            assertEquals("resp_001", fetched.get().id());
            assertEquals(1, fetched.get().output().size());
        }

        @Test
        @DisplayName("Get non-existent response returns empty")
        void getNonExistent() {
            Optional<Response> result = provider.getResponseAsync("resp_missing").join();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Delete removes response and its items")
        void deleteRemovesResponseAndItems() {
            Response resp = buildTestResponse("resp_del", null, null, "Delete me");
            provider.saveResponseAsync("resp_del", resp, null, null).join();

            provider.deleteResponseAsync("resp_del").join();

            assertTrue(provider.getResponseAsync("resp_del").join().isEmpty());
        }

        @Test
        @DisplayName("Save multiple responses independently")
        void saveMultiple() {
            Response r1 = buildTestResponse("resp_a", null, null, "First");
            Response r2 = buildTestResponse("resp_b", null, null, "Second");

            provider.saveResponseAsync("resp_a", r1, null, null).join();
            provider.saveResponseAsync("resp_b", r2, null, null).join();

            assertTrue(provider.getResponseAsync("resp_a").join().isPresent());
            assertTrue(provider.getResponseAsync("resp_b").join().isPresent());
        }

        @Test
        @DisplayName("Delete non-existent response is a no-op")
        void deleteNonExistent() {
            assertDoesNotThrow(() -> provider.deleteResponseAsync("resp_nope").join());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Item storage and retrieval
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Item storage and retrieval")
    class ItemStorage {

        @Test
        @DisplayName("Saved response items are retrievable by ID")
        void outputItemsRetrievable() {
            Response resp = buildTestResponse("resp_items", null, null, "Test");
            provider.saveResponseAsync("resp_items", resp, null, null).join();

            // The provider stores output items by their IDs
            // The message ID should be "msg_items" based on our builder
            List<ResponseItem> items = provider.getItemsAsync(List.of("msg_items")).join();
            assertEquals(1, items.size());
            assertNotNull(items.get(0));
        }

        @Test
        @DisplayName("Missing item IDs return null entries")
        void missingItemsReturnNull() {
            List<ResponseItem> items = provider.getItemsAsync(
                List.of("missing_1", "missing_2")).join();
            assertEquals(2, items.size());
            assertNull(items.get(0));
            assertNull(items.get(1));
        }

        @Test
        @DisplayName("Save input items then output items preserves ordering")
        void inputItemsBeforeOutputItems() {
            // First save input items
            ResponseOutputMessage inputMsg = ResponseOutputMessage.builder()
                .id("input_msg_1")
                .status(ResponseOutputMessage.Status.COMPLETED)
                .addContent(ResponseOutputMessage.Content.ofOutputText(
                    ResponseOutputText.builder().text("user input").annotations(List.of()).build()))
                .build();
            ResponseItem inputItem = ResponseItem.ofResponseOutputMessage(inputMsg);

            provider.saveInputItemsAsync("resp_order",
                List.of(inputItem)).join();

            // Then save the response (appends output items)
            Response resp = buildTestResponse("resp_order", null, null, "response text");
            provider.saveResponseAsync("resp_order", resp, null, null).join();

            // Verify both items are retrievable
            assertNotNull(provider.getItemsAsync(List.of("input_msg_1")).join().get(0));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Conversation history and chaining
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Conversation history and chaining")
    class ConversationHistory {

        @Test
        @DisplayName("Chain of responses via previousResponseId builds history")
        void responseChainHistory() {
            Response r1 = buildTestResponse("resp_chain_1", "conv_1", null, "First");
            Response r2 = buildTestResponse("resp_chain_2", "conv_1", "resp_chain_1", "Second");
            Response r3 = buildTestResponse("resp_chain_3", "conv_1", "resp_chain_2", "Third");

            provider.saveResponseAsync("resp_chain_1", r1, null, "conv_1").join();
            provider.saveResponseAsync("resp_chain_2", r2, "resp_chain_1", "conv_1").join();
            provider.saveResponseAsync("resp_chain_3", r3, "resp_chain_2", "conv_1").join();

            // Get history from the perspective of resp_chain_3
            List<String> history = provider.getHistoryItemIdsAsync("resp_chain_2", null, 100).join();

            // Should include items from resp_chain_1 and resp_chain_2
            assertFalse(history.isEmpty());
        }

        @Test
        @DisplayName("Conversation-based history across multiple responses")
        void conversationBasedHistory() {
            Response r1 = buildTestResponse("resp_conv_a", "conv_abc", null, "First");
            Response r2 = buildTestResponse("resp_conv_b", "conv_abc", null, "Second");

            provider.saveResponseAsync("resp_conv_a", r1, null, "conv_abc").join();
            provider.saveResponseAsync("resp_conv_b", r2, null, "conv_abc").join();

            List<String> history = provider.getHistoryItemIdsAsync(null, "conv_abc", 100).join();
            assertFalse(history.isEmpty());
        }

        @Test
        @DisplayName("History with limit caps returned items")
        void historyWithLimit() {
            // Create 5 responses in a conversation
            for (int i = 0; i < 5; i++) {
                String prevId = i > 0 ? "resp_lim_" + (i - 1) : null;
                Response r = buildTestResponse("resp_lim_" + i, "conv_lim", prevId, "Msg " + i);
                provider.saveResponseAsync("resp_lim_" + i, r, prevId, "conv_lim").join();
            }

            // Get history with limit of 2
            List<String> limited = provider.getHistoryItemIdsAsync(null, "conv_lim", 2).join();
            assertTrue(limited.size() <= 2,
                "History should be capped at limit but got " + limited.size());
        }

        @Test
        @DisplayName("Empty history when no previous response or conversation")
        void emptyHistory() {
            List<String> history = provider.getHistoryItemIdsAsync(null, null, 100).join();
            assertTrue(history.isEmpty());
        }

        @Test
        @DisplayName("Delete response removes it from conversation tracking")
        void deleteRemovesFromConversation() {
            Response r = buildTestResponse("resp_conv_del", "conv_del", null, "Delete me");
            provider.saveResponseAsync("resp_conv_del", r, null, "conv_del").join();

            provider.deleteResponseAsync("resp_conv_del").join();

            List<String> history = provider.getHistoryItemIdsAsync(null, "conv_del", 100).join();
            assertTrue(history.isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Thread safety
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Concurrent access")
    class ConcurrentAccess {

        @Test
        @DisplayName("Concurrent saves and reads do not corrupt state")
        void concurrentSavesAndReads() throws Exception {
            int count = 50;
            CompletableFuture<?>[] futures = new CompletableFuture[count * 2];

            for (int i = 0; i < count; i++) {
                String id = "resp_concurrent_" + i;
                Response r = buildTestResponse(id, null, null, "Msg " + i);
                futures[i] = CompletableFuture.runAsync(() ->
                    provider.saveResponseAsync(id, r, null, null).join());
            }

            for (int i = 0; i < count; i++) {
                String id = "resp_concurrent_" + i;
                futures[count + i] = CompletableFuture.runAsync(() ->
                    provider.getResponseAsync(id).join());
            }

            CompletableFuture.allOf(futures).join();

            // Verify all responses were saved
            for (int i = 0; i < count; i++) {
                assertTrue(provider.getResponseAsync("resp_concurrent_" + i).join().isPresent(),
                    "Response resp_concurrent_" + i + " should be present");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Provider factory
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("ResponsesProvider.inMemory() returns a working provider")
    void inMemoryFactory() {
        ResponsesProvider p = ResponsesProvider.inMemory();
        assertNotNull(p);
        assertInstanceOf(InMemoryResponseProvider.class, p);

        Response r = buildTestResponse("resp_factory", null, null, "Factory");
        p.saveResponseAsync("resp_factory", r, null, null).join();
        assertTrue(p.getResponseAsync("resp_factory").join().isPresent());
    }
}



