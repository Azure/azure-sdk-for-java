// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.vertx.core.http.HttpClosedException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class VertxAsyncHttpClientTests {
    static final String RETURN_HEADERS_AS_IS_PATH = "/returnHeadersAsIs";
    private static final String SHORT_BODY = "hi there";
    private static final String LONG_BODY = createLongBody();
    private static WireMockServer server;

    @BeforeAll
    public static void beforeAll() {
        server = new WireMockServer(WireMockConfiguration.options()
            .extensions(new VertxAsyncHttpClientResponseTransformer())
            .dynamicPort()
            .disableRequestJournal()
            .gzipDisabled(true));

        server.stubFor(get("/short").willReturn(aResponse().withBody(SHORT_BODY)));
        server.stubFor(get("/long").willReturn(aResponse().withBody(LONG_BODY)));
        server.stubFor(get("/error").willReturn(aResponse().withBody("error").withStatus(500)));
        server.stubFor(post("/shortPost").willReturn(aResponse().withBody(SHORT_BODY)));
        server.stubFor(get(RETURN_HEADERS_AS_IS_PATH).willReturn(aResponse()
            .withTransformers(VertxAsyncHttpClientResponseTransformer.NAME)));
        server.stubFor(get("/empty").willReturn(aResponse().withBody(new byte[0])));
        server.start();
    }

    @AfterAll
    public static void afterAll() throws Exception {
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
    public void responseBodyAsStringAsyncWithCharset() {
        HttpClient client = new VertxAsyncHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, "/short").getBodyAsString(StandardCharsets.UTF_8))
            .assertNext(result -> assertEquals(SHORT_BODY, result))
            .verifyComplete();
    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpResponse response = getResponse("/error");
        assertEquals(500, response.getStatusCode());
        StepVerifier.create(response.getBodyAsString())
            .expectNext("error")
            .expectComplete()
            .verify(Duration.ofSeconds(20));
    }

    @Test
    public void testFlowableBackpressure() {
        HttpResponse response = getResponse("/long");

        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create();
        stepVerifierOptions.initialRequest(0);

        StepVerifier.create(response.getBody(), stepVerifierOptions)
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
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", "123")
            .setBody(Flux.error(new RuntimeException("boo")));

        StepVerifier.create(client.send(request))
            .expectErrorMessage("boo")
            .verify();
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();
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
        Assertions.assertTimeout(Duration.ofMillis(5000), () -> {
            CountDownLatch latch = new CountDownLatch(1);
            try (ServerSocket ss = new ServerSocket(0)) {
                Mono.fromCallable(() -> {
                    latch.countDown();
                    Socket socket = ss.accept();
                    // give the client time to get request across
                    Thread.sleep(500);
                    // respond but don't send the complete response
                    byte[] bytes = new byte[1024];
                    int n = socket.getInputStream().read(bytes);
                    String response = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/plain\r\n"
                        + "Content-Length: 10\r\n"
                        + "\r\n" //
                        + "zi";
                    OutputStream out = socket.getOutputStream();
                    out.write(response.getBytes());
                    out.flush();
                    // kill the socket with HTTP response body incomplete
                    socket.close();
                    return 1;
                }).subscribeOn(Schedulers.boundedElastic()).subscribe();
                //
                latch.await();
                HttpClient client = new VertxAsyncHttpClientBuilder().build();
                HttpRequest request = new HttpRequest(HttpMethod.GET,
                    new URL("http://localhost:" + ss.getLocalPort() + "/ioException"));

                StepVerifier.create(client.send(request)
                        .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getBody())
                            .zipWith(Mono.just(response.getStatusCode()))))
                    .expectError(HttpClosedException.class)
                    .verify(Duration.ofSeconds(5));
            }
        });
    }

    @Test
    public void testConcurrentRequests() throws NoSuchAlgorithmException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();
        byte[] expectedDigest = digest(LONG_BODY);
        long expectedByteCount = (long) numRequests * LONG_BODY.getBytes(StandardCharsets.UTF_8).length;

        Mono<Long> numBytesMono = Flux.range(1, numRequests)
            .parallel(10)
            .runOn(Schedulers.boundedElastic())
            .flatMap(n -> Mono.fromCallable(() -> getResponse(client, "/long", Context.NONE)).flatMapMany(response -> {
                MessageDigest md = md5Digest();
                return response.getBody()
                    .doOnNext(buffer -> md.update(buffer.duplicate()))
                    .doOnComplete(() -> assertArrayEquals(expectedDigest, md.digest(), "wrong digest!"));
            }))
            .sequential()
            .map(buffer -> (long) buffer.remaining())
            .reduce(Long::sum);

        StepVerifier.create(numBytesMono)
            .expectNext(expectedByteCount)
            .expectComplete()
            .verify(Duration.ofSeconds(60));
    }

    @Test
    public void validateHeadersReturnAsIs() {
        HttpClient client = new VertxAsyncHttpClientProvider().createInstance();

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

    @Test
    public void testBufferedResponse() {
        Context context = new Context("azure-eagerly-read-response", true);
        HttpClient client = new VertxAsyncHttpClientBuilder().build();
        HttpResponse response = getResponse(client, "/short", context);

        StepVerifier.create(response.getBody())
            .assertNext(buffer -> {
                assertEquals(SHORT_BODY, new String(buffer.array()));
            })
            .verifyComplete();
    }

    @Test
    public void testEmptyBufferResponse() {
        HttpResponse response = getResponse("/empty");

        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create();
        stepVerifierOptions.initialRequest(0);

        StepVerifier.create(response.getBody(), stepVerifierOptions)
            .expectNextCount(0)
            .thenRequest(1)
            .verifyComplete();
    }

    @Test
    public void testEmptyBufferedResponse() {
        Context context = new Context("azure-eagerly-read-response", true);
        HttpClient client = new VertxAsyncHttpClientBuilder().build();
        HttpResponse response = getResponse(client, "/empty", context);

        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create();
        stepVerifierOptions.initialRequest(0);

        StepVerifier.create(response.getBody(), stepVerifierOptions)
            .expectNextCount(0)
            .thenRequest(1)
            .verifyComplete();
    }

    private static MessageDigest md5Digest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] digest(String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(s.getBytes(StandardCharsets.UTF_8));
        return md.digest();
    }

    private static HttpResponse getResponse(String path) {
        HttpClient client = new VertxAsyncHttpClientBuilder().build();
        return getResponse(client, path, Context.NONE);
    }

    private static HttpResponse getResponse(HttpClient client, String path, Context context) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request, context).block();
    }

    static URL url(WireMockServer server, String path) {
        try {
            return new URL("http://localhost:" + server.port() + path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createLongBody() {
        StringBuilder builder = new StringBuilder("abcdefghijk".length() * 1000000);
        for (int i = 0; i < 1000000; i++) {
            builder.append("abcdefghijk");
        }

        return builder.toString();
    }

    private void checkBodyReceived(String expectedBody, String path) {
        HttpClient client = new VertxAsyncHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, path).getBodyAsByteArray())
            .assertNext(bytes -> assertEquals(expectedBody, new String(bytes, StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    private HttpResponse doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request).block();
    }
}
