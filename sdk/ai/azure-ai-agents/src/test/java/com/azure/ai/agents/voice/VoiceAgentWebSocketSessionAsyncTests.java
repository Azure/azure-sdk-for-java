// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentWebSocketAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentWebSocketSessionAsyncClient;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketHttpResponse;
import com.azure.ai.agents.models.RawRealtimeServerEvent;
import com.azure.ai.agents.models.RealtimeResponseCreateEvent;
import com.azure.ai.agents.models.RealtimeSessionCreatedEvent;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentWarningEvent;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketOverflowStrategy;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.WebsocketServerSpec;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock("voice-agent-websocket-tls")
public class VoiceAgentWebSocketSessionAsyncTests {
    private DisposableServer server;

    @Test
    public void handshakeUsesSdkManagedProtocolValues() {
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<io.netty.handler.codec.http.HttpHeaders> headers = new AtomicReference<>();
        server = VoiceAgentWebSocketSessionTests.tlsServer().host("localhost").port(0).handle((request, response) -> {
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

        BetaVoiceAgentWebSocketSessionAsyncClient session = builder.beta()
            .buildBetaVoiceAgentWebSocketAsyncClient()
            .openWebSocketSession("agent name")
            .block(Duration.ofSeconds(5));
        session.closeAsync().block(Duration.ofSeconds(5));
        assertFalse(session.isOpen());

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
    public void handshakeUsesHttpLogApplicationIdFallback() {
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<String> userAgent = new AtomicReference<>();
        server = VoiceAgentWebSocketSessionTests.tlsServer().host("localhost").port(0).handle((request, response) -> {
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

        BetaVoiceAgentWebSocketSessionAsyncClient session = builder.beta()
            .buildBetaVoiceAgentWebSocketAsyncClient()
            .openWebSocketSession("agent")
            .block(Duration.ofSeconds(5));
        session.closeAsync().block(Duration.ofSeconds(5));

        assertTrue(userAgent.get().startsWith("log-app azsdk-java-azure-ai-agents/"), userAgent.get());
        assertTrue(decode(requestUri.get()).contains("x-ms-client-sdk=" + userAgent.get()));
    }

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
    public void typedStringAndMappingSendsRejectInvalidJson() {
        List<String> messages = new CopyOnWriteArrayList<>();
        server = startServer(messages, new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
            new AtomicReference<>(), new AtomicReference<>(), false);
        AgentsClientBuilder builder = builder(server.port());
        String raw = "{\"type\": \"response.create\"}";
        BinaryData mapping = BinaryData.fromObject(Collections.singletonMap("type", "response.cancel"));
        BetaVoiceAgentWebSocketSessionAsyncClient session = builder.beta()
            .buildBetaVoiceAgentWebSocketAsyncClient()
            .openWebSocketSession("agent", tlsOptions())
            .block(Duration.ofSeconds(5));
        try {
            StepVerifier.create(session.sendEvent(BinaryData.fromString("not valid json")))
                .expectError(IllegalArgumentException.class)
                .verify(Duration.ofSeconds(5));
            session.sendEvent(new RealtimeResponseCreateEvent())
                .then(session.sendEvent(BinaryData.fromString(raw)))
                .then(session.sendEvent(mapping))
                .block(Duration.ofSeconds(5));
            StepVerifier.create(session.receiveEvents().take(3)).expectNextCount(3).verifyComplete();
        } finally {
            session.close();
        }
        assertEquals(3, messages.size());
        assertEquals("response.create", BinaryData.fromString(messages.get(0)).toObject(Map.class).get("type"));
        assertEquals(raw, messages.get(1));
        assertEquals(mapping.toObject(Map.class), BinaryData.fromString(messages.get(2)).toObject(Map.class));
    }

    @Test
    public void pingPongFramesAreNotApplicationEvents() {
        Flux<WebSocketFrame> frames = Flux.defer(() -> Flux.just(new PingWebSocketFrame(), new PongWebSocketFrame(),
            new TextWebSocketFrame("{\"type\":\"session.created\",\"session\":{}}"),
            new TextWebSocketFrame("{\"type\":\"future.event\",\"foo\":\"bar\"}")));
        server = frameWebSocketServer(frames, false);
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block(Duration.ofSeconds(5));
        List<RealtimeServerEvent> events;
        try {
            events = session.receiveEvents().take(2).collectList().block(Duration.ofSeconds(5));
        } finally {
            session.close();
        }
        assertEquals(2, events.size());
        assertInstanceOf(RealtimeSessionCreatedEvent.class, events.get(0));
        RawRealtimeServerEvent unknown = assertInstanceOf(RawRealtimeServerEvent.class, events.get(1));
        assertEquals("bar", unknown.getRawEvent().toObject(Map.class).get("foo"));
    }

    @Test
    public void malformedEventsCanBeReportedAndSkipped() {
        AtomicInteger failures = new AtomicInteger();
        server = frameWebSocketServer(Flux.defer(() -> Flux.just(new TextWebSocketFrame("{broken"),
            new BinaryWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { (byte) 0xc3, 0x28 })),
            new BinaryWebSocketFrame(Unpooled.copiedBuffer(warningJson(), StandardCharsets.UTF_8)),
            new TextWebSocketFrame(warningJson()))), true);
        VoiceAgentWebSocketConnectionOptions options
            = tlsOptions().setMalformedEventHandler(error -> failures.incrementAndGet());
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", options).block(Duration.ofSeconds(5));
        StepVerifier.create(session.receiveEvents())
            .assertNext(this::assertWarningEvent)
            .assertNext(this::assertWarningEvent)
            .verifyComplete();
        session.close();
        assertEquals(2, failures.get());
    }

    @Test
    public void boundedQueuesHonorOverflowPolicies() {
        for (VoiceAgentWebSocketOverflowStrategy strategy : VoiceAgentWebSocketOverflowStrategy.values()) {
            server = frameWebSocketServer(Flux.range(0, 4)
                .map(index -> new TextWebSocketFrame("{\"type\":\"future.event\",\"index\":" + index + "}")), true);
            VoiceAgentWebSocketConnectionOptions options
                = tlsOptions().setReceiveBufferCapacity(2).setOverflowStrategy(strategy);
            BetaVoiceAgentWebSocketSessionAsyncClient session
                = createAsyncClient(server.port()).openWebSocketSession("agent", options).block(Duration.ofSeconds(5));
            boolean overflowError = strategy == VoiceAgentWebSocketOverflowStrategy.ERROR;
            List<RealtimeServerEvent> received = new ArrayList<>();
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                while (session.isOpen()) {
                    Thread.yield();
                }
            });
            if (overflowError) {
                StepVerifier.create(session.receiveEvents())
                    .expectNextCount(2)
                    .expectError(IllegalStateException.class)
                    .verify();
            } else {
                received.addAll(session.receiveEvents().collectList().block(Duration.ofSeconds(5)));
            }
            session.close();
            if (!overflowError) {
                assertEquals(2, received.size());
                int first = strategy == VoiceAgentWebSocketOverflowStrategy.DROP_OLDEST ? 2 : 0;
                for (int index = 0; index < received.size(); index++) {
                    assertEquals(first + index,
                        ((RawRealtimeServerEvent) received.get(index)).getRawEvent().toObject(Map.class).get("index"));
                }
            }
            server.disposeNow();
        }
    }

    @Test
    public void messageSizeLimitCannotBeBypassedByRecoveryHandler() {
        server = oneShotWebSocketServer(warningJson());
        AtomicBoolean recovered = new AtomicBoolean();
        VoiceAgentWebSocketConnectionOptions options
            = tlsOptions().setMaxMessageSize(16).setMalformedEventHandler(error -> recovered.set(true));
        StepVerifier.create(createAsyncClient(server.port()).openWebSocketSession("agent", options)
            .flatMapMany(BetaVoiceAgentWebSocketSessionAsyncClient::receiveEvents)).expectError().verify();
        assertFalse(recovered.get());
    }

    @Test
    public void rawEventRoundTripsAndOptionsValidateBounds() throws Exception {
        BinaryData payload = BinaryData.fromString("{\"type\":\"future.event\",\"nested\":{\"value\":42}}");
        RawRealtimeServerEvent event = new RawRealtimeServerEvent(payload);
        RawRealtimeServerEvent copy = BinaryData.fromObject(event).toObject(RawRealtimeServerEvent.class);
        assertEquals(payload.toObject(Map.class), copy.getRawEvent().toObject(Map.class));
        server = oneShotWebSocketServer("{\"type\":42,\"value\":1}");
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block(Duration.ofSeconds(5));
        try {
            StepVerifier.create(session.receiveEvents())
                .assertNext(received -> assertInstanceOf(RawRealtimeServerEvent.class, received))
                .verifyComplete();
        } finally {
            session.close();
        }
        VoiceAgentWebSocketConnectionOptions options = new VoiceAgentWebSocketConnectionOptions();
        assertThrows(IllegalArgumentException.class, () -> options.setReceiveBufferCapacity(0));
        assertThrows(IllegalArgumentException.class, () -> options.setReceiveBufferCapacity(65537));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxMessageSize(0));
        assertThrows(NullPointerException.class, () -> options.setOverflowStrategy(null));
    }

    @Test
    public void insecureEndpointsAreRejectedBeforeAuthentication() {
        AtomicBoolean requested = new AtomicBoolean();
        TokenCredential credential = context -> {
            requested.set(true);
            return Mono.error(new AssertionError("Token retrieval must not run."));
        };
        for (String endpoint : new String[] {
            "http://example.com",
            "ws://example.com",
            "https://user@example.com",
            "https://example.com/#fragment" }) {
            AgentsClientBuilder clientBuilder = new AgentsClientBuilder().endpoint(endpoint).credential(credential);
            StepVerifier
                .create(clientBuilder.beta().buildBetaVoiceAgentWebSocketAsyncClient().openWebSocketSession("agent"))
                .expectError(IllegalArgumentException.class)
                .verify();
        }
        assertFalse(requested.get());
    }

    @Test
    public void rawEventsUseCustomizedTlsTransport() {
        List<String> messages = new CopyOnWriteArrayList<>();
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket(
                (inbound, outbound) -> outbound.sendString(inbound.receive().asString().doOnNext(messages::add)).then(),
                WebsocketServerSpec.builder().protocols("realtime").build()))
            .bindNow();
        BinaryData payload = BinaryData.fromString("{\"type\":\"future.event\",\"value\":42}");
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block(Duration.ofSeconds(5));
        StepVerifier.create(session.sendEvent(BinaryData.fromString("[]")))
            .expectError(IllegalArgumentException.class)
            .verify();
        StepVerifier.create(session.receiveEvents().take(1))
            .then(() -> session.sendEvent(payload).block(Duration.ofSeconds(5)))
            .assertNext(received -> assertEquals(42,
                ((RawRealtimeServerEvent) received).getRawEvent().toObject(Map.class).get("value")))
            .verifyComplete();
        session.close();
        assertEquals(1, messages.size());
    }

    @Test
    public void customCloseFrame() {
        server = startServer(new CopyOnWriteArrayList<>(), new AtomicReference<>(), new AtomicReference<>(),
            new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(), false);
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block(Duration.ofSeconds(5));
        assertNotNull(session);
        StepVerifier.create(session.closeAsync(1006, "invalid")).expectError(IllegalArgumentException.class).verify();
        session.closeAsync(4002, "done").block(Duration.ofSeconds(5));
        assertEquals(4002, session.getCloseCode());
        assertEquals("done", session.getCloseReason());
    }

    @Test
    public void asyncSessionNegotiatesHandshakeAndExchangesTypedEvents() {
        List<String> clientMessages = new CopyOnWriteArrayList<>();
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> foundryFeatures = new AtomicReference<>();
        AtomicReference<String> userAgent = new AtomicReference<>();
        AtomicReference<String> customHeader = new AtomicReference<>();
        server
            = startServer(clientMessages, requestUri, authorization, foundryFeatures, userAgent, customHeader, false);
        AtomicReference<List<String>> requestedScopes = new AtomicReference<>();
        TokenCredential credential = request -> {
            requestedScopes.set(request.getScopes());
            return Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        };
        VoiceAgentWebSocketConnectionOptions options
            = tlsOptions().setStoreEnabled(true).setAgentVersionOverride("version 2");
        BetaVoiceAgentWebSocketAsyncClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .clientOptions(new ClientOptions().setApplicationId("test-app")
                    .setHeaders(Collections.singletonList(new Header("X-Test-Header", "test-value"))))
                .beta()
                .buildBetaVoiceAgentWebSocketAsyncClient();
        Mono<BetaVoiceAgentWebSocketSessionAsyncClient> sessionMono
            = client.openWebSocketSession("agent name", options);
        options.setStoreEnabled(false).setAgentVersionOverride("mutated");
        BetaVoiceAgentWebSocketSessionAsyncClient session = sessionMono.block();
        assertTrue(session.isOpen());
        StepVerifier.create(session.receiveEvents().take(4))
            .then(() -> session.sendText("hello").block())
            .assertNext(this::assertWarningEvent)
            .then(() -> session.appendInputAudio(BinaryData.fromBytes(new byte[] { 1, 2, 3 })).block())
            .assertNext(this::assertWarningEvent)
            .then(() -> session.createResponse().block())
            .assertNext(this::assertWarningEvent)
            .then(() -> session.cancelResponse("response-1").block())
            .assertNext(this::assertWarningEvent)
            .verifyComplete();
        assertEquals(Collections.singletonList("https://ai.azure.com/.default"), requestedScopes.get());
        assertEquals("Bearer test-token", authorization.get());
        assertEquals("VoiceAgents=V1Preview", foundryFeatures.get());
        assertTrue(userAgent.get().startsWith("test-app azsdk-java-"));
        assertEquals("test-value", customHeader.get());
        String decodedUri = decode(requestUri.get());
        assertTrue(decodedUri.contains("/agents/agent name/endpoint/protocols/voice"));
        assertTrue(decodedUri.contains("api-version=v1"));
        assertTrue(decodedUri.contains("transport=websocket"));
        assertTrue(decodedUri.contains("store=true"));
        assertTrue(decodedUri.contains("x-agent-version-override=version 2"));
        assertTrue(decodedUri.contains("x-ms-client-sdk=test-app azsdk-java-"));
        assertEquals(4, clientMessages.size());
        StepVerifier.create(session.receiveEvents())
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && error.getMessage().contains("Only one"))
            .verify();
        session.close();
        assertFalse(session.isOpen());
    }

    @Test
    public void tokenFailureOccursBeforeNetworkAccess() {
        AtomicBoolean connected = new AtomicBoolean();
        server = VoiceAgentWebSocketSessionTests.tlsServer().host("localhost").port(0).handle((request, response) -> {
            connected.set(true);
            return response.send();
        }).bindNow();
        TokenCredential credential = request -> Mono.error(new IllegalStateException("token unavailable"));
        BetaVoiceAgentWebSocketAsyncClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .beta()
                .buildBetaVoiceAgentWebSocketAsyncClient();
        StepVerifier.create(client.openWebSocketSession("agent", tlsOptions()))
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && error.getMessage().contains("token unavailable"))
            .verify();
        assertFalse(connected.get());
    }

    @Test
    public void tokenAcquisitionDoesNotUseHandshakeTimeout() {
        server = startServer(new CopyOnWriteArrayList<>(), new AtomicReference<>(), new AtomicReference<>(),
            new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(), false);
        TokenCredential credential = request -> Mono.delay(Duration.ofMillis(1500))
            .map(ignored -> new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketAsyncClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .beta()
                .buildBetaVoiceAgentWebSocketAsyncClient();
        VoiceAgentWebSocketConnectionOptions options = tlsOptions().setHandshakeTimeout(Duration.ofSeconds(1));
        StepVerifier.withVirtualTime(() -> client.openWebSocketSession("agent", options).flatMap(session -> {
            assertTrue(session.isOpen());
            return session.closeAsync();
        })).thenAwait(Duration.ofMillis(1500)).verifyComplete();
    }

    @Test
    public void rejectedHandshakeMapsConflictToAzureException() {
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle(
                (request, response) -> response.status(HttpResponseStatus.CONFLICT).sendString(Mono.just("conflict")))
            .bindNow();
        StepVerifier.create(createAsyncClient(server.port()).openWebSocketSession("disabled-agent", tlsOptions()))
            .expectErrorSatisfies(error -> {
                ResourceModifiedException exception = assertInstanceOf(ResourceModifiedException.class, error);
                assertEquals(409, exception.getResponse().getStatusCode());
            })
            .verify();
    }

    @Test
    public void nettyHandshakeResponseExposesBufferedBody() {
        DefaultFullHttpResponse nettyResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.CONFLICT, Unpooled.copiedBuffer("conflict", StandardCharsets.UTF_8));
        VoiceAgentWebSocketHttpResponse response
            = new VoiceAgentWebSocketHttpResponse(URI.create("wss://example.com"), nettyResponse);
        assertEquals("conflict", response.getBodyAsString().block());
    }

    @Test
    public void cancellingAsyncConnectCancelsTokenRequest() {
        AtomicBoolean tokenRequestCancelled = new AtomicBoolean();
        TokenCredential credential
            = request -> Mono.<AccessToken>never().doOnCancel(() -> tokenRequestCancelled.set(true));
        BetaVoiceAgentWebSocketAsyncClient client = new AgentsClientBuilder().endpoint("https://example.com")
            .credential(credential)
            .configuration(Configuration.NONE)
            .beta()
            .buildBetaVoiceAgentWebSocketAsyncClient();
        StepVerifier.create(client.openWebSocketSession("agent", tlsOptions())).thenCancel().verify();
        assertTrue(tokenRequestCancelled.get());
    }

    @Test
    public void unknownEventFallsBackToRealtimeServerEvent() {
        server = oneShotWebSocketServer("{\"type\":\"future.event\",\"value\":42}");
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block();
        StepVerifier.create(session.receiveEvents()).assertNext(event -> {
            assertEquals("future.event", event.getType().toString());
            RawRealtimeServerEvent raw = assertInstanceOf(RawRealtimeServerEvent.class, event);
            assertEquals(42, raw.getRawEvent().toObject(Map.class).get("value"));
        }).verifyComplete();
        session.close();
    }

    @Test
    public void fragmentedTextFrameIsAggregated() {
        String message = warningJson();
        int split = message.length() / 2;
        server = frameWebSocketServer(Flux.just(new TextWebSocketFrame(false, 0, message.substring(0, split)),
            new ContinuationWebSocketFrame(true, 0, message.substring(split))), true);
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block();
        StepVerifier.create(session.receiveEvents()).assertNext(this::assertWarningEvent).verifyComplete();
        session.close();
    }

    @Test
    public void binaryJsonFrameIsParsed() {
        server = frameWebSocketServer(
            Mono.just(new BinaryWebSocketFrame(Unpooled.copiedBuffer(warningJson(), StandardCharsets.UTF_8))), true);
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block();
        StepVerifier.create(session.receiveEvents()).assertNext(this::assertWarningEvent).verifyComplete();
        session.close();
    }

    @Test
    public void malformedJsonTerminatesReceiveStream() {
        server = oneShotWebSocketServer("{not-json");
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block();
        StepVerifier.create(session.receiveEvents()).expectError().verify();
        assertFalse(session.isOpen());
    }

    @Test
    public void closeIsIdempotentAndSendAfterCloseFails() {
        server = startServer(new CopyOnWriteArrayList<>(), new AtomicReference<>(), new AtomicReference<>(),
            new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(), false);
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("agent", tlsOptions()).block();
        StepVerifier.create(session.closeAsync().then(session.closeAsync())).verifyComplete();
        StepVerifier.create(session.sendText("after close"))
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && error.getMessage().contains("not open"))
            .verify();
    }

    @Test
    public void secureSessionUsesWssAndReceivesTypedEvent() throws Exception {
        server = oneShotWebSocketServer(warningJson());
        BetaVoiceAgentWebSocketSessionAsyncClient session
            = createAsyncClient(server.port()).openWebSocketSession("secure-agent", tlsOptions())
                .block(Duration.ofSeconds(5));
        assertEquals("wss", URI.create(session.getEndpoint()).getScheme());
        StepVerifier.create(session.receiveEvents()).assertNext(this::assertWarningEvent).verifyComplete();
        session.close();
    }

    private AgentsClientBuilder builder(int port) {
        return new AgentsClientBuilder().endpoint("https://localhost:" + port)
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))));
    }

    private BetaVoiceAgentWebSocketAsyncClient createAsyncClient(int port) {
        return builder(port).configuration(Configuration.NONE).beta().buildBetaVoiceAgentWebSocketAsyncClient();
    }

    private static VoiceAgentWebSocketConnectionOptions tlsOptions() {
        return new VoiceAgentWebSocketConnectionOptions();
    }

    private DisposableServer frameWebSocketServer(org.reactivestreams.Publisher<? extends WebSocketFrame> frames,
        boolean close) {
        WebsocketServerSpec spec = WebsocketServerSpec.builder().protocols("realtime").build();
        return VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request,
                response) -> response.sendWebsocket((inbound, outbound) -> close
                    ? outbound.sendObject(frames).then(outbound.sendClose())
                    : outbound.sendObject(frames).then(inbound.receive().then()), spec))
            .bindNow();
    }

    private DisposableServer oneShotWebSocketServer(String message) {
        WebsocketServerSpec spec = WebsocketServerSpec.builder().protocols("realtime").build();
        return VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket((inbound,
                outbound) -> outbound.sendString(Mono.just(message), StandardCharsets.UTF_8).then(outbound.sendClose()),
                spec))
            .bindNow();
    }

    private DisposableServer startServer(List<String> clientMessages, AtomicReference<String> requestUri,
        AtomicReference<String> authorization, AtomicReference<String> foundryFeatures,
        AtomicReference<String> userAgent, AtomicReference<String> customHeader, boolean sendInitialEvent) {
        WebsocketServerSpec spec = WebsocketServerSpec.builder().protocols("realtime").build();
        return VoiceAgentWebSocketSessionTests.tlsServer().host("localhost").port(0).handle((request, response) -> {
            requestUri.set(request.uri());
            authorization.set(request.requestHeaders().get(HttpHeaderNames.AUTHORIZATION));
            foundryFeatures.set(request.requestHeaders().get("Foundry-Features"));
            userAgent.set(request.requestHeaders().get(HttpHeaderNames.USER_AGENT));
            customHeader.set(request.requestHeaders().get("X-Test-Header"));
            return response.sendWebsocket((inbound, outbound) -> {
                Flux<String> replies = inbound.receive()
                    .asString(StandardCharsets.UTF_8)
                    .doOnNext(clientMessages::add)
                    .map(ignored -> warningJson());
                if (sendInitialEvent) {
                    replies = replies.startWith(warningJson());
                }
                return outbound.sendString(replies, StandardCharsets.UTF_8).then();
            }, spec);
        }).bindNow();
    }

    private void assertWarningEvent(RealtimeServerEvent event) {
        VoiceAgentWarningEvent warning = assertInstanceOf(VoiceAgentWarningEvent.class, event);
        assertEquals("loopback warning", warning.getWarning().getMessage());
        assertEquals("test_warning", warning.getWarning().getCode());
    }

    private static String warningJson() {
        return "{\"type\":\"warning\",\"event_id\":\"event-1\",\"warning\":{"
            + "\"message\":\"loopback warning\",\"code\":\"test_warning\"}}";
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }
}
