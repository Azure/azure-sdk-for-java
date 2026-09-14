// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.models.VoiceAgentTransport;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.UrlBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class VoiceAgentWebSocketUtils {
    static final String TOKEN_SCOPE = "https://ai.azure.com/.default";
    static final String PREVIEW_FEATURE = "VoiceAgents=V1Preview";
    static final String SUBPROTOCOL = "realtime";
    static final int INBOUND_CAPACITY = 256;

    private VoiceAgentWebSocketUtils() {
    }

    static boolean isProtectedHeader(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return "authorization".equals(lower)
            || "host".equals(lower)
            || "upgrade".equals(lower)
            || "connection".equals(lower)
            || "foundry-features".equals(lower)
            || lower.startsWith("sec-websocket-");
    }

    static URI buildWebSocketUri(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        URI endpoint = configuration.getEndpoint();
        String scheme;
        if ("https".equalsIgnoreCase(endpoint.getScheme()) || "wss".equalsIgnoreCase(endpoint.getScheme())) {
            scheme = "wss";
        } else if ("http".equalsIgnoreCase(endpoint.getScheme()) || "ws".equalsIgnoreCase(endpoint.getScheme())) {
            scheme = "ws";
        } else {
            throw new IllegalArgumentException("Unsupported endpoint scheme: " + endpoint.getScheme());
        }

        String basePath = endpoint.getRawPath() == null ? "" : endpoint.getRawPath().replaceAll("/$", "");
        String path = basePath + "/agents/" + encode(agentName) + "/endpoint/protocols/voice";
        URI baseUri = URI.create(scheme + "://" + endpoint.getRawAuthority() + path);
        if (options.getConnectionUrl() != null) {
            baseUri = options.getConnectionUrl();
            int endpointPort = endpoint.getPort() == -1 ? 443 : endpoint.getPort();
            int overridePort = baseUri.getPort() == -1 ? 443 : baseUri.getPort();
            if (!"wss".equalsIgnoreCase(baseUri.getScheme())
                || baseUri.getHost() == null
                || !baseUri.getHost().equalsIgnoreCase(endpoint.getHost())
                || endpointPort != overridePort
                || baseUri.getRawUserInfo() != null
                || baseUri.getRawFragment() != null) {
                throw new IllegalArgumentException(
                    "Connection URL must be a wss URL on the project endpoint's host and port, without user information or a fragment.");
            }
        }
        UrlBuilder url = UrlBuilder.parse(baseUri.toString());
        url.setQueryParameter("api-version",
            encode(options.getApiVersion() == null ? configuration.getApiVersion() : options.getApiVersion()));
        url.setQueryParameter("x-ms-client-sdk", encode(configuration.getUserAgent()));
        VoiceAgentTransport transport = options.getTransport();
        if (transport != null) {
            url.setQueryParameter("transport", encode(transport.toString()));
        }
        if (options.isStoreEnabled() != null) {
            url.setQueryParameter("store", options.isStoreEnabled().toString());
        }
        if (options.getAgentVersionOverride() != null) {
            url.setQueryParameter("x-agent-version-override", encode(options.getAgentVersionOverride()));
        }
        if (options.getAgentSessionId() != null) {
            url.setQueryParameter("agent_session_id", encode(options.getAgentSessionId()));
        }
        options.getExtraQuery().forEach((name, value) -> url.setQueryParameter(encode(name), encode(value)));
        return URI.create(url.toString());
    }

    static TokenRequestContext createTokenRequestContext(VoiceAgentWebSocketConnectionOptions options) {
        return options.getCredentialScopes() == null || options.getCredentialScopes().isEmpty()
            ? new TokenRequestContext().addScopes(TOKEN_SCOPE)
            : new TokenRequestContext().setScopes(options.getCredentialScopes());
    }

    static HttpHeaders buildHeaders(VoiceAgentWebSocketClientConfiguration configuration,
        VoiceAgentWebSocketConnectionOptions options, String token) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.USER_AGENT, configuration.getUserAgent());
        if (configuration.getHeaders() != null) {
            for (HttpHeader header : configuration.getHeaders()) {
                if (!isProtectedHeader(header.getName())) {
                    headers.set(HttpHeaderName.fromString(header.getName()), header.getValue());
                }
            }
        }
        headers.set(HttpHeaderName.fromString("Foundry-Features"), options.getFoundryFeatures());
        if (options.getStructuredInputs() != null) {
            headers.set(HttpHeaderName.fromString("x-ms-voice-structured-inputs"), options.getStructuredInputs());
        }
        options.getExtraHeaders().forEach((name, value) -> {
            if (!isProtectedHeader(name) || "Foundry-Features".equalsIgnoreCase(name)) {
                headers.set(HttpHeaderName.fromString(name), value);
            }
        });
        return headers.set(HttpHeaderName.AUTHORIZATION, "Bearer " + token);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException error) {
            throw new IllegalStateException("UTF-8 encoding is unavailable.", error);
        }
    }
}
