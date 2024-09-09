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
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.LONG_BODY;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.RETURN_HEADERS_AS_IS_PATH;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.SHORT_BODY;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.TIMEOUT;
import static com.azure.core.validation.http.HttpValidatonUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VertxAsyncHttpClientTests {
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
        HttpClient client = new VertxAsyncHttpClientBuilder().build();
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
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, url("/shortPost")).setHeader(HttpHeaderName.CONTENT_LENGTH, "132")
                .setBody(Flux.error(new RuntimeException("boo")));

        StepVerifier.create(client.send(request)).expectErrorMessage("boo").verify();
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();
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
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url("/connectionClose"));

        StepVerifier.create(client.send(request).flatMap(HttpResponse::getBodyAsByteArray))
            .verifyError(IOException.class);
    }

    @Test
    public void testConcurrentRequests() {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();

        ParallelFlux<byte[]> responses = Flux.range(1, numRequests)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> doRequest(client, "/long"))
            .flatMap(response -> Mono.using(() -> response, HttpResponse::getBodyAsByteArray, HttpResponse::close));

        StepVerifier.create(responses).thenConsumeWhile(response -> {
            assertArraysEqual(LONG_BODY, response);
            return true;
        }).expectComplete().verify(Duration.ofSeconds(60));
    }

    @Test
    public void testConcurrentRequestsSync() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();

        ForkJoinPool pool = new ForkJoinPool();
        try {
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
        } finally {
            pool.shutdown();
            assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));
        }
    }

    @Test
    public void validateHeadersReturnAsIs() {
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();

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
        HttpClient client = new VertxAsyncHttpClientBuilder().build();

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
        HttpClient client = new VertxAsyncHttpClientBuilder().build();

        StepVerifier
            .create(getResponse(client, "/empty", context).flatMapMany(HttpResponse::getBody),
                EMPTY_INITIAL_REQUEST_OPTIONS)
            .expectNextCount(0)
            .thenRequest(1)
            .verifyComplete();
    }

    @Test
    public void perCallTimeout() {
        HttpClient client = new VertxAsyncHttpClientBuilder().responseTimeout(Duration.ofSeconds(10)).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url(TIMEOUT));

        // Verify a smaller timeout sent through Context times out the request.
        StepVerifier.create(client.send(request, new Context(HttpUtils.AZURE_RESPONSE_TIMEOUT, Duration.ofSeconds(1))))
            .expectErrorMatches(e -> e instanceof TimeoutException)
            .verify();

        // Then verify not setting a timeout through Context does not time out the request.
        StepVerifier.create(client.send(request)
            .flatMap(response -> Mono.zip(FluxUtil.collectBytesInByteBufferStream(response.getBody()),
                Mono.just(response.getStatusCode()))))
            .assertNext(tuple -> {
                assertArraysEqual(SHORT_BODY, tuple.getT1());
                assertEquals(200, tuple.getT2());
            })
            .verifyComplete();
    }

    @Test
    public void perCallTimeoutSync() {
        HttpClient client = new VertxAsyncHttpClientBuilder().responseTimeout(Duration.ofSeconds(10)).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url(TIMEOUT));

        // Verify a smaller timeout sent through Context times out the request.
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> client.sendSync(request, new Context(HttpUtils.AZURE_RESPONSE_TIMEOUT, Duration.ofSeconds(1))));
        assertInstanceOf(TimeoutException.class, ex.getCause());

        // Then verify not setting a timeout through Context does not time out the request.
        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertArraysEqual(SHORT_BODY, response.getBodyAsByteArray().block());
        }
    }

    private static Mono<HttpResponse> getResponse(String path) {
        HttpClient client = new VertxAsyncHttpClientBuilder().build();
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
        HttpClient client = new VertxAsyncHttpClientBuilder().build();
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
