// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.implementation.ReactorNettyClientProvider;
import com.azure.core.util.Context;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.azure.core.http.netty.NettyAsyncHttpClient.ReactorNettyHttpResponse;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class ReactorNettyClientTests {
    private static final String SHORT_BODY_PATH = "/short";
    private static final String LONG_BODY_PATH = "/long";

    private static final String SHORT_BODY = "hi there";
    private static final String LONG_BODY = createLongBody();

    private static WireMockServer server;

    @BeforeAll
    public static void beforeClass() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(
            WireMock.get(SHORT_BODY_PATH).willReturn(WireMock.aResponse().withBody(SHORT_BODY)));
        server.stubFor(WireMock.get(LONG_BODY_PATH).willReturn(WireMock.aResponse().withBody(LONG_BODY)));
        server.stubFor(WireMock.get("/error")
            .willReturn(WireMock.aResponse().withBody("error").withStatus(500)));
        server.stubFor(
            WireMock.post("/shortPost").willReturn(WireMock.aResponse().withBody(SHORT_BODY)));
        server.start();
        // ResourceLeakDetector.setLevel(Level.PARANOID);
    }

    @AfterAll
    public static void afterClass() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void testFluxResponseShortBodyAsByteArrayAsync() {
        checkBodyReceived(SHORT_BODY, SHORT_BODY_PATH);
    }

    @Test
    public void testFluxResponseLongBodyAsByteArrayAsync() {
        checkBodyReceived(LONG_BODY, LONG_BODY_PATH);
    }

    @Test
    public void testDispose() throws InterruptedException {
        ReactorNettyHttpResponse response = getResponse(LONG_BODY_PATH);
        response.getBody().subscribe().dispose();
        // Wait for scheduled connection disposal action to execute on netty event-loop
        Thread.sleep(5000);
        Assertions.assertTrue(response.internConnection().isDisposed());
    }

    @Test
    public void testCancel() {
        ReactorNettyHttpResponse response = getResponse(LONG_BODY_PATH);
        //
        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create();
        stepVerifierOptions.initialRequest(0);
        //
        StepVerifier.create(response.getBody(), stepVerifierOptions)
            .expectNextCount(0)
            .thenRequest(1)
            .expectNextCount(1)
            .thenCancel()
            .verify();
        Assertions.assertTrue(response.internConnection().isDisposed());
    }

    @Test
    public void testFluxWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpResponse response = getResponse("/error");
        StepVerifier.create(response.getBodyAsString())
            .expectNext("error")
            .expectComplete()
            .verify(Duration.ofSeconds(20));
        Assertions.assertEquals(500, response.getStatusCode());
    }

    @Test
    public void testFluxBackpressure() {
        HttpResponse response = getResponse(LONG_BODY_PATH);
        //
        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create();
        stepVerifierOptions.initialRequest(0);
        //
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
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", "123")
            .setBody(Flux.error(new RuntimeException("boo")));

        StepVerifier.create(client.send(request))
            .expectErrorMessage("boo")
            .verify();
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", String.valueOf(contentChunk.length() * (repetitions + 1)))
            .setBody(Flux.just(contentChunk)
                .repeat(repetitions)
                .map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))
                .concatWith(Flux.error(new RuntimeException("boo"))));
        StepVerifier.create(client.send(request))
            // .awaitDone(10, TimeUnit.SECONDS)
            .expectErrorMessage("boo")
            .verify();
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContentFlux() {
        assertTimeout(ofMillis(5000), () -> {
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
                    System.out.println(new String(bytes, 0, n, StandardCharsets.UTF_8));
                    String response = "HTTP/1.1 200 OK\r\n" //
                        + "Content-Type: text/plain\r\n" //
                        + "Content-Length: 10\r\n" //
                        + "\r\n" //
                        + "zi";
                    OutputStream out = socket.getOutputStream();
                    out.write(response.getBytes());
                    out.flush();
                    // kill the socket with HTTP response body incomplete
                    socket.close();
                    return 1;
                }).subscribeOn(Schedulers.elastic()).subscribe();
                //
                latch.await();
                HttpClient client = new NettyAsyncHttpClientBuilder().build();
                HttpRequest request = new HttpRequest(HttpMethod.GET,
                    new URL("http://localhost:" + ss.getLocalPort() + "/ioException"));

                HttpResponse response = client.send(request).block();

                assertNotNull(response);
                assertEquals(200, response.getStatusCode());

                System.out.println("reading body");

                StepVerifier.create(response.getBodyAsByteArray())
                    .verifyError(IOException.class);
            }
        });
    }

    @Test
    public void testConcurrentRequests() throws NoSuchAlgorithmException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new NettyAsyncHttpClientBuilder().build();
        byte[] expectedDigest = digest();

        Mono<Long> numBytesMono = Flux.range(1, numRequests)
            .parallel(10)
            .runOn(reactor.core.scheduler.Schedulers.newElastic("io", 30))
            .flatMap(n -> Mono.fromCallable(() -> getResponse(client, "/long")).flatMapMany(response -> {
                MessageDigest md = md5Digest();
                return response.getBody()
                    .doOnNext(md::update)
                    .map(bb -> new NumberedByteBuffer(n, bb))
                    .doOnComplete(() -> Assertions.assertArrayEquals(expectedDigest, md.digest(), "wrong digest!"));
            }))
            .sequential()
            .map(nbb -> (long) nbb.bb.limit())
            .reduce(Long::sum)
            .subscribeOn(reactor.core.scheduler.Schedulers.newElastic("io", 30))
            .publishOn(reactor.core.scheduler.Schedulers.newElastic("io", 30));

        StepVerifier.create(numBytesMono)
            .expectNext((long) (numRequests * LONG_BODY.getBytes(StandardCharsets.UTF_8).length))
            .verifyComplete();
    }

    /**
     * Tests that deep copying the buffers returned by Netty will make the stream returned to the customer resilient to
     * Netty reclaiming them once the 'onNext' operator chain has completed.
     */
    @Test
    public void deepCopyBufferConfiguredInBuilder() {
        HttpClient client = new NettyAsyncHttpClientBuilder().disableBufferCopy(false).build();

        HttpResponse response = client.send(new HttpRequest(HttpMethod.GET, url(server, LONG_BODY_PATH))).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        DelayWriteStream delayWriteStream = new DelayWriteStream();
        response.getBody().doOnNext(delayWriteStream::write).blockLast();
        assertEquals(LONG_BODY, delayWriteStream.aggregateAsString());
    }

    /**
     * Tests that preventing deep copying the buffers returned by Netty won't make the stream returned to the customer
     * resilient to Netty reclaiming them once the 'onNext' operator chain has completed.
     */
    @Test
    public void ignoreDeepCopyBufferConfiguredInBuilder() {
        HttpClient client = new NettyAsyncHttpClientBuilder().disableBufferCopy(true).build();

        HttpResponse response = client.send(new HttpRequest(HttpMethod.GET, url(server, LONG_BODY_PATH))).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        DelayWriteStream delayWriteStream = new DelayWriteStream();
        response.getBody().doOnNext(delayWriteStream::write).blockLast();
        assertNotEquals(LONG_BODY, delayWriteStream.aggregateAsString());
    }

    /**
     * Tests that deep copying of buffers is able to be configured via {@link Context}.
     */
    @Test
    public void deepCopyBufferConfiguredByContext() {
        HttpClient client = new ReactorNettyClientProvider().createInstance();

        HttpResponse response = client.send(new HttpRequest(HttpMethod.GET, url(server, LONG_BODY_PATH))).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        DelayWriteStream delayWriteStream = new DelayWriteStream();
        response.getBody().doOnNext(delayWriteStream::write).blockLast();
        assertEquals(LONG_BODY, delayWriteStream.aggregateAsString());
    }

    private static MessageDigest md5Digest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] digest() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ReactorNettyClientTests.LONG_BODY.getBytes(StandardCharsets.UTF_8));
        return md.digest();
    }

    private static final class NumberedByteBuffer {
        final long n;
        final ByteBuffer bb;

        NumberedByteBuffer(long n, ByteBuffer bb) {
            this.n = n;
            this.bb = bb;
        }
    }

    private static ReactorNettyHttpResponse getResponse(String path) {
        NettyAsyncHttpClient client = new NettyAsyncHttpClient();
        return getResponse(client, path);
    }

    private static ReactorNettyHttpResponse getResponse(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return (ReactorNettyHttpResponse) client.send(request).block();
    }

    private static URL url(WireMockServer server, String path) {
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
        StepVerifier.create(doRequest(new NettyAsyncHttpClient(), path).getBodyAsByteArray())
            .assertNext(bytes -> assertEquals(expectedBody, new String(bytes, StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    private ReactorNettyHttpResponse doRequest(NettyAsyncHttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return (ReactorNettyHttpResponse) client.send(request).block();
    }

    private static final class DelayWriteStream {
        List<ByteBuffer> internalBuffers = new ArrayList<>();

        public void write(ByteBuffer buffer) {
            internalBuffers.add(buffer);
        }

        public String aggregateAsString() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            for (ByteBuffer buffer : internalBuffers) {
                int bufferSize = buffer.remaining();
                int offset = buffer.position();

                for (int i = 0; i < bufferSize; i++) {
                    outputStream.write(buffer.get(i + offset));
                }
            }

            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
