// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.agentserver.api.IsolationContext;
import com.microsoft.agentserver.api.PlatformHeaders;
import com.microsoft.agentserver.api.ResponsesProvider;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP-backed implementation of {@link ResponsesProvider} that persists
 * state to the Azure AI Foundry storage API using an Azure Core
 * {@link HttpPipeline} for retry, authentication, telemetry, and tracing.
 * <p>
 * Equivalent to the C# {@code FoundryStorageProvider}.
 */
public final class FoundryStorageProvider implements ResponsesProvider, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FoundryStorageProvider.class);
    private static final String API_VERSION = "v1";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    private static final String FOUNDRY_STORAGE_SCOPE = "https://ai.azure.com/.default";
    private static final ObjectMapper MAPPER = ObjectMapperFactory.getObjectMapper();

    private final HttpPipeline pipeline;
    private final URI storageBaseUri;
    private final ExecutorService ioExecutor;

    /**
     * Creates a new FoundryStorageProvider with a pre-built HttpPipeline and a custom executor
     * for async I/O operations.
     *
     * @param pipeline       the Azure Core HTTP pipeline (includes retry, auth, telemetry)
     * @param storageBaseUri the base URI for the Foundry storage API (e.g. "<a href="https://host/storage/">...</a>")
     * @param ioExecutor     the executor for async I/O operations
     */
    public FoundryStorageProvider(HttpPipeline pipeline, URI storageBaseUri, ExecutorService ioExecutor) {
        this.pipeline = pipeline;
        this.storageBaseUri = storageBaseUri.toString().endsWith("/")
            ? storageBaseUri
            : URI.create(storageBaseUri + "/");
        this.ioExecutor = ioExecutor;
        LOGGER.debug("Initialized FoundryStorageProvider with base URI: {}", this.storageBaseUri);
    }

    /**
     * Creates a new FoundryStorageProvider with a pre-built HttpPipeline and the
     * {@linkplain #defaultIoExecutor() default I/O executor}.
     *
     * @param pipeline       the Azure Core HTTP pipeline (includes retry, auth, telemetry)
     * @param storageBaseUri the base URI for the Foundry storage API (e.g. "https://host/storage/")
     */
    public FoundryStorageProvider(HttpPipeline pipeline, URI storageBaseUri) {
        this(pipeline, storageBaseUri, defaultIoExecutor());
    }

    /**
     * Creates a new FoundryStorageProvider with DefaultAzureCredential and the
     * {@linkplain #defaultIoExecutor() default I/O executor}.
     *
     * @param storageBaseUri the base URI for the Foundry storage API
     */
    public FoundryStorageProvider(URI storageBaseUri) {
        this(storageBaseUri, new DefaultAzureCredentialBuilder().build());
    }

    /**
     * Creates a new FoundryStorageProvider with a custom TokenCredential and the
     * {@linkplain #defaultIoExecutor() default I/O executor}.
     *
     * @param storageBaseUri the base URI for the Foundry storage API
     * @param credential     the Azure token credential for authentication
     */
    public FoundryStorageProvider(URI storageBaseUri, TokenCredential credential) {
        this(buildPipeline(credential), storageBaseUri);
    }

    /**
     * Creates a default {@link ExecutorService} for blocking HTTP I/O operations.
     * <p>
     * Uses a cached thread pool with daemon threads to prevent thread starvation
     * of the {@code ForkJoinPool.commonPool()}, which is designed for CPU-bound work.
     *
     * @return a new cached thread pool with daemon threads named {@code "foundry-storage-io"}
     */
    private static ExecutorService defaultIoExecutor() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "foundry-storage-io");
            t.setDaemon(true);
            return t;
        });
    }

    private static HttpPipeline buildPipeline(TokenCredential credential) {
        return new HttpPipelineBuilder()
            .policies(
                new BearerTokenAuthenticationPolicy(credential, FOUNDRY_STORAGE_SCOPE),
                new RetryPolicy(),
                new HttpLoggingPolicy(new HttpLogOptions()))
            .build();
    }

    // ── Request building ──────────────────────────────────────

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Creates a FoundryStorageProvider from environment variables.
     * Requires FOUNDRY_PROJECT_ENDPOINT to be set.
     * Uses DefaultAzureCredential for authentication (managed identity in hosted environments).
     *
     * @return a configured FoundryStorageProvider
     * @throws IllegalStateException if FOUNDRY_PROJECT_ENDPOINT is not set
     */
    public static FoundryStorageProvider fromEnvironment() {
        String endpoint = System.getenv("FOUNDRY_PROJECT_ENDPOINT");
        if (endpoint == null || endpoint.isBlank()) {
            LOGGER.error("FOUNDRY_PROJECT_ENDPOINT environment variable is not set");
            throw new IllegalStateException(
                "FOUNDRY_PROJECT_ENDPOINT environment variable is required for FoundryStorageProvider.");
        }

        String baseUri = endpoint.endsWith("/") ? endpoint + "storage/" : endpoint + "/storage/";
        LOGGER.debug("Creating FoundryStorageProvider from environment with base URI: {}", baseUri);
        return new FoundryStorageProvider(URI.create(baseUri));
    }

    private String buildUrl(String path, String extraQuery) {
        String base = storageBaseUri.toString() + path + "?api-version=" + API_VERSION;
        if (extraQuery != null && !extraQuery.isEmpty()) {
            base += "&" + extraQuery;
        }
        return base;
    }

    // ── ResponsesProvider implementation ──────────────────────

    private HttpResponse sendRequest(HttpMethod method, String path, String extraQuery, BinaryData body) {
        return sendRequest(method, path, extraQuery, body, null);
    }

    private HttpResponse sendRequest(HttpMethod method, String path, String extraQuery, BinaryData body, IsolationContext isolation) {
        String url = buildUrl(path, extraQuery);
        LOGGER.debug("Sending {} request to {}", method, url);
        HttpRequest request = new HttpRequest(method, url);

        if (body != null) {
            request.setBody(body);
            request.setHeader(HttpHeaderName.CONTENT_TYPE, JSON_CONTENT_TYPE);
        }
        request.setHeader(HttpHeaderName.ACCEPT, "application/json");

        // Apply platform isolation headers (required by Foundry storage API)
        if (isolation != null) {
            if (isolation.userIsolationKey() != null) {
                request.setHeader(HttpHeaderName.fromString(PlatformHeaders.USER_ISOLATION_KEY),
                    isolation.userIsolationKey());
            }
            if (isolation.chatIsolationKey() != null) {
                request.setHeader(HttpHeaderName.fromString(PlatformHeaders.CHAT_ISOLATION_KEY),
                    isolation.chatIsolationKey());
            }
        }

        HttpResponse response = pipeline.sendSync(request, com.azure.core.util.Context.NONE);
        LOGGER.debug("{} {} returned status {}", method, path, response.getStatusCode());
        throwIfError(response);
        return response;
    }

    private void throwIfError(HttpResponse response) {
        int status = response.getStatusCode();
        if (status >= 200 && status < 300) {
            return;
        }
        String responseBody = response.getBodyAsBinaryData() != null
            ? response.getBodyAsBinaryData().toString() : "";
        LOGGER.warn("Foundry storage API error: HTTP {} - {}", status, responseBody);
        if (status == 404) {
            throw new FoundryStorageException("Resource not found", status, responseBody);
        }
        throw new FoundryStorageException(
            "Foundry storage API error: HTTP " + status, status, responseBody);
    }

    @Override
    public CompletableFuture<List<ResponseItem>> getItemsAsync(List<String> itemIds) {
        LOGGER.debug("getItemsAsync called with {} item IDs", itemIds.size());
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectNode body = MAPPER.createObjectNode();
                ArrayNode idsArray = body.putArray("item_ids");
                for (String id : itemIds) {
                    idsArray.add(id);
                }

                try (HttpResponse response = sendRequest(
                    HttpMethod.POST, "items/batch/retrieve", null,
                    BinaryData.fromString(MAPPER.writeValueAsString(body)))) {

                    JsonNode root = MAPPER.readTree(response.getBodyAsBinaryData().toString());
                    List<ResponseItem> result = new ArrayList<>();
                    if (root.isArray()) {
                        for (JsonNode node : root) {
                            if (node.isNull()) {
                                result.add(null);
                            } else {
                                result.add(MAPPER.treeToValue(node, ResponseItem.class));
                            }
                        }
                    }
                    LOGGER.debug("getItemsAsync returning {} items ({} requested)", result.size(), itemIds.size());
                    return result;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to get items from Foundry storage for {} IDs", itemIds.size(), e);
                throw new FoundryStorageException("Failed to get items from Foundry storage", e);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<List<String>> getHistoryItemIdsAsync(
        String previousResponseId,
        String conversationId,
        int limit) {
        LOGGER.debug("getHistoryItemIdsAsync called with previousResponseId={}, conversationId={}, limit={}",
            previousResponseId, conversationId, limit);
        return CompletableFuture.supplyAsync(() -> {
            try {
                StringBuilder query = new StringBuilder("limit=" + limit);
                if (previousResponseId != null && !previousResponseId.isEmpty()) {
                    query.append("&previous_response_id=")
                        .append(URLEncoder.encode(previousResponseId, StandardCharsets.UTF_8));
                }
                if (conversationId != null && !conversationId.isEmpty()) {
                    query.append("&conversation_id=")
                        .append(URLEncoder.encode(conversationId, StandardCharsets.UTF_8));
                }

                // Use the low-level send so we can treat 404 as "no history yet"
                // (a brand-new conversation or response) without logging a warning.
                String url = buildUrl("history/item_ids", query.toString());
                LOGGER.debug("Sending GET request to {}", url);
                HttpRequest request = new HttpRequest(HttpMethod.GET, url);
                request.setHeader(HttpHeaderName.ACCEPT, "application/json");
                try (HttpResponse response = pipeline.sendSync(request, com.azure.core.util.Context.NONE)) {
                    int status = response.getStatusCode();
                    if (status == 404) {
                        LOGGER.debug("No prior history for previousResponseId={} / conversationId={} (404 from storage)",
                            previousResponseId, conversationId);
                        return List.<String>of();
                    }
                    throwIfError(response);
                    JsonNode root = MAPPER.readTree(response.getBodyAsBinaryData().toString());
                    List<String> ids = new ArrayList<>();
                    if (root.isArray()) {
                        for (JsonNode node : root) {
                            ids.add(node.asText());
                        }
                    }
                    LOGGER.debug("getHistoryItemIdsAsync returning {} IDs", ids.size());
                    return ids;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to get history item IDs from Foundry storage", e);
                throw new FoundryStorageException("Failed to get history item IDs from Foundry storage", e);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<Void> saveResponseAsync(
        String responseId,
        Response response,
        String previousResponseId,
        String conversationId) {
        LOGGER.debug("saveResponseAsync called for responseId={}, previousResponseId={}, conversationId={}",
            responseId, previousResponseId, conversationId);
        // Note: input items are included via createResponseAsync; this is now only used
        // as a fallback or for update-only scenarios.
        return createResponseAsync(responseId, response, List.of(), List.of());
    }

    @Override
    public CompletableFuture<Void> saveInputItemsAsync(
        String responseId,
        List<ResponseItem> inputItems) {
        // Input items are now sent as part of the create envelope in createResponseAsync.
        // This method is kept for interface compatibility but is effectively a no-op
        // when called after createResponseAsync has already included the items.
        LOGGER.debug("saveInputItemsAsync called for responseId={} with {} items (no-op, items sent in envelope)",
            responseId, inputItems != null ? inputItems.size() : 0);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Creates a response in Foundry storage using the envelope format.
     * Sends POST /responses with body: {"response": {...}, "input_items": [...], "history_item_ids": [...]}
     * This matches the C# and Python SDK CreateResponseAsync contract.
     *
     * @param responseId     the response ID
     * @param response       the response object
     * @param inputItems     the resolved input items
     * @param historyItemIds the history item IDs from prior conversation turns
     * @return a future that completes when persistence is done
     */
    public CompletableFuture<Void> createResponseAsync(
        String responseId,
        Response response,
        List<ResponseItem> inputItems,
        List<String> historyItemIds) {
        return createResponseAsync(responseId, response, inputItems, historyItemIds, null);
    }

    /**
     * Creates a response in Foundry storage using the envelope format,
     * forwarding platform isolation headers to the storage API.
     *
     * @param responseId     the response ID
     * @param response       the response object
     * @param inputItems     the resolved input items
     * @param historyItemIds the history item IDs from prior conversation turns
     * @param isolation      the isolation context from the inbound request (may be null)
     * @return a future that completes when persistence is done
     */
    public CompletableFuture<Void> createResponseAsync(
        String responseId,
        Response response,
        List<ResponseItem> inputItems,
        List<String> historyItemIds,
        IsolationContext isolation) {
        LOGGER.debug("createResponseAsync called for responseId={} with {} input items and {} history IDs",
            responseId, inputItems != null ? inputItems.size() : 0,
            historyItemIds != null ? historyItemIds.size() : 0);
        return CompletableFuture.runAsync(() -> {
            try {
                // Build the envelope: {"response": {...}, "input_items": [...], "history_item_ids": [...]}
                ObjectNode envelope = MAPPER.createObjectNode();

                // Serialize the response and sanitize it
                JsonNode responseNode = MAPPER.valueToTree(response);
                ObjectNode sanitized = sanitizeResponseForStorage((ObjectNode) responseNode);
                envelope.set("response", sanitized);

                // Serialize input items
                ArrayNode itemsArray = MAPPER.createArrayNode();
                if (inputItems != null) {
                    for (ResponseItem item : inputItems) {
                        JsonNode itemNode = MAPPER.valueToTree(item);
                        itemsArray.add(itemNode);
                    }
                }
                envelope.set("input_items", itemsArray);

                // Serialize history item IDs
                ArrayNode historyArray = MAPPER.createArrayNode();
                if (historyItemIds != null) {
                    for (String id : historyItemIds) {
                        historyArray.add(id);
                    }
                }
                envelope.set("history_item_ids", historyArray);

                String jsonBody = MAPPER.writeValueAsString(envelope);
                LOGGER.debug("Storage request body (create envelope): {}", jsonBody);

                // POST to /responses (no ID in path!) - this is the create endpoint
                try (HttpResponse ignored = sendRequest(HttpMethod.POST, "responses", null,
                    BinaryData.fromString(jsonBody), isolation)) {
                    LOGGER.debug("Successfully created response {} in storage", responseId);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to create response {} in Foundry storage", responseId, e);
                throw new FoundryStorageException("Failed to create response in Foundry storage", e);
            }
        }, ioExecutor);
    }

    // ── Helpers ───────────────────────────────────────────────

    @Override
    public CompletableFuture<Optional<Response>> getResponseAsync(String responseId) {
        return getResponseAsync(responseId, null);
    }

    @Override
    public CompletableFuture<Optional<Response>> getResponseAsync(String responseId, IsolationContext isolation) {
        LOGGER.debug("getResponseAsync called for responseId={}", responseId);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = buildUrl("responses/" + encode(responseId), null);
                HttpRequest request = new HttpRequest(HttpMethod.GET, url);
                request.setHeader(HttpHeaderName.ACCEPT, "application/json");
                applyIsolationHeaders(request, isolation);

                try (HttpResponse response = pipeline.sendSync(request, com.azure.core.util.Context.NONE)) {
                    if (response.getStatusCode() == 404) {
                        LOGGER.debug("Response {} not found in Foundry storage", responseId);
                        return Optional.empty();
                    }
                    throwIfError(response);

                    String json = response.getBodyAsBinaryData().toString();
                    Response result = MAPPER.readValue(json, Response.class);
                    LOGGER.debug("Successfully retrieved response {}", responseId);
                    return Optional.of(result);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to get response {} from Foundry storage", responseId, e);
                throw new FoundryStorageException("Failed to get response from Foundry storage", e);
            }
        }, ioExecutor);
    }

    private static void applyIsolationHeaders(HttpRequest request, IsolationContext isolation) {
        if (isolation == null) {
            return;
        }
        if (isolation.userIsolationKey() != null) {
            request.setHeader(HttpHeaderName.fromString(PlatformHeaders.USER_ISOLATION_KEY),
                isolation.userIsolationKey());
        }
        if (isolation.chatIsolationKey() != null) {
            request.setHeader(HttpHeaderName.fromString(PlatformHeaders.CHAT_ISOLATION_KEY),
                isolation.chatIsolationKey());
        }
    }

    @Override
    public CompletableFuture<Void> deleteResponseAsync(String responseId) {
        LOGGER.debug("deleteResponseAsync called for responseId={}", responseId);
        return CompletableFuture.runAsync(() -> {
            try (HttpResponse ignored = sendRequest(HttpMethod.DELETE, "responses/" + encode(responseId), null, null)) {
                LOGGER.debug("Successfully deleted response {}", responseId);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<List<ResponseItem>> getInputItemsForResponseAsync(String responseId) {
        LOGGER.debug("getInputItemsForResponseAsync called for responseId={}", responseId);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = buildUrl("responses/" + encode(responseId) + "/input_items", null);
                HttpRequest request = new HttpRequest(HttpMethod.GET, url);
                request.setHeader(HttpHeaderName.ACCEPT, "application/json");

                try (HttpResponse response = pipeline.sendSync(request, com.azure.core.util.Context.NONE)) {
                    if (response.getStatusCode() == 404) {
                        LOGGER.debug("Input items for response {} not found", responseId);
                        return List.of();
                    }
                    throwIfError(response);

                    String json = response.getBodyAsBinaryData().toString();
                    JsonNode root = MAPPER.readTree(json);
                    JsonNode dataNode = root.has("data") ? root.get("data") : root;
                    List<ResponseItem> result = new ArrayList<>();
                    if (dataNode.isArray()) {
                        for (JsonNode node : dataNode) {
                            if (!node.isNull()) {
                                result.add(MAPPER.treeToValue(node, ResponseItem.class));
                            }
                        }
                    }
                    LOGGER.debug("getInputItemsForResponseAsync returning {} items for response {}",
                        result.size(), responseId);
                    return result;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to get input items for response {} from Foundry storage", responseId, e);
                throw new FoundryStorageException("Failed to get input items from Foundry storage", e);
            }
        }, ioExecutor);
    }

    // ── Sanitization ─────────────────────────────────────────

    /**
     * Sanitizes a response JSON object for storage API compatibility.
     * The OpenAI Java SDK serializes extra fields (agent_id, created_by, logprobs)
     * and uses floating-point for timestamps. This method produces a body that
     * matches the C#/.NET ResponseObject schema exactly.
     */
    private ObjectNode sanitizeResponseForStorage(ObjectNode node) {
        // Remove fields not in the storage schema
        node.remove("agent_id");
        node.remove("created_by");
        node.remove("logprobs");
        node.remove("service_tier");
        node.remove("metadata");

        // Remove camelCase duplicates (OpenAI Java SDK serializes both snake_case and camelCase)
        node.remove("createdAt");
        node.remove("completedAt");
        node.remove("cancelledAt");
        node.remove("failedAt");
        node.remove("parallelToolCalls");
        node.remove("toolChoice");
        node.remove("previousResponseId");
        node.remove("maxOutputTokens");
        node.remove("incompleteDetails");
        node.remove("topP");

        // Ensure timestamps are integers (SDK may serialize as floats)
        ensureIntTimestamp(node, "created_at");
        ensureIntTimestamp(node, "completed_at");
        ensureIntTimestamp(node, "cancelled_at");
        ensureIntTimestamp(node, "failed_at");

        // Remove fields that should not be present if null (C#/Python omit nulls)
        // Only add these if they have real values; the storage API doesn't expect explicit nulls
        // Storage backend (C# reference impl) writes these as explicit `null` when
        // unset rather than omitting them. Ensure they are always present so
        // the platform deserializer doesn't 500 on a missing required key.
        ensureNullIfAbsent(node, "instructions");
        ensureNullIfAbsent(node, "error");
        ensureNullIfAbsent(node, "incomplete_details");
        // agent_reference is always set by AgentServerResponsesApi.normalizeIdsAndStamp;
        // if it's missing on the way in, default to null to match the
        // C# WriteNull behavior.
        ensureNullIfAbsent(node, "agent_reference");

        // These are truly optional in the C# schema (Optional.IsDefined wraps with no else)
        // — omit when null/missing.
        removeIfNull(node, "previous_response_id");
        removeIfNull(node, "cancelled_at");
        removeIfNull(node, "failed_at");

        // Sanitize output items (remove openai-java-only extras; ensure schema fields)
        if (node.has("output") && node.get("output").isArray()) {
            for (JsonNode outputItem : node.get("output")) {
                if (outputItem.isObject()) {
                    ObjectNode itemObj = (ObjectNode) outputItem;
                    itemObj.remove("created_by");
                    itemObj.remove("logprobs");
                    // Sanitize content parts within output items
                    if (itemObj.has("content") && itemObj.get("content").isArray()) {
                        for (JsonNode contentPart : itemObj.get("content")) {
                            if (contentPart.isObject()) {
                                ObjectNode partObj = (ObjectNode) contentPart;
                                // C# OutputMessageContentOutputTextContent always writes
                                // logprobs as an array (defaults to []). The platform
                                // deserializer 500s when this field is absent.
                                if ("output_text".equals(partObj.path("type").asText())) {
                                    if (!partObj.has("logprobs") || partObj.get("logprobs").isNull()) {
                                        partObj.set("logprobs", MAPPER.createArrayNode());
                                    }
                                    if (!partObj.has("annotations") || partObj.get("annotations").isNull()) {
                                        partObj.set("annotations", MAPPER.createArrayNode());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ensure usage has required sub-objects
        if (node.has("usage") && !node.get("usage").isNull()) {
            ObjectNode usage = (ObjectNode) node.get("usage");
            if (!usage.has("input_tokens_details") || usage.get("input_tokens_details").isNull()) {
                ObjectNode inputDetails = MAPPER.createObjectNode();
                inputDetails.put("cached_tokens", 0);
                usage.set("input_tokens_details", inputDetails);
            }
            if (!usage.has("output_tokens_details") || usage.get("output_tokens_details").isNull()) {
                ObjectNode outputDetails = MAPPER.createObjectNode();
                outputDetails.put("reasoning_tokens", 0);
                usage.set("output_tokens_details", outputDetails);
            }
        }

        return node;
    }

    /**
     * Ensures a timestamp field is an integer (not a float like 1.778771021E9).
     */
    private void ensureIntTimestamp(ObjectNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            JsonNode val = node.get(field);
            if (val.isDouble() || val.isFloat()) {
                node.put(field, val.longValue());
            }
        }
    }

    /**
     * Removes a field from the node if it is null or missing.
     * The storage API expects fields to be omitted rather than explicitly null.
     */
    private void removeIfNull(ObjectNode node, String field) {
        if (node.has(field) && node.get(field).isNull()) {
            node.remove(field);
        }
    }

    /**
     * Ensures the field is present in the object, set to JSON null if it was
     * missing. The C# reference serializer writes these keys as explicit nulls
     * rather than omitting them, and the storage backend deserializer rejects
     * envelopes where they are absent.
     */
    private void ensureNullIfAbsent(ObjectNode node, String field) {
        if (!node.has(field)) {
            node.putNull(field);
        }
    }

    /**
     * Shuts down the I/O executor used for async HTTP operations.
     * Outstanding tasks are allowed to complete, but no new tasks will be accepted.
     */
    @Override
    public void close() {
        ioExecutor.shutdown();
    }
}

