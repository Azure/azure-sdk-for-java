// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.FoundryStorageProvider;
import com.microsoft.agentserver.api.implementation.IdGenerator;
import com.microsoft.agentserver.api.implementation.InMemoryResponseProvider;
import com.microsoft.agentserver.api.implementation.ItemConversion;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ToolChoiceOptions;
import com.openai.models.responses.inputitems.ResponseItemList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of {@link ResponsesApi} for the agent server protocol.
 * <p>
 * Orchestrates request handling by delegating to a {@link ResponseHandler} for response
 * generation, and a {@link ResponsesProvider} for state persistence. Supports both
 * synchronous and streaming response creation.
 * <p>
 * In hosted environments ({@link FoundryEnvironment#IS_HOSTED}), uses
 * {@link FoundryStorageProvider} for persistence; otherwise falls back to
 * {@link InMemoryResponseProvider}.
 * <p>
 * <strong>Threading model:</strong> This class is a synchronous façade over the
 * asynchronous {@link ResponsesProvider}. Methods such as {@link #getResponse},
 * {@link #deleteResponse}, and {@link #listInputItems} block the calling thread
 * via {@link java.util.concurrent.CompletableFuture#join()} until the provider
 * operation completes. Callers should be aware that these methods will block and
 * should not be invoked from async pipelines or reactive threads without offloading.
 */
class AgentServerResponsesApi implements ResponsesApi, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentServerResponsesApi.class);

    private final ResponseHandler responseHandler;
    private final ResponsesProvider provider;

    /**
     * Executor for background ({@code background=true}) response processing. Daemon
     * threads so the JVM is not kept alive by in-flight background work.
     */
    private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "agentserver-background");
        t.setDaemon(true);
        return t;
    });

    /**
     * Process-scoped registry of in-flight background executions.
     * Used to coordinate cancellation with the
     * background finalisation so cancellation always wins.
     */
    private final ExecutionTracker executionTracker = new ExecutionTracker();

    /**
     * In-memory buffer of SSE events for replay via {@code GET /responses/{id}?stream=true}.
     *
     */
    private final EventReplayStore eventReplayStore = new EventReplayStore();

    public AgentServerResponsesApi(ResponseHandler responseHandler, ResponsesProvider provider) {
        this.responseHandler = responseHandler;
        this.provider = provider;
    }

    /**
     * Tracks active background executions by response ID so that cancel/finalise
     * can coordinate.
     */
    static final class ExecutionTracker {
        private final Map<String, Execution> executions = new ConcurrentHashMap<>();

        Execution register(String responseId, boolean background, boolean store) {
            Execution execution = new Execution(background, store);
            executions.put(responseId, execution);
            return execution;
        }

        Execution get(String responseId) {
            return executions.get(responseId);
        }

        void evict(String responseId) {
            executions.remove(responseId);
        }
    }

    /**
     * Coordination state for a single in-flight execution. The mutable flags
     * ({@code cancelRequested}, {@code finalized}) are guarded by the monitor of
     * the {@code Execution} instance itself.
     */
    static final class Execution {
        final boolean background;
        final boolean store;
        boolean cancelRequested;
        boolean finalized;

        Execution(boolean background, boolean store) {
            this.background = background;
            this.store = store;
        }
    }

    private static String extractResponseIdFromMetadata(AgentServerCreateResponse createResponse) {
        try {
            return createResponse.responseCreateParams().metadata()
                .map(meta -> {
                    com.openai.core.JsonValue val = meta._additionalProperties().get("response_id");
                    if (val == null) return null;
                    @SuppressWarnings("unchecked")
                    java.util.Optional<String> str = val.asString();
                    return str.orElse(null);
                })
                .orElse(null);
        } catch (Exception e) {
            LOGGER.debug("Could not extract response_id from metadata", e);
            return null;
        }
    }

    private static String extractResponseItemId(ResponseItem item) {
        return ItemConversion.extractItemId(item);
    }

    /**
     * Resolves the response ID for a new create request, in priority order:
     * <ol>
     *  <li>{@code x-agent-response-id} HTTP header.</li>
     *  <li>{@code metadata.response_id} field on the request body (legacy override).</li>
     *  <li>Freshly generated via {@link IdGenerator#generateResponseId()}.</li>
     * </ol>
     */
    private static String resolveResponseId(AgentServerCreateResponse createResponse, RequestMetadata metadata) {
        if (metadata != null) {
            String override = metadata.getResponseIdOverride();
            if (override != null && !override.isEmpty()) {
                return override;
            }
        }
        String fromBody = extractResponseIdFromMetadata(createResponse);
        if (fromBody != null) {
            return fromBody;
        }
        return new IdGenerator(null).generateResponseId();
    }

    /**
     * Auto-stamps the resolved {@code agent_session_id} onto a
     * {@link Response} by adding it to the additional-properties map. Returns the
     * original instance unchanged when {@code sessionId} is {@code null}/empty.
     */
    private static Response stampSessionId(Response resp, String sessionId) {
        if (resp == null || sessionId == null || sessionId.isEmpty()) {
            return resp;
        }
        return resp.toBuilder()
            .putAdditionalProperty("agent_session_id", com.openai.core.JsonValue.from(sessionId))
            .build();
    }

    /**
     * Combines (response-ID resolution) and (session-ID stamping) and
     * normalizes all child IDs (output message ids, conversation id) to share
     * the resolved response's partition key. The Foundry storage backend routes
     * an envelope to a single shard by partition key, so a mix of partitions in
     * a single envelope causes the storage create to fail with HTTP 500.
     */
    private static Response normalizeIdsAndStamp(Response handlerResp, String responseId, String sessionId, AgentReference agentRef) {
        Response.Builder rebuilt = handlerResp.toBuilder();
        if (!responseId.equals(handlerResp.id())) {
            rebuilt.id(responseId);
        }

        // Re-partition any output message item IDs whose partition doesn't already
        // match the resolved response. The Foundry storage backend routes envelopes
        // by partition key; mixed partitions in a single envelope are rejected.
        // (Streaming already produces aligned IDs; this primarily fixes the sync
        // path where ResponseBuilder generates a fresh partition.)
        String partitionKey;
        try {
            partitionKey = IdGenerator.extractPartitionKey(responseId);
        } catch (RuntimeException e) {
            partitionKey = null;
        }

        if (partitionKey != null) {
            IdGenerator idGen = new IdGenerator(partitionKey);
            List<ResponseOutputItem> normalizedOutput = new ArrayList<>();
            for (ResponseOutputItem item : handlerResp.output()) {
                if (item.isMessage()) {
                    ResponseOutputMessage msg = item.asMessage();
                    if (!sharesPartition(msg.id(), partitionKey)) {
                        normalizedOutput.add(ResponseOutputItem.ofMessage(
                            msg.toBuilder().id(idGen.generateMessageItemId()).build()));
                        continue;
                    }
                }
                normalizedOutput.add(item);
            }
            rebuilt.output(normalizedOutput);
        }

        if (sessionId != null && !sessionId.isEmpty()) {
            rebuilt.putAdditionalProperty(
                "agent_session_id", com.openai.core.JsonValue.from(sessionId));
        }

        // echo the request's agent_reference onto the response so the
        // platform storage backend can correlate the persisted response with the
        // originating agent. Stored as an additional property because the openai
        // Response model has no first-class agent_reference field.
        if (agentRef != null) {
            com.fasterxml.jackson.databind.node.ObjectNode refNode = com.microsoft.agentserver.api.serialization.ObjectMapperFactory
                .getObjectMapper().createObjectNode();
            if (agentRef.type() != null) {
                refNode.put("type", agentRef.type().toString().toLowerCase(java.util.Locale.ROOT));
            }
            if (agentRef.name() != null) {
                refNode.put("name", agentRef.name());
            }
            if (agentRef.version() != null) {
                refNode.put("version", agentRef.version());
            }
            if (agentRef.label() != null) {
                refNode.put("label", agentRef.label());
            }
            rebuilt.putAdditionalProperty("agent_reference", com.openai.core.JsonValue.from(refNode));
        }

        return rebuilt.build();
    }

    private static boolean sharesPartition(String id, String partitionKey) {
        if (id == null) {
            return false;
        }
        try {
            return partitionKey.equals(IdGenerator.extractPartitionKey(id));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Validates a response-ID path parameter before any lookup. Malformed
     * IDs produce HTTP 400 ({@code "Malformed identifier."}) rather than 404.
     *
     * @param responseId the path parameter to validate
     * @throws ApiException 400 if the ID is not a well-formed response ID
     */
    private static void validateResponseId(String responseId) throws ApiException {
        if (!IdGenerator.isValidResponseId(responseId)) {
            // param="responseId{<value>}" so clients can echo the offending value.
            String param = "responseId{" + (responseId == null ? "" : responseId) + "}";
            throw new ApiException(400,
                ApiError.invalidRequest("Malformed identifier.", ApiError.CODE_INVALID_PARAMETERS, param));
        }
    }

    @Override
    public CreateResponse createResponse(AgentServerCreateResponse createResponse) throws ApiException {
        return createResponse(createResponse, RequestMetadata.EMPTY);
    }

    @Override
    public CreateResponse createResponse(AgentServerCreateResponse createResponse, RequestMetadata metadata) throws ApiException {
        LOGGER.debug("createResponse called");

        ResponseCreateParams.Body params = createResponse.responseCreateParams();
        boolean background = params.background().orElse(false);
        boolean store = params.store().orElse(true);

        // background=true requires store=true.
        if (background && !store) {
            throw new ApiException(400, ApiError.invalidRequest("The 'background' parameter requires 'store' to be true.", ApiError.CODE_UNSUPPORTED_PARAMETER, "background"));
        }

        String responseId = resolveResponseId(createResponse, metadata);
        // resolve the session ID once for this request; stamped on every
        // Response object that leaves the API.
        String sessionId = SessionIdResolver.resolve(createResponse, FoundryEnvironment.SESSION_ID);

        ResponseContext context = buildContext(responseId, sessionId, createResponse, metadata);

        // : emit the spec-required invoke_agent {model} span around handler
        // execution. The span scopes everything the handler does so per-tool /
        // per-LLM child spans (when emitted) attach to it.
        io.opentelemetry.api.trace.Span span = Observability.startInvokeAgentSpan(
            extractModelName(params),
            responseId,
            extractConversationId(params),
            createResponse.agent() != null ? createResponse.agent().name() : null,
            createResponse.agent() != null ? createResponse.agent().version() : null,
            false);
        try (io.opentelemetry.context.Scope ignored = span.makeCurrent()) {
            if (background) {
                CreateResponse bg = createBackgroundResponse(responseId, sessionId, context, createResponse);
                return bg;
            }

            CreateResponse response;
            try {
                response = responseHandler.createResponse(context, createResponse);
            } catch (RuntimeException e) {
                String code = e.getClass().getSimpleName();
                String msg = e.getMessage();
                if (e.getCause() instanceof ApiException ae) {
                    code = ae.getError() != null ? ae.getError().code() : code;
                    msg = ae.getMessage();
                }
                Observability.recordInvokeAgentError(span, code, msg, e);
                throw e;
            }

            // ensure the resolved response ID (which may have come from the
            // x-agent-response-id header or `metadata.response_id`) is the one that
            // both the client sees and storage uses — even when handlers build their
            // own ID internally via ResponseBuilder. Also stamp the session ID,
            // and re-partition child IDs (msg_, conv_) to match the resolved response's
            // partition key (required by Foundry storage so the envelope routes to a
            // single shard).
            Response handlerResp = response.response();
            if (handlerResp != null) {
                handlerResp = normalizeIdsAndStamp(handlerResp, responseId, sessionId, createResponse.agent());
                response = new CreateResponse(response.agent(), handlerResp);
            }

            persistResponse(context, response);

            return response;
        } finally {
            span.end();
        }
    }

    /**
     * Handles {@code background=true}, non-streaming creation (matrix C3 / Rules
     * ). Persists an initial {@code in_progress} snapshot so the
     * response is immediately retrievable via GET, returns that snapshot to the
     * caller right away, and processes the handler asynchronously, persisting the
     * terminal result when done. Cancellation always wins.
     */
    private CreateResponse createBackgroundResponse(
        String responseId, String sessionId, ResponseContext context, AgentServerCreateResponse createResponse) {

        // Phase 1: persist the in_progress snapshot (with input items) so GET works
        // while processing runs in the background. stamp the resolved session ID.
        Response initial = stampSessionId(
            buildInitialResponse(responseId, createResponse, ResponseStatus.IN_PROGRESS),
            sessionId);
        persistStreamingResponse(context, initial);

        Execution execution = executionTracker.register(responseId, true, true);
        final String finalResponseId = responseId;
        final String finalSessionId = sessionId;

        backgroundExecutor.submit(() -> {
            try {
                CreateResponse result = responseHandler.createResponse(context, createResponse);
                // Re-stamp the terminal response with the response ID we already handed
                // to the client (Phase 1), the background flag, and the session ID, so
                // GET on that ID observes a coherent completed background response.
                Response terminal = result.response();
                if (terminal != null) {
                    Response.Builder rebuilt = terminal.toBuilder().background(true);
                    if (!finalResponseId.equals(terminal.id())) {
                        rebuilt.id(finalResponseId);
                    }
                    rebuilt.putAdditionalProperty(
                        "agent_session_id", com.openai.core.JsonValue.from(finalSessionId));
                    terminal = rebuilt.build();
                }
                final Response finalTerminal = terminal;
                synchronized (execution) {
                    if (!execution.cancelRequested && !execution.finalized && finalTerminal != null) {
                        persistStreamingResponse(context, finalTerminal);
                        execution.finalized = true;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Background response {} processing failed", finalResponseId, e);
                synchronized (execution) {
                    if (!execution.cancelRequested && !execution.finalized) {
                        Response failed = stampSessionId(
                            buildInitialResponse(finalResponseId, createResponse, ResponseStatus.FAILED),
                            finalSessionId);
                        persistStreamingResponse(context, failed);
                        execution.finalized = true;
                    }
                }
            } finally {
                executionTracker.evict(finalResponseId);
            }
        });

        // return immediately with the in_progress snapshot.
        return new CreateResponse(null, initial);
    }

    /**
     * Builds a minimal {@link Response} snapshot for a background response in the
     * given lifecycle {@code status} (used for the initial {@code in_progress}
     * persistence and for failure fallback).
     */
    private Response buildInitialResponse(
        String responseId, AgentServerCreateResponse createResponse, ResponseStatus status) {

        ResponseCreateParams.Body params = createResponse.responseCreateParams();

        String conversationId = params.conversation()
            .flatMap(conv -> {
                if (conv.isId()) {
                    return Optional.of(conv.asId());
                }
                if (conv.isResponseConversationParam()) {
                    return Optional.of(conv.asResponseConversationParam().id());
                }
                return Optional.<String>empty();
            })
            .orElse(null);

        Response.Builder builder = Response.builder()
            .id(responseId)
            .createdAt(System.currentTimeMillis() / 1000.0)
            .status(status)
            .background(true)
            .output(List.of())
            .error(Optional.empty())
            .incompleteDetails(Optional.empty())
            .instructions(Optional.empty())
            .metadata(Optional.empty())
            .parallelToolCalls(false)
            .temperature(Optional.empty())
            .toolChoice(ToolChoiceOptions.AUTO)
            .tools(List.of())
            .topP(Optional.empty())
            .previousResponseId(params.previousResponseId().orElse(null));

        if (params.model().isPresent()) {
            params.model().ifPresent(builder::model);
        } else {
            builder.model(FoundryEnvironment.AGENT_NAME != null ? FoundryEnvironment.AGENT_NAME : "unknown-agent");
        }

        if (conversationId != null) {
            builder.conversation(Response.Conversation.builder().id(conversationId).build());
        }

        return builder.build();
    }

    @Override
    public ResponseEventStream createStreamingResponse(AgentServerCreateResponse createResponse) throws ApiException {
        return createStreamingResponse(createResponse, RequestMetadata.EMPTY);
    }

    @Override
    public ResponseEventStream createStreamingResponse(AgentServerCreateResponse createResponse, RequestMetadata metadata) throws ApiException {
        LOGGER.debug("createStreamingResponse called");

        ResponseCreateParams.Body params = createResponse.responseCreateParams();
        boolean background = params.background().orElse(false);
        boolean store = params.store().orElse(true);

        // background=true requires store=true (matrix C8).
        if (background && !store) {
            throw new ApiException(400, ApiError.invalidRequest("The 'background' parameter requires 'store' to be true.", ApiError.CODE_UNSUPPORTED_PARAMETER, "background"));
        }

        String responseId = resolveResponseId(createResponse, metadata);
        // resolve the session ID once for this request.
        String sessionId = SessionIdResolver.resolve(createResponse, FoundryEnvironment.SESSION_ID);

        ResponseContext context = buildContext(responseId, sessionId, createResponse, metadata);

        // : emit the spec-required invoke_agent {model} span. For streaming the
        // span lives until the stream terminates (completion or failure), so the
        // handler's tool/LLM child spans (when emitted) attach to it.
        io.opentelemetry.api.trace.Span invokeSpan = Observability.startInvokeAgentSpan(
            extractModelName(params),
            responseId,
            extractConversationId(params),
            createResponse.agent() != null ? createResponse.agent().name() : null,
            createResponse.agent() != null ? createResponse.agent().version() : null,
            true);
        ResponseEventStream stream;
        try (io.opentelemetry.context.Scope ignored = invokeSpan.makeCurrent()) {
            stream = responseHandler.createAsync(context, createResponse);
        } catch (RuntimeException e) {
            Observability.recordInvokeAgentError(invokeSpan,
                e.getClass().getSimpleName(), e.getMessage(), e);
            invokeSpan.end();
            throw e;
        }

        // stamp the resolved session ID on every snapshot the stream produces.
        if (stream instanceof AgentServerResponseEventStream impl) {
            impl.setAgentSessionId(sessionId);
        }

        // C4 (background+stream): persist an initial in_progress snapshot so GET
        // works immediately while the response streams in the background.
        if (background) {
            Response initial = stampSessionId(
                buildInitialResponse(responseId, createResponse, ResponseStatus.IN_PROGRESS),
                sessionId);
            persistStreamingResponse(context, initial);
        }

        // For stored responses, init the replay buffer so a mid-stream replay
        // request sees `hasBuffer()==true` (avoiding a false "not stream=true"
        //  rejection) and gets whatever has been emitted so far.
        if (store) {
            eventReplayStore.initBuffer(responseId);
            // Capture any events the handler emitted synchronously before we wired
            // up the subscriber below.
            for (ResponseEvent existing : stream.getEvents()) {
                eventReplayStore.append(responseId, existing);
            }
        }

        // Register the in-flight execution so cancel/disconnect can coordinate with
        // the terminal persistence. Background streams are tagged as background so
        // signalClientDisconnected is a no-op for them; explicit cancel
        // remains supported. Non-background streams enable.
        final Execution execution = executionTracker.register(responseId, background, store);

        final String finalResponseId = responseId;
        final boolean storeEnabled = store;
        final boolean isBackground = background;
        // Track the count of events we already appended above so we don't
        // double-buffer those when the subscribe callback fires.
        final int alreadyBuffered = store ? stream.getEvents().size() : 0;
        final java.util.concurrent.atomic.AtomicInteger seenCount =
            new java.util.concurrent.atomic.AtomicInteger(0);
        stream.subscribe(
            event -> {
                // Live mid-stream buffering: append each new event
                // so a concurrent replay request observes it. Skip the prefix the
                // synchronous handler already emitted before subscribe attached.
                if (storeEnabled && seenCount.getAndIncrement() >= alreadyBuffered) {
                    eventReplayStore.append(finalResponseId, event);
                }
            },
            failure -> {
                LOGGER.error("Stream failed for response {}", finalResponseId, failure);
                Observability.recordInvokeAgentError(invokeSpan,
                    failure.getClass().getSimpleName(), failure.getMessage(), failure);
                invokeSpan.end();
            },
            () -> {
                LOGGER.debug("Stream completed for response {}", finalResponseId);
                try {
                    synchronized (execution) {
                        if (execution.cancelRequested || execution.finalized) {
                            // Cancellation won the race; do not overwrite
                            // the cancelled snapshot with the natural terminal state.
                            return;
                        }
                        Response resp = stream.getResponse();
                        if (resp != null) {
                            if (isBackground) {
                                // C4: re-stamp background=true so GET on the terminal
                                // observes a coherent background response (mirroring C3).
                                resp = resp.toBuilder().background(true).build();
                            }
                            // Match the sync path: normalize child IDs to the resolved
                            // response's partition, stamp agent_reference and
                            // agent_session_id. Without these the storage POST
                            // fails (envelope partition mismatch, missing agent_reference).
                            resp = normalizeIdsAndStamp(resp, finalResponseId, sessionId, createResponse.agent());
                            persistStreamingResponse(context, resp);
                        }
                        execution.finalized = true;
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to persist streaming response", e);
                } finally {
                    executionTracker.evict(finalResponseId);
                    invokeSpan.end();
                }
            }
        );

        return stream;
    }

    private static String extractModelName(ResponseCreateParams.Body params) {
        try {
            if (params.model().isPresent()) {
                var model = params.model().get();
                if (model._json().isPresent() && model._json().get().asString().isPresent()) {
                    return model._json().get().asString().get().toString();
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }

    private static String extractConversationId(ResponseCreateParams.Body params) {
        try {
            return params.conversation()
                .flatMap(c -> c.isId() ? java.util.Optional.of(c.asId()) : java.util.Optional.<String>empty())
                .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public com.openai.models.responses.Response getResponse(String responseId, List<String> include) throws ApiException {
        return getResponse(responseId, include, RequestMetadata.EMPTY);
    }

    @Override
    public com.openai.models.responses.Response getResponse(String responseId, List<String> include, RequestMetadata metadata) throws ApiException {
        LOGGER.debug("getResponse called for responseId={}", responseId);
        validateResponseId(responseId);
        com.openai.models.responses.Response resp = provider.getResponseAsync(responseId, metadata.getIsolation())
            .join()
            .orElse(null);

        if (resp == null) {
            throw new ApiException(404, ApiError.invalidRequest(responseId + " not found"));
        }

        return resp;
    }

    @Override
    public ResponseStreamReplay replayResponseStream(String responseId, Integer startingAfter) throws ApiException {
        LOGGER.debug("replayResponseStream called for responseId={}, startingAfter={}", responseId, startingAfter);
        validateResponseId(responseId);

        // Not found / store=false → 404.
        com.openai.models.responses.Response resp = provider.getResponseAsync(responseId)
            .join()
            .orElse(null);
        if (resp == null) {
            throw new ApiException(404, ApiError.invalidRequest(responseId + " not found"));
        }

        // SSE replay requires background=true (checked before stream).
        if (!resp.background().orElse(false)) {
            throw new ApiException(400, ApiError.invalidRequest(
                "This response cannot be streamed because it was not created with background=true.",
                ApiError.CODE_INVALID_REQUEST, "stream"));
        }

        // SSE replay requires stream=true at creation. The presence of a replay
        // buffer indicates the response was created streaming.
        if (!eventReplayStore.hasBuffer(responseId)) {
            throw new ApiException(400, ApiError.invalidRequest(
                "This response cannot be streamed because it was not created with stream=true.",
                ApiError.CODE_INVALID_REQUEST, "stream"));
        }

        List<ResponseStreamReplay.ReplayEvent> events = eventReplayStore.replay(responseId, startingAfter)
            .orElseGet(List::of);
        return new ResponseStreamReplay(events);
    }

    @Override
    public com.openai.models.responses.Response cancelResponse(String responseId) throws ApiException {
        LOGGER.debug("cancelResponse called for responseId={}", responseId);
        validateResponseId(responseId);

        com.openai.models.responses.Response resp = provider.getResponseAsync(responseId)
            .join()
            .orElse(null);

        // (not found) → 404. Non-background in-flight responses are also not findable.
        if (resp == null) {
            throw new ApiException(404, ApiError.invalidRequest(responseId + " not found"));
        }

        // the background check happens first, regardless of status.
        boolean background = resp.background().orElse(false);
        if (!background) {
            throw new ApiException(400, ApiError.invalidRequest("Cannot cancel a synchronous response."));
        }

        com.openai.models.responses.ResponseStatus status = resp.status().orElse(null);

        // cancelling an already-cancelled response is idempotent.
        if (com.openai.models.responses.ResponseStatus.CANCELLED.equals(status)) {
            return resp;
        }

        // terminal states cannot be cancelled.
        if (com.openai.models.responses.ResponseStatus.COMPLETED.equals(status)) {
            throw new ApiException(400, ApiError.invalidRequest("Cannot cancel a completed response."));
        }
        if (com.openai.models.responses.ResponseStatus.FAILED.equals(status)) {
            throw new ApiException(400, ApiError.invalidRequest("Cannot cancel a failed response."));
        }
        if (com.openai.models.responses.ResponseStatus.INCOMPLETE.equals(status)) {
            throw new ApiException(400, ApiError.invalidRequest("Cannot cancel a response in terminal state."));
        }

        // queued or in_progress → wind down to cancelled with output cleared.
        com.openai.models.responses.Response cancelled = resp.toBuilder()
            .status(com.openai.models.responses.ResponseStatus.CANCELLED)
            .output(Collections.emptyList())
            .build();

        // Coordinate with any in-flight background execution so the background
        // finaliser does not overwrite the cancelled state.
        Execution execution = executionTracker.get(responseId);
        if (execution != null) {
            synchronized (execution) {
                execution.cancelRequested = true;
                provider.saveResponseAsync(responseId, cancelled, null, null).join();
            }
        } else {
            provider.saveResponseAsync(responseId, cancelled, null, null).join();
        }
        LOGGER.debug("Response {} cancelled (output cleared)", responseId);
        return cancelled;
    }

    @Override
    public void signalClientDisconnected(String responseId) {
        // only non-background responses are affected by client disconnect.
        // Background responses outlive the connection — no-op there.
        Execution execution = executionTracker.get(responseId);
        if (execution == null || execution.background) {
            return;
        }
        synchronized (execution) {
            if (execution.cancelRequested || execution.finalized) {
                return;
            }
            execution.cancelRequested = true;
            // Persist the cancelled snapshot only when store=true; per, store=false
            // disconnects produce no retrievable response (GET → 404).
            if (execution.store) {
                try {
                    com.openai.models.responses.Response existing =
                        provider.getResponseAsync(responseId).join().orElse(null);
                    com.openai.models.responses.Response.Builder builder = existing != null
                        ? existing.toBuilder()
                        : com.openai.models.responses.Response.builder()
                        .id(responseId)
                        .createdAt(System.currentTimeMillis() / 1000.0)
                        .model("")
                        .parallelToolCalls(false)
                        .tools(java.util.List.of())
                        .error(java.util.Optional.empty())
                        .incompleteDetails(java.util.Optional.empty())
                        .instructions(java.util.Optional.empty())
                        .metadata(java.util.Optional.empty())
                        .temperature(java.util.Optional.empty())
                        .topP(java.util.Optional.empty())
                        .toolChoice(com.openai.models.responses.ToolChoiceOptions.AUTO);
                    com.openai.models.responses.Response cancelled = builder
                        .status(com.openai.models.responses.ResponseStatus.CANCELLED)
                        .output(java.util.Collections.emptyList())
                        .build();
                    provider.saveResponseAsync(responseId, cancelled, null, null).join();
                } catch (Exception e) {
                    LOGGER.warn("Failed to persist cancelled-on-disconnect snapshot for {}", responseId, e);
                }
            }
            LOGGER.debug("Response {} cancelled on client disconnect", responseId);
        }
    }

    // ── Private helpers ─────────────────────────────────────────

    private ResponseContext buildContext(String responseId, String sessionId, AgentServerCreateResponse createResponse, RequestMetadata metadata) {
        return ResponseContext.builder()
            .responseId(responseId)
            .provider(provider)
            .request(createResponse.responseCreateParams())
            .isolation(metadata.getIsolation())
            .clientHeaders(metadata.getClientHeaders())
            .queryParameters(metadata.getQueryParameters())
            .requestId(metadata.getRequestId())
            .sessionId(sessionId)
            .build();
    }

    @Override
    public void deleteResponse(String responseId) throws ApiException {
        LOGGER.debug("deleteResponse called for responseId={}", responseId);
        validateResponseId(responseId);
        provider.deleteResponseAsync(responseId).join();
        // Best-effort removal of any buffered SSE events so replay is no longer possible.
        eventReplayStore.delete(responseId);
    }

    @Override
    public AgentServerResponseItemList listInputItems(String responseId, Integer limit, String order, String after, String before, List<String> include) throws ApiException {
        LOGGER.debug("listInputItems called for responseId={}", responseId);
        validateResponseId(responseId);

        // Verify the response exists
        com.openai.models.responses.Response resp = provider.getResponseAsync(responseId)
            .join()
            .orElse(null);

        if (resp == null) {
            throw new ApiException(404, ApiError.invalidRequest(responseId + " not found"));
        }

        // Retrieve the stored input items (not output items)
        List<ResponseItem> allItems = provider.getInputItemsForResponseAsync(responseId)
            .join();

        int effectiveLimit = (limit != null) ? limit : 20;
        boolean descending = "desc".equalsIgnoreCase(order);


        // Apply cursor-based pagination. `after` slices off everything up to and including
        // that item; `before` slices off everything from that item onward; both combine
        // as an exclusive range. Items always operate on the underlying stored order
        // (i.e. cursor semantics are positional and order-independent, per ).
        int fromIdx = 0;
        int toIdx = allItems.size();
        if (after != null && !after.isEmpty()) {
            for (int i = 0; i < allItems.size(); i++) {
                if (after.equals(extractResponseItemId(allItems.get(i)))) {
                    fromIdx = Math.max(fromIdx, i + 1);
                    break;
                }
            }
        }
        if (before != null && !before.isEmpty()) {
            for (int i = 0; i < allItems.size(); i++) {
                if (before.equals(extractResponseItemId(allItems.get(i)))) {
                    toIdx = Math.min(toIdx, i);
                    break;
                }
            }
        }
        List<ResponseItem> windowed = fromIdx < toIdx
            ? allItems.subList(fromIdx, toIdx)
            : List.of();

        // Apply ordering
        List<ResponseItem> ordered;
        if (descending) {
            ordered = new ArrayList<>(windowed);
            Collections.reverse(ordered);
        } else {
            ordered = windowed;
        }

        // Apply limit
        List<ResponseItem> page = ordered.size() > effectiveLimit
            ? ordered.subList(0, effectiveLimit)
            : ordered;

        boolean hasMore = ordered.size() > effectiveLimit;
        String firstId = page.isEmpty() ? null : extractResponseItemId(page.get(0));
        String lastId = page.isEmpty() ? null : extractResponseItemId(page.get(page.size() - 1));

        ResponseItemList itemList = ResponseItemList.builder()
            .data(page)
            .firstId(firstId != null ? firstId : "")
            .lastId(lastId != null ? lastId : "")
            .hasMore(hasMore)
            .build();

        return new AgentServerResponseItemList(null, itemList);
    }

    private void persistResponse(ResponseContext context, CreateResponse response) {
        if (response.response() != null) {
            com.openai.models.responses.Response resp = response.response();
            persistStreamingResponse(context, resp);
        }
    }

    private void persistStreamingResponse(ResponseContext context, com.openai.models.responses.Response resp) {
        String previousResponseId = resp.previousResponseId().orElse(null);
        String conversationId = resp.conversation()
            .map(com.openai.models.responses.Response.Conversation::id)
            .orElse(null);

        // Build role-preserving input_items directly from the raw request body.
        // The context.getInputItemsAsync() path returns ResponseOutputItems which
        // are hardcoded to role=assistant — wrong for stored conversation history.
        List<ResponseItem> inputItems = Collections.emptyList();
        try {
            ResponseCreateParams.Body body = context instanceof AgentServerResponseContext c
                ? c.getRequestBody() : null;
            if (body != null && body.input().isPresent()) {
                ResponseCreateParams.Input in = body.input().get();
                if (in.isResponse()) {
                    IdGenerator idGen = new IdGenerator(IdGenerator.extractPartitionKey(resp.id()));
                    List<ResponseItem> items = new ArrayList<>();
                    for (com.openai.models.responses.ResponseInputItem raw : in.asResponse()) {
                        ResponseItem converted = ItemConversion.toResponseItem(raw, idGen);
                        if (converted != null) {
                            items.add(converted);
                        }
                    }
                    inputItems = items;
                } else if (in.isText() && !in.asText().isEmpty()) {
                    // Plain-text input — synthesize a single user message.
                    IdGenerator idGen = new IdGenerator(IdGenerator.extractPartitionKey(resp.id()));
                    inputItems = List.of(ItemConversion.toResponseItem(
                        com.openai.models.responses.ResponseInputItem.ofEasyInputMessage(
                            com.openai.models.responses.EasyInputMessage.builder()
                                .role(com.openai.models.responses.EasyInputMessage.Role.USER)
                                .content(com.openai.models.responses.EasyInputMessage.Content.ofTextInput(in.asText()))
                                .build()),
                        idGen));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to gather input items for response {}", resp.id(), e);
        }

        // Get history item IDs if there's a previous response
        List<String> historyItemIds = Collections.emptyList();
        if (previousResponseId != null) {
            try {
                historyItemIds = provider.getHistoryItemIdsAsync(previousResponseId, conversationId, 100).join();
            } catch (Exception e) {
                LOGGER.warn("Failed to get history item IDs for response {}", resp.id(), e);
            }
        }

        // Single envelope call: POST /responses with {response, input_items, history_item_ids}
        try {
            // Pass isolation context so storage API receives platform isolation headers
            IsolationContext isolation = context.getIsolation();
            if (provider instanceof FoundryStorageProvider foundryProvider) {
                foundryProvider.createResponseAsync(resp.id(), resp, inputItems, historyItemIds, isolation).join();
            } else {
                provider.createResponseAsync(resp.id(), resp, inputItems, historyItemIds).join();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to persist response {} to Foundry storage", resp.id(), e);
        }
    }

    /**
     * Shuts down the background executor, waiting up to 5 seconds for in-flight
     * tasks to complete. Should be called when the hosting container is stopping
     * (e.g. via a JVM shutdown hook or framework lifecycle callback).
     */
    @Override
    public void close() {
        backgroundExecutor.shutdown();
        try {
            if (!backgroundExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                LOGGER.warn("Background executor did not terminate within 5s; forcing shutdown");
                backgroundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while awaiting background executor shutdown");
            backgroundExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

