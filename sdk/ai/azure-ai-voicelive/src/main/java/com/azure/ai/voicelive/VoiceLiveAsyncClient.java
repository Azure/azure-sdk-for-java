// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * The VoiceLiveAsyncClient provides methods to create and manage real-time voice communication sessions
 * with the Azure VoiceLive service.
 */
@ServiceClient(builder = VoiceLiveClientBuilder.class, isAsync = true)
public final class VoiceLiveAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveAsyncClient.class);

    private final URI endpoint;
    private final AzureKeyCredential keyCredential;
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
    VoiceLiveAsyncClient(URI endpoint, AzureKeyCredential keyCredential, String apiVersion,
        HttpHeaders additionalHeaders) {
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
     * Starts a new VoiceLiveSession for real-time voice communication.
     *
     * @param model The model to use for the session.
     * @return A Mono containing the connected VoiceLiveSession.
     */
    public Mono<VoiceLiveSession> startSession(String model) {
        Objects.requireNonNull(model, "'model' cannot be null");

        return Mono.fromCallable(() -> convertToWebSocketEndpoint(endpoint, model)).flatMap(wsEndpoint -> {
            VoiceLiveSession session;
            if (keyCredential != null) {
                session = new VoiceLiveSession(wsEndpoint, keyCredential);
            } else {
                session = new VoiceLiveSession(wsEndpoint, tokenCredential);
            }
            return session.connect(additionalHeaders).thenReturn(session);
        });
    }

    /**
     * Gets the service endpoint.
     *
     * @return The service endpoint.
     */
    public URI getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the API version.
     *
     * @return The API version.
     */
    String getApiVersion() {
        return apiVersion;
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

            // Build query string
            StringBuilder queryBuilder = new StringBuilder();
            if (httpEndpoint.getQuery() != null && !httpEndpoint.getQuery().isEmpty()) {
                queryBuilder.append(httpEndpoint.getQuery());
            }

            // Add api-version if not present
            if (!queryBuilder.toString().contains("api-version=")) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append("api-version=").append(apiVersion);
            }

            // Add model if not present
            if (!queryBuilder.toString().contains("model=")) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append("model=").append(model);
            }

            return new URI(scheme, httpEndpoint.getUserInfo(), httpEndpoint.getHost(), httpEndpoint.getPort(), path,
                queryBuilder.length() > 0 ? queryBuilder.toString() : null, httpEndpoint.getFragment());
        } catch (URISyntaxException e) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("Failed to convert endpoint to WebSocket URI", e));
        }
    }
}
