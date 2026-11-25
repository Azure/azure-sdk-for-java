// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionClient;
import com.azure.ai.agents.VoiceAgentsWebSocketSessionAsyncClient;
import com.azure.ai.agents.VoiceAgentsWebSocketSessionClient;
import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import com.openai.core.JsonValue;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ResponseCreateEvent;
import com.openai.models.live.ServerEvent;
import io.netty.handler.codec.http.HttpHeaderNames;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.WebsocketServerSpec;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock("voice-agent-websocket-tls")
@Isolated("Changes the JVM-wide TLS context for localhost WebSocket tests")
@Timeout(20)
public class VoiceAgentLiveWebSocketTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String STARTED
        = "{\"type\":\"session.started\",\"conversation_id\":\"conversation-1\",\"session\":{\"id\":\"live-1\"}}";
    private static final String RESPONSE = "{\"type\":\"response.done\",\"future_field\":{\"native\":true}}";
    private static final String FUTURE = "{\"type\":\"future.live.event\",\"unknown\":[1,2,3]}";
    private static final String ERROR
        = "{\"type\":\"error\",\"error\":{\"type\":\"server_error\",\"message\":\"native Live error\"}}";
    private static final String COMMAND = "{\"type\":\"session.update\",\"session\":{\"future_setting\":true}}";
    private DisposableServer server;

    @Test
    public void liveSessionExposesOnlyAsynchronousClose() {
        assertTrue(AsyncCloseable.class.isAssignableFrom(BetaVoiceAgentsLiveWebSocketSessionAsyncClient.class));
        assertFalse(AutoCloseable.class.isAssignableFrom(BetaVoiceAgentsLiveWebSocketSessionAsyncClient.class));
        assertThrows(NoSuchMethodException.class,
            () -> BetaVoiceAgentsLiveWebSocketSessionAsyncClient.class.getMethod("close"));
    }

    @BeforeAll
    static void installTlsCertificate() {
        VoiceAgentWebSocketSessionTests.installTlsCertificate();
    }

    @AfterAll
    static void restoreTlsConfiguration() {
        VoiceAgentWebSocketSessionTests.restoreTlsConfiguration();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @ParameterizedTest
    @CsvSource({ "false,false", "false,true", "true,false", "true,true" })
    public void handshakeUsesLiveRouteWithoutRealtimeSubprotocol(boolean async, boolean allowPreview) throws Exception {
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<io.netty.handler.codec.http.HttpHeaders> headers = new AtomicReference<>();
        server = VoiceAgentWebSocketSessionTests.tlsServer().host("localhost").port(0).handle((request, response) -> {
            requestUri.set(request.uri());
            headers.set(request.requestHeaders().copy());
            return response.sendWebsocket((inbound, outbound) -> inbound.receive().then(),
                WebsocketServerSpec.builder().protocols("realtime").build());
        }).bindNow();
        AgentsClientBuilder builder = builder().allowPreview(allowPreview)
            .endpoint("https://localhost:" + server.port() + "/api/projects/project/")
            .clientOptions(new ClientOptions().setApplicationId("live-app")
                .setHeaders(Arrays.asList(new Header("X-Custom", "value"), new Header("Authorization", "override"),
                    new Header("Foundry-Features", "override"), new Header("Sec-WebSocket-Protocol", "realtime"))));
        String inputs = "{\"name\":\"a & b/\u4f60\u597d\"}";
        VoiceAgentLiveWebSocketConnectionOptions options
            = new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(true)
                .setAgentVersionOverride("version 1")
                .setStructuredInputs(inputs);
        if (async) {
            Mono<BetaVoiceAgentsLiveWebSocketSessionAsyncClient> connection = builder.beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent /name", options);
            options.setStoreEnabled(false).setAgentVersionOverride("changed").setStructuredInputs("{}");
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = connection.block(TIMEOUT);
            assertNotNull(session);
            assertTrue(session.isOpen());
            session.closeAsync().block(TIMEOUT);
            assertFalse(session.isOpen());
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient session = builder.beta()
                .buildBetaVoiceAgentsLiveWebSocketClient()
                .openWebSocketSession("agent /name", options)) {
                assertTrue(session.isOpen());
            }
        }

        URI uri = URI.create(requestUri.get());
        assertEquals("/api/projects/project/agents/agent%20%2Fname/endpoint/protocols/voice/live/sessions",
            uri.getRawPath());
        String query = URLDecoder.decode(uri.getRawQuery(), StandardCharsets.UTF_8.name());
        assertTrue(query.contains("api-version=v1"), query);
        assertTrue(query.contains("store=true"), query);
        assertTrue(query.contains("x-agent-version-override=version 1"), query);
        assertTrue(query.contains("structured_inputs=" + inputs), query);
        assertFalse(query.contains("transport="), query);
        assertFalse(query.contains("agent_session_id="), query);
        assertNull(headers.get().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL));
        assertNull(headers.get().get("x-ms-voice-structured-inputs"));
        assertEquals("VoiceAgents=V1Preview", headers.get().get("Foundry-Features"));
        assertEquals("value", headers.get().get("X-Custom"));
        assertTrue(headers.get().get(HttpHeaderNames.USER_AGENT).startsWith("live-app azsdk-java-"));
        assertFalse("override".equals(headers.get().get(HttpHeaderNames.AUTHORIZATION)));

        if (async) {
            VoiceAgentsWebSocketSessionAsyncClient session
                = builder.buildVoiceAgentsWebSocketAsyncClient().openWebSocketSession("agent").block(TIMEOUT);
            assertNotNull(session);
            session.closeAsync().block(TIMEOUT);
        } else {
            try (VoiceAgentsWebSocketSessionClient session
                = builder.buildVoiceAgentsWebSocketClient().openWebSocketSession("agent")) {
                assertTrue(session.isOpen());
            }
        }
        assertEquals(allowPreview ? "VoiceAgents=V1Preview" : null, headers.get().get("Foundry-Features"));
    }

    @Test
    public void syncNativeEventsAreLosslessAndSessionIsBidirectional() {
        List<String> sent = new CopyOnWriteArrayList<>();
        startEchoServer(sent);
        BetaVoiceAgentsLiveWebSocketSessionClient session
            = builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent");
        try {
            Iterator<BinaryData> events = session.receiveRawEvents(TIMEOUT).iterator();
            assertEquals(STARTED, events.next().toString());
            assertEquals(RESPONSE, events.next().toString());
            assertEquals(FUTURE, events.next().toString());
            assertEquals(ERROR, events.next().toString());
            assertThrows(IllegalStateException.class, session::receiveRawEvents);
            assertThrows(IllegalStateException.class, session::receiveEvents);
            assertThrows(IllegalArgumentException.class, () -> session.sendEvent(BinaryData.fromString("[]")));
            session.sendEvent(BinaryData.fromString(COMMAND));
            assertEquals(COMMAND, events.next().toString());
            assertEquals(Arrays.asList(COMMAND), sent);
            session.close(1000, "done");
            assertFalse(session.isOpen());
            assertThrows(IllegalStateException.class, () -> session.sendEvent(BinaryData.fromString(COMMAND)));
        } finally {
            session.close();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void rawReceptionDoesNotRequireTypedDecoding(boolean async) {
        String message = "not-json";
        String unusualPayload = "{\"type\":\"session.started\",\"session\":7}";
        AtomicInteger malformed = new AtomicInteger();
        VoiceAgentLiveWebSocketConnectionOptions options = new VoiceAgentLiveWebSocketConnectionOptions()
            .setMalformedEventHandler(error -> malformed.incrementAndGet());
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket((inbound,
                outbound) -> outbound.sendString(Flux.just(message, unusualPayload)).then(inbound.receive().then())))
            .bindNow();
        if (async) {
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = builder().beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent", options)
                .block(TIMEOUT);
            assertNotNull(session);
            try {
                StepVerifier.create(session.receiveRawEvents().take(2))
                    .assertNext(event -> assertEquals(message, event.toString()))
                    .assertNext(event -> assertEquals(unusualPayload, event.toString()))
                    .verifyComplete();
            } finally {
                session.closeAsync().block(TIMEOUT);
            }
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent", options)) {
                Iterator<BinaryData> events = session.receiveRawEvents(TIMEOUT).iterator();
                assertEquals(message, events.next().toString());
                assertEquals(unusualPayload, events.next().toString());
            }
        }
        assertEquals(0, malformed.get());
    }

    @Test
    public void asyncNativeEventsAreLosslessAndSessionIsBidirectional() {
        List<String> sent = new CopyOnWriteArrayList<>();
        startEchoServer(sent);
        BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = builder().beta()
            .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
            .openWebSocketSession("agent")
            .block(TIMEOUT);
        assertNotNull(session);
        try {
            StepVerifier.create(session.sendEvent(BinaryData.fromString("[]")))
                .expectError(IllegalArgumentException.class)
                .verify(TIMEOUT);
            StepVerifier
                .create(session.sendEvent(BinaryData.fromString(COMMAND))
                    .thenMany(session.receiveRawEvents())
                    .map(BinaryData::toString))
                .expectNext(STARTED, RESPONSE, FUTURE, ERROR)
                .expectNext(COMMAND)
                .thenCancel()
                .verify(TIMEOUT);
            assertEquals(Arrays.asList(COMMAND), sent);
            StepVerifier.create(session.receiveRawEvents()).expectError(IllegalStateException.class).verify(TIMEOUT);
            StepVerifier.create(session.receiveEvents()).expectError(IllegalStateException.class).verify(TIMEOUT);
            session.closeAsync(1000, "done").block(TIMEOUT);
            assertFalse(session.isOpen());
            StepVerifier.create(session.sendEvent(BinaryData.fromString(COMMAND)))
                .expectError(IllegalStateException.class)
                .verify(TIMEOUT);
        } finally {
            session.closeAsync().block(TIMEOUT);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void sameBuilderKeepsLiveAndRealtimeHandshakesIndependent(boolean async) {
        List<String> requestUris = new CopyOnWriteArrayList<>();
        List<io.netty.handler.codec.http.HttpHeaders> headers = new CopyOnWriteArrayList<>();
        server = VoiceAgentWebSocketSessionTests.tlsServer().host("localhost").port(0).handle((request, response) -> {
            requestUris.add(request.uri());
            headers.add(request.requestHeaders().copy());
            return response.sendWebsocket((inbound, outbound) -> inbound.receive().then(),
                WebsocketServerSpec.builder().protocols("realtime").build());
        }).bindNow();
        AgentsClientBuilder builder = builder();
        String inputs = "{\"name\":\"value\"}";
        VoiceAgentLiveWebSocketConnectionOptions liveOptions
            = new VoiceAgentLiveWebSocketConnectionOptions().setStructuredInputs(inputs);
        VoiceAgentWebSocketConnectionOptions realtimeOptions
            = new VoiceAgentWebSocketConnectionOptions().setStructuredInputs(inputs);
        if (async) {
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient live = builder.beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent", liveOptions)
                .block(TIMEOUT);
            assertNotNull(live);
            try {
                assertTrue(live.isOpen());
            } finally {
                live.closeAsync().block(TIMEOUT);
            }
            VoiceAgentsWebSocketSessionAsyncClient realtime = builder.buildVoiceAgentsWebSocketAsyncClient()
                .openWebSocketSession("agent", realtimeOptions)
                .block(TIMEOUT);
            assertNotNull(realtime);
            try {
                assertTrue(realtime.isOpen());
            } finally {
                realtime.closeAsync().block(TIMEOUT);
            }
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient live
                = builder.beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent", liveOptions)) {
                assertTrue(live.isOpen());
            }
            try (VoiceAgentsWebSocketSessionClient realtime
                = builder.buildVoiceAgentsWebSocketClient().openWebSocketSession("agent", realtimeOptions)) {
                assertTrue(realtime.isOpen());
            }
        }
        assertEquals(2, requestUris.size());
        assertEquals("/agents/agent/endpoint/protocols/voice/live/sessions", URI.create(requestUris.get(0)).getPath());
        assertTrue(requestUris.get(0).contains("structured_inputs="));
        assertFalse(requestUris.get(0).contains("transport="));
        assertNull(headers.get(0).get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL));
        assertNull(headers.get(0).get("x-ms-voice-structured-inputs"));
        assertEquals("/agents/agent/endpoint/protocols/voice", URI.create(requestUris.get(1)).getPath());
        assertTrue(requestUris.get(1).contains("transport=websocket"));
        assertFalse(requestUris.get(1).contains("structured_inputs="));
        assertEquals("realtime", headers.get(1).get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL));
        assertEquals(inputs, headers.get(1).get("x-ms-voice-structured-inputs"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void typedEventsRoundTripWithoutLosingUnknownFields(boolean async) {
        List<String> sent = new CopyOnWriteArrayList<>();
        String audio = "{\"type\":\"session.output_audio.delta\",\"delta\":\"AAA=\",\"future_field\":true}";
        String backend = "{\"type\":\"response.event\",\"event_id\":\"backend-1\","
            + "\"event\":{\"type\":\"response.completed\",\"response\":{\"id\":\"response-1\",\"future_field\":true}}}";
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request,
                response) -> response.sendWebsocket((inbound,
                    outbound) -> outbound.sendString(Flux.concat(Flux.just(STARTED, audio, backend, FUTURE, ERROR),
                        inbound.receive().asString().doOnNext(sent::add))).then()))
            .bindNow();
        ClientEvent command = ClientEvent.ofResponseCreate(ResponseCreateEvent.builder()
            .eventId("command-1")
            .putAdditionalProperty("future_setting", JsonValue.from(true))
            .build());
        String commandJson = OpenAIJsonHelper.toBinaryData(command).toString();
        if (async) {
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = builder().beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent")
                .block(TIMEOUT);
            assertNotNull(session);
            try {
                assertThrows(NullPointerException.class, () -> session.sendEvent((ClientEvent) null));
                StepVerifier.create(session.sendEvent(command).thenMany(session.receiveEvents()))
                    .assertNext(event -> assertStartedEvent(event))
                    .assertNext(event -> assertAudioEvent(event, audio))
                    .assertNext(event -> assertBackendEvent(event, backend))
                    .assertNext(event -> assertEventJson(event, FUTURE))
                    .assertNext(event -> {
                        assertTrue(event.isErrorEvent());
                        assertEventJson(event, ERROR);
                    })
                    .assertNext(event -> assertEventJson(event, commandJson))
                    .thenCancel()
                    .verify(TIMEOUT);
                StepVerifier.create(session.receiveRawEvents())
                    .expectError(IllegalStateException.class)
                    .verify(TIMEOUT);
            } finally {
                session.closeAsync().block(TIMEOUT);
            }
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent")) {
                assertThrows(NullPointerException.class, () -> session.sendEvent((ClientEvent) null));
                assertThrows(IllegalArgumentException.class, () -> session.receiveEvents(Duration.ZERO));
                Iterator<ServerEvent> events = session.receiveEvents(TIMEOUT).iterator();
                assertStartedEvent(events.next());
                assertAudioEvent(events.next(), audio);
                assertBackendEvent(events.next(), backend);
                assertEventJson(events.next(), FUTURE);
                ServerEvent error = events.next();
                assertTrue(error.isErrorEvent());
                assertEventJson(error, ERROR);
                session.sendEvent(command);
                assertEventJson(events.next(), commandJson);
                assertThrows(IllegalStateException.class, session::receiveRawEvents);
            }
        }
        assertEquals(Arrays.asList(commandJson), sent);
    }

    private static void assertStartedEvent(ServerEvent event) {
        assertTrue(event.isSessionStarted());
        assertEquals(JsonValue.from("conversation-1"),
            event.asSessionStarted()._additionalProperties().get("conversation_id"));
        assertEventJson(event, STARTED);
    }

    private static void assertAudioEvent(ServerEvent event, String json) {
        assertTrue(event.isSessionOutputAudioDelta());
        assertEquals("AAA=", event.asSessionOutputAudioDelta().delta());
        assertEventJson(event, json);
    }

    private static void assertBackendEvent(ServerEvent event, String json) {
        assertTrue(event.isResponseEvent());
        assertEquals("backend-1", event.asResponseEvent().eventId());
        assertEventJson(event, json);
    }

    private static void assertEventJson(ServerEvent event, String json) {
        assertEquals(BinaryData.fromString(json).toObject(Object.class),
            OpenAIJsonHelper.toBinaryData(event).toObject(Object.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void handshakeFailuresSurfaceAsAzureExceptions(boolean async) {
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.status(404).send())
            .bindNow();
        if (async) {
            StepVerifier
                .create(builder().beta().buildBetaVoiceAgentsLiveWebSocketAsyncClient().openWebSocketSession("missing"))
                .expectError(ResourceNotFoundException.class)
                .verify(TIMEOUT);
        } else {
            assertThrows(ResourceNotFoundException.class,
                () -> builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("missing"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void malformedJsonTerminatesReception(boolean async) {
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket(
                (inbound, outbound) -> outbound.sendString(Mono.just("[]")).then(inbound.receive().then())))
            .bindNow();
        if (async) {
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = builder().beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent")
                .block(TIMEOUT);
            assertNotNull(session);
            try {
                StepVerifier.create(session.receiveEvents())
                    .expectError(IllegalArgumentException.class)
                    .verify(TIMEOUT);
            } finally {
                session.closeAsync().block(TIMEOUT);
            }
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent")) {
                assertThrows(IllegalArgumentException.class, () -> session.receiveEvents(TIMEOUT).iterator().hasNext());
            }
        }
    }

    @Test
    public void connectionOptionsValidateBoundsAndCopyAllSettings() {
        VoiceAgentLiveWebSocketConnectionOptions options = new VoiceAgentLiveWebSocketConnectionOptions();
        assertEquals(256, options.getReceiveBufferCapacity());
        assertEquals(32 * 1024 * 1024, options.getMaxMessageSize());
        assertThrows(IllegalArgumentException.class, () -> options.setReceiveBufferCapacity(0));
        assertThrows(IllegalArgumentException.class, () -> options.setReceiveBufferCapacity(65537));
        assertThrows(IllegalArgumentException.class, () -> options.setMaxMessageSize(0));
        assertThrows(IllegalArgumentException.class, () -> options.setHandshakeTimeout(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> options.setCloseTimeout(Duration.ofSeconds(-1)));
        options.setReceiveBufferCapacity(1)
            .setMaxMessageSize(1024)
            .setStoreEnabled(true)
            .setHandshakeTimeout(TIMEOUT)
            .setCloseTimeout(TIMEOUT)
            .setAgentVersionOverride("1")
            .setStructuredInputs("{\"name\":\"value\"}");
        VoiceAgentLiveWebSocketConnectionOptions copy = new VoiceAgentLiveWebSocketConnectionOptions(options);
        options.setReceiveBufferCapacity(65536).setStoreEnabled(false).setStructuredInputs("{}");
        assertEquals(1, copy.getReceiveBufferCapacity());
        assertEquals(1024, copy.getMaxMessageSize());
        assertEquals(true, copy.isStoreEnabled());
        assertEquals(TIMEOUT, copy.getHandshakeTimeout());
        assertEquals(TIMEOUT, copy.getCloseTimeout());
        assertEquals("1", copy.getAgentVersionOverride());
        assertEquals("{\"name\":\"value\"}", copy.getStructuredInputs());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void throwingMalformedEventCallbackTerminatesTypedReception(boolean async) {
        IllegalStateException failure = new IllegalStateException("Malformed event callback failed.");
        VoiceAgentLiveWebSocketConnectionOptions options
            = new VoiceAgentLiveWebSocketConnectionOptions().setMalformedEventHandler(error -> {
                throw failure;
            });
        server
            = VoiceAgentWebSocketSessionTests.tlsServer()
                .host("localhost")
                .port(0)
                .handle((request, response) -> response.sendWebsocket((inbound,
                    outbound) -> outbound.sendString(Flux.just("not-json", STARTED)).then(inbound.receive().then())))
                .bindNow();
        if (async) {
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = builder().beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent", options)
                .block(TIMEOUT);
            assertNotNull(session);
            try {
                StepVerifier.create(session.receiveEvents())
                    .expectErrorMatches(error -> error == failure)
                    .verify(TIMEOUT);
                assertFalse(session.isOpen());
            } finally {
                session.closeAsync().block(TIMEOUT);
            }
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent", options)) {
                Iterator<ServerEvent> events = session.receiveEvents(TIMEOUT).iterator();
                assertEquals(failure, assertThrows(IllegalStateException.class, events::hasNext));
                assertFalse(events.hasNext());
                assertFalse(session.isOpen());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void peerClosurePreservesEventsAndCloseMetadata(boolean async) {
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request,
                response) -> response.sendWebsocket((inbound, outbound) -> outbound.sendString(Mono.just(STARTED))
                    .then(outbound.sendClose(1000, "finished"))))
            .bindNow();
        if (async) {
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = builder().beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent")
                .block(TIMEOUT);
            assertNotNull(session);
            try {
                StepVerifier.create(session.receiveRawEvents().map(BinaryData::toString))
                    .expectNext(STARTED)
                    .expectComplete()
                    .verify(TIMEOUT);
                assertEquals(1000, session.getCloseCode());
                assertEquals("finished", session.getCloseReason());
                assertFalse(session.isOpen());
            } finally {
                session.closeAsync().block(TIMEOUT);
            }
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent")) {
                Iterator<BinaryData> events = session.receiveRawEvents(TIMEOUT).iterator();
                assertEquals(STARTED, events.next().toString());
                assertFalse(events.hasNext());
                assertEquals(1000, session.getCloseCode());
                assertEquals("finished", session.getCloseReason());
                assertFalse(session.isOpen());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void malformedEventCallbackCanSkipInvalidJson(boolean async) {
        AtomicInteger malformed = new AtomicInteger();
        VoiceAgentLiveWebSocketConnectionOptions options = new VoiceAgentLiveWebSocketConnectionOptions()
            .setMalformedEventHandler(error -> malformed.incrementAndGet());
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket((inbound,
                outbound) -> outbound.sendString(Flux.just("[]", "not-json", STARTED)).then(inbound.receive().then())))
            .bindNow();
        if (async) {
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = builder().beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent", options)
                .block(TIMEOUT);
            assertNotNull(session);
            try {
                StepVerifier.create(session.receiveEvents())
                    .assertNext(VoiceAgentLiveWebSocketTests::assertStartedEvent)
                    .thenCancel()
                    .verify(TIMEOUT);
            } finally {
                session.closeAsync().block(TIMEOUT);
            }
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent", options)) {
                assertStartedEvent(session.receiveEvents(TIMEOUT).iterator().next());
            }
        }
        assertEquals(2, malformed.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void oversizedEventsFailEvenWhenMalformedCallbackIsConfigured(boolean async) {
        AtomicInteger malformed = new AtomicInteger();
        VoiceAgentLiveWebSocketConnectionOptions options
            = new VoiceAgentLiveWebSocketConnectionOptions().setMaxMessageSize(32)
                .setMalformedEventHandler(error -> malformed.incrementAndGet());
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket(
                (inbound, outbound) -> outbound.sendString(Mono.just(STARTED)).then(inbound.receive().then())))
            .bindNow();
        if (async) {
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient session = builder().beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession("agent", options)
                .block(TIMEOUT);
            assertNotNull(session);
            try {
                StepVerifier.create(session.receiveRawEvents()).expectError().verify(TIMEOUT);
            } finally {
                session.closeAsync().block(TIMEOUT);
            }
        } else {
            try (BetaVoiceAgentsLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentsLiveWebSocketClient().openWebSocketSession("agent", options)) {
                assertThrows(IllegalArgumentException.class,
                    () -> session.receiveRawEvents(TIMEOUT).iterator().hasNext());
            }
        }
        assertEquals(0, malformed.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void unsupportedHttpSettingsAreRejected(boolean async) {
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://example.com")
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))))
            .httpClient(com.azure.core.http.HttpClient.createDefault());
        if (async) {
            assertThrows(IllegalStateException.class,
                () -> builder.beta().buildBetaVoiceAgentsLiveWebSocketAsyncClient());
        } else {
            assertThrows(IllegalStateException.class, () -> builder.beta().buildBetaVoiceAgentsLiveWebSocketClient());
        }
    }

    private AgentsClientBuilder builder() {
        return new AgentsClientBuilder().endpoint("https://localhost:" + server.port())
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))))
            .configuration(Configuration.NONE);
    }

    private void startEchoServer(List<String> sent) {
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request,
                response) -> response.sendWebsocket((inbound,
                    outbound) -> outbound.sendString(Flux.concat(Flux.just(STARTED, RESPONSE, FUTURE, ERROR),
                        inbound.receive().asString().doOnNext(sent::add))).then()))
            .bindNow();
    }
}
