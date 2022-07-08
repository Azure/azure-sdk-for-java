// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkAsyncHttpClientTests {

    private static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    private static final byte[] LONG_BODY = createLongBody();

    private static final StepVerifierOptions EMPTY_INITIAL_REQUEST_OPTIONS = StepVerifierOptions.create()
        .initialRequest(0);

    private static WireMockServer server;

    @BeforeAll
    public static void beforeClass() {
        server = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort()
            .disableRequestJournal()
            .gzipDisabled(true));

        server.stubFor(get("/short").willReturn(aResponse().withBody(SHORT_BODY)));
        server.stubFor(get("/long").willReturn(aResponse().withBody(LONG_BODY)));
        server.stubFor(get("/error").willReturn(aResponse().withBody("error").withStatus(500)));
        server.stubFor(post("/shortPost").willReturn(aResponse().withBody(SHORT_BODY)));
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
        Mono<byte[]> response = getResponse("/short").cache().flatMap(HttpResponse::getBodyAsByteArray);

        // Subscription:1
        StepVerifier.create(response)
            .assertNext(Assertions::assertNotNull)
            .expectComplete()
            .verify(Duration.ofSeconds(20));

        // Subscription:2
        // Getting the bytes of an JDK HttpClient response closes the stream on first read.
        // Subsequent reads will return an IllegalStateException due to the stream being closed.
        StepVerifier.create(response)
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
        StepVerifier.create(getResponse("/long").flatMapMany(HttpResponse::getBody), EMPTY_INITIAL_REQUEST_OPTIONS)
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
        HttpClient client = new JdkHttpClientProvider().createInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", "123")
            .setBody(Flux.error(new RuntimeException("boo")));

        StepVerifier.create(client.send(request))
            .expectErrorMessage("boo")
            .verify();
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = new JdkHttpClientProvider().createInstance();
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
                .expectErrorMessage("boo")
                .verify(Duration.ofSeconds(10));
        } catch (Exception ex) {
            assertEquals("boo", ex.getMessage());
        }
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContentFlowable() {
        HttpClient client = new JdkAsyncHttpClientBuilder().build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, "/connectionClose"));

        StepVerifier.create(client.send(request).flatMap(HttpResponse::getBodyAsByteArray))
            .verifyError(IOException.class);
    }

    @Test
    public void testConcurrentRequests() {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new JdkHttpClientProvider().createInstance();

        Mono<Long> numBytesMono = Flux.range(1, numRequests)
            .parallel(25)
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

    private static Mono<HttpResponse> getResponse(String path) {
        HttpClient client = new JdkAsyncHttpClientBuilder().build();
        return getResponse(client, path);
    }

    private static Mono<HttpResponse> getResponse(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request);
    }

    private static URL url(WireMockServer server, String path) {
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
        HttpClient client = new JdkAsyncHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, path).flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(bytes -> Assertions.assertArrayEquals(expectedBody, bytes))
            .verifyComplete();
    }

    private Mono<HttpResponse> doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request);
    }
}
