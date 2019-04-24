// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class ReactorNettyClientTests {

    private static final String SHORT_BODY = "hi there";
    private static final String LONG_BODY = createLongBody();

    private static WireMockServer server;

    @BeforeClass
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
        // ResourceLeakDetector.setLevel(Level.PARANOID);
    }

    @AfterClass
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
        HttpResponse response = getResponse("/short");
        // Subscription:1
        response.bodyAsByteArray().block();
        // Subscription:2
        StepVerifier.create(response.bodyAsByteArray())
                .expectNextCount(0) // TODO: Check with smaldini, what is the verifier operator equivalent to .awaitDone(20, TimeUnit.SECONDS)
                .verifyError(IllegalStateException.class);

    }

    @Test
    public void testDispose() throws InterruptedException {
        HttpResponse response = getResponse("/long");
        response.body().subscribe().dispose();
        // Wait for scheduled connection disposal action to execute on netty event-loop
        Thread.sleep(5000);
        Assert.assertTrue(response.internConnection().isDisposed());
    }



    @Test
    public void testCancel() {
        HttpResponse response = getResponse("/long");
        //
        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create();
        stepVerifierOptions.initialRequest(0);
        //
        StepVerifier.create(response.body(), stepVerifierOptions)
                .expectNextCount(0)
                .thenRequest(1)
                .expectNextCount(1)
                .thenCancel()
                .verify();
        Assert.assertTrue(response.internConnection().isDisposed());
    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpResponse response = getResponse("/error");
        StepVerifier.create(response.bodyAsString())
                .expectNext("error") // TODO: .awaitDone(20, TimeUnit.SECONDS) [See previous todo]
                .verifyComplete();
        assertEquals(500, response.statusCode());
    }

    @Test
    @Ignore("Not working accurately at present")
    public void testFlowableBackpressure() {
        HttpResponse response = getResponse("/long");
        //
        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create();
        stepVerifierOptions.initialRequest(0);
        //
        StepVerifier.create(response.body(), stepVerifierOptions)
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
                .withHeader("Content-Length", "123")
                .withBody(Flux.error(new RuntimeException("boo")));

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
                .withHeader("Content-Length", String.valueOf(contentChunk.length() * repetitions))
                .withBody(Flux.just(contentChunk)
                        .repeat(repetitions)
                        .map(s -> Unpooled.wrappedBuffer(s.getBytes(StandardCharsets.UTF_8)))
                        .concatWith(Flux.error(new RuntimeException("boo"))));
        StepVerifier.create(client.send(request))
                // .awaitDone(10, TimeUnit.SECONDS)
                .expectErrorMessage("boo")
                .verify();
    }

    @Test(timeout = 5000)
    public void testServerShutsDownSocketShouldPushErrorToContentFlowable()
            throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Socket> sock = new AtomicReference<>();
        ServerSocket ss = new ServerSocket(0);
        try {
            Completable.fromCallable(() -> {
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
            .subscribeOn(Schedulers.io())
                .subscribe();
            //
            latch.await();
            HttpClient client = HttpClient.createDefault();
            HttpRequest request = new HttpRequest(HttpMethod.GET,
                    new URL("http://localhost:" + ss.getLocalPort() + "/get"));
            HttpResponse response = client.send(request).block();
            assertEquals(200, response.statusCode());
            System.out.println("reading body");
            //
            StepVerifier.create(response.bodyAsByteArray())
                    // .awaitDone(20, TimeUnit.SECONDS)
                    .verifyError(IOException.class);
        } finally {
            ss.close();
        }
    }

    @Test
    public void testConcurrentRequests() throws NoSuchAlgorithmException {
        long t = System.currentTimeMillis();
        int numRequests = 100; // 100 = 1GB of data read
        long timeoutSeconds = 60;
        HttpClient client = HttpClient.createDefault();
        byte[] expectedDigest = digest(LONG_BODY);

        Mono<Long> numBytesMono = Flux.range(1, numRequests)
                .parallel(10)
                .runOn(reactor.core.scheduler.Schedulers.newElastic("io", 30))
                .flatMap(n -> Mono.fromCallable(() -> getResponse(client, "/long")).flatMapMany(response -> {
                    MessageDigest md = md5Digest();
                    return response.body()
                            .doOnNext(bb -> {
                                bb.retain();
                                if (bb.hasArray()) {
                                    // Heap buffer
                                    md.update(bb.array());
                                } else {
                                    // Direct buffer
                                    int len = bb.readableBytes();
                                    byte[] array = new byte[len];
                                    bb.getBytes(bb.readerIndex(), array);
                                    md.update(array);
                                }
                            })
                            .map(bb -> new NumberedByteBuf(n, bb))
//                          .doOnComplete(() -> System.out.println("completed " + n))
                            .doOnComplete(() -> Assert.assertArrayEquals("wrong digest!", expectedDigest,
                                    md.digest()));
                }))
                .sequential()
                // enable the doOnNext call to see request numbers and thread names
                // .doOnNext(g -> System.out.println(g.n + " " +
                // Thread.currentThread().getName()))
                .map(nbb -> {
                    long bytesCount = (long) nbb.bb.readableBytes();
                    ReferenceCountUtil.release(nbb.bb);
                    return bytesCount;
                })
                .reduce((x, y) -> x + y)
                .subscribeOn(reactor.core.scheduler.Schedulers.newElastic("io", 30))
                .publishOn(reactor.core.scheduler.Schedulers.newElastic("io", 30));

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
        byte[] expectedDigest = md.digest();
        return expectedDigest;
    }

    private static final class NumberedByteBuf {
        final long n;
        final ByteBuf bb;

        NumberedByteBuf(long n, ByteBuf bb) {
            this.n = n;
            this.bb = bb;
        }
    }

    private static HttpResponse getResponse(String path) {
        HttpClient client = HttpClient.createDefault();
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
        StringBuilder s = new StringBuilder(10000000);
        for (int i = 0; i < 1000000; i++) {
            s.append("abcdefghijk");
        }
        return s.toString();
    }

    private void checkBodyReceived(String expectedBody, String path) {
        HttpClient client = HttpClient.createDefault();
        HttpResponse response = doRequest(client, path);
        String s = new String(response.bodyAsByteArray().block(),
                StandardCharsets.UTF_8);
        assertEquals(expectedBody, s);
    }

    private HttpResponse doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        HttpResponse response = client.send(request).block();
        return response;
    }
}
