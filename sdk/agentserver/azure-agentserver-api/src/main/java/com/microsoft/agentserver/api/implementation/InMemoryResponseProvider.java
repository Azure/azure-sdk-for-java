// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.implementation;

import com.microsoft.agentserver.api.ResponsesProvider;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe, in-memory implementation of {@link ResponsesProvider}.
 * <p>
 * Stores items as {@link ResponseItem} — a union type that naturally carries
 * role information ({@code ResponseInputMessageItem} for user/system/developer,
 * {@code ResponseOutputMessage} for assistant). No separate tracking of
 * input vs output items is needed.
 * <p>
 * Suitable for single-instance / development use; for production multi-instance
 * deployments, implement a durable backend instead.
 */
public final class InMemoryResponseProvider implements ResponsesProvider {

    /**
     * Default maximum number of responses to retain. When exceeded, the oldest
     * response (by insertion order) is evicted to prevent unbounded memory growth.
     * Set to 0 for unlimited (not recommended in production).
     */
    static final int DEFAULT_MAX_RESPONSES = 10_000;

    private final int maxResponses;

    /**
     * Insertion-order tracker for LRU eviction when {@link #maxResponses} is reached.
     */
    private final ConcurrentLinkedDeque<String> insertionOrder = new ConcurrentLinkedDeque<>();

    /**
     * Item ID → stored item (input or output).
     */
    private final Map<String, ResponseItem> items = new ConcurrentHashMap<>();

    /**
     * Response ID → full Response object.
     */
    private final Map<String, com.openai.models.responses.Response> responses = new ConcurrentHashMap<>();

    /**
     * Response ID → ordered list of item IDs belonging to that response.
     */
    private final Map<String, List<String>> responseItemIds = new ConcurrentHashMap<>();

    /**
     * Response ID → its previous response ID (for chaining).
     */
    private final Map<String, String> responsePreviousId = new ConcurrentHashMap<>();

    /**
     * Conversation ID → ordered list of response IDs in that conversation.
     */
    private final Map<String, List<String>> conversationResponses = new ConcurrentHashMap<>();

    /**
     * Response ID → ordered list of input item IDs for that response.
     */
    private final Map<String, List<String>> responseInputItemIds = new ConcurrentHashMap<>();

    /**
     * Extracts the item ID from a {@link ResponseItem} union type.
     */
    static String extractItemId(ResponseItem item) {
        return ItemConversion.extractItemId(item);
    }

    /**
     * Creates an in-memory provider with the default max-responses cap
     * ({@value #DEFAULT_MAX_RESPONSES}).
     */
    public InMemoryResponseProvider() {
        this(DEFAULT_MAX_RESPONSES);
    }

    /**
     * Creates an in-memory provider with a custom max-responses cap.
     *
     * @param maxResponses the maximum number of responses to retain (0 = unlimited)
     */
    public InMemoryResponseProvider(int maxResponses) {
        this.maxResponses = maxResponses;
    }

    /**
     * Extracts the item ID from a {@link ResponseOutputItem}.
     */
    static String extractOutputItemId(ResponseOutputItem item) {
        return ItemConversion.extractOutputItemId(item);
    }

    @Override
    public CompletableFuture<List<ResponseItem>> getItemsAsync(List<String> itemIds) {
        List<ResponseItem> result = new ArrayList<>(itemIds.size());
        for (String id : itemIds) {
            result.add(items.get(id)); // null if not found, per contract
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<List<String>> getHistoryItemIdsAsync(
        String previousResponseId,
        String conversationId,
        int limit) {

        List<String> allItemIds = new ArrayList<>();

        if (previousResponseId != null && !previousResponseId.isEmpty()) {
            collectResponseChain(previousResponseId, allItemIds, limit);
        } else if (conversationId != null && !conversationId.isEmpty()) {
            List<String> responseIds = conversationResponses.get(conversationId);
            if (responseIds != null) {
                for (String respId : responseIds) {
                    List<String> ids = responseItemIds.get(respId);
                    if (ids != null) {
                        allItemIds.addAll(ids);
                        if (allItemIds.size() >= limit) {
                            break;
                        }
                    }
                }
            }
        }

        if (allItemIds.size() > limit) {
            allItemIds = allItemIds.subList(allItemIds.size() - limit, allItemIds.size());
        }

        return CompletableFuture.completedFuture(Collections.unmodifiableList(allItemIds));
    }

    /**
     * Persists a completed response and its output items.
     * Output items are converted to {@link ResponseItem} for uniform storage.
     */
    public void saveResponse(
        String responseId,
        com.openai.models.responses.Response response,
        String previousResponseId,
        String conversationId) {

        // Track insertion order and evict the oldest when the cap is reached.
        if (maxResponses > 0 && !responses.containsKey(responseId)) {
            insertionOrder.addLast(responseId);
            while (responses.size() >= maxResponses) {
                String oldest = insertionOrder.pollFirst();
                if (oldest != null) {
                    evictResponse(oldest);
                }
            }
        }

        responses.put(responseId, response);

        List<String> itemIdList = new ArrayList<>(response.output().size());

        for (ResponseOutputItem outputItem : response.output()) {
            ResponseItem item = ItemConversion.toResponseItem(outputItem);
            String itemId = extractOutputItemId(outputItem);
            if (item != null && itemId != null) {
                items.put(itemId, item);
                itemIdList.add(itemId);
            }
        }

        responseItemIds.merge(responseId, itemIdList, (existing, newIds) -> {
            // If input items were already stored, append output items after them
            List<String> combined = new ArrayList<>(existing.size() + newIds.size());
            combined.addAll(existing);
            combined.addAll(newIds);
            return combined;
        });

        if (previousResponseId != null && !previousResponseId.isEmpty()) {
            responsePreviousId.put(responseId, previousResponseId);
        }

        if (conversationId != null && !conversationId.isEmpty()) {
            conversationResponses
                .computeIfAbsent(conversationId, k -> new CopyOnWriteArrayList<>())
                .add(responseId);
        }
    }

    @Override
    public CompletableFuture<Void> saveResponseAsync(
        String responseId,
        com.openai.models.responses.Response response,
        String previousResponseId,
        String conversationId) {
        saveResponse(responseId, response, previousResponseId, conversationId);
        return CompletableFuture.completedFuture(null);
    }

    // ── Private helpers ─────────────────────────────────────────

    @Override
    public CompletableFuture<Void> saveInputItemsAsync(
        String responseId,
        List<ResponseItem> inputItems) {
        if (inputItems == null || inputItems.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<String> inputItemIds = new ArrayList<>();
        for (ResponseItem item : inputItems) {
            String itemId = extractItemId(item);
            if (itemId != null) {
                items.put(itemId, item);
                inputItemIds.add(itemId);
            }
        }

        if (!inputItemIds.isEmpty()) {
            // Track input item IDs separately for getInputItemsForResponseAsync
            responseInputItemIds.put(responseId, Collections.unmodifiableList(inputItemIds));

            // Store input item IDs first; output items will be appended by saveResponse
            responseItemIds.merge(responseId, inputItemIds, (existing, newIds) -> {
                List<String> combined = new ArrayList<>(newIds.size() + existing.size());
                combined.addAll(newIds);
                combined.addAll(existing);
                return combined;
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<com.openai.models.responses.Response>> getResponseAsync(String responseId) {
        return CompletableFuture.completedFuture(Optional.ofNullable(responses.get(responseId)));
    }

    @Override
    public CompletableFuture<List<ResponseItem>> getInputItemsForResponseAsync(String responseId) {
        List<String> inputIds = responseInputItemIds.get(responseId);
        if (inputIds == null || inputIds.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        List<ResponseItem> result = new ArrayList<>(inputIds.size());
        for (String id : inputIds) {
            ResponseItem item = items.get(id);
            if (item != null) {
                result.add(item);
            }
        }
        return CompletableFuture.completedFuture(Collections.unmodifiableList(result));
    }

    @Override
    public CompletableFuture<Void> deleteResponseAsync(String responseId) {
        com.openai.models.responses.Response removed = responses.remove(responseId);
        if (removed != null) {
            List<String> itemIds = responseItemIds.remove(responseId);
            if (itemIds != null) {
                for (String itemId : itemIds) {
                    items.remove(itemId);
                }
            }
            responsePreviousId.remove(responseId);
            removed.conversation().ifPresent(conv -> {
                List<String> respIds = conversationResponses.get(conv.id());
                if (respIds != null) {
                    respIds.remove(responseId);
                }
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Evicts a single response and its associated items/metadata from all maps.
     * Used by the cap-based eviction logic in {@link #saveResponse}.
     */
    private void evictResponse(String responseId) {
        com.openai.models.responses.Response removed = responses.remove(responseId);
        if (removed != null) {
            List<String> itemIds = responseItemIds.remove(responseId);
            if (itemIds != null) {
                for (String itemId : itemIds) {
                    items.remove(itemId);
                }
            }
            responseInputItemIds.remove(responseId);
            responsePreviousId.remove(responseId);
            removed.conversation().ifPresent(conv -> {
                List<String> respIds = conversationResponses.get(conv.id());
                if (respIds != null) {
                    respIds.remove(responseId);
                }
            });
        }
    }

    private void collectResponseChain(String responseId, List<String> accumulator, int limit) {
        List<String> chain = new ArrayList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        String current = responseId;
        while (current != null && visited.add(current)) {
            chain.add(current);
            current = responsePreviousId.get(current);
        }
        Collections.reverse(chain);

        for (String respId : chain) {
            List<String> ids = responseItemIds.get(respId);
            if (ids != null) {
                accumulator.addAll(ids);
                if (accumulator.size() >= limit) {
                    return;
                }
            }
        }
    }
}
