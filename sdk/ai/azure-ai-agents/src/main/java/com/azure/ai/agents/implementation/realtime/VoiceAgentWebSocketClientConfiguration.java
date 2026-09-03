// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.ProxyOptions;

import java.net.URI;
import java.util.Objects;

/**
 * Immutable configuration used by the voice-agent WebSocket clients.
 */
public final class VoiceAgentWebSocketClientConfiguration {
    private final URI endpoint;
    private final TokenCredential credential;
    private final String apiVersion;
    private final String userAgent;
    private final HttpHeaders headers;
    private final ProxyOptions proxyOptions;

    /**
     * Creates the connection configuration.
     *
     * @param endpoint the Foundry project endpoint.
     * @param credential the credential used for authentication.
     * @param apiVersion the service API version.
     * @param userAgent the SDK user agent.
     * @param headers safe additional handshake headers.
     * @param proxyOptions proxy settings loaded from configuration.
     */
    public VoiceAgentWebSocketClientConfiguration(URI endpoint, TokenCredential credential, String apiVersion,
        String userAgent, HttpHeaders headers, ProxyOptions proxyOptions) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        this.credential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.apiVersion = Objects.requireNonNull(apiVersion, "'apiVersion' cannot be null.");
        this.userAgent = Objects.requireNonNull(userAgent, "'userAgent' cannot be null.");
        this.headers = headers == null ? new HttpHeaders() : headers;
        this.proxyOptions = proxyOptions;
    }

    /**
     * Gets the endpoint.
     *
     * @return the endpoint.
     */
    public URI getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the credential.
     *
     * @return the credential.
     */
    public TokenCredential getCredential() {
        return credential;
    }

    /**
     * Gets the API version.
     *
     * @return the API version.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Gets the user agent.
     *
     * @return the user agent.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Gets safe additional headers.
     *
     * @return safe additional headers.
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets proxy settings.
     *
     * @return proxy settings, or {@code null}.
     */
    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }
}
