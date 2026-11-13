// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.implementation.realtime.WebSocketSessionTransport;
import com.azure.ai.agents.implementation.realtime.WebSocketTransportOptions;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceAgentTransport;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketOverflowStrategy;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.HttpHeaders;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VoiceAgentsWebSocketSessionClientTests {
    @Test
    public void connectionUriUsesSelectedTransport() throws Exception {
        VoiceAgentWebSocketClientConfiguration configuration = new VoiceAgentWebSocketClientConfiguration(
            URI.create("https://localhost"),
            request -> Mono.just(new com.azure.core.credential.AccessToken("token", OffsetDateTime.now().plusHours(1))),
            "v1", "test-user-agent", null, null, null);

        URI defaultUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent",
            new VoiceAgentWebSocketConnectionOptions());
        URI websocketUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent",
            new VoiceAgentWebSocketConnectionOptions().setTransportType(VoiceAgentTransport.WEBSOCKET));
        URI webRtcUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent",
            new VoiceAgentWebSocketConnectionOptions(
                new VoiceAgentWebSocketConnectionOptions().setTransportType(VoiceAgentTransport.WEBRTC)));

        assertEquals("websocket", queryParameter(defaultUri, "transport"));
        assertEquals("websocket", queryParameter(websocketUri, "transport"));
        assertEquals("webrtc", queryParameter(webRtcUri, "transport"));
    }

    @Test
    public void connectionUrisPreserveEncodedPathsAndReplaceEndpointQuery() throws Exception {
        VoiceAgentWebSocketClientConfiguration configuration = new VoiceAgentWebSocketClientConfiguration(
            URI.create("https://localhost:8443/project%20name/?discard=old&api-version=old"),
            request -> Mono.just(new com.azure.core.credential.AccessToken("token", OffsetDateTime.now().plusHours(1))),
            "v1", "test-user-agent", null, null, null);

        URI realtimeUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent/name",
            new VoiceAgentWebSocketConnectionOptions().setAgentSessionId("session & id"));
        URI liveUri = VoiceAgentWebSocketUtils.buildLiveWebSocketUri(configuration, "agent/name",
            new VoiceAgentLiveWebSocketConnectionOptions().setStructuredInputs("{\"name\":\"a & b\"}"));

        for (URI uri : new URI[] { realtimeUri, liveUri }) {
            assertEquals("wss", uri.getScheme());
            assertEquals("localhost", uri.getHost());
            assertEquals(8443, uri.getPort());
            assertEquals("v1", queryParameter(uri, "api-version"));
            assertEquals(null, queryParameter(uri, "discard"));
        }
        assertEquals("/project%20name/agents/agent%2Fname/endpoint/protocols/voice", realtimeUri.getRawPath());
        assertEquals("/project%20name/agents/agent%2Fname/endpoint/protocols/voice/live/sessions",
            liveUri.getRawPath());
        assertEquals("session & id", queryParameter(realtimeUri, "agent_session_id"));
        assertEquals("{\"name\":\"a & b\"}", queryParameter(liveUri, "structured_inputs"));
    }

    @Test
    public void syncTransportHonorsNonProxyHosts() {
        InetSocketAddress proxyAddress = new InetSocketAddress("localhost", 8080);
        ProxyOptions proxyOptions
            = new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setNonProxyHosts("localhost");
        VoiceAgentWebSocketClientConfiguration configuration = new VoiceAgentWebSocketClientConfiguration(
            URI.create("https://localhost"),
            request -> Mono.just(new com.azure.core.credential.AccessToken("token", OffsetDateTime.now().plusHours(1))),
            "v1", "test-user-agent", null, proxyOptions, null);
        OkHttpClient client = WebSocketSessionTransport.createHttpClient(configuration,
            new WebSocketTransportOptions(new VoiceAgentWebSocketConnectionOptions()));

        List<Proxy> bypassed = client.proxySelector().select(URI.create("https://LOCALHOST/session"));
        List<Proxy> proxied = client.proxySelector().select(URI.create("https://example.com/session"));

        assertEquals(Proxy.NO_PROXY, bypassed.get(0));
        assertEquals(new Proxy(Proxy.Type.HTTP, proxyAddress), proxied.get(0));
    }

    @Test
    public void syncConnectionPreservesTokenFailure() {
        IllegalStateException failure = new IllegalStateException("Token unavailable");
        VoiceAgentWebSocketClientConfiguration configuration = new VoiceAgentWebSocketClientConfiguration(
            URI.create("https://localhost"), request -> Mono.error(failure), "v1", "test-user-agent", null, null, null);

        IllegalStateException actual = assertThrows(IllegalStateException.class,
            () -> WebSocketSessionTransport.connect(configuration, URI.create("wss://localhost/session"),
                new WebSocketTransportOptions(new VoiceAgentWebSocketConnectionOptions()), null,
                token -> new HttpHeaders()));

        assertSame(failure, actual);
    }

    @Test
    public void syncConnectionPreservesHeaderInitializationFailure() {
        IllegalStateException failure = new IllegalStateException("Headers unavailable");
        VoiceAgentWebSocketClientConfiguration configuration = new VoiceAgentWebSocketClientConfiguration(
            URI.create("https://localhost"),
            request -> Mono.just(new com.azure.core.credential.AccessToken("token", OffsetDateTime.now().plusHours(1))),
            "v1", "test-user-agent", null, null, null);

        IllegalStateException actual = assertThrows(IllegalStateException.class,
            () -> WebSocketSessionTransport.connect(configuration, URI.create("wss://localhost/session"),
                new WebSocketTransportOptions(new VoiceAgentWebSocketConnectionOptions()), null, token -> {
                    throw failure;
                }));

        assertSame(failure, actual);
    }

    @Test
    public void liveOptionsAndTransportSnapshotAreIndependent() {
        Consumer<Throwable> handler = error -> {
        };
        VoiceAgentLiveWebSocketConnectionOptions options
            = new VoiceAgentLiveWebSocketConnectionOptions().setReceiveBufferCapacity(17)
                .setMaxMessageSize(4096)
                .setOverflowStrategy(VoiceAgentWebSocketOverflowStrategy.DROP_OLDEST)
                .setMalformedEventHandler(handler)
                .setHandshakeTimeout(Duration.ofSeconds(2))
                .setCloseTimeout(Duration.ofSeconds(3))
                .setStoreEnabled(true)
                .setAgentVersionOverride("version-1")
                .setStructuredInputs("{\"name\":\"value\"}");
        VoiceAgentLiveWebSocketConnectionOptions copy = new VoiceAgentLiveWebSocketConnectionOptions(options);
        WebSocketTransportOptions snapshot = new WebSocketTransportOptions(options);
        options.setReceiveBufferCapacity(1)
            .setMaxMessageSize(1)
            .setOverflowStrategy(VoiceAgentWebSocketOverflowStrategy.ERROR)
            .setMalformedEventHandler(null)
            .setHandshakeTimeout(Duration.ofSeconds(10))
            .setCloseTimeout(Duration.ofSeconds(10))
            .setStoreEnabled(false)
            .setAgentVersionOverride(null)
            .setStructuredInputs(null);

        assertTransportSnapshot(snapshot, handler);
        assertTransportSnapshot(new WebSocketTransportOptions(copy), handler);
        assertEquals(true, copy.isStoreEnabled());
        assertEquals("version-1", copy.getAgentVersionOverride());
        assertEquals("{\"name\":\"value\"}", copy.getStructuredInputs());
    }

    @Test
    public void realtimeTransportSnapshotIsIndependent() {
        Consumer<Throwable> handler = error -> {
        };
        VoiceAgentWebSocketConnectionOptions options
            = new VoiceAgentWebSocketConnectionOptions().setReceiveBufferCapacity(17)
                .setMaxMessageSize(4096)
                .setOverflowStrategy(VoiceAgentWebSocketOverflowStrategy.DROP_OLDEST)
                .setMalformedEventHandler(handler)
                .setHandshakeTimeout(Duration.ofSeconds(2))
                .setCloseTimeout(Duration.ofSeconds(3));
        WebSocketTransportOptions snapshot = new WebSocketTransportOptions(options);
        options.setReceiveBufferCapacity(1)
            .setMaxMessageSize(1)
            .setOverflowStrategy(VoiceAgentWebSocketOverflowStrategy.ERROR)
            .setMalformedEventHandler(null)
            .setHandshakeTimeout(Duration.ofSeconds(10))
            .setCloseTimeout(Duration.ofSeconds(10));

        assertTransportSnapshot(snapshot, handler);
    }

    @Test
    public void liveOptionsValidateTransportSettings() {
        VoiceAgentLiveWebSocketConnectionOptions options = new VoiceAgentLiveWebSocketConnectionOptions();
        assertEquals(256, options.getReceiveBufferCapacity());
        assertEquals(32 * 1024 * 1024, options.getMaxMessageSize());
        assertEquals(VoiceAgentWebSocketOverflowStrategy.ERROR, options.getOverflowStrategy());
        assertEquals(Duration.ofSeconds(30), options.getHandshakeTimeout());
        assertEquals(Duration.ofSeconds(10), options.getCloseTimeout());
        assertThrows(IllegalArgumentException.class, () -> options.setReceiveBufferCapacity(0));
        assertThrows(IllegalArgumentException.class, () -> options.setReceiveBufferCapacity(65537));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxMessageSize(0));
        assertThrows(NullPointerException.class, () -> options.setOverflowStrategy(null));
        assertThrows(IllegalArgumentException.class, () -> options.setHandshakeTimeout(null));
        assertThrows(IllegalArgumentException.class, () -> options.setHandshakeTimeout(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.setCloseTimeout(Duration.ofSeconds(-1)));
        assertThrows(NullPointerException.class, () -> new VoiceAgentLiveWebSocketConnectionOptions(null));
    }

    private static void assertTransportSnapshot(WebSocketTransportOptions snapshot, Consumer<Throwable> handler) {
        assertEquals(17, snapshot.getReceiveBufferCapacity());
        assertEquals(4096, snapshot.getMaxMessageSize());
        assertEquals(VoiceAgentWebSocketOverflowStrategy.DROP_OLDEST, snapshot.getOverflowStrategy());
        assertSame(handler, snapshot.getMalformedEventHandler());
        assertEquals(Duration.ofSeconds(2), snapshot.getHandshakeTimeout());
        assertEquals(Duration.ofSeconds(3), snapshot.getCloseTimeout());
    }

    private static String queryParameter(URI uri, String name) throws Exception {
        for (String parameter : uri.getRawQuery().split("&")) {
            String[] pair = parameter.split("=", 2);
            if (name.equals(pair[0])) {
                return pair.length == 2 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()) : "";
            }
        }
        return null;
    }
}
