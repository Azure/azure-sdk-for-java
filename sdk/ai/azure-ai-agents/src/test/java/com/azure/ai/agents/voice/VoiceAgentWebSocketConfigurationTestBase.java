// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class VoiceAgentWebSocketConfigurationTestBase {
    static final String TLS_RESOURCE_LOCK = "voice-agent-websocket-tls";

    private DisposableServer server;

    @BeforeAll
    static void installTlsCertificate() {
        VoiceAgentWebSocketSessionTests.installTlsCertificate();
    }

    @AfterEach
    void disposeServer() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @AfterAll
    static void restoreTlsConfiguration() {
        VoiceAgentWebSocketSessionTests.restoreTlsConfiguration();
    }

    @Test
    void handshakeUsesSdkManagedProtocolValues() {
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<io.netty.handler.codec.http.HttpHeaders> headers = new AtomicReference<>();
        server = tlsServer().host("localhost").port(0).handle((request, response) -> {
            requestUri.set(request.uri());
            headers.set(request.requestHeaders().copy());
            return response.sendWebsocket((inbound, outbound) -> inbound.receive().then(),
                WebsocketServerSpec.builder().protocols("realtime").build());
        }).bindNow();
        AgentsClientBuilder builder
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project/")
                .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))))
                .configuration(Configuration.NONE)
                .httpLogOptions(new HttpLogOptions().setApplicationId("log-app"))
                .clientOptions(new ClientOptions().setApplicationId("client-app")
                    .setHeaders(Arrays.asList(new Header("X-Custom", "custom-value"),
                        new Header("Authorization", "Basic override"), new Header("User-Agent", "override-agent"),
                        new Header("Foundry-Features", "override-feature"),
                        new Header("Sec-WebSocket-Protocol", "override-protocol"))));

        openAndClose(builder, "agent name");

        assertEquals("Bearer test-token", headers.get().get(HttpHeaderNames.AUTHORIZATION));
        assertEquals("realtime", headers.get().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL));
        assertEquals("VoiceAgents=V1Preview", headers.get().get("Foundry-Features"));
        assertEquals("custom-value", headers.get().get("X-Custom"));
        assertEquals(1, headers.get().getAll(HttpHeaderNames.USER_AGENT).size());
        String userAgent = headers.get().get(HttpHeaderNames.USER_AGENT);
        assertTrue(userAgent.startsWith("client-app azsdk-java-azure-ai-agents/"), userAgent);
        assertFalse(userAgent.contains("log-app"), userAgent);
        assertFalse(userAgent.contains("override-agent"), userAgent);
        String uri = decode(requestUri.get());
        assertTrue(uri.contains("api-version=v1"));
        assertTrue(uri.contains("transport=websocket"));
        assertTrue(uri.contains("x-ms-client-sdk=" + userAgent));
        assertTrue(uri.startsWith("/api/projects/project/agents/agent name/endpoint/protocols/voice?"));
    }

    @Test
    void handshakeUsesHttpLogApplicationIdFallback() {
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<String> userAgent = new AtomicReference<>();
        server = tlsServer().host("localhost").port(0).handle((request, response) -> {
            requestUri.set(request.uri());
            userAgent.set(request.requestHeaders().get(HttpHeaderNames.USER_AGENT));
            return response.sendWebsocket((inbound, outbound) -> inbound.receive().then(),
                WebsocketServerSpec.builder().protocols("realtime").build());
        }).bindNow();
        AgentsClientBuilder builder
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))))
                .configuration(Configuration.NONE)
                .httpLogOptions(new HttpLogOptions().setApplicationId("log-app"))
                .clientOptions(new ClientOptions());

        openAndClose(builder, "agent");

        assertTrue(userAgent.get().startsWith("log-app azsdk-java-azure-ai-agents/"), userAgent.get());
        assertTrue(decode(requestUri.get()).contains("x-ms-client-sdk=" + userAgent.get()));
    }

    abstract void openAndClose(AgentsClientBuilder builder, String agentName);

    private static HttpServer tlsServer() {
        return VoiceAgentWebSocketSessionTests.tlsServer();
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }
}
