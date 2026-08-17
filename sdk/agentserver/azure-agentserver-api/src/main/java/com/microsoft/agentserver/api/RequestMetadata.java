// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Carries HTTP-level metadata extracted from the incoming request that should be
 * propagated to the {@link ResponseContext}. This includes platform headers
 * (isolation keys, client headers), query parameters, and request ID.
 * <p>
 * Framework adapters (Spring, JAX-RS) create instances from the HTTP request
 * and pass them to the API layer via {@link ResponsesApi} methods.
 */
public final class RequestMetadata {

    /**
     * Empty metadata instance with no headers or parameters.
     * Used when no HTTP context is available (e.g., unit tests, direct API calls).
     */
    public static final RequestMetadata EMPTY = new RequestMetadata(
        IsolationContext.EMPTY, Collections.emptyMap(), Collections.emptyMap(), null, null);

    private final IsolationContext isolation;
    private final Map<String, String> clientHeaders;
    private final Map<String, String> queryParameters;
    private final String requestId;
    private final String responseIdOverride;

    /**
     * Creates a new {@link RequestMetadata} instance.
     *
     * @param isolation          the isolation context (non-null).
     * @param clientHeaders      the forwarded {@code x-client-*} headers (non-null).
     * @param queryParameters    the query parameters (non-null).
     * @param requestId          the request ID for correlation, or {@code null}.
     * @param responseIdOverride the value of the {@code x-agent-response-id} request
     *                           header, or {@code null} when absent / empty.
     */
    public RequestMetadata(
        IsolationContext isolation,
        Map<String, String> clientHeaders,
        Map<String, String> queryParameters,
        String requestId,
        String responseIdOverride) {
        this.isolation = Objects.requireNonNull(isolation, "isolation must not be null");
        this.clientHeaders = Objects.requireNonNull(clientHeaders, "clientHeaders must not be null");
        this.queryParameters = Objects.requireNonNull(queryParameters, "queryParameters must not be null");
        this.requestId = requestId;
        this.responseIdOverride = (responseIdOverride == null || responseIdOverride.isEmpty())
            ? null : responseIdOverride;
    }

    /**
     * Backwards-compatible 4-arg constructor for callers that do not carry an
     * {@code x-agent-response-id} override (e.g. direct API tests).
     */
    public RequestMetadata(
        IsolationContext isolation,
        Map<String, String> clientHeaders,
        Map<String, String> queryParameters,
        String requestId) {
        this(isolation, clientHeaders, queryParameters, requestId, null);
    }

    /**
     * Gets the platform-injected isolation keys.
     */
    public IsolationContext getIsolation() {
        return isolation;
    }

    /**
     * Gets the forwarded client headers (prefixed with {@code x-client-}).
     */
    public Map<String, String> getClientHeaders() {
        return clientHeaders;
    }

    /**
     * Gets the query parameters from the HTTP request.
     */
    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    /**
     * Gets the request ID for correlation.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Gets the value of the {@code x-agent-response-id} request header — when
     * non-null/non-empty, this overrides the generated response ID.
     *
     * @return the override, or {@code null} when the header was absent or empty.
     */
    public String getResponseIdOverride() {
        return responseIdOverride;
    }
}

