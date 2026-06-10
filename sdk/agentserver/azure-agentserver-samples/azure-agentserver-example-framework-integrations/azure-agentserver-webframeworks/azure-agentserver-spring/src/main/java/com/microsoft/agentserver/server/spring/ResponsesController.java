// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.server.spring;

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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring MVC controller that exposes the {@link ResponsesApi} as HTTP endpoints.
 * <p>
 * This class handles all Spring-specific concerns (annotations, SSE transport,
 * ResponseEntity wrapping) and delegates business logic to the core {@link ResponsesApi}.
 */
@RestController
@RequestMapping("/responses")
public class ResponsesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponsesController.class);

    private final ResponsesApi responsesApi;

    public ResponsesController(ResponsesApi responsesApi) {
        this.responsesApi = responsesApi;
    }

    /**
     * Create a model response (non-streaming).
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateResponse> createResponse(
        @RequestBody AgentServerCreateResponse createResponse,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) throws ApiException {
        RequestMetadata metadata = extractMetadata(httpRequest);
        CreateResponse result = responsesApi.createResponse(createResponse, metadata);
        publishSessionId(httpRequest, httpResponse, result.response());
        return ResponseEntity.ok(result);
    }

    /**
     * Create a streaming model response (SSE).
     */
    @PostMapping(path = "/streaming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter createStreamingResponse(
        @RequestBody AgentServerCreateResponse createResponse,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) throws ApiException {

        RequestMetadata metadata = extractMetadata(httpRequest);
        ResponseEventStream stream = responsesApi.createStreamingResponse(createResponse, metadata);
        // Snapshot the response ID so we can signal client disconnect.
        final String responseId = stream.getResponse() != null ? stream.getResponse().id() : null;
        // stamp the resolved session ID on the response BEFORE the SSE body
        // commits the response.
        publishSessionId(httpRequest, httpResponse, stream.getResponse());

        // Timeout of 0 means no timeout — the stream ends when we call complete().
        SseEmitter emitter = new SseEmitter(0L);

        // detect client disconnect (timeout/error/early-completion mid-stream) and
        // signal it to the API so the response winds down to `cancelled`. signalClientDisconnected
        // is a no-op for background responses and once the natural terminal has fired.
        Runnable signalDisconnect = () -> {
            if (responseId != null) {
                responsesApi.signalClientDisconnected(responseId);
            }
        };
        emitter.onError(t -> signalDisconnect.run());
        emitter.onTimeout(signalDisconnect);

        stream.subscribe(
            event -> {
                try {
                    String json = ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(event.streamEvent());
                    LOGGER.debug("SSE event [{}]: {}", event.eventName(), json);
                    // Per the protocol: each SSE event is written as
                    // `event: {type}\ndata: {json}\n\n`. No `id:` line is emitted — the
                    // `sequence_number` field in the JSON payload is the resumption cursor.
                    //
                    // We bypass SseEmitter.event() because its builder hard-codes
                    // `event:<name>\n` / `data:<json>\n` with no space after the colon,
                    // and the Foundry trajectory UI's strict SSE parser silently drops
                    // events that don't use the conventional `field: value` format
                    // (manifesting as an infinite spinner). Build the spaced frame
                    // ourselves and write it verbatim via send(Set<DataWithMediaType>).
                    String frame = "event: " + event.eventName() + "\ndata: " + json + "\n\n";
                    emitter.send(java.util.Collections.singleton(
                        new ResponseBodyEmitter.DataWithMediaType(frame, MediaType.TEXT_PLAIN)));
                } catch (Exception e) {
                    LOGGER.error("Failed to send SSE event (client may have disconnected)", e);
                    // treat send failure as client disconnect.
                    signalDisconnect.run();
                }
            },
            failure -> {
                // the terminal event (already emitted by the library)
                // signals stream completion. There is no `[DONE]` sentinel.
                LOGGER.error("Stream failed", failure);
                emitter.complete();
            },
            () -> {
                // no `[DONE]` sentinel — the terminal event ends the stream.
                emitter.complete();
            }
        );

        return emitter;
    }

    /**
     * Get a model response by ID (non-streaming JSON).
     * <p>
     * SSE replay ({@code ?stream=true}) is routed to {@link #getResponseStream}
     * by {@link StreamRoutingFilter}, mirroring the POST create/streaming split.
     */
    @GetMapping(path = "/{responseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<com.openai.models.responses.Response> getResponse(
        @PathVariable("responseId") String responseId,
        @RequestParam(value = "include", required = false) List<String> include,
        @RequestParam(value = "include_obfuscation", required = false) Boolean includeObfuscation,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse)
        throws ApiException {
        RequestMetadata metadata = extractMetadata(httpRequest);
        com.openai.models.responses.Response resp = responsesApi.getResponse(responseId, include, metadata);
        publishSessionId(httpRequest, httpResponse, resp);
        return ResponseEntity.ok(resp);
    }

    /**
     * Replay a response's SSE event stream (the API spec,
     * ). Reached via {@code GET /responses/{id}?stream=true}, which
     * {@link StreamRoutingFilter} forwards to this {@code /stream} sub-resource.
     * <p>
     * Uses the same {@link SseEmitter} machinery as {@link #createStreamingResponse}
     * so both endpoints share identical SSE framing semantics (no {@code id:}
     * line, no {@code [DONE]} sentinel).
     */
    @GetMapping(path = "/{responseId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getResponseStream(
        @PathVariable("responseId") String responseId,
        @RequestParam(value = "starting_after", required = false) Integer startingAfter)
        throws ApiException {
        // Precondition validation happens BEFORE returning the emitter
        // so ApiException (400/404) flows through the GlobalExceptionHandler.
        ResponseStreamReplay replay = responsesApi.replayResponseStream(responseId, startingAfter);

        // Timeout of 0 means no timeout — the stream ends when we call complete().
        SseEmitter emitter = new SseEmitter(0L);
        try {
            for (ResponseStreamReplay.ReplayEvent event : replay.events()) {
                // `event: {type}\ndata: {json}\n\n`, no `id:` line —
                // the `sequence_number` is already present in the JSON payload.
                emitter.send(SseEmitter.event()
                    .name(event.eventName())
                    .data(event.data(), MediaType.APPLICATION_JSON));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to send SSE replay event for response {}", responseId, e);
            emitter.completeWithError(e);
            return emitter;
        }
        // no `[DONE]` sentinel — completing the emitter ends the stream.
        emitter.complete();
        return emitter;
    }

    /**
     * Cancel a background model response by ID.
     * <p>
     * Per the API spec, this cancels a
     * {@code background=true} response that is still {@code queued} or
     * {@code in_progress}, returning HTTP 200 with the cancelled response.
     * Rejections (synchronous response, terminal state, not found) surface as
     * {@link ApiException} (400/404).
     */
    @PostMapping(path = "/{responseId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<com.openai.models.responses.Response> cancelResponse(
        @PathVariable("responseId") String responseId,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) throws ApiException {
        com.openai.models.responses.Response resp = responsesApi.cancelResponse(responseId);
        publishSessionId(httpRequest, httpResponse, resp);
        return ResponseEntity.ok(resp);
    }

    /**
     * Delete a model response by ID.
     * <p>
     * Per the API spec, a successful delete
     * returns HTTP 200 with a body of the shape
     * {@code { "id": "...", "object": "response", "deleted": true }}.
     */
    @DeleteMapping(path = "/{responseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteResponse(@PathVariable("responseId") String responseId) throws ApiException {
        responsesApi.deleteResponse(responseId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", responseId);
        body.put("object", "response");
        body.put("deleted", true);
        return ResponseEntity.ok(body);
    }

    /**
     * List input items for a response.
     */
    @GetMapping(path = "/{responseId}/input_items", produces = MediaType.APPLICATION_JSON_VALUE)
    public AgentServerResponseItemList listInputItems(
        @PathVariable("responseId") String responseId,
        @RequestParam(value = "limit", defaultValue = "20") Integer limit,
        @RequestParam(value = "order", required = false) String order,
        @RequestParam(value = "after", required = false) String after,
        @RequestParam(value = "before", required = false) String before,
        @RequestParam(value = "include", required = false) List<String> include) throws ApiException {
        return responsesApi.listInputItems(responseId, limit, order, after, before, include);
    }

    // ── Private helpers ─────────────────────────────────────────

    /**
     * Publishes the resolved {@code agent_session_id} so the
     * {@link PlatformHeaderFilter} echoes it as {@code x-agent-session-id}.
     * For SSE responses (where the body may already be committing), this also
     * sets the header directly on the {@link HttpServletResponse}.
     */
    private static void publishSessionId(HttpServletRequest request, HttpServletResponse response,
                                         com.openai.models.responses.Response resp) {
        if (resp == null) {
            return;
        }
        com.openai.core.JsonValue val = resp._additionalProperties().get("agent_session_id");
        if (val == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        java.util.Optional<String> opt = val.asString();
        opt.filter(s -> !s.isEmpty()).ifPresent(s -> {
            request.setAttribute(PlatformHeaderFilter.SESSION_ID_ATTR, s);
            if (response != null && !response.isCommitted()) {
                response.setHeader(PlatformHeaders.SESSION_ID, s);
            }
        });
    }

    /**
     * Extracts platform metadata from the HTTP request headers and attributes.
     * Includes isolation keys, client headers (x-client-*), query parameters,
     * and the resolved request ID.
     */
    private RequestMetadata extractMetadata(HttpServletRequest request) {
        // Extract all headers into a case-insensitive map
        Map<String, String> allHeaders = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                allHeaders.put(name.toLowerCase(), request.getHeader(name));
            }
        }

        // Isolation context from platform headers
        IsolationContext isolation = IsolationContext.fromHeaders(allHeaders);

        // Client headers (x-client-* prefix)
        Map<String, String> clientHeaders = IsolationContext.extractClientHeaders(allHeaders);

        // Query parameters
        Map<String, String> queryParameters;
        if (request.getParameterMap().isEmpty()) {
            queryParameters = Collections.emptyMap();
        } else {
            Map<String, String> params = new LinkedHashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    params.put(key, values[0]);
                }
            });
            queryParameters = Collections.unmodifiableMap(params);
        }

        // Request ID (set by PlatformHeaderFilter)
        String requestId = (String) request.getAttribute(PlatformHeaders.REQUEST_ID);

        // x-agent-response-id request header overrides the generated response ID.
        String responseIdOverride = allHeaders.get(PlatformHeaders.RESPONSE_ID_OVERRIDE);

        return new RequestMetadata(isolation, clientHeaders, queryParameters, requestId, responseIdOverride);
    }
}

