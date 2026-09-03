// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.VoiceAgentServerEventWarning;
import com.azure.ai.agents.models.VoiceAgentTransport;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Header;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;
import reactor.test.StepVerifier;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentWebSocketSessionTests {
    private DisposableServer server;

    @AfterEach
    public void disposeServer() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @Test
    public void asyncSessionNegotiatesHandshakeAndExchangesTypedEvents() {
        List<String> clientMessages = new CopyOnWriteArrayList<>();
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> foundryFeatures = new AtomicReference<>();
        AtomicReference<String> userAgent = new AtomicReference<>();
        server = startServer(clientMessages, requestUri, authorization, foundryFeatures, userAgent, false);
        AtomicReference<List<String>> requestedScopes = new AtomicReference<>();
        TokenCredential credential = request -> {
            requestedScopes.set(request.getScopes());
            return Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        };
        VoiceAgentWebSocketConnectionOptions options
            = new VoiceAgentWebSocketConnectionOptions().setTransport(VoiceAgentTransport.WEBSOCKET)
                .setStoreEnabled(true)
                .setAgentVersionOverride("version 2");
        BetaVoiceAgentWebSocketAsyncClient client
            = new AgentsClientBuilder().endpoint("http://127.0.0.1:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .clientOptions(new ClientOptions().setApplicationId("test-app")
                    .setHeaders(Collections.singletonList(new Header("X-Test-Header", "test-value"))))
                .beta()
                .buildBetaVoiceAgentWebSocketAsyncClient();

        VoiceAgentWebSocketSessionAsyncClient session = client.connect("agent name", options).block();
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
        String decodedUri = decode(requestUri.get());
        assertTrue(decodedUri.contains("/agents/agent name/endpoint/protocols/voice"));
        assertTrue(decodedUri.contains("api-version=v1"));
        assertTrue(decodedUri.contains("transport=websocket"));
        assertTrue(decodedUri.contains("store=true"));
        assertTrue(decodedUri.contains("x-agent-version-override=version 2"));
        assertTrue(decodedUri.contains("x-ms-client-sdk=test-app azsdk-java-"));
        assertEquals(4, clientMessages.size());
        assertTrue(clientMessages.get(0).contains("\"type\":\"conversation.item.create\""));
        assertTrue(clientMessages.get(0).contains("\"role\":\"user\""));
        assertTrue(clientMessages.get(0).contains("\"text\":\"hello\""));
        assertTrue(clientMessages.get(1).contains("\"audio\":\"AQID\""));
        assertTrue(clientMessages.get(2).contains("\"type\":\"response.create\""));
        assertTrue(clientMessages.get(3).contains("\"response_id\":\"response-1\""));

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
        server = HttpServer.create().host("127.0.0.1").port(0).handle((request, response) -> {
            connected.set(true);
            return response.send();
        }).bindNow();
        TokenCredential credential = request -> Mono.error(new IllegalStateException("token unavailable"));
        BetaVoiceAgentWebSocketAsyncClient client
            = new AgentsClientBuilder().endpoint("http://127.0.0.1:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .buildBetaVoiceAgentWebSocketAsyncClient();

        StepVerifier.create(client.connect("agent"))
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && error.getMessage().contains("token unavailable"))
            .verify();
        assertFalse(connected.get());
    }

    @Test
    public void rejectedHandshakeMapsConflictToAzureException() {
        server = HttpServer.create()
            .host("127.0.0.1")
            .port(0)
            .handle(
                (request, response) -> response.status(HttpResponseStatus.CONFLICT).sendString(Mono.just("conflict")))
            .bindNow();
        TokenCredential credential
            = request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketAsyncClient client
            = new AgentsClientBuilder().endpoint("http://127.0.0.1:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .buildBetaVoiceAgentWebSocketAsyncClient();

        StepVerifier.create(client.connect("disabled-agent")).expectErrorSatisfies(error -> {
            ResourceModifiedException exception = assertInstanceOf(ResourceModifiedException.class, error);
            assertEquals(409, exception.getResponse().getStatusCode());
        }).verify();
    }

    @Test
    public void unknownEventFallsBackToRealtimeServerEvent() {
        server = oneShotWebSocketServer("{\"type\":\"future.event\",\"value\":42}");
        VoiceAgentWebSocketSessionAsyncClient session = createAsyncClient(server.port()).connect("agent").block();

        StepVerifier.create(session.receiveEvents())
            .assertNext(event -> assertEquals("future.event", event.getType().toString()))
            .verifyComplete();
        session.close();
    }

    @Test
    public void fragmentedTextFrameIsAggregated() {
        String message = warningJson();
        int split = message.length() / 2;
        Flux<WebSocketFrame> frames = Flux.just(new TextWebSocketFrame(false, 0, message.substring(0, split)),
            new ContinuationWebSocketFrame(true, 0, message.substring(split)));
        server = frameWebSocketServer(frames);
        VoiceAgentWebSocketSessionAsyncClient session = createAsyncClient(server.port()).connect("agent").block();

        StepVerifier.create(session.receiveEvents()).assertNext(this::assertWarningEvent).verifyComplete();
        session.close();
    }

    @Test
    public void binaryFrameTerminatesReceiveStream() {
        server = frameWebSocketServer(Mono.just(new BinaryWebSocketFrame()));
        VoiceAgentWebSocketSessionAsyncClient session = createAsyncClient(server.port()).connect("agent").block();

        StepVerifier.create(session.receiveEvents())
            .expectErrorMatches(
                error -> error instanceof IllegalArgumentException && error.getMessage().contains("JSON text frames"))
            .verify();
        assertFalse(session.isOpen());
    }

    @Test
    public void malformedJsonTerminatesReceiveStream() {
        server = oneShotWebSocketServer("{not-json");
        VoiceAgentWebSocketSessionAsyncClient session = createAsyncClient(server.port()).connect("agent").block();

        StepVerifier.create(session.receiveEvents()).expectError().verify();
        assertFalse(session.isOpen());
    }

    @Test
    public void closeIsIdempotentAndSendAfterCloseFails() {
        List<String> clientMessages = new CopyOnWriteArrayList<>();
        server = startServer(clientMessages, new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
            new AtomicReference<>(), false);
        VoiceAgentWebSocketSessionAsyncClient session = createAsyncClient(server.port()).connect("agent").block();

        StepVerifier.create(session.closeAsync().then(session.closeAsync())).verifyComplete();
        StepVerifier.create(session.sendText("after close"))
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && error.getMessage().contains("not open"))
            .verify();
    }

    @Test
    public void secureSessionUsesWssAndReceivesTypedEvent() throws Exception {
        File certificate = resourceFile("websocket-localhost-cert.pem");
        File privateKey = resourceFile("websocket-localhost-key.pem");
        Http11SslContextSpec serverSsl = Http11SslContextSpec.forServer(certificate, privateKey);
        WebsocketServerSpec websocketSpec = WebsocketServerSpec.builder().protocols("realtime").build();
        server = HttpServer.create()
            .host("127.0.0.1")
            .port(0)
            .secure(ssl -> ssl.sslContext(serverSsl))
            .handle((request, response) -> response.sendWebsocket(
                (inbound, outbound) -> outbound.sendString(Mono.just(warningJson()), StandardCharsets.UTF_8)
                    .then(outbound.sendClose()),
                websocketSpec))
            .bindNow();

        Http11SslContextSpec clientSsl
            = Http11SslContextSpec.forClient().configure(builder -> builder.trustManager(certificate));
        HttpClient httpClient = HttpClient.create().secure(ssl -> ssl.sslContext(clientSsl));
        TokenCredential credential
            = request -> Mono.just(new AccessToken("tls-token", OffsetDateTime.now().plusHours(1)));
        VoiceAgentWebSocketClientConfiguration configuration = new VoiceAgentWebSocketClientConfiguration(
            URI.create("https://localhost:" + server.port() + "/api/projects/project"), credential, "v1",
            "azure-ai-agents-test", new HttpHeaders(), null);
        VoiceAgentWebSocketSessionAsyncClient session = new VoiceAgentWebSocketSessionAsyncClient(configuration,
            "secure-agent", new VoiceAgentWebSocketConnectionOptions(), httpClient);

        session.connect().block();
        assertEquals("wss", session.getEndpoint().getScheme());
        StepVerifier.create(session.receiveEvents()).assertNext(this::assertWarningEvent).verifyComplete();
        session.close();
    }

    @Test
    public void syncSessionReceivesTypedEventAndCloses() {
        List<String> clientMessages = new CopyOnWriteArrayList<>();
        AtomicReference<String> requestUri = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> foundryFeatures = new AtomicReference<>();
        AtomicReference<String> userAgent = new AtomicReference<>();
        server = startServer(clientMessages, requestUri, authorization, foundryFeatures, userAgent, true);
        TokenCredential credential
            = request -> Mono.just(new AccessToken("sync-token", OffsetDateTime.now().plusHours(1)));
        BetaVoiceAgentWebSocketClient client
            = new AgentsClientBuilder().endpoint("http://127.0.0.1:" + server.port() + "/api/projects/project")
                .credential(credential)
                .configuration(Configuration.NONE)
                .buildBetaVoiceAgentWebSocketClient();

        try (VoiceAgentWebSocketSessionClient session = client.connect("sync-agent")) {
            RealtimeServerEvent event = session.receiveEvents().iterator().next();
            assertWarningEvent(event);
            session.sendFunctionCallOutput("call-1", "{\"temperature\":72}");
            assertTrue(session.isOpen());
        }
    }

    private BetaVoiceAgentWebSocketAsyncClient createAsyncClient(int port) {
        TokenCredential credential
            = request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        return new AgentsClientBuilder().endpoint("http://127.0.0.1:" + port + "/api/projects/project")
            .credential(credential)
            .configuration(Configuration.NONE)
            .buildBetaVoiceAgentWebSocketAsyncClient();
    }

    private DisposableServer frameWebSocketServer(org.reactivestreams.Publisher<? extends WebSocketFrame> frames) {
        WebsocketServerSpec spec = WebsocketServerSpec.builder().protocols("realtime").build();
        return HttpServer.create()
            .host("127.0.0.1")
            .port(0)
            .handle((request, response) -> response
                .sendWebsocket((inbound, outbound) -> outbound.sendObject(frames).then(outbound.sendClose()), spec))
            .bindNow();
    }

    private DisposableServer oneShotWebSocketServer(String message) {
        WebsocketServerSpec spec = WebsocketServerSpec.builder().protocols("realtime").build();
        return HttpServer.create()
            .host("127.0.0.1")
            .port(0)
            .handle((request, response) -> response.sendWebsocket((inbound,
                outbound) -> outbound.sendString(Mono.just(message), StandardCharsets.UTF_8).then(outbound.sendClose()),
                spec))
            .bindNow();
    }

    private DisposableServer startServer(List<String> clientMessages, AtomicReference<String> requestUri,
        AtomicReference<String> authorization, AtomicReference<String> foundryFeatures,
        AtomicReference<String> userAgent, boolean sendInitialEvent) {
        WebsocketServerSpec spec = WebsocketServerSpec.builder().protocols("realtime").build();
        return HttpServer.create().host("127.0.0.1").port(0).handle((request, response) -> {
            requestUri.set(request.uri());
            authorization.set(request.requestHeaders().get(HttpHeaderNames.AUTHORIZATION));
            foundryFeatures.set(request.requestHeaders().get("Foundry-Features"));
            userAgent.set(request.requestHeaders().get(HttpHeaderNames.USER_AGENT));
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
        VoiceAgentServerEventWarning warning = assertInstanceOf(VoiceAgentServerEventWarning.class, event);
        assertEquals("loopback warning", warning.getWarning().getMessage());
        assertEquals("test_warning", warning.getWarning().getCode());
    }

    private static String warningJson() {
        return "{\"type\":\"warning\",\"event_id\":\"event-1\",\"warning\":{"
            + "\"message\":\"loopback warning\",\"code\":\"test_warning\"}}";
    }

    private static File resourceFile(String name) throws Exception {
        URL resource = VoiceAgentWebSocketSessionTests.class.getClassLoader().getResource(name);
        assertNotNull(resource);
        return Paths.get(resource.toURI()).toFile();
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }
}
