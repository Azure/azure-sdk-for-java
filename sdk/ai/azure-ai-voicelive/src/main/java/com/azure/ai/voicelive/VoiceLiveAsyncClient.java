// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.RequestSession;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.BinaryData;
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
    private final HttpPipeline pipeline;
    private final String apiVersion;
    private final HttpHeaders additionalHeaders;

    /**
     * Creates a VoiceLiveAsyncClient with API key authentication.
     *
     * @param endpoint The service endpoint.
     * @param keyCredential The API key credential.
     * @param pipeline The HTTP pipeline.
     * @param apiVersion The API version.
     * @param additionalHeaders Additional headers to include in requests.
     */
    VoiceLiveAsyncClient(URI endpoint, AzureKeyCredential keyCredential, HttpPipeline pipeline, String apiVersion,
        HttpHeaders additionalHeaders) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = Objects.requireNonNull(keyCredential, "'keyCredential' cannot be null");
        this.tokenCredential = null;
        this.pipeline = Objects.requireNonNull(pipeline, "'pipeline' cannot be null");
        this.apiVersion = Objects.requireNonNull(apiVersion, "'apiVersion' cannot be null");
        this.additionalHeaders = additionalHeaders != null ? additionalHeaders : new HttpHeaders();
    }

    /**
     * Creates a VoiceLiveAsyncClient with token authentication.
     *
     * @param endpoint The service endpoint.
     * @param tokenCredential The token credential.
     * @param pipeline The HTTP pipeline.
     * @param apiVersion The API version.
     * @param additionalHeaders Additional headers to include in requests.
     */
    VoiceLiveAsyncClient(URI endpoint, TokenCredential tokenCredential, HttpPipeline pipeline, String apiVersion,
        HttpHeaders additionalHeaders) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.keyCredential = null;
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' cannot be null");
        this.pipeline = Objects.requireNonNull(pipeline, "'pipeline' cannot be null");
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
                session = new VoiceLiveSession(this, wsEndpoint, keyCredential);
            } else {
                session = new VoiceLiveSession(this, wsEndpoint, tokenCredential);
            }
            return session.connect(additionalHeaders).thenReturn(session);
        });
    }

    /**
     * Starts a new VoiceLiveSession with specified session configuration.
     *
     * @param sessionOptions The configuration for the session.
     * @return A Mono containing the connected VoiceLiveSession.
     */
    public Mono<VoiceLiveSession> startSession(VoiceLiveSessionOptions sessionOptions) {
        Objects.requireNonNull(sessionOptions, "'sessionOptions' cannot be null");
        Objects.requireNonNull(sessionOptions.getModel(), "'model' in sessionOptions cannot be null");

        return startSession(sessionOptions.getModel()).flatMap(session -> {
            RequestSession requestSession = convertToRequestSession(sessionOptions);
            ClientEventSessionUpdate sessionUpdateEvent = new ClientEventSessionUpdate(requestSession);
            return session.sendCommand(sessionUpdateEvent).thenReturn(session);
        });
    }

    /**
     * Converts VoiceLiveSessionOptions to RequestSession.
     *
     * @param options The session options to convert.
     * @return A RequestSession instance.
     */
    private RequestSession convertToRequestSession(VoiceLiveSessionOptions options) {
        RequestSession requestSession = new RequestSession();

        if (options.getModel() != null) {
            requestSession.setModel(options.getModel());
        }
        if (options.getModalities() != null) {
            requestSession.setModalities(options.getModalities());
        }
        if (options.getInstructions() != null) {
            requestSession.setInstructions(options.getInstructions());
        }
        if (options.getVoice() != null) {
            requestSession.setVoice(options.getVoice());
        }
        if (options.getInputAudioFormat() != null) {
            requestSession.setInputAudioFormat(options.getInputAudioFormat());
        }
        if (options.getOutputAudioFormat() != null) {
            requestSession.setOutputAudioFormat(options.getOutputAudioFormat());
        }
        if (options.getTurnDetection() != null) {
            requestSession.setTurnDetection(options.getTurnDetection());
        }
        if (options.getTools() != null) {
            requestSession.setTools(options.getTools());
        }
        if (options.getToolChoice() != null) {
            requestSession.setToolChoice(BinaryData.fromString(options.getToolChoice()));
        }
        if (options.getTemperature() != null) {
            requestSession.setTemperature(options.getTemperature());
        }
        if (options.getMaxResponseOutputTokens() != null) {
            requestSession.setMaxResponseOutputTokens(BinaryData.fromObject(options.getMaxResponseOutputTokens()));
        }

        return requestSession;
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
                path = path.replaceAll("/$", "") + "/voice-agent/realtime";
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
