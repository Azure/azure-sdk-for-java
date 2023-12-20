// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.UrlBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledForJreRange(max = JRE.JAVA_11)
@Execution(ExecutionMode.SAME_THREAD)
public class JdkHttpClientTests {

    private static final byte[] SHORT_BODY = "hi there".getBytes(StandardCharsets.UTF_8);
    private static final byte[] LONG_BODY = createLongBody();

    private static final StepVerifierOptions EMPTY_INITIAL_REQUEST_OPTIONS = StepVerifierOptions.create()
        .initialRequest(0);

    private static LocalTestServer server;

    @BeforeAll
    public static void beforeClass() {
        server = new LocalTestServer((req, resp, requestBody) -> {
            String path = req.getServletPath();
            boolean get = "GET".equalsIgnoreCase(req.getMethod());
            boolean post = "POST".equalsIgnoreCase(req.getMethod());

            if (get && "/short".equals(path)) {
                resp.setContentLength(SHORT_BODY.length);
                resp.getOutputStream().write(SHORT_BODY);
            } else if (get && "/long".equals(path)) {
                resp.setContentLength(LONG_BODY.length);
                resp.getOutputStream().write(LONG_BODY);
            } else if (get && "/error".equals(path)) {
                resp.setStatus(500);
                resp.setContentLength(5);
                resp.getOutputStream().write("error".getBytes(StandardCharsets.UTF_8));
            } else if (post && "/shortPost".equals(path)) {
                resp.setContentLength(SHORT_BODY.length);
                resp.getOutputStream().write(SHORT_BODY);
            } else if (get && "/connectionClose".equals(path)) {
                resp.getHttpChannel().getConnection().close();
            } else if (post && "/shortPostWithBodyValidation".equals(path)) {
                if (!Arrays.equals(LONG_BODY, 1, 43, requestBody, 0, 42)) {
                    resp.sendError(400, "Request body does not match expected value");
                }
            } else {
                throw new ServletException("Unexpected request: " + req.getMethod() + " " + path);
            }
        });

        server.start();
    }

    @AfterAll
    public static void afterClass() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testFlowableResponseShortBodyAsByteArrayAsync() {
        checkBodyReceived(SHORT_BODY, "/short");
    }

    @Test
    public void testFlowableResponseShortBodyAsByteArraySync() throws IOException {
        checkBodyReceivedSync(SHORT_BODY, "/short");
    }

    @Test
    public void testFlowableResponseLongBodyAsByteArrayAsync() {
        checkBodyReceived(LONG_BODY, "/long");
    }

    @Test
    public void testResponseLongBodyAsByteArraySync() throws IOException {
        checkBodyReceivedSync(LONG_BODY, "/long");
    }

    @Test
    public void testBufferResponseSync() {
        HttpClient client = new JdkHttpClientBuilder().build();
        try (HttpResponse response = doRequestSync(client, "/long").buffer()) {
            TestUtils.assertArraysEqual(LONG_BODY, response.getBodyAsBinaryData().toBytes());
        }
    }

    @Test
    public void testBufferedResponseSync() {
        HttpClient client = new JdkHttpClientBuilder().build();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, "/long"));
        try (HttpResponse response = client.sendSync(request, new Context("azure-eagerly-read-response", true))) {
            TestUtils.assertArraysEqual(LONG_BODY, response.getBodyAsBinaryData().toBytes());
        }
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
        // Subsequent reads will return an IllegalStateException (from reactor) due to the stream being closed.
        StepVerifier.create(response)
            .expectNextCount(0)
            .expectError(IllegalStateException.class)
            .verify(Duration.ofSeconds(20));

    }

    @Test
    public void testMultipleGetBodyBytesSync() {
        HttpClient client = new JdkHttpClientBuilder().build();
        try (HttpResponse response = doRequestSync(client, "/short")) {
            Mono<byte[]> responseBody = response.getBodyAsByteArray();

            // Subscription:1
            StepVerifier.create(responseBody)
                .assertNext(Assertions::assertNotNull)
                .expectComplete()
                .verify(Duration.ofSeconds(20));

            // Subscription:2
            // Getting the bytes of an JDK HttpClient response closes the stream on first read.
            // Subsequent reads will return an IOException due to the stream being closed.
            StepVerifier.create(responseBody)
                .expectNextCount(0)
                .expectError(IOException.class)
                .verify(Duration.ofSeconds(20));
        }
    }

    @Test
    @Timeout(20)
    public void testMultipleGetBinaryDataSync() {
        HttpClient client = new JdkHttpClientBuilder().build();
        try (HttpResponse response = doRequestSync(client, "/short")) {
            TestUtils.assertArraysEqual(SHORT_BODY, response.getBodyAsBinaryData().toBytes());
            TestUtils.assertArraysEqual(SHORT_BODY, response.getBodyAsBinaryData().toBytes());
        }
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
    @Timeout(20)
    public void testFlowableWhenServerReturnsBodyAndNoErrorsWhenHttp500ReturnedSync() {
        HttpClient client = new JdkHttpClientBuilder().build();
        try (HttpResponse response = doRequestSync(client, "/error")) {
            assertEquals(500, response.getStatusCode());
            assertEquals("error", response.getBodyAsString().block());
        }
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
            .setHeader(HttpHeaderName.CONTENT_LENGTH, "132")
            .setBody(Flux.error(new RuntimeException("boo")));

        StepVerifier.create(client.send(request))
            .expectErrorMessage("boo")
            .verify();
    }

    @Test
    public void testProgressReporterAsync() {
        HttpClient client = new JdkHttpClientProvider().createInstance();

        ConcurrentLinkedDeque<Long> progress = new ConcurrentLinkedDeque<>();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(SHORT_BODY.length + LONG_BODY.length))
            .setBody(Flux.just(ByteBuffer.wrap(LONG_BODY))
                .concatWith(Flux.just(ByteBuffer.wrap(SHORT_BODY))));

        Contexts contexts = Contexts.with(Context.NONE)
            .setHttpRequestProgressReporter(ProgressReporter.withProgressListener(progress::add));
        StepVerifier.create(client.send(request, contexts.getContext()))
            .expectNextCount(1)
            .expectComplete()
            .verify();

        List<Long> progressList = progress.stream().collect(Collectors.toList());
        assertEquals(LONG_BODY.length, progressList.get(0));
        assertEquals(SHORT_BODY.length + LONG_BODY.length, progressList.get(1));
    }

    @Test
    public void testProgressReporterSync() {
        HttpClient client = new JdkHttpClientProvider().createInstance();

        ConcurrentLinkedDeque<Long> progress = new ConcurrentLinkedDeque<>();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(SHORT_BODY.length + LONG_BODY.length))
            .setBody(Flux.just(ByteBuffer.wrap(LONG_BODY))
                .concatWith(Flux.just(ByteBuffer.wrap(SHORT_BODY))));

        Contexts contexts = Contexts.with(Context.NONE)
            .setHttpRequestProgressReporter(ProgressReporter.withProgressListener(progress::add));

        try (HttpResponse response = client.sendSync(request, contexts.getContext())) {
            assertEquals(200, response.getStatusCode());
            List<Long> progressList = progress.stream().collect(Collectors.toList());
            assertEquals(LONG_BODY.length, progressList.get(0));
            assertEquals(SHORT_BODY.length + LONG_BODY.length, progressList.get(1));
        }
    }

    @Test
    public void testFileUploadSync() throws IOException {
        Path tempFile = writeToTempFile(LONG_BODY);
        tempFile.toFile().deleteOnExit();
        BinaryData body = BinaryData.fromFile(tempFile, 1L, 42L);

        HttpClient client = new JdkHttpClientProvider().createInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPostWithBodyValidation"))
            .setBody(body);

        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testStreamUploadAsync() {
        HttpClient client = new JdkHttpClientProvider().createInstance();

        InputStream requestBody = new ByteArrayInputStream(LONG_BODY, 1, 42);
        BinaryData body = BinaryData.fromStream(requestBody, 42L);
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPostWithBodyValidation"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, "42")
            .setBody(body);

        StepVerifier.create(client.send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponseSync() {
        HttpClient client = new JdkHttpClientProvider().createInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, "132")
            .setBody(Flux.error(new RuntimeException("boo")));

        UncheckedIOException thrown = assertThrows(UncheckedIOException.class,
            () -> client.sendSync(request, Context.NONE));
        assertEquals("boo", thrown.getCause().getMessage());
    }

    @Test
    public void testRequestBodyEndsInErrorShouldPropagateToResponse() {
        HttpClient client = new JdkHttpClientProvider().createInstance();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)))
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
    public void testRequestBodyEndsInErrorShouldPropagateToResponseSync() {
        HttpClient client = new JdkHttpClientProvider().createInstance();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentChunk.length() * (repetitions + 1)))
            .setBody(Flux.just(contentChunk)
                .repeat(repetitions)
                .map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))
                .concatWith(Flux.error(new RuntimeException("boo"))));

        UncheckedIOException thrown = assertThrows(UncheckedIOException.class,
            () -> client.sendSync(request, Context.NONE));
        assertEquals("boo", thrown.getCause().getMessage());
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContentFlowable() {
        HttpClient client = new JdkHttpClientBuilder().build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, "/connectionClose"));

        StepVerifier.create(client.send(request).flatMap(HttpResponse::getBodyAsByteArray))
            .verifyError(IOException.class);
    }

    @Test
    public void testServerShutsDownSocketShouldPushErrorToContentSync() {
        HttpClient client = new JdkHttpClientBuilder().build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, "/connectionClose"));
        assertThrows(UncheckedIOException.class, () -> client.sendSync(request, Context.NONE));
    }

    @Test
    public void testConcurrentRequests() {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new JdkHttpClientProvider().createInstance();

        ParallelFlux<byte[]> responses = Flux.range(1, numRequests)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> doRequest(client, "/long")
                .flatMap(HttpResponse::getBodyAsByteArray));

        StepVerifier.create(responses)
            .thenConsumeWhile(response -> {
                TestUtils.assertArraysEqual(LONG_BODY, response);
                return true;
            })
            .expectComplete()
            .verify(Duration.ofSeconds(60));
    }

    @Test
    public void testConcurrentRequestsSync() throws InterruptedException {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new JdkHttpClientProvider().createInstance();

        ForkJoinPool pool = new ForkJoinPool();
        List<Callable<Void>> requests = new ArrayList<>(numRequests);
        for (int i = 0; i < numRequests; i++) {
            requests.add(() -> {
                try (HttpResponse response = doRequestSync(client, "/long")) {
                    byte[] body = response.getBodyAsBinaryData().toBytes();
                    TestUtils.assertArraysEqual(LONG_BODY, body);
                    return null;
                }
            });
        }

        pool.invokeAll(requests);
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS));
    }

    @Test
    public void testIOExceptionInWriteBodyTo() {
        HttpClient client = new JdkHttpClientProvider().createInstance();

        assertThrows(IOException.class, () -> {
            try (HttpResponse response = doRequestSync(client, "/long")) {
                response.writeBodyTo(new ThrowingWritableByteChannel());
            }
        });
    }

    private static Mono<HttpResponse> getResponse(String path) {
        HttpClient client = new JdkHttpClientBuilder().build();
        return doRequest(client, path);
    }

    private static URL url(LocalTestServer server, String path) {
        try {
            return UrlBuilder.parse(server.getHttpUri() + path).toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] createLongBody() {
        byte[] duplicateBytes = "abcdefghijk".getBytes(StandardCharsets.UTF_8);
        byte[] longBody = new byte[duplicateBytes.length * 100000];

        for (int i = 0; i < 100000; i++) {
            System.arraycopy(duplicateBytes, 0, longBody, i * duplicateBytes.length,
                duplicateBytes.length);
        }

        return longBody;
    }

    private static void checkBodyReceived(byte[] expectedBody, String path) {
        HttpClient client = new JdkHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, path).flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(bytes -> TestUtils.assertArraysEqual(expectedBody, bytes))
            .verifyComplete();
    }

    private static void checkBodyReceivedSync(byte[] expectedBody, String path) throws IOException {
        HttpClient client = new JdkHttpClientBuilder().build();
        try (HttpResponse response = doRequestSync(client, path)) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            WritableByteChannel body = Channels.newChannel(outStream);
            response.writeBodyTo(body);
            TestUtils.assertArraysEqual(expectedBody, outStream.toByteArray());
        }
    }

    private static Mono<HttpResponse> doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request);
    }

    private static HttpResponse doRequestSync(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.sendSync(request, Context.NONE);
    }

    private static Path writeToTempFile(byte[] body) throws IOException {
        Path tempFile = Files.createTempFile("data", null);
        tempFile.toFile().deleteOnExit();
        String tempFilePath = tempFile.toString();
        FileOutputStream outputStream = new FileOutputStream(tempFilePath);
        outputStream.write(body);
        outputStream.close();
        return tempFile;
    }

    private static final class ThrowingWritableByteChannel implements WritableByteChannel {
        private boolean open = true;
        int writeCount = 0;

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (writeCount++ < 3) {
                int remaining = src.remaining();
                src.position(src.position() + remaining);
                return remaining;
            } else {
                throw new IOException();
            }
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            open = false;
        }
    }
}
