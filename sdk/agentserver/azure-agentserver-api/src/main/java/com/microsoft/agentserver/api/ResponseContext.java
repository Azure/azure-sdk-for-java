// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public interface ResponseContext {
    /**
     * Creates a new {@link Builder} for constructing {@link ResponseContext} instances.
     *
     * @return a new builder instance.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the unique response identifier.
     */
    String getResponseId();

    /**
     * Gets whether the server is shutting down.
     * Handlers can use this to distinguish shutdown from explicit cancel or client disconnect.
     */
    boolean isShutdownRequested();

    /**
     * Gets whether this request has been cancelled (e.g., client disconnected).
     * Handlers should poll this periodically during long-running operations
     * and abort gracefully when it returns {@code true}.
     *
     * @return {@code true} if the request has been cancelled.
     */
    boolean isCancelled();

    /**
     * Requests cancellation of this response context.
     * Called by the hosting infrastructure when the client disconnects
     * or an explicit cancel is received.
     */
    void cancel();


    /**
     * Gets the full raw JSON request body.
     * Allows handlers to access custom or extension fields that are not part of the typed model.
     * Returns {@code null} when no raw body is available (e.g., test-constructed contexts).
     */
    JsonNode getRawBody();

    /**
     * Resolves and returns the input items for the current request.
     * Inline items are converted to {@link ResponseOutputItem} instances;
     * item references are resolved via the provider. Results are cached
     * after the first call.
     *
     * @return a future containing the resolved input items.
     */
    CompletableFuture<List<ResponseOutputItem>> getInputItemsAsync();

    /**
     * Resolves and returns the conversation history items for the current request.
     * History is fetched from the provider using {@code previous_response_id} and/or
     * {@code conversation} context. Items are returned as {@link ResponseItem} in
     * ascending (chronological) order, naturally carrying role information.
     * Results are cached after the first call.
     *
     * @return a future containing the resolved history items, or an empty list if no conversation context exists.
     */
    CompletableFuture<List<ResponseItem>> getHistoryAsync();

    /**
     * Gets the platform-injected isolation keys for this request.
     * Handlers use these opaque partition keys to scope user-private and
     * conversation-shared state. Returns {@link IsolationContext#EMPTY}
     * when the platform headers are absent (e.g., local development).
     *
     * @return the isolation context for this request.
     */
    default IsolationContext getIsolation() {
        return IsolationContext.EMPTY;
    }

    /**
     * Gets the forwarded client headers (those prefixed with {@code x-client-})
     * from the original HTTP request.
     *
     * @return an unmodifiable map of client header name → value (may be empty, never null).
     */
    default Map<String, String> getClientHeaders() {
        return Collections.emptyMap();
    }

    /**
     * Gets the query parameters from the original HTTP request.
     *
     * @return an unmodifiable map of parameter name → value (may be empty, never null).
     */
    default Map<String, String> getQueryParameters() {
        return Collections.emptyMap();
    }

    /**
     * Gets the request ID for this request (from the {@code x-request-id} header
     * or auto-generated). Useful for correlation in logs and downstream calls.
     *
     * @return the request ID, or {@code null} if not set.
     */
    default String getRequestId() {
        return null;
    }

    /**
     * Gets the resolved session ID for this request, per the Responses Protocol
     * Spec.
     * <p>
     * Priority chain:
     * <ol>
     *  <li>{@code request.agent_session_id} payload field (client-supplied).</li>
     *  <li>{@code FOUNDRY_AGENT_SESSION_ID} environment variable (platform-supplied
     *  when running in a Foundry hosted container).</li>
     *  <li>Deterministic SHA-256 derivation from agent identity + partition source
     *  ({@code conversation_id} or {@code previous_response_id}), or a random
     *  value when no conversational context exists.</li>
     * </ol>
     * Handlers should prefer this value over reading
     * {@code FoundryEnvironment.SESSION_ID} directly: it is request-scoped (so it
     * stays correct if the hosting model ever supports multiple sessions per
     * container) and honors client-supplied {@code agent_session_id} overrides.
     *
     * @return the resolved session ID (never null/empty when the context was
     * created by the platform; may be {@code null} for test-constructed
     * contexts that did not supply one).
     */
    default String getSessionId() {
        return null;
    }

    /**
     * Builder for constructing {@link ResponseContext} instances.
     * <p>
     * All parameters are set via fluent setters. Required parameters
     * ({@code responseId}, {@code provider}, {@code request}) are validated
     * when {@link #build()} is called.
     *
     * <pre>{@code
     * ResponseContext ctx = ResponseContext.builder()
     *     .responseId(responseId)
     *     .provider(provider)
     *     .request(params)
     *     .rawBody(jsonNode)
     *     .historyLimit(50)
     *     .build();
     * }</pre>
     */
    final class Builder {
        private String responseId;
        private ResponsesProvider provider;
        private ResponseCreateParams.Body request;
        private JsonNode rawBody;
        private Integer historyLimit;
        private IsolationContext isolation;
        private Map<String, String> clientHeaders;
        private Map<String, String> queryParameters;
        private String requestId;
        private String sessionId;

        Builder() {
        }

        /**
         * Sets the unique response identifier (required).
         *
         * @param responseId the response identifier.
         * @return this builder.
         */
        public Builder responseId(String responseId) {
            this.responseId = responseId;
            return this;
        }

        /**
         * Sets the responses provider for resolving item references and history (required).
         *
         * @param provider the responses' provider.
         * @return this builder.
         */
        public Builder provider(ResponsesProvider provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Sets the create-response request body containing input items (required).
         *
         * @param request the request body.
         * @return this builder.
         */
        public Builder request(ResponseCreateParams.Body request) {
            this.request = request;
            return this;
        }

        /**
         * Sets the create-response request, extracting its body (required).
         *
         * @param request the create-response request.
         * @return this builder.
         */
        public Builder request(ResponseCreateParams request) {
            Objects.requireNonNull(request, "request must not be null");
            this.request = request._body();
            return this;
        }

        /**
         * Sets the full raw JSON request body.
         *
         * @param rawBody the raw JSON body, or {@code null} if not available.
         * @return this builder.
         */
        public Builder rawBody(JsonNode rawBody) {
            this.rawBody = rawBody;
            return this;
        }

        /**
         * Sets the maximum number of history items to fetch.
         * Defaults to {@link AgentServerResponseContext#DEFAULT_FETCH_HISTORY_COUNT} if not set.
         *
         * @param historyLimit the maximum number of history items.
         * @return this builder.
         */
        public Builder historyLimit(int historyLimit) {
            this.historyLimit = historyLimit;
            return this;
        }

        /**
         * Sets the platform-injected isolation context (optional).
         * Defaults to {@link IsolationContext#EMPTY} if not set.
         *
         * @param isolation the isolation context.
         * @return this builder.
         */
        public Builder isolation(IsolationContext isolation) {
            this.isolation = isolation;
            return this;
        }

        /**
         * Sets the forwarded client headers (optional).
         * These are headers prefixed with {@code x-client-} from the HTTP request.
         *
         * @param clientHeaders the client headers map.
         * @return this builder.
         */
        public Builder clientHeaders(Map<String, String> clientHeaders) {
            this.clientHeaders = clientHeaders;
            return this;
        }

        /**
         * Sets the query parameters from the HTTP request (optional).
         *
         * @param queryParameters the query parameter map.
         * @return this builder.
         */
        public Builder queryParameters(Map<String, String> queryParameters) {
            this.queryParameters = queryParameters;
            return this;
        }

        /**
         * Sets the request ID for correlation (optional).
         *
         * @param requestId the request ID.
         * @return this builder.
         */
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Sets the resolved session ID for this request (optional).
         * <p>
         * Typically set by the API layer from {@code SessionIdResolver}. Handlers
         * read it back via {@link ResponseContext#getSessionId()}.
         *
         * @param sessionId the resolved session ID, or {@code null} if not available.
         * @return this builder.
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Builds and returns a new {@link ResponseContext} instance.
         *
         * @return the constructed {@link ResponseContext}.
         * @throws NullPointerException if {@code responseId}, {@code provider}, or {@code request} is null.
         */
        public ResponseContext build() {
            Objects.requireNonNull(responseId, "responseId must not be null");
            Objects.requireNonNull(provider, "provider must not be null");
            Objects.requireNonNull(request, "request must not be null");
            return new AgentServerResponseContext(
                responseId, provider, request, rawBody, historyLimit,
                isolation, clientHeaders, queryParameters, requestId, sessionId);
        }
    }
}
