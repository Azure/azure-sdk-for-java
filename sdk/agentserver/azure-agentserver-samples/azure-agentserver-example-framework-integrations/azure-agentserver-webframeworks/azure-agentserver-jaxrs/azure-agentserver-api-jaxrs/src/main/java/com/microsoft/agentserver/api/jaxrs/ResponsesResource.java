// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.jaxrs;

import com.microsoft.agentserver.api.ApiError;
import com.microsoft.agentserver.api.ApiException;
import com.microsoft.agentserver.api.AgentServerCreateResponse;
import com.microsoft.agentserver.api.AgentServerResponseItemList;
import com.microsoft.agentserver.api.CreateResponse;
import com.microsoft.agentserver.api.IsolationContext;
import com.microsoft.agentserver.api.PlatformHeaders;
import com.microsoft.agentserver.api.RequestMetadata;
import com.microsoft.agentserver.api.ResponseEventStream;
import com.microsoft.agentserver.api.ResponseStreamReplay;
import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.api.serialization.ObjectMapperFactory;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JAX-RS resource that exposes the {@link ResponsesApi} as HTTP endpoints.
 * <p>
 * This class handles all JAX-RS-specific concerns (annotations, SSE transport,
 * Response wrapping) and delegates business logic to the core {@link ResponsesApi}.
 */
@Path("/responses")
public class ResponsesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponsesResource.class);

    private final ResponsesApi responsesApi;

    public ResponsesResource(ResponsesApi responsesApi) {
        this.responsesApi = responsesApi;
    }

    /**
     * Create a model response (non-streaming).
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createResponse(
        AgentServerCreateResponse createResponse,
        @Context HttpHeaders headers,
        @Context UriInfo uriInfo,
        @Context ContainerRequestContext rc) throws ApiException {
        RequestMetadata metadata = extractMetadata(headers, uriInfo);
        CreateResponse result = responsesApi.createResponse(createResponse, metadata);
        publishSessionId(rc, result.response());
        return Response.ok().entity(result).build();
    }

    /**
     * Create a streaming model response (SSE).
     */
    @POST
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Path("/streaming")
    public void createStreamingResponse(
        @Context SseEventSink eventSink,
        @Context Sse sse,
        @Context HttpHeaders headers,
        @Context UriInfo uriInfo,
        @Context ContainerRequestContext rc,
        AgentServerCreateResponse createResponse) throws ApiException {

        if (eventSink == null) {
            throw new ApiException(500, ApiError.serverError("SseEventSink was not injected properly"));
        }
        if (sse == null) {
            throw new ApiException(500, ApiError.serverError("Sse context was not injected properly"));
        }

        RequestMetadata metadata = extractMetadata(headers, uriInfo);
        ResponseEventStream stream = responsesApi.createStreamingResponse(createResponse, metadata);
        // Snapshot the response ID so we can signal client disconnect.
        final String responseId = stream.getResponse() != null ? stream.getResponse().id() : null;
        // publish the resolved session ID so the header filter echoes it.
        publishSessionId(rc, stream.getResponse());

        OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder();

        stream.subscribe(
            event -> {
                try {
                    String json = ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(event.streamEvent());
                    LOGGER.debug("SSE event [{}]: {}", event.eventName(), json);
                    // Per the protocol: each SSE event is written as
                    // `event: {type}\ndata: {json}\n\n`. No `id:` line is emitted — the
                    // `sequence_number` field in the JSON payload is the resumption cursor.
                    eventSink.send(eventBuilder
                            .name(event.eventName())
                            .data(json)
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .build())
                        .toCompletableFuture()
                        .join();
                    // detect client disconnect mid-stream.
                    if (eventSink.isClosed() && responseId != null) {
                        responsesApi.signalClientDisconnected(responseId);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to send SSE event (client may have disconnected)", e);
                    // treat send failure as client disconnect.
                    if (responseId != null) {
                        responsesApi.signalClientDisconnected(responseId);
                    }
                }
            },
            failure -> {
                // the terminal event (already emitted by the library)
                // signals stream completion. There is no `[DONE]` sentinel.
                LOGGER.error("Stream failed", failure);
                closeQuietly(eventSink);
            },
            () -> {
                // no `[DONE]` sentinel — the terminal event ends the stream.
                closeQuietly(eventSink);
            }
        );
    }

    /**
     * Get a model response by ID (non-streaming JSON).
     * <p>
     * SSE replay ({@code ?stream=true}) is routed to {@link #getResponseStream}
     * by {@link RoutingFilter}, mirroring the POST create/streaming split.
     */
    @GET
    @Path("/{response_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(
        @PathParam("response_id") String responseId,
        @QueryParam("include") List<String> include,
        @QueryParam("include_obfuscation") Boolean includeObfuscation,
        @Context HttpHeaders headers,
        @Context UriInfo uriInfo,
        @Context ContainerRequestContext rc) throws ApiException {
        RequestMetadata metadata = extractMetadata(headers, uriInfo);
        com.openai.models.responses.Response resp = responsesApi.getResponse(responseId, include, metadata);
        publishSessionId(rc, resp);
        return Response.ok().entity(resp).build();
    }

    /**
     * Replay a response's SSE event stream (the API spec,
     * ). Reached via {@code GET /responses/{id}?stream=true}, which
     * {@link RoutingFilter} rewrites to this {@code /stream} sub-resource.
     * <p>
     * Uses the same {@link Sse}/{@link SseEventSink} machinery as
     * {@link #createStreamingResponse} so both endpoints share identical SSE
     * framing semantics (no {@code id:} line, no {@code [DONE]}
     * sentinel).
     */
    @GET
    @Path("/{response_id}/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getResponseStream(
        @PathParam("response_id") String responseId,
        @QueryParam("starting_after") Integer startingAfter,
        @Context SseEventSink eventSink,
        @Context Sse sse) throws ApiException {
        // Precondition validation happens BEFORE touching the sink so
        // ApiException (400/404) flows through the standard ApiExceptionMapper.
        ResponseStreamReplay replay = responsesApi.replayResponseStream(responseId, startingAfter);

        if (eventSink == null) {
            throw new ApiException(500, ApiError.serverError("SseEventSink was not injected properly"));
        }
        if (sse == null) {
            throw new ApiException(500, ApiError.serverError("Sse context was not injected properly"));
        }

        OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder();
        try {
            for (ResponseStreamReplay.ReplayEvent event : replay.events()) {
                // `event: {type}\ndata: {json}\n\n`, no `id:` line —
                // the `sequence_number` is already present in the JSON payload.
                eventSink.send(eventBuilder
                        .name(event.eventName())
                        .data(event.data())
                        .mediaType(MediaType.APPLICATION_JSON_TYPE)
                        .build())
                    .toCompletableFuture()
                    .join();
            }
        } finally {
            // no `[DONE]` sentinel — closing the sink ends the stream.
            closeQuietly(eventSink);
        }
    }

    /**
     * Cancel a background model response by ID.
     * <p>
     * Per the API spec, this cancels a
     * {@code background=true} response that is still {@code queued} or
     * {@code in_progress}, returning HTTP 200 with the cancelled {@link Response}.
     * Rejections (synchronous response, terminal state, not found) surface as
     * {@link ApiException} (400/404).
     */
    @POST
    @Path("/{response_id}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelResponse(
        @PathParam("response_id") String responseId,
        @Context ContainerRequestContext rc) throws ApiException {
        com.openai.models.responses.Response resp = responsesApi.cancelResponse(responseId);
        publishSessionId(rc, resp);
        return Response.ok().entity(resp).build();
    }

    /**
     * Delete a model response by ID.
     * <p>
     * Per the API spec, a successful delete
     * returns HTTP 200 with a body of the shape
     * {@code { "id": "...", "object": "response", "deleted": true }}.
     */
    @DELETE
    @Path("/{response_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteResponse(@PathParam("response_id") String responseId) throws ApiException {
        responsesApi.deleteResponse(responseId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", responseId);
        body.put("object", "response");
        body.put("deleted", true);
        return Response.ok().entity(body).build();
    }

    /**
     * List input items for a response.
     */
    @GET
    @Path("/{response_id}/input_items")
    @Produces(MediaType.APPLICATION_JSON)
    public AgentServerResponseItemList listInputItems(
        @PathParam("response_id") String responseId,
        @QueryParam("limit") @DefaultValue("20") Integer limit,
        @QueryParam("order") String order,
        @QueryParam("after") String after,
        @QueryParam("before") String before,
        @QueryParam("include") List<String> include) throws ApiException {
        return responsesApi.listInputItems(responseId, limit, order, after, before, include);
    }

    private static void closeQuietly(SseEventSink sink) {
        try {
            sink.close();
        } catch (Exception e) {
            LOGGER.error("Failed to close SseEventSink", e);
        }
    }

    /**
     * Publishes the resolved {@code agent_session_id} onto the JAX-RS
     * request-context property that {@link PlatformHeaderResponseFilter} echoes
     * as the {@code x-agent-session-id} response header. No-op when the response
     * has no stamped session ID (e.g. legacy stored entries).
     */
    private static void publishSessionId(ContainerRequestContext rc, com.openai.models.responses.Response resp) {
        if (rc == null || resp == null) {
            return;
        }
        com.openai.core.JsonValue val = resp._additionalProperties().get("agent_session_id");
        if (val == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        java.util.Optional<String> opt = val.asString();
        opt.filter(s -> !s.isEmpty()).ifPresent(s ->
            rc.setProperty(PlatformHeaderResponseFilter.SESSION_ID_PROPERTY, s));
    }

    /**
     * Extracts platform metadata from JAX-RS HttpHeaders and UriInfo.
     */
    private RequestMetadata extractMetadata(HttpHeaders headers, UriInfo uriInfo) {
        // Flatten headers into a single-value map (lowercase keys)
        Map<String, String> allHeaders = new LinkedHashMap<>();
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        if (requestHeaders != null) {
            requestHeaders.forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    allHeaders.put(key.toLowerCase(), values.get(0));
                }
            });
        }

        // Isolation context
        IsolationContext isolation = IsolationContext.fromHeaders(allHeaders);

        // Client headers (x-client-* prefix)
        Map<String, String> clientHeaders = IsolationContext.extractClientHeaders(allHeaders);

        // Query parameters
        Map<String, String> queryParameters;
        MultivaluedMap<String, String> queryParams = uriInfo != null ? uriInfo.getQueryParameters() : null;
        if (queryParams == null || queryParams.isEmpty()) {
            queryParameters = Collections.emptyMap();
        } else {
            Map<String, String> params = new LinkedHashMap<>();
            queryParams.forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    params.put(key, values.get(0));
                }
            });
            queryParameters = Collections.unmodifiableMap(params);
        }

        // Request ID from header
        String requestId = allHeaders.get(PlatformHeaders.REQUEST_ID);

        // x-agent-response-id request header overrides the generated response ID.
        String responseIdOverride = allHeaders.get(PlatformHeaders.RESPONSE_ID_OVERRIDE);

        return new RequestMetadata(isolation, clientHeaders, queryParameters, requestId, responseIdOverride);
    }
}

