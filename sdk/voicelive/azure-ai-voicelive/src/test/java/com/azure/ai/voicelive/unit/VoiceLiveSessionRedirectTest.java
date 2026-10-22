// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveClientBuilder;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AgentSessionConfig;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.KeyCredential;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VoiceLiveSessionRedirectTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String FAKE_KEY = "FAKE-KEY-123";
    private static final String FAKE_TOKEN = "FAKE-TOKEN-123";

    private DisposableServer origin;
    private DisposableServer redirectTarget;

    @AfterEach
    void tearDown() {
        try {
            if (origin != null) {
                origin.disposeNow(TIMEOUT);
            }
        } finally {
            if (redirectTarget != null) {
                redirectTarget.disposeNow(TIMEOUT);
            }
        }
    }

    @ParameterizedTest(name = "token={0}, session={1}, status={2}, redirect={3}")
    @MethodSource("redirectScenarios")
    void handshakeDoesNotFollowRedirects(boolean tokenCredential, SessionType sessionType, int status,
        RedirectType redirectType) {
        AtomicInteger redirectedRequests = new AtomicInteger();
        redirectTarget = HttpServer.create()
            .host(redirectType == RedirectType.CROSS_HOST ? "localhost" : "127.0.0.1")
            .port(0)
            .handle((request, response) -> {
                redirectedRequests.incrementAndGet();
                return response.status(400).send();
            })
            .bindNow(TIMEOUT);

        String location = redirectLocation(redirectType);
        AtomicInteger handshakeRequests = new AtomicInteger();
        AtomicReference<HttpHeaders> handshakeHeaders = new AtomicReference<>();
        origin = HttpServer.create().host("127.0.0.1").port(0).handle((request, response) -> {
            if ("/capture".equals(request.path())) {
                redirectedRequests.incrementAndGet();
                return response.status(400).send();
            }
            handshakeRequests.incrementAndGet();
            handshakeHeaders.set(request.requestHeaders().copy());
            return response.status(status).header(HttpHeaderNames.LOCATION, location).send();
        }).bindNow(TIMEOUT);

        AtomicReference<WebSocketClientHandshakeException> handshakeError = new AtomicReference<>();
        StepVerifier.create(startSession(createClient(tokenCredential), sessionType))
            .expectErrorSatisfies(
                error -> handshakeError.set(assertInstanceOf(WebSocketClientHandshakeException.class, error)))
            .verify(TIMEOUT);

        assertEquals(1, handshakeRequests.get());
        assertAuthentication(tokenCredential, handshakeHeaders.get());
        assertEquals(0, redirectedRequests.get(), "No request should be sent to a redirect destination");
        assertEquals(status, handshakeError.get().response().status().code());
    }

    @ParameterizedTest(name = "token={0}, session={1}")
    @MethodSource("sessionConfigurations")
    void directHandshakeStillAuthenticates(boolean tokenCredential, SessionType sessionType) {
        AtomicReference<HttpHeaders> handshakeHeaders = new AtomicReference<>();
        origin = HttpServer.create().host("127.0.0.1").port(0).handle((request, response) -> {
            handshakeHeaders.set(request.requestHeaders().copy());
            return response.sendWebsocket((inbound, outbound) -> inbound.receive().then(),
                WebsocketServerSpec.builder().protocols("realtime").build());
        }).bindNow(TIMEOUT);

        VoiceLiveSessionAsyncClient session = startSession(createClient(tokenCredential), sessionType).block(TIMEOUT);
        try {
            assertNotNull(session);
            assertAuthentication(tokenCredential, handshakeHeaders.get());
        } finally {
            if (session != null) {
                session.closeAsync().block(TIMEOUT);
            }
        }
    }

    private VoiceLiveAsyncClient createClient(boolean tokenCredential) {
        VoiceLiveClientBuilder builder = new VoiceLiveClientBuilder().endpoint("http://127.0.0.1:" + origin.port());
        if (tokenCredential) {
            builder.credential(context -> Mono.just(new AccessToken(FAKE_TOKEN, OffsetDateTime.now().plusHours(1))));
        } else {
            builder.credential(new KeyCredential(FAKE_KEY));
        }
        return builder.buildAsyncClient();
    }

    private static Mono<VoiceLiveSessionAsyncClient> startSession(VoiceLiveAsyncClient client,
        SessionType sessionType) {
        switch (sessionType) {
            case DEFAULT:
                return client.startSession();

            case MODEL:
                return client.startSession("fake-model", null);

            case AGENT:
                return client.startSession(new AgentSessionConfig("fake-agent", "fake-project"), null);

            default:
                throw new IllegalArgumentException("Unexpected session type: " + sessionType);
        }
    }

    private String redirectLocation(RedirectType redirectType) {
        switch (redirectType) {
            case CROSS_HOST:
                return "ws://localhost:" + redirectTarget.port() + "/capture";

            case CROSS_PORT:
                return "ws://127.0.0.1:" + redirectTarget.port() + "/capture";

            case RELATIVE:
                return "/capture";

            default:
                throw new IllegalArgumentException("Unexpected redirect type: " + redirectType);
        }
    }

    private static void assertAuthentication(boolean tokenCredential, HttpHeaders headers) {
        assertNotNull(headers);
        assertEquals(tokenCredential ? "Bearer " + FAKE_TOKEN : FAKE_KEY,
            headers.get(tokenCredential ? HttpHeaderNames.AUTHORIZATION.toString() : "api-key"));
    }

    private static Stream<Arguments> sessionConfigurations() {
        return Stream.of(false, true)
            .flatMap(tokenCredential -> Stream.of(SessionType.values())
                .map(sessionType -> Arguments.of(tokenCredential, sessionType)));
    }

    private static Stream<Arguments> redirectScenarios() {
        return Stream.of(false, true)
            .flatMap(tokenCredential -> Stream.of(SessionType.values())
                .flatMap(sessionType -> IntStream.of(301, 302, 303, 307, 308)
                    .boxed()
                    .flatMap(status -> Stream.of(RedirectType.values())
                        .map(redirectType -> Arguments.of(tokenCredential, sessionType, status, redirectType)))));
    }

    private enum SessionType {
        DEFAULT, MODEL, AGENT
    }

    private enum RedirectType {
        CROSS_HOST, CROSS_PORT, RELATIVE
    }
}
