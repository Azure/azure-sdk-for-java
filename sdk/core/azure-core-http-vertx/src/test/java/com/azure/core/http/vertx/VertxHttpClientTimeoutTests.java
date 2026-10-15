// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.PoolOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
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

    @ParameterizedTest
    @CsvSource({ "false, false", "false, true", "true, false", "true, true" })
    public void completedWriteDoesNotTimeOutResponse(boolean synchronous, boolean slowBody) throws Exception {
        try (TimeoutTestServer server = new TimeoutTestServer()) {
            com.azure.core.util.HttpClientOptions options
                = new com.azure.core.util.HttpClientOptions().setWriteTimeout(Duration.ofMillis(500))
                    .setResponseTimeout(Duration.ofSeconds(2))
                    .setReadTimeout(Duration.ofSeconds(2));
            HttpClient client = new VertxHttpClientProvider().createInstance(options);
            HttpRequest request = server.request(slowBody ? "/slow-body" : "/slow-headers");
            if (synchronous) {
                try (HttpResponse response = client.sendSync(request, Context.NONE)) {
                    assertEquals("ok", response.getBodyAsString().block(TEST_TIMEOUT));
                }
            } else {
                StepVerifier
                    .create(client.send(request)
                        .flatMap(
                            response -> Mono.using(() -> response, HttpResponse::getBodyAsString, HttpResponse::close)))
                    .expectNext("ok")
                    .expectComplete()
                    .verify(TEST_TIMEOUT);
            }
        }
    }

    @Test
    public void stalledUploadTimesOutAndCancelsBody() throws Exception {
        try (TimeoutTestServer server = new TimeoutTestServer()) {
            CompletableFuture<Void> cancelled = new CompletableFuture<>();
            HttpRequest request = server.request("/upload")
                .setHttpMethod(HttpMethod.POST)
                .setBody(Flux.just(ByteBuffer.wrap(new byte[] { 1 }))
                    .concatWith(Flux.never())
                    .doOnCancel(() -> cancelled.complete(null)));
            HttpClient client = server.writeTimeoutClient();

            StepVerifier.create(client.send(request))
                .expectErrorMatches(error -> error instanceof TimeoutException && error.getMessage().contains("write"))
                .verify(TEST_TIMEOUT);
            cancelled.get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            assertFastRequestSucceeds(client, server);
        }
    }

    @Test
    public void uploadProgressRestartsWriteTimeout() throws Exception {
        try (TimeoutTestServer server = new TimeoutTestServer()) {
            HttpRequest request = server.request("/upload")
                .setHttpMethod(HttpMethod.POST)
                .setBody(
                    Flux.interval(Duration.ofMillis(100)).take(6).map(ignored -> ByteBuffer.wrap(new byte[] { 1 })));
            StepVerifier.create(server.writeTimeoutClient()
                .send(request)
                .flatMap(response -> Mono.using(() -> response, HttpResponse::getBodyAsString, HttpResponse::close)))
                .expectNext("ok")
                .expectComplete()
                .verify(TEST_TIMEOUT);
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
                    } else if ("/slow-headers".equals(request.path())) {
                        vertx.setTimer(600, ignored -> request.response().end("ok"));
                    } else if ("/slow-body".equals(request.path())) {
                        request.response().setChunked(true).write("o");
                        vertx.setTimer(600, ignored -> request.response().end("k"));
                    } else if ("/upload".equals(request.path())) {
                        request.body().onSuccess(ignored -> request.response().end("ok"));
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
            HttpClientOptions options
                = new HttpClientOptions().setIdleTimeout(0).setReadIdleTimeout(0).setWriteIdleTimeout(0);
            PoolOptions poolOptions = new PoolOptions().setHttp1MaxSize(1);
            try {
                // PoolOptions is available in both runtimes; HttpClientOptions.setMaxPoolSize is removed in 5.
                // Resolve the factory because its return type also changes in Vert.x 5.
                io.vertx.core.http.HttpClient nativeClient = (io.vertx.core.http.HttpClient) ReflectionUtils
                    .getMethodInvoker(Vertx.class,
                        Vertx.class.getMethod("createHttpClient", HttpClientOptions.class, PoolOptions.class), true)
                    .invokeWithArguments(vertx, options, poolOptions);
                return new VertxHttpClient(nativeClient, options, responseTimeout);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private HttpClient writeTimeoutClient() {
            return new VertxHttpClientBuilder().vertx(vertx)
                .configuration(Configuration.NONE)
                .writeTimeout(Duration.ofMillis(500))
                .responseTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(2))
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
