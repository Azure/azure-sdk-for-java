// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.azure.ai.voicelive.implementation.VoiceLiveTracer;
import com.azure.ai.voicelive.models.AgentSessionConfig;
import com.azure.ai.voicelive.models.VoiceLiveRequestOptions;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Mono;

/**
 * The VoiceLiveAsyncClient provides methods to create and manage real-time voice communication sessions
 * with the Azure VoiceLive service.
 */
@ServiceClient(builder = VoiceLiveClientBuilder.class, isAsync = true)
public final class VoiceLiveAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveAsyncClient.class);

    private final URI endpoint;
    private final KeyCredential keyCredential;
    private final TokenCredential tokenCredential;
    private final String apiVersion;
    private final HttpHeaders additionalHeaders;

    VoiceLiveAsyncClient(URI endpoint, KeyCredential keyCredential, String apiVersion, HttpHeaders additionalHeaders) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null");
        this.tokenCredential = null;
        this.apiVersion = Objects.requireNonNull(apiVersion, "'apiVersion' cannot be null");
        this.additionalHeaders = additionalHeaders != null ? additionalHeaders : new HttpHeaders();
    }

    VoiceLiveAsyncClient(URI endpoint, TokenCredential tokenCredential, String apiVersion,
        HttpHeaders additionalHeaders) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = null;
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null");
        this.apiVersion = Objects.requireNonNull(apiVersion, "'apiVersion' cannot be null");
        this.additionalHeaders = additionalHeaders != null ? additionalHeaders : new HttpHeaders();
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication using the default
     * service-side model configuration.
     *
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession() {
        return startSessionInternal(null, null, null);
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication with custom request options.
     *
     * @param model The model to use for the session.
     * @param requestOptions Custom query parameters and headers for the request. May be null.
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     * @throws NullPointerException if {@code model} is null.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession(String model, VoiceLiveRequestOptions requestOptions) {
        Objects.requireNonNull(model, "'model' cannot be null");

        return startSessionInternal(model, null, requestOptions);
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication with an Azure AI Foundry agent.
     *
     * <p>This method configures the session to connect directly to an Azure AI Foundry agent,
     * using the agent configuration to set the appropriate query parameters.</p>
     *
     * @param agentConfig The agent session configuration containing the agent name, project name,
     *                    and optional parameters.
     * @param requestOptions Custom query parameters and headers for the request. May be null.
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     * @throws NullPointerException if {@code agentConfig} is null.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession(AgentSessionConfig agentConfig,
        VoiceLiveRequestOptions requestOptions) {
        Objects.requireNonNull(agentConfig, "'agentConfig' cannot be null");

        return startSessionInternal(null, agentConfig, requestOptions);
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication.
     *
     * @param model The model name to use for the session, or null.
     * @param agentConfig The agent session configuration to use for the session, or null.
     * @param requestOptions Custom query parameters and headers for the request, or null.
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     */
    private Mono<VoiceLiveSessionAsyncClient> startSessionInternal(String model, AgentSessionConfig agentConfig,
        VoiceLiveRequestOptions requestOptions) {
        Map<String, String> queryParameters
            = agentConfig == null ? new LinkedHashMap<>() : new LinkedHashMap<>(toQueryParameters(agentConfig));
        HttpHeaders mergedHeaders = additionalHeaders;

        if (requestOptions != null) {
            if (requestOptions.getCustomQueryParameters() != null) {
                queryParameters.putAll(requestOptions.getCustomQueryParameters());
            }
            mergedHeaders = mergeHeaders(additionalHeaders, requestOptions.getCustomHeaders());
        }

        final Map<String, String> finalQueryParameters = queryParameters.isEmpty() ? null : queryParameters;
        final HttpHeaders connectionHeaders = mergedHeaders;
        final String sessionModel = model;
        final AgentSessionConfig sessionAgentConfig = agentConfig;

        URI wsEndpoint = convertToWebSocketEndpoint(endpoint, sessionModel, finalQueryParameters);
        VoiceLiveSessionAsyncClient session = createSessionClient(wsEndpoint, sessionModel, sessionAgentConfig);
        return session.connect(connectionHeaders).thenReturn(session);
    }

    /**
     * Creates a VoiceLiveSessionAsyncClient with the appropriate credentials and optional tracing.
     *
     * @param wsEndpoint The WebSocket endpoint URI.
     * @param model The model name, used for tracing span names.
     * @param agentSessionConfig The agent session configuration, or null.
     * @return A new VoiceLiveSessionAsyncClient instance.
     */
    private VoiceLiveSessionAsyncClient createSessionClient(URI wsEndpoint, String model,
        AgentSessionConfig agentSessionConfig) {
        VoiceLiveTracer voiceLiveTracer = VoiceLiveTracer.create(wsEndpoint, model, null);
        if (keyCredential != null) {
            return new VoiceLiveSessionAsyncClient(wsEndpoint, keyCredential, voiceLiveTracer, agentSessionConfig);
        } else {
            return new VoiceLiveSessionAsyncClient(wsEndpoint, tokenCredential, voiceLiveTracer, agentSessionConfig);
        }
    }

    /**
     * Converts an {@link AgentSessionConfig} to a map of WebSocket query parameters.
     */
    static Map<String, String> toQueryParameters(AgentSessionConfig agentConfig) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("agent-name", agentConfig.getAgentName());
        params.put("agent-project-name", agentConfig.getProjectName());
        if (agentConfig.getAgentVersion() != null && !agentConfig.getAgentVersion().isEmpty()) {
            params.put("agent-version", agentConfig.getAgentVersion());
        }
        if (agentConfig.getConversationId() != null && !agentConfig.getConversationId().isEmpty()) {
            params.put("conversation-id", agentConfig.getConversationId());
        }
        if (agentConfig.getAuthenticationIdentityClientId() != null
            && !agentConfig.getAuthenticationIdentityClientId().isEmpty()) {
            params.put("agent-authentication-identity-client-id", agentConfig.getAuthenticationIdentityClientId());
        }
        if (agentConfig.getFoundryResourceOverride() != null && !agentConfig.getFoundryResourceOverride().isEmpty()) {
            params.put("foundry-resource-override", agentConfig.getFoundryResourceOverride());
        }
        return params;
    }

    /**
     * Merges two HttpHeaders objects, with custom headers taking precedence.
     *
     * @param baseHeaders The base headers.
     * @param customHeaders The custom headers to merge.
     * @return The merged HttpHeaders.
     */
    private HttpHeaders mergeHeaders(HttpHeaders baseHeaders, HttpHeaders customHeaders) {
        HttpHeaders merged = new HttpHeaders();
        if (baseHeaders != null) {
            for (HttpHeader header : baseHeaders) {
                merged.set(HttpHeaderName.fromString(header.getName()), header.getValue());
            }
        }
        if (customHeaders != null) {
            for (HttpHeader header : customHeaders) {
                merged.set(HttpHeaderName.fromString(header.getName()), header.getValue());
            }
        }
        return merged;
    }

    /**
     * Converts an HTTP endpoint to a WebSocket endpoint with additional custom query parameters.
     *
     * @param httpEndpoint The HTTP endpoint to convert.
     * @param model The model name to include in the query string.
     * @param additionalQueryParams Additional custom query parameters to include.
     * @return The WebSocket endpoint URI.
     */
    private URI convertToWebSocketEndpoint(URI httpEndpoint, String model, Map<String, String> additionalQueryParams) {
        try {
            String scheme;
            switch (httpEndpoint.getScheme().toLowerCase()) {
                case "wss":
                case "ws":
                    scheme = httpEndpoint.getScheme();
                    break;

                case "https":
                    scheme = "wss";
                    break;

                case "http":
                    scheme = "ws";
                    break;

                default:
                    throw LOGGER.logExceptionAsError(
                        new IllegalArgumentException("Scheme " + httpEndpoint.getScheme() + " is not supported"));
            }

            String path = httpEndpoint.getPath();
            if (!path.endsWith("/realtime")) {
                path = path.replaceAll("/$", "") + "/voice-live/realtime";
            }

            // Build query parameter map to avoid duplicates
            Map<String, String> queryParams = new LinkedHashMap<>();

            // Start with existing query parameters from the endpoint URL
            if (httpEndpoint.getQuery() != null && !httpEndpoint.getQuery().isEmpty()) {
                String[] pairs = httpEndpoint.getQuery().split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    if (idx > 0) {
                        String key = pair.substring(0, idx);
                        String value = pair.substring(idx + 1);
                        queryParams.put(key, value);
                    }
                }
            }

            // Add/override with custom query parameters from request options
            if (additionalQueryParams != null && !additionalQueryParams.isEmpty()) {
                queryParams.putAll(additionalQueryParams);
            }

            // Ensure api-version is set (SDK's version takes precedence)
            queryParams.put("api-version", apiVersion);

            // Add model if provided (function parameter takes precedence)
            if (model != null && !model.isEmpty()) {
                queryParams.put("model", model);
            }

            // Build final query string
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }

            return new URI(scheme, httpEndpoint.getUserInfo(), httpEndpoint.getHost(), httpEndpoint.getPort(), path,
                queryBuilder.length() > 0 ? queryBuilder.toString() : null, httpEndpoint.getFragment());
        } catch (URISyntaxException e) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Failed to convert endpoint to WebSocket URI", e));
        }
    }
}
