// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import com.azure.ai.agents.models.RawRealtimeServerEvent;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceAgentTransport;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public final class VoiceAgentWebSocketUtils {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceAgentWebSocketUtils.class);

    public static <T> IterableStream<T> decodeEvents(IterableStream<BinaryData> messages,
        WebSocketEventDecoder<T> decoder, Consumer<Throwable> malformedEventHandler, Runnable closeOnError) {
        return IterableStream.of(() -> new Iterator<T>() {
            private final Iterator<BinaryData> source = messages.iterator();
            private T next;
            private boolean terminated;

            @Override
            public boolean hasNext() {
                while (next == null && !terminated && source.hasNext()) {
                    BinaryData message = source.next();
                    try {
                        next = decodeEvent(message, decoder, malformedEventHandler);
                    } catch (RuntimeException error) {
                        terminated = true;
                        closeOnError.run();
                        throw LOGGER.logExceptionAsError(error);
                    }
                }
                return next != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw LOGGER.logExceptionAsError(new NoSuchElementException());
                }
                T event = next;
                next = null;
                return event;
            }
        });
    }

    public static <T> Flux<T> decodeEvents(Flux<BinaryData> messages, WebSocketEventDecoder<T> decoder,
        Consumer<Throwable> malformedEventHandler, Runnable closeOnError) {
        return messages.handle((message, sink) -> {
            try {
                T event = decodeEvent(message, decoder, malformedEventHandler);
                if (event != null) {
                    sink.next(event);
                }
            } catch (RuntimeException error) {
                closeOnError.run();
                sink.error(error);
            }
        });
    }

    private static <T> T decodeEvent(BinaryData message, WebSocketEventDecoder<T> decoder,
        Consumer<Throwable> malformedEventHandler) {
        try {
            return decoder.decode(message.toString());
        } catch (IOException | RuntimeException error) {
            if (malformedEventHandler != null) {
                malformedEventHandler.accept(error);
                return null;
            }
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid JSON event.", error));
        }
    }

    public static String decodeEvent(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString();
    }

    public static String validateEvent(BinaryData event) {
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

    public static RealtimeServerEvent deserializeEvent(String json) throws IOException {
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

    private static final String TOKEN_SCOPE = "https://ai.azure.com/.default";
    public static final String SUBPROTOCOL = "realtime";

    private VoiceAgentWebSocketUtils() {
    }

    public static BinaryData deserializeRawEvent(String json) {
        BinaryData event = BinaryData.fromString(json);
        validateEvent(event);
        return event;
    }

    public static void validateClose(int code, String reason) {
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

    private static boolean isProtectedHeader(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return "authorization".equals(lower)
            || "user-agent".equals(lower)
            || "host".equals(lower)
            || "upgrade".equals(lower)
            || "connection".equals(lower)
            || "foundry-features".equals(lower)
            || lower.startsWith("sec-websocket-");
    }

    public static URI buildWebSocketUri(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentWebSocketConnectionOptions options) {
        UrlBuilder url = buildSessionUrl(configuration, agentName, "", options.isStoreEnabled(),
            options.getAgentVersionOverride());
        VoiceAgentTransport transport = options.getTransportType();
        url.setQueryParameter("transport", transport == null ? "websocket" : encode(transport.toString()));
        if (options.getAgentSessionId() != null) {
            url.setQueryParameter("agent_session_id", encode(options.getAgentSessionId()));
        }
        return URI.create(url.toString());
    }

    public static URI buildLiveWebSocketUri(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        VoiceAgentLiveWebSocketConnectionOptions options) {
        UrlBuilder url = buildSessionUrl(configuration, agentName, "/live/sessions", options.isStoreEnabled(),
            options.getAgentVersionOverride());
        if (options.getStructuredInputs() != null) {
            url.setQueryParameter("structured_inputs", encode(options.getStructuredInputs()));
        }
        return URI.create(url.toString());
    }

    private static UrlBuilder buildSessionUrl(VoiceAgentWebSocketClientConfiguration configuration, String agentName,
        String suffix, Boolean store, String agentVersionOverride) {
        Objects.requireNonNull(agentName, "'agentName' cannot be null.");
        if (agentName.isEmpty()) {
            throw new IllegalArgumentException("'agentName' cannot be empty.");
        }
        if (!suffix.isEmpty() && !suffix.startsWith("/")) {
            throw new IllegalArgumentException("A voice-agent route suffix must start with '/'.");
        }
        URI endpoint = configuration.getEndpoint();
        if (!"https".equalsIgnoreCase(endpoint.getScheme()) && !"wss".equalsIgnoreCase(endpoint.getScheme())) {
            throw new IllegalArgumentException(
                "Voice-agent WebSocket endpoints must use https or wss to protect credentials.");
        }
        if (endpoint.getHost() == null || endpoint.getRawUserInfo() != null || endpoint.getRawFragment() != null) {
            throw new IllegalArgumentException(
                "The project endpoint must have a host and no user information or fragment.");
        }

        String basePath = endpoint.getRawPath() == null ? "" : endpoint.getRawPath().replaceAll("/$", "");
        String path = basePath + "/agents/" + encode(agentName) + "/endpoint/protocols/voice" + suffix;
        UrlBuilder url = UrlBuilder.parse(endpoint.toString()).setScheme("wss").setPath(path);
        // Initialize the lazy query before clearing it; the pinned Azure Core version otherwise retains it.
        url.getQuery();
        url.clearQuery();
        url.setQueryParameter("api-version", encode(configuration.getApiVersion()));
        url.setQueryParameter("x-ms-client-sdk", encode(configuration.getUserAgent()));
        if (store != null) {
            url.setQueryParameter("store", store.toString());
        }
        if (agentVersionOverride != null) {
            url.setQueryParameter("x-agent-version-override", encode(agentVersionOverride));
        }
        return url;
    }

    public static TokenRequestContext createTokenRequestContext() {
        return new TokenRequestContext().addScopes(TOKEN_SCOPE);
    }

    public static HttpHeaders buildHeaders(VoiceAgentWebSocketClientConfiguration configuration,
        VoiceAgentWebSocketConnectionOptions options, String token) {
        HttpHeaders headers = buildHeaders(configuration, token);
        if (options.getStructuredInputs() != null) {
            headers.set(HttpHeaderName.fromString("x-ms-voice-structured-inputs"), options.getStructuredInputs());
        }
        return headers;
    }

    public static HttpHeaders buildHeaders(VoiceAgentWebSocketClientConfiguration configuration, String token) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.USER_AGENT, configuration.getUserAgent());
        if (configuration.getHeaders() != null) {
            for (HttpHeader header : configuration.getHeaders()) {
                if (!isProtectedHeader(header.getName())) {
                    headers.set(HttpHeaderName.fromString(header.getName()), header.getValue());
                }
            }
        }
        if (configuration.getPreviewFeatures() != null) {
            headers.set(HttpHeaderName.fromString("Foundry-Features"), configuration.getPreviewFeatures());
        }
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
