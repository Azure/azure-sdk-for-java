// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.agentserver.api.implementation.IdGenerator;
import com.microsoft.agentserver.api.implementation.ItemConversion;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Enhanced implementation of {@link ResponseContext} that resolves input items
 * and conversation history from the request, using lazy-cached async resolution.
 * Inline items are converted via {@link ItemConversion}; item references are
 * resolved via {@link ResponsesProvider#getItemsAsync}.
 * <p>
 * Accepts either {@link ResponseCreateParams} or {@link ResponseCreateParams.Body}
 * as input — both expose the same API surface ({@code input()},
 * {@code previousResponseId()}, {@code conversation()}, etc.).
 */
final class AgentServerResponseContext implements ResponseContext {

    /**
     * Default maximum number of history items to fetch.
     */
    static final int DEFAULT_FETCH_HISTORY_COUNT = 100;

    private final String responseId;
    private final ResponsesProvider provider;
    private final ResponseCreateParams.Body request;
    private final JsonNode rawBody;
    private final int historyLimit;
    private final IsolationContext isolation;
    private final Map<String, String> clientHeaders;
    private final Map<String, String> queryParameters;
    private final String requestId;
    private final String sessionId;
    // Lazy-cached async results (AtomicReference for thread-safe single-init)
    private final AtomicReference<CompletableFuture<List<ResponseOutputItem>>> inputItemsRef =
        new AtomicReference<>();
    private final AtomicReference<CompletableFuture<List<String>>> historyItemIdsRef =
        new AtomicReference<>();
    private final AtomicReference<CompletableFuture<List<ResponseItem>>> historyRef =
        new AtomicReference<>();
    private volatile boolean shutdownRequested;
    private volatile boolean cancelled;

    /**
     * Initializes a new instance of {@code AgentServerResponseContext}.
     * <p>
     * Use {@link ResponseContext#builder()} to construct instances via the fluent builder API.
     *
     * @param responseId      the unique response identifier.
     * @param provider        the responses provider for resolving item references and history.
     * @param request         the create-response request body containing input items.
     * @param rawBody         the full raw JSON request body, or {@code null} if not available.
     * @param historyLimit    maximum number of history items to fetch, or {@code null} for the default (100).
     * @param isolation       the isolation context, or {@code null} for {@link IsolationContext#EMPTY}.
     * @param clientHeaders   the forwarded client headers, or {@code null} for empty.
     * @param queryParameters the query parameters, or {@code null} for empty.
     * @param requestId       the request ID for correlation, or {@code null}.
     * @param sessionId       the resolved session ID per {@code SessionIdResolver}, or {@code null}.
     */
    AgentServerResponseContext(
        String responseId,
        ResponsesProvider provider,
        ResponseCreateParams.Body request,
        JsonNode rawBody,
        Integer historyLimit,
        IsolationContext isolation,
        Map<String, String> clientHeaders,
        Map<String, String> queryParameters,
        String requestId,
        String sessionId) {
        this.responseId = Objects.requireNonNull(responseId, "responseId must not be null");
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.request = Objects.requireNonNull(request, "request must not be null");
        this.rawBody = rawBody;
        this.historyLimit = historyLimit != null ? historyLimit : DEFAULT_FETCH_HISTORY_COUNT;
        this.isolation = isolation != null ? isolation : IsolationContext.EMPTY;
        this.clientHeaders = clientHeaders != null ? clientHeaders : Collections.emptyMap();
        this.queryParameters = queryParameters != null ? queryParameters : Collections.emptyMap();
        this.requestId = requestId;
        this.sessionId = sessionId;
    }

    /**
     * Backward-compatible constructor without platform headers.
     */
    AgentServerResponseContext(
        String responseId,
        ResponsesProvider provider,
        ResponseCreateParams.Body request,
        JsonNode rawBody,
        Integer historyLimit) {
        this(responseId, provider, request, rawBody, historyLimit, null, null, null, null, null);
    }

    /**
     * Converts a {@link ResponseItem} back to a {@link ResponseOutputItem} for input resolution.
     * <p>
     * Note: {@code FunctionCall} items cannot be converted because
     * {@link ResponseItem#asFunctionCall()} returns {@code ResponseFunctionToolCallItem}
     * (input variant) while {@link ResponseOutputItem#ofFunctionCall} requires
     * {@code ResponseFunctionToolCall} (output variant).
     */
    private static ResponseOutputItem toOutputItem(ResponseItem item) {
        if (item.isResponseOutputMessage()) {
            return ResponseOutputItem.ofMessage(item.asResponseOutputMessage());
        }
        if (item.isFileSearchCall()) {
            return ResponseOutputItem.ofFileSearchCall(item.asFileSearchCall());
        }
        if (item.isWebSearchCall()) {
            return ResponseOutputItem.ofWebSearchCall(item.asWebSearchCall());
        }
        if (item.isComputerCall()) {
            return ResponseOutputItem.ofComputerCall(item.asComputerCall());
        }
        if (item.isReasoning()) {
            return ResponseOutputItem.ofReasoning(item.asReasoning());
        }
        if (item.isCodeInterpreterCall()) {
            return ResponseOutputItem.ofCodeInterpreterCall(item.asCodeInterpreterCall());
        }
        if (item.isShellCall()) {
            return ResponseOutputItem.ofShellCall(item.asShellCall());
        }
        if (item.isShellCallOutput()) {
            return ResponseOutputItem.ofShellCallOutput(item.asShellCallOutput());
        }
        if (item.isApplyPatchCall()) {
            return ResponseOutputItem.ofApplyPatchCall(item.asApplyPatchCall());
        }
        if (item.isApplyPatchCallOutput()) {
            return ResponseOutputItem.ofApplyPatchCallOutput(item.asApplyPatchCallOutput());
        }
        return null;
    }

    /**
     * Lazily initializes and caches a {@link CompletableFuture} in the given
     * {@link AtomicReference}, ensuring at most one computation.
     */
    private static <T> CompletableFuture<T> getOrInit(
        AtomicReference<CompletableFuture<T>> ref,
        java.util.function.Supplier<CompletableFuture<T>> supplier) {
        CompletableFuture<T> existing = ref.get();
        if (existing != null) {
            return existing;
        }
        CompletableFuture<T> created = supplier.get();
        if (ref.compareAndSet(null, created)) {
            return created;
        }
        return ref.get();
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    @Override
    public String getResponseId() {
        return responseId;
    }

    @Override
    public boolean isShutdownRequested() {
        return shutdownRequested;
    }

    /**
     * Sets whether the server is shutting down.
     * This is called by the hosting infrastructure, not by handlers.
     */
    public void setShutdownRequested(boolean shutdownRequested) {
        this.shutdownRequested = shutdownRequested;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public JsonNode getRawBody() {
        return rawBody;
    }

    @Override
    public IsolationContext getIsolation() {
        return isolation;
    }

    @Override
    public Map<String, String> getClientHeaders() {
        return clientHeaders;
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

    /**
     * Package-private accessor for the original request body (needed by API helpers).
     */
    ResponseCreateParams.Body getRequestBody() {
        return request;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    // ── Private resolution methods ──────────────────────────────

    @Override
    public CompletableFuture<List<ResponseOutputItem>> getInputItemsAsync() {
        return getOrInit(inputItemsRef, this::resolveInputItemsAsync);
    }

    @Override
    public CompletableFuture<List<ResponseItem>> getHistoryAsync() {
        return getOrInit(historyRef, this::resolveHistoryAsync);
    }

    /**
     * Gets the cached history item IDs. Used by the orchestrator to pass IDs
     * to the provider without duplicating storage.
     *
     * @return a future containing the resolved history item IDs.
     */
    public CompletableFuture<List<String>> getHistoryItemIdsAsync() {
        return getOrInit(historyItemIdsRef, this::resolveHistoryItemIdsAsync);
    }

    private CompletableFuture<List<ResponseOutputItem>> resolveInputItemsAsync() {
        Optional<ResponseCreateParams.Input> inputOpt = request.input();
        if (inputOpt.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        ResponseCreateParams.Input input = inputOpt.get();

        // If the input is a plain text string, there are no structured items to resolve
        if (input.isText()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        List<ResponseInputItem> items = input.asResponse();
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        IdGenerator idGen = new IdGenerator(IdGenerator.extractPartitionKey(responseId));

        // Separate inline items from item references
        List<ResponseOutputItem> results = new ArrayList<>(items.size());
        List<String> referenceIds = new ArrayList<>();
        List<Integer> referencePositions = new ArrayList<>();

        for (ResponseInputItem item : items) {
            if (item.isItemReference()) {
                referenceIds.add(item.asItemReference().id());
                referencePositions.add(results.size());
                results.add(null); // placeholder — index matches results.size()
            } else {
                ResponseOutputItem output = ItemConversion.toOutputItem(item, idGen);
                if (output != null) {
                    results.add(output);
                }
                // Non-convertible, non-reference items are silently skipped
            }
        }

        // If no references, return the inline results immediately
        if (referenceIds.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.unmodifiableList(results));
        }

        // Batch-resolve references via the provider
        return provider.getItemsAsync(referenceIds)
            .thenApply(resolved -> {
                for (int i = 0; i < referencePositions.size(); i++) {
                    int pos = referencePositions.get(i);
                    if (i < resolved.size() && resolved.get(i) != null) {
                        ResponseOutputItem converted = toOutputItem(resolved.get(i));
                        if (converted != null) {
                            results.set(pos, converted);
                        }
                    }
                }
                // Remove unresolved placeholders (nulls remaining from failed references)
                return results.stream()
                    .filter(Objects::nonNull)
                    .toList();
            });
    }

    // ── Utility ─────────────────────────────────────────────────

    private CompletableFuture<List<String>> resolveHistoryItemIdsAsync() {
        String previousResponseId = request.previousResponseId().orElse(null);
        String conversationId = request.conversation()
            .flatMap(conv -> conv.isId() ? Optional.of(conv.asId()) : Optional.empty())
            .orElse(null);

        if (isNullOrEmpty(previousResponseId) && isNullOrEmpty(conversationId)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return provider.getHistoryItemIdsAsync(previousResponseId, conversationId, historyLimit);
    }

    private CompletableFuture<List<ResponseItem>> resolveHistoryAsync() {
        return getHistoryItemIdsAsync()
            .thenCompose(ids -> {
                if (ids.isEmpty()) {
                    return CompletableFuture.completedFuture(Collections.emptyList());
                }
                return provider.getItemsAsync(ids)
                    .thenApply(items -> items.stream()
                        .filter(Objects::nonNull)
                        .toList());
            });
    }
}



