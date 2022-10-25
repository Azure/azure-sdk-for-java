// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

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
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkHttpClientTests {

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
        HttpResponse response = doRequestSync(client, "/long").buffer();
        Assertions.assertArrayEquals(LONG_BODY, response.getBodyAsBinaryData().toBytes());
    }

    @Test
    public void testBufferedResponseSync() {
        HttpClient client = new JdkHttpClientBuilder().build();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, "/long"));
        HttpResponse response = client.sendSync(request, new Context("azure-eagerly-read-response", true));
        Assertions.assertArrayEquals(LONG_BODY, response.getBodyAsBinaryData().toBytes());
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
        HttpResponse response = doRequestSync(client, "/short");
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

    @Test
    @Timeout(20)
    public void testMultipleGetBinaryDataSync() {
        HttpClient client = new JdkHttpClientBuilder().build();
        HttpResponse response = doRequestSync(client, "/short");

        Assertions.assertArrayEquals(SHORT_BODY, response.getBodyAsBinaryData().toBytes());
        Assertions.assertArrayEquals(SHORT_BODY, response.getBodyAsBinaryData().toBytes());
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
        HttpResponse response = doRequestSync(client, "/error");
        assertEquals(500, response.getStatusCode());
        assertEquals("error", response.getBodyAsString().block());
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
    public void testProgressReporterAsync() {
        HttpClient client = new JdkHttpClientProvider().createInstance();

        ConcurrentLinkedDeque<Long> progress = new ConcurrentLinkedDeque<>();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", String.valueOf(SHORT_BODY.length + LONG_BODY.length))
            .setBody(Flux.just(ByteBuffer.wrap(LONG_BODY))
                .concatWith(Flux.just(ByteBuffer.wrap(SHORT_BODY))));

        Contexts contexts = Contexts.with(Context.NONE).setHttpRequestProgressReporter(ProgressReporter.withProgressListener(p -> progress.add(p)));
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
            .setHeader("Content-Length", String.valueOf(SHORT_BODY.length + LONG_BODY.length))
            .setBody(Flux.just(ByteBuffer.wrap(LONG_BODY))
                .concatWith(Flux.just(ByteBuffer.wrap(SHORT_BODY))));

        Contexts contexts = Contexts.with(Context.NONE).setHttpRequestProgressReporter(ProgressReporter.withProgressListener(p -> progress.add(p)));
        HttpResponse response = client.sendSync(request, contexts.getContext());
        assertEquals(200, response.getStatusCode());
        List<Long> progressList = progress.stream().collect(Collectors.toList());
        assertEquals(LONG_BODY.length, progressList.get(0));
        assertEquals(SHORT_BODY.length + LONG_BODY.length, progressList.get(1));
    }

    @Test
    public void testFileUploadSync() throws IOException {
        WireMockServer local = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort()
            .maxRequestJournalEntries(1)
            .gzipDisabled(true));

        local.stubFor(post("/shortPost").willReturn(aResponse().withStatus(200).withBody(SHORT_BODY)));
        local.start();

        Path tempFile = writeToTempFile(LONG_BODY);
        tempFile.toFile().deleteOnExit();
        BinaryData body = BinaryData.fromFile(tempFile, 1L, 42L);

        HttpClient client = new JdkHttpClientProvider().createInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(local, "/shortPost"))
            .setBody(body);

        HttpResponse response = client.sendSync(request, Context.NONE);
        assertEquals(200, response.getStatusCode());

        local.verify(postRequestedFor(urlEqualTo("/shortPost")).withRequestBody(binaryEqualTo(body.toBytes())));
        local.shutdown();
    }

    @Test
    public void testStreamUploadAsync() throws IOException {
        WireMockServer local = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort()
            .maxRequestJournalEntries(1)
            .gzipDisabled(true));

        local.stubFor(post("/post").willReturn(aResponse().withStatus(200).withBody(SHORT_BODY)));
        local.start();

        HttpClient client = new JdkHttpClientProvider().createInstance();

        InputStream requestBody = new ByteArrayInputStream(SHORT_BODY);
        BinaryData body = BinaryData.fromStream(requestBody);
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(local, "/post"))
            .setHeader("Content-Length", String.valueOf(SHORT_BODY.length))
            .setBody(body);

        StepVerifier.create(client.send(request))
            .consumeNextWith(r -> {
                assertEquals(200, r.getStatusCode());
                local.verify(postRequestedFor(urlEqualTo("/post")).withRequestBody(binaryEqualTo(SHORT_BODY)));
            })
            .expectComplete()
            .verify();

        local.shutdown();
    }

    @Test
    public void testRequestBodyIsErrorShouldPropagateToResponseSync() {
        HttpClient client = new JdkHttpClientProvider().createInstance();
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", "123")
            .setBody(Flux.error(new RuntimeException("boo")));

        UncheckedIOException thrown = assertThrows(UncheckedIOException.class, () -> client.sendSync(request, Context.NONE));
        assertEquals("boo", thrown.getCause().getMessage());
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
    public void testRequestBodyEndsInErrorShouldPropagateToResponseSync() {
        HttpClient client = new JdkHttpClientProvider().createInstance();
        String contentChunk = "abcdefgh";
        int repetitions = 1000;
        HttpRequest request = new HttpRequest(HttpMethod.POST, url(server, "/shortPost"))
            .setHeader("Content-Length", String.valueOf(contentChunk.length() * (repetitions + 1)))
            .setBody(Flux.just(contentChunk)
                .repeat(repetitions)
                .map(s -> ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)))
                .concatWith(Flux.error(new RuntimeException("boo"))));

        UncheckedIOException thrown = assertThrows(UncheckedIOException.class, () -> client.sendSync(request, Context.NONE));
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

        Mono<Long> numBytesMono = Flux.range(1, numRequests)
            .parallel(25)
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> doRequest(client, "/long")
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
    public void testConcurrentRequestsSync() {
        int numRequests = 100; // 100 = 1GB of data read
        HttpClient client = new JdkHttpClientProvider().createInstance();

        Mono<Long> numBytesMono = Flux.range(1, numRequests)
            .parallel(25)
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> {
                HttpResponse response = doRequestSync(client, "/long");
                byte[] body = response.getBodyAsBinaryData().toBytes();
                assertArrayEquals(LONG_BODY, body);
                return Flux.just((long) body.length);
            })
            .sequential()
            .reduce(0L, Long::sum);

        StepVerifier.create(numBytesMono)
            .expectNext((long) numRequests * LONG_BODY.length)
            .expectComplete()
            .verify(Duration.ofSeconds(60));
    }

    private Mono<HttpResponse> getResponse(String path) {
        HttpClient client = new JdkHttpClientBuilder().build();
        return doRequest(client, path);
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
        HttpClient client = new JdkHttpClientBuilder().build();
        StepVerifier.create(doRequest(client, path).flatMap(HttpResponse::getBodyAsByteArray))
            .assertNext(bytes -> Assertions.assertArrayEquals(expectedBody, bytes))
            .verifyComplete();
    }

    private void checkBodyReceivedSync(byte[] expectedBody, String path) throws IOException {
        HttpClient client = new JdkHttpClientBuilder().build();
        HttpResponse response = doRequestSync(client, path);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        WritableByteChannel body = Channels.newChannel(outStream);
        response.writeBodyTo(body);
        Assertions.assertArrayEquals(expectedBody, outStream.toByteArray());
    }

    private Mono<HttpResponse> doRequest(HttpClient client, String path) {
        HttpRequest request = new HttpRequest(HttpMethod.GET, url(server, path));
        return client.send(request);
    }

    private HttpResponse doRequestSync(HttpClient client, String path) {
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
}
