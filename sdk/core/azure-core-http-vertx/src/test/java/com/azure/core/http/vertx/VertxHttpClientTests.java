// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.LONG_BODY;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.RETURN_HEADERS_AS_IS_PATH;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.SHORT_BODY;
import static com.azure.core.validation.http.HttpValidatonUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VertxHttpClientTests {
    private static final String SERVER_HTTP_URI = VertxHttpClientLocalTestServer.getServer().getHttpUri();

    private static final StepVerifierOptions EMPTY_INITIAL_REQUEST_OPTIONS
        = StepVerifierOptions.create().initialRequest(0);

    @Test
    public void testFlowableResponseShortBodyAsByteArrayAsync() {
        checkBodyReceived(SHORT_BODY, "/short");
    }

    @Test
    public void testFlowableResponseLongBodyAsByteArrayAsync() {
        checkBodyReceived(LONG_BODY, "/long");
    }

    @Test
    public void responseBodyAsStringAsyncWithCharset() {
        HttpClient client = new VertxHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, "/short").flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(result -> assertArrayEquals(SHORT_BODY, result))
            .verifyComplete();
    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpResponse response = getResponse("/error").block();
        assertEquals(500, response.getStatusCode());
        StepVerifier.create(response.getBodyAsString())
            .expectNext("error")
            .expectComplete()
            .verify(Duration.ofSeconds(20));
    }

    @Test
    public void testFlowableBackpressure() {
        StepVerifier.create(getResponse("/long").flatMapMany(HttpResponse::getBody), EMPTY_INITIAL_REQUEST_OPTIONS)
            .expectNextCount(0)
            .thenRequest(1)
            .expectNextCount(1)
            .thenRequest(3)
            .expectNextCount(3)
            .thenRequest(Long.MAX_VALUE)
            .thenConsumeWhile(Objects::nonNull)
            .verifyComplete();
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponse() {
        HttpClient client = new VertxHttpClientProvider().createInstance();
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, url("/shortPost")).setHeader(HttpHeaderName.CONTENT_LENGTH, "132")
                .setBody(Flux.error(new RuntimeException("boo")));

        StepVerifier.create(client.send(request)).expectErrorMessage("boo").verify();
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = new VertxHttpClientProvider().createInstance();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url("/shortPost"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)))
            .setBody(Flux.just(contentChunk)
                .repeat(repetitions)
                .map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))
                .concatWith(Flux.error(new RuntimeException("boo"))));

        try {
            StepVerifier.create(client.send(request)).expectErrorMessage("boo").verify(Duration.ofSeconds(10));
        } catch (Exception ex) {
            assertEquals("boo", ex.getMessage());
        }
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContentFlowable() {
        HttpClient client = new VertxHttpClientProvider().createInstance();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url("/connectionClose"));

        StepVerifier.create(client.send(request).flatMap(HttpResponse::getBodyAsByteArray))
            .verifyError(IOException.class);
    }

    @Test
    public void testConcurrentRequests() {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new VertxHttpClientProvider().createInstance();

        ParallelFlux<byte[]> responses = Flux.range(1, numRequests)
            .parallel((int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2.0))
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> doRequest(client, "/long"), true, 1, 1)
            .flatMap(response -> Mono.using(() -> response, HttpResponse::getBodyAsByteArray, HttpResponse::close),
                true, 1, 1);

        StepVerifier.create(responses).thenConsumeWhile(response -> {
            assertArraysEqual(LONG_BODY, response);
            return true;
        }).expectComplete().verify(Duration.ofSeconds(60));
    }

    @Test
    public void testConcurrentRequestsSync() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new VertxHttpClientProvider().createInstance();

        ForkJoinPool pool = new ForkJoinPool((int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2.0));
        List<Callable<Void>> requests = new ArrayList<>(numRequests);
        for (int i = 0; i < numRequests; i++) {
            requests.add(() -> {
                try (HttpResponse response = doRequestSync(client, "/long")) {
                    byte[] body = response.getBodyAsBinaryData().toBytes();
                    assertArraysEqual(LONG_BODY, body);
                    return null;
                }
            });
        }

        pool.invokeAll(requests);
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));
    }

    @Test
    public void validateHeadersReturnAsIs() {
        HttpClient client = new VertxHttpClientProvider().createInstance();

        HttpHeaderName singleValueHeaderName = HttpHeaderName.fromString("singleValue");
        final String singleValueHeaderValue = "value";

        HttpHeaderName multiValueHeaderName = HttpHeaderName.fromString("Multi-value");
        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");

        HttpHeaders headers = new HttpHeaders().set(singleValueHeaderName, singleValueHeaderValue)
            .set(multiValueHeaderName, multiValueHeaderValue);

        StepVerifier
            .create(client.send(new HttpRequest(HttpMethod.GET, url(RETURN_HEADERS_AS_IS_PATH), headers, Flux.empty())))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());

                HttpHeaders responseHeaders = response.getHeaders();
                HttpHeader singleValueHeader = responseHeaders.get(singleValueHeaderName);
                assertEquals(singleValueHeaderName.getCaseSensitiveName(), singleValueHeader.getName());
                assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

                HttpHeader multiValueHeader = responseHeaders.get(multiValueHeaderName);
                assertEquals(multiValueHeaderName.getCaseSensitiveName(), multiValueHeader.getName());
                assertLinesMatch(multiValueHeaderValue, multiValueHeader.getValuesList());
            })
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    @Test
    public void testBufferedResponse() {
        Context context = new Context("azure-eagerly-read-response", true);
        HttpClient client = new VertxHttpClientBuilder().build();

        StepVerifier.create(getResponse(client, "/short", context).flatMapMany(HttpResponse::getBody))
            .assertNext(buffer -> assertArrayEquals(SHORT_BODY, buffer.array()))
            .verifyComplete();
    }

    @Test
    public void testEmptyBufferResponse() {
        StepVerifier.create(getResponse("/empty").flatMapMany(HttpResponse::getBody), EMPTY_INITIAL_REQUEST_OPTIONS)
            .expectNextCount(0)
            .thenRequest(1)
            .verifyComplete();
    }

    @Test
    public void testEmptyBufferedResponse() {
        Context context = new Context("azure-eagerly-read-response", true);
        HttpClient client = new VertxHttpClientBuilder().build();

        StepVerifier
            .create(getResponse(client, "/empty", context).flatMapMany(HttpResponse::getBody),
                EMPTY_INITIAL_REQUEST_OPTIONS)
            .expectNextCount(0)
            .thenRequest(1)
            .verifyComplete();
    }

    @Test
    public void perCallTimeout() throws Exception {
        assertPerCallTimeout(false);
    }

    @Test
    public void perCallTimeoutSync() throws Exception {
        assertPerCallTimeout(true);
    }

    private static void assertPerCallTimeout(boolean synchronous) throws Exception {
        Duration perCallTimeout = Duration.ofSeconds(1);
        Duration testTimeout = Duration.ofSeconds(20);
        Vertx vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(2));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        BlockingQueue<HttpServerRequest> received = new LinkedBlockingQueue<>();
        List<Future<HttpResponse>> pending = new ArrayList<>();
        try {
            // Holding a request must not occupy a shared Jetty worker or block a Vert.x event loop.
            HttpServer server = vertx.createHttpServer()
                .requestHandler(received::add)
                .listen(0, "localhost")
                .toCompletionStage()
                .toCompletableFuture()
                .get(testTimeout.toMillis(), TimeUnit.MILLISECONDS);
            HttpClient client = new VertxHttpClientBuilder().vertx(vertx)
                .configuration(Configuration.NONE)
                .writeTimeout(Duration.ZERO)
                .readTimeout(Duration.ZERO)
                .responseTimeout(Duration.ofMinutes(1))
                .build();
            HttpRequest request
                = new HttpRequest(HttpMethod.GET, "http://localhost:" + server.actualPort() + "/timeout");
            Context override = new Context(HttpUtils.AZURE_RESPONSE_TIMEOUT, perCallTimeout);

            Future<HttpResponse> timed = sendTimeoutRequest(client, request, override, synchronous, executor);
            pending.add(timed);
            assertNotNull(received.poll(testTimeout.toMillis(), TimeUnit.MILLISECONDS),
                "The timeout request never reached the server.");
            // Never answer this request: the override must expire regardless of server scheduling delays.
            ExecutionException error = assertThrows(ExecutionException.class,
                () -> timed.get(testTimeout.toMillis(), TimeUnit.MILLISECONDS).close());
            Throwable failure = error.getCause();
            if (synchronous) {
                failure = assertInstanceOf(RuntimeException.class, failure).getCause();
            }
            assertInstanceOf(TimeoutException.class, failure);

            Future<HttpResponse> normal = sendTimeoutRequest(client, request, Context.NONE, synchronous, executor);
            pending.add(normal);
            HttpServerRequest held = received.poll(testTimeout.toMillis(), TimeUnit.MILLISECONDS);
            assertNotNull(held, "The follow-up request never reached the server.");
            // The same client/request must remain pending beyond the earlier override. An inherited one-second
            // deadline would fail with ExecutionException instead of this test-side waiting timeout.
            assertThrows(TimeoutException.class,
                () -> normal.get(perCallTimeout.multipliedBy(2).toMillis(), TimeUnit.MILLISECONDS).close());
            held.response()
                .end(Buffer.buffer(SHORT_BODY))
                .toCompletionStage()
                .toCompletableFuture()
                .get(testTimeout.toMillis(), TimeUnit.MILLISECONDS);
            try (HttpResponse response = normal.get(testTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                assertEquals(200, response.getStatusCode());
                assertArraysEqual(SHORT_BODY, response.getBodyAsByteArray().block(testTimeout));
            }
        } finally {
            pending.forEach(result -> result.cancel(true));
            try {
                vertx.close()
                    .toCompletionStage()
                    .toCompletableFuture()
                    .get(testTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } finally {
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(testTimeout.toMillis(), TimeUnit.MILLISECONDS),
                    "The synchronous request worker did not stop.");
            }
        }
    }

    private static Future<HttpResponse> sendTimeoutRequest(HttpClient client, HttpRequest request, Context context,
        boolean synchronous, ExecutorService executor) {
        return synchronous
            ? executor.submit(() -> client.sendSync(request, context))
            : client.send(request, context).toFuture();
    }

    private static Mono<HttpResponse> getResponse(String path) {
        HttpClient client = new VertxHttpClientBuilder().build();
        return getResponse(client, path, Context.NONE);
    }

    private static Mono<HttpResponse> getResponse(HttpClient client, String path, Context context) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(path));
        return client.send(request, context);
    }

    static URL url(String path) {
        try {
            return new URI(SERVER_HTTP_URI + path).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkBodyReceived(byte[] expectedBody, String path) {
        HttpClient client = new VertxHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, path).flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(bytes -> assertArrayEquals(expectedBody, bytes))
            .verifyComplete();
    }

    private static Mono<HttpResponse> doRequest(HttpClient client, String path) {
        return client.send(new HttpRequest(HttpMethod.GET, url(path)));
    }

    private static HttpResponse doRequestSync(HttpClient client, String path) {
        return client.sendSync(new HttpRequest(HttpMethod.GET, url(path)), Context.NONE);
    }
}
