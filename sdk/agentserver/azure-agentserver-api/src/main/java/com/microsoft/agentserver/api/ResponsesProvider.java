// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.InMemoryResponseProvider;
import com.openai.models.responses.ResponseItem;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Provides pluggable response state persistence for the Responses API server.
 * <p>
 * Items are stored as {@link ResponseItem}, a union type that naturally carries
 * role information: {@code ResponseInputMessageItem} (user/system/developer) vs
 * {@code ResponseOutputMessage} (assistant), plus tool calls and other item types.
 * <p>
 * When no custom implementation is registered, the SDK provides an in-memory default.
 */
public interface ResponsesProvider {

    static ResponsesProvider inMemory() {
        return new InMemoryResponseProvider();
    }

    /**
     * Retrieves items by their identifiers (batch lookup).
     * Items not found are returned as {@code null} entries.
     *
     * @param itemIds the item identifiers to look up.
     * @return a future containing items matching the requested IDs
     * ({@code null} for missing entries).
     */
    CompletableFuture<List<ResponseItem>> getItemsAsync(List<String> itemIds);

    /**
     * Retrieves the list of history item IDs for a conversation chain,
     * starting from a previous response and/or conversation ID.
     *
     * @param previousResponseId the previous response ID to look up history from, or {@code null}.
     * @param conversationId     the conversation ID to scope history, or {@code null}.
     * @param limit              maximum number of history item IDs to return.
     * @return a future containing an ordered list of history item IDs.
     */
    CompletableFuture<List<String>> getHistoryItemIdsAsync(
        String previousResponseId,
        String conversationId,
        int limit);

    /**
     * Persists a completed response and its output items.
     *
     * @param responseId         the response ID.
     * @param response           the full response object.
     * @param previousResponseId the previous response ID, or {@code null}.
     * @param conversationId     the conversation ID, or {@code null}.
     * @return a future that completes when persistence is done.
     */
    CompletableFuture<Void> saveResponseAsync(
        String responseId,
        com.openai.models.responses.Response response,
        String previousResponseId,
        String conversationId);

    /**
     * Persists the resolved input items for a response, so they appear
     * in conversation history before the output items.
     *
     * @param responseId the response ID these input items belong to.
     * @param inputItems the resolved input items as {@link ResponseItem}s.
     * @return a future that completes when persistence is done.
     */
    default CompletableFuture<Void> saveInputItemsAsync(
        String responseId,
        List<ResponseItem> inputItems) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Creates a response in storage with its input items and history in a single
     * atomic operation (envelope format). This matches the Foundry storage API
     * contract used by the C# and Python SDKs.
     * <p>
     * The default implementation delegates to {@link #saveResponseAsync} for
     * backward compatibility with in-memory providers.
     *
     * @param responseId     the response ID.
     * @param response       the full response object.
     * @param inputItems     the resolved input items.
     * @param historyItemIds the history item IDs from prior conversation turns.
     * @return a future that completes when persistence is done.
     */
    default CompletableFuture<Void> createResponseAsync(
        String responseId,
        com.openai.models.responses.Response response,
        List<ResponseItem> inputItems,
        List<String> historyItemIds) {
        // Default: delegate to the legacy separate-call pattern
        return saveResponseAsync(responseId, response, null, null);
    }

    /**
     * Retrieves the stored input items for a given response.
     * These are the items that were sent as input when the response was created.
     *
     * @param responseId the response ID.
     * @return a future containing the input items, or an empty list if not found.
     */
    default CompletableFuture<List<ResponseItem>> getInputItemsForResponseAsync(String responseId) {
        return CompletableFuture.completedFuture(List.of());
    }

    /**
     * Retrieves a previously stored response by its ID.
     *
     * @param responseId the response ID.
     * @return a future containing the response, or empty if not found.
     */
    CompletableFuture<Optional<com.openai.models.responses.Response>> getResponseAsync(String responseId);

    /**
     * Retrieves a previously stored response by its ID, forwarding the inbound
     * request's platform isolation headers to the backend (required by Foundry
     * storage, which partitions responses by isolation key — a GET without the
     * matching headers returns 404 even though the response exists).
     * <p>
     * Default implementation ignores isolation for in-memory providers.
     *
     * @param responseId the response ID.
     * @param isolation  the inbound isolation context (may be {@code null} for tests / local runs).
     * @return a future containing the response, or empty if not found.
     */
    default CompletableFuture<Optional<com.openai.models.responses.Response>> getResponseAsync(
        String responseId, IsolationContext isolation) {
        return getResponseAsync(responseId);
    }

    /**
     * Deletes a previously stored response by its ID.
     *
     * @param responseId the response ID.
     * @return a future that completes when deletion is done.
     */
    CompletableFuture<Void> deleteResponseAsync(String responseId);
}
