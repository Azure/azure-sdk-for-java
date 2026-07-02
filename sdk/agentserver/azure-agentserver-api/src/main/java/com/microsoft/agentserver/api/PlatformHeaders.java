// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

/**
 * Defines the HTTP header names used across the AgentServer platform.
 * These headers form the wire contract between the Foundry platform,
 * agent containers, and downstream storage services.
 * <p>
 * <b>Response headers</b> (set by the server on every response):
 * <ul>
 *  <li>{@link #REQUEST_ID} — request correlation ID.</li>
 *  <li>{@link #SERVER_VERSION} — server SDK identity.</li>
 *  <li>{@link #SESSION_ID} — resolved session ID (when applicable).</li>
 * </ul>
 * <b>Request headers</b> (set by the platform or client):
 * <ul>
 *  <li>{@link #REQUEST_ID} — client-provided correlation ID (echoed back on response).</li>
 *  <li>{@link #USER_ISOLATION_KEY} / {@link #CHAT_ISOLATION_KEY} — platform isolation keys.</li>
 *  <li>{@link #CLIENT_HEADER_PREFIX} — prefix for pass-through client headers.</li>
 *  <li>{@link #TRACE_PARENT} — W3C Trace Context propagation header.</li>
 *  <li>{@link #CLIENT_REQUEST_ID} — Azure SDK client correlation header.</li>
 * </ul>
 */
public final class PlatformHeaders {

    /**
     * The {@code x-request-id} header — carries the request correlation ID.
     * On responses, the server always sets this header.
     * On requests, clients may set it to provide their own correlation ID.
     */
    public static final String REQUEST_ID = "x-request-id";

    /**
     * The {@code x-platform-server} header — identifies the server SDK stack
     * (hosting version, protocol versions, language, and runtime).
     * Set on every response by the platform header filter.
     */
    public static final String SERVER_VERSION = "x-platform-server";

    /**
     * The {@code x-agent-session-id} header — the resolved session ID for the request.
     * Set on responses by protocol-specific session resolution logic.
     */
    public static final String SESSION_ID = "x-agent-session-id";

    /**
     * The {@code x-agent-response-id} request header — when present and non-empty,
     * the container MUST use this value as the response ID instead of generating
     * one.
     */
    public static final String RESPONSE_ID_OVERRIDE = "x-agent-response-id";

    /**
     * The {@code x-agent-user-isolation-key} header — the platform-injected
     * partition key for user-private state.
     */
    public static final String USER_ISOLATION_KEY = "x-agent-user-isolation-key";

    /**
     * The {@code x-agent-chat-isolation-key} header — the platform-injected
     * partition key for conversation-scoped state.
     */
    public static final String CHAT_ISOLATION_KEY = "x-agent-chat-isolation-key";

    /**
     * The prefix {@code x-client-} for pass-through client headers.
     * All request headers starting with this prefix are extracted and forwarded
     * to the handler via the response context.
     */
    public static final String CLIENT_HEADER_PREFIX = "x-client-";

    /**
     * The {@code traceparent} header — W3C Trace Context propagation header.
     * Used for distributed tracing correlation on outbound storage requests.
     */
    public static final String TRACE_PARENT = "traceparent";

    /**
     * The {@code x-ms-client-request-id} header — Azure SDK client correlation header.
     * Logged for diagnostic correlation with upstream Azure SDK callers.
     */
    public static final String CLIENT_REQUEST_ID = "x-ms-client-request-id";

    private PlatformHeaders() {
        // Static constants class
    }
}

