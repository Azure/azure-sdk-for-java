// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.models.VoiceAgentTransport;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;

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
        StringBuilder query = new StringBuilder("api-version=").append(encode(configuration.getApiVersion()))
            .append("&x-ms-client-sdk=")
            .append(encode(configuration.getUserAgent()));
        VoiceAgentTransport transport = options.getTransport();
        if (transport != null) {
            query.append("&transport=").append(encode(transport.toString()));
        }
        if (options.isStoreEnabled() != null) {
            query.append("&store=").append(options.isStoreEnabled());
        }
        if (options.getAgentVersionOverride() != null) {
            query.append("&x-agent-version-override=").append(encode(options.getAgentVersionOverride()));
        }

        return URI.create(scheme + "://" + endpoint.getRawAuthority() + path + "?" + query);
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException error) {
            throw new IllegalStateException("UTF-8 encoding is unavailable.", error);
        }
    }
}
