// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import okhttp3.Dispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class OkHttpAsyncHttpClientTests {
    static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";

    private static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    private static final byte[] LONG_BODY = createLongBody();

    private static WireMockServer server;

    @BeforeAll
    public static void beforeClass() {
        server = new WireMockServer(WireMockConfiguration.options()
            .extensions(new OkHttpAsyncHttpClientResponseTransformer())
            .dynamicPort()
            .disableRequestJournal()
            .gzipDisabled(true));

        server.stubFor(get("/short").willReturn(aResponse().withBody(SHORT_BODY)));
        server.stubFor(get("/long").willReturn(aResponse().withBody(LONG_BODY)));
        server.stubFor(get("/error").willReturn(aResponse().withBody("error").withStatus(500)));
        server.stubFor(post("/shortPost").willReturn(aResponse().withBody(SHORT_BODY)));
        server.stubFor(get(RETURN_HEADERS_AS_IS_PATH).willReturn(aResponse()
            .withTransformers(OkHttpAsyncHttpClientResponseTransformer.NAME)));
        server.stubFor(get("/connectionClose").willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        server.start();
    }

    @AfterAll
    public static void afterClass() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void testFlowableResponseShortBodyAsByteArrayAsync() {
        checkBodyReceived(SHORT_BODY, "/short");
    }

    @Test
    public void testFlowableResponseLongBodyAsByteArrayAsync() {
        checkBodyReceived(LONG_BODY, "/long");
    }

    @Test
    public void testMultipleSubscriptionsEmitsError() {
        HttpResponse response = getResponse("/short").block();

        // Subscription:1
        StepVerifier.create(response.getBodyAsByteArray())
            .assertNext(Assertions::assertNotNull)
            .expectComplete()
            .verify(Duration.ofSeconds(20));

        // Subscription:2
        // Getting the bytes of an OkHttp response closes the stream on first read.
        // Subsequent reads will return an IllegalStateException due to the stream being closed.
        StepVerifier.create(response.getBodyAsByteArray())
            .expectNextCount(0)
            .expectError(IllegalStateException.class)
            .verify(Duration.ofSeconds(20));

    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        StepVerifier.create(getResponse("/error")
            .flatMap(response -> {
                assertEquals(500, response.getStatusCode());
                return response.getBodyAsString();
            }))
            .expectNext("error")
            .expectComplete()
            .verify(Duration.ofSeconds(20));
    }

    @Test
    public void testFlowableBackpressure() {
        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create();
        stepVerifierOptions.initialRequest(0);

        StepVerifier.create(getResponse("/long").flatMapMany(HttpResponse::getBody), stepVerifierOptions)
            .expectNextCount(0)
            .thenRequest(1)
            .expectNextCount(1)
            .thenRequest(3)
            .expectNextCount(3)
            .thenRequest(Long.MAX_VALUE)
            .thenConsumeWhile(ByteBuffer::hasRemaining)
            .verifyComplete();
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponse() {
        HttpClient client = new OkHttpAsyncClientProvider().createInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", "123")
            .setBody(Flux.error(new RuntimeException("boo")));

        StepVerifier.create(client.send(request))
            .expectErrorMatches(e -> e.getMessage().contains("boo"))
            .verify();
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = new OkHttpAsyncClientProvider().createInstance();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", String.valueOf(contentChunk.length() * (repetitions + 1)))
            .setBody(Flux.just(contentChunk)
                .repeat(repetitions)
                .map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))
                .concatWith(Flux.error(new RuntimeException("boo"))));

        try {
            StepVerifier.create(client.send(request))
                .expectErrorMatches(e -> e.getMessage().contains("boo"))
                .verify(Duration.ofSeconds(10));
        } catch (Exception ex) {
            assertEquals("boo", ex.getMessage());
        }
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContentFlowable() {
        HttpClient client = new OkHttpAsyncClientProvider().createInstance();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, "/connectionClose"));

        StepVerifier.create(client.send(request).flatMap(HttpResponse::getBodyAsByteArray))
            .verifyError(IOException.class);
    }

    @Test
    public void testConcurrentRequests() {
        int numRequests = 100; // 100 = 1GB of data read
        int concurrency = 10;
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(concurrency); // this is 5 by default.
        HttpClient client = new OkHttpAsyncHttpClientBuilder()
            .dispatcher(dispatcher)
            .build();

        Mono<Long> numBytesMono = Flux.range(1, numRequests)
            .parallel(concurrency)
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> getResponse(client, "/long")
                .flatMapMany(HttpResponse::getBodyAsByteArray)
                .doOnNext(bytes -> assertArrayEquals(LONG_BODY, bytes)))
            .sequential()
            .map(buffer -> (long) buffer.length)
            .reduce(0L, Long::sum);

        StepVerifier.create(numBytesMono)
            .expectNext((long) numRequests * LONG_BODY.length)
            .expectComplete()
            .verify(Duration.ofSeconds(60));
    }

    @Test
    public void validateHeadersReturnAsIs() {
        HttpClient client = new OkHttpAsyncClientProvider().createInstance();

        final String singleValueHeaderName = "singleValue";
        final String singleValueHeaderValue = "value";

        final String multiValueHeaderName = "Multi-value";
        final List<String> multiValueHeaderValue = Arrays.asList("value1", "value2");

        HttpHeaders headers = new HttpHeaders()
            .set(singleValueHeaderName, singleValueHeaderValue)
            .set(multiValueHeaderName, multiValueHeaderValue);

        StepVerifier.create(client.send(new HttpRequest(HttpMethod.GET, url(server, RETURN_HEADERS_AS_IS_PATH),
                headers, Flux.empty())))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());

                HttpHeaders responseHeaders = response.getHeaders();
                HttpHeader singleValueHeader = responseHeaders.get(singleValueHeaderName);
                assertEquals(singleValueHeaderName, singleValueHeader.getName());
                assertEquals(singleValueHeaderValue, singleValueHeader.getValue());

                HttpHeader multiValueHeader = responseHeaders.get("Multi-value");
                assertEquals(multiValueHeaderName, multiValueHeader.getName());
                assertLinesMatch(multiValueHeaderValue, multiValueHeader.getValuesList());
            })
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    private static Mono<HttpResponse> getResponse(String path) {
        HttpClient client = new OkHttpAsyncHttpClientBuilder().build();
        return getResponse(client, path);
    }

    private static Mono<HttpResponse> getResponse(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request);
    }

    static URL url(WireMockServer server, String path) {
        try {
            return new URL("http://localhost:" + server.port() + path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] createLongBody() {
        byte[] duplicateBytes = "abcdefghijk".getBytes(StandardCharsets.UTF_8);
        byte[] longBody = new byte[duplicateBytes.length * 100000];

        for (int i = 0; i < 100000; i++) {
            System.arraycopy(duplicateBytes, 0, longBody, i * duplicateBytes.length, duplicateBytes.length);
        }

        return longBody;
    }

    private void checkBodyReceived(byte[] expectedBody, String path) {
        HttpClient client = new OkHttpAsyncHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, path).flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(bytes -> assertArrayEquals(expectedBody, bytes))
            .verifyComplete();
    }

    private Mono<HttpResponse> doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request);
    }
}
