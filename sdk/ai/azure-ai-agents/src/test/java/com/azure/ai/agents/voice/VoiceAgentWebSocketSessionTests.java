// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentWebSocketClient;
import com.azure.ai.agents.BetaVoiceAgentWebSocketSessionClient;
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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock("voice-agent-websocket-tls")
public class VoiceAgentWebSocketSessionTests {
    private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
    private static final String TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";
    private static final String TRUST_STORE_TYPE_PROPERTY = "javax.net.ssl.trustStoreType";
    private static final String ORIGINAL_TRUST_STORE = System.getProperty(TRUST_STORE_PROPERTY);
    private static final String ORIGINAL_TRUST_STORE_PASSWORD = System.getProperty(TRUST_STORE_PASSWORD_PROPERTY);
    private static final String ORIGINAL_TRUST_STORE_TYPE = System.getProperty(TRUST_STORE_TYPE_PROPERTY);
    private static final SSLContext ORIGINAL_SSL_CONTEXT = getDefaultSslContext();
    private static final TestCertificate TLS_CERTIFICATE = TestCertificate.create();

    @BeforeAll
    static void installTlsCertificate() {
        TLS_CERTIFICATE.installTrustStore();
    }

    private DisposableServer server;

    @Test
    public void typedStringAndMappingSendsRejectInvalidJson() {
        List<String> messages = new CopyOnWriteArrayList<>();
        server = startServer(messages, new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
            new AtomicReference<>(), new AtomicReference<>(), false);
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost:" + server.port())
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))));
        String raw = "{\"type\": \"response.create\"}";
        BinaryData mapping = BinaryData.fromObject(Collections.singletonMap("type", "response.cancel"));
        try (BetaVoiceAgentWebSocketSessionClient session
            = builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession("agent", tlsOptions())) {
            assertThrows(IllegalArgumentException.class,
                () -> session.sendEvent(BinaryData.fromString("not valid json")));
            session.sendEvent(new RealtimeResponseCreateEvent());
            session.sendEvent(BinaryData.fromString(raw));
            session.sendEvent(mapping);
            Iterator<RealtimeServerEvent> events = session.receiveEvents(Duration.ofSeconds(5)).iterator();
            for (int index = 0; index < 3; index++) {
                assertWarningEvent(events.next());
            }
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
        server = tlsServer().host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket(
                (inbound, outbound) -> outbound.sendObject(frames).then(inbound.receive().then()),
                WebsocketServerSpec.builder().protocols("realtime").build()))
            .bindNow();
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost:" + server.port())
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))));
        List<RealtimeServerEvent> events = new ArrayList<>();
        try (BetaVoiceAgentWebSocketSessionClient session
            = builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession("agent", tlsOptions())) {
            Iterator<RealtimeServerEvent> iterator = session.receiveEvents(Duration.ofSeconds(5)).iterator();
            events.add(iterator.next());
            events.add(iterator.next());
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
            new TextWebSocketFrame(warningJson()))));
        VoiceAgentWebSocketConnectionOptions options
            = tlsOptions().setMalformedEventHandler(error -> failures.incrementAndGet());
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost:" + server.port())
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))));
        try (BetaVoiceAgentWebSocketSessionClient session
            = builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession("agent", options)) {
            Iterator<RealtimeServerEvent> events = session.receiveEvents(Duration.ofSeconds(5)).iterator();
            assertWarningEvent(events.next());
            assertWarningEvent(events.next());
            assertFalse(events.hasNext());
        }
        assertEquals(2, failures.get());
    }

    @Test
    public void boundedQueuesHonorOverflowPolicies() {
        for (VoiceAgentWebSocketOverflowStrategy strategy : VoiceAgentWebSocketOverflowStrategy.values()) {
            server = frameWebSocketServer(Flux.range(0, 4)
                .map(index -> new TextWebSocketFrame("{\"type\":\"future.event\",\"index\":" + index + "}")));
            VoiceAgentWebSocketConnectionOptions options
                = tlsOptions().setReceiveBufferCapacity(2).setOverflowStrategy(strategy);
            AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost:" + server.port())
                .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))));
            List<RealtimeServerEvent> received = new ArrayList<>();
            boolean overflowError = strategy == VoiceAgentWebSocketOverflowStrategy.ERROR;
            try (BetaVoiceAgentWebSocketSessionClient session
                = builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession("agent", options)) {
                assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                    while (session.isOpen()) {
                        Thread.yield();
                    }
                });
                Iterator<RealtimeServerEvent> events = session.receiveEvents(Duration.ofSeconds(5)).iterator();
                if (overflowError) {
                    assertThrows(IllegalStateException.class, events::hasNext);
                } else {
                    events.forEachRemaining(received::add);
                }
            }
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
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost:" + server.port())
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))));
        assertThrows(RuntimeException.class, () -> {
            try (BetaVoiceAgentWebSocketSessionClient session
                = builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession("agent", options)) {
                session.receiveEvents(Duration.ofSeconds(5)).iterator().next();
            }
        });
        assertFalse(recovered.get());
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
            AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint).credential(credential);
            assertThrows(IllegalArgumentException.class,
                () -> builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession("agent"));
        }
        assertFalse(requested.get());
    }

    static HttpServer tlsServer() {
        return HttpServer.create()
            .secure(ssl -> ssl.sslContext(Http11SslContextSpec.forServer(TLS_CERTIFICATE.keyManagerFactory)));
    }

    private static VoiceAgentWebSocketConnectionOptions tlsOptions() {
        return new VoiceAgentWebSocketConnectionOptions();
    }

    @Test
    public void syncReceiveStreamRejectsSecondIterator() {
        server = startServer(new CopyOnWriteArrayList<>(), new AtomicReference<>(), new AtomicReference<>(),
            new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(), false);
        BetaVoiceAgentWebSocketClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))))
                .beta()
                .buildBetaVoiceAgentWebSocketClient();

        try (BetaVoiceAgentWebSocketSessionClient session = client.openWebSocketSession("agent", tlsOptions())) {
            Iterable<RealtimeServerEvent> events = session.receiveEvents(Duration.ofSeconds(5));
            events.iterator();
            IllegalStateException exception = assertThrows(IllegalStateException.class, events::iterator);
            assertEquals("The receiveEvents stream may only be iterated once.", exception.getMessage());
        }
    }

    @Test
    public void rawEventsUseCustomizedTlsTransport() {
        List<String> messages = new CopyOnWriteArrayList<>();
        server = tlsServer().host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket(
                (inbound, outbound) -> outbound.sendString(inbound.receive().asString().doOnNext(messages::add)).then(),
                WebsocketServerSpec.builder().protocols("realtime").build()))
            .bindNow();
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint("https://localhost:" + server.port())
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))));
        BinaryData payload = BinaryData.fromString("{\"type\":\"future.event\",\"value\":42}");
        try (BetaVoiceAgentWebSocketSessionClient session
            = builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession("agent", tlsOptions())) {
            assertThrows(IllegalArgumentException.class, () -> session.sendEvent(BinaryData.fromString("[]")));
            assertThrows(IllegalArgumentException.class, () -> session.sendEvent(BinaryData.fromString("{} {}")));
            session.sendEvent(payload);
            RawRealtimeServerEvent received = assertInstanceOf(RawRealtimeServerEvent.class,
                session.receiveEvents(Duration.ofSeconds(5)).iterator().next());
            assertEquals(payload.toObject(Map.class), received.getRawEvent().toObject(Map.class));
        }
        assertEquals(1, messages.size());
    }

    @Test
    public void customCloseFrameAndReceiveTimeout() {
        server = startServer(new CopyOnWriteArrayList<>(), new AtomicReference<>(), new AtomicReference<>(),
            new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(), false);
        AgentsClientBuilder builder
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))));
        try (BetaVoiceAgentWebSocketSessionClient session
            = builder.beta().buildBetaVoiceAgentWebSocketClient().openWebSocketSession("agent", tlsOptions())) {
            Iterator<RealtimeServerEvent> iterator = session.receiveEvents(Duration.ofMillis(20)).iterator();
            IllegalStateException timeout = assertThrows(IllegalStateException.class, iterator::hasNext);
            assertInstanceOf(TimeoutException.class, timeout.getCause());
            assertTrue(session.isOpen());
            assertThrows(IllegalArgumentException.class, () -> session.close(1005, "invalid"));
            session.close(4001, "finished");
            assertEquals(4001, session.getCloseCode());
            assertEquals("finished", session.getCloseReason());
        }
    }

    @AfterEach
    public void disposeServer() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @AfterAll
    static void restoreTlsConfiguration() {
        SSLContext.setDefault(ORIGINAL_SSL_CONTEXT);
        restoreProperty(TRUST_STORE_PROPERTY, ORIGINAL_TRUST_STORE);
        restoreProperty(TRUST_STORE_PASSWORD_PROPERTY, ORIGINAL_TRUST_STORE_PASSWORD);
        restoreProperty(TRUST_STORE_TYPE_PROPERTY, ORIGINAL_TRUST_STORE_TYPE);
    }

    @Test
    public void syncConnectRejectsNullArguments() {
        TokenCredential credential
            = request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketClient client
            = new AgentsClientBuilder().endpoint("https://localhost/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .beta()
                .buildBetaVoiceAgentWebSocketClient();

        NullPointerException agentNameException
            = assertThrows(NullPointerException.class, () -> client.openWebSocketSession(null, tlsOptions()));
        assertEquals("'agentName' cannot be null.", agentNameException.getMessage());

        NullPointerException optionsException
            = assertThrows(NullPointerException.class, () -> client.openWebSocketSession("agent", null));
        assertEquals("'options' cannot be null.", optionsException.getMessage());
    }

    @Test
    public void syncTokenFailureOccursBeforeNetworkAccess() {
        AtomicBoolean connected = new AtomicBoolean();
        server = tlsServer().host("localhost").port(0).handle((request, response) -> {
            connected.set(true);
            return response.send();
        }).bindNow();
        TokenCredential credential = request -> Mono.error(new IllegalStateException("token unavailable"));
        BetaVoiceAgentWebSocketClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .beta()
                .buildBetaVoiceAgentWebSocketClient();

        IllegalStateException exception
            = assertThrows(IllegalStateException.class, () -> client.openWebSocketSession("agent", tlsOptions()));
        assertTrue(exception.getMessage().contains("token unavailable"));
        assertFalse(connected.get());
    }

    @Test
    public void syncRejectedHandshakeMapsConflictToAzureException() {
        server = tlsServer().host("localhost")
            .port(0)
            .handle(
                (request, response) -> response.status(HttpResponseStatus.CONFLICT).sendString(Mono.just("conflict")))
            .bindNow();
        TokenCredential credential
            = request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .beta()
                .buildBetaVoiceAgentWebSocketClient();

        ResourceModifiedException exception = assertThrows(ResourceModifiedException.class,
            () -> client.openWebSocketSession("disabled-agent", tlsOptions()));
        assertEquals(409, exception.getResponse().getStatusCode());
        assertEquals("conflict", exception.getResponse().getBodyAsString().block());
    }

    @Test
    public void syncClientRejectsEmptyAgentName() {
        TokenCredential credential
            = request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketClient client = new AgentsClientBuilder().endpoint("https://example.com")
            .credential(credential)
            .configuration(Configuration.NONE)
            .beta()
            .buildBetaVoiceAgentWebSocketClient();

        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> client.openWebSocketSession("", tlsOptions()));
        assertEquals("'agentName' cannot be empty.", exception.getMessage());
    }

    @Test
    public void syncReceiveBufferOverflowFailsTheEventStream() {
        Flux<WebSocketFrame> frames = Flux.range(0, 257).map(index -> new TextWebSocketFrame(warningJson()));
        server = frameWebSocketServer(frames);
        TokenCredential credential
            = request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .beta()
                .buildBetaVoiceAgentWebSocketClient();

        try (BetaVoiceAgentWebSocketSessionClient session = client.openWebSocketSession("agent", tlsOptions())) {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                while (session.isOpen()) {
                    Thread.yield();
                }
            });
            IllegalStateException exception
                = assertThrows(IllegalStateException.class, () -> session.receiveEvents().iterator().hasNext());
            assertEquals("Voice-agent receive buffer overflow.", exception.getMessage());
        }
    }

    @Test
    public void syncOrderlyClosePreservesFullReceiveBuffer() {
        Flux<WebSocketFrame> frames = Flux.range(0, 256).map(index -> new TextWebSocketFrame(warningJson()));
        server = frameWebSocketServer(frames);
        TokenCredential credential
            = request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .beta()
                .buildBetaVoiceAgentWebSocketClient();

        try (BetaVoiceAgentWebSocketSessionClient session = client.openWebSocketSession("agent", tlsOptions())) {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                Iterator<RealtimeServerEvent> events = session.receiveEvents().iterator();
                int eventCount = 0;
                while (events.hasNext()) {
                    events.next();
                    eventCount++;
                }
                assertEquals(256, eventCount);
            });
        }
    }

    @Test
    public void syncSessionReceivesTypedEventAndCloses() {
        List<String> clientMessages = new CopyOnWriteArrayList<>();
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> foundryFeatures = new AtomicReference<>();
        AtomicReference<String> userAgent = new AtomicReference<>();
        server = startServer(clientMessages, requestUri, authorization, foundryFeatures, userAgent,
            new AtomicReference<>(), true);
        TokenCredential credential
            = request -> Mono.just(new AccessToken("sync-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketClient client
            = new AgentsClientBuilder().endpoint("https://localhost:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .beta()
                .buildBetaVoiceAgentWebSocketClient();

        try (BetaVoiceAgentWebSocketSessionClient session = client.openWebSocketSession("sync-agent", tlsOptions())) {
            Iterator<RealtimeServerEvent> events = session.receiveEvents().iterator();
            assertWarningEvent(events.next());
            session.sendFunctionCallOutput("call-1", "{\"temperature\":72}");
            assertWarningEvent(events.next());
            assertWarningEvent(events.next());

            assertEquals(2, clientMessages.size());
            Map<?, ?> functionOutput = BinaryData.fromString(clientMessages.get(0)).toObject(Map.class);
            assertEquals("conversation.item.create", functionOutput.get("type"));
            Map<?, ?> item = (Map<?, ?>) functionOutput.get("item");
            assertEquals("function_call_output", item.get("type"));
            assertEquals("call-1", item.get("call_id"));
            assertEquals("{\"temperature\":72}", item.get("output"));
            Map<?, ?> responseCreate = BinaryData.fromString(clientMessages.get(1)).toObject(Map.class);
            assertEquals("response.create", responseCreate.get("type"));
            assertTrue(session.isOpen());
        }
    }

    private DisposableServer frameWebSocketServer(org.reactivestreams.Publisher<? extends WebSocketFrame> frames) {
        WebsocketServerSpec spec = WebsocketServerSpec.builder().protocols("realtime").build();
        return tlsServer().host("localhost")
            .port(0)
            .handle((request, response) -> response
                .sendWebsocket((inbound, outbound) -> outbound.sendObject(frames).then(outbound.sendClose()), spec))
            .bindNow();
    }

    private DisposableServer oneShotWebSocketServer(String message) {
        WebsocketServerSpec spec = WebsocketServerSpec.builder().protocols("realtime").build();
        return tlsServer().host("localhost")
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
        return tlsServer().host("localhost").port(0).handle((request, response) -> {
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

    private static final class TestCertificate {
        private final Path path;
        private final String password;
        private final KeyStore keyStore;
        private final KeyManagerFactory keyManagerFactory;

        private TestCertificate(Path path, String password, KeyStore keyStore, KeyManagerFactory keyManagerFactory) {
            this.path = path;
            this.password = password;
            this.keyStore = keyStore;
            this.keyManagerFactory = keyManagerFactory;
        }

        private static TestCertificate create() {
            try {
                Path path = Files.createTempFile("voice-agent-websocket-", ".p12");
                Files.delete(path);
                path.toFile().deleteOnExit();
                String password = UUID.randomUUID().toString();
                String executable
                    = Paths
                        .get(System.getProperty("java.home"), "bin",
                            System.getProperty("os.name").startsWith("Windows") ? "keytool.exe" : "keytool")
                        .toString();
                Process process = new ProcessBuilder(executable, "-genkeypair", "-alias", "localhost", "-keyalg", "RSA",
                    "-keysize", "2048", "-validity", "1", "-dname", "CN=localhost", "-ext", "SAN=dns:localhost",
                    "-storetype", "PKCS12", "-keystore", path.toString(), "-storepass", password, "-keypass", password,
                    "-noprompt").redirectErrorStream(true).start();
                if (process.waitFor() != 0) {
                    throw new IllegalStateException("keytool failed to generate the test certificate.");
                }
                KeyStore store = KeyStore.getInstance("PKCS12");
                try (InputStream input = Files.newInputStream(path)) {
                    store.load(input, password.toCharArray());
                }
                KeyManagerFactory keyManagerFactory
                    = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(store, password.toCharArray());
                return new TestCertificate(path, password, store, keyManagerFactory);
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
        }

        private void installTrustStore() {
            try {
                System.setProperty(TRUST_STORE_PROPERTY, path.toString());
                System.setProperty(TRUST_STORE_PASSWORD_PROPERTY, password);
                System.setProperty(TRUST_STORE_TYPE_PROPERTY, "PKCS12");
                TrustManagerFactory trustManagerFactory
                    = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
                SSLContext.setDefault(sslContext);
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
        }

    }

    private static SSLContext getDefaultSslContext() {
        try {
            return SSLContext.getDefault();
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

}
