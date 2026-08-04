// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.FoundryStorageProvider;
import com.microsoft.agentserver.api.implementation.InMemoryResponseProvider;
import com.openai.models.responses.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Defines the contract for the OpenAI Responses API as hosted by the
 * Azure AI Foundry agent server infrastructure.
 * <p>
 * This interface is framework-agnostic — it uses only domain types and can be
 * implemented without any dependency on a specific HTTP framework (JAX-RS, Spring, etc.).
 * Framework-specific adapters (e.g., {@code java-agent-server-api-jaxrs}) provide
 * the HTTP transport layer on top of this interface.
 */
public interface ResponsesApi {

    /**
     * Creates a new {@link Builder} for constructing {@link ResponsesApi} instances.
     *
     * @return a new builder instance.
     */
    static Builder builder() {
        return new Builder();
    }

    static ResponsesApi create(ResponseHandler handler) {
        return ResponsesApi.builder().responseHandler(handler).build();
    }

    /**
     * Creates a synchronous (non-streaming) model response.
     *
     * @param createResponse the create-response request
     * @return the completed response
     * @throws ApiException if the request is invalid or processing fails
     */
    CreateResponse createResponse(AgentServerCreateResponse createResponse) throws ApiException;

    /**
     * Creates a synchronous (non-streaming) model response with HTTP request metadata.
     * The metadata (isolation keys, client headers, query parameters, request ID) is
     * propagated to the {@link ResponseContext} available to handlers.
     *
     * @param createResponse the create-response request
     * @param metadata       the HTTP request metadata
     * @return the completed response
     * @throws ApiException if the request is invalid or processing fails
     */
    default CreateResponse createResponse(AgentServerCreateResponse createResponse, RequestMetadata metadata) throws ApiException {
        return createResponse(createResponse);
    }

    /**
     * Creates a streaming model response, returning an event stream that produces
     * Server-Sent Events as the response is generated.
     *
     * @param createResponse the create-response request
     * @return the event stream for streaming delivery
     * @throws ApiException if the request is invalid or processing fails
     */
    ResponseEventStream createStreamingResponse(AgentServerCreateResponse createResponse) throws ApiException;

    /**
     * Creates a streaming model response with HTTP request metadata.
     * The metadata (isolation keys, client headers, query parameters, request ID) is
     * propagated to the {@link ResponseContext} available to handlers.
     *
     * @param createResponse the create-response request
     * @param metadata       the HTTP request metadata
     * @return the event stream for streaming delivery
     * @throws ApiException if the request is invalid or processing fails
     */
    default ResponseEventStream createStreamingResponse(AgentServerCreateResponse createResponse, RequestMetadata metadata) throws ApiException {
        return createStreamingResponse(createResponse);
    }

    /**
     * Retrieves a previously stored response by ID.
     *
     * @param responseId the response identifier
     * @param include    optional list of fields to include
     * @return the stored response
     * @throws ApiException if the response is not found or retrieval fails
     */
    Response getResponse(String responseId, List<String> include) throws ApiException;

    /**
     * Variant that accepts the inbound {@link RequestMetadata} so the API layer
     * can forward platform isolation headers to the storage backend. Required
     * for {@code FoundryStorageProvider}, which partitions stored responses by
     * isolation key — a GET without the matching headers returns 404 even when
     * the response exists.
     *
     * @param responseId the response identifier
     * @param include    optional list of fields to include
     * @param metadata   the inbound request metadata (may be {@link RequestMetadata#EMPTY})
     * @return the stored response
     * @throws ApiException if the response is not found or retrieval fails
     */
    default Response getResponse(String responseId, List<String> include, RequestMetadata metadata) throws ApiException {
        return getResponse(responseId, include);
    }

    /**
     * Cancels a background response that is still queued or in progress.
     * <p>
     * Behaviour follows the API spec:
     * <ul>
     *  <li>If the response does not exist (or was never persisted) → {@link ApiException} 404.</li>
     *  <li>If the response was not created with {@code background=true} → {@link ApiException}
     *  400 ({@code "Cannot cancel a synchronous response."}). The background check happens
     *  first, before status checks.</li>
     *  <li>If the response is already {@code cancelled} → returns it unchanged (idempotent).</li>
     *  <li>If the response is in a terminal state ({@code completed}/{@code failed}/{@code incomplete})
     *  → {@link ApiException} 400 with the corresponding message.</li>
     *  <li>If the response is {@code queued} or {@code in_progress} → it winds down to
     *  {@code cancelled} with its output cleared and the cancelled
     *  {@link Response} is returned.</li>
     * </ul>
     *
     * @param responseId the response identifier
     * @return the cancelled (or already-cancelled) response
     * @throws ApiException with status 404 if not found, or 400 if the response cannot be cancelled
     */
    Response cancelResponse(String responseId) throws ApiException;

    /**
     * Signals that the HTTP client disconnected from a non-background response
     * before processing completed. For non-background responses the
     * implementation winds down to {@code cancelled} (output cleared,
     * cancelled snapshot persisted if {@code store=true}) and prevents the
     * normal terminal persistence from overwriting it. For background responses
     * this is a no-op.
     * <p>
     * Safe to call unconditionally from the adapter on disconnect detection —
     * if no in-flight execution matches the ID, the call is a no-op.
     *
     * @param responseId the in-flight response identifier whose client disconnected
     */
    default void signalClientDisconnected(String responseId) {
        // No-op default keeps the interface backward compatible for custom impls.
    }

    /**
     * Replays the stored SSE event sequence for a background streaming response.
     * Triggered by
     * {@code GET /responses/{id}?stream=true}.
     * <p>
     * Preconditions: the response must have been created with
     * {@code store=true}, {@code background=true} and {@code stream=true}.
     * <ul>
     *  <li>Not found / {@code store=false} → {@link ApiException} 404.</li>
     *  <li>{@code background=false} → {@link ApiException} 400
     *  ({@code "This response cannot be streamed because it was not created with background=true."}).</li>
     *  <li>{@code stream=false} → {@link ApiException} 400
     *  ({@code "This response cannot be streamed because it was not created with stream=true."}).</li>
     * </ul>
     * Otherwise returns the events with {@code sequence_number > startingAfter};
     * if {@code startingAfter} is at or beyond the last sequence number,
     * the returned list is empty.
     *
     * @param responseId    the response identifier
     * @param startingAfter replay only events with a greater sequence number, or {@code null} for all
     * @return the (possibly empty) replay event sequence
     * @throws ApiException 404 if not found/not stored, 400 if not replayable
     */
    ResponseStreamReplay replayResponseStream(String responseId, Integer startingAfter) throws ApiException;

    /**
     * Deletes a previously stored response by ID.
     *
     * @param responseId the response identifier
     * @throws ApiException if deletion fails
     */
    void deleteResponse(String responseId) throws ApiException;

    /**
     * Returns a paginated list of input items for a given response.
     *
     * @param responseId the response identifier
     * @param limit      maximum number of items to return (default 20, range 1-100)
     * @param order      sort order ({@code "asc"} or {@code "desc"})
     * @param after      cursor for forward pagination — return items after this item ID
     * @param before     cursor for backward pagination — return items before this item ID
     * @param include    optional list of fields to include
     * @return the paginated item list
     * @throws ApiException if the response is not found or retrieval fails
     */
    AgentServerResponseItemList listInputItems(
        String responseId,
        Integer limit,
        String order,
        String after,
        String before,
        List<String> include) throws ApiException;

    /**
     * Builder for constructing {@link ResponsesApi} instances backed by
     * {@link AgentServerResponsesApi}.
     * <p>
     * All parameters are set via fluent setters. The {@code responseHandler} is required
     * and validated when {@link #build()} is called. The {@code provider} is optional —
     * if not set, an appropriate provider is resolved automatically based on the
     * hosting environment.
     *
     * <pre>{@code
     * ResponsesApi api = ResponsesApi.builder()
     *     .responseHandler(myHandler)
     *     .provider(myProvider)
     *     .build();
     * }</pre>
     */
    final class Builder {
        private static final Logger LOGGER = LoggerFactory.getLogger(ResponsesApi.class);

        private ResponseHandler responseHandler;
        private ResponsesProvider provider;

        Builder() {
        }

        private static ResponsesProvider resolveProvider() {
            // : when FOUNDRY_HOSTING_ENVIRONMENT is set, the SDK MUST auto-activate
            // the Foundry state provider for durable, multi-instance persistence.
            if (FoundryEnvironment.IS_HOSTED) {
                try {
                    FoundryStorageProvider provider = FoundryStorageProvider.fromEnvironment();
                    LOGGER.info("Hosted environment detected (FOUNDRY_HOSTING_ENVIRONMENT set). "
                        + "Using FoundryStorageProvider.");
                    return provider;
                } catch (RuntimeException e) {
                    // Hosted but FOUNDRY_PROJECT_ENDPOINT is missing/invalid — log and fall
                    // back to in-memory rather than failing startup, so the container can
                    // still serve health probes for diagnosis.
                    LOGGER.error("Failed to initialise FoundryStorageProvider in hosted mode; "
                        + "falling back to InMemoryResponseProvider. Cause: {}", e.getMessage());
                }
            } else {
                LOGGER.debug("Local environment detected. Using InMemoryResponseProvider.");
            }
            return new InMemoryResponseProvider();
        }

        /**
         * Sets the response handler that generates responses (required).
         *
         * @param responseHandler the response handler.
         * @return this builder.
         */
        public Builder responseHandler(ResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
            return this;
        }

        /**
         * Sets the responses provider for state persistence (optional).
         * <p>
         * If not set, a provider is resolved automatically based on the hosting
         * environment (Foundry storage in hosted environments, in-memory locally).
         *
         * @param provider the responses' provider.
         * @return this builder.
         */
        public Builder provider(ResponsesProvider provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Builds and returns a new {@link ResponsesApi} instance.
         *
         * @return the constructed {@link ResponsesApi}.
         * @throws NullPointerException if {@code responseHandler} is null.
         */
        public ResponsesApi build() {
            Objects.requireNonNull(responseHandler, "responseHandler must not be null");
            if (provider == null) {
                provider = resolveProvider();
            }
            return new AgentServerResponsesApi(responseHandler, provider);
        }
    }
}
