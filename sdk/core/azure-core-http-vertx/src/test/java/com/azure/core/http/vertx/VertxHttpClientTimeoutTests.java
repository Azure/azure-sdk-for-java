// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VertxHttpClientTimeoutTests {
    private static final Duration TEST_TIMEOUT = Duration.ofSeconds(5);

    @ParameterizedTest
    @CsvSource({ "false, false", "false, true", "true, false", "true, true" })
    public void responseTimeoutReleasesConnection(boolean synchronous, boolean perCallTimeout) throws Exception {
        try (TimeoutTestServer server = new TimeoutTestServer()) {
            HttpClient client = server.client(perCallTimeout ? Duration.ZERO : Duration.ofSeconds(1));
            Context context
                = perCallTimeout ? new Context(HttpUtils.AZURE_RESPONSE_TIMEOUT, Duration.ofSeconds(1)) : Context.NONE;
            HttpRequest request = server.request("/hang");

            if (synchronous) {
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    try (HttpResponse response = client.sendSync(request, context)) {
                        assertEquals(200, response.getStatusCode());
                    }
                });
                assertInstanceOf(TimeoutException.class, exception.getCause());
            } else {
                StepVerifier.create(client.send(request, context))
                    .expectError(TimeoutException.class)
                    .verify(TEST_TIMEOUT);
            }

            assertNotNull(server.received.get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
            assertFastRequestSucceeds(client, server);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void cancelledRequestReleasesConnection(boolean bufferingResponse) throws Exception {
        try (TimeoutTestServer server = new TimeoutTestServer()) {
            HttpClient client = server.client(Duration.ZERO);
            Context context
                = bufferingResponse ? new Context(HttpUtils.AZURE_EAGERLY_READ_RESPONSE, true) : Context.NONE;
            CompletableFuture<HttpResponse> pending
                = client.send(server.request(bufferingResponse ? "/buffered" : "/hang"), context).toFuture();

            assertNotNull(server.received.get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
            assertFalse(pending.isDone());
            assertTrue(pending.cancel(true));

            assertFastRequestSucceeds(client, server);
        }
    }

    @Test
    public void cancelledQueuedRequestDoesNotHoldConnection() throws Exception {
        try (TimeoutTestServer server = new TimeoutTestServer()) {
            HttpClient client = server.client(Duration.ZERO);
            CompletableFuture<HttpResponse> first = client.send(server.request("/hang")).toFuture();
            HttpServerRequest held = server.received.get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            // The first request owns the only connection, so this request is waiting for the pool.
            CompletableFuture<HttpResponse> queued = client.send(server.request("/hang")).toFuture();
            assertTrue(queued.cancel(true));

            held.response().end("ok");
            try (HttpResponse response = first.get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                assertEquals("ok", response.getBodyAsString().block(TEST_TIMEOUT));
            }

            // A cancelled request must not occupy the connection when acquisition eventually completes.
            assertFastRequestSucceeds(client, server);
        }
    }

    @Test
    public void successfulRequestsReuseConnection() throws Exception {
        try (TimeoutTestServer server = new TimeoutTestServer()) {
            HttpClient client = server.client(Duration.ofSeconds(1));
            StepVerifier
                .create(client.send(server.request("/fast"), new Context(HttpUtils.AZURE_EAGERLY_READ_RESPONSE, true))
                    .flatMap(
                        response -> Mono.using(() -> response, HttpResponse::getBodyAsString, HttpResponse::close)))
                .expectNext("ok")
                .expectComplete()
                .verify(TEST_TIMEOUT);

            assertFastRequestSucceeds(client, server);
            assertEquals(1, server.connections.get());
        }
    }

    private static void assertFastRequestSucceeds(HttpClient client, TimeoutTestServer server) {
        StepVerifier
            .create(client.send(server.request("/fast"))
                .flatMap(response -> Mono.using(() -> response, HttpResponse::getBodyAsString, HttpResponse::close)))
            .expectNext("ok")
            .expectComplete()
            .verify(TEST_TIMEOUT);
    }

    private static final class TimeoutTestServer implements AutoCloseable {
        private final Vertx vertx = Vertx.vertx();
        private final CompletableFuture<HttpServerRequest> received = new CompletableFuture<>();
        private final AtomicInteger connections = new AtomicInteger();
        private final HttpServer server;

        private TimeoutTestServer() throws Exception {
            server = vertx.createHttpServer()
                .connectionHandler(ignored -> connections.incrementAndGet())
                .requestHandler(request -> {
                    if ("/hang".equals(request.path())) {
                        received.complete(request);
                    } else if ("/buffered".equals(request.path())) {
                        request.response()
                            .setChunked(true)
                            .write("partial")
                            .onSuccess(ignored -> received.complete(request));
                    } else {
                        request.response().end("ok");
                    }
                })
                .listen(0, "localhost")
                .toCompletionStage()
                .toCompletableFuture()
                .get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        }

        private HttpClient client(Duration responseTimeout) {
            // No other timeout may release the connection and mask missing response-timeout/cancellation cleanup.
            HttpClientOptions options = new HttpClientOptions().setMaxPoolSize(1)
                .setIdleTimeout(0)
                .setReadIdleTimeout(0)
                .setWriteIdleTimeout(0);
            return new VertxHttpClientBuilder().vertx(vertx)
                .httpClientOptions(options)
                .responseTimeout(responseTimeout)
                .build();
        }

        private HttpRequest request(String path) {
            return new HttpRequest(HttpMethod.GET, "http://localhost:" + server.actualPort() + path);
        }

        @Override
        public void close() {
            try {
                vertx.close()
                    .toCompletionStage()
                    .toCompletableFuture()
                    .get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
