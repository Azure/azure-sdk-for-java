// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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

    /**
     * Creates a VoiceLiveAsyncClient with API key authentication.
     *
     * @param endpoint The service endpoint.
     * @param keyCredential The API key credential.
     * @param apiVersion The API version.
     * @param additionalHeaders Additional headers to include in requests.
     */
    VoiceLiveAsyncClient(URI endpoint, KeyCredential keyCredential, String apiVersion, HttpHeaders additionalHeaders) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null");
        this.tokenCredential = null;
        this.apiVersion = Objects.requireNonNull(apiVersion, "'apiVersion' cannot be null");
        this.additionalHeaders = additionalHeaders != null ? additionalHeaders : new HttpHeaders();
    }

    /**
     * Creates a VoiceLiveAsyncClient with token authentication.
     *
     * @param endpoint The service endpoint.
     * @param tokenCredential The token credential.
     * @param apiVersion The API version.
     * @param additionalHeaders Additional headers to include in requests.
     */
    VoiceLiveAsyncClient(URI endpoint, TokenCredential tokenCredential, String apiVersion,
        HttpHeaders additionalHeaders) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = null;
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null");
        this.apiVersion = Objects.requireNonNull(apiVersion, "'apiVersion' cannot be null");
        this.additionalHeaders = additionalHeaders != null ? additionalHeaders : new HttpHeaders();
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication.
     *
     * @param model The model to use for the session.
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     * @throws NullPointerException if {@code model} is null.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession(String model) {
        Objects.requireNonNull(model, "'model' cannot be null");

        return Mono.fromCallable(() -> convertToWebSocketEndpoint(endpoint, model)).flatMap(wsEndpoint -> {
            VoiceLiveSessionAsyncClient session;
            if (keyCredential != null) {
                session = new VoiceLiveSessionAsyncClient(wsEndpoint, keyCredential);
            } else {
                session = new VoiceLiveSessionAsyncClient(wsEndpoint, tokenCredential);
            }
            return session.connect(additionalHeaders).thenReturn(session);
        });
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication without specifying a model.
     * The model can be provided via custom query parameters or through the endpoint URL if required by the service.
     *
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession() {
        return Mono.fromCallable(() -> convertToWebSocketEndpoint(endpoint, null)).flatMap(wsEndpoint -> {
            VoiceLiveSessionAsyncClient session;
            if (keyCredential != null) {
                session = new VoiceLiveSessionAsyncClient(wsEndpoint, keyCredential);
            } else {
                session = new VoiceLiveSessionAsyncClient(wsEndpoint, tokenCredential);
            }
            return session.connect(additionalHeaders).thenReturn(session);
        });
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication with custom request options.
     *
     * @param model The model to use for the session.
     * @param requestOptions Custom query parameters and headers for the request.
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     * @throws NullPointerException if {@code model} or {@code requestOptions} is null.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession(String model, VoiceLiveRequestOptions requestOptions) {
        Objects.requireNonNull(model, "'model' cannot be null");
        Objects.requireNonNull(requestOptions, "'requestOptions' cannot be null");

        return Mono
            .fromCallable(() -> convertToWebSocketEndpoint(endpoint, model, requestOptions.getCustomQueryParameters()))
            .flatMap(wsEndpoint -> {
                VoiceLiveSessionAsyncClient session;
                if (keyCredential != null) {
                    session = new VoiceLiveSessionAsyncClient(wsEndpoint, keyCredential);
                } else {
                    session = new VoiceLiveSessionAsyncClient(wsEndpoint, tokenCredential);
                }
                // Merge additional headers with custom headers from requestOptions
                HttpHeaders mergedHeaders = mergeHeaders(additionalHeaders, requestOptions.getCustomHeaders());
                return session.connect(mergedHeaders).thenReturn(session);
            });
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication with custom request options.
     * The model can be provided via custom query parameters.
     *
     * @param requestOptions Custom query parameters and headers for the request.
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     * @throws NullPointerException if {@code requestOptions} is null.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession(VoiceLiveRequestOptions requestOptions) {
        Objects.requireNonNull(requestOptions, "'requestOptions' cannot be null");

        return Mono
            .fromCallable(() -> convertToWebSocketEndpoint(endpoint, null, requestOptions.getCustomQueryParameters()))
            .flatMap(wsEndpoint -> {
                VoiceLiveSessionAsyncClient session;
                if (keyCredential != null) {
                    session = new VoiceLiveSessionAsyncClient(wsEndpoint, keyCredential);
                } else {
                    session = new VoiceLiveSessionAsyncClient(wsEndpoint, tokenCredential);
                }
                // Merge additional headers with custom headers from requestOptions
                HttpHeaders mergedHeaders = mergeHeaders(additionalHeaders, requestOptions.getCustomHeaders());
                return session.connect(mergedHeaders).thenReturn(session);
            });
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication with an Azure AI Foundry agent.
     *
     * <p>This method configures the session to connect directly to an Azure AI Foundry agent,
     * using the agent configuration to set the appropriate query parameters.</p>
     *
     * @param agentConfig The agent session configuration containing the agent name, project name,
     *                    and optional parameters.
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     * @throws NullPointerException if {@code agentConfig} is null.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession(AgentSessionConfig agentConfig) {
        Objects.requireNonNull(agentConfig, "'agentConfig' cannot be null");

        return Mono.fromCallable(() -> convertToWebSocketEndpoint(endpoint, null, agentConfig.toQueryParameters()))
            .flatMap(wsEndpoint -> {
                VoiceLiveSessionAsyncClient session;
                if (keyCredential != null) {
                    session = new VoiceLiveSessionAsyncClient(wsEndpoint, keyCredential);
                } else {
                    session = new VoiceLiveSessionAsyncClient(wsEndpoint, tokenCredential);
                }
                return session.connect(additionalHeaders).thenReturn(session);
            });
    }

    /**
     * Starts a new VoiceLiveSessionAsyncClient for real-time voice communication with an Azure AI Foundry agent
     * and custom request options.
     *
     * <p>This method configures the session to connect directly to an Azure AI Foundry agent,
     * combining the agent configuration with additional custom options.</p>
     *
     * @param agentConfig The agent session configuration containing the agent name, project name,
     *                    and optional parameters.
     * @param requestOptions Custom query parameters and headers for the request.
     * @return A Mono containing the connected VoiceLiveSessionAsyncClient.
     * @throws NullPointerException if {@code agentConfig} or {@code requestOptions} is null.
     */
    public Mono<VoiceLiveSessionAsyncClient> startSession(AgentSessionConfig agentConfig,
        VoiceLiveRequestOptions requestOptions) {
        Objects.requireNonNull(agentConfig, "'agentConfig' cannot be null");
        Objects.requireNonNull(requestOptions, "'requestOptions' cannot be null");

        // Merge agent config params with custom query params (custom params take precedence)
        Map<String, String> mergedParams = new LinkedHashMap<>(agentConfig.toQueryParameters());
        if (requestOptions.getCustomQueryParameters() != null) {
            mergedParams.putAll(requestOptions.getCustomQueryParameters());
        }

        return Mono.fromCallable(() -> convertToWebSocketEndpoint(endpoint, null, mergedParams)).flatMap(wsEndpoint -> {
            VoiceLiveSessionAsyncClient session;
            if (keyCredential != null) {
                session = new VoiceLiveSessionAsyncClient(wsEndpoint, keyCredential);
            } else {
                session = new VoiceLiveSessionAsyncClient(wsEndpoint, tokenCredential);
            }
            // Merge additional headers with custom headers from requestOptions
            HttpHeaders mergedHeaders = mergeHeaders(additionalHeaders, requestOptions.getCustomHeaders());
            return session.connect(mergedHeaders).thenReturn(session);
        });
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
     * Converts an HTTP endpoint to a WebSocket endpoint.
     *
     * @param httpEndpoint The HTTP endpoint to convert.
     * @param model The model name to include in the query string.
     * @return The WebSocket endpoint URI.
     */
    private URI convertToWebSocketEndpoint(URI httpEndpoint, String model) {
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
