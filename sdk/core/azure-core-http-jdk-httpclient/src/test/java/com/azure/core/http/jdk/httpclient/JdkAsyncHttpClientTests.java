// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkAsyncHttpClientTests {

    private static final String SHORT_BODY = "hi there";
    private static final String LONG_BODY = createLongBody();

    private static WireMockServer server;

    @BeforeAll
    public static void beforeClass() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort().disableRequestJournal());
        server.stubFor(
                WireMock.get("/short").willReturn(WireMock.aResponse().withBody(SHORT_BODY)));
        server.stubFor(WireMock.get("/long").willReturn(WireMock.aResponse().withBody(LONG_BODY)));
        server.stubFor(WireMock.get("/error")
                .willReturn(WireMock.aResponse().withBody("error").withStatus(500)));
        server.stubFor(
                WireMock.post("/shortPost").willReturn(WireMock.aResponse().withBody(SHORT_BODY)));
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
    @Disabled("This tests behaviour of reactor netty's ByteBufFlux, not applicable for httpclient")
    public void testMultipleSubscriptionsEmitsError() {
        HttpResponse response = getResponse("/short");
        // Subscription:1
        response.getBodyAsByteArray().block();
        // Subscription:2
        StepVerifier.create(response.getBodyAsByteArray())
                .expectNextCount(0) // TODO: Check with smaldini, what is the verifier operator equivalent to .awaitDone(20, TimeUnit.SECONDS)
                .verifyError(IllegalStateException.class);

    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpResponse response = getResponse("/error");
        StepVerifier.create(response.getBodyAsString())
                .expectNext("error") // TODO: .awaitDone(20, TimeUnit.SECONDS) [See previous todo]
                .verifyComplete();
        Assertions.assertEquals(500, response.getStatusCode());
    }

    @Disabled("Not working accurately at present")
    @Test
    public void testFlowableBackpressure() {
        HttpResponse response = getResponse("/long");
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
                .thenRequest(Long.MAX_VALUE)// TODO: Check with smaldini, what is the verifier operator to ignore all next emissions
                .expectNextCount(1507)
                .verifyComplete();
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponse() {
        HttpClient client = HttpClient.createDefault();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
                .setHeader("Content-Length", "123")
                .setBody(Flux.error(new RuntimeException("boo")));

        StepVerifier.create(client.send(request))
                .expectErrorMessage("boo")
                .verify();
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = HttpClient.createDefault();
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
    public void testServerShutsDownSocketShouldPushErrorToContentFlowable()
            throws IOException, InterruptedException {
        Assertions.assertTimeout(Duration.ofMillis(5000), () -> {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Socket> sock = new AtomicReference<>();
            try (ServerSocket ss = new ServerSocket(0)) {
                Mono.fromCallable(() -> {
                    latch.countDown();
                    Socket socket = ss.accept();
                    sock.set(socket);
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
                })
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
                //
                latch.await();
                HttpClient client = HttpClient.createDefault();
                HttpRequest request = new HttpRequest(HttpMethod.GET,
                    new URL("http://localhost:" + ss.getLocalPort() + "/get"));

                StepVerifier.create(client.send(request))
                    .assertNext(response -> {
                        Assertions.assertEquals(200, response.getStatusCode());
                        System.out.println("reading body");
                        StepVerifier.create(response.getBodyAsByteArray())
                            .verifyError(IOException.class);
                    })
                    .verifyComplete();
            }
        });
    }

    @Disabled("This flakey test fails often on MacOS. https://github.com/Azure/azure-sdk-for-java/issues/4357.")
    @Test
    public void testConcurrentRequests() throws NoSuchAlgorithmException {
        long t = System.currentTimeMillis();
        int numRequests = 100; // 100 = 1GB of data read
        long timeoutSeconds = 60;
        HttpClient client = HttpClient.createDefault();
        byte[] expectedDigest = digest(LONG_BODY);

        Mono<Long> numBytesMono = Flux.range(1, numRequests)
                .parallel(10)
                .runOn(Schedulers.boundedElastic())
                .flatMap(n -> Mono.fromCallable(() -> getResponse(client, "/long")).flatMapMany(response -> {
                    MessageDigest md = md5Digest();
                    return response.getBody()
                            .doOnNext(md::update)
                            .map(bb -> new NumberedByteBuffer(n, bb))
//                          .doOnComplete(() -> System.out.println("completed " + n))
                            .doOnComplete(() -> Assertions.assertArrayEquals(expectedDigest,
                                    md.digest(), "wrong digest!"));
                }))
                .sequential()
                // enable the doOnNext call to see request numbers and thread names
                // .doOnNext(g -> System.out.println(g.n + " " +
                // Thread.currentThread().getName()))
                .map(nbb -> (long) nbb.bb.limit())
                .reduce(Long::sum)
                .subscribeOn(Schedulers.boundedElastic());

        StepVerifier.create(numBytesMono)
//              .awaitDone(timeoutSeconds, TimeUnit.SECONDS)
                .expectNext((long) (numRequests * LONG_BODY.getBytes(StandardCharsets.UTF_8).length))
                .verifyComplete();
//
//        long numBytes = numBytesMono.block();
//        t = System.currentTimeMillis() - t;
//        System.out.println("totalBytesRead=" + numBytes / 1024 / 1024 + "MB in " + t / 1000.0 + "s");
//        assertEquals(numRequests * LONG_BODY.getBytes(StandardCharsets.UTF_8).length, numBytes);
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

    private static final class NumberedByteBuffer {
        final long n;
        final ByteBuffer bb;

        NumberedByteBuffer(long n, ByteBuffer bb) {
            this.n = n;
            this.bb = bb;
        }
    }

    private static HttpResponse getResponse(String path) {
        HttpClient client = new JdkAsyncHttpClientBuilder().build();
        return getResponse(client, path);
    }

    private static HttpResponse getResponse(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request).block();
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
        HttpClient client = new JdkAsyncHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, path).getBodyAsByteArray())
            .assertNext(bytes -> Assertions.assertEquals(expectedBody, new String(bytes, StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    private HttpResponse doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request).block();
    }
}
