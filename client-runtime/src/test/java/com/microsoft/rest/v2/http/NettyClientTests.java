package com.microsoft.rest.v2.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

public class NettyClientTests {

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
        response.bodyAsByteArray().blockingGet();
        // subscribe again
        response.bodyAsByteArray() //
                .test() //
                .awaitDone(20, TimeUnit.SECONDS) //
                .assertNoValues() //
                .assertError(IllegalStateException.class);
    }

    @Test
    public void testCancel() throws InterruptedException {
        HttpResponse response = getResponse("/long");
        TestSubscriber<ByteBuffer> ts = response.body() //
                .test(0) //
                .requestMore(1) //
                .awaitCount(1) //
                .assertNotComplete() //
                .assertValueCount(1);
        ts.cancel();
        Thread.sleep(100);
        ts.requestMore(100);
        Thread.sleep(100);
        ts.assertNotComplete() //
                .assertValueCount(1);
    }

    @Test
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500Returned() {
        HttpResponse response = getResponse("/error");
        response //
                .bodyAsString() //
                .test() //
                .awaitDone(20, TimeUnit.SECONDS) //
                .assertValues("error") //
                .assertNoErrors();
        assertEquals(500, response.statusCode());
    }

    @Test
    public void testFlowableBackpressure() {
        HttpResponse response = getResponse("/long");
        response //
                .body() //
                .test(0) //
                .assertValueCount(0) //
                .assertNoErrors() //
                .requestMore(1) //
                .awaitCount(1) //
                .assertValueCount(1) ///
                .requestMore(3) //
                .awaitCount(4) //
                .requestMore(Long.MAX_VALUE) //
                .awaitDone(20, TimeUnit.SECONDS).assertComplete();
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponse() {
        HttpClient client = HttpClient.createDefault();
        HttpRequest request = new HttpRequest("", HttpMethod.POST, url(server, "/shortPost"), null) //
                .withBody(Flowable.error(new RuntimeException("boo")));
        client.sendRequestAsync(request)
                .test() //
                .awaitDone(10, TimeUnit.SECONDS) //
                .assertNoValues() //
                .assertErrorMessage("boo");
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = HttpClient.createDefault();
        HttpRequest request = new HttpRequest("", HttpMethod.POST, url(server, "/shortPost"), null) //
                .withBody(Flowable.just("abcdefgh") //
                        .repeat(1000) //
                        .map(NettyClientTests::toByteBuffer) //
                        .concatWith(Flowable.error(new RuntimeException("boo"))));
        client.sendRequestAsync(request)
                .test() //
                .awaitDone(10, TimeUnit.SECONDS) //
                .assertNoValues() //
                .assertErrorMessage("boo");
    }


    private static ByteBuffer toByteBuffer(String s) {
        return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testRequestBeforeShutdownSucceeds() throws Exception {
        final HttpClientFactory factory = new NettyClient.Factory();
        HttpClient client = factory.create(null);
        HttpRequest request = new HttpRequest("", HttpMethod.GET, url(server, "/long"), null);
        client.sendRequestAsync(request).blockingGet();
        factory.close();
    }

    @Test
    public void testRequestAfterShutdownIsRejected() throws Exception {
        final HttpClientFactory factory = new NettyClient.Factory();
        HttpClient client = factory.create(null);
        HttpRequest request = new HttpRequest("", HttpMethod.GET, url(server, "/get"), null);

        LoggerFactory.getLogger(getClass()).info("Closing factory");
        factory.close();

        try {
            LoggerFactory.getLogger(getClass()).info("Sending request");
            client.sendRequestAsync(request).blockingGet();
            fail();
        } catch (RejectedExecutionException ignored) {
            // expected
        }
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
            }) //
                    .subscribeOn(Schedulers.io()) //
                    .subscribe();
            latch.await();
            HttpClient client = HttpClient.createDefault();
            HttpRequest request = new HttpRequest("", HttpMethod.GET,
                    new URL("http://localhost:" + ss.getLocalPort() + "/get"), null);
            HttpResponse response = client.sendRequestAsync(request).blockingGet();
            assertEquals(200, response.statusCode());
            System.out.println("reading body");
            response.bodyAsByteArray() //
                    .test() //
                    .awaitDone(20, TimeUnit.SECONDS) //
                    .assertError(IOException.class) //
                    .assertErrorMessage("channel inactive");
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
        long numBytes = Flowable.range(1, numRequests) //
                // Note that WireMock default threads for accepting connections is 10
                // we start 10 different threads each of which will deal with the range
                // in round-robin fashion
                .parallel(10) //
                .runOn(Schedulers.io()) //
                .flatMap(n -> Single //
                        .fromCallable(() -> getResponse(client, "/long")) //
                        .flatMapPublisher(response -> {
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            return response //
                                    .body() //
                                    .doOnNext(bb -> md.update(bb)) //
                                    .map(bb -> new NumberedByteBuffer(n, bb))
//                                    .doOnComplete(() -> System.out.println("completed " + n)) //
                                    .doOnComplete(() -> Assert.assertArrayEquals("wrong digest!", expectedDigest,
                                            md.digest()));
                        }))
                .sequential() //
                // enable the doOnNext call to see request numbers and thread names
                // .doOnNext(g -> System.out.println(g.n + " " +
                // Thread.currentThread().getName())) //
                .map(nbb -> (long) nbb.bb.limit()) //
                .reduce((x, y) -> x + y) //
                .subscribeOn(Schedulers.io()) //
                .observeOn(Schedulers.io()) //
                .test() //
                .awaitDone(timeoutSeconds, TimeUnit.SECONDS) //
                .assertComplete() //
                .assertValueCount(1) //
                .values() //
                .get(0);
        t = System.currentTimeMillis() - t;
        System.out.println("totalBytesRead=" + numBytes / 1024 / 1024 + "MB in " + t / 1000.0 + "s");
        assertEquals(numRequests * LONG_BODY.getBytes(StandardCharsets.UTF_8).length, numBytes);
    }

    private static byte[] digest(String s) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(s.getBytes(StandardCharsets.UTF_8));
        byte[] expectedDigest = md.digest();
        return expectedDigest;
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
        HttpClient client = HttpClient.createDefault();
        return getResponse(client, path);
    }

    private static HttpResponse getResponse(HttpClient client, String path) {
        HttpRequest request = new HttpRequest("", HttpMethod.GET, url(server, path), null);
        return client.sendRequestAsync(request).blockingGet();
    }

    @Test
    @Ignore("Fails intermittently due to race condition")
    public void testInFlightRequestSucceedsAfterCancellation() throws Exception {
        // Retry a few times in case shutdown begins before the request is submitted to
        // Netty
        for (int i = 0; i < 3; i++) {
            final HttpClientFactory factory = new NettyClient.Factory();
            HttpClient client = factory.create(null);
            HttpRequest request = new HttpRequest("", HttpMethod.GET, url(server, "/get"), null);

            Future<HttpResponse> asyncResponse = client.sendRequestAsync(request).toFuture();
            Thread.sleep(100);
            factory.close();

            boolean shouldRetry = false;
            try {
                asyncResponse.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                shouldRetry = true;
            }

            if (!shouldRetry) {
                break;
            }

            if (i == 2) {
                fail();
            } else {
                LoggerFactory.getLogger(getClass())
                        .info("Shutdown started before sending request. Retry attempt " + (i + 1));
            }
        }
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
        String s = new String(response.bodyAsByteArray().blockingGet(),
                StandardCharsets.UTF_8);
        assertEquals(expectedBody, s);
    }

    private HttpResponse doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest("", HttpMethod.GET, url(server, path), null);
        HttpResponse response = client.sendRequestAsync(request).blockingGet();
        return response;
    }

}
