// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.models.RawRealtimeServerEvent;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentTransport;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.BinaryData;
import com.azure.core.util.UrlBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

final class VoiceAgentWebSocketUtils {
    static String decodeEvent(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString();
    }

    static String validateEvent(BinaryData event) {
        String json = Objects.requireNonNull(event, "'event' cannot be null.").toString();
        try (JsonReader reader = JsonProviders.createReader(json)) {
            if (reader.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("A realtime event must be a JSON object.");
            }
            reader.readUntyped();
            if (reader.nextToken() != JsonToken.END_DOCUMENT) {
                throw new IllegalArgumentException("A realtime event must contain only one JSON object.");
            }
            return json;
        } catch (IOException error) {
            throw new IllegalArgumentException("Invalid realtime JSON event.", error);
        }
    }

    static RealtimeServerEvent deserializeEvent(String json) throws IOException {
        validateEvent(BinaryData.fromString(json));
        RawRealtimeServerEvent raw = new RawRealtimeServerEvent(BinaryData.fromString(json));
        if (raw.getType() == null) {
            return raw;
        }
        try (JsonReader reader = JsonProviders.createReader(json)) {
            RealtimeServerEvent event = RealtimeServerEvent.fromJson(reader);
            return event.getClass() == RealtimeServerEvent.class ? raw : event;
        }
    }

    static final String TOKEN_SCOPE = "https://ai.azure.com/.default";
    static final String PREVIEW_FEATURE = "VoiceAgents=V1Preview";
    static final String SUBPROTOCOL = "realtime";
    static final int INBOUND_CAPACITY = 256;

    private VoiceAgentWebSocketUtils() {
    }

    static void validateClose(int code, String reason) {
        if (code < 1000
            || code >= 5000
            || code == 1004
            || code == 1005
            || code == 1006
            || (code >= 1015 && code < 3000)) {
            throw new IllegalArgumentException("Invalid WebSocket close code: " + code);
        }
        if (reason == null || reason.getBytes(StandardCharsets.UTF_8).length > 123) {
            throw new IllegalArgumentException("Close reason must be non-null and at most 123 UTF-8 bytes.");
        }
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
        } else {
            throw new IllegalArgumentException(
                "Voice-agent WebSocket endpoints must use https or wss to protect credentials.");
        }
        if (endpoint.getHost() == null || endpoint.getRawUserInfo() != null || endpoint.getRawFragment() != null) {
            throw new IllegalArgumentException(
                "The project endpoint must have a host and no user information or fragment.");
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
